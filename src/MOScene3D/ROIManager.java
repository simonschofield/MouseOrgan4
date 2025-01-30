package MOScene3D;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


import MOApplication.MainDocument;
import MOCompositing.ContributingSpritesList;
import MOMaths.PVector;
import MOMaths.Rect;
import MOSprite.SpriteSeed;
import MOSprite.SpriteSeedBatch;
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
	//float renderScale;
	ArrayList<SubROI> SubROIList = new ArrayList<SubROI>();

	String currentROIName;

	boolean saveOutContributingSeedReport;
	
	
	
	
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


	//public boolean isFullsizeRender() {
	//	if(GlobalSettings.getSessionScale()==1) return true;
	//	return false;
	//}

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
		
		
		//PVector normalisedLocalROISpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(localROIDocSpace);
		//Rect currentROIRect = getCurrentROIInfo().ROIRect;
		//return currentROIRect.interpolate(normalisedLocalROISpace);
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
	
	
	
	public SpriteSeedBatch applyROIToSpriteSeedBatch(SpriteSeedBatch seedbatch) {
		if(isUsingMaster()) return seedbatch;

		
		System.out.println("here applyROIToSpriteSeedBatch 1");
		//SpriteSeedBatch contributingSpriteSeedBatch = removeNoncontributingSpritesInROI(seedbatch);
		
		SpriteSeedBatch contributingSpriteSeedBatch = seedbatch;
		
		
		SubROI currentROIInfo =  this.getCurrentROIInfo();
		
		
		SpriteSeedBatch seedbatchOut = new SpriteSeedBatch();
		contributingSpriteSeedBatch.resetItemIterator();
		// The seeds are read in the master coordinate system doc space.
		// They need to be normalised within the current subROI, then converted to the SubROI document space
		// The current document space is that stored in the GlobalSettings coordinate system
		
		
		
		while( contributingSpriteSeedBatch.areItemsRemaining()) {

			SpriteSeed s = contributingSpriteSeedBatch.getNextSeed().copy();
			PVector masterDocSpacePoint = s.getDocPoint();

			PVector subROIDocspacePoint = masterDocSpaceToSubROIDocSpace(masterDocSpacePoint);
			s.setDocPoint(subROIDocspacePoint);

			seedbatchOut.addSpriteSeed(s);
			System.out.println(" seeds master docpoint " + masterDocSpacePoint.toStr() +  " in ROI doc space " + subROIDocspacePoint.toString());
		}



		seedbatchOut.resetItemIterator();

		//System.out.println("applyROIToSeeds: seeds before appplication of ROI " + seedbatch.getNumItems() + ". Adjusted number of seeds in ROI " + seedbatchOut.getNumItems());
		return seedbatchOut;
	}
	
	
	
	
	
	


	public SpriteSeedBatch removeNoncontributingSpritesInROI(SpriteSeedBatch seedbatch) {
		// this only removed seeds if a "contributing sprite" file has been saved for this ROI (i.e. with the ROI's name) in the seeds folder
		// if the file cannot be found, then the class is alerted to save one out at the end of this session
		if(isUsingMaster()) return seedbatch;
		//SpriteCropDecisionList spriteCropList = theDocument.getRenderBorder().getSpriteCropDecisionList();
		ContributingSpritesList spriteCropList = new ContributingSpritesList();

		String fname = getContributingSpritesFilePathAndName();
		boolean loadResult = spriteCropList.load(fname);
		if(loadResult == false) {
			saveOutContributingSeedReport = true;
			return seedbatch;
		}
		System.out.println("here removeNoncontributingSpritesInROI 1");
		return spriteCropList.removeNonContributingSprites(seedbatch);

	}


	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void saveContributingSpritesReport(MainDocument theDocument, boolean forcesave) {
		// called at the end of the session

		if(isUsingMaster()) return;

		if(forcesave) saveOutContributingSeedReport = true;

		if(saveOutContributingSeedReport==false) return;


		String fname = getContributingSpritesFilePathAndName();

		System.out.println("saveContributingSpritesReport: saving" + fname);


		theDocument.getRenderBorder().getContributingSpritesList().save( fname );

	}


	public void deleteContributingSpritesReport() {
		// probably called if you are regenerating a master seed batch, which makes the previous
		// contributing sprite report redundant

		String fname = getContributingSpritesFilePathAndName();

		Path fPath = Paths.get(fname);
		try {
			Files.deleteIfExists(fPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private String getContributingSpritesFilePathAndName() {
		String roiname = getCurrentROIName();
		String sessionname = GlobalSettings.getDocumentName();
		return GlobalSettings.getUserSessionPath() + "seeds//contributingSprites_" + sessionname + "_" + roiname + ".csv";
	}



    public float getCurrentROIProportionOfMasterHeight() {
    	if(isUsingMaster()) {
    		return 1;
    	}
    	float crh = getCurrentROIInfo().ROIRect.getHeight();
    	float mrh = getROIInfo("master").ROIRect.getHeight();
    	
    	float proportion =  mrh/crh;
    	
    	//System.out.println("roi h " + crh + ", master h " + mrh + " proportion " + proportion);
    	return proportion;
    }
    
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




/* OLD ROIHELPER class now deleted
 * 
 package MOAppSessionHelpers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


// The process of using a master image and ROIs within the master image works thus: The master image is rendered out
// say at sale 0.2. This generates an output image - The master image - of 1200 x 2400 pixels. This is the master height and width. The rscale is 0.2.

import MOApplication.MainDocument;
import MOCompositing.ContributingSpritesList;
import MOMaths.PVector;
import MOMaths.Rect;

import MOSprite.SpriteSeed;
import MOSprite.SpriteSeedBatch;
import MOUtils.GlobalSettings;
import MOUtils.ImageDimensions;
//The process of using a master image and ROIs within the master image works thus: The master image is rendered out
// at a fixed size, irrespective of the sessionScale

// Next look for the ROI renders you want to make in the master image and demark them. The units for defining ROIs is in the pixel diemsions of the master image. 
// Add each ROI image to the helper using the pixel coordinates you find in the master image to define both the ROI. Also add the desired full render width of the 
// ROI region when rendered at full scale. Each ROI also requires a name used to access each ROI once set up.This is used to recalculate the entire scale of the ROI image, probably to a much larger resolution than the master image.

// Once added, you will use the ROI helper to do the following
// in initialiseUserSession establish all the ROIs and master image dimensions BEFORE creating the document image
// in initialiseUserSession set the current ROI using its name. If this is not called, then the master image is used
// in initialiseUserSession get the unscaled extents of the ROI image (or master image) with getFullSizeROIRenderDims() - returns pixel dimension values
// in loadContentUserSession, to set up the ROI extents in the sceneData3D object use getNormalisedROIExtentsRect()
// in loadContentUserSession, to set up the PPA of the ROI render use getNormalisedPPARect()
// if you need to check if you are rendering the master image or a ROI you can use isUsingROI()






public class ROIHelper {

	ImageDimensions masterDimensions;
	float renderScale;
	ArrayList<ROIInfo> ROIInfoList = new ArrayList<ROIInfo>();

	String currentROIName;

	boolean saveOutContributingSeedReport;

	public ROIHelper(int masterWidth, int masterHeight, float rScale){
		// This is established before the render document is initialised in initialiseSession()
		// initialised with the saved-out render size of the master image, rendered at scale rscale
		// this then can return the full-size width and height of the master render if needed.
		renderScale = rScale;

		masterDimensions = new ImageDimensions(masterWidth, masterHeight);
		Rect extents = new Rect(0,0,masterWidth, masterHeight);
		addROI("master",  extents, (int)(masterWidth/renderScale));
		setCurrentROIName("master");
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




	public boolean isFullsizeRender() {
		if(renderScale==1f) return true;
		return false;
	}

	public void addROI(String name, Rect extents,  int fullROIRenderWidth) {
		// all dims are in Master Image Pixel Space
		ROIInfo ri = new ROIInfo(name,  extents, fullROIRenderWidth);
		ROIInfoList.add(ri);
	}

	public ImageDimensions getFullSizeROIRenderDims() {
		// returns the full size image, based on the ppaFullPixelWidth.
		// This is used to establish the output render size in initialiseSession()
		//float widthOfROIExtentsInMaster = getCurrentROIInfo().ROIExtents.getWidth();
		//float widthOfPPAExtentsInMaster = getCurrentROIInfo().ppaExtents.getWidth();
		//float scaleUp = widthOfROIExtentsInMaster/widthOfPPAExtentsInMaster;
		//int desiredPPAFullRenderWidth = 
		int ROIEXtentsFullRenderWidth = (int)getCurrentROIInfo().extentsFullPixelWidth;
		float aspect = getCurrentROIInfo().ROIExtents.aspect();
		int ROIEXtentsFullRenderHeight = (int) (ROIEXtentsFullRenderWidth/aspect);
		return new ImageDimensions(ROIEXtentsFullRenderWidth,ROIEXtentsFullRenderHeight);
	}

	public Rect getNormalisedROIExtentsRect() {
		// as a normalised rect within the master rect
		// used to specify a ROI within the SceneData3D
		Rect roiExtents = getCurrentROIInfo().ROIExtents;
		//System.out.println("ROIHelper::getNormalisedROIExtentsRect current roi extents " + roiExtents.toStr());
		Rect masterRect = masterDimensions.getRect();
		//System.out.println("ROIHelper::getNormalisedROIExtentsRect masterRect " + masterRect.toStr());
		Rect nomalised = masterRect.norm(roiExtents);
		//System.out.println("ROIHelper::getNormalisedROIExtentsRect normalised " + nomalised.toStr());

		//System.out.println("ROIHelper::maps back to  normalised " + masterRect.left*nomalised.left + " " + masterRect.top*nomalised.top + " " + masterRect.right*nomalised.right + " " + masterRect.bottom*nomalised.bottom + " ");

		return nomalised;
	}


	//public Rect getNormalisedPPARect() {
	// the PPA rect within the ROI image.
	// This is called when establishing the PPA in the ROI document
	//	Rect roiExtents = getCurrentROIInfo().ROIExtents;
	//	Rect ppaExtents = getCurrentROIInfo().ppaExtents;
	//	return roiExtents.norm(ppaExtents);
	//}

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
		ROIInfo ri = getCurrentROIInfo();
		System.out.println("_____ROI______");
		System.out.println("ROI Name " + ri.name);

		System.out.println("ROI ROIExtents " + ri.ROIExtents.toStr());
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
	public SpriteSeedBatch applyROIToSpriteSeedBatch(SpriteSeedBatch seedbatch) {
		if(isUsingMaster()) return seedbatch;

		System.out.println("here applyROIToSpriteSeedBatch 1");
		SpriteSeedBatch contributingSpriteData = removeNoncontributingSpritesInROI(seedbatch);
		// adjusts the document point of seeds from a seed batch of a whole scene (no ROI)
		// to a specific ROI within that scene by mapping the original doc points into the nw
		// doc space represented by the ROI

		// as this is quite destructive this method returns a new SeedBatch

		// when seeds are saved the locations are saved in normalised form
		// This is converted to the doc space of the host session upon loading

		// The ROIRect is stored in normalised form
		// so to convert into the the ROI of the host session
		// 1/ convert the master seed location's doc point to normalised form within the current coord system system
		// 2/ 
		//System.out.println("here applyROIToSpriteSeedBatch 2");
		Rect theROI = getNormalisedROIExtentsRect();

		System.out.println("apply ROI to seeds " + theROI.toStr());
		SpriteSeedBatch seedbatchOut = new SpriteSeedBatch();
		contributingSpriteData.resetItemIterator();
		while( contributingSpriteData.areItemsRemaining()) {

			SpriteSeed s = contributingSpriteData.getNextSeed().copy();
			PVector newSceneDocPoint = s.getDocPoint();
			PVector normalisedPoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(newSceneDocPoint);


			PVector newROIPoint = theROI.norm(normalisedPoint); // convert to normalised space within the roi
			PVector newDocSpacePt = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(newROIPoint);
			//System.out.println("applyROIToSeeds: seeds docpoint before appplication of ROI " + newSceneDocPoint.toString() + ". Adjusted by ROI " + newDocSpacePt.toString());
			s.setDocPoint(newDocSpacePt);

			seedbatchOut.addSpriteSeed(s);

		}



		seedbatchOut.resetItemIterator();

		System.out.println("applyROIToSeeds: seeds before appplication of ROI " + seedbatch.getNumItems() + ". Adjusted number of seeds in ROI " + seedbatchOut.getNumItems());
		return seedbatchOut;
	}


	public SpriteSeedBatch removeNoncontributingSpritesInROI(SpriteSeedBatch seedbatch) {
		// this only removed seeds if a "contributing sprite" file has been saved for this ROI (i.e. with the ROI's name) in the seeds folder
		// if the file cannot be found, then the class is alerted to save one out at the end of this session
		if(isUsingMaster()) return seedbatch;
		//SpriteCropDecisionList spriteCropList = theDocument.getRenderBorder().getSpriteCropDecisionList();
		ContributingSpritesList spriteCropList = new ContributingSpritesList();

		String fname = getContributingSpritesFilePathAndName();
		boolean loadResult = spriteCropList.load(fname);
		if(loadResult == false) {
			saveOutContributingSeedReport = true;
			return seedbatch;
		}
		System.out.println("here removeNoncontributingSpritesInROI 1");
		return spriteCropList.removeNonContributingSprites(seedbatch);

	}


	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void saveContributingSpritesReport(MainDocument theDocument, boolean forcesave) {
		// called at the end of the session

		if(isUsingMaster()) return;

		if(forcesave) saveOutContributingSeedReport = true;

		if(saveOutContributingSeedReport==false) return;


		String fname = getContributingSpritesFilePathAndName();

		System.out.println("saveContributingSpritesReport: saving" + fname);


		theDocument.getRenderBorder().getContributingSpritesList().save( fname );

	}


	public void deleteContributingSpritesReport() {
		// probably called if you are regenerating a master seed batch, which makes the previous
		// contributing sprite report redundant

		String fname = getContributingSpritesFilePathAndName();

		Path fPath = Paths.get(fname);
		try {
			Files.deleteIfExists(fPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private String getContributingSpritesFilePathAndName() {
		String roiname = getCurrentROIName();
		String sessionname = GlobalSettings.getDocumentName();
		return GlobalSettings.getUserSessionPath() + "seeds//contributingSprites_" + sessionname + "_" + roiname + ".csv";
	}







	// private
	ROIInfo getCurrentROIInfo() {

		return getROIInfo(currentROIName);
	}

	ROIInfo getROIInfo(String name) {

		for(ROIInfo ri: ROIInfoList) {
			if(ri.name.equals(name)) return ri;

		}
		return null;

	}


}





class ROIInfo{

	String name;
	Rect ROIExtents;
	//Rect ppaExtents;
	int extentsFullPixelWidth;

	public ROIInfo(String name, Rect rOIExtents, int extentsFullPixelWidth) {
		//public ROIInfo(String name, Rect rOIExtents, Rect ppaExtents, int ppaFullPixelWidth) {
		this.name = name;
		ROIExtents = rOIExtents;
		//this.ppaExtents = ppaExtents;
		this.extentsFullPixelWidth = extentsFullPixelWidth;
	}



}


 * 
 * 
 * 
 */
 






