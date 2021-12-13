package MOUtils;

public class MOUtilGlobals {
	
	///////////////////
	// private as these are VERY important
	//
	private static float sessionScale = 0;
	private static ImageCoordinateSystem theDocumentCoordSystem;
	
	
	
	////////////////////
	public static String sampleLibPath = "C:\\simon\\sample lib\\";
	public static String mouseOrganImageCachePath = "C:\\mouseOrganImageCache2\\";
	public static String userSessionPath = "";
	
	
	// MOUtilGlobals.sampleLibPath
	// MOUtilGlobals.theDocumentCoordSystem
	// MOUtilGlobals.getSessionScale();
	// MOUtilGlobals.userSessionPath;
	
	
	
	public static void setSessionScale(float s) {
		// only the surface can set this on instantiation by the Surface!!!!
		sessionScale = s;
	}
	
	public static float getSessionScale() {
		if(sessionScale == 0) {
			System.out.println("MOUtilGlobals session scale has not been set! EXITING");
			System.exit(0);
		}
		return sessionScale;
	}

	public static ImageCoordinateSystem getTheDocumentCoordSystem() {
		return theDocumentCoordSystem;
	}

	public static void setTheDocumentCoordSystem(ImageCoordinateSystem theDocumentCoordSystem) {
		// only set by surface.initiaiseDocument
		MOUtilGlobals.theDocumentCoordSystem = theDocumentCoordSystem;
	}
}


