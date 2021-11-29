package MOUtils;


///////////////////////////////////////////////////////////
//
//
public class UniqueID{
	int idNumCounter = 0;


	public int getUniqueID(){
		return idNumCounter++;
	}

	public void reset(){
		idNumCounter=0;
	}

	public void setMinNewID(int n){
		if( n > idNumCounter){
			idNumCounter = n + 1;
		}
	}
}
