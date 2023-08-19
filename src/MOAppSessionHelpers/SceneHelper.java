package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;


import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOImageCollections.DirectoryFileNameScanner;
import MOImageCollections.ScaledImageAssetGroup;
import MOImageCollections.ScaledImageAssetGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.SNum;
import MOMaths.Vertices2;
import MOSprite.Sprite;
import MOSprite.SpriteFont;
import MOUtils.GlobalSettings;
import MOUtils.ImageDimensions;

public class SceneHelper {
	
	/**
	 * creates a mask image of submitted sprite if the sprite attribute string contains the spriteAttributeStringToContain. If so then this sprite is ADDED ot the mask (i.e. pastes
	 * white (or replacement image) to the mask). If it does NOT match (i.e. does not contain the spriteAttributeStringToContain string) then the sprite is SUBTRACTED from the mask (i.e. pastes black).
	 * This algorithm assumes the mask image named exists. The replacementImage is nullable. If not null, the replacement image is (scaled then) pasted. If null, then the sprite is pasted in white.
	 * 
	 * 
	 * @param sprite - the sprite being pasted
	 * @param spriteAttributeString - the string property f the sprite used to identify the sprite. This could be SpriteSeedBatchName,SpriteFontName,ImageAssetGroupName or ImageGroupItemShortName
	 * @param spriteAttributeStringToContain - the above string is checked to see if it contains this string. If so the paste ADDS this sprite to the mask, else this sprite is SUBTRACTED from the mask
	 * @param maskName - The string name of the render target
	 * @param replacementImage - Nullable, in which chase simple white/black masking happens. If an image, the image is scled then used as the pasting image.
	 */
	public static void pasteToMaskImage(Sprite sprite, String spriteAttributeString, String spriteAttributeStringToContain, String maskName, BufferedImage replacementImage) {

		if( spriteAttributeString.contains(spriteAttributeStringToContain) ) {
			
			if(replacementImage!=null) {
				BufferedImage blendedImage = ImageProcessing.replaceVisiblePixels(sprite.getImage(), replacementImage);
				GlobalSettings.getDocument().getRenderTarget(maskName).pasteSpriteAltImage(sprite, blendedImage, 1);
			}else {
				GlobalSettings.getDocument().getRenderTarget(maskName).pasteSpriteMask(sprite, Color.white);
				}
			

		} else {
			GlobalSettings.getDocument().getRenderTarget(maskName).pasteSpriteMask(sprite, Color.black);
		}
		
	}
	
	// load a SpriteImageGroup and add it to the ImageGroupManager (ImageGroupManager must be instantiated before calling this)
	public static void loadSpriteImageGroup(String spriteImageGroupSamplePath, String spriteImageGroupName, String fileNameContains) {
		    ScaledImageAssetGroup spriteImageGroup = new ScaledImageAssetGroup(spriteImageGroupName);
		    DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(spriteImageGroupSamplePath);
		    if(fileNameContains != null) {
		    	dfns.setFileNameContains(fileNameContains);
		    }
		    spriteImageGroup.setDirectoryFileNameScanner(dfns);
		    spriteImageGroup.loadSessionScaledImages();
		    GlobalSettings.getImageAssetGroupManager().addImageAssetGroup(spriteImageGroup);
		}
	
	
	
	
	public static float millimeterToDocspace(float mm) {
		// you give it a dimension in the printed version (e.g. 5mm)
		// and it returns the doc space dimension that is that size when printed at full size. Gives the same answer at all
		// draft resolutions.
		ImageDimensions dims = GlobalSettings.getFullScaleDocumentDimension();
		// Assumes print resolution is 300dpi, (or 11.811 pixels per mm)
		
		if(dims.width > dims.height) {
			// use width
			float numMMAcrossLongestEdgeOfImage = dims.width/11.811f; // this is the number of MM (at full size res) that are equal to a doc space of 1,
			// Therefore 1/numMMAcrossLongestEdgeOfImage equals the doc space occupied by 1mm
			return mm/numMMAcrossLongestEdgeOfImage;
		}else {
			float numMMAcrossLongestEdgeOfImage = dims.height/11.811f;
			return mm/numMMAcrossLongestEdgeOfImage;
		}
	}
	
	public static float fullScalePixelsToDocSpace(float pixels) {
		// Allows the user to get the doc space measurement
		// for a number of pixels in the full scale image. This is useful for defining 
		// certain drawing operations, such as defining line thickness and circle radius. 
		// To be resolution independent, these operations take measurement in document space.
		// But the user may wish to think in pixel size in the final 100% scale image.
		float pixelsScaled = pixels*GlobalSettings.getSessionScale();
		return (pixelsScaled/GlobalSettings.getTheDocumentCoordSystem().getLongestBufferEdge());
	}
	
	public static float docSpaceToFullScalePixels(float docSpaceMeasurement) {
		// Allows the user to get the number of pixels that represent a docSpaceMeasurement in the full scale 100% image
		return docSpaceMeasurement*GlobalSettings.getTheDocumentCoordSystem().getLongestBufferEdge();
		//return pixelsInThisImage*GlobalSettings.getSessionScale();
	}
	
	static void randomRotateScaleSprite(Sprite sprite, float scaleAmt, float rotAmount) {
		randomRotateScaleSprite( sprite,  scaleAmt,  rotAmount, true);
	}
	
	public static void drawSpriteRect(Sprite sprite, Color c, RenderTarget rt) {
		Rect spriteRectBufferSpace = sprite.getDocumentBufferSpaceRect();
		
		rt.getVectorShapeDrawer().setDrawingStyle(new Color(255,255,255,0), c, 4);
		rt.getVectorShapeDrawer().drawRect(spriteRectBufferSpace);
		
	}
	
	
	public static void drawQuad(Sprite sprite, Color c, RenderTarget rt) {
		Vertices2 verts = sprite.imageQuad.getSpriteBufferSpaceQuadVertices();
		PVector shift = sprite.spriteBufferSpaceToDocumentBufferSpace(PVector.ZERO());
		verts.translate(shift.x, shift.y);
		rt.getVectorShapeDrawer().setDrawingStyle(new Color(255,255,255,0), c, 4);
		rt.getVectorShapeDrawer().drawVertices2(verts);
		
		//for(int n = 0; n<4; n++) {
		//	PVector quadPt = verts.get(n);
		//	PVector docBufferPt = sprite.spriteBufferSpaceToDocumentBufferSpace(quadPt);
		//	rt.getVectorShapeDrawer().setDrawingStyle(new Color(255,255,255,0), c, 4);
		//	rt.getVectorShapeDrawer().drawEllipse(docBufferPt.x, docBufferPt.y, 5, 5);

			
		//}
		 
	}
	
	public static Rect getDocSpaceRectFromNormalisedRect(float left, float top, float right, float bottom) {
		Rect nRect = new Rect();
		nRect.left = left;
		nRect.top = top;
		nRect.right = right;
		nRect.bottom = bottom;
		
		return getDocSpaceRectFromNormalisedRect(nRect);
	}
	
	public static Rect getDocSpaceRectFromNormalisedRect(Rect normalisedRect) {
		PVector topLeft = GlobalSettings.getDocument().getCoordinateSystem().normalisedSpaceToDocSpace(normalisedRect.getTopLeft());
		PVector bottomRight = GlobalSettings.getDocument().getCoordinateSystem().normalisedSpaceToDocSpace(normalisedRect.getBottomRight());
		return new Rect(topLeft, bottomRight);
	}
	
	static void randomRotateScaleSprite(Sprite sprite, float scaleAmt, float rotAmount, boolean flipInRotationDirection) {
		QRandomStream ranStream = sprite.getRandomStream();
		float rscale = ranStream.randRangeF(1-scaleAmt,1+scaleAmt);
		
		float rrot = ranStream.randRangeF(-rotAmount,rotAmount);
		
		if(flipInRotationDirection && rrot > 0) sprite.mirror(true);
		sprite.rotate(rrot);
		sprite.scale(rscale,rscale);
	}
	
	static void randomMirrorSprite(Sprite sprite, boolean inX, boolean inY) {
		QRandomStream ranStream = sprite.getRandomStream();
		boolean coinTossX = ranStream.randomEvent(0.5f);
		boolean coinTossY = ranStream.randomEvent(0.5f);
		if(coinTossX && inX) {
			sprite.mirror(true);
		}
		if(coinTossY && inY) {
			sprite.mirror(false);
		}
		
		
	}
	
	
	static void addLighting(Sprite sprite, float brightness) {
		// when the brightness is low, so is the contrast
		//sprite.image = ImageProcessing.adjustContrast(sprite.image, brightness);
		
		sprite.setImage(ImageProcessing.adjustBrightness(sprite.getImage(), brightness));
		//sprite.image = ImageProcessing.adjustBrightnessNoClip(sprite.image, brightness);

	}
	
	static void addContrast(Sprite sprite, float contrast) {
		// when the brightness is low, so is the contrast
		sprite.setImage(ImageProcessing.adjustContrast(sprite.getImage(), contrast));
		
		
	}
	
	static void addRandomHSV(Sprite sprite, float rH, float rS, float rV) {
		QRandomStream ranStream = sprite.getRandomStream();
		float randH = ranStream.randRangeF(-rH, rH);
		float randS = ranStream.randRangeF(-rS, rS);
		float randV = ranStream.randRangeF(-rV, rV);
		
		
		sprite.setImage(ImageProcessing.adjustHSV(sprite.getImage(), randH, randS, randV));
	}
	
	
	
	
	
}


class StochasticMethods{
	
	static BufferedImage adjustHSV(BufferedImage img, SNum h, SNum s, SNum v) {
		return ImageProcessing.adjustHSV(img, h.get(), s.get(), v.get());
	}
	
	
	
	
}


