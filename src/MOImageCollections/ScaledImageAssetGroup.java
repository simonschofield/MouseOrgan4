package MOImageCollections;


import java.awt.image.BufferedImage;
import java.util.ArrayList;
import MOImage.ImageProcessing;
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

	//boolean deferCache = true;
	
	
	/////////////////////////////////////////////////////////////////////////
	// CACHE MODES
	//
	// CACHEMODE_ADAPTIVE_LOADANDSAVE is the default mode, and should be used if no post processing is involved. It will
	// load the cache if the cache is OK. Else it loads from the source-library, and then saves the result to the cache for 
	// later use. It does not cache load or save 100% scaled assets, as it is quicker to just load the source-library files directly
	//
	// CACHEMODE_NONE. Never loads or saves the cache. Used to simply load from the source-lib. Probably used with saveCache() method later on after
	// some post-load processing has happened that you want to commit to the cache, after which you should probably use CACHEMODE_FORCE_LOAD_NO_SAVE.
	//
	// CACHEMODE_FORCE_LOAD_NO_SAVE forces the load to be from the cache only. If the cache is NOT OK, the process will terminate. This should be used to 
	// load post-load processed images, which have been previously committed to the cache through using a previous saveCache().
	//
	//
	public static final int	CACHEMODE_ADAPTIVE_LOADANDSAVE = 0;  // 
	public static final int	CACHEMODE_NONE = 1;
	public static final int	CACHEMODE_FORCE_LOAD_NO_SAVE = 2;
	
	private int currentCacheMode = CACHEMODE_ADAPTIVE_LOADANDSAVE;
	
	public ScaledImageAssetGroup(String name) {
		sessionScale = GlobalSettings.getSessionScale();
		groupName = name;
		
	}
	
	
	
	
	public ScaledImageAssetGroup copy(String copyName) {
		ScaledImageAssetGroup cpy = new ScaledImageAssetGroup(copyName);
		
		
		cpy.widthExtrema = this.widthExtrema.copy();
		cpy.heightExtrema = this.heightExtrema.copy();


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
	
	public void setCacheMode(int cacheMode) {
		currentCacheMode = cacheMode;
	}
	
	public int getCacheMode() {
		return currentCacheMode;
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
		// loadSessionScaledImages is different to the superclass loadImages in that it applies the sessionScale to the image 
		// and deals with caching of sessionScaled versions of the images for subsequent uses, therefore speeding up the system.
		// 

		if(currentCacheMode == CACHEMODE_ADAPTIVE_LOADANDSAVE) {
			loadImages_CACHEMODE_ADAPTIVE_LOADANDSAVE();
			return;
		}
		
		if(currentCacheMode == CACHEMODE_NONE) {
			loadImages_CACHEMODE_NONE();
			return;
		}
		
		if(currentCacheMode == CACHEMODE_FORCE_LOAD_NO_SAVE) {
			loadImages_CACHEMODE_FORCE_LOAD_NO_SAVE();
			return;
		}
		
		
		System.out.println("ScaledImageAssetGroup::loadSessionScaledImages ...the cache mode is set to an illegal value...  " + currentCacheMode + " fatal error");
		System.exit(0);
		
	}
	
	public void cacheImages() {
			
			String cachedImagesFolderName = getCachedScaledImagesFolderName();
			
			boolean ok = MOStringUtils.createDirectory(cachedImagesFolderName);
			if (!ok){
				// cannot create required cache folder
				System.out.println("problem creating cache folder ..." + cachedImagesFolderName);
				return;
			}
			
			// we need to ensure the cached images are saved with the best quality
			for (ImageAsset moImage: theImageAssetList) {
				String thisShortFileName = moImage.name;
				String fullCachePathAndName = cachedImagesFolderName + thisShortFileName + ".png";
				ImageProcessing.saveImage(fullCachePathAndName, moImage.image);
			}
			System.out.println("cacheImages:: cached " + theImageAssetList.size() + " images");
	
		}
			

	public void overlayScaledImageAssetGroup(ScaledImageAssetGroup overlayGroup, float alpha) {
		// Overlay the images in overlayGroup onto this group
		// this assumes that the overlay is the same dimensions as the target image and there is the same number of images
		// in the overlay group as in the target group.
		// 
		
		if(this.getNumImageAssets() != overlayGroup.getNumImageAssets()) {
			
			// throw a wobble
			System.out.println("ScaledImageAssetGroup::overlayScaledImageAssetGroup:: image collections not the same in number");
		}
		
		for(int n = 0; n< this.getNumImageAssets(); n++) {
			ImageAsset overlay = overlayGroup.getImageAsset(n);
			ImageAsset target = this.getImageAsset(n);
			
			target.image = ImageProcessing.getCompositeImage(overlay.image, target.image, 0, 0, alpha);
			
		}
		
		
	}
	
	/////////////////////////////////////////////////////////////////////////
	// Private methods cache-related methods
	//
	// 
	private void loadImages_CACHEMODE_ADAPTIVE_LOADANDSAVE() {

		if (sessionScale > 0.99) {
			// If no session scale is applied (i.e.) the render is a Full-Size
			// just use the base-class load image to load the 100% assetLib images.
			// 
			super.loadImages();
			setAllImageaTo_TYPE_INT_ARGB();
			return;
		}

		// if you get this far, then check to see if the cache exists and is OK
		// If so, then load from the cache and return
		//
		String cachedImagesFolderName = getCachedScaledImagesFolderName();

		if (checkCacheOK(cachedImagesFolderName)) {
			
			// System.out.println("loading from cache folder ..." + cachedImagesFolderName);
			loadCachedImages();
			return;
		}
		
		// If you get to here, you need to 
		// 1/ load the full size images
		// 2/ apply theSessionScale, 
		// 3/ save the scaled version to the cache
		loadFullSizeImagesAndSessionScale();
		cacheImages();

		isLoaded();
	}
	
	private void loadImages_CACHEMODE_NONE() {
		
		loadFullSizeImagesAndSessionScale();
		isLoaded();
	}
	
	private void loadImages_CACHEMODE_FORCE_LOAD_NO_SAVE() {
		
		String cachedImagesFolderName = getCachedScaledImagesFolderName();

		if (checkCacheOK(cachedImagesFolderName)) {
			// System.out.println("loading from cache folder ..." + cachedImagesFolderName);
			loadCachedImages();
		} else {
			 System.out.println("ScaledImageAssetGroup::loadImages_CACHEMODE_FORCE_LOAD_NO_SAVE ...the cache " + cachedImagesFolderName + " is NOT OK fatal error");
			 System.exit(0);
		}
		
	}
	
	
	
	private void loadCachedImages() {
		String cachedImagesFolderName = getCachedScaledImagesFolderName();
		DirectoryFileNameScanner cacheFileNameScanner = new DirectoryFileNameScanner(cachedImagesFolderName);
		cacheFileNameScanner.copyConstraints(directoryFileNameScanner);
		ArrayList<String> filesInCache = cacheFileNameScanner.getFullPathAndFileNamesList();
		super.loadImages(filesInCache);
		
		setAllImageaTo_TYPE_INT_ARGB();
		
		
		System.out.println("loadCachedImages:: loaded " + theImageAssetList.size() + " images");
	}
	
	
	private void loadFullSizeImagesAndSessionScale() {
		// Loads and scales each image separately so as not to use a lot of memory
		ArrayList<String> filesToLoad = directoryFileNameScanner.getFullPathAndFileNamesList();		
		for(String s: filesToLoad) {
			loadFullSizeImage_SessionScale(s);
		}
		System.out.println("loadFullSizeImagesAndSessionScale:: loaded " + theImageAssetList.size() + " images");
	}
	
	private void loadFullSizeImage_SessionScale(String fullSizeImagePathAndName) {
		// called by the method above
			
		BufferedImage img = ImageProcessing.loadImage(fullSizeImagePathAndName);
		if(img == null) {
			System.out.println("loadFullSizeImage_SessionScale::image == null - could not be loaded");
		
		}
	
		int currentInterpolationQuality = ImageProcessing.getInterpolationQuality();
		ImageProcessing.setInterpolationQuality(ImageProcessing.INTERPOLATION_BICUBIC);
		if(sessionScale != 1) {
			img = ImageProcessing.scaleImage(img,sessionScale);
		}
		ImageProcessing.setInterpolationQuality(currentInterpolationQuality);
		// we need to ensure the cached images are saved with the best quality
		String thisShortFileName = MOStringUtils.getShortFileNameFromFullPathAndFileName(fullSizeImagePathAndName);
		img = ImageProcessing.assertImageTYPE_INT_ARGB(img);
		addImageAsset( img, thisShortFileName);
			
		
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
	
		if (MOStringUtils.checkDirectoryExist(cacheDirectory) == false) return false;
		
		if (MOStringUtils.getDirectoryContentFileCount(cacheDirectory) == 0) return false;
		
		ArrayList<String> expectedFiles = directoryFileNameScanner.getShortNameList();

		DirectoryFileNameScanner cacheFileNameScanner = new DirectoryFileNameScanner(cacheDirectory);
		ArrayList<String> filesInCache = cacheFileNameScanner.getShortNameList();

		if (GenericArrayListUtils.listsAContainedInB(expectedFiles, filesInCache))
			return true;

		return false;

	}
	
	
	
}// end of class
	
	







