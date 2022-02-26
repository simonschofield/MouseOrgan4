package MOScene3D;

import MOMaths.Range;

///////////////////////////////////////////////////////////////////////////////////
//This is a particular filter you impose on the distance buffer to skew the distance
//values into the range and population you would like for your image
//It has a complete knock-on effect for all geometry (depth, 3-D locations, normals)

//works by mapping the original value, in the range MIN_ORIG_DIST, MAX_ORIG_DIST, through a floating-point LUT of 


public class DistanceBufferFilter{
	Range original_extrema = new Range();
	float zGamma = 1.0f;

	void setOriginalExtrema(Range e) {
		original_extrema = e.copy();
		System.out.println("DistanceBufferFilter setoriginalExtrema " + original_extrema.toStr());
	}

	public void setDistanceGamma(float zbend) {

		zGamma = zbend;
	}

	float applyFilter(float unfilteredDistance) {
		if(zGamma == 1.0f) return unfilteredDistance;

		float normalised = original_extrema.norm(unfilteredDistance);

		// apply your filter here
		//normalised = (float) Math.pow((double)normalised, 0.99999999);

		//normalised = 1 - (normalised*normalised);
		normalised = (float)Math.pow(normalised, zGamma);

		return original_extrema.lerp(normalised);
	}
}




