package MOSprite;

import java.awt.image.BufferedImage;

import MOImage.BendImage;
import MOImage.ImageProcessing;
import MOImageCollections.ScaledImageAssetGroup;
import MOImageCollections.ScaledImageAssetGroupManager;
import MOImageCollections.MOColorTransform;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.SNum;
import MOScene3D.SceneData3D;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;

public class Sprite {
	
	
	////////////////////////////////////////////////////
	// the images contained within this sprite. The first image (image(0)) is the "main" image, and sets
	// the size of all other added images. This implementation does not work with images of different dimensions
	public SpriteImages images;
	
	////////////////////////////////////////////////////
	// the id is a unique integer. It is set by the SpriteSeed constructor from the static UniqueID class declared above. 
	// This is used in seeding the sprite's random number generator, thereby ensuring the same random events happen to each sprite
	// regardless of previous random events
	// It is also used in optimisations such as registering whether or not a sprite is used in a render.
	private int uniqueID;						

	// the randomKey is used to guarantee the same outcome from stochastic processes, rather than relying on the UniqueID. It is initially set to equal the uniqueID, but if the user is unhappy
	// with the random outcomes, then the randomKey can be changed (for instance in seed batch creation), whereas UniqueID should NEVER be altered.
	public int randomKey;	
		

	////////////////////////////////////////////////////
	// Item size-in-scene data and pivot-point
	// 
	private float sizeInScene = 1; // this is the size in the scene as set by the user 
	public boolean useRelativeSizes = false;
	
	/////////////////////////////////////////////////////
	// The pivot-point - the local origin for pasting and transforms
	//
	private PVector pivotPoint = new PVector(0.5f, 0.5f);
	
	/////////////////////////////////////////////////////
	// the doc point used to position  the item in the scene
	// While certain processes generate a doc point with Z value for depth, the docPoint should never contain
	// any Z value, as this will screw up scaling calculations
	public PVector docPoint = new PVector(0.5f, 0.5f);
	
	/////////////////////////////////////////////////////
	// the depth is set to the normalised depth in the 3D scene, 
	// usually used to sort the render order of the seeds
	// 
	public float depth = 1;	
		
	/////////////////////////////////////////////////////
	// refers to the "main" image within the sprite - its ImmageAssetGroup, and its Shortname. This may become a property of the ImageAsset
	public String ImageAssetGroupName = "";		
	public String ImageGroupItemShortName= "";						
	//public float alpha = 1;
	
	
	///////////////////////////////////////////////////////////////
	// for internal workings
	//
	//public SpriteImageQuad imageQuad;
	private QRandomStream qRandomStream;
	
	
	////////////////////////////////////////////////////////////
	// Extra Sprite Data
	//
	// To be replaced with a keyValuePairList called spriteData
	
	public KeyValuePairList spriteData = new KeyValuePairList();
	
	

	public Sprite() {
		SpriteSeed seed = new SpriteSeed();
		setSpriteSeedData(seed);
		setRandomStreamPos(this.randomKey);
		//overlayImages = new SpriteOverlayImages(this);
		images = new SpriteImages();
	}

	public Sprite(BufferedImage img) {
		SpriteSeed seed = new SpriteSeed();
		setSpriteSeedData(seed);
		setRandomStreamPos(this.randomKey);
		
		//overlayImages = new SpriteOverlayImages(this);
		images = new SpriteImages();
		setMainImage(img);
	}


	public Sprite(SpriteSeed seed) {
		setSpriteSeedData(seed);
		setRandomStreamPos(this.randomKey);
		
		images = new SpriteImages();
	}
	
	public void setSpriteSeedData(SpriteSeed seed) {
		KeyValuePairList kvlist = seed.getAsKeyValuePairList();
		//System.out.println("setSpriteSeedData " + kvlist.getAsCSVLine());
		setSpriteData( kvlist );
		
		
	}
	
	
	
	public void setSpriteData(KeyValuePairList dataIn) {

		if( dataIn.keyExists("DocPoint") ) {
			float[] dp = dataIn.getVector("DocPoint");
			docPoint = new PVector(dp[0], dp[1]);
		}
		
		if( dataIn.keyExists("UniqueID") ) {
			uniqueID = dataIn.getInt("UniqueID");
		}
		
		if( dataIn.keyExists("RandomKey") ) {
			randomKey = dataIn.getInt("RandomKey");
		}
		
		if( dataIn.keyExists("PivotPoint") ) {
			float[] pp = dataIn.getVector("PivotPoint");
			pivotPoint = new PVector(pp[0], pp[1]);
		}
		
		if( dataIn.keyExists("SizeInScene") ) {
			sizeInScene = dataIn.getFloat("SizeInScene");
		}
		
		if( dataIn.keyExists("UseRelativeSizes") ) {
			useRelativeSizes = dataIn.getBoolean("UseRelativeSizes");
		}
		
		if( dataIn.keyExists("Depth") ) {
			depth = dataIn.getFloat("Depth");
		}
		
		
		if( dataIn.keyExists("ImageAssetGroupName")) {
			ImageAssetGroupName  = dataIn.getString("ImageAssetGroupName");
		}
		
		if( dataIn.keyExists("ImageGroupItemShortName")) {
			ImageGroupItemShortName  = dataIn.getString("ImageGroupItemShortName");
		}

		String[] excluded = {"DocPoint", "UniqueID", "RandomKey", "PivotPoint", "Depth", "SizeInScene", "UseRelativeSizes", "ImageAssetGroupName", "ImageGroupItemShortName"};
		//System.out.println("Sprite data to append is " + dataIn.getAsCSVLine());
		spriteData.append(dataIn, excluded); 
		//System.out.println("Sprite data is now " + spriteData.getAsCSVLine());
	}
	
	// for convenience
	public void setSpriteData(KeyValuePair kvp) {
		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.addKeyValuePair(kvp);
		setSpriteData(kvlist);
	}
	
	// generally for identification purposes
	String getSpiteDataString(String key) {
		String s = spriteData.getString(key);
		if(s == null) {
			System.out.println("Sprite2::getSpiteDataString, the key " + key + " does not exist");
		}
		return s;
		
	}
	
	public boolean spiteDataStringEquals(String key, String matchThis) {
		String s = spriteData.getString(key);
		if(s == null) {
			System.out.println("Sprite2::getSpiteDataStringEquals, the key " + key + " does not exist");
			return false;
		}
		return s.equals(matchThis);
		
	}
	
	public boolean spiteDataStringContains(String key, String containsThis) {
		String s = spriteData.getString(key);
		if(s == null) {
			return false;
		}
		return s.contains(containsThis);

	}
	
	
	public void addImage(BufferedImage img, String name) {
		images.setImage(name, img);
	}
	
	public BufferedImage getImage(int i) {
		return images.getImage(i);
	}
	
	public void setImage(int i, BufferedImage img) {
		images.setImage(i, img);
	}
	
	public int getNumImages() {
		return images.getNumImages();
	}
	
	
	public BufferedImage getImage(String name) {
		return images.getImage(name);
	}
	
	// for convenience
	public BufferedImage getMainImage() {
		return images.getImage(0);
	}

	// for convenience
	public void setMainImage(BufferedImage img) {
		images.setImage("main",img);
	}


	
	
	
	// called by the generating sprite font
	// this selects the image as well so is good to go....
	public void setSpriteFontDataAndSelectImage(SpriteFont sf) {
		
		int index = sf.getRandomSpriteImageGroupItemNumber();
		ScaledImageAssetGroup siag = sf.getSpriteImageGroup();
		String shortName = siag.getImageAssetName(index);
		BufferedImage img = siag.getImage(index);
		setMainImage(img);
		
		KeyValuePairList kvlist = new KeyValuePairList();

		kvlist.addKeyValue("SpriteFontName", sf.thisSpriteFontName); 
		kvlist.addKeyValue("ImageAssetGroupName" , sf.imageSampleGroupName);
		kvlist.addKeyValue("ImageGroupItemShortName" , shortName);
		kvlist.addKeyValue("SizeInScene" , sf.sizeInScene);
		kvlist.addKeyValue("UseRelativeSizes" , sf.useRelativeSizes);
		kvlist.addKeyValue("PivotPoint" , sf.spritePivotPoint.array());

		setSpriteData(kvlist);
	}

	public Sprite copy() {
		// returns a completely identical, but independent copy
		// The image is set by shared reference for speed purposes
		//
		Sprite cpy = new Sprite();

		cpy.uniqueID = this.getUniqueID();
		cpy.randomKey = this.randomKey;
		
		cpy.ImageAssetGroupName = this.ImageAssetGroupName;
		cpy.ImageGroupItemShortName= this.ImageGroupItemShortName;

		cpy.sizeInScene = this.sizeInScene;
		cpy.useRelativeSizes = this.useRelativeSizes;
		cpy.pivotPoint = this.pivotPoint.copy();

		cpy.docPoint = this.docPoint.copy();
		cpy.depth = this.depth;

		cpy.setRandomStream(this.qRandomStream);
		
		cpy.images = this.images;

		cpy.spriteData = spriteData.copy();
		
		return cpy;
	}


	public void initQuad() {

		//imageQuad = new SpriteImageQuad(this);
	}

	public void setRandomStreamPos(int rseed) {
		// a sprite's random stream is set by this
		// therefore guaranteeing reproducible effects to this sprite.
		// When is sprite created without a seed, the sprite batch manager
		// generates a sprite using a unique ID. 
		qRandomStream = new QRandomStream(rseed);
	}

	public int getID() {
		return getUniqueID();
	}

	String toStr() {
		//return "ImageSprite seed:" + seed.toStr() + " own doc pt:" + docPoint + " Image:" + image;
		//return "ImageSprite doc pt:" + docPoint + " Image:" + getImageName();
		return "";
	}

	

	public int getImageWidth() {
		return getMainImage().getWidth();

	}

	public int getImageHeight() {
		return getMainImage().getHeight();

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
		//imageQuad.setPivotPoint(pivotPoint);
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


	///////////////////////////////////////////////////////////////////////////////////////////////
	// Maps any pixel location in the sprite's own image to the document buffer space
	// taking into account the sprite's docPoint.
	// This is the only place where the sprite's docPoint and pivotPoint is used to map to the buffer space paste region
	//
	public PVector spriteLocalBufferSpaceToDocumentBufferSpace(PVector spriteBufferSpace) {

		// work out where the top left of the sprite buffer is in relation to the document
		PVector bufferSpacePastePoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(this.getDocPoint());
		PVector spriteOffsetBufferSpace = getPivotPointLocalBufferSpace();
		PVector topLeftOfSpriteImageInDocBufferSpace = PVector.sub(bufferSpacePastePoint, spriteOffsetBufferSpace);

		// now add the spriteBufferSpace location to topLeftOfSpriteImageInBufferSpace
		return PVector.add(spriteBufferSpace, topLeftOfSpriteImageInDocBufferSpace);

	}
	
	private PVector getPivotPointLocalBufferSpace() {
		// this is the amount you add in pixels to shift the sprite according to its pivot point
		// so an origin of (1,1) (bottom right hand corner) would result in a subtraction of the whole width and height of sprite pos.
		int offsetX = (int) ( pivotPoint.x * getImageWidth() );
		int offsetY = (int) ( pivotPoint.y * getImageHeight() );

		return new PVector(offsetX, offsetY);
	}

		
	public Rect getDocumentBufferSpaceRect() {
		// returns the rect in BufferSpace of the sprite at its current docPoint
		PVector topLeft = spriteLocalBufferSpaceToDocumentBufferSpace(  new PVector(0,0));
		PVector bottomRight = spriteLocalBufferSpaceToDocumentBufferSpace( new PVector(getImageWidth()-1,getImageHeight()-1));
		return new Rect(topLeft, bottomRight);
	}

	public Rect getDocSpaceRect() {
		// returns the rect in DocumentSpace of the sprite at its current docPoint
		Rect docBufferRect = getDocumentBufferSpaceRect();
		return GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(docBufferRect);
	}



	////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	//

	//public PVector getQuadPoint(PVector docPt, float  nx, float ny) {
	//	return imageQuad.getQuadDocumentPoint(docPt, nx, ny);
	//}


	SNum getSNum(int seedOffset, Integer sequencePos) {
		return qRandomStream.snum(seedOffset, sequencePos);
	}



	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// geometric transforms change the sprite image in-place
	//
	public void scale(float scaleW, float scaleH) {
		//imageQuad.theImage = image;
		//imageQuad.applyScale(scaleW, scaleH);

		//if( isQuickRenderMode() ) return;
		
		if(scaleW==scaleH) {
			// chance to use double scaling on very big scale reductions
			//setImage(ImageProcessing.scaleImage(getImage(), scaleW));
		}else {
			//setImage(ImageProcessing.scaleImage(getImage(), scaleW, scaleH));
		}

		images.scale(scaleW, scaleH);
	}

	public void rotate(float degrees) {
		// this function has the visual effect of rotating the image of the sprite around the sprite origin
		// It does so by rotating the image and the pivot point around the image centre, rather than rotating the image around
		// the pivot point, which could result in a much larger resultant image.
		// When the shape is pastes using its new pivot point, it is as if it has been rotated around the origin
		// Much more efficient than otherwise

		
		//imageQuad.applyRotation2(degrees);

		double toRad = Math.toRadians(degrees);

		int oldBufferWidth = getImageWidth();
		int oldBufferHeight = getImageHeight();
		images.rotate(degrees);
		//setImage(ImageProcessing.rotateImage(getImage(), degrees));

		// the rest is about rotating the origin point.
		// Rotate rotation point around (0,0) in image-pixel space
		float rx = oldBufferWidth * (pivotPoint.x - 0.5f);
		float ry = oldBufferHeight * (pivotPoint.y - 0.5f);

		float newX = (float) (rx * Math.cos(toRad) - ry * Math.sin(toRad));
		float newY = (float) (ry * Math.cos(toRad) + rx * Math.sin(toRad));

		pivotPoint.x = (newX / getImageWidth()) + 0.5f;
		pivotPoint.y = (newY / getImageHeight()) + 0.5f;
		
		

	}
	
	



	public void mirror(boolean inX) {
		
		
		//imageQuad.applyMirror(inX);
		
		//if( isQuickRenderMode() ) return;
		
		
		if (inX) {
			//setImage(ImageProcessing.mirrorImage(getImage(), true, false));
			pivotPoint.x = 1.0f - pivotPoint.x;
		} else {
			// in Y
			//setImage(ImageProcessing.mirrorImage(getImage(), false, true));
			pivotPoint.y = 1.0f - pivotPoint.y;
		}
		
		images.mirror(inX);
		
	}
	




	/**
	 * Bends an image to the left
	 * The bend harshness is the gamma applied to the curve. 1.2 == very gentle curve over the length of the image, 10.0 == very harsh curve at the end of the image
	 * @param startBend -  just set this to 0, as other results are poor. .....is always a parametric 0..1, where 0 is the bottom of the image
	 * @param bendAmt -  0 == no displacement, 1 == the displacement is equivalent to the image height (which would be huge)
	 * @param severity - is the gamma applied to the curve. 1.2 == very gentle curve over the length of the image, 10.0 == very harsh curve at the end of the image
	 */
	public void bend(float startBend, float bendAmt, float severity) {
		
		//imageQuad.applyHorizontalTopShear2(bendAmt);
		
		//if( isQuickRenderMode() ) return;
		
		float oldWidth = getImageWidth();
		
		BendImage bendImage = new BendImage();
		
		images.bend(startBend, bendAmt, severity);
		//BufferedImage bentImage = bendImage.bendImage(this.getImage(), startBend, bendAmt, severity);
		//this.setImage(bentImage);

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
		float heightInPixels = GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(sizeY);
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
		int i = getImageAssetGroup().getIndexOfImageAsset(ImageGroupItemShortName);
		return getImageAssetGroup().getRelativeImageHeight(i);
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
			float widthInPixels = GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(sizeX);
			scaleX = (widthInPixels / getImageWidth());

			if (scaleX > 2) {
				System.out.println(
						ImageAssetGroupName + "/" + ImageGroupItemShortName + " scaleToSizeInDocSpace overscaled in X, original size in pixels "
								+ getImageWidth() + " to be scale to " + widthInPixels + " scale " + scaleX);
			}
		}

		if (sizeY != null) {
			float heightInPixels = GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(sizeY);
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
	// The sizeInScene value becomes the size of the sprite within the scene using the scenes 3D units. This is calculated from the 
	// depth at the paste-point of the sprite using the geometry-buffers 3D methods, based on the sprites height .
	//
	// Size in scene does not take into account the pivot point, and is purely relative to the height of the image. It is concerned only with scaling the bitmap appropriately.
	//
	public float scaleToSizeInScene(SceneData3D sceneData, float scaleModifier) {
		// scales the image to the correct size using  sizeInScene to represent the
		// items's size in the 3D scene in world units.
		// System.out.println("Sprite ID " + this.uniqueID);
		sizeInScene *= scaleModifier;
		float heightInPixels = getBufferSpaceHeightFromSizeInScene(sceneData) * getRelativeSizeInGroup();
		
		//System.out.println(" scaleToSizeinScene - sizeInScene:" + sizeInScene + " scaleModifyer " + scaleModifier + " target height in pixels " + heightInPixels+ " sprite image height " + getImageHeight());
		float scale = (heightInPixels / getImageHeight()); 
		if (scale > 1.5f) {
			System.out.println(ImageGroupItemShortName + " overscaled, original size in pixels " + getImageHeight() + " to be scale to " + heightInPixels + " scale " + scale);
		}
		//System.out.println(" scaled by "  + scale );
		scale(scale, scale);
		
		
		
		
		return scale;
	}

	// get getBufferSpaceSizeInScene
	float getBufferSpaceHeightFromSizeInScene(SceneData3D sceneData) {
		float scale3D = sceneData.get3DScale(getDocPoint());
		float heightDocSpace = sizeInScene * scale3D;

		//System.out.println("getHeightInRenderTargetPixels3D: scale3D " + scale3D );
		float docSizeInPixels =  GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(heightDocSpace);
		//System.out.println("sprite point " + docPoint.toString() + " height doc space = " + heightDocSpace + "  size pixels " + docSizeInPixels);
		//System.out.println();
		return docSizeInPixels;
	}


	

	///////////////////////////////////////////////////////////////////////////////////////////
	/// end of geometric transforms
	///////////////////////////////////////////////////////////////////////////////////////////

	//public void  colorTransform(int function, float p1, float p2, float p3) {
	//	setImage(ImageProcessing.colorTransform( getImage(),  function,  p1,  p2,  p3));
	//}

	public void colorTransform(MOColorTransform colTransform) {

		setMainImage(colTransform.doColorTransforms(getMainImage()));
		
	}



	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// experimental: towards applying masked effects
	// think this is pretty rubbish.....


	
	public void mergeMaskedImage(BufferedImage maskedImage){

		ImageProcessing.compositeImage_ChangeTarget(maskedImage, getMainImage(), 0, 0, 1);

	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// experimental: A region is extracted from the sprite via the mask image. The mask image is a grey-scale
	// image that extracts the visible region region of the sprite-image. If the mask image is white then the extraction is 100%, 127 would yield a 50% extraction etc.
	// The effect is applied to the extracted image and re-merged with the original

	
	

	public int getUniqueID() {
		return uniqueID;
	}

}

