package MOAppSessionHelpers;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import MOApplication.Surface;

import MOCompositing.BufferedImageRenderTarget;
import MOImage.ConvolutionFilter;
import MOImage.ImageProcessing;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Plane3D;
import MOMaths.QRandomStream;
import MOMaths.Range;
import MOMaths.Ray3D;
import MOMaths.Intersection3D;
import MOMaths.Rect;
import MOMaths.SNum;
import MOMaths.Vertices2;
import MOPointGeneration.PackingInterpolationScheme;
import MOScene3D.SceneData3D;
import MOSimpleUI.Slider;
import MOSimpleUI.TextInputBox;
//import MOSimpleUI.Slider;
//import MOSimpleUI.TextInputBox;
import MOSimpleUI.UIEventData;
import MOSprite.Sprite;
import MOSprite.SpriteSeedBatch;
import MOUtils.GlobalSettings;
import MOUtils.MOStringUtils;




	
public class Scene3DHelper {
		static Surface theSurface = null;
		static SceneData3D sceneData3D = null;
		
		static float measuringToolSize = 10;
		
		public static void initialise(SceneData3D sd3d, Surface s) {
			theSurface = s;
			sceneData3D = sd3d;
			
			add3DMeasuringToolSlider();
			makeRenderImageMenu();
			System.out.println("initialising scene data helper" );
		}
		
		public static SceneData3D getSceneData3D() {
			return sceneData3D;
		}
		
		
		
		
		
		
		static PVector vec(float x, float y, float z) {
			return new PVector(x,y,z);
		}
		
		public static SpriteSeedBatch createSeedbatch3D(String name, String packingImage, float controlValMin, float controlValMax, float radAtControlMin, float radAtControlMax, int pointPackingRanSeed, int seedRandomKey) {
			
			return createSeedbatch3D( name,  packingImage,  controlValMin,  controlValMax,  radAtControlMin,  radAtControlMax,  pointPackingRanSeed,  seedRandomKey,1000000);
		}
		
		public static SpriteSeedBatch createSeedbatch3D(String name, String packingImage, float controlValMin, float controlValMax, float radAtControlMin, float radAtControlMax, int pointPackingRanSeed, int seedRandomKey, int maxNumPoints) {
			PackingInterpolationScheme interpolationScheme = new PackingInterpolationScheme( controlValMin,  controlValMax,  radAtControlMin,  radAtControlMax, PackingInterpolationScheme.EXCLUDE,  PackingInterpolationScheme.CLAMP); 
			SpriteSeedBatchHelper_Scene3D seedBatchHelper = new SpriteSeedBatchHelper_Scene3D(name, sceneData3D);
			seedBatchHelper.definePointPacking(packingImage, interpolationScheme, pointPackingRanSeed);
			seedBatchHelper.setDepthSensitivePacking(0.5f, 125);
			seedBatchHelper.setMaxNumPoints(maxNumPoints);
			return seedBatchHelper.generateSpriteSeedBatch(seedRandomKey);
		} 
		
		
		
		
		// 
		///////////////////////////////////////////////////////
		// captures a 16bit gray image storing the depth of the ROI
		// This is normalised to the depth extrema within the scene being rendered (which may be a ROI).
		// No-substance is set to zero, so the smallest depth value is 1
		public static BufferedImage captureSceneDepth() {
			System.out.println("ERROR using broken captureSceneDepth" );
			
			// Displays the lighting projeted onto the current sceneData3D based on the current settings
			int width = GlobalSettings.getTheDocumentCoordSystem().getBufferWidth();
			int height = GlobalSettings.getTheDocumentCoordSystem().getBufferHeight();
			BufferedImage sceneDepthImage = new BufferedImage(width,height,BufferedImage.TYPE_USHORT_GRAY);
			WritableRaster sceneDepthImageRaster = sceneDepthImage.getRaster();
			
			//Range fullExtrema = sceneData3D_v1.getFullSceneDepthExtrema();
			//Range localExtrema = sceneData3D_v1.getROIDepthExtrema(true);

			//System.out.println("Full scene Depth Extrema " + fullExtrema.toStr());
			//System.out.println("Local ROI Depth Extrema " + localExtrema.toStr());
			
			
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {

					PVector docSpace = GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(x, y);
					float originalDepth = sceneData3D.getDepth(docSpace);
					boolean isSubstance = sceneData3D.isSubstance(docSpace);
					
					if(isSubstance) {
						//float localNormalisedDepth = localExtrema.norm(originalDepth);
						
						
						
						//int ushortDepthRange = (int)(localNormalisedDepth*65535);
						
						//if(y%10==0 && x == 10) {
						//	
						//	System.out.println("x y " + x + ", " + y + "   originalDepth = " + originalDepth + " nomalised locally " + localNormalisedDepth + " UShort " + ushortDepthRange);
						//}
						
						//if(ushortDepthRange<1) ushortDepthRange=1;
						//sceneDepthImageRaster.setSample(x, y, 0,ushortDepthRange);
					} else {
						sceneDepthImageRaster.setSample(x, y, 0,0);
						
					}

					
				}
				
			}

			return sceneDepthImage;
		
	}
	
		
		
		
		public static void shiftSpriteOriginBy3DYAmountV2(Sprite sprite, float shiftY) {
			// adjusts the anchor point Y of a sprite so that it is lifted or dropped by an amount - shiftY -  in 3D
			// Hence an image can be used to add height-detail to the sprites.
			// FYI: Just altering the anchor-point by a fixed amount will not do, as sprites of differing heights will have inconsistent outcomes wrt the "baseline"
			
			//
			// work out the new shifted doc point
			
			PVector docPoint = sprite.getDocPoint();
			//float scl3d = sceneData3D.get3DScale(docPoint);
			//float scaledShift = shiftY * scl3d;
			//PVector shiftedDocPoint = new PVector(docPoint.x,docPoint.y+scaledShift);
			
			PVector exiting3DPoint =  sceneData3D.get3DSurfacePoint(docPoint);
			PVector displaced3DPoint = new PVector(exiting3DPoint.x, exiting3DPoint.y+shiftY,exiting3DPoint.z);
			PVector shiftedDocPointNOROI = sceneData3D.depthBuffer3d.world3DToDocSpace(displaced3DPoint);
			PVector shiftedDocPoint= sceneData3D.masterDocSpaceToROIDocSpace(shiftedDocPointNOROI);
			
			
			
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
		
		public static void shiftSpriteDocPoint3DYAmount(Sprite sprite, float shiftY) {
			
			PVector shiftedDocPt = shiftDocPoint3DYAmount(sprite.getDocPoint(),  shiftY);
			sprite.setDocPoint(shiftedDocPt);
			
		}
		
		public static PVector shiftDocPoint3DYAmount(PVector initialDocPoint, float shiftY) {
			// adjusts the initialDocPoint by calculating a point in 3D that is shiftY (in 3D units) above the 3D location of the initial docPoint. Probably best used
			// at the very end of the sprite transforms, as otherwise you may be picking up scene data from the wrong point. Think of it as being like
			// an invisible stick - length shiftY - above the initial scene point - the sprite is pasted at the top of this stick.
			// 
			// used in preference to shifting the pivot point of sprites because the using the pivot point, the resultant shift is relative the assets height, not an absolute in scene terms. 
			// So if you want a range of assets of different heights to be shifted by the same amount, this is the only way.
			//
			//
			
			PVector exiting3DPoint =  sceneData3D.get3DSurfacePoint(initialDocPoint);
			PVector displaced3DPoint = new PVector(exiting3DPoint.x, exiting3DPoint.y+shiftY,exiting3DPoint.z);
			PVector shiftedDocPointNOROI = sceneData3D.depthBuffer3d.world3DToDocSpace(displaced3DPoint);
			PVector shiftedDocPoint= sceneData3D.masterDocSpaceToROIDocSpace(shiftedDocPointNOROI);
			
			return shiftedDocPoint;
		}
		
		/*
		static float getLerpDistanceEffect(Sprite sprite, float distMinEffect, float distMaxEffect) {
			float dist = sceneData3D_v1.getDistance(sprite.getDocPoint());
			float val = MOMaths.norm(dist, distMinEffect, distMaxEffect);
			return MOMaths.constrain(val, 0, 1);
		}
		
		static float getRampedDistanceEffect(Sprite sprite, float distMinEffectNear, float distMaxEffect, float distMinEffectFar) {
			float dist = sceneData3D_v1.getDistance(sprite.getDocPoint());
			if(dist < distMaxEffect) {
				return getLerpDistanceEffect( sprite,  distMinEffectNear,  distMaxEffect);
			}
			
			return getLerpDistanceEffect( sprite,  distMinEffectFar,  distMaxEffect);
			
		}
		*/
		
		
		public static float addWave(Sprite sprite, String waveImageName, float amt, boolean flipInDirection) {
			
			
			
			PVector grad = sceneData3D.getCurrentRenderGradiant(sprite.getDocPoint());
			float mag = grad.mag();
			
			if(mag>0.001) {
				float rot = grad.heading();
				float scaledRot = rot*mag*amt;
				System.out.println("rotation " + scaledRot);
				if(scaledRot > 0 && flipInDirection) sprite.setMainImage(ImageProcessing.mirrorImage(sprite.getMainImage(), true, false));
				sprite.rotate((float)Math.toDegrees(scaledRot));
				return scaledRot;
			}
			return 0;
		}
		
		
		public static void addWave(Sprite sprite, float rotationDegrees,  boolean flipInDirection) {
			
			if(rotationDegrees > 0 && flipInDirection) sprite.setMainImage(ImageProcessing.mirrorImage(sprite.getMainImage(), true, false));
			sprite.rotate(rotationDegrees);
			
		}
		
		public static float addWave(Sprite sprite, String waveImageName, float degreesLeft, float degreesRight, float noise, boolean flipInDirection) {
			
			
			float rotationDegrees =  getWaveRotationDegrees( waveImageName, sprite.getDocPoint(), sprite.getRandomStream(),  degreesLeft,  degreesRight,  noise);
			
			
			if(rotationDegrees > 0 && flipInDirection) sprite.setMainImage(ImageProcessing.mirrorImage(sprite.getMainImage(), true, false));
			sprite.rotate(rotationDegrees);
			return rotationDegrees;
		}
		
		
		public static float getWaveRotationDegrees(String waveImageName, PVector docPt, QRandomStream ranStream, float degreesLeft, float degreesRight, float noise) {
			// return degrees rotation based on a wave image
			sceneData3D.setCurrentRenderImage(waveImageName);
			float v = sceneData3D.getCurrentRender01Value(docPt);
			
			
			degreesLeft = ranStream.perturbProportional(degreesLeft, noise);
			degreesRight = ranStream.perturbProportional(degreesRight, noise);
			
			float rotationDegrees = MOMaths.lerp(v,-degreesLeft,degreesRight);
			
			
			return rotationDegrees;
		}
		
		
		
		////
		static float addLighting(Sprite sprite, String lightingImage, float dark, float bright, float noise) {
			sceneData3D.setCurrentRenderImage(lightingImage);
			float v = sceneData3D.getCurrentRender01Value(sprite.getDocPoint());
			
			if(noise > 0.001) {
				
			QRandomStream ranStream = sprite.getRandomStream();
			v = ranStream.perturbProportional(v, noise);
			}
			
			float brightness = MOMaths.lerp(v, dark, bright);
			SceneHelper.addLighting( sprite,  brightness );
			return brightness;
		}
		
	
	
	public static void handleMyUIEvents(UIEventData uied) {
		
		if (uied.eventIsFromWidget("SceneData View")) {
			System.out.println("change scene view to " + uied.menuItem);
			if(uied.menuItem.contentEquals("none")) {
				theSurface.setCanvasBackgroundImage(null);
			} else {
				BufferedImage viewIm = sceneData3D.getRenderImage(uied.menuItem, true);
				theSurface.setCanvasBackgroundImage(viewIm);
			}
		}
		
		
		if (uied.eventIsFromWidget("ruler size slider")) {
			//uied.print(2);
			update3DMeasuringToolSlider(uied.sliderValue);
		}
		

	}
	
	static void makeRenderImageMenu() {
		ArrayList<String> names = sceneData3D.getRenderImageNames();
	    String nameArray[] = new String[names.size()+1];
	    nameArray[0] = "none";
	    int i = 1;
	    for(String name: names) {
	    	nameArray[i++] = name;
	    }
	    theSurface.theUI.addMenu("SceneData View", 0, 100, nameArray);
	}
	
	
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
				
				
				if( sceneData3D.isSubstance(docPt)==false) continue;
				
				PVector p3d = sceneData3D.get3DSurfacePoint(docPt);
				
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
	
	public static Color testGrid3D(PVector p3d, float gridSpace, float gridLineThickness, boolean xLines, boolean yLines,boolean zLines) {
		PVector offset = new PVector(-10000,-10000,-10000);
		offset.add(p3d);
		float xMod = Math.abs(p3d.x % gridSpace);
		float yMod = Math.abs(p3d.y % gridSpace);
		float zMod = Math.abs(p3d.z % gridSpace);
		
		if(xLines && xMod < gridLineThickness) return Color.BLACK;
		if(yLines && yMod < gridLineThickness) return Color.BLACK;
		if(zLines && zMod < gridLineThickness) return Color.BLACK;
		return Color.WHITE;
	}
	
	
	
	static void add3DMeasuringToolSlider() {
		Slider s = theSurface.theUI.addSlider("ruler size slider", 0, 260);
		TextInputBox tib = theSurface.theUI.addTextInputBox("ruler size", 0, 290, "0");
		theSurface.theUI.setText("ruler size", "0.5");
		tib.setWidgetDims(40, 30);
		update3DMeasuringToolSlider(0.5f);
	}
	
	
	static void update3DMeasuringToolSlider(float slideVal) {
		float s = slideVal*10;
		measuringToolSize = s;
		//System.out.println("MeasuringToolSize " + measuringToolSize);
		
		String rounded = MOStringUtils.roundNumber(s, 3);
		theSurface.theUI.setText("ruler size", rounded);
	}
	
	public static void draw3DMeasuringTool(PVector docPt, boolean visible) {
		
		if(visible == false) {
			theSurface.theUI.deleteCanvasOverlayShapes("measuringTool");
			return;
		}
		
		
		//System.out.println("measuringToolSize " + measuringToolSize);
		
		
		theSurface.theUI.deleteCanvasOverlayShapes("measuringTool");
		PVector endPt = docPt.copy();
		float worldScale = sceneData3D.get3DScale(docPt);
		PVector p3d = sceneData3D.get3DSurfacePoint(docPt);
		//float distance = sceneData3D_v1.getDistance(docPt);
		float depth = sceneData3D.getDepth(docPt);
		
		
		
		float textX = endPt.x;
		endPt.y -= (worldScale * measuringToolSize);
		float textY1 = endPt.y + (0.1f);
		float textY2 = endPt.y + (0.12f);
		float textY3 = endPt.y + (0.14f);
		float len = docPt.dist(endPt);
		// System.out.println("roi depth extrema = " + normalisedDepthExtrema.toStr() + " masterNormalisedDepth " + masterNormalisedDepth + " ROI normalised depth " + roiNormalisedDepth);
		// theUI.addCanvasOverlayShape("mouseDot", uied.docSpacePt, radiusOffset, "ellipse", new Color(127, 0, 0, 255), Color.gray, 1);
		
		theSurface.theUI.addCanvasOverlayShape("measuringTool", docPt, endPt, "line", Color.black, Color.blue, 4);
		theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY1), " 3d size " + measuringToolSize ,  Color.red, 20);
		theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY2), " depth " + depth ,  Color.red, 20);
		theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY3), "  p3d = " + p3d.toStr() ,  Color.red, 20);
		//theSurface.theUI.addCanvasOverlayText("measuringTool", new PVector(textX, textY2), ,  Color.blue, 20);
		//theSurface.theUI.addCanvasOverlayText("measuringTool", endPt, "  Stick Hght = " + measuringToolSize,  Color.blue, 20);
		
	}
	
	/*
	public static void print3DSceneData(PVector docPt) {
		float worldScale = sceneData3D_v1.get3DScale(docPt);
		float distance = sceneData3D_v1.getDistance(docPt);
		
		float normalisedDepth = sceneData3D_v1.getDepthNormalised(docPt);
		
		System.out.println("3DSceneData at:" + docPt.toStr() + " world scale:" + worldScale + " Distance:" + distance +  " Normalised depth:" + normalisedDepth);     
	}
	*/
	
}
