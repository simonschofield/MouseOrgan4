package MOScene3D;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


import MOApplication.MainDocument;
//import MOCompositing.ContributingSpritesList;
import MOMaths.PVector;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOSprite.SpriteBatch;
//import MOSprite.SpriteSeed;
//import MOSprite.SpriteSeedBatch;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;
import MOUtils.ImageDimensions;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Defines the relationship between the Master Render (the whole render) and SubROIs (sub rectangles of the master render, rendered seperately)
// The Master Render rect is stored as a document space rect. I.e. docwidth = bufferWidth/longestEdge, docheight = bufferHeight/longestEdge
// The SubROIs are stored in master rect space, as sub rectanges contained within the master rect.
// This should make conversion between the Sub ROI document space -> master rect doc space easy, and vice versa
// The ROIManager needs to be initialised before the document initialisation, as the ROI manager calculates the render dimensions, depending on which ROI is being used. 
// The Master Render has a fixed render size, irrespective of session scale, so that Sub ROIs can be found within a reliable fixed dimension render using Photoshop.
// 
//
//
//




public class ROIManager {
	
	

	ImageCoordinateSystem masterCoordinateSystem;
	ImageDimensions masterDimensions;

	ArrayList<SubROI> SubROIList = new ArrayList<SubROI>();

	String currentROIName;
	
	// useful rects for testing etc
	public Rect IDRect; 
	public Rect TopLeftQtrRect; 
	public Rect TopRightQtrRect; 
	public Rect LeftHalfRect; 
	public Rect TopHalfRect; 
	public Rect CentreRect; 

	public ROIManager(int masterWidth, int masterHeight){
		init(masterWidth, masterHeight);
	}
	
	private void init(int masterWidth, int masterHeight){
		// This is established before the render document is initialised in initialiseSession()
		// initialised with the saved-out render size of the master image, rendered at scale rscale
		// this then can return the full-size width and height of the master render if needed.
		masterCoordinateSystem = new ImageCoordinateSystem(masterWidth,  masterHeight);
		Rect extents = new Rect(0,0, masterWidth,  masterHeight);
		
		System.out.println("master doc rect = " + extents.toStr());
		masterDimensions = new ImageDimensions(masterWidth,  masterHeight); //masterCoordinateSystem.getBufferDimensions();
		
		
		
		addROI("master", extents, masterWidth);
		setCurrentROIName("master");
		
		
		int widthOver2 = (int)(masterWidth/2f);
		int heightOver2 = (int)(masterHeight/2f);
		
		IDRect = new Rect(0,0,masterWidth,masterHeight); 
		TopLeftQtrRect = new Rect(0,0,widthOver2,heightOver2); 
		TopRightQtrRect = new Rect(widthOver2,0,widthOver2,heightOver2); 
		LeftHalfRect = new Rect(0,0,widthOver2,masterHeight); 
		TopHalfRect = new Rect(0,0,masterWidth,heightOver2); 
		CentreRect = new Rect(widthOver2/2f,heightOver2/2f,widthOver2,heightOver2); 
	}

	
	public ImageCoordinateSystem getImageCoordinateSystem() {
		
		return masterCoordinateSystem;
	}

	public void setCurrentROIName(String s) {
		if(s.contains("master" ) || s.contains("Master" )) {
			currentROIName = "master";
		} else {
			currentROIName = s;
		}

	}

	public String getCurrentROIName() {
		return currentROIName;
	}
	
	public Rect getMasterDocumentRect() {
	 return getImageCoordinateSystem().getDocumentRect();
	}

	public Rect getCurrentROIRect() {
		return getCurrentROIInfo().ROIRect;
	}

	public void addROI(String name, Rect ROIRectInMasterPixels,  int fullROIRenderWidth) {
		// extensts passed in are in Master Rect Buffer Space
		// extents are calculated to be in Master Rect doc space
		
		// convert to points within the
		Rect ROIInMasterDocSpace = masterCoordinateSystem.bufferSpaceToDocSpace(ROIRectInMasterPixels);
		
		SubROI subroi = new SubROI(name,  ROIInMasterDocSpace, fullROIRenderWidth);
		SubROIList.add(subroi);
	}
	
	
	PVector subROIDocSpaceToMasterDocSpace(PVector currentSessionDocSpace) {
		// Converts between the local ROI document space, assumed to be currently in-play, to the master document space
		// assumes that the ROI is currently in-play, so can use the Document level coordinate space conversions

		if(isUsingMaster()) return currentSessionDocSpace.copy();
		
		Rect currentSessionDocRect =  GlobalSettings.getTheDocumentCoordSystem().getDocumentRect();
		Rect currentROIRect = getCurrentROIRect();
		
		// we map the point from currentRender Doc space to the Curent ROI rectangle (whihc sould have the same aspect!). This
		// produces a point within the current ROI that is already in Master Rect Doc Space units.
		PVector masterDocRectPt = Rect.map(currentSessionDocSpace, currentSessionDocRect, currentROIRect);
		return masterDocRectPt;

	}
	
	PVector masterDocSpaceToSubROIDocSpace(PVector masterDocPoint) {
		// Converts between the master render document space and the local ROI render Document space, 
		// assumes that the ROI is currently in-play, so can use the Document level coordinate space conversions
		if(isUsingMaster()) return masterDocPoint.copy();

		
		Rect SubROIDocSpaceRect = getCurrentROIDocRect();
		Rect currentSessionDocumentRect = GlobalSettings.getTheDocumentCoordSystem().getDocumentRect();
		PVector normalisedWithinSubROI = SubROIDocSpaceRect.norm(masterDocPoint);
		PVector subROIDocspacePoint = currentSessionDocumentRect.interpolate(normalisedWithinSubROI);
		
		//System.out.println("masterDocSpaceToSubROIDocSpace : master " + masterDocPoint.toStr() + " normalised " +  normalisedWithinSubROI.toStr() + " subROI " +  subROIDocspacePoint.toStr() );

		return subROIDocspacePoint;
		
	}
	

	public ImageDimensions getCurrentRenderDims() {
		int ROIEXtentsFullRenderWidth = (int) getCurrentROIInfo().extentsFullPixelWidth;
		float aspect = getCurrentROIInfo().ROIRect.aspect();
		int ROIEXtentsFullRenderHeight = (int) (ROIEXtentsFullRenderWidth/aspect);
		
		if(isUsingMaster()) {
			return new ImageDimensions(ROIEXtentsFullRenderWidth,ROIEXtentsFullRenderHeight);
		} 
		
		// if not, then sessionScale the ROI
		float scl = GlobalSettings.getSessionScale();
		return new ImageDimensions((int)(ROIEXtentsFullRenderWidth*scl),(int)(ROIEXtentsFullRenderHeight*scl));
	}
	

	public boolean isUsingROI() {
		return !isUsingMaster();
	}

	public boolean isUsingMaster() {
		if(currentROIName.contains("master") || currentROIName.contains("Master")) return true;
		return false;
	}

	public boolean isCurrentROI(String currentROI) {
		if(currentROIName.equals(currentROI)) return true;
		return false;
	}

	public void printCurrentROIInfo() {
		SubROI ri = getCurrentROIInfo();
		System.out.println("_____ROI______");
		System.out.println("ROI Name " + ri.name);

		System.out.println("ROI ROIExtents " + ri.ROIRect.toStr());
		//System.out.println("ROI ppaExtents " + ri.ppaExtents.toStr());

		System.out.println("ROI pixel width " + ri.extentsFullPixelWidth);
		System.out.println("_________________");

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// SpriteBatch methods. To do with cropping out a ROI from a larger "master" spriteBatch
	// Use the collated seeds for this only. 
	//
	//

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 
	// It is used to adjust the seeds locations into the ROI space 
	//
	

	public SpriteBatch applyROIToSpriteBatch(SpriteBatch seedbatch) {
		if(isUsingMaster()) return seedbatch;

		
		System.out.println("here applyROIToSpriteBatch 1");
		//SpriteBatch contributingSpriteSeedBatch = removeNoncontributingSpritesInROI(seedbatch);
		
		//SpriteBatch contributingSpriteSeedBatch = seedbatch;
		
		
		SubROI currentROIInfo =  this.getCurrentROIInfo();
		
		
		SpriteBatch spriteBatchOut = new SpriteBatch();
		//contributingSpriteSeedBatch.resetItemIterator();
		// The seeds are read in the master coordinate system doc space.
		// They need to be normalised within the current subROI, then converted to the SubROI document space
		// The current document space is that stored in the GlobalSettings coordinate system
		
		
		
		while( seedbatch.areItemsRemaining()) {

			Sprite s = seedbatch.getNextSprite().copy();
			PVector masterDocSpacePoint = s.getDocPoint();

			PVector subROIDocspacePoint = masterDocSpaceToSubROIDocSpace(masterDocSpacePoint);
			s.setDocPoint(subROIDocspacePoint);

			spriteBatchOut.addSprite(s);
			//System.out.println(" seeds master docpoint " + masterDocSpacePoint.toStr() +  " in ROI doc space " + subROIDocspacePoint.toString());
		}



		spriteBatchOut.resetItemIterator();

		//System.out.println("applyROIToSeeds: seeds before appplication of ROI " + seedbatch.getNumItems() + ". Adjusted number of seeds in ROI " + seedbatchOut.getNumItems());
		return spriteBatchOut;
	}
	
	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//public String getROISpriteBatchDataFileName() {
	//	String roiname = getCurrentROIName();
	//	String sessionname = GlobalSettings.getDocumentName();
	//	return GlobalSettings.getUserSessionPath() + "seeds//ROISpriteBatchData_" + sessionname + ".csv";
	//}



    
    public Rect getCurrentROIDocRect() {
    	return getCurrentROIInfo().ROIRect;
    }


	public SubROI getCurrentROIInfo() {

		return getROIInfo(currentROIName);
	}

	// private
	SubROI getROIInfo(String name) {

		for(SubROI ri: SubROIList) {
			if(ri.name.equals(name)) return ri;
		}
		System.out.println("ROIManager::getROIInfo(String name) - cannot find ROI of name " + name );
		return null;

	}


}

class SubROI{

	String name;
	Rect ROIRect;
	//Rect ppaExtents;
	int extentsFullPixelWidth;

	public SubROI(String name, Rect rOIExtents, int extentsFullPixelWidth) {
		//public ROIInfo(String name, Rect rOIExtents, Rect ppaExtents, int ppaFullPixelWidth) {
		this.name = name;
		ROIRect = rOIExtents;
		//this.ppaExtents = ppaExtents;
		this.extentsFullPixelWidth = extentsFullPixelWidth;
	}



}





 






