package MOUtils;
////////////////////////////////////////////////////////////////////////////////
//for session iteration - getting points generated one after another
//
public abstract class CollectionIterator {
	int itemIteratorCounter = 0;
	
	boolean justfinishedFlag = false;
	boolean isFinishedFlag = false;
	
	public boolean areItemsRemaining() {
		if (getNumItemsRemaining() <= 0)
			return false;
		return true;
	}
	
	
	public void reset() {
		itemIteratorCounter = 0;
		justfinishedFlag = false;
		isFinishedFlag = false;
	}
	
	public boolean isJustFinished() {
		// returns true just once upon finishing the iteration
		if(areItemsRemaining()) return false;
		if(justfinishedFlag == false) {
			justfinishedFlag = true;
			isFinishedFlag = true;
			return true;
		}
		return false;
	}
	
	public boolean isFinished() {
		return isFinishedFlag;
	}

	public int getNumItemsRemaining() {
		return getNumItems() - itemIteratorCounter;
	}

	public Object getNextItem() {
		if (itemIteratorCounter >= getNumItems()) {
			isFinishedFlag = true;
			return null;
		}
		return getItem(itemIteratorCounter++);
	}
	
	public void advanceIterator(int n) {
		setIterator(itemIteratorCounter +  n);
	}
	
	public void setIterator(int n) {
		if(n < 0) { 
				itemIteratorCounter = 0;
				return;
		}
		if(n > getNumItems() ) {
			itemIteratorCounter = getNumItems();
			return;
		}
		itemIteratorCounter = n;
		if(areItemsRemaining()) justfinishedFlag = false;
	}
	
	
	public int getIteratorCounter() {
		return itemIteratorCounter;
	}

	public void resetItemIterator() {
		itemIteratorCounter = 0;
		justfinishedFlag = false;
		isFinishedFlag = false;
	}
	
	public abstract int getNumItems();
	
	public abstract Object getItem(int n);

}
