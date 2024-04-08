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
	// Adding All Images
	// Load images using the embedded DirectoryFileScanner
	//
	public void loadImages() {
		if(directoryFileNameScanner == null) {
			System.out.println("ImageGroup:loadImages -directoryFineNameScanner = null ");
			return;
		}
		//System.out.println("here loadImages");
		ArrayList<String> allPathAndNames = directoryFileNameScanner.getFullPathAndFileNamesList();
		loadImages( allPathAndNames);

		// will print a warning if no files are loaded
		isLoaded();

	}


	//////////////////////////////////////////////////////////////////////////////////////
	// Adding A list of iages Image
	// The reason this needs to be a seperate method is that it is called by the subclass when loading from the cache
	// 
	protected void loadImages(ArrayList<String> allPathAndNames) {
		for(String thisFilePathAndName: allPathAndNames) {
			//System.out.println("loading " + thisFilePathAndName);
			loadImage(thisFilePathAndName);
		}
	}


	//////////////////////////////////////////////////////////////////////////////////////
	// Adding Single Image
	// Load a single image using a a file path and name
	// 
	protected void loadImage(String pathAndName) {
		BufferedImage img = ImageProcessing.loadImage(pathAndName);
		String thisShortFileName = MOStringUtils.getShortFileNameFromFullPathAndFileName(pathAndName);
		addImageAsset(img, thisShortFileName);
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
	// applying processes to all images after load
	//
	///////////////////////////////////////////////////////////////////////////
	//content manipulation methods - affect all members of the group
	//
	public void scaleAll(float x, float y) {

		for (ImageAsset moImage: theImageAssetList) {
			moImage.image = ImageProcessing.scaleImage(moImage.image, x, y);
		}
	}

	public void rotateAll(float rot) {

		for (ImageAsset moImage: theImageAssetList) {
			moImage.image = ImageProcessing.rotateImage(moImage.image, rot);
		}
	}

	public void resizeToAll(int x, int y) {

		for (ImageAsset moImage: theImageAssetList) {
			moImage.image = ImageProcessing.resizeTo(moImage.image, x, y);
		}
	}

	public void cropAll(Rect cropRect) {
		if (cropRect.equals(new Rect())) return;

		for (ImageAsset moImage: theImageAssetList) {
			moImage.image = ImageProcessing.cropImageWithNormalisedRect(moImage.image, cropRect);
		}
	}

	public void addBoarderProportionAll(float left, float top, float right, float bottom) {
		//calculates the new additions as a proportion of the existing width or height

		for (ImageAsset moImage: theImageAssetList) {
			int w = moImage.image.getWidth();
			int h = moImage.image.getHeight();
			int leftAddition = (int) (w * left);
			int topAddition = (int) (h * top);
			int rightAddition = (int) (w * right);
			int bottomAddition = (int) (h * bottom);

			moImage.image = ImageProcessing.addBoarder(moImage.image, leftAddition, topAddition, rightAddition,bottomAddition);
		}

	}

	public void colorTransformAll(MOColorTransform colTransform) {
		System.out.println("in do colour transform all. N umber of images = " + theImageAssetList.size());
		for (ImageAsset moImage: theImageAssetList) {
			moImage.image = colTransform.doColorTransforms(moImage.image);
			moImage.calculateStats();
		}
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



	protected void setAllImageaTo_TYPE_INT_ARGB() {
		// all image content items should be of type INT_ARGB for all the
		// operations to work OK. This makes sure they are.		
		for (ImageAsset moImage : theImageAssetList) {
			moImage.image = ImageProcessing.assertImageTYPE_INT_ARGB(moImage.image);
			}
		
	}



}

