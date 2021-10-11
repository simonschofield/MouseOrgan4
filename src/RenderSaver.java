//////////////////////////////////////////////////////////////////////////////////
// Helps manage the saving process for renders, either as single images or as collections of images (layers) through the creation of a specially made subdirectory
// Single images are time stamped. 
// When a folder is made for multiple images, the folder is time-stamped and the images within follow a naming convention described below
// 
//
// Single Image, no sub directory
// UserSessionPath/baseName_timeStamp.png
//
// Using sub directory without layers -  this is probably done when generating images with  masks
// UserSessionPath/baseName_timestamp/basename.png and UserSessionPath/baseName_timestamp/basename_mask.png
//
// Using Layers
// Using sub directory without Photoshop layering convention
// UserSessionPath/baseName_timestamp/img_MOLayer_(num increasing).png
// Using sub directory with Photoshop layering convention
// UserSessionPath/baseName_timestamp/img_MOLayer_(num increasing)_PSLayer_(num decreasing from 99).png
//

class RenderSaver {

	String baseName;
	boolean useSubDirectory = false;
	boolean useLayers = false;
	String subDirectory;
	int imageNumCounter = 0;
	

	boolean photoshopLayerOrderNumbering = false;
	int photoshopLayerNumberMaxNum = 99;

	RenderSaver(String baseName) {
		this.baseName = baseName;

		
	}

	void createSubDirectory(boolean usinglayer) {
		useLayers = usinglayer;
		useSubDirectory = true;
		subDirectory = MOUtils.createDirectory(GlobalObjects.theSurface.getUserSessionPath(), baseName, true);
	}

	void usePhotoshopLayerOrderNumbering(int maxNum) {
	// Can only be set with sub directory mode
	// when this is set to true the naming convention is altered to
	// load the images as photoshop layers via the photoshop
	// load image layers as stack script
	
		if(useSubDirectory==false) {
			System.out.println("RenderSaver: cannot set Photoshop Layer Ordering Mode without setting a sub directory first - use createSubDirectory(true)");
			return;
		}
		photoshopLayerOrderNumbering = true;
		photoshopLayerNumberMaxNum = maxNum;
	}
	
	

	void saveImageFile() {
		if (useSubDirectory) {
			if(useLayers) {
				saveLayer(false);
				return;
			} else {
				
				String fullPathAndName = subDirectory + "\\" + baseName + ".png";
				System.out.println("saveRender and masks to folder: saving " + fullPathAndName);
				GlobalObjects.theDocument.saveRenderToFile(fullPathAndName);
			}
		} else {

			String path = GlobalObjects.theSurface.getUserSessionPath();
			String timeStamp = MOUtils.getDateStamp();
			String fullPathAndName = path + baseName + "_" + timeStamp + ".png";
			System.out.println("saveRender: saving " + fullPathAndName);
			GlobalObjects.theDocument.saveRenderToFile(fullPathAndName);
		}

	}
	
	void saveLayer(boolean clearImage) {
		String name = "img";
		
		if (photoshopLayerOrderNumbering) {
			int thisPhotoshopLayerNum = photoshopLayerNumberMaxNum - imageNumCounter;
			name = name + "_PSLayer_" + thisPhotoshopLayerNum;
		}

		name = name + "_MOLayer_" + imageNumCounter;

		String fullPathAndName = subDirectory + "\\" + name + ".png";
		System.out.println("saveRenderLayers to folder: saving " + fullPathAndName);
		GlobalObjects.theDocument.saveRenderToFile(fullPathAndName);

		imageNumCounter++;
		
		if(clearImage) GlobalObjects.theDocument.clearImage();
	}
	
	

}












	