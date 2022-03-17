package MOImageCollections;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.ImageProcessing;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOUtils.GlobalSettings;
import MOUtils.GenericArrayListUtils;
import MOUtils.MOStringUtils;

//////////////////////////////////////////////////////////////////////////////////////////////
//
// SpriteImageGroup
// Inherits from NamedImageGroup. 
// Contains a list of NamedImages, but SpriteImages are scaled by the global SessionScale
// the scaled items are cached for rapid reload next time.
// The group is also named and accessible though the SpriteImageGroupManager
// Intended for storing images ready to be used in Sprites. i.e. session-scaled alpha cut-out images
// A SpriteSeed + A SpriteImage = Sprite


public class SpriteImageGroup extends NamedImageGroup{
	// this is the global session scale as set by the userSession
	// it cannot be different between objects, so is a static
	static float sessionScale = 1;
	
	private String groupName = "";
	
	public SpriteImageGroup(String name) {
		sessionScale = GlobalSettings.getSessionScale();
		groupName = name;
	}
	
	
	public SpriteImageGroup copy(String copyName) {
		SpriteImageGroup cpy = new SpriteImageGroup(copyName);
		
		
		cpy.widthExtrema = this.widthExtrema.copy();
		cpy.heightExtrema = this.heightExtrema.copy();

		
		cpy.cropRect = this.cropRect.copy();
		cpy.preScale = this.preScale;

		cpy.copyNamedImagesFromOtherGroup(this);
		
		return cpy;
	}
	
	public boolean isNamed(String name) {
		if (groupName.contentEquals(name))
			return true;
		return false;
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	
	public float getRelativeSize(int num) {
		// this is used by the sprite to establish a particular 
		// image's relative height to the others in the group
		int imgHght = getImage(num).getHeight();
		float heightMaxItem = heightExtrema.getUpper();
		return  imgHght / heightMaxItem;
	}
	
	
	public void loadSamples() {
		// loadSamples is different to loadImages in that it applies the sessionScale to the image 
		// and deals with caching of sessionScaled versions of the images for subsequent uses, therefore speeding up the system.
		// 
		// pre-scaling and cropping only takes place after the cache save/load so is never committed to the cache
		// however preScaling and cropping takes place immediately per image load when loading images at full scale, or upon loading pre-cached images
		
		// If no session scale is applied (i.e.) the render is a Full-Size
		// just use the base-class load image. This will do preScaling and cropping if required
		if (sessionScale > 0.99) {
			// System.out.println("NOT using cache folder ...");

			// this base class method loads at 100% scale, and then does the preScaling and
			// rect cropping.
			super.loadImages();
			assertImageTYPE_INT_ARGB();
			return;
		}

		// if needs rescaling due to session scale, then the cache comes into play.
		// check to see if a folder called targetDirectory//cached_scaled_*percentile*
		// exists
		String cachedImagesFolderName = getCachedScaledImagesFolderName();
		// first get the list of files, as they should be, in the non-cache directory
		// (the original files in the sample lib)
		//ArrayList<String> sampleLibFileNames = getShortFileNamesInDirectory(directoryPath, fileStringEndsWith, fileStringContains);
				

				
		
		if (checkCacheOK(cachedImagesFolderName)) {
			// if the cache exists and  the images are all present in the cache then load those images using the base class load, end.
			// They are already scaled as that's the whole point of the cache...
			// The base class loadImage does any preScaling and cropping
			// System.out.println("loading from cache folder ..." + cachedImagesFolderName);
			DirectoryFileNameScanner cacheFileNameScanner = new DirectoryFileNameScanner(cachedImagesFolderName);
			cacheFileNameScanner.copyConstraints(directoryFileNameScanner);
			ArrayList<String> filesInCache = cacheFileNameScanner.getFullPathAndFileNamesList();
			super.loadImages(filesInCache);
			
			assertImageTYPE_INT_ARGB();
			return;
		}
		
		// If you get to here, then the cache needs making.  you need to 
		// 1/ load the full size image
		// 2/ apply theSessionScale, 
		// 3/ save this scaled version to the cache
		// 4/ apply any preScel or crop
		
		boolean ok = MOStringUtils.createDirectory(cachedImagesFolderName);
		if (!ok){
			// cannot create required cache folder
			System.out.println("problem creating cache folder ..." + cachedImagesFolderName);
			return;
		}
		
		ArrayList<String> filesToLoad = directoryFileNameScanner.getFullPathAndFileNamesList();		
		for(String thisImagePathAndName: filesToLoad) {
			loadFullSizeImage_SessionScale_Cache_PrescaleCrop(thisImagePathAndName, cachedImagesFolderName);
		}
		assertImageTYPE_INT_ARGB();
		
		// will write a comment to console if no images are loaded
		isLoaded();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	//content manipulation methods - affect all members of the group
	//
	public void scaleAll(float x, float y) {
		
		for (NamedImage imageSample: imageList) {
			imageSample.image = ImageProcessing.scaleImage(imageSample.image, x, y);
		}
	}

	public void rotateAll(float rot) {
		
		for (NamedImage imageSample: imageList) {
			imageSample.image = ImageProcessing.rotateImage(imageSample.image, rot);
		}
	}

	public void resizeToAll(int x, int y) {
		
		for (NamedImage imageSample: imageList) {
			imageSample.image = ImageProcessing.resizeTo(imageSample.image, x, y);
		}
	}

	public void cropAll(Rect cropRect) {
		if (cropRect.equals(new Rect())) return;
		
		for (NamedImage imageSample: imageList) {
			imageSample.image = ImageProcessing.cropImageWithNormalisedRect(imageSample.image, cropRect);
		}
	}

	public void addBoarderProportionAll(float left, float top, float right, float bottom) {
		//calculates the new additions as a proportion of the existing width or height

		for (NamedImage imageSample: imageList) {
			int w = imageSample.image.getWidth();
			int h = imageSample.image.getHeight();
			int leftAddition = (int) (w * left);
			int topAddition = (int) (h * top);
			int rightAddition = (int) (w * right);
			int bottomAddition = (int) (h * bottom);

			imageSample.image = ImageProcessing.addBoarder(imageSample.image, leftAddition, topAddition, rightAddition,bottomAddition);
		}

	}

	public void colorTransformAll(int function, float p1, float p2, float p3) {
		
		for (NamedImage imageSample: imageList) {
			imageSample.image = ImageProcessing.colorTransform(imageSample.image, function, p1, p2, p3);
		}
	}
	
	

	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Private methods below here
	//
	
	private void loadFullSizeImage_SessionScale_Cache_PrescaleCrop(String fullSizeImagePathAndName, String cacheDirectoryPath) {
		BufferedImage img = ImageProcessing.loadImage(fullSizeImagePathAndName);
		if(sessionScale != 1) {
			img = ImageProcessing.scaleImage(img,sessionScale,sessionScale);
		}
		String thisShortFileName = MOStringUtils.getShortFileNameFromFullPathAndFileName(fullSizeImagePathAndName);
		String fullCachePathAndName = cacheDirectoryPath + thisShortFileName + ".png";
		ImageProcessing.saveImage(fullCachePathAndName, img);
		
		
		// NOW apply preScale and crop if any
		img = applyPreScaleAndCrop(img);
		
		// NOW add the image to the list
		addNamedImage( img, thisShortFileName);
	}
	
	
	
		
	
	
	private void assertImageTYPE_INT_ARGB() {
		// all image content items should be of type INT_ARGB for all the
		// operations to work OK. This makes sure they are.		
		for (NamedImage imageSample : imageList) {
			if (imageSample.image.getType() != BufferedImage.TYPE_INT_ARGB) {
				imageSample.image = ImageProcessing.convertColorModel(imageSample.image, BufferedImage.TYPE_INT_ARGB);
			}
		}
	}
	
	
	////////////
	private String getCachedScaledImagesFolderName() {
		int scalePercentile = (int) (sessionScale * 100);
		String cachFolderRoot = GlobalSettings.getMouseOrganImageCachePath();
		String originalTargetDirectory = directoryFileNameScanner.getTargetDirectory();
		String strippedDirectoryPath = originalTargetDirectory.replace(GlobalSettings.getSampleLibPath(), "");
		// System.out.println("directoryPath ..." + directoryPath);
		// System.out.println("strippedDirectoryPath ..." + strippedDirectoryPath);
		String cachedFolderName = cachFolderRoot + strippedDirectoryPath + "\\cached_scaled_" + scalePercentile + "\\";
		
		System.out.println("scaled images cache directory ..." + cachedFolderName);
		return cachedFolderName;
	}
	
	// this checks to see if the directory exists, and if it does then it checks to
	// see if all the cached files exist also.
	// if not then return false.
	private boolean checkCacheOK(String cacheDirectory) {
		if (MOStringUtils.checkDirectoryExist(cacheDirectory) == false)
			return false;
		
		ArrayList<String> expectedFiles = directoryFileNameScanner.getShortNameList();
		
		DirectoryFileNameScanner cacheFileNameScanner = new DirectoryFileNameScanner(cacheDirectory);
		ArrayList<String> filesInCache = cacheFileNameScanner.getShortNameList();

		if (GenericArrayListUtils.listsAContainedInB(expectedFiles, filesInCache))
			return true;

		return false;

	}
	
}// end of class
	
	







