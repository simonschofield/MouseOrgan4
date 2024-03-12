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
// ScaledImageAssetGroup
// Inherits from ImageAssetGroup. 
// but the images are scaled by the global SessionScale upon load.
// The scaled items are cached for rapid reload next time.
// The group is also named and accessible though the SpriteImageGroupManager
// Intended for storing images ready to be used in Sprites. i.e. session-scaled alpha cut-out images
// A SpriteSeed + A SpriteImage = Sprite


public class ScaledImageAssetGroup extends ImageAssetGroup{
	// this is the global session scale as set by the userSession
	// it cannot be different between objects, so is a static
	
	
	
	static float sessionScale = 1;
	
	private String groupName = "";
	
	private MOColorTransform preCacheColorTransforms = null;
	
	
	public ScaledImageAssetGroup(String name) {
		sessionScale = GlobalSettings.getSessionScale();
		groupName = name;
	}
	
	void setPreCacheImageProcessingOperationsList(MOColorTransform colTransforms) {
		preCacheColorTransforms = colTransforms;
		
		
		// can check here to see if its changed
		
	}
	
	
	public ScaledImageAssetGroup copy(String copyName) {
		ScaledImageAssetGroup cpy = new ScaledImageAssetGroup(copyName);
		
		
		cpy.widthExtrema = this.widthExtrema.copy();
		cpy.heightExtrema = this.heightExtrema.copy();

		
		cpy.cropRect = this.cropRect.copy();
		cpy.preScale = this.preScale;

		cpy.copyImageAssetsFromOtherGroup(this);
		
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
	
	
	public float getRelativeImageHeight(int num) {
		// this is used by the sprite to establish a particular 
		// image's relative height to the others in the group
		int imgHght = getImage(num).getHeight();
		float heightMaxItem = heightExtrema.getUpper();
		return  imgHght / heightMaxItem;
	}
	
	public float getRelativeImageWidth(int num) {
		// this is used by the sprite to establish a particular 
		// image's relative width to the others in the group
		int imgWdth = getImage(num).getWidth();
		float widthMaxItem = widthExtrema.getUpper();
		return  imgWdth / widthMaxItem;
	}
	
	
	public void loadSessionScaledImages() {
		// loadSessionScaledImages is different to the superclass loadImages in that it applies the sessionScale and possibly a set of image processing operations to the image 
		// and deals with caching of sessionScaled versions of the images for subsequent uses, therefore speeding up the system.
		// 
		// pre-scaling and cropping only takes place after the cache save/load so is never committed to the cache
		// however preScaling and cropping takes place immediately per image load when loading images at full scale, or upon loading pre-cached images
		
		// If no session scale is applied (i.e.) the render is a Full-Size
		// just use the base-class load image. This will do preScaling and cropping if required
		// however IF the preCacheImageProcessingOperationsList is set, then we have to cache at all scales including 100%
		
		boolean colorTransformsHaveChanged = colorTransformParametersHaveChanged();
		System.out.println("color tansforms changed = " + colorTransformsHaveChanged);
		
		if (sessionScale > 0.99 && colorTransformsHaveChanged==false && preCacheColorTransforms==null) {
			// This is when no scaling is required, and no color transform is being used, so just use the original sample lib files
			super.loadImages();
			assertImageTYPE_INT_ARGB();
			return;
		}

		// if needs rescaling due to session scale or pre-image-processing, then the cache comes into play.
		// check to see if a folder called targetDirectory//cached_scaled_*percentile*
		// exists
		String cachedImagesFolderName = getCachedScaledImagesFolderName();
		// first get the list of files, as they should be, in the non-cache directory
		// (the original files in the sample lib)
		//ArrayList<String> sampleLibFileNames = getShortFileNamesInDirectory(directoryPath, fileStringEndsWith, fileStringContains);
				

				
		
		if (checkCacheOK(cachedImagesFolderName) && colorTransformsHaveChanged==false) {
			// IF the cache is OK and no changes have been made them load the cache....
			// if the cache exists and  the images are all present, and the color transform has not been changed betwee sessions,
			// load the chached images images using the base class load, end.
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
		// 3/ Apply ant image-processing operations
		// 4/ save this scaled version to the cache
		// 5/ apply any preScel or crop
		
		boolean ok = MOStringUtils.createDirectory(cachedImagesFolderName);
		if (!ok){
			// cannot create required cache folder
			System.out.println("problem creating cache folder ..." + cachedImagesFolderName);
			return;
		}
		
		ArrayList<String> filesToLoad = directoryFileNameScanner.getFullPathAndFileNamesList();		
		
		// we need to ensure the cached images are saved with the best quality
		int currentInterpolationQuality = ImageProcessing.getInterpolationQuality();
		ImageProcessing.setInterpolationQuality(ImageProcessing.INTERPOLATION_BICUBIC);
		
		
		for(String thisImagePathAndName: filesToLoad) {
			loadFullSizeImage_SessionScale_Process_Cache_PrescaleCrop(thisImagePathAndName, cachedImagesFolderName);
		}
		assertImageTYPE_INT_ARGB();
		
		ImageProcessing.setInterpolationQuality(currentInterpolationQuality);
		
		// will write a comment to console if no images are loaded
		isLoaded();
		
		cacheColorTransformParameters();
	}
	
	
	private void loadFullSizeImage_SessionScale_Process_Cache_PrescaleCrop(String fullSizeImagePathAndName, String cacheDirectoryPath) {
		BufferedImage img = ImageProcessing.loadImage(fullSizeImagePathAndName);
		if(img == null) {
			System.out.println("loadFullSizeImage_SessionScale_Process_Cache_PrescaleCrop::image == null - could not be loaded");
			
		}
		

		if(sessionScale != 1) {
			img = ImageProcessing.scaleImage(img,sessionScale);
		}
		
		if(preCacheColorTransforms != null) {
			img = preCacheColorTransforms.doColorTransforms(img);
			if(img == null) {
				System.out.println("loadFullSizeImage_SessionScale_Process_Cache_PrescaleCrop::image == null after color transform");
				
			}
		}
		
		String thisShortFileName = MOStringUtils.getShortFileNameFromFullPathAndFileName(fullSizeImagePathAndName);
		String fullCachePathAndName = cacheDirectoryPath + thisShortFileName + ".png";
		ImageProcessing.saveImage(fullCachePathAndName, img);
		
		
		// NOW apply preScale and crop if any
		img = applyPreScaleAndCrop(img);
		
		// NOW add the image to the list
		addImageAsset( img, thisShortFileName);
	}
	
	
	
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
		for (ImageAsset moImage: theImageAssetList) {
			moImage.image = colTransform.doColorTransforms(moImage.image);
			moImage.calculateStats();
		}
	}
	
	

	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Private methods below here
	//
	
	
	
	
	
	
	 
		
	
	
	private void assertImageTYPE_INT_ARGB() {
		// all image content items should be of type INT_ARGB for all the
		// operations to work OK. This makes sure they are.		
		for (ImageAsset moImage : theImageAssetList) {
			moImage.image = ImageProcessing.assertImageTYPE_INT_ARGB(moImage.image);
			}
		
	}
	
	private boolean colorTransformParametersHaveChanged() {
		boolean isDirty = false;
		
		
		String chachePath = getColorTransformParameterCachePath();
		// check to see if the directory exists
		
		boolean directoryExists = MOStringUtils.checkDirectoryExist(chachePath);
		
		if(directoryExists==false && preCacheColorTransforms == null) {
			// the color transforms were not, and are not being used, so nothing can have changed
			System.out.println("The color transforms were not, and are not being used, so nothing can have changed");
			isDirty = false;
		}
		

		if(directoryExists && preCacheColorTransforms == null)	{
			// the color transform was previously being used but is no longer being used anymore, so re-cache the content without color transforms
			System.out.println("the color transform was previously being used but is no longer being used anymore, so re-cache the content without color transforms");
			isDirty = true;
		}
		
		if(directoryExists==false && preCacheColorTransforms != null)	{
			// the color transform is now being used, , so re-cache the content  color transforms
			System.out.println("the color transform is now being used, , so re-cache the content with the color transforms");
			isDirty = true;
		}
		
		if(directoryExists && preCacheColorTransforms != null) {
			// compare the saved color transform to this one.
			
			MOColorTransform cachedColorTransform = new MOColorTransform();
			cachedColorTransform.loadParameters(chachePath + "parameters.csv");
			
			if(preCacheColorTransforms.equals(cachedColorTransform)) {
				System.out.println("the color transform has NOT changed this session");
				isDirty=false;
			} else {
				System.out.println("the color transform HAS changed this session");
				isDirty=true;
			}
			
		}
		
		return isDirty;
		
	}
	
	
	void cacheColorTransformParameters() {
		String chachePath = getColorTransformParameterCachePath();
		if(preCacheColorTransforms==null) {
			MOStringUtils.deleteDirectory(chachePath);
			return;
		}
		
		MOStringUtils.createDirectory(chachePath);
		preCacheColorTransforms.saveParameters(chachePath + "parameters.csv");
		
	}
	
	public String getColorTransformParameterCachePath() {
		return getCachedScaledImagesFolderName() + "ColorTransformParameters\\";
	}
	
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
	
	







