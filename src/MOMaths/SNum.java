package MOMaths;

/////////////////////////////////////////////////////////////////////////////////////
//SNum stands for Stochastic Number.
//These classes allow you to pass "pre-programmed" stochastic numbers into functions
//The number is always generated in the form of a float
//To keep the random stream independent from other random streams
//all StochasticNumbers require an explicit random seed, or a pre-existing QRandomStream
//The default method is  - fixed value of 0



public class SNum{


	public static final int	FIXED = 0;
	public static final int	UNIFORM_RANGE=1;
	public static final int	NORMALISED_RANGE=2;
	public static final int	SKEWED_NORMALISED_RANGE=3;
	public static final int	INTEGER_RANGE = 4;


	private QRandomStream qRandomStream;

	int method = FIXED;
	float lowerLimit = 0;
	float upperLimit = 1;
	float centerOfDistribution = 0;
	int numAverageSamples = 4;

	public SNum(int rseed){
		qRandomStream = new QRandomStream(rseed);
	}

	public SNum(QRandomStream qrs, int seedOffset, Integer streamPosition){
		// make a fresh copy of the QRandomStream , with counter set to 0
		// and can also set a seedoffset (set to 0 for exactly same stream sequence)
		// streamPosition is nullable, in which case the copied random stream gets the position of the
		// one passed in
		setRandomStream(qrs, seedOffset, streamPosition);
	}

	public SNum copy() {
		SNum newSnum = new SNum(qRandomStream,0,0);
		return newSnum;
	}

	public SNum copy(QRandomStream qrs, int seedOffset, Integer streamPosition) {
		// makes a copy but is initialised with another random stream.
		// The copy has the start conditions of the passed random stream, but is totally independent there after
		SNum newSnum = new SNum(qrs,seedOffset,streamPosition);
		return newSnum;
	}

	public void setRandomStream(QRandomStream qrs,  int seedOffset, Integer streamPosition) {
		int newSeed = qrs.getSeed() + seedOffset;
		qRandomStream = qrs.copy();
		qRandomStream.setSeedAndPosition(newSeed, streamPosition);
	}

	/*
	SNum getNewSNum(int seed) {
		// this returns a deep copy, with a new random stream
		// but set to the same seed and position of the exiting object
		QRandomStream copyOfStream = randomStream.copy();
		copyOfStream.seed = seed;
		SNum newCopy = new SNum(copyOfStream);
		newCopy.setMethod(method, lowerLimit, upperLimit);
		return newCopy;
	}*/



	///////////////////////////////////////////////////////////////
	// these methods set the method and values in this object
	void fixed(float v) {
		setMethod(FIXED, v, Float.MAX_VALUE);
	}


	public void uRange(float lo, float hi) {
		setMethod(UNIFORM_RANGE, lo, hi);
	}

	void nRange(float lo, float hi) {
		setMethod(NORMALISED_RANGE, lo, hi);
	}

	public void snRange(float lo, float cen, float hi) {
		setMethod(SKEWED_NORMALISED_RANGE, lo, cen, hi);
	}

	void iRange(int lo, int hi) {
		setMethod(INTEGER_RANGE, lo, hi);
	}

	/*
	//////////////////////////////////////////////////////////
	// these methods return a copy of the number

	SNum getFixed(float v){
		SNum sn = getNewSNum(getSeed());
		sn.setMethod(SNumMethod.FIXED, v, Float.MAX_VALUE);
		return sn;
	}

	SNum getURange(float lo, float hi, int seed){
		SNum sn = getNewSNum(seed);
		sn.setMethod(SNumMethod.UNIFORM_RANGE, lo, hi);
		return sn;
	}

	SNum getNRange(float lo, float hi, int seed){
		SNum sn = getNewSNum(seed);
		sn.setMethod(SNumMethod.NORMALISED_RANGE, lo, hi);
		return sn;
	}

	SNum getIRange(int lo, int hi, int seed){
		SNum sn = getNewSNum(seed);
		sn.setMethod(SNumMethod.INTEGER_RANGE, lo, hi);
		return sn;
	}
	*/



	public void setMethod(int m, float lo, float hi) {
		method = m;
		lowerLimit = lo;
		centerOfDistribution = (hi-lo)/2;
		upperLimit = hi;
	}

	void setMethod(int m, float lo, float cen, float hi) {
		method = m;
		lowerLimit = lo;
		centerOfDistribution = cen;
		upperLimit = hi;
	}


	public float get() {

		switch(method) {
			case FIXED:
				return lowerLimit;
			case UNIFORM_RANGE:
				return qRandomStream.randRangeF(lowerLimit, upperLimit);
			case NORMALISED_RANGE:
				return qRandomStream.randRangeAveragedDistribution(lowerLimit, upperLimit, numAverageSamples);
			case SKEWED_NORMALISED_RANGE:
				return qRandomStream.skewedNormalisedRange(lowerLimit, centerOfDistribution, upperLimit);
			case INTEGER_RANGE:
				return qRandomStream.randRangeInt((int)lowerLimit, (int)upperLimit);
		}
		return 0;
	}



}





