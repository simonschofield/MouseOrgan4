package MOUtils;


///////////////////////////////////////////////////////////
//
//
public class UniqueID{
	private boolean[] idList;

	public UniqueID(){
		idList = new boolean[1000000];
	}
	
	UniqueID(int maxNumIDs){
		
		idList = new boolean[maxNumIDs];
	}
	
	
	public int getUniqueID(){
		for(int n = 0; n < idList.length; n++) {
			boolean b = idList[n];
			if(b == false) {
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
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// this to be called if some other process, such as a file-load, takes up some Unique ID's
	// generally, you should do all the grabbing before getting any uniqueID's at run-time
	// otherwise, the ID you are grabbing may have already been generated
	public void grabID(int n){
		boolean b = idList[n];
		if(b==true) {
			System.out.println("UniqueID::grabID - grabbing an already taken ID " + n);
		}
		idList[n] = true;
	}
}