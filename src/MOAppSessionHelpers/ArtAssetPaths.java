package MOAppSessionHelpers;


import java.util.ArrayList;

import MOUtils.GlobalSettings;
import MOUtils.MOStringUtils;

public class ArtAssetPaths {

	/////////////////////////////////////////////////////////////////////////////////////////
	// The class associates a user-give name to a particular path to a directory containing the assets in the sample lib.
	// The user-given name should be unique to that set of assets, as the name (the baseName) is then used to build a path in the current cache directory
	// The structure of the cache is now very flat (i.e. C:currentcachDirectory//baseName//cached_scaled_10)
	// so the cache no longer makes any reference to the sample lib path.
	// The means that the any new groups created at run-time can be cached. It also means that, if new groups are made at run time, the includes list for creating the cache
	// will be shorter (only including assets to load from the sample lib) from the includes list for loading from the cache (same as load list, but with extra created group names).
	// Sample lib paths and cache paths can then be queried by using the base name.

	static ArtAssetPathList pathList = new ArtAssetPathList();
	
	private static String sampleLib = GlobalSettings.getSampleLibPath();
	private static String wildGrassPath = "wild grass\\";
	private static String wildFlowerPath = "wild flowers\\";

	

	//public static String noiseBasedTextures = GlobalSettings.getSampleLibPath() + "textures\\noise based textures";


	public static void buildpaths() {

		// grasses
		pathList.addAssetPath("basicGrass", sampleLib + wildGrassPath + "low level coverage\\basic grass_6000\\whole");
		pathList.addAssetPath("basicSkinnyGrass", sampleLib + wildGrassPath + "low level coverage\\basic_grass_skinny_6000\\whole");
		pathList.addAssetPath("couchGrass", sampleLib + wildGrassPath + "low level coverage\\green couch_03500\\whole");
		pathList.addAssetPath("dryMeadowGrass", sampleLib + wildGrassPath + "mixed meadow grass\\dry meadow grass_10000\\whole");
		pathList.addAssetPath("greenMeadowGrass", sampleLib + wildGrassPath + "mixed meadow grass\\green meadow grass_10000\\whole");
		pathList.addAssetPath("tallFescue", sampleLib + wildGrassPath + "species\\tallfescue_10000\\whole");
		pathList.addAssetPath("yorkshirefog", sampleLib + wildGrassPath + "species\\yorkshirefog_10000\\whole");
		pathList.addAssetPath("barranbrome", sampleLib + wildGrassPath + "species\\barranBrome_10000\\whole");
		pathList.addAssetPath("blackbent", sampleLib + wildGrassPath + "species\\blackBent_10000\\whole");
		pathList.addAssetPath("timothy", sampleLib + wildGrassPath + "species\\timothy_7000\\whole");
		pathList.addAssetPath("cocksfoot", sampleLib + wildGrassPath + "species\\cocksfoot_10000\\whole");
		pathList.addAssetPath("wildBarley", sampleLib + wildGrassPath + "species\\wild barley_06500\\whole");
		
		// wild flowers
		pathList.addAssetPath("oxeyeDaisy", sampleLib + wildFlowerPath + "oxeye daisys_05500\\whole");
		pathList.addAssetPath("oxeyeDaisyFlowers", sampleLib + wildFlowerPath + "oxeye daisys_05500\\whole flowers");
		pathList.addAssetPath("cowparsley", sampleLib + wildFlowerPath + "cowparsley\\whole");
		pathList.addAssetPath("cowparsleyFlowers", sampleLib + wildFlowerPath + "cowparsley\\whole flowers");
		pathList.addAssetPath("dandelion", sampleLib + wildFlowerPath + "dandelion_6000\\whole");
		pathList.addAssetPath("dandelionFlowers", sampleLib + wildFlowerPath + "dandelion_6000\\whole flowers");
		pathList.addAssetPath("cornCockle", sampleLib + wildFlowerPath + "corncockle_7000\\whole");
		pathList.addAssetPath("cornCockleFlowers", sampleLib + wildFlowerPath + "corncockle_7000\\whole flowers");
		pathList.addAssetPath("buttercup", sampleLib + wildFlowerPath + "buttercups_7500\\whole");
		pathList.addAssetPath("buttercupFlowers", sampleLib + wildFlowerPath + "buttercups_7500\\whole flowers");
		
		pathList.addAssetPath("ribwortPlantain", sampleLib + wildFlowerPath + "ribwortPlantain_6000\\whole");
		pathList.addAssetPath("clover", sampleLib + wildFlowerPath + "clover\\whole");
		
		
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Returns the path to the original source asset in the Sample Lib directory
	// For convenience, the user can ask for the simple baseName of the asset, they get the path to the "Whole" directory
	// 
	// Will throw an excepti0n if the asset path does not exist here
	//
	public static String getAssetSampleLibPath(String baseName) {
		return pathList.getPath(baseName);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Returns the path to the cached version of the asset.
	// Assumes the cachePath has already been set
	// In this newer version the cache does not duplicate the structure of the SampleLib but is a flattened directory structure where each base name folder appears at the top level. 
	// So ensure each different asset has a unique base-name. 
	// The enables more than one cached version to be generated from a single Asset at run-time. 
	// 
	// 
	public static String getAssetCachePath(String baseName) {
		
		
		int scalePercentile = (int) (GlobalSettings.getSessionScale() * 100);
		String cachFolderRoot = GlobalSettings.getMouseOrganImageCachePath(); // C:cacheName//
	
		String cachedFolderName = cachFolderRoot + baseName + "\\cached_scaled_" + scalePercentile + "\\";
		
		System.out.println("scaled images cache directory ..." + cachedFolderName);
		return cachedFolderName;
	}
	
	// returns the basename from any varient
	
	
	public static boolean checkAssetBaseNameExists(String baseNameQuery) {
		ArrayList<String> baseNames = getRegisteredBaseNames();
		for(String baseName: baseNames) {
			if(baseName.equals(baseNameQuery)) return true;
			
		}
		System.out.println("checkAssetBaseNameExists has returned false on " + baseNameQuery);
		return false;
	}
	

	public static ArrayList<String> getRegisteredBaseNames() {
		return pathList.getRegisteredBaseNames();
	}
	
	


}


class ArtAssetPathList{
	
	ArrayList<ArtAssetPathListElement> pathList = new ArrayList<ArtAssetPathListElement>();
	
	
	void addAssetPath(String baseName, String path) {
		ArtAssetPathListElement el = new ArtAssetPathListElement(baseName,  path);
		pathList.add(el);
	}
	
	
	
	public String getPath(String baseName) {
		
		ArtAssetPathListElement el = getArtAssetPathListElement(baseName);
		if(el==null) {
			return "";
		}
		

		boolean assetExists = MOStringUtils.checkDirectoryExist(el.fullPath);
		if(assetExists) {
			return el.fullPath;
		} else {
			System.out.println("ArtAssetPathList.getPath - directory does not exist  " + el.fullPath);
			
	    }

		return null;
	}

	private ArtAssetPathListElement getArtAssetPathListElement(String baseNm) {
		for(ArtAssetPathListElement el: pathList) {
			if(el.baseName.equals(baseNm)) return el;
		}
		System.out.println("ArtAssetPathListElement.getArtAssetPathListElement - cannot find element with basename of  " + baseNm);
		return null;
		
	}
	
	ArrayList<String> getRegisteredBaseNames(){
		ArrayList<String> out = new ArrayList<String>();
		for(ArtAssetPathListElement el: pathList) {
			out.add(el.baseName);
		}
		return out;
	}
	
}


class ArtAssetPathListElement{
	String baseName;
	String fullPath;
	
	ArtAssetPathListElement(String name, String path){
		baseName = name;
		fullPath = path;
		
		boolean assetExists = MOStringUtils.checkDirectoryExist(fullPath);
		if(assetExists == false) {
			System.out.println("ArtAssetPathListElement - rootPath->whole directory for does not exist  " + fullPath);
	    }
	}
	
	
	
}

