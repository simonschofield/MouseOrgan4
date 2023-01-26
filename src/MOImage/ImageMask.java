package MOImage;

import java.awt.Color;
import java.awt.image.BufferedImage;
/////////////////////////////////////////////////////////////////////////////
// Creates a  BW make from an ARGB image where white is inside, black is outside
// For the purposes of Edge enhancing
//
//
//
public class ImageMask {
	
	BufferedImage mask;
	int maskThreashold; 
	public ImageMask(BufferedImage sourceImage, int threashold) {
		//relies on alpha being present to create the mask
		if(sourceImage.getType() != BufferedImage.TYPE_INT_ARGB) {
			System.out.println("ImageMask: needs an ARGB type as source");
			return;
		}
		maskThreashold = threashold;
		mask = createMask(sourceImage) ;
	}
	
	BufferedImage createMask(BufferedImage sourceImage) {
		int w = sourceImage.getWidth();
		int h = sourceImage.getHeight();
		BufferedImage outputImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());

		int WHITE = MOPackedColor.packARGB(255, 255,255,255);
		int BLACK = MOPackedColor.packARGB(255, 0,0,0);

		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){

				int sourcePix = sourceImage.getRGB(x, y);

				int aVal = MOPackedColor.getAlpha(sourcePix);
				
				if(aVal < maskThreashold) {
					outputImage.setRGB(x,y,BLACK);
				}else {
					outputImage.setRGB(x,y,WHITE);
				}
				
			}
		}

		return outputImage;
	}

	BufferedImage getEdgeImage() {
	    ConvolutionFilter edgeFilter = new ConvolutionFilter("edge");
	    return edgeFilter.convolveBufferedImage(mask);
	  }
}