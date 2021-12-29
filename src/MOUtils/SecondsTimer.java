package MOUtils;

public class SecondsTimer{
	  
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
	  
	  
	  public void start(){
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
	  public float getElapsedTime(){
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
	  
	  public float getTimeSinceStart(){
	    return (System.currentTimeMillis() - startMillis)/1000.0f;
	  }
	  
	  public void printTimeSinceStart(String message) {
		  System.out.println("Timer: " + message + getTimeSinceStart());
	  }
	  
	  
}