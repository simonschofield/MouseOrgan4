package MOAppSessionHelpers;
import java.io.File;
import java.util.ArrayList;

import MOApplication.MainDocument;
//import MOCompositing.MainDocumentRenderTarget;
import MOCompositing.RenderTarget;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;

//////////////////////////////////////////////////////////////////////////////////
// Helps manage the saving process for renders, either directly to the userSession directory, or in a specially created sub-directory
// The user decides if images and directories from subsequent sessions are overwritten, or saved as new (automatically enumerated) files/directories by
// setting the mode to either RenderSaver.FILENAME_OVERWRITE or RenderSaver.FILENAME_INCREMENT
//
// If using FILENAME_INCREMENT, each session is saved with an incremented number in the name in the form "_sessionXXXX" (where XXXX is always a 4-character leading-zero-padded integer).
// The process is based on scanning the userSessionPath for files/folders containing such a string. The highest previously saved session number in the userSessionPath is identified and  the new incremented number is found.
//
// If using FILENAME_OVERWRITE, this will overwrite previously saved files/directories saved. The action is sensitive to previously saved SESSION_DIFFERENTIATOR_INCREMENT sessions, and will target the highest 
// numerical session files/directories it finds on the UserSessionPath. If no previous SESSION_DIFFERENTIATOR_NUMERICAL is found, then the differentiating file string is 0000
//
// calling saveDocumentImages() causes the main render target and all supplementary render targets to be saved with the following naming conventions.
//
// NOT USING A SUB DIRECTORY
// Files are saved to the userSessionDirectory with the following convention
//		GlobalSettings.mainSessionName + "_" + GlobalSettings.currentSchemea + "_" +  renderTargetName + enumerator + fileExtension
//
// USING A SUB DIRECTORY
// The naming convention for the directory is 
// userSessionPath/ GlobalSettings.mainSessionName + enumerator
// Within this renders are files saved with the following convention 
// 		GlobalSettings.mainSessionName + "_" + GlobalSettings.currentSchemea + "_" +  renderTargetName + fileExtension
//
// SAVING LAYERS
// Layers can only be generated from the Document's Main() render. Layers are always saved in an assigned sub-directory.
// calling saveLayer(finalLayerBoolean) causes the following
// 
// the Layer filename follows the convention 
// GlobalSettings.mainSessionName + "_" + GlobalSettings.currentSchemea + "_" +  renderTargetName + "_Layer_" + (num increasing or decreasing) + ".png"
// 
// Once saved, the render is cleared, unless it is the final one. Upon saving the final layer, any other supplementary renders from the Document are saved as whole images following the 
// USING A SUB DIRECTORY protocol
// Utilising reverse layer naming enable the correctly ordered import of layers using Photoshop's File->Scripts->Load Files into Stack for depth sorted layers
public class RenderSaver {
	
	
	MainDocument theDocument;
	
	int currentSessionEnumerator = 0;
	boolean useSubDirectory = false;
	
	String subDirectoryPath;
	
	int layerCounter = 0;
	boolean useReverseLayerNumbering = false;
	
	final public static int FILENAME_OVERWRITE = 0;
	final public static int FILENAME_INCREMENT = 1;
	
	
	int session_filename_mode = FILENAME_INCREMENT; // this is the default as it's safer
	String session_differentiator_string = "0000";
	
	public RenderSaver(int mode, boolean useSubdirectory, MainDocument doc) {
		
		this.theDocument = doc;
		this.session_filename_mode = mode;
		
		useSubDirectory = useSubdirectory;
		
		layerCounter = 0;
		
		updateCurrentSessionEnumerator();
		if(useSubDirectory) {
			subDirectoryPath = MOStringUtils.createDirectory(GlobalSettings.getUserSessionPath(), GlobalSettings.getDocumentName() + getSessionEnumeratorString() + "\\", false);
		}
	}
	
	public void useReverseLayerNumbering(int startNum) {
		useReverseLayerNumbering = true;
		layerCounter = startNum;
	}
	

	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// saveDocumentImages This is called by the user, probably in UserSession.finaliseUserSession
	// 
	// 
	// 
	// 
	public void saveDocumentImages() {
		if (useSubDirectory) {
			// if useSubDirectory==true assume : 
			// if FILENAME_INCREMENT an new enumerated directory has been created at instantiation of this object and subDirectoryPath set to this
			// if FILENAME_OVERWRITE then the previously-saved highest-numbered session directory has been set as the subDirectoryPath, so the files within are overwritten
			saveDocumentMainImage(subDirectoryPath,"");
			saveDocumentSupplementaryImages(subDirectoryPath,"");
			
		} else {
			// if FILENAME_INCREMENT the filename generated will use the highest existing + 1 as the enumerator
			// if FILENAME_OVERWRITE the filename generated will use the highest existing so will overwrite this file
			
			String numerator = getSessionEnumeratorString();
			saveDocumentMainImage(GlobalSettings.getUserSessionPath(),numerator);
			saveDocumentSupplementaryImages(GlobalSettings.getUserSessionPath(),numerator);
		}

	}
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Saving layers assumes this: The layers saved are derived from the main Document Render
	// and these are cleared after each save.
	// The final layer is not cleared, and when the final layer is saved, any supplementary Document RenderTarget
	// images are also saved to the directory.
	// The calling of saveLayer is going to be managed by the user
	//
	// sometimes the user will want to save the supplementary images intact between layers (such as in making a selection mask for the whole image)
	// sometimes they want the supp images saved and deleted per layer - as in multiple sequenced images, hence saveSupplementaryImagesAtEachLayer
	
	//
	public void saveLayer(boolean finalLayer, boolean saveSupplementaryImagesAtEachLayer) {
		if(useSubDirectory==false) {
			System.out.println("RenderSaver: saveLayer - cannot save layers without setting a sub directory first - set third argument of constructor to true");
			return;
		}
		
		String name = theDocument.getMain().getFullSessionName();
		
		String layerCounterString = MOStringUtils.getPaddedNumberString(layerCounter,2);
		String layerString = "_Layer_" + layerCounterString;
		
		name = name + layerString;

		String fullPathAndName = subDirectoryPath + "\\" + name + ".png";
		System.out.println("saveRenderLayers to folder: saving " + fullPathAndName);
		theDocument.getRenderTarget("main").saveRenderToFile(fullPathAndName);
		
		if(saveSupplementaryImagesAtEachLayer) {
			String dirPath = subDirectoryPath + "\\";
			saveDocumentSupplementaryImages(dirPath, layerString);
			if(!finalLayer) {
				clearDocumentSupplementaryImages();
			}
		}
		
		if(!finalLayer) {
			theDocument.getRenderTarget("main").clearImage();
			
		}
		
		
		if (useReverseLayerNumbering) {
			layerCounter--;
		} else {
			layerCounter++;
		}
		
		
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// private methods
	// 
	// 
	
	private void updateCurrentSessionEnumerator() {
		int i = searchDirectoryForHighestSessionEnumerator();
		if(session_filename_mode == FILENAME_OVERWRITE) currentSessionEnumerator = i;
		if(session_filename_mode == FILENAME_INCREMENT) currentSessionEnumerator = i + 1;
	}
	
	private String getSessionEnumeratorString() {
		// turns the integer value currentSessionEnumerator into a String of 
		// of the following format "_sessionXXXX" where XXXX is the 4 figure zero-padded number.
		String sessionNumberString = MOStringUtils.getPaddedNumberString(currentSessionEnumerator,4);
		String fullString = "_session" + sessionNumberString;
		//System.out.println("getCurrentSessionString()::  " + fullString);
		return fullString;
	}
	
	private void saveDocumentMainImage(String fullPath, String enumerator) {
		// this is where the document needs to have its own name and schemaName
		
			String fullSessionName = theDocument.getMain().getFullSessionName();
			String ext = theDocument.getMain().getFileExtension();
			String fullPathAndName = fullPath + fullSessionName + enumerator + ext;
			theDocument.getMain().saveRenderToFile(fullPathAndName);
		
		//theDocument.getRenderTarget("main").saveRenderToFile(fullPathAndName  + ".png" );
		
	}
	
	private void saveDocumentSupplementaryImages(String fullPath, String enumerator) {
		if(theDocument.getNumRenderTargets()==1) return;
		for(int n = 1; n < theDocument.getNumRenderTargets(); n++) {
			RenderTarget rt = theDocument.getRenderTarget(n);
			String fullSessionName = rt.getFullSessionName();
			String ext = rt.getFileExtension();
			String fullPathAndName =  fullPath + fullSessionName + enumerator + ext;
			theDocument.getRenderTarget(n).saveRenderToFile(fullPathAndName);
		}
	}
	
	private void clearDocumentSupplementaryImages() {
		if(theDocument.getNumRenderTargets()==1) return;
		for(int n = 1; n < theDocument.getNumRenderTargets(); n++) {
			theDocument.getRenderTarget(n).clearImage();;
		}
		
	}

	private int searchDirectoryForHighestSessionEnumerator() {
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
					//System.out.println("Found a file called name = " + shortName);
					foundFilesOrFolders.add(thisFileOrFolder);
				}
				if (containsIncrementString == true && thisFileOrFolder.isDirectory() && useSubDirectory==true) {
					//System.out.println("Found a directory called name = " + shortName);
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
			//System.out.println("Found number string = " + numberSubString + " integer value " + ival);
			if(ival > highestNumber) highestNumber = ival;
		}
		
		System.out.println("Found highest number " + highestNumber);
		return highestNumber;
		
		
	}
	

}












	