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
		PVector rp = this.randomPoint2();
		rp.normalize();
		rp.mult(maxDist);
		return PVector.add(currentPt, rp);
	}
	
	
	
}// end RandomStream class


class QRandomStream{
	private static int uniqueID = 0;
	private static ArrayList<Integer> visitationOrder;
	private static ArrayList<Integer> randomNumbers;
	private int arraySize = 10000;
	int seed = 1;
	
	int iterationCounter = 0;
	
	
	
	QRandomStream(int rseed){
		init(rseed);
	}
	
	QRandomStream(){
		init(  getUniqueID()  );
	}
	
	void init(int rseed){
		if(randomNumbers == null) {
			initRandomNumbers();
		}
		seed = rseed;
	}
	
	int getUniqueID() {
		uniqueID++;
		if(uniqueID >= 10000) uniqueID = 0;
		return uniqueID;
	}
	
	boolean randomEvent(float prob){
		float r = nextFloat();
		if( r < prob ) return true;
		return false;
	}
	
	int randRangeInt(int lo, int hi){
		float r = nextFloat();
		return (int)( (lo + r*(hi-lo) ) + 0.5f);
	}
	
	float randRangeGaussian(float lo, float hi) {
		return ( randRangeF( lo,  hi) + randRangeF( lo,  hi ) + randRangeF( lo,  hi ))/3.0f;
		
	}
	
	float randRangeF(float lo, float hi){
		float r = nextFloat();
		return (lo + r*(hi-lo) );
	}
	
	float perturb(float v, float amt) {
		return v + (randRangeF(-v,v) * amt);
	}
	
	int nextInt() {
		
		int idexOfRandomNumber = (visitationOrder.get(iterationCounter) + seed)%arraySize;
		iterationCounter++;
		if(iterationCounter >= arraySize) iterationCounter = 0;
		return randomNumbers.get(idexOfRandomNumber);
	}
	
	float nextFloat() {
		float f = nextInt()/(float)arraySize;
		return f;
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




