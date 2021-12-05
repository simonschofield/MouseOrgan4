package MOImageClasses;
import java.awt.image.BufferedImage;
import java.util.Arrays;

// builds a tonal histogram of the image
// while the class expects an ARGB type image, it uses the green channel exclusively for the tonal data
// so make sure the image is "grey scaled" before submitting
public class ImageHistogram {
	// this stores the number of pixel in the image mapping to each of the 256 tones
	int[] histogramData = new int[256];
	
	// when the bands are created, key values are made based on popularity or even-split
	int[] bandKeyValues;
	// stores the index number of the band in bandkeyValues against each value 0..255, so is a LUT
	int[] bandKeyValuesLUT = new int[256];
	ImageHistogram(BufferedImage img){
		
		for(int y=0; y < img.getHeight(); y++) {
			for(int x = 0; x < img.getWidth(); x++) {
				int c = img.getRGB(x, y);
				int val = ImageProcessing.getChannelFromARGB(c,2);
				histogramData[val]++;
			}
		}
		
		
		
	}
	
	
	
	void createBandsOnPopularity(int num) {
		// finds the num most populous values, stores these in bandKeyValues, sorts them lo..ho
		// then creates a LUT (bandKeyValuesLUT) for all values (0..255) storing the index of the closest bandKeyValue
		//
		bandKeyValues = new int[num];
		int histCopy[] = histogramData.clone(); 
		// find the top ranking num values
		for(int n = 0; n < num; n++) {
			int ind = findIndexOfLargestEntry(histCopy);
			bandKeyValues[n] = ind;
			histCopy[ind] = 0;
		}
		
		
		// so now we have the num-most popular values, but not in the right order lo-hi
		// to sort into lo to hi...
		Arrays.sort(bandKeyValues);
		
		for(int i = 0; i < 256; i++) {
			bandKeyValuesLUT[i] = calculateBandIndexFromKeyValues(i);
			//System.out.println("bandKeyValuesLUT i " + i + " is " + bandKeyValuesLUT[i]);
		}
		
	}
	
	int getNumberOfBands() {
		return bandKeyValues.length;
	}
	
	int getBandNum(int pixelVal) {
		return bandKeyValuesLUT[pixelVal];
	}
	
	// don't really need this but just to show how it works
	int getBandValue(int bandNum) {
		return bandKeyValues[bandNum];
	}
	
	
	
	
	
	
	private int findIndexOfLargestEntry(int[] a) {
		int ind = -1;
		int largest = -1;
		for(int i = 0; i < a.length; i++) {
			int val = a[i];
			if(val > largest) {
				largest = val;
				ind = i;
			}
		}
		return ind;
	}
	
	
	private int calculateBandIndexFromKeyValues(int pixelVal) {
		// finds which of the quantisedValueGroups this pixelValue is nearest to, and returns that index number
		int currentIndex = 0;
		int closestIndex = -1;
		int proximity = 1000000000;
		for(int thisVal : bandKeyValues) {
			int dif = Math.abs(thisVal-pixelVal);
			if(dif < proximity) {
				proximity = dif;
				closestIndex = currentIndex;
			}
			currentIndex++;
		}
		return closestIndex;
	}
	
}
