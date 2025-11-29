package MOImage;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOMaths.MOMaths;
import MOMaths.PVector;
import MOUtils.ImageCoordinateSystem;
////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//
//
//
//
public class BilinearBufferedImageSampler {
	BufferedImage sourceImage;
	int width, height;
	ImageCoordinateSystem coordinateSystem;
	ByteImageGetterSetter greyScaleImageAccess;
	int imageType;

	public BilinearBufferedImageSampler(BufferedImage img) {
		sourceImage = img;
		imageType = sourceImage.getType();
		if(imageType == BufferedImage.TYPE_INT_ARGB || imageType == BufferedImage.TYPE_BYTE_GRAY) {
			//OK
		} else {
			System.out.println("Wrong image type is type " + imageType);
		}

		if(imageType == BufferedImage.TYPE_BYTE_GRAY) {
			greyScaleImageAccess = new ByteImageGetterSetter(img);
		}



		width = sourceImage.getWidth();
		height = sourceImage.getHeight();

		coordinateSystem = new ImageCoordinateSystem(width,height);
	}


	public float getPixelNearest01(float x, float y) {

		return this.getTone01((int)x,(int)y);

	}

	public float getPixelBilin01(PVector docSpace) {

		PVector bufferSpace = coordinateSystem.docSpaceToBufferSpace(docSpace);
		return getPixelBilin01(bufferSpace.x, bufferSpace.y);
	}

	public float getPixelBilin01(float x, float y){
	    // works in image pixel coordinates, but floating point accuracy,

	    // regarding the 4 pixels we are concerned with
	    // A B
	    // C D
	    // ((int)x,(int)y) is the coordinate at the top left of A
	    // B,C and D are ventured into as the floating point component of x and y move between 0...1
	    // This algorithm works out the average Color of them based on the degree of area overlap of each pixel

	    int xLow = (int)x;
	    int yLow = (int)y;
	    float offsetX = x - xLow;
	    float offsetY = y - yLow;

	    int xLowPlus1 = Math.min(xLow+1, width-1);
	    int yLowPlus1 = Math.min(yLow+1, height-1);




	    // get the four pixels
	    float pixelA = this.getTone01(xLow,yLow);

	    // if there is no mantissa, then don't bother to interpolate
	    if(offsetX == 0 && offsetY == 0) {
			return pixelA;
		}

	    float pixelB = this.getTone01(xLowPlus1,yLow);
	    float pixelC = this.getTone01(xLow,yLowPlus1);
	    float pixelD = this.getTone01(xLowPlus1,yLowPlus1);

	    // if they happen to be all the same anyway return the value ...
	    if(pixelA == pixelB && pixelA == pixelC && pixelA == pixelD) {
			return pixelA;
		}

	    // ... otherwise work out the foating point bit of the pixel location


	    // use this work out the overlap for each pixel
	    float amountA = (1-offsetX) * (1-offsetY);
	    float amountB = (offsetX) * (1-offsetY);
	    float amountC = (1-offsetX) * (offsetY);
	    float amountD = (offsetX) * (offsetY);

	    // sanity check that all the areas add up to 1
	    // float sumShouldEqual1 = amountA + amountB + amountC + amountD;
	    // if( !near(sumShouldEqual1,1) ) println("sums = ", sumShouldEqual1);
	    // now average all the red Colors based on their relative amounts in A,B,C & D
	    float aveR = (pixelA*amountA + pixelB*amountB +pixelC*amountC + pixelD*amountD);


	    //println(aveR,aveG,aveB);
	    //System.out.println("offset " + offsetX + "," + offsetY + " = " + (int) (aveR *255));
	    return aveR;

	  }

	public int getAlphaBilin(float x, float y) {
		return (int)( getAlphaBilin01( x,  y) * 255 );
	}

	public float getAlphaBilin01(float x, float y){
	    // works in image pixel coordinates, but floating point accuracy,

	    // regarding the 4 pixels we are concerned with
	    // A B
	    // C D
	    // ((int)x,(int)y) is the coordinate at the top left of A
	    // B,C and D are ventured into as the mantissa of x and y move between 0...1
	    // This algorithm works out the average Color of them based on the degree of area overlap of each pixel

	    int xLow = (int)x;
	    int yLow = (int)y;
	    float offsetX = x - xLow;
	    float offsetY = y - yLow;

	    int xLowPlus1 = Math.min(xLow+1, width-1);
	    int yLowPlus1 = Math.min(yLow+1, height-1);



	    // get the four pixels
	    float alphaA = this.getAlpha01(xLow,yLow);

	    // if there is no mantissa, then don't bother to interpolate
	    if(offsetX == 0 && offsetY == 0) {
			return alphaA;
		}

	    float alphaB = this.getAlpha01(xLowPlus1,yLow);
	    float alphaC = this.getAlpha01(xLow,yLowPlus1);
	    float alphaD = this.getAlpha01(xLowPlus1,yLowPlus1);

	    //System.out.println(alphaA + " " + alphaB + " " + alphaC + " " + alphaD);


	    // if they happen to be all the same anyway return the value ...
	    if(alphaA == alphaB && alphaA == alphaC && alphaA == alphaD) {
			return alphaA;
		}

	    // ... otherwise work out the foating point bit of the pixel location


	    // use this work out the overlap for each pixel
	    float amountA = (1-offsetX) * (1-offsetY);
	    float amountB = (offsetX) * (1-offsetY);
	    float amountC = (1-offsetX) * (offsetY);
	    float amountD = (offsetX) * (offsetY);

	    // sanity check that all the areas add up to 1
	    // float sumShouldEqual1 = amountA + amountB + amountC + amountD;
	    // if( !near(sumShouldEqual1,1) ) println("sums = ", sumShouldEqual1);
	    // now average all the red Colors based on their relative amounts in A,B,C & D
	    float aveR = (alphaA*amountA + alphaB*amountB +alphaC*amountC + alphaD*amountD);




	    return aveR;

	  }


	private float  getTone01(int x, int y) {

		if(imageType == BufferedImage.TYPE_INT_ARGB) {
			int ival = getRGBAClamped(x,  y);
			Color fval = new Color(ival);
			return fval.getRed()/255f;

		} else {
			return getGrayClamped( x,  y)/255f;
		}



	}


	private int getRGBAClamped(int x, int y){
	    x = (int) MOMaths.constrain(x,0f,width-1);
	    y = (int) MOMaths.constrain(y,0f,height-1);
	    return sourceImage.getRGB(x,y);
	  }


	private int getGrayClamped(int x, int y){
	    x = (int) MOMaths.constrain(x,0f,width-1);
	    y = (int) MOMaths.constrain(y,0f,height-1);
	    return greyScaleImageAccess.getPixel(x, y);
	  }

	private float getAlpha01(int x, int y) {

		return getAlpha(x, y) * 0.00392f; // i.e. divided by 255
	}

	private int getAlpha(int x, int y){
		x = (int) MOMaths.constrain(x,0f,width-1);
	    y = (int) MOMaths.constrain(y,0f,height-1);
	    int ci = sourceImage.getRGB(x,y);
	    ///Color c = new Color(ci);
	    int alpha =  MOPackedColor.getAlpha(ci);
		//int alpha =  c.getAlpha();
		//if(alpha > 0 && alpha < 255) System.out.println("x y " + x + "," + y + " source alpha = " + alpha);
		return alpha;
	  }

}
