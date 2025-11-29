package MOImage;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

import MOMaths.MOMaths;
import MOMaths.PVector;

/**
 * Draws a "stroke" around the alpha boundary of an image, similar to Photoshop's "stroke" command.
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
		if(brushWidth<2) {
			brushWidth=2;
		}
		createBrush(brushWidth,hardness);
	}

	/**
	 * Returns the raw edge of the alpha of the input image as an ARGB type image. Edge colour is black.
	 */
	public BufferedImage getAsARGBRawEdge(BufferedImage sourceImage) {
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();
		//System.out.println("getting raw edge");
		BufferedImage rawEdgePixels = getRawEdge(sourceImage);

		//System.out.println("converting to alpha image");
		BufferedImage alphaImage  = convertMaskToAlpha(rawEdgePixels);
		//System.out.println("done");
		return alphaImage;
		//return ImageProcessing.getCompositeImage(edgesAsAlpha, sourceImage, 0, 0,1);

	}

	public BufferedImage drawEdges(BufferedImage sourceImage) {
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();
		BufferedImage rawEdgePixels = getRawEdge(sourceImage);
		BufferedImage outputImage = new BufferedImage(width,height, sourceImage.getType());

		Graphics2D g2d= outputImage.createGraphics();
		AlphaComposite src_over = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
		g2d.setComposite(src_over);
		int halfBrushWidth = (int) (brushWidth/2f);
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				int pixelCol = rawEdgePixels.getRGB(x,y);
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


	public BufferedImage drawEdgesFixedBrushStamps(BufferedImage sourceImage) {
		// this method creates a more "resolution independent" result. In the above drawEdges(...) method, larger scaled versions of the same sourceImage  produce
		// a much larger rawEdgePixels image, resulting in many more brush applications (stamps). This makes large images produce
		// ever more dark images, and makes a resolution independent effect impossible, so session-scaling is compromised.

		// This method produces a fixed size rawEdgePixels image based on the brush width (which should be already session scaled),
		// so differently scaled, but otherwise identical,  sourceImages produce the same amount of brush stamps. As the brush is session scaled
		// the results should be much more consistent between different scales of source image.

		// The size of the rawEdgePixels image is dependent on the brush size, so that adjacent stamps are overlapping by the radius of the brush (once session scaled).
		// Full scale:   If the input width is 800, and the brush width is 20 (therefore brush radius is 10), there should be width/brush radius pixel across. The resultant edgePixelImage is 800/10 with i.e. 80 pixel wide.
		// scaling by 50%: 400 width, brush size is 5 in radius, 400/5 = 80

		width = sourceImage.getWidth();
		height = sourceImage.getHeight();


		BufferedImage brushScaledSourceImage = ImageProcessing.scaleImage(sourceImage, 1/(brushWidth/2.0f));


		BufferedImage rawEdgePixels = getRawEdge(brushScaledSourceImage);

		ArrayList<PVector> edgePoints = getWhitePixelPointList(rawEdgePixels, true);

		BufferedImage outputImage = new BufferedImage(width,height, sourceImage.getType());

		Graphics2D g2d= outputImage.createGraphics();
		AlphaComposite src_over = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
		g2d.setComposite(src_over);
		int halfBrushWidth = (int) (brushWidth/2f);


		for(PVector p: edgePoints) {
				int x = (int) (p.x * width);
				int y = (int) (p.y * height);
				g2d.drawImage(brush, x-halfBrushWidth, y-halfBrushWidth, null);
		}


		g2d.dispose();
		//System.out.println("finished drawing edges");
		return outputImage;
	}


	public BufferedImage getJitteredEdge(BufferedImage src, int numJits) {

		BufferedImage edgeImg =  getAsARGBRawEdge( src);
		if(MOMaths.isOdd(numJits)) {
			numJits+=1;
		}
		int numJistsOver2 = numJits/2;

		for(int n = -numJistsOver2; n <= numJistsOver2; n++) {
			src = ImageProcessing.getCompositeImage(edgeImg, src, n, n ,1);
		}
		return src;
	}


	/**
	 * get a (non-alpha) b/w single-pixel representation of the edges as white edge-pixels on black
	 *
	 */
	public BufferedImage getRawEdge(BufferedImage src) {
		ImageMask imageMask = new ImageMask(src,127);
		BufferedImage out =  imageMask.getEdgeImage2();
		//System.out.println("get Raw edge : image type out is " + out.getType());
		return out;
	}


	BufferedImage despeckle(BufferedImage maskImage) {
		DespeckleImage despeckle = new DespeckleImage();
		return despeckle.despeckleImage(maskImage, 2, 0);
	}


	BufferedImage convertMaskToAlpha(BufferedImage mask) {
		// creates an ARGB image from a greyscale input mask, where the mask (white) pixels get converted to black
		// and the alpha set
		int w = mask.getWidth();
		int h = mask.getHeight();
		WritableRaster maskRaster = mask.getRaster();
		BufferedImage edgeAlpha = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		WritableRaster edgeAlphaRaster = edgeAlpha.getAlphaRaster();



		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				int edgeValue = maskRaster.getSample(x, y, 0);
				if(edgeValue > 127) {
					// it is an edge pixel
					edgeAlphaRaster.setSample(x, y, 0, 255);
				}else {
					edgeAlphaRaster.setSample(x, y, 0, 0);

				}


			}



		}
		return edgeAlpha;
	}

	public ArrayList<PVector> getWhitePixelPointList(BufferedImage edgeIm, boolean useNormalisedCoordinates) {
		// returns with normalised coordinates
	    int w = edgeIm.getWidth();
	    int h = edgeIm.getHeight();

	    ArrayList<PVector> spin = new ArrayList<>();

	    for (int y = 0; y < h; y++) {
	      for (int x = 0; x < w; x++) {

	        int c = edgeIm.getRGB(x, y);
	        if ( MOPackedColor.getRed(c) > 127 ) {

	          float px = x;
	          float py = y;
	          if(useNormalisedCoordinates) {
	        	 px /= w;
	        	 py /= h;
	          }
	          spin.add(new PVector(px, py));
	        }
	      }
	    }
	    //System.out.println("buildWhitePixelPointList created with " + spin.size() + " points");
	    return spin;
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
		if( red<127 ) {
			return BLACK;
		}

		// this is where we sample every pixel around the centre pixel
		// according to the sample-matrix size
		for (int i = 0; i < 3; i++){
			for (int j= 0; j < 3; j++){

				//
				// work out which pixel are we testing
				int xloc = Xcen+i-1;
				int yloc = Ycen+j-1;

				// Make sure we haven't walked off our image
				if( xloc < 0 || xloc >= width || yloc < 0 || yloc >= height) {
					continue;
				}


				// Calculate the convolution
				col = sourceImage.getRGB(xloc,yloc);

				if(MOPackedColor.getRed(col)>127 && neighbor_matrix[i][j]==1) {
					neighbors++;
				}

			}
		}

		if( neighbors >= neighboursToSurvive) {
			return pixelCol;
		}
		return BLACK;
	}

	int despeckleBlackPixel(int Xcen, int Ycen,  BufferedImage sourceImage, int neighboursToSurvive)
	{


		int col;
		int neighbors = 0;
		int pixelCol = sourceImage.getRGB(Xcen,Ycen);
		int red = MOPackedColor.getRed(pixelCol);
		if( red>127 ) {
			return WHITE;
		}
		// this is where we sample every pixel around the centre pixel
		// according to the sample-matrix size
		for (int i = 0; i < 3; i++){
			for (int j= 0; j < 3; j++){

				//
				// work out which pixel are we testing
				int xloc = Xcen+i-1;
				int yloc = Ycen+j-1;

				// Make sure we haven't walked off our image
				if( xloc < 0 || xloc >= width || yloc < 0 || yloc >= height) {
					continue;
				}


				// Calculate the convolution
				col = sourceImage.getRGB(xloc,yloc);

				if(MOPackedColor.getRed(col)<127 && neighbor_matrix[i][j]==1) {
					neighbors++;
				}

			}
		}

		if( neighbors >= neighboursToSurvive) {
			return pixelCol;
		}
		return WHITE;
	}

}
