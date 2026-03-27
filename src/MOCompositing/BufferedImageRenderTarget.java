package MOCompositing;

////////////////////////////////////////////////////////////////////////////////
// Render target class
// The documentROI is in real document space coordinates
// This is a Java Graphics2D implementation of the render target to
// hopefully open up all of Java's graphics functionality

import java.awt.*;
import java.awt.color.ColorSpace;
//import java.awt.event.*;
//import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import MOAppSessionHelpers.SceneHelper;
import MOImage.FloatImage;
import MOImage.ImageProcessing;
import MOImage.MOPackedColor;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOSprite.Sprite;
import MOUtils.ImageCoordinateSystem;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;
import MOVectorGraphics.VectorShape;
import MOVectorGraphics.VectorShapeDrawer;


/**
 * Contains a BufferedImage as a document output image and methods enabling graphics operations, such as sprite pasting and shape drawing, onto that image. A render target is named, and accessed through its name.
 * The RenderTargetInterface ensures that the two different types of RenderTargets (FloatImageRenderTarget and BufferedImageRenderTarget) can be in the same list within the MainDocument RenderTarget list.
 * All images in the MinDocument are the same dimension.
 */
public class BufferedImageRenderTarget implements RenderTargetInterface{
	private Graphics2D graphics2D;
	protected BufferedImage targetRenderImage;
	
	public boolean saveRenderAtEndOfSession = true;
	
	
	public ImageCoordinateSystem coordinateSystem;
	

	VectorShapeDrawer shapeDrawer;
	
	String renderTargetName = "";
	

	/**
	 * Contains a BufferedImage as a document output image and methods enabling graphics operations, such as sprite pasting and shape drawing, onto that image. A render target is named, and accessed through its name.
	 * The RenderTargetInterface ensures that the two different types of RenderTargets (FloatImageRenderTarget and BufferedImageRenderTarget) can be in the same list within the MainDocument's RenderTargets list.
	 * All images in the MinDocument are the same dimension. They types of buffered image are BufferedImage.TYPE_INT_ARGB  Used for colour + alpha images in, may also be used to provide INT images if required.
	 * TYPE_BYTE_GRAY  for 8 bit grey scale images. TYPE_USHORT_GRAY  for 16 bit greyscale images<p>
	 * @param w - the pixel width of the buffered image
	 * @param h - the pixel height of the buffered image
	 * @param imgType - Three BufferedImage static types are used in the system:- TYPE_INT_ARGB, TYPE_BYTE_GRAY and TYPE_USHORT_GRAY
	 * 
	 */
	public BufferedImageRenderTarget(String name, int w, int h, int imgType) {
		
		coordinateSystem = GlobalSettings.getTheDocumentCoordSystem();
		
		targetRenderImage = new BufferedImage(w, h, imgType);
		
		
		graphics2D = targetRenderImage.createGraphics();
		getGraphics2D().setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		shapeDrawer = new VectorShapeDrawer(getGraphics2D());
		
		renderTargetName = name;
	}
	

	/**
	 * Returns the name of this render target
	 */
	public String getName() {
		return renderTargetName;
	}
	
	/**
	 * Called by the render save on saving the individual render targets
	 */
	public String getFullSessionName() {
		String sessname = GlobalSettings.getDocumentName() + "_" +  renderTargetName;
		return sessname;
	}
	
	/**
	 * returns the file extension to be used when saving the image contained in this render target
	 */
	public String getFileExtension() {
		return ".png";
	}
	
	
	
	/**
	 * Returns the BufferedImage contained within
	 */
	public BufferedImage getBufferedImage() {
		return targetRenderImage;
	}
	
	/**
	 * Returns a deep copy of BufferedImage contained within
	 */
	public BufferedImage copyImage() {
		return ImageProcessing.copyImage(targetRenderImage);
	}
	
	/**
	 * returns the image type contained within
	 */
	public int getImageType() {
		int t =  targetRenderImage.getType();
		if(t==0) {
			System.out.println("BufferedImageRenderTarget::getType - is returning 0 ... this is reserved for Floating Point images");
		}
		return t;
	}
	
	

	
	/**
	 * Returns the coordinate system used (should be the same for all RenderTargets in the MainDocumen)
	 */
	public ImageCoordinateSystem getCoordinateSystem() {
		return coordinateSystem;
	}
	
	/**
	 * Fills the image with "blank" i.e. transparent black
	 */
	public void clearImage() {
		Color blank = new Color(0,0,0,0);
		fillBackground(blank); 
	}

	/**
	 * Sets the whole RenderTarget image to colour c
	 * @param c - Color type, including alpha
	 */
	public void fillBackground(Color c) {
		getGraphics2D().setBackground(c);
		getGraphics2D().clearRect(0, 0, coordinateSystem.getBufferWidth(), coordinateSystem.getBufferHeight());
	}
	
	/**
	 * Legacy from NNetwork stuff. Works in document space. Clears everything outside this rect (i.e. sets to Color(0,0,0,0))
	 * @param r
	 */
	public void clearOutsideRect_DocSpace(Rect r) {
		// deletes everything outside of the rect
		float dW = coordinateSystem.getDocumentWidth();
		float dH = coordinateSystem.getDocumentHeight();
		float rl = r.left;
		float rt = r.top;
		float rw = r.getWidth();
		float rh = r.getHeight();
		// the top rect
		Rect topRect = new Rect(0,0,dW,rt); 
		Rect leftRect = new Rect(0,rt,rl,rh);
		Rect rightRect = new Rect(rl+rw, rt, dW-(rl+rw), rh);
		Rect bottomRect = new Rect(0,rt+rh, dW, dH-(rt+rh));
		fillBackground_DocSpace( topRect, new Color(0,0,0,0));
		fillBackground_DocSpace( leftRect, new Color(0,0,0,0));
		fillBackground_DocSpace( rightRect, new Color(0,0,0,0));
		fillBackground_DocSpace( bottomRect, new Color(0,0,0,0));
	}
	
	
	
	/**
	 * Fills a rectangle with colour fillColor (not the same as drawing a rect, as it uses Graphics2D.clearBackground())
	 * @param r
	 * @param fillColor
	 */
	public void fillBackground_DocSpace(Rect r, Color fillColor) {
		// not the same as drawing a rect, as it uses Graphics2D.clearBackground()
		// which has no line styles etc....
		Rect bufferRect = coordinateSystem.docSpaceToBufferSpace(r);
		fillBackground_BufferSpace(bufferRect, fillColor);
	}
	
	
	/**
	 * Fills a rectangular region defined in buffer-space by r, with color c
	 * @param r - a rectangle in buffer space (therefore integers) defining the region to be filled
	 * @param c -Color type, including alpha
	 */
	public void fillBackground_BufferSpace(Rect r, Color c) {
		getGraphics2D().setBackground(c);
		getGraphics2D().clearRect((int)r.left, (int) r.top, (int) r.getWidth(), (int) r.getHeight());
	}
	
	/**
	 * Replaces the content of the buffered image with the image img
	 * @param img - The image. This is re-sized to fit the entire Rendertarget image
	 * @param alpha - an alpha amount used overall
	 */
	public void fillBackgroundWithImage(BufferedImage img, float alpha) {
		BufferedImage resizedImage = ImageProcessing.resizeTo(img, coordinateSystem.getBufferWidth(), coordinateSystem.getBufferHeight());
		pasteImage_BufferSpace(resizedImage,0,0,alpha);
	}


	/**
	 * Generic sprite paste. Pastes the pixel values of the image into the receiving RenderTarget using AlphaComposite.SRC_OVER
	 * @param sprite - the sprite being pasted.
	 */
	public void pasteSprite(Sprite sprite) {
		
		int x = (int) sprite.getDocumentBufferSpaceRect().getTopLeft().x;
		int y = (int) sprite.getDocumentBufferSpaceRect().getTopLeft().y;
		
		float alpha = 1;
		if( sprite.spriteData.keyExists("Alpha") ) {
			alpha = sprite.spriteData.getFloat("Alpha");
		}

		pasteImage_BufferSpace(sprite.getCurrentImage(), x,y,  alpha);
	}
	
	
	
	/**
	 * Pastes the Color-type c through the sprite's alpha channel. I.e. alters a pixel in the targetRenderImage where the alpha in the sprite image is > 0
	 * @param sprite - the sprite who' alpha is being used to paste the colour c
	 * @param c - the colour pasted to the image
	 */
	public void pasteSprite_ReplaceColour(Sprite sprite, Color c) {
		pasteImage_ReplaceColour(sprite.getCurrentImage(), sprite.getDocSpaceRect().getTopLeft(),  c);
	}
	
	
	
	/**
	 * uses an alternative image when pasting - Uses the AltImage's alpha to define the pasted shape, not the sprite's main image alpha, unless useSpriteAlpha == true in which 
	 * case the sprite's alpha is used as to on the altImage though using AlphaComposite.SRC_IN.
	 * @param sprite
	 * @param altImage - The image to be used instead of the sprite's own image. It is resized to the same dimensions as the sprite's image
	 * @param alpha - an alpha amount of the new pasted pixels
	 * @param useSpriteAlpha - if true, then use the sprite's existing alpha channel tp paste the altImage "through"
	 */
	public void pasteSprite_AltImage(Sprite sprite, BufferedImage altImage, float alpha, boolean useSpriteAlpha) {
		
		if(useSpriteAlpha) {
			BufferedImage altImageWithSpritesAlpha = ImageProcessing.replaceVisiblePixels(sprite.getCurrentImage(), altImage);
			pasteImage_DocSpace(altImageWithSpritesAlpha, sprite.getDocSpaceRect().getTopLeft(),  alpha);
		}else {
			pasteImage_DocSpace(altImage, sprite.getDocSpaceRect().getTopLeft(),  alpha);
		}
		
	}
	
	/**
	 * A contribution mask is a separate grey-scale render showing where a particular type of sprite contributes to the output image. This can then be used in post processing to treat 
	 * just those areas where the contributing sprite is visible.  It is down to the user to determine if a particular sprite contributes or not;  for instance using the sprite batch name, 
	 * or asset type to determine this before calling this method. To work correctly, ALL sprites must be submitted to this method; non-contributing sprites are just as important as contributing ones.<p>
	 * 
	 * In many cases this results in a simple black and white mask, but a replacement image can be used also, which is sized to the sprite's image dimensions and 
	 * pasted through the sprite's visible region. This has been used in the past to add in grey-scale ramp effects <p>
	 * 
	 * When using the method, if the contribution is TRUE then this sprite (or sprite overlay image) is ADDED to the mask (i.e. pastes white (or replacement image) to the mask). If contribution is FALSE then the sprite is SUBTRACTED from the mask (i.e. pastes black).
	 * This algorithm assumes the mask image named exists. The replacementImage is nullable. If not null, the replacement image is (scaled then) pasted. If null, then the sprite is pasted in white.<p>
	 *
	 * @param sprite - the sprite being pasted
	 * @param spriteOverlayImageName - The sprite overlayImage name; in many cases this will just be "main". If not set, null or blank, then main is used
	 * @param contribute - TRUE paste white or replacement image, FALSE paste black
	 * @param replacementImage - An image, scaled and pasted through the visible regions of the sprite.  Is Nullable, in which chase simple white/black pasting happens.
	 * @param brightness  - adjusts the brightness of the contribution colour, or the replacementImage if used (0..1)
	 */
	public void pasteSprite_ContributionMask(Sprite sprite,  String spriteOverlayImageName, boolean contribute, BufferedImage replacementImage, float brightness) {
		// The full logic ...
		// If contribute == false, OR the overlayImage is not main, then always paste whole (main) image in black, then...
		// If contribute == true, then contribute the image (either main or overlayName) in white, or if the replacement image is specified, use the replacement imae as the in-fill.
		
		if( sprite.containsImageNamed(spriteOverlayImageName) == false) {
			//System.out.println("RenderTarget::pasteSprite_ContributionMask - sprite does not have an overlay image named " + spriteOverlayImageName + ", setting to main image");
			sprite.setCurrentImage("main");
		}
		
		
		if( contribute == false ||  !spriteOverlayImageName.equals("main") ) {
			sprite.setCurrentImage("main");
			pasteSprite_ReplaceColour(sprite, Color.black);
		}
		
		
		if( contribute == true ) {
			
			sprite.setCurrentImage(spriteOverlayImageName);
			
			if(replacementImage!=null) {
				BufferedImage blendedImage = ImageProcessing.replaceVisiblePixels(sprite.getCurrentImage(), replacementImage);

				if(brightness < 1) {
					blendedImage = ImageProcessing.adjustBrightness(blendedImage, brightness);
				}


				pasteSprite_AltImage(sprite, blendedImage,1f,false);
			}else {

				Color c = Color.white;
				if(brightness < 1) {
					c = new Color(brightness,brightness,brightness);
				}


				pasteSprite_ReplaceColour(sprite, c);
				}

			
			sprite.setCurrentImage("main");
			
		} 

		

	}

	
	/**
	 * Experimental erase operation. Rather than adding to what is already there, this method reduces the alpha of pixels within the existing targetRenderImage. It subtracts the 
	 * sprite's alpha from the existing alpha. The sprite's colour is ignored. 
	 * @param sprite
	 */
	public void pasteSprite_Erase(Sprite sprite) {
		PVector topLeftDocSpace = sprite.getDocSpaceRect().getTopLeft();
		PVector bufferPt = coordinateSystem.docSpaceToBufferSpace(topLeftDocSpace);
		
		float alpha = 1;
		if( sprite.spriteData.keyExists("Alpha") ) {
			alpha = sprite.spriteData.getFloat("Alpha");
		}
		
		
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_OUT, alpha);
		getGraphics2D().setComposite(ac);
		getGraphics2D().drawImage(sprite.getCurrentImage(), (int)bufferPt.x, (int)bufferPt.y, null);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// 
	// 
	/**
	 * pastes the topleft of the image img at docSpaceTopLeftPoint
	 * @param img - the image being pasted using  AlphaComposite.SRC_OVER
	 * @param docSpaceTopLeftPoint - the doc-space point in the RenderTarget, at which the top-left of img will be pasted
	 * @param alpha - an over-all alpha amount in the range 0-1
	 */
	public void pasteImage_DocSpace(BufferedImage img, PVector docSpaceTopLeftPoint, float alpha) {
		//Rect r = getPasteRectDocSpace(img, docSpacePoint);
		PVector bufferPt = coordinateSystem.docSpaceToBufferSpace(docSpaceTopLeftPoint);
		pasteImage_BufferSpace(img, (int) bufferPt.x, (int) bufferPt.y, alpha);
	}

	
	/**
	 * This is the method that actually does the compositing of the sprite image to the RenderTarget image using AlphaComposite.SRC_OVER
	 * Also used in ScaledImageAssetGroupManager.ParadeContent(..)
	 * @param img
	 * @param x - The buffer-space x coordinate at which the top-left of the sprite's image is composited
	 * @param y - The buffer-space y coordinate at which the top-left of the sprite's image is composited
	 * @param alpha - an over-all alpha amount in the range 0-1
	 */
	public void pasteImage_BufferSpace(BufferedImage img, int x, int y, float alpha) {
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		getGraphics2D().setComposite(ac);
		getGraphics2D().drawImage(img, x, y, null);
	}
	
	/**
	 * Pastes an image's top-left at the RenderTaget docSpacePoint BUT, replaces the colour of the image with a single colour while maintaining the alpha of the pasted image
	 * @param img - the image being pasted as a "mask" through which color c is applied
	 * @param docSpacePoint - the point in the Rendertaar=get in DocSpace where the top-left of the image will be applied
	 * @param c - the replacement colour
	 */
	public void pasteImage_ReplaceColour(BufferedImage img, PVector docSpacePoint, Color c) {
		BufferedImage spriteMaskImage =  ImageProcessing.replaceColor(img, c);
		pasteImage_DocSpace(spriteMaskImage, docSpacePoint, 1);
	}
	
	
	/**
	 * Used in creating SpriteID-images via the SceneInformationInspector class if sprite ID's are set to be added
	 * @param sprite - the sprite's uniqueID number is pasted as an integer to the BufferedImage (so it has to be a TYPE_INT_ARGB image). 
	 * These can then be queried back from the image to determine exactly which sprite was pasted where.
	 */
	public void pasteSpriteIDToARGBImage(Sprite sprite) {
		if(getImageType() != BufferedImage.TYPE_INT_ARGB) {
			System.out.println("RenderTarget.pasteSpriteIDToARGBImage :: target image is not TYPE_INT_ARGB");
			return;
		}
		
		//WritableRaster renderTargeImageData = targetRenderImage.getRaster();
		//WritableRaster spriteImageAlphaData = sprite.getMainImage().getAlphaRaster();
		
		Rect bufferSpaceRect = sprite.getDocumentBufferSpaceRect();
		int idval = sprite.getID();
		BufferedImage spriteImage = sprite.getCurrentImage();
		
		//System.out.println(" UShort Depth  = " + shortval);
		for (int y = 0; y < bufferSpaceRect.getHeight(); y++) {
			for (int x = 0; x < bufferSpaceRect.getWidth(); x++) {
				
				
				
				
				int spriteRGBA = spriteImage.getRGB(x, y);
				int alpha = MOPackedColor.getAlpha(spriteRGBA);
				
				
				int bufferPointX = (int) (x + bufferSpaceRect.left);
				int bufferPointY = (int) (y + bufferSpaceRect.top);
				
				if( bufferSpaceRect.isPointInside(bufferPointX, bufferPointY)==false) continue;
				//System.out.println("wirting ID :: " + idval + " at pt " + bufferPointX + "," + bufferPointY);
				if(alpha>16) this.setPixel(bufferPointX, bufferPointY,idval);
				
				
			}
		}
		
	}
	
	
	/**
	 * Called by the SceneInformationInspector when spriteID's are required. Used in conjunction with spriteID image only (see the pasteSpriteIDToARGBImage(..) method in this class) 
	 * @param docPoint
	 * @return
	 */
	public int getSpriteID(PVector docPoint) {
		
		int id = getPixel(docPoint);
		
		return id;
	}
	
	
	
	/**
	 * Sets a pixel at buffer space X,y to the Color type c.
	 * Direct access to the target image. Slow so use carefully
	 * @param x - Buffer-space x coordinate
	 * @param y - Buffer-space y coordinate
	 * @param c - colour as Color-type
	 */
	public void setPixel(int x, int y, Color c) {
		
		targetRenderImage.setRGB(x, y, c.getRGB());
		
	}
	
	/**
	 * Sets a pixel at buffer space X,y to the packed colour c.
	 * Direct access to the target image. Slow so use carefully
	 * @param x - Buffer-space x coordinate
	 * @param y - Buffer-space y coordinate
	 * @param c - colour as packed integer
	 */
	public void setPixel(int x, int y, int c) {
		
		targetRenderImage.setRGB(x, y, c);
		
	}
	
	
	/**
	 * Returns the value of a pixel at a docSpace location as a packed int
	 * Direct access to the target image. Slow so use carefully
	 * @param docSpace
	 * @return pixel data as packed int
	 */
	public int getPixel(PVector docSpace) {
		PVector bufferSpace = coordinateSystem.docSpaceToBufferSpaceClamped(docSpace);
		return getPixel((int)bufferSpace.x,(int)bufferSpace.y);
	}
	
	/**
	 * Returns the value of a pixel at buffer space x,y as a packed int
	 * @param x
	 * @param y
	 * @return pixel data as packed int
	 */
	public int getPixel(int x, int y) {
		
		return targetRenderImage.getRGB(x, y);
		
	}
	

	/**
	 * Called by the ViewController class to get the current view rect from the whole
	 * @param docSpaceRect - the document space rect which is the portion of the whole document image currently in view
	 * @return the buffered image contained within docSpaceRect
	 */
	BufferedImage getCropDocSpace(Rect docSpaceRect) {
		PVector topLeft = coordinateSystem.docSpaceToBufferSpace(docSpaceRect.getTopLeft());
		PVector bottomRight = coordinateSystem.docSpaceToBufferSpace(docSpaceRect.getBottomRight());
		Rect bufferSpaceRect = new Rect(topLeft, bottomRight);
		return ImageProcessing.cropImage(targetRenderImage, bufferSpaceRect);
	}
	
	

	

	/**
	 * Saves the current render to file. called by the RenderSaver class, but is useful generally.
	 * @param pathAndFilename 
	 */
	public void saveRenderToFile(String pathAndFilename) {
		// the correct extension has already been added by the render saver
		if(saveRenderAtEndOfSession == false) {
			System.out.println("RenderTarget:saveRenderToFile  " + pathAndFilename + " save set to false ");
			return;
		}
		System.out.println("RenderTarget:saveRenderToFile  " + pathAndFilename);
		ImageProcessing.saveImage(pathAndFilename, targetRenderImage);
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Vector-style drawing operations
	// 
	
	
	
	
	
	/**
	 * Draws a circle directly to the BufferedImage render target. All units are in documentSpace, including lineThickness and circle radius  so, in this case, radius and lineThickness are fractions of the longest edge
	 * As this is not very human-friendly, use SceneHelper.millimeterToDocspace(mmInFinalPrintedImage) to help
	 * @param docPoint - the circles centre point
	 * @param radiusDocSpace
	 * @param fillColor
	 * @param lineColor
	 * @param lineThicknessDocSpace
	 */
	public void drawCircle(PVector docPoint, float radiusDocSpace, Color fillColor, Color lineColor,
			float lineThicknessDocSpace) {

		// work out if its succumbs to the permitted paste area
		PVector docPtMinusRad = new PVector(docPoint.x - radiusDocSpace, docPoint.y - radiusDocSpace);
		PVector docPtPlusRad = new PVector(docPoint.x + radiusDocSpace, docPoint.y + radiusDocSpace);
		Rect r = new Rect(docPtMinusRad, docPtPlusRad);
		

		PVector bufpt = coordinateSystem.docSpaceToBufferSpace(docPoint);

		float lineThicknessInPixels = lineThicknessDocSpace * coordinateSystem.getLongestBufferEdge();
		shapeDrawer.setDrawingStyle(fillColor, lineColor, lineThicknessInPixels);

		//shapeDrawer.setDrawingStyle(fillColor, Color.BLACK, 4);

		float radiusInPixels = radiusDocSpace * coordinateSystem.getLongestBufferEdge();

		//
		float left = bufpt.x - radiusInPixels;
		float top = bufpt.y - radiusInPixels;
		float w = radiusInPixels * 2;

		shapeDrawer.drawEllipse(left, top, w, w);

	}
	
	/**
	 * Draws a rectangle directly to the BufferedImage render target. All units are in BufferSpace, including lineThickness and circle radius  so, in this case, radius and lineThickness are fractions of the longest edge
	 * As this is not very human-friendly, use SceneHelper.millimeterToDocspace(mmInFinalPrintedImage) to help
	 * @param r
	 * @param fillColor
	 * @param lineColor
	 * @param lineThickness
	 */
	public void drawRectBufferSpace(Rect r, Color fillColor, Color lineColor, float lineThickness) {
		shapeDrawer.setDrawingStyle(fillColor, lineColor, lineThickness);
		shapeDrawer.drawRect(r);
	}
	
	
	
	
	
	
	/**
	 * @param r
	 * @param fillColor
	 * @param lineColor
	 * @param pixelWeight
	 */
	public void drawRect_DocSpace(Rect r, Color fillColor, Color lineColor, float pixelWeight) {
		shapeDrawer.setDrawingStyle(fillColor, lineColor, pixelWeight);
		Rect bufferRect = coordinateSystem.docSpaceToBufferSpace(r);
		shapeDrawer.drawRect(bufferRect);
	}

	
	/**
	 * @param docSpacePoints
	 * @param c
	 * @param pixelRadius
	 */
	public void drawPoints(ArrayList<PVector> docSpacePoints, Color c, float pixelRadius) {
		float r = pixelRadius; 
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		shapeDrawer.setDrawingStyle(ca, ca, 2);

		for (PVector p : docSpacePoints) {
			PVector bufpt = coordinateSystem.docSpaceToBufferSpace(p);
			shapeDrawer.drawEllipse(bufpt.x, bufpt.y, r, r);
		}

	}

	/**
	 * Using a Doc-space location, draws a point directly to the Rendertarget's BufferedImage. This is not session scaled.
	 * @param docSpacePoint - the doc Space centre of the point (circle)
	 * @param c - the colour including alpha if required
	 * @param pixelDiameter - the full diameter of the point (circle)
	 */
	public void drawPoint(PVector docSpacePoint, Color c, float pixelDiameter) {
		
		shapeDrawer.setDrawingStyle(c, c, 2);
		PVector bufpt = coordinateSystem.docSpaceToBufferSpace(docSpacePoint);
		float halfRadius = pixelDiameter/2;
		shapeDrawer.drawEllipse(bufpt.x-halfRadius, bufpt.y-halfRadius, pixelDiameter, pixelDiameter);

	}
	
	/**
	 * Using buffer-space locations defined in the Line2 l, draws a line directly to the Rendertarget's BufferedImage. This is not session scaled.
	 * @param l - the line defined as a Line2 type
	 * @param c - the colour including alpha if required
	 * @param w - the line thickness in pixels (must be > 0)
	 */
	void drawLine(Line2 l, Color c, float w) {
		//Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
		shapeDrawer.setDrawingStyle(c, c, w);
		PVector bufStart = coordinateSystem.docSpaceToBufferSpace(l.p1);
		PVector bufEnd = coordinateSystem.docSpaceToBufferSpace(l.p2);
		shapeDrawer.drawLine(bufStart.x, bufStart.y, bufEnd.x, bufEnd.y);
	}

	/**
	 * Using buffer-space locations defined by the line between start and end, draws a line directly to the Rendertarget's BufferedImage. This is not session scaled.
	 * @param start  - Pvector start pos of line in buffer space coordinated
	 * @param end - Pvector end pos of line in buffer space coordinated
	 * @param c - the colour including alpha if required
	 * @param w - the line thickness in pixels (must be > 0)
	 */
	public void drawLine(PVector start, PVector end, Color c, float w) {
		//Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
		shapeDrawer.setDrawingStyle(c, c, w);
		PVector bufStart = coordinateSystem.docSpaceToBufferSpace(start);
		PVector bufEnd = coordinateSystem.docSpaceToBufferSpace(end);
		shapeDrawer.drawLine(bufStart.x, bufStart.y, bufEnd.x, bufEnd.y);
	}
	
	
	/**
	 * Using buffer-space locations defined by the line between start and end, draws a line directly to the Rendertarget's BufferedImage. This is not session scaled.
	 * @param start  - Pvector start pos of line in buffer space coordinated
	 * @param end - Pvector end pos of line in buffer space coordinated
	 * @param c - the colour including alpha if required
	 * @param w - the line thickness in pixels (must be > 0)
	 * @param dashPattern - the array representing the dashing pattern. Java->awt->BasicStroke. Dash pattern styles in 100% render pixel dimensions, and are session scaled
	 */
	public void drawLine(PVector start, PVector end, Color c, float w, float[] dashPattern) {
		if(dashPattern == null) {
			//System.out.println("setting plain style");
			shapeDrawer.setDrawingStyle(MOImage.MOColor.invisibleCol(), c, w);
		}else {
			//System.out.println("setting dashed style");
			float[] scaledDashPattern = sessionScaleDashPattern(dashPattern);
			shapeDrawer.setDrawingStyle(MOImage.MOColor.invisibleCol(), c, w, scaledDashPattern);		
		}
		PVector bufStart = coordinateSystem.docSpaceToBufferSpace(start);
		PVector bufEnd = coordinateSystem.docSpaceToBufferSpace(end);
		shapeDrawer.drawLine(bufStart.x, bufStart.y, bufEnd.x, bufEnd.y);
	}
	
	
	/**
	 * Using Vertices2 object to define a set of connected doc-space  lines with no fill. Can be closed or open, draws a line directly to the Rendertarget's BufferedImage.  
	 * @param docSpaceVertices -  Vetritice2 object defining a set of connected lines.
	 * @param c - the colour including alpha if required
	 * @param w - the line thickness in pixels (must be > 0)
	 * @param dashPattern - the array representing the dashing pattern. See Java->awt->BasicStroke. Dash pattern styles in 100% render pixel dimensions, and are session scaled
	 */
	public void drawVertices2NoFill(Vertices2 docSpaceVertices, Color c, float w, float[] dashPattern) {
		// for the sake of ease of use, dash pattern styles are in 100% render pixel dimensions, so if the render is 
		// scaled-down, so should the dash styles
		
		
		// draws the lines only, has an option for dashes
		if(dashPattern == null) {
			//System.out.println("setting plain style");
			shapeDrawer.setDrawingStyle(MOImage.MOColor.invisibleCol(), c, w);
		}else {
			//System.out.println("setting dashed style");
			float[] scaledDashPattern = sessionScaleDashPattern(dashPattern);
			shapeDrawer.setStrokeCapJoin(BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
			shapeDrawer.setDrawingStyle(MOImage.MOColor.invisibleCol(), c, w, scaledDashPattern);		
			}
		

		Vertices2 vbuff = docSpaceVertices.getInBufferSpace(false);
		if(vbuff==null) return;
		
		shapeDrawer.drawVertices2(vbuff);
	}
	
	/**
	 * Using Vertices2 object to define a set of connected doc-space  lines with fill. Can be closed or open, draws a line directly to the Rendertarget's BufferedImage.   
	 * @param docSpaceVertices -  Vetritice2 object defining a set of connected lines.
	 * @param fillCol
	 * @param lineCol
	 * @param w - the line thickness in pixels (must be > 0)
	 */
	public void drawVertices2(Vertices2 docSpaceVertices, Color fillCol, Color lineCol, float w) {
		shapeDrawer.setDrawingStyle(fillCol,lineCol, w);
		Vertices2 vbuff = docSpaceVertices.getInBufferSpace(false);
		shapeDrawer.drawVertices2(vbuff);
	}
	
	
	
	
	/**
	 * Draws text in string str to the render target using a DocSpace point to define its location
	 * @param str - The string to be rendered
	 * @param docSpacePoint - the baseline of the first character is at this location
	 * @param size
	 * @param c
	 */
	void drawText(String str, PVector docSpacePoint, int size, Color c) {
		PVector bufpt = coordinateSystem.docSpaceToBufferSpace(docSpacePoint);
		drawText( str, (int)bufpt.x, (int)bufpt.y,  size,  c);
	}

	/**
	 * Draws text in string str to the render target using buffer space x y to define its location
	 * @param str - The string to be rendered
	 * @param bufferX - the baseline of the first character is at this x location
	 * @param bufferY - the baseline of the first character is at this y location
	 * @param size
	 * @param c
	 */
	public void drawText(String str, int bufferX, int bufferY, int size, Color c) {
		VectorShape textShape = new VectorShape();
		textShape.setTextShape(bufferX, bufferY, str, c, size);
		shapeDrawer.drawDrawnShape(textShape);
	}
	
	
	
	
	/**
	 * Used by Sprite and other classes to do drawing directly to the BufferdImage
	 * @return
	 */
	public VectorShapeDrawer getVectorShapeDrawer() {
		return shapeDrawer;
	}

	/**
	 * Used by other classes to do drawing directly to the BufferdImage
	 * @return
	 */
	public Graphics2D getGraphics2D() {
		return graphics2D;
	}

	
	
	/**
	 * Private method used to scale the dash pattern (defined in 100% pixels) to the session-scaled units
	 * @param dashPattern
	 * @return
	 */
	private float[] sessionScaleDashPattern(float[] dashPattern) {
		int num = dashPattern.length;
		float[] scaled = new float[num];
		float sessionScale = GlobalSettings.getSessionScale();
		for(int n = 0; n < num; n++) {
			float sf = dashPattern[n] * sessionScale;
			scaled[n] = sf;
		}
		return scaled;
	}

	
	/**
	 * Looks like a debug method. Works in DocSpace
	 * @param docSpaceVertices
	 * @param c
	 * @param w
	 */
	public void drawVerticesWithPoints(Vertices2 docSpaceVertices, Color c, float w) {
		int numLines = docSpaceVertices.getNumLines();
		for(int n = 0; n < numLines; n++) {
			Line2 l = docSpaceVertices.getLine(n);
			drawLine( l,  c,  w);
			drawPoint(l.p1, c, w+4);
			drawPoint(l.p2, c, w+4);
		}
		
	}
	

	

}