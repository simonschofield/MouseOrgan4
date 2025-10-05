package MOApplication;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import MOImage.ImageDimensions;
//import MOCompositing.ContributingSpritesList;
import MOMaths.PVector;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOSprite.SpriteBatch;
//import MOSprite.SpriteSeed;
//import MOSprite.SpriteSeedBatch;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Defines the relationship between the Master Render (the whole render) and SubROIs (sub rectangles of the master render, rendered separately)
// The Master Render rect is stored as a document space rect. I.e. docwidth = bufferWidth/longestEdge, docheight = bufferHeight/longestEdge
// The SubROIs are stored in master rect doc space, as sub rectanges contained within the master rect.
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
	public Rect WholeRect; 
	public Rect WholeRect_5pcBorder; 
	public Rect WholeRect_10pcBorder; 
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
		Rect masterPixelExtents = new Rect(0,0, masterWidth,  masterHeight);
		//System.out.println("ROI Manager init master width: " + masterWidth + " master height " + masterHeight);
		System.out.println("master doc rect = " + masterPixelExtents.toStr());
		masterDimensions = new ImageDimensions(masterWidth,  masterHeight); //masterCoordinateSystem.getBufferDimensions();
		
		
		
		addROI("master", masterPixelExtents, masterWidth);
		setCurrentROIName("master");
		
		// adds some predefined rois, for testing,
		// with a pre-defined width equivalent to a 24-inch wide print
		addPredefinedROIs(24*300);
	}

	
	public ImageCoordinateSystem getMasterImageCoordinateSystem() {
		
		return masterCoordinateSystem;
	}
	
	public void addPredefinedROIs(int fullSizeRenderWidth) {
		int masterWidth = masterCoordinateSystem.getBufferWidth();
		int masterHeight = masterCoordinateSystem.getBufferHeight();
		
		int widthOver2 = (int)(masterWidth/2f);
		int heightOver2 = (int)(masterHeight/2f);
		
		float borderW_percentile = masterWidth/100f;
		float borderH_percentile = masterHeight/100f;
		
		
		WholeRect = new Rect(0,0,masterWidth,masterHeight); 
		addROI("WholeRect", WholeRect, fullSizeRenderWidth);
		
		int w5pc = (int)(borderW_percentile*5);
		int h5pc = (int)(borderH_percentile*5);
		WholeRect_5pcBorder = new Rect(w5pc,h5pc,masterWidth-(2*w5pc),masterHeight-(2*h5pc)); 
		addROI("WholeRect_5pcBorder", WholeRect_5pcBorder, fullSizeRenderWidth);
		
		int w10pc = (int)(borderW_percentile*10);
		int h10pc = (int)(borderH_percentile*10);
		WholeRect_10pcBorder = new Rect(w10pc,h10pc,masterWidth-(2*w10pc),masterHeight-(2*h10pc)); 
		addROI("WholeRect_10pcBorder", WholeRect_10pcBorder, fullSizeRenderWidth);
		
		
		TopLeftQtrRect = new Rect(0,0,widthOver2,heightOver2); 
		addROI("TopLeftQtrRect", TopLeftQtrRect, fullSizeRenderWidth);
		
		TopRightQtrRect = new Rect(widthOver2,0,widthOver2,heightOver2); 
		addROI("TopRightQtrRect", TopRightQtrRect, fullSizeRenderWidth);
		
		LeftHalfRect = new Rect(0,0,widthOver2,masterHeight); 
		addROI("LeftHalfRect", LeftHalfRect, fullSizeRenderWidth);
		
		TopHalfRect = new Rect(0,0,masterWidth,heightOver2); 
		addROI("TopHalfRect", TopHalfRect, fullSizeRenderWidth);
		
		CentreRect = new Rect(widthOver2/2f,heightOver2/2f,widthOver2,heightOver2); 
		addROI("CentreRect", CentreRect, fullSizeRenderWidth);
		
		
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// when getting the current render dimensions using a ROIManager session, this should be the ONLY place
	// to calculate the render dimensions from the session scale
	//
	public ImageCoordinateSystem getCurrentROIImageCoordinateSystem_SessionScaled() {
		SubROI roi =  getCurrentROIInfo();
		
		int ROIEXtentsFullRenderWidth = roi.extentsFullPixelWidth;
		int ROIEXtentsFullRenderHeight = roi.extentsFullPixelHeight;
		
		if(isUsingMaster()==false) {
			float scl = GlobalSettings.getSessionScale();
			ROIEXtentsFullRenderWidth = (int)Math.round(ROIEXtentsFullRenderWidth*scl); // session scaling of the document happens here
			return new ImageCoordinateSystem(ROIEXtentsFullRenderWidth, roi.ROIRect);
		}
		
		
		return new ImageCoordinateSystem(ROIEXtentsFullRenderWidth, ROIEXtentsFullRenderHeight);
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
		return getMasterImageCoordinateSystem().getDocumentRect();
	}
	
	
	

	public Rect getCurrentROIRect() {
		return getCurrentROIInfo().ROIRect;
	}

	public void addROI(String name, Rect ROIRectInMasterPixels,  int fullROIRenderWidth) {
		// extents passed in as ROIRectInMasterPixels are in Master Rect Buffer Space
		// in order to facilitate the process of defining ROIs using a Photoshop image of the master, and 
		// using the pixel location values of the roi defined (e.g. by using the vector rect tool).
		// Extents are then calculated and stored internally in Master Rect doc space
		
		// convert to points within the
		Rect ROIInMasterDocSpace = masterCoordinateSystem.bufferSpaceToDocSpace(ROIRectInMasterPixels);
		
		//System.out.println("ROIInMasterDocSpace = " + ROIInMasterDocSpace.toStr());
		
		
		SubROI subroi = new SubROI(name,  ROIInMasterDocSpace, fullROIRenderWidth);
		SubROIList.add(subroi);
	}
	

	public void drawROI(String roiName, Color c, float lineWEightPixels, String renderTagetName) {
		if(isUsingMaster()) {
			Rect roiRect = getROIDocRect(roiName);
			//System.out.println(" Drawing ROI REct rect  " + name + " " + roiRect.toStr());
			//GlobalSettings.getDocument().getMain().drawRect_DocSpace(roiRect, new Color(0,0,0,0), c, 10);
			
			GlobalSettings.getDocument().getBufferedImageRenderTarget(renderTagetName).drawRect_DocSpace(roiRect, new Color(0,0,0,0), c, lineWEightPixels);
		}
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
		System.out.println("ROI pixel height " + ri.extentsFullPixelHeight);
		System.out.println("_________________");

	}
	
	
	
	public ArrayList<String> getROINames(){
		// does not return the "master" name, only the other ROIs
		// used in deleting old ROI sprite batch files
		ArrayList<String> namesOut = new ArrayList<String>();
		for(SubROI ri: SubROIList) {
			if(ri.name.equals("master") || ri.name.equals("Master")) continue;
			namesOut.add(ri.name);
			//System.out.println("getROINames " + ri.name);
		}
		return namesOut;
	}

	
    
    public Rect getCurrentROIDocRect() {
    	return getCurrentROIInfo().ROIRect;
    }
    
    public Rect getROIDocRect(String name) {
		SubROI info =  getROIInfo(name);
		return info.ROIRect;
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
	int extentsFullPixelHeight;
	
	public SubROI(String name, Rect rOIExtents, int pixelWidth) {
		//public ROIInfo(String name, Rect rOIExtents, Rect ppaExtents, int ppaFullPixelWidth) {
		this.name = name;
		ROIRect = rOIExtents;
		//this.ppaExtents = ppaExtents;
		extentsFullPixelWidth = pixelWidth;
		
		float aspect = ROIRect.aspect();
		extentsFullPixelHeight = (int) Math.round(extentsFullPixelWidth/aspect);
		
		
		
	}

	

}





 






