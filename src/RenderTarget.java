
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

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Generic RenderTarget
//
//
//
//


class RenderTarget{
	protected Graphics2D graphics2D;
	protected BufferedImage bufferedImage;
	
	// these are the pixel dimensions of the output image
	int bufferWidth, bufferHeight;
	float longestBufferEdge;
	
	// these are the parametrised (0..1) width and height
	// of the document. The longest edge is set to 1.0
	float documentWidth, documentHeight;

	ShapeDrawer shapeDrawer;
	
	
	public RenderTarget() {

	}
	
	public RenderTarget(int w, int h, int imgType) {
		setRenderBuffer( w,  h,  imgType);
	}
	
	void setRenderBuffer(int w, int h, int imgType) {
		
		// BufferedImage.TYPE_INT_ARGB
		bufferedImage = new BufferedImage(w, h, imgType);
		
		bufferWidth = w;
		bufferHeight = h;
		graphics2D = bufferedImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		longestBufferEdge = Math.max(bufferWidth, bufferHeight);
		
		documentWidth = bufferWidth / longestBufferEdge;
		documentHeight = bufferHeight / longestBufferEdge;
		
		
		shapeDrawer = new ShapeDrawer(graphics2D);
		
	}
	
	
	public BufferedImage getImage() {
		return bufferedImage;
	}
	
	public BufferedImage copyImage() {
		return ImageProcessing.copyImage(bufferedImage);
	}
	
	
	void clearImage() {
		Color blank = new Color(0,0,0,0);
		fillBackground(blank); 
	}

	void fillBackground(Color c) {
		graphics2D.setBackground(c);
		graphics2D.clearRect(0, 0, bufferWidth, bufferHeight);
	}

	
	// these are in image-buffer space
	int getBufferWidth() {
		return bufferWidth;
	}

	int getBufferHeight() {
		return bufferHeight;
	}
	
	int getLongestBufferEdge() {
		return Math.max(bufferWidth, bufferHeight);
	}

	// these are in document space
	float getDocumentWidth() {
		return documentWidth;
	}

	float getDocumentHeight() {
		return documentHeight;
	}
	
	float getDocumentAspect() {
		return documentWidth / documentHeight;
	}
	
	boolean isInsideDocumentSpace(PVector p) {
        
        if(isInsideXDocumentSpace(p.x) && isInsideYDocumentSpace(p.y)) return true;
        return false;
    }
    
    boolean isInsideXDocumentSpace(float x) {
    	float w = getDocumentWidth();
    	if(x >= 0 && x <= w) return true;
    	return false;
    }
    
    boolean isInsideYDocumentSpace(float y) {
    	float h = getDocumentHeight();
    	if(y >= 0 && y <= h) return true;
    	return false;
    	
    }
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// pastes the topleft of the image at docSpacePoint
	// 
	void pasteImage(BufferedImage img, PVector docSpacePoint, float alpha) {

		Rect r = getPasteRectDocSpace(img, docSpacePoint);
		PVector bufferPt = docSpaceToBufferSpace(docSpacePoint);
		pasteImage_BufferCoordinates(img, (int) bufferPt.x, (int) bufferPt.y, alpha);

	// this is where we need to add the mask images if any
	
	// documentMaskImages.pasteImage_BufferCoordinates(img, (int) bufferPt.x, (int) bufferPt.y, alpha);
	// where the sprite contains information about the mask image it is

	}

	////////////////////////////////////////////////////////////////////////////////////
	// This is the method that actually does the compositing of the sprite with the
	// document image
	public void pasteImage_BufferCoordinates(BufferedImage img, int x, int y, float alpha) {
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		graphics2D.setComposite(ac);
		graphics2D.drawImage(img, x, y, null);
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
		Rect bufferSpaceRect = new Rect(topLeft, bottomRight);
		return getCropBufferSpace(bufferSpaceRect);
	}

	BufferedImage getCropBufferSpace(Rect bufferSpaceRect) {
		return ImageProcessing.cropImage(bufferedImage, bufferSpaceRect);
	}

	void saveRenderToFile(String pathAndFilename) {
		System.out.println("RenderTarget:saveRenderToFile  " + pathAndFilename);
		// check to see if extension exists
		String ext = MOUtils.getFileExtension(pathAndFilename);
		String extensionChecked = pathAndFilename;
		if (ext.contentEquals("")) {
			extensionChecked = pathAndFilename + ".png";
		}

		// check to see if extension is correct
		ext = MOUtils.getFileExtension(extensionChecked);
		if (ext.contentEquals(".png") || ext.contentEquals(".PNG")) {
			// OK
		} else {
			System.out.println("RenderTarget:saveRenderToFile file extesion is wrong - " + ext);
			return;
		}

		try {
			// retrieve image
			File outputfile = new File(extensionChecked);
			ImageIO.write(bufferedImage, "png", outputfile);
		} catch (IOException e) {
			System.out.println("RenderTarget:saveRenderToFile could not save file - " + extensionChecked + " " + e);
		}

	}

	////////////////////////////////////////////////////////////////////////////
	// coordinate space transformations
	//
	//
	PVector docSpaceToBufferSpace(PVector docPt) {

		float bx = docPt.x * longestBufferEdge;
		float by = docPt.y * longestBufferEdge;
		return new PVector(bx, by);
	}

	PVector bufferSpaceToDocSpace(PVector p) {
		return bufferSpaceToDocSpace((int) (p.x), (int) (p.y));
	}

	PVector bufferSpaceToDocSpace(int bx, int by) {

		float docX = bx / longestBufferEdge;
		float docY = by / longestBufferEdge;
		return new PVector(docX, docY);
	}

	PVector docSpaceToNormalisedSpace(PVector docPt) {

		PVector buffPt = docSpaceToBufferSpace(docPt);
		return new PVector(buffPt.x / bufferWidth, buffPt.y / bufferHeight);

	}

	PVector normalisedSpaceToDocSpace(PVector normPt) {
		// Doesn't lose precision by avoiding bufferspace methods
		float bx = normPt.x * bufferWidth;
		float by = normPt.y * bufferHeight;

		float docX = bx / longestBufferEdge;
		float docY = by / longestBufferEdge;
		return new PVector(docX, docY);

	}
	
	
	//////////////////////////////////////////////////////////////////////
	// scaled drawing operations
	// All units are in documentSpace, including lineThickness
	// so, in this case, radius and lineThickness are fractions of the longest edge.

	void drawCircle(PVector docPoint, float radiusDocSpace, Color fillColor, Color lineColor,
			float lineThicknessDocSpace) {

		// work out if its succumbs to the permitted paste area
		Rect r = new Rect(docPoint.x - radiusDocSpace, docPoint.y - radiusDocSpace, docPoint.x + radiusDocSpace,
				docPoint.y + radiusDocSpace);
		

		PVector bufpt = docSpaceToBufferSpace(docPoint);

		int lineThicknessInPixels = (int) (lineThicknessDocSpace * getLongestBufferEdge());
		shapeDrawer.setDrawingStyle(fillColor, lineColor, lineThicknessInPixels);

		//shapeDrawer.setDrawingStyle(fillColor, Color.BLACK, 4);

		float radiusInPixels = radiusDocSpace * getLongestBufferEdge();

		//
		float left = bufpt.x - radiusInPixels;
		float top = bufpt.y - radiusInPixels;
		float w = radiusInPixels * 2;

		shapeDrawer.drawEllipse(left, top, w, w);

	}

	//////////////////////////////////////////
	// debug drawing operations other than paste
	// these do not scale the drawing to the document
	void drawPoints(ArrayList<PVector> docSpacePoints, Color c) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		shapeDrawer.setDrawingStyle(ca, ca, 2);

		for (PVector p : docSpacePoints) {
			PVector bufpt = docSpaceToBufferSpace(p);
			shapeDrawer.drawEllipse(bufpt.x, bufpt.y, 6, 6);
		}

	}

	void drawPoint(PVector docSpacePoint, Color c, int size) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		shapeDrawer.setDrawingStyle(ca, ca, 2);
		PVector bufpt = docSpaceToBufferSpace(docSpacePoint);
		shapeDrawer.drawEllipse(bufpt.x, bufpt.y, size, size);

	}

	void drawLine(PVector start, PVector end, Color c, int thickness) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
		shapeDrawer.setDrawingStyle(ca, ca, thickness);
		PVector bufStart = docSpaceToBufferSpace(start);
		PVector bufEnd = docSpaceToBufferSpace(end);
		shapeDrawer.drawLine(bufStart.x, bufStart.y, bufEnd.x, bufEnd.y);
	}

	void drawText(String str, int bufferX, int bufferY, int size, Color c) {
		DrawnShape textShape = new DrawnShape();
		textShape.setTextShape(bufferX, bufferY, str, c, size);
		shapeDrawer.drawDrawnShape(textShape);
	}

}