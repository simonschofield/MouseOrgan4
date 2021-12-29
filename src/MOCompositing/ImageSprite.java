package MOCompositing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;

import MOImage.BendImage;
import MOImage.ImageProcessing;
//import GlobalObjects;
//import MainDocumentRenderTarget;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.SNum;
import MOSceneData.SceneData3D;
import MOUtils.MOUtilGlobals;

//////////////////////////////////////////////////////////////////////
// 
// ImageSprite combines with a BufferedImage
// and other data (origin (pivot point), docPoint (also in seed), sizeInScene
// ImageQuad, it's own random stream
//
// Mouse organ only really considers one imageSprite at a time, after most decisions
// have been made about what goes where (e.g. using seeds)

/////////////////////////////////////////////////////////////////////////////////////////////////////
// coordinate system nomenclature
// SpriteNormalisedSpace - where the point within the sprite is expressed as a normalised x,y coordinate, both in the range 0..1
// SpriteBufferSpace - where the point in the sprite is expressed as the pixel location in the sprite's image buffer coordinate, in range x: 0..getBufferWidth()-1, y: 0..getBufferHeight()-1
// origin - stored natively in SpriteNormalisedSpace coordinates, representing the pivot/paste point of the sprite
// docPoint - the location of the sprite's origin in the main render-target's document space

public class ImageSprite{
	
	
	// critical (always needed) information
	public PVector docPoint = PVector.ZERO();
	
	private BufferedImage image;
	
	PVector origin = new PVector(0.5f,0.5f);

	public float sizeInScene = 1; // 
	
	// secondary (sometimes needed) information
	public String imageSampleGroupName = "";
	public String shortImageFileName = "";
	String seedBatchName = "";
	int id = 0;
	
		
		
	// for internal workings
	public ImageQuad imageQuad;
	
	//private int bufferWidth; 
	//private int bufferHeight;
	private float aspect;
	
	private QRandomStream qRandomStream;
	
	
	
	public ImageSprite(){
		
		
	}
	
	
	public ImageSprite(BufferedImage img, PVector orig, float sizeInScn, int id ){

		setImage(img); 
		
		this.sizeInScene = sizeInScn;
		this.origin = orig;
		//System.out.println("ImageSprite::Get sprite origin " + this.origin.toStr());
	    imageQuad = new ImageQuad(this);
	    setID_RandomSeed(id);
	}
	
	public void initQuad() {
		imageQuad = new ImageQuad(this);
	}
	
	public void setID_RandomSeed(int rseed) {
		
		// a sprite's random stream is set by this
		// therefore guaranteeing reproducible effects to this sprite.
		// When is sprite created without a seed, the sprite batch manager
		// generates a sprite using a unique ID. 
		this.id = rseed;
		qRandomStream = new QRandomStream(rseed);
	}
	
	String toStr() {
		//return "ImageSprite seed:" + seed.toStr() + " own doc pt:" + docPoint + " Image:" + image;
		return "ImageSprite doc pt:" + docPoint + " Image:" + getImageName();
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	
	public void setImage(BufferedImage img) {
		if(this.image == null) {
			this.image = img;
			imageQuad = new ImageQuad(this);
		} else {
			this.image = img;
		}
		
		
	}
	
	public int getImageWidth() {
		return getImage().getWidth();
		
	}
	
	public int getImageHeight() {
		return getImage().getHeight();
		
	}
	
	public PVector getDocPoint() {
		return docPoint.copy();
	}
	
	public void setDocPoint(PVector p) {
		docPoint = p.copy();
	}
	
	void setOrigin(PVector p) {
		origin = p.copy();
	}
	
	public PVector getOrigin() {
		return origin.copy();
	}
	
	boolean imageSampleGroupEquals(String s) {
		return imageSampleGroupName.contentEquals(s);
	}
	
	public boolean seedBatchEquals(String s) {
		return seedBatchName.contentEquals(s);
	}
	
	
	
	
	public QRandomStream getRandomStream() {
		return qRandomStream;
	}
	
	/*
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
	}*/
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Image sprite coordinate methods
	//
	//
	
	public PVector spriteNormalisedSpaceToDocumentSpace(PVector spriteNormSpace) {
		PVector spriteBufferPoint = spriteNormalisedSpaceToSpriteBufferSpace( spriteNormSpace);
		PVector docBufferPoint = spriteBufferSpaceToDocumentBufferSpace( spriteBufferPoint);
		
		return MOUtilGlobals.getTheDocumentCoordSystem().bufferSpaceToDocSpace(docBufferPoint);
	}
	
	
	public PVector spriteNormalisedSpaceToSpriteBufferSpace(PVector spriteNormSpace) {
		
		return new PVector( (int)(spriteNormSpace.x * getImageWidth()),  (int)(spriteNormSpace.y * getImageHeight()));
	}
	
	// once the doc point has been set for the sprite, this method maps any pixel location in the sprite to the document buffer space
	public PVector spriteBufferSpaceToDocumentBufferSpace(PVector spriteBufferSpace) {
		
		// work out where the top left of the sprite buffer is in relation to the document
		PVector bufferSpacePastePoint = MOUtilGlobals.getTheDocumentCoordSystem().docSpaceToBufferSpace(this.getDocPoint());
		PVector spriteOffsetBufferSpace = getSpriteOriginOffsetBufferSpace();
		PVector topLeftOfSpriteImageInDocBufferSpace = PVector.add(bufferSpacePastePoint, spriteOffsetBufferSpace);
		
		// now add the spriteBufferSpace location to topLeftOfSpriteImageInBufferSpace
		return PVector.add(spriteBufferSpace, topLeftOfSpriteImageInDocBufferSpace);
		
	}
	
	
	
	
	// needed by the RenderTarget to paste
	PVector getOriginInSpriteBufferSpace() {
			
		return new PVector( (origin.x * getImageWidth()), (origin.y * getImageHeight()) );
	}
		
	
	
	PVector getSpriteOriginOffsetBufferSpace() {
		// this is the amount you add in pixels to shift the sprite according to its normalised origin
		// so an origin of (1,1) (bottom right hand corner) would result in a subtraction of the whole width and height of sprite pos.
		int offsetX = (int) - ( origin.x * getImageWidth() );
		int offsetY = (int) - ( origin.y * getImageHeight() );
		
		return new PVector(offsetX, offsetY);
	}
	
	
	public Rect getDocumentBufferSpaceRect() {
		// returns the rect in BufferSpace of the sprite at its current docPoint
		PVector topLeft = spriteBufferSpaceToDocumentBufferSpace(  new PVector(0,0));
		PVector bottomRight = spriteBufferSpaceToDocumentBufferSpace( new PVector(getImageWidth()-1,getImageHeight()-1));
		return new Rect(topLeft, bottomRight);
	}
	
	public Rect getDocSpaceRect() {
		Rect docBufferRect = getDocumentBufferSpaceRect();
		return MOUtilGlobals.getTheDocumentCoordSystem().bufferSpaceToDocSpace(docBufferRect);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	
	public PVector getQuadPoint(PVector docPt, float  nx, float ny) {
		return imageQuad.getQuadDocumentPoint(docPt, nx, ny);
	}
	
	
	SNum getSNum(int seedOffset, Integer sequencePos) {
		return qRandomStream.snum(seedOffset, sequencePos);
	}
	
	
	
	float getAspect() {
		this.aspect = getImageWidth()/(float)getImageHeight();
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
	public void scale(float scaleW, float scaleH) {
		//imageQuad.theImage = image;
		imageQuad.applyScale(scaleW, scaleH);
		setImage(ImageProcessing.scaleImage(getImage(), scaleW, scaleH));
		
		
		
	}
	
	public void rotate(float degrees) {
		// this function has the visual effect of rotating the image of the sprite around the sprite origin
		// It does so by rotating the image and the origin point around the image centre, rather than rotating the image around
		// the origin, which could result in a much larger resultant image.
		// When the shape is pastes using its new origin point, it is as if it has been rotated around the origin
		// Much more efficient than otherwise
		
		//System.out.println("sprite rotate at top of method");
		//imageQuad.printVertices();
		
		
		PVector oldCentre = imageQuad.getCentre();
		
		double toRad = Math.toRadians(degrees);
		
		int oldBufferWidth = getImageWidth();
		int oldBufferHeight = getImageHeight();
		setImage(ImageProcessing.rotateImage(getImage(), degrees));
	
		// the rest is about rotating the origin point.
		// Rotate rotation point around (0,0) in image-pixel space
		float rx = oldBufferWidth * (origin.x - 0.5f);
		float ry = oldBufferHeight * (origin.y - 0.5f);

		float newX = (float) (rx * Math.cos(toRad) - ry * Math.sin(toRad));
		float newY = (float) (ry * Math.cos(toRad) + rx * Math.sin(toRad));

		

		origin.x = (newX / getImageWidth()) + 0.5f;
		origin.y = (newY / getImageHeight()) + 0.5f;
		
		imageQuad.applyRotation(degrees, oldCentre);
		
	}
	
	
	
	public void mirror(boolean inX) {
		if (inX) {
			setImage(ImageProcessing.mirrorImage(getImage(), true, false));
			origin.x = 1.0f - origin.x;
		} else {
			// in Y
			setImage(ImageProcessing.mirrorImage(getImage(), false, true));
			origin.y = 1.0f - origin.y;
		}
		imageQuad.applyMirror(inX);
	}
	
	
	void bend(float startBend, float bendAmt, float severity) {
		BendImage bendImage = new BendImage();
		float oldWidth = imageQuad.getImageWidth();
		this.setImage(bendImage.bendImage(this.getImage(), startBend, bendAmt, severity));
		

		// A -ve    bend means a shift of the top to the left
		// It applied only to the top of the image
		float shift = ((getImageWidth() - oldWidth) * MOMaths.getUnitSign(bendAmt));
		imageQuad.applyHorizontalTopShear(Math.abs(shift));
		
		// have to recalculate the origin.x to compensate for the image width getting larger
		origin.x = origin.x * (oldWidth/getImageWidth());
		if(bendAmt < 0) {
			// flip the origin
			origin.x = 1 - origin.x;
			imageQuad.applyMirror(true);
		}
		
	}
	
	
	
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// Scaling sprites in the scene.
	// Sprites have a sizeInScene variable set by the user. If in 2D, then the value refers to the HEIGHT of the sprite in document-space dimensions
	// i.e. size of 1 means that the height of image is scaled to be the same as the longest edge of the document
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
		// This method allows the user to specify the doc space size of the sprite in X, or in Y, or both. Use NULL to not define a particular dimension.
		// If only a size-in-X is specified then the Y is scaled proportionally to this to preserve the aspect of the sprite.
		// If only a size-in-Y is specified then the X is scaled proportionally to this to preserve the aspect of the sprite.
		// If both values are defined then the sprite is scaled independently in X and Y.
		
		if (sizeX == null && sizeY == null)
			return;
		float scaleY = 1;
		float scaleX = 1;
		//System.out.println("scaleToSizeInDocSpace  sizeXY " + sizeX + "  " + sizeY);
		if (sizeX != null) {
			float widthInPixels = docSizeToRenderTargetPixels2D(sizeX);
			scaleX = (widthInPixels / getImageWidth());
			
			if (scaleX > 2) {
				System.out.println(
						imageSampleGroupName + "/" + shortImageFileName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
								+ getImageWidth() + " to be scale to " + widthInPixels + " scale " + scaleX);
			}
		}

		if (sizeY != null) {
			float heightInPixels = docSizeToRenderTargetPixels2D(sizeY);
			scaleY = (heightInPixels / getImageHeight());


			if (scaleY > 2) {
				System.out.println(
						imageSampleGroupName + "/" + shortImageFileName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
								+ getImageHeight() + " to be scale to " + heightInPixels + " scale " + scaleY);
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
		
		//System.out.println("scaling in X and Y " + scaleX + "," + scaleY);
		scale(scaleX, scaleY);

		//
	}

	float docSizeToRenderTargetPixels2D(float size) {

		PVector heightDocSpaceVector = new PVector(0, size);
		PVector heightInPixelsVector = MOUtilGlobals.getTheDocumentCoordSystem().docSpaceToBufferSpace(heightDocSpaceVector);
		return (float) Math.abs(heightInPixelsVector.y);

	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// 2D operation map to line
	// Experimental but quite well tested
	// Overrides any pre-set sprite origin and sizeInScene
	// The origin is set to be the bottom centre of the sprite. This gets mapped to line.p1
	// The other end gets mapped to line.p2  . The sprite is scaled so that it is as high as the line p1->p2
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



	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Scaling to 3D scene
	// The sizeInScene value becomes the size of the sprite within the scene using the scenes 3D units. This is claculated from the 
	// depth at the paste-point of the sprite using the geometry-buffers 3D methods.
	//
	public void scaleToSizeInScene(SceneData3D sceneData, float scaleModifier) {
		// scales the image to the correct size using  sizeInScene to represent the
		// items's size in the 3D scene in world units.

		float heightInPixels = getHeightInRenderTargetPixels3D(sceneData);
		//System.out.println(" scaleToSizeinScene - sizeInScene:" + sizeInScene + " scaleModifyer " + scaleModifier + " height in pixels " + heightInPixels);
		float scale = (heightInPixels / getImageHeight()) * scaleModifier;
		if (scale > 1) {
			System.out.println(imageSampleGroupName + " overscaled, original size in pixels " + getImageHeight()
			+ " to be scale to " + heightInPixels + " scale " + scale);
		}

		scale(scale, scale);
		//System.out.println("target size in pixels " + heightInActualPixels + " scale " + scale + " resultant height " + bufferHeight);
	}

	float getHeightInRenderTargetPixels3D(SceneData3D sceneData) {

		float heightDocSpace = sizeInScene * sceneData.get3DScale(docPoint);
		return docSizeToRenderTargetPixels2D(heightDocSpace);

	}



	
	///////////////////////////////////////////////////////////////////////////////////////////
	/// end of geometric transforms
	///////////////////////////////////////////////////////////////////////////////////////////
	
	void  colorTransform(int function, float p1, float p2, float p3) {
		setImage(ImageProcessing.colorTransform( getImage(),  function,  p1,  p2,  p3));
	}

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// experimental: towards applying masked effects
	// think this is pretty rubbish.....
	
	
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
		
		return ImageProcessing.getMaskedImage(getImage(),  mask,  maskRect_BufferSpace);
	}
	
	//... once you have done the treatment to the masked part, remerge it
	void mergeMaskedImage(BufferedImage maskedImage){
		
		ImageProcessing.compositeImage_ChangeTarget(maskedImage, getImage(), 0, 0, 1);
		
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// end of .... experimental: towards applying masked effects
	// 

	
	

	void logOperation(String operationname, Float p1, Float p2, Float p3, Float p4) {
		
		
		
		
	}
	
	
}


