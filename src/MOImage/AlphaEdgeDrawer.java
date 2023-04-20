package MOImage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;

import MOMaths.MOMaths;
import MOMaths.PVector;

/**
 * Draws a "stroke" around the alpha boundary of an image, simular to Photoshops "stroke" command.
 * All images during these processed are maintained in ARGB space to allow for speed-hikes
 *  The mask image is ARGB, where all alpha is set to 255, but the image is black or white
 *  Same for Despeckle
 * 
 */
public class AlphaEdgeDrawer {



	public static final int CLAMP_EDGES = 0;
	public static final int WRAP_EDGES = 1;

	protected float radius;
	int width,height;

	int brushWidth;
	
	
	BufferedImage brush;
	
	/**
	 * Construct a Gaussian filter
	 * @param brushWidth in pixels, so will need prior session-scaling. Minimum is 2. 
	 * @param hardness is the distribution curve of the generated brush. A harness of 1 is quite hard, for a softer brush use 0.5 
	 */
	public AlphaEdgeDrawer(int brushWidth, float hardness) {
		if(brushWidth<2) brushWidth=2;
		createBrush(brushWidth,hardness);
	}

	
	
	
	public BufferedImage drawEdges(BufferedImage sourceImage) {
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();
		BufferedImage maskImage = getRawEdge(sourceImage);
		BufferedImage outputImage = new BufferedImage(width,height, sourceImage.getType());

		Graphics2D g2d= outputImage.createGraphics();
		AlphaComposite src_over = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
		g2d.setComposite(src_over);
		int halfBrushWidth = (int) (brushWidth/2f);
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int pixelCol = maskImage.getRGB(x,y);
				if( MOPackedColor.getRed(pixelCol) < 127) {
					continue;
				} else {
					
					//drawBrush(x,y,outputImage);
					g2d.drawImage(brush, x-halfBrushWidth, y-halfBrushWidth, null);
					
				}
				

			}
		}
		g2d.dispose();
		//System.out.println("finished drawing edges");
		return outputImage;
	}
	
	/**
	 * get a (non-alpha) b/w single-pixel representation of the edges as white edge-pixels on black
	 *
	 */
	public BufferedImage getRawEdge(BufferedImage src) {
		ImageMask imageMask = new ImageMask(src,127);
		return imageMask.getEdgeImage();

	}
	

	//////////////////////////////////////////////////////////////////////////////////
	// called at the start to make the brush
	//
	//
	private void createBrush(int size, float sd) {
		brushWidth = size;

		brush = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);

		for (int j = 0; j < brushWidth; j++) {
			for (int i = 0; i < brushWidth; i++) {
				int a = (int) (255*gaussianValue(sd, i, j, brushWidth));
				int argb = MOPackedColor.packARGB(a, 0,0,0);
				brush.setRGB(i, j, argb);
			}
		
		}

		
	}

	private float gaussianValue(float sd, int x, int y, int matrixDim) {
		float cx = (matrixDim-1) / 2.0f;
		float distToCentre = PVector.dist(new PVector(cx, cx, 0), new PVector(x, y, 0));
		
		return MOMaths.gaussianCurve(distToCentre, 1.0f, 0, sd*cx);
	}



}




class Despeckle{
	float[][] neighbor_matrix = { { 1,  1,  1 },
			{ 1,  0,  1 },
			{ 1,  1,  1 } };

	int WHITE, BLACK;
	int width,height;

	BufferedImage despeckleImage(BufferedImage sourceImage, int neighboursToSurvive, int removeBlackOrWhite) {
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();
		BufferedImage outputImage = new BufferedImage(width,height,sourceImage.getType());
		WHITE = MOPackedColor.packARGB(255, 255,255,255);
		BLACK = MOPackedColor.packARGB(255, 0,0,0);

		int c;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){

				if(removeBlackOrWhite==0){
					c = despeckleBlackPixel(x, y, sourceImage, neighboursToSurvive);
				}else{
					c = despeckleWhitePixel(x, y, sourceImage, neighboursToSurvive);
				}
				outputImage.setRGB(x,y,c);

			}
		}

		return outputImage;
	}






	int despeckleWhitePixel(int Xcen, int Ycen,  BufferedImage sourceImage, int neighboursToSurvive)
	{
		// removes lonely white pixels from a black surround
		int col;


		int neighbors = 0;
		int pixelCol = sourceImage.getRGB(Xcen,Ycen);
		int red = MOPackedColor.getRed(pixelCol);
		if( red<127 ) return BLACK;

		// this is where we sample every pixel around the centre pixel
		// according to the sample-matrix size
		for (int i = 0; i < 3; i++){
			for (int j= 0; j < 3; j++){

				//
				// work out which pixel are we testing
				int xloc = Xcen+i-1;
				int yloc = Ycen+j-1;

				// Make sure we haven't walked off our image
				if( xloc < 0 || xloc >= width) continue;
				if( yloc < 0 || yloc >= height) continue;


				// Calculate the convolution
				col = sourceImage.getRGB(xloc,yloc);

				if(MOPackedColor.getRed(col)>127 && neighbor_matrix[i][j]==1) neighbors++;

			}
		}

		if( neighbors >= neighboursToSurvive) return pixelCol;
		return BLACK;
	}

	int despeckleBlackPixel(int Xcen, int Ycen,  BufferedImage sourceImage, int neighboursToSurvive)
	{


		int col;
		int neighbors = 0;
		int pixelCol = sourceImage.getRGB(Xcen,Ycen);
		int red = MOPackedColor.getRed(pixelCol);
		if( red>127 ) return WHITE;
		// this is where we sample every pixel around the centre pixel
		// according to the sample-matrix size
		for (int i = 0; i < 3; i++){
			for (int j= 0; j < 3; j++){

				//
				// work out which pixel are we testing
				int xloc = Xcen+i-1;
				int yloc = Ycen+j-1;

				// Make sure we haven't walked off our image
				if( xloc < 0 || xloc >= width) continue;
				if( yloc < 0 || yloc >= height) continue;


				// Calculate the convolution
				col = sourceImage.getRGB(xloc,yloc);

				if(MOPackedColor.getRed(col)<127 && neighbor_matrix[i][j]==1) neighbors++;

			}
		}

		if( neighbors >= neighboursToSurvive) return pixelCol;
		return WHITE;
	}  

}
