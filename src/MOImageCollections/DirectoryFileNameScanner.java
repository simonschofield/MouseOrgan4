package MOImageCollections;
import java.io.File;
import java.util.ArrayList;

import MOMaths.MOMaths;
import MOUtils.MOStringUtils;



//////////////////////////////////////////////////////////////////////////////////////////////
//DirectoryFileNameScanner =  improved version of  DirectoryItemScanner
//abstract DirectoryItemGroup class. 
//deals with collecting the names of a type of item (.png, .svg etc.) within the specified directory
public class DirectoryFileNameScanner  {

	private String directoryPath = "";

	// filters
	private String fileType = "";
	private String fileNameContains = "";
	private Integer cropFileNameListLow = 0;
	private Integer cropFileNameListHigh = null;

	private ArrayList<String> fullPathAndFileNamesList = new ArrayList<String>();
	

	
	
	public DirectoryFileNameScanner(String targetDirectory) {
		setTargetDirectory( targetDirectory);
	}
	
	
	public DirectoryFileNameScanner(String targetDirectory, String fileTp) {
		setTargetDirectory( targetDirectory);
		setFileType(fileTp);
	}
	
	public void setTargetDirectory(String targetDirectory) {
		directoryPath = targetDirectory;
		cacheFileNames();
	}
	
	public void setFileType(String fileTp) {
		fileType = fileTp;
		cacheFileNames();
	}

	public void setFileNameContains(String fileStrContains) {
		fileNameContains = fileStrContains;
		cacheFileNames();
	}
	
	public void keep(String[] keepTheseShortFileNames) {
		if(keepTheseShortFileNames == null) return;
		
		ArrayList<String> shortNameList = getShortNameList();
		ArrayList<String> keptFullPathAndFileNamesList  = new ArrayList<String>();
		
		for(int n = 0; n < shortNameList.size(); n++) {
			String thisShortName = shortNameList.get(n);
			if(MOStringUtils.stringListContains(keepTheseShortFileNames, thisShortName)) {
				String keepFullPathAndFileName = fullPathAndFileNamesList.get(n);
				keptFullPathAndFileNamesList.add(keepFullPathAndFileName);
			}
		}
		fullPathAndFileNamesList = keptFullPathAndFileNamesList;
	}
	
	public void remove(String[] removeTheseShortFileNames) {
		if(removeTheseShortFileNames == null) return;
		
		ArrayList<String> shortNameList = getShortNameList();
		ArrayList<String> keptFullPathAndFileNamesList  = new ArrayList<String>();
		
		for(int n = 0; n < shortNameList.size(); n++) {
			String thisShortName = shortNameList.get(n);
			if(MOStringUtils.stringListContains(removeTheseShortFileNames, thisShortName)==false) {
				String keepFullPathAndFileName = fullPathAndFileNamesList.get(n);
				keptFullPathAndFileNamesList.add(keepFullPathAndFileName);
			}
		}
		fullPathAndFileNamesList = keptFullPathAndFileNamesList;
		
		
		
	}
	
	///////////////////////////////////////////////////////////////////////////
	// is applied to those already found by filters set so far
	public void setFileListRange(Integer lo, Integer hi) {
		cropFileNameListLow = lo;
		cropFileNameListHigh = hi;
		cacheFileNames();
	}
	
	
	

	public String getTargetDirectory() {
		return directoryPath;
	}
	
	

	public ArrayList<String> getShortNameList() {
		ArrayList<String> shortFileNamesList = new ArrayList<String>();
		for(String fullPathAndFileName: fullPathAndFileNamesList) {
			
			File f = new File(fullPathAndFileName); 
			String shortFileNameWithExtension = f.getName();
			int strlen = shortFileNameWithExtension.length();
			String fileNameOnly = shortFileNameWithExtension.substring(0, strlen - 4);
			shortFileNamesList.add(fileNameOnly);
			
		}
		return shortFileNamesList;
	}
	
	

	
	
	public void copyConstraints(DirectoryFileNameScanner otherScanner) {
		this.fileType = otherScanner.fileType;
		this.fileNameContains = otherScanner.fileNameContains;
		this.cropFileNameListLow = otherScanner.cropFileNameListLow;
		this.cropFileNameListHigh = otherScanner.cropFileNameListHigh;
		cacheFileNames();
	}

	public ArrayList<String> getFullPathAndFileNamesList() {
		return fullPathAndFileNamesList;
	}

	public int getNumFiles() {
		return fullPathAndFileNamesList.size();
	}
	
	boolean isFileNamesFound() {
		if( fullPathAndFileNamesList.size()==0 ) {
			System.out.println("DirectoryFileNameScanner:: no files matching criteria in directory " + directoryPath );
			return false;
		}
		return true;
	}
	
	
	
	
	
	

	////////////////////////////////////////////////////////////////////////////
	// Private methods
	
	
	
	private void cacheFileNames() {
		collectFilesNamesWithFilters();
		cropFileList();
	}
	
	
	private void cropFileList() {
		int numExistingFiles = getNumFiles();
		Integer low = cropFileNameListLow;
		Integer high = cropFileNameListHigh;
		
		if(low == null) {
			low = 0;
		}
		
		if(high == null) {
			high = numExistingFiles;
		} 
		
		low = MOMaths.constrain(low, 0, numExistingFiles-1);
		high = MOMaths.constrain(high, 1, numExistingFiles);

		ArrayList<String> tempFullPathAndFileNamesList = new ArrayList<String>();
		//ArrayList<String> tempShortFileNamesList = new ArrayList<String>();

		for(int n = low; n < high; n++) {
			String thisFullPathAndName = fullPathAndFileNamesList.get(n);
			//String thisShortFileName = shortFileNamesList.get(n);
			tempFullPathAndFileNamesList.add(thisFullPathAndName);
			//tempShortFileNamesList.add(thisShortFileName);
		}
		fullPathAndFileNamesList = tempFullPathAndFileNamesList;
		//shortFileNamesList = tempShortFileNamesList;
		
		
		// will generate a warning if no files are resultant
		isFileNamesFound();
	}
	
	
	

	private void collectFilesNamesWithFilters() {
		if(directoryPath.equals("")) {
			System.out.println("DirectoryFileNameScanner:: target directory is not defined");
			return;
		}
		
		fullPathAndFileNamesList = new ArrayList<String>();
		//shortFileNamesList = new ArrayList<String>();
		//System.out.println("cacheFileNames: dir path "+ directoryPath);
		File folder = new File(directoryPath);
		
		if( folder.exists() == false ) {
			System.out.println("DirectoryFileNameScanner:: target directory does not exists" + directoryPath );
			return;
		}
		
		
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fullPathAndName = listOfFiles[i].getAbsolutePath();
				String shortFileNameWithExtension = listOfFiles[i].getName();
				//if (fileName.endsWith(fileEndsWith) || isWildCard(fileEndsWith) ) {
				if (fileNameMeetsCriteria(shortFileNameWithExtension)) {
					fullPathAndFileNamesList.add(fullPathAndName);
				}
			} else if (listOfFiles[i].isDirectory()) {
				// ignore
			}
		}
		
		// will generate a warning if no files are found
		isFileNamesFound();
		
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
	
	public void printToConsoleShortFileNames() {
		// for debugging only
		ArrayList<String> shortFileNamesList = this.getShortNameList();
		for(String s: shortFileNamesList) {
			System.out.print(s + ", ");
		}
		System.out.println();
	}
	

}
