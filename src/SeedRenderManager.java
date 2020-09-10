import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;




////////////////////////////////////////////////////////////////////////////////
//Takes a seedbatchmanager, with all its collated seeds
//and farms the seeds out for render in the updateUserSession loop.
//
class SeedRenderLayer extends CollectionIterator{
	Range depthExtrema;
	ArrayList<Seed> layerSeeds = new ArrayList<Seed>();
	boolean isActive = true;
	
	
	SeedRenderLayer(ArrayList<Seed> collatedSeeds, float nearDepth, float farDepth){
		// initialised by the collated seeds from the Seed Render Manager
		depthExtrema = new Range(nearDepth, farDepth);
		
		for(Seed s : collatedSeeds) {
			if( depthExtrema.isBetweenInc(s.getDepth()) ) layerSeeds.add(s);
		}
		
		System.out.println("created SeedRenderLayer : depth extrema " + nearDepth + " " + farDepth + " num seeds found " + layerSeeds.size());
	}
	
	void setActive(boolean a) {
		isActive = a;
		
	}
	
	boolean isActive() {
		return isActive;
	}
	
	
	
	Seed getNextItem() {
		return (Seed) super.getNextItem();
	}

	@Override
	int getNumItems() {
		// TODO Auto-generated method stub
		return layerSeeds.size();
	}

	@Override
	Object getItem(int n) {
		// TODO Auto-generated method stub
		return layerSeeds.get(n);
	}
	
	boolean isFinished() {
		if( isActive == false) return true;
		return super.isFinished();
	}
	
	
}

public class SeedRenderManager{
	Surface theSurface;
	
	
	
	float[] depthLayerDemarkers;
	int numLayers = 1;
	
	ArrayList<Seed> collatedSeeds = new ArrayList<Seed>();
	
	ArrayList<SeedRenderLayer> layers = new ArrayList<SeedRenderLayer>();
	SeedRenderLayer currentLayer;
	
	int currentLayerNum = 0;
	
	int[] layerSwitches;
	boolean isFinishedAllLayersFlag = false;
	
	String layerRenderDirectory = "";
	
	SeedRenderManager(SeedBatchManager seedBatchManager){
		
		theSurface = seedBatchManager.theSurface;
		
		// initialise it with one big layer. The default position
		collatedSeeds = seedBatchManager.getCollatedSeeds();
		SeedRenderLayer layer0 = new SeedRenderLayer(collatedSeeds, 0, 1);
		layers.add(layer0);
		currentLayer = layer0;
	}
	
	// these are in normalised depth, biggest depth to least depth
	void setDepthLayers(float[] depthDemarkers) {
		
		// copy over the demarker array and insert 1 at the start and 0 at the end
		depthLayerDemarkers = new float[depthDemarkers.length+2];
		depthLayerDemarkers[0] = 1f;
		for(int n = 0; n < depthDemarkers.length; n++) {
			depthLayerDemarkers[n+1] = depthDemarkers[n];
		}
		depthLayerDemarkers[depthLayerDemarkers.length-1] = 0f;
		numLayers = depthLayerDemarkers.length-1; // i.e the gaps between the posts
	
		layers.clear();
		
		for(int n = 0; n <  numLayers; n++) {
			float thislayerFarDepth = depthLayerDemarkers[n];
			float thislayerNearDepth = depthLayerDemarkers[n+1];
			SeedRenderLayer thisLayer = new SeedRenderLayer(collatedSeeds, thislayerNearDepth, thislayerFarDepth);
			layers.add(thisLayer);
		}
		
		setCurrentLayer(0);
		
	}
	
	Range getLayerDepthExtremaFromArray(int layerNum){
		float far = depthLayerDemarkers[layerNum];
		float near = depthLayerDemarkers[layerNum+1];
		return new Range(near, far);
	}
	
	
	boolean setCurrentLayer(int n) {
		if( n >= numLayers) {
			System.out.println("SeedRenderManager: setCurrentLayer finished all layers " + n + " , numLayers =  " + numLayers);
			return false;
		}
		currentLayer = layers.get(n);
		currentLayerNum = n;
		return true;
	}

	
	
	int getNumberOfLayers() {
		return numLayers;
	}
	
	
	
	Seed getNextItem() {
		if(isFinishedAllLayersFlag) return null;
		
		if( currentLayer.isFinished() ) {
			boolean moreLayers = advanceCurrentLayer();
			
			if(moreLayers == false) {
				isFinishedAllLayersFlag = true;
			}
		}
		
		Seed thisSeed = currentLayer.getNextItem();
		
		
		
		return thisSeed;
	}
	
	boolean finishedAllLayers(){
		return  isFinishedAllLayersFlag;
	
	}
	
	

	boolean advanceCurrentLayer() {
		// save current render,
		saveLayerRender();
		
		currentLayerNum++;
		if(currentLayerNum >= getNumberOfLayers()) return false;
		
		setCurrentLayer(currentLayerNum);
		
		// if more layers to do....
		// clear current render,
		Color emptyPixel = new Color(0,0,0,0);
		theSurface.theDocument.fillBackground(emptyPixel);
	
		return true;
	}
	
	
	void advanceToSeedNumber(int n) {
		
		int layerNumSeedsSum = 0;
		int layerNum = 0;
		while(true) {
			
			layerNumSeedsSum += layers.get(layerNum).getNumItems();
			
			if(layerNumSeedsSum > n) {
				// found the containing layer
				boolean OK = setCurrentLayer(layerNum);
				if(OK == false) return;
				int startInThislayer = n - (layerNumSeedsSum - currentLayer.getNumItems());
				currentLayer.advanceIterator(startInThislayer);
				break;
			}
			
			layerNum++;
		}
		
	}
	
	void setActiveLayers(boolean[] activeList) {
		// should be the same number as the number of layers
		//
		for(int n = 0; n < activeList.length; n++) {
			boolean active = activeList[n];
			layers.get(n).setActive(active);
		}
		
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
	
	
	
	
	
	
	
	

	
}


//public class SeedRenderManager extends CollectionIterator{
//	Surface theSurface;
//	
//	
//	float[] depthLayerDemarkers;
//	int numLayers = 1;
//	
//	ArrayList<Seed> collatedSeeds = new ArrayList<Seed>();
//	ArrayList<Seed> currentLayerSeeds = new ArrayList<Seed>();
//	int currentLayerNum = 0;
//	int[] layerSwitches;
//	boolean isFinishedAllLayersFlag = false;
//	
//	String layerRenderDirectory = "";
//	
//	SeedRenderManager(SeedBatchManager seedBatchManager){
//		
//		theSurface = seedBatchManager.theSurface;
//		
//		collatedSeeds = seedBatchManager.getCollatedSeeds();
//		gatherSeedLayer(0);
//	}
//	
//	void setDepthLayers(float[] depthDemarkers) {
//		depthLayerDemarkers = depthDemarkers;
//		numLayers = depthLayerDemarkers.length+1;
//		
//		for(Seed s : collatedSeeds) {
//			
//			s.layerNumber = getLayerFromDepth(s.getDepth());
//			
//		}
//		
//		gatherSeedLayer(0);
//	
//	}
//
//	int getLayerFromDepth(float depth) {
//		if(depth > depthLayerDemarkers[0]) return 0;
//		if(depthLayerDemarkers.length == 1) return 1;
//		
//		for(int layerNum = 1;  layerNum <depthLayerDemarkers.length; layerNum++) {
//		    float thisDemarkation = depthLayerDemarkers[layerNum];
//			if(depth>thisDemarkation) return layerNum;
//		}
//		// shouldn't get here
//		return numLayers-1;
//	}
//	
//	int getNumberOfLayers() {
//		return numLayers;
//	}
//	
//	
//	
//	void gatherSeedLayer(int layerNum) {
//		currentLayerSeeds.clear();
//		for(Seed s : collatedSeeds) {
//			
//			if(s.layerNumber == layerNum) currentLayerSeeds.add(s);
//			
//		}
//		
//		currentLayerSeeds.sort(Comparator.comparing(Seed::getDepth).reversed());
//		currentLayerNum = layerNum;
//		
//		System.out.println("gatherSeedLayer layer num " + currentLayerNum + " num seeds " + currentLayerSeeds.size());
//		System.out.println("from a total of " + collatedSeeds.size());
//	}
//	
//	
//	
//	Seed getNextItem() {
//		if(isFinishedAllLayersFlag) return null;
//		
//		Seed thisSeed = (Seed) super.getNextItem();
//		
//		if( finishedLayer() ) {
//			System.out.println("finished layer " + currentLayerNum);
//			boolean moreLayers = advanceCurrentLayer();
//			System.out.println("more layers? " + moreLayers);
//			System.out.println("next layer " + currentLayerNum);
//			if(moreLayers) {
//				resetItemIterator();
//			} else {
//				isFinishedAllLayersFlag = true;
//			}
//		}
//		
//		return thisSeed;
//	}
//	
//	boolean finishedAllLayers(){
//		return  isFinishedAllLayersFlag;
//	
//	}
//	
//	boolean finishedLayer() {
//		return isFinished();
//		
//	}
//
//	boolean advanceCurrentLayer() {
//		// save current render,
//		saveLayerRender();
//		
//		currentLayerNum++;
//		if(currentLayerNum >= getNumberOfLayers()) return false;
//		
//		// if more layers to do....
//		// clear current render,
//		Color emptyPixel = new Color(0,0,0,0);
//		theSurface.theDocument.fillBackground(emptyPixel);
//		
//		gatherSeedLayer(currentLayerNum);
//		return true;
//	}
//	
//	
//	void saveLayerRender() {
//		String userSessionPath = theSurface.getUserSessionPath();
//		if(getNumberOfLayers()==1) {
//			String suggestedName = MOUtils.getDateStampedImageFileName("Render_");
//			System.out.println("saveRenderLayer: saving " + suggestedName);
//			theSurface.theDocument.saveRenderToFile(userSessionPath + suggestedName);
//			
//		}else {
//		assertLayerDirectory();
//		
//		int fileLayerNum = getNumberOfLayers() - currentLayerNum;
//		String paddedFileLayerNum = MOUtils.getPaddedNumberString(fileLayerNum, 2);
//		String fullLayerRenderPathAndName = layerRenderDirectory + "\\layer_" + paddedFileLayerNum + ".png";
//		System.out.println("saveRenderLayer: saving " + fullLayerRenderPathAndName);
//		theSurface.theDocument.saveRenderToFile(fullLayerRenderPathAndName);
//		}
//		
//	}
//	
//	void assertLayerDirectory() {
//		if(layerRenderDirectory.contentEquals("")==false) return;
//		String userSessionPath = theSurface.getUserSessionPath();
//		layerRenderDirectory = userSessionPath + "layers_" + MOUtils.getDateStamp();
//		
//		boolean result = MOUtils.createDirectory(layerRenderDirectory);
//		System.out.println("created directory " + layerRenderDirectory + "  " + result);
//	}
//	
//	
//	
//	
//	
//	@Override
//	int getNumItems() {
//		// TODO Auto-generated method stub
//		return currentLayerSeeds.size();
//	}
//
//
//	@Override
//	Seed getItem(int n) {
//		// TODO Auto-generated method stub
//		return currentLayerSeeds.get(n);
//	}
//	
//	
//	
//
//	
//}


