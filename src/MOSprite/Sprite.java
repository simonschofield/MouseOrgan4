package MOSprite;

import java.awt.image.BufferedImage;

import MOImage.BendImage;
import MOImage.ImageProcessing;
import MOImageCollections.ScaledImageAssetGroup;
import MOImageCollections.ScaledImageAssetGroupManager;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.SNum;
import MOScene3D.SceneData3D;
import MOUtils.GlobalSettings;

public class Sprite {
	

	////////////////////////////////////////////////////////////
	// Sprite Data
	//
	
	////////////////////////////////////////////////////
	// For Identification purposes
	// this enables the user to identify seeds from different batches, and sprite fonts within biomes (or individually) 
	public String SpriteSeedBatchName = "";
	public String SpriteFontName = "";
	public String ImageAssetGroupName = "";

	// the id is a unique integer. It is set by the SpriteSeed constructor from the static UniqueID class declared above. 
	// This is used in seeding the sprite's random number generator, thereby ensuring the same random events happen to each seed
	// regardless of previous random events
	// It is also used in optimisations such as registering whether or not a seed is used in a render.
	public int uniqueID;

	public int randomKey;
	
	
	// Sprite image related stuff
	// the name of the image sample group to be used by
	// the number of the item within that group
	// The short name, which is derived usually from the file name (without extension)
	public int ImageGroupItemNumber = 0;
	public String ImageGroupItemShortName= "";
	private BufferedImage image;
	public float alpha = 1;


	////////////////////////////////////////////////////
	// Item size-in-scene data and pivot-point
	//
	public float sizeInScene = 1;
	public boolean useRelativeSizes = false;
	private PVector pivotPoint = new PVector(0.5f, 0.5f);


	/////////////////////////////////////////////////////
	// Geometric transforms applied

	// the doc point used to position  the item in the scene
	public PVector docPoint = new PVector(0.5f, 0.5f);
	

	/////////////////////////////////////////////////////
	// the depth is set to the normalised depth in the 3D scene, 
	// usually used to sort the render order of the seeds
	// 
	public float depth = 1;
	
	/////////////////////////////////////////////////////
	// this is so that you can turn sprites on and off in clipping/culling processes
	// 
	public boolean isActive = true;

	
	
	
	
	//////////////////////////////////////////////////////
	// The following geometric modifications are probably set from the SpriteSeed by some external process. They are not automatically
	// applied, and should be thought of as "recommendations" from the generating system. 
	//
	
	// scale
	public float scale = 1;

	//Rotation, in degrees clockwise
	//where 0 represent the "up" of the image
	public float rotation = 0;

	// flip in x and y
	public boolean flipX = false;
	public boolean flipY = false;

	

	///////////////////////////////////////////////////////////////
	// for internal workings
	//
	public SpriteImageQuad imageQuad;
	

	private QRandomStream qRandomStream;

	private boolean quickRenderMode = false;
	
	
	public Sprite() {
		SpriteSeed seed = new SpriteSeed();
		setSpriteSeedData(seed);
		setRandomStreamPos(seed.getRandomKey());
	}

	public Sprite(BufferedImage img) {
		SpriteSeed seed = new SpriteSeed();
		setSpriteSeedData(seed);
		setRandomStreamPos(seed.getRandomKey());
		setImage(img);
	}


	public Sprite(SpriteSeed seed) {
		setSpriteSeedData(seed);
		setRandomStreamPos(seed.getRandomKey());
	}
	
	
	public void setQuickRenderMode(boolean b) {
		quickRenderMode = b;
		
	}
	
	public boolean isQuickRenderMode() {
		return quickRenderMode;
		
	}


	public void setSpriteSeedData(SpriteSeed seed) {
		
		// set positional data: data from seed, unique to this sprite
		SpriteSeedBatchName = seed.SeedBatchName;
		docPoint = new PVector(seed.docPointX, seed.docPointY);		 
		scale  = seed.scale;
		rotation  = seed.rotation;
		flipX  = seed.flipX;
		flipY  = seed.flipY;
		depth = seed.depth;
		uniqueID = seed.getUniqueID();
		randomKey = seed.getRandomKey();
	}
	
	
	// called by the generating sprite font
	// this selects the image as well so is good to go....
	public void setSpriteFontDataAndSelectImage(SpriteFont sf) {
		SpriteFontName = sf.thisSpriteFontName;
		ImageAssetGroupName = sf.imageSampleGroupName;
		sizeInScene = sf.sizeInScene;
		useRelativeSizes = sf.useRelativeSizes;
		pivotPoint = sf.spritePivotPoint.copy();
		
		ImageGroupItemNumber = sf.getRandomSpriteImageGroupItemNumber();

		ScaledImageAssetGroup siag = sf.getSpriteImageGroup();
		ImageGroupItemShortName = siag.getImageAssetName(ImageGroupItemNumber);
		BufferedImage img = siag.getImage(ImageGroupItemNumber);
		
		setImage(img);
	}

	public Sprite copy() {
		// not sure we need this tbd
		//Sprite cpy = new Sprite(this.spriteData);
		//cpy.setImage(ImageProcessing.copyImage(this.image));
		//cpy.imageQuad = this.imageQuad.copy(cpy);

		//cpy.setRandomStream(this.qRandomStream);
		//cpy.alpha = this.alpha;
		return null;
	}


	public void initQuad() {

		imageQuad = new SpriteImageQuad(this);
	}

	public void setRandomStreamPos(int rseed) {
		// a sprite's random stream is set by this
		// therefore guaranteeing reproducible effects to this sprite.
		// When is sprite created without a seed, the sprite batch manager
		// generates a sprite using a unique ID. 
		qRandomStream = new QRandomStream(rseed);
	}

	public int getID() {
		return uniqueID;
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
	
	public float getAspect() {
		float aspect = getImageWidth()/(float)getImageHeight();
		return aspect;
	}

	public PVector getDocPoint() {
		return docPoint.copy();
	}

	public void setDocPoint(PVector p) {
		docPoint = p.copy();
	}

	public void setPivotPoint(PVector p) {
		pivotPoint = p.copy();
		imageQuad.setPivotPoint(pivotPoint);
	}

	public PVector getPivotPoint() {
		return pivotPoint.copy();
	}

	//////////////////////////////////////////////////////////
	// Identification string matching
	//
	String getImageName() {
		return ImageGroupItemShortName;
	}
	
	
	public boolean seedBatchNameEquals(String s) {
		return SpriteSeedBatchName.contentEquals(s);
	}

	public boolean spriteFontNameEquals(String s) {
		return SpriteFontName.contentEquals(s);
	}
	
	public boolean imageAssetGroupNameEquals(String s) {
		return ImageAssetGroupName.contentEquals(s);
	}
	
	public boolean imageNameEquals(String s) {
		return ImageGroupItemShortName.equals(s);
	}
	
	public boolean seedBatchNameContains(String s) {
		//System.out.println("seedbach name " + SpriteSeedBatchName);
		return SpriteSeedBatchName.contains(s);
	}

	public boolean spriteFontNameContains(String s) {
		return SpriteFontName.contains(s);
	}
	
	public boolean imageAssetGroupNameContains(String s) {
		return ImageAssetGroupName.contains(s);
	}
	
	public boolean imageNameContains(String s) {
		return ImageGroupItemShortName.contains(s);
		
	}

	//////////////////////////////////////////////////////////
	// random stream stuff
	//
	public QRandomStream getRandomStream() {
		return qRandomStream;
	}

	public void setRandomStream(QRandomStream qrs) {
		// set this random stream to an identical state as qrs  
		qRandomStream = qrs.copy();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	// Image sprite coordinate methods
	// Once the sprite's docPoint has been set, these methods return the document-space, or render-target
	// buffer-space point of a point within the sprite
	// 

	////////////////////////////////////////////////////////////////////////////////////////////
	// returns the document space position of a normalised point within the sprite.
	// If the sprite's docPoint has been set, then this is taken into account.
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

		return new PVector( (pivotPoint.x * getImageWidth()), (pivotPoint.y * getImageHeight()) );
	}



	PVector getSpriteOriginOffsetBufferSpace() {
		// this is the amount you add in pixels to shift the sprite according to its normalised origin
		// so an origin of (1,1) (bottom right hand corner) would result in a subtraction of the whole width and height of sprite pos.
		int offsetX = (int) - ( pivotPoint.x * getImageWidth() );
		int offsetY = (int) - ( pivotPoint.y * getImageHeight() );

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



	

	


	

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// geometric transforms change the sprite image in-place
	//
	public void scale(float scaleW, float scaleH) {
		//imageQuad.theImage = image;
		imageQuad.applyScale(scaleW, scaleH);

		if( isQuickRenderMode() ) return;
		
		if(scaleW==scaleH) {
			// chance to use double scaling on very big scale reductions
			setImage(ImageProcessing.scaleImage(getImage(), scaleW));
		}else {
			setImage(ImageProcessing.scaleImage(getImage(), scaleW, scaleH));
		}


	}

	public void rotate(float degrees) {
		// this function has the visual effect of rotating the image of the sprite around the sprite origin
		// It does so by rotating the image and the origin point around the image centre, rather than rotating the image around
		// the origin, which could result in a much larger resultant image.
		// When the shape is pastes using its new origin point, it is as if it has been rotated around the origin
		// Much more efficient than otherwise

		//System.out.println("sprite rotate at top of method");
		//imageQuad.printVertices();
		imageQuad.applyRotation2(degrees);

		if( isQuickRenderMode() ) return;
		
		//PVector oldCentre = imageQuad.getCentre();

		double toRad = Math.toRadians(degrees);

		int oldBufferWidth = getImageWidth();
		int oldBufferHeight = getImageHeight();
		setImage(ImageProcessing.rotateImage(getImage(), degrees));

		// the rest is about rotating the origin point.
		// Rotate rotation point around (0,0) in image-pixel space
		float rx = oldBufferWidth * (pivotPoint.x - 0.5f);
		float ry = oldBufferHeight * (pivotPoint.y - 0.5f);

		float newX = (float) (rx * Math.cos(toRad) - ry * Math.sin(toRad));
		float newY = (float) (ry * Math.cos(toRad) + rx * Math.sin(toRad));


		//PVector newPivotPoint = new PVector();
		pivotPoint.x = (newX / getImageWidth()) + 0.5f;
		pivotPoint.y = (newY / getImageHeight()) + 0.5f;
		
		
		

	}



	public void mirror(boolean inX) {
		
		
		imageQuad.applyMirror(inX);
		
		if( isQuickRenderMode() ) return;
		
		
		if (inX) {
			setImage(ImageProcessing.mirrorImage(getImage(), true, false));
			pivotPoint.x = 1.0f - pivotPoint.x;
		} else {
			// in Y
			setImage(ImageProcessing.mirrorImage(getImage(), false, true));
			pivotPoint.y = 1.0f - pivotPoint.y;
		}
		
	}


	public void crop(Rect normalisedCropRect, boolean shiftOrigin) {



		Rect cropRect = normalisedCropRect.getScaled(getImageWidth(), getImageHeight());

		setImage(ImageProcessing.cropImage( getImage() , cropRect ));
		if(shiftOrigin==true) {
			// then work out the shift between the original rect and the croprect
			// The original rect is (in normalised coords ) (0,0,1,1) i.e. the identity rect
			// The crop rect will be (x,y,w,h), representing the normalised cropRect
			// so work out a scale and translate to map the cropRect to the identity rect
			float tx = -cropRect.left;
			float ty = -cropRect.top;

			float scX = 1/cropRect.getWidth();
			float scY = 1/cropRect.getHeight();

			PVector existingOrigin = getPivotPoint();
			PVector shiftedOrigin = new PVector();

			shiftedOrigin.x = existingOrigin.x*scX;
			shiftedOrigin.y = existingOrigin.y*scY;

			shiftedOrigin.x+=tx;
			shiftedOrigin.y+=ty;
			setPivotPoint(shiftedOrigin);
		}

	}


	/**
	 * Bends an image to the left
	 * The bend harshness is the gamma applied to the curve. 1.2 == very gentle curve over the length of the image, 10.0 == very harsh curve at the end of the image
	 * @param startBend -  just set this to 0, as other results are poor. .....is always a parametric 0..1, where 0 is the bottom of the image
	 * @param bendAmt -  0 == no displacement, 1 == the displacement is equivalent to the image height (whihc would be huge)
	 * @param severity - is the gamma applied to the curve. 1.2 == very gentle curve over the length of the image, 10.0 == very harsh curve at the end of the image
	 */
	public void bend(float startBend, float bendAmt, float severity) {
		
		imageQuad.applyHorizontalTopShear2(bendAmt);
		
		if( isQuickRenderMode() ) return;
		
		float oldWidth = getImageWidth();
		
		BendImage bendImage = new BendImage();
		
		BufferedImage bentImage = bendImage.bendImage(this.getImage(), startBend, bendAmt, severity);
		this.setImage(bentImage);

		// have to recalculate the origin.x to compensate for the image width getting wider
		pivotPoint.x = pivotPoint.x * (oldWidth/getImageWidth());
		if(bendAmt < 0) {
			// flip the origin
			pivotPoint.x = 1 - pivotPoint.x;
			
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
		float scale = scaleModifier * sizeInScene * getRelativeSizeInGroup();
		//System.out.println("scaleToSizeInScene  scaleModifier " + scaleModifier + "  sizeInScene " + data.sizeInScene + " result " + scale);
		scaleToSizeInDocSpace(null, scale);
	}

	void scaleYToSizeInDocSpace(Float sizeY) {
		float heightInPixels = docSizeToRenderTargetPixels2D(sizeY);
		float scaleY = (heightInPixels / getImageHeight());

		scale(1, scaleY);

	}

	ScaledImageAssetGroup getImageAssetGroup() {
		ScaledImageAssetGroupManager sigm = GlobalSettings.getImageAssetGroupManager();
		if(sigm==null) System.out.println("ERROR Sprite::SpriteImageGroupManager == null");
		ScaledImageAssetGroup sig = sigm.getScaledImageAssetGroup(ImageAssetGroupName);
		return sig;
	}

	float getRelativeSizeInGroup() {
		if( useRelativeSizes == false ) return 1f;
		return getImageAssetGroup().getRelativeImageHeight(ImageGroupItemNumber);
	}


	public void scaleToSizeInDocSpace(Float sizeX, Float sizeY) {
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
						ImageAssetGroupName + "/" + ImageGroupItemShortName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
								+ getImageWidth() + " to be scale to " + widthInPixels + " scale " + scaleX);
			}
		}

		if (sizeY != null) {
			float heightInPixels = docSizeToRenderTargetPixels2D(sizeY);
			scaleY = (heightInPixels / getImageHeight());


			if (scaleY > 2) {
				System.out.println(
						ImageAssetGroupName + "/" + ImageGroupItemShortName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
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
		pivotPoint = new PVector(0.5f, 1.0f);
		float r = line.getRotation();
		float len = line.getLength()*overlap;


		//System.out.println( "BEFORE SCALE sprite width " + this.getImageBufferWidth() + " sprite height " + this.getImageBufferHeight() + " aspect = " + getAspect());
		scaleToSizeInDocSpace(null, len);
		//System.out.println( "AFTER SCALE sprite width " + this.getImageBufferWidth() + " sprite height " + this.getImageBufferHeight() + " aspect = " + getAspect() + "\n");
		rotate(r);
		setDocPoint(line.p1);
	}

	public void mapToLine2(Line2 line, float overlapAtStart, float overlapAtEnd, float minLength) {
		// This version tries to deal with problem short lines, i.e. where the line is considerably shorter than the desired minLength (in network crawling, the targetCrawlStepDistance)
		// Short lines result in a noticeably small sprite.
		// Here, short lines are caught using the minLength parameter. If short, the line is scaled to the minlength, preserving aspect,
		// then scaled only in Y to the correct length, preserving the X, therefore making a squat version of the shape but fitted to
		// the line
		// overlap is also dealt with in a better way, in that the sprite is scaled to overlap the line in either directions. The overlap amounts is a proportion of the initial line length
		// where 0 is no overlap added, and 1 is the whole line length added at either start or end.
		pivotPoint = new PVector(0.5f, 1.0f);
		float r = line.getRotation();
		float len = line.getLength();

		float overlapP1 = len*overlapAtStart;
		float overlapP2 = len*overlapAtEnd;
		float overlapTotal = overlapP1 + overlapP2;
		float lenWithOverlap = len + overlapTotal;

		float newP1Y = 1 - MOMaths.norm(0, -overlapP1, len+overlapP2);
		PVector shiftedorigin = new PVector(0.5f, newP1Y);


		if(len < (minLength*0.9f)) {
			// this gets the line scale in X and Y proportionally to the correct Y height
			scaleToSizeInDocSpace(null, minLength+overlapTotal);
			// then scrunches the sprite in Y only
			scaleYToSizeInDocSpace(lenWithOverlap);
		} else {
			// scale normally
			scaleToSizeInDocSpace(null, lenWithOverlap);
		}
		//System.out.println( "AFTER SCALE sprite width " + this.getImageBufferWidth() + " sprite height " + this.getImageBufferHeight() + " aspect = " + getAspect() + "\n");
		setPivotPoint(shiftedorigin);
		rotate(r);
		setDocPoint(line.p1);
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Scaling to 3D scene. This should be the first transform to be applied to the sprite. 
	// The sizeInScene value becomes the size of the sprite within the scene using the scenes 3D units. This is claculated from the 
	// depth at the paste-point of the sprite using the geometry-buffers 3D methods.
	//
	public float scaleToSizeInScene(SceneData3D sceneData, float scaleModifier) {
		// scales the image to the correct size using  sizeInScene to represent the
		// items's size in the 3D scene in world units.

		float heightInPixels = getHeightInRenderTargetPixels3D(sceneData) * scaleModifier * getRelativeSizeInGroup();
		
		//System.out.println(" scaleToSizeinScene - sizeInScene:" + sizeInScene + " scaleModifyer " + scaleModifier + " height in pixels " + heightInPixels);
		float scale = (heightInPixels / getImageHeight()); 
		if (scale > 1.5f) {
			System.out.println(ImageGroupItemShortName + " overscaled, original size in pixels " + getImageHeight() + " to be scale to " + heightInPixels + " scale " + scale);
		}

		scale(scale, scale);
		//System.out.println(" scaled by "  + scale );
		return scale;
	}

	float getHeightInRenderTargetPixels3D(SceneData3D sceneData) {
		float scale3D = sceneData.get3DScale(getDocPoint());
		float heightDocSpace = sizeInScene * scale3D;

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
	// experimental: A region is extracted from the sprite via the mask image. The mask image is a grey-scale
	// image that extracts the visible region region of the sprite-image. If the mask image is white then the extraction is 100%, 127 would yield a 50% extraction etc.
	// The effect is applied to the extracted image and re-merged with the original

	public void applyEffectToMaskedRegion(BufferedImage maskImage, int function, float p1, float p2, float p3) {
		BufferedImage extractedRegion = ImageProcessing.extractImageUsingGrayscaleMask(getImage(), maskImage);
		extractedRegion = ImageProcessing.colorTransform(extractedRegion,  function,  p1,  p2,  p3);
		mergeMaskedImage(extractedRegion);
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// end of .... experimental: towards applying masked effects
	// 




	void logOperation(String operationname, Float p1, Float p2, Float p3, Float p4) {




	}

}













