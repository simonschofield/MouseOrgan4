package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;


import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOImageCollections.DirectoryFileNameScanner;
import MOImageCollections.SpriteImageGroup;
import MOImageCollections.SpriteImageGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.SNum;
import MOMaths.Vertices2;
import MOSpriteSeed.Sprite;
import MOSpriteSeed.SpriteSeedFont;
import MOUtils.GlobalSettings;
import MOUtils.ImageDimensions;

public class SceneHelper {
	
	// load a SpriteImageGroup and add it to the ImageGroupManager (ImageGroupManager must be instantiated before calling this)
	public static void loadSpriteImageGroup(String spriteImageGroupSamplePath, String spriteImageGroupName, String fileNameContains) {
		    SpriteImageGroup spriteImageGroup = new SpriteImageGroup(spriteImageGroupName);
		    DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(spriteImageGroupSamplePath);
		    if(fileNameContains != null) {
		    	dfns.setFileNameContains(fileNameContains);
		    }
		    spriteImageGroup.setDirectoryFileNameScanner(dfns);
		    spriteImageGroup.loadSamples();
		    GlobalSettings.getTheSpriteImageGroupManager().addSpriteImageGroup(spriteImageGroup);
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
	
	public float fullScalePixelsToDocSpace(float pixels) {
		// Allows the user to get the doc space measurement
		// for a number of pixels in the full scale image. This is useful for defining 
		// certain drawing operations, such as defining line thickness and circle radius. 
		// To be resolution independent, these operations take measurement in document space.
		// But the user may wish to think in pixel size in the final 100% scale image.
		float pixelsScaled = pixels*GlobalSettings.getSessionScale();
		return (pixelsScaled/GlobalSettings.getTheDocumentCoordSystem().getLongestBufferEdge());
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


