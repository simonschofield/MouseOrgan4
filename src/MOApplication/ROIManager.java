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

/**
* Defines the relationship between the Master Render (the whole render) and SubROIs (sub rectangles of the master render, rendered separately once the maste render has been finalised)<p>
* A Master render is set up as a  fixed-dimension image, which is invariant with sessionScale.<p>
* The Master Render is given the name "Master", and the ROIs are given user-defined names, along with the rects that define their extents within the master render.<p>
* The Master Render rect is stored as a document space rect. I.e. docwidth = bufferWidth/longestEdge, docheight = bufferHeight/longestEdge<p>
* The SubROIs are stored in master rect doc space, as sub rectanges contained within the master rect.<p>
* This should make conversion between the Sub ROI document space -> master rect doc space easy, and vice versa<p>
* The ROIManager needs to be initialised before the document initialisation, as the ROI manager calculates the render dimensions, depending on which ROI is being used. <p>
* The Master Render has a fixed render size, irrespective of session scale, so that Sub ROIs can be found within a reliable fixed dimension render using Photoshop.<p>
* 
*
*
*/
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

	
	/**
	 * @param masterWidth
	 * @param masterHeight
	 * ROI means Region Of Interest, and this class enables the user to specify regions of a Master render to be rendered separately at any size and session scale.
	 * The class ins initialised with a full-scene Master render of set height and width. The diemsions of this Master will not change with the render-session scale.
	 * When using a SceneData3D, the scenedata3d object initialises this class using the SceneData3D.createROIManager(int width) method, to ensure the same aspect ratio
	 * is maintained between the SceneData3D scene, and the ROI Master image.
	 * A number of pre-defined ROIs are automatically created, including one for the whole master render called "WholeRect".
	 */
	public ROIManager(int masterWidth, int masterHeight){
		
		// This is established before the render document is initialised in initialiseSession()
		// initialised with the saved-out render size of the master image, rendered at scale rscale
		// this then can return the full-size width and height of the master render if needed.
		masterCoordinateSystem = new ImageCoordinateSystem(masterWidth,  masterHeight);
		Rect masterPixelExtents = new Rect(0,0, masterWidth,  masterHeight);
		//System.out.println("ROI Manager init master width: " + masterWidth + " master height " + masterHeight);
		System.out.println("master doc rect = " + masterPixelExtents.toStr());
		masterDimensions = new ImageDimensions(masterWidth,  masterHeight); //masterCoordinateSystem.getBufferDimensions();

		addROI("master", masterPixelExtents, masterWidth);
		setCurrentROI("master");
		
		// adds some predefined rois, for testing,
		// with a pre-defined width equivalent to a 24-inch wide print
		addPredefinedROIs(24*300);
	}

	/**
	 * @return an ImageCoordinateSystem which is that of the master render. This will remain the same regardless of session scale.
	 * 
	 */
	public ImageCoordinateSystem getMasterImageCoordinateSystem() {
		return masterCoordinateSystem;
	}
	
	
	/**
	 * @return an ImageCoordinateSystem which is initialised to have the same BufferSpace as the current ROI
	 * NOT called by the user, but called by the MainDocument object at initialisation, in order to have the correct
	 * ImageCoordinateSystem in play for ROIs.
	 * this should be the ONLY method used to calculate and return the render dimensions from the session scale
	 */
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
	

	/**
	 * Sets the current ROI being used by the current UserSession. This must be called in the initialiseUserSession phase of the UserSession set up.
	 * @param roiName - the user defined ROI name, or "Master", or one of the pre-defiend ROIs e.g. "WholeRect"
	 */
	public void setCurrentROI(String roiName) {
		if(roiName.contains("master" ) || roiName.contains("Master" )) {
			currentROIName = "master";
		} else {
			currentROIName = roiName;
		}

	}

	
	/**
	 * @return  the string name of the current ROI being rendered.
	 */
	public String getCurrentROIName() {
		return currentROIName;
	}
	
	
	/**
	 * @return the document-space rectangle of the Master ROI
	 */
	public Rect getMasterDocumentRect() {
		return getMasterImageCoordinateSystem().getDocumentRect();
	}

	/**
	 * @return the document-space rectangle of the current ROI being rendered
	 */
	public Rect getCurrentROIRect() {
		return getCurrentROIInfo().ROIRect;
	}

	
	/**
	 * Adds a new ROI to the list of available ROIs contained within the Master render.
	 * To make this user-freindly, extents are user-defined in the ROIRectInMasterPixels in Master Image Buffer Space coordinates/dimensions. 
 	 * Viewing the master image in photoshop, ROIs can be found/defined using either the selection tool
	 * or a vector shape rectangle, and the coordinates read from this. These values are then converted inot Document Space for internal use.
	 * @param name - the name of the new ROI being added
	 * @param ROIRectInMasterPixels - The buffer-space extents of the ROI within the Master.
	 * @param fullROIRenderWidth - The desired size of the output image at scale 1 (100%)
	 *  
	 */
	public void addROI(String name, Rect ROIRectInMasterPixels,  int fullROIRenderWidth) {
		// convert to points within the
		Rect ROIInMasterDocSpace = masterCoordinateSystem.bufferSpaceToDocSpace(ROIRectInMasterPixels);
		
		//System.out.println("ROIInMasterDocSpace = " + ROIInMasterDocSpace.toStr());
		
		
		SubROI subroi = new SubROI(name,  ROIInMasterDocSpace, fullROIRenderWidth);
		SubROIList.add(subroi);
	}
	

	/**
	 * Generally used for de-bugging and for convenience to see the ROI as super-imposed over the master render. 
	 * @param roiName - the roi to be drawn
	 * @param c - the color of the border
	 * @param lineWEightPixels
	 * @param renderTagetName
	 */
	public void drawROI(String roiName, Color c, float lineWEightPixels, String renderTagetName) {
		if(isUsingMaster()) {
			Rect roiRect = getROIDocRect(roiName);
			//System.out.println(" Drawing ROI REct rect  " + name + " " + roiRect.toStr());
			//GlobalSettings.getDocument().getMain().drawRect_DocSpace(roiRect, new Color(0,0,0,0), c, 10);
			
			GlobalSettings.getDocument().getBufferedImageRenderTarget(renderTagetName).drawRect_DocSpace(roiRect, new Color(0,0,0,0), c, lineWEightPixels);
		}
	}
	

	
	
	
	

	/**
	 * @return true is NOT using the master i.e. using a user-defined, or preset ROI
	 */
	public boolean isUsingROI() {
		return !isUsingMaster();
	}

	/**
	 * @return true is currently using the master render
	 */
	public boolean isUsingMaster() {
		if(currentROIName.contains("master") || currentROIName.contains("Master")) return true;
		return false;
	}

	/**
	 * @param currentROI
	 * @return true if currentROI matches the current ROI name
	 */
	public boolean isCurrentROI(String currentROI) {
		if(currentROIName.equals(currentROI)) return true;
		return false;
	}

	/**
	 * Used for debugging
	 */
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
	
	
	
	/**
	 * Returns an array of all ROI names, used by the app to automatically delete all ROI spritebatches when the master is updated.
	 * @return an array list of all ROI names 
	 */
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

	
    
    /**
     * @return
     */
    public Rect getCurrentROIDocRect() {
    	return getCurrentROIInfo().ROIRect;
    }
    
    /**
     * @param name
     * @return
     */
    public Rect getROIDocRect(String name) {
		SubROI info =  getROIInfo(name);
		return info.ROIRect;
	}


	/**
	 * @return
	 */
	public SubROI getCurrentROIInfo() {

		return getROIInfo(currentROIName);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	// private
	//
	//
	SubROI getROIInfo(String name) {

		for(SubROI ri: SubROIList) {
			if(ri.name.equals(name)) return ri;
		}
		System.out.println("ROIManager::getROIInfo(String name) - cannot find ROI of name " + name );
		return null;

	}
	
	private void addPredefinedROIs(int fullSizeRenderWidth) {
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


}

/////////////////////////////////////////////////////////////////////////////////////////////
// private to the ROIManager (above)
//
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





 






