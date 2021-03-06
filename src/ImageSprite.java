
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;


//////////////////////////////////////////////////////////////////////
// Enables the tracking of arbitrary points within a sprite after multiple transforms to the sprite
// The point is accessed using normalised coordinates within the sprite's image-space, as if the shape has not been transformed
// ( (0,0) is top left (1,1) is bottom right), in the same way the sprite's origin is defined.
// So, after scaling, rotating, mirroring and bending a sprite, you can find where point (0.75,0.666) (using normalised coords) 
// is positioned within documentSpace
// This then enables you to 
// 	- sample a key image at transformedImagePoint to determine what the value is under that sprite at that point
// 	- link sprites together at known points to create linked and hinged items

// All internal units are in the pixel coordinate space (although with float accuracy).

// The transforms should be applied AFTER the BufferedImage has been transformed, as the calculation for ROT and SHEAR
// depend on the new BufferedImage Size
//

class ImageQuad{
	ImageSprite theSprite;
	
	
	// these are the points which are transformed
	PVector topLeft, topRight, bottomLeft, bottomRight;
	
	ImageQuad(ImageSprite sprite){
		theSprite = sprite;
		

		topLeft = PVector.ZERO();
		topRight = new PVector(theSprite.image.getWidth(),0);
		bottomLeft = new PVector(0,theSprite.image.getHeight());
		bottomRight = new PVector(theSprite.image.getWidth(),theSprite.image.getHeight());
	}
	
	
	
	
	PVector getQuadDocumentPoint(PVector pastePoint, float normX, float normY) {
		// pastePoint is in document space
		// normx and normy are in normalised space within the original sprite image-space - so before any transforms are applied
		
		// this is the key function. It enables the user to track any point in the original image
		// after multiple transformations. 
		// so given a pastePoint in document space, you may wish to know where the top centre point of the image has got to
		// in documentSpace coordinates
		// PVector transformedImagePoint = getDocumentPointUsingNormalisedCoordinates( pastePoint, 0.5 , 0.0) would return that point
		
		PVector pixelLocOfEnquiryPtInQuad = getQuadPointBufferCoords( normX,  normY);
		//System.out.println(debugQuadPointsToStr());
		//System.out.println("Pixel Loc in sprite of Enquiry Nom Point " + normX + " " + normY + " is " + pixelLocOfEnquiryPtInQuad.toStr());
		//PVector spriteOriginPixelLoc = new PVector(theSprite.origin.x * getImageWidth(), theSprite.origin.y * getImageHeight());
		PVector spriteOriginPixelLoc = theSprite.getOriginBufferCoords();
		PVector pixCoordOfDocumentPastePoint = GlobalObjects.theDocument.docSpaceToBufferSpace(pastePoint);
		
		PVector pixelOffsetEnquryPointFomSpriteOrigin = new PVector(pixelLocOfEnquiryPtInQuad.x - spriteOriginPixelLoc.x, pixelLocOfEnquiryPtInQuad.y - spriteOriginPixelLoc.y);
		PVector pixelCoordinateInDocument = pixCoordOfDocumentPastePoint.add(pixelOffsetEnquryPointFomSpriteOrigin);
		return GlobalObjects.theDocument.bufferSpaceToDocSpace(pixelCoordinateInDocument);
	}
	
	
	
	PVector getQuadPointBufferCoords(float normX, float normY) {
		// returns the pixel location of a quad point within the 
		// sprite's image
		PVector topLinePoint = PVector.lerp(topLeft, topRight, normX);
		PVector bottomLinePoint = PVector.lerp(bottomLeft, bottomRight, normX);
		return PVector.lerp(topLinePoint, bottomLinePoint, normY);
		
	}
	
	void applyTranslation(PVector t) {
		topLeft = topLeft.add(t);
		topRight = topRight.add(t);
		bottomLeft = bottomLeft.add(t);
		bottomRight = bottomRight.add(t);
		
	}
	
	
	void applyScale(float sx, float sy) {
		// should always work even if the image had been previously rotated
		topLeft = topLeft.scale(sx, sy);
		topRight = topRight.scale(sx, sy);
		bottomLeft = bottomLeft.scale(sx, sy);
		bottomRight = bottomRight.scale(sx, sy);
		
	}
	
	void applyRotation(float degrees, PVector oldCentre) {
		// rotates round the centre of the image
		topLeft = MOMaths.rotatePoint(topLeft, oldCentre, degrees);
		topRight = MOMaths.rotatePoint(topRight, oldCentre, degrees);
		bottomLeft = MOMaths.rotatePoint(bottomLeft, oldCentre, degrees);
		bottomRight = MOMaths.rotatePoint(bottomRight, oldCentre, degrees);
		
		// then shift all the points to be round the new centre
		// i.e. shift all by newCentre-oldCentre
		PVector newCentre = getCentre();
		PVector centreOffset = newCentre.sub(oldCentre);
		applyTranslation(centreOffset);
	}
	
	
	void applyHorizontalTopShear(float dxTop) {
		// applies the shear to the image
		// so quad points are sheared according to their relative Y position within the image rectangle
		
		
		float tlYNorm = MOMaths.norm(topLeft.y, getImageHeight(), 0);
		float trYNorm = MOMaths.norm(topRight.y,  getImageHeight(), 0);
		float blYNorm = MOMaths.norm(bottomLeft.y, getImageHeight(), 0);
		float brYNorm = MOMaths.norm(bottomRight.y,  getImageHeight(), 0);
		
		topLeft.x += (dxTop * tlYNorm);
		topRight.x += (dxTop * trYNorm);
		bottomLeft.x += (dxTop * blYNorm);
		bottomRight.x += (dxTop * brYNorm);
		
	}
	
	void applyMirror(boolean inX) {
		
		if(inX) {
			// flip the x coords around the x axis
			float w = getImageWidth();
			topLeft.x = w - topLeft.x;
			topRight.x = w - topRight.x;
			bottomLeft.x = w - bottomLeft.x;
			bottomRight.x = w - bottomRight.x;
		} else {
			// flip the y coords around the Y axis
			float h = getImageHeight();
			topLeft.y = h - topLeft.y;
			topRight.y = h - topRight.y;
			bottomLeft.y = h - bottomLeft.y;
			bottomRight.y = h - bottomRight.y;
		}
		
		
	}
	
	float getImageWidth() {
		return theSprite.image.getWidth();
	}
	
	float getImageHeight() {
		return theSprite.image.getHeight();
	}
	
	PVector getCentre() {
		return new PVector(getImageWidth()/2f, getImageHeight()/2f);
	}
	
	
	////////////////////////////////////
	// debugging functions
	void debugDrawQuadCorners(PVector pastePoint) {
		debugDrawQuadPoint( pastePoint, 0, 0, Color.BLACK);
		debugDrawQuadPoint( pastePoint, 1, 0, Color.BLACK);
		debugDrawQuadPoint( pastePoint, 0, 1, Color.BLACK);
		debugDrawQuadPoint( pastePoint, 1, 1, Color.BLACK);
		}
		
	
	void debugDrawQuadPoint(PVector pastePoint, float nx, float ny, Color c) {
		PVector docSpacePoint = getQuadDocumentPoint( pastePoint, nx, ny);
		GlobalObjects.theDocument.drawPoint(docSpacePoint, c, 5);
		}
		
		
	String debugQuadPointsToStr() {
		
		return "topLeft " + topLeft.toStr() + ", topRight " + topRight.toStr() + "bottomLeft " + bottomLeft.toStr() + ", bottomRight " + bottomRight.toStr();
	}
	
	
	
}











//////////////////////////////////////////////////////////////////////
// 
// ImageSprite combines with a BufferedImage
// and other data (origin (pivot point), docPoint (also in seed), sizeInScene
// ImageQuad, it's own random stream
//
// Mouse organ only really considers one imageSprite at a time, after most decisions
// have been made about what goes where (e.g. using seeds)


public class ImageSprite{
	
	
	// critical (always needed) information
	PVector docPoint = PVector.ZERO();
	
	BufferedImage image;
	
	PVector origin = new PVector(0.5f,0.5f);

	float sizeInScene = 1; // 
	
	// secondary (sometimes needed) information
	String imageSampleGroupName = "";
	String shortImageFileName = "";
	String seedBatchName = "";
	int id = 0;
	
	// for internal workings
	private ImageQuad imageQuad;
	
	private int bufferWidth; 
	private int bufferHeight;
	private float aspect;
	
	private QRandomStream qRandomStream;
	
	private SceneData3D sceneData;
	
	
	
	ImageSprite(){
		
		
	}
	
	
	ImageSprite(BufferedImage img, PVector orig, float sizeInScn, int id ){

		setImage(img); 
		
		this.sizeInScene = sizeInScn;
		this.origin = orig;
	    imageQuad = new ImageQuad(this);
	    setID_RandomSeed(id);
	}
	
	void setID_RandomSeed(int rseed) {
		// not sure sprites need a unique ID, however
		// seeds do have one, and a sprite's random stream is set by this
		// therefore guaranteeing reproducible effects to this sprite.
		// When is sprite created without a seed, the sprite batch manager
		// generates a sprite using a unique ID. 
		this.id = rseed;
		qRandomStream = new QRandomStream(rseed);
	}
	
	String toStr() {
		//return "ImageSprite seed:" + seed.toStr() + " own doc pt:" + docPoint + " Image:" + image;
		return "ImageSprite doc pt:" + docPoint + " Image:" + image;
	}
	
	
	void setImage(BufferedImage img) {
		this.image = img;
		this.bufferWidth = image.getWidth();
		this.bufferHeight = image.getHeight();
		this.aspect = bufferWidth/(float)bufferHeight;
		imageQuad = new ImageQuad(this);
	}
	
	PVector getDocPoint() {
		return docPoint.copy();
	}
	
	void setDocPoint(PVector p) {
		docPoint = p.copy();
	}
	
	
	PVector getQuadPoint(PVector docPt, float  nx, float ny) {
		return imageQuad.getQuadDocumentPoint(docPt, nx, ny);
	}
	
	
	SNum getSNum(int seedOffset, Integer sequencePos) {
		return qRandomStream.snum(seedOffset, sequencePos);
	}
	
	int getImageBufferWidth() {
		return bufferWidth;
		
	}
	
	int getImageBufferHeight() {
		return bufferHeight;
		
	}
	
	float getAspect() {
		this.aspect = bufferWidth/(float)bufferHeight;
		return this.aspect;
	}
	
	String getImageName() {
		return shortImageFileName;
	}
	
	
	boolean imageNameContains(String s) {
		return shortImageFileName.contains(s);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// geometric transforms change the sprite image in-place
	//
	void scale(float scaleW, float scaleH) {
		image = ImageProcessing.scaleImage(image, scaleW, scaleH);
		bufferWidth = image.getWidth();
		bufferHeight = image.getHeight();
		
		//imageQuad.theImage = image;
		imageQuad.applyScale(scaleW, scaleH);
	}
	
	void rotate(float degrees) {
		// this function has the visual effect of rotating the image of the sprite around the sprite origin
		// It does so by rotating the image and the origin point around the image centre, rather than rotating the image around
		// the origin, which could result in a much larger resultant image.
		// When the shape is pastes using its new origin point, it is as if it has been rotated around the origin
		// Much more efficient than otherwise
		PVector oldCentre = imageQuad.getCentre();
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
		//PVector newCentre = imageQuad.getCentre();
		imageQuad.applyRotation(degrees, oldCentre);
		
	}
	
	
	
	void mirror(boolean inX) {
		if (inX) {
			image = ImageProcessing.mirrorImage(image, true, false);
			origin.x = 1.0f - origin.x;
		} else {
			// in Y
			image = ImageProcessing.mirrorImage(image, false, true);
			origin.y = 1.0f - origin.y;
		}
		imageQuad.applyMirror(inX);
	}
	
	
	void bend(float startBend, float bendAmt, float severity) {
		BendImage bendImage = new BendImage();
		float oldWidth = imageQuad.getImageWidth();
		this.image = bendImage.bendImage(this.image, startBend, bendAmt, severity);
		bufferWidth = image.getWidth();
		bufferHeight = image.getHeight();
		
		
		
		
		// A -ve    bend means a shift of the top to the left
		// It applied only to the top of the image
		float shift = ((bufferWidth - oldWidth) * MOMaths.getUnitSign(bendAmt));
		imageQuad.applyHorizontalTopShear(Math.abs(shift));
		
		// have to recalculate the origin.x to compensate for the image width getting larger
		origin.x = origin.x * (oldWidth/bufferWidth);
		if(bendAmt < 0) {
			// flip the origin
			origin.x = 1 - origin.x;
			imageQuad.applyMirror(true);
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// Experimental - needs generalising in terms of what point gets mapped to where
	// map to line
	// The origin is set to be the bottom centre of the sprite. This gets mapped to p1.
	// The other end gets mapped to p2. The sprite is scaled so that it is as high as the line p1->p2
	// The sprite is rotated into the same direction as the line p1-p2
	void mapToLine2(Line2 line, float overlap) {
		origin = new PVector(0.5f, 1.0f);
		float r = line.getRotation();
		float len = line.getLength()*overlap;
		//System.out.println( "BEFORE SCALE sprite width " + this.getImageBufferWidth() + " sprite height " + this.getImageBufferHeight() + " aspect = " + getAspect());
		scaleToSizeInDocSpace(null, len);
		//System.out.println( "AFTER SCALE sprite width " + this.getImageBufferWidth() + " sprite height " + this.getImageBufferHeight() + " aspect = " + getAspect() + "\n");
		rotate(r);
		setDocPoint(line.p1);
	}
	
	
	void scaleToDocSpace(float docSpaceWidth, float docSpaceHeight) {
		float widthInPixels = docSizeToRenderTargetPixels2D(docSpaceWidth);
		float scalerX = (widthInPixels / bufferWidth);
		
		float heightInPixels = docSizeToRenderTargetPixels2D(docSpaceHeight);
		float scalerY = (heightInPixels / bufferHeight);
		
		//System.out.println(" scaleWidthToDocSpace scaleing by " + scaler);
		scale(scalerX,scalerY);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// scaling to 3D scene
	//
	void scaleToSizeInScene(SceneData3D sceneData, float scaleModifier) {
		// scales the image to the correct size using  sizeInScene to represent the
		// items's size in the 3D scene in world units.

		float heightInPixels = getHeightInRenderTargetPixels3D(sceneData);
		//System.out.println(" scaleToSizeinScene - sizeInScene:" + sizeInScene + " scaleModifyer " + scaleModifier + " height in pixels " + heightInPixels);
		float scale = (heightInPixels / bufferHeight) * scaleModifier;
		if (scale > 1) {
			System.out.println(imageSampleGroupName + " overscaled, original size in pixels " + bufferHeight
					+ " to be scale to " + heightInPixels + " scale " + scale);
		}

		scale(scale, scale);
		//System.out.println("target size in pixels " + heightInActualPixels + " scale " + scale + " resultant height " + bufferHeight);
	}

	float getHeightInRenderTargetPixels3D(SceneData3D sceneData) {

		float heightDocSpace = sizeInScene * sceneData.get3DScale(docPoint);
		return docSizeToRenderTargetPixels2D(heightDocSpace);

	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// scaling to 2D scene
	//
	void scaleToSizeInScene(float scaleModifier) {
		// The height of the sample image is set using pre-set sizeInScene member variable as a documentSpace measurement.
		// i.e. sizeInScene of 1 means that the image is scaled to be the same as the longest edge of the document
		float scale = scaleModifier * sizeInScene;
		//System.out.println("scaleToSizeInScene  scaleModifier " + scaleModifier + "  sizeInScene " + sizeInScene + " result " + scale);
		scaleToSizeInDocSpace(null, scale);
	}

	void scaleToSizeInDocSpace(Float sizeX, Float sizeY) {
		// i.e. size of 1 means that the image is scaled to be the same as the longest edge of the document
		if (sizeX == null && sizeY == null)
			return;
		float scaleY = 1;
		float scaleX = 1;
		//System.out.println("scaleToSizeInDocSpace  sizeXY " + sizeX + "  " + sizeY);
		if (sizeX != null) {
			float widthInPixels = docSizeToRenderTargetPixels2D(sizeX);
			scaleX = (widthInPixels / bufferWidth);
			
			if (scaleX > 2) {
				System.out.println(
						imageSampleGroupName + "/" + shortImageFileName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
								+ bufferWidth + " to be scale to " + widthInPixels + " scale " + scaleX);
			}
		}

		if (sizeY != null) {
			float heightInPixels = docSizeToRenderTargetPixels2D(sizeY);
			scaleY = (heightInPixels / bufferHeight);


			if (scaleY > 2) {
				System.out.println(
						imageSampleGroupName + "/" + shortImageFileName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
								+ bufferHeight + " to be scale to " + heightInPixels + " scale " + scaleY);
			}
			
		}
		//System.out.println("scaleToSizeInDocSpace  scaleXY " + scaleX + "  " + scaleY);
		
		if(sizeX == null) {
			//System.out.println("scaling in Y only " + scaleY);
			scale(scaleY, scaleY);
			return;
		}
		
		if(sizeY == null) {
			//System.out.println("scaling in X only " + scaleX);
			scale(scaleX, scaleX);
			return;
		}
		
		System.out.println("scaling in X and Y " + scaleX + "," + scaleY);
		scale(scaleX, scaleY);

		//
	}

	float docSizeToRenderTargetPixels2D(float size) {

		PVector heightDocSpaceVector = new PVector(0, size);
		PVector heightInPixelsVector = GlobalObjects.theDocument.docSpaceToBufferSpace(heightDocSpaceVector);
		return (float) Math.abs(heightInPixelsVector.y);

	}



	
	///////////////////////////////////////////////////////////////////////////////////////////
	/// end of geometric transforms
	///////////////////////////////////////////////////////////////////////////////////////////
	
	
	boolean imageSampleGroupEquals(String s) {
		return imageSampleGroupName.contentEquals(s);
	}
	
	boolean seedBatchEquals(String s) {
		return seedBatchName.contentEquals(s);
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
		
		PVector docPt = docPoint.copy();
		
		float unit3DatDocPt = sceneData.get3DScale(docPt);
		float heightDocSpace = sizeInScene*unit3DatDocPt;
		float widthDocSpace = heightDocSpace * aspect;
		
		float x1 = docPt.x - (widthDocSpace* origin.x);
		float y1 = docPt.y - (heightDocSpace * origin.y);
		float x2 = docPt.x + (widthDocSpace* (1.0f-origin.x));
		float y2 = docPt.y + (heightDocSpace* (1.0f-origin.y));
		
		
		return new Rect(new PVector(x1,y1), new PVector(x2,y2) );
	}
	
	Rect getPasteRectDocSpace(MainDocumentRenderTarget rt) {
		PVector spriteOffset = this.getOriginBufferCoords();
		
		PVector docSpaceSpriteOffset = rt.bufferSpaceToDocSpace(spriteOffset);
		PVector docSpacePt = this.getDocPoint();
		PVector shiftedDocSpacePt = PVector.sub(docSpacePt, docSpaceSpriteOffset);
		
		return rt.getPasteRectDocSpace(this.image, shiftedDocSpacePt);
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// experimental: towards applying masked effects
	// 
	
	
	BufferedImage getMaskedImage(BufferedImage mask, PVector maskCentre, PVector maskBottomRight) {
		// maskRect is in normalised Quad Space
		// preserves the scale of the mask despite any rotation, but does not rotate the mask ever, soreally only
		// works with radial and symmetrical masks
		PVector maskCentre_BufferSpace = imageQuad.getQuadPointBufferCoords(maskCentre.x, maskCentre.y);
		PVector maskBottomRight_BufferSpace = imageQuad.getQuadPointBufferCoords(maskBottomRight.x, maskBottomRight.y);
		float distToBottomRight = maskCentre_BufferSpace.dist(maskBottomRight_BufferSpace);
		
		PVector brNorm = maskBottomRight.normalize(null);
		PVector bottomRightScaled = PVector.mult(brNorm, distToBottomRight);
		Rect maskRect_BufferSpace = new Rect(PVector.sub(maskCentre_BufferSpace,bottomRightScaled), PVector.add(maskCentre_BufferSpace,bottomRightScaled));
		
		//System.out.println("dist cen-br " + distToBottomRight + " brNorm " + brNorm.toStr() + " BottomRightScaled " + bottomRightScaled + " rect " + maskRect_BufferSpace.toStr());
		
		return ImageProcessing.getMaskedImage(image,  mask,  maskRect_BufferSpace);
	}
	
	//... once you have done the treatment to the masked part, remerge it
	void mergeMaskedImage(BufferedImage maskedImage){
		
		ImageProcessing.compositeImage_ChangeTarget(maskedImage, image, 0, 0, 1);
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// cropping the sprite image to the permittedPasteArea
	//
	// if the render target needs to crop to the permittedPasteArea before pasting
	// The permittedPasteArea is in document space (the class PermittedpastewrArea converts user's normalised space to document space)
	boolean cropToPermittedPasteArea(MainDocumentRenderTarget rt) {
		// assume that the sprite has been tested for being wholly-inside already, so that by here,
		// we know a crop is due.
		
		// get intersection of both in documentSpace
		Rect permittedPasteArea = rt.permittedPasteArea.permittedPasteAreaRect;
		
		//System.out.println("cropToPermittedPasteArea");
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
	boolean doBespokeCrop(MainDocumentRenderTarget rt, BufferedImage preCroppedImage, String edgeCropReport) {
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
	boolean addBespokeCropToEdge(MainDocumentRenderTarget rt, BufferedImage preCroppedImage, String theEdge) {
		int numCropImages = rt.permittedPasteArea.permittedPasteAreaCropImages.getNumItems();
		int n = qRandomStream.randRangeInt(0, numCropImages-1);
		BufferedImage croppingMask = rt.permittedPasteArea.permittedPasteAreaCropImages.getImage(n);
		int sourceImageW = preCroppedImage.getWidth();
		int sourceImageH = preCroppedImage.getHeight();
		
		if(theEdge.contentEquals("LEFT")) {
			// don't need to rotate the crop image
			if(croppingMask.getWidth() > sourceImageW) return false;
			croppingMask = stretchCroppingMaskToFitEdge(croppingMask, croppingMask.getWidth(), sourceImageH);
			applyCroppingMask(preCroppedImage, croppingMask, 0, 0);
			return true;
		}
		if(theEdge.contentEquals("RIGHT")) {
			croppingMask = ImageProcessing.rotate90(croppingMask, 2);
			if(croppingMask.getWidth() > sourceImageW) return false;
			croppingMask = stretchCroppingMaskToFitEdge(croppingMask, croppingMask.getWidth(), sourceImageH);
			applyCroppingMask(preCroppedImage, croppingMask, preCroppedImage.getWidth()-croppingMask.getWidth(), 0);
			return true;
		}
		if(theEdge.contentEquals("TOP")) {
			croppingMask = ImageProcessing.rotate90(croppingMask, 1);
			if(croppingMask.getHeight() > sourceImageH) return false;
			croppingMask = stretchCroppingMaskToFitEdge(croppingMask, sourceImageW, croppingMask.getHeight());
			applyCroppingMask(preCroppedImage, croppingMask, 0, 0);
			return true;
		}
		if(theEdge.contentEquals("BOTTOM")) {
			croppingMask = ImageProcessing.rotate90(croppingMask, 3);
			if(croppingMask.getHeight() > sourceImageH) return false;
			croppingMask = stretchCroppingMaskToFitEdge(croppingMask, sourceImageW, croppingMask.getHeight());
			applyCroppingMask(preCroppedImage, croppingMask, 0, preCroppedImage.getHeight()-croppingMask.getHeight());
			return true;
		}
		
		// asked to do something unknown
		return false;
		
	}
	
	// returns the source image resized to match the w,h, if w or h are larger than the source image
	// if the existing size in h or w is larger than h,w then crop in that dimension
	// if it is larger then scale in that dimension
	BufferedImage stretchCroppingMaskToFitEdge(BufferedImage source, int newW, int newH) {
		
		if( source.getWidth() > newW ) {
			Rect r = new Rect(0,0,newW, source.getHeight());
			source = ImageProcessing.cropImage(source, r);
		}
		if( source.getHeight() > newH ) {
			Rect r = new Rect(0,0,source.getWidth(), newH);
			source = ImageProcessing.cropImage(source, r);
		}
		
		
		if( source.getWidth() < newW) {
			source = ImageProcessing.resizeTo(source,newW, source.getHeight());
		}
		if( source.getHeight() < newH) {
			source = ImageProcessing.resizeTo(source,source.getWidth(), newH);
		}
		
		//System.out.println("matchImageSize: postScaledSize " + outimg.getWidth() + " " + outimg.getHeight());
		return source;
	}
	
	
	
	
	void applyCroppingMask(BufferedImage preCroppedImage, BufferedImage maskImage, int offsetX, int offsetY) {
		// the mask image uses its own alpha to modify the preCroppedImage
		// pixels in the preCroopeImage are made transparent (alpha'd out) where the mask image is solid.
		int maskW = maskImage.getWidth();
		int maskH = maskImage.getHeight();
		
		// crops out just the part we want to mask - i.e. only those pixels "under" the mask
		Rect cropR = new Rect(offsetX, offsetY, offsetX+maskW, offsetY+maskH);
		
		BufferedImage preCroppedImageOverlap = ImageProcessing.cropImage(preCroppedImage, cropR);

		// apply the crop mask. We are preserving those parts of no alpha in the mask - a hole in the mask means the pixels in the image being masked survive.
		BufferedImage croppedByMaskImage =  ImageProcessing.getMaskedImage(preCroppedImageOverlap,  maskImage,  0, 0, AlphaComposite.DST_OUT);

		// paste back in the masked section, using Porter Duff SRC - i.e. replace everything in target with source including alpha.
		ImageProcessing.compositeImage_ChangeTarget(croppedByMaskImage, preCroppedImage, offsetX, offsetY, 1.0f, AlphaComposite.SRC);
		
		
	}
	
	//
	// end of cropping stuff
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	
	void  colorTransform(int function, float p1, float p2, float p3) {
		image = ImageProcessing.colorTransform( image,  function,  p1,  p2,  p3);
	}
	
	
	
}


