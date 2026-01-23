package MOScene3D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

import MOImage.ByteImageGetterSetter;
import MOMaths.MOMaths;
import MOMaths.Range;

/**
 * Defines a multi-contol point ramp suitable for calculating a specific tone (in any range) and alpha value (in any range) given a specific control value (in any range).
 * Ramps are made of at least 2 control points (the ToneRampControlPoint class). As control points are added, they are sorted in order of their control point values. <p>
 * 
 * One purpose, used in BasePointLighting,  is to mix in an amount of tone with another colour (the basePointTone) to create a ramp-enhanced tone. hence we need both the tone and 
 * an alpha value to be ramped, in order to mix in the correct amount using the modifyTone(..) method here.
 * 
 * The interpolated values for any point on the ramp is then calculated using a control value ranging from
 * the lowest control-point control value, to the highest. Input control values are clamped to this range.<p>
 * 
 * A ramp is defined by calling addControlPoint(..) to add at least two control points <p>
 * Once defined call modifyTone(float controlValue, float baseTone) to modify an existing "base tone" with the ramp values.
 */
public class ToneRamp {
	
	ArrayList<ToneRampControlPoint> rampItems = new ArrayList<ToneRampControlPoint>();
	Range controlValueExtrema;
	
	
	public ToneRamp(){
		
		controlValueExtrema = new Range();
		controlValueExtrema.initialiseForExtremaSearch();
	}
	
	/**
	 * adds a ramp control-point "stage" to the full ramp. There must be > 1 for the thing to work.
	 * @param c - the control value, in whatever range you like. This is the input "position" on the ramp.
	 * @param v - The value achieved at the control value defined in this ramp-element. It is blended between adjacent control points
	 * @param a - An "alpha" blend effect to accompany the tone value. It is similarly blended
	 */
	public void addControlPoint(float c, float v, float a) {
		ToneRampControlPoint tcp = new ToneRampControlPoint(c,v,a);
		rampItems.add(tcp);
		
		controlValueExtrema.addExtremaCandidate(c);
		
		// the elements are sorted against the control value, with the lowest control value in the zeroth element
		rampItems.sort(Comparator.comparing(ToneRampControlPoint::getControlValue));
		
	}
	
	
	
	/**
	 * Once a ToneRamp has been created, a "base tone" can be modified with ramp values, based on a control value. Base tone and returned values should be in the range 0..1
	 * @param controlValue - the value used to interpolate over the ramp. can be any range (e.g. 3D height of item)
	 * @param baseTone - the input tone to be modifies (range 0..1)
	 * @return - the modified tone (range 0..1)
	 */
	public float modifyTone(float controlValue, float baseTone) {
		ToneRampControlPoint tcp =  getValue(controlValue);
		return MOMaths.lerp( tcp.alphaValueAtControlPoint, baseTone, tcp.toneValueAtControlPoint );
	}
	
	
	
	/**
	 * One a ramp has been created, this method returns the interpolated value and alpha values based on an input control value
	 * @param c - the control value in whatever range was specified when creating the ramp
	 * @return - ToneRampControlPoint class containing the interpolated toneValueAtControlPoint and alphaValueAtControlPoint
	 */
	public ToneRampControlPoint getValue(float c) {
		// the query value c finds a two control point where
		// c >= lower control-point control value
		// c < upper control point control value, unless upper control point is the final element in the array, in which case c can equal upper control point control value
		// c is always constrained to be between the lowest and highest control values in the list of control points.
		if(rampItems.size() < 2) {
			System.out.println("ToneRamp:getValue() being called and not enough control points in the ramp, there need to be at least 2. Returning null");
			return null;
		}
		
		c = controlValueExtrema.constrain(c);
		
		
		ToneRampControlPoint lowerControlPoint;
		ToneRampControlPoint upperControlPoint;
		// Deal with exceptions. if c == lower limit, return the first control point un-interpolated
		if(c == controlValueExtrema.getLower()) {
			return rampItems.get(0);
		}
		
		// Deal with exceptions. if c == upper limit, return the final control point un-interpolated
		if(c == controlValueExtrema.getUpper()) {
			return rampItems.get(rampItems.size()-1);
		}
		
		// Deal with exceptions. Many ramps will only have 2 control points
		if(rampItems.size()==2) {
			lowerControlPoint = rampItems.get(0);
			upperControlPoint = rampItems.get(1);
			return interpolateControlPoints(c, lowerControlPoint, upperControlPoint);
		}
		
		
		// only get here if there are three or more control points
		for(int n = 0; n < rampItems.size()-1; n++) {
			lowerControlPoint = rampItems.get(n);
			upperControlPoint = rampItems.get(n+1);

			if(c >= lowerControlPoint.controlPointValue && c < upperControlPoint.controlPointValue) {
				return interpolateControlPoints(c, lowerControlPoint, upperControlPoint);
			}

		}
		
		// if you get here, something has gone wrong
		System.out.println("ToneRamp:getValue() something else has gone wrong, cannot find control value c " + c + ", in range of control values " + controlValueExtrema.toStr() + ". Returning null");
		return null;
	}
	
	
	
	private ToneRampControlPoint interpolateControlPoints(float c, ToneRampControlPoint tcpA, ToneRampControlPoint tcpB) {
		//System.out.println("interpolateControlPoints c " + c + ", tcpA " + tcpA.toStr() + ", tcpB " + tcpB.toStr());
		float normalisedCP = MOMaths.norm(c, tcpA.controlPointValue, tcpB.controlPointValue);
		normalisedCP = MOMaths.constrain(normalisedCP, 0, 1);
		float interpolatedValueAtControlPoint = MOMaths.lerp(normalisedCP, tcpA.toneValueAtControlPoint, tcpB.toneValueAtControlPoint);
		float interpolatedAlphaAtControlPoint = MOMaths.lerp(normalisedCP, tcpA.alphaValueAtControlPoint, tcpB.alphaValueAtControlPoint);
		
		return new ToneRampControlPoint(normalisedCP, interpolatedValueAtControlPoint, interpolatedAlphaAtControlPoint);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// debug
	//
	//
	
	/**
	 * @return string of the contained control point values in order
	 */
	public String toStr() {
		String strout = "";
		for(ToneRampControlPoint tcp: rampItems) {
			
			strout += tcp.toStr() + "\n";
		}
		
		return strout;
	}
	
	
	/**
	 * Debug testing. Generates an image with a base tone, adding in the currently defined ramp tone. This can then be saved and inspected.
	 * returns a test image with the control value ranging from 0 at the bottom of the image, to 
	 * @param wd
	 * @param ht
	 * @param baseTone
	 * @return
	 */
	public BufferedImage makeTestImage(int wd, int ht, float baseTone) {

		BufferedImage img =  new BufferedImage(wd, ht, BufferedImage.TYPE_BYTE_GRAY);
		
		ByteImageGetterSetter byteImageGetterSetter = new ByteImageGetterSetter(img);
		float controlValue = 0;
		float controlvalueYStep = 1.0f/ht;
		for(int y = 0; y < ht; y++) {
			
			ToneRampControlPoint tcp =  getValue(controlValue);
			
			
			float rampValue = tcp.toneValueAtControlPoint;
			float toneAlpha = tcp.alphaValueAtControlPoint;
			float alphaBlendedTone = MOMaths.lerp( toneAlpha, baseTone, rampValue );
			
			int rampedvaluei = (int)  (alphaBlendedTone*255);
			
			for(int x = 0; x < wd; x++) {
				byteImageGetterSetter.setPixel(x, y, rampedvaluei);
			}
			controlValue += controlvalueYStep;
		}
		return img;
		
	}
	
}


/**
 * A single control point on a ramp. This contains controlPointValue, toneValueAtControlPoint, alphaValueAtControlPoint
 * Ramps are composed of multiple control points. They are sorted in the order lowest control value to highest control value.<p>
 * 
 */
class ToneRampControlPoint{
	/**
	 * Used in conjunction with other RampElements in the RampClass
	 * The controlValue is a value at which the rampvalue is achieved
	 */
	float controlPointValue;
	float toneValueAtControlPoint;
	float alphaValueAtControlPoint;
	int index;
	
	
	public ToneRampControlPoint(float controlPointValue, float valueAtControlPoint, float alphaAtControlPoint) {
		this.controlPointValue = controlPointValue;
		this.toneValueAtControlPoint = valueAtControlPoint;
		this.alphaValueAtControlPoint = alphaAtControlPoint;
	}
	
	public String toStr() {
		
		return "[ control value " + controlPointValue + ", tone value " + toneValueAtControlPoint + ", alpha value " + alphaValueAtControlPoint + " ]";
		
	}
	
	public float getControlValue() {
		// used in sorting the order of the control values in the ToneRamp's addControlPoint method
		return controlPointValue;
	}
	
	
}
