package MOUtils;

import java.io.File;

import MOApplication.MainDocument;
import MOApplication.ROIManager;
import MOApplication.RenderSaver;
import MOApplication.Surface;
import MOImage.ImageDimensions;
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
	private static SceneData3D theSceneData3D; // set by the singleton SceneData3D on instantiation
	private static RenderSaver theRenderSaver; // set on instantiation of the render saver
	private static ROIManager theROIManager;

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// paths used by the system
	//
	// The userSessionPath is set in init()
	private static String sampleLibPath = "C:\\simon\\art assets\\sample lib\\";
	private static String dataAssetsPath = "C:\\simon\\art assets\\data\\";
	private static String liveProjectsBasePath = "C:\\simon\\Artwork\\MouseOrgan Projects\\";
	private static String mouseOrganImageCachePath = "C:\\simon\\mouseOrganCaches\\defaultCache\\";
	private static String userSessionPath = "";

	
	/**
	 * The default session name is set to be the same as the containing directory<p>
	 * But can be set by the user
	 */
	public static String mainSessionName = "";
	
	/**
	 * Set by the user. Is used in file saving. In many cases it is not necessary to set this<p>
	 * as the individual files saved by the render-saver are named automatically
	 */
	public static String currentSchemea = "";


	public static boolean printOn = false;



	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Unique ID source
	// This should be accessed globally
	//
	static UniqueID uniqueIDSource;

	
	/**
	 * Initialisation of Session Immutable variables. Called early on in UserSession->initialiseUserSession()  <p>
	 * Also sets the default session name and instantiates the singleton theImageAssetGroupManager
	 * @param userSessionPth - the path to the current session directory from liveProjectsBasePath "C:\\simon\\Artwork\\MouseOrgan Projects\\". Must end with a "\\"
	 * @param sessionScl - the session scale of the current session between 0.1-1.0. 
	 * @param surf - Reference to the UserSession (it is subclassed from the Surface class).
	 * @return - the full path to the current session
	 */
	public static String initialiseSession(String userSessionPth, float sessionScl, Surface surf) {
	// only called in Surface.initialiseSystem()

			if(uniqueIDSource == null) {
				uniqueIDSource = new UniqueID(1000000);
			}


			String fullPath = getLiveProjectsBasePath() + userSessionPth;

			boolean ok = MOUtils.MOStringUtils.checkDirectoryExist(fullPath);

			if(!ok) {
				System.out.println("Fatal Error:: GlobalSettings.makeUserSessionPath - path " + fullPath + " does not exist, please check .... EXITING");
				System.exit(0);
			}

			if (!fullPath.contains(liveProjectsBasePath)) {
				System.out.println("GlobalSettings::init USER SESSION PATH IS NOT SET CORRECTLY ..... EXITING");
				System.out.println("please use makeUserSessionPath(String subPath) ");
				System.exit(0);
			}

			GlobalSettings.userSessionPath = fullPath;
			sessionScale = sessionScl;

			//ImageProcessing.setInterpolationQuality(getRenderQuality());
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
	/*
	public static void setDocumentDimensions(String userSessionPth, int fullScaleRenderW, int fullScaleRenderH, float sessionScl, Surface surf){
		// only called in Surface.initialiseSystem()

		if(!userSessionPth.contains(liveProjectsBasePath)) {
			System.out.println("GlobalSettings::init USER SESSION PATH IS NOT SET CORRECTLY ..... EXITING");
			System.out.println("please use makeUserSessionPath(String subPath) ");
			System.exit(0);
		}


		GlobalSettings.userSessionPath = userSessionPth;
		sessionScale = sessionScl;
		fullScaleRenderWidth = fullScaleRenderW;
		fullScaleRenderHeight = fullScaleRenderH;

		//ImageProcessing.setInterpolationQuality(getRenderQuality());
		setDefaultSessionName();

		// for your convenience, and because we don't want a null one
		theImageAssetGroupManager = new ScaledImageAssetGroupManager();
		theSurface = surf;
		}
		*/

	

	

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// public data access methods
	//
	//
	/**
	 * @return - the session scale. usually 0.1, 0.25, 0.5 or 1.0
	 */
	public static float getSessionScale() {
		if(sessionScale == 0) {
			System.out.println("FATAL:: GlobalSettings session scale has not been set! call GlobalSettings.initialiseSession(....) earlier. EXITING");
			System.exit(0);
		}
		return sessionScale;
	}

	/**
	 * @return
	 */
	public static MainDocument getDocument() {
		return theDocument;
	}

	/**
	 * @return
	 */
	public static Surface getTheApplicationSurface() {
		return theSurface;
	}

	/**
	 * @return
	 */
	public static ImageCoordinateSystem getTheDocumentCoordSystem() {
		if(theDocumentCoordSystem == null) {
			System.out.println("GlobalSettings theDocumentCoordSystem has not been set, please create MainDocument! EXITING");
			//System.exit(0);
		}
		return theDocumentCoordSystem;
	}

	
	
	/**
	 * The principle place where the document name is set. This may need to be in the mainDocument class
	 * @return
	 */
	public static String getDocumentName() {
		if(currentSchemea.equals("")) {
			return mainSessionName;
		}
		return mainSessionName + "_" + currentSchemea;
	}



	/**
	 * @return The ImageDimensions of the output render targets at a render scale of 1
	 */
	public static ImageDimensions getFullScaleDocumentDimension() {
		int fullScaleRenderWidth = (int) (theDocumentCoordSystem.getBufferWidth()* sessionScale);
		int fullScaleRenderHeight = (int) (theDocumentCoordSystem.getBufferHeight()* sessionScale);
		return new ImageDimensions(fullScaleRenderWidth, fullScaleRenderHeight);
	}


	/**
	 * @return - the path to the current project directory (where your UserSession file is)
	 */
	public static String getUserSessionPath() {
		return userSessionPath;
	}

	/**
	 * @return - the path to the sampleLib
	 */
	public static String getSampleLibPath() {
		return sampleLibPath;
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getDataAssetsPath(String type) {
		String pth = dataAssetsPath;
		if(type!=null) {
			pth = pth + type + "\\";
		}
		return pth;
	}

	

	/**
	 * @return
	 */
	public static ScaledImageAssetGroupManager getImageAssetGroupManager() {
		if(theImageAssetGroupManager==null) {
			System.out.println("GlobalSettings theImageAssetGroupManager has not been set!");

		}
		return theImageAssetGroupManager;
	}



	/**
	 * @return
	 */
	public static SceneData3D getSceneData3D() {
		return theSceneData3D;
	}


	/**
	 * @return
	 */
	public static RenderSaver getRenderSaver() {
		return theRenderSaver;
	}

	
	/**
	 * @return
	 */
	public static ROIManager getROIManager() {
		return theROIManager;
	}
	
	

	
	//////////////////////////////////////////////////////
	// private methods, and methods not to be used by the user here
	//
	//
	
	/**
	 * Called internally upon instantiation of the singleton MainDocument. Creates a global reference to the MainDocument, and its ImageCoordinateSystem
	 * @param md - reference to the main document, 
	 */
	public static void setTheDocumentCoordSystem(MainDocument md) {
		// only set by the MainDocument in Surface.initialiseSystem() after init()
		GlobalSettings.theDocument = md;
		GlobalSettings.theDocumentCoordSystem = md.getCoordinateSystem();
	}
	
	/**
	 * @param cacheName - called by ArtAssetLoaderHelper.loadAssets
	 */
	public static void setMouseOrganImageCacheName(String cacheName) {
		// "C:\\mouseOrganImageCache2\\";
		mouseOrganImageCachePath = "C:\\simon\\mouseOrganCaches\\" + cacheName + "\\";
	}
	
	/**
	 * @return - called by ArtAssetPaths class
	 */
	public static String getMouseOrganImageCachePath() {
		return mouseOrganImageCachePath;
	}
	
	
	/**
	 * @param theSpriteImageGroupManager - ONLY SET BY SINGLETON ScaledImageAssetGroupManager upon instantiation
	 */
	public static void setImageAssetGroupManager(ScaledImageAssetGroupManager theSpriteImageGroupManager) {
		// only set by the SpriteImageGroupManager when instantiated
		GlobalSettings.theImageAssetGroupManager = theSpriteImageGroupManager;
	}
	
	
	/**
	 * @param sd3d - ONLY SET BY SINGLETON SceneData3D upon instantiation
	 */
	public static void setSceneData3D(SceneData3D sd3d) {
		theSceneData3D = sd3d;
	}
	
	
	/**
	 * @param rs - ONLY SET BY SINGLETON RenderSaver upon instantiation
	 */
	public static void setRenderSaver(RenderSaver rs) {
		theRenderSaver = rs;
	}
	
	
	/**
	 * @param rm - ONLY SET BY SINGLETON ROIManager upon instantiation
	 */
	public static void setROIManager(ROIManager rm) {
		theROIManager = rm;
	}
	
	
	
	
	
	/**
	 * The default session name is set to be the same as the containing directory
	 */
	private static void setDefaultSessionName() {
		// the default document name is the directory containing the session
		if(mainSessionName.equals("")){
			File f  = new File(GlobalSettings.getUserSessionPath());
			mainSessionName = f.getName();
			System.out.println("default doc name is " + mainSessionName);
		}
	}
	
	
	
	
	private static String getLiveProjectsBasePath() {

		return liveProjectsBasePath;
	}
	
	
	//////////////////////////////////////////////////////
	// Public but Depricated
	//
	//
	
		/**
		 * DEPRICATED - not assuming anything about draft renders now
		 * use ImageProcessing.getInterpolationQuality()
		 * 
		 * @return
		 */
		public static boolean isDraftRender() {
			if (GlobalSettings.getSessionScale() < 1.0f) {
				return true;
			}
			return false;
		}
	
		/**
		 * DEPRICATED
		 * 
		 * @return - returns the next free UniqueID available DEPRICATED
		 */
		public static int getNextUniqueID() {
			return uniqueIDSource.getUniqueID();
		}
	
		/**
		 * DEPRICATED
		 * 
		 * @param n - the unique ID to be grabbed from the UniqueID source. DEPRICATED
		 */
		public static void grabUniqueIDFromOtherSource(int n) {
			uniqueIDSource.grabID(n);
		}
	
		/**
		 * DEPRICATED
		 * 
		 * @param s
		 * @return
		 */
		public static boolean isSessionName(String s) {
			return mainSessionName.contentEquals(s);
		}
	
		/**
		 * DEPRICATED
		 * 
		 * @param s
		 * @return
		 */
		public static boolean sessionNameContains(String s) {
			return mainSessionName.contains(s);
	
		}
	
		/**
		 * DEPRICATED
		 * 
		 * @param s
		 * @return
		 */
		public static boolean isSchemaName(String s) {
			return currentSchemea.contentEquals(s);
		}
	
		/**
		 * DEPRICATED
		 * 
		 * @param s
		 * @return
		 */
		public static boolean schemaNameContains(String s) {
			return currentSchemea.contains(s);
	
		}

}


