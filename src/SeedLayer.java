
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

///////////////////////////////////////////////////////////////////////////
// a seed factory takes a PointGenerator and a ContentItemSelector
// and makes a number of seeds with it
// These can then be added to the SeedStore
public class SeedLayer{
	String layerName = "";
	ContentItemSelector contentItemSelector;
	PointGenerator pointGenerator;
	boolean isVisible = true;
	ArrayList<Seed> seeds = new ArrayList<Seed>();
	
	SeedLayer(String name){
		layerName = name;
	}
	
	@SuppressWarnings("unchecked")
	void loadSeeds(String path) {
		String seedsDirectoryPath = path + "seeds\\";
		String pathandname = seedsDirectoryPath + layerName + ".sds";
		System.out.println("loading seed layer " + pathandname);
		seeds = (ArrayList<Seed>)(SerializableFile.load(pathandname));
		if(seeds==null) {
			System.out.println("load failed, does file exist?");
		}else {
			System.out.println("number seed loaded " + seeds.size());
		}
	}
	
	ArrayList<Seed> generateSeeds(ContentItemSelector cc, PointGenerator pg){
		contentItemSelector = cc;
		pointGenerator = pg;
		if(pointGenerator.getNumItems()==0) {
			pointGenerator.generatePoints();
		}
		while(pointGenerator.areItemsRemaining()) {
			Seed seed = pointGenerator.getNextSeed();
			seed.layerName = this.layerName;
			seed.contentItemDescriptor = contentItemSelector.selectContentItemDescription();
			seeds.add(seed);
		}
		return seeds;
	}
	
	boolean nameEquals(String n) {
		 return n.contentEquals(layerName);
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
		String pathandname = path + "seeds\\" + layerName + ".sds";
		SerializableFile.save(pathandname, seeds);
	}
	
	void ensureSeedsDirectoryExists(String path) {
		String alledgedDirectory = path + "seeds";
		if(MOUtils.checkDirectoryExist(alledgedDirectory)) return;
		MOUtils.createDirectory(alledgedDirectory);
	}

}


///////////////////////////////////////////////////////////////////////////
// SeedLayerManager
//
//
class SeedLayerManager extends CollectionIterator {
	// important classes necessary for independently generating new seed layers
	Surface theSurface;
	ContentGroupManager contentManager;
	SceneData3D sceneData3D;
	
	//
	ArrayList<SeedLayer> seedLayers = new ArrayList<SeedLayer>();
	Range depthConstraintRange = new Range();
	
	// collated seeds are those seeds visible for a render
	ArrayList<Seed> collatedSeeds = new ArrayList<Seed>();
	Range collatedSeedsDepthExtrema = new Range();
	
	SeedLayerManager(Surface srf, ContentGroupManager cgm, SceneData3D sd3d){
		theSurface = srf;
		contentManager = cgm;
		sceneData3D = sd3d;
	}
	
	
	 
	
	
	void addSeedLayer(SeedLayer sl) {
		seedLayers.add(sl);
	}
	
	void setSeedLayerVisible(String name, boolean vis) {
		SeedLayer sl = getSeedLayer(name);
		sl.setVisible(vis);
	}
	
	boolean isSeedLayerVisible(String name) {
		SeedLayer sl = getSeedLayer(name);
		return sl.isVisible();
	}
	
	void setDepthFilter(float lo, float hi) {
		depthConstraintRange.limit1 = lo;
		depthConstraintRange.limit2 = hi;
		
	}
	
	SeedLayer getSeedLayer(String name) {
		for(SeedLayer sl: seedLayers) {
			if(sl.nameEquals(name)) return sl;
		}
		System.out.println("SeedLayerManager:getSeedLayer , cannot find layer " + name);
		return null;
	}
	
	
	
	void drawSeedLayerPoints(String name, Color c) {
		theSurface.theDocument.drawPoints(getSeedLayer(name).getPoints(), c);
	}
	
	void prepareForRender() {
		collateLayers();
		depthSort();
		filterOnDepth();
		if(collatedSeeds.size()==0) {
			System.out.println("SeedLayerManager has collated NO seeds");
		}
	}
	
	void collateLayers() {
		collatedSeeds.clear();
		for(SeedLayer sl: seedLayers) {
			if(sl.isVisible()) {
				System.out.println("SeedLayerManager adding layer " + sl.layerName + " num seeds " + sl.getSeeds().size());
				collatedSeeds.addAll(sl.getSeeds());
			}
			System.out.println("SeedLayerManager has collated " + collatedSeeds.size());
		}
		
		
	}
	
	void depthSort() {
		collatedSeeds.sort(Comparator.comparing(Seed::getDepth).reversed());
		updateCollatedSeedDepthExtrema();
		System.out.println("collated seed extrema before z filer" + collatedSeedsDepthExtrema.toStr());
	}
	
	void filterOnDepth() {
		ArrayList<Seed> tempList = new ArrayList<Seed>();
		for(Seed s : collatedSeeds) {
			float d = s.getDepth();
			if( depthConstraintRange.isBetweenInc(d) ) tempList.add(s);
			
		}
		collatedSeeds = tempList;
		updateCollatedSeedDepthExtrema();
		System.out.println("collated seed extrema after z filer" + collatedSeedsDepthExtrema.toStr());
	}
	
	
	void updateCollatedSeedDepthExtrema() {
		int n = collatedSeeds.size();
		if(n==0) return;
		collatedSeedsDepthExtrema.limit2 = collatedSeeds.get(0).getDepth();
		collatedSeedsDepthExtrema.limit1 = collatedSeeds.get(n-1).getDepth();
		
		
	}
	
	ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(Seed s : collatedSeeds) {
			points.add(s.docPoint);
			
		}
		return points;
	}
	
	
	
	Seed getNextItem() {
		return (Seed)(super.getNextItem());
	}


	@Override
	int getNumItems() {
		// TODO Auto-generated method stub
		return collatedSeeds.size();
	}


	@Override
	Seed getItem(int n) {
		// TODO Auto-generated method stub
		return collatedSeeds.get(n);
	}
	
	////////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////
	// SeedLayer generation methods. The do rely on the globals
	// contentManager, seedlayerManager, and scenedat3d so maybe should be part o
	//
	void createSeedLayer(Boolean makeNewSeeds, String layerName, String[] contentGroupNames, float[] contentGroupProbs,
			int cisRSeed, String namePointDisImage, float pointDisRLo, float pointDisRHi, float pointDisThreshold,
			int pointDistRSeed) {

		SeedLayer seedLayerBiome1 = new SeedLayer(layerName);

		if (makeNewSeeds) {

			ContentItemSelector cis = new ContentItemSelector(contentManager, cisRSeed);
			int numContentGroups = contentGroupNames.length;
			for (int n = 0; n < numContentGroups; n++) {
				cis.addContentItemProbability(contentGroupNames[n], contentGroupProbs[n]);
			}

			PointGenerator_RadialPack pointField = getPointGenerator(namePointDisImage, pointDisRLo, pointDisRHi,
					pointDisThreshold, pointDistRSeed);

			seedLayerBiome1.generateSeeds(cis, pointField);
			seedLayerBiome1.saveSeeds(theSurface.getUserSessionPath());

		} else {

			seedLayerBiome1.loadSeeds(theSurface.getUserSessionPath());

		}

		addSeedLayer(seedLayerBiome1);
	}

	void createSeedLayer(Boolean makeNewSeeds, String layerName, String contentGroupName, int cisRSeed,
			String namePointDisImage, float pointDisRLo, float pointDisRHi, float pointDisThreshold,
			int pointDistRSeed) {

		SeedLayer seedLayerBiome1 = new SeedLayer(layerName);

		if (makeNewSeeds) {

			ContentItemSelector cis = new ContentItemSelector(contentManager, cisRSeed);

			cis.addContentItemProbability(contentGroupName, 1);

			PointGenerator_RadialPack pointField = getPointGenerator(namePointDisImage, pointDisRLo, pointDisRHi,
					pointDisThreshold, pointDistRSeed);

			seedLayerBiome1.generateSeeds(cis, pointField);
			seedLayerBiome1.saveSeeds(theSurface.getUserSessionPath());

		} else {

			seedLayerBiome1.loadSeeds(theSurface.getUserSessionPath());

		}

		addSeedLayer(seedLayerBiome1);
	}

	PointGenerator_RadialPack getPointGenerator(String namePointDisImage, float pointDisRLo, float pointDisRHi,
			float pointDisThreshold, int pointDistRSeed) {
		sceneData3D.setCurrentRenderImage(namePointDisImage);
		BufferedImage pointDistributionImage = sceneData3D.getCurrentRenderImage();
		PointGenerator_RadialPack pointField = new PointGenerator_RadialPack(pointDistRSeed, theSurface.theDocument.getDocumentAspect(), sceneData3D); // rseed, doc aspect

		pointField.setMaskImage(sceneData3D.getSubstanceMaskImage());
		pointField.setPackingRadius(pointDisRLo, pointDisRHi, pointDisThreshold, pointDistributionImage);
		return pointField;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////
	
}




@SuppressWarnings("serial")
class Seed implements Serializable{
	String layerName;
	PVector docPoint;
	float depth;
	
	
	ContentItemDescription contentItemDescriptor;
	
	public Seed(PVector p, ContentItemDescription cis) {
		docPoint = p;
		contentItemDescriptor = cis;
	}
	
	public Seed(PVector docpt) {
		docPoint = docpt;
		
	}
	
	float getDepth() {
		return depth;
	}
	
}










