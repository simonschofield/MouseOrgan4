package ImageCollectionClasses;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImageClasses.ImageProcessing;
import MOMaths.Range;
import MOMaths.Rect;
import MOUtils.MOStringUtils;

//////////////////////////////////////////////////////////////////////////////////////////////
//
// ImageItemGroup
// A Simple list of ImageSamples where only the image and name is used. Each name is unique.
// While the class uses the ImageSample type to bind names to BufferedImages, this should be used to store collections of
// regular images, such as in the SceneData3D class, where collections of texture images are collected.
// 
// 
// Images can be added en-mass from a target director using a DirectoryFileNameScanner. This method can be repeated
// by repeatedly setting the DirectoryFileNameScanner and calling loadImages()
// or one at a time using addImage() method
// or copied from another group.


public class ImageItemGroup {

	protected DirectoryFileNameScanner directoryFileNameScanner;
	
	protected ArrayList<ImageItem> imageList = new ArrayList<ImageItem>();
	
	
	Range widthExtrema = new Range();
	Range heightExtrema = new Range();

	
	Rect cropRect = new Rect();
	float preScale = 1f;
	
	

	public ImageItemGroup() {
		
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
	
	
	void clearImages() {
		imageList = new ArrayList<ImageItem>();
		
		
	}
	
	
	// debug only
	public void printImageNames() {
		ArrayList<String> names =  getImageNameList();
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
		addImage(img, thisShortFileName);
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
	public void addImage(BufferedImage img, String shortName) {
		
		if(checkUniqueName(shortName)==false) return;
		
		ImageItem namedImage = new ImageItem();
		namedImage.image = img;
		namedImage.name = shortName;
		
		imageList.add(namedImage);
		widthExtrema.addExtremaCandidate(img.getWidth());
		heightExtrema.addExtremaCandidate(img.getHeight());
		
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	// Adding Images
	// copying from another image group
	// Makes a deep copy of the images so will increase memory footprint
	//
	public void copyImagesFromOtherGroup(ImageItemGroup otherGroup) {
		int i = otherGroup.getNumImages();
		for(int n = 0; n < i; n++) {
			copyImageFromOtherGroup( n,  otherGroup);
		}
		
	}
	
	
	public void copyImageFromOtherGroup(int n, ImageItemGroup otherGroup) {
		// makes a new independent copy
		BufferedImage img = otherGroup.getImage(n);
		String imageName = otherGroup.getImageName(n);
		BufferedImage copyOfImage = ImageProcessing.copyImage(img);
		addImage(copyOfImage, imageName);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////
	//
	// data access methods
	//
	public int getNumImages() {
		return imageList.size();
	}

	public BufferedImage getImage(int n) {
		if( checkLegalIndex(n)==false) return null;
		if (getNumImages() == 0) {
			System.out.println("getImage:: ImageGroup has no images ");
			return null;
		}
		return imageList.get(n).image;
		
	}
	

	public BufferedImage getImage(String shortName) {
		int n = 0;
		for (ImageItem thisImage : imageList) {
			if (thisImage.name.contentEquals(shortName))
				return thisImage.image;
			n++;
		}
		System.out.println("ImageGroup:getImage - cannot find image called " + shortName);
		return null;
	}
	
	
	public String getImageName(int n) {
		if( checkLegalIndex(n)==false) return null;
		return imageList.get(n).name;
		
	}

	public int getIndexOfImageShortName(String shortName) {
		int n = 0;
		for (ImageItem thisImage : imageList) {
			if (thisImage.name.contentEquals(shortName))
				return n;
			n++;
		}
		System.out.println("ImageGroup:getIndexOfImageShortName - cannot find image called " + shortName);
		return 0;
	}
	
	public ArrayList<String> getImageNameList() {
		ArrayList<String> imageNames = new ArrayList<String>();
		for (ImageItem thisImage : imageList) {
			imageNames.add(thisImage.name);
		}
		return imageNames;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////
	// private
	//

	protected boolean checkLegalIndex(int i) {
		int n = getNumImages();
		if(i >= 0 && i < n) return true;
		System.out.println("ImageGroup:checkLegalIndex - index out of range " + i + "from a group of " + n + " images");
		return false;
	}

	protected boolean isLoaded() {

		if(imageList.size()==0) {
			System.out.println("ImageGroup: no images have been loaded - call loadImage() ?");
			return false;
		}
		return true;
	}


	private boolean checkUniqueName(String newName) {

		for (ImageItem thisImage : imageList) {
			if (thisImage.name.contentEquals(newName))
			{
				System.out.println("ImageGroup:checkUniqueName- attempting to add duplicate named image " + newName);
				return false;
			}
		}
		return true;

	}
	
	
	
	



}

