import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

//////////////////////////////////////////////////////////////////////////////////////////////
// DirectoryItemScanner 
// abstract DirectoryItemGroup class. 
// deals with collecting the names of a type of item (.png, .svg etc.) within the specified directory
abstract class DirectoryItemScanner {

	String directoryPath;

	ArrayList<String> contentGroupNameList = new ArrayList<String>();
	ArrayList<String> contentGroupShortNameList = new ArrayList<String>();

	public DirectoryItemScanner(String targetDirectory) {
		init(targetDirectory);

	}

	void init(String targetDirectory) {
		directoryPath = targetDirectory;
	}

	ArrayList<String> getShortNameList() {
		return contentGroupShortNameList;
	}

	String getShortNameOfItem(int n) {
		return contentGroupShortNameList.get(n);
	}
	
    int getNumFileTypeInTargetDirectory(String fileStrEndsWith,  String fileStrContains) {
		return getFilesInDirectory(directoryPath, fileStrEndsWith, fileStrContains).size();
	}

    
	// returns the full path and filename
	ArrayList<String> getFilesInDirectory(String dir, String fileStrEndsWith,  String fileStrContains) {
		ArrayList<String> filenames = new ArrayList<String>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fileName = listOfFiles[i].getAbsolutePath();
				//if (fileName.endsWith(fileEndsWith) || isWildCard(fileEndsWith) ) {
				if ( fileNameMeetsCriteria(fileName, fileStrEndsWith, fileStrContains) ) {
					filenames.add(fileName);
				}
			} else if (listOfFiles[i].isDirectory()) {
				// ignore
			}
		}
		return filenames;
	}
	
	
	
	

	// returns the filename only, without extension
	ArrayList<String> getShortFileNamesInDirectory(String dir, String fileStrEndsWith,  String fileStrContains) {
		ArrayList<String> filenames = new ArrayList<String>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fileNameWithExtension = listOfFiles[i].getName();
				//if (fileNameWithExtension.endsWith(fileEndsWith) || isWildCard(fileEndsWith) ) {
				if ( fileNameMeetsCriteria(fileNameWithExtension, fileStrEndsWith, fileStrContains) ) {
					int strlen = fileNameWithExtension.length();
					String fileNameOnly = fileNameWithExtension.substring(0, strlen - 4);
					filenames.add(fileNameOnly);
				}
			} else if (listOfFiles[i].isDirectory()) {
				// ignore
			}
		}
		return filenames;
	}
	
	
	
	
	
	boolean fileNameMeetsCriteria(String fileName, String mustEndWith, String mustContain) {
		
		// test ending
		if( isWildCard(mustEndWith) == false ) {
			if(  fileName.endsWith(mustEndWith) == false  )
			  return false;
			}
		// test containment
		if( isWildCard(mustContain) == false ) {
			if(  fileName.contains(mustContain) == false  )
			  return false;
			}
		return true;
	}
	
	
	boolean isWildCard(String s) {
		if(s == null) return true;
		if(s.contentEquals("*") || s.contentEquals("")) return true;
		return false;
	}
	
	/*
	boolean checkDirectoryExist(String foldername) {

		File targetFolder = new File(foldername);
		if (targetFolder.exists())
			return true;
		return false;

	}
	
	boolean createDirectory(String foldername) {
		File targetFolder = new File(foldername);
		if (targetFolder.exists()) return true;
		return targetFolder.mkdirs();
	}*/
	
	
	

	// abstract methods
	abstract void loadContent();

	abstract int getNumItems();

}

//////////////////////////////////////////////////////////////////////////////////////////////
//
// ImageGroup
// A Simple list of images held in memory
class DirectoryImageGroup extends DirectoryItemScanner {

	ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();
	
	Range widthExtrema = new Range();
	Range heightExtrema = new Range();
	
	// filters
	String fileStringEndsWith = "";
	String fileStringContains = "";
	
	public DirectoryImageGroup(String targetDirectory, String fileStrEndsWith, String fileStrContains) {
		super(targetDirectory);
		fileStringEndsWith = fileStrEndsWith;
		fileStringContains = fileStrContains;
		// TODO Auto-generated constructor stub
	}
	
	void loadContent() {
		loadContent(directoryPath, fileStringEndsWith, fileStringContains);
	}

	void loadContent(String dir, String fileStrEndsWith, String fileStrContains) {
		ArrayList<String> allNames = getFilesInDirectory(dir, fileStrEndsWith, fileStrContains);
		int numImage = allNames.size();
		loadContent(dir, fileStrEndsWith, fileStrContains, 0, numImage - 1, 1 , new Rect());
	}
	
	
	
	
	void loadContent(int fromIndex, int toIndex, float preScale, Rect cropRect) {
		loadContent(directoryPath, fileStringEndsWith, fileStringContains, fromIndex, toIndex, preScale, cropRect);
	}

	void loadContent(String dir, String fileStrEndsWith, String fileStrContains, int fromIndex, int toIndex, float preScale, Rect cropRect) {

		ArrayList<String> allNames = getFilesInDirectory(dir, fileStrEndsWith, fileStrContains);
		ArrayList<String> allSortNames = getShortFileNamesInDirectory(dir, fileStrEndsWith, fileStrContains);
		System.out.println("load content " + dir + " from to " + fromIndex + "," + toIndex);
		for (int i = fromIndex; i <= toIndex; i++) {
			String pathAndName = allNames.get(i);
			BufferedImage img = ImageProcessing.loadImage(pathAndName);
			
			
			if(cropRect.equals(new Rect())==false) {
				// crop rect is in parametric form, need to turn this into actual pixel values for this image
				img = ImageProcessing.cropImageWithParametricRect(img,cropRect);
			}	
			
			if(preScale < 1) {
				img = ImageProcessing.scaleImage(img, preScale, preScale);
			}
					
					
			widthExtrema.addExtremaCandidate(img.getWidth());
			heightExtrema.addExtremaCandidate(img.getHeight());
			
			contentGroupNameList.add(pathAndName);
			String shortName = allSortNames.get(i);
			contentGroupShortNameList.add(shortName);
			System.out.println("added image " + pathAndName + " shortname " + shortName);
			imageList.add(img);
		}
		System.out.println("loaded " + directoryPath + " width etrema " + widthExtrema.toStr() + " height etrema " + heightExtrema.toStr());
	}

	

	// specific to this class
	BufferedImage getImage(int n) {
		if (getNumItems() == 0) {
			System.out.println("getImage:: ImageGroup has no images ");
			return null;
		}
		return imageList.get(n);
	}

	BufferedImage getImage(String shortName) {
		int n = 0;
		for (String thisName : contentGroupShortNameList) {
			if (thisName.contentEquals(shortName))
				return imageList.get(n);
			n++;
		}
		System.out.println("ImageGroup:getImage - cannot find image called " + shortName);
		return null;
	}

	int getNumItems() {
		return imageList.size();
	}
	
	void setImage(BufferedImage img, int n) {
		if (n < 0 || n >= getNumItems() ) {
			System.out.println("setImage:: out of range ");
			return;
		}
		imageList.set(n,img);
	}
	
	
	

}





