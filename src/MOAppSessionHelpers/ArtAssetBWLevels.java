package MOAppSessionHelpers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImageCollections.MOColorTransform;
import MOMaths.MOMaths;
import MOUtils.GlobalSettings;
///////////////////////////////////////////////////////////////////////////////////////////////////////////
// This class helps the user define what is a "dark" version of an art asset, and what is a "light" version of an art asset using 
// levels to adjust between the two. 
// Once defines, the user can ask for the level using 
//
//
//
//
public class ArtAssetBWLevels {
	static boolean initialised = false;
	static ArrayList<ArtAssetBWLevelsElement> assetlevelsList = new ArrayList<ArtAssetBWLevelsElement>();
	 
	
	public static float[] getLevels(String name, float blend) {
		
		if(initialised == false) {
			init();
		}
		
		
		
		ArtAssetBWLevelsElement el = getArtAssetBWLevelsElement(name);
		if(el==null) return new float[] {0,0,0,0,0};
		float[] darkLevels = el.levelsDark;
		float[] lightLevels = el.levelsLight;
		
		float[] out = new float[5];
		for(int n = 0; n < 5; n++) {
			out[n] = MOMaths.lerp(blend, darkLevels[n], lightLevels[n]);
		}
		
		return out;
	}
	
	public static float[] getDefaultLevels(String name) {
		if(initialised == false) {
			init();
		}
		
		ArtAssetBWLevelsElement el = getArtAssetBWLevelsElement(name);
		
		return el.levels_defaultBW;
	}

	

	//////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// private
	//
	static void init() {
		
		// grasses
		addlevels("basicGrass", 0f,0.56f,255f,   38f, 1.46f, 158f,   115,0.72f,255  );
		addlevels("basicSkinnyGrass", 0f,0.56f,255f,   38f, 1.46f, 158f,   115,0.72f,255  );
		addlevels("couchGrass", 0f,0.56f,255f,   38f, 1.46f, 158f,   115,0.72f,255  );
		addlevels("dryMeadowGrass", 0f,0.69f,255f,   38f,3.36f,170f,   0,1,255  ); // still needs doing, deafult is OK
		addlevels("greenMeadowGrass", 0f,0.69f,255f,   38f,3.36f,170f,   53,0.8f,255  );
		addlevels("tallFescue", 46f,0.67f,255f,   34f,0.76f,143f,   0,1.0f,212  );
		addlevels("yorkshirefog", 0f,0.69f,255f,   38f,3.36f,170f, 0,1,255  ); // still needs doing
		addlevels("barranbrome", 92f,0.87f,255f,   68f,1.91f,190f, 35,1f,169  );
		addlevels("blackbent", 0f,0.93f,255f,   53f,3.34f,174f, 0,1,255  );
		addlevels("timothy", 47f,1.1f,255f,   6f,1.92f,126f, 69,1.2f,179  );
		addlevels("cocksfoot", 45f,0.59f,210f,   58f,2.65f,154f, 56,0.78f,183  );
		addlevels("wildBarley", 0f,0.57f,214f,   26f,1.7f,135f, 31,0.64f,143  );
		
		// wild flowers... and their flowers
		addlevels("oxeyeDaisy", 54f,0.8f,255f,   72f,3.13f,170f,   47,0.66f,193  );// still needs doing
		addlevels("oxeyeDaisyFlowers", 54f,0.8f,255f,   72f,3.13f,170f,   0,1,255  );// still needs doing
		addlevels("cowparsley", 54f,0.8f,255f,   72f,3.13f,170f,   47,0.66f,193  );// still needs doing
		addlevels("cowparsleyFlowers", 54f,0.8f,255f,   72f,3.13f,170f,   0,1,255  );// still needs doing
		addlevels("dandelion", 54f,0.8f,255f,   72f,3.13f,170f ,   47,0.66f,193 );// still needs doing
		addlevels("dandelionFlowers",54f,0.8f,255f,   72f,3.13f,170f ,   50, 0.8f,203 );// still needs doing
		addlevels("cornCockle", 54f,0.8f,255f,   72f,3.13f,170f,   0,0.8f,193  );// still needs doing
		addlevels("cornCockleFlowers",54f,0.8f,255f,   72f,3.13f,170f,   0,1,255  );// still needs doing
		addlevels("buttercup", 54f,0.8f,255f,   72f,3.13f,170f ,   47,0.66f,193 );// still needs doing
		addlevels("buttercupFlowers",54f,0.8f,255f,   72f,3.13f,170f ,   47,0.66f,193 );// still needs doing
		addlevels("ribwortPlantain", 54f,0.8f,255f,   72f,3.13f,170f,   47,0.66f,193  );// still needs doing
		
		addlevels("clover", 54f,0.8f,255f,   72f,3.13f,170f  ,0,0,0);// still needs doing
		
		
		
		initialised = true;
	}
	
	
	static void addlevels(String name, float darkLow, float darkMid, float darkHigh, float lightLow, float lightMid, float lightHigh, float defaultLow, float defaultMid, float defaultHigh) {
		// this method assumes outlow and outhigh are always 0 and 255
		
		float[] dark = {darkLow, darkMid, darkHigh, 0, 255};
		float[] light = {lightLow, lightMid, lightHigh, 0, 255};
		float[] defaultbw = {defaultLow, defaultMid, defaultHigh, 0, 255};
		
		
		ArtAssetBWLevelsElement levels = new ArtAssetBWLevelsElement(name, dark, light, defaultbw);
		assetlevelsList.add(levels);
	}
	
	
	
	
	static ArtAssetBWLevelsElement getArtAssetBWLevelsElement(String name) {
		
		for(ArtAssetBWLevelsElement a: assetlevelsList) {
			if(a.assetName.equals(name)) return a;
		}
		System.out.println("ArtAssetBWLevels.getArtAssetBWLevelsElement cannot find levels called " + name);
		return null;
		
	}
	
	
	
	
	
	
	
}

class ArtAssetBWLevelsElement{
	
	String assetName;
	float levelsDark[];
	float levelsLight[];
	float levels_defaultBW[];
	
	ArtAssetBWLevelsElement(String name, float[] dark, float[] light, float[] defaultBW) {
		assetName = name;
		levelsDark = dark.clone();
		levelsLight = light.clone();
		levels_defaultBW = defaultBW.clone();
	}

	
}
