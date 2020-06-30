import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

 



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//produces a CMY halftone render of the sourceImage
//which is finally composited onto the output document.
//To do this the class generates three temporary images, which are initialised as copies of the render target.
//
public class CMY_HalftoneImage{
	RenderTarget cmyRenderTarget;
	
	ArrayList<PVector> gridPoints = new ArrayList<PVector>();
	Color cyan_rgb = new Color(0,211,255);
	Color magenta_rgb = new Color(255,0,165);
	Color yellow_rgb = new Color(255,237,0);

	KeyImageSampler sourceImageSampler;
	
	RenderTarget theDocument;
	Surface theSurface;
	
	Rect documentTargetRect;
	
	float screenDotSpacing;
	
	public CMY_HalftoneImage(Surface surface,  BufferedImage srcImage) {
		initialise( surface,   srcImage,  new Rect());
	}
	
	public CMY_HalftoneImage(Surface surface,  BufferedImage srcImage, Rect targetArea) {
		// the target  area is in normalised space, to be more human readable
		initialise( surface,   srcImage,  targetArea);
	}
	
	
	private void initialise(Surface surface,  BufferedImage srcImage, Rect targetArea) {
		//sourceImage = srcImage;
		sourceImageSampler = new KeyImageSampler(srcImage);
		
		theSurface = surface;
		theDocument = theSurface.theDocument;
		
		
		documentTargetRect = targetArea;
		cmyRenderTarget = new RenderTarget();
		int newWidth = (int) (documentTargetRect.getWidth()*theDocument.getBufferWidth());
		int newHeight = (int) (documentTargetRect.getHeight()*theDocument.getBufferHeight());
		cmyRenderTarget.setRenderBufferSize(newWidth, newHeight);

	}
	
	void setPermittedPasteAreaActive(boolean active) {
		// just two options, 
		// not active = hard-cropping the halftone circles to the cmyRenderTarget edge
		// active = only allowing complete halftone circles within the cmyRenderTarget image
		cmyRenderTarget.permittedPasteArea.setActive(active);
		
	}
	
	public void makeCMYhalftoneComposite(int dotSpacingPixels, boolean saveOutImages) {
		// dotSpacingPixels is the number of pixels you want the dot spacing to be at in pixels in 100% scale output image
		// i.e. when the session scale is at 100%
		float dotSpacingNomalisedSpace = (dotSpacingPixels / (float)cmyRenderTarget.getLongestBufferEdge())*theSurface.getSessionScale();
		makeCMYhalftoneComposite( dotSpacingNomalisedSpace,  saveOutImages);
	}
	
	public void makeCMYhalftoneComposite(float dotSpacingNomalisedSpace, boolean saveOutImages) {
		// spacingNomalisedSpace is with regard to THIS render target
		cmyRenderTarget.fillBackground(Color.WHITE);
		
		
		BufferedImage cyanTempImage;
		BufferedImage magentaTempImage;
		BufferedImage yellowTempImage;
		makeCMYhalftone( dotSpacingNomalisedSpace, 0, saveOutImages);
		
		cyanTempImage = cmyRenderTarget.copyImage();
		cmyRenderTarget.fillBackground(Color.WHITE);
		
		makeCMYhalftone( dotSpacingNomalisedSpace, 1, saveOutImages);
		magentaTempImage = cmyRenderTarget.copyImage();
		cmyRenderTarget.fillBackground(Color.WHITE);
		
		makeCMYhalftone( dotSpacingNomalisedSpace, 2, saveOutImages);
		yellowTempImage = cmyRenderTarget.copyImage();
		//theDocument.clearImage();
		
		//
		BufferedImage my =  ImageProcessing.multiplyImages(magentaTempImage , yellowTempImage);
		BufferedImage composite = ImageProcessing.multiplyImages(cyanTempImage , my);
		
		
		// need to convert the topleft of the documentTargetRect, which is in normalised space,
		// into document space
		PVector docSpaceTopLeft = theDocument.coordinateSpaceCoverter.normalizedSpaceToDocSpace(documentTargetRect.getTopLeft());
		theDocument.pasteImage(composite, docSpaceTopLeft, 1);
		
		if(saveOutImages) {
			String pathAndName = theSurface.getUserSessionPath() + "cmyComposite.png";
			ImageProcessing.saveImage(pathAndName, composite);
			}
		
	}
	
	public void makeCMYhalftone(float spacingDocSpace, int CMYindex, boolean saveOutImages) {
		//0 == C, 1 == M, 2 == Y
		float degrees = 45;
		Color dotColor = cyan_rgb;
		String filename = "cyan.png";
		if(CMYindex==0) {
			degrees = 45;
			dotColor = cyan_rgb;
			filename = "cyan.png";
		}
		if(CMYindex==1) {
			degrees = 15;
			dotColor = magenta_rgb;
			filename = "magenta.png";
		}
		if(CMYindex==2) {
			degrees = 0;
			dotColor = yellow_rgb;
			filename = "yellow.png";
		}

		makeHalftone( spacingDocSpace,  degrees, CMYindex, dotColor); 
		
		if(saveOutImages) {
			String pathAndName = theSurface.getUserSessionPath() + filename;
			ImageProcessing.saveImage(pathAndName, cmyRenderTarget.copyImage());
			}
		
	}
	
	
	public void makeHalftone(float spacingNormalisedSpace, float degrees, int CMYindex, Color dotColor) {
		
		screenDotSpacing = spacingNormalisedSpace;
		generateAngledGrid(screenDotSpacing,  degrees);
		
		// this square-packs the circles completely at 100% size, the 0.9f scaling is a fudge factor to avoid large areas of completely
		// filled colour, which looks odd.
		float maxDotRadius = (screenDotSpacing*1.414213562f)/2f * 0.9f;		
				
		for(PVector p: gridPoints) {
			
			
			// get the sample colour, convert to CMY
	        // then adjust the size of the dot according to the tonal value
			
			// convert the output image docspace coordinate to normalised space
			PVector pnorm = cmyRenderTarget.coordinateSpaceCoverter.docSpaceToNormalizedSpace(p);
			Color sampleCol = sourceImageSampler.getPixelColorNormalised(pnorm);
			
			float[] cmy =  RGBtoCMY(sampleCol);
	         
	        //sampleCol = self.toneAdjustPixel(sampleCol, adjustTone)

	        float thisCMYChannelValue = cmy[CMYindex];
	        float scl = (float) Math.sqrt(thisCMYChannelValue);
	        float radiusDocSpace = maxDotRadius*scl;

	        cmyRenderTarget.drawCircle(p, radiusDocSpace, dotColor, dotColor, 0);
			
			
		}
		
	}
	
	
	// Generates a parametric "square-on grid" under rotation which completely fills the render taget rectangle rect (0..1,0..1) with a spacing 
	// defined by 1/num
    // in order to do this many spurious points are calculated outside the value 0...1,
    // but ignored
    void generateAngledGrid(float spacingNormalisedSpace, float degrees) {
    	
    	gridPoints.clear();
    	
        double rads = degrees * 0.0174532925f;

        float stepSize = spacingNormalisedSpace;
        
    
        float dx = (float) Math.cos(rads);
        float dy = (float) Math.sin(rads);


        float thisX = 0;
        float startX = 0;
        float thisY = (float) (-dy*2);
        float startY = (float) (-dy*2);
        while(true) {
            PVector p = new PVector(thisX,thisY);
            
            //if(theDocument.isInsideDocumentSpace(p)) {
            //	gridPoints.add(p);
            //}
            gridPoints.add(p);

            thisX = thisX + dx*stepSize;
            thisY = thisY + dy*stepSize;

            if(thisX > cmyRenderTarget.getDocumentWidth()) {
                startX = startX - stepSize*dy;
                thisX = startX;
                startY = startY + stepSize*dx;
                thisY = startY;
            }

            if(startY >= cmyRenderTarget.getDocumentHeight()) break;
        }
        
        
    }   
        
    
	
	float[] RGBtoCMY(Color rgb) {
		// returns the cmy in the range 0..1
		float[] cmy = new float[3];
		cmy[0] = (255-rgb.getRed())/255f;
		cmy[1] = (255-rgb.getGreen())/255f;
		cmy[2] = (255-rgb.getBlue())/255f;
		return cmy;
	}
}




////////////////////////////////////////////////////////////////////////////////
// simple wrapper round an image, so you can use parametric (longest edge 0..1, shortest edge 0..1/aspect) and normalised 
// sample space
// Also does bilinear sampling of the image

class KeyImageSampler{
	BufferedImage sourceImage;
	int sourceImageWidth;
	int sourceImageHeight;
	float sourceImageAspect;
	boolean sourceHasAlpha;
	
	boolean useBilinearSampling = false;
	
	KeyImageSampler(BufferedImage src){
		sourceImage = src;
		sourceImageWidth = sourceImage.getWidth();
		sourceImageHeight = sourceImage.getHeight();
		sourceImageAspect = sourceImageWidth/(float)sourceImageHeight;
		sourceHasAlpha = ImageProcessing.hasAlpha(sourceImage);
	}
	
	void setBilinearSampling(boolean b) {
		useBilinearSampling = b;
		
	}
	
	Color getPixelColorParametric(PVector p) {
		// where the space is 0..1 on longest edge, 0..1/aspect on shortest edge of THIS image
		int pixelX = (int) MOMaths.constrain(p.x *  sourceImageWidth/maxParametricX() , 0, sourceImageWidth-1);
		int pixelY = (int) MOMaths.constrain(p.y *  sourceImageHeight/maxParametricY() , 0, sourceImageHeight-1);
		int packedColor =  sourceImage.getRGB(pixelX, pixelY);
		return ImageProcessing.packedIntToColor(packedColor, sourceHasAlpha);
	}
	
	Color getPixelColorNormalised(PVector p) {
		int pixelX = (int) MOMaths.constrain(p.x *  sourceImageWidth , 0, sourceImageWidth-1);
		int pixelY = (int) MOMaths.constrain(p.y *  sourceImageHeight , 0, sourceImageHeight-1);
		int packedColor =  sourceImage.getRGB(pixelX, pixelY);
		return ImageProcessing.packedIntToColor(packedColor, sourceHasAlpha);
	}
	
	// this must return a number equal to or less than 1
	float maxParametricX() {
		if(sourceImageAspect>=1)  return 1;
		return sourceImageAspect;
	}
	
	// this must return a number equal to or less than 1
	float maxParametricY() {
		if(sourceImageAspect<=1)  return 1;
		return 1/sourceImageAspect;
	}
	
}

