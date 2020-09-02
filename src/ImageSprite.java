import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.PixelGrabber;
//////////////////////////////////////////////////////////////////////
// 

// Sprite
// A sprite is a class which wraps up a Seed, and combines with 
//
//
//



public class ImageSprite{
	
	Seed seed;
	
	BufferedImage image;
	
	PVector origin = new PVector(0.5f,0.5f);
	
	PVector docPoint;
	
	float sizeInScene = 1; // 
	
	
	int bufferWidth; 
	int bufferHeight;
	float aspect;
	QRandomStream qRandomStream;
	
	SceneData3D sceneData;
	
	ImageSprite(){
		
		
	}
	
	ImageSprite(BufferedImage im, PVector orig, float sizeInScn){
		seed = new Seed(new PVector(0,0));
		init(im,orig,sizeInScn);
	}
	
	
	ImageSprite(Seed s, BufferedImage im, PVector orig, float sizeInScn) {
		//System.out.println("ImageSprite constructor sizeInScn " + sizeInScn);
		initWithSeed( s,  im,  orig, sizeInScn);
	}
	

	String toStr() {
		return "ImageSprite seed:" + seed.toStr() + " own doc pt:" + docPoint + " Image:" + image;
		
	}
	
	
	
	void init(BufferedImage im, PVector orig, float sizeInScn) {
		
		image = im;
		bufferWidth = image.getWidth();
	    bufferHeight = image.getHeight();
	    aspect = bufferWidth/(float)bufferHeight;
	    origin = orig.copy();
	    sizeInScene = sizeInScn;
	    //System.out.println("ImageSprite:init sizeInScene " + sizeInScene);
	    qRandomStream = new QRandomStream();
	}
	
	void initWithSeed(Seed s, BufferedImage im, PVector orig, float sizeInScn) {
		seed = s;
		docPoint = seed.docPoint.copy();
		init(im,orig,sizeInScn);
	}
	
	PVector getDocPoint() {
		return docPoint.copy();
	}
	
	void setDocPoint(PVector p) {
		seed.docPoint = p.copy();
		docPoint = seed.docPoint;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// geometric transforms change the sprite image in-place
	//
	void scale(float scaleW, float scaleH) {
		image = ImageProcessing.scaleImage(image, scaleW, scaleH);
		bufferWidth = image.getWidth();
		bufferHeight = image.getHeight();
	}
	
	void rotate(float degrees) {
		// this function has the visual effect of rotating the image of the sprite around the sprite origin
		// It does so by rotating the image and the origin point around the image centre, rather than rotating the image around
		// the origin, which could result in a much larger resultant image.
		// When the shape is pastes using its new origin point, it is as if it has been rotated around the origin
		// Much more efficient than otherwise
	
		double toRad = Math.toRadians(degrees);
		image = ImageProcessing.rotateImage(image, degrees);
	
		// the rest is about rotating the origin point.
		// Rotate rotation point around (0,0) in image-pixel space
		float rx = bufferWidth * (origin.x - 0.5f);
		float ry = bufferHeight * (origin.y - 0.5f);

		float newX = (float) (rx * Math.cos(toRad) - ry * Math.sin(toRad));
		float newY = (float) (ry * Math.cos(toRad) + rx * Math.sin(toRad));

		//shift rotation point back into parametric space of new image size
		bufferWidth = image.getWidth();
		bufferHeight = image.getHeight();

		origin.x = (newX / bufferWidth) + 0.5f;
		origin.y = (newY / bufferHeight) + 0.5f;
	
	}
	
	void scaleRotateSprite(float scaleX, float scaleY, float degrees) {
		// this function has the visual effect of rotating the image of the sprite around the sprite origin
		// It does so by rotating the image and the origin point  around it's centre (as normal) 
		// so that, when the shape is pasted it is as if it has been rotated around the origin
		// Much more efficient than otherwise
	
		double toRad = Math.toRadians(degrees);

		// rotate rotation point around (0,0) in image-pixel space
		float rx = bufferWidth * (origin.x - 0.5f) * scaleX;
		float ry = bufferHeight * (origin.y - 0.5f) * scaleY;

		image = ImageProcessing.scaleRotateImage(image, scaleX, scaleY, degrees);

		float newX = (float) (rx * Math.cos(toRad) - ry * Math.sin(toRad));
		float newY = (float) (ry * Math.cos(toRad) + rx * Math.sin(toRad));

		//shift rotation point back into parametric space of new image size
		bufferWidth = image.getWidth();
		bufferHeight = image.getHeight();

		origin.x = (newX / bufferWidth) + 0.5f;
		origin.y = (newY / bufferHeight) + 0.5f;
	}
	
	void mirrorSprite(boolean inX) {
		if (inX) {
			image = ImageProcessing.mirrorImage(image, true, false);
			origin.x = 1.0f - origin.x;
		} else {
			// in Y
			image = ImageProcessing.mirrorImage(image, false, true);
			origin.y = 1.0f - origin.y;
		}
	
	}
	
	boolean seedLayerEquals(String s) {
		return s.contentEquals(seed.batchName);
	}
	
	boolean contentGroupEquals(String s) {
		return seed.contentItemDescriptor.contentGroupName.contentEquals(s);
	}
	
	boolean seedBatchEquals(String s) {
		return seed.batchName.contentEquals(s);
	}
	
	// needed by the RenderTarget to paste
	PVector getOriginBufferCoords() {
	    return new PVector( (origin.x * bufferWidth), (origin.y* bufferHeight) );
	}
	
	
	QRandomStream getRandomStream() {
		return qRandomStream;
	}
	
	Rect getPasteRectDocSpace(SceneData3D sceneData) {
		// returns a document space rect to paste this sprite into
		// using the sizeInWorld as a 3d calculation
		
		// tbd -= a 2d version of this
		
		PVector docPt = seed.docPoint;
		
		float unit3DatDocPt = sceneData.get3DScale(docPt);
		float heightDocSpace = sizeInScene*unit3DatDocPt;
		float widthDocSpace = heightDocSpace * aspect;
		
		float x1 = docPt.x - (widthDocSpace* origin.x);
		float y1 = docPt.y - (heightDocSpace * origin.y);
		float x2 = docPt.x + (widthDocSpace* (1.0f-origin.x));
		float y2 = docPt.y + (heightDocSpace* (1.0f-origin.y));
		
		
		return new Rect(new PVector(x1,y1), new PVector(x2,y2) );
	}
	
	Rect getPasteRectDocSpace(RenderTarget rt) {
		PVector spriteOffset = this.getOriginBufferCoords();
		PVector docSpaceSpriteOffset = rt.bufferSpaceToDocSpace(spriteOffset);
		PVector docSpacePt = this.getDocPoint();
		PVector shiftedDocSpacePt = PVector.sub(docSpacePt, docSpaceSpriteOffset);
		return rt.getPasteRectDocSpace(this.image, shiftedDocSpacePt);
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// experimental: towards applying masked effects
	// Applies the mask image, which in an ARGB INT image, to the sprites image.Only the alpha of the mask image is considered, and is
	// overlayed with the sprite image's alpha using the Porter-Duff SRC_IN blend mode -  i.e. the source image is cropped to the mask image.
	
	void maskImage(BufferedImage mask) {
		BufferedImage mask_copy = ImageProcessing.copyImage(mask);
		if(ImageProcessing.isSameDimensions(image, mask_copy) == false) {
			mask_copy = ImageProcessing.scaleToTarget(mask, image);
		}
		image = ImageProcessing.getMaskedImage(image, mask_copy, 0, 0);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// cropping the sprite image to the permittedPasteArea
	//
	// if the render target needs to crop to the permittedPasteArea before pasting
	// The permittedPasteArea is in document space
	boolean cropToPermittedPasteArea(RenderTarget rt) {
		// assume that the sprite has been tested for being wholly-inside already, so that by here,
		// we know a crop is due.
		
		// get intersection of both in documentSpace
		Rect permittedPasteArea = rt.permittedPasteArea.permittedPasteAreaRect;
		Rect uncroppedSpriteRect = getPasteRectDocSpace(rt);
		Rect croppedSpriteRect = permittedPasteArea.getBooleanIntersection(uncroppedSpriteRect);
		
		
		// if there is no intersection between the two rects (i.e. the sprite is totally outside the permitted paste area,
		// the getBooleanIntersection method returns null and this method returns false
		if(croppedSpriteRect==null) return false;
		

		// there is some sort of crop, get the edge crop report
		String edgeCropReport = uncroppedSpriteRect.reportIntersection(permittedPasteArea);
		
		// Shift the uncroppedSpriteRect so that it is relative to it's own origin, rather than the document space  
		// 
		float uncroppedLeft = uncroppedSpriteRect.left;
		float uncroppedTop = uncroppedSpriteRect.top;
		croppedSpriteRect.translate(-uncroppedLeft, -uncroppedTop);
		
		// work out the buffer space coords in the sprite image
		PVector bTopLeft = rt.docSpaceToBufferSpace(croppedSpriteRect.getTopLeft());
		PVector bBottomRight = rt.docSpaceToBufferSpace(croppedSpriteRect.getBottomRight());
		
		
		
		Rect croppedRectBufferSpace = new Rect(bTopLeft,bBottomRight);
		//System.out.println("doBespokeCrop:croppedRectBufferSpace " + croppedRectBufferSpace.toStr());
		if(croppedRectBufferSpace.getWidth() < 1 || croppedRectBufferSpace.getHeight() < 1) return false;
		
		// As we don't want to complicate things, and don't want to have to adjust the origin
		// of the sprite to adjust to the new crop, we just delete the pixels outside of the croppedRectBufferSpace
		BufferedImage outputImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		BufferedImage preCroppedImage = ImageProcessing.cropImage(image, croppedRectBufferSpace);
		
		if( rt.permittedPasteArea.permittedPasteAreaCropImages != null){
			// add the bespoke crop to the cropping image set
			boolean result = doBespokeCrop(rt, preCroppedImage, edgeCropReport );
			if(result == false) {
				// the bespoke crop obliterated the image
				return false;
			}
		}
		
		// paste the cropped image back into the empty output image at the correct point
		ImageProcessing.compositeImage_ChangeTarget(preCroppedImage, outputImage, (int)bTopLeft.x, (int)bTopLeft.y, 1);
		image = outputImage;
		
		return true;
	}
	
	// alters the preCroppedImage
	boolean doBespokeCrop(RenderTarget rt, BufferedImage preCroppedImage, String edgeCropReport) {
		// do the bespoke crop using the selected crop image
		String splitEdgeReport[] = edgeCropReport.split(",");
		for(String edge:splitEdgeReport) {
			boolean result = addBespokeCropToEdge( rt,  preCroppedImage, edge);
			if(result == false) {
				// the addBespokeCropToEdge crop obliterated the image
				return false;
			}
		}
		return true;
	}
	
	// alters the preCroppedImage
	boolean addBespokeCropToEdge(RenderTarget rt, BufferedImage preCroppedImage, String theEdge) {
		int numCropImages = rt.permittedPasteArea.permittedPasteAreaCropImages.getNumItems();
		int n = qRandomStream.randRangeInt(0, numCropImages-1);
		BufferedImage cropper = rt.permittedPasteArea.permittedPasteAreaCropImages.getImage(n);
		int sourceImageW = preCroppedImage.getWidth();
		int sourceImageH = preCroppedImage.getHeight();
		
		if(theEdge.contentEquals("LEFT")) {
			// don't need to rotate the crop image
			if(cropper.getWidth() > sourceImageW) return false;
			cropper = matchImageSize(cropper, cropper.getWidth(), sourceImageH);
			modifyAplhaUsingGrayScaleMask(preCroppedImage, cropper, 0, 0);
			return true;
		}
		if(theEdge.contentEquals("RIGHT")) {
			cropper = ImageProcessing.rotate90(cropper, 2);
			if(cropper.getWidth() > sourceImageW) return false;
			cropper = matchImageSize(cropper, cropper.getWidth(), sourceImageH);
			modifyAplhaUsingGrayScaleMask(preCroppedImage, cropper, preCroppedImage.getWidth()-cropper.getWidth(), 0);
			return true;
		}
		if(theEdge.contentEquals("TOP")) {
			cropper = ImageProcessing.rotate90(cropper, 1);
			if(cropper.getHeight() > sourceImageH) return false;
			cropper = matchImageSize(cropper, sourceImageW, cropper.getHeight());
			modifyAplhaUsingGrayScaleMask(preCroppedImage, cropper, 0, 0);
			return true;
		}
		if(theEdge.contentEquals("BOTTOM")) {
			cropper = ImageProcessing.rotate90(cropper, 3);
			if(cropper.getHeight() > sourceImageH) return false;
			cropper = matchImageSize(cropper, sourceImageW, cropper.getHeight());
			modifyAplhaUsingGrayScaleMask(preCroppedImage, cropper, 0, preCroppedImage.getHeight()-cropper.getHeight());
			return true;
		}
		
		// asked to do something unknown
		return false;
		
	}
	
	// returns the source image resized to match the w,h, if w or h are larger than the source image
	// if the existing size in h or w is larger than h,w then crop in that dimension
	// if it is larger then scale in that dimension
	BufferedImage matchImageSize(BufferedImage source, int newW, int newH) {
		
		if( source.getWidth() > newW ) {
			Rect r = new Rect(0,0,newW, source.getHeight());
			source = ImageProcessing.cropImage(source, r);
		}
		if( source.getHeight() > newH ) {
			Rect r = new Rect(0,0,source.getWidth(), newH);
			source = ImageProcessing.cropImage(source, r);
		}
		
		
		if( source.getWidth() < newW) {
			source = ImageProcessing.scaleTo(source,newW, source.getHeight());
		}
		if( source.getHeight() < newH) {
			source = ImageProcessing.scaleTo(source,source.getWidth(), newH);
		}
		
		//System.out.println("matchImageSize: postScaledSize " + outimg.getWidth() + " " + outimg.getHeight());
		return source;
	}
	
	// alters the preCroppedImage
	void modifyAplhaUsingGrayScaleMask(BufferedImage preCroppedImage, BufferedImage maskImage, int offsetX, int offsetY) {
		// the mask image uses its own tone value to calculate the alpha written into the preCroppedImage
		int maskW = maskImage.getWidth();
		int maskH = maskImage.getHeight();
		
		Rect cropR = new Rect(offsetX, offsetY, offsetX+maskW, offsetY+maskH);
		
		BufferedImage preCroppedImageOverlap = ImageProcessing.cropImage(preCroppedImage, cropR);

		int[] maskPixelUnpacked = new int[4];
		int[] imagePixelUnpacked = new int[4];

		for(int y = 0; y < maskH; y++) {
			for(int x = 0; x < maskW; x++) {
				int maskPixel = maskImage.getRGB(x,y);
				ImageProcessing.unpackARGB(maskPixel, maskPixelUnpacked);
				int newAlpha = maskPixelUnpacked[1];// this is the red value of the pixel
				//if(newAlpha == 255) continue;
				
				int imagePixel = preCroppedImageOverlap.getRGB(x,y);
				ImageProcessing.unpackARGB(imagePixel, imagePixelUnpacked);
				int existingAlpha = imagePixelUnpacked[0];
				int newPixel = ImageProcessing.packARGB(Math.min(newAlpha, existingAlpha), imagePixelUnpacked[1], imagePixelUnpacked[2], imagePixelUnpacked[3]);
				
				preCroppedImageOverlap.setRGB(x,y,newPixel);
			}
		}
		
		
		ImageProcessing.compositeImage_ChangeTarget(preCroppedImageOverlap, preCroppedImage, offsetX, offsetY,1);
		
	}
	
	//
	// end of cropping stuff
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// scaling to 3D scene
	//
	void scaleToSizeInScene(SceneData3D sceneData, RenderTarget renderTarget, float scaleModifier) {
		// scales the image to the correct size using  sizeInScene to represent the
		// items's size in the 3D scene in world units.
		
		float heightInPixels = getHeightInRenderTargetPixels( sceneData,  renderTarget);
		//System.out.println(" scaleToSizeinScene - sizeInScene:" + sizeInScene + " scaleModifyer " + scaleModifier + " height in pixels " + heightInPixels);
		float scale = (heightInPixels/bufferHeight) * scaleModifier;
		if(scale > 1) {
		 System.out.println(seed.contentItemDescriptor.contentGroupName + " overscaled, original size in pixels " + bufferHeight + " to be scale to " + heightInPixels + " scale " + scale);
		}

		scale(scale,scale);
		//System.out.println("target size in pixels " + heightInActualPixels + " scale " + scale + " resultant height " + bufferHeight);
	}
	
	float getHeightInRenderTargetPixels(SceneData3D sceneData, RenderTarget renderTarget) {
		
		float heightDocSpace = sizeInScene * sceneData.get3DScale(seed.docPoint);
		
		PVector heightDocSpaceVector = new PVector(0, heightDocSpace);

		PVector heightInPixelsVector = renderTarget.docSpaceToBufferSpace(heightDocSpaceVector);
		return (float)Math.abs(heightInPixelsVector.y);
		
	}
	
	
	
	
	
	
	
	
}


