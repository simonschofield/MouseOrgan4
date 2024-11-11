package MOAppSessionHelpers;

import MOImageCollections.MOColorTransform;
import MOImageCollections.ScaledImageAssetGroup;
import MOImageCollections.ScaledImageAssetGroupManager;
import MOUtils.GlobalSettings;
import MOUtils.MOStringUtils;

public class ArtAssetLoaderHelper {
	
	
	public static void loadAssets(String cacheName, int chacheMode, String[] includesList) {
		// Big chunk of code that occurs in all user sessions doing BW landscape renders, moved to here
		//
		// Cache mode can be set to  ScaledImageAssetGroup.CACHEMODE_FORCE_LOAD_NO_SAVE, where only cached assets are loaded (fast)
		// or ScaledImageAssetGroup.CACHEMODE_NONE, where things are loaded processed and cached (slow, for new alterations to the processing)
		//
		// includes string list is for selecting only parts to be loaded and cached. So you don't have to cache everything again
		GlobalSettings.setMouseOrganImageCacheName(cacheName);
		
		ArtAssetPaths.buildpaths();
		
		if(checkIncludeListAssetsExist(includesList)==false) {
			System.exit(0);
		}
		
		// decide here if you are EITHER building a new set of cached images by loading imaged from the asset-lib, post-processing and then saving the cached images (so using ScaledImageAssetGroup.CACHEMODE_NONE)
		// OR, having built the caches successfully, now force the use of the cache only (using ScaledImageAssetGroup.CACHEMODE_FORCE_LOAD_NO_SAVE)
		//
		//
		//
		int currentcachMode = chacheMode;    // CACHEMODE_NONE for first-time caching of post-processed images, after that use CACHEMODE_FORCE_LOAD_NO_SAVE
		
		if(currentcachMode == ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB) {
			
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
	
	
	static private boolean checkIncludeListAssetsExist(String[] includesList) {
		boolean ok = true;
		for(String name: includesList) {
			if(beginsWithUnderscore(name)) continue;
			
			if(ArtAssetPaths.checkAssetBaseNameExists(name)==false) {
				ok = false;
				System.out.println("ArtAssetLoad.checkIncludeListAssetsExist: The name " + name + " is not included in the list of ArtAssetPaths");
			}
			
		}
		return ok;
		
		
	}
	

	
    private static void loadAssetsFromCacheOnly(String cacheName,  String[] includesList) {
		GlobalSettings.setMouseOrganImageCacheName(cacheName);
		ScaledImageAssetGroupManager imageSampleGroupManager = GlobalSettings.getImageAssetGroupManager(); 
		for(String assetname: includesList) {
			
			String modifiedName = assetname;
			String path = ArtAssetPaths.getAssetSampleLibPath(modifiedName);
			imageSampleGroupManager.loadImageAssetGroup(assetname,  path, ScaledImageAssetGroup.LOADMODE_FROM_CACHE);
		}

	}
	
	
	
	private static void loadAssetAndsCache_BWLandscapeNoLevels(String cacheName,  String[] includesList) {
		
		
		GlobalSettings.setMouseOrganImageCacheName(cacheName);
		
		for(String assetName: includesList) {
			if( beginsWithUnderscore(assetName)) continue;
			loadAssetAndMakeGrayscale(assetName);
		}

		GlobalSettings.getImageAssetGroupManager().cacheAll();

		if(includesListContains(includesList,"_quit")) {
			System.out.println("quitting");
			System.exit(0);
			}
		
	}
	
	
   private static void loadAssetAndsCache_Default(String[] includesList) {
		
		
		GlobalSettings.setMouseOrganImageCacheName("deafultCache");
		
		for(String assetName: includesList) {
			if( beginsWithUnderscore(assetName)) continue;
			loadAssetDeafult(assetName);
		}

		GlobalSettings.getImageAssetGroupManager().cacheAll();

		if(includesListContains(includesList,"_quit")) {
			System.out.println("quitting");
			System.exit(0);
			}
		
	}
	
	
	
	private static boolean includesListContains(String[] includesList, String thisString) {
		if(includesList == null ) return true;
		
		return MOStringUtils.stringListContains(includesList,  thisString);
	}
	
	
	private static boolean beginsWithUnderscore(String name) {
		return name.substring(0, 1).equals("_");
	}
	
	private static void loadAssetDeafult(String assetBaseName) {
		ScaledImageAssetGroup assetGroup = null;
		String assetPath = ArtAssetPaths.getAssetSampleLibPath(assetBaseName); 
		assetGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup(assetBaseName, assetPath, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
	}
	
	
	private static void loadAssetAndMakeGrayscale(String assetBaseName) {
		ScaledImageAssetGroup assetGroup = null;
		String assetPath = ArtAssetPaths.getAssetSampleLibPath(assetBaseName); 
		assetGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup(assetBaseName, assetPath, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
		MOColorTransform colorTransformGrey = new MOColorTransform();
		colorTransformGrey.addGreyscaleTransform();
		assetGroup.colorTransformAll(colorTransformGrey);
	}
	
	
	private static void loadBWAssetAndApplyLevels(String assetBaseName, float[] levels, boolean overlayEdges) {
		
		ScaledImageAssetGroup assetGroup = null;
		
		String assetPathWhole = ArtAssetPaths.getAssetSampleLibPath(assetBaseName);
		
		
		if(overlayEdges) {
			String assetPathEdges = ArtAssetPaths.getAssetSampleLibPath(assetBaseName + "Edges");
			assetGroup = overlayScaledImageAssetGroup(assetBaseName, assetPathWhole, assetPathEdges, 1.0f);
		} else {
			assetGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup(assetBaseName, assetPathWhole, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
		}
		
		MOColorTransform colorTransformGrey = new MOColorTransform();
		colorTransformGrey.addGreyscaleTransform();
		assetGroup.colorTransformAll(colorTransformGrey);
		
		if(levels!=null) {
			MOColorTransform levelsTransform = new MOColorTransform();
			levelsTransform.addLevelsTransform(levels[0], levels[1], levels[2], levels[3], levels[4]);
			assetGroup.colorTransformAll(levelsTransform);
		}
		
		
		
	}
	

	public static ScaledImageAssetGroup overlayScaledImageAssetGroup(String targetGroupName, String tagetAssetPath, String overlayAssetPath, float alpha) {
		// Loads both the target and overlay group.
		// Pastes the overlay sprite on-top of the target sprite groups. The composited target group is then kept in the ImageAssetGroupManager, while the overlay group is removed.
		// Overlay sprites must be sized exactly the same as the target (under-lay) sprite, and have the same number of assets.
		//
		ScaledImageAssetGroup targetGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup(targetGroupName,  tagetAssetPath, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
		ScaledImageAssetGroup overlayGroup = GlobalSettings.getImageAssetGroupManager().loadImageAssetGroup("tempOverlayGroupName",  overlayAssetPath, ScaledImageAssetGroup.LOADMODE_FROM_ASSETLIB);
		targetGroup.overlayScaledImageAssetGroup(overlayGroup, alpha);
		GlobalSettings.getImageAssetGroupManager().removeImageAssetGroup("tempOverlayGroupName");
		return targetGroup;
	}
}
