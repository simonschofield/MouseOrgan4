package MOAppSessionHelpers;


import java.util.ArrayList;

import MOUtils.GlobalSettings;
import MOUtils.MOStringUtils;
/**
 * This is a "soft coded" static helper class. i.e. it is expected to have new asset paths added by the user in the buildPaths() method. Each new path must be given a unique short-name (the baseName).<p>
 * 
 * 
 * The class associates a user-give "baseName" to a particular path to a directory containing the assets in the sample lib so saves on laborious path definition in the UserSession.
 * Instead, assets are loaded using the short baseName, which is placed into a list to be loaded.<p>
 * The baseName should be unique to that set of assets, as the baseName is then used to build a path in the current cache directory<p>
 * The baseName is then also used as the group-name of the ScaledImageAssetGroup within the ScaledImageAssetGroupManager.<p>
 * 
 * 
 * NOTE: Caches no longer makes any duplication of the sample lib directory structure. Caches now has a very "flat" structure (e.g. C:currentcachDirectory//baseName//cached_scaled_10). 
 * Each baseName folder contains every scaled version of that asset. Hence the requirement for unique baseNames in each image cache.
 * The means that any new groups created at run-time, by group duplication, can now be cached under a new baseName. <p>
 * NOTE: If new groups are made at run time, the includes list for loading original assets from the SampleLib will be shorter than the includes list 
 * for loading from the cache (same as load list, but with extra created group names). <p>
 * 
 * Sample lib paths and cache paths can then be queried by using the base name.
 */
public class ArtAssetPaths {


	static ArtAssetPathList pathList = new ArtAssetPathList();


	/**
	 * Add your new paths here.
	 * Called by the ArtAssetLoaderHelper.loadAssets() to build the paths
	 */
	public static void buildpaths() {
		//
		//
		// ADD NEW PATHS HERE
		//
		//
		//
		
		

		String sampleLib = GlobalSettings.getSampleLibPath();
		String wildGrassPath = "wild grass\\";
		String wildFlowerPath = "wild flowers\\";
		
		// NOTE: this could be defined in a text file 
		//
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

		// "basicGrass",  "couchGrass", "greenMeadowGrass", "tallFescue", "barranbrome", "ribwortPlantain", "blackbent", "timothy", "cocksfoot", "wildBarley"

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

		// "oxeyeDaisy", "oxeyeDaisyFlowers", "dandelion", "dandelionFlowers", "cornCockle", "cornCockleFlowers", "buttercup", "buttercupFlowers"
	}

	/**
	*
	* Returns the path to the original source asset in the Sample Lib directory
	* For convenience, the user can ask for the simple baseName of the asset, they get the path to the "Whole" directory
	*
	* Will throw an exception if the asset path does not exist here<p>
	* Used at various points in ArtAssetLoaderHelper<p>
	* 
	* @param baseName
	* @return
	*/
	public static String getAssetSampleLibPath(String baseName) {
		return pathList.getPath(baseName);
	}

	
	
	/**
	*
	* Returns the path to the cached version of the asset based on the currently used cache defined by GlobalSettings.getMouseOrganImageCachePath()/GlobalSettings.setMouseOrganImageCachePath()
	* For convenience, the user can ask for the simple baseName of the asset, they get the path to the "Whole" directory
	* In this newer version the cache does not duplicate the structure of the SampleLib but is a flattened directory structure where each base name folder appears at the top level.
	* Will throw an excepti0n if the asset path does not exist here <p>
	*
	* Used by ScaledImageAssetGroup.getCachedScaledImagesFolderName(). The baseName is set as the group name in the ScaledImageAssetGroup, this is then used to load the correct cache images.<p>
	* 
	* @param baseName
	* @return
	*/
	public static String getAssetCachePath(String baseName) {


		int scalePercentile = (int) (GlobalSettings.getSessionScale() * 100);
		String cachFolderRoot = GlobalSettings.getMouseOrganImageCachePath(); // C:cacheName//

		String cachedFolderName = cachFolderRoot + baseName + "\\cached_scaled_" + scalePercentile + "\\";

		//System.out.println("scaled images cache directory ..." + cachedFolderName);
		return cachedFolderName;
	}

	// returns the basename from any varient


	/**
	 * Used by the ArtAssetLoaderHelper.checkIncludelistAssetExists() method. Not sure the user needs this
	 * @param baseNameQuery
	 * @return
	 */
	public static boolean checkAssetBaseNameExists(String baseNameQuery) {
		ArrayList<String> baseNames = getRegisteredBaseNames();
		for(String baseName: baseNames) {
			if(baseName.equals(baseNameQuery)) {
				return true;
			}

		}
		System.out.println("checkAssetBaseNameExists has returned false on " + baseNameQuery);
		return false;
	}


	/**
	 * Used by this.checkAssetBaseNameExists(String)
	 * @return - an array-list of strings of all the asset baseNames
	 */
	public static ArrayList<String> getRegisteredBaseNames() {
		return pathList.getRegisteredBaseNames();
	}




}


/**
 * Used privately by ArtAssetPaths
 */
class ArtAssetPathList{

	ArrayList<ArtAssetPathListElement> pathList = new ArrayList<>();


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
			if(el.baseName.equals(baseNm)) {
				return el;
			}
		}
		System.out.println("ArtAssetPathListElement.getArtAssetPathListElement - cannot find element with basename of  " + baseNm);
		return null;

	}

	ArrayList<String> getRegisteredBaseNames(){
		ArrayList<String> out = new ArrayList<>();
		for(ArtAssetPathListElement el: pathList) {
			out.add(el.baseName);
		}
		return out;
	}

}


/**
 * Used privately by ArtAssetPathList
 */
class ArtAssetPathListElement{
	String baseName;
	String fullPath;

	ArtAssetPathListElement(String name, String path){
		baseName = name;
		fullPath = path;

		boolean assetExists = MOStringUtils.checkDirectoryExist(fullPath);
		if(!assetExists) {
			System.out.println("ArtAssetPathListElement - rootPath->whole directory for does not exist  " + fullPath);
	    }
	}



}

