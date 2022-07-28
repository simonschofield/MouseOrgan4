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
				if(scaledRot > 0 && flipInDirection) sprite.setImage(ImageProcessing.mirrorImage(sprite.getImage(), true, false));
				sprite.rotate((float)Math.toDegrees(scaledRot));
				return scaledRot;
			}
			return 0;
		}
		
		static float addWave(Sprite sprite, String waveImageName, float degreesLeft, float degreesRight, float noise) {
			sceneData3D.setCurrentRenderImage(waveImageName);
			float v = sceneData3D.getCurrentRender01Value(sprite.getDocPoint());
			
			QRandomStream ranStream = sprite.getRandomStream();
			degreesLeft = ranStream.perturb(degreesLeft, noise);
			degreesRight = ranStream.perturb(degreesRight, noise);
			
			float rotationDegrees = MOMaths.lerp(v,-degreesLeft,degreesRight);
			
			//System.out.println("rotation " + v + " " + rotationDegrees);
			if(rotationDegrees > 0) sprite.setImage(ImageProcessing.mirrorImage(sprite.getImage(), true, false));
			sprite.rotate(rotationDegrees);
			return rotationDegrees;
		}
		
		////
		static float addLighting(Sprite sprite, String lightingImage, float dark, float bright, float noise) {
			sceneData3D.setCurrentRenderImage(lightingImage);
			float v = sceneData3D.getCurrentRender01Value(sprite.getDocPoint());
			
			if(noise > 0.001) {
				
			QRandomStream ranStream = sprite.getRandomStream();
			v = ranStream.perturb(v, noise);
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
		float normalisedDepth = sceneData3D.getDepthNormalised(docPt);
		
		float textX = endPt.x;
		endPt.y -= (worldScale * measuringToolSize);
		float textY1 = endPt.y + (0.1f);
		float textY2 = endPt.y + (0.12f);
		float len = docPt.dist(endPt);
		//System.out.println("measure: doc pt " + docPt.toStr() + " end pt "  + endPt.toStr() + " len " + len + " world scale " + worldScale);
		// theUI.addCanvasOverlayShape("mouseDot", uied.docSpacePt, radiusOffset, "ellipse", new Color(127, 0, 0, 255), Color.gray, 1);
		
		theSurface.theUI.addCanvasOverlayShape("measuringTool", docPt, endPt, "line", Color.black, Color.blue, 4);
		theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY1), "  distance = " + distance + " depth " + depth + "  norm depth = " + normalisedDepth,  Color.red, 20);
		theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY2), "  p3d = " + p3d.toStr(),  Color.red, 20);
		//theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY2), ,  Color.blue, 20);
		//theSurface.theUI.addCanvasOverlayText("measuringTool", endPt, "  Stick Hght = " + measuringToolSize,  Color.blue, 20);
		
	}
	
	
	public static void print3DSceneData(PVector docPt) {
		float worldScale = sceneData3D.get3DScale(docPt);
		float distance = sceneData3D.getDistance(docPt);
		float unfilteredDistance = sceneData3D.geometryBuffer3d.getUnfilteredDistance(docPt);
		float normalisedDepth = sceneData3D.getDepthNormalised(docPt);
		
		System.out.println("3DSceneData at:" + docPt.toStr() + " world scale:" + worldScale + " Distance:" + distance + " Unfiltered distance:" + unfilteredDistance + " Normalised depth:" + normalisedDepth);     
	}
	
}
