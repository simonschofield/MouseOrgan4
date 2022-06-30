package MOUtils;

import java.util.ArrayList;

import MOMaths.MOMaths;
import MOMaths.Range;

/////////////////////////////////////////////////////////////////////////////
//
// A Histogram is split into a number of equal bands (sometimes called "bins")
// between the values anticipatedLoVal and anticipatedHiVal
// Values will be clamped between these two extremes
//
// data is stored in its raw form in values.
// it is also arranged into bins of a histogram. using a histogram it is possible to add "fractional" or "weighted" items to the data
// as the population is measured as a floating point
//
//
//
//
public class Histogram {
	int[] populationBands;
	ArrayList<Float> values;
	int numBands;
	float anticipatedLoVal, anticipatedHiVal;
	Range actualValueRange;

	public Histogram(int numBands, float lo, float hi) {
		values = new ArrayList<Float>();
		this.populationBands = new int[numBands];
		this.numBands = numBands;
		this.anticipatedLoVal = lo;
		this.anticipatedHiVal = hi;
		actualValueRange = new Range();
		actualValueRange.initialiseForExtremaSearch();
	}
	
	
	

	public void add(float val) {
		updateActualHiLoVals(val);
		values.add(val);
		int index = (int) (numBands * MOMaths.norm(val, this.anticipatedLoVal, this.anticipatedHiVal));
		index = MOMaths.constrain(index, 0, numBands-1);
		this.populationBands[index] += 1;
	}

	private void updateActualHiLoVals(float val) {
		actualValueRange.addExtremaCandidate(val);
	}

	public Range getActualValueRange() {
		return actualValueRange;
	}
	
	public int getNumBands() {
		return numBands;
	}
	
	public float getMeanValue() {
		int numVals = values.size();
		
		float sum=0;
		for(float f: values) {
			sum+=f;
		}
		return sum/numVals;
	}
	
	public float getMedianValue() {
		return actualValueRange.lerp(0.5f);
	}
	
	public float getModeValue() {
		float largestBandPopulation = 0;
		float valueOfMostPopulousBand = 0;
		for(int n = 0; n < numBands; n++) {
			int numInThisband = populationBands[n];
			if(numInThisband > largestBandPopulation) {
				valueOfMostPopulousBand = getMidValueOfBand(n);
				largestBandPopulation = numInThisband;
			}
		}
		return valueOfMostPopulousBand;
	}
	
	
	public float getStandardDeviation() {
		// 1/ calculate the mean value
		// 2/ for each value calculate the difference from the mean and square it (diff)
		// 3/ sum all the diff values and divide by the number of samples (variance).
		// 4/ sqrt(variance) == standard deviation

		float[] rawVals = getRawValues();
		int numSamples = rawVals.length;
		float sumDiffSqd = 0;
		float mean = getMeanValue();
		for(int n = 0; n<numSamples; n++) {
			float thisVal = rawVals[n];
			float diff = thisVal-mean;
			sumDiffSqd += (diff*diff);
		}
		float variance = sumDiffSqd/numSamples;
		return (float) Math.sqrt(variance);
	}

	public int getTotalNumSamples() {
		return values.size();
	}
	
	float getBandWidth() {
		return (anticipatedHiVal-anticipatedLoVal)/numBands;
	}
	
	float getMidValueOfBand(int n) {
		float midBandOffsetvalue = getBandWidth()/2f;
		float position = (float)n/numBands;
		return MOMaths.lerp(position, anticipatedLoVal, anticipatedHiVal) + midBandOffsetvalue;
	}
	
	float getSummedValueOfBand(int n) {
		float v = getMidValueOfBand(n);
		return v * populationBands[n];
	}
	
	public float[] getRawValues() {
		// copies the data from the values ArrayList to a raw float array
		int num = getTotalNumSamples();
		float[] fvals = new float[num];
		for(int n = 0; n < num;n++) {
			fvals[n] = values.get(n);
		}
		return fvals;
	}
	
	public float[] getValuesFromHistogram() {
		// strips out the data as individual values. These will be the mid values
		// of each band, rather than their original values.
		int totalNum = getTotalNumSamples();
		int index=0;
		float[] histVals = new float[totalNum];
		for(int band=0; band < numBands; band++) {
			int numEntries = populationBands[band];
			float midValue = getMidValueOfBand(band);
			for(int n = 0; n < numEntries; n++) {
				histVals[index] = midValue;
				index++;
				if(index>=totalNum) {
					return histVals;
				}
			}
			
			
		}
				
		return histVals;
		
	}
	

	
	// The following "circular" methods assume a data set in the range 0..1 and give answers in that range
	// The data is normalised before it is used so will work for any data range
	// and are used primarily for Hue calculations, which is circular data.
	// Circular data poses different problems when finding distances between values, as distance in the linear range 0..1 is not necessarily the shortest distance.
	// e.g. In linear data "what is the month between December and February" would give "July"
	// where the answer you are looking for is "January"
	
	public float getCircularMean() {
		float[] rawvals =  getRawValues();
		return getCircularMean(rawvals);
	}
	
	//public float getCircularMeanFromHistogram() {
	//	float[] rawvals =  getValuesFromHistogram();
	//	return getCircularMean(rawvals);
	//}
	
	private float getCircularMean(float[] nomralisedVals)
	{
		// expects normalised values, returns values in the anticipated range
		// hence is private
		// The trick used here is to convert that range to radians then 
		// use trigonometry to add the cumulative angles
		
		float TWOPI = (float) (Math.PI*2);
		float[] vals = converFloatsToRange(nomralisedVals, 0,TWOPI);

		int pop = vals.length;
		
		if(pop<=0) {
			// There should be 1 or more values
			// however if there is no data then return 0
	    	return 0;
		}

	    double sumSin = 0;
	    double sumCos = 0;
	    int counter = 0;

	    for (float bearing : vals)
	    {
	        sumSin += Math.sin(bearing);
	        sumCos += Math.cos(bearing);
	        counter++; 
	    }

	    
	    // now do the trigonometry on the average of the angles
	    double avBearing = Math.atan2(sumSin/counter, sumCos/counter);
	   
	    // now convert back from radian range into the data range
	    // first into range 0..1, and check for negative value
	    avBearing /= TWOPI;
	    if (avBearing<0)  avBearing += 1f;
	    // then back into he original data range
	    return MOMaths.lerp((float)avBearing, anticipatedLoVal, anticipatedHiVal);
	}
	
	private float[] converFloatsToRange(float[] rawvals, float lo, float hi) {
		int num = rawvals.length;
		float[] normvals = new float[num];
		for(int i=0; i<num;i++) {
			float nval = MOMaths.norm(rawvals[i], anticipatedLoVal, anticipatedHiVal);
			normvals[i] = MOMaths.lerp(nval,lo,hi);
		}
		return normvals;
		
	}
	
	public float getCircularStandardDeviation() {
		float[] rawvals =  getRawValues();
		return getCircularStandardDeviation(rawvals);
		
	}
	
	//public float getCircularStandardDeviationFromHistogram() {
	//	float[] histvals =  getValuesFromHistogram();
	//	return getCircularStandardDeviation(histvals);
	//	
	//}
	
	
	private float getCircularStandardDeviation(float[] normalisedVals) {
		// 1/ calculate the mean value
		// 2/ for each value calculate the difference from the mean and square it (diff)
		// 3/ sum all the diff values and divide by the number of samples (variance).
		// 4/ sqrt(variance) == standard deviation

		float[] rawVals = normalisedVals.clone();
		int numSamples = rawVals.length;
		float sumDiffSqd = 0;
		float mean = getCircularMean(normalisedVals);
		for(int n = 0; n<numSamples; n++) {
			float thisVal = rawVals[n];
			float diff = getCircularDiff(thisVal, mean); // losing the sign does not matter
			sumDiffSqd += (diff*diff);
		}
		float variance = sumDiffSqd/numSamples;
		return (float) Math.sqrt(variance);
	}
	
	private float getCircularDiff(float valA, float valB) {
		// expects the full circle range to be 0..1
		float d1 = MOMaths.diff(valA,valB); // looking one way round the circle
		float d2 = MOMaths.diff(valA,valB-1); // looking the other way round the circle
		return Math.min(d1,d2);
	}

}// end Histogram class




