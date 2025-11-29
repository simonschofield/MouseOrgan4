package MOUtils;


///////////////////////////////////////////////////////////
// Generates unique integer IDs for items. Zero (0) is reserved, as this will clash with
// the 0 value in ID images.
// If the user needs to reserve any IDs (for instance if a load are taken by a file load), then then can be "grabbed"
// at file load time, but all grabbing must happen BEFORE dynamic IDs are dispatched, otherwise there will be clashes.
//
public class UniqueID{
	private boolean[] idList;

	public UniqueID(){
		idList = new boolean[1000000];
		grabID(0);
	}

	UniqueID(int maxNumIDs){
		idList = new boolean[maxNumIDs];
		grabID(0);
	}


	public int getUniqueID(){
		for(int n = 0; n < idList.length; n++) {
			boolean b = idList[n];
			if(!b) {
				idList[n]=true;
				return n;
			}
		}
		System.out.println("UniqueID::getNextUniqueID - all the IDs have been taken");
		return 0;
	}

	public void reset(){
		for(int n = 0; n < idList.length; n++) {
			boolean b = idList[n] = false;
		}
		grabID(0);
	}


	public boolean isGrabbed(int i) {
		return  idList[i];
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// this to be called if some other process, such as a file-load, takes up some Unique ID's
	// generally, you should do all the grabbing from file before getting any uniqueID's at run-time
	// otherwise, the ID you are grabbing from file may have already been assigned
	public void grabID(int n){
		boolean b = idList[n];
		if(b) {
			System.out.println("UniqueID::grabID - grabbing an already taken ID " + n);
		}
		idList[n] = true;
	}
}