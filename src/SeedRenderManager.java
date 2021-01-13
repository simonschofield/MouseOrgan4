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
		Range extrFromPoints = getDepthExtremaFromPoints();
		System.out.println("Extrema from points :  " + extrFromPoints.toStr());
	}
	
	void setActive(boolean a) {
		isActive = a;
		
	}
	
	
	
	
	boolean isActive() {
		return isActive;
	}
	
	Range getDepthExtremaFromPoints() {
		Range extr = new Range();
		extr.initialiseForExtremaSearch();
		for(Seed s : layerSeeds) {
			float thisDepth = s.getDepth();
			extr.addExtremaCandidate(thisDepth);
			
		}
		return extr;
	}
	
	Range getYExtremaFromPoints() {
		Range extr = new Range();
		extr.initialiseForExtremaSearch();
		for(Seed s : layerSeeds) {
			float thisY = s.docPoint.y;
			extr.addExtremaCandidate(thisY);
		}
		return extr;
	}
	
	ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(Seed s : layerSeeds) {
			points.add(s.docPoint);
		}
		return points;
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
		if(n < getNumItems()) {
			return layerSeeds.get(n);
		}
		return null;
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
	String renderNameEnhancement = "";
	
	SeedRenderManager(SeedBatchManager seedBatchManager){
		
		theSurface = seedBatchManager.theSurface;
		collatedSeeds = seedBatchManager.getCollatedSeeds();
		// initialise it with one big layer. The default position
		createDefaultSingleLayer();
	}
	
	
	void createDefaultSingleLayer() {
		layers.clear();
		
		SeedRenderLayer layer0 = new SeedRenderLayer(collatedSeeds, 0, 1);
		
		layers.add(layer0);
		currentLayer = layer0;
	}
	
	
	void reset() {
		setCurrentLayer(0);
		isFinishedAllLayersFlag = false;
		for(SeedRenderLayer l: layers) {
			l.reset();
		}
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

	int getCurrentLayerNum() {
		return currentLayerNum;
	}
	
	int getNumLayers() {
		return numLayers;
	}
	
	
	
	Seed getNextItem() {
		if(isFinishedAllLayersFlag) return null;
		
		if( currentLayer.isFinished() ) {
			boolean moreLayers = advanceCurrentLayer();
			
			if(moreLayers == false) {
				System.out.println("SeedRenderManager: Finished rendering all layers");
				isFinishedAllLayersFlag = true;
				return null;
			}
		}
		
		Seed thisSeed = currentLayer.getNextItem();
		
		
		
		return thisSeed;
	}
	
	boolean finishedAllLayers(){
		return  isFinishedAllLayersFlag;
	
	}
	
	void drawLayerPoints() {
		Color[] palette = MOUtils.getBasic12ColorPalette();
		for(int n = 0; n < layers.size(); n++) {
			int colnum = n%12;
			Color c = palette[colnum];
			
			ArrayList<PVector> points = layers.get(n).getPoints();
			theSurface.theDocument.drawPoints(points, c);
		}
		
	}

	boolean advanceCurrentLayer() {
		// save current render,
		if(currentLayer.isActive()) saveLayerRender();
		
		currentLayerNum++;
		if(currentLayerNum >= getNumLayers()) return false;
		
		setCurrentLayer(currentLayerNum);
		
		// if more layers to do....
		// clear current render,
		theSurface.theDocument.clearImage();
	
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
	
	
	void setLayerActive(int layerNum, boolean active) {
		layers.get(layerNum).setActive(active);
	}
	
	void setLayersActive(boolean[] activeList) {
		// should be the same number as the number of layers
		//
		for(int n = 0; n < activeList.length; n++) {
			boolean active = activeList[n];
			layers.get(n).setActive(active);
		}
		
	}
	
	void setRenderNameEnahancement(String name) {
		renderNameEnhancement = name;

	}
	
	void saveLayerRender() {
		
		String userSessionPath = theSurface.getUserSessionPath();
		if(getNumLayers()==1) {
			String suggestedName = MOUtils.getDateStampedImageFileName("Render_" + renderNameEnhancement);
			System.out.println("saveRenderLayer: saving " + suggestedName);
			theSurface.theDocument.saveRenderToFile(userSessionPath + suggestedName);
			
		}else {
			// the out string format is im01_seedLayer05, im02_seedLayer04 ... im06_seedLayer00
			// where the im_number correctly layers the images into Photoshop's Scripts->load files into stack
			// and the seedLayer_num is the layer num within MouseOrgan
			assertLayerDirectory();
			
			int imNum = getNumLayers() - currentLayerNum;
			String paddedSeedLayerNum = MOUtils.getPaddedNumberString(currentLayerNum, 2);
			String paddedImNum = MOUtils.getPaddedNumberString(imNum, 2);
			String fullLayerRenderPathAndName = layerRenderDirectory + "\\im" + paddedImNum + "_seedLayer" + paddedSeedLayerNum + ".png";
			System.out.println("saveRenderLayer: saving " + fullLayerRenderPathAndName);
			theSurface.theDocument.saveRenderToFile(fullLayerRenderPathAndName);
			}
		
	}
	
	void assertLayerDirectory() {
		if(layerRenderDirectory.contentEquals("")==false) return;
		String userSessionPath = theSurface.getUserSessionPath();
		layerRenderDirectory = userSessionPath + "layers_" + renderNameEnhancement + MOUtils.getDateStamp();
		
		boolean result = MOUtils.createDirectory(layerRenderDirectory);
		System.out.println("created directory " + layerRenderDirectory + "  " + result);
	}
	
	
	
	
	
	
	
	

	
}

