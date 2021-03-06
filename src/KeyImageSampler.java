import java.awt.Color;
import java.awt.image.BufferedImage;

////////////////////////////////////////////////////////////////////////////////
// simple wrapper round an image, so you can use document space (of the HOST application, longest edge 0..1, shortest edge 0..1/aspect) and 
// normalised  space (0..1 in both x and y) to access the data
// TBD bilinear sampling of the image

class KeyImageSampler{
	private BufferedImage bufferedImage;
	private FloatImage floatImage;
	int sourceImageWidth;
	int sourceImageHeight;
	float sourceImageAspect;
	boolean sourceHasAlpha;
	
	boolean useBilinearSampling = false;
	
	Rect documentExtentsRect = new Rect(0,0,1,1);
	
	
	KeyImageSampler(BufferedImage src){
		bufferedImage = src;
		sourceImageWidth = bufferedImage.getWidth();
		sourceImageHeight = bufferedImage.getHeight();
		sourceImageAspect = sourceImageWidth/(float)sourceImageHeight;
		sourceHasAlpha = ImageProcessing.hasAlpha(bufferedImage);
	}
	
	KeyImageSampler(FloatImage src){
		floatImage = src;
		sourceImageWidth = floatImage.getWidth();
		sourceImageHeight = floatImage.getHeight();
		sourceImageAspect = sourceImageWidth/(float)sourceImageHeight;
		
	}
	
	void setBilinearSampling(boolean b) {
		useBilinearSampling = b;
		
	}
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// for BufferedImages only
	//
	BufferedImage getBufferedImage() { return bufferedImage;}
	
	Color getPixelDocSpace(PVector docSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector np = GlobalObjects.theDocument.docSpaceToNormalisedSpace(docSpace);
		return getPixelNormalisedSpace(np);
	}
	
	Color getPixelNormalisedSpace(PVector p) {
		PVector pixelLoc = normalisedSpaceToBufferSpace(p);
		int packedColor =  bufferedImage.getRGB((int)pixelLoc.x, (int)pixelLoc.y);
		return ImageProcessing.packedIntToColor(packedColor, sourceHasAlpha);
	}
	
	
	float getValue01DocSpace(PVector docSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector pixelLoc = docSpaceToBufferSpace( docSpace);
		return ImageProcessing.getValue01Clamped(bufferedImage,(int)pixelLoc.x, (int)pixelLoc.y);
	}
	
	float getValue01NormalisedSpace(PVector p) {
		PVector pixelLoc = normalisedSpaceToBufferSpace(p);
		return ImageProcessing.getValue01Clamped(bufferedImage,(int)pixelLoc.x, (int)pixelLoc.y);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// for FloatImages only
	//
	FloatImage getFloatImage() {return floatImage;}
	
	
	float getFloatImageValDocSpace(PVector docSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector pixelLoc = docSpaceToBufferSpace( docSpace);
		return floatImage.get((int)pixelLoc.x, (int)pixelLoc.y);
	}
	
	float getFloatImageValNormalisedpace(PVector normalSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector pixelLoc = normalisedSpaceToBufferSpace( normalSpace);
		return floatImage.get((int)pixelLoc.x, (int)pixelLoc.y);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//  General functions
	//
	int getWidth() { return sourceImageWidth;}
	
	int getHeight() { return sourceImageHeight;}
	
	PVector normalisedSpaceToBufferSpace(PVector normSpace) {
		
		//
		//normSpace = documentExtentsRect.norm(normSpace);
		
		
		int pixelX = (int) MOMaths.constrain(normSpace.x *  sourceImageWidth , 0, sourceImageWidth-1);
		int pixelY = (int) MOMaths.constrain(normSpace.y *  sourceImageHeight , 0, sourceImageHeight-1);
		return new PVector(pixelX,pixelY);
	}
	
	PVector docSpaceToBufferSpace(PVector docSpace) {
		// the document space refers to the DocSpace of the HOST application
		PVector np = GlobalObjects.theDocument.docSpaceToNormalisedSpace(docSpace);
		return normalisedSpaceToBufferSpace(np);
	}
	
	
	PVector bufferSpaceToDocSpace(PVector p) {
		PVector normPt = bufferSpaceToNormalisedSpace(p);
		return GlobalObjects.theDocument.normalisedSpaceToDocSpace(normPt);
	}
	
	
	PVector bufferSpaceToNormalisedSpace(PVector p) {
		float nx = p.x/sourceImageWidth;
		float ny = p.y/sourceImageHeight;
		//PVector normSpace = new PVector(nx,ny);
		
		
		
		return new PVector(nx,ny);
	
	}
}

