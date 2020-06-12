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
	ArrayList<String> shortContentGroupNameList = new ArrayList<String>();

	public DirectoryItemScanner(String targetDirectory) {
		init(targetDirectory);

	}

	void init(String targetDirectory) {
		directoryPath = targetDirectory;
	}

	ArrayList<String> getShortNameList() {
		return shortContentGroupNameList;
	}

	String getShortNameOfItem(int n) {
		return shortContentGroupNameList.get(n);
	}

	// returns the full path and filename
	ArrayList<String> getFilesInDirectory(String dir, String extension) {
		ArrayList<String> filenames = new ArrayList<String>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fileName = listOfFiles[i].getAbsolutePath();
				if (fileName.endsWith(extension)) {
					filenames.add(fileName);
				}
			} else if (listOfFiles[i].isDirectory()) {
				// ignore
			}
		}
		return filenames;
	}

	// returns the filename only, without extension
	ArrayList<String> getShortFileNamesInDirectory(String dir, String extension) {
		ArrayList<String> filenames = new ArrayList<String>();
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fileNameWithExtension = listOfFiles[i].getName();
				if (fileNameWithExtension.endsWith(extension)) {
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
	}

	// abstract methods
	abstract void loadContent();

	abstract void loadContent(int fromIndex, int toIndex);

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
	
	public DirectoryImageGroup(String targetDirectory) {
		super(targetDirectory);
		
		// TODO Auto-generated constructor stub
	}

	void loadContent(String dir) {
		ArrayList<String> allNames = getFilesInDirectory(dir, ".png");
		int numImage = allNames.size();
		loadContent(0, numImage - 1);
	}

	void loadContent(String dir, int fromIndex, int toIndex) {

		ArrayList<String> allNames = getFilesInDirectory(dir, ".png");
		ArrayList<String> allSortNames = getShortFileNamesInDirectory(dir, ".png");
		System.out.println("load content " + dir);
		for (int i = fromIndex; i <= toIndex; i++) {
			String pathAndName = allNames.get(i);
			BufferedImage img = ImageProcessing.loadImage(pathAndName);
			
			widthExtrema.addExtremaCandidate(img.getWidth());
			heightExtrema.addExtremaCandidate(img.getHeight());
			
			contentGroupNameList.add(pathAndName);
			String shortName = allSortNames.get(i);
			shortContentGroupNameList.add(shortName);
			System.out.println("added image " + pathAndName + " shortname " + shortName);
			imageList.add(img);
		}
		System.out.println("loaded " + directoryPath + " width etrema " + widthExtrema.toStr() + " height etrema " + heightExtrema.toStr());
	}

	void loadContent() {
		loadContent(directoryPath);
	}

	void loadContent(int fromIndex, int toIndex) {
		loadContent(directoryPath, fromIndex, toIndex);
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
		for (String thisName : shortContentGroupNameList) {
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





