package MOUtils;

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

	public Rect getRect() {

		return new Rect(0,0,width,height);

	}
	
	public String toStr() {
		return width + ","+ height;
	}

}

