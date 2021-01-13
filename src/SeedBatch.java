
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

///////////////////////////////////////////////////////////////////////////
//a seed 
//
//
@SuppressWarnings("serial")
class Seed implements Serializable{
	String batchName;
	PVector docPoint;
	
	// the depth is set to the normalised depth in the 3D scene, this is done by the 
	// PointGenerator.getNextSeed() method
	// 
	float depth;
	
	
	int id;
	int layerNumber = 0;
	
	ImageSampleDescription contentItemDescriptor;
	
	public Seed(PVector p, ImageSampleDescription cis) {
		docPoint = p;
		contentItemDescriptor = cis;
	}
	
	public Seed(PVector docpt) {
		docPoint = docpt;
		
	}
	
	float getDepth() {
		return depth;
	}
	
	String toStr() {
		
		return "seed " + id + " batchname:" + batchName + " docPoint:" + docPoint.toStr() + " content Item: " + contentItemDescriptor.toStr();
	}
	
}





///////////////////////////////////////////////////////////////////////////
// a SeedBatch takes a PointGenerator and a ContentItemSelector
// and makes a number of seeds with it
// These can then be added to the SeedRenderManager
// Normally, the use does not have to explicitly create SeedBatches, but interfaces directly wit the SeedBatchManager
public class SeedBatch{
	String batchName = "";
	ImageSampleSelector contentItemSelector;
	//PointGenerator pointGenerator;
	boolean isVisible = true;
	ArrayList<Seed> seeds = new ArrayList<Seed>();
	int uniqueSeedIDCounter = 0;
	
	SeedBatch(String name){
		batchName = name;
	}
	
	@SuppressWarnings("unchecked")
	void loadSeeds(String path) {
		String seedsDirectoryPath = path + "seeds\\";
		String pathandname = seedsDirectoryPath + batchName + ".sds";
		System.out.println("loading seed layer " + pathandname);
		seeds = (ArrayList<Seed>)(SerializableFile.load(pathandname));
		if(seeds==null) {
			System.out.println("load failed, does file exist?");
		}else {
			System.out.println("number seed loaded " + seeds.size());
		}
	}
	
	ArrayList<Seed> generateSeeds(ImageSampleSelector cc, PointGenerator pg){
		contentItemSelector = cc;
		PointGenerator pointGenerator = pg;
		if(pointGenerator.getNumItems()==0) {
			pointGenerator.generatePoints();
		}
		while(pointGenerator.areItemsRemaining()) {
			Seed seed = pointGenerator.getNextSeed();
			seed.batchName = this.batchName;
			seed.contentItemDescriptor = contentItemSelector.selectImageSampleDescription(seed.docPoint);
			seed.id = uniqueSeedIDCounter++;
			seeds.add(seed);
		}
		return seeds;
	}
	
	boolean nameEquals(String n) {
		 return n.contentEquals(batchName);
	}
	
	ArrayList<Seed> getSeeds(){
		return seeds;
	}
	
	void setVisible(boolean vis) {
		isVisible = vis;
	}
	
	boolean isVisible() {
		return isVisible;
	}
	
	ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(Seed s : seeds) {
			points.add(s.docPoint);
		}
		return points;
	}
	
	void saveSeeds(String path) {
		// there should be a directory in the project folder called seeds
		ensureSeedsDirectoryExists(path);
		String pathandname = path + "seeds\\" + batchName + ".sds";
		SerializableFile.save(pathandname, seeds);
	}
	
	void ensureSeedsDirectoryExists(String path) {
		String alledgedDirectory = path + "seeds";
		if(MOUtils.checkDirectoryExist(alledgedDirectory)) return;
		MOUtils.createDirectory(alledgedDirectory);
	}

}

///////////////////////////////////////////////////////////////////////////
//SeedBatchManager
//
//
class SeedBatchManager {
	// important classes necessary for independently generating new seed layers
	Surface theSurface;
	ImageSampleGroupManager contentManager;
	SceneData3D sceneData3D;

	//
	ArrayList<SeedBatch> seedBatches = new ArrayList<SeedBatch>();
	Range depthConstraintRange = new Range();

	// collated seeds are those seeds visible for a render
	ArrayList<Seed> collatedSeeds = new ArrayList<Seed>();
	

	SeedBatchManager(Surface srf, ImageSampleGroupManager cgm, SceneData3D sd3d) {
		theSurface = srf;
		contentManager = cgm;
		sceneData3D = sd3d;
	}

	void addSeedBatch(SeedBatch sl) {
		seedBatches.add(sl);
	}

	ArrayList<Seed> getCollatedSeeds(){
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
		if(batch == null) return;
		ArrayList<PVector> points = batch.getPoints();
		theSurface.theDocument.drawPoints(points, c);
	}


	void collateSeedBatches() {
		collatedSeeds.clear();
		for (SeedBatch sl : seedBatches) {
				collatedSeeds.addAll(sl.getSeeds());
			//System.out.println("SeedBatchManager has collated " + collatedSeeds.size());
		}
		if (collatedSeeds.size() == 0) {
			System.out.println("SeedLayerManager has collated NO seeds");
		}
		
		updateSeedDepthsAgainstScene();
		
		depthSort();
	}
	
	void updateSeedDepthsAgainstScene() {
		// call this if you are changing to a different depth filter
		// TBD: ideally checks to see if the filter has changed,
		for(Seed s : collatedSeeds) {
			
			float d = sceneData3D.getDepthNormalised(s.docPoint);
			s.depth = d;
		}
	}

	void depthSort() {
		collatedSeeds.sort(Comparator.comparing(Seed::getDepth).reversed());
	}


	ArrayList<PVector> getPoints() {
		ArrayList<PVector> points = new ArrayList<PVector>();
		for (Seed s : collatedSeeds) {
			points.add(s.docPoint);

		}
		return points;
	}

	

	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////
	// SeedBatch generation methods. The do rely on the globals
	// contentManager, seedBatchManager, and scenedat3d 
	//
	void createSeedBatch(Boolean makeNewSeeds, String batchName, ImageSampleSelector cis,
			String namePointDisImage, float pointDisRLo, float pointDisRHi, float pointDisThreshold,
			int pointDistRSeed) {
		
		SeedBatch seedBatchBiome1 = new SeedBatch(batchName);

		if (makeNewSeeds) {
			//PointGenerator_RadialPack pointField = getPointGenerator(namePointDisImage, pointDisRLo, pointDisRHi,
			//		pointDisThreshold, pointDistRSeed);
			
			PointGenerator_RadialPack3D pointField = getPointGenerator3D(namePointDisImage, pointDisRLo, pointDisRHi,
					pointDisThreshold, pointDistRSeed);

			seedBatchBiome1.generateSeeds(cis, pointField);
			seedBatchBiome1.saveSeeds(theSurface.getUserSessionPath());

		} else {

			seedBatchBiome1.loadSeeds(theSurface.getUserSessionPath());

		}

		addSeedBatch(seedBatchBiome1);
		
	}
	
	
	void createSeedBatch(Boolean makeNewSeeds, String batchName, String[] contentGroupNames, float[] contentGroupProbs,
			int cisRSeed, String namePointDisImage, float pointDisRLo, float pointDisRHi, float pointDisThreshold,
			int pointDistRSeed) {
		
		ImageSampleSelector cis = new ImageSampleSelector(contentManager, cisRSeed);
		int numContentGroups = contentGroupNames.length;
		for (int n = 0; n < numContentGroups; n++) {
			cis.addContentItemProbability(contentGroupNames[n], contentGroupProbs[n]);
			}
		
		createSeedBatch( makeNewSeeds,  batchName,  cis, namePointDisImage,  pointDisRLo,  pointDisRHi,  pointDisThreshold, pointDistRSeed);
	}

	/**
	 * @brief 
	 * @param makeNewSeeds
	 * @param batchName
	 * @param contentGroupName
	 * @param cisRSeed
	 * @param namePointDisImage
	 * @param pointDisRLo
	 * @param pointDisRHi
	 * @param pointDisThreshold
	 * @param pointDistRSeed
	 */
	void createSeedBatch(Boolean makeNewSeeds, String batchName, String contentGroupName, int cisRSeed,
			String namePointDisImage, float pointDisRLo, float pointDisRHi, float pointDisThreshold,
			int pointDistRSeed) {
		
		ImageSampleSelector cis = new ImageSampleSelector(contentManager, cisRSeed);
		cis.addContentItemProbability(contentGroupName, 1);
		createSeedBatch( makeNewSeeds,  batchName,  cis, namePointDisImage,  pointDisRLo,  pointDisRHi,  pointDisThreshold, pointDistRSeed);
	}

	PointGenerator_RadialPack3D getPointGenerator(String namePointDisImage, float pointDisRLo, float pointDisRHi,
			float pointDisThreshold, int pointDistRSeed) {
		sceneData3D.setCurrentRenderImage(namePointDisImage);
		BufferedImage pointDistributionImage = sceneData3D.getCurrentRenderImage();
		PointGenerator_RadialPack3D pointField = new PointGenerator_RadialPack3D(pointDistRSeed, sceneData3D); 

		pointField.setMaskImage(sceneData3D.getSubstanceMaskImage());
		pointField.setPackingRadius(pointDisRLo, pointDisRHi, pointDisThreshold, pointDistributionImage);
		return pointField;
	}
	
	
	PointGenerator_RadialPack3D getPointGenerator3D(String namePointDisImage, float pointDisRLo, float pointDisRHi,
			float pointDisThreshold, int pointDistRSeed) {
		sceneData3D.setCurrentRenderImage(namePointDisImage);
		BufferedImage pointDistributionImage = sceneData3D.getCurrentRenderImage();
		PointGenerator_RadialPack3D pointField = new PointGenerator_RadialPack3D(pointDistRSeed, sceneData3D); // rseed, doc aspect

		pointField.setMaskImage(sceneData3D.getSubstanceMaskImage());
		pointField.setPackingRadius(pointDisRLo, pointDisRHi, pointDisThreshold, pointDistributionImage);
		return pointField;
	}

////////////////////////////////////////////////////////////////////////////////

}
















