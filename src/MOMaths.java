import java.text.SimpleDateFormat;
import java.util.Date;

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
			// scales y to be in range 0...1
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

//////////////////////////////////////////////////////////
//
//

class ImageDimensions{
	int width = 0;
	int height = 0;
	
	public ImageDimensions(int w, int h) {
		width = w;
		height = h;
		
	}
	
	Rect getRect() {
		
		return new Rect(0,0,width,height);
		
	}
	
}


//////////////////////////////////////////////////////////
//Range class
//Defines two values that form the limits of the range
//
class Range {
	float limit1;
	float limit2;

	public Range() {
		limit1 = 0.0f;
		limit2 = 0.0f;
	}

	public Range(float l1, float l2) {
		limit1 = l1;
		limit2 = l2;
	}

	void initialiseForExtremaSearch() {
		limit1 = Float.MAX_VALUE;
		limit2 = -Float.MAX_VALUE;

	}

	float getLower() {
		return Math.min(limit1, limit2);
	}

	float getUpper() {
		return Math.max(limit1, limit2);
	}

	boolean isBetweenInc(float v) {
		if (v >= getLower() && v <= getUpper())
			return true;
		return false;
	}

	Range copy() {
		return new Range(limit1, limit2);
	}

	float getDifference() {
		return getUpper() - getLower();
	}

	float getMidValue() {
		return (getLower() + getUpper()) / 2.0f;
	}

	void addExtremaCandidate(float v) {

		if (v == -Float.MAX_VALUE || v == Float.MAX_VALUE)
			return;

		if (limit1 == 0 && limit2 == 0) {
			limit1 = v;
			limit2 = v;
			return;
		}
		if (v < limit1)
			limit1 = v;
		if (v > limit2)
			limit2 = v;
	}

	float norm(float v) {
		// returns the value v normalised between the limits
		return MOMaths.norm(v, limit1, limit2);
	}

	String toStr() {

		return "limit1 " + limit1 + ", limit2 " + limit2;
	}

	float lerp(float v) {
		// returns the value v normalised between the limits
		return MOMaths.lerp(v, limit1, limit2);
	}

	float getMatch_Square(float f) {
		if (this.isBetweenInc(f))
			return 1.0f;
		return 0.0f;
	}

	float getMatch_Ramped(float f) {
		// match is 1 in the middle and raps to 0 at each limit
		float mid = getMidValue();
		float match = 0;
		// say range is 0.25..0.75 and f is 0.3
		if (f < mid) {
			match = MOMaths.norm(f, getLower(), mid);
		} else {
			match = MOMaths.norm(f, getUpper(), mid);
		}

		return MOMaths.constrain(match, 0.0f, 1.0f);
	}

	float getMatch_Gaussian(float f) {
		// 96% of a guass curve is within +- 2stdDev
		// so by setting stdDev = getDifference()/4.0 we achieve this
		float mid = getMidValue();
		float stdDev = getDifference() / 4.0f;
		return MOMaths.gaussianCurve(f, 1.0f, mid, stdDev);
	}

	float getMatch_RampedSquare(float f, float rampSize) {
		// this sort of shape ... __/^^^\__
		// rampsize is the size of the ramp in proportion to the difference min-max
		if (this.isBetweenInc(f))
			return 1.0f;
		float r = Math.abs(getDifference()) * rampSize;

		if (f < getLower()) {
			float rampedval = MOMaths.map(f, getLower() - r, getLower(), 0f, 1.0f);
			return MOMaths.constrain(rampedval, 0, 1);
		}
		if (f > getUpper()) {
			float rampedval = MOMaths.map(f, getUpper(), getUpper() + r, 1.0f, 0f);
			return MOMaths.constrain(rampedval, 0f, 1f);
		}
		// should never reach this
		return 0;
	}

}
