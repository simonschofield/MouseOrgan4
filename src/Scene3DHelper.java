import java.awt.Color;

public class Scene3DHelper {
	static Surface theSurface = null;
	static SceneData3D sceneData3D = null;
	
	static void initialise(Surface s, SceneData3D sd3d) {
		theSurface = s;
		sceneData3D = sd3d;
	}
	
	static void randomRotateScaleSprite(ImageSprite sprite, float scaleAmt, float rotAmount) {
		QRandomStream ranStream = sprite.qRandomStream;
		float rscale = ranStream.randRangeF(1-scaleAmt,1+scaleAmt);
		rotAmount = (float) Math.toRadians((double)rotAmount);
		float rrot = ranStream.randRangeF(-rotAmount,rotAmount);
		
		if(rrot > 0) sprite.image = ImageProcessing.mirrorImage(sprite.image, false, true);
		sprite.rotate(rrot);
		sprite.scale(rscale,rscale);
	}
	
	static float addWave(ImageSprite sprite, String waveImageName, float degreesLeft, float degreesRight, float noise) {
		sceneData3D.setCurrentRenderImage(waveImageName);
		float v = sceneData3D.getCurrentRender01Value(sprite.getSeedDocPoint());
		
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
		float v = sceneData3D.getCurrentRender01Value(sprite.getSeedDocPoint());
		
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
		float dist = sceneData3D.getDistance(sprite.getSeedDocPoint());
		float val = MOMaths.norm(dist, distMinEffect, distMaxEffect);
		return MOMaths.constrain(val, 0, 1);
	}
	
	static float getRampedDistanceEffect(ImageSprite sprite, float distMinEffectNear, float distMaxEffect, float distMinEffectFar) {
		float dist = sceneData3D.getDistance(sprite.getSeedDocPoint());
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
