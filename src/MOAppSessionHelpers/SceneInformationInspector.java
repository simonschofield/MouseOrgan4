package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOApplication.Surface;
import MOCompositing.BufferedImageRenderTarget;
import MOMaths.PVector;
import MOScene3D.SceneData3D;
import MOSimpleUI.SimpleUI;
import MOSimpleUI.Slider;
import MOSimpleUI.TextInputBox;
import MOSimpleUI.UIEventData;
import MOSprite.Sprite;
import MOSprite.SpriteBatch;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePairList;
import MOUtils.MOStringUtils;

public class SceneInformationInspector {
	
	static Surface theSurface = null;
	static SceneData3D sceneData3D = null;
	static SimpleUI theUI;
	static SpriteBatch theSpriteBatch;
	static float measuringToolSize = 10;
	static PVector measuringToolStartPoint;
	
	static String toolMode = "3d info";
	
	static boolean initialised = false;
	
	static BufferedImageRenderTarget spriteIDRenderTarget;
	static String overlayGroup = "overlayGroup";
	
	static Color darkBlue = new Color(74,74,240);
	///////////////////////////////////////////////////////////////////////////////////////////
	// initialise() must be called from  loadContentUserSession(), as it requires the UI to be already initialised
	// which only happens after initialiseUserSession()
	// As the inclusion of SpriteID takes a lot of memory and pasting time, it is nullable
	// As the scene may be only 2D, the SceneData3D is also nullable
	//
	public static void initialise(Surface s, SpriteBatch sb, SceneData3D sd3d) {
		
		theSurface = s;
		theUI = theSurface.theUI;
		
		theSpriteBatch = sb;
		sceneData3D = sd3d;
		
		if(theSpriteBatch != null) {
			GlobalSettings.getDocument().addRenderTarget("spriteIDRenderTarget", BufferedImage.TYPE_INT_ARGB);
			spriteIDRenderTarget = GlobalSettings.getDocument().getBufferedImageRenderTarget("spriteIDRenderTarget");
		}
		
		
		if( sceneData3D != null ) {
			add3DMeasuringToolSlider();
		}
		
		if( theSpriteBatch != null && sceneData3D != null) {
			// then we need a tool mode interface
			theUI.addRadioButton("3d info", 0, 430, "info tools").setSelected(true);
			theUI.addRadioButton("sprite info", 0, 460, "info tools");
		}
		
		initialised = true;
	}
	
	public static boolean isInitialised() {
		return initialised;
	}
	
	public static boolean isSpriteIDInfoInitialised() {
		if(theSpriteBatch==null) return false;
		return true;
	}
	
	public static boolean isScene3DInfoInitialised() {
		if(sceneData3D==null) return false;
		return true;
	}
	
	public static void updateToolFromUI(UIEventData uied) {
		
		if( isInitialised() == false) return;
		
		if (uied.eventIsFromWidget("3d info")) {
			toolMode = "3d info";
			
		}
		
		if (uied.eventIsFromWidget("sprite info")) {
			toolMode = "sprite info";
		}
		
		if (uied.eventIsFromWidget("ruler size slider")) {
			//System.out.println("ruler slider ");
			update3DMeasuringToolSlider(uied.sliderValue);
		}
		
	}
	
	
	
	
	
	public static void handleCanvasMouseEvent(UIEventData uied) {
		if( isInitialised() == false) return;
		
		
		if(uied.mouseEventType.contentEquals("mouseMoved")) return;
		
		if(uied.mouseEventType.contentEquals("mousePressed") || uied.mouseEventType.contentEquals("mouseDragged")) {
			
			if(toolMode.equals("3d info")) draw3DMeasuringTool(uied.docSpacePt, true, uied.mouseEventType);
			if(toolMode.equals("sprite info")) showSpriteInfo(uied.docSpacePt);
			
		}
		
		if( uied.mouseEventType.contentEquals("mouseReleased")  ) {
			theSurface.theUI.deleteCanvasOverlayShapes(overlayGroup);
		}

	}
	
	
	
	
	public static void pasteSpriteID(Sprite sprite) {
		
		if( isInitialised() == false) return;
		
		
		if(  spriteIDRenderTarget == null    ) {
			System.out.println("SceneInformationInspector:: pasteToSpriteIDRenderTarget the sprite ID render option has not been used - please re-initialise inlcuding the spritebatch ");
			return;
		}
		
		spriteIDRenderTarget.pasteSpriteIDToARGBImage(sprite);
	}
	
	
	public static void showSpriteInfo(PVector docPt) {
		///////////////////////////////////////////////////////////////////////////////////////
		// Displays sprite data information under the mouse, 
		// This requires a sriteIDRenderTarget to be created (just an ARGB image), and sprites
		// must paste their ID inot it, as they are added to the image using something like
		// theDocument.getBufferedImageRenderTarget( "spriteID" ).pasteSpriteIDToARGBImage(sprite);
		// 
		//
		//System.out.println("here 1");
		int spriteID = spriteIDRenderTarget.getSpriteID(docPt);
		//System.out.println("sprite id = " + spriteID);
		// if the docPoint is over "blank space" then a 0 is returned
		if(spriteID == 0) return;
		
		
		Sprite sprite = theSpriteBatch.getSpriteFromID(spriteID);

		String imageGroupName = sprite.ImageAssetGroupName;
		String shortImageName = sprite.ImageGroupItemShortName;
		KeyValuePairList kvpl = sprite.getSpriteData(null);
		
		//System.out.println("");
		//kvpl.printMe();
		//System.out.println("");
		
		
		
		String spriteBatchName = kvpl.getString("SpriteBatchName");
		String spriteFontName = kvpl.getString("SpriteFontName");
		
		theSurface.theUI.deleteCanvasOverlayShapes(overlayGroup);
		PVector canvasMousePoint = theSurface.theUI.docSpaceToCanvasCoord(docPt);
		
		
		///////////////////////////////////////////////////////////////////////////////////////
		// the backing rectangle
		//
		PVector canvasBackingRectTL = PVector.add(canvasMousePoint, new PVector(10,10));
		PVector canvasBackingRectBR = PVector.add(canvasBackingRectTL, new PVector(550,160));
		PVector rectTopLeft =  theSurface.theUI.canvasCoordToDocSpace(canvasBackingRectTL);
		PVector rectBottomRight = theSurface.theUI.canvasCoordToDocSpace(canvasBackingRectBR);
		//add the white background rectangle
		Color lightGray = new Color(220,220,220,170);
		theSurface.theUI.addCanvasOverlayShape_DoscSpace(overlayGroup, rectTopLeft, rectBottomRight, "rect", lightGray, lightGray, 4);
		
		
		int textSize = 15;
		float gap = theSurface.theUI.canvasUnitsToDocSpaceUnits(textSize+10);
		
		float textX = rectTopLeft.x + gap;
		float textY1 = rectTopLeft.y + (gap*1);
		float textY2 = rectTopLeft.y + (gap*2);
		float textY3 = rectTopLeft.y + (gap*3);
		float textY4 = rectTopLeft.y + (gap*4);
		float textY5 = rectTopLeft.y + (gap*5);
		float textY6 = rectTopLeft.y + (gap*6);
		
		
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY1), "SpriteID " + spriteID ,  darkBlue, textSize);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY2), "Sprite image group " + imageGroupName ,  darkBlue, textSize);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY3), "Image short name = " + shortImageName ,  darkBlue, textSize);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY4), "Sprite batch name = " + spriteBatchName ,  darkBlue, textSize);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY5), "Sprite font name = " + spriteFontName ,  darkBlue, textSize);
		
	}
	
	static void add3DMeasuringToolSlider() {
		Slider s = theSurface.theUI.addSlider("ruler size slider", 0, 260);
		TextInputBox tib = theSurface.theUI.addTextInputBox("ruler size", 0, 290, "0");
		theSurface.theUI.setText("ruler size", "0.5");
		tib.setWidgetDims(40, 30);
		update3DMeasuringToolSlider(0.5f);
	}
	
	
	static void update3DMeasuringToolSlider(float slideVal) {
		float s = slideVal*10;
		measuringToolSize = s;
		//System.out.println("MeasuringToolSize " + measuringToolSize);
		
		String rounded = MOStringUtils.roundNumber(s, 3);
		theSurface.theUI.setText("ruler size", rounded);
	}
	
	
	public static void draw3DMeasuringTool(PVector docPt, boolean visible, String mouseEventType) {
		///////////////////////////////////////////////////////////////////////////////////////
		// Displays scene data information under the mouse, and draws two measuring lines
		// The blue line indicates a set scene height (defaul 5) in the scene. The green line indicates a drag distance, and the respective (3D, doc space) lengths
		// are shown in the text box.
		//
		// Implementation note
		// some of these drawing events happen in doc Space, and are always in relation to the overall scene zoom
		// such as the measuring lines. 
		// Some things we want in a consistent "ScreenSpace", such as the text box, that are not zoomed along with the scene.
		// For these things we need to calculate (on the fly) the screen-space locations, then convert them into doc space.
		//
		// canvasSpace = theSurface.theUI.docSpaceToCanvasCoord(PVector docSpace)
		//
		// docSpace =  theSurface.theUI.appWindowCoordinateToDocSpace(canvasX, canvasY)
		//
		// converting distances between screen and doc space requires two points generated and the distance measured.
		//
		
		
		
		
		if(visible == false) {
			theSurface.theUI.deleteCanvasOverlayShapes("measuringTool");
			return;
		}
		
		
		if(mouseEventType.contentEquals("mousePressed")) {
			measuringToolStartPoint = docPt.copy();
		}
		
		

		theSurface.theUI.deleteCanvasOverlayShapes(overlayGroup);
		PVector heightIndicatorEndPt = docPt.copy();
		float worldScale = sceneData3D.get3DScale(docPt);
		PVector startPoint3D = sceneData3D.get3DSurfacePoint(measuringToolStartPoint);
		PVector currentPoint3D = sceneData3D.get3DSurfacePoint(docPt);
		
		float dragDistance3DSurface = startPoint3D.dist(currentPoint3D);
		float dragDistanceDocSpace = docPt.dist(measuringToolStartPoint);
		
		//float distance = sceneData3D_v1.getDistance(docPt);
		float depth = sceneData3D.getDepth(docPt);
		float normalisedDepth = sceneData3D.getNormalisedDepth(docPt);
		
		heightIndicatorEndPt.y -= (worldScale * measuringToolSize);
		
		//float len = docPt.dist(heightIndicatorEndPt);
		
		// the rect is to be 20 screen pixels to the right of the mouse
		PVector canvasMousePoint = theSurface.theUI.docSpaceToCanvasCoord(docPt);
		
		
		
		PVector canvasBackingRectTL = PVector.add(canvasMousePoint, new PVector(10,10));
		PVector canvasBackingRectBR = PVector.add(canvasBackingRectTL, new PVector(550,160));
		PVector rectTopLeft =  theSurface.theUI.canvasCoordToDocSpace(canvasBackingRectTL);
		PVector rectBottomRight = theSurface.theUI.canvasCoordToDocSpace(canvasBackingRectBR);
		//add the white background rectangle
		Color lightGray = new Color(220,220,220,170);
		theSurface.theUI.addCanvasOverlayShape_DoscSpace(overlayGroup, rectTopLeft, rectBottomRight, "rect", lightGray, lightGray, 4);
		
		
		
		int textSize = 15;
		
		//PVector zeroV = theSurface.theUI.canvasCoordToDocSpace(new PVector(0,0));
		//PVector dsgap = theSurface.theUI.canvasCoordToDocSpace(new PVector(textSize+10,textSize+10));
		
		//float gap = zeroV.dist(dsgap);
		float gap = theSurface.theUI.canvasUnitsToDocSpaceUnits(textSize+10);
		
		float textX = rectTopLeft.x + gap;
		float textY1 = rectTopLeft.y + (gap*1);
		float textY2 = rectTopLeft.y + (gap*2);
		float textY3 = rectTopLeft.y + (gap*3);
		float textY4 = rectTopLeft.y + (gap*4);
		float textY5 = rectTopLeft.y + (gap*5);
		float textY6 = rectTopLeft.y + (gap*6);
		
		
		theSurface.theUI.addCanvasOverlayShape_DoscSpace(overlayGroup, docPt, heightIndicatorEndPt, "line", Color.black, Color.blue, 3);
		theSurface.theUI.addCanvasOverlayShape_DoscSpace(overlayGroup, docPt, measuringToolStartPoint, "line", Color.black, Color.green, 3);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY1), "3d height line " + measuringToolSize ,  Color.red, textSize);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY2), "Depth " + depth + ".  Norm depth " + normalisedDepth,  Color.red, textSize);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY3), "3D Loc = " + currentPoint3D.toStr() + " 3D drag dist = " + dragDistance3DSurface ,  Color.red, textSize);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY4), "Doc Point = " + docPt.toStr() + " drag doc dist " + dragDistanceDocSpace,  Color.red, textSize);
		
		PVector bp = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(docPt);
		theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY5), "Buffer XY = " + bp.toStr() ,  Color.red, textSize);
		
		if( GlobalSettings.getDocument().renderTargetExists("SpriteID")) {
			
			int spriteID = GlobalSettings.getDocument().getBufferedImageRenderTarget("SpriteID").getSpriteID(docPt);
			theSurface.theUI.addCanvasOverlayText_DocSpace(overlayGroup, new PVector(textX, textY6), "SpriteID = " + spriteID,  Color.red, textSize);
		}
		
		//GlobalSettings.getDocument().getMain().drawPoint(docPt, Color.RED, 5);
		
	}
	
	
}
