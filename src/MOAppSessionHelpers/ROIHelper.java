package MOAppSessionHelpers;
import java.util.ArrayList;
// The process of using a master image and ROIs within the master image works thus: The master image is rendered out
// say at sale 0.2. This generates an output image - The master image - of 1200 x 2400 pixels. This is the master height and width. The rscale is 0.2.

import MOMaths.Rect;
import MOUtils.ImageDimensions;
//The process of using a master image and ROIs within the master image works thus: The master image is rendered out
//say at sale 0.2. This generates an output image - The master image - of 1200 x 2400 pixels. This is the master height and width. The rscale is 0.2.

// Next look for the ROI renders you want to make in the master image and demark them. These will be the extnst in pixels of each ROI image. 
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
		
		
		float getRenderScale() {
			
			return renderScale;
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
			System.out.println("ROIHelper::getNormalisedROIExtentsRect current roi extents " + roiExtents.toStr());
			Rect masterRect = masterDimensions.getRect();
			System.out.println("ROIHelper::getNormalisedROIExtentsRect masterRect " + masterRect.toStr());
			Rect nomalised = masterRect.norm(roiExtents);
			System.out.println("ROIHelper::getNormalisedROIExtentsRect normalised " + nomalised.toStr());
			
			System.out.println("ROIHelper::maps back to  normalised " + masterRect.left*nomalised.left + " " + masterRect.top*nomalised.top + " " + masterRect.right*nomalised.right + " " + masterRect.bottom*nomalised.bottom + " ");
			
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


