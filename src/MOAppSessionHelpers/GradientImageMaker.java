package MOAppSessionHelpers;

import java.awt.image.BufferedImage;

import MOImage.ImageProcessing;
import MOImageCollections.DirectoryFileNameScanner;
import MOImageCollections.ImageAssetGroup;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOScene3D.SceneData3D;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.MOStringUtils;

public class GradientImageMaker {
	
	public static final int HARDLIGHT = 0;
	public static final int MEDIUMLIGHT = 1;
	public static final int MEDIUMSOFTLIGHT = 2;
	public static final int SOFTLIGHT = 3;
	
	
	BufferedImage linearGradientImage;
	BufferedImage blackImage;
	BufferedImage whiteImage;

	public GradientImageMaker() {

		linearGradientImage = ImageProcessing.loadImage(GlobalSettings.getSampleLibPath() + "mask images\\gradients\\gradientUnbiased.png");
		blackImage = ImageProcessing.loadImage(GlobalSettings.getSampleLibPath() + "mask images\\gradients\\black.png");
		whiteImage = ImageProcessing.loadImage(GlobalSettings.getSampleLibPath() + "mask images\\gradients\\white.png");
	}


	
	
	public void testBrightness(BufferedImage testImage) {
		
		String targetDirectory = GlobalSettings.getUserSessionPath() + "testBrightness";
		MOStringUtils.createDirectory(targetDirectory);
		
		
		for(int n = 1; n <= 10; n++) {
			float 	brightness	 = MOMaths.round(n/ 10f, 4);
			BufferedImage gradientImage = makeGradient_SoftLight(brightness);
			BufferedImage blendedImage = ImageProcessing.replaceVisiblePixels(testImage, gradientImage);
			//String paddedNumber = MOStringUtils.getPaddedNumberString(n, 3);
			String fname = targetDirectory + "\\testGradient_"+ brightness + ".png";
						
			ImageProcessing.saveImage(fname, blendedImage);
		
		
		}
		
	}
	
	
	
	public BufferedImage makeGradient(float brightness, int gradientType) {
		if(gradientType==this.HARDLIGHT) {
			return makeGradient_HardLight( brightness);
		}
		if(gradientType==this.MEDIUMLIGHT) {
			return makeGradient_MediumLight( brightness);
		}
		if(gradientType==this.MEDIUMSOFTLIGHT) {
			return makeGradient_MediumLight( brightness);
		}
		if(gradientType==this.SOFTLIGHT) {
			return makeGradient_SoftLight( brightness);
		}
		return null;
		
	}
	
	public BufferedImage makeGradient_HardLight(float brightness) {
		// Lights the very tops with quite a sharp gradient
		
		return makeGradient( brightness, 0.99f, 0.8f, 0.05f, 0.2f);
		

	}
	
	public BufferedImage makeGradient_MediumLight(float brightness) {
		// 
		
		return makeGradient( brightness, 0.99f, 0.75f, 0.1f, 0.35f);
		

	}
	
	public BufferedImage makeGradient_MediumSoftLight(float brightness) {
		// 
		
		return makeGradient( brightness, 0.99f, 0.625f, 0.15f, 0.425f);
		

	}
	
	public BufferedImage makeGradient_SoftLight(float brightness) {
		// blends quite a way down with smoother gradient
		
		return makeGradient( brightness, 0.99f, 0.5f, 0.2f, 0.5f);
		

	}
	
	public BufferedImage makeGradient_VerySoftLight(float brightness) {
		// blends quite a way down with smoother gradient
		
		return makeGradient( brightness, 0.99f, 0.4f, 0.25f, 0.6f);
		

	}


	public BufferedImage makeGradient(float brightness, float midpointDark, float midPointBright, float rangeDark, float rangeBright) {
		// brightness is in the range 0...1
		// as the brightness increases the blend mid point decreases and the range increases so as to provide a smoother blend in dark regions
		// in dark regions the blend mid point increases
		if(brightness < 0.001f) return blackImage;

		float range = 0.4f;
		float midpoint = MOMaths.map(brightness, 0f, 1f, midpointDark, midPointBright);

		// when brightness < 0.66, range == 0.1
		// when brightness > 0.66, range == 0.4
		range = MOMaths.map(brightness,  0,  1, rangeDark, rangeBright);

		float rangeConstrained = MOMaths.constrain(range, 0.05f, 0.9f);



		return makeGradient(rangeConstrained, midpoint);

	}

	public BufferedImage makeGradient(float range, float midpoint) {
		// Range is the proportion of the span which is being blended, so the smaller the range, the tighter the blend
		// Midpoint is the centre of the blend range, where 0 == the very bottom, and 1 = the very top
		// The gamma is always set to 1
		if(midpoint > 0.99) return blackImage;

		float midPoint255 = midpoint*255;
		float halfRange = range*127.5f;
		float shadowVal = MOMaths.constrain(midPoint255-halfRange, 0,255);
		float highlightVal = MOMaths.constrain(midPoint255+halfRange, 0,255);

		return ImageProcessing.adjustLevels(linearGradientImage, shadowVal, 1, highlightVal, 0, 255);

	}




}
