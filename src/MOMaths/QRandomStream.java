package MOMaths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/////////////////////////////////////////////////////////////////////////////////////
//QRandomStream class provides a computationally fast way of generating random numbers
//where the authenticity of the random number sequence is not paramount
//All instances of QRandomStream share the same list of (1  million) random integers (0... listSize-1)
//but each different seed uses a different visitation-order to the list, which still guarantees every one is visited,
//so using a different seed produced a completely different order.
//
public  class QRandomStream {
	
	private static ArrayList<Integer> visitationOrder;
	private static ArrayList<Integer> randomNumbers;
	private int arraySize = 1000000;
	private int seed = 1;

	private int sequencePosition = 0;

	public QRandomStream(int rseed) {
		init(rseed,0);
	}


	private void init(int rseed, int streamPosition) {
		if (randomNumbers == null) {
			initRandomNumbers();
		}
		setSeedAndPosition(rseed, streamPosition);
	}
	
	public QRandomStream copy() {
		// returns a completely independent copy, but exactly the same stream sequence and current position
		QRandomStream newCopy = new QRandomStream(seed);
		newCopy.sequencePosition = sequencePosition;
		return newCopy;
	}
	
	///////////////////////////////////////////////
	// seed and sequencePosition methods
	void setSeedAndPosition(Integer s, Integer p) {
		if(s != null) {
			seed = s;
		}
		if(p != null) {
			sequencePosition = p%getMaxSequencePosition();
		}
	}
	
	///////////////////////////////////////////////
	// call this before any random number call to guarantee
	// the same result for that i num.
	// The same input i will always generate the same random number sequence
	// If you want different results, then re-seed the stream
	public void setState(int i) {
		setSeedAndPosition(null, i);
	}
	
	
	int getSeed() {
		return seed;
	}
	
	
	int getPosition() {
		return sequencePosition;
	}
	
	int getMaxSequencePosition() {
		return arraySize-1;
	}

	
	///////////////////////////////////////////////
	// random number generating methods 
	// each call advances the sequencePosition by at least 1 place
	
	public int nextInt() {
		// all random number generating methods call this method
		// hence the sequenceIncrement is here
		int idexOfRandomNumber = (visitationOrder.get(sequencePosition) + seed)%arraySize;
		sequencePosition++;
		if(sequencePosition >= getMaxSequencePosition()) sequencePosition = 0;
		return randomNumbers.get(idexOfRandomNumber);
	}
	
	float nextFloat() {
		float f = nextInt()/(float)arraySize;
		return f;
	}
	
	
	public boolean randomEvent(float prob) {
		float r = nextFloat();
		if (r < prob)
			return true;
		return false;
	}

	public int randRangeInt(int lo, int hi) {
		float r = nextFloat();
		return (int) ((lo + r * (hi - lo)) + 0.5f);
	}

	public float randRangeGaussian(float lo, float hi) {
		// apparently should be 20 samples for a true Gaussian
		return randRangeAveragedDistribution( lo,  hi, 20); 
	}

	float randRangeAveragedDistribution(float lo, float hi, int numSamples) {
		/* using 4 average-samples this is the population distribution between 0..1
		 * (total population 1000)
		Range: 0.0,0.1: population: 3
		Range: 0.1,0.2: population: 15
		Range: 0.2,0.3: population: 76
		Range: 0.3,0.4: population: 160
		Range: 0.4,0.5: population: 230
		Range: 0.5,0.6: population: 259
		Range: 0.6,0.7: population: 167
		Range: 0.7,0.8: population: 65
		Range: 0.8,0.9: population: 23
		Range: 0.9,1.0: population: 2
		 */
		float sum = 0;
		for (int n = 0; n < numSamples; n++)
			{
			sum += randRangeF(lo,hi);
			}
		return sum / numSamples;
		
	}
	
	float skewedNormalisedRange(float lo, float cen, float hi) {
		/* lo = 0, cen = 0.25, hi = 1, population 1000 
		 * 	Range: 0.0,0.05: population: 3
			Range: 0.05,0.1: population: 15
			Range: 0.1,0.15: population: 76
			Range: 0.15,0.2: population: 160
			Range: 0.2,0.25: population: 230
			Range: 0.25,0.3: population: 92
			Range: 0.3,0.35: population: 76
			Range: 0.35,0.4: population: 91
			Range: 0.4,0.45: population: 56
			Range: 0.45,0.5: population: 53
			Range: 0.5,0.55: population: 58
			Range: 0.55,0.6: population: 27
			Range: 0.6,0.65: population: 24
			Range: 0.65,0.7: population: 14
			Range: 0.7,0.75: population: 12
			Range: 0.75,0.8: population: 4
			Range: 0.8,0.85: population: 7
			Range: 0.85,0.9: population: 2
			Range: 0.9,0.95: population: 0
			Range: 0.95,1.0: population: 0
		 */
		float n = randRangeAveragedDistribution(-1, 1, 4);
		if(n < 0) return MOMaths.map(n, -1, 0, lo, cen);
		if(n > 0) return MOMaths.map(n, 0, 1, cen, hi);
		// should never get here...
		return cen;
	}

	public float randRangeF(float lo, float hi) {
		float r = nextFloat();
		return (lo + r * (hi - lo));
	}

	public float perturb(float v, float amt) {
		return v + (randRangeF(-v, v) * amt);
	}


	float nextGaussian()
	{
		// Box Muller algorithm
	    float u, v, S;

	    do
	    {
	        u = 2.0f * nextFloat()  - 1.0f;
	        v = 2.0f * nextFloat()  - 1.0f;
	        S = u * u + v * v;
	    }
	    while (S >= 1.0);

	    float fac = (float) Math.sqrt(-2.0 * Math.log(S) / S);
	    return u * fac;
	}
	
	//////////////////////////////////////////////////////////////
	// Generates an random sequence independent SNum from this stream
	//
	public SNum snum(int seedOffset, Integer sequencePosition) {
		return new SNum(this, seedOffset, sequencePosition);
	}
	
	
	/// private stuff, don't call
	private void initRandomNumbers() {
		randomNumbers = new ArrayList<Integer>();
		visitationOrder = new ArrayList<Integer>();
		for(int i = 0; i < arraySize; i++) {
			randomNumbers.add(i);
			visitationOrder.add(i);
		}
		Random rnd = new Random(1);
		Collections.shuffle(randomNumbers, rnd);
		Collections.shuffle(visitationOrder, rnd);
	}
	
	
	
}
