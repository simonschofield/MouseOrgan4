import java.awt.Color;
import java.awt.image.BufferedImage;

public class Scene3DHelper {
	static Surface theSurface = null;
	static SceneData3D sceneData3D = null;
	
	static void initialise(Surface s, SceneData3D sd3d) {
		theSurface = s;
		sceneData3D = sd3d;
		sceneData3D.makeRenderImageMenu(theSurface.getSimpleUI(), 0, 100);
	}
	
	static void randomRotateScaleSprite(ImageSprite sprite, float scaleAmt, float rotAmount) {
		randomRotateScaleSprite( sprite,  scaleAmt,  rotAmount, true);
	}
	
	static void randomRotateScaleSprite(ImageSprite sprite, float scaleAmt, float rotAmount, boolean flipInRotationDirection) {
		QRandomStream ranStream = sprite.qRandomStream;
		float rscale = ranStream.randRangeF(1-scaleAmt,1+scaleAmt);
		
		float rrot = ranStream.randRangeF(-rotAmount,rotAmount);
		
		if(flipInRotationDirection && rrot > 0) sprite.mirrorSprite(true);
		sprite.rotate(rrot);
		sprite.scale(rscale,rscale);
	}
	
	static void randomMirrorSprite(ImageSprite sprite, boolean inX, boolean inY) {
		QRandomStream ranStream = sprite.qRandomStream;
		boolean coinTossX = ranStream.randomEvent(0.5f);
		boolean coinTossY = ranStream.randomEvent(0.5f);
		if(coinTossX && inX) {
			sprite.mirrorSprite(true);
		}
		if(coinTossY && inY) {
			sprite.mirrorSprite(false);
		}
		
		
	}
	
	static float addWave(ImageSprite sprite, String waveImageName, float degreesLeft, float degreesRight, float noise) {
		sceneData3D.setCurrentRenderImage(waveImageName);
		float v = sceneData3D.getCurrentRender01Value(sprite.getDocPoint());
		
		QRandomStream ranStream = sprite.qRandomStream;
		degreesLeft = ranStream.perturb(degreesLeft, noise);
		degreesRight = ranStream.perturb(degreesRight, noise);
		
		float rotationDegrees = MOMaths.lerp(v,-degreesLeft,degreesRight);
		
		//System.out.println("rotation " + v + " " + rotationDegrees);
		if(rotationDegrees > 0) sprite.image = ImageProcessing.mirrorImage(sprite.image, false, true);
		sprite.rotate(rotationDegrees);
		return rotationDegrees;
	}
	
	////
	static float addLighting(ImageSprite sprite, String lightingImage, float dark, float bright, float noise) {
		sceneData3D.setCurrentRenderImage(lightingImage);
		float v = sceneData3D.getCurrentRender01Value(sprite.getDocPoint());
		
		if(noise > 0.001) {
			
		QRandomStream ranStream = sprite.qRandomStream;
		v = ranStream.perturb(v, noise);
		}
		
		float brightness = MOMaths.lerp(v, dark, bright);
		addLighting( sprite,  brightness );
		return brightness;
	}
	
	static void addLighting(ImageSprite sprite, float brightness) {
		// when the brightness is low, so is the contrast
		//sprite.image = ImageProcessing.adjustContrast(sprite.image, brightness);
		
		sprite.image = ImageProcessing.adjustBrightness(sprite.image, brightness);
		//sprite.image = ImageProcessing.adjustBrightnessNoClip(sprite.image, brightness);

	}
	
	static void addContrast(ImageSprite sprite, float contrast) {
		// when the brightness is low, so is the contrast
		sprite.image = ImageProcessing.adjustContrast(sprite.image, contrast);
		
		
	}
	
	static void addRandomHSV(ImageSprite sprite, float rH, float rS, float rV) {
		QRandomStream ranStream = sprite.qRandomStream;
		float randH = ranStream.randRangeF(-rH, rH);
		float randS = ranStream.randRangeF(-rS, rS);
		float randV = ranStream.randRangeF(-rV, rV);
		
		
		sprite.image = ImageProcessing.adjustHSV(sprite.image, randH, randS, randV);
	}
	
	static void addClump(ImageSprite sprite, float scaleAmt, float rotAmount){
		
		
	}
	
	
	static float getLerpDistanceEffect(ImageSprite sprite, float distMinEffect, float distMaxEffect) {
		float dist = sceneData3D.getDistance(sprite.getDocPoint());
		float val = MOMaths.norm(dist, distMinEffect, distMaxEffect);
		return MOMaths.constrain(val, 0, 1);
	}
	
	static float getRampedDistanceEffect(ImageSprite sprite, float distMinEffectNear, float distMaxEffect, float distMinEffectFar) {
		float dist = sceneData3D.getDistance(sprite.getDocPoint());
		if(dist < distMaxEffect) {
			return getLerpDistanceEffect( sprite,  distMinEffectNear,  distMaxEffect);
		}
		
		return getLerpDistanceEffect( sprite,  distMinEffectFar,  distMaxEffect);
		
	}
	
	
	ImageSprite createImageSprite(PVector docPos, String imageContentGroupName, ContentGroupManager contentManager) {
		ContentItemDescription cis = new ContentItemDescription(imageContentGroupName,1);
		
		Seed s = new  Seed(docPos,cis);
		s.batchName = "";
		
		ImageSprite sprite =  contentManager.getSprite(s);
		sprite.scale(0.5f, 0.5f);
		return sprite;
	}
	
	
	static void handle3DRenderImageMenuCall(UIEventData uied) {
		
		if (uied.eventIsFromWidget("SceneData View")) {
			System.out.println("change scene view to " + uied.menuItem);
			if(uied.menuItem.contentEquals("none")) {
				theSurface.setAlternateView(null);
			} else {
				BufferedImage viewIm = sceneData3D.getRenderImageROI(uied.menuItem);
				theSurface.setAlternateView(viewIm);
			}
		}

	}
	
	static void draw3DMeasuringTool(PVector docPt, boolean visible) {
		
		if(visible == false) {
			theSurface.theUI.deleteCanvasOverlayShapes("measuringTool");
			return;
		}
		
		float measuringToolSize = theSurface.theUI.getSliderValue("ruler size slider")*10; 
		
		theSurface.theUI.deleteCanvasOverlayShapes("measuringTool");
		PVector endPt = docPt.copy();
		float worldScale = sceneData3D.get3DScale(docPt);
		float depth = sceneData3D.getDepth(docPt);
		float normalisedDepth = sceneData3D.getDepthNormalised(docPt);
		float bothMult = worldScale * normalisedDepth;
		endPt.y -= (worldScale * measuringToolSize);
		theSurface.theUI.addCanvasOverlayShape("measuringTool", docPt, endPt, "line", Color.black, Color.blue, 4);
		theSurface.theUI.addCanvasOverlayText("measuringTool", docPt, "  Normalized Depth = " + normalisedDepth,  Color.blue, 10);
		theSurface.theUI.addCanvasOverlayText("measuringTool", endPt, "  Stick Hght = " + measuringToolSize,  Color.blue, 10);
		// System.out.println("World 3d scaler " + worldScale + " depth " + depth + "
		// both mult = " + bothMult);
		// System.out.println("depth*normalisedDepth " + depth/normalisedDepth);
	}
	
	
	
	
}
