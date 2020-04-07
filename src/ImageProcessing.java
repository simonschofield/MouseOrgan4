import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.DataBufferInt;
import java.awt.image.LookupOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageProcessing {

	// 0 == nearest neighbour
	// 1 == bilinear
	// 2 == bicubic
	static int interpolationQuality = 2;

	public static void setInterpolationQuality(int q) {
		interpolationQuality = q;
	}

	private static void setInterpolationQuality(Graphics2D g) {
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
		}
		return img;
	}

	public static void saveImage(String pathAndName, BufferedImage img) {
		try {
			// retrieve image
			File outputfile = new File(pathAndName);
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			// do nothing
		}

	}

	public static BufferedImage copyImage(BufferedImage source) {
		BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
		Graphics g = b.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return b;
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
	
	public static BufferedImage convertColorModel(BufferedImage src, int colorModel) {
	    BufferedImage img= new BufferedImage(src.getWidth(), src.getHeight(), colorModel);
	    Graphics2D g2d= img.createGraphics();
	    g2d.drawImage(src, 0, 0, null);
	    g2d.dispose();
	    return img;
	}
	
	public static void pasteIntoImage(BufferedImage sourceImg, BufferedImage targetImg, int xPosTarget, int yPosTarget) {
		Graphics2D g2d= targetImg.createGraphics();
	    g2d.drawImage(sourceImg, xPosTarget, yPosTarget, null);
	    g2d.dispose();
	}
	
	public static BufferedImage cropImage(BufferedImage src, Rect r) {
		  //System.out.println("ImageProcessing:cropImage rect " + r.toStr());
	      BufferedImage dest = src.getSubimage((int)r.left,(int)r.top, (int)r.getWidth(), (int)r.getHeight());
	      return dest; 
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
	
	public static BufferedImage scaleTo(BufferedImage originalImage, int newW, int newH) {
		// scales the originalImage to be newW, newH
		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		float wScale = newW/(float)w;
		float hScale = newH/(float)h;
		return scaleImage( originalImage,wScale,hScale);
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

	public static BufferedImage mirrorImage(BufferedImage originalImage, boolean flipX, boolean flipY) {

		if (!flipX && !flipY) {
			// probably should catch this to stop time wasting
			return copyImage(originalImage);
		}

		int w = originalImage.getWidth();
		int h = originalImage.getHeight();
		AffineTransform tx = null;
		AffineTransformOp op;

		if (flipX && !flipY) {
			// Flip the image vertically
			tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -h);
		}

		if (!flipX && flipY) {
			// Flip the image horizontally
			tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-w, 0);
		}

		if (flipX && flipY) {
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
	public static BufferedImage brightenImage(BufferedImage image, float brightness) {
		byte[][] data = new byte[3][256];
		for (int n = 0; n < 256; n++) {
			byte newVal = (byte) (MOMaths.constrain((n * brightness), 0, 255));
			data[0][n] = newVal;
			data[1][n] = newVal;
			data[2][n] = newVal;
		}
		return pointFunction(image, data);
	}

	public static BufferedImage contrastImage(BufferedImage image, float contrast) {
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
		BufferedImage img = pointFunction(image, data);
		saveImage("C:\\simon\\Artwork\\MouseOrgan4\\inverted.png", img);
		return img;
	}

	public static BufferedImage pointFunction(BufferedImage image, byte[][] lutArray) {
		ByteLookupTable lut = new ByteLookupTable(0, lutArray);
		BufferedImageOp op = new LookupOp(lut, null);
		BufferedImage outImg = op.filter(image, null);
		return outImg;
	}
	
	
	
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

	
	static int[] unpackARGB(int packedCol) {
		int[] col = new int[4];
		col[0] = (packedCol >> 24) & 0xFF;// alpha
		col[1] = (packedCol >> 16) & 0xFF;// red
		col[2] = (packedCol >> 8) & 0xFF; // green
		col[3] = packedCol & 0xFF; // blue
		return col;
	}

	static void unpackARGB(int packedCol, int[] col) {

		col[0] = (packedCol >> 24) & 0xFF;// alpha
		col[1] = (packedCol >> 16) & 0xFF;// red
		col[2] = (packedCol >> 8) & 0xFF; // green
		col[3] = packedCol & 0xFF; // blue
	}

	static Color packedIntToColor(int packedCol, boolean hasAlpha) {
		return new Color(packedCol, hasAlpha);
	}

	static float packedIntToVal01(int packedCol, boolean hasAlpha) {
		Color c = packedIntToColor(packedCol, hasAlpha);
		return (c.getRed() + c.getGreen() + c.getBlue()) / 765f;
	}

	static int packARGB(int a, int r, int g, int b) {
		// Packs four 8 bit numbers into one 32 bit number

		a = a << 24; // Binary: 11111111000000000000000000000000
		r = r << 16; // Binary: 00000000110011000000000000000000
		g = g << 8; // Binary: 00000000000000001100110000000000
		// b remains untouched

		int argb = a | r | g | b;
		return argb;
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

}

class ConvolutionFilter {
	float[][] edge_matrix = { { 0, -2, 0 }, { -2, 8, -2 }, { 0, -2, 0 } };

	float[][] blur_matrix = { { 0.1f, 0.1f, 0.1f }, { 0.1f, 0.1f, 0.1f }, { 0.1f, 0.1f, 0.1f } };

	float[][] sharpen_matrix = { { 0, -1, 0 }, { -1, 5, -1 }, { 0, -1, 0 } };

	float[][] gaussianblur_matrix = { { 0.000f, 0.000f, 0.001f, 0.001f, 0.001f, 0.000f, 0.000f },
			{ 0.000f, 0.002f, 0.012f, 0.020f, 0.012f, 0.002f, 0.000f },
			{ 0.001f, 0.012f, 0.068f, 0.109f, 0.068f, 0.012f, 0.001f },
			{ 0.001f, 0.020f, 0.109f, 0.172f, 0.109f, 0.020f, 0.001f },
			{ 0.001f, 0.012f, 0.068f, 0.109f, 0.068f, 0.012f, 0.001f },
			{ 0.000f, 0.002f, 0.012f, 0.020f, 0.012f, 0.002f, 0.000f },
			{ 0.000f, 0.000f, 0.001f, 0.001f, 0.001f, 0.000f, 0.000f } };

	float[][] sobelX_matrix5 = { { 2, 1, 0, -1, -2 }, { 2, 1, 0, -1, -2 }, { 4, 2, 0, -2, -4 }, { 2, 1, 0, -1, -2 },
			{ 2, 1, 0, -1, -2 } };

	float[][] sobelY_matrix5 = { { 2, 2, 4, 2, 2 }, { 1, 1, 2, 1, 1 }, { 0, 0, 0, 0, 0 }, { -1, -1, -2, -1, -1 },
			{ -2, -2, -4, -2, -2 } };

	float[][] sobelX_matrix3 = { { 2, 0, -2 }, { 4, 0, -4 }, { 2, 0, -2 } };

	float[][] sobelY_matrix3 = { { 2, 4, 2 }, { 0, 0, 0 }, { -2, -4, -2 } };
	float[][] currentMatrix;
	int currentMatrixDim;

	public ConvolutionFilter(String type) {
		setCurrentFilter(type);
	}

	void setCurrentFilter(String type) {
		if (type.toLowerCase() == "edge") {
			currentMatrix = edge_matrix;
			currentMatrixDim = 3;
			return;
		}
		if (type.toLowerCase() == "blur") {
			currentMatrix = blur_matrix;
			currentMatrixDim = 3;
			return;
		}
		if (type.toLowerCase() == "gaussianblur") {
			currentMatrix = gaussianblur_matrix;
			currentMatrixDim = 7;
			getCurrentMatrixSum();
			return;
		}
		if (type.toLowerCase() == "sobelx3") {
			currentMatrix = sobelX_matrix3;
			currentMatrixDim = 3;
			return;
		}
		if (type.toLowerCase() == "sobely3") {
			currentMatrix = sobelY_matrix3;
			currentMatrixDim = 3;
			return;
		}

		if (type.toLowerCase() == "sobelx5") {
			currentMatrix = sobelX_matrix5;
			currentMatrixDim = 5;
			return;
		}
		if (type.toLowerCase() == "sobely5") {
			currentMatrix = sobelY_matrix5;
			currentMatrixDim = 5;
			return;
		}

		// println("ConvolutionFilter: unknown filter requested - ", type);
	}

	float getCurrentMatrixSum() {
		float sum = 0;
		for (int j = 0; j < currentMatrixDim; j++) {

			for (int i = 0; i < currentMatrixDim; i++) {

				sum += currentMatrix[i][j];

			}
		}

		return sum;
	}

	float convolveFloatPixel(int x, int y, FloatImage img) {

		// x,y is the central pixel of the floatimage in the convolution
		float total = 0.0f;
		int offset = currentMatrixDim / 2;

		// println("current matrix dim ",currentMatrixDim);
		for (int i = 0; i < currentMatrixDim; i++) {
			for (int j = 0; j < currentMatrixDim; j++) {
				// get the image pixel clamped to the dims
				int xloc = x + i - offset;
				int yloc = y + j - offset;
				float imgval = img.getClamped(xloc, yloc);

				// Calculate the convolution
				total += (imgval * currentMatrix[i][j]);
				// println("loc total",loc,total);
			}
		}

		// Return the resulting color
		return total;
	}

	//////////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	public void createGaussianKernal(int size, float sd) {
		currentMatrixDim = size;
		// println("distances");
		currentMatrix = new float[currentMatrixDim][currentMatrixDim];
		for (int j = 0; j < currentMatrixDim; j++) {
			for (int i = 0; i < currentMatrixDim; i++) {
				// currentMatrix[i][j]=gaussianDiscrete2D(sd,i-(size/2),j-(size/2));
				currentMatrix[i][j] = gaussianValue(sd, i, j);
			}
			// println();
		}

		// this will produce a Gaussian Curve with values of 1.0 at the centre, fading
		// off.
		// we need to scal eit so that the sum of all values = 1
		float sumAllValues = getCurrentMatrixSum();

		for (int j = 0; j < currentMatrixDim; j++) {
			for (int i = 0; i < currentMatrixDim; i++) {
				float scaledVal = currentMatrix[i][j] / sumAllValues;
				currentMatrix[i][j] = scaledVal;
				// print("[",scaledVal,"]");
			}
			// println();
		}
	}

	private float gaussianValue(float sd, int x, int y) {
		float cx = (currentMatrixDim - 1) / 2.0f;
		float distToCentre = PVector.dist(new PVector(cx, cx, 0), new PVector(x, y, 0));
		// print("[",distToCentre,"]");
		return MOMaths.gaussianCurve(distToCentre, 1.0f, 0, sd);
	}

}// end ConvolutionFilter class
