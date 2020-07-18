import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;




////////////////////////////////////////////////////////////////////////////////
//Takes a seedbatchmanager, with all its collated seeds
//and farms the seeds out for render in the updateUserSession loop.
//




public class SeedRenderManager extends CollectionIterator{
	Surface theSurface;
	
	
	float[] depthLayerDemarkers;
	int numLayers = 1;
	
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
	
	void setDepthLayers(float[] depthDemarkers) {
		depthLayerDemarkers = depthDemarkers;
		numLayers = depthLayerDemarkers.length+1;
		
		for(Seed s : collatedSeeds) {
			
			s.layerNumber = getLayerFromDepth(s.getDepth());
			
		}
		
		gatherSeedLayer(0);
	
	}

	int getLayerFromDepth(float depth) {
		if(depth > depthLayerDemarkers[0]) return 0;
		if(depthLayerDemarkers.length == 1) return 1;
		
		for(int layerNum = 1;  layerNum <depthLayerDemarkers.length; layerNum++) {
		    float thisDemarkation = depthLayerDemarkers[layerNum];
			if(depth>thisDemarkation) return layerNum;
		}
		// shouldn't get here
		return numLayers-1;
	}
	
	int getNumberOfLayers() {
		return numLayers;
	}
	
	
	
	void gatherSeedLayer(int layerNum) {
		currentLayerSeeds.clear();
		for(Seed s : collatedSeeds) {
			
			if(s.layerNumber == layerNum) currentLayerSeeds.add(s);
			
		}
		
		currentLayerSeeds.sort(Comparator.comparing(Seed::getDepth).reversed());
		currentLayerNum = layerNum;
		
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


/* OLD - used a striated image to define layers - didn't seem that useful in the end
 * 
 * 
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


 * 
 * 
 */
 

