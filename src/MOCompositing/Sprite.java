package MOCompositing;

import java.awt.image.BufferedImage;

import MOImage.BendImage;
import MOImage.ImageProcessing;
import MOImageCollections.SpriteImageGroup;
import MOImageCollections.SpriteImageGroupManager;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.SNum;
import MOScene3D.SceneData3D;
import MOSpriteSeed.SpriteSeed;
import MOUtils.GlobalSettings;

public class Sprite {
	public SpriteSeed data;
	
	// for internal workings
	public SpriteImageQuad imageQuad;
	private BufferedImage image;
	private QRandomStream qRandomStream;
		
	public float alpha = 1;
	
	public Sprite(BufferedImage img) {
		data = new SpriteSeed();
		setImage(img);
	}
	
	public Sprite(SpriteSeed sseed) {
		data = sseed.copy();
		setRandomSeed(data.id);

		// This method is called by Sprite initialisation to
		// fully make a sprite from a seed.
		SpriteImageGroupManager sigm = GlobalSettings.getTheSpriteImageGroupManager();
		SpriteImageGroup sig = sigm.getSpriteImageGroup(data.spriteImageGroupName);
		if (sig == null) {
			System.out.println("ERROR Sprite::SpriteSeed constructor - cannot find spriteImageGroup called "
					+ data.spriteImageGroupName);
			return;
		}
		BufferedImage img = sig.getImage(data.spriteImageGroupItemNumber);
		if (img == null) {
			System.out.println("ERROR Sprite::SpriteSeed constructor - cannot find image number "
					+ data.spriteImageGroupItemNumber + " in " + data.spriteImageGroupName);
			return;
		}
		setImage(img);
	}
	
	Sprite copy() {
		
		Sprite cpy = new Sprite(this.data);
		cpy.imageQuad = this.imageQuad.copy(cpy);
		cpy.setImage(ImageProcessing.copyImage(this.image));
		cpy.setRandomStream(this.qRandomStream);
		cpy.alpha = this.alpha;
		return cpy;
	}
	
	
	public void initQuad() {
		
		imageQuad = new SpriteImageQuad(this);
	}
	
	public void setRandomSeed(int rseed) {
		// a sprite's random stream is set by this
		// therefore guaranteeing reproducible effects to this sprite.
		// When is sprite created without a seed, the sprite batch manager
		// generates a sprite using a unique ID. 
		qRandomStream = new QRandomStream(rseed);
	}
	
	String toStr() {
		//return "ImageSprite seed:" + seed.toStr() + " own doc pt:" + docPoint + " Image:" + image;
		//return "ImageSprite doc pt:" + docPoint + " Image:" + getImageName();
		return "";
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	
	public void setImage(BufferedImage img) {
		//
		// The sprite initially has an actual reference to the image in the SIG.
		// However, any attempt to alter it results in a new image, so the original is left unaltered.
		//
		img.setAccelerationPriority(1);
		if(this.image == null) {
			this.image = img;
			initQuad();
		} else {
			this.image = img;
		}
		
		//System.out.println("reference to image in sprite is " + img);
	}
	
	public int getImageWidth() {
		return getImage().getWidth();
		
	}
	
	public int getImageHeight() {
		return getImage().getHeight();
		
	}
	
	public PVector getDocPoint() {
		return data.getDocPoint();
	}
	
	public void setDocPoint(PVector p) {
		data.setDocPoint(p);
	}
	
	void setOrigin(PVector p) {
		data.origin = p.copy();
	}
	
	public PVector getOrigin() {
		return data.origin.copy();
	}
	
	boolean spriteImageGroupEquals(String s) {
		return data.spriteImageGroupName.contentEquals(s);
	}
	
	public boolean seedBatchEquals(String s) {
		//System.out.println("seedFontis " + data.spriteSeedFontName + " testing " + s);
		return data.spriteSeedBatchName.contentEquals(s);
	}
	
	public boolean seedFontEquals(String s) {
		//System.out.println("seedFontis " + data.spriteSeedFontName + " testing " + s);
		return data.spriteSeedFontName.contentEquals(s);
	}
	
	public QRandomStream getRandomStream() {
		return qRandomStream;
	}
	
	public void setRandomStream(QRandomStream qrs) {
		// set this random stream to an identical state as qrs  
		qRandomStream = qrs.copy();
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
		
		return GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(docBufferPoint);
	}
	
	
	public PVector spriteNormalisedSpaceToSpriteBufferSpace(PVector spriteNormSpace) {
		
		return new PVector( (int)(spriteNormSpace.x * getImageWidth()),  (int)(spriteNormSpace.y * getImageHeight()));
	}
	
	// once the doc point has been set for the sprite, this method maps any pixel location in the sprite to the document buffer space
	public PVector spriteBufferSpaceToDocumentBufferSpace(PVector spriteBufferSpace) {
		
		// work out where the top left of the sprite buffer is in relation to the document
		PVector bufferSpacePastePoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(this.getDocPoint());
		PVector spriteOffsetBufferSpace = getSpriteOriginOffsetBufferSpace();
		PVector topLeftOfSpriteImageInDocBufferSpace = PVector.add(bufferSpacePastePoint, spriteOffsetBufferSpace);
		
		// now add the spriteBufferSpace location to topLeftOfSpriteImageInBufferSpace
		return PVector.add(spriteBufferSpace, topLeftOfSpriteImageInDocBufferSpace);
		
	}
	
	public PVector spriteBufferSpaceToDocSpace(PVector spriteBufferSpace) {
		PVector docBufferPoint = spriteBufferSpaceToDocumentBufferSpace( spriteBufferSpace);
		return GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(docBufferPoint);
	}
	
	
	
	// needed by the RenderTarget to paste
	PVector getOriginInSpriteBufferSpace() {
			
		return new PVector( (data.origin.x * getImageWidth()), (data.origin.y * getImageHeight()) );
	}
		
	
	
	PVector getSpriteOriginOffsetBufferSpace() {
		// this is the amount you add in pixels to shift the sprite according to its normalised origin
		// so an origin of (1,1) (bottom right hand corner) would result in a subtraction of the whole width and height of sprite pos.
		int offsetX = (int) - ( data.origin.x * getImageWidth() );
		int offsetY = (int) - ( data.origin.y * getImageHeight() );
		
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
		return GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(docBufferRect);
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
		float aspect = getImageWidth()/(float)getImageHeight();
		return aspect;
	}
	
	String getImageName() {
		return data.spriteImageGroupItemShortName;
	}
	
	
	boolean imageNameContains(String s) {
		return data.spriteImageGroupItemShortName.contains(s);
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
		float rx = oldBufferWidth * (data.origin.x - 0.5f);
		float ry = oldBufferHeight * (data.origin.y - 0.5f);

		float newX = (float) (rx * Math.cos(toRad) - ry * Math.sin(toRad));
		float newY = (float) (ry * Math.cos(toRad) + rx * Math.sin(toRad));

		

		data.origin.x = (newX / getImageWidth()) + 0.5f;
		data.origin.y = (newY / getImageHeight()) + 0.5f;
		
		imageQuad.applyRotation(degrees, oldCentre);
		
	}
	
	
	
	public void mirror(boolean inX) {
		if (inX) {
			setImage(ImageProcessing.mirrorImage(getImage(), true, false));
			data.origin.x = 1.0f - data.origin.x;
		} else {
			// in Y
			setImage(ImageProcessing.mirrorImage(getImage(), false, true));
			data.origin.y = 1.0f - data.origin.y;
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
		
		// have to recalculate the origin.x to compensate for the image width getting wider
		data.origin.x = data.origin.x * (oldWidth/getImageWidth());
		if(bendAmt < 0) {
			// flip the origin
			data.origin.x = 1 - data.origin.x;
			imageQuad.applyMirror(true);
		}
		
	}
	
	
	
	

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// Scaling sprites in the scene.
	// Sprites have a sizeInScene variable set by the user. If in 2D, then the value refers to the HEIGHT of the sprite in document-space dimensions
	// i.e. size of 1 means that the height of image is scaled to be the same as the longest edge of the document
	// scaling to 2D scene
	//
	public void scaleToSizeInScene(float scaleModifier) {
		// The height of the sample image is set using pre-set sizeInScene member variable as a documentSpace measurement.
		// i.e. sizeInScene of 1 means that the image is scaled to be the same as the longest edge of the document
		float scale = scaleModifier * data.sizeInScene;
		//System.out.println("scaleToSizeInScene  scaleModifier " + scaleModifier + "  sizeInScene " + data.sizeInScene + " result " + scale);
		scaleToSizeInDocSpace(null, scale);
	}
	
	void scaleYToSizeInDocSpace(Float sizeY) {
		float heightInPixels = docSizeToRenderTargetPixels2D(sizeY);
		float scaleY = (heightInPixels / getImageHeight());

		scale(1, scaleY);
		
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
						data.spriteImageGroupName + "/" + data.spriteImageGroupItemShortName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
								+ getImageWidth() + " to be scale to " + widthInPixels + " scale " + scaleX);
			}
		}

		if (sizeY != null) {
			float heightInPixels = docSizeToRenderTargetPixels2D(sizeY);
			scaleY = (heightInPixels / getImageHeight());


			if (scaleY > 2) {
				System.out.println(
						data.spriteImageGroupName + "/" + data.spriteImageGroupItemShortName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
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
		PVector heightInPixelsVector = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(heightDocSpaceVector);
		return (float) Math.abs(heightInPixelsVector.y);

	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// 2D operation map to line
	// Experimental but quite well tested
	// Overrides any pre-set sprite origin and sizeInScene
	// The origin is set to be the bottom centre of the sprite. This gets mapped to line.p1
	// The other end gets mapped to line.p2  . The sprite is scaled so that it is as high as the line p1->p2
	// The sprite is rotated into the same direction as the line p1-p2
	public void mapToLine2(Line2 line, float overlap) {
		data.origin = new PVector(0.5f, 1.0f);
		float r = line.getRotation();
		float len = line.getLength()*overlap;
		
		
		//System.out.println( "BEFORE SCALE sprite width " + this.getImageBufferWidth() + " sprite height " + this.getImageBufferHeight() + " aspect = " + getAspect());
		scaleToSizeInDocSpace(null, len);
		//System.out.println( "AFTER SCALE sprite width " + this.getImageBufferWidth() + " sprite height " + this.getImageBufferHeight() + " aspect = " + getAspect() + "\n");
		rotate(r);
		setDocPoint(line.p1);
	}

	public void mapToLine2(Line2 line, float overlap, float minLength) {
		// this version tries to deal with problem short lines, which would otherwise result in a very small scale sprites being made.
		// Here, short lines are caught using the minLength parameter. If short, the line is scales to the minlength, preserving aspect,
		// then scales only in Y to the correct length, preserving the X, therefore making a squat version of the shape but fitted to
		// the line
		data.origin = new PVector(0.5f, 1.0f);
		float r = line.getRotation();
		float len = line.getLength();
		float lenWithOverlap = len*overlap;
		
		if(len < (minLength*0.9f)) {
			// this gets the line scale in X and Y proportionally to the correct Y height
			scaleToSizeInDocSpace(null, minLength*overlap);
			// then scrunches the sprite in Y only
			scaleYToSizeInDocSpace(len);
		} else {
		    // scale normally
			scaleToSizeInDocSpace(null, lenWithOverlap);
		}
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
	public float scaleToSizeInScene(SceneData3D sceneData, float scaleModifier) {
		// scales the image to the correct size using  sizeInScene to represent the
		// items's size in the 3D scene in world units.

		float heightInPixels = getHeightInRenderTargetPixels3D(sceneData);
		//System.out.println(" scaleToSizeinScene - sizeInScene:" + sizeInScene + " scaleModifyer " + scaleModifier + " height in pixels " + heightInPixels);
		float scale = (heightInPixels / getImageHeight()) * scaleModifier;
		if (scale > 1) {
			System.out.println(data.spriteImageGroupItemShortName + " overscaled, original size in pixels " + getImageHeight()
			+ " to be scale to " + heightInPixels + " scale " + scale);
		}

		scale(scale, scale);
		//System.out.println("target size in pixels " + heightInActualPixels + " scale " + scale + " resultant height " + bufferHeight);
		return scale;
	}

	float getHeightInRenderTargetPixels3D(SceneData3D sceneData) {
		float scale3D = sceneData.get3DScale(data.getDocPoint());
		float heightDocSpace = data.sizeInScene * scale3D;
		
				//System.out.println("getHeightInRenderTargetPixels3D: scale3D " + scale3D );
		float docSizeInPixels =  docSizeToRenderTargetPixels2D(heightDocSpace);
		//System.out.println("sprite id " + this.id + " doc point " + docPoint.toString() + " height doc space = " + heightDocSpace + "  size pixels " + docSizeInPixels);
		//System.out.println();
		return docSizeInPixels;
	}



	
	///////////////////////////////////////////////////////////////////////////////////////////
	/// end of geometric transforms
	///////////////////////////////////////////////////////////////////////////////////////////
	
	public void  colorTransform(int function, float p1, float p2, float p3) {
		setImage(ImageProcessing.colorTransform( getImage(),  function,  p1,  p2,  p3));
	}

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// experimental: towards applying masked effects
	// think this is pretty rubbish.....
	
	
//	BufferedImage getMaskedImage(BufferedImage mask, PVector maskCentre, PVector maskBottomRight) {
//		// maskRect is in normalised Quad Space
//		// preserves the scale of the mask despite any rotation, but does not rotate the mask ever, soreally only
//		// works with radial and symmetrical masks
//		PVector maskCentre_BufferSpace = imageQuad.getQuadPointBufferCoords(maskCentre.x, maskCentre.y);
//		PVector maskBottomRight_BufferSpace = imageQuad.getQuadPointBufferCoords(maskBottomRight.x, maskBottomRight.y);
//		float distToBottomRight = maskCentre_BufferSpace.dist(maskBottomRight_BufferSpace);
//		
//		PVector brNorm = maskBottomRight.normalize(null);
//		PVector bottomRightScaled = PVector.mult(brNorm, distToBottomRight);
//		Rect maskRect_BufferSpace = new Rect(PVector.sub(maskCentre_BufferSpace,bottomRightScaled), PVector.add(maskCentre_BufferSpace,bottomRightScaled));
//		
//		//System.out.println("dist cen-br " + distToBottomRight + " brNorm " + brNorm.toStr() + " BottomRightScaled " + bottomRightScaled + " rect " + maskRect_BufferSpace.toStr());
//		
//		return ImageProcessing.getMaskedImage(getImage(),  mask,  maskRect_BufferSpace);
//	}
	
	//... once you have done the treatment to the masked part, remerge it
	
	public void mergeMaskedImage(BufferedImage maskedImage){
		
		ImageProcessing.compositeImage_ChangeTarget(maskedImage, getImage(), 0, 0, 1);
		
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// end of .... experimental: towards applying masked effects
	// 

	
	

	void logOperation(String operationname, Float p1, Float p2, Float p3, Float p4) {
		
		
		
		
	}
	
}
