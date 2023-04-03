package MOAppSessionHelpers;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import MOApplication.Surface;

import MOCompositing.RenderTarget;
import MOImage.ConvolutionFilter;
import MOImage.ImageProcessing;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Plane3D;
import MOMaths.QRandomStream;
import MOMaths.Range;
import MOMaths.Ray3D;
import MOMaths.Intersection3D;
import MOMaths.Rect;
import MOMaths.SNum;
import MOMaths.Vertices2;
import MOScene3D.SceneData3D;
import MOSimpleUI.Slider;
import MOSimpleUI.TextInputBox;
//import MOSimpleUI.Slider;
//import MOSimpleUI.TextInputBox;
import MOSimpleUI.UIEventData;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;




	
public class Scene3DHelper {
		static Surface theSurface = null;
		static SceneData3D sceneData3D = null;
		
		
		
		public static void initialise(SceneData3D sd3d, Surface s) {
			theSurface = s;
			sceneData3D = sd3d;
			add3DMeasuringToolSlider();
			makeRenderImageMenu();
		}
		
		static PVector vec(float x, float y, float z) {
			return new PVector(x,y,z);
		}
		
		
		
		public static void shiftSpriteOriginBy3DYAmount(Sprite sprite, float shiftY) {
			// adjusts the anchor point Y of a sprite so that it is lifted or dropped by an amount - shiftY -  in 3D
			// Hence an image can be used to add height-detail to the sprites.
			// FYI: Just altering the anchorpoint by an amount will not do, as sprites of differing heights will have inconsistent outcomes wrt the "baseline"
			
			//
			// work out the new shifted doc point
			
			PVector docPoint = sprite.getDocPoint();
			float scl3d = sceneData3D.get3DScale(docPoint);
			float scaledShift = shiftY * scl3d;
			PVector shiftedDocPoint = new PVector(docPoint.x,docPoint.y+scaledShift);
			
			//PVector exiting3DPoint =  sceneData3D.get3DSurfacePoint(docPoint);
			//PVector displaced3DPoint = new PVector(exiting3DPoint.x, exiting3DPoint.y+shiftY,exiting3DPoint.z);
			//PVector shiftedDocPointNOROI = sceneData3D.geometryBuffer3d.world3DToDocSpace(displaced3DPoint);
			//PVector shiftedDocPoint= sceneData3D.getROILoc(shiftedDocPointNOROI);
			
			
			
			// you can do this next bit in doc space or buffer space
			// Work out the shift between the two points in buffer space
			PVector spBufferSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(docPoint);
			PVector shiftedSpBufferSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(shiftedDocPoint);
			float shiftBufferSpace = shiftedSpBufferSpace.y - spBufferSpace.y;
			
			// The shift represents a drop (or rise) below the bottom of the sprite.
			// We are going to shift the anchor point Y of the sprite so that it is at this new location.
			// To work this out, we need to know the proportion this drop represent
			// New Y = (spriteBufferHeight + dropInPixels)/spriteBufferHeight
			// So if drop == 0, the Y == 1
			// If drop > 0, the proportion will new Y will be > 1
			// if drop is < 0 (a rise) the new Y will be < 1
			float spriteheight = sprite.getImageHeight();
			
			float newY = (shiftBufferSpace+spriteheight)/spriteheight;   
			//System.out.println("sprite type " + sprite.spriteData.ImageAssetGroupName + " buffer height " + spriteheight + " new Y " + newY );
		    sprite.spriteData.origin.y = newY; 
		    
		    
		}
		
		public static void shiftSpriteOriginBy3DYAmountV2(Sprite sprite, float shiftY) {
			// adjusts the anchor point Y of a sprite so that it is lifted or dropped by an amount - shiftY -  in 3D
			// Hence an image can be used to add height-detail to the sprites.
			// FYI: Just altering the anchorpoint by an amount will not do, as sprites of differing heights will have inconsistent outcomes wrt the "baseline"
			
			//
			// work out the new shifted doc point
			
			PVector docPoint = sprite.getDocPoint();
			//float scl3d = sceneData3D.get3DScale(docPoint);
			//float scaledShift = shiftY * scl3d;
			//PVector shiftedDocPoint = new PVector(docPoint.x,docPoint.y+scaledShift);
			
			PVector exiting3DPoint =  sceneData3D.get3DSurfacePoint(docPoint);
			PVector displaced3DPoint = new PVector(exiting3DPoint.x, exiting3DPoint.y+shiftY,exiting3DPoint.z);
			PVector shiftedDocPointNOROI = sceneData3D.geometryBuffer3d.world3DToDocSpace(displaced3DPoint);
			PVector shiftedDocPoint= sceneData3D.masterDocSpaceToROIDocSpace(shiftedDocPointNOROI);
			
			
			
			// you can do this next bit in doc space or buffer space
			// Work out the shift between the two points in buffer space
			PVector spBufferSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(docPoint);
			PVector shiftedSpBufferSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(shiftedDocPoint);
			float shiftBufferSpace = shiftedSpBufferSpace.y - spBufferSpace.y;
			
			// The shift represents a drop (or rise) below the bottom of the sprite.
			// We are going to shift the anchor point Y of the sprite so that it is at this new location.
			// To work this out, we need to know the proportion this drop represent
			// New Y = (spriteBufferHeight + dropInPixels)/spriteBufferHeight
			// So if drop == 0, the Y == 1
			// If drop > 0, the proportion will new Y will be > 1
			// if drop is < 0 (a rise) the new Y will be < 1
			float spriteheight = sprite.getImageHeight();
			
			float newY = (shiftBufferSpace+spriteheight)/spriteheight;   
			//System.out.println("sprite type " + sprite.spriteData.ImageAssetGroupName + " buffer height " + spriteheight + " new Y " + newY );
		    sprite.spriteData.origin.y = newY; 
		    
		    
		}
		
		
		static float getLerpDistanceEffect(Sprite sprite, float distMinEffect, float distMaxEffect) {
			float dist = sceneData3D.getDistance(sprite.getDocPoint());
			float val = MOMaths.norm(dist, distMinEffect, distMaxEffect);
			return MOMaths.constrain(val, 0, 1);
		}
		
		static float getRampedDistanceEffect(Sprite sprite, float distMinEffectNear, float distMaxEffect, float distMinEffectFar) {
			float dist = sceneData3D.getDistance(sprite.getDocPoint());
			if(dist < distMaxEffect) {
				return getLerpDistanceEffect( sprite,  distMinEffectNear,  distMaxEffect);
			}
			
			return getLerpDistanceEffect( sprite,  distMinEffectFar,  distMaxEffect);
			
		}
		
		
		public static float addWave(Sprite sprite, String waveImageName, float amt, boolean flipInDirection) {
			
			
			
			PVector grad = sceneData3D.getCurrentRenderGradiant(sprite.getDocPoint());
			float mag = grad.mag();
			
			if(mag>0.001) {
				float rot = grad.heading();
				float scaledRot = rot*mag*amt;
				System.out.println("rotation " + scaledRot);
				if(scaledRot > 0 && flipInDirection) sprite.setImage(ImageProcessing.mirrorImage(sprite.getImage(), true, false));
				sprite.rotate((float)Math.toDegrees(scaledRot));
				return scaledRot;
			}
			return 0;
		}
		
		
		public static void addWave(Sprite sprite, float rotationDegrees,  boolean flipInDirection) {
			
			if(rotationDegrees > 0 && flipInDirection) sprite.setImage(ImageProcessing.mirrorImage(sprite.getImage(), true, false));
			sprite.rotate(rotationDegrees);
			
		}
		
		public static float addWave(Sprite sprite, String waveImageName, float degreesLeft, float degreesRight, float noise, boolean flipInDirection) {
			
			
			float rotationDegrees =  getWaveRotationDegrees( waveImageName, sprite.getDocPoint(), sprite.getRandomStream(),  degreesLeft,  degreesRight,  noise);
			
			
			if(rotationDegrees > 0 && flipInDirection) sprite.setImage(ImageProcessing.mirrorImage(sprite.getImage(), true, false));
			sprite.rotate(rotationDegrees);
			return rotationDegrees;
		}
		
		
		public static float getWaveRotationDegrees(String waveImageName, PVector docPt, QRandomStream ranStream, float degreesLeft, float degreesRight, float noise) {
			// return degrees rotation based on a wave image
			sceneData3D.setCurrentRenderImage(waveImageName);
			float v = sceneData3D.getCurrentRender01Value(docPt);
			
			
			degreesLeft = ranStream.perturbProportional(degreesLeft, noise);
			degreesRight = ranStream.perturbProportional(degreesRight, noise);
			
			float rotationDegrees = MOMaths.lerp(v,-degreesLeft,degreesRight);
			
			
			return rotationDegrees;
		}
		
		
		
		////
		static float addLighting(Sprite sprite, String lightingImage, float dark, float bright, float noise) {
			sceneData3D.setCurrentRenderImage(lightingImage);
			float v = sceneData3D.getCurrentRender01Value(sprite.getDocPoint());
			
			if(noise > 0.001) {
				
			QRandomStream ranStream = sprite.getRandomStream();
			v = ranStream.perturbProportional(v, noise);
			}
			
			float brightness = MOMaths.lerp(v, dark, bright);
			SceneHelper.addLighting( sprite,  brightness );
			return brightness;
		}
		
	
	
	public static void handleMyUIEvents(UIEventData uied) {
		
		if (uied.eventIsFromWidget("SceneData View")) {
			System.out.println("change scene view to " + uied.menuItem);
			if(uied.menuItem.contentEquals("none")) {
				theSurface.setCanvasBackgroundImage(null);
			} else {
				BufferedImage viewIm = sceneData3D.getRenderImage(uied.menuItem, true);
				theSurface.setCanvasBackgroundImage(viewIm);
			}
		}
		
		
		if (uied.eventIsFromWidget("ruler size slider")) {
			update3DMeasuringToolSlider();
		}
		

	}
	
	static void makeRenderImageMenu() {
		ArrayList<String> names = sceneData3D.getRenderImageNames();
	    String nameArray[] = new String[names.size()+1];
	    nameArray[0] = "none";
	    int i = 1;
	    for(String name: names) {
	    	nameArray[i++] = name;
	    }
	    theSurface.theUI.addMenu("SceneData View", 0, 100, nameArray);
	}
	
	
	static void add3DMeasuringToolSlider() {
		Slider s = theSurface.theUI.addSlider("ruler size slider", 0, 260);
		s.setSliderValue(0.5f);
		TextInputBox tib = theSurface.theUI.addTextInputBox("ruler size", 0, 290, "0");
		tib.setWidgetDims(40, 30);
		update3DMeasuringToolSlider();
	}
	
	
	static void update3DMeasuringToolSlider() {
		float s = theSurface.theUI.getSliderValue("ruler size slider")*10;
		String ss = "" + s;
		theSurface.theUI.setText("ruler size", ss);
	}
	
	public static void draw3DMeasuringTool(PVector docPt, boolean visible) {
		
		if(visible == false) {
			theSurface.theUI.deleteCanvasOverlayShapes("measuringTool");
			return;
		}
		
		float measuringToolSize = theSurface.theUI.getSliderValue("ruler size slider")*10; 
		
		theSurface.theUI.deleteCanvasOverlayShapes("measuringTool");
		PVector endPt = docPt.copy();
		float worldScale = sceneData3D.get3DScale(docPt);
		PVector p3d = sceneData3D.get3DSurfacePoint(docPt);
		float distance = sceneData3D.getDistance(docPt);
		float depth = sceneData3D.getDepth(docPt);
		float masterNormalisedDepth = sceneData3D.getDepthNormalised(docPt);
		//Range normalisedDepthExtrema = sceneData3D.getROIDepthExtrema(false);
		//float roiNormalisedDepth = normalisedDepthExtrema.norm(masterNormalisedDepth);
		
		float roiNormalisedDepth = sceneData3D.getROINormalisedDepth(docPt);
		
		float textX = endPt.x;
		endPt.y -= (worldScale * measuringToolSize);
		float textY1 = endPt.y + (0.1f);
		float textY2 = endPt.y + (0.12f);
		float len = docPt.dist(endPt);
		// System.out.println("roi depth extrema = " + normalisedDepthExtrema.toStr() + " masterNormalisedDepth " + masterNormalisedDepth + " ROI normalised depth " + roiNormalisedDepth);
		// theUI.addCanvasOverlayShape("mouseDot", uied.docSpacePt, radiusOffset, "ellipse", new Color(127, 0, 0, 255), Color.gray, 1);
		
		theSurface.theUI.addCanvasOverlayShape("measuringTool", docPt, endPt, "line", Color.black, Color.blue, 4);
		theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY1), "  distance = " + distance + " depth " + depth + " master norm depth = " + masterNormalisedDepth,  Color.red, 20);
		theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY2), "  p3d = " + p3d.toStr() + " roi norm depth " +  roiNormalisedDepth,  Color.red, 20);
		//theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY2), ,  Color.blue, 20);
		//theSurface.theUI.addCanvasOverlayText("measuringTool", endPt, "  Stick Hght = " + measuringToolSize,  Color.blue, 20);
		
	}
	
	
	public static void print3DSceneData(PVector docPt) {
		float worldScale = sceneData3D.get3DScale(docPt);
		float distance = sceneData3D.getDistance(docPt);
		
		float normalisedDepth = sceneData3D.getDepthNormalised(docPt);
		
		System.out.println("3DSceneData at:" + docPt.toStr() + " world scale:" + worldScale + " Distance:" + distance +  " Normalised depth:" + normalisedDepth);     
	}
	
}
