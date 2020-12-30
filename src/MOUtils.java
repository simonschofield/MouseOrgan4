import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class MOUtils {


	
	
	static String getDateStamp() {
		String datestamp = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
		return datestamp;
	}
	
	
	static String getDateStampedImageFileName(String baseName) {
		String dateStamp = getDateStamp();
		String wholeFileName = baseName + "_" + dateStamp + ".png";
		return wholeFileName;
	}
	
	static String getFileExtension(String filename) {
		// filename can be with or without path
		File file = new File(filename);
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}
	
	static boolean checkDirectoryExist(String foldername) {
		File targetFolder = new File(foldername);
		if (targetFolder.exists())
			return true;
		return false;
	}
	
	static boolean createDirectory(String foldername) {
		File targetFolder = new File(foldername);
		if (targetFolder.exists()) return true;
		return targetFolder.mkdirs();
	}
	
	static String getPaddedNumberString(Integer num, int lengthOfString) {
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
	
	static String addCommaSeparatedString(String exisitingString, String toAdd) {
		if(exisitingString.contentEquals("")) {
			exisitingString += toAdd;
			return exisitingString;
		}
		exisitingString += ",";
		exisitingString += toAdd;
		return exisitingString;
	}
	
	
	
	static ArrayList<String> readTextFile(String pathAndName) {
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
	
	
	static Color[] getBasic12ColorPalette() {
		Color[] cols = new Color[12];
		int n = 0;
		cols[n++] = Color.BLACK;
		cols[n++] = Color.RED;
		cols[n++] = Color.GREEN;
		cols[n++] = Color.CYAN;
		cols[n++] = Color.DARK_GRAY;
		cols[n++] = Color.MAGENTA;
		cols[n++] = Color.YELLOW;
		cols[n++] = Color.BLUE;
		cols[n++] = Color.LIGHT_GRAY;
		cols[n++] = Color.ORANGE;
		cols[n++] = Color.GRAY;
		cols[n++] = Color.PINK;
		return cols;
	}
}

class SecondsTimer{
	  
	  long startMillis = 0;
	  float elapsedTime;
	  
	  float durationEndTime = 0;
	  
	  public SecondsTimer(){
	    start();
	  }
	  
	  
	  void start(){
	    startMillis = System.currentTimeMillis();
	    elapsedTime = 0;
	  }
	  
	  
	  void startDuration(float dur){
		durationEndTime = getTimeSinceStart() + dur;
	  }
		  
	  boolean isInDuration(){
	    if( getTimeSinceStart() > durationEndTime) return false;
	    return true;
	  }
		  
		  
	  // returns the elapsed time since you last called this function
	  float getElapsedTime(){
	    float tot = getTimeSinceStart();
	    elapsedTime = tot - elapsedTime;
	    return elapsedTime;
	    
	  }
	  
	  float getTimeSinceStart(){
	    return (System.currentTimeMillis() - startMillis)/1000.0f;
	  }
	  
	  void printTimeSinceStart(String message) {
		  System.out.println("Timer: " + message + getTimeSinceStart());
	  }
	  
	  
}

class GenericArrayListUtils{
	
	
	public static <T> ArrayList<T> trimList(ArrayList<T> listIn, Integer from, Integer to){
		ArrayList<T> trimmedList = new ArrayList<T>();
		if(from == null) from = 0;
		if(to == null) to = listIn.size()-1;
		if(to > listIn.size()-1) to = listIn.size()-1;
		
		for(int n = from; n <= to; n++) {
			T t = listIn.get(n);
			trimmedList.add(t);
		}
		
		return trimmedList;
	}
	
	public static <T> boolean listsContentsAreEqual(ArrayList<T> listA, ArrayList<T> listB) {
		if(listA.size() != listB.size()) return false;
		for(int n = 0; n < listB.size(); n++) {
			T sA = listA.get(n);
			T sB = listB.get(n);
			if( sA.equals(sB)==false ) return false;
		}
		return true;
	}
	
}


class KeepAwake{
	Robot hal;
	SecondsTimer timer;
	int mouseMoveDirection = 1;
	boolean isActive = true;
    KeepAwake(){
        timer = new SecondsTimer();
    	try {
			hal = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

            timer.startDuration(60f);
        }
        
    void update() {
    	if(!isActive) return;
    	if(timer.isInDuration()) return;
    	Point pi = MouseInfo.getPointerInfo().getLocation();
    	hal.mouseMove(pi.x+mouseMoveDirection,pi.y);
    	mouseMoveDirection *= -1;
    	timer.startDuration(60f);
    	pi = MouseInfo.getPointerInfo().getLocation();
    	//System.out.println("KeepAwake mouse move x = " + pi.x);
    }
    
    void setActive(boolean a) {
    	isActive = a;
    }
    
}

///
//

/////////////////////////////////////////////////////////////////////////////////
// PeriodicAction class, used for limiting actions like print statements
// to either the first few, or every n times
class PeriodicAction {
	int counter;
	int period = 1;

	void setPeriod(int n) {
		period = n;
	}

	boolean tryDoPeriodic() {

		if (counter >= period) {
			counter = 0;
			return true;
		}
		counter++;
		return false;
	}

	boolean tryDoUpto() {
		if (counter <= period) {
			counter = 0;
			return true;
		}
		counter++;
		return false;
	}
}
