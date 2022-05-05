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
import java.io.File;
import java.io.IOException;


import javax.imageio.ImageIO;

import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Rect;


public class ImageProcessing {

	
	// 0 == nearest neighbour
	// 1 == bilinear
	// 2 == bicubic
	public static final int	INTERPOLATION_NEARESTNEIGHBOR = 0;
	public static final int	INTERPOLATION_BILINEAR = 1;
	public static final int	INTERPOLATION_BICUBIC = 2;
	
	public static final int	COLORTRANSFORM_NONE = 0;
	public static final int	COLORTRANSFORM_HSV = 1;
	public static final int	COLORTRANSFORM_BRIGHTNESS_NOCLIP = 2;
	public static final int	COLORTRANSFORM_BRIGHTNESS = 3;
	public static final int	COLORTRANSFORM_CONTRAST = 4;
	public static final int	COLORTRANSFORM_LEVELS = 5;
	public static final int	COLORTRANSFORM_BLENDWITHCOLOR = 6;
	public static final int	COLORTRANSFORM_SET_DOMINANT_HUE = 7;
	
	
	
	
	
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
		if (interpolationQuality == 0)
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		if (interpolationQuality == 1)
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		if (interpolationQuality == 2)
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

	}
	
	

	public static BufferedImage loadImage(String pathAndName) {

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(pathAndName));
		} catch (IOException e) {
			System.out.println("loadImage: cannot load = " + pathAndName);
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
	
	
	public static BufferedImage getMaskedImage(BufferedImage source,  BufferedImage mask, Rect maskRect) {
		// Not used
		//
		// Mask rect is in pixel coordinates of the source, and can be outside the source
		// The mask is scaled to the rect, and applied.
		// Areas outside the mask image rect are wholly masked-out.
		// the type of mask used is one that preserves the SOLID parts of the underlying image (DST_OUT)
		
		int newMaskWidth = (int)maskRect.getWidth();
		int newMaskHeight = (int)maskRect.getHeight();
		BufferedImage resizedMask = resizeTo(mask, newMaskWidth, newMaskHeight);
		
		// create an empty image the same size as source
		BufferedImage maskFullSize = createEmptyCopy(source);
		
		// paste the mask into it
		int x = (int)maskRect.left;
		int y = (int)maskRect.top;
		compositeImage_ChangeTarget(resizedMask, maskFullSize,  x,  y, 1f);
		
		
		return getMaskedImage( source,  maskFullSize,  0,  0, AlphaComposite.DST_IN);
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
		System.out.println("sourceImage " + sourceImage.getWidth() + "," + sourceImage.getHeight());
		System.out.println("mask " + mask.getWidth() + "," + mask.getHeight());
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
        
        System.out.println("sourceImagePixels " + sourceImagePixels.length);
        System.out.println("maskPixels " + maskPixels.length);
        //int length = width*height;
        for (int i = 0; i < sourceImagePixels.length; i++) {
            
        	int[] exisitingCol = unpackARGB(sourceImagePixels[i]);
            int maskAlpha = getGreen(maskPixels[i]); 
            int newAlpha = Math.min(exisitingCol[0], maskAlpha);
            imageOutPixels[i] = packARGB(newAlpha, exisitingCol[1], exisitingCol[2], exisitingCol[3]);
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
		return packedIntToVal01( packedCol,  hasAlpha);
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// pixel transforms
	//	
	
	static int[] unpackARGB(int packedCol) {
		int[] col = new int[4];
		col[0] = (packedCol >> 24) & 0xFF;// alpha
		col[1] = (packedCol >> 16) & 0xFF;// red
		col[2] = (packedCol >> 8) & 0xFF; // green
		col[3] = packedCol & 0xFF; // blue
		return col;
	}
	
	
	static int getRed(int packedCol) {
		return (packedCol >> 16) & 0xFF;// red
	}
	
	static int getGreen(int packedCol) {
		return (packedCol >> 8) & 0xFF; // green
	}
	
	static int getBlue(int packedCol) {
		return packedCol & 0xFF; // blue
	}
	
	static int getAlpha(int packedCol) {
		return (packedCol >> 24) & 0xFF;// alpha
	}

	static void unpackARGB(int packedCol, int[] col) {

		col[0] = (packedCol >> 24) & 0xFF;// alpha
		col[1] = (packedCol >> 16) & 0xFF;// red
		col[2] = (packedCol >> 8) & 0xFF; // green
		col[3] = packedCol & 0xFF; // blue
	}
	
	static int getChannelFromARGB(int packedCol, int channel) {
		return unpackARGB(packedCol)[channel];

	}

	public static Color packedIntToColor(int packedCol, boolean hasAlpha) {
		return new Color(packedCol, hasAlpha);
	}

	static float packedIntToVal01(int packedCol, boolean hasAlpha) {
		Color c = packedIntToColor(packedCol, hasAlpha);
		return (c.getRed() + c.getGreen() + c.getBlue()) / 765f;
	}

	public static int packARGB(int a, int r, int g, int b) {
		// Packs four 8 bit numbers into one 32 bit number

		a = a << 24; // Binary: 11111111000000000000000000000000
		r = r << 16; // Binary: 00000000110011000000000000000000
		g = g << 8; // Binary: 00000000000000001100110000000000
		// b remains untouched

		int argb = a | r | g | b;
		return argb;
	}
	
	public static Color blendColor(Color c1, Color c2, float blendAmt) {
		//System.out.println("blend amt " + blendAmt);
		float c1r = c1.getRed();
		float c1g = c1.getGreen();
		float c1b = c1.getBlue();
		float c1a = c1.getAlpha();
		//System.out.println("color1 " + c1r + " " + c1g + " "+ c1b + " "+ c1a + " " );
		float c2r = c2.getRed();
		float c2g = c2.getGreen();
		float c2b = c2.getBlue();
		float c2a = c2.getAlpha();
		//System.out.println("color2 " + c2r + " " + c2g + " "+ c2b + " "+ c2a + " " );
		int r = (int) MOMaths.lerp(blendAmt, c1r, c2r);
		int g = (int) MOMaths.lerp(blendAmt, c1g, c2g);
		int b = (int) MOMaths.lerp(blendAmt, c1b, c2b);
		int a = (int) MOMaths.lerp(blendAmt, c1a, c2a);
		//System.out.println("blend color " + r + " " + g + " "+ b + " "+ a + " " );
		return new Color(r,g,b,a);
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

	public static BufferedImage scaleImage(BufferedImage originalImage, float inx, float iny) {
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();

		int scaledWidth = (int) (w * inx);
		int scaledHeight = (int) (h * iny);
		
		if(scaledWidth == 0) scaledWidth = 1;
		if(scaledHeight == 0) scaledHeight = 1;
		
		BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = scaledImage.createGraphics();

		setInterpolationQuality(g);
		// System.out.println("Current rendering hint is " + g.getRenderingHints());
		g.scale(inx, iny);
		g.drawImage(originalImage, 0, 0, null);
		g.dispose(); // release used resources before g is garbage-collected
		return scaledImage;
	}
	
	
//	public static BufferedImage resizeTo(BufferedImage originalImage, int newW, int newH) {
//		// scales the originalImage to be newW, newH
//		int w = originalImage.getWidth();
//		int h = originalImage.getHeight();
//		float wScale = newW/(float)w;
//		float hScale = newH/(float)h;
//		return scaleImage( originalImage,wScale,hScale);
//	}
	
	public static BufferedImage resizeTo(BufferedImage originalImage, int scaledWidth, int scaledHeight)
	{
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, originalImage.getType());
 
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        setInterpolationQuality(g2d);
        g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
 
        return outputImage;
    }
	
	public static BufferedImage scaleToTarget(BufferedImage originalImage, BufferedImage target) {
		return resizeTo(originalImage, target.getWidth(),  target.getHeight());
	}

	public static BufferedImage scaleRotateImage(BufferedImage originalImage, float scalex, float scaley,
			float rotateDegs) {
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();

		int scaledWidth = (int) (w * scalex);
		int scaledHeight = (int) (h * scaley);
		
		if(scaledWidth == 0) scaledWidth = 1;
		if(scaledHeight == 0) scaledHeight = 1;

		double toRad = Math.toRadians(rotateDegs);
		int hPrime = (int) (scaledWidth * Math.abs(Math.sin(toRad)) + scaledHeight * Math.abs(Math.cos(toRad)));
		int wPrime = (int) (scaledHeight * Math.abs(Math.sin(toRad)) + scaledWidth * Math.abs(Math.cos(toRad)));

		BufferedImage scaledImage = new BufferedImage(wPrime, hPrime, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = scaledImage.createGraphics();

		setInterpolationQuality(g);

		g.translate(wPrime / 2, hPrime / 2);
		g.scale(scalex, scaley);
		g.rotate(toRad);
		g.translate(-w / 2, -h / 2);

		g.drawImage(originalImage, 0, 0, null);
		g.dispose(); // release used resources before g is garbage-collected
		return scaledImage;

	}

	public static BufferedImage mirrorImage(BufferedImage originalImage, boolean flipInX, boolean flipInY) {

		if (!flipInX && !flipInY) {
			// probably should catch this to stop time wasting
			return copyImage(originalImage);
		}

		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
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
	// color transforms
	//
	
	
	public static BufferedImage colorTransform(BufferedImage img, int function, float p1, float p2, float p3) {
		
			//System.out.println("in colorAdjustAll . Function = " + function);
			
			switch (function) {
			case COLORTRANSFORM_HSV: {
				return ImageProcessing.adjustHSV(img, p1, p2, p3);
			}
			case COLORTRANSFORM_BRIGHTNESS_NOCLIP: {
				return ImageProcessing.adjustBrightnessNoClip(img, p1);
			
			}
			case COLORTRANSFORM_BRIGHTNESS: {
				return ImageProcessing.adjustBrightness(img, p1);
			
			}
			case COLORTRANSFORM_CONTRAST: {
				return ImageProcessing.adjustContrast(img, p1);
			
			}
			case COLORTRANSFORM_LEVELS: {
				return ImageProcessing.adjustLevels(img, p1, p2, p3);
			
			}
			case COLORTRANSFORM_SET_DOMINANT_HUE:{
				return ImageProcessing.setDominantHue(img, p1);
			}
			default:
				return img;
			}
			

	}
	
	
	public static BufferedImage colorTransformMasked(BufferedImage img, BufferedImage maskImage, int function, float p1, float p2, float p3) {
		return null;
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
	
	public static BufferedImage tintWithColor(BufferedImage image, Color c) {
		// black pixels in the source remains black,  white pixels become the tint colour
		image = assertImageTYPE_INT_ARGB(image);
		byte[][] data = new byte[4][256];
		
		
		for (int n = 0; n < 256; n++) {
			//data[0][n] = (byte) n;
			float amt = n/255.0f;
			data[0][n] = (byte) MOMaths.lerp(amt, n, c.getRed());
			data[1][n] = (byte) MOMaths.lerp(amt, n, c.getGreen());
			data[2][n] = (byte) MOMaths.lerp(amt, n, c.getBlue());
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
	
	
	public static BufferedImage replaceColor(BufferedImage image, Color c) {
		// replaces exisiting rgb values with colour c, preserving alpha
		byte[][] data = new byte[3][256];
		
		byte r = (byte) c.getRed();
		byte g = (byte) c.getGreen();
		byte b = (byte) c.getBlue();
		for (int n = 0; n < 256; n++) {
			data[0][n] = b;
			data[1][n] = g;
			data[2][n] = r;
		}
		return pointFunction(image, data);
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

			unpackARGB(pixelsIn[i], unpacked);

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
			newRGBUnpacked = unpackARGB(newRGBPacked);
			pixelsOut[i] = packARGB(unpacked[0], newRGBUnpacked[1], newRGBUnpacked[2], newRGBUnpacked[3]);

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
		int imtype = img.getType();
		
		if(imtype != BufferedImage.TYPE_INT_ARGB) {
			img = convertColorModel(img, BufferedImage.TYPE_INT_ARGB);
		}
		//System.out.println("AFTER adjustHSV incoming image is of type " + img.getType() + " BufferedImage.TYPE_INT_ARGB is " + BufferedImage.TYPE_INT_ARGB);
		int[] pixelsIn = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		
		float[] hsv = new float[4];
		int[] unpacked = new int[4];
		
		float hueTotal = 0;
		int numPixelsProcessed = 0;
		for (int i = 0; i < pixelsIn.length; i++) {

			unpackARGB(pixelsIn[i], unpacked);

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

			int[] unpacked1 = unpackARGB(pixelsIn1[i]);
			int[] unpacked2 = unpackARGB(pixelsIn2[i]);

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
		
		return packARGB(a,r,g,b);
	}

	
	
}// end of ImageProcessing static class


	
