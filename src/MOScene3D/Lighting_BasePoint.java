package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOImage.ImageDimensions;
import MOImage.MOPackedColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.Progress;

public class Lighting_BasePoint extends Lighting_CommonUtils{

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// renderShadow is called immediately after a sprite is pasted. In order to use this class, you first need to add a
	// FloatRendertarget and a greyscale render target sIt works out the shadow cast by that sprite on all sprites previously pasted
	// using a kind of ray tracing. It iterates over the scene already pasted, and added to the depthRender, and for each pixel works out
	// if a ray cast from that pixel towards the light is obscured by the new sprite being pasted. If so draw shadow in the shadowRender at that point.
	// The light is assumed to be a parallel source.
	// There are a lot of optimisations here


	Range basePointValueRange;

	public Lighting_BasePoint(SceneData3D scene3D,  String nameOfShadowRender, float minBasePointValue, float maxBasePointValue){
		super(scene3D, nameOfShadowRender);

		Range worldY = sceneData3D.depthBuffer3d.worldYExtrema;
		System.out.println("worldY extrama " + worldY.toStr() );
		float sceneYMin = sceneData3D.depthBuffer3d.worldYExtrema.getUpper();

		basePointValueRange = new Range(minBasePointValue,maxBasePointValue);

		coordinateSystem = GlobalSettings.getDocument().getCoordinateSystem();

		progress = new Progress("Shadows: ");
	}



	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// original lighting based on a single point sample at the base of the sprite, with the option to
	// include a 3D ramp from black at the base, to the base-point colour (tone) at the top of the ramp (and beyond)
	// If the rampBaseHeight and rampTopHeight are both set to 0, then no ramp is added so the base-point tone is applied over the whole sprite.
	// The ramp is measured and calculated in 3D height units, measured from the base of the sprite, as taller sprites would receive more light, or the light would be on a roughly consistent height level
	//
	// sprite
	//
	// endBaseShadingHeight - height of span from 0 (black) -> endBaseShadeHeight (base_point_sample_tone)
	// startTipLigtheningHeight - height of span from base_point_sample_tone (startTipLigtheningHeight) -> tipPoint (tipShade)
	// tipBrightening (0..1) - tipShade = lerp(tipBrightening, base_point_sample_tone, white )
	//
	//

	public void basePointLighting(Sprite sprite, String projectLightTextureName,  float baseRampEndHeight, float tipRampStart, float tipRampEnd, float tipBrighteningAmount, float stochasticAmount) {
		debugFlag=false;
		progress.update();

		// dont need z render depth texture for this algorithm - just usesd the scenedata

		// set the projected shadow render
		sceneData3D.setCurrentRenderImage(projectLightTextureName);



		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.BLACK);


		PVector spriteDocPt = sprite.getDocPoint();
		float spriteDepth = sprite.getDepth();

		float basePointBrightness = sceneData3D.getCurrentRender01Value(spriteDocPt);
		basePointBrightness = basePointValueRange.lerp(basePointBrightness);

		//System.out.println("basePointBrightness "+ basePointBrightness );


		PVector basePoint3D = 	sceneData3D.get3DSurfacePoint(spriteDocPt);

		if(this.debugFlag) {
			System.out.println("Sprite ");
		}

		// the stochastic change in ramp values must be calculated once at the start, and is a scaling in the range
		// - stochasticAmount, + stochasticAmount

		float thisStochasticAmount = sprite.getRandomStream().keyedFloat(sprite.randomKey, -stochasticAmount, stochasticAmount);


		BufferedImage spriteImage = sprite.getMainImage();
		int spriteBufferW = spriteImage.getWidth();
		int spriteBufferH = spriteImage.getHeight();
		Rect spriteBoundingRectBufferSpace = sprite.getDocumentBufferSpaceRect();
		ImageDimensions spriteImageDimensions = new ImageDimensions(spriteBufferW,spriteBufferH);

		// we do it bottom to top, so as to trap the bright point going upwards
		for (int y = (int) spriteBoundingRectBufferSpace.bottom; y >= spriteBoundingRectBufferSpace.top; y--) {


			// work out the doc space location of the pixel above the basepoint, in docSpace
			PVector docSpaceOfY = BStoDS(0,y);
			PVector aboveBasePointAtThisY = new PVector(spriteDocPt.x, docSpaceOfY.y);


			// convert this into a 3D point (at the sprite's depth)
			PVector y3D  = sceneData3D.get3DVolumePoint(aboveBasePointAtThisY, spriteDepth);


			// measure this distance between the basePoint, and this y3D point
			// This gives oyu the height of this row of ixels above the
			// base point in 3D units
			float thisYHeight3D = y3D.dist(basePoint3D);




			float rampValue = getRampValue(thisYHeight3D,  basePointBrightness, baseRampEndHeight,  tipRampStart,  tipRampEnd,  tipBrighteningAmount,  thisStochasticAmount);







			int rampedvaluei = (int)(rampValue*255);

			for (int x = (int) spriteBoundingRectBufferSpace.left; x <= spriteBoundingRectBufferSpace.right; x++) {

				if( !shadowRenderTarget.getCoordinateSystem().isInsideBufferSpace(x, y)) {
					continue;
				}




				int pixelLocationInSpriteImageX = x - (int)spriteBoundingRectBufferSpace.left;
				int pixelLocationInSpriteImageY = y - (int)spriteBoundingRectBufferSpace.top;

				if( !spriteImageDimensions.isLegalIndex(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY) ) {
					continue;
				}

				int spriteRGBA = spriteImage.getRGB(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY);
				int alpha = MOPackedColor.getAlpha(spriteRGBA);
				if(alpha == 0) {
					continue;
				}
				int existingValue = shadowImageGetSet.getPixel(x, y);
				int blendedValue = alphaBlend(existingValue, rampedvaluei,  alpha);
				shadowImageGetSet.setPixel(x, y, blendedValue);

			}// for X
		}// for Y






	}


	float getRampValue(float thisHeight, float basePointBrightness, float baseRampEndHeight, float tipRampStart, float tipRampEnd, float tipBrighteningAmount, float stochasticAmount) {
		float shadowVal = this.basePointValueRange.getLower();
		if( baseRampEndHeight == 0) {
			return basePointBrightness;
		}

		if( MOMaths.isBetweenInc(thisHeight, 0, baseRampEndHeight)) {


			return MOMaths.mapClamped(thisHeight, 0, baseRampEndHeight, shadowVal, basePointBrightness);

		}

		if( MOMaths.isBetweenInc(thisHeight, baseRampEndHeight, tipRampStart)) {

			return basePointBrightness;

		}


		float maxBrightness = MOMaths.constrain(basePointBrightness*tipBrighteningAmount, 0f, 1f);

		if( MOMaths.isBetweenInc(thisHeight, tipRampStart, tipRampEnd) ) {




			return MOMaths.mapClamped(thisHeight, tipRampStart, tipRampEnd, basePointBrightness, maxBrightness);

		}


		// if you get here then the height is over the tip so return basePointBrightness*tipBrighteningAmount
		return maxBrightness;

	}

	int alphaBlend(int oldVal, int newVal, int alpha) {
		float af = alpha*0.003922f;
		return (int)MOMaths.lerp(af, oldVal, newVal);

	}

	//PVector BStoDS(int x, int y) {
	//	return GlobalSettings.getDocument().getCoordinateSystem().bufferSpaceToDocSpace(x,y);
	///}


	//PVector BStoDS(PVector buffPt) {
	//	return GlobalSettings.getDocument().getCoordinateSystem().bufferSpaceToDocSpace(buffPt);
	//}


}
