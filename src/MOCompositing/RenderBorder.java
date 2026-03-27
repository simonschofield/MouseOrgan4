package MOCompositing;



import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.ImageProcessing;
import MOImage.MOColor;
import MOImageCollections.DirectoryFileNameScanner;
import MOImageCollections.ScaledImageAssetGroup;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;

/****************************************************************
* RenderBorder determines what happens to sprites pasted on the Border of an image
*
* It operates by culling/cropping/leaving-intact a sprite just before the final paste operations into the receiving output-image buffers
*
* The BorderRect is SET using Normalised coordinates (0..1 in both x and y) as this is human-readable, but STORED using docSpace coordinates

* If a sprite is completely within the Border rect then it will be pasted as normal
*
* Each of the four edges of the Border-rect can have a different Border-action
* the actions that can be done on an edge are
* NONE : allow the paste in full, no cropping at all
* EXCLUDE : do not paste any sprite overlapping this BorderRect edge
* RECT : Crop to this hard edge of the BorderRect
* BESPOKE : Apply a bespoke crop to a sprite overlapping this edge using the crop images
*
* Sometimes sprites will be cropped by two edges with different actions (or more) in which case a dominant action is decided
* with the following precedence
* (HIGH) EXCLUDED, BESPOKE_CROP, RECT_CROP, NONE (LOW)
*
* It also has a second purpose; to help log which sprites (via their unique ID) contribute to the image (i.e. are not cropped out, rejected etc). This can then be saved between sessions
* and used to speed-up subsequent renders 
*/


/**
*
* RenderBorder determines what happens to sprites pasted on the Border of an image
*
* It operates by culling/cropping/leaving-intact a sprite just before the final paste operations into the RenderTarget
*
*/
public class RenderBorder {
	
	//private ContributingSpritesList contributingSpritesList;
	
	
	public static final int	CROP_ACTION_NONE = 0;
	public static final int	CROP_ACTION_EXCLUDE = 1;
	public static final int	CROP_ACTION_RECT = 2;
	public static final int	CROP_ACTION_BESPOKE = 3;
	
	
	boolean isActive = false;

	private Rect BorderRect = new Rect();

	int leftEdgeAction = CROP_ACTION_NONE;
	int topEdgeAction = CROP_ACTION_NONE;
	int rightEdgeAction = CROP_ACTION_NONE;
	int bottomEdgeAction = CROP_ACTION_NONE;

	ScaledImageAssetGroup bespokeCropImages;
	
	// this is for selecting the particular edge-mask
	QRandomStream qRandomStream = new QRandomStream(1);
	
	
	/**
	*
	* RenderBorder determines what happens to sprites pasted on the Border of an image
	*
	* It operates by culling/cropping/leaving-intact a sprite just before the final paste operations into the receiving output-image buffers
	*
	*/
	public RenderBorder(){
		BorderRect = GlobalSettings.getTheDocumentCoordSystem().getDocumentRect();
	}
	
	
	/**
	 * Setting up the border. The left,right,top, bottom are in Normalised space, as working in DocSpace for humans is difficult. However, the boarder rectangle, one defined, is stored in DocSpace coordinates
	 * Edge actions are statics defined as
	 * CROP_ACTION_NONE : allow the paste in full, no cropping at all
	 * CROP_ACTION_EXCLUDE : do not paste any sprite overlapping this BorderRect edge
	 * CROP_ACTION_RECT : Crop to this hard edge of the BorderRect
	 * CROP_ACTION_BESPOKE : Apply a bespoke crop to a sprite overlapping this edge using the crop images
	 * 
	 * @param left - The left-most edge of the crop rectangle in normalised-space 
	 * @param top - The top-most edge of the crop rectangle in normalised-space 
	 * @param right - The right-most edge of the crop rectangle in normalised-space (usually 1-left) 
	 * @param bottom - The bottom-most edge of the crop rectangle in normalised-space (usually 1-top) 
	 * @param edgeAction  - The action (see above) used on all 4 edges
	 */
	public void setBorders(float left, float top, float right, float bottom, int edgeAction) {
		setBorders( left,  top,  right,  bottom, edgeAction,edgeAction,edgeAction,edgeAction);
	}
	
	
	/**
	 * Setting up the border. The left,right,top, bottom are in Normalised space, as working in DocSpace for humans is difficult. However, the boarder rectangle, one defined, is stored in DocSpace coordinates
	 * Edge actions are statics defined as
	 * CROP_ACTION_NONE : allow the paste in full, no cropping at all
	 * CROP_ACTION_EXCLUDE : do not paste any sprite overlapping this BorderRect edge
	 * CROP_ACTION_RECT : Crop to this hard edge of the BorderRect
	 * CROP_ACTION_BESPOKE : Apply a bespoke crop to a sprite overlapping this edge using the crop images
	 * 
	 * @param left - The left-most edge of the crop rectangle in normalised-space 
	 * @param top - The top-most edge of the crop rectangle in normalised-space 
	 * @param right - The right-most edge of the crop rectangle in normalised-space (usually 1-left) 
	 * @param bottom - The bottom-most edge of the crop rectangle in normalised-space (usually 1-top)  
	 * @param leftAction - The action (see above) used on the left edge
	 * @param topAction - The action (see above) used on the top edge
	 * @param rightAction - The action (see above) used on the right edge
	 * @param bottomAction - The action (see above) used on the bottom edge
	 */
	public void setBorders(float left, float top, float right, float bottom, int leftAction, int topAction, int rightAction, int bottomAction){
		setBorderRectWithNomalisedCoords( left,  top,  right,  bottom);
		leftEdgeAction = leftAction;
		topEdgeAction = topAction;
		rightEdgeAction = rightAction;
		bottomEdgeAction = bottomAction;
		isActive = true;
	}
	
	
	
	
	/**
	 * Loads a collection of images to define the bespoke cropping applied to each sprite. A crop image is a 32 bit png with alpha. The
	 * solid, full-alpha,  part of the crop image is used to remove pixels from the sprite within the border rect, along with anything outside the border rect. Crop images are rotated to deal with
	 * each edge of the output image. The default (unrotated) form of a crop-image is configured to be used on the left edge of the output image, so crop images must be produced in a vertical format
	 * with the left-most side being the solid-pixels crop region, and the right most edge being transparent pixels. Crop image are selected randomly from the collection, but keyed to the sprite randomKey
	 * 
	 * @param pathandfilename - path to the bespoke crop image collection. Need the full path. Something like ... GlobalSettings.getSampleLibPath() + "mask images//waveCropAlpha"
	 * @param unscaledWidth - The pixel width of the crop-image defined in pixels in the full scale (100%) output image
	 * @param unscaledHeight  - The pixel height of the crop-image defined in pixels in the full scale (100%) output image
	 */
	public void setBespokeCropImageSampleGroup(String pathandfilename, int unscaledWidth, int unscaledHeight) {
		DirectoryFileNameScanner cropdfns = new DirectoryFileNameScanner(pathandfilename, "png");
		bespokeCropImages = new ScaledImageAssetGroup("cropImages");
		bespokeCropImages.setDirectoryFileNameScanner(cropdfns);
		bespokeCropImages.loadImages();
		float sessionScale = GlobalSettings.getSessionScale();
		bespokeCropImages.resizeToAll((int)(unscaledWidth*sessionScale), (int)(unscaledHeight*sessionScale));
	}
	
	
	
	
	/**
	 * @param active - switches the cropping on and off
	 */
	public void setActive(boolean active) {
		isActive = active;
	}
	
	
	/**
	 * @return - the DocSpace border rectangle - vcan be used to display the boarder during the render process
	 */
	public Rect getBorderRect() {
		//System.out.println(">>>>getBorderRect " + BorderRect.toStr() );
		return BorderRect.copy();
	}
	
	
	/**
	 * @param docSpacePt 
	 * @return -  true if docSpacePt is fully inside the border rect
	 */
	public boolean isPointInsideBorderRect(PVector docSpacePt) {
		return BorderRect.isPointInside(docSpacePt);
	}
	
	/**
	 * @param r
	 * @return true if r is fully inside the border rect
	 */
	public boolean isWhollyInsideBorderRect(Rect r) {
		// simple interface for any doc space rect, either in or out!
		// if the renderBorder is inactive, then all rects are within it.
		if(isActive == false) return true;
		return r.isWhollyInsideOther(BorderRect);
	}
	

	/**
	 * The main method used to crop the sprite according to the conditions set.
	 * If the sprite needs cropping then the sprite's image is altered in-place. This does not alter the dimensions of the sprite image(s), rather
	 * it makes cropped pixels blank (alpha of zero).
	 * The return boolean is whether or not to continue with the sprite after crop; if false, then the sprite has been completely excluded or
	 * obliterated by the crop action. This can be used to determine whether a sprite contributes to a particular render.
	 * 
	 * @param sprite - the sprite to be cropped. Sets cropped pixels to transparent but does not alter its uncropped image size
	 * @return boolean - is this sprite completely cropped? Used in optimising ROI images
	 */
	public boolean cropSprite(Sprite sprite) {
		// has to deal with the main sprite image, and then possibly overlay images, which must be
		// cropped the same.The same bespoke cropping is guaranteed by the sprite's ran seed number
		int numImages = sprite.getNumImages();
		
		for(int n = 0; n < numImages; n++) {
			boolean cropResult = cropSpriteImage( sprite, n);
			// if any of the sprite images is fully cropped then return false, as there is no point carrying on
			if(cropResult == false) return false;
		}
		return true;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// private methods
	//
	
	
	/**
	 * @param sprite
	 * @param enumeratedImageNum
	 * @return
	 */
	private boolean cropSpriteImage(Sprite sprite, int enumeratedImageNum) {
		int numImage = sprite.getNumImages();
		
		String overlapReport = sprite.getDocSpaceRect().reportIntersection(BorderRect);
		
		//System.out.println("cropSprite 
		
		// do the trivial non-cropping actions if the image is wholly inside or outside the BorderRect
		// or is fully excluded, or there is no action to be taken
		if(overlapReport.equals("NONE")) return false;
		if(overlapReport.equals("WHOLLYINSIDE")) return true;
		if(checkCrop_Action_None(overlapReport)) return true;
		if(checkCrop_Action_Exclude(overlapReport)) return false;
		
		
		// beyond this point some cropping of the sprite takes place
		// so work out the crop-rect. First get the intersection of the sprite's rect
		// with the document rect (all in doc space units)
		Rect uncroppedSpriteRect = sprite.getDocSpaceRect();
		Rect spriteDocRectIntersection = BorderRect.getBooleanIntersection(uncroppedSpriteRect);
		
		// debug
		//Color c = MOColor.getRandomRGB(255);
		//GlobalSettings.getDocument().getMain().drawRect_DocSpace(spriteDocRectIntersection, new Color(0,0,0,0), c, 3);
		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// Shift the croppedSpriteRect so that it is relative to the uncroppedSprte rect in the sprites own space, rather   
		// than the document space.
		// this is now the sprite's local docSpace representation of the area within the sprite to be kept (i.e. cropped to)
		Rect spriteSpaceDocRectIntersection = spriteDocRectIntersection.copy();
		spriteSpaceDocRectIntersection.translate(-uncroppedSpriteRect.left, -uncroppedSpriteRect.top);
		
		
		//
		// get the BufferSpace unit equivalent of this rect
		Rect croppedRectBufferSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(spriteSpaceDocRectIntersection);
		
		// this does not work because topleft is 0,0 .. and this now translates to some abitraru point outside of local doc space. need to use units as above!
		//PVector testLT = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(spriteSpaceDocRectIntersection.getTopLeft());
		//PVector testBR = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(spriteSpaceDocRectIntersection.getBottomRight());
		//Rect testRect = new Rect(testLT,testBR);
		
		
		// System.out.println("croppedRectBufferSpace " + croppedRectBufferSpace.toStr() + " testRect " + testRect.toStr() );
		
		//System.out.println("doBespokeCrop:uncroppedSpriteRect " + uncroppedSpriteRect.toStr() + " in doc rect " + documentRect.toStr() + " translatedCroppedSpriteRect " + translatedSpriteDocRectIntersection.toStr());
		//System.out.println("doBespokeCrop:croppedRectBufferSpace " + croppedRectBufferSpace.toStr() + " sprite image size " + sprite.getImageWidth() + "," + + sprite.getImageHeight());
		// if the crop rect is zero in either dimension return the culled decision
		if(croppedRectBufferSpace.getWidth() < 1 || croppedRectBufferSpace.getHeight() < 1) {
			return false;
		}

		
		// Now we have got the basic (and legal) crop rect
		// As we don't want to complicate things, and don't want to have to adjust the origin
		// of the sprite to adjust to the new crop, we just delete the pixels outside of the croppedRectBufferSpace
		BufferedImage preCroppedImage = ImageProcessing.cropImage(sprite.getImage(enumeratedImageNum), croppedRectBufferSpace);

		
		
		if( checkBespokeCropOK() ){
			// add the bespoke crop to the cropping image set
			
			// reset the QRandim stream of the bespoke cropping randomness to the sprites ID
			qRandomStream = new QRandomStream(sprite.randomKey);
			boolean result = doBespokeCrop(preCroppedImage, overlapReport );
			if(result == false) {
				// the bespoke crop obliterated the image
				return false;
			}
			
			
		}
		
		// paste the cropped image back into the empty output image at the correct point
		BufferedImage outputImage = new BufferedImage(sprite.getImageWidth(), sprite.getImageHeight(), sprite.getCurrentImage().getType());
		ImageProcessing.compositeImage_ChangeTarget(preCroppedImage, outputImage, (int)croppedRectBufferSpace.left, (int)croppedRectBufferSpace.top, 1);
		sprite.setImage(enumeratedImageNum,outputImage);
		
	    // image has been cropped OK
		return true;
	}
	

	
	private boolean checkBespokeCropOK() {
		boolean bespokeCropCalledFor = (leftEdgeAction == CROP_ACTION_BESPOKE  || topEdgeAction == CROP_ACTION_BESPOKE ||
				 						rightEdgeAction == CROP_ACTION_BESPOKE || bottomEdgeAction == CROP_ACTION_BESPOKE );
		
		if(bespokeCropCalledFor == false) return false;
		
		if(  bespokeCropImages == null ) {
				System.out.println("RenderBorder::checkBespokeCropImagesLoaded CROP_ACTION_BESPOKE_CROP set but no crop images loaded" );
				return false;
				}
		
		return true;
		
		
	}
	
	
	// this checks to see if a particular edge has CROP_ACTION_NONE set. If so then the sprite is not cropped
	// in any way
	private boolean checkCrop_Action_None(String overlapReport) {
		
		//System.out.println(" in checkCrop_Action_None overlapReport = "  + overlapReport);
		int action = cropEdgeAction( overlapReport);
		
		if(action == CROP_ACTION_NONE) return true;
		return false;
	}
	
	
	// this checks to see if a particular edge has CROP_ACTION_NONE set. If so then the sprite is not cropped
	// in any way
	private boolean checkCrop_Action_Exclude(String overlapReport) {
			int action = cropEdgeAction( overlapReport);
			
			if(action == CROP_ACTION_EXCLUDE) return true;
			return false;
		}
		
	

	// alters the preCroppedImage
	private boolean doBespokeCrop( BufferedImage preCroppedImage, String edgeCropReport) {
		// do the bespoke crop using the selected crop image
		//System.out.println("Bespoke crop " + edgeCropReport);
		String splitEdgeReport[] = edgeCropReport.split(",");
		for(String edge:splitEdgeReport) {
			boolean result = addBespokeCropToEdge(preCroppedImage, edge);
			if(result == false) {
				// the addBespokeCropToEdge crop obliterated the image
				//System.out.println("Bespoke crop resulted in no image");
				return false;
			}
		}
		return true;
	}

	// alters the preCroppedImage
	private boolean addBespokeCropToEdge(BufferedImage preCroppedImage, String theEdge) {
		int numCropImages = bespokeCropImages.getNumImageAssets();
		int n = qRandomStream.randRangeInt(0, numCropImages-1);
		BufferedImage croppingMask = bespokeCropImages.getImage(n);
		int sourceImageW = preCroppedImage.getWidth();
		int sourceImageH = preCroppedImage.getHeight();
		//System.out.println("addBespokeCropToEdge " + theEdge + " cropping mask width " + croppingMask.getWidth() + " cropping mask hgt " + croppingMask.getHeight() + " image w " + sourceImageW + " image h " + sourceImageH);
		if(theEdge.contentEquals("LEFT")) {
			// don't need to rotate the crop image
			if(croppingMask.getWidth() > sourceImageW) return false;
			croppingMask = stretchCroppingMaskToFitEdge(croppingMask, croppingMask.getWidth(), sourceImageH);
			applyCroppingMask(preCroppedImage, croppingMask, 0, 0,theEdge);
			return true;
		}
		if(theEdge.contentEquals("RIGHT")) {
			croppingMask = ImageProcessing.rotate90(croppingMask, 2);
			if(croppingMask.getWidth() > sourceImageW) return false;
			croppingMask = stretchCroppingMaskToFitEdge(croppingMask, croppingMask.getWidth(), sourceImageH);
			applyCroppingMask(preCroppedImage, croppingMask, preCroppedImage.getWidth()-croppingMask.getWidth(), 0, theEdge);
			return true;
		}
		if(theEdge.contentEquals("TOP")) {
			croppingMask = ImageProcessing.rotate90(croppingMask, 1);
			if(croppingMask.getHeight() > sourceImageH) return false;
			croppingMask = stretchCroppingMaskToFitEdge(croppingMask, sourceImageW, croppingMask.getHeight());
			applyCroppingMask(preCroppedImage, croppingMask, 0, 0,theEdge);
			return true;
		}
		if(theEdge.contentEquals("BOTTOM")) {
			croppingMask = ImageProcessing.rotate90(croppingMask, 3);
			if(croppingMask.getHeight() > sourceImageH) return false;
			croppingMask = stretchCroppingMaskToFitEdge(croppingMask, sourceImageW, croppingMask.getHeight());
			applyCroppingMask(preCroppedImage, croppingMask, 0, preCroppedImage.getHeight()-croppingMask.getHeight(),theEdge);
			return true;
		}

		// asked to do something unknown
		return false;

	}

	// returns the source image resized to match the w,h, if w or h are larger than the source image
	// if the existing size in h or w is larger than h,w then crop in that dimension
	// if it is larger then scale in that dimension
	private BufferedImage stretchCroppingMaskToFitEdge(BufferedImage source, int newW, int newH) {
		
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

		//System.out.println("matchImageSize: postScaledSize " + source.getWidth() + " " + source.getHeight());
		return source;
	}




	private void applyCroppingMask(BufferedImage preCroppedImage, BufferedImage maskImage, int offsetX, int offsetY, String theEdge) {
		// the mask image uses its own alpha to modify the preCroppedImage
		// pixels in the preCroopeImage are made transparent (alpha'd out) where the mask image is solid.
		int maskW = maskImage.getWidth();
		int maskH = maskImage.getHeight();

		// crops out just the part we want to mask - i.e. only those pixels "under" the mask
		Rect cropR = new Rect(offsetX, offsetY, maskW, maskH);

		BufferedImage preCroppedImageOverlap = ImageProcessing.cropImage(preCroppedImage, cropR);

		// apply the crop mask. We are preserving those parts of no alpha in the mask - a hole in the mask means the pixels in the image being masked survive.
		
		// the fudge is to get rid of straggling border pixels in the cropped image left by...(my guess)... resizing of the mask image creating soft borders???
		int fudgeX=0,fudgeY=0, fudgeAmt = 4;;
		if(theEdge.contentEquals("LEFT")) { fudgeX = -fudgeAmt; fudgeY = 0; }
		if(theEdge.contentEquals("TOP")) { fudgeX = 0; fudgeY = -fudgeAmt; }
		if(theEdge.contentEquals("RIGHT")) { fudgeX = fudgeAmt; fudgeY = 0; }
		if(theEdge.contentEquals("BOTTOM")) { fudgeX = 0; fudgeY = fudgeAmt; }
		
		BufferedImage croppedByMaskImage =  ImageProcessing.getMaskedImage(preCroppedImageOverlap,  maskImage,  fudgeX, fudgeY, AlphaComposite.DST_OUT);

		// paste back in the masked section, using Porter Duff SRC - i.e. replace everything in target with source including alpha.
		ImageProcessing.compositeImage_ChangeTarget(croppedByMaskImage, preCroppedImage, offsetX, offsetY, 1.0f, AlphaComposite.SRC);


	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// if the user wants to tweak one of the Borders (maybe on the fly for different sprites) then
	// this function  offers a nullable solution. If argument is set to null then the parameter is not changed
	public void setBorderRectWithNomalisedCoords(Float newLeft, Float newTop, Float newRight, Float newBottom) {
		
		Rect normRect = getBorderRectInNormalisedCoords();
		float left = normRect.left;
		float top = normRect.top;
		float right = normRect.right;
		float bottom = normRect.bottom;
		
		if(newLeft != null) left = newLeft;
		if(newTop != null) top = newTop;
		if(newRight != null) right = newRight;
		if(newBottom != null) bottom = newBottom;
		
		setBorderRectWithNomalisedCoords( left,  top,  right,  bottom);
	}
	
	
	public Rect getBorderRectInNormalisedCoords() {
		PVector topleftDocSpace = BorderRect.getTopLeft();
		PVector bottomRightDocSpace = BorderRect.getBottomRight();
		PVector topLeftNormSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(topleftDocSpace);
		PVector bottomRightNormSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(bottomRightDocSpace);
		return new Rect(topLeftNormSpace, bottomRightNormSpace);
	}
	

	/////////////////////////////////////////////////////////////////////////////////////////////////
	//

	private void setBorderRectWithNomalisedCoords(float left, float top, float right, float bottom) {
		PVector topLeftNormSpace = new PVector(left,top);
		PVector bottomRightNormSpace = new PVector(right,bottom);
		PVector topLeft = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(topLeftNormSpace);
		PVector bottomRight = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(bottomRightNormSpace);
		
		BorderRect = new Rect(topLeft, bottomRight);
		System.out.println(">>>>RenderBorder: BorderRect " + BorderRect.toStr());
	}
	
	
	private int cropEdgeAction(String overlapReport) {
		// given an intersection edge report from the Sprite
		// decide what edge crop action to do.
		
		// if totally outside the ppa
		if(overlapReport.contentEquals("NONE")) return CROP_ACTION_EXCLUDE;
		
		ArrayList<Integer> actions = getActionsFromOverlapReport(overlapReport);
		
		if(actions.size()==0) {
			// this is a BUG FIX. Very large sprites create actions list with no elements
			return CROP_ACTION_EXCLUDE;
		}
		
		
		if(actions.size()>1) {
			// if you get this far then the overlapReport must contains two or more edges, and we have to make a decision on this
			return getDominantAction(actions);
		}
		// ..then the overlapReport must contain only one edge, so return the edgeAction
		return actions.get(0);
		
		//System.out.println("cropEdgeDecision on intersection: " + overlapReport);
		
		
	}
	
	private int getDominantAction(ArrayList<Integer> actions) {
		// only copes with 2 actions

		int action1 = actions.get(0);
		int action2 = actions.get(1);
		if( action1 == action2) return action1;
		
		// if one side is set to no crop, but the other edge is different let the different action dominate
		if( action1 == CROP_ACTION_NONE && action2 != action1) return action2;
		if( action2 == CROP_ACTION_NONE && action2 != action1) return action1;
		
		if(action1 == CROP_ACTION_EXCLUDE || action2 == CROP_ACTION_EXCLUDE) return CROP_ACTION_EXCLUDE;
		if(action1 == CROP_ACTION_BESPOKE || action2 == CROP_ACTION_BESPOKE) return CROP_ACTION_BESPOKE;
		// should cause a crash, but should never get here
		System.out.println("RenderBorder getDominantAction: problem with decided action between " + action1 +" and "+ action2);
		return -1;
	}
	
	
	private ArrayList<Integer> getActionsFromOverlapReport(String edgeString) {
		ArrayList<Integer> actionsOut = new ArrayList<Integer>();
		if(edgeString.contains("TOP")) actionsOut.add(topEdgeAction);
		if(edgeString.contains("LEFT")) actionsOut.add(leftEdgeAction);
		if(edgeString.contains("RIGHT")) actionsOut.add(rightEdgeAction);
		if(edgeString.contains("BOTTOM")) actionsOut.add(bottomEdgeAction);
		return actionsOut;
	}
	
	
}


