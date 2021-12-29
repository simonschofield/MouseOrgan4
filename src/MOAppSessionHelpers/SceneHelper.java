package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOCompositing.ImageSprite;
import MOImage.ImageProcessing;
import MOImage.RenderTarget;
import MOImageCollections.ImageSampleGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.SNum;
import MOMaths.Vertices2;

public class SceneHelper {
	
	
	
	static void randomRotateScaleSprite(ImageSprite sprite, float scaleAmt, float rotAmount) {
		randomRotateScaleSprite( sprite,  scaleAmt,  rotAmount, true);
	}
	
	public static void drawSpriteRect(ImageSprite sprite, Color c, RenderTarget rt) {
		Rect spriteRectBufferSpace = sprite.getDocumentBufferSpaceRect();
		
		rt.getVectorShapeDrawer().setDrawingStyle(new Color(255,255,255,0), c, 4);
		rt.getVectorShapeDrawer().drawRect(spriteRectBufferSpace);
		
	}
	
	
	public static void drawQuad(ImageSprite sprite, Color c, RenderTarget rt) {
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
	
	static void randomRotateScaleSprite(ImageSprite sprite, float scaleAmt, float rotAmount, boolean flipInRotationDirection) {
		QRandomStream ranStream = sprite.getRandomStream();
		float rscale = ranStream.randRangeF(1-scaleAmt,1+scaleAmt);
		
		float rrot = ranStream.randRangeF(-rotAmount,rotAmount);
		
		if(flipInRotationDirection && rrot > 0) sprite.mirror(true);
		sprite.rotate(rrot);
		sprite.scale(rscale,rscale);
	}
	
	static void randomMirrorSprite(ImageSprite sprite, boolean inX, boolean inY) {
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
	
	
	static void addLighting(ImageSprite sprite, float brightness) {
		// when the brightness is low, so is the contrast
		//sprite.image = ImageProcessing.adjustContrast(sprite.image, brightness);
		
		sprite.setImage(ImageProcessing.adjustBrightness(sprite.getImage(), brightness));
		//sprite.image = ImageProcessing.adjustBrightnessNoClip(sprite.image, brightness);

	}
	
	static void addContrast(ImageSprite sprite, float contrast) {
		// when the brightness is low, so is the contrast
		sprite.setImage(ImageProcessing.adjustContrast(sprite.getImage(), contrast));
		
		
	}
	
	static void addRandomHSV(ImageSprite sprite, float rH, float rS, float rV) {
		QRandomStream ranStream = sprite.getRandomStream();
		float randH = ranStream.randRangeF(-rH, rH);
		float randS = ranStream.randRangeF(-rS, rS);
		float randV = ranStream.randRangeF(-rV, rV);
		
		
		sprite.setImage(ImageProcessing.adjustHSV(sprite.getImage(), randH, randS, randV));
	}
	
	
	
	
	static void addClump(ImageSprite sprite, float scaleAmt, float rotAmount){
		
		
	}
	

	ImageSprite createImageSprite(PVector docPos, String imageContentGroupName, ImageSampleGroupManager contentManager) {
		
		ImageSprite sprite =  contentManager.getSprite(imageContentGroupName,1);
		
		sprite.docPoint = docPos;
		return sprite;
	}
	
}


class StochasticMethods{
	
	static BufferedImage adjustHSV(BufferedImage img, SNum h, SNum s, SNum v) {
		return ImageProcessing.adjustHSV(img, h.get(), s.get(), v.get());
	}
	
	
	
	
}


