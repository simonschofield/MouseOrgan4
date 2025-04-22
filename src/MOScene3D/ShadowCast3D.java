package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.FloatImageRenderTarget;
import MOImage.MOPackedColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Ray3D;
import MOMaths.Rect;
import MOMaths.Rect3D;
import MOMaths.Util3D;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;

public class ShadowCast3D {

	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// renderShadow is called immediately after a sprite is pasted. In order to use this class, you first need to add a
	// FloatRendertarget and a greyscale render target sIt works out the shadow cast by that sprite on all sprites previously pasted
	// using a kind of ray tracing. It iterates over the scene already pasted, and added to the depthRender, and for each pixel works out
	// if a ray cast from that pixel towards the light is obscured by the new sprite being pasted. If so draw shadow in the shadowRender at that point. 
	// The light is assumed to be a parallel source.
	// There are a lot of optimisations here
	
	PVector lightDirection, negativeLightDirection;
	FloatImageRenderTarget depthRenderTarget;
	
	BufferedImageRenderTarget shadowRenderTarget;
	BufferedImage shadowRenderImage;
	ImageCoordinateSystem coordinateSystem;
	int shadowRenderImageWidth, shadowRenderImageHeight;
	byte[] shadowBuffer;
	
	
	SceneData3D sceneData3D;
	
	boolean printDebug = true;
	int progress = 0;
	
	public ShadowCast3D(SceneData3D scene3D, PVector lightDir, String nameOfDepthRender, String nameOfShadowRender, boolean addSceneSurfaceToDepth){
		if(scene3D == null) {
			System.out.println("ShadowCast3D  SceneData3D == null, please initialse first ");
			
		}
		sceneData3D = scene3D;
		
		lightDirection = lightDir.copy();
		negativeLightDirection = PVector.mult(lightDirection,-1);
		
		GlobalSettings.getDocument().addFloatRenderTarget(nameOfDepthRender, true, 1);
		GlobalSettings.getDocument().addRenderTarget(nameOfShadowRender, BufferedImage.TYPE_BYTE_GRAY);
		
		depthRenderTarget = GlobalSettings.getDocument().getFloatImageRenderTarget(nameOfDepthRender);
		shadowRenderTarget = GlobalSettings.getDocument().getBufferedImageRenderTarget(nameOfShadowRender);
		shadowRenderTarget.fillBackground(Color.WHITE);
		shadowRenderImage = shadowRenderTarget.getImage();
		shadowRenderImageWidth = shadowRenderImage.getWidth();
		shadowRenderImageHeight = shadowRenderImage.getHeight();
		
		shadowBuffer = ((DataBufferByte) shadowRenderImage.getRaster().getDataBuffer()).getData();
		
		coordinateSystem = depthRenderTarget.getCoordinateSystem();
		
		if(addSceneSurfaceToDepth) {
			addSceneSurfaceToDepth();
		}
	}
	
	private void addSceneSurfaceToDepth() {
		int w = coordinateSystem.getBufferWidth();
		int h = coordinateSystem.getBufferHeight();
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				PVector docSpacePt = coordinateSystem.bufferSpaceToDocSpace(x, y);
				float depthVal = sceneData3D.getDepth(docSpacePt);
				depthRenderTarget.setPixel(x, y, depthVal);
			}
		}
	}
	
	
	
	public void renderShadow(Sprite sprite) {
		if((progress%1000) == 0) {
			System.out.println("creating shadow for sprite " + progress );
		}
		progress++;
		
		// paste the sprite into the depth buffer
		depthRenderTarget.pasteFloatValues(sprite, sprite.getDepth());
		
		// paste the sprite as white into the shadow buffer to remove any shadows behind this sprite
		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.WHITE);
		
		
		// get the sprites Rect3D
		Rect3D spriteRectScene3D = sprite.getSpriteRectInScene3D();

		// get the sprite's base point. We will use this to form a "basePlane" (normal 0,1,0)
		PVector baseOfSprite = GlobalSettings.getSceneData3D().get3DSurfacePoint(sprite.getDocPoint());

		float shadowCastingSpriteDepth = sprite.getDepth();

		// for each of the 2 top sprite rect corners, project a ray in the lightDirection and see
		// where this intersects the basePlane
		PVector c1 = spriteRectScene3D.getCorners()[0];
		PVector c2 = spriteRectScene3D.getCorners()[1];

		Ray3D c1Ray = new Ray3D(c1, lightDirection);
		Ray3D c2Ray = new Ray3D(c2, lightDirection);

		// calculate where these hit the basePlane
		PVector c1RayBasePlaneIntersectionPoint = Util3D.getPlaneInYIntersection(c1Ray, baseOfSprite);
		PVector c2RayBasePlaneIntersectionPoint = Util3D.getPlaneInYIntersection(c2Ray, baseOfSprite);

		// The rayBaseIntersectionPoints and the sprites base points c3,c4 give us the world extents of the possible shadow
		// project these back into doc space
		PVector docSpaceExtents[] = new PVector[6];
		docSpaceExtents[0] = sceneData3D.world3DToDocSpace(c1RayBasePlaneIntersectionPoint);
		docSpaceExtents[1] = sceneData3D.world3DToDocSpace(c2RayBasePlaneIntersectionPoint);
		docSpaceExtents[2] = sprite.getDocSpaceRect().getTopLeft();
		docSpaceExtents[3] = sprite.getDocSpaceRect().getTopRight();
		docSpaceExtents[4] = sprite.getDocSpaceRect().getBottomLeft();
		docSpaceExtents[5] = sprite.getDocSpaceRect().getBottomRight();

		
		//drawShadowExtentsVertices(docSpaceExtents);
		
		// The hull of these vertices forms a 5-sided shape - A rectangle with a triangle attached to one side, depending on the light direction
		// We can call this the potential shadow region. Any previous sprite's pixels falling within the shadow region can receive shade. Any point
		// outside it cannot receive shade.
		// if the light ray direction is going left (-ve x) then the vertices of the shadow region are [2][3][5][4][2] for the sprite rect and [0][4][2][0] for the triangle
		// if the light ray direction is going right (+ve x) then the vertices of the shadow region are [2][3][5][4] for the sprite rect and [1][5][3][1] for the triangle
		// UNLESS the shape is cropped by the edge of the image!!!
		// anyway, the inside check for the triangles is more expensive than the ray casting
		
		// get the extents of these points
		Rect shadowRectDocSpaceExtents = Rect.getExtents(docSpaceExtents);

		
		
		
		// turn into buffer space. This represents the portion of the image you need to iterate over
		PVector bufferSpaceTopLeft = DStoBS(shadowRectDocSpaceExtents.getTopLeft());
		PVector bufferSpaceBottomRight = DStoBS(shadowRectDocSpaceExtents.getBottomRight());

		

		Rect shadowBoundingRect = new Rect(bufferSpaceTopLeft, bufferSpaceBottomRight);
		//drawShadowBoundingRect(shadowBoundingRect);
		//System.out.println("shadowBoundingRect " + shadowBoundingRect.toStr());
		
		
		
		for (int y = (int) shadowBoundingRect.top; y <= shadowBoundingRect.bottom; y++) {

			for (int x = (int) shadowBoundingRect.left; x <= shadowBoundingRect.right; x++) {

				// while we are iterating over the image in buffer space, it may be best to do all the calculations in doc space
				// so convert back to docSpace
				PVector shadowPixelDocSpace = BStoDS(x, y);

				// get the current depth render depth at that point. 
				float shadowRenderDepth = depthRenderTarget.getPixel(shadowPixelDocSpace);
				PVector shadowRenderPoint3D = sceneData3D.get3DVolumePoint(shadowPixelDocSpace, shadowRenderDepth);

				// check to see if there is something there to receive a shadow. Copy the surface z values in
				// first if you want the surface to receive shadows.
				if (shadowRenderDepth == 0) continue; 
				//debug("here1");
				// check to see if you are trying to shade the new sprite
				if (shadowRenderDepth == shadowCastingSpriteDepth) continue; 
				//debug("here2");
				// if the pixel already in shade?? If so, then continue. Previously shadowed pixel cannot be re-lit.
				int shadowval = shadowBuffer[x+y*shadowRenderImageWidth] & 0xFF;
				if (shadowval < 127) continue;
				//debug("here3 shadow val = " + shadowval);
				// alternative byte handling, maybe better/works?
				// byte shadowval = shadowBuffer[x+y*shadowRenderImageWidth] & 0xFF;
				
				// now cast a ray from this pixel along the light ray (reverse direction).
				// does this light ray intersect the spriteRect3D?? If so, does it intersect a visible pixel?? If so draw a shadow at this point.
				Ray3D rayTowardsLight = new Ray3D(shadowRenderPoint3D, lightDirection);

				if ( spriteRectScene3D.intersects(rayTowardsLight )) {
					//debug("here4");
					// get the norm point within the rectangle
					PVector intersectionPoint = rayTowardsLight.intersectionPoint.copy();

					PVector normalisedLocationWithinSpriteRect3D = spriteRectScene3D.norm(intersectionPoint);

					int pixelLocationInSpriteImageX =  (int)(sprite.getImageWidth() * normalisedLocationWithinSpriteRect3D.x);
					int pixelLocationInSpriteImageY =  (int)(sprite.getImageHeight() * normalisedLocationWithinSpriteRect3D.y);

					pixelLocationInSpriteImageX = MOMaths.constrain(pixelLocationInSpriteImageX, 0, sprite.getImageWidth()-1);
					pixelLocationInSpriteImageY = MOMaths.constrain(pixelLocationInSpriteImageY, 0, sprite.getImageHeight()-1);
					
					int spriteRGBA = sprite.getMainImage().getRGB(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY);
					int alpha = MOPackedColor.getAlpha(spriteRGBA);

					if(alpha > 16) {
						// do a darken-only
						int loc = x + y * shadowRenderImageWidth;
						int existingValue = (shadowBuffer[loc] & 0xFF);
						int newValue =   ((255-alpha));
						if(newValue < existingValue) {
							byte newByteVal = (byte) (newValue);
							//if(existingValue!=255) System.out.println("alpha " + alpha + " existing value " + existingValue + " newValue (as int 255-alpha) " + newValue + " newByteVal " + newByteVal);
							shadowBuffer[loc] = newByteVal;
						} 
					}

					

				}// end if

			}// end x loop

		}//end y loop

	}// end method
	
	
	void debug(String s) {
		if(printDebug==false) return;
		System.out.println(s);
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
	
	
	private void drawShadowExtentsVertices(PVector[] points) {
		GlobalSettings.getDocument().getMain().getVectorShapeDrawer().setDrawingStyle(Color.blue, Color.blue, 5);
		int n = 0;
		for(PVector p: points) {
			PVector bp = this.coordinateSystem.docSpaceToBufferSpace(p);
			System.out.println("shadow extents vertices " + n + " " + bp.toStr());
			GlobalSettings.getDocument().getMain().getVectorShapeDrawer().drawEllipse(bp.x, bp.y, 15, 15);
			n++;
		}
		
	}
	
	private void drawShadowBoundingRect(Rect r) {
		GlobalSettings.getDocument().getMain().drawRectBufferSpace(r, new Color(0,0,0,0), new Color(255,0,0,127), 10f);
		
		
	}
	
	
	
	
}
