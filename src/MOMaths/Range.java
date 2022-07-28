package MOMaths;

//////////////////////////////////////////////////////////
//Range class
//Defines two values that form the limits of the range
//
public class Range {
	public float limit1;
	public float limit2;

	// for working out the mean
	int numExtremaSamples = 0;
	float sumExtremaSamples = 0;
	
	public Range() {
		limit1 = 0.0f;
		limit2 = 0.0f;
	}

	public Range(float l1, float l2) {
		limit1 = l1;
		limit2 = l2;
	}

	public void initialiseForExtremaSearch() {
		limit1 = Float.MAX_VALUE;
		limit2 = -Float.MAX_VALUE;
		numExtremaSamples = 0;
		sumExtremaSamples = 0;
	}

	public float getLower() {
		return Math.min(limit1, limit2);
	}

	public float getUpper() {
		return Math.max(limit1, limit2);
	}

	public boolean isBetweenInc(float v) {
		if (v >= getLower() && v <= getUpper())
			return true;
		return false;
	}

	public Range copy() {
		return new Range(limit1, limit2);
	}

	public float getDifference() {
		return getUpper() - getLower();
	}

	public float getMidValue() {
		return (getLower() + getUpper()) / 2.0f;
	}
	
	public float getMeanExtremaValue() {
		
		return sumExtremaSamples/numExtremaSamples;
	}

	public void addExtremaCandidate(float v) {

		if (v == -Float.MAX_VALUE || v == Float.MAX_VALUE)
			return;

		numExtremaSamples ++;
		sumExtremaSamples += v;
		
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

	public float norm(float v) {
		// returns the value v normalised between the limits
		return MOMaths.norm(v, limit1, limit2);
	}

	public String toStr() {

		return "limit1 " + limit1 + ", limit2 " + limit2;
	}

	public float lerp(float v) {
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
