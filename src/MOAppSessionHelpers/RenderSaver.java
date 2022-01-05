package MOAppSessionHelpers;
import java.io.File;
import java.util.ArrayList;

import MOApplication.MainDocument;
//import MOCompositing.MainDocumentRenderTarget;

import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;

//////////////////////////////////////////////////////////////////////////////////
// Helps manage the saving process for renders, either as single images or as collections of images through the creation of a sub-directory.
// The user decides if images and directories from subsequent sessions are overwritten, or saved as new (differently named) files/directories. 
//
// The baseName is derived from the MainDocument and defines the general name for this set of renders. It prefixes all individually saved images, and the name of the directory if created.
//
// If using SESSION_DIFFERENTIATOR_INCREMENT, each session is saved with an incremented number in the name in the form "_sessionXXXX" (where XXXX is always a 4-character leading-zero-padded integer).
// The process is based on scanning the userSessionPath for files/folders containing such a string. The highest previously saved session number in the userSessionPath is identified and  the new incremented number is found.
//
// If using SESSION_DIFFERENTIATOR_OVERWRITE, this will overwrite previously saved files/directories saved. The action is sensitive to previously saved SESSION_DIFFERENTIATOR_INCREMENT sessions, and will target the highest 
// numerical session files/directories it finds on the UserSessionPath. If no previous SESSION_DIFFERENTIATOR_NUMERICAL is found, then the differentiating file string is 0000
//

//
// 
//
//
//
//
//
// Individual images, no sub directory
// UserSessionPath/baseName_sessionXXXX.png
//
// If a session saves out multiple images, then using a sub-directory is recommended . When a directory is made for multiple images, the directory is created with the "_sessionXXXX" string, not the files within.
//
// Using sub directory
// this is probably done when generating images with  masks or saving an image in layers
// UserSessionPath/baseName_sessionXXXX/basename_main.png and UserSessionPath/baseName_SESSION_DIFFERENTIATOR/basename_mask1.png, where#
// main and mask1 strings are got from the MainDocument
//
// Using Layers always creates a sub directory
// Using sub directory without Photoshop layering convention
// UserSessionPath/baseName_SESSION_DIFFERENTIATOR/img_MOLayer_(num increasing).png
// Using sub directory with Photoshop layering convention
// UserSessionPath/baseName_SESSION_DIFFERENTIATOR/img_MOLayer_(num increasing)_PSLayer_(num decreasing from 99).png
//
// subdirectory
public class RenderSaver {
	
	
	MainDocument theDocument;
	
	
	int currentSessionNumber = 0;
	boolean useSubDirectory = false;
	boolean useLayers = false;
	String subDirectory;
	int imageNumCounter = 0;
	
	boolean overwriteDirectoryContents = false;
	boolean photoshopLayerOrderNumbering = false;
	int photoshopLayerNumberMaxNum = 99;

	final public static int SESSION_FILENAME_OVERWRITE = 0;
	final public static int SESSION_FILENAME_INCREMENT = 1;
	
	
	int session_filename_mode = SESSION_FILENAME_INCREMENT; // this is the default as it's safer
	String session_differentiator_string = "0000";
	
	public RenderSaver(int mode, MainDocument rt) {
		
		this.theDocument = rt;
		this.session_filename_mode = mode;
		updateCurrentSessionNumber();
	}
	
	

	//
	// if the session_differentiator is set  to NUMERICAL or DATE_TIME then the system will always crate a new directory every session
	// and save all renders within this directory.
	// If it is not time stamped, then a new directory will be created on the first run, and files within will be
	// over-written on subsequent runs, which is sometimes desirable on long test cycles.
	//
	public void createSubDirectory(boolean usinglayers) {
		useLayers = usinglayers;
		useSubDirectory = true;
		updateCurrentSessionNumber();
		subDirectory = MOStringUtils.createDirectory(GlobalSettings.getUserSessionPath(), GlobalSettings.getDocumentName() + getCurrentSessionString(), false);
		
	}
	
	private void updateCurrentSessionNumber() {
		int i = searchDirectoryForHighestSessionNumber();
		if(session_filename_mode == SESSION_FILENAME_OVERWRITE) currentSessionNumber = i;
		if(session_filename_mode == SESSION_FILENAME_INCREMENT) currentSessionNumber = i + 1;
	}
	
	private String getCurrentSessionString() {
		String sessionNumberString = MOStringUtils.getPaddedNumberString(currentSessionNumber,4);
		String fullString = "_session" + sessionNumberString;
		System.out.println("getCurrentSessionString()::  " + fullString);
		return fullString;
	}

	void usePhotoshopLayerOrderNumbering(int maxNum) {
	// Can only be set with sub directory mode
	// when this is set to true the naming convention is altered to
	// load the images as photoshop layers via the photoshop
	// load image layers as stack script
	
		if(useSubDirectory==false) {
			System.out.println("RenderSaver: cannot set Photoshop Layer Ordering Mode without setting a sub directory first - use createSubDirectory(true)");
			return;
		}
		photoshopLayerOrderNumbering = true;
		photoshopLayerNumberMaxNum = maxNum;
	}
	
	

	public void saveImageFile() {
		if (useSubDirectory) {
			if(useLayers) {
				saveLayer(false);
				return;
			} else {
				
				String fullPathAndName = subDirectory + "\\" + GlobalSettings.getDocumentName() + ".png";
				System.out.println("saveRender and masks to folder: saving " + fullPathAndName);
				theDocument.getRenderTarget("main").saveRenderToFile(fullPathAndName);
			}
		} else {

			String path = GlobalSettings.getUserSessionPath();
			String timeStamp = MOStringUtils.getDateStamp();
			String fullPathAndName = path + GlobalSettings.getDocumentName() + "_" + timeStamp + ".png";
			System.out.println("saveRender: saving " + fullPathAndName);
			theDocument.getRenderTarget("main").saveRenderToFile(fullPathAndName);
		}

	}
	
	void saveLayer(boolean clearImage) {
		String name = "img";
		
		if (photoshopLayerOrderNumbering) {
			int thisPhotoshopLayerNum = photoshopLayerNumberMaxNum - imageNumCounter;
			name = name + "_PSLayer_" + thisPhotoshopLayerNum;
		}

		name = name + "_MOLayer_" + imageNumCounter;

		String fullPathAndName = subDirectory + "\\" + name + ".png";
		System.out.println("saveRenderLayers to folder: saving " + fullPathAndName);
		theDocument.getRenderTarget("main").saveRenderToFile(fullPathAndName);

		imageNumCounter++;
		
		if(clearImage) theDocument.getRenderTarget("main").clearImage();
	}
	


	private int searchDirectoryForHighestSessionNumber() {
		// if NOT using folders, look for the highest file name containing the session string
		// if using folders, look for the highest folder name containing the session string

		File folder = new File(GlobalSettings.getUserSessionPath());
		
		File[] listOfFilesAndFolders = folder.listFiles();
		ArrayList<File> foundFilesOrFolders = new ArrayList<File>();
		for (int i = 0; i < listOfFilesAndFolders.length; i++) {
			File thisFileOrFolder = listOfFilesAndFolders[i];
			
				
				String shortName = thisFileOrFolder.getName();
				boolean containsIncrementString =  shortName.contains("_session");
				if (containsIncrementString == true && thisFileOrFolder.isFile() && useSubDirectory==false) {
					System.out.println("Found a file called name = " + shortName);
					foundFilesOrFolders.add(thisFileOrFolder);
				}
				if (containsIncrementString == true && thisFileOrFolder.isDirectory() && useSubDirectory==true) {
					System.out.println("Found a directory called name = " + shortName);
					foundFilesOrFolders.add(thisFileOrFolder);
				}
				
		}
		
		if(foundFilesOrFolders.size()==0) return 0;
		
		int highestNumber = 0;
		for (int i = 0; i < foundFilesOrFolders.size(); i++) {
			String thisFileOrFolder = foundFilesOrFolders.get(i).toString();
			int indexOfSessionNumber = thisFileOrFolder.indexOf("_session") + 8;
			String numberSubString = thisFileOrFolder.substring(indexOfSessionNumber, indexOfSessionNumber+4);
			int ival = Integer.parseInt(numberSubString);
			System.out.println("Found number string = " + numberSubString + " integer value " + ival);
			if(ival > highestNumber) highestNumber = ival;
		}
		
		System.out.println("Found highest number " + highestNumber);
		return highestNumber;
		
		
	}
	

}












	