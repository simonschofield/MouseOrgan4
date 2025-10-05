package MOMaths;

public class MOMaths {

	public static boolean isNaN(float n) {
		Float N = n;
		return N.isNaN();
	}
	
	
	public static float epsilon() {
		return (Float.MIN_VALUE * 3);
	}
	
	public static boolean nearZero(float v) {

		  if ( Math.abs(v) <= epsilon()) return true;
		  return false;
	}
	
	public static boolean isOdd(int n) {
		if (n % 2 == 0) return false;
		return true;
	}

	public static boolean isBetweenInc(float v, float a, float b) {
		float lo = Math.min(a, b);
		float hi = Math.max(a, b);
		if (v >= lo && v <= hi)
			return true;
		return false;
	}

	public static boolean isClose(float v, float target, float tol) {
		if (diff(v,target) >= tol)
			return false;
		return true;
	}
	
	public static float diff(float a, float b) {
		return Math.abs(a - b);
	}

	public static float lerp(float p, float lo, float hi) {

		return lo + p * (hi - lo);
	}

	public static float norm(float p, float lo, float hi) {

		float dev = hi - lo;
		if (dev == 0)
			return 0.0f;

		return (p - lo) / dev;
	}

	public static float map(float p, float inRangeLo, float inRangeHi, float outRangeLo, float outRangeHi) {
		float p1 = norm(p, inRangeLo, inRangeHi);
		return lerp(p1, outRangeLo, outRangeHi);

	}
	
	///////////////////////////////////////////////////////////////////////////////
	// as p1 is in the range 0..1 , we apply gamma here
	// A gamma of < 1, will result in p1 being bent up at low values, and not much differece at high value, so pushes more into high values quicker
	// A gamma of > 1 will result in p1 being bent down at low values, with a more rapid increase in th e higher values
	public static float mapWithGamma(float p, float inRangeLo, float inRangeHi, float outRangeLo, float outRangeHi, float gamma) {
		float p1 = norm(p, inRangeLo, inRangeHi);
		
		p1 = (float) Math.pow(p1,  gamma);

		return lerp(p1, outRangeLo, outRangeHi);

	}
	
	
	
	
	public static float mapClamped(float p, float inRangeLo, float inRangeHi, float outRangeLo, float outRangeHi) {
		float p1 = norm(p, inRangeLo, inRangeHi);
		float m =  lerp(p1, outRangeLo, outRangeHi);
		return constrain(m, outRangeLo, outRangeHi);
	}

	public static float constrain(float val, float lower, float upper) {
		if (val < lower)
			return lower;
		if (val > upper)
			return upper;
		return val;
	}
	
	public static int constrain(int val, int lower, int upper) {
		if (val < lower)
			return lower;
		if (val > upper)
			return upper;
		return val;
	}

	public static float wrap(float val, float lower, float upper) {
		return ((val - lower) % (upper - lower)) + lower;
		
	}
	
	
	public static float wrap01(float val) {
		
		if(val < 0) {
			val += 1;
			return val;
		}
		if(val > 1) {
			val -= 1;
			return val;
		}
		return val;
	}
	
	public static float round(float f, int numPlaces) {
		double d = (double)f;
		int multiplier = (int) Math.pow(10, numPlaces);
		int dmult = (int) (d * multiplier);
		return (float)(dmult / (double) multiplier);
	}

	public static float sin(float theta) {

		return (float) (Math.sin(theta));
	}

	public static float getUnitSign(float in) {
		if (in < 0)
			return -1;
		return 1;
	}

	public static float cos(float theta) {

		return (float) (Math.cos(theta));
	}

	// raises the input value by a power
	public static float gammaCurve(float v, float gamma) {

		return (float) Math.pow(v, gamma);

	}

	// creates a "flipped" gamma curve
	public static float inverseGammaCurve(float v, float gamma) {

		return (float) (1.0 - Math.pow(1.0 - v, (double) gamma));

	}

	// creates a nice S-shaped curve, useful for contrast functions

	/**
	 * Generates a Gaussian-distribution value (the Y value) based on the input (the X value)
	 * @param xVal is the distance along the X axis you want the Y value generated
	 * @param curveHeight is the maximum Y value of the curve
	 * @param centreVal - is the centre value of the X axis (probably will be zero)
	 * @param stdDev -  is the drop-off in value from the centre point
	 * @return
	 */
	public static float gaussianCurve(float xVal, float curveHeight, float centreVal, float stdDev) {
		// returns the Y val on the gaussian curve at xVal
		float e = 2.71828f;
		float a = xVal - centreVal;
		float raisedBy = -(a * a) / (2 * (stdDev * stdDev));
		return (float) (curveHeight * Math.pow(e, raisedBy));

	}

	public static PVector rotatePoint(PVector p, PVector around, float degrees) {
		// rotates a point clockwise round another point by degrees

		double toRad = Math.toRadians((double) degrees);
		float rx = p.x - around.x;
		float ry = p.y - around.y;

		float newX = (float) (rx * Math.cos(toRad) - ry * Math.sin(toRad));
		float newY = (float) (ry * Math.cos(toRad) + rx * Math.sin(toRad));

		newX += around.x;
		newY += around.y;

		return new PVector(newX, newY);
	}
	
	public static PVector orthogonal(PVector v) {
		// only works with 2D vectors. The Z is ignored but maintained
		// returns the clockwise orthogonal. 
		return new PVector(v.y, -v.x, v.z);
	}
	
	

	//interpolates the input value between the low and hi values
	public static float ramp(float v, float low, float hi) {
		float rampedV = lerp(low, hi, v);
		return constrain(rampedV, 0, 1);
	}
	
	public static PVector bisector(PVector v1, PVector v2) {
		// returns the vector that bisects two other vectors. 2 and 3d. 
		//float angleBetween = PVector.angleBetween(v1, v2);
		float angleBetween = getClockwiseAngleBetween(v1, v2);
		float halfAngle = angleBetween/2;
		PVector v = v1.copy();
		v.rotate(halfAngle);
		return v; //orthogonal(v);
	}
	
	public static float getHingedAngleBetween(PVector p1, PVector join, PVector p2) {
		// calculated the CLOCKWISE angle between line p1->join and line join->p2
		// the the angle returned (in radians) is the angle between the two lines, where an acute angle is a low number < PI and
		// an obtuse angle is a high number > PI. Two lines forming one straight line would be == PI.
		// Two lines -- would be 180 degrees, two lines _| would be 90, and two lines /| would be 45 
		
		PVector vThis = PVector.sub(join, p1);
		PVector vOther = PVector.sub(p2, join);
		return getClockwiseAngleBetween(vThis, vOther);
		//float dot = vThis.dot(vOther)  ;    // dot product between [x1, y1] and [x2, y2]
		//float det = vThis.x*vOther.y - vThis.y*vOther.x;      
		//return (float) (Math.atan2(det, dot) + Math.PI);  // atan2(y, x) or atan2(sin, cos)
		
	}
	
	public static float getClockwiseAngleBetween(PVector v1, PVector v2){
		// calculated the CLOCKWISE angle between line p1 and p2
		// the the angle returned (in radians) is the angle between the vectors, where an acute angle is a low number < PI and
		// an obtuse angle is a high number > PI. Two lines forming one straight line would be == PI.
		// Two lines -- would be 180 degrees, two lines _| would be 90, and two lines /| would be 45 
		
		
		float dot = v1.dot(v2)  ;    // dot product between [x1, y1] and [x2, y2]
		float det = v1.x*v2.y - v1.y*v2.x;      
		return (float) (Math.atan2(det, dot) + Math.PI);  // atan2(y, x) or atan2(sin, cos)
		
	}
	
	public static boolean isPointInTriangle(PVector p, PVector t1, PVector t2, PVector t3)
	{
		// untested
	    float s = (t1.x - t3.x) * (p.y - t3.y) - (t1.y - t3.y) * (p.x - t3.x);
	    float t = (t2.x - t1.x) * (p.y - t1.y) - (t2.y - t1.y) * (p.x - t1.x);

	    if ((s < 0) != (t < 0) && s != 0 && t != 0)
	        return false;

	    var d = (t3.x - t2.x) * (p.y - t2.y) - (t3.y - t2.y) * (p.x - t2.x);
	    return d == 0 || (d < 0) == (s + t <= 0);
	}
	

}


