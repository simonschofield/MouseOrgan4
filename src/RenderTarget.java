
////////////////////////////////////////////////////////////////////////////////
// Render target class
// The documentROI is in real document space coordinates
// This is a Java Graphics2D implementation of the render target to
// hopefully open up all of Java's graphics functionality

import java.awt.*;
//import java.awt.event.*;
//import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

class RenderTarget {

	Graphics2D graphics2D;
	BufferedImage bufferedImage;

	

	// these are the pixel dimensions of the output image
	int bufferWidth, bufferHeight;

	// these are the normalised width and height
	// of the document. The longest edge is set to 1.0
	float documentWidth, documentHeight;

	ShapeDrawer shapeDrawer;

	
	///
	private Rect permittedPasteArea;
	boolean bespokeCropToPermittedPasteArea = false;
	ImageContentGroup permittedPasteAreaCropImages;
	
	
	public RenderTarget() {

	}

	void setRenderBufferSize(int w, int h) { 

		bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		
		bufferWidth = w;
		bufferHeight = h;
		graphics2D = bufferedImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (bufferWidth > bufferHeight) {
			documentWidth = 1.0f;
			documentHeight = (float) bufferHeight / (float) bufferWidth;
		} else {
			documentHeight = 1.0f;
			documentWidth = (float) bufferWidth / (float) bufferHeight;
		}

		permittedPasteArea = new Rect(0, 0, documentWidth, documentWidth);

		shapeDrawer = new ShapeDrawer(graphics2D);
	}

	public BufferedImage getImage() {
		return bufferedImage;
	}

	void fillBackground(Color c) {

		// shapeDrawer.cacheCurrentDrawingStyle();
		shapeDrawer.setFillColor(c);
		shapeDrawer.drawRect(0, 0, bufferWidth, bufferHeight);
		// shapeDrawer.restoreCachedDrawingStyle();
	}

	float getDocumentAspect() {
		return documentWidth / documentHeight;
	}

	int getBufferWidth() {
		return bufferWidth;
	}

	int getBufferHeight() {
		return bufferHeight;
	}

	float getDocumentWidth() {
		return documentWidth;
	}

	float getDocumentHeight() {
		return documentHeight;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// sets each boundary of the permitted paste area. each field is nullable, in that is is not changed
	// All measurements are in document space
	// if applyCrop == false, then do not permit any pasting which overlaps the permittedPasteArea, ignore the cropImages is any
	// if applyCrop == true, then crop the sprite to the permittedPasteArea
	// if cropImages is not set (==null) and applyCrop == true, the  crop is a simple hard rectangular crop to the permittedPasteArea
	// if cropImages is set and applyCrop == true, the  crop is made by adding the crop effect to the hard rectangular crop to the permittedPasteArea
	void setPermittedPasteArea(Float left, Float top, Float right, Float bottom, boolean applyCrop, ImageContentGroup cropImages) {
		if(left != null ) permittedPasteArea.left = left;
		if(top != null ) permittedPasteArea.top = top;
		if(right != null ) permittedPasteArea.right = right;
		if(bottom != null ) permittedPasteArea.bottom = bottom;
		bespokeCropToPermittedPasteArea = applyCrop;
		permittedPasteAreaCropImages = cropImages;
	}
	
	Rect getPermittedPasteArea() {
		return permittedPasteArea;
	}
	
	String reportPermittedPasteAreaOverlap(ImageSprite sprite) {
		Rect r = sprite.getPasteRectDocSpace(this);
		return r.reportIntersection(permittedPasteArea);

	}
	
	String reportDocumentBoundaryPasteAreaOverlap(ImageSprite sprite) {
		Rect standardDocumentBoundary = new Rect(0,0,documentWidth, documentHeight);
		Rect r = sprite.getPasteRectDocSpace(this);
		return r.reportIntersection(standardDocumentBoundary);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// pastes the topleft of the image at docSpacePoint
	//
	void pasteSprite(ImageSprite sprite, float alpha) {
		// work out the offset in the image from the origin
		Rect r = sprite.getPasteRectDocSpace(this); 
		
		
		if (r.isWhollyInsideOther( permittedPasteArea ) == false)
			{
			// decide what to do
			// the options are
			// 1/ just return (don't allow any paste)
			// 2/ crop to the permitted area with bespoke crop
			// 3/ a hard geometric crop to the area
			if( bespokeCropToPermittedPasteArea == false ) return;
			// if you get this far, 
			boolean cropOK = sprite.cropToPermittedPasteArea(this);
			if(cropOK==false) return;
			}
		
		
		
		pasteImage(sprite.image, r.getTopLeft(),  alpha);
	}
	

	////////////////////////////////////////////////////////////////////////////////////
	// pastes the topleft of the image at docSpacePoint
	// 
	void pasteImage(BufferedImage img, PVector docSpacePoint, float alpha) {
		
		Rect r = getPasteRectDocSpace(img, docSpacePoint);
		PVector bufferPt = docSpaceToBufferSpace(docSpacePoint);
		pasteImage_BufferCoordinates(img, (int) bufferPt.x, (int) bufferPt.y, alpha);
	}
	
	private void pasteImage_BufferCoordinates(BufferedImage img, int x, int y, float alpha) {

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		graphics2D.drawImage(img, x, y, null);
		graphics2D.setComposite(ac);

	}
	
	
	Rect getPasteRectDocSpace(BufferedImage img, PVector docSpacePoint) {
		// Final pasting always by defining the top left of the image in doc space
		// give an image and its upperleft at docSpacePoint
		// return the doc space rect it occupies
		PVector bottomRightOffset = bufferSpaceToDocSpace(img.getWidth(), img.getHeight());
		PVector bottomRight = PVector.add(docSpacePoint, bottomRightOffset);
		return new Rect(docSpacePoint.x, docSpacePoint.y, bottomRight.x, bottomRight.y);
	}

	

	////////////////////////////////////////////////////////////////////////////////////
	// this is called by the Surface to get the current view rect from the whole
	//////////////////////////////////////////////////////////////////////////////////// bufferedImage
	//
	BufferedImage getCropDocSpace(Rect docSpaceRect) {
		PVector topLeft = docSpaceToBufferSpace(docSpaceRect.getTopLeft());
		PVector bottomRight = docSpaceToBufferSpace(docSpaceRect.getBottomRight());
		int cropX = (int) (topLeft.x);
		int cropY = (int) (topLeft.y);
		int cropWidth = (int) (bottomRight.x - topLeft.x);
		int cropHeight = (int) (bottomRight.y - topLeft.y);
		return bufferedImage.getSubimage(cropX, cropY, cropWidth, cropHeight);

	}

	void saveRenderToFile(String pathAndFilename) {
		try {
			// retrieve image
			File outputfile = new File(pathAndFilename);
			ImageIO.write(bufferedImage, "png", outputfile);
		} catch (IOException e) {
			// do nothing
		}

	}

	////////////////////////////////////////////////////////////////////////////
	// coordinate space transformations
	//
	//
	PVector docSpaceToBufferSpace(PVector docPt) {
		float bx = 0;
		float by = 0;
		if (getDocumentAspect() > 1) {
			bx = docPt.x * bufferWidth;
			by = docPt.y * bufferWidth;
		} else {
			bx = docPt.x * bufferHeight;
			by = docPt.y * bufferHeight;
		}
		return new PVector(bx, by);
	}

	
	PVector bufferSpaceToDocSpace(PVector p) {
		return bufferSpaceToDocSpace((int) p.x, (int) p.y);
	}

	PVector bufferSpaceToDocSpace(int bx, int by) {
		// calculate the document space position of the pixel within the buffer
		float docX = 0;
		float docY = 0;
		if (getDocumentAspect() > 1) {
			// landscape
			docX = (float) bx / (float) bufferWidth;
			docY = (float) by / (float) bufferWidth;
		} else {
			// portrait or square
			docY = (float) by / (float) bufferHeight;
			docX = (float) bx / (float) bufferHeight;
		}

		return new PVector(docX, docY);
	}

	

	///////////////////////
	// for debugging
	void drawPoints(ArrayList<PVector> docSpacePoints, Color c) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 127);
		shapeDrawer.setDrawingStyle(ca, ca, 2);

		for (PVector p : docSpacePoints) {
			PVector bufpt = docSpaceToBufferSpace(p);
			shapeDrawer.drawEllipse(bufpt.x, bufpt.y, 4, 4);
		}

	}

}
