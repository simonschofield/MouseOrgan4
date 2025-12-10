package MOAppSessionHelpers;
import java.util.ArrayList;

import MOMaths.MOMaths;


/**
 * For every art asset group, defined by their baseName, return the ImageProcessing.level settings that defined the dark, mid and loght versions of that asset group.
 * The process is very user-driven, and is best done using the "paradeContent()" method of the ScaledAssetGroupManager to save out an image of
 * entire group and arrive at the level setting using Photoshop.
 * 
 * It does not do the actual processing of the images, juts returns the level settings.
 * 
 *  This is a "soft coded" helper class. i.e. it is expected to have the pre-set levels added by the user using the baseNames defined in ArtAssetPaths.<p>
 *  
 *  This class helps the user define what is a "dark" version of an art asset, and what is a "light" version of an art asset using levels to adjust between the two.
 *  Once defined, the user can ask for the level using a control variable (0..1), where 0 returns the dark version, 1 returns the bright version,
 *  
 *  
 */
public class ArtAssetPresetLevels {
	static boolean initialised = false;
	static ArrayList<ArtAssetLevelsListElement> assetlevelsList = new ArrayList<>();


	/**
	 * 
	 * 
	 * @param name - the baseName of the image asset group
	 * @param blend - the blend point between dark (0) and light (1)
	 * @return - an array of values defining the 5 level as an array of floats settings listed below <p>
	 * 
	 * input value ranges are all 0..255 except for midtoneGamma, which is 0.01...10 s <p>
	 * shadowVal - output outShadowVal is mapped to this value in the input image <p>
	 * midGamma - in the range 0.001-10, where 1 is in the middle. It is the amount of "bend" between shadowVal and highlightVal - as per Photoshop's dialog <p>
	 * highlightVal - output outHighlightVal is mapped to this value in the input image <p>
	 * outShadowVal -  The darkest colour output in the destination image <p>
	 * outHighlightVal -  The brightest colour output in the destination image <p>
	 * 
	 *
	 */
	 */
	public static float[] getLevels(String name, float blend) {

		if(!initialised) {
			init();
		}



		ArtAssetLevelsListElement el = getArtAssetLevelsListElement(name);
		if(el==null) {
			return new float[] {0,0,0,0,0};
		}
		float[] darkLevels = el.levelsDark;
		float[] lightLevels = el.levelsLight;

		float[] out = new float[5];
		for(int n = 0; n < 5; n++) {
			out[n] = MOMaths.lerp(blend, darkLevels[n], lightLevels[n]);
		}

		return out;
	}

	public static float[] getDefaultLevels(String name) {
		if(!initialised) {
			init();
		}

		ArtAssetLevelsListElement el = getArtAssetLevelsListElement(name);

		return el.levels_defaultBW;
	}



	//////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// private
	//
	static void init() {

		// grasses
		setDarkLevel("basicGrass", 0f, 0.56f, 255f, 0, 255 );     setLightLevel("basicGrass", 38f, 1.46f, 158f, 0, 255 );     setBWLevel("basicGrass", 115, 0.72f, 255, 0, 255 );

		setDarkLevel("basicSkinnyGrass", 0f,0.56f,255f, 0, 255 );     setLightLevel("basicSkinnyGrass", 38f, 1.46f, 158f, 0, 255 );     setBWLevel("basicSkinnyGrass", 115,0.72f,255, 0, 255 );

		setDarkLevel("couchGrass", 0f,0.56f,255f, 0, 255 );     setLightLevel("couchGrass", 38f, 1.46f, 158f, 0, 255 );     setBWLevel("couchGrass", 115,0.72f,255, 0, 255 );

		setDarkLevel("dryMeadowGrass", 0f,0.69f,255f, 0, 255 );     setLightLevel("dryMeadowGrass", 38f,3.36f,170f, 0, 255 );     setBWLevel("dryMeadowGrass", 0,1,255, 0, 255 );

		setDarkLevel("greenMeadowGrass", 0f,0.69f,255f, 0, 255 );     setLightLevel("greenMeadowGrass", 38f,3.36f,170f, 0, 255 );     setBWLevel("greenMeadowGrass", 53,0.8f,255, 0, 255 );

		setDarkLevel("tallFescue", 0f,0.63f,255f, 0, 255 );     setLightLevel("tallFescue", 34,2.58f,159,44,255 );     setBWLevel("tallFescue", 53,0.8f,255, 0, 255 );

		setDarkLevel("yorkshirefog", 48,0.65f,227, 0, 255 );     setLightLevel("yorkshirefog", 7,1.4f,166,37,255 );     setBWLevel("yorkshirefog", 0,1,255, 0, 255 ); // needs BW doing

		setDarkLevel("barranbrome", 92f,0.87f,255f, 0, 255 );     setLightLevel("barranbrome", 0f,1.81f,158f,87,255 );     setBWLevel("barranbrome", 35,1f,169, 0, 255 );

		setDarkLevel("blackbent", 76f, 0.56f, 218, 0, 255 );     setLightLevel("blackbent", 53f,4.34f,141f,79,255 );     setBWLevel("blackbent", 0,1,255, 0, 255 ); // BW still needs doing

		setDarkLevel("timothy", 47f,1.1f,255f, 0, 255 );     setLightLevel("timothy", 6f,1.92f,126f,0,255 );     setBWLevel("timothy", 69,1.2f,179, 0,255 );

		setDarkLevel("cocksfoot", 45f,0.59f,210f, 0, 255 );     setLightLevel("cocksfoot", 58f,2.65f,154f,0,255 );     setBWLevel("cocksfoot", 56,0.78f,183, 0,255 );

		setDarkLevel("wildBarley", 70,0.81f, 170, 0, 255 );     setLightLevel("wildBarley", 36,1.99f,135,38,255 );     setBWLevel("wildBarley", 31,0.36f,143, 0,255 );

		// wild flowers... and their flowers
		setLevels("oxeyeDaisy", 0f,0.63f,255f,   29,1.61f,169f,   47,0.66f,193  );
		setLevels("oxeyeDaisyFlowers", 0f,1f,255f,   0f,1f,255f,   0,1,255  );
		setLevels("cowparsley", 54f,0.8f,255f,   72f,3.13f,170f,   47,0.66f,193  );// still needs doing
		setLevels("cowparsleyFlowers", 0f,1f,255f,   0f,1f,255f,   0,1,255  );// still needs doing
		setLevels("dandelion", 54f,0.8f,255f,   72f,3.13f,170f ,   47,0.66f,193 );// still needs doing
		setLevels("dandelionFlowers",0f,1f,255f,   0f,1f,255f,   0,1,255 );// still needs doing
		setLevels("cornCockle", 54f,0.8f,255f,   72f,3.13f,170f,   0,0.8f,193  );// still needs doing
		setLevels("cornCockleFlowers",0f,1f,255f,   0f,1f,255f,   0,1,255  );// still needs doing
		setLevels("buttercup", 54f,0.8f,255f,   72f,3.13f,170f ,   47,0.66f,193 );// still needs doing
		setLevels("buttercupFlowers",0f,1f,255f,   0f,1f,255f,   0,1,255 );// still needs doing

		// wild flowers that do not have overlay flowers
		setLevels("ribwortPlantain", 54f,0.8f,255f,   72f,3.13f,170f,   47,0.66f,193  );// still needs doing
		setLevels("clover", 54f,0.8f,255f,   72f,3.13f,170f  ,0,0,0);// still needs doing



		initialised = true;
	}

	static void setLevels(String name, float darkLow, float darkMid, float darkHigh, float lightLow, float lightMid, float lightHigh, float bwLow, float bwMid, float bwHigh) {
		setDarkLevel( name, darkLow, darkMid, darkHigh, 0, 255);
		setLightLevel( name, lightLow, lightMid, lightHigh, 0, 255);
		setBWLevel( name, bwLow, bwMid, bwHigh, 0, 255);
	}

	static void setDarkLevel(String name, float low, float mid, float high, float outLow, float outHigh) {

		ArtAssetLevelsListElement levels = getArtAssetLevelsListElement(name);

		if(levels==null) {
			levels = new ArtAssetLevelsListElement(name);
			assetlevelsList.add(levels);
		}

		levels.setDark(low, mid, high, outLow, outHigh);
	}

	static void setLightLevel(String name, float low, float mid, float high, float outLow, float outHigh) {

		ArtAssetLevelsListElement levels = getArtAssetLevelsListElement(name);

		if(levels==null) {
			levels = new ArtAssetLevelsListElement(name);
			assetlevelsList.add(levels);
		}

		levels.setLight(low, mid, high, outLow, outHigh);
	}


	static void setBWLevel(String name, float low, float mid, float high, float outLow, float outHigh) {

		ArtAssetLevelsListElement levels = getArtAssetLevelsListElement(name);

		if(levels==null) {
			levels = new ArtAssetLevelsListElement(name);
			assetlevelsList.add(levels);
		}

		levels.setBW(low, mid, high, outLow, outHigh);

	}

	static private ArtAssetLevelsListElement getArtAssetLevelsListElement(String name) {

		if(assetlevelsList.size()==0) {
			return null;
		}

		for(ArtAssetLevelsListElement a: assetlevelsList) {
			if(a.assetName.equals(name)) {
				return a;
			}
		}
		return null;

	}







}

class ArtAssetLevelsListElement{

	String assetName;
	float levelsDark[];
	float levelsLight[];
	float levels_defaultBW[];


	ArtAssetLevelsListElement(String name){
		assetName = name;
		levelsDark = getDefault();
		levelsLight = getDefault();
		levels_defaultBW = getDefault();
	}

	public void setDark(float low, float mid, float high, float outLow, float outHigh) {
		levelsDark = new float[]{low,mid,high,outLow,outHigh};
	}

	public void setLight(float low, float mid, float high, float outLow, float outHigh) {
		levelsLight = new float[]{low,mid,high,outLow,outHigh};
	}

	public void setBW(float low, float mid, float high, float outLow, float outHigh) {
		levels_defaultBW = new float[]{low,mid,high,outLow,outHigh};
	}


	private float[] getDefault() {

		float[] l = new float[]{0f,1f,255f,0f,255f};
		return l;
	}



}
