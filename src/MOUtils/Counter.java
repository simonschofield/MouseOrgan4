package MOUtils;

/////////////////////////////////////////////////////////////////////////////
//
//

public class Counter{
	int num = 0;
	int maxNum = Integer.MAX_VALUE;

	public Counter(){
		num = 0;
	}

	public void setLoop(int loopLength) {
		maxNum = loopLength;
	}

	public int next(){
		int thisNum = num;
		num++;
		if(num >= maxNum) num = 0;
		return thisNum;
	}

}

