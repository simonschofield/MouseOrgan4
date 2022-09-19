package MOImageCollections;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.ImageProcessing;
import MOMaths.Range;
import MOMaths.Rect;
import MOUtils.MOStringUtils;

//////////////////////////////////////////////////////////////////////////////////////////////
//
// ImageAssetGroup
// Contains a list of ImageAssets where only the image and name is used. Each name is unique.
// While the class uses the ImageAsset type to bind names to BufferedImages, this should be used to store collections of
// regular images, such as in the SceneData3D class, where collections of texture images are collected.
// 
// 
// Images can be added en-mass from a target director using a DirectoryFileNameScanner. This method can be repeated
// by repeatedly setting the DirectoryFileNameScanner and calling loadImages()
// or one at a time using addImage() method
// or copied from another group.


public class ImageAssetGroup {

	protected DirectoryFileNameScanner directoryFileNameScanner;
	
	protected ArrayList<ImageAsset> theImageAssetList = new ArrayList<ImageAsset>();
	
	
	protected Range widthExtrema = new Range();
	protected Range heightExtrema = new Range();

	
	Rect cropRect = new Rect();
	float preScale = 1f;
	
	

	public ImageAssetGroup() {
		
	}
	
	public void calculateImageStats() {
		for (ImageAsset thisImage : theImageAssetList) {
			thisImage.calculateStats();
		}
	}
	
	public void setDirectoryFileNameScanner(DirectoryFileNameScanner dfns){
		directoryFileNameScanner = dfns;
	}
	
	
	public void setCrop(Rect r) {
		cropRect = r.copy();
	}
	
	public void setPreScale(float s) {
		preScale = s;
		
	}
	
	
	void clearImageAssets() {
		theImageAssetList = new ArrayList<ImageAsset>();
	}
	
	
	// debug only
	public void printImageNames() {
		ArrayList<String> names =  getImageAssetNamesList();
		System.out.println("ImageGroup: has loaded the following image");
		for(String thisName: names) {
			System.out.println(thisName);
		}
	}
	


	//////////////////////////////////////////////////////////////////////////////////////
	// Adding Images
	// Load images using the embedded DirectoryFileScanner
	//
	public void loadImages() {
		if(directoryFileNameScanner == null) {
			System.out.println("ImageGroup:loadImages -directoryFineNameScanner = null ");
			return;
		}
		
		ArrayList<String> allPathAndNames = directoryFileNameScanner.getFullPathAndFileNamesList();
		loadImages(allPathAndNames);
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	// Adding Images
	// Load a list of images using a list of file names
	// 
	protected void loadImages(ArrayList<String> allPathAndNames) {
		
		for(String thisFilePathAndName: allPathAndNames) {
			loadImage(thisFilePathAndName);
		}
		
		// will print a warning if no files are loaded
		isLoaded();

	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	// Adding Images
	// Load a single image using a a file path and name
	// 
	protected void loadImage(String pathAndName) {
		
		BufferedImage img = ImageProcessing.loadImage(pathAndName);

		img = applyPreScaleAndCrop(img);

		String thisShortFileName = MOStringUtils.getShortFileNameFromFullPathAndFileName(pathAndName);
		addImageAsset(img, thisShortFileName);
	}
	
	
	protected BufferedImage applyPreScaleAndCrop(BufferedImage img) {
		if(cropRect.equals(new Rect())==false) {
			// crop rect is in parametric form, need to turn this into actual pixel values for this image
			img = ImageProcessing.cropImageWithNormalisedRect(img,cropRect);
		}	

		if(preScale < 1) {
			img = ImageProcessing.scaleImage(img, preScale, preScale);
		}
		return img;
	}



	//////////////////////////////////////////////////////////////////////////////////////
	// Adding Images
	// Adds a single image with a name
	// only add images to the images list through this method
	//
	public void addImageAsset(BufferedImage img, String shortName) {
		
		if(checkUniqueName(shortName)==false) return;
		
		ImageAsset namedImage = new ImageAsset();
		namedImage.image = img;
		namedImage.name = shortName;
		
		theImageAssetList.add(namedImage);
		widthExtrema.addExtremaCandidate(img.getWidth());
		heightExtrema.addExtremaCandidate(img.getHeight());
		
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	// Adding Images
	// copying from another image group
	// Makes a deep copy of the images so will increase memory footprint
	//
	public void copyImageAssetsFromOtherGroup(ImageAssetGroup otherGroup) {
		int i = otherGroup.getNumImageAssets();
		for(int n = 0; n < i; n++) {
			copyImageAssetsFromOtherGroup( n,  otherGroup);
		}
		
	}
	
	
	public void copyImageAssetsFromOtherGroup(int n, ImageAssetGroup otherGroup) {
		// makes a new independent copy
		BufferedImage img = otherGroup.getImage(n);
		String imageName = otherGroup.getImageAssetName(n);
		BufferedImage copyOfImage = ImageProcessing.copyImage(img);
		addImageAsset(copyOfImage, imageName);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////
	//
	// data access methods
	//
	public int getNumImageAssets() {
		return theImageAssetList.size();
	}
	
	public ImageAsset getImageAsset(int n) {
		if( checkLegalIndex(n)==false) return null;
		return theImageAssetList.get(n);
	}

	public BufferedImage getImage(int n) {
		if( checkLegalIndex(n)==false) return null;
		if (getNumImageAssets() == 0) {
			System.out.println("getImage:: ImageGroup has no images ");
			return null;
		}
		BufferedImage img = theImageAssetList.get(n).image;
		//System.out.println("reference to image in Group is " + img);
		return img;
	}
	

	public BufferedImage getImage(String shortName) {
		int n = 0;
		for (ImageAsset thisImage : theImageAssetList) {
			if (thisImage.name.contentEquals(shortName))
				return thisImage.image;
			n++;
		}
		System.out.println("ImageGroup:getImage - cannot find image called " + shortName);
		return null;
	}
	
	
	public String getImageAssetName(int n) {
		if( checkLegalIndex(n)==false) return null;
		return theImageAssetList.get(n).name;
		
	}

	public int getIndexOfImageAsset(String shortName) {
		int n = 0;
		for (ImageAsset thisImage : theImageAssetList) {
			if (thisImage.name.contentEquals(shortName))
				return n;
			n++;
		}
		System.out.println("ImageGroup:getIndexOfImageShortName - cannot find image called " + shortName);
		return 0;
	}
	
	public ArrayList<String> getImageAssetNamesList() {
		ArrayList<String> imageNames = new ArrayList<String>();
		for (ImageAsset thisImage : theImageAssetList) {
			imageNames.add(thisImage.name);
		}
		return imageNames;
	}
	
	public void  replaceImage(BufferedImage newImage, String newName, int n) {
		// replaces an image within an existing image asset
		// identified by index n
		if( checkLegalIndex(n)==false) return;
		
		ImageAsset moImg = theImageAssetList.get(n);
		moImg.image = newImage;
		moImg.name = newName;
		moImg.calculateStats();
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////
	// private
	//

	protected boolean checkLegalIndex(int i) {
		int n = getNumImageAssets();
		if(i >= 0 && i < n) return true;
		System.out.println("ImageAssetsGroup:checkLegalIndex - index out of range " + i + "from a group of " + n + " images");
		return false;
	}

	protected boolean isLoaded() {

		if(theImageAssetList.size()==0) {
			System.out.println("ImageAssetsGroup: no images have been loaded - call loadImage() ?");
			return false;
		}
		return true;
	}


	private boolean checkUniqueName(String newName) {

		for (ImageAsset thisImage : theImageAssetList) {
			if (thisImage.name.contentEquals(newName))
			{
				System.out.println("ImageAssetsGroup:checkUniqueName- attempting to add duplicate named image " + newName);
				return false;
			}
		}
		return true;

	}
	
	
	
	



}

