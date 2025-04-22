package MOPointGeneration;

import MOMaths.MOMaths;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Class PackingInterpolationScheme
//is used to define a radius-based distribution against a control input (e.g. point of distribution against image value, or Z)
//The packing radius is calculated against an input control value, (probably an image pixel value 0...1)
//getRadius() always returns a the packing radius, as this is used to calculate possible neighbouring point spacings and their "exclusion zones" by the utilising packing algorithm. 
//The user sets the desired packing radius, and this is used to calculate the internal Interpolation Units using either RADIUS, SURFACE_AREA or VOLUME options (default is SA) .
//
//Explanation: If the this class just interpolated a RADIUS against tone,  the the packing would be disproportionally spaced against the tone as the SA (and therefore the packing) of a circle is proportional to the
//square of its radius. For instance, if the spacing was to increase with brightness, then small increases in image brightness would result in increasingly large spacings, . 
//Hence the user may wish the interpolation to be in terms of resultant surface area, or volume of a point's "exclusion zone". Surface_area is the default mode.
//
//Another special consideration with distribution interpolations, is what to do beyond the controlMin and controlMax values
//This is dealt with by the underInputMinAction and overInputMaxOption settings, so that beyond this the min/max input extents:-
//EXCLUDE -  report that this value is excluded, so no further action may be taken (e.g. do not put a point down at all)
//CLAMP -  clamp the input to that extent, so the returned value is constant beyond that extent (e.g. keep the same distribution as if it was the extent)
//EXTRAPOLATE - keep on interpolating beyond that extent (i.e. so the output values exceed the outputValueAtInputMin/Max values )
//

public class PackingInterpolationScheme {
	
    // under and over control val min/max options
	public static final int EXCLUDE = 0; // Excludes the placement of points if input control val is over or under. excluded() method returns true;
	public static final int CLAMP = 1; // control value is clamped at limit
	public static final int EXTRAPOLATE = 2; // control value continues to be extrapolated outside of limits
	
	
	// control value is interpreted as a distance for packing, based on the following schemes
	public static final int RANGE_UNITS_RADIUS = 3; // the output packing distance is linearly proportional to control value
	public static final int RANGE_UNITS_SURFACE_AREA = 4; // The output packing distance is proportional to a surface area calculated from the packing distance R i.e. PI(R*R) 
	public static final int RANGE_UNITS_VOLUME = 5; // The output packing distance is proportional to a volume area calculated from the packing distance R i.e. (4/3)PI(R*R*R) 
	
	
	

	float radiusAtControlMin;
	float radiusAtControlMax; 

	float controlValueMin = 0;
	float controlValueMax = 1;

	float interpolationUnitsAtControlValMin = 0;
	float interpolationUnitsAtControlValMax = 1;

	int underControlValueMinOption = EXTRAPOLATE;
	int overControlValueMaxOption = EXTRAPOLATE;

	//userUnits - RADIUS, SURFACE_AREA, VOLUME
	int interpolationUnitsOption = RANGE_UNITS_SURFACE_AREA;

	public PackingInterpolationScheme() {

	}

	public PackingInterpolationScheme(float controlValMin, float controlValMax, float radAtControlMin, float radAtControlMax,
			int underControlValMinOption, int overControlValMaxOption) {
		set(controlValMin, controlValMax, radAtControlMin, radAtControlMax, underControlValMinOption, overControlValMaxOption);
	}

	void setRangeUnits(int m) {
		if (m == RANGE_UNITS_RADIUS)
			interpolationUnitsOption = m;
		if (m == RANGE_UNITS_SURFACE_AREA)
			interpolationUnitsOption = m;
		if (m == RANGE_UNITS_VOLUME)
			interpolationUnitsOption = m;
		// need to kick the whole system to recalculate the interpolation units
		set(controlValueMin, controlValueMax, radiusAtControlMin, radiusAtControlMax, underControlValueMinOption, overControlValueMaxOption);
	}

	void set(float controlValMin, float controlValMax, float radAtControlMin, float radAtControlMax,
			int underControlValMinOption, int overControlValMaxOption) {

		controlValueMin = controlValMin;
		controlValueMax = controlValMax;

		radiusAtControlMin = radAtControlMin;
		radiusAtControlMax = radAtControlMax;


		//if(rangeUnits == RANGE_UNITS_RADIUS) {
		interpolationUnitsAtControlValMin = radiusAtControlMin;
		interpolationUnitsAtControlValMax = radiusAtControlMax;
		//}
		if(interpolationUnitsOption == RANGE_UNITS_SURFACE_AREA) {
			interpolationUnitsAtControlValMin = (float)Math.PI * (radiusAtControlMin*radiusAtControlMin);
			interpolationUnitsAtControlValMax = (float)Math.PI * (radiusAtControlMax*radiusAtControlMax);;
		}
		if(interpolationUnitsOption == RANGE_UNITS_VOLUME) {
			interpolationUnitsAtControlValMin = (float)Math.PI * (radiusAtControlMin*radiusAtControlMin*radiusAtControlMin);
			interpolationUnitsAtControlValMax = (float)Math.PI * (radiusAtControlMax*radiusAtControlMax*radiusAtControlMax);
		}

		underControlValueMinOption = underControlValMinOption;
		overControlValueMaxOption = overControlValMaxOption;

	}

	boolean isExcluded(float controlVal) {
		boolean result = false;
		if (controlVal < controlValueMin && underControlValueMinOption == EXCLUDE) result = true;
			
		if (controlVal > controlValueMax && overControlValueMaxOption == EXCLUDE)result = true;
		
		
			
		//System.out.println("Control val " + controlVal + " controlValueMax " + controlValueMax + " overControlValueMaxOption " + overControlValueMaxOption);
		
		
		
		return result;
	}


	public float getRadius(float controlVal) {

		if (  (underControlValueMinOption == CLAMP || underControlValueMinOption == EXCLUDE) && controlVal < controlValueMin)  controlVal = controlValueMin;
		if (  (overControlValueMaxOption == CLAMP || overControlValueMaxOption == EXCLUDE)  && controlVal > controlValueMax)   controlVal = controlValueMax;

		float val = MOMaths.map(controlVal, controlValueMin, controlValueMax, interpolationUnitsAtControlValMin, interpolationUnitsAtControlValMax);

		if (interpolationUnitsOption == RANGE_UNITS_SURFACE_AREA) {
			return (float) Math.sqrt(val / Math.PI);
		}

		if (interpolationUnitsOption == RANGE_UNITS_VOLUME) {
			return (float) Math.cbrt(val / Math.PI);
		}

		return val;

	}
	
	
	public float getMaxRadius() {
		// as we cannot be sure which way the system is interpolating, we do both extremes and return the max
		float r1 = getRadius(controlValueMin);
		float r2 = getRadius(controlValueMax);
		return Math.max(r1, r2);
	}

}
