package MOUtils;

import java.io.File;

import MOApplication.MainDocument;
import MOApplication.Surface;
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
import MOImageCollections.ScaledImageAssetGroupManager;
import MOScene3D.SceneData3D;

public class GlobalSettings {
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// critical info used all over the system, once set can only be got
	// 
	// The sessionScale is set in init()
	// The theDocumentCoordSystem is set as soon as the MainDocument is instantiated
	private static float sessionScale = 0;
	private static ImageCoordinateSystem theDocumentCoordSystem;
	private static ScaledImageAssetGroupManager  theImageAssetGroupManager;
	private static MainDocument theDocument;
	private static Surface theSurface;
	private static SceneData3D theSceneData3D;
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// paths used by the system
	// 
	// The userSessionPath is set in init()
	private static String sampleLibPath = "C:\\simon\\art assets\\sample lib\\";
	private static String dataAssetsPath = "C:\\simon\\art assets\\data\\";
	private static String liveProjectsBasePath = "C:\\simon\\Artwork\\MouseOrgan Projects\\";
	private static String mouseOrganImageCachePath = "C:\\simon\\mouseOrganCaches\\defaultCache\\";
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
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Unique ID source
	// This should be accessed globally
	//
	static UniqueID uniqueIDSource;  
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Initialisation of Session Immutables	
	// Called from UserSession->initialiseUserSession() which must contain a call to Surface.initialiseSystem(....)
	// Surface.initialiseSystem(....) calls init(...), instantiates the MainDocument then calls setTheDocumentCoordSystem(...)
	public static String initialiseSession(String userSessionPth, float sessionScl, Surface surf) {
	// only called in Surface.initialiseSystem()
			
			if(uniqueIDSource == null) {
				uniqueIDSource = new UniqueID(1000000);
			}
		
		
			String fullPath = GlobalSettings.makeUserSessionPath(userSessionPth); // adds the sub path to the base path
		
			if (fullPath.contains(liveProjectsBasePath) == false) {
				System.out.println("GlobalSettings::init USER SESSION PATH IS NOT SET CORRECTLY ..... EXITING");
				System.out.println("please use makeUserSessionPath(String subPath) ");
				System.exit(0);
			}
	
			GlobalSettings.userSessionPath = fullPath;
			sessionScale = sessionScl;

			ImageProcessing.setInterpolationQuality(getRenderQuality());
			setDefaultSessionName();
	
			// for your convenience, and because we don't want a null one
			theImageAssetGroupManager = new ScaledImageAssetGroupManager();
			theSurface = surf;
			
			return fullPath;
		}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Initialisation	
	// Called from UserSession->initialiseUserSession() which must contain a call to Surface.initialiseSystem(....)
	// Surface.initialiseSystem(....) calls init(...), instantiates the MainDocument then calls setTheDocumentCoordSystem(...)
	public static void setDocumentDimensions(String userSessionPth, int fullScaleRenderW, int fullScaleRenderH, float sessionScl, Surface surf){
		// only called in Surface.initialiseSystem()
		
		if(userSessionPth.contains(liveProjectsBasePath)==false) {
			System.out.println("GlobalSettings::init USER SESSION PATH IS NOT SET CORRECTLY ..... EXITING");
			System.out.println("please use makeUserSessionPath(String subPath) ");
			System.exit(0);
		}
		
		
		GlobalSettings.userSessionPath = userSessionPth;
		sessionScale = sessionScl; 
		fullScaleRenderWidth = fullScaleRenderW;
		fullScaleRenderHeight = fullScaleRenderH;
		
		ImageProcessing.setInterpolationQuality(getRenderQuality());
		setDefaultSessionName();
		
		// for your convenience, and because we don't want a null one
		theImageAssetGroupManager = new ScaledImageAssetGroupManager();
		theSurface = surf;
		}
	
	public static void setTheDocumentCoordSystem(MainDocument md) {
		// only set by the MainDocument in Surface.initialiseSystem() after init()
		GlobalSettings.theDocument = md;
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
			System.out.println("FATAL:: GlobalSettings session scale has not been set! call GlobalSettings.initialiseSession(....) earlier. EXITING");
			System.exit(0);
		}
		return sessionScale;
	}
	
	public static MainDocument getDocument() {
		return theDocument;
	}
	
	public static Surface getTheApplicationSurface() {
		return theSurface;
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
	
	public static boolean isSessionName(String s) {
		return mainSessionName.contentEquals(s);
	}
	
	public static boolean sessionNameContains(String s) {
		return mainSessionName.contains(s);
	
	}
	

	public static boolean isSchemaName(String s) {
		return currentSchemea.contentEquals(s);
	}
	
	public static boolean schemaNameContains(String s) {
		return currentSchemea.contains(s);
	
	}
	
	public static String getDocumentName() {
		if(currentSchemea.equals("")) return mainSessionName;
		return mainSessionName + "_" + currentSchemea;
	}
	
	
	
	
	public static ImageDimensions getFullScaleDocumentDimension() {
		return new ImageDimensions(fullScaleRenderWidth, fullScaleRenderHeight);
	}
	
	
	public static String makeUserSessionPath(String subPath) {
		String userSessPath = getLiveProjectsBasePath() + subPath;
		
		boolean ok = MOUtils.MOStringUtils.checkDirectoryExist(userSessPath);
		
		if(!ok) {
			System.out.println("Fatal Error:: GlobalSettings.makeUserSessionPath - path " + userSessPath + " does not exist, please check");
			System.exit(0);
		}
		
		//userSessionPath = userSessPath;
		return userSessPath;
		
	}
	
	
	private static String getLiveProjectsBasePath() {
		
		return liveProjectsBasePath;
	}

	public static String getUserSessionPath() {
		return userSessionPath;
	}

	public static String getSampleLibPath() {
		return sampleLibPath;
	}
	
	public static String getDataAssetsPath(String type) {
		String pth = dataAssetsPath;
		if(type!=null) {
			pth = pth + type + "\\";
		}
		return pth;
	}
	
	
	public static void setMouseOrganImageCacheName(String cacheName) {
		// "C:\\mouseOrganImageCache2\\";
		mouseOrganImageCachePath = "C:\\simon\\mouseOrganCaches\\" + cacheName + "\\";
	}


	public static String getMouseOrganImageCachePath() {
		return mouseOrganImageCachePath;
	}

	public static ScaledImageAssetGroupManager getImageAssetGroupManager() {
		if(theImageAssetGroupManager==null) {
			System.out.println("GlobalSettings theImageAssetGroupManager has not been set!");
			
		}
		return theImageAssetGroupManager;
	}

	public static void setImageAssetGroupManager(ScaledImageAssetGroupManager theSpriteImageGroupManager) {
		// only set by the SpriteImageGroupManager when instantiated
		GlobalSettings.theImageAssetGroupManager = theSpriteImageGroupManager;
	}
	
	public static int getNextUniqueID() {
		return uniqueIDSource.getUniqueID();
	}
	
	public static void grabUniqueIDFromOtherSource(int n) {
		uniqueIDSource.grabID(n);
	}
	
	
	public static void setSceneData3D(SceneData3D sd3d) {
		theSceneData3D = sd3d;
	}
	
	public static SceneData3D getSceneData3D() {
		return theSceneData3D;
	}

}


