package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.FloatImageRenderTarget;
import MOImage.ByteImageGetterSetter;
import MOImage.MOColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;
import MOUtils.Progress;

public class Lighting_CommonUtils {

	SceneData3D sceneData3D;
	protected ImageCoordinateSystem coordinateSystem;

	
	
	BufferedImageRenderTarget shadowRenderTarget;
	BufferedImage shadowRenderImage;
	protected int shadowRenderImageType = BufferedImage.TYPE_BYTE_GRAY;
	
	
	WritableRaster shadowShortImageGetSet;
	ByteImageGetterSetter  shadowByteImageGetSet;

	FloatImageRenderTarget depthRenderTarget;
	PVector lightDirection;
	PVector negativeLightDirection;

	public boolean debugFlag = false;
	Progress progress;

	// used by both base-point lighting and inter-shadowing
	ToneRamp toneRamp;

	// this is used to determine whether or not a particular sprite in a ROI session contributes to the image
	// Because shadows extend beyond the sprite itself, sprites outside the ROI may contribute, so should be included in the
	// roi session sprite batch file.
	Rect theDoumentDocSpaceRect;


	public Lighting_CommonUtils(String nameOfShadowRender, int renderImageType) {
		
		sceneData3D = GlobalSettings.getSceneData3D();
		if(sceneData3D == null) {
			System.out.println("Lighting_CommonUtils  SceneData3D == null, please initialse first ");

		}
		
		if( renderImageType == BufferedImage.TYPE_BYTE_GRAY || renderImageType == BufferedImage.TYPE_USHORT_GRAY ) {
			shadowRenderImageType = renderImageType;
		}else {
			System.out.println("Lighting_CommonUtils:: you cannot set the output render to type " + renderImageType + " - Only TYPE_BYTE_GRAY and TYPE_USHORT_GRAY ");
			// defaults to TYPE_BYTE_GRAY
		}
		

		coordinateSystem = GlobalSettings.getDocument().getCoordinateSystem();
		theDoumentDocSpaceRect = coordinateSystem.getDocumentRect();


		GlobalSettings.getDocument().addRenderTarget(nameOfShadowRender, shadowRenderImageType);
		shadowRenderTarget = GlobalSettings.getDocument().getBufferedImageRenderTarget(nameOfShadowRender);
		shadowRenderTarget.fillBackground(Color.WHITE);
		
		if(shadowRenderImageType == BufferedImage.TYPE_BYTE_GRAY) {
			shadowByteImageGetSet = new ByteImageGetterSetter(shadowRenderTarget.getBufferedImage());
		}
		if(shadowRenderImageType == BufferedImage.TYPE_USHORT_GRAY) {
			shadowShortImageGetSet = shadowRenderTarget.getBufferedImage().getRaster();
		}
		
		
		// set a default light direction
		setLightDirection( vec(-0.5f,-1,1) );
	}


	protected void initialiseDepthRender(String nameOfDepthRender, boolean addSceneSurfaceToDepthRender) {

		GlobalSettings.getDocument().addFloatRenderTarget(nameOfDepthRender, true, 1);
		depthRenderTarget = GlobalSettings.getDocument().getFloatImageRenderTarget(nameOfDepthRender);

		//System.out.println("coordinateSystem rect = " + coordinateSystem.getDocumentRect().toStr() + " getCurrentROIDocRect " + theROIManangerDocSpaceRect.toStr());
		if(addSceneSurfaceToDepthRender) {
			addSceneSurfaceToDepth();
		} else {
			depthRenderTarget.setAll(Float.MAX_VALUE);
		}
	}
	
	
	/**
	 * Sets the light direction into the scene. Must have +ve z, so shadows go into the scene
	 * @param lightDir
	 */
	public void setLightDirection(PVector lightDir) {
		PVector v = lightDir.copy();

		v.z = Math.abs(v.z);
		
		v.normalize();
		lightDirection = lightDir.copy();
		negativeLightDirection = PVector.mult(lightDir, -1);
		System.out.println("setLightDirection " + lightDirection.toStr());
	}


	protected void addSceneSurfaceToDepth() {
		int w = coordinateSystem.getBufferWidth();
		int h = coordinateSystem.getBufferHeight();

		int bw = depthRenderTarget.getFloatImage().getWidth();
		int bh = depthRenderTarget.getFloatImage().getHeight();


		System.out.println("shadowBuffer w h " + w + "," + h);
		System.out.println("check shadowBuffer w h " + bw + "," + bh);
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {

				PVector docSpacePt = coordinateSystem.bufferSpaceToDocSpace(x, y);
				float depthVal = sceneData3D.getDepth(docSpacePt);
				//System.out.println("shadowBuffer w h " + w + "," + h + " at  x y " + x + "," + y);
				depthRenderTarget.setPixel(x, y, depthVal);
			}
		}
	}


	


	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// useful shorthands, propose adding these to a new class
	// which can be mixed in when needed
	PVector vec(float x, float y, float z) {
		return new PVector(x,y,z);
	}
	
	PVector DStoBS(PVector docPt) {
		return this.coordinateSystem.docSpaceToBufferSpace(docPt);
	}

	PVector BStoDS(PVector buffPt) {
		return this.coordinateSystem.bufferSpaceToDocSpace(buffPt);
	}

	PVector BStoDS(int x, int y) {
		return this.coordinateSystem.bufferSpaceToDocSpace(x,y);
	}


	Rect DStoBS(Rect docSpaceRect) {

		// turn into buffer space. This represents the portion of the image you need to iterate over
		PVector bufferSpaceTopLeft = DStoBS(docSpaceRect.getTopLeft());
		PVector bufferSpaceBottomRight = DStoBS(docSpaceRect.getBottomRight());
		return new Rect(bufferSpaceTopLeft, bufferSpaceBottomRight);

	}



	///////////////////////////////////////////////////////////////////////////////////////////////
	// debugging methods below here
	//
	//
	public void showProgress(boolean show, int totalNum) {
		progress.active = show;
		progress.reset(totalNum);
	}

	public void println(String s) {
		if(!debugFlag) {
			return;
		}
		System.out.println(s);
	}

	public void drawShadowExtentsVertices(PVector[] points, Color c) {

		GlobalSettings.getDocument().getMain().getVectorShapeDrawer().setDrawingStyle(c, c, 5);
		int n = 0;

		for(PVector p: points) {
			PVector bp = this.coordinateSystem.docSpaceToBufferSpace(p);
			System.out.println("shadow extents vertices " + n + " " + bp.toStr());
			GlobalSettings.getDocument().getMain().getVectorShapeDrawer().drawEllipse(bp.x, bp.y, 15, 15);
			n++;
		}

	}

	public void drawRectBufferSpace(Sprite sprite) {
		Rect spriteBufferRect = sprite.getDocumentBufferSpaceRect();
		Rect spriteDocSpaceRect = sprite.getDocSpaceRect();
		float aspect = spriteBufferRect.aspect();
		int id = sprite.getUniqueID();
		Color rc = MOColor.getKeyedRandomRGB(id,200);
		System.out.println("document docSpace rect " + GlobalSettings.getDocument().getCoordinateSystem().getDocumentRect().toStr());
		System.out.println("sprite id " + id + " cl " + rc.toString() + " buffer rect " + spriteBufferRect.toStr() + " doc Rect " + spriteDocSpaceRect.toStr());
		GlobalSettings.getDocument().getMain().drawRectBufferSpace(spriteBufferRect, new Color(0,0,0,0), rc, 10f);
	}

	
	/**
	 * Calculates a new interpolated value from ints using a control value in the range 0...255, so prefigured for raw alpha values
	 * @param valA - int in any range
	 * @param valB - int in any range
	 * @param alpha - in the range 0...255
	 * @return the interpolated value  between A & B
	 */
	protected static int lerpInt256(int valA, int valB, int alpha) {
		float af = alpha*0.003922f;
		return (int)MOMaths.lerp(af, valA, valB);

	}
	
	protected static int lerpInt65535(int valA, int valB, int alpha) {
		float af = alpha*0.00001525879f;
		return (int)MOMaths.lerp(af, valA, valB);

	}


}
