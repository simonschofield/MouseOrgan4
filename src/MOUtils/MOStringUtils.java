package MOUtils;

import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;


import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class MOStringUtils {


	
	
	public static String getDateStamp() {
		String datestamp = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
		return datestamp;
	}
	
	
	public static String getDateStampedImageFileName(String baseName) {
		String dateStamp = getDateStamp();
		String wholeFileName = baseName + "_" + dateStamp + ".png";
		return wholeFileName;
	}
	
	public static String getFileExtension(String filename) {
		// filename can be with or without path
		File file = new File(filename);
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}
	
	public static String getFileNameWithoutExtension(String filename) {
		// filename can be with or without path
		//File file = new File(filename);
	    //String name = file.getName();
	    int lastIndexOf = filename.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return filename; // no extension anyway
	    }
	    return filename.substring(0,lastIndexOf);
	}
	
	public static String getShortFileNameFromFullPathAndFileName(String fullPathAndFileName) {
		// returns the file name without path or extension
		File f = new File(fullPathAndFileName); 
		String shortFileNameWithExtension = f.getName();
		int strlen = shortFileNameWithExtension.length();
		String fileNameOnly = shortFileNameWithExtension.substring(0, strlen - 4);
		return fileNameOnly;
	}
	
	public static boolean checkDirectoryExist(String foldername) {
		File targetFolder = new File(foldername);
		if (targetFolder.exists())
			return true;
		return false;
	}
	
	public static boolean createDirectory(String foldername) {
		File targetFolder = new File(foldername);
		if (targetFolder.exists()) return true;
		return targetFolder.mkdirs();
	}
	
	public static String createDirectory(String path, String name, boolean timeStamped) {
		String directory = path + name;
		if(timeStamped) {
			directory = directory + "_" + getDateStamp();
		}
		boolean result = createDirectory(directory);
		System.out.println("created directory " + directory + "  " + result);
		return directory;
	}
	
	public static String getPaddedNumberString(Integer num, int lengthOfString) {
		String  inputString =  num.toString();
	    if (inputString.length() >= lengthOfString) {
	        return inputString;
	    }
	    StringBuilder sb = new StringBuilder();
	    while (sb.length() < lengthOfString - inputString.length()) {
	        sb.append('0');
	    }
	    sb.append(inputString);
	 
	    return sb.toString();
		
		
	}
	
	public static String addCommaSeparatedString(String exisitingString, String toAdd) {
		if(exisitingString.contentEquals("")) {
			exisitingString += toAdd;
			return exisitingString;
		}
		exisitingString += ",";
		exisitingString += toAdd;
		return exisitingString;
	}
	
	
	
	public static ArrayList<String> readTextFile(String pathAndName) {
		ArrayList<String> stListOut = new ArrayList<String>();
		File myObj = new File(pathAndName);
		try{
	      Scanner myReader = new Scanner(myObj);
	      while (myReader.hasNextLine()) {
	        String data = myReader.nextLine();
	        stListOut.add(data);
	        //System.out.println(data);
	      }
	      myReader.close();
		}catch (IOException e) {}
		return stListOut;
	}


	public static void deleteDirectory(String foldername) {
		// TODO Auto-generated method stub
		File targetFolder = new File(foldername);
		if (targetFolder.exists()) targetFolder.delete();
	}
	
	
	
}












