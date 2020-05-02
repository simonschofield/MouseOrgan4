
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
	
	String cropEdgeDecision(String overlapReport) {
		// give an intersection edge report from the Sprite
		// decide what edge crop action to do.
		
		if(overlapReport.contentEquals("NONE")) return "EXCLUDE";
		
		String action = getActionByEdgeString(overlapReport);
		if(action.contentEquals("")==false) return action;
		// if you get this far then the overlapReport must contains two or more edges, and we have to make a decision on this
		//System.out.println("cropEdgeDecision on intersection: " + overlapReport);
		return getDominantAction(overlapReport);
		
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
		return "";
	}
	
	String reportPermittedPasteAreaOverlap(ImageSprite sprite) {
		Rect r = sprite.getPasteRectDocSpace(theRenderTarget);
		return r.reportIntersection(permittedPasteAreaRect);

	}
	
	boolean isSpriteWhollyInside(ImageSprite sprite) {
		Rect r = sprite.getPasteRectDocSpace(theRenderTarget); 
		return r.isWhollyInsideOther( this.permittedPasteAreaRect );
		
	}
	
	
}

class RenderTarget {

	Graphics2D graphics2D;
	BufferedImage bufferedImage;

	

	// these are the pixel dimensions of the output image
	int bufferWidth, bufferHeight;

	// these are the normalised width and height
	// of the document. The longest edge is set to 1.0
	float documentWidth, documentHeight;

	ShapeDrawer shapeDrawer;

	PermittedPasteArea permittedPasteAreaClass;
	
	CoordinateSpaceConverter coordinateSpaceCoverter;
	
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


		shapeDrawer = new ShapeDrawer(graphics2D);
		
		coordinateSpaceCoverter = new CoordinateSpaceConverter(w, h, getDocumentAspect());
		permittedPasteAreaClass = new PermittedPasteArea(this);
	}

	public BufferedImage getImage() {
		return bufferedImage;
	}

	void fillBackground(Color c) {
		graphics2D.setBackground(c);
		graphics2D.clearRect(0, 0, bufferWidth, bufferHeight);
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
		permittedPasteAreaClass.set(left, top, right, bottom, edgeCropAction, cropImages);
	}
	
	
	void setPermittedPasteArea(float left, float top, float right, float bottom, String leftAct, String topAct, String rightAct, String bottomAct, ImageContentGroup cropImages) {
		// NO_CROP : Don't do anything, just allow the image to be pasted, also default action
		// EXCLUDE_OVERLAPPING : do not paste any sprite overlapping an edge with this setting
		// CROP : Crop any sprite to the hard edge
		// BESPOKE_CROP : Apply a bespoke crop to a sprite overlapping this edge
		
		permittedPasteAreaClass.set(left, top, right, bottom, leftAct,  topAct,  rightAct,  bottomAct, cropImages);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// pastes the topleft of the image at docSpacePoint
	//
	void pasteSprite(ImageSprite sprite, float alpha) {
		// work out the offset in the image from the origin
		Rect r = sprite.getPasteRectDocSpace(this); 
		String overlapReport =  permittedPasteAreaClass.reportPermittedPasteAreaOverlap(sprite);
		
		
		if( overlapReport.contentEquals("WHOLLYINSIDE") || permittedPasteAreaClass.isActive()==false){
			pasteImage(sprite.image, r.getTopLeft(),  alpha);
			return;
		}
		
		
		// otherwise do some sort of crop
		// decide what to do
		// the options are
		// 1/ if EXCLUDE_OVERLAPPING don't allow any paste, so return
		// 2/ crop to the permitted area with bespoke crop
		// 3/ a hard geometric crop to the area
		//if( bespokeCropToPermittedPasteArea == false ) return;
		
		
		String cropDecision = permittedPasteAreaClass.cropEdgeDecision(overlapReport);
		if( cropDecision.contentEquals("EXCLUDE") ) return;
		//if( permittedPasteAreaClass.leftEdgeAction.contentEquals("EXCLUDE") ) return;
		// if you get this far, 
		boolean cropOK = sprite.cropToPermittedPasteArea(this);
		if(cropOK==false) return;
			
		
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
