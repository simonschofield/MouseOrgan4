package ImageLoadingClasses;
import java.io.File;
import java.util.ArrayList;

import MOUtils.MOMaths;



//////////////////////////////////////////////////////////////////////////////////////////////
//DirectoryFileNameScanner =  improved version of  DirectoryItemScanner
//abstract DirectoryItemGroup class. 
//deals with collecting the names of a type of item (.png, .svg etc.) within the specified directory
public class DirectoryFileNameScanner  {

	private String directoryPath = "";

	// filters
	private String fileType = "";
	private String fileNameContains = "";

	private ArrayList<String> fullPathAndFileNamesList = new ArrayList<String>();
	private ArrayList<String> shortFileNamesList = new ArrayList<String>();

	public DirectoryFileNameScanner() {
		

	}
	
	public DirectoryFileNameScanner(String targetDirectory, String fileTp) {
		setTargetDirectory( targetDirectory);
		setFileType(fileTp);
	}
	
	void setTargetDirectory(String targetDirectory) {
		directoryPath = targetDirectory;
		cacheFileNames();
	}
	
	void setFileType(String fileTp) {
		fileType = fileTp;
		cacheFileNames();
	}

	public void setFileNameContains(String fileStrContains) {
		fileNameContains = fileStrContains;
		cacheFileNames();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Limits the numerical range of files to a sub-range
	// This is applied to the range of files found after cacheFileNames()
	public void cropFileList(Integer low, Integer high) {
		int numExistingFiles = getNumFiles();
		if(high == null) high = numExistingFiles;
		if(low == null) low = 0;
		
		low = MOMaths.constrain(low, 0, numExistingFiles-1);
		high = MOMaths.constrain(high, 1, numExistingFiles);

		ArrayList<String> tempFullPathAndFileNamesList = new ArrayList<String>();
		ArrayList<String> tempShortFileNamesList = new ArrayList<String>();
		
		for(int n = low; n < high; n++) {
			String thisFullPathAndName = fullPathAndFileNamesList.get(n);
			String thisShortFileName = shortFileNamesList.get(n);
			tempFullPathAndFileNamesList.add(thisFullPathAndName);
			tempShortFileNamesList.add(thisShortFileName);
		}
		fullPathAndFileNamesList = tempFullPathAndFileNamesList;
		shortFileNamesList = tempShortFileNamesList;
	}
	

	
	String getTargetDirectory() {
		return directoryPath;
	}
	

	ArrayList<String> getShortNameList() {
		return shortFileNamesList;
	}

	ArrayList<String> getFullPathAndFileNamesList() {
		return fullPathAndFileNamesList;
	}

	int getNumFiles() {
		return shortFileNamesList.size();
	}

	////////////////////////////////////////////////////////////////////////////
	// Private methods
	private void cacheFileNames() {
		if(directoryPath.equals("")) {
			System.out.println("DirectoryFileNameScanner:: target directory is not defined");
			return;
		}
		
		fullPathAndFileNamesList = new ArrayList<String>();
		shortFileNamesList = new ArrayList<String>();
		//System.out.println("cacheFileNames: dir path "+ directoryPath);
		File folder = new File(directoryPath);
		
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fullPathAndName = listOfFiles[i].getAbsolutePath();
				String shortFileNameWithExtension = listOfFiles[i].getName();
				//if (fileName.endsWith(fileEndsWith) || isWildCard(fileEndsWith) ) {
				if (fileNameMeetsCriteria(shortFileNameWithExtension)) {
					fullPathAndFileNamesList.add(fullPathAndName);
					
					
					int strlen = shortFileNameWithExtension.length();
					String fileNameOnly = shortFileNameWithExtension.substring(0, strlen - 4);
					shortFileNamesList.add(fileNameOnly);
					
					
				}
			} else if (listOfFiles[i].isDirectory()) {
				// ignore
			}
		}
		
	}
	

	private boolean fileNameMeetsCriteria(String shortFileNameWithExtension) {

		// test ending
		if (isWildCard(fileType) == false) {
			// if the extension is NOT  the upper OR lower case versions of the fileType string return false
			if ( shortFileNameWithExtension.endsWith(fileType.toLowerCase())==false &&
				 shortFileNameWithExtension.endsWith(fileType.toUpperCase())==false     ) return false;
		}
		// test contains
		if (isWildCard(fileNameContains) == false) {
			if (shortFileNameWithExtension.contains(fileNameContains) == false)
				return false;
		}
		return true;
	}

	private boolean isWildCard(String s) {
		if (s == null)
			return true;
		if (s.contentEquals("*") || s.contentEquals(""))
			return true;
		return false;
	}
	
	public void listShortFileNames() {
		// for debugging only
		for(String s: shortFileNamesList) {
			System.out.println(s);
		}
		
	}

}
