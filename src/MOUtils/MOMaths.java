package MOUtils;


public class MOMaths {

	public static boolean isNaN(float n) {
		Float N = n;
		return N.isNaN();
	}

	public static boolean isBetweenInc(float v, float a, float b) {
		float lo = Math.min(a, b);
		float hi = Math.max(a, b);
		if (v >= lo && v <= hi)
			return true;
		return false;
	}

	public static boolean isClose(float v, float target, float tol) {
		if (Math.abs(v - target) >= tol)
			return false;
		return true;
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
	
	public static PVector bisector(PVector v1, PVector v2) {
		// returns the vector that bisects two other vectors. 2 and 3d. 
		float angleBetween = PVector.angleBetween(v1, v2);
		float halfAngle = angleBetween/2;
		PVector v = v1.copy();
		v.rotate(halfAngle);
		return orthogonal(v);
	}

	//interpolates the input value between the low and hi values
	public static float ramp(float v, float low, float hi) {
		float rampedV = lerp(low, hi, v);
		return constrain(rampedV, 0, 1);
	}
	
	//////////////////////////////////////////////////////////////////////////
	// coordinate space conversion between 
	// doc space (longest edge 0..1, shortest edge < 1 according to aspect)... and
	// normalised space (both edges in range 0..1, regardless of aspect)
	// these are useful for the mouseorgan in many places
	static PVector docSpaceToNormalisedSpace(PVector docSpace, float aspect) {
		
		
		PVector normalisedPoint = docSpace.copy();
		if(aspect > 1) {
			// i.e. landscape
			// x already in range 0..1
			// scales y up to be in range 0...1
			normalisedPoint.y = docSpace.y *aspect; 
		} else {
			// i.e. portrait
			// y already in range 0..1
			// scales x to be in range 0..1
			normalisedPoint.x = docSpace.x/aspect;
		}
		return normalisedPoint;
		
	}
	
	
	static PVector normalisedSpaceToDocSpace(PVector normSpace, float aspect) {
		
		PVector docSpacePoint = normSpace.copy();
		if(aspect > 1) {
			// i.e. landscape
			// keep x in 0...1 range
			// reduce y to be reduced by the aspect
			docSpacePoint.y /= aspect;
		} else {
			// i.e. portrait
			// keep y in range 0..1
			// reduce x by the aspect
			docSpacePoint.x *= aspect;
		}
		return docSpacePoint;
	}

}


