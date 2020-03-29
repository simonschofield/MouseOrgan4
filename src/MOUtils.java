import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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




class KeepAwake{
	Robot hal;
	SecondsTimer timer;
	int mouseMoveDirection = 1;
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
    	if(timer.isInDuration()) return;
    	Point pi = MouseInfo.getPointerInfo().getLocation();
    	hal.mouseMove(pi.x+mouseMoveDirection,pi.y);
    	mouseMoveDirection *= -1;
    	timer.startDuration(60f);
    	pi = MouseInfo.getPointerInfo().getLocation();
    	//System.out.println("KeepAwake mouse move x = " + pi.x);
    }
    
}
