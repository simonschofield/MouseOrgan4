
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

///////////////////////////////////////////////////////////////////////////
// a seed factory takes a PointGenerator and a ContentItemSelector
// and makes a number of seeds with it
// These can then be added to the SeedStore
public class SeedBatch{
	String batchName = "";
	ContentItemSelector contentItemSelector;
	PointGenerator pointGenerator;
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
	
	ArrayList<Seed> generateSeeds(ContentItemSelector cc, PointGenerator pg){
		contentItemSelector = cc;
		pointGenerator = pg;
		if(pointGenerator.getNumItems()==0) {
			pointGenerator.generatePoints();
		}
		while(pointGenerator.areItemsRemaining()) {
			Seed seed = pointGenerator.getNextSeed();
			seed.batchName = this.batchName;
			seed.contentItemDescriptor = contentItemSelector.selectContentItemDescription(seed.docPoint);
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
	ContentGroupManager contentManager;
	SceneData3D sceneData3D;

	//
	ArrayList<SeedBatch> seedBatches = new ArrayList<SeedBatch>();
	Range depthConstraintRange = new Range();

	// collated seeds are those seeds visible for a render
	ArrayList<Seed> collatedSeeds = new ArrayList<Seed>();
	

	SeedBatchManager(Surface srf, ContentGroupManager cgm, SceneData3D sd3d) {
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
		theSurface.theDocument.drawPoints(getSeedBatch(name).getPoints(), c);
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
		depthSort();
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
	void createSeedBatch(Boolean makeNewSeeds, String batchName, ContentItemSelector cis,
			String namePointDisImage, float pointDisRLo, float pointDisRHi, float pointDisThreshold,
			int pointDistRSeed) {
		
		SeedBatch seedBatchBiome1 = new SeedBatch(batchName);

		if (makeNewSeeds) {
			PointGenerator_RadialPack pointField = getPointGenerator(namePointDisImage, pointDisRLo, pointDisRHi,
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
		
		ContentItemSelector cis = new ContentItemSelector(contentManager, cisRSeed);
		int numContentGroups = contentGroupNames.length;
		for (int n = 0; n < numContentGroups; n++) {
			cis.addContentItemProbability(contentGroupNames[n], contentGroupProbs[n]);
			}
		
		createSeedBatch( makeNewSeeds,  batchName,  cis, namePointDisImage,  pointDisRLo,  pointDisRHi,  pointDisThreshold, pointDistRSeed);
	}

	void createSeedBatch(Boolean makeNewSeeds, String batchName, String contentGroupName, int cisRSeed,
			String namePointDisImage, float pointDisRLo, float pointDisRHi, float pointDisThreshold,
			int pointDistRSeed) {
		
		ContentItemSelector cis = new ContentItemSelector(contentManager, cisRSeed);
		cis.addContentItemProbability(contentGroupName, 1);
		createSeedBatch( makeNewSeeds,  batchName,  cis, namePointDisImage,  pointDisRLo,  pointDisRHi,  pointDisThreshold, pointDistRSeed);
	}

	PointGenerator_RadialPack getPointGenerator(String namePointDisImage, float pointDisRLo, float pointDisRHi,
			float pointDisThreshold, int pointDistRSeed) {
		sceneData3D.setCurrentRenderImage(namePointDisImage);
		BufferedImage pointDistributionImage = sceneData3D.getCurrentRenderImage();
		PointGenerator_RadialPack pointField = new PointGenerator_RadialPack(pointDistRSeed,
				theSurface.theDocument.getDocumentAspect(), sceneData3D); // rseed, doc aspect

		pointField.setMaskImage(sceneData3D.getSubstanceMaskImage());
		pointField.setPackingRadius(pointDisRLo, pointDisRHi, pointDisThreshold, pointDistributionImage);
		return pointField;
	}

////////////////////////////////////////////////////////////////////////////////

}



////////////////////////////////////////////////////////////////////////////////
// Takes a seedbatchmanager, with all its collated seeds
// and farms the seeds out for render in the updateUserSession loop.
// 



class SeedRenderManager extends CollectionIterator{
	Surface theSurface;
	BufferedImage layerImage;
	ImageHistogram layerImageHistogram;
	
	CoordinateSpaceConverter coordinateSpaceConverter;
	ArrayList<Seed> collatedSeeds = new ArrayList<Seed>();
	ArrayList<Seed> currentLayerSeeds = new ArrayList<Seed>();
	int currentLayerNum = 0;
	int[] layerSwitches;
	boolean isFinishedAllLayersFlag = false;
	
	String layerRenderDirectory = "";
	
	SeedRenderManager(SeedBatchManager seedBatchManager){
		
		theSurface = seedBatchManager.theSurface;
		
		collatedSeeds = seedBatchManager.getCollatedSeeds();
		gatherSeedLayer(0);
	}
	
	void createRenderLayers(BufferedImage layerImg, int numLayers) {
		// this analyses the image to establish the different layers and 
		// ascribes to each seed the layer.
		layerImage = ImageProcessing.copyImage(layerImg);
		coordinateSpaceConverter = new CoordinateSpaceConverter(layerImage.getWidth(),layerImage.getHeight(), theSurface.theDocument.getDocumentAspect());
		layerImageHistogram = new ImageHistogram(layerImage);
		layerImageHistogram.createBandsOnPopularity(numLayers);
		
		for(Seed seed: collatedSeeds) {
			PVector docPt = seed.docPoint;
			seed.layerNumber = getLayerFromLayerImage(docPt);
			//
		}
		System.out.println("createRenderLayers num layers = " + getNumberOfLayers());
		gatherSeedLayer(0);
	}
	
	int getNumberOfLayers() {
		if(layerImageHistogram == null) return 1;
		return layerImageHistogram.getNumberOfBands();
		
	}
	
	int getLayerFromLayerImage(PVector docPt) {
		PVector coord = coordinateSpaceConverter.docSpaceToImageCoord(docPt);
		int packedCol = layerImage.getRGB((int)coord.x, (int)coord.y);
		int[] unpacked = new int[4];
		ImageProcessing.unpackARGB( packedCol, unpacked);
		int val = unpacked[1];
		return layerImageHistogram.getBandNum(val);
	}
	
	void gatherSeedLayer(int n) {
		currentLayerSeeds.clear();
		for(Seed s : collatedSeeds) {
			
			if(s.layerNumber == n) currentLayerSeeds.add(s);
			
		}
		
		currentLayerSeeds.sort(Comparator.comparing(Seed::getDepth).reversed());
		currentLayerNum = n;
		
		System.out.println("gatherSeedLayer layer num " + currentLayerNum + " num seeds " + currentLayerSeeds.size());
		System.out.println("from a total of " + collatedSeeds.size());
	}
	
	
	
	Seed getNextItem() {
		if(isFinishedAllLayersFlag) return null;
		
		Seed thisSeed = (Seed) super.getNextItem();
		
		if( finishedLayer() ) {
			System.out.println("finished layer " + currentLayerNum);
			boolean moreLayers = advanceCurrentLayer();
			System.out.println("more layers? " + moreLayers);
			System.out.println("next layer " + currentLayerNum);
			if(moreLayers) {
				resetItemIterator();
			} else {
				isFinishedAllLayersFlag = true;
			}
		}
		
		return thisSeed;
	}
	
	boolean finishedAllLayers(){
		return  isFinishedAllLayersFlag;
	
	}
	
	boolean finishedLayer() {
		return isFinished();
		
	}

	boolean advanceCurrentLayer() {
		// save current render,
		saveLayerRender();
		
		currentLayerNum++;
		if(currentLayerNum >= getNumberOfLayers()) return false;
		
		// if more layers to do....
		// clear current render,
		Color emptyPixel = new Color(0,0,0,0);
		theSurface.theDocument.fillBackground(emptyPixel);
		
		gatherSeedLayer(currentLayerNum);
		return true;
	}
	
	
	void saveLayerRender() {
		String userSessionPath = theSurface.getUserSessionPath();
		if(getNumberOfLayers()==1) {
			String suggestedName = MOUtils.getDateStampedImageFileName("Render_");
			System.out.println("saveRenderLayer: saving " + suggestedName);
			theSurface.theDocument.saveRenderToFile(userSessionPath + suggestedName);
			
		}else {
		assertLayerDirectory();
		
		int fileLayerNum = getNumberOfLayers() - currentLayerNum;
		String paddedFileLayerNum = MOUtils.getPaddedNumberString(fileLayerNum, 2);
		String fullLayerRenderPathAndName = layerRenderDirectory + "\\layer_" + paddedFileLayerNum + ".png";
		System.out.println("saveRenderLayer: saving " + fullLayerRenderPathAndName);
		theSurface.theDocument.saveRenderToFile(fullLayerRenderPathAndName);
		}
		
	}
	
	void assertLayerDirectory() {
		if(layerRenderDirectory.contentEquals("")==false) return;
		String userSessionPath = theSurface.getUserSessionPath();
		layerRenderDirectory = userSessionPath + "layers_" + MOUtils.getDateStamp();
		
		boolean result = MOUtils.createDirectory(layerRenderDirectory);
		System.out.println("created directory " + layerRenderDirectory + "  " + result);
	}
	
	
	
	
	
	@Override
	int getNumItems() {
		// TODO Auto-generated method stub
		return currentLayerSeeds.size();
	}


	@Override
	Seed getItem(int n) {
		// TODO Auto-generated method stub
		return currentLayerSeeds.get(n);
	}
	
	
	

	
}





@SuppressWarnings("serial")
class Seed implements Serializable{
	String batchName;
	PVector docPoint;
	float depth;
	int id;
	int layerNumber = 0;
	
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










