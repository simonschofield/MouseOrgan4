package MOAppSessionHelpers;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOApplication.Surface;
import MOImage.ImageProcessing;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Range;
import MOMaths.Rect;
import MOScene3D.SceneData3D;
//import MOSimpleUI.Slider;
//import MOSimpleUI.TextInputBox;
import MOSimpleUI.UIEventData;
import MOSprite.Sprite;
//import MOSprite.SpriteSeedBatch;
import MOUtils.GlobalSettings;





/**
 * A place where you can put odd 3D "helper" type methods. Waves and point shifting etc. Also a 3D test method.
 */
public class Scene3DHelper {
		
		
		/**
		 * adjusts the anchor point Y of a sprite so that it is lifted or dropped by an amount - shiftY -  in 3D. Hence an image can be used to add height-detail to the sprites.<p>
		 * FYI: Just altering the anchor-point by a fixed amount will not do, as sprites of differing heights will have inconsistent outcomes wrt the "baseline"<p>
		 * This seems over-elaborate, why can't we just re-calculate the base point in 3D? Perhaps has a knock-on effect wrt rotation/waves????
		 * @param sprite
		 * @param shiftY
		 */
		public static void shiftSpriteOriginBy3DYAmountV2(Sprite sprite, float shiftY) {
			// 
			// 
			// 

			//
			// work out the new shifted doc point

			PVector docPoint = sprite.getDocPoint();
			//float scl3d = sceneData3D.get3DScale(docPoint);
			//float scaledShift = shiftY * scl3d;
			//PVector shiftedDocPoint = new PVector(docPoint.x,docPoint.y+scaledShift);

			PVector exiting3DPoint =  GlobalSettings.getSceneData3D().get3DSurfacePoint(docPoint);
			PVector displaced3DPoint = new PVector(exiting3DPoint.x, exiting3DPoint.y+shiftY,exiting3DPoint.z);
			PVector shiftedDocPoint = GlobalSettings.getSceneData3D().depthBuffer3d.world3DToDocSpace(displaced3DPoint);
			//PVector shiftedDocPoint= shiftedDocPointNOROI;



			// you can do this next bit in doc space or buffer space
			// Work out the shift between the two points in buffer space
			PVector spBufferSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(docPoint);
			PVector shiftedSpBufferSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(shiftedDocPoint);
			float shiftBufferSpace = shiftedSpBufferSpace.y - spBufferSpace.y;

			// The shift represents a drop (or rise) below the bottom of the sprite.
			// We are going to shift the anchor point Y of the sprite so that it is at this new location.
			// To work this out, we need to know the proportion this drop represent
			// New Y = (spriteBufferHeight + dropInPixels)/spriteBufferHeight
			// So if drop == 0, the Y == 1
			// If drop > 0, the proportion will new Y will be > 1
			// if drop is < 0 (a rise) the new Y will be < 1

			// new line to take into consideration all possible existing pivot points, not just y=1
			float exisitingPivotY = sprite.getPivotPoint().y;

			float spriteheight = sprite.getImageHeight()*exisitingPivotY;




			float newY = (shiftBufferSpace+spriteheight)/spriteheight;
			//System.out.println("sprite type " + sprite.spriteData.ImageAssetGroupName + " buffer height " + spriteheight + " new Y " + newY );
			PVector pivotPoint = sprite.getPivotPoint();

		    pivotPoint.y = newY;
		    sprite.setPivotPoint(pivotPoint);

		}

		
		


		/**
		 * Rotates and, if required, flips-in-x the sprite. Art assets for grasses etc are, by default, facing towards the left. If the sprite is rotated further towards the left (Anti-clockwise)
		 * then there is no flip. If the sprite is rotated CW towards the right, then a flip-in-x occurs.
		 * @param sprite -  the sprite. This is altered by the method.
		 * @param rotationDegrees - -ve rotation is anti-clockwise, +ve is clockwise and results in a flip
		 * @param flipInDirection - turns flipping on and off
		 */
		public static void addWave(Sprite sprite, float rotationDegrees,  boolean flipInDirection) {

			if(rotationDegrees > 0 && flipInDirection) {
				sprite.mirror(true);
			}
			sprite.rotate(rotationDegrees);
			

		}

		


		/**
		 * Based on the value found in an image at docPt, a degrees-rotation is returned. A value v (0..1) if calculated from the image pixel value then 
		 * the value v is used to interpolate between degreesLeft and degreesRight. An amount of proportional perturbation is added to degreesLeft and degreesRight
		 * @param waveImageName - the image texture responsible for creating the wave effect
		 * @param docPt - the docPoint in the above image being sampled
		 * @param ranStream - a randomStream with which to do the perturbation - probably from the sprite being rotated.
		 * @param degreesLeft - maximum degrees left
		 * @param degreesRight - maximum degrees right
		 * @param noise - perturbation between 0...1, if 0 then no perturbation, if 1 then the output range will be between 0 and 2*input-value.
		 * @return - the final rotation in degrees
		 */
		public static float getWaveRotationDegrees_InterpolateOnPixelValue(String waveImageName, PVector docPt, QRandomStream ranStream, float degreesLeft, float degreesRight, float noise) {
			// return degrees rotation based on a wave image
			GlobalSettings.getSceneData3D().setCurrentRenderImage(waveImageName);
			float v = GlobalSettings.getSceneData3D().getCurrentRender01Value(docPt);


			degreesLeft = ranStream.perturbProportional(degreesLeft, noise);
			degreesRight = ranStream.perturbProportional(degreesRight, noise);

			float rotationDegrees = MOMaths.lerp(v,-degreesLeft,degreesRight);


			return rotationDegrees;
		}
		
		
		/**
		 * A wave rotation based on the gradient found at docPoint in the image, using a convolution filter
		 * @param waveImageName - the image texture responsible for creating the wave effect
		 * @param docPt - the docPoint in the above image being sampled
		 * @param ranStream - a randomStream with which to do the perturbation - probably from the sprite being rotated.
		 * @param amt - The rotation is based on the heading of the gradient vector * magnitude * amount
		 * @param noise -  perturbation between 0...1, if 0 then no perturbation, if 1 then the output range will be between 0 and 2*input-value.
		 * @return
		 */
		public static float getWaveRotationDegrees_InterpolateOnImageGradient(String waveImageName, PVector docPt, QRandomStream ranStream, float amt, float noise) {

			PVector grad = GlobalSettings.getSceneData3D().getCurrentRenderGradiant(docPt);
			float mag = grad.mag();

			if(mag>0.001f) {
				float rot = grad.heading();
				float scaledRot = rot*mag*amt;
				return scaledRot;
			}
			return 0;
		}
		
		
		
		/**
		 * this is used to fudge the appearance of base grass etc to make it fill out in the distance, but be skinny in the foreground
		 * @param sprite
		 * @param nearDepth - in normalised units of the master render (1 far, 0 near)
		 * @param farDepth - in normalised units of the master render (1 far, 0 near)
		 * @param nearWidthScale
		 * @param farWidthScale
		 * @param nearHeightScale
		 * @param farHeightScale
		 */
		public static void tweakSpriteScaleOnDepth(Sprite sprite, float nearDepth, float farDepth, float nearWidthScale, float farWidthScale, float nearHeightScale, float farHeightScale) {


			
				float nd = GlobalSettings.getSceneData3D().getNormalisedDepth(sprite.docPoint);
				if(nd > farDepth) {
					return;
				}


				float widthScale = MOMaths.mapClamped(nd, nearDepth, farDepth, nearWidthScale, farWidthScale);
				float heightScale = MOMaths.mapClamped(nd, nearDepth, farDepth, nearHeightScale, farHeightScale);

				sprite.scale(widthScale, heightScale);

			}





	/**
	 * Was used during the development of the SceneData3D class
	 * kept because it might be useful for debugging. Test Mode 3, which paints a grid onto the scene in 3D
	 */
	public static void test3D() {
		Range xRange = new Range(); xRange.initialiseForExtremaSearch();
		Range yRange = new Range(); yRange.initialiseForExtremaSearch();
		Range zRange = new Range(); zRange.initialiseForExtremaSearch();

		int w = GlobalSettings.getTheDocumentCoordSystem().getBufferWidth();
		int h = GlobalSettings.getTheDocumentCoordSystem().getBufferHeight();

		int stepx = (int) (w/4f);
		int stepy = (int) (h/4f);

		boolean returnLine = false;

		Rect smallSampleRect = new Rect(stepx*3, stepy*3, 4,4);

		int testmode = 0;

		for(int y = 0; y < h; y++) {
			for( int x = 0; x < w; x++) {

				PVector bufferSpacePt = new PVector(x,y);
				PVector docPt = GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(bufferSpacePt);


				if( !GlobalSettings.getSceneData3D().isSubstance(docPt)) {
					continue;
				}

				PVector p3d = GlobalSettings.getSceneData3D().get3DSurfacePoint(docPt);

				xRange.addExtremaCandidate(p3d.x);
				yRange.addExtremaCandidate(p3d.y);
				zRange.addExtremaCandidate(p3d.z);



				//for testing 5 steps across the image
				if(testmode==0) {
				    if( x == 0 || x == stepx || x == stepx*2 || x == stepx*3 || x == (stepx*4)-1) {
				    	System.out.print(x + "," + y + p3d.toStr() + ", ");
				    	returnLine = true;
				    }
				}

			    // for testing a little rectangle of pixels
				if(testmode==1) {
					if( smallSampleRect.isPointInside(x, y)) {
						System.out.print(x + "," + y + p3d.toStr() + ", ");
						returnLine = true;
					}
				}

				//System.out.println("xy " + x + " " + y);
				if(testmode==2) {

					Color c = testGrid3D( p3d, 3, 0.2f, true, false, true);
					//System.out.println("xy " + x + " " + y + c.toString());
					GlobalSettings.getDocument().getMain().setPixel(x, y, c);
				}


			}

			if( testmode <=1 && returnLine ) {
				System.out.println();
				returnLine = false;
			}

			if(testmode==2 && y%100 == 0) {
				GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
				System.out.println("update " + y);
			}

		}

		System.out.println("X Extema " + xRange.toStr() + ", Y Extema " + yRange.toStr() + ", Z Extema " + zRange.toStr());
	}

	/**
	 * For any point in the scene in 3D, determines if that point is a grid line (black) or grid space (white)
	 * kept because it might be useful for debugging
	 * @param p3d
	 * @param gridSpace
	 * @param gridLineThickness
	 * @param xLines
	 * @param yLines
	 * @param zLines
	 * @return
	 */
	public static Color testGrid3D(PVector p3d, float gridSpace, float gridLineThickness, boolean xLines, boolean yLines,boolean zLines) {
		PVector offset = new PVector(-10000,-10000,-10000);
		offset.add(p3d);
		float xMod = Math.abs(p3d.x % gridSpace);
		float yMod = Math.abs(p3d.y % gridSpace);
		float zMod = Math.abs(p3d.z % gridSpace);

		if((xLines && xMod < gridLineThickness) || (yLines && yMod < gridLineThickness)) {
			return Color.BLACK;
		}
		if(zLines && zMod < gridLineThickness) {
			return Color.BLACK;
		}
		return Color.WHITE;
	}








}
