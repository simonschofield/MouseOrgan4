package MOImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ByteImageGetterSetter {


	BufferedImage sourceImage;
	int width, height;
	byte[] byteBuffer;

	public ByteImageGetterSetter(BufferedImage img) {
		sourceImage = img;
		if(sourceImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
			System.out.println("Wrong image type, should be TYPE_BYTE_GRAY");
		}
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();

		byteBuffer = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
	}

	public int getWidth() { return width; }
	public int getHeight() { return height;}

	public boolean isInsideImage(int x, int y) {
		return (x >= 0 && x < width && y >= 0 && y < height);
	}
	
	public ImageCoordinate clampToDimensions(int x, int y) {
		if(x < 0) x = 0;
		if(x >= width) x = width-1;
		if(y < 0) y = 0;
		if(y >= height) y = height-1;
		return new ImageCoordinate(x,y);
	}
	
	
	public int getPixelClamped(int x, int y) {
		ImageCoordinate ic =  clampToDimensions( x,  y);
		return getPixel(ic.x, ic.y);
	}

	public int getPixel(int x, int y) {
		
		int loc = x + y * width;
		return (byteBuffer[loc] & 0xFF);
	}
	
	
	public int getPixelBilinear(float xf, float yf) {
		
		// works in image pixel coordinates, but floating point accuracy,

	    // regarding the 4 pixels we are concerned with
	    // A B
	    // C D
	    // ((int)x,(int)y) is the coordinate at the top left of A
	    // B,C and D are ventured into as the floating point component of x and y move between 0...1
	    // This algorithm works out the average Color of them based on the degree of area overlap of each pixel

	    int xLow = (int)xf;
	    int yLow = (int)yf;
	    float offsetX = xf - xLow;
	    float offsetY = yf - yLow;

	    int xLowPlus1 = Math.min(xLow+1, width-1);
	    int yLowPlus1 = Math.min(yLow+1, height-1);




	    // get the four pixels
	    int pixelA = this.getPixelClamped(xLow,yLow);

	    // if there is no mantissa, then don't bother to interpolate
	    if(offsetX == 0 && offsetY == 0) {
			return pixelA;
		}

	    int pixelB = this.getPixelClamped(xLowPlus1,yLow);
	    int pixelC = this.getPixelClamped(xLow,yLowPlus1);
	    int pixelD = this.getPixelClamped(xLowPlus1,yLowPlus1);

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
	    return (int)aveR;

	}


	public void setPixel(int x, int y, int val) {
		int loc = x + y * width;
		byteBuffer[loc] = (byte) val;
	}
	
	public int getPixelFromNormalisedCoordinate(float xn, float yn) {
		//int x = (int)(xn*width);
		//int y = (int)(yn*height);
		//if( isInsideImage( x, y) ) return getPixel(x,y);
		//return 0;
		float x = xn*width;
		float y = yn*height;
		return getPixelBilinear(x,y);
		
	}
	
	
	


}
