import java.util.Random;

/////////////////////////////////////////////////////////////////////////////
//
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