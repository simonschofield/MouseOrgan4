import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import MOImageClasses.ImageProcessing;
import MOMaths.PVector;
import MOMaths.Rect;

// This is an improved view controller that uses the whole window and is not limited to the aspect of the document
// Given the following unchanging parameters
// Document size (w, h and aspect)
// ViewDisplayRegion (w,h and aspect)
// calculate the view (i.e. the portion of the document buffer) to be shown.
// Other than "zoonState 0 - fit to window", the calculation of this cropped area
// is made on currentZoom (1... > 1, == scale = 1/zoom), and panX and panY (which are in buffer space pixels.
public class ViewController {
	// this is the document buffer pixel space rect which represents
	// a region of the image to be cropped out and shown in the
	// viewDisplayRegion. Hence, apart from the fitToWindow view, it
	// always has the same aspect as the viewDisplayRecT
	Rect currentViewCropRect;

	// This is a flag
	// 0 == fit to window
	// 1 == first zoom, fitting the shortest image dimension to the width/height of
	// the view rect
	// 2 upwards == zooms into the image
	int zoomSetting = 0;

	// always 1 or larger, defines the "magnification" from 1
	float currentZoom = 1.0f;

	// are the actual pixel coordinates of the centre of the currentViewRect
	//
	float currentXPan = 0, currentYPan = 0;

	// this is the view display rect in pixel dimensions within
	// the Application window. It does not change.
	Rect viewDisplayRect;
	float viewDisplayRectAspect;

	// This is the rect to position the image in the centre of the view under FitToWindow
	Rect fitToWindowCentreRect = new Rect();
	
	// this is the dimension of the main document in buffer space. They do not
	// change.
	private int theDocumentWidth;
	private int theDocumentHeight;
	private float theDocumentAspect;
	private PVector theDocumentCentre;
	Rect theDocumentRect;
	
	Color viewDisplayRectBackgroundColor = Color.WHITE;
	
	
	///////////////////////////////////////////////////////////////////////////
	// Background Image
	// When set, is adjusted to have the same size as the main document image
	// so all view-mapping operations are identical
	BufferedImage backgroundImage = null;
	
	public ViewController() {

	}

	void init() {
		viewDisplayRect = GlobalObjects.theSurface.getViewDisplayRegion();
		System.out.println(
				"viewRect width " + viewDisplayRect.getWidth() + " viewRect height " + viewDisplayRect.getWidth());
		viewDisplayRectAspect = viewDisplayRect.aspect();

		theDocumentWidth = GlobalObjects.theDocument.coordinateSystem.getBufferWidth();
		theDocumentHeight = GlobalObjects.theDocument.coordinateSystem.getBufferHeight();
		theDocumentAspect = theDocumentWidth / (float) theDocumentHeight;
		theDocumentCentre = new PVector(theDocumentWidth / 2f, theDocumentHeight / 2f);
		theDocumentRect = new Rect(0,0,theDocumentWidth,theDocumentHeight);
		currentViewCropRect = new Rect(0, 0, theDocumentWidth, theDocumentHeight);

	}
	
	public void setBackgroundImage(BufferedImage bi) {
		if(bi==null) {
			backgroundImage = null;
			return;
		}
		backgroundImage = ImageProcessing.resizeTo(bi, theDocumentWidth, theDocumentHeight);
		System.out.println(
				"background image set to width " + theDocumentWidth + "  height " + theDocumentHeight);
	}

	Rect getCurrentViewCropRect() {
		return currentViewCropRect.copy();
	}
	
	
	Rect getCurrentViewPortDocSpace() {
		// returns the currentViewCropRect (whihc is in buffer-space) into documentSpace
		// this is needed by external objects only to do cropping operations
		PVector topLeftBufferSpace = currentViewCropRect.getTopLeft();
		PVector bottomRightBufferSpace = currentViewCropRect.getBottomRight();
		PVector topLeftDocSpace = GlobalObjects.theDocument.coordinateSystem.bufferSpaceToDocSpace(topLeftBufferSpace);
		PVector bottomRightDocSpace = GlobalObjects.theDocument.coordinateSystem.bufferSpaceToDocSpace(bottomRightBufferSpace);
		return new Rect(topLeftDocSpace, bottomRightDocSpace);
	}
	
	void setViewDisplayRectBackgroundColor(Color c) {
		viewDisplayRectBackgroundColor = c;
		
	}

	// this is called frequently by the surface to update the display to the new zoom/pan
	void updateDisplay(Graphics2D g2d) {

		if (zoomSetting == 0) {
			showFitToViewDisplayRect(g2d);
		}

		if (zoomSetting > 0) {
			showZoom(g2d);
		}

	}
	
	
	// coordinate space conversion between the doc space and the the absolute
	// pixel location in the application window (not offset by the canvas location)
	// and vice-versa
	PVector appWindowCoordinateToDocSpace(int x, int y) {
		// used by the SimpleUI interface canvas object
		// takes a pixel location within the viewDisplayRect (0,0) top left
		// and maps it to Document space
		x = x + (int)viewDisplayRect.left;
		y = y + (int)viewDisplayRect.top;
		PVector viewDisplayRectPoint = new PVector(x,y);
		
		if (zoomSetting == 0) {
			// do the correct mapping for the fitToWindow set up
			PVector pixelInImageBuffer = Rect.map(viewDisplayRectPoint,  fitToWindowCentreRect, theDocumentRect);
			PVector docSpace = GlobalObjects.theDocument.coordinateSystem.bufferSpaceToDocSpace(pixelInImageBuffer);
			//System.out.println("window click x " + x + " y " + y + "doc space point is " + docSpace.toStr());
			return docSpace;
		}
		
		// if zoom > 1 the use the currentViewCropRect
		PVector pixelInImageBuffer = Rect.map(viewDisplayRectPoint, viewDisplayRect, currentViewCropRect);
		return GlobalObjects.theDocument.coordinateSystem.bufferSpaceToDocSpace(pixelInImageBuffer);
	}
	
	
	PVector docSpaceToAppWindowCoordinate(PVector docSpacePt) {
		// used by the SimpleUI interface canvas to draw overlay items to the viewDisplayRect
		
		PVector pixelInImageBuffer = GlobalObjects.theDocument.coordinateSystem.docSpaceToBufferSpace(docSpacePt);
		if (zoomSetting == 0) {
			return Rect.map(pixelInImageBuffer, theDocumentRect, fitToWindowCentreRect);
		}
		
		float xInCurrentCropRect = pixelInImageBuffer.x - currentViewCropRect.left + (currentXPan - currentViewCropRect.getWidth()/2f);
		float yInCurrentCropRect = pixelInImageBuffer.y - currentViewCropRect.top  + (currentYPan - currentViewCropRect.getHeight()/2f);
		PVector pointInCurrentCropRect = new PVector(xInCurrentCropRect,yInCurrentCropRect);
		return Rect.map(pointInCurrentCropRect, currentViewCropRect, viewDisplayRect);
	}

	void showFitToViewDisplayRect(Graphics2D g2d) {
		// "fit to window" stuff. This is a special case, so uses own algorithm.
		// draw the whole image offset in the centre of the viewDisplayRect

		// also resets zoom and pan for subsequent zooms
		currentZoom = 1;
		currentXPan = theDocumentCentre.x;
		currentYPan = theDocumentCentre.y;
		
		currentViewCropRect = new Rect(0, 0, theDocumentWidth, theDocumentHeight);
		
		fitToWindowCentreRect = getFitToViewDisplayRect();
		
		drawView(g2d, fitToWindowCentreRect);
		
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	// returns the absolute application pixel coordinates of the region in the centre
	// of the viewDisplayRect that can contain (a scaled version of) the whole image
	Rect getFitToViewDisplayRect() {
		Rect fitToWindowRect = new Rect();
		if (theDocumentAspect > viewDisplayRectAspect) {
			// do "landscape fit" where it needs padding top/bottom for "landscape fit"
			// work out the scale to shrink the image into this rect
			float scaleToFit = viewDisplayRect.getWidth() / theDocumentWidth;
			float scaledImageHeight = theDocumentWidth * scaleToFit;
			int halfDifferenceInHeight = (int) ((viewDisplayRect.getHeight() - scaledImageHeight) / 2f);

			fitToWindowRect.setWithDimensions(viewDisplayRect.left, viewDisplayRect.top + halfDifferenceInHeight, viewDisplayRect.getWidth(), scaledImageHeight);
		} else {
			// do "portrait fit" where it needs padding left right
			// work out the scale to shrink the image into this rect
			float scaleToFit = viewDisplayRect.getHeight() / theDocumentHeight;
			float scaledImageWidth = (theDocumentWidth * scaleToFit);
			int halfDifferenceInWidth = (int) ((viewDisplayRect.getWidth() - scaledImageWidth) / 2f);
			
			fitToWindowRect.setWithDimensions( viewDisplayRect.left + halfDifferenceInWidth, viewDisplayRect.top, scaledImageWidth, viewDisplayRect.getHeight());
		}
		return fitToWindowRect; 
	}
	


	void showZoom(Graphics2D g2d) {
		drawView(g2d, viewDisplayRect);
	}
	
	
	
	void drawView(Graphics2D g2d, Rect imageDisplayRegion) {
		
		Color inertFill = new Color(230,230,230);
		g2d.setColor(inertFill);
		g2d.fillRect((int) viewDisplayRect.left, (int) viewDisplayRect.top, (int) viewDisplayRect.getWidth(),
				(int) viewDisplayRect.getHeight());
		
		g2d.setColor(viewDisplayRectBackgroundColor);	
		g2d.fillRect((int) imageDisplayRegion.left, (int) imageDisplayRegion.top,
				(int) imageDisplayRegion.getWidth(), (int) imageDisplayRegion.getHeight());
		

		if(backgroundImage != null) {
			// draw the background image first, then the render on top
			//System.out.println("ViewControl: drawView currentViewCropRect " + currentViewCropRect.toStr());
			
			BufferedImage croppedToCurrentViewRectImg = ImageProcessing.cropImage(backgroundImage, currentViewCropRect);
			
			
			
			g2d.drawImage(croppedToCurrentViewRectImg, (int) imageDisplayRegion.left,
					(int) imageDisplayRegion.top, (int) imageDisplayRegion.getWidth(), (int) imageDisplayRegion.getHeight(), null);
			
		}
		
		// draw the render image
		BufferedImage displayImage = GlobalObjects.theDocument.getCropBufferSpace(currentViewCropRect);
		g2d.drawImage(displayImage, (int) imageDisplayRegion.left,
				(int) imageDisplayRegion.top, (int) imageDisplayRegion.getWidth(), (int) imageDisplayRegion.getHeight(), null);
		
		
		
	}
	
	
	void showActualSize() {
		// width of screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		float screenWidth =  (float)screenSize.getWidth();
		

		// physical width of viewDisplayRect window in inches
		float physicalWidthOfScreen = 23.5f;
		float physicalWidthOfViewDisplayRect = physicalWidthOfScreen * (viewDisplayRect.getWidth()/screenWidth);
		
		// so a full-scale image would be shown at 300 dpi
		// an image shown at SessionScale of 0.5 would show 300*0.5 pixels
		float fullSizeDPI = 300 * GlobalObjects.theSurface.getSessionScale();
		float pixelsShownAtThisWidthForFullSize = physicalWidthOfViewDisplayRect*fullSizeDPI;
		
		// now work out the scale required so that
		// currentViewCropRect.width is set to equal pixelsShownAtThisWidthForFullSize
		// Under "portrait" fit the total width of the image is shown in the viewDisplayRect at a  scale of 1
		// under "landscape" fit the scale is set to 1 to show the whole height of the image, so the scale for the correct dpi needs to be increased by * aspect of image
		// However, the result would be the same whatever aspect the image is
		// so we don't need to take this into consideration
		float scale = pixelsShownAtThisWidthForFullSize / theDocumentWidth;
		if (theDocumentAspect > viewDisplayRectAspect) scale *= theDocumentAspect;
		
		// work out the zoomSetting that would be needed for this scale
		int zoomSettingCounter = 1;
		float testScale = 1f;
		while(testScale > scale) {
			zoomSettingCounter++;
			testScale /= 1.25;
			if(zoomSettingCounter > 1000) {
				System.out.println("showActualSize something went wrong");
				return;
			}
		}
		
		zoomSetting = zoomSettingCounter;
		currentZoom = 1/scale;
		currentViewCropRect = calculateViewCropRect(currentXPan,currentYPan);
		
		System.out.println("showActualSize set zoom to" + currentZoom + " zoomSettings to " + zoomSetting);
	}

	float getCurrentScale() {
		return 1 / currentZoom;
	}

	float getCurrentZoom() {
		return currentZoom;
	}

	

	void setZoomPlusMinus(int dzoom) {

		zoomSetting += dzoom;

		if (zoomSetting < 0) zoomSetting = 0;

		if(zoomSetting == 0) return;
		
		if(zoomSetting == 1) currentZoom = 1f;
		
		if(zoomSetting > 1) {
			
			if(dzoom == 1) {
				currentZoom = currentZoom * 1.25f;
			}
			if(dzoom == -1) {
				currentZoom = currentZoom/ 1.25f;
			}
			
		}

		//System.out.println("zoomSetting " + zoomSetting + " currentZoom " + currentZoom);

		currentViewCropRect = calculateViewCropRect(currentXPan,currentYPan);

	}

	
	void shiftXY(float dx, float dy) {
		// the amount of shift is scaled here to be a proportion of the current view

		float attemptedShiftX = currentXPan + dx * theDocumentWidth * getCurrentScale();
		float attemptedShiftY = currentYPan + dy * theDocumentHeight * getCurrentScale();

		currentViewCropRect = calculateViewCropRect(attemptedShiftX,attemptedShiftY);
		
		System.out.println("currentViewRect " + currentViewCropRect + " currentXPan " + currentXPan + " currentYPan " + currentYPan);
	}
	
	Rect calculateViewCropRect(float attemptedPanX, float attempetedPanY) {
		// calculates the currentViewCropRect based on
		// the currentScale (1/currentZoom) and the attemptedPanX and Y

		float scale = getCurrentScale();

		float cropBoxWidth, cropBoxHeight;

		if (theDocumentAspect < viewDisplayRectAspect) {
			// then it fits pillar box in to the always letter box VDR
			// so the width of the currentViewRect is set to the width of the document image
			// and the height of the currentViewRect is set to this width /
			// viewDisplayRectAspect
			cropBoxWidth = theDocumentWidth * scale;
			cropBoxHeight = cropBoxWidth / viewDisplayRectAspect;
		} else {
			// then it fits letter box in to the always letter box VDR
			// so the height of the currentViewRect is set to the height of the document
			// image
			// and the width of the currentViewRect this height / viewDisplayRectAspect
			cropBoxHeight = theDocumentHeight * scale;
			cropBoxWidth = cropBoxHeight / viewDisplayRectAspect;
		}

		Rect untranslatedViewRect = new Rect(0, 0, cropBoxWidth, cropBoxHeight);
		untranslatedViewRect.setCentreTo(attemptedPanX, attempetedPanY);
		Rect translatedViewRect = shuntInside(untranslatedViewRect);
		currentXPan = translatedViewRect.getCentre().x;
		currentYPan = translatedViewRect.getCentre().y;
		return translatedViewRect;
	}
	
	

	// called from UI
	//
	//
	public void keyboardViewInput(KeyEvent e) {
		// println("keyboard zoom", theKey, theKeyCode);
		// - + zoom keys

		if (e.getKeyChar() == 'a') {
			// this is the - key (zoom out)
			showActualSize();
		}
		
		if (e.getKeyChar() == '-') {
			// this is the - key (zoom out)
			setZoomPlusMinus(-1);

		}
		if (e.getKeyChar() == '=') {
			// this is the + key (zoom in)
			setZoomPlusMinus(1);
		}

		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			// track left
			shiftXY(0.2f, 0f);
		}

		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			// track up
			shiftXY(0f, 0.2f);
		}

		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			// track right
			shiftXY(-0.2f, 0);
		}

		if (e.getKeyCode() == KeyEvent.VK_UP) {
			// track down
			shiftXY(0f, -0.2f);
		}

		System.out.println(
				"zoomSetting " + zoomSetting + " current zoom " + currentZoom + " current scale " + getCurrentScale());
		// System.out.println(" new view rect " + currentViewRect.left + ", " +
		// currentViewRect.top + ", " + currentViewRect.getWidth() + ", " +
		// currentViewRect.getHeight());

	}
	
	Rect shuntInside(Rect r) {
		// this deals with a rect in buffer space. If it is outside the
		// legal bounds of (0,0, documentWidth, documentHeight)
		// it is shunted to the nearest legal position

		if (r.getWidth() > theDocumentWidth || r.getHeight() > theDocumentHeight) {
			// illegal upscaling, don't allow change
			return currentViewCropRect;
		}

		if (r.isWhollyInsideOther(new Rect(0, 0, theDocumentWidth, theDocumentHeight))) {
			// is wholly inside document so does not need shunting
			// System.out.println("shuntInside::wholly inside");
			return r.copy();
		}

		// System.out.println("shuntInside::applying shunt");

		Rect shiftedRect = r.copy();
		// check shifting in x
		if (r.left < 0) {
			float dif = r.left; // generates a -ve num
			shiftedRect.left = 0;
			shiftedRect.right = r.right - dif; // shifts to the right
		}
		if (r.right > theDocumentWidth) {
			float dif = theDocumentWidth - r.right; // generates a -ve num
			shiftedRect.left = r.left + dif; // shifts to the left
			shiftedRect.right = theDocumentWidth;
		}

		if (r.top < 0) {
			float dif = r.top; // generates a -ve num
			shiftedRect.top = 0;
			shiftedRect.bottom = r.bottom - dif; // shifts down (+veley)
		}
		if (r.bottom > theDocumentHeight) {
			float dif = theDocumentHeight - r.bottom; // generates a -ve num
			shiftedRect.top = r.top + dif; // shifts up (-veley)
			shiftedRect.bottom = theDocumentHeight;
		}

		return shiftedRect;

	}

	
}
