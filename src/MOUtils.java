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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// is used to define distribution against an input (e.g. point of distribution against image value)
// The special consideration with distribution interpolations, is what to do beyond the inputMin and inputMax values
// This is dealt with by the underInputMinAction a,d overInputMaxOption settings, so that beyond this the min/max input extents:-
// EXCLUDE -  report that this value is excluded, so no further action may be taken (e.g. do not put a point down at all)
// CLAMP -  clamp the input to that extent, so the returned value is constant beyond that extent (e.g. keep the same distribution as if it was the extent)
// EXTRAPOLATE - keep on interpolating beyond that extent (i.e. so the output values exceed the outputValueAtInputMin/Max values )
//
// getValue() always returns a the packing radius, which is used to calculate possible neighbouring point spacings and their "exclusion zones" by the utilising packing algorithm. 
// The units which are interpolated between (rangeAtControlValMin...rangeAtControlValMax) can be set to either RADIUS, SURFACE_AREA or VOLUME.

// Explanation: If the this class just interpolated a RADIUS against tone,  the the packing would be dis-proportionally spaced against the tone. For instance, if the spacing was to increase with brightness, 
// then small increases in image brightness would result in increasingly large spacings, as the SA (and therefore the packing) of a circle is proportional to the square of its radius. 
// Hence the user may wish the interpolation to be in terms of resultant surface area, or volume of a point's "exclusion zone". Surface_area is the default mode.
//
class PackingInterpolationScheme{
	static final int EXCLUDE = 0; // excluded() returns true is the input is under the inputMin, or over the inputMax, value is clamped at limit.
	static final int CLAMP = 1; // returns false from outsideLimit, value is clamped at limit
	static final int EXTRAPOLATE = 2; // returns false from outsideLimit, value continues to be extrapolated
	static final int RANGE_UNITS_RADIUS = 3; // the output range is regarded as a simple linear interpolation, so left alone
	static final int RANGE_UNITS_SURFACE_AREA = 4; // The output value range is the radius derived from a linear interpolation of surface areas. 
	static final int RANGE_UNITS_VOLUME = 5; // The output value range is the radius derived from a linear interpolation of surface areas. 
	
	float controlValueMin = 0;
	float controlValueMax = 1;
	
	float rangeAtControlValMin = 0;
	float rangeAtControlValMax = 1;
	
	int underControlValueMinOption = EXTRAPOLATE;
	int overControlValueMaxOption = EXTRAPOLATE;
	
	
	// userUnits - RADIUS, SURFACE_AREA, VOLUME
	int rangeUnits = RANGE_UNITS_SURFACE_AREA;
	
	PackingInterpolationScheme(){
	
	}
	
	
	PackingInterpolationScheme(float controlValMin, float controlValMax, float rangeMin, float rangeMax, int underControlValMinOption, int overControlValMaxOption){
		set(  controlValMin,  controlValMax,  rangeMin,  rangeMax,  underControlValMinOption,  overControlValMaxOption);
	}
	
	void setRangeUnits(int m) {
		if(m == RANGE_UNITS_RADIUS) rangeUnits = m;
		if(m == RANGE_UNITS_SURFACE_AREA) rangeUnits = m;
		if(m == RANGE_UNITS_VOLUME) rangeUnits = m;
	}
	
	
	void set(float controlValMin, float controlValMax, float unitRangeMin, float unitRangeMax, int underControlValMinOption, int overControlValMaxOption) {
		
		controlValueMin = controlValMin;
		controlValueMax = controlValMax;
		
		rangeAtControlValMin = unitRangeMin;
		rangeAtControlValMax = unitRangeMax;
		
		
		underControlValueMinOption = underControlValMinOption;
		overControlValueMaxOption = overControlValMaxOption;
		
		
	}
	
	
	
	boolean isExcluded(float controlVal) {
		if(controlVal < controlValueMin && underControlValueMinOption == EXCLUDE) return true;
		if(controlVal > controlValueMax && overControlValueMaxOption == EXCLUDE) return true;
		return false;
	}
	
	// getRadius
	float getValue(float controlVal) {
		
		
		if((underControlValueMinOption == CLAMP || underControlValueMinOption == EXCLUDE) &&  controlVal < controlValueMin) controlVal = controlValueMin;
		if((overControlValueMaxOption == CLAMP  || overControlValueMaxOption == EXCLUDE ) &&  controlVal >  controlValueMax) controlVal = controlValueMax;
		
		float val =  MOMaths.map(controlVal, controlValueMin, controlValueMax, rangeAtControlValMin, rangeAtControlValMax);
		
		if(rangeUnits == RANGE_UNITS_SURFACE_AREA) {
			return (float) Math.sqrt(val/Math.PI);
		}
		
		if(rangeUnits == RANGE_UNITS_VOLUME) {
			return (float) Math.cbrt(val/Math.PI);
		}
		
		return val;
		
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







