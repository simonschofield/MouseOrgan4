package MOUtils;

public class Progress {
    static int lastpercentdisplayed = 0;

    public static void reset() {
    	lastpercentdisplayed = 0;
    }
    
    public static void  print(int thisNum, int outOf) {
    	
		int percent = (int)( (thisNum/(float)outOf) * 100);
		if(percent%10==0 && percent>lastpercentdisplayed) {
			lastpercentdisplayed = percent;
			System.out.print( percent + "% (" + thisNum + " out of " + outOf +  "), " );
		}
	}
}
