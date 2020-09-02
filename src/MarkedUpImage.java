import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

/////////////////////////////////////////////////////////////////////////
// This class turns a pre-prepared ARGB image (the source image) into 4 split images, based on each
// channel of the source image. The four output images are A -> graphicImage, R-> regionInteriorImage, G-> linkPointsImage, B-> unuseddataImage
// The pre-prepared image is intended to be used in Map making. The individual channels contain the following data
// A: this is kept as the visual part of the graphics (the bit that gets pasted). The RGB is set to black, so we have a black, alphered image
// R: The internal "fill" area of a region. The R channel becomes the alpha channel of the image, the rest set to black
// G: The link-points image. This is extracted and kept, it is analysed and a list of PVector link-points (in PSpace) produced
// B: currently discarded
public class MarkedUpImage {
	BufferedImage graphicImage;
	BufferedImage regionInteriorImage;
	BufferedImage linkPointsImage;
	ArrayList<PVector> linkPoints;
	
	public MarkedUpImage() {
		
	}
	
	public BufferedImage loadMarkupTiff(String filePathAndName) {
		BufferedImage loadedImage = ImageProcessing.loadImage(filePathAndName);
		ingestMarkupImage(loadedImage);
		return loadedImage;
	}
	
	public void ingestMarkupImage(BufferedImage src) {

		src = ImageProcessing.assertImageTYPE_INT_ARGB(src);

	    int w = src.getWidth();
	    int h = src.getHeight();

	    //int[] srcBuffer = src.getData().getPixels(0, 0, w, h, (int[]) null);
	    
	    int[] srcBuffer = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
	    graphicImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    regionInteriorImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    linkPointsImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    
	    int[] graphicImageBuffer = ((DataBufferInt) graphicImage.getRaster().getDataBuffer()).getData();
	    int[] regionInteriorImageBuffer = ((DataBufferInt) regionInteriorImage.getRaster().getDataBuffer()).getData(); 
	    int[] linkPointsImageBuffer = ((DataBufferInt) linkPointsImage.getRaster().getDataBuffer()).getData(); 
		
	  
	    int[] sourcePixelUnpacked = new int[4];
	    int bufferlength = w*h;
	    
	    for (int i=0; i<bufferlength; i++) {
	    	sourcePixelUnpacked = ImageProcessing.unpackARGB(srcBuffer[i]);
	    	
	    	// set the A of the graphicsImage to the A of the sourceImage 
	    	int alphaVal = sourcePixelUnpacked[0];
	    	int redVal = sourcePixelUnpacked[1]; 
	    	int greenVal = sourcePixelUnpacked[2]; 
	    	int blueVal = sourcePixelUnpacked[3]; 
	    	if(i%100 == 0) System.out.println("raw pix " + srcBuffer[i] + "A R G B = " + alphaVal + " " + redVal + " " + greenVal + " " + blueVal);
	    	graphicImageBuffer[i] = ImageProcessing.packARGB(alphaVal, 0, 0, 0);
	    	
	    	// set the A of the regionInteriorPixelUnpacked to the R of the sourceImage 
	    	regionInteriorImageBuffer[i] = ImageProcessing.packARGB(sourcePixelUnpacked[1], 0, 0, 0);
	    	
	    	// set the A of the linkPointsPixelUnpacked to the G of the sourceImage 
	    	linkPointsImageBuffer[i] = ImageProcessing.packARGB(sourcePixelUnpacked[2], 0, 0, 0);
	    }

	    
	}
	
	
	void testSave(String path) {
		String graphicImagePathAndName = path + "\\testGraphic.png";
		String regionInteriorImagePathAndName = path + "\\testRegionInterior.png";
		String linkPointsImagePathAndName = path + "\\testLinkPoints.png";
		ImageProcessing.saveImage( graphicImagePathAndName, graphicImage);
		ImageProcessing.saveImage( regionInteriorImagePathAndName, regionInteriorImage);
		ImageProcessing.saveImage( linkPointsImagePathAndName, linkPointsImage);
	}

}
