package MOUtils;

import MOMaths.Rect;

//////////////////////////////////////////////////////////
//
//

public class ImageDimensions{
	int width = 0;
	int height = 0;

	public ImageDimensions(int w, int h) {
		width = w;
		height = h;

	}

	public Rect getRect() {

		return new Rect(0,0,width,height);

	}

}

