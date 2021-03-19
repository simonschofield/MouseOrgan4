import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

///////////////////////////////////////////////////////////////////////////
//SeedBatchManager
//
//
class SeedBatchManager  extends CollectionIterator{
	// important classes necessary for independently generating new seed layers

	ImageSampleGroupManager contentManager;
	SceneData3D sceneData3D;

	//
	ArrayList<SeedBatch> seedBatches = new ArrayList<SeedBatch>();
	Range depthConstraintRange = new Range();

	// collated seeds are those seeds visible for a render
	ArrayList<Seed> collatedSeeds = new ArrayList<Seed>();

	SeedBatchManager() {

	}

	SeedBatchManager(ImageSampleGroupManager cgm, SceneData3D sd3d) {

		contentManager = cgm;
		sceneData3D = sd3d;
	}

	void addSeedBatch(SeedBatch sl) {
		seedBatches.add(sl);
	}
	
	int getNumSeedBatches() {
		return seedBatches.size();
	}

	ArrayList<Seed> getCollatedSeeds() {
		collateSeedBatches();
		return collatedSeeds;
	}

	SeedBatch getSeedBatch(String name) {
		for (SeedBatch sl : seedBatches) {
			if (sl.nameEquals(name))
				return sl;
		}
		System.out.println("SeedBatchManager:getSeedBatch , cannot find batch " + name);
		return null;
	}

	void drawSeedBatchPoints(String name, Color c) {
		SeedBatch batch = getSeedBatch(name);
		if (batch == null)
			return;
		ArrayList<PVector> points = batch.getPoints();
		GlobalObjects.theDocument.drawPoints(points, c);
	}

	void collateSeedBatches() {
		collatedSeeds.clear();
		for (SeedBatch sl : seedBatches) {
			collatedSeeds.addAll(sl.getSeeds());
			// System.out.println("SeedBatchManager has collated " + collatedSeeds.size());
		}
		if (collatedSeeds.size() == 0) {
			System.out.println("SeedLayerManager has collated NO seeds");
		}

		updateSeedDepthsAgainstScene();

		depthSort();
	}

	void updateSeedDepthsAgainstScene() {
		if(sceneData3D == null) return;
		// call this if you are changing to a different depth filter
		// TBD: ideally checks to see if the filter has changed,
		for (Seed s : collatedSeeds) {

			float d = sceneData3D.getDepthNormalised(s.getDocPoint());
			s.setDepth(d);
		}
	}

	void depthSort() {
		collatedSeeds.sort(Comparator.comparing(Seed::getDepth).reversed());
	}

	ArrayList<PVector> getPoints() {
		ArrayList<PVector> points = new ArrayList<PVector>();
		for (Seed s : collatedSeeds) {
			points.add(s.getDocPoint());

		}
		return points;
	}

	////////////////////////////////////////////////////////////////////////////////
	// Creating seed batches from 3D SceneData
	//
	//
	// using a pre-made ImageSampleSelector
	void createSeedBatch(Boolean makeNewSeeds, String batchName, ImageSampleSelector cis, String namePointDisImage,
			PackingInterpolationScheme is, int pointDistRSeed) {

		SeedBatch seedBatchBiome1 = new SeedBatch(batchName);

		if (makeNewSeeds) {
			PointGenerator_RadialPackSurface3D pointField = getPointGenerator3D(namePointDisImage, is, pointDistRSeed);
			seedBatchBiome1.generateSeeds(cis, pointField);
			seedBatchBiome1.saveSeeds(GlobalObjects.theSurface.getUserSessionPath());

		} else {
			seedBatchBiome1.loadSeeds(GlobalObjects.theSurface.getUserSessionPath());
		}

		addSeedBatch(seedBatchBiome1);

	}

	// using a list of imageSampleGroup and associated probabilities
	void createSeedBatch(Boolean makeNewSeeds, String batchName, String[] contentGroupNames, float[] contentGroupProbs,
			int cisRSeed, String namePointDisImage, PackingInterpolationScheme is, int pointDistRSeed) {

		ImageSampleSelector cis = new ImageSampleSelector(contentManager, cisRSeed);
		int numContentGroups = contentGroupNames.length;
		for (int n = 0; n < numContentGroups; n++) {
			cis.addContentItemProbability(contentGroupNames[n], contentGroupProbs[n]);
		}

		createSeedBatch(makeNewSeeds, batchName, cis, namePointDisImage, is, pointDistRSeed);
	}

	// for a single imageSampleGroup
	void createSeedBatch(Boolean makeNewSeeds, String batchName, String contentGroupName, int cisRSeed,
			String namePointDisImage, PackingInterpolationScheme is, int pointDistRSeed) {

		ImageSampleSelector cis = new ImageSampleSelector(contentManager, cisRSeed);
		cis.addContentItemProbability(contentGroupName, 1);
		createSeedBatch(makeNewSeeds, batchName, cis, namePointDisImage, is, pointDistRSeed);
	}

	PointGenerator_RadialPackSurface3D getPointGenerator3D(String namePointDisImage, PackingInterpolationScheme is, int pointDistRSeed) {
		sceneData3D.setCurrentRenderImage(namePointDisImage);
		BufferedImage pointDistributionImage = sceneData3D.getCurrentRenderImage();
		PointGenerator_RadialPackSurface3D pointField = new PointGenerator_RadialPackSurface3D(pointDistRSeed, sceneData3D);

		pointField.setMaskImage(sceneData3D.getSubstanceMaskImage());
		pointField.setPackingRadius(is, pointDistributionImage);
		return pointField;
	}
	
	
	Seed getNextSeed() {
		return (Seed) getNextItem();
	}

	@Override
	int getNumItems() {
		// TODO Auto-generated method stub
		return collatedSeeds.size();
	}

	@Override
	Object getItem(int n) {
		// TODO Auto-generated method stub
		return collatedSeeds.get(n);
	}

	////////////////////////////////////////////////////////////////////////////////

}
