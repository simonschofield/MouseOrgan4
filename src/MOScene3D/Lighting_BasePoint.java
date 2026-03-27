package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

import MOImage.ByteImageGetterSetter;
import MOImage.ImageDimensions;
import MOImage.MOPackedColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.Progress;

/**
 * Lighting_BasePoint uses the tone sampled at the base of the pated sprite to give it an overall tone for each sprite. The render is created in a separate 8-bit greyscale render target. 
 * There is also the facility to add a "ramped" effect. This is used to add darkness around the base and add a lightened tip.
 * 
 */
public class Lighting_BasePoint extends Lighting_CommonUtils{

	String lightingTextureName;
	ToneRamp toneRamp;
	/**
	 * @param nameOfShadowRender - The name of the new render created to store the lighting image
	 * @param lightingSourceTextureName - the texture name in the SceneData3D for the lighting calculation
	 */
	public Lighting_BasePoint(String nameOfShadowRender, String lightingSourceTextureName){
		super(nameOfShadowRender);
		lightingTextureName = lightingSourceTextureName;
		
		Range worldY = sceneData3D.depthBuffer3d.worldYExtrema;
		//System.out.println("worldY extrama " + worldY.toStr() );
		float sceneYMin = sceneData3D.depthBuffer3d.worldYExtrema.getUpper();

		coordinateSystem = GlobalSettings.getDocument().getCoordinateSystem();
		
		shadowRenderTarget.fillBackground(Color.BLACK);
	}



	
	/**
	 * A "user friendly" method for specifying the tone ramp required for this BasePointLighting. Used by grasses etc. The ramp is usually darkening at the base (so adding black), with a alpha decreasing to zero as it 
	 * progresses up towards the top of the ramp section. Then there is usually an unaffected middle bit (between the baseTopControlPointHeight and the tipBottomControlPointHeight). Then the tip starts adding white at
	 * tipBottomControlPointHeight with the alpha value decreasing to zero at tipTopControlPointHeight
	 * @param baseTopControlPointHeight - the height in world 3D units - the same as sizeInScene of the sprite - of the top of the base part of the ramp where the black mix fades to zero
	 * @param baseBottomAlphaValue - The alpha value (or amount of black) mixed in at the base of the sprite
	 * @param tipBottomControlPointHeight - the height in world 3D units - the same as sizeInScene of the sprite
	 * @param tipTopControlPointHeight - the height in world 3D units - the same as sizeInScene of the sprite
	 * @param tipTopAlphaValue - The maximum amount of white mixed in at the tip region of the sprite
	 */
	public void setToneRamp(float baseTopControlPointHeight, float baseBottomAlphaValue,  float tipBottomControlPointHeight,  float tipTopControlPointHeight, float tipTopAlphaValue) {
		
		toneRamp = new ToneRamp();
		// the base has full base tone + alpha
		toneRamp.addControlPoint(0, 0, baseBottomAlphaValue);
		
		// this fades to nothing by baseTopControlPoint
		toneRamp.addControlPoint(baseTopControlPointHeight, 0, 0);
		
		
		// the mid ramp has nothing to contribute, so between the previous and this no alpha
		// this then defines the start of the tip
		toneRamp.addControlPoint(tipBottomControlPointHeight, 1, 0);
		
		
		// this defines the end of the tip ramp. Anything beyond this is constant
		toneRamp.addControlPoint(tipTopControlPointHeight, 1, tipTopAlphaValue);
	}
	
	
	/**
	 * Once the base point lighting has beens set up (via the constructor for the class, and optional use of setToneRamp(..)), the lighting calculation for each sprite is added immediately to the output lighting RenderTarget when
	 * this method is called (there is no deferred process)
	 * @param sprite - the sprite being added to the scene
	 */
	public void pasteLighting(Sprite sprite) {
		debugFlag=false;
		//progress.update();

		// dont need z render depth texture for this algorithm - just usesd the scenedata

		// set the projected shadow render
		sceneData3D.setCurrentRenderImage(lightingTextureName);



		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.BLACK);


		PVector spriteDocPt = sprite.getDocPoint();
		float spriteDepth = sprite.getDepth();

		float basePointBrightness = sceneData3D.getCurrentRender01Value(spriteDocPt);
		

		//System.out.println("basePointBrightness "+ basePointBrightness );


		PVector basePoint3D = 	sceneData3D.get3DSurfacePoint(spriteDocPt);

		if(this.debugFlag) {
			System.out.println("Sprite ");
		}

		// the stochastic change in ramp values must be calculated once at the start, and is a scaling in the range
		// - stochasticAmount, + stochasticAmount


		BufferedImage spriteImage = sprite.getCurrentImage();
		int spriteBufferW = spriteImage.getWidth();
		int spriteBufferH = spriteImage.getHeight();
		Rect spriteBoundingRectBufferSpace = sprite.getDocumentBufferSpaceRect();
		ImageDimensions spriteImageDimensions = new ImageDimensions(spriteBufferW,spriteBufferH);

		// we do it bottom to top, so as to trap the bright point going upwards
		for(int y = (int) spriteBoundingRectBufferSpace.bottom; y >= spriteBoundingRectBufferSpace.top; y--) {


			//work out the doc space location of the pixel above the basepoint, in docSpace
			PVector docSpaceOfY = BStoDS(0,y);
			PVector aboveBasePointAtThisY = new PVector(spriteDocPt.x, docSpaceOfY.y);


			// convert this into a 3D point (at the sprite's depth)
			PVector y3D  = sceneData3D.get3DVolumePoint(aboveBasePointAtThisY, spriteDepth);


			// measure this distance between the basePoint, and this y3D point
			// This gives oyu the height of this row of ixels above the
			// base point in 3D units
			
			float thisYHeight3D = y3D.dist(basePoint3D);
			
			int rampedvalue;
			
			if(toneRamp!=null) {
				rampedvalue = (int)  (toneRamp.modifyTone(thisYHeight3D, basePointBrightness) * 255);
			} else {
				rampedvalue = (int)  (basePointBrightness * 255);
			}

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
				int blendedValue = alphaBlend(existingValue, rampedvalue,  alpha);
				shadowImageGetSet.setPixel(x, y, blendedValue);

			}// for X
		}// for Y

	}

	private int alphaBlend(int oldVal, int newVal, int alpha) {
		float af = alpha*0.003922f;
		return (int)MOMaths.lerp(af, oldVal, newVal);

	}
	
//	/**
//	 * OLD METHOD - probably can be deleted
//	* Lighting_BasePoint uses the tone sampled at the base of the pated sprite to give it an overall tone for each sprite. The render is created in a separate 8-bit greyscale render target. 
//    * There is also the facility to add a "ramped" effect. This is used to add darkness around the base and add a lightened tip.<p>
//    * 
//    * Ramp effect measurements are in real-scene sprite height dimensions, so short sprites may never achieve their tip values, which seems to match real-life.
//	* If the rampBaseHeight and rampTopHeight are both set to 0, then no ramp is added so the base-point tone is applied over the whole sprite.
//	* The ramp is measured and calculated in 3D height units, measured from the base of the sprite, as taller sprites would receive more light, or the light would be on a roughly consistent height level
//	*
//	* @param sprite - the sprite being pasted and "lit"
//	
//	* @param baseRampEndHeight - height of span from 0 (black) -> endBaseShadeHeight (base_point_sample_tone)
//	* @param tipRampStart - height of span from base_point_sample_tone (startTipLigtheningHeight) -> tipPoint (tipShade)
//	* @param tipRampEnd - the height at which tip-brightening ends
//	* @param tipBrighteningAmount
//	* @param stochasticAmount
//	*/
//	
//	
//	
//	
//	public void basePointLighting(Sprite sprite,  float baseRampEndHeight, float tipRampStart, float tipRampEnd, float tipBrighteningAmount, float stochasticAmount) {
//		debugFlag=false;
//		//progress.update();
//
//		// dont need z render depth texture for this algorithm - just usesd the scenedata
//
//		// set the projected shadow render
//		sceneData3D.setCurrentRenderImage(lightingTextureName);
//
//
//
//		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.BLACK);
//
//
//		PVector spriteDocPt = sprite.getDocPoint();
//		float spriteDepth = sprite.getDepth();
//
//		float basePointBrightness = sceneData3D.getCurrentRender01Value(spriteDocPt);
//		
//
//		//System.out.println("basePointBrightness "+ basePointBrightness );
//
//
//		PVector basePoint3D = 	sceneData3D.get3DSurfacePoint(spriteDocPt);
//
//		if(this.debugFlag) {
//			System.out.println("Sprite ");
//		}
//
//		// the stochastic change in ramp values must be calculated once at the start, and is a scaling in the range
//		// - stochasticAmount, + stochasticAmount
//
//		float thisStochasticAmount = sprite.getRandomStream().keyedFloat(sprite.randomKey, -stochasticAmount, stochasticAmount);
//
//
//		BufferedImage spriteImage = sprite.getCurrentImage();
//		int spriteBufferW = spriteImage.getWidth();
//		int spriteBufferH = spriteImage.getHeight();
//		Rect spriteBoundingRectBufferSpace = sprite.getDocumentBufferSpaceRect();
//		ImageDimensions spriteImageDimensions = new ImageDimensions(spriteBufferW,spriteBufferH);
//
//		// we do it bottom to top, so as to trap the bright point going upwards
//		for (int y = (int) spriteBoundingRectBufferSpace.bottom; y >= spriteBoundingRectBufferSpace.top; y--) {
//
//
//			// work out the doc space location of the pixel above the basepoint, in docSpace
//			PVector docSpaceOfY = BStoDS(0,y);
//			PVector aboveBasePointAtThisY = new PVector(spriteDocPt.x, docSpaceOfY.y);
//
//
//			// convert this into a 3D point (at the sprite's depth)
//			PVector y3D  = sceneData3D.get3DVolumePoint(aboveBasePointAtThisY, spriteDepth);
//
//
//			// measure this distance between the basePoint, and this y3D point
//			// This gives oyu the height of this row of ixels above the
//			// base point in 3D units
//			float thisYHeight3D = y3D.dist(basePoint3D);
//
//
//
//
//			float rampValue = getRampValue(thisYHeight3D,  basePointBrightness, baseRampEndHeight,  tipRampStart,  tipRampEnd,  tipBrighteningAmount,  thisStochasticAmount);
//
//
//
//
//
//
//
//			int rampedvaluei = (int)(rampValue*255);
//
//			for (int x = (int) spriteBoundingRectBufferSpace.left; x <= spriteBoundingRectBufferSpace.right; x++) {
//
//				if( !shadowRenderTarget.getCoordinateSystem().isInsideBufferSpace(x, y)) {
//					continue;
//				}
//
//
//
//
//				int pixelLocationInSpriteImageX = x - (int)spriteBoundingRectBufferSpace.left;
//				int pixelLocationInSpriteImageY = y - (int)spriteBoundingRectBufferSpace.top;
//
//				if( !spriteImageDimensions.isLegalIndex(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY) ) {
//					continue;
//				}
//
//				int spriteRGBA = spriteImage.getRGB(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY);
//				int alpha = MOPackedColor.getAlpha(spriteRGBA);
//				if(alpha == 0) {
//					continue;
//				}
//				int existingValue = shadowImageGetSet.getPixel(x, y);
//				int blendedValue = alphaBlend(existingValue, rampedvaluei,  alpha);
//				shadowImageGetSet.setPixel(x, y, blendedValue);
//
//			}// for X
//		}// for Y
//
//
//
//
//
//
//	}
//	
//	float getRampValue(float thisHeight, float basePointBrightness, float baseRampEndHeight, float tipRampStart, float tipRampEnd, float tipBrighteningAmount, float stochasticAmount) {
//		float shadowVal = 0;
//		if( baseRampEndHeight == 0) {
//			return basePointBrightness;
//		}
//
//		if( MOMaths.isBetweenInc(thisHeight, 0, baseRampEndHeight)) {
//
//
//			return MOMaths.mapClamped(thisHeight, 0, baseRampEndHeight, shadowVal, basePointBrightness);
//
//		}
//
//		if( MOMaths.isBetweenInc(thisHeight, baseRampEndHeight, tipRampStart)) {
//
//			return basePointBrightness;
//
//		}
//
//
//		float maxBrightness = MOMaths.constrain(basePointBrightness*tipBrighteningAmount, 0f, 1f);
//
//		if( MOMaths.isBetweenInc(thisHeight, tipRampStart, tipRampEnd) ) {
//
//
//
//
//			return MOMaths.mapClamped(thisHeight, tipRampStart, tipRampEnd, basePointBrightness, maxBrightness);
//
//		}
//
//
//		// if you get here then the height is over the tip so return basePointBrightness*tipBrighteningAmount
//		return maxBrightness;
//
//	}
//	

	

}






