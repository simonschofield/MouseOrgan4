package MOAppSessionHelpers;

import MOUtils.GlobalSettings;

public class ArtAssetPaths {
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// bank of paths to the assets used
	// Any path from this class should NOT end with a trailing slash "\\" as they are given to a DirectoryScanner
	// and so identify the directory, not the files within
	//
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// Notes on structure of the SampleLib directory
	// If a type of collection (e.g. Oxeyedaisy) has extra variants within its structure (e.g. flowers, or edge rendering)
	// there is a top level directory called "Oxeyedaisy", and the "normal" image files are in a sub-directory called "whole". All
	// the other variants are contained in separate sub-directories at the same level
	// Overlay Images - For files that are overlays of the "whole" file, they should be the same dimensions, and have the same file name.
	// Overlay image directories begin with the name "whole" and then specifics about the overlay content
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Pre-prepared black and white "drawing" style images
	// This is being depricated
	//
	static String wildGrassBWPath = "wild grass\\bw\\";

	public static String couchGrassBW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "basic grass_6000_improved";
	public static String dryMeadowGrassBW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "dry meadow grass_10000";
	public static String greenMeadowGrass1BW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "green meadow grass_1_10000";
	public static String greenMeadowGrass2BW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "green meadow grass_2_10000";
	public static String tallFescueBW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "tall_fescue_10000";
	public static String brightGrassBW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "bright grass_10000";
	public static String yorkshirefogBW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "yorkshirefog_10000";
	public static String barranbromeBW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "barran_brome_10000_fineline";
	public static String blackbentBW = GlobalSettings.getSampleLibPath() + wildGrassBWPath + "black_bent_10000_fineline";
	
	static String wildFlowerBWPath = "wild flowers\\bw\\";
	
	
	
	public static String oxeyeDaisyBW = GlobalSettings.getSampleLibPath() + wildFlowerBWPath + "oxeye daisys_natural_05500";
	public static String cowparsleyBW = GlobalSettings.getSampleLibPath() + wildFlowerBWPath + "Cow_parsley_whole_10000";
	public static String dandelionBW = GlobalSettings.getSampleLibPath() + wildFlowerBWPath + "Dandelion_6000";
	public static String cornCockleBW = GlobalSettings.getSampleLibPath() + wildFlowerBWPath + "corncockle_7000";
	public static String buttercupBW = GlobalSettings.getSampleLibPath() + wildFlowerBWPath + "Buttercups_7500";
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Colour images - wild grass and associated assets
	//
	//
	static String whole = "\\whole";
	static String wholeEdges7 = "\\whole edges 7 pixels";
	static String wholeFlowers = "\\whole flowers";
	static String wholeFlowerEdges7 = "\\whole flower edges 7 pixels";
	
	static String wildGrassPath = "wild grass\\";
   
    
	static String couchGrassRootPath = GlobalSettings.getSampleLibPath() + wildGrassPath + "low level coverage\\basic grass_6000";
	static String dryMeadowGrassRootPath = GlobalSettings.getSampleLibPath() + wildGrassPath + "mixed meadow grass\\dry meadow grass_10000";
	static String greenMeadowGrassRootPath = GlobalSettings.getSampleLibPath() + wildGrassPath + "mixed meadow grass\\green meadow grass_10000";
	static String tallFescueRootPath = GlobalSettings.getSampleLibPath() + wildGrassPath + "species\\tallfescue_10000";
	static String yorkshirefogRootPath = GlobalSettings.getSampleLibPath() + wildGrassPath + "species\\yorkshirefog_10000";
	static String barranbromeRootPath = GlobalSettings.getSampleLibPath() + wildGrassPath + "species\\barran_brome_10000";
	static String blackbentRootPath = GlobalSettings.getSampleLibPath() + wildGrassPath + "species\\black_bent_hardened_10000";
	
	public static String couchGrassWhole = couchGrassRootPath + whole;
	public static String dryMeadowGrassWhole = dryMeadowGrassRootPath + whole;
	public static String greenMeadowGrassWhole = greenMeadowGrassRootPath + whole;
	public static String tallFescueWhole = tallFescueRootPath + whole;
	public static String yorkshirefogWhole = yorkshirefogRootPath + whole;
	public static String barranbromeWhole = barranbromeRootPath + whole;
	public static String blackbentWhole = blackbentRootPath + whole;
	
	
	public static String couchGrassEdges = couchGrassRootPath + wholeEdges7;
	public static String dryMeadowGrassEdges = dryMeadowGrassRootPath + wholeEdges7;
	public static String greenMeadowGrassEdges = greenMeadowGrassRootPath + wholeEdges7;
	public static String tallFescueEdges = tallFescueRootPath + wholeEdges7;
	public static String yorkshirefogEdges = yorkshirefogRootPath + wholeEdges7;
	public static String barranbromeEdges = barranbromeRootPath + wholeEdges7;
	public static String blackbentEdges = blackbentRootPath + wholeEdges7;
	
	// a folder containing just 3 image for testing the process
	static String grassTestRootPath = GlobalSettings.getSampleLibPath() + wildGrassPath + "mixed meadow grass\\test_10000";
	public static String grassTestWhole = grassTestRootPath + whole;
	public static String grassTestEdges = grassTestRootPath + wholeEdges7;
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	//
	// Colour images - wild flowers and associated assets
	//
	//
	static String wildFlowerPath = "wild flowers\\";
	
	
	
	static String oxeyeDaisyRootPath = GlobalSettings.getSampleLibPath() + wildFlowerPath + "oxeye daisys_brightFlowers_05500";
	public static String oxeyeDaisyWhole = oxeyeDaisyRootPath + whole;
	public static String oxeyeDaisyWholeEdges = oxeyeDaisyRootPath + wholeEdges7;
	public static String oxeyeDaisyFlowers = oxeyeDaisyRootPath + wholeFlowers;
	public static String oxeyeDaisyFlowerEdges = oxeyeDaisyRootPath + wholeFlowerEdges7;
	
	static String cowparsleyRootPath = GlobalSettings.getSampleLibPath() + wildFlowerPath + "cowparsley";
	public static String cowparsleyWhole = cowparsleyRootPath + whole;
	public static String cowparsleyWholeEdges = cowparsleyRootPath + wholeEdges7;
	public static String cowparsleyFlowers = cowparsleyRootPath + wholeFlowers;
	public static String cowparsleyFlowerEdges = cowparsleyRootPath + wholeFlowerEdges7;
	public static String cowparsleyFlowersFantasia = cowparsleyRootPath + "whole flower fantasia";
	public static String cowparsleyFlowersFantasiaEdges = cowparsleyRootPath + "whole flower fantasia edges 7 pixels";
	
	static String dandelionRootPath = GlobalSettings.getSampleLibPath() + wildFlowerPath + "dandelion_6000";
	public static String dandelionWhole = dandelionRootPath + whole;
	public static String dandelionWholeEdges = dandelionRootPath + wholeEdges7;
	public static String dandelionFlowers  = dandelionRootPath + wholeFlowers;
	public static String dandelionFlowerEdges  = dandelionRootPath + wholeFlowerEdges7;
	
	static String cornCockleRootPath = GlobalSettings.getSampleLibPath() + wildFlowerPath + "corncockle_7000";
	public static String cornCockleWhole = cornCockleRootPath + whole;
	public static String cornCockleWholeEdges = cornCockleRootPath + wholeEdges7;
	public static String cornCockleFlowers = cornCockleRootPath + wholeFlowers;
	public static String cornCockleFlowerEdges = cornCockleRootPath + wholeFlowerEdges7;
	
	static String buttercupRootPath = GlobalSettings.getSampleLibPath() + wildFlowerPath + "buttercups_7500";
	public static String buttercupWhole = buttercupRootPath + whole;
	public static String buttercupWholeEdges = buttercupRootPath + wholeEdges7;
	public static String buttercupFlowers = buttercupRootPath + wholeFlowers;
	public static String buttercupFlowerEdges = buttercupRootPath + wholeFlowerEdges7;
	public static String buttercupTestFlowers = buttercupRootPath + "\\testFlowers";
	
	
	public static String noiseBasedTextures = GlobalSettings.getSampleLibPath() + "textures\\noise based textures";
	
	
	
	
	
	
	
	
	
	
}
