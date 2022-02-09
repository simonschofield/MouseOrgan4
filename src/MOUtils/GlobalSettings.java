package MOUtils;

import java.io.File;

import MOApplication.MainDocument;
import MOImage.ImageProcessing;
///////////////////////////////////////////////////////////////////////////////////////////////////////
// This is a repository for all common data that needs to accessed across the application, partially in order to avoid complex passing of objects around but
// also to gather important settings into one place.
// The data is quite specific to the workings of the mouse organ application, but have been placed in a low-level Package to optimise accessibility and
// minimise circular dependencies
//
// Apart from the sessionName and schemaName, all the data is set once at the start of the session and remains unchanging throughout the session.
//
//
// typical order of initialisation
// ROI helper (Optional) - this generates the full size render dims
// in UserSession initialiseUserSession you MUST call initialsieSystem(....)
// RenderSaver (Optional)

public class GlobalSettings {
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// critical info used all over the system, once set can only be got
	// 
	// The sessionScale is set in init()
	// The theDocumentCoordSystem is set as soon as the MainDocument is instantiated
	private static float sessionScale = 0;
	private static ImageCoordinateSystem theDocumentCoordSystem;
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// paths used by the system
	// 
	// The userSessionPath is set in init()
	private static String sampleLibPath = "C:\\simon\\sample lib\\";
	private static String mouseOrganImageCachePath = "C:\\mouseOrganImageCache2\\";
	private static String userSessionPath = "";
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// A session has a name, the default is the directory in which the session takes place (from userSessionPath)
	// This is used to give the renders their names e.g. LondonFlowers
	// The schema is used for variants within this session name e.g. LondonFlowers_Tulips,  LondonFlowers_Poppys
	private static int fullScaleRenderWidth;
	private static int fullScaleRenderHeight;
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// A session has a name, the default is the directory in which the session takes place (derived from userSessionPath)
	// This is used to give the renders their names e.g. LondonFlowers
	// The schema is used for variants within this session name e.g. LondonFlowers_Tulips,  LondonFlowers_Poppys
	public static String mainSessionName = "";
	public static String currentSchemea = "";
	
	
	public static boolean printOn = false;
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Initialisation	
	// Called from UserSession->initialiseUserSession() which must contain a call to Surface.initialiseSystem(....)
	// Surface.initialiseSystem(....) calls init(...), instantiates the MainDocument then calls setTheDocumentCoordSystem(...)
	public static void init(String userSessionPth, int fullScaleRenderW, int fullScaleRenderH, float sessionScl){
		// only called in Surface.initialiseSystem()
		GlobalSettings.userSessionPath = userSessionPth;
		sessionScale = sessionScl; 
		fullScaleRenderWidth = fullScaleRenderW;
		fullScaleRenderHeight = fullScaleRenderH;
		
		ImageProcessing.setInterpolationQuality(getRenderQuality());
		setDefaultSessionName();
		
		}
	
	public static void setTheDocumentCoordSystem(MainDocument md) {
		// only set by the MainDocument in Surface.initialiseSystem() after init()
		GlobalSettings.theDocumentCoordSystem = md.getCoordinateSystem();
	}
	
	private static void setDefaultSessionName() {
		// the default document name is the directory containing the session 
		if(mainSessionName.equals("")){
			File f  = new File(GlobalSettings.getUserSessionPath());
			mainSessionName = f.getName();
			System.out.println("default doc name is " + mainSessionName);
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// public data access methods
	//
	//
	public static float getSessionScale() {
		if(sessionScale == 0) {
			System.out.println("GlobalSettings session scale has not been set! EXITING");
			System.exit(0);
		}
		return sessionScale;
	}

	public static ImageCoordinateSystem getTheDocumentCoordSystem() {
		if(theDocumentCoordSystem == null) {
			System.out.println("GlobalSettings theDocumentCoordSystem has not been set, please create MainDocument! EXITING");
			//System.exit(0);
		}
		return theDocumentCoordSystem;
	}

	
	public static boolean isDraftRender() {
		if(GlobalSettings.getSessionScale() < 1.0f) return true;
		return false;
	}
	
	public static int getRenderQuality() {
		if(isDraftRender()) return 0;
		return 2;
	}
	

	public static boolean isSchemaName(String s) {
		return currentSchemea.contentEquals(s);
	}
	
	public static boolean schemaNameContains(String s) {
		return currentSchemea.contains(s);
	
	}
	
	public static String getDocumentName() {
		return mainSessionName + "_" + currentSchemea;
	}
	
	
	public static ImageDimensions getFullScaleDocumentDimentsion() {
		return new ImageDimensions(fullScaleRenderWidth, fullScaleRenderHeight);
	}

	public static String getUserSessionPath() {
		return userSessionPath;
	}

	public static String getSampleLibPath() {
		return sampleLibPath;
	}


	public static String getMouseOrganImageCachePath() {
		return mouseOrganImageCachePath;
	}

}

