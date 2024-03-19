package MOImage;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.DataBufferInt;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;


import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Rect;
import MOUtils.Histogram;


/**
 * @author cmp3schofs
 *
 */
/**
 * @author cmp3schofs
 *
 */
/**
 * @author cmp3schofs
 *
 */
public class ImageProcessing {


	// 0 == nearest neighbour
	// 1 == bilinear
	// 2 == bicubic
	public static final int	INTERPOLATION_NEARESTNEIGHBOR = 0;
	public static final int	INTERPOLATION_BILINEAR = 1;
	public static final int	INTERPOLATION_BICUBIC = 2;

	


	static int interpolationQuality = 2;
	static int interpolationQualityRestore = interpolationQuality;
	
	
	public static void setInterpolationQuality(int q) {
		// this is set by the user
		interpolationQualityRestore = interpolationQuality;
		interpolationQuality = q;
	}

	public static int getInterpolationQuality() {
		return interpolationQuality;
	}

	public static void restoreInterpolationQuality() {
		interpolationQuality = interpolationQualityRestore;
	}

	private static void setInterpolationQuality(Graphics2D g) {
		// this is called during all scale and rotation functions and sets the interpolation to the current global interpolation scheme
		if (interpolationQuality == INTERPOLATION_NEARESTNEIGHBOR) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}
		if (interpolationQuality == INTERPOLATION_BILINEAR) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}
		if (interpolationQuality == INTERPOLATION_BICUBIC) {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		}


		//System.out.println("interpolation quality " + interpolationQuality);

	}



	public static BufferedImage loadImage(String pathAndName) {

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(pathAndName));
		} catch (IOException e) {
			System.out.println("loadImage: cannot load = " + pathAndName + " " + e.getMessage());

		}

		//System.out.println("in loadImage image type = " + img.getType());
		//System.out.println("in loadImage image color model = " + img.getColorModel());

		return img;
	}




	public static void saveImage(String pathAndName, BufferedImage img) {
		try {
			// retrieve image
			File outputfile = new File(pathAndName);
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			System.out.println("saveImage: cannot save = " + pathAndName);
		}

	}

	public static BufferedImage copyImage(BufferedImage source) {
		BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		Graphics g = b.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return b;
	}

	public static BufferedImage createEmptyCopy(BufferedImage source) {
		return new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	}


	public static boolean hasAlpha(BufferedImage image) {
		/* These are the Java Image Types
		public static final int TYPE_CUSTOM = 0;
	    public static final int TYPE_INT_RGB = 1;
	    public static final int TYPE_INT_ARGB = 2;
	    public static final int TYPE_INT_ARGB_PRE = 3;
	    public static final int TYPE_INT_BGR = 4;
	    public static final int TYPE_3BYTE_BGR = 5;
	    public static final int TYPE_4BYTE_ABGR = 6;
	    public static final int TYPE_4BYTE_ABGR_PRE = 7;
	    public static final int TYPE_USHORT_565_RGB = 8;
	    public static final int TYPE_USHORT_555_RGB = 9;
	    public static final int TYPE_BYTE_GRAY = 10;
	    public static final int TYPE_USHORT_GRAY = 11;
	    public static final int TYPE_BYTE_BINARY = 12;
	    public static final int TYPE_BYTE_INDEXED = 13;
		 */
		int t = image.getType();
		if(t==2 || t==3 || t==6 || t==7) return true;
		return false;
	}

	public static BufferedImage assertImageTYPE_INT_ARGB(BufferedImage source) {
		// this is the preferred color model for the MouseOrgan system for output images and content items
		if(source.getType() != BufferedImage.TYPE_INT_ARGB) {
			return ImageProcessing.convertColorModel(source, BufferedImage.TYPE_INT_ARGB);
		}		
		return source;
	}

	public static boolean isSameDimensions(BufferedImage imageA, BufferedImage imageB) {
		return (imageA.getWidth() == imageB.getWidth() && imageA.getHeight() == imageB.getHeight());
	}

	public static Rect getImageBufferRect(BufferedImage img) {
		return new Rect(0,0,img.getWidth(), img.getHeight());
	}

	public static BufferedImage convertColorModel(BufferedImage src, int colorModel) {
		BufferedImage img= new BufferedImage(src.getWidth(), src.getHeight(), colorModel);
		Graphics2D g2d = img.createGraphics();

		g2d.drawImage(src, 0, 0, null);
		g2d.dispose();
		return img;
	}

	public static BufferedImage cropImage(BufferedImage src, Rect r) {
		// returns a sub image, but shares the same image buffer data as the src image
		int wid = Math.min(src.getWidth(), (int)r.getWidth());
		int hig = Math.min(src.getHeight(), (int)r.getHeight());

		BufferedImage subImg = src.getSubimage((int)r.left,(int)r.top, wid, hig);
		return subImg;  
	}


	public static BufferedImage deepCropImage(BufferedImage src, Rect r) {
		//creates a completely independent cropped image
		BufferedImage shallowCrop = cropImage( src,  r);
		return copyImage(shallowCrop);
	}


	public static BufferedImage cropImageWithNormalisedRect(BufferedImage src, Rect r) {
		int w = src.getWidth();
		int h = src.getHeight();
		Rect pixelCropRect = new Rect(new PVector(r.left*w, r.top*h) , new PVector(r.right*w, r.bottom*h));
		return cropImage(src, pixelCropRect);
	}


	public static BufferedImage addBoarder(BufferedImage img, int left, int top, int right, int bottom) {
		// adds a transparent boarder around an image. Units are in pixels.
		int newWidth = left + img.getWidth() + right;
		int newHeight = top + img.getHeight() + bottom;
		BufferedImage imgOut = new BufferedImage(newWidth, newHeight, img.getType());
		compositeImage_ChangeTarget(img, imgOut, left, top, 1);
		return imgOut;
	}


	public static BufferedImage clearImage(BufferedImage img) {
		Color blank = new Color(0,0,0,0);
		return fill(img,blank); 
	}

	public static BufferedImage fill(BufferedImage img, Color c) {
		BufferedImage imgCopy = createEmptyCopy(img);
		Graphics2D g2d = imgCopy.createGraphics();
		g2d.setBackground(c);
		g2d.clearRect(0, 0, imgCopy.getWidth(), imgCopy.getHeight());
		return imgCopy;
	}




	///////////////////////////////////////////////////////////////////////////////////////
	// compositing operations using SRC_OVER (the classic paste), preserves alpha of source
	// and contributes with grater alpha
	public static void compositeImage_ChangeTarget(BufferedImage source, BufferedImage target, int x, int y, float alpha) {
		compositeImage_ChangeTarget( source,  target,  x,  y,  alpha, AlphaComposite.SRC_OVER);
	}

	public static void compositeImage_ChangeTarget(BufferedImage source, BufferedImage target, int x, int y, float alpha, int mode){
		Graphics2D g2d= target.createGraphics();
		AlphaComposite src_over = AlphaComposite.getInstance(mode, alpha);
		g2d.setComposite(src_over);
		g2d.drawImage(source, x, y, null);
		g2d.dispose();
	}


	public static BufferedImage getCompositeImage(BufferedImage source, BufferedImage target, int x, int y,float alpha) {
		return  getCompositeImage( source,  target,  x,  y, alpha, AlphaComposite.SRC_OVER);
	}

	public static BufferedImage getCompositeImage(BufferedImage source, BufferedImage target, int x, int y,float alpha, int mode) {
		BufferedImage target_copy = copyImage(target);
		compositeImage_ChangeTarget( source,  target_copy,  x,  y,  alpha, mode);
		return target_copy;
	}



	///////////////////////////////////////////////////////////////////////////////////////
	// mask compositing operations
	//


	/**
	 * Used to "chop-out" bits of image (the source) using the alpha of another image (the mask), so the source image is eroded. Probably works best if both images are
	 * already the same size, but can handle differently sized images.  
	 * @param source - the image to be alpha-cropped. This remains unchanged by the method, a copy is returned
	 * @param mask -  the image containing the alpha mask to be applied to the source image
	 * @param x - an offset of the mask
	 * @param y - an offset of the mask
	 * @param compositeMode - 	AlphaComposite.DST_OUT preserves the parts of source which are overlaid by transparent alpha values in the mask, 
	 * 							AlphaComposite.DST_IN preserves those parts under the solid parts of the mask
	 * @return - a copy the alpha-cropped source image
	 */
	public static BufferedImage getMaskedImage(BufferedImage source,  BufferedImage mask, int x, int y, int compositeMode) {
		// used by RenderBoarder to mask out parts of the image using the bespoke crop images
		//
		// AlphaComposite.DST_OUT preserves the parts of source which are overlaid by transparent alpha values
		// AlphaComposite.DST_IN preserves those parts under the solid parts of the mask
		BufferedImage source_copy = copyImage(source);
		Graphics2D g2d= source_copy.createGraphics();

		AlphaComposite src_in = AlphaComposite.getInstance(compositeMode, 1.0f);
		g2d.setComposite(src_in);
		g2d.drawImage(mask, x, y, null);

		g2d.dispose();
		return source_copy;
	}
	
	/**
	 * Used to "chop-out" bits of image (the source) using the alpha of another image (the mask), so the source image is eroded. Size the mask to be the same size as the source
	 * then applies AlphaComposite.DST_OUT, to erode those parts in the source which are solid in the mask
	 * @param source - the image to be alpha-cropped. This remains unchanged by the method, a copy is returned
	 * @param mask -  the image containing the alpha mask to be applied to the source image
	 * @param extraOverlap -  to avoid edge pixels in the source remaining uncropped by a resize of the mask, we suggest using a border of around 4 pixels
	 
	 * @return - a copy the alpha-cropped source image
	 */
	public static BufferedImage getMaskedImage(BufferedImage source,  BufferedImage mask, int extraOverlap) {
		
		
		int newMaskWidth = (int)source.getWidth()+extraOverlap*2;
		int newMaskHeight = (int)source.getHeight()+extraOverlap*2;
		BufferedImage resizedMask = resizeTo(mask, newMaskWidth, newMaskHeight);
		return getMaskedImage(source,  resizedMask, -extraOverlap, -extraOverlap, AlphaComposite.DST_OUT);
	}



	/**
	 * replaces the visible pixels in the source image with those in the toReplace image
	 * The toReplace image is resized to the same image dimensions of the source
	 * @param source - the sprite-type image with alpha
	 * @param toReplace - an image, probably without alpha, containing the pixels to be "mapped" onto the source image
	 * @return - an alpha-d image with its visible pixels changed
	 */
	public static BufferedImage replaceVisiblePixels(BufferedImage source,  BufferedImage toReplace) {

		int newMaskWidth = (int)source.getWidth();
		int newMaskHeight = (int)source.getHeight();
		BufferedImage resizedMask = resizeTo(toReplace, newMaskWidth, newMaskHeight);

		return getMaskedImage(source,  resizedMask, 0, 0, AlphaComposite.SRC_IN);

	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// Used in masked effects
	// The idea is that you have a greyscale mask that represents the parts of the 
	// source image you want to affect.The greysale mask can be made any size, and is resized to match the source image.
	// The mask can be any image format, as it is converted to TYPE_INT_ARGB before use. (The green channel is used)
	// This method returns this masked image. You submit the masked-image to the effect, and
	// then re-merge it with the original image.
	//
	static public BufferedImage extractImageUsingGrayscaleMask(BufferedImage sourceImage, BufferedImage mask) {
		// returns a copy of imageIn preserving the parts of the imageIn which are in the lighter regions of the mask
		// Those in the dark mask regions are alpha-ed out
		// i.e. new alpha = min(maskAlpha, exisitingAlpha);
		//System.out.println("sourceImage " + sourceImage.getWidth() + "," + sourceImage.getHeight());
		//System.out.println("mask " + mask.getWidth() + "," + mask.getHeight());
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();

		if( isSameDimensions(sourceImage, mask) == false ) {
			mask = resizeTo(mask, width, height);
		}

		sourceImage = assertImageTYPE_INT_ARGB(sourceImage);
		mask = assertImageTYPE_INT_ARGB(mask);

		int[] sourceImagePixels = ((DataBufferInt) sourceImage.getRaster().getDataBuffer()).getData();
		int[] maskPixels = ((DataBufferInt) mask.getRaster().getDataBuffer()).getData();

		BufferedImage imageOut = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		int[] imageOutPixels = ((DataBufferInt) imageOut.getRaster().getDataBuffer()).getData();

		//System.out.println("sourceImagePixels " + sourceImagePixels.length);
		//System.out.println("maskPixels " + maskPixels.length);
		//int length = width*height;
		for (int i = 0; i < sourceImagePixels.length; i++) {

			int[] exisitingCol = MOPackedColor.unpackARGB(sourceImagePixels[i]);
			int maskAlpha = MOPackedColor.getGreen(maskPixels[i]); 
			int newAlpha = Math.min(exisitingCol[0], maskAlpha);
			imageOutPixels[i] = MOPackedColor.packARGB(newAlpha, exisitingCol[1], exisitingCol[2], exisitingCol[3]);
		}

		return imageOut;
	}




	// new tbd
	// not used but demonstrates use of BandCombineOp
	private BufferedImage toAlpha(BufferedImage src) {
		/*
	        This matrix specifies which band(s) to manipulate and how. The 3-ones
	        in the right-most column sets dst's RGB bands to white. The solitary
	        one in the bottom row will copy the green band into dst's alpha band.

	        Footnote: when the grayscale was converted to ARGB I expected the RGB
	        bands to be identical. After some testing the bands were found to be
		 *near* identical; it seems a color conversion from grayscale to 
	        RGB is not as simple as a bulk memory copy. The differences are low 
	        enough that no one would've noticed a difference had the either the 
	        red or blue band been chosen over the green band.
		 */
		final float[][] matrix = new float[][] {
			{0, 0, 0, 1},
			{0, 0, 0, 1},
			{0, 0, 0, 1},
			{0, 1, 0, 0}};

			BandCombineOp op = new BandCombineOp(matrix, null);
			BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
			op.filter(src.getRaster(), dst.getRaster());
			return dst;
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////
	// value get 
	//	
	public static float getValue01Clamped(BufferedImage src, int x, int y) {
		x = MOMaths.constrain(x, 0, src.getWidth()-1);
		y = MOMaths.constrain(y, 0, src.getHeight()-1);
		return getValue01( src,  x,  y);
	}
	static float getValue01(BufferedImage src, int x, int y) {
		int packedCol = src.getRGB(x, y);
		boolean hasAlpha = hasAlpha(src);
		return MOPackedColor.packedIntToVal01( packedCol,  hasAlpha);
	}



	


	/////////////////////////////////////////////////////////////////////////////////////////////////
	// Geometric transforms
	//	
	public static BufferedImage rotateImage(BufferedImage originalImage, float degree) {
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		double toRad = Math.toRadians(degree);
		int hPrime = (int) (w * Math.abs(Math.sin(toRad)) + h * Math.abs(Math.cos(toRad)));
		int wPrime = (int) (h * Math.abs(Math.sin(toRad)) + w * Math.abs(Math.cos(toRad)));

		BufferedImage rotatedImage = new BufferedImage(wPrime, hPrime, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = rotatedImage.createGraphics();

		setInterpolationQuality(g);

		// System.out.println("Current rendering hint is " + g.getRenderingHints());

		g.translate(wPrime / 2, hPrime / 2);
		g.rotate(toRad);
		g.translate(-w / 2, -h / 2);
		g.drawImage(originalImage, 0, 0, null);
		g.dispose(); // release used resources before g is garbage-collected
		return rotatedImage;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Public Scale image methods - using NEARESTNEIGHBOR will never use progressive scaling
	// BILINEAR and BICUBIC will use progressive when needed (i.e. when < 0.5 scale ratio)
	public static BufferedImage scaleImage(BufferedImage originalImage, float inx, float iny) {
		//

		if(interpolationQuality>INTERPOLATION_NEARESTNEIGHBOR && inx==iny && inx < 1) {
			return scaleImage_Progressive(originalImage, inx);
		}
		
		if(interpolationQuality>INTERPOLATION_NEARESTNEIGHBOR && (inx <= 0.5f || iny <= 0.5)) {
			return scaleImage_NonUniform_ProgressiveUniformPreScale(originalImage, inx, iny);
		}
		
		return scaleImage_SinglePass(originalImage, inx, iny);
	}


	public static BufferedImage scaleImage(BufferedImage originalImage, float sc) {
		//

		if(interpolationQuality>INTERPOLATION_NEARESTNEIGHBOR && sc < 1) {
			return scaleImage_Progressive(originalImage, sc);
		}else {
			return scaleImage_SinglePass(originalImage, sc,sc);
		}

	}

	

	public static BufferedImage scaleImageToFitRect(BufferedImage img, Rect rect) {
		// scales the image uniformly to fit the rect - so no distortion in x or y
		// the rect should be in Image Buffer Units (pixels)
		float rectW = rect.getWidth();
		float rectH = rect.getHeight();
		float rectAspect = rect.aspect();

		float imageW = img.getWidth();
		float imageH = img.getHeight();
		float aspectImg = imageW/imageH;

		// if the rect is bigger than the image then scale up, else scale down
		float scalefactor = 1;
		if(aspectImg > rectAspect) {
			scalefactor = rectH/imageH;
		}else {
			scalefactor = rectW/imageW;
		}
		System.out.println("ImageProcessin scaleImageToFitRect:: scale factor "+scalefactor);
		return ImageProcessing.scaleImage(img, scalefactor, scalefactor);
	}


	public static BufferedImage resizeTo(BufferedImage originalImage, int scaledWidth, int scaledHeight)
	{
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		if(w==scaledWidth && h==scaledHeight) return originalImage;

		float scaleX = scaledWidth/(float)w;
		float scaleY = scaledHeight/(float)h;
		return ImageProcessing.scaleImage(originalImage, scaleX, scaleY);

	}

	public static BufferedImage scaleToTarget(BufferedImage originalImage, BufferedImage target) {
		return resizeTo(originalImage, target.getWidth(),  target.getHeight());
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// private Scale image methods
	//
	// Most of the scaling in this application is downscaling. Downscaling by more than 50% in a single pass
	// is very poor as it skips source-image pixels resulting in poor results. If the downscale is between 1 and 0.5,
	// then use the single pass, otherwise use progressive scaling
	//
	private static BufferedImage scaleImage_SinglePass(BufferedImage originalImage, float inx, float iny) {
		// if inx != iny the you have to use this
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();

		int scaledWidth = (int) (w * inx);
		int scaledHeight = (int) (h * iny);

		if(scaledWidth <= 0) scaledWidth = 1;
		if(scaledHeight <= 0) scaledHeight = 1;

		BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, originalImage.getType());
		Graphics2D g = scaledImage.createGraphics();
		setInterpolationQuality(g);
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
		g.dispose(); // release used resources before g is garbage-collected
		return scaledImage;
	}
	

	private static BufferedImage scaleImage_Progressive(BufferedImage before, float scale ) {
		// Multi-step rescale operation
		// This technique is described in Chris Campbell’s blog The Perils of Image.getScaledInstance(). As Chris mentions, when 
		// downscaling to something less than factor 0.5, you get the best result by doing multiple downscaling with a minimum factor of 0.5 
		// (in other words: each scaling operation should scale to maximum half the size).
		// Currently can only work with uniform scaling
		int w = before.getWidth();
		int h = before.getHeight();
		int targetW = (int) (w*scale);
		int targetH = (int) (h*scale);
		Integer longestSideLength = Math.max(targetW, targetH);

		setInterpolationQuality(1);// set this method to use bicubic exclusively

		Double ratio = h > w ? longestSideLength.doubleValue() / h : longestSideLength.doubleValue() / w;
		while (ratio < 0.5) {
			//BufferedImage tmp = progressiveScale(before, 0.5);
			BufferedImage tmp =  scaleImage_SinglePass(before,0.5f,0.5f);
			before = tmp;
			w = before.getWidth();
			h = before.getHeight();
			ratio = h > w ? longestSideLength.doubleValue() / h : longestSideLength.doubleValue() / w;
		}
		BufferedImage after = scaleImage_SinglePass(before,ratio.floatValue(),ratio.floatValue());
		
		restoreInterpolationQuality();
		return after;
	}
	
	
	private static BufferedImage scaleImage_NonUniform_ProgressiveUniformPreScale(BufferedImage originalImage, float inx, float iny) {
		// This method uses a progressive scale to create a uniform scale based on the longest resultant edge,
		// then scales along the shortest edge using a single pass to do the non-uniform bit
		float uniformPrescale = Math.max(inx, iny);
		float residualXScale = 1;
		float residualYScale = 1;
		if(inx > iny) {
			residualYScale = iny/inx;
		}else {
			residualXScale = inx/iny;
		}
		
		BufferedImage tmp = scaleImage_Progressive( originalImage, uniformPrescale );

		return scaleImage_SinglePass(tmp,residualXScale,residualYScale);
	}

	











	

	public static BufferedImage mirrorImage(BufferedImage originalImage, boolean flipInX, boolean flipInY) {

		if(originalImage==null) {
			System.out.println("ImageProcessing mirrorImage - image is null, returning original");
			return originalImage;

		}

		if (!flipInX && !flipInY) {
			// probably should catch this to stop time wasting
			return copyImage(originalImage);
		}

		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		//System.out.println("ImageProcessing mirrorImage - image is size " + w + " " + h);
		AffineTransform tx = null;
		AffineTransformOp op;

		if (flipInX && !flipInY) {
			// Flip the image horizontally
			tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-w, 0);
		}

		if (!flipInX && flipInY) {
			// Flip the image vertically
			tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -h);
		}

		if (flipInX && flipInY) {
			// Flip the image vertically and horizontally; equivalent to rotating the image
			// 180 degrees
			tx = AffineTransform.getScaleInstance(-1, -1);
			tx.translate(-w, -h);
		}

		op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(originalImage, null);
	}



	public static BufferedImage rotate90(BufferedImage src, int steps) {
		float rotationAngle = steps*90;

		double theta = (Math.PI * 2) / 360 * rotationAngle;
		int width = src.getWidth();
		int height = src.getHeight();
		BufferedImage dest;
		if (rotationAngle == 90 || rotationAngle == 270) {
			dest = new BufferedImage(src.getHeight(), src.getWidth(), src.getType());
		} else {
			dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		}

		Graphics2D graphics2D = dest.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		if (rotationAngle == 90) {
			graphics2D.translate((height - width) / 2, (height - width) / 2);
			graphics2D.rotate(theta, height / 2, width / 2);
		} else if (rotationAngle == 270) {
			graphics2D.translate((width - height) / 2, (width - height) / 2);
			graphics2D.rotate(theta, height / 2, width / 2);
		} else {
			graphics2D.translate(0, 0);
			graphics2D.rotate(theta, width / 2, height / 2);
		}
		graphics2D.drawRenderedImage(src, null);
		return dest;
	}




	


	/////////////////////////////////////////////////////////////////////////////////////////////////
	// LUT-based point functions
	//

	public static BufferedImage pointFunction(BufferedImage image, byte[][] lutArray) {
		ByteLookupTable lut = new ByteLookupTable(0, lutArray);
		BufferedImageOp op = new LookupOp(lut, null);
		BufferedImage outImg = op.filter(image, null);
		return outImg;
	}
	
	public static BufferedImage pointFunction16BitGray(BufferedImage image, short[] lutArray) {
		ShortLookupTable lut = new ShortLookupTable(0, lutArray);
		BufferedImageOp op = new LookupOp(lut, null);
		BufferedImage outImg = op.filter(image, null);
		return outImg;
	}

	public static BufferedImage tintWithColor(BufferedImage image, Color c) {
		// black pixels in the source remains black,  white pixels become the tint colour
		
		// to stop blended colour being lighter than the color c, we need to clamp the brightness of the resultant colour
		
		
		
		
		image = assertImageTYPE_INT_ARGB(image);
		byte[][] data = new byte[4][256];
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		
		for (int n = 0; n < 256; n++) {
			//data[0][n] = (byte) n;
			float amt = n/255.0f;
			
			float blendR = MOMaths.lerp(amt, n, r);
			float blendG = MOMaths.lerp(amt, n, g);
			float blendB = MOMaths.lerp(amt, n, b);
			
			data[0][n] = (byte) Math.min(blendR, r);
			data[1][n] = (byte) Math.min(blendG, g);
			data[2][n] = (byte) Math.min(blendB, b);
			data[3][n] = (byte) n; // keep alpha the same as input
		}
		return pointFunction(image, data);
	}

	public static BufferedImage tintWithColor(BufferedImage image, Color darkCol, Color lightCol) {
		// black pixels are replaced by dark color,  white pixels become lightCol color
		image = assertImageTYPE_INT_ARGB(image);
		byte[][] data = new byte[4][256];


		for (int n = 0; n < 256; n++) {
			//data[0][n] = (byte) n;
			float amt = n/255.0f;
			Color c = MOColor.blendColor(amt, darkCol, lightCol);
			data[0][n] = (byte) c.getRed();
			data[1][n] = (byte) c.getGreen();
			data[2][n] = (byte) c.getBlue();
			data[3][n] = (byte) n;
		}
		return pointFunction(image, data);
	}

	public static BufferedImage blendWithColor(BufferedImage image, Color c, float amt) {
		// blends exisiting rgb values with colour c acording to parameter amt (0..1), preserving alpha
		image = assertImageTYPE_INT_ARGB(image);
		//ignores any alpha, just blends the rgb values.
		byte[][] data = new byte[4][256];


		for (int n = 0; n < 256; n++) {
			//data[0][n] = (byte) n;

			data[0][n] = (byte) MOMaths.lerp(amt, n, c.getRed());
			data[1][n] = (byte) MOMaths.lerp(amt, n, c.getGreen());
			data[2][n] = (byte) MOMaths.lerp(amt, n, c.getBlue());
			data[3][n] = (byte) n;
		}
		return pointFunction(image, data);
	}

	public static BufferedImage makeGreyscale(BufferedImage image) {
		// replaces existing rgb values with grey value
		BufferedImage img = copyImage(image);
		int width = img.getWidth();
		int height = img.getHeight();
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int p = img.getRGB(x,y);
				int a = (p>>24) & 0xFF;
				if(a<1) continue;
				int r = (p>>16) & 0xFF;
				int g = (p>>8) & 0xFF;
				int b = p & 0xFF;
				int avg = (r + g + b)/3;
				p = (a<<24) | (avg<<16) | (avg<<8) |  avg;
				img.setRGB(x, y, p);
			}
		}
		return img;
	}


	public static BufferedImage replaceColor(BufferedImage image, Color c) {
		// replaces exisiting rgb values with colour c, preserving alpha
		byte[][] data = new byte[3][256];

		byte r = (byte) c.getRed();
		byte g = (byte) c.getGreen();
		byte b = (byte) c.getBlue();
		for (int n = 0; n < 256; n++) {
			data[0][n] = r;
			data[1][n] = g;
			data[2][n] = b;
		}
		return pointFunction(image, data);
	}
	
	public static BufferedImage replace16BitGrayValue(BufferedImage image, Color c) {
		// used in pasting a sprite depth value is 16 bit gray.
		// replaces existing values with colour c, preserving alpha
		
		image = ImageProcessing.convertColorModel(image, BufferedImage.TYPE_USHORT_GRAY);
		
		short[] data = new short[256*256];

		float rf = (float) c.getRed(); 
		// assuming the colour is in the range 0...1
		short val = (short)(rf*(255*255));
		
		for (int n = 0; n < 256*256; n++) {
			data[n] = val;
			
		}
		return pointFunction16BitGray(image, data);
	}
	

	public static BufferedImage adjustBrightness(BufferedImage image, float brightness) {
		//System.out.println("in adjustBrightness image type = " + image.getType());
		byte[][] data = new byte[3][256];
		for (int n = 0; n < 256; n++) {
			byte newVal = (byte) (MOMaths.constrain((n * brightness), 0, 255));
			data[0][n] = newVal;
			data[1][n] = newVal;
			data[2][n] = newVal;
		}
		return pointFunction(image, data);
	}

	public static BufferedImage adjustBrightnessNoClip(BufferedImage image, float brightness) {
		byte[][] data = new byte[3][256];

		for (int n = 0; n < 256; n++) {
			float val01 = n/256f;
			float brightness01 = brightnessCurve(val01, brightness);
			byte newVal = (byte) (brightness01*256);
			data[0][n] = newVal;
			data[1][n] = newVal;
			data[2][n] = newVal;
		}
		return pointFunction(image, data);
	}

	public static BufferedImage adjustContrast(BufferedImage image, float contrast) {
		byte[][] data = new byte[3][256];
		for (int n = 0; n < 256; n++) {
			float v = n / 256f;
			byte newVal = (byte) (contrastCurve(v, contrast) * 256);
			data[0][n] = newVal;
			data[1][n] = newVal;
			data[2][n] = newVal;
		}
		return pointFunction(image, data);
	}

	public static BufferedImage invert(BufferedImage image) {
		byte[][] data = new byte[3][256];
		for (int n = 0; n < 256; n++) {

			byte newVal = (byte) (255 - n);
			data[0][n] = newVal;
			data[1][n] = newVal;
			data[2][n] = newVal;
		}
		return pointFunction(image, data);

	}
	
	public static BufferedImage threshold(BufferedImage image, int threshPoint) {
		byte[][] data = new byte[3][256];
		for (int n = 0; n < 256; n++) {
			byte newVal = (byte) 0;
	
			if(n>threshPoint) newVal = (byte) 255;
			data[0][n] = newVal;
			data[1][n] = newVal;
			data[2][n] = newVal;
		}
		return pointFunction(image, data);

	}
	

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// point functions cont. 
	// adjustLevels is an approximation to Photoshop's levels
	// All inputs are in the range 0..255


	public static BufferedImage adjustLevels(BufferedImage image, float shadowVal, float midtoneVal, float highlightVal, float outShadowVal,  float outHighlightVal){
		// work out gamma correction value
		float gamma = 1;
		float midtoneNormal = midtoneVal/255.0f;
		if(midtoneVal < 128){
			midtoneNormal *= 2;
			gamma = 1 + (9* (1-midtoneNormal));
			gamma = Math.min(gamma,9.99f);
		} else {
			midtoneNormal = ( midtoneNormal * 2) - 1;
			gamma = 1 - midtoneNormal;
			gamma = Math.max(gamma, 0.01f);
		}

		// create the LUT
		byte[][] lut = new byte[3][256];
		for(int n = 0; n < 256; n++) {
			float v  = ajustLevels_applyInputLevels(n,  shadowVal,   highlightVal);
			v = ajustLevels_applyMidTones( v,  gamma);
			v = ajustLevels_applyOutputLevels(v,  outShadowVal,   outHighlightVal);
			lut[0][n] = (byte)v;
			lut[1][n] = (byte)v;
			lut[2][n] = (byte)v;
		}
		return  pointFunction(image, lut);
	}

	public static BufferedImage adjustLevels(BufferedImage image, float shadowVal, float midtoneVal, float highlightVal) {
		// shorthand of main function above with output set to 0,255
		return adjustLevels( image,  shadowVal,  midtoneVal,  highlightVal, 0,  255);
	}

	private static float ajustLevels_applyInputLevels(float valIn, float shadowVal,  float highlightVal){
		return 255 * (( valIn - shadowVal) / (highlightVal - shadowVal));
	}

	private static float ajustLevels_applyMidTones(float valIn, float gamma){
		return (float) (255 * (Math.pow( (valIn/255), gamma)));
	}

	private static float ajustLevels_applyOutputLevels(float valIn, float outShadowVal,  float outHighlightVal){
		return (valIn/255.0f) * ( outHighlightVal - outShadowVal) + outShadowVal;
	}

	// end adjustLevels
	////////////////////////////////////////////////////////////////////////////////////////////////


	static float brightnessCurve(float v, float amt) {
		// 0..1 decreases brightness
		// 1..2 increases brightness
		// for brightening, ensures the white does not clip, and the blacks get a little offset
		// darkening is just a straight scale

		if (amt >= 1.0) {
			float brightness = MOMaths.map(amt, 1, 2, 0, 1);
			float brigtnessCurve = MOMaths.lerp(brightness, 1, 4);

			float curveValue = MOMaths.inverseGammaCurve(v, brigtnessCurve);

			float boostBlacks = MOMaths.lerp(brightness, 0.0f,0.2f);

			//println("amt,brightness, brightnessCurce, rampAmt, ranpval", amt,brightness,brigtnessCurve, rampAmount, rampValue);
			float curvePlusBoostBlack =  MOMaths.map(curveValue,0,1,boostBlacks,1);

			// in the last quartile amt = 1.5..2, make sure the whites are not totally clipped
			return curvePlusBoostBlack;

		} else {
			return v*amt;
		}

	}


	public static float contrastCurve(float v, float amt) {
		// 0..1 decreases contrast
		// 1..2 increases contrast
		// contrast: generate a sigmoid function
		// low contrast has a steepness of 8, high a steepness of 15
		if (amt >= 1.0) {
			amt = MOMaths.constrain(amt - 1, 0, 1);
			float steepness = MOMaths.map(amt, 0, 1, 8, 17);

			float f = (float) (1.0 / (1 + Math.exp(-steepness * (v - 0.5))));

			float contrast = MOMaths.lerp(amt, v, f);

			return MOMaths.constrain(contrast, 0, 1);
		} else {

			float steepness = MOMaths.map(amt, 0, 1, 0.5f, 0);

			float contrast = MOMaths.map(v, 0, 1, steepness, 1 - steepness);

			return MOMaths.constrain(contrast, 0, 1);
		}

	}



	/////////////////////////////////////////////////////////////////////////////////////////////////
	// HSV methods
	//
	public static BufferedImage adjustHSV(BufferedImage img, float dh, float ds, float dv) {
		// all input values operate in the range 0..1, with h having its own wrap-around for numbers outside of 0..1
		// Hue
		// 0.0    is equivalent to red
		// 0.1666 is equivalent to yellow
		// 0.3333 is equivalent to green
		// 0.5    is equivalent to cyan
		// 0.6666 is equivalent to blue
		// 0.8333 is equivalent to magenta
		// 0.9999 is equivalent to red


		int w = img.getWidth();
		int h = img.getHeight();
		int imtype = img.getType();

		if(imtype != BufferedImage.TYPE_INT_ARGB) {
			img = convertColorModel(img, BufferedImage.TYPE_INT_ARGB);
		}
		//System.out.println("AFTER adjustHSV incoming image is of type " + img.getType() + " BufferedImage.TYPE_INT_ARGB is " + BufferedImage.TYPE_INT_ARGB);
		int[] pixelsIn = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();


		BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] pixelsOut = ((DataBufferInt) outputImage.getRaster().getDataBuffer()).getData();

		float[] hsv = new float[4];
		int[] unpacked = new int[4];
		int[] newRGBUnpacked = new int[4];
		int newRGBPacked;
		for (int i = 0; i < pixelsIn.length; i++) {

			MOPackedColor.unpackARGB(pixelsIn[i], unpacked);

			// if alpha is 0 then we don't need to process this pixel
			if (unpacked[0] == 0)
				continue;

			// this converts the rgb into the array hsv
			Color.RGBtoHSB(unpacked[1], unpacked[2], unpacked[3], hsv);

			// do your HSB shifting here
			hsv[0] = hsv[0] + dh; // HSBtoRGB does its own value wrapping
			hsv[1] = MOMaths.constrain(hsv[1] + ds, 0f, 1f);
			hsv[2] = MOMaths.constrain(hsv[2] + dv, 0f, 1f);

			// convert the hsb back to rgb
			newRGBPacked = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
			newRGBUnpacked = MOPackedColor.unpackARGB(newRGBPacked);
			pixelsOut[i] = MOPackedColor.packARGB(unpacked[0], newRGBUnpacked[1], newRGBUnpacked[2], newRGBUnpacked[3]);

		}

		return outputImage;

	}



	public static BufferedImage setDominantHue(BufferedImage img, float newDominantHue) {
		// rather than simply setting the hue to a set color, we get the dominant hue of the image and use it as
		// a centre-point for adjustment
		float currentDominantHue = getDominantHue( img);
		float dif = newDominantHue - currentDominantHue;
		return adjustHSV( img, dif, 0, 0);
	}

	public static float getDominantHue(BufferedImage img) {
		// all input values operate in the range 0..1, with h having its own wrap-around for numbers outside of 0..1
		img = assertImageTYPE_INT_ARGB(img);

		//System.out.println("AFTER adjustHSV incoming image is of type " + img.getType() + " BufferedImage.TYPE_INT_ARGB is " + BufferedImage.TYPE_INT_ARGB);
		int[] pixelsIn = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

		float[] hsv = new float[4];
		int[] unpacked = new int[4];

		float hueTotal = 0;
		int numPixelsProcessed = 0;
		for (int i = 0; i < pixelsIn.length; i++) {

			MOPackedColor.unpackARGB(pixelsIn[i], unpacked);

			// if alpha is 0 then we don't need to process this pixel
			if (unpacked[0] == 0)
				continue;

			// this converts the rgb into the array hsv
			Color.RGBtoHSB(unpacked[1], unpacked[2], unpacked[3], hsv);

			// get the hue
			hueTotal += hsv[0]; // HSBtoRGB does its own value wrapping
			numPixelsProcessed++;
		}
		float hueAverage = hueTotal/numPixelsProcessed;
		return hueAverage;
	}




	////////////////////////////////////////////////////////////////////////////////////////////////
	// Multiply two images
	//
	public static BufferedImage multiplyImages(BufferedImage img1, BufferedImage img2) {
		// both images should be the same size
		int w = img1.getWidth();
		int h = img1.getHeight();

		int w2 = img2.getWidth();
		int h2 = img2.getHeight();

		// validate
		if( !(w == w2 && h == h2) ) {
			System.out.println("ImageProcessing:multiplyImages input image aree not the same size - returning null");
			return null;
		}
		img1 = assertImageTYPE_INT_ARGB(img1);
		img2 = assertImageTYPE_INT_ARGB(img2);


		int[] pixelsIn1 = ((DataBufferInt) img1.getRaster().getDataBuffer()).getData();
		int[] pixelsIn2 = ((DataBufferInt) img2.getRaster().getDataBuffer()).getData();

		BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] pixelsOut = ((DataBufferInt) outputImage.getRaster().getDataBuffer()).getData();

		for (int i = 0; i < pixelsIn1.length; i++) {

			int[] unpacked1 = MOPackedColor.unpackARGB(pixelsIn1[i]);
			int[] unpacked2 = MOPackedColor.unpackARGB(pixelsIn2[i]);

			// if both alphas are 0 then we don't need to process this pixel
			if (unpacked1[0] == 0 && unpacked2[0] == 0)
				continue;
			pixelsOut[i] = colorMultiply(unpacked1,unpacked2);
		}

		return outputImage;
	}

	static int colorMultiply(int[] unpacked1, int[] unpacked2) {

		//int a = Math.max(unpacked1[0] , unpacked2[0]);

		// my idea for alpha multiplication
		// most input image will be alpha 255, e.g. an image against a white background. These pose no problem and the output also has alpha of 255
		// however, IF, alpha comes into play...
		// Think of two overlapping sheets of coloured glass
		// The result builds on the most dense alpha, but is not a straight sum;
		// when the min value is low, we can just about add it wholesale, but not so much that it makes the final sum > 255 , i.e. min*(1-max)
		// when the min value is around 0.5, we can add min*0.5
		// when the min value is high, we can only add a fraction of it (*i.e min*(1-max))
		int a = Math.max(unpacked1[0] , unpacked2[0]);
		if(a != 255) {
			float maxA =  a/255f;
			float minA =  Math.min(unpacked1[0] , unpacked2[0])/255f;
			float af  = maxA + (minA * (1-maxA));
			a = (int) af*255;
		}
		int r = (int)((unpacked1[1] * unpacked2[1]) / 255f);
		int g = (int)((unpacked1[2] * unpacked2[2]) / 255f);
		int b = (int)((unpacked1[3] * unpacked2[3]) / 255f);

		return MOPackedColor.packARGB(a,r,g,b);
	}



}// end of ImageProcessing static class



