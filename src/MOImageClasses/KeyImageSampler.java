package MOImageClasses;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Rect;
import MOUtils.ImageCoordinateSystem;
import MOUtils.MOUtilGlobals;

////////////////////////////////////////////////////////////////////////////////
// simple wrapper round an image, so you can use document space (of the HOST application, longest edge 0..1, shortest edge 0..1/aspect) and 
// normalised  space (0..1 in both x and y) to access the data
// TBD bilinear sampling of the image

public class KeyImageSampler{
	private BufferedImage bufferedImage;
	private FloatImage floatImage;
	
	int sourceImageWidth;
	int sourceImageHeight;
	float sourceImageAspect;
	boolean sourceHasAlpha;
	
	boolean useBilinearSampling = false;
	
	Rect documentExtentsRect = new Rect();
	
	
	
	
	public KeyImageSampler(BufferedImage src){
		bufferedImage = src;
		sourceImageWidth = bufferedImage.getWidth();
		sourceImageHeight = bufferedImage.getHeight();
		sourceImageAspect = sourceImageWidth/(float)sourceImageHeight;
		sourceHasAlpha = ImageProcessing.hasAlpha(bufferedImage);
	}
	
	public KeyImageSampler(FloatImage src){
		floatImage = src;
		sourceImageWidth = floatImage.getWidth();
		sourceImageHeight = floatImage.getHeight();
		sourceImageAspect = sourceImageWidth/(float)sourceImageHeight;
		
	}
	
	public void setBilinearSampling(boolean b) {
		useBilinearSampling = b;
		
	}
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// for BufferedImages only
	//
	public BufferedImage getBufferedImage() { return bufferedImage;}
	
	public Color getPixelDocSpace(PVector docSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector np = MOUtilGlobals.theDocumentCoordSystem.docSpaceToNormalisedSpace(docSpace);
		return getPixelNormalisedSpace(np);
	}
	
	public Color getPixelNormalisedSpace(PVector p) {
		PVector pixelLoc = normalisedSpaceToBufferSpace(p);
		int packedColor =  bufferedImage.getRGB((int)pixelLoc.x, (int)pixelLoc.y);
		return ImageProcessing.packedIntToColor(packedColor, sourceHasAlpha);
	}
	
	
	public float getValue01DocSpace(PVector docSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector pixelLoc = docSpaceToBufferSpace( docSpace);
		return ImageProcessing.getValue01Clamped(bufferedImage,(int)pixelLoc.x, (int)pixelLoc.y);
	}
	
	public float getValue01NormalisedSpace(PVector p) {
		PVector pixelLoc = normalisedSpaceToBufferSpace(p);
		return ImageProcessing.getValue01Clamped(bufferedImage,(int)pixelLoc.x, (int)pixelLoc.y);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// for FloatImages only
	//
	public FloatImage getFloatImage() {return floatImage;}
	
	
	public float getFloatImageValDocSpace(PVector docSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector pixelLoc = docSpaceToBufferSpace( docSpace);
		return floatImage.get((int)pixelLoc.x, (int)pixelLoc.y);
	}
	
	public float getFloatImageValNormalisedpace(PVector normalSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector pixelLoc = normalisedSpaceToBufferSpace( normalSpace);
		return floatImage.get((int)pixelLoc.x, (int)pixelLoc.y);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//  General functions
	//
	public int getWidth() { return sourceImageWidth;}
	
	public int getHeight() { return sourceImageHeight;}
	
	public PVector normalisedSpaceToBufferSpace(PVector normSpace) {
		
		//
		//normSpace = documentExtentsRect.norm(normSpace);
		
		
		int pixelX = (int) MOMaths.constrain(normSpace.x *  sourceImageWidth , 0, sourceImageWidth-1);
		int pixelY = (int) MOMaths.constrain(normSpace.y *  sourceImageHeight , 0, sourceImageHeight-1);
		return new PVector(pixelX,pixelY);
	}
	
	public PVector docSpaceToBufferSpace(PVector docSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector np = MOUtilGlobals.theDocumentCoordSystem.docSpaceToNormalisedSpace(docSpace);
		return normalisedSpaceToBufferSpace(np);
	}
	
	
	public PVector bufferSpaceToDocSpace(PVector p) {
		PVector normPt = bufferSpaceToNormalisedSpace(p);
		return MOUtilGlobals.theDocumentCoordSystem.normalisedSpaceToDocSpace(normPt);
	}
	
	
	public PVector bufferSpaceToNormalisedSpace(PVector p) {
		float nx = p.x/sourceImageWidth;
		float ny = p.y/sourceImageHeight;
		//PVector normSpace = new PVector(nx,ny);
		
		
		
		return new PVector(nx,ny);
	
	}
}

