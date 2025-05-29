package MOZZZ_Depricated;

/////////////////////////////////////////////////////////////////////////////////
//PeriodicAction class, used for limiting actions like print statements
//to either the first few, or every n times
public class PeriodicAction {
	int counter;
	int period = 1;

	public void setPeriod(int n) {
		period = n;
	}

	public boolean tryDoPeriodic() {

		if (counter >= period) {
			counter = 0;
			return true;
		}
		counter++;
		return false;
	}

	public boolean tryDoUpto() {
		if (counter <= period) {
			counter = 0;
			return true;
		}
		counter++;
		return false;
	}
}





