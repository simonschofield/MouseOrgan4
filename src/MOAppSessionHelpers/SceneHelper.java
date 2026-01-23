package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.FloatImageRenderTarget;
import MOImage.ImageDimensions;
import MOImage.ImageProcessing;
import MOImageCollections.ScaledImageAssetGroup;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.SNum;
import MOScene3D.SceneData3D;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;

/**
 * Contains a rag tag of static methods that might be useful. 
 */
public class SceneHelper {



	/**
	 * Convert between final print size (in mm) and doc space dimension, based on 300 ppi.
	 * Given a dimension in the full-size printed version (e.g. 5mm), it returns the doc space dimension that is that size when printed at full size. Gives the same answer at all user Session scales<p>
	 *
	 * 
	 * @param mm - the mm dimension in the fullsize, final image
	 * @return the doc space equivalent of the above value 
	 */
	public static float millimeterToDocspace(float mm) {
	
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

	/**
	 * Converts between a number of pixels in the full scale version of the output image, and the doc space equivalent
	 * @param pixels - the number of pixels in the fullsize final image
	 * @return the doc space equivalent of the above value 
	 */
	public static float fullScalePixelsToDocSpace(float pixels) {
		float pixelsScaled = pixels*GlobalSettings.getSessionScale();
		return (pixelsScaled/GlobalSettings.getTheDocumentCoordSystem().getLongestBufferEdge());
	}

	/**
	 * Coverts between a doc space amount, and the number of pixels in the final full-scale image
	 * @param docSpaceMeasurement
	 * @return
	 */
	public static float docSpaceToFullScalePixels(float docSpaceMeasurement) {
		return docSpaceMeasurement*GlobalSettings.getTheDocumentCoordSystem().getLongestBufferEdge();
	}
	
	
	
	
	
	
	static void randomRotateScaleSprite(Sprite sprite, float scaleAmt, float rotAmount) {
		randomRotateScaleSprite( sprite,  scaleAmt,  rotAmount, true);
	}

	public static void drawSpriteRect(Sprite sprite, Color c, BufferedImageRenderTarget rt) {
		Rect spriteRectBufferSpace = sprite.getDocumentBufferSpaceRect();

		rt.getVectorShapeDrawer().setDrawingStyle(new Color(255,255,255,0), c, 4);
		rt.getVectorShapeDrawer().drawRect(spriteRectBufferSpace);

	}


	static void randomRotateScaleSprite(Sprite sprite, float scaleAmt, float rotAmount, boolean flipInRotationDirection) {
		QRandomStream ranStream = sprite.getRandomStream();
		float rscale = ranStream.randRangeF(1-scaleAmt,1+scaleAmt);

		float rrot = ranStream.randRangeF(-rotAmount,rotAmount);

		if(flipInRotationDirection && rrot > 0) {
			sprite.mirror(true);
		}
		sprite.rotate(rrot);
		sprite.scale(rscale,rscale);
	}

	static void randomMirrorSprite(Sprite sprite, boolean inX, boolean inY) {
		QRandomStream ranStream = sprite.getRandomStream();
		boolean coinTossX = ranStream.probabilityEvent(0.5f);
		boolean coinTossY = ranStream.probabilityEvent(0.5f);
		if(coinTossX && inX) {
			sprite.mirror(true);
		}
		if(coinTossY && inY) {
			sprite.mirror(false);
		}


	}

	static void addRandomHSV(Sprite sprite, float rH, float rS, float rV) {
		QRandomStream ranStream = sprite.getRandomStream();
		float randH = ranStream.randRangeF(-rH, rH);
		float randS = ranStream.randRangeF(-rS, rS);
		float randV = ranStream.randRangeF(-rV, rV);


		sprite.setMainImage(ImageProcessing.adjustHSV(sprite.getCurrentImage(), randH, randS, randV));
	}


}




