package MOUtils;

/////////////////////////////////////////////////////////////////////////////
//
//Used for debugging
//
public class Histogram {
	int[] data;
	float numBands;
	float anticipatedLoVal, anticipatedHiVal;
	float actualLoVal = Float.MAX_VALUE;
	float actualHiVal = -Float.MAX_VALUE;

	public Histogram(int numBands, float lo, float hi) {
		this.data = new int[numBands];
		this.numBands = numBands;
		this.anticipatedLoVal = lo;
		this.anticipatedHiVal = hi;

	}

	void add(float val) {
		updateActualHiLoVals(val);
		int index = (int) (numBands * MOMaths.norm(val, this.anticipatedLoVal, this.anticipatedHiVal));
		this.data[index] += 1;
	}

	void updateActualHiLoVals(float val) {
		if (val < this.actualLoVal)
			this.actualLoVal = val;
		if (val > this.actualHiVal)
			this.actualHiVal = val;

	}

	float getActualHiVal() {
		return this.actualHiVal;
	}

	float getActualLoVal() {
		return this.actualLoVal;
	}

	void printReport() {
		System.out.println("Histogram: anticipated lo hi :" +  this.anticipatedLoVal + " " + this.anticipatedHiVal);
		System.out.println("Histogram: actual lo hi      :" + this.actualLoVal + " " +  this.actualHiVal);
		System.out.println("distribution of data as follows using anticipated range:");
		for (int n = 0; n < this.numBands; n++) {
			String rs = rangeString(n);
			Integer population = this.data[n];
			System.out.println(rs + ": population: " + population);
		}
	}

	String rangeString(int n){
		float plo = MOMaths.norm(n, 0, this.numBands);
		float phi = MOMaths.norm(n+1, 0, this.numBands);
		Float rangeLo = MOMaths.lerp( plo, this.anticipatedLoVal, this.anticipatedHiVal);
		Float rangeHi = MOMaths.lerp( phi, this.anticipatedLoVal, this.anticipatedHiVal);
		return "Range: " + rangeLo.toString() + "," + rangeHi.toString();
		
		
	
	}

}// end Histogram class




