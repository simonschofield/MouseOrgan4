
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

class PermittedPasteArea {
	// The rect is set using Normalized coordinates (0..1 in both x and y), but stored using docSpace coordinates
	// If the PermittedPasteArea is active
	//	- then some cropping will occur. 
	// 	- if sprites are completely outside they will be EXCLUDED
	//  - if they are completely within they will be pasted as normal
	//  - if the fall on a boundary, then a decision is made as to the action
	//  	-- if the boundary is comprised of just one edge then that edge action takes place
	//      -- if the boundary has two edges, then EXCLUDED takes precedence over CROP or BESPOKE_CROP
	// the actions that can be done on an edge are
	
	// EXCLUDE : do not paste any sprite overlapping an edge with this setting
	// CROP : Crop any sprite to the hard edge, also default action
	// BESPOKE_CROP : Apply a bespoke crop to a sprite overlapping this edge
	boolean isActive = false;
	
	Rect permittedPasteAreaRect;
	
	String leftEdgeAction = "CROP";
	String topEdgeAction = "CROP";
	String rightEdgeAction = "CROP";
	String bottomEdgeAction = "CROP";
	
	ImageContentGroup permittedPasteAreaCropImages;
	RenderTarget theRenderTarget;
	
	
	PermittedPasteArea(RenderTarget rt){
		theRenderTarget = rt;
		setPermittedPasteRectWithNomalizedCoords( 0,  0,  1,  1);
	}
	
	void set(float left, float top, float right, float bottom, String edgeAction,  ImageContentGroup bespokeCropImage) {
		set( left,  top,  right,  bottom, edgeAction,edgeAction,edgeAction,edgeAction,  bespokeCropImage);
	}
	
	void setActive(boolean active) {
		isActive = active;
	}
	
	boolean isActive() {
		return isActive;
	}
	
	void set(float left, float top, float right, float bottom, String leftAction, String topAction, String rightAction, String bottomAction, ImageContentGroup bespokeCropImage){
		setPermittedPasteRectWithNomalizedCoords( left,  top,  right,  bottom);
		leftEdgeAction = leftAction;
		topEdgeAction = topAction;
		rightEdgeAction = rightAction;
		bottomEdgeAction = bottomAction;
		
		permittedPasteAreaCropImages = bespokeCropImage;
		isActive = true;
	}
	
	private void setPermittedPasteRectWithNomalizedCoords(float left, float top, float right, float bottom) {
		PVector topLeftNormSpace = new PVector(left,top);
		PVector bottomRightNormSpace = new PVector(right,bottom);
		PVector topLeft = theRenderTarget.coordinateSpaceCoverter.normalizedSpaceToDocSpace(topLeftNormSpace);
		PVector bottomRight = theRenderTarget.coordinateSpaceCoverter.normalizedSpaceToDocSpace(bottomRightNormSpace);
		
		permittedPasteAreaRect = new Rect(topLeft, bottomRight);
	}
	
	Rect getRect() {
		return permittedPasteAreaRect.copy();
	}
	
	boolean isPointInside(PVector docSpacePt) {
		
		return permittedPasteAreaRect.isPointInside(docSpacePt);
	}
	
	boolean isFullyPermitted(Rect r) {
		// simple interface for drawn shapes based on their rectangle, either in or out!
		if(isActive == false) return true;
		return r.isWhollyInsideOther(permittedPasteAreaRect);
	}
	
	String cropEdgeDecision(String overlapReport) {
		// given an intersection edge report from the Sprite
		// decide what edge crop action to do.
		
		// if totally outside the ppa
		if(overlapReport.contentEquals("NONE")) return "EXCLUDE";
		
		String action = getActionByEdgeString(overlapReport);
		if(action.contentEquals("MULTIPLE_EDGES")) {
			// if you get this far then the overlapReport must contains two or more edges, and we have to make a decision on this
			return getDominantAction(overlapReport);
		}
		// ..then the overlapReport must contain only one edge, so return the edgeAction
		return action;
		
		//System.out.println("cropEdgeDecision on intersection: " + overlapReport);
		
		
	}
	
	String getDominantAction(String overlapReport) {
		// only allows for first two string components
		String[] edgeStrings = overlapReport.split(",");
		if( edgeStrings.length < 2) {
			System.out.println("PermittedPasteArea getDominantAction: problem with number of edges reported - num = " + edgeStrings.length);
			return "EXCLUDE";
		}
		
		String edgeString1 = edgeStrings[0];
		String edgeString2 = edgeStrings[1];
		String action1 = getActionByEdgeString(edgeString1);
		String action2 = getActionByEdgeString(edgeString2);
		if( action1.contentEquals(action2)) return action1;
		
		if(action1.contentEquals("EXCLUDE") || action2.contentEquals("EXCLUDE")) return "EXCLUDE";
		if(action1.contentEquals("BESPOKE_CROP") || action2.contentEquals("BESPOKE_CROP")) return "BESPOKE_CROP";
		// should cause a crash, but should never get here
		System.out.println("PermittedPasteArea getDominantAction: problem with decided action between " + action1 +" and "+ action2);
		return null;
	}
	
	String getActionByEdgeString(String edgeString) {
		
		if(edgeString.contentEquals("TOP")) return topEdgeAction;
		if(edgeString.contentEquals("LEFT")) return leftEdgeAction;
		if(edgeString.contentEquals("RIGHT")) return rightEdgeAction;
		if(edgeString.contentEquals("BOTTOM")) return bottomEdgeAction;
		return "MULTIPLE_EDGES";
	}
	
	String reportPermittedPasteAreaOverlap(ImageSprite sprite) {
		Rect r = sprite.getPasteRectDocSpace(theRenderTarget);
		return r.reportIntersection(permittedPasteAreaRect);

	}
	

	boolean isSpriteWhollyInside(ImageSprite sprite) {
		Rect r = sprite.getPasteRectDocSpace(theRenderTarget); 
		return r.isWhollyInsideOther( this.permittedPasteAreaRect );
		
	}
	
	/*
	String reportPermittedPasteAreaOverlap(DrawnShape shape) {
		Rect r = shape.getPasteRectDocSpace(theRenderTarget);
		return r.reportIntersection(permittedPasteAreaRect);

	}
	*/
	
}

class RenderTarget {

	Graphics2D graphics2D;
	private BufferedImage bufferedImage;

	

	// these are the pixel dimensions of the output image
	int bufferWidth, bufferHeight;
	float longestBufferEdge;
	
	// these are the parametrised (0..1) width and height
	// of the document. The longest edge is set to 1.0
	float documentWidth, documentHeight;

	ShapeDrawer shapeDrawer;

	PermittedPasteArea permittedPasteArea;
	
	CoordinateSpaceConverter coordinateSpaceCoverter;
	
	
	
	public RenderTarget() {

	}

	void setRenderBufferSize(int w, int h) { 

		bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		
		bufferWidth = w;
		bufferHeight = h;
		graphics2D = bufferedImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		longestBufferEdge = Math.max(bufferWidth, bufferHeight);
		
		documentWidth = bufferWidth / longestBufferEdge;
		documentHeight = bufferHeight / longestBufferEdge;
		
		
		shapeDrawer = new ShapeDrawer(graphics2D);
		
		coordinateSpaceCoverter = new CoordinateSpaceConverter(w, h, getDocumentAspect());
		permittedPasteArea = new PermittedPasteArea(this);
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
	// 
	// All arguments are in Normalised space, as working in DocSpace for humans is difficult.
	//
	
	// old legacy way of setting margins
	void setPermittedPasteArea(float left, float top, float right, float bottom, boolean applyCrop, ImageContentGroup cropImages) {
		// NO_CROP : Don't do anything, just allow the image to be pasted, also default action
		// EXCLUDE_OVERLAPPING : do not paste any sprite overlapping an edge with this setting
		// CROP : Crop any sprite to the hard edge
		// BESPOKE_CROP : Apply a bespoke crop to a sprite overlapping this edge
		String edgeCropAction = "EXCLUDE";
		if(applyCrop && cropImages==null) {
			edgeCropAction = "CROP";
		}
		if(applyCrop && cropImages!=null) {
			edgeCropAction = "BESPOKE_CROP";
		}
		permittedPasteArea.set(left, top, right, bottom, edgeCropAction, cropImages);
	}
	
	
	void setPermittedPasteArea(float left, float top, float right, float bottom, String leftAct, String topAct, String rightAct, String bottomAct, ImageContentGroup cropImages) {
		// NO_CROP : Don't do anything, just allow the image to be pasted, also default action
		// EXCLUDE_OVERLAPPING : do not paste any sprite overlapping an edge with this setting
		// CROP : Crop any sprite to the hard edge
		// BESPOKE_CROP : Apply a bespoke crop to a sprite overlapping this edge
		
		permittedPasteArea.set(left, top, right, bottom, leftAct,  topAct,  rightAct,  bottomAct, cropImages);
	}
	
	void setPermittedPasteArea(boolean active) {
		
		permittedPasteArea.setActive(active);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// pastes the topleft of the image at docSpacePoint
	//
	void pasteSprite(ImageSprite sprite, float alpha) {
		// work out the offset in the image from the origin
		Rect r = sprite.getPasteRectDocSpace(this); 
		
		//System.out.println("pasteSprite pasteRectDocSpace = " + r.toStr());
		
		String overlapReport =  permittedPasteArea.reportPermittedPasteAreaOverlap(sprite);
		//System.out.println("overlap report " + overlapReport);
		
		if( overlapReport.contentEquals("WHOLLYINSIDE") || permittedPasteArea.isActive()==false){
			//System.out.println("here 1");
			pasteImage(sprite.image, r.getTopLeft(),  alpha);
			return;
		}
		
		//System.out.println("here 2");
		// otherwise do some sort of crop
		// decide what to do
		// the options are
		// 1/ if EXCLUDE_OVERLAPPING don't allow any paste, so return
		// 2/ crop to the permitted area with bespoke crop
		// 3/ a hard geometric crop to the area
		//if( bespokeCropToPermittedPasteArea == false ) return;
		
		
		String cropDecision = permittedPasteArea.cropEdgeDecision(overlapReport);
		//System.out.println("cropDecision " + cropDecision);
		if( cropDecision.contentEquals("EXCLUDE") ) return;
		//System.out.println("here 3");
		//if( permittedPasteAreaClass.leftEdgeAction.contentEquals("EXCLUDE") ) return;
		// if you get this far, 
		boolean cropOK = sprite.cropToPermittedPasteArea(this);
		if(cropOK==false) return;
		//System.out.println("here 4");
			
		
		//System.out.println(" pasted ");
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
		return getCropBufferSpace( bufferSpaceRect);
		//int cropX = (int) (topLeft.x);
		//int cropY = (int) (topLeft.y);
		//int cropWidth = (int) (bottomRight.x - topLeft.x);
		//int cropHeight = (int) (bottomRight.y - topLeft.y);
		//return bufferedImage.getSubimage(cropX, cropY, cropWidth, cropHeight);

	}
	
	BufferedImage getCropBufferSpace(Rect bufferSpaceRect) {
		PVector topLeft = bufferSpaceRect.getTopLeft();
		PVector bottomRight = bufferSpaceRect.getBottomRight();
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
	
		float bx = docPt.x * longestBufferEdge;
		float by = docPt.y * longestBufferEdge;
		return new PVector(bx, by);
	}

	
	PVector bufferSpaceToDocSpace(PVector p) {
		return bufferSpaceToDocSpace((int) p.x, (int) p.y);
	}

	PVector bufferSpaceToDocSpace(int bx, int by) {
		
		float docX = bx/longestBufferEdge;
		float docY = by/longestBufferEdge;
		return new PVector(docX, docY);
	}
	
	
	
	
	//////////////////////////////////////////////////////////////////////
	// scaled drawing operations
	// All units are in documentSpace, including lineThickness
	// so, in this case, radius and lineThickness are fractions of the longest edge.
	
	void drawCircle(PVector docPoint, float radiusDocSpace, Color fillColor, Color lineColor, float lineThicknessDocSpace) {
		
		// work out if its succumbs to the permitted paste area
		Rect r = new Rect(docPoint.x-radiusDocSpace,docPoint.y-radiusDocSpace, docPoint.x+radiusDocSpace,docPoint.y+radiusDocSpace);
		if( permittedPasteArea.isFullyPermitted(r) == false ) {
			//System.out.println("not permitted");
			return;
		}

		PVector bufpt = docSpaceToBufferSpace(docPoint);

		int lineThicknessInPixels = (int) (lineThicknessDocSpace * getLongestBufferEdge());
		shapeDrawer.setDrawingStyle(fillColor, lineColor, lineThicknessInPixels);

		//shapeDrawer.setDrawingStyle(fillColor, Color.BLACK, 4);
		
		
	    float radiusInPixels = radiusDocSpace * getLongestBufferEdge();
	    
	    
	    //
	    float left = bufpt.x - radiusInPixels;
	    float top = bufpt.y - radiusInPixels;
	    float w = radiusInPixels*2;
	    
	    
	    shapeDrawer.drawEllipse(left,top,w,w);

	}
	

	//////////////////////////////////////////
	// debug drawing operations other than paste
	// these do not scale the drawing to the document
	void drawPoints(ArrayList<PVector> docSpacePoints, Color c) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 127);
		shapeDrawer.setDrawingStyle(ca, ca, 2);

		for (PVector p : docSpacePoints) {
			PVector bufpt = docSpaceToBufferSpace(p);
			shapeDrawer.drawEllipse(bufpt.x, bufpt.y, 6, 6);
		}

	}
	
	
	void drawPoint(PVector docSpacePoint, Color c, int size) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 127);
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
