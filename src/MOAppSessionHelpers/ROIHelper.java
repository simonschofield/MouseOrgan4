package MOAppSessionHelpers;
import java.util.ArrayList;
// The process of using a master image and ROIs within the master image works thus: The master image is rendered out
// say at sale 0.2. This generates an output image - The master image - of 1200 x 2400 pixels. This is the master height and width. The rscale is 0.2.

import MOMaths.Rect;
import MOUtils.ImageDimensions;

// Next look for the ROI renders you want to make in the master image and demark them. These will be the PPAs (permitted paste areas) of each ROI image. Each PPA area requires a bigger
// full extents to allow for the pasting of sprites outside the PPA, but still partially appearing within the PPA. These larger areas are the ROIExtents; demark them too.
// Add each ROI image to the helper using the pixel coordinates you find in the master image to define both the ROIExtents and the PPA of each. Also add the desired full render width of the 
// PPA region when rendered at full scale. Each ROI also requires a name used to access each ROI once set up.This is used to recalculate the entire scale of the ROI image, probably to a much larger resolution than the master image.

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
		
		
		
		ROIHelper(int masterWidth, int masterHeight, float rScale){
			// This is established before the render document is initialised in initialiseSession()
			// initialised with the saved-out render size of the master image, rendered at scale rscale
			// this then can return the full-size width and height of the master render if needed.
			renderScale = rScale;
			
			masterDimensions = new ImageDimensions(masterWidth, masterHeight);
			Rect extents = new Rect(0,0,masterWidth, masterHeight);
			addROI("master",  extents, extents, (int)(masterWidth/renderScale));
			setCurrentROI("master");
		}
		
		
		void setCurrentROI(String s) {
			currentROIName = s;
			
		}
		
		String getCurrentROI() {
			
			return currentROIName;
		}
		
		
		float getRenderScale() {
			
			return renderScale;
		}
		
		void addROI(String name, Rect extents, Rect ppa, int fullPPARenderWidth) {
			// all dims are in Master Image Pixel Space
			ROIInfo ri = new ROIInfo(name,  extents,  ppa,  fullPPARenderWidth);
			ROIInfoList.add(ri);
		}
		
		ImageDimensions getFullSizeROIRenderDims() {
			// returns the full size image, based on the ppaFullPixelWidth.
			// This is used to establish the output render size in initialiseSession()
			float widthOfROIExtentsInMaster = getCurrentROIInfo().ROIExtents.getWidth();
			float widthOfPPAExtentsInMaster = getCurrentROIInfo().ppaExtents.getWidth();
			float scaleUp = widthOfROIExtentsInMaster/widthOfPPAExtentsInMaster;
			int desiredPPAFullRenderWidth = getCurrentROIInfo().ppaFullPixelWidth;
			int ROIEXtentsFullRenderWidth = (int)(desiredPPAFullRenderWidth*scaleUp);
			float aspect = getCurrentROIInfo().ROIExtents.aspect();
			int ROIEXtentsFullRenderHeight = (int) (ROIEXtentsFullRenderWidth/aspect);
			return new ImageDimensions(ROIEXtentsFullRenderWidth,ROIEXtentsFullRenderHeight);
		}
		
		Rect getNormalisedROIExtentsRect() {
			// as a normalised rect within the master rect
			// used to specify a ROI within the SceneData3D
			Rect roiExtents = getCurrentROIInfo().ROIExtents;
			Rect masterRect = masterDimensions.getRect();
			return masterRect.norm(roiExtents);
		}
		
		
		Rect getNormalisedPPARect() {
			// the PPA rect within the ROI image.
			// This is called when establishing the PPA in the ROI document
			Rect roiExtents = getCurrentROIInfo().ROIExtents;
			Rect ppaExtents = getCurrentROIInfo().ppaExtents;
			return roiExtents.norm(ppaExtents);
		}
		
		boolean isUsingROI() {
			return !isUsingMaster();
		}
		
		boolean isUsingMaster() {
			if(currentROIName.equals("master")) return true;
			return false;
		}
		
		boolean isCurrentROI(String currentROI) {
			if(currentROIName.equals(currentROI)) return true;
			return false;
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
	Rect ppaExtents;
	int ppaFullPixelWidth;
	
	public ROIInfo(String name, Rect rOIExtents, Rect ppaExtents, int ppaFullPixelWidth) {
	
		this.name = name;
		ROIExtents = rOIExtents;
		this.ppaExtents = ppaExtents;
		this.ppaFullPixelWidth = ppaFullPixelWidth;
	}
	
}
