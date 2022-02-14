package MOCompositing;

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

import MOImage.ImageProcessing;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.ImageCoordinateSystem;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;
import MOVectorGraphics.VectorShape;
import MOVectorGraphics.VectorShapeDrawer;



//////////////////////////////////////////////////////////////////////////////////////////////////////////////
// BufferdImage RenderTarget
// It has also been set up to be a MainDocumentRenderTarget
//


public class RenderTarget implements MainDocumentRenderTarget{
	private Graphics2D graphics2D;
	protected BufferedImage targetRenderImage;
	
	
	public ImageCoordinateSystem coordinateSystem;
	

	VectorShapeDrawer shapeDrawer;
	
	String renderTargetName = "";
	
	public RenderTarget() {

	}
	
	public RenderTarget(int w, int h, int imgType) {
		setRenderBuffer( w,  h,  imgType);
	}
	
	
	public void setName(String name) {
		renderTargetName = name;
	}
	
	public String getName() {
		return renderTargetName;
	}
	
	public String getFullSessionName() {
		String sessname = GlobalSettings.mainSessionName + "_" + GlobalSettings.currentSchemea + "_" +  renderTargetName;
		return sessname;
	}
	
	public String getFileExtension() {
		return ".png";
	}
	
	protected void setRenderBuffer(int w, int h, int imgType) {
		
		
		coordinateSystem = new ImageCoordinateSystem(w,h);
		
		
		
		
		targetRenderImage = new BufferedImage(w, h, imgType);
		
		
		setGraphics2D(targetRenderImage.createGraphics());
		getGraphics2D().setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		
		shapeDrawer = new VectorShapeDrawer(getGraphics2D());
		
	}
	
	
	public BufferedImage getImage() {
		return targetRenderImage;
	}
	
	public BufferedImage copyImage() {
		return ImageProcessing.copyImage(targetRenderImage);
	}
	
	
	public ImageCoordinateSystem getCoordinateSystem() {
		return coordinateSystem;
	}
	
	public void clearImage() {
		Color blank = new Color(0,0,0,0);
		fillBackground(blank); 
	}

	public void fillBackground(Color c) {
		getGraphics2D().setBackground(c);
		getGraphics2D().clearRect(0, 0, coordinateSystem.getBufferWidth(), coordinateSystem.getBufferHeight());
	}
	
	void fillBackgroundWithImage(BufferedImage img, float alpha) {
		BufferedImage resizedImage = ImageProcessing.resizeTo(img, coordinateSystem.getBufferWidth(), coordinateSystem.getBufferHeight());
		pasteImage_BufferCoordinates(resizedImage,0,0,alpha);
	}

	
	
	
	

	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// Generic sprite paste used by all sub-classes. Pastes the pixel values of the image
	// into the receiving render-image
	// All subclasses must implement pasteImage_BufferCoordinates(BufferedImage img, int x, int y, float alpha);
	//
	//
	//
	public void pasteSprite(Sprite sprite) {
		pasteImage(sprite.getImage(), sprite.getDocSpaceRect().getTopLeft(),  sprite.alpha);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// pastes the topleft of the image at docSpacePoint
	// 
	public void pasteImage(BufferedImage img, PVector docSpacePoint, float alpha) {
		Rect r = getPasteRectDocSpace(img, docSpacePoint);
		PVector bufferPt = coordinateSystem.docSpaceToBufferSpace(docSpacePoint);
		pasteImage_BufferCoordinates(img, (int) bufferPt.x, (int) bufferPt.y, alpha);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// This is the method that actually does the compositing of the image within the
	// document image
	//
	// This will work with all Java pre-defined image types!
	//
	//
	public void pasteImage_BufferCoordinates(BufferedImage img, int x, int y, float alpha) {
		//System.out.println("compositing " + x + " " + y);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		getGraphics2D().setComposite(ac);
		getGraphics2D().drawImage(img, x, y, null);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// Generic sprite mask-paste used by all sub-classes. Pastes the Color C through the sprite's alpha channel.
	// I.e. alters a pixel in the targetRenderImage where the alpha in the sprite image is > 0
	// All subclasses must implement pasteImageMask_BufferCoordinates(BufferedImage img, int x, int y, float alpha);
	//
	//
	//
	public void pasteSpriteMask(Sprite sprite, Color c) {
		pasteImageMask(sprite.getImage(), sprite.getDocSpaceRect().getTopLeft(),  sprite.alpha, c);
	}
	
	
	public void pasteImageMask(BufferedImage img, PVector docSpacePoint, float alpha, Color c) {
		BufferedImage spriteMaskImage = ImageProcessing.replaceColor(img, c);
		pasteImage(spriteMaskImage, docSpacePoint, alpha);
	}
	
	
	
	public Rect getPasteRectDocSpace(BufferedImage img, PVector docSpacePoint) {
		// Final pasting always by defining the top left of the image in doc space
		// give an image and its upperleft at docSpacePoint
		// return the doc space rect it occupies
		PVector bottomRightOffset = coordinateSystem.bufferSpaceToDocSpace(img.getWidth(), img.getHeight());
		PVector bottomRight = PVector.add(docSpacePoint, bottomRightOffset);
		return new Rect(docSpacePoint, bottomRight);
	}

	////////////////////////////////////////////////////////////////////////////////////
	// this is called by the Surface to get the current view rect from the whole
	//////////////////////////////////////////////////////////////////////////////////// bufferedImage
	//
	BufferedImage getCropDocSpace(Rect docSpaceRect) {
		PVector topLeft = coordinateSystem.docSpaceToBufferSpace(docSpaceRect.getTopLeft());
		PVector bottomRight = coordinateSystem.docSpaceToBufferSpace(docSpaceRect.getBottomRight());
		Rect bufferSpaceRect = new Rect(topLeft, bottomRight);
		return getCropBufferSpace(bufferSpaceRect);
	}

	public BufferedImage getCropBufferSpace(Rect bufferSpaceRect) {
		return ImageProcessing.cropImage(targetRenderImage, bufferSpaceRect);
	}

	public void saveRenderToFile(String pathAndFilename) {
		System.out.println("RenderTarget:saveRenderToFile  " + pathAndFilename);
		// check to see if extension exists
		String ext = MOStringUtils.getFileExtension(pathAndFilename);
		String extensionChecked = pathAndFilename;
		if (ext.contentEquals("")) {
			extensionChecked = pathAndFilename + ".png";
		}

		// check to see if extension is correct
		ext = MOStringUtils.getFileExtension(extensionChecked);
		if (ext.contentEquals(".png") || ext.contentEquals(".PNG")) {
			// OK
		} else {
			System.out.println("RenderTarget:saveRenderToFile file extesion is wrong - " + ext);
			return;
		}

		try {
			// retrieve image
			File outputfile = new File(extensionChecked);
			ImageIO.write(targetRenderImage, "png", outputfile);
		} catch (IOException e) {
			System.out.println("RenderTarget:saveRenderToFile could not save file - " + extensionChecked + " " + e);
		}

	}

	
	
	//////////////////////////////////////////////////////////////////////
	// scaled drawing operations
	// All units are in documentSpace, including lineThickness
	// so, in this case, radius and lineThickness are fractions of the longest edge.

	public void drawCircle(PVector docPoint, float radiusDocSpace, Color fillColor, Color lineColor,
			float lineThicknessDocSpace) {

		// work out if its succumbs to the permitted paste area
		PVector docPtMinusRad = new PVector(docPoint.x - radiusDocSpace, docPoint.y - radiusDocSpace);
		PVector docPtPlusRad = new PVector(docPoint.x + radiusDocSpace, docPoint.y + radiusDocSpace);
		Rect r = new Rect(docPtMinusRad, docPtPlusRad);
		

		PVector bufpt = coordinateSystem.docSpaceToBufferSpace(docPoint);

		int lineThicknessInPixels = (int) (lineThicknessDocSpace * coordinateSystem.getLongestBufferEdge());
		shapeDrawer.setDrawingStyle(fillColor, lineColor, lineThicknessInPixels);

		//shapeDrawer.setDrawingStyle(fillColor, Color.BLACK, 4);

		float radiusInPixels = radiusDocSpace * coordinateSystem.getLongestBufferEdge();

		//
		float left = bufpt.x - radiusInPixels;
		float top = bufpt.y - radiusInPixels;
		float w = radiusInPixels * 2;

		shapeDrawer.drawEllipse(left, top, w, w);

	}

	//////////////////////////////////////////
	// debug drawing operations other than paste
	// these do not scale the drawing to the document
	public void drawPoints(ArrayList<PVector> docSpacePoints, Color c, float pixelRadius) {
		float r = pixelRadius * GlobalSettings.getSessionScale();
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		shapeDrawer.setDrawingStyle(ca, ca, 2);

		for (PVector p : docSpacePoints) {
			PVector bufpt = coordinateSystem.docSpaceToBufferSpace(p);
			shapeDrawer.drawEllipse(bufpt.x, bufpt.y, r, r);
		}

	}

	public void drawPoint(PVector docSpacePoint, Color c, int size) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		shapeDrawer.setDrawingStyle(ca, ca, 2);
		PVector bufpt = coordinateSystem.docSpaceToBufferSpace(docSpacePoint);
		shapeDrawer.drawEllipse(bufpt.x, bufpt.y, size, size);

	}

	public void drawLine(PVector start, PVector end, Color c, int w) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
		shapeDrawer.setDrawingStyle(ca, ca, w);
		PVector bufStart = coordinateSystem.docSpaceToBufferSpace(start);
		PVector bufEnd = coordinateSystem.docSpaceToBufferSpace(end);
		shapeDrawer.drawLine(bufStart.x, bufStart.y, bufEnd.x, bufEnd.y);
	}
	
	
	void drawLine(Line2 l, Color c, int w) {
		Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
		shapeDrawer.setDrawingStyle(ca, ca, w);
		PVector bufStart = coordinateSystem.docSpaceToBufferSpace(l.p1);
		PVector bufEnd = coordinateSystem.docSpaceToBufferSpace(l.p2);
		shapeDrawer.drawLine(bufStart.x, bufStart.y, bufEnd.x, bufEnd.y);
	}
	
	
	
	void drawVertices(Vertices2 v, Color c, int w) {
		int numLines = v.getNumLines();
		for(int n = 0; n < numLines; n++) {
			Line2 l = v.getLine(n);
			drawLine( l,  c,  w);
		}
		
	}
	
	public void drawVerticesWithPoints(Vertices2 v, Color c, int w) {
		int numLines = v.getNumLines();
		for(int n = 0; n < numLines; n++) {
			Line2 l = v.getLine(n);
			drawLine( l,  c,  w);
			drawPoint(l.p1, c, w+4);
			drawPoint(l.p2, c, w+4);
		}
		
	}
	
	void drawText(String str, PVector docSpacePoint, int size, Color c) {
		PVector bufpt = coordinateSystem.docSpaceToBufferSpace(docSpacePoint);
		drawText( str, (int)bufpt.x, (int)bufpt.y,  size,  c);
	}

	public void drawText(String str, int bufferX, int bufferY, int size, Color c) {
		VectorShape textShape = new VectorShape();
		textShape.setTextShape(bufferX, bufferY, str, c, size);
		shapeDrawer.drawDrawnShape(textShape);
	}
	
	public VectorShapeDrawer getVectorShapeDrawer() {
		return shapeDrawer;
	}

	public Graphics2D getGraphics2D() {
		return graphics2D;
	}

	public void setGraphics2D(Graphics2D graphics2d) {
		graphics2D = graphics2d;
	}

}