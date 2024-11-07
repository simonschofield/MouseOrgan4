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
			if(cacheName.equals("mouseOrganBWLandscapeCachePreLevels")) {
				loadAssetAndsCache_BWLandscapePreLevels( cacheName,   includesList);
				return;
			}
			if(cacheName.equals("mouseOrganBWLandscapeCacheNoLevels")) {
				loadAssetAndsCache_BWLandscapeNoLevels( cacheName,   includesList);
				ArtAssetBWLevels.init();
				return;
			}
			
			
			loadAssetAndsCache_Default( cacheName,  includesList);
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
	
	
	
    private static void loadAssetAndsCache_Default(String cacheName,  String[] includesList) {
    	
    	
    }
	
	
	
	private static void loadAssetAndsCache_BWLandscapePreLevels(String cacheName,  String[] includesList) {
		// This method sets a default level for the asset, and was used in the first 2023 renders
		GlobalSettings.setMouseOrganImageCacheName(cacheName);
		

		
		for(String assetName: includesList) {
			if( beginsWithUnderscore(assetName)) continue;
			if(assetName.contains("Edges")) continue; // these get loaded when the whole asset is loaded
			float[] levels = ArtAssetBWLevels.getDefaultLevels(assetName);
			loadBWAssetAndApplyLevels(assetName, levels, includesListContains(includesList, assetName + "Edges"));
		}
		
//		MOColorTransform wildflowerStemBrighten = new MOColorTransform();
//		wildflowerStemBrighten.addLevelsTransform(47,0.66f,193, 0, 255);
//
//		float[] genericStemsLevels = {47,0.66f,193, 0, 255};
//		
//		if( includesListContains(includesList, "cornCockle")) {
//			
//			float[] stemsLevels = {0,0.8f,193, 0, 255};
//			loadBWAssetAndApplyLevels("cornCockle", stemsLevels, includesListContains(includesList, "cornCockleEdges"));
//			float[] flowersLevels = {47,0.66f,193, 0, 255};
//			loadBWAssetAndApplyLevels("cornCockleFlowers", flowersLevels, includesListContains(includesList, "cornCockleFlowersEdges"));
//
//		}
//		
//		if( includesListContains(includesList, "dandelion")) {
//			
//			loadBWAssetAndApplyLevels("dandelion", genericStemsLevels, includesListContains(includesList, "dandelionEdges"));
//			float[] flowersLevels = {50, 0.8f,203, 0, 255};
//			loadBWAssetAndApplyLevels("dandelionFlowers", flowersLevels, includesListContains(includesList, "dandelionFlowersEdges"));
//			
//		}
//		
//		if( includesListContains(includesList, "buttercup")) {
//			
//			loadBWAssetAndApplyLevels("buttercup", genericStemsLevels, includesListContains(includesList, "buttercupEdges"));
//			float[] flowersLevels = {47,0.66f,193, 0, 255};
//			loadBWAssetAndApplyLevels("buttercupFlowers", flowersLevels, includesListContains(includesList, "buttercupFlowersEdges"));
//		
//		}
//
//		if( includesListContains(includesList, "daisy")) {
//			
//			loadBWAssetAndApplyLevels("daisy", genericStemsLevels, includesListContains(includesList, "daisyEdges"));
//			float[] flowersLevels = null;
//			loadBWAssetAndApplyLevels("daisyFlowers", flowersLevels, includesListContains(includesList, "daisyFlowersEdges"));
//			
//		}
//		
//
//		
//		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		//
//		// wild grass load, colour-transforms and cache
//		//
//		
//		/// using local method String resultantImageAssetGroupName, String pathToImageAssetTargetGroup, String pathToImageAssetOverlayGroup, float alpha
//		if( includesListContains(includesList, "basicSkinnyGrass")) {
//			float[] levels = {115,0.72f,255, 0, 255};
//			loadBWAssetAndApplyLevels("basicSkinnyGrass", levels, includesListContains(includesList, "basicSkinnyGrassEdges"));
//		}
//		
//		if( includesListContains(includesList, "greenMeadowGrass")) {
//			float[] levels = {53,0.8f,255, 0, 255};
//			loadBWAssetAndApplyLevels("greenMeadowGrass", levels, includesListContains(includesList, "greenMeadowGrassEdges"));
//		}
//		
//		if( includesListContains(includesList, "dryMeadowGrass")) {
//			float[] levels = null;
//			loadBWAssetAndApplyLevels("dryMeadowGrass", levels, includesListContains(includesList, "dryMeadowGrassEdges"));
//		}
//		
//		if( includesListContains(includesList, "tallFescue")) {
//			float[] levels = {0,1.0f,212, 0, 255};
//			loadBWAssetAndApplyLevels("tallFescue", levels, includesListContains(includesList, "tallFescueEdges"));
//		}
//		
//		if( includesListContains(includesList, "barranbrome")) {
//			float[] levels = {35,1f,169, 0, 255};
//			loadBWAssetAndApplyLevels("barranbrome", levels, includesListContains(includesList, "barranbromeEdges"));
//		}
//		
//		
//		if( includesListContains(includesList, "blackbent")) {
//			float[] levels = null;
//			loadBWAssetAndApplyLevels("blackbent", levels, includesListContains(includesList, "blackbentEdges"));
//		}
//		
//		if( includesListContains(includesList, "timothy")) {
//			float[] levels = {69,1.2f,179, 0, 255};
//			loadBWAssetAndApplyLevels("timothy", levels, includesListContains(includesList, "timothyEdges"));
//		}
//		
//		
//		if( includesListContains(includesList, "cocksfoot")) {
//			float[] levels = {56,0.78f,183, 0, 255};
//			loadBWAssetAndApplyLevels("cocksfoot", levels, includesListContains(includesList, "cocksfootEdges"));
//		}
//		
//		if( includesListContains(includesList, "ribwortPlantain")) {
//			float[] levels = {52,1.1f,167, 0, 255};
//			loadBWAssetAndApplyLevels("ribwortPlantain", levels, includesListContains(includesList, "ribwortPlantainEdges"));
//		}
//		
//		if( includesListContains(includesList, "wildBarley")) {
//			float[] levels = {31,0.64f,143, 0, 255};
//			loadBWAssetAndApplyLevels("wildBarley", levels, includesListContains(includesList, "wildBarleyEdges"));
//		}
//		

		GlobalSettings.getImageAssetGroupManager().cacheAll();
		
		
		
		System.out.println("quitting");
		System.exit(0);
		
		
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
	
	
	
	private static boolean includesListContains(String[] includesList, String thisString) {
		if(includesList == null ) return true;
		
		return MOStringUtils.stringListContains(includesList,  thisString);
	}
	
	
	private static boolean beginsWithUnderscore(String name) {
		return name.substring(0, 1).equals("_");
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
