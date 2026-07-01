package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

import MOImage.BilinearBufferedImageSampler;
import MOImage.ByteImageGetterSetter;
import MOImage.ImageDimensions;
import MOImage.ImageProcessing;
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
	float[] lightingSourceTextureLevelMapping = {0,1f,255,0,255};
	
	BilinearBufferedImageSampler lightingSourceTextureSampler;
	
	
	/**
	 * @param nameOfShadowRender - The name of the new render created to store the lighting image
	 * @param lightingSourceTextureName - the texture name in the SceneData3D for the lighting calculation
	 */
	public Lighting_BasePoint(String nameOfShadowRender, String lightingSourceTextureName){
		super(nameOfShadowRender, BufferedImage.TYPE_BYTE_GRAY);
		
		init(lightingSourceTextureName);
	}
	
	
	
	/**
	 * @param nameOfShadowRender - The name of the new render created to store the lighting image
	 * @param lightingSourceTextureName - the texture name in the SceneData3D for the lighting calculation
	 */
	public Lighting_BasePoint(String nameOfShadowRender, String lightingSourceTextureName, int renderImageType){
		super(nameOfShadowRender, renderImageType);
		
		init(lightingSourceTextureName);
	}
	
	
	private void init(String lightingSourceTextureName) {
		
		lightingTextureName = lightingSourceTextureName;
		
		Range worldY = sceneData3D.depthBuffer3d.worldYExtrema;
		//System.out.println("worldY extrama " + worldY.toStr() );
		float sceneYMin = sceneData3D.depthBuffer3d.worldYExtrema.getUpper();

		coordinateSystem = GlobalSettings.getDocument().getCoordinateSystem();
		
		shadowRenderTarget.fillBackground(Color.BLACK);
		
		
		BufferedImage lightingTextureImage = sceneData3D.getRenderImage(lightingSourceTextureName, false);
		
		BufferedImage lightingTextureImageGrey = ImageProcessing.assertImageTYPE_BYTE_GRAY(lightingTextureImage);
		
		
		lightingSourceTextureSampler = new BilinearBufferedImageSampler(lightingTextureImageGrey);
	}

	/**
	 * Stores a levels-type mapping to be applied to all values from the input lightingTexture
	 * All input value ranges are all 0..255 except for midtoneGamma, which is 0.01...10 s
	 * @param shadowVal - in the range 0..255
	 * @param midtoneGamma - In the range 0.001-10.0, where 1 is "no effect".
	 * @param highlightVal - in the range 0..255
	 * @param outShadowVal - in the range 0..255
	 * @param outHighlightVal - in the range 0..255
	 */
	public void setLightingImageLevelAdjustment(float shadowVal, float midtoneGamma, float highlightVal, float outShadowVal,  float outHighlightVal) {
		
		lightingSourceTextureLevelMapping[0] = shadowVal;
		lightingSourceTextureLevelMapping[1] = midtoneGamma;
		lightingSourceTextureLevelMapping[2] = highlightVal;
		lightingSourceTextureLevelMapping[3] = outShadowVal;
		lightingSourceTextureLevelMapping[4] = outHighlightVal;
		
	}
	
	public void setLightingImageLevelAdjustment(float[] levels) {
		if(levels == null) return;
		
		lightingSourceTextureLevelMapping[0] = levels[0];
		lightingSourceTextureLevelMapping[1] = levels[1];
		lightingSourceTextureLevelMapping[2] = levels[2];
		if(levels.length == 3) return;
		lightingSourceTextureLevelMapping[3] = levels[3];
		lightingSourceTextureLevelMapping[4] = levels[4];
		
	}
	
	
	/**
	 * A "user friendly" method for specifying the tone ramp required for this BasePointLighting. Used by grasses etc. The ramp is usually darkening at the base (so adding black), with a alpha decreasing to zero as it 
	 * progresses up towards the top of the ramp section. Then there is usually an unaffected middle bit (between the baseTopControlPointHeight and the tipBottomControlPointHeight). Then the tip starts adding white at
	 * tipBottomControlPointHeight with the alpha value decreasing to zero at tipTopControlPointHeight
	 * @param baseTopControlPointHeight - the height in world 3D units of the top of the base part of the ramp where the black mix fades to zero
	 * @param baseBottomAlphaValue - The alpha value (or amount of black) mixed in at the base of the sprite
	 * @param tipBottomControlPointHeight - the height in world 3D units - the same as sizeInScene of the sprite
	 * @param tipTopControlPointHeight - the height in world 3D units - the same as sizeInScene of the sprite
	 * @param tipTopAlphaValue - The maximum alpha value for the amount of white mixed in at the tip region of the sprite
	 */
	public void setToneRamp( float baseTopControlPointHeight, float baseBottomAlphaValue,  float tipBottomControlPointHeight,  float tipTopControlPointHeight, float tipTopAlphaValue) {
		
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
		if(shadowRenderImageType == BufferedImage.TYPE_BYTE_GRAY) {
			pasteLighting_8Bit(sprite);
		}
		if(shadowRenderImageType == BufferedImage.TYPE_USHORT_GRAY) {
			pasteLighting_16Bit(sprite);
		}
	}
	
	public void pasteLighting_8Bit(Sprite sprite) {
		
		
		

		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.BLACK);

		PVector spriteDocPt = sprite.getDocPoint();
		float spriteDepth = sprite.getDepth();

		float rawBasePointBrightness = lightingSourceTextureSampler.getPixelBilin01(spriteDocPt)*255;
		
		float basePointBrightness = ImageProcessing.getLevelMapping(rawBasePointBrightness, lightingSourceTextureLevelMapping[0], lightingSourceTextureLevelMapping[1], 
																							lightingSourceTextureLevelMapping[2],lightingSourceTextureLevelMapping[3],lightingSourceTextureLevelMapping[4]);	
		
		basePointBrightness/=255f;

		
		basePointBrightness = MOMaths.constrain(basePointBrightness, 0, 1);
		
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
				int existingValue = shadowByteImageGetSet.getPixel(x, y);
				int blendedValue = lerpInt256(existingValue, rampedvalue,  alpha);
				shadowByteImageGetSet.setPixel(x, y, blendedValue);

			}// for X
		}// for Y

	}

	
	private void pasteLighting_16Bit(Sprite sprite) {
		
		// should work for 16 bit
		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.BLACK);

		PVector spriteDocPt = sprite.getDocPoint();
		float spriteDepth = sprite.getDepth();

		// floating point bilinear suitable for 16 bit
		float rawBasePointBrightness = lightingSourceTextureSampler.getPixelBilin01(spriteDocPt)*255;

		//System.out.println(" rawBasePointBrightness " + rawBasePointBrightness + ", rawBasePointBrightness1 " + rawBasePointBrightness1);
		
		// level mapping works in the range 0..255, but as it is floating point accurate, it is suitable for 16 bit so long as the mapping  here occurs 0...255
		float basePointBrightness = ImageProcessing.getLevelMapping(rawBasePointBrightness, lightingSourceTextureLevelMapping[0], lightingSourceTextureLevelMapping[1], 
																							lightingSourceTextureLevelMapping[2],lightingSourceTextureLevelMapping[3],lightingSourceTextureLevelMapping[4]);	
		
		basePointBrightness/=255f;

		// so, base point brightness is fp accurate, amd in the range 0...1, i.e. suitable for 16 bit
		
		
		
		basePointBrightness = MOMaths.constrain(basePointBrightness, 0, 1);
		
		PVector basePoint3D = 	sceneData3D.get3DSurfacePoint(spriteDocPt);

		BufferedImage spriteImage = sprite.getCurrentImage();
		int spriteBufferW = spriteImage.getWidth();
		int spriteBufferH = spriteImage.getHeight();
		Rect spriteBoundingRectBufferSpace = sprite.getDocumentBufferSpaceRect();
		ImageDimensions spriteImageDimensions = new ImageDimensions(spriteBufferW,spriteBufferH);

		// we do it bottom to top, so as to trap the bright point going upwards
		for(int y = (int) spriteBoundingRectBufferSpace.bottom; y >= spriteBoundingRectBufferSpace.top; y--) {
			int blendedValue = 0;

			//work out the doc space location of the pixel above the basepoint, in docSpace
			PVector docSpaceOfY = BStoDS(0,y);
			PVector aboveBasePointAtThisY = new PVector(spriteDocPt.x, docSpaceOfY.y);


			// convert this into a 3D point (at the sprite's depth)
			PVector y3D  = sceneData3D.get3DVolumePoint(aboveBasePointAtThisY, spriteDepth);


			// measure this distance between the basePoint, and this y3D point
			// This gives oyu the height of this row of pixels above the
			// base point in 3D units
			
			float thisYHeight3D = y3D.dist(basePoint3D);
			
			int rampedvalue;
			// tone ramp works by taking the current 3D height if the row of pixels, and returns a value between 0..1 in both value and alpha
			// Using modifyTone() method, we submit the basePointBrightness and the current Height. The toneramp finds the interpolated value and alpha (based on Yheight) and used them to return 
			// the modified final tone value in the range 0..1. 
			if(toneRamp!=null) {
				rampedvalue = (int)  (toneRamp.modifyTone(thisYHeight3D, basePointBrightness) * 65535);
			} else {
				rampedvalue = (int)  (basePointBrightness * 65535);
			}

			int left = (int) spriteBoundingRectBufferSpace.left;
			int right = (int) spriteBoundingRectBufferSpace.right;
			int halfway = (int) (left+(right-left)/2f);
			for (int x = left ; x <= right; x++) {

				if( !shadowRenderTarget.getCoordinateSystem().isInsideBufferSpace(x, y)) {
					continue;
				}

				int pixelLocationInSpriteImageX = x - (int)spriteBoundingRectBufferSpace.left;
				int pixelLocationInSpriteImageY = y - (int)spriteBoundingRectBufferSpace.top;

				if( !spriteImageDimensions.isLegalIndex(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY) ) {
					continue;
				}

				int spriteRGBA = spriteImage.getRGB(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY);
				int alpha = MOPackedColor.getAlpha(spriteRGBA)*256;
				if(alpha == 0) {
					continue;
				}
				
				int existingValue = shadowShortImageGetSet.getSample(x, y, 0);
				blendedValue = lerpInt65535(existingValue, rampedvalue,  alpha);
				shadowShortImageGetSet.setSample(x, y, 0, blendedValue);
				
			}// for X
			
		}// for Y
	
	}
	

}






