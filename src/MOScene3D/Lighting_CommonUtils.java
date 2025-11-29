package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.FloatImageRenderTarget;
import MOImage.ByteImageGetterSetter;
import MOImage.MOColor;
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
	ByteImageGetterSetter  shadowImageGetSet;

	FloatImageRenderTarget depthRenderTarget;
	PVector lightDirection;
	PVector negativeLightDirection;

	public boolean debugFlag = false;
	Progress progress;


	// this is used to determine whether or not a particular sprite in a ROI session contributes to the image
	// Because shadows extend beyond the sprite itself, sprites outside the ROI may contribute, so should be included in the
	// roi session sprite batch file.
	Rect theDoumentDocSpaceRect;


	public Lighting_CommonUtils(SceneData3D sd3d, String nameOfShadowRender) {

		if(sd3d == null) {
			System.out.println("Lighting_CommonUtils  SceneData3D == null, please initialse first ");

		}

		sceneData3D = sd3d;
		coordinateSystem = GlobalSettings.getDocument().getCoordinateSystem();
		theDoumentDocSpaceRect = coordinateSystem.getDocumentRect();


		GlobalSettings.getDocument().addRenderTarget(nameOfShadowRender, BufferedImage.TYPE_BYTE_GRAY);
		shadowRenderTarget = GlobalSettings.getDocument().getBufferedImageRenderTarget(nameOfShadowRender);
		shadowRenderTarget.fillBackground(Color.WHITE);
		shadowImageGetSet = new ByteImageGetterSetter(shadowRenderTarget.getBufferedImage());
	}


	protected void initialiseDepthRender(String nameOfDepthRender, boolean addSceneSurfaceToDepthRender, PVector lightDir) {

		GlobalSettings.getDocument().addFloatRenderTarget(nameOfDepthRender, true, 1);
		depthRenderTarget = GlobalSettings.getDocument().getFloatImageRenderTarget(nameOfDepthRender);
		lightDirection = lightDir.copy();
		negativeLightDirection = PVector.mult(lightDir, -1);
		//System.out.println("coordinateSystem rect = " + coordinateSystem.getDocumentRect().toStr() + " getCurrentROIDocRect " + theROIManangerDocSpaceRect.toStr());
		if(addSceneSurfaceToDepthRender) {
			addSceneSurfaceToDepth();
		}
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




}
