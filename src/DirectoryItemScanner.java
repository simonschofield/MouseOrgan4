import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

//////////////////////////////////////////////////////////////////////////////////////////////
// DirectoryItemScanner 
// abstract DirectoryItemGroup class. 
// deals with collecting the names of a type of item (.png, .svg etc.) within the specified directory
class DirectoryItemScanner {

	String directoryPath;

	ArrayList<String> directoryContentPathAndNamesList = new ArrayList<String>();
	ArrayList<String> directoryContentShortNameList = new ArrayList<String>();

	public DirectoryItemScanner(String targetDirectory) {
		init(targetDirectory);

	}

	void init(String targetDirectory) {
		directoryPath = targetDirectory;
	}

	ArrayList<String> getShortNameList() {
		return directoryContentShortNameList;
	}

	String getShortNameOfItem(int n) {
		return directoryContentShortNameList.get(n);
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
	
	
	void loadImages() {
		loadImages(directoryPath, fileStringEndsWith, fileStringContains);
	}

	void loadImages(String dir, String fileStrEndsWith, String fileStrContains) {
		ArrayList<String> allNames = getFilesInDirectory(dir, fileStrEndsWith, fileStrContains);
		int numImage = allNames.size();
		loadImages(dir, fileStrEndsWith, fileStrContains, 0, numImage - 1, 1 , new Rect());
	}
	
	
	
	
	void loadImages(int fromIndex, int toIndex, float preScale, Rect cropRect) {
		loadImages(directoryPath, fileStringEndsWith, fileStringContains, fromIndex, toIndex, preScale, cropRect);
	}

	void loadImages(String dir, String fileStrEndsWith, String fileStrContains, int fromIndex, int toIndex, float preScale, Rect cropRect) {

		ArrayList<String> allPathAndNames = getFilesInDirectory(dir, fileStrEndsWith, fileStrContains);
		ArrayList<String> allShortNames = getShortFileNamesInDirectory(dir, fileStrEndsWith, fileStrContains);
		System.out.println("load content " + dir + " from to " + fromIndex + "," + toIndex);
		for (int i = fromIndex; i <= toIndex; i++) {
			String pathAndName = allPathAndNames.get(i);
			BufferedImage img = ImageProcessing.loadImage(pathAndName);
			
			
			if(cropRect.equals(new Rect())==false) {
				// crop rect is in parametric form, need to turn this into actual pixel values for this image
				img = ImageProcessing.cropImageWithNormalisedRect(img,cropRect);
			}	
			
			if(preScale < 1) {
				img = ImageProcessing.scaleImage(img, preScale, preScale);
			}
					
					
			widthExtrema.addExtremaCandidate(img.getWidth());
			heightExtrema.addExtremaCandidate(img.getHeight());
			
			directoryContentPathAndNamesList.add(pathAndName);
			String shortName = allShortNames.get(i);
			addImage( img,  shortName);
		}
		System.out.println("loaded " + directoryPath + " width etrema " + widthExtrema.toStr() + " height etrema " + heightExtrema.toStr());
	}
	
	void copyImageFromOtherGroup(String shortName, DirectoryImageGroup otherGroup) {
		// makes a new independent copy
		BufferedImage img = otherGroup.getImage(shortName);
		BufferedImage copyOfImage = ImageProcessing.copyImage(img);
		addImage(copyOfImage, shortName);
	}

	void addImage(BufferedImage img, String shortName) {
		directoryContentShortNameList.add(shortName);
		//System.out.println("added image " + pathAndName + " shortname " + shortName);
		imageList.add(img);
		
		if(directoryContentShortNameList.size() != imageList.size()) {
			System.out.println("DirectoryImageGroup:Image name list and BufferedImage list are out of step!");
		}
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
		for (String thisName : directoryContentShortNameList) {
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
	
	void copyImage(String shortName, DirectoryImageGroup otherGroup) {
		BufferedImage img = otherGroup.getImage(shortName);
		
		if(img!=null) {
			addImage(img,shortName);
		}
	}
	
	
	

}





