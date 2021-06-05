////////////////////////////////////////////////////////////////////////////////
//for session iteration - getting points generated one after another
//
abstract class CollectionIterator {
	int itemIteratorCounter = 0;
	
	boolean justfinishedFlag = false;
	boolean isFinishedFlag = false;
	
	boolean areItemsRemaining() {
		if (getNumItemsRemaining() <= 0)
			return false;
		return true;
	}
	
	
	void reset() {
		itemIteratorCounter = 0;
		justfinishedFlag = false;
		isFinishedFlag = false;
	}
	
	boolean isJustFinished() {
		// returns true just once upon finishing the iteration
		if(areItemsRemaining()) return false;
		if(justfinishedFlag == false) {
			justfinishedFlag = true;
			isFinishedFlag = true;
			return true;
		}
		return false;
	}
	
	boolean isFinished() {
		return isFinishedFlag;
	}

	int getNumItemsRemaining() {
		return getNumItems() - itemIteratorCounter;
	}

	Object getNextItem() {
		if (itemIteratorCounter >= getNumItems()) {
			isFinishedFlag = true;
			return null;
		}
		return getItem(itemIteratorCounter++);
	}
	
	void advanceIterator(int n) {
		setIterator(itemIteratorCounter +  n);
	}
	
	void setIterator(int n) {
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
	
	
	int getIteratorCounter() {
		return itemIteratorCounter;
	}

	void resetItemIterator() {
		itemIteratorCounter = 0;
		justfinishedFlag = false;
		isFinishedFlag = false;
	}
	
	abstract int getNumItems();
	
	abstract Object getItem(int n);

}
