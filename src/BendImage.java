import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public class BendImage {

    static BufferedImage image;
    int lutSize;
    float bendLUT[];
    
    void createBendLUT(int numEntries, float bendStart, float displacementInX, float bendHarshness) {
    	
    	bendLUT = new float[numEntries];
    	lutSize = numEntries;
    	for(int i = 0; i < lutSize; i++) {
    		float p = i/(float)lutSize;
    		bendLUT[i] = bendLine(p,  bendStart,  displacementInX,  bendHarshness);
    	}
    }
    
    float bendLine(float y, float bendStart, float displacementInX, float bendHarshness){
    	// given a point on a line v (0..1), create a bend
    	// starting at bendStart (0..1), and ending at 1
    	// The total displacement in x is displacementInX. Otherwise the result is always in range 0..1, with the final value being 1
    	// The bend harshness is the gamma applied to the curve. 1.2 == very gentle curve, 10.0 == very harsh curve
    	
    	// first, invert the y, so that it bends the top of the image, not the bottom
    	y = 1-y;
    	if( y < bendStart) return 0;
    	  
    	float p = MOMaths.norm(y, bendStart, 1.0f);
    	  
    	float benddist = (float) Math.pow(p, bendHarshness);

    	return (benddist*displacementInX);
    	}

    BufferedImage bendImage(BufferedImage source, float bendStart, float displacementInX, float bendHarshness) {
    	//
    	// bendStart is always a parametric 0..1, where 0 is the bottom of the image
    	// displacementInX, 0 == no displacement, 1 == the displacement is equivalent to the image height
    	// which would be huge, but by yoking the displacement to the height, we get a consistent result across
    	// images from the same ContentGroup (same height, but different widths)
    	//
    	
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
    	if( ImageProcessing.getInterpolationQuality()==0 ) antiAlias = false;
    	
    	for(int y = 0; y < sourceImageHeight; y++) {
    		int dx1 = (int) bendLUT[y];
    		float remainder = bendLUT[y]-dx1;
    		int dy1 = y;
    		int dx2 = dx1+sourceImageWidth-1;
    		int dy2 = dy1+1;
    		
    		int sx1 = 0;
    		int sy1 = y;
    		int sx2 = sourceImageWidth-1;
    		int sy2 = sy1+1;
    		
    		// nearest neighbour translation
    		AlphaComposite src_over_main = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
    		g2d.setComposite(src_over_main);
    		g2d.drawImage(source, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    		
    		// applying anti-alias
    		if(antiAlias) {
    			// the principle of writing the weakest image last applies, so that it is not overwritten
    			if(remainder<0.5) {
	    			// leading edge first (x-1)
	    			// This decreases in strength as the pixel x remainder goes from 0..1
		    		AlphaComposite src_over_leading = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1-remainder);
		    		g2d.setComposite(src_over_leading);
		    		g2d.drawImage(source, dx1-1, dy1, dx2-1, dy2, sx1, sy1, sx2, sy2, null);
	    			
	    			
		    		// trailing edge last (x+1)
		    		// This builds in strength as the pixel x remainder goes from 0..1
		    		AlphaComposite src_over_trailing = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, remainder);
		    		g2d.setComposite(src_over_trailing);
		    		g2d.drawImage(source, dx1+1, dy1, dx2+1, dy2, sx1, sy1, sx2, sy2, null);
	    			}
    			
    			if(remainder>=0.5) {

    	    		// trailing edge first (x+1)
    	    		// This builds in strength as the pixel x remainder goes from 0..1
    	    		AlphaComposite src_over_trailing = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, remainder);
    	    		g2d.setComposite(src_over_trailing);
    	    		g2d.drawImage(source, dx1+1, dy1, dx2+1, dy2, sx1, sy1, sx2, sy2, null);
    	    		
    	    		// leading edge last (x-1)
        			// This decreases in strength as the pixel x remainder goes from 0..1
    	    		AlphaComposite src_over_leading = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1-remainder);
    	    		g2d.setComposite(src_over_leading);
    	    		g2d.drawImage(source, dx1-1, dy1, dx2-1, dy2, sx1, sy1, sx2, sy2, null);
        			}
    			
    			
    		
    		
    		}
    		
    	}

    	return bentImage;
    }

   
}








































/*import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.util.Hashtable;

import javax.media.jai.InterpolationBilinear;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.Warp;
import javax.media.jai.WarpGrid;
import javax.swing.*;

public class WarpImage {
	
	
	
	public BufferedImage TestWarp (BufferedImage wibble)
    {
		RenderedOp srcImage = JAI.create("fileload", new ParameterBlock().add("C:\\sample lib\\wild grass\\BACKUP mixed green meadow grass upright_10000 - Copy\\scan0000 new.png"), null);
        //        srcImage = PictureLoader.loadImages(new File(path), null)
        //                                .get(1);
        //        srcImage = buildPattern(20, 10, 50, 50);
        //Dimension dimension = new Dimension(srcImage.getWidth(), srcImage.getHeight());
        //setPreferredSize(dimension);

        //        float[]        xCoeffs = new float[] { 0f, 1.25f, 0.04f };
        //        float[]        yCoeffs = new float[] { 0f, -0.02f, 1.5f };
        //        Warp           warp = new WarpAffine(xCoeffs, yCoeffs);
        //
        int xStep = 500;
        int xNumCells = 2;
        int yStep = 500;
        int yNumCells = 1;
        float[] warpPositions = new float[]{
            -100f, 0f, 500f, 100f, 1000f, 0f, // top line
            0f, 500f, 500f, 500f, 1000f, 500f
        }; // bot line
        Warp warp = new WarpGrid(0, xStep, xNumCells, 0, yStep, yNumCells, warpPositions);
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(invert(srcImage));
        pb.add(warp);
        pb.add(new InterpolationBilinear());
        RenderedImage dstImage = invert(JAI.create("warp", pb));
        
        return renderedImageToBufferedImage(dstImage);
    }
	
	private RenderedImage invert (RenderedImage image)
    {
        return JAI.create(
                "Invert",
                new ParameterBlock().addSource(image).add(null).add(null).add(null).add(null).add(null),
                null);
    }
	
	
	RenderedImage bufferedImageToRenderedImage(BufferedImage bufferedImage) {
		//Graphics2D g=(Graphics2D)bufferedImage.getGraphics();
		return bufferedImage;
		//g.drawRenderedImage(image,null);
		
	}
	
	public BufferedImage renderedImageToBufferedImage(RenderedImage img) {
	    if (img instanceof BufferedImage) {
	        return (BufferedImage)img;  
	    }   
	    ColorModel cm = img.getColorModel();
	    int width = img.getWidth();
	    int height = img.getHeight();
	    WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
	    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	    Hashtable properties = new Hashtable();
	    String[] keys = img.getPropertyNames();
	    if (keys!=null) {
	        for (int i = 0; i < keys.length; i++) {
	            properties.put(keys[i], img.getProperty(keys[i]));
	        }
	    }
	    BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
	    img.copyData(raster);
	    return result;
	}
	
	
	
	

}

*/
