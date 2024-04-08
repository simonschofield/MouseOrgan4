package MOImage;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOMaths.MOMaths;

public class BilinearBufferedImageSampler {
	BufferedImage sourceImage;
	int width, height;
	
	public BilinearBufferedImageSampler(BufferedImage img) {
		sourceImage = img;
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();
	}
	
	public float getPixelNearest01(float x, float y) {
		
		return this.getClamped01((int)x,(int)y);
		
	}
	
	public float getPixelBilin01(float x, float y){
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
	    float pixelA = this.getClamped01(xLow,yLow);
	    
	    // if there is no mantissa, then don't bother to interpolate
	    if(offsetX == 0 && offsetY == 0) return pixelA;
	    
	    float pixelB = this.getClamped01(xLowPlus1,yLow);
	    float pixelC = this.getClamped01(xLow,yLowPlus1);
	    float pixelD = this.getClamped01(xLowPlus1,yLowPlus1);
	    
	    // if they happen to be all the same anyway return the value ...
	    if(pixelA == pixelB && pixelA == pixelC && pixelA == pixelD) return pixelA;
	    
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
	    
	    return aveR;
	    
	  }
	
	
	private float getClamped01(int x, int y) {
		int ival = getClamped(x,  y);
		Color fval = new Color(ival);
		return fval.getRed()/255f;
	}
	
	
	private int getClamped(int x, int y){
	    x = (int) MOMaths.constrain(x,0f,width-1);
	    y = (int) MOMaths.constrain(y,0f,height-1);
	    return sourceImage.getRGB(x,y);
	  }
	
	
	
}
