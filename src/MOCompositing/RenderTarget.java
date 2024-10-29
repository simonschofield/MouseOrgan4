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
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import MOAppSessionHelpers.SceneHelper;
import MOImage.ImageProcessing;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOSprite.Sprite;
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
	
	public int getType() {
		return targetRenderImage.getType();
	}
	
	
	public void setImage(BufferedImage img) {
		// set the renderTarget image after initialisation
		// does checking to see if this incoming image is the correct type and size
		if(img.getWidth()!=targetRenderImage.getWidth() || img.getHeight()!=targetRenderImage.getHeight()) {
			System.out.println("RenderTarget::setImage - image is wrong size, size" + img.getWidth() + " ," + img.getHeight() + " should be " + targetRenderImage.getWidth() + " ," + targetRenderImage.getHeight());
			return;
		}
		if(img.getType() != targetRenderImage.getType()){
			System.out.println("RenderTarget::setImage - image is type, is type " + img.getType() + " should be " + targetRenderImage.getType());
			return;
		}
		targetRenderImage = img;
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
	
	public void fillBackground_BufferSpace(Rect r, Color c) {
		getGraphics2D().setBackground(c);
		getGraphics2D().clearRect((int)r.left, (int) r.top, (int) r.getWidth(), (int) r.getHeight());
	}
	
	public void fillBackgroundWithImage(BufferedImage img, float alpha) {
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
		pasteImage_TopLeftDocPoint(sprite.getImage(), sprite.getDocSpaceRect().getTopLeft(),  sprite.alpha);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// pastes the topleft of the image at docSpacePoint
	// 
	public void pasteImage_TopLeftDocPoint(BufferedImage img, PVector docSpaceTopLeftPoint, float alpha) {
		//Rect r = getPasteRectDocSpace(img, docSpacePoint);
		PVector bufferPt = coordinateSystem.docSpaceToBufferSpace(docSpaceTopLeftPoint);
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
	// No alpha is allowed as this would being muddy 
	//
	public void pasteSpriteMask(Sprite sprite, Color c) {
		pasteImageMask(sprite.getImage(), sprite.getDocSpaceRect().getTopLeft(),  c);
	}
	
	// replaces the colour of the image with a single colour
	public void pasteImageMask(BufferedImage img, PVector docSpacePoint, Color c) {
		
		BufferedImage spriteMaskImage =  ImageProcessing.replaceColor(img, c);
		
		pasteImage_TopLeftDocPoint(spriteMaskImage, docSpacePoint, 1);
	}
	
	// uses an alternative image when pasting - this allows the user to process the sprite's
	// normal image in anyway desired and the used instead of the sprite image
	public void pasteSpriteAltImage(Sprite sprite, BufferedImage altImage, float alpha) {
		pasteImage_TopLeftDocPoint(altImage, sprite.getDocSpaceRect().getTopLeft(),  alpha);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// Experimental erase operation
	// Rather than adding to what is already there, this method reduces the alpha of pixels within the existing targetRenderImage.
	// It subtracts the sprite's alpha from the existing alpha. The sprite's colour is ignored. 
	public void pasteSpriteErase(Sprite sprite) {
		PVector topLeftDocSpace = sprite.getDocSpaceRect().getTopLeft();
		PVector bufferPt = coordinateSystem.docSpaceToBufferSpace(topLeftDocSpace);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_OUT, sprite.alpha);
		getGraphics2D().setComposite(ac);
		getGraphics2D().drawImage(sprite.getImage(), (int)bufferPt.x, (int)bufferPt.y, null);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// Experimental custom mask paste operation for 16 bit data (e.g. a depth mask),
	// When pasting to a 16 bit depth target image we cannot use the sprite's image colour-data as the data to paste, as this will
	// be 8 bit color data, so is incapable of carrying the data resolution required. There is no Java 16 bit colour model with alpha, so normal compositing  operations are 
	// also ruled out. Therefore, we have to paste 16bit data "long-hand"
	// Not sure if there should be any anti-alias smoothing included working on the edges of the alpha sprites
	// The depth value of 0 is reserved to represent "no substance" e.g. sky
	// The value to be added should be in the range 0...1
	public void pasteSpriteMaskTo16BitGray(Sprite sprite, float val) {
		
		if(getType() != BufferedImage.TYPE_USHORT_GRAY) {
			System.out.println("RenderTarget.pasteSpriteMaskTo16BitGray :: target mask is not USHORT_GRAY");
			return;
		}
		
		int max = Short.MAX_VALUE;
		
		
		PVector topLeftDocSpace = sprite.getDocSpaceRect().getTopLeft();
		PVector bufferPt = coordinateSystem.docSpaceToBufferSpace(topLeftDocSpace);
		
		int sourceWidth = sprite.getImageWidth();
		int sourceHeight = sprite.getImageHeight();
		int targetOffsetX = (int) bufferPt.x;
		int targetOffsetY = (int) bufferPt.y;
		int targetWidth = targetRenderImage.getWidth();
		int targetHeight = targetRenderImage.getHeight();
		
		WritableRaster targetImageData = targetRenderImage.getRaster();
		WritableRaster sourceImageAlphaData = sprite.getImage().getAlphaRaster();
		
		int shortval = (int) (val * 65535);
		if(shortval < 1) shortval = 1;
		
		//System.out.println(" UShort Depth  = " + shortval);
		for (int y = 0; y < sourceHeight; y++) {
			for (int x = 0; x < sourceWidth; x++) {
				
				int targetX = x + targetOffsetX;
				int targetY = y + targetOffsetY;
				if(targetX < 0 || targetX >= targetWidth || targetY < 0 || targetY >= targetHeight) continue;
					
				int sourceImageAphaValue = sourceImageAlphaData.getSample(x, y, 0);
				if(sourceImageAphaValue>127) targetImageData.setSample(targetX, targetY, 0,shortval);
				
				
			}
		}

	}

	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// the sprite has a similar method, we might need this here as it is very generalised
	// but for the moment removed
	/*
	public Rect getPasteRectDocSpace(BufferedImage img, PVector docSpacePoint) {
		// Final pasting always by defining the top left of the image in doc space
		// give an image and its upperleft at docSpacePoint
		// return the doc space rect it occupies
		PVector bottomRightOffset = coordinateSystem.bufferSpaceToDocSpace(img.getWidth(), img.getHeight());
		PVector bottomRight = PVector.add(docSpacePoint, bottomRightOffset);
		return new Rect(docSpacePoint, bottomRight);
	}*/
	
	////////////////////////////////////////////////////////////////////////////////////
	// Unusual direct access to the target image. Probably only used for tsting stuff
	public void setPixel(int x, int y, Color c) {
		
		targetRenderImage.setRGB(x, y, c.getRGB());
		
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

	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// scaled drawing operations
	// All units are in documentSpace, including lineThickness and circle radius
	// so, in this case, radius and lineThickness are fractions of the longest edge.
	// As this is not very human-friendly, use SceneHelper.millimeterToDocspace(mmInFinalPrintedImage) to help
	//
	//
	//
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
	
	public void clearRect(Rect r) {
		// deletes everything inside of the rect
		fillBackground_DocSpace( r, new Color(0,0,0,0));
	}
	
	public void clearOutsideRect(Rect r) {
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
		clearRect(topRect);
		clearRect(leftRect);
		clearRect(rightRect);
		clearRect(bottomRect);
	}
	
	public void fillBackground_DocSpace(Rect r, Color fillColor) {
		// not the same as drawing a rect, as it uses Graphics2D.clearBackground()
		// which has no line styles etc....
		Rect bufferRect = coordinateSystem.docSpaceToBufferSpace(r);
		fillBackground_BufferSpace(bufferRect, fillColor);
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

	public void drawPoint(PVector docSpacePoint, Color c, float pixelRadius) {
		float r = pixelRadius * GlobalSettings.getSessionScale();
		shapeDrawer.setDrawingStyle(c, c, 2);
		PVector bufpt = coordinateSystem.docSpaceToBufferSpace(docSpacePoint);
		float halfRadius = r/2;
		shapeDrawer.drawEllipse(bufpt.x-halfRadius, bufpt.y-halfRadius, r, r);

	}
	
	void drawLine(Line2 l, Color c, float w) {
		//Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
		shapeDrawer.setDrawingStyle(c, c, w);
		PVector bufStart = coordinateSystem.docSpaceToBufferSpace(l.p1);
		PVector bufEnd = coordinateSystem.docSpaceToBufferSpace(l.p2);
		shapeDrawer.drawLine(bufStart.x, bufStart.y, bufEnd.x, bufEnd.y);
	}

	public void drawLine(PVector start, PVector end, Color c, float w) {
		//Color ca = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
		shapeDrawer.setDrawingStyle(c, c, w);
		PVector bufStart = coordinateSystem.docSpaceToBufferSpace(start);
		PVector bufEnd = coordinateSystem.docSpaceToBufferSpace(end);
		shapeDrawer.drawLine(bufStart.x, bufStart.y, bufEnd.x, bufEnd.y);
	}
	
	
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
	
	
	public void drawVertices2NoFill(Vertices2 v, Color lineCol, float w, float[] dashPattern) {
		// for the sake of ease of use, dash pattern styles are in 100% render pixel dimensions, so if the render is 
		// scaled-down, so should the dash styles
		
		
		// draws the lines only, has an option for dashes
		if(dashPattern == null) {
			//System.out.println("setting plain style");
			shapeDrawer.setDrawingStyle(MOImage.MOColor.invisibleCol(), lineCol, w);
		}else {
			//System.out.println("setting dashed style");
			float[] scaledDashPattern = sessionScaleDashPattern(dashPattern);
			shapeDrawer.setStrokeCapJoin(BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
			shapeDrawer.setDrawingStyle(MOImage.MOColor.invisibleCol(), lineCol, w, scaledDashPattern);		
			}
		

		Vertices2 vbuff = v.getInBufferSpace(false);
		if(vbuff==null) return;
		
		shapeDrawer.drawVertices2(vbuff);
	}
	
	public void drawVertices2(Vertices2 v, Color fillCol, Color lineCol, float w) {
		shapeDrawer.setDrawingStyle(fillCol,lineCol, w);
		Vertices2 vbuff = v.getInBufferSpace(false);
		shapeDrawer.drawVertices2(vbuff);
	}
	
	
	public void drawVerticesWithPoints(Vertices2 v, Color c, float w) {
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

}