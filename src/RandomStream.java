import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/////////////////////////////////////////////////////////////////////////////
// This is a proper random stream
//

public class RandomStream{
	
	
	Random rstream;
	
	
	public RandomStream(){
		rstream = new Random();
	}
	
	public RandomStream(int rseed){
		rstream = new Random();
		
		if(rseed != -1) rstream.setSeed(rseed);
	}
	
	void seed(int rseed){
		rstream.setSeed(rseed);
	}
	
	boolean randomEvent(float prob){
		float r = rstream.nextFloat();
		if( r < prob ) return true;
		return false;
	}
	
	int randRangeInt(int lo, int hi){
		int r = rstream.nextInt(hi-lo);
		return r+lo;
	}
	
	
	
	float randRangeF(float lo, float hi){
		float r = rstream.nextFloat();
		return (lo + r*(hi-lo) );
	}
	
	float perturb(float v, float amt) {
		
		return v + (randRangeF(-v,v) * amt);
		
	}
	
	PVector randomPoint3(){
		float x = rstream.nextFloat();
		float y = rstream.nextFloat();
		float z = rstream.nextFloat();
		return new PVector(x,y,z);
	}
	
	PVector randomPoint2(){
		float x = rstream.nextFloat();
		float y = rstream.nextFloat();
		return new PVector(x,y,0);
	}
	
	PVector randomPoint2(float aspect){
		float maxX = 1f;
		float maxY = 1f;
	
		if(aspect > 1.0) {
			maxY = 1.0f/aspect;
		}else {
			maxX = aspect;
		}
		
		float x = rstream.nextFloat()*maxX;
		float y = rstream.nextFloat()*maxY;
		return new PVector(x,y,0);
	}
	
	PVector randomPoint2(Rect area){
		float x = rstream.nextFloat();
		float y = rstream.nextFloat();
		return area.interpolate(new PVector(x,y));
	}
	
	PVector brownianWalk(PVector currentPt, float maxDist){
		// same as, but better than, jiggle point
		float direction = this.randRangeF(0, 6.283185f);
		PVector directionVector = PVector.fromAngle(direction);
		directionVector.normalize();
		float thisDist = this.randRangeF(maxDist/4, maxDist);
		directionVector.mult(thisDist);
		//PVector rdirection = Pvector.
		
		
		
		//PVector rp = this.randomPoint2();
		//rp.sub(-0.5f, -0.5f);
		//rp.normalize();
		//rp.mult(maxDist);
		return PVector.add(currentPt, directionVector);
	}
	
	
	
}// end RandomStream class

/////////////////////////////////////////////////////////////////////////////////////
// QRandomStream class provides a computationally fast way of generating random numbers
// where the authenticity of the random number sequence is not paramount
// All instances of QRandomStream share the same list of (1  million) random integers (0... listSize-1)
// but each different seed uses a different visitation-order to the list, which still guarantees every one is visited,
// so using a different seed produced a completely different order.
// 
class QRandomStream {
	
	private static ArrayList<Integer> visitationOrder;
	private static ArrayList<Integer> randomNumbers;
	private int arraySize = 1000000;
	private int seed = 1;

	private int sequencePosition = 0;

	QRandomStream(int rseed) {
		init(rseed,0);
	}


	private void init(int rseed, int streamPosition) {
		if (randomNumbers == null) {
			initRandomNumbers();
		}
		setSeedAndPosition(rseed, streamPosition);
	}
	
	QRandomStream copy() {
		// returns a completely independent copy, but exactly the same stream sequence
		QRandomStream newCopy = new QRandomStream(seed);
		newCopy.sequencePosition = sequencePosition;
		return newCopy;
	}
	
	///////////////////////////////////////////////
	// seed and sequencePosition methods
	void setSeedAndPosition(Integer s, Integer p) {
		if(s != null) seed = s;
		if(p != null) {
			sequencePosition = p%getMaxSequencePosition();
		}
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
	
	int nextInt() {
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
	
	
	boolean randomEvent(float prob) {
		float r = nextFloat();
		if (r < prob)
			return true;
		return false;
	}

	int randRangeInt(int lo, int hi) {
		float r = nextFloat();
		return (int) ((lo + r * (hi - lo)) + 0.5f);
	}

	float randRangeGaussian(float lo, float hi) {
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

	float randRangeF(float lo, float hi) {
		float r = nextFloat();
		return (lo + r * (hi - lo));
	}

	float perturb(float v, float amt) {
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
	SNum snum(int seedOffset, Integer sequencePosition) {
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

/////////////////////////////////////////////////////////////////////////////////////
// SNum stands for Stochastic Number.
// These classes allow you to pass "pre-programmed" stochastic numbers into functions
// The number is always generated in the form of a float
// To keep the random stream independent from other random streams
// all StochasticNumbers require an explicit random seed, or a pre-existing QRandomStream
// The default method is  - fixed value of 0



class SNum{
	
	
	static final int	FIXED = 0;
	static final int	UNIFORM_RANGE=1;
	static final int	NORMALISED_RANGE=2;
	static final int	SKEWED_NORMALISED_RANGE=3;
	static final int	INTEGER_RANGE = 4;
	
	
	private QRandomStream qRandomStream;
	
	int method = FIXED;
	float lowerLimit = 0;
	float upperLimit = 1;
	float centerOfDistribution = 0;
	int numAverageSamples = 4;
	
	SNum(int rseed){
		qRandomStream = new QRandomStream(rseed);
	}
	
	SNum(QRandomStream qrs, int seedOffset, Integer streamPosition){
		// make a fresh copy of the QRandomStream , with counter set to 0
		// and can also set a seedoffset (set to 0 for exactly same stream sequence)
		// streamPosition is nullable, in which case the copied random stream gets the position of the 
		// one passed in
		setRandomStream(qrs, seedOffset, streamPosition);
	}
	
	SNum copy() {
		SNum newSnum = new SNum(qRandomStream,0,0);
		return newSnum;
	}
	
	SNum copy(QRandomStream qrs, int seedOffset, Integer streamPosition) {
		// makes a copy but is initialised with another random stream.
		// The copy has the start conditions of the passed random stream, but is totally independent there after
		SNum newSnum = new SNum(qrs,seedOffset,streamPosition);
		return newSnum;
	}
	
	void setRandomStream(QRandomStream qrs,  int seedOffset, Integer streamPosition) {
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
	

	void uRange(float lo, float hi) {
		setMethod(UNIFORM_RANGE, lo, hi);
	}
	
	void nRange(float lo, float hi) {
		setMethod(NORMALISED_RANGE, lo, hi);
	}
	
	void snRange(float lo, float cen, float hi) {
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
	
	
	
	void setMethod(int m, float lo, float hi) {
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
	
	
	float get() {
		
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




