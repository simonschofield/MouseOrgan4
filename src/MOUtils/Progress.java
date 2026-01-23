package MOUtils;

public class Progress {
    int lastpercentdisplayed = 0;
    int count;
    int totalNum;
    public boolean active = true;

    String startMessage;
    boolean startMessagePrinted = false;

    public Progress() {
    	reset();
    }

    public Progress(String startMessage, int totalNum) {
    	reset(totalNum);
    	setStartMessage(startMessage);
    }

    public void setStartMessage(String s) {
    	startMessage = s;

    }

    public void reset() {
    	lastpercentdisplayed = 0;
    	count = 0;
    	startMessagePrinted = false;
    }

    public void reset(int totalN) {
    	reset();
    	totalNum = totalN;
    }

    public void update() {
    	if(!startMessagePrinted) {
			System.out.print( startMessage );
			startMessagePrinted = true;
		}
    	tryprint(count, totalNum);
    	count++;
    }

   private void  tryprint(int thisNum, int outOf) {

		int percent = (int)( (thisNum/(float)outOf) * 100);
		if(percent%10==0 && percent>lastpercentdisplayed) {
			lastpercentdisplayed = percent;
			if(active) {
				System.out.print( percent + "%(" + thisNum + "/" + outOf +  "), ");
			}
		}
	}
}
