package MOImage;

import java.awt.image.BufferedImage;

import MOMaths.Rect;

//////////////////////////////////////////////////////////
//
//

public class ImageDimensions{
	public int width = 0;
	public int height = 0;

	public ImageDimensions(int w, int h) {
		width = w;
		height = h;

	}
	
	public ImageDimensions(BufferedImage img) {
		width = img.getWidth();
		height = img.getHeight();

	}
	
	public ImageDimensions(FloatImage img) {
		width = img.getWidth();
		height = img.getHeight();

	}

	public Rect getRect() {

		return new Rect(0,0,width,height);

	}
	
	public boolean isLegalIndex(int x, int y) {
		// working with legal indexes into the image. Not quite the same as the rect
		return x >= 0 && x < width && y >= 0 && y < height;
	}
	
	public ImageCoordinate constrain(int x, int y) {
		// working with legal indexes into the image. Not quite the same as the rect
		if( isLegalIndex( x,  y) ) return new ImageCoordinate(x,y);
		if(x >= width) x =  width-1;
		if(x < 0) x = 0;
		if(y >= height) y =  height-1;
		if(y < 0) y = 0;
		
		return new ImageCoordinate(x,y);
		
	}
	
	public String toStr() {
		return "ImageDimensions: " + width + ","+ height;
	}

}

