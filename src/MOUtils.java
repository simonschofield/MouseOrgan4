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
	
	static String getFileNameWithoutExtension(String filename) {
		// filename can be with or without path
		//File file = new File(filename);
	    //String name = file.getName();
	    int lastIndexOf = filename.lastIndexOf(".");
	    if (lastIndexOf == -1) {
	        return filename; // no extension anyway
	    }
	    return filename.substring(0,lastIndexOf);
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
	
	static String createDirectory(String path, String name, boolean timeStamped) {
		String directory = path + name;
		if(timeStamped) {
			directory = directory + "_" + getDateStamp();
		}
		boolean result = createDirectory(directory);
		System.out.println("created directory " + directory + "  " + result);
		return directory;
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
	  float lastNow;
	  
	  float durationEndTime = 0;
	  
	  // used for calculating average interval times
	  // for calls to getElapsedTime() - generally used
	  // in profiling
	  int counter = 0;
	  
	  public SecondsTimer(){
	    start();
	  }
	  
	  
	  void start(){
	    startMillis = System.currentTimeMillis();
	    lastNow = 0;
	    counter = 0;
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
		 float now =  getTimeSinceStart();
		 float elapsedTime = now - lastNow;
		 lastNow = now;
		 counter++;
		 return elapsedTime;
	    
	  }
	  
	  // returns the average elapsed time, based on cumulative elapsed times
	  // so, gets more accurate the longer it runs
	  float getAverageElapsedTime() {
		  getElapsedTime();
		  float now =  getTimeSinceStart();
		  return (now/counter);
		  
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

/////////////////////////////////////////////////////////////////////////////
//
//

class Counter{
	int num = 0;
	int maxNum = Integer.MAX_VALUE;
	
	Counter(){
		num = 0;
	}
	
	void setLoop(int loopLength) {
		maxNum = loopLength;
	}
	
	int next(){
		int thisNum = num;
		num++;
		if(num >= maxNum) num = 0;
		return thisNum;
	}
	
}




///////////////////////////////////////////////////////////
//
//
class UniqueID{
	int idNumCounter = 0;


	int getUniqueID(){
		return idNumCounter++;
	}

	void reset(){
		idNumCounter=0;
	}

	void setMinNewID(int n){
		if( n > idNumCounter){
			idNumCounter = n + 1;
		}
	}
}

/////////////////////////////////////////////////////////////////////////////
//
//Used for debugging
//
class Histogram {
	int[] data;
	float numBands;
	float anticipatedLoVal, anticipatedHiVal;
	float actualLoVal = Float.MAX_VALUE;
	float actualHiVal = -Float.MAX_VALUE;

	public Histogram(int numBands, float lo, float hi) {
		this.data = new int[numBands];
		this.numBands = numBands;
		this.anticipatedLoVal = lo;
		this.anticipatedHiVal = hi;

	}

	void add(float val) {
		updateActualHiLoVals(val);
		int index = (int) (numBands * MOMaths.norm(val, this.anticipatedLoVal, this.anticipatedHiVal));
		this.data[index] += 1;
	}

	void updateActualHiLoVals(float val) {
		if (val < this.actualLoVal)
			this.actualLoVal = val;
		if (val > this.actualHiVal)
			this.actualHiVal = val;

	}

	float getActualHiVal() {
		return this.actualHiVal;
	}

	float getActualLoVal() {
		return this.actualLoVal;
	}

	void printReport() {
		System.out.println("Histogram: anticipated lo hi :" +  this.anticipatedLoVal + " " + this.anticipatedHiVal);
		System.out.println("Histogram: actual lo hi      :" + this.actualLoVal + " " +  this.actualHiVal);
		System.out.println("distribution of data as follows using anticipated range:");
		for (int n = 0; n < this.numBands; n++) {
			String rs = rangeString(n);
			Integer population = this.data[n];
			System.out.println(rs + ": population: " + population);
		}
	}

	String rangeString(int n){
		float plo = MOMaths.norm(n, 0, this.numBands);
		float phi = MOMaths.norm(n+1, 0, this.numBands);
		Float rangeLo = MOMaths.lerp( plo, this.anticipatedLoVal, this.anticipatedHiVal);
		Float rangeHi = MOMaths.lerp( phi, this.anticipatedLoVal, this.anticipatedHiVal);
		return "Range: " + rangeLo.toString() + "," + rangeHi.toString();
		
		
	
	}

}// end Histogram class







