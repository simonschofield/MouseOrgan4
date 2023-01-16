package MOImage;

import java.awt.image.BufferedImage;

import MOMaths.MOMaths;
import MOMaths.PVector;

public class ConvolutionFilter {
	float[][] edge_matrix = { { 0, -2, 0 }, { -2, 8, -2 }, { 0, -2, 0 } };

	float[][] blur_matrix = { { 0.1f, 0.1f, 0.1f }, { 0.1f, 0.1f, 0.1f }, { 0.1f, 0.1f, 0.1f } };

	float[][] sharpen_matrix = { { 0, -1, 0 }, { -1, 5, -1 }, { 0, -1, 0 } };

	float[][] gaussianblur_matrix = { 
			{ 0.000f, 0.000f, 0.001f, 0.001f, 0.001f, 0.000f, 0.000f },
			{ 0.000f, 0.002f, 0.012f, 0.020f, 0.012f, 0.002f, 0.000f },
			{ 0.001f, 0.012f, 0.068f, 0.109f, 0.068f, 0.012f, 0.001f },
			{ 0.001f, 0.020f, 0.109f, 0.172f, 0.109f, 0.020f, 0.001f },
			{ 0.001f, 0.012f, 0.068f, 0.109f, 0.068f, 0.012f, 0.001f },
			{ 0.000f, 0.002f, 0.012f, 0.020f, 0.012f, 0.002f, 0.000f },
			{ 0.000f, 0.000f, 0.001f, 0.001f, 0.001f, 0.000f, 0.000f } };

	float[][] sobelX_matrix5 = { 
			{ 2, 1, 0, -1, -2 }, 
			{ 2, 1, 0, -1, -2 }, 
			{ 4, 2, 0, -2, -4 }, 
			{ 2, 1, 0, -1, -2 },
			{ 2, 1, 0, -1, -2 } };

	float[][] sobelY_matrix5 = { 
			{  2,  2,  4,  2,  2 }, 
			{  1,  1,  2,  1,  1 }, 
			{  0,  0,  0,  0,  0 }, 
			{ -1, -1, -2, -1, -1 },
			{ -2, -2, -4, -2, -2 } };

	float[][] sobelX_matrix3 = { { 2, 0, -2 }, { 4, 0, -4 }, { 2, 0, -2 } };

	float[][] sobelY_matrix3 = { { 2, 4, 2 }, { 0, 0, 0 }, { -2, -4, -2 } };
	float[][] currentMatrix;
	int currentMatrixDim;

	public ConvolutionFilter() {
		
		// should probably set the identity matrix
	}
	
	
	
	public ConvolutionFilter(String type) {
		setCurrentFilter(type);
	}
	
	BufferedImage convolveBufferedImage(BufferedImage sourceImage) {
	    int w = sourceImage.getWidth();
	    int h = sourceImage.getHeight();
	    BufferedImage outputImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());
	    
	    
	   
	      for(int y = 0; y < h; y++){
	        for(int x = 0; x < w; x++){
	        
	        int c = convolvePixel(x, y, sourceImage);
	        
	        outputImage.setRGB(x,y,c);
	        
	        }
	      }
	    
	    return outputImage;
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
				int xloc = x + j - offset;
				int yloc = y + i - offset;
				float imgval = img.getClamped(xloc, yloc);

				// Calculate the convolution
				total += (imgval * currentMatrix[i][j]);
				// println("loc total",loc,total);
			}
		}

		// Return the resulting color
		return total;
	}
	
	
	int convolvePixel(int x, int y, BufferedImage img) {
		// Ignores the alpha channel if there is one, and returns a solid image (no transparency)
		// x,y is the central pixel of the floatimage in the convolution
		float totalR = 0.0f;
		float totalG = 0.0f;
		float totalB = 0.0f;
		int offset = currentMatrixDim / 2;

		// println("current matrix dim ",currentMatrixDim);
		for (int i = 0; i < currentMatrixDim; i++) {
			for (int j = 0; j < currentMatrixDim; j++) {
				// get the image pixel clamped to the dims
				int xloc = x + j - offset;
				int yloc = y + i - offset;
				int sourcePix = img.getRGB(xloc, yloc);
				
				float rVal = MOPackedColor.getRed(sourcePix);
				float gVal = MOPackedColor.getGreen(sourcePix);
				float bVal = MOPackedColor.getBlue(sourcePix);
				
				totalR += (rVal * currentMatrix[i][j]);
				totalG += (gVal * currentMatrix[i][j]);
				totalB += (bVal * currentMatrix[i][j]);
				
				// println("loc total",loc,total);
			}
		}

		// Return the resulting color
		return MOPackedColor.packARGB(255, (int)totalR,(int)totalG,(int)totalB);
	}
	
	
	public PVector getGradient(PVector docSpace, BufferedImage img) {
		// uses a 5x5 Sobel filet.
		// returns the direction and magnitude from dark to light
		// The vector returned is in the direction of change from dark to light
		// A magnitude of 0, means the image is flat in that region using a 5x5 kernel
		KeyImageSampler kimg =  new KeyImageSampler(img);
		PVector bufferXY =  kimg.docSpaceToBufferSpace( docSpace);
		return getGradient((int)bufferXY.x, (int)bufferXY.y, kimg.getBufferedImage() );
	}
	
	PVector getGradient(int x, int y, BufferedImage img) {
		setCurrentFilter("sobelx5");
		float dx = convolvePixel( x,  y,  img);
		
		setCurrentFilter("sobely5");
		float dy = convolvePixel( x,  y,  img);

		PVector grad =  new PVector(-dx,-dy);
		
		//grad.rotate((float)Math.toRadians(90));
		return grad;
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
