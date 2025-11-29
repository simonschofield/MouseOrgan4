package MOImageCollections;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import MOImage.ImageProcessing;
import MOImage.MOPackedColor;
import MOUtils.Histogram;

public class ImageStats {
	Histogram hueHistogram;
	Histogram satHistogram;
	Histogram valHistogram;
	Histogram colorPresenceHistogram;



	public float dominantHue;
	public float meanBrightness;
	public float colourfulness;
	public float contrast;

	public ImageStats(BufferedImage img) {
		calculateImageStats(img);
	}


	public void calculateImageStats(BufferedImage fullsizeImage) {
		// all input values operate in the range 0..1, with h having its own wrap-around for numbers outside of 0..1

		int w = Math.min(100, fullsizeImage.getWidth());
		int h= Math.min(100, fullsizeImage.getHeight());
		BufferedImage img = ImageProcessing.resizeTo(fullsizeImage,w,h);

		img = ImageProcessing.assertImageTYPE_INT_ARGB(img);
		//System.out.println("AFTER adjustHSV incoming image is of type " + img.getType() + " BufferedImage.TYPE_INT_ARGB is " + BufferedImage.TYPE_INT_ARGB);
		int[] pixelsIn = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

		float[] hsv = new float[4];
		int[] unpacked = new int[4];

		hueHistogram = new Histogram(255,0,1);
		satHistogram = new Histogram(255,0,1);
		valHistogram = new Histogram(255,0,1);
		colorPresenceHistogram  = new Histogram(255,0,1);


		for (int element : pixelsIn) {

			MOPackedColor.unpackARGB(element, unpacked);

			// if alpha is 0 then we don't need to process this pixel
			if (unpacked[0] == 0) {
				continue;
			}

			// this converts the rgb into the array hsv
			Color.RGBtoHSB(unpacked[1], unpacked[2], unpacked[3], hsv);

			float hue = hsv[0];
			float sat = hsv[1];
			float val = hsv[2];
			hueHistogram.add(hue);
			satHistogram.add(sat);
			valHistogram.add(val);

			// hues with string saturation get multiple entries, and are therefore weighted as more significant
			// compared to hues with low saturations
			if(sat > 0.9f) {
				colorPresenceHistogram.add(hue);
			}
			if(sat > 0.8f) {
				colorPresenceHistogram.add(hue);
			}
			if(sat > 0.7f) {
				colorPresenceHistogram.add(hue);
			}
			if(sat > 0.6f) {
				colorPresenceHistogram.add(hue);
			}
			if(sat > 0.5f) {
				colorPresenceHistogram.add(hue);
			}
			if(sat > 0.4f) {
				colorPresenceHistogram.add(hue);
			}
			if(sat > 0.3f) {
				colorPresenceHistogram.add(hue);
			}
			if(sat > 0.2f) {
				colorPresenceHistogram.add(hue);
			}
			if(sat > 0.1f) {
				colorPresenceHistogram.add(hue);
			}
			colorPresenceHistogram.add(hue);

		}

		// Hue
		// 0.0    is equivalent to red
		// 0.1666 is equivalent to yellow
		// 0.3333 is equivalent to green
		// 0.5    is equivalent to cyan
		// 0.6666 is equivalent to blue
		// 0.8333 is equivalent to magenta
		// 0.9999 is equivalent to red

		dominantHue = colorPresenceHistogram.getCircularMean();
		//float hueVariance = getHueStats().getCircularStandardDeviationFromRawVals();
		//float satMean = stats.getSatStats().getMeanValue();

		meanBrightness = getValStats().getMeanValue();
		float brightessVariety = getValStats().getStandardDeviation();
		contrast = brightessVariety*2;


		colourfulness = colorPresenceHistogram.getCircularStandardDeviation()*50;




	}

	public void printStats(String assetName ) {

		System.out.println(" image " + assetName + " has a Hue mean of " + dominantHue + ", an overall brightness of " + meanBrightness + ", a contrast of " + contrast + ", an apparent colourfulness of " + colourfulness );
	}

	public Histogram getHueStats() {
		return hueHistogram;
	}

	public Histogram getSatStats() {
		return satHistogram;
	}

	public Histogram getValStats() {
		return valHistogram;
	}



}
