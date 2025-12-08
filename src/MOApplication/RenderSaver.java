package MOApplication;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import MOCompositing.RenderTargetInterface;
import MOUtils.GlobalSettings;
import MOUtils.MOStringUtils;


/**
    * The Render Saver Class. Helps manage the saving process for renders to a new directory created in the User Session directory
	* The user decides if directories from subsequent sessions are overwritten, or saved as new (automatically enumerated) files/directories by
	* setting the mode to either RenderSaver.FILENAME_OVERWRITE or RenderSaver.FILENAME_INCREMENT <p>
	*
	* If using FILENAME_INCREMENT, each session is saved with an incremented number in the name in the form "_sessionXXXX" (where XXXX is always a 4-character leading-zero-padded integer).<p>
	* The process is based on scanning the userSessionPath for files/folders containing such a string. The highest previously saved session number in the userSessionPath is identified and  the new incremented number is found.<p>
	*
	* If using FILENAME_OVERWRITE, this will overwrite previously saved directory (the one with the highest enumeration so far). The action is sensitive to previously saved SESSION_DIFFERENTIATOR_INCREMENT sessions, and will target the highest
	* numerical session files/directories it finds on the UserSessionPath. If no previous SESSION_DIFFERENTIATOR_NUMERICAL is found, then the differentiating file string is 0000<p>
	*
	* calling saveDocumentImages() causes the main render target and all other render targets to be saved with the following naming conventions.<p>
	*
	*
	* when creating the  SUB DIRECTORY The naming convention for the directory is<p>
	* userSessionPath/ GlobalSettings.mainSessionName + enumerator
	* Within this renders are files saved with the following convention
	* 		GlobalSettings.mainSessionName + "_" + GlobalSettings.currentSchemea + "_" +  renderTargetName + fileExtension<p>
	* 
	* Also saves a copy of the UserSession.java file in a directory called "UserSessionSourceCodeArchive" within the new sub-directory<p>
	* 
	* If isActive is set to false, then nothing is saved<p>
	*
*/
public class RenderSaver {


	MainDocument theDocument;
	boolean isActive = true;

	int currentSessionEnumerator = 0;

	
	boolean subDirectoryCreated = false;
	String subDirectoryPath;

	

	final public static int FILENAME_OVERWRITE = 0;
	final public static int FILENAME_INCREMENT = 1;


	int session_filename_mode = FILENAME_INCREMENT; // this is the default as it's safer
	String session_differentiator_string = "0000";


	boolean saveUserSessionSourceCode = true;

	/**
	 * Helps manage the saving process for MainDocument's renders to a new directory created in the User Session directory
	* If using FILENAME_INCREMENT, each session is saved with an incremented number in the name in the form "_sessionXXXX" (where XXXX is always a 4-character leading-zero-padded integer).
	* The process is based on scanning the userSessionPath for files/folders containing such a string. The highest previously saved session number in the userSessionPath is identified and  the new incremented number is found.<p>
	*
	* If using FILENAME_OVERWRITE, this will overwrite previously saved directory (the one with the highest enumeration so far). The action is sensitive to previously saved SESSION_DIFFERENTIATOR_INCREMENT sessions, and will target the highest,<P>
	* numerical session files/directories it finds on the UserSessionPath. If no previous SESSION_DIFFERENTIATOR_NUMERICAL is found, then the differentiating file string is 0000
	* @param mode - either RenderSaver.FILENAME_INCREMENT or enderSaver.FILENAME_OVERWRITE, see above
	* @param doc - reference to the MainDocument singleton
	*/
	public RenderSaver(int mode, MainDocument doc) {

		this.theDocument = doc;
		this.session_filename_mode = mode;

		updateCurrentSessionEnumerator();

		GlobalSettings.setRenderSaver(this);
	}

	/**
	 * @param b
	 */
	public void setActive(boolean b) {
		isActive = b;
	}

	/**
	 * @return
	 */
	public boolean isActive() {
		if(!isActive){
			System.out.println( "RenderSaver is inactive - no file saves" );
		}
		return isActive;
	}


	/**
	 * saveDocumentImages is called by the user when the renders are required to be saved, probably in UserSession.finaliseUserSession
	 *
	 */
	public void saveDocumentImages() {

		if( !isActive()) {
			return;
		}

		String currentNumeratorString = getSessionEnumeratorString();

		createSubDirectory();
		saveAllDocumentImages(subDirectoryPath,"");

		if(saveUserSessionSourceCode) {
			saveUserSessionSourceCode(GlobalSettings.getUserSessionPath(),currentNumeratorString);
		}


	}

	/**
	 * @param pth
	 */
	public void saveAllImagesInUserSpecifiedLocation(String pth) {
		// called from the save menu
		//saveDocumentMainImage(pth,"");
		saveAllDocumentImages(pth,"");
	}

	/**
	 * 
	 */
	public void saveUserSessionSourceCode() {
		String currentNumeratorString = getSessionEnumeratorString();
		saveUserSessionSourceCode(GlobalSettings.getUserSessionPath(),currentNumeratorString);
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////
	// private methods
	//
	//
	
	private void createSubDirectory() {

		if(!subDirectoryCreated) {
			subDirectoryPath = MOStringUtils.createDirectory(GlobalSettings.getUserSessionPath(), GlobalSettings.mainSessionName + getSessionEnumeratorString() + "\\", false);
			subDirectoryCreated = true;
		}
	}

	private void updateCurrentSessionEnumerator() {
		int i = searchDirectoryForHighestSessionEnumerator();
		if(session_filename_mode == FILENAME_OVERWRITE) {
			currentSessionEnumerator = i;
		}
		if(session_filename_mode == FILENAME_INCREMENT) {
			currentSessionEnumerator = i + 1;
		}
	}

	private String getSessionEnumeratorString() {
		// turns the integer value currentSessionEnumerator into a String of
		// of the following format "_sessionXXXX" where XXXX is the 4 figure zero-padded number.
		String sessionNumberString = MOStringUtils.getPaddedNumberString(currentSessionEnumerator,4);
		String fullString = "_session" + sessionNumberString;
		//System.out.println("getCurrentSessionString()::  " + fullString);
		return fullString;
	}



	private void saveAllDocumentImages(String fullPath, String enumerator) {
		//if(theDocument.getNumRenderTargets()==1) return;
		for(int n = 0; n < theDocument.getNumRenderTargets(); n++) {
			RenderTargetInterface rt = theDocument.getRenderTarget(n);
			String fullSessionName = rt.getFullSessionName();

			// don't want to save the sprite ID render if there is one.
			if(fullSessionName.contains("spriteIDRenderTarget")) {
				continue;
			}


			String ext = rt.getFileExtension();
			String fullPathAndName =  fullPath + fullSessionName + enumerator + ext;
			theDocument.getRenderTarget(n).saveRenderToFile(fullPathAndName);
		}
	}



	private void saveUserSessionSourceCode(String fullPath, String enumerator) {
		String sourecCodeDir = ensureUserSessionSourceCodeDirectory();
		String fullSessionName = theDocument.getMain().getFullSessionName();
		Path srcPath = Paths.get(GlobalSettings.getUserSessionPath() + "UserSession.java");

		Path destPath = Paths.get(sourecCodeDir + fullSessionName + enumerator + ".txt");
		try {
			Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("RenderSaver::saveUserSessionSourceCode  problems saving file " + destPath);
			e.printStackTrace();
		}
	}

	private String ensureUserSessionSourceCodeDirectory() {
		String dirName = GlobalSettings.getUserSessionPath() + "UserSessionSourceCodeArchive\\";

		
		dirName = subDirectoryPath + "\\UserSessionSourceCodeArchive\\";
		


		if(!MOStringUtils.checkDirectoryExist(dirName)) {
			MOStringUtils.createDirectory(dirName);
		}
		return dirName;
	}


	private int searchDirectoryForHighestSessionEnumerator() {
		// if NOT using folders, look for the highest file name containing the session string
		// if using folders, look for the highest folder name containing the session string

		String pth = GlobalSettings.getUserSessionPath();
		System.out.println("in render save pth is = " + pth);

		File folder = new File(pth);

		File[] listOfFilesAndFolders = folder.listFiles();
		ArrayList<File> foundFilesOrFolders = new ArrayList<>();
		
		
		for (File thisFileOrFolder : listOfFilesAndFolders) {
			String shortName = thisFileOrFolder.getName();
				boolean containsIncrementString =  shortName.contains("_session");
				
				if (containsIncrementString && thisFileOrFolder.isDirectory()) {
					//System.out.println("Found a directory called name = " + shortName);
					foundFilesOrFolders.add(thisFileOrFolder);
				}
		}
		

		if(foundFilesOrFolders.size()==0) {
			return 0;
		}

		int highestNumber = 0;
		for (File foundFilesOrFolder : foundFilesOrFolders) {
			String thisFileOrFolder = foundFilesOrFolder.toString();
			int indexOfSessionNumber = thisFileOrFolder.indexOf("_session") + 8;
			String numberSubString = thisFileOrFolder.substring(indexOfSessionNumber, indexOfSessionNumber+4);
			int ival = Integer.parseInt(numberSubString);
			//System.out.println("Found number string = " + numberSubString + " integer value " + ival);
			if(ival > highestNumber) {
				highestNumber = ival;
			}
		}

		System.out.println("Found highest number " + highestNumber);
		return highestNumber;


	}


}












