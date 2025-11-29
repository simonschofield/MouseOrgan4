package MOImage;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import MOMaths.MOMaths;


public class BendImage {


    int lutSize;
    float bendLUT[];




    // bend the image left or right: negative displacementInX bends Left, while positive displacementInX bends right.
    public BufferedImage bendImage(BufferedImage source, float bendStart, float displacementInX, float bendHarshness) {
    	if(displacementInX<0) {
    		return bendImageLeft( source,  bendStart,  -displacementInX,  bendHarshness);
    	}
    	return bendImageRight( source,  bendStart,  displacementInX,  bendHarshness);

    }

    BufferedImage bendImageLeft(BufferedImage source, float bendStart, float displacementInX, float bendHarshness) {
    	BufferedImage flippedImage = ImageProcessing.mirrorImage(source, true, false);
    	BufferedImage bentflippedImage = bendImageRight(flippedImage,  bendStart,  displacementInX,  bendHarshness);
    	return ImageProcessing.mirrorImage(bentflippedImage, true, false);
    }

    BufferedImage bendImageRight(BufferedImage source, float bendStart, float displacementInX, float bendHarshness) {
    	//
    	// bendStart is always a parametric 0..1, where 0 is the bottom of the image
    	// displacementInX, 0 == no displacement, 1 == the displacement is equivalent to the image height
    	// which would be huge, but by yoking the displacement to the height, we get a consistent result across
    	// images from the same ContentGroup (same height, but different widths). It cannot be less than the current width
    	// The bend harshness is the gamma applied to the curve. 1.2 == very gentle curve over the length of the image, 10.0 == very harsh curve at the end of the image

    	// the LUT will contain the actual pixel displacement amounts
    	int sourceImageHeight = source.getHeight();
    	int sourceImageWidth = source.getWidth();
    	float pixelDisplacement = sourceImageHeight * displacementInX;
    	createBendLUT(sourceImageHeight,  bendStart,  pixelDisplacement,  bendHarshness);

    	// the first entry in the LUT contains the greatest displacement amount, so use this to calculate the new image width
    	int bentImageWidth = (int) (sourceImageWidth + bendLUT[0] + 1);
    	BufferedImage bentImage = new BufferedImage(bentImageWidth, sourceImageHeight, source.getType());

    	// now copy over the y-strips of image, displacing them all
    	Graphics2D g2d = bentImage.createGraphics();

    	boolean antiAlias = true;
    	if( ImageProcessing.getInterpolationQuality()==0 ) {
			antiAlias = false;
		}

    	for(int y = 0; y < sourceImageHeight; y++) {
    		float displacement = bendLUT[y];

    		int dx1 = (int) displacement;
    		int dy1 = y;
    		int dx2 = dx1+sourceImageWidth-1;
    		int dy2 = dy1+1;

    		int sx1 = 0;
    		int sy1 = y;
    		int sx2 = sourceImageWidth-1;
    		int sy2 = sy1+1;

    		if(antiAlias) {

    			float remainder = displacement-dx1;

	    		// leading edge first (x-1)
				// This decreases in strength as the pixel x remainder goes from 0..1
	    		AlphaComposite src_over_leading = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1-remainder);
	    		g2d.setComposite(src_over_leading);
	    		g2d.drawImage(source, dx1-1, dy1, dx2-1, dy2, sx1, sy1, sx2, sy2, null);


	    		// main "middle" bit using neighbour translation
	    		AlphaComposite src_over_main = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
	    		g2d.setComposite(src_over_main);
	    		g2d.drawImage(source, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);


	    		// trailing edge last (x+1)
	    		// This builds in strength as the pixel x remainder goes from 0..1
	    		AlphaComposite src_over_trailing = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, remainder);
	    		g2d.setComposite(src_over_trailing);
	    		g2d.drawImage(source, dx1+1, dy1, dx2+1, dy2, sx1, sy1, sx2, sy2, null);
    		} else {

    			g2d.drawImage(source, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);

    		}


    	}

    	return bentImage;
    }

    //////////////////////////////////////////////////////////////////////
    // private
    //

	private void  createBendLUT(int numEntries, float bendStart, float displacementInX, float bendHarshness) {

	    	bendLUT = new float[numEntries];
	    	lutSize = numEntries;
	    	for(int i = 0; i < lutSize; i++) {
	    		float p = i/(float)lutSize;
	    		bendLUT[i]  = bendLine(p,  bendStart,  displacementInX,  bendHarshness);
	    	}
	    }


	private float  bendLine(float y, float bendStart, float displacementInX, float bendHarshness){
    	// given a point on a line v (0..1), create a bend
    	// starting at bendStart (0..1), and ending at 1
    	// The total displacement in x is displacementInX. Otherwise the result is always in range 0..1, with the final value being 1
    	// The bend harshness is the gamma applied to the curve. 1.2 == very gentle curve, 10.0 == very harsh curve

    	// first, invert the y, so that it bends the top of the image, not the bottom
    	y = 1-y;
    	if( y < bendStart) {
			return 0;
		}

    	float p = MOMaths.norm(y, bendStart, 1.0f);

    	float benddist = (float) Math.pow(p, bendHarshness);

    	return (benddist*displacementInX);
    	}



}








































