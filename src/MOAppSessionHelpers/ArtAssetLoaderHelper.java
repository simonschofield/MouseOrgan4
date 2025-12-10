package MOAppSessionHelpers;

import MOImageCollections.MOColorTransform;
import MOImageCollections.ScaledImageAssetGroup;
import MOImageCollections.ScaledImageAssetGroupManager;
import MOUtils.GlobalSettings;
import MOUtils.MOStringUtils;

/**
 * This is a "soft coded" helper class. i.e. it is expected to have code added by the user. In this case, new pre-procesing operations for new image-caches.<p>
 * 
 * Used in UserSession.loadContentUserSession() to load assets into the ImageAssetGroupManager from either a cache or the sampleLib, depending on the mode.<p>
 * Helps load image collections into the imageSampleGroupManager and works in conjunction with ArtAssetPaths to save labourious path definition. <p>
 * Loading happens either from the sampleLib, which contains "virgin" 100% scaled assets, or from the image cache, which contains
 * scaled-and-treated sets of images. There can be different caches: the "default" cache only saves images that have been rescaled according to the session scale. Other caches will have been created via some post processing
 * and then saved to the currently defined cache (at whatever current session-scale) by using GlobalSettings.getImageAssetGroupManager().cacheAll(); <p>
 */
public class ArtAssetLoaderHelper {


	/**
	 * Used in UserSession.loadContentUserSession() to load assets into the ImageAssetGroupManager from either a cache or the sampleLib, depending on the mode.<p>
	 * Helps load image collections into the imageSampleGroupManager and works in conjunction with ArtAssetPaths to save labourious path definition. <p>
	 * Loading happens either from the sampleLib, which contains "virgin" 100% scaled assets, or from the image cache, which contains
	 * scaled-and-treated sets of images. There can be different caches: the "default" cache only saves images that have been rescaled according to the session scale. Other caches will have been created via some post processing
	 * and then saved to the currently defined cache (at whatever current session-scale) by using GlobalSettings.getImageAssetGroupManager().cacheAll(); <p>
	 * 
	 * If the user wishes to define a new pre-process, and therefore a new cache, as part of the loading, then they have to add a new if-statement to just below the comment "// ADD NEW PRE-PROCESSES HERE", and 
	 * probably add a new method that defines the pre-process operation as a private method here.
	 * The current cache is set by GlobalSettings.setMouseOrganImageCacheName(cacheName), which simply sets a path to the cache location.<p>
	 * 
	 * This is the only public method in this class - ArtAssetLoaderHelper
	 * 
	 * @param cacheName -  the name of the cache to use
	 * @param chacheMode - These are defined in the ScaledImageAssetGroup.  ScaledImageAssetGroup.LOADMODE_FROM_CACHE, where only cached assets are loaded (fast) or ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB, 
	 * where things are loaded processed and cached (slow, as it loads full-scale versions and may include user-defined pre-processing of the assets)
	 * @param includesList - A list of the asset names to be loaded. These are defined by the user in ArtAssetPaths class. If a string begins with an underscore, it is regarded as a command (e.g. "_quit")
	 */
	public static void loadAssets(String cacheName, int chacheMode, String[] includesList) {
		// includes string list is for selecting only parts to be loaded and cached. So you don't have to cache everything again
		GlobalSettings.setMouseOrganImageCacheName(cacheName);

		ArtAssetPaths.buildpaths();

		if(!checkIncludeListAssetsExist(includesList)) {
			System.exit(0);
		}

		// decide here if you are EITHER building a new set of cached images by loading imaged from the asset-lib, post-processing and then saving the cached images (so using ScaledImageAssetGroup.CACHEMODE_NONE)
		// OR, having built the caches successfully, now force the use of the cache only (using ScaledImageAssetGroup.CACHEMODE_FORCE_LOAD_NO_SAVE)
		//
		//
		//
		int currentcachMode = chacheMode;    // CACHEMODE_NONE for first-time caching of post-processed images, after that use CACHEMODE_FORCE_LOAD_NO_SAVE

		if(currentcachMode == ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB) {
			//
			// ADD NEW PRE-PROCESSES HERE
			// Add your own different image pre-processing for different caches here
			// if the cache directory does not yet exist, then this should create it the first time used
			//
			if(cacheName.equals("BWLandscapeNoLevels")) {
				loadAssetAndsCache_BWLandscapeNoLevels( cacheName,   includesList);
				ArtAssetPresetLevels.init();
				return;
			}

			// if you get here then load using the default, i.e. from the sample lib and cache in a folder called "deafultCache"
			loadAssetAndsCache_Default(includesList);
			return;
		}


		if(currentcachMode == ScaledImageAssetGroup.LOADMODE_FROM_CACHE) {
			loadAssetsFromCacheOnly( cacheName, includesList);
		}




	}
	
	/////////////////////////////////////////////////////////////////
	// private methods below here
	//
	//

	
	/**
	 * Called by loadAssets() method above
	 * @param includesList
	 * @return
	 */
	static private boolean checkIncludeListAssetsExist(String[] includesList) {
		boolean ok = true;
		for(String name: includesList) {
			if(beginsWithUnderscore(name)) {
				continue;
			}

			if(!ArtAssetPaths.checkAssetBaseNameExists(name)) {
				ok = false;
				System.out.println("ArtAssetLoad.checkIncludeListAssetsExist: The name " + name + " is not included in the list of ArtAssetPaths");
			}

		}
		return ok;


	}


	
    /**
     * Called by loadAssets() method above
     * @param cacheName
     * @param includesList
     */
    private static void loadAssetsFromCacheOnly(String cacheName,  String[] includesList) {
		GlobalSettings.setMouseOrganImageCacheName(cacheName);
		ScaledImageAssetGroupManager imageSampleGroupManager = GlobalSettings.getImageAssetGroupManager();
		for(String assetname: includesList) {

			String modifiedName = assetname;
			String path = ArtAssetPaths.getAssetSampleLibPath(modifiedName);
			imageSampleGroupManager.loadImageAssetGroup(assetname,  path, ScaledImageAssetGroup.LOADMODE_FROM_CACHE);
		}

	}

    /**
     * Called by loadAssets() method above
     * @param includesList
     */
    private static void loadAssetAndsCache_Default(String[] includesList) {


		GlobalSettings.setMouseOrganImageCacheName("deafultCache");

		for(String assetName: includesList) {
			if( beginsWithUnderscore(assetName)) {
				continue;
			}
			loadAssetDeafult(assetName);
		}

		GlobalSettings.getImageAssetGroupManager().cacheAll();

		if(includesListContains(includesList,"_quit")) {
			System.out.println("quitting");
			System.exit(0);
			}

	}



	/**
	 * Checks to see if the assets thisString is contained in the include list. 
	 * @param includesList
	 * @param thisString
	 * @return
	 */
	private static boolean includesListContains(String[] includesList, String thisString) {
		if(includesList == null ) {
			return true;
		}

		return MOStringUtils.stringListContains(includesList,  thisString);
	}


	private static boolean beginsWithUnderscore(String name) {
		return name.substring(0, 1).equals("_");
	}

	private static void loadAssetDeafult(String assetBaseName) {
		String assetPath = ArtAssetPaths.getAssetSampleLibPath(assetBaseName);
		GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup(assetBaseName, assetPath, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
	}


	
	
	/**
	 * An example of a user-defined loading and caching process that post-processes the assets. If the user wishes to create two versions of an asset to be cached, first duplicate the 
	 * scales image asset group with a new name, then rpcess. It will be included in the cache seperately.
	 * @param cacheName
	 * @param includesList
	 */
	private static void loadAssetAndsCache_BWLandscapeNoLevels(String cacheName,  String[] includesList) {


		GlobalSettings.setMouseOrganImageCacheName(cacheName);

		for(String assetName: includesList) {
			if( beginsWithUnderscore(assetName)) {
				continue;
			}
			loadAssetAndMakeGrayscale(assetName);
		}

		GlobalSettings.getImageAssetGroupManager().cacheAll();

		if(includesListContains(includesList,"_quit")) {
			System.out.println("quitting");
			System.exit(0);
			}

	}


   

	private static void loadAssetAndMakeGrayscale(String assetBaseName) {
		ScaledImageAssetGroup assetGroup = null;
		String assetPath = ArtAssetPaths.getAssetSampleLibPath(assetBaseName);
		assetGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup(assetBaseName, assetPath, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
		MOColorTransform colorTransformGrey = new MOColorTransform();
		colorTransformGrey.addGreyscaleTransform();
		assetGroup.colorTransformAll(colorTransformGrey);
	}


//	private static void loadBWAssetAndApplyLevels(String assetBaseName, float[] levels, boolean overlayEdges) {
//
//		ScaledImageAssetGroup assetGroup = null;
//
//		String assetPathWhole = ArtAssetPaths.getAssetSampleLibPath(assetBaseName);
//
//
//		if(overlayEdges) {
//			String assetPathEdges = ArtAssetPaths.getAssetSampleLibPath(assetBaseName + "Edges");
//			assetGroup = overlayScaledImageAssetGroup(assetBaseName, assetPathWhole, assetPathEdges, 1.0f);
//		} else {
//			assetGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup(assetBaseName, assetPathWhole, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
//		}
//
//		MOColorTransform colorTransformGrey = new MOColorTransform();
//		colorTransformGrey.addGreyscaleTransform();
//		assetGroup.colorTransformAll(colorTransformGrey);
//
//		if(levels!=null) {
//			MOColorTransform levelsTransform = new MOColorTransform();
//			levelsTransform.addLevelsTransform(levels[0], levels[1], levels[2], levels[3], levels[4]);
//			assetGroup.colorTransformAll(levelsTransform);
//		}
//
//
//
//	}
//
//
//	public static ScaledImageAssetGroup overlayScaledImageAssetGroup(String targetGroupName, String tagetAssetPath, String overlayAssetPath, float alpha) {
//		// Loads both the target and overlay group.
//		// Pastes the overlay sprite on-top of the target sprite groups. The composited target group is then kept in the ImageAssetGroupManager, while the overlay group is removed.
//		// Overlay sprites must be sized exactly the same as the target (under-lay) sprite, and have the same number of assets.
//		//
//		ScaledImageAssetGroup targetGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup(targetGroupName,  tagetAssetPath, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
//		ScaledImageAssetGroup overlayGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup("tempOverlayGroupName",  overlayAssetPath, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
//		targetGroup.overlayScaledImageAssetGroup(overlayGroup, alpha);
//		GlobalSettings.getImageAssetGroupManager().removeImageAssetGroup("tempOverlayGroupName");
//		return targetGroup;
//	}
}
