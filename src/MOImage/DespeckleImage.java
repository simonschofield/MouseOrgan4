package MOImage;


import java.awt.image.BufferedImage;

public class DespeckleImage{
    float[][] neighbor_matrix = { { 1,  1,  1 },
                              	  { 1,  0,  1 },
                              	  { 1,  1,  1 } };




	public BufferedImage despeckleImage(BufferedImage sourceImage, int neighboursToSurvive, int removeBlackOrWhite) {
		int w = sourceImage.getWidth();
		int h = sourceImage.getHeight();
		BufferedImage outputImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		int c;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {

				if (removeBlackOrWhite == 0) {
					c = despeckleBlackPixel(x, y, sourceImage, neighboursToSurvive);
				} else {
					c = despeckleWhitePixel(x, y, sourceImage, neighboursToSurvive);
				}
				outputImage.setRGB(x, y, c);

			}
		}

		return outputImage;
	}

	private int despeckleWhitePixel(int Xcen, int Ycen, BufferedImage sourceImage, int neighboursToSurvive) {
		// removes lonely white pixels from a black surround
		int col;

		int black = MOPackedColor.packARGB(255, 0, 0, 0);
		int neighbors = 0;
		int pixelCol = sourceImage.getRGB(Xcen, Ycen);
		if (MOPackedColor.getRed(pixelCol) < 127) {
			return black;
		}
		// this is where we sample every pixel around the centre pixel
		// according to the sample-matrix size
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				//
				// work out which pixel are we testing
				int xloc = Xcen + i - 1;
				int yloc = Ycen + j - 1;

				// Make sure we haven't walked off our image
				if (xloc < 0 || xloc >= sourceImage.getWidth() || yloc < 0 || yloc >= sourceImage.getHeight()) {
					continue;
				}

				// Calculate the convolution
				col = sourceImage.getRGB(xloc, yloc);

				if (MOPackedColor.getRed(col) > 127 && neighbor_matrix[i][j] == 1) {
					neighbors++;
				}

			}
		}

		if (neighbors >= neighboursToSurvive) {
			return pixelCol;
		}
		return black;
	}

	private int despeckleBlackPixel(int Xcen, int Ycen, BufferedImage sourceImage, int neighboursToSurvive) {
		// removes lonely black pixels from a white surround
		int col;

		int white = MOPackedColor.packARGB(255, 255, 255, 255);
		int neighbors = 0;
		int pixelCol = sourceImage.getRGB(Xcen, Ycen);
		if (MOPackedColor.getRed(pixelCol) > 127) {
			return white;
		}
		// this is where we sample every pixel around the centre pixel
		// according to the sample-matrix size
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				//
				// work out which pixel are we testing
				int xloc = Xcen + i - 1;
				int yloc = Ycen + j - 1;

				// Make sure we haven't walked off our image
				if (xloc < 0 || xloc >= sourceImage.getWidth() || yloc < 0 || yloc >= sourceImage.getHeight()) {
					continue;
				}

				// Calculate the convolution
				col = sourceImage.getRGB(xloc, yloc);

				if (MOPackedColor.getRed(col) < 127 && neighbor_matrix[i][j] == 1) {
					neighbors++;
				}

			}
		}

		if (neighbors >= neighboursToSurvive) {
			return pixelCol;
		}
		return white;
	}

}
