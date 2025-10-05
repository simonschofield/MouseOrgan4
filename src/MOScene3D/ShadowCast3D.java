package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import MOApplication.ROIManager;
import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.FloatImageRenderTarget;
import MOImage.BilinearBufferedImageSampler;
import MOImage.ByteImageGetterSetter;
import MOImage.ImageCoordinate;
import MOImage.ImageDimensions;
import MOImage.ImageProcessing;
import MOImage.MOColor;
import MOImage.MOPackedColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;
import MOUtils.Progress;

public class ShadowCast3D {


	
	PVector lightDirection, negativeLightDirection;
	FloatImageRenderTarget depthRenderTarget;

	BufferedImageRenderTarget shadowRenderTarget;
	BufferedImage shadowRenderImage;
	ImageCoordinateSystem coordinateSystem;

	ByteImageGetterSetter  shadowImageGetSet;
	//BufferedImage projectedLightImage;

	// this is used to determine whether or not a particular sprite in a ROI session contributes to the image
	// Because shadows extend beyond the sprite itself, sprites outside the ROI may contribute, so should be included in the
	// roi session sprite batch file.
	Rect theDoumentDocSpaceRect;
	
	SceneData3D sceneData3D;


	boolean debugFlag = true;

	Progress progress;

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// The shadow render is the primary output of this class, and will be saved automatically by the render saver at the
	// end of the session. When adding render targets, you can either have a pre-created target, which may be re-used
	// by other operations, or, if the render targets have not been created prior to using this class; these will be created at instantiation.
	// The depth render target may not be required to be kept, so the user should delete it before the renders are saved
	//
	// To create any mask, all sprites in the render must be passed and added to the mask, even if they are not shadow-processed, they need to contribute to the mask
	// with a non-contributing sprite colour (usually white or black).
	// 
	// The nature of the output shadow image is in development. They could be .. 
	// For radialIntershading the output is the amount of shade per pixel and could be represented in two ways
	// 1/ A greyscale image, initially white, where the shading is subtracted from the current value in the image. sprites are added as white stencils once the shading has occurred. THis can then be viewed
	//    quickly for ease while rendering, and could form the basis for an underlying image for top-image multiplication, inverted could form a separate shadow mask.
	// 2/ An ARGB image, black with alpha of zero set everywhere. As the shadow is added, the alpha is set with higher values, so the black becomes "revealed". Sprites are added with alpha zero 
	//   and black in-fill after shading has occurred, so are "revealed " by subsequent shadowing. 
	// For BasePointLighting, the output is the amount of light per pixel
	// 2/ 
	//
	//
	public ShadowCast3D(SceneData3D scene3D, PVector lightDir, String nameOfDepthRender, String nameOfShadowRender, boolean addSceneSurfaceToDepth){
		if(scene3D == null) {
			System.out.println("ShadowCast3D  SceneData3D == null, please initialse first ");

		}
		sceneData3D = scene3D;

		Range worldY = sceneData3D.depthBuffer3d.worldYExtrema;
		System.out.println("worldY extrama " + worldY.toStr() );
		float sceneYMin = sceneData3D.depthBuffer3d.worldYExtrema.getUpper();


		lightDirection = lightDir.copy();
		negativeLightDirection = PVector.mult(lightDirection,-1);

		GlobalSettings.getDocument().addFloatRenderTarget(nameOfDepthRender, true, 1);
		GlobalSettings.getDocument().addRenderTarget(nameOfShadowRender, BufferedImage.TYPE_BYTE_GRAY);

		depthRenderTarget = GlobalSettings.getDocument().getFloatImageRenderTarget(nameOfDepthRender);
		shadowRenderTarget = GlobalSettings.getDocument().getBufferedImageRenderTarget(nameOfShadowRender);
		shadowRenderTarget.fillBackground(Color.WHITE);
		shadowImageGetSet = new ByteImageGetterSetter(shadowRenderTarget.getBufferedImage());


		coordinateSystem = depthRenderTarget.getCoordinateSystem();
		theDoumentDocSpaceRect = coordinateSystem.getDocumentRect();
		
		//System.out.println("coordinateSystem rect = " + coordinateSystem.getDocumentRect().toStr() + " getCurrentROIDocRect " + theROIManangerDocSpaceRect.toStr());
		if(addSceneSurfaceToDepth) {
			addSceneSurfaceToDepth();
		}

		progress = new Progress("Shadows: ");
	}
	
	
	

	public void showProgress(boolean show, int totalNum) {
		progress.active = show;
		progress.reset(totalNum);
	}

	private void addSceneSurfaceToDepth() {
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


	////////////////////////////////////////////////////////////////////////////////////////////////
	// radialInterShadowing darkens nearby existing substance within a set 3d distance from the new sprite to create a soft "inter-shadowing" effect.
	// The process is sped up by calculating distances against against a "shadow line" that is a simple 3D line from the base of the sprite, to its tip, including rotation - the shadePointSource
	// The shadePointSource's height is defined parametrically by shadePointSourceHeight, where 0 is the very base of the sprite and 1 is the very top.
	// The radius is in 3D units, and has a drop-off of shade intensity defined by radius with dropOffGamma
	//
	// Because we only want lightening ABOVE the shade source point and not below it, we calculate d as the distance to a line segment between the sprite base point, and the sprite shade source point. 
	//
	// The amount of shade is calculated for point at distance d from shadeSource. 
	// normalisedShade = norm(d, radius, 0); // so when d == radius,normalisedShade = 0,  and when  d == 0 ,normalisedShade = 1
	// shadeAmount = pow(normalisedShade, dropOffGamma) // so when gamma == 1, drop off in linear, gamma < 1, drops off to maximum quickly at start. Gamma > 1 drops off only toward the end
	// 
	// The return value relates to the contribution of the sprite's shadow to the image. Using a ROI, a sprite may be completely cropped from the ROI's image, but the shadow still contributes.
	// Returns false if the shadow is completely outside the current doc space rect. Returns true otherwise.
	//

	public boolean radialInterShadowing(Sprite sprite, float maxShadowContribution, float shadowLineHeightProportion, float radius, float dropOffGamma, boolean useLightDirection) {
		debugFlag=false;
		

		
		float depth = sprite.getDepth();
		float spriteSize3D = sprite.getSizeInScene();
		
		
		
		// get the document space sprite top and bottom points, mapped into the scene
		
		PVector mappedSpriteBasePointDocSpace = sprite.mapNormalisedLocalSpritePointToDocSpace( 0.5f, 1.0f);
		PVector mappedSpriteTopPointDocSpace = sprite.mapNormalisedLocalSpritePointToDocSpace( 0.5f, 0);

		
		
		PVector shadowLineBasePoint3D = sceneData3D.get3DVolumePoint(mappedSpriteBasePointDocSpace, depth);
		PVector mappedSpriteTopPoint3D = sceneData3D.get3DVolumePoint(mappedSpriteTopPointDocSpace, depth);

		// interpolate over this to get the shadowLineTopPoint
		PVector shadowLineTopPoint3D = PVector.lerp(shadowLineBasePoint3D, mappedSpriteTopPoint3D, shadowLineHeightProportion);
		
		Line3D shadowLine = new Line3D(shadowLineBasePoint3D, shadowLineTopPoint3D);
		
		
		

		//System.out.println("shadowLine " + shadowLine.toStr());

		// establish screen-bounds of region to process
		// 
		float r = radius;
		PVector lineBase = shadowLine.p1.copy();
		PVector lineBasePlusRX = new PVector(lineBase.x+r, lineBase.y, lineBase.z);
		PVector lineBaseMinusRX = new PVector(lineBase.x-r, lineBase.y, lineBase.z);
		
		PVector lineTop = shadowLine.p2.copy();
		PVector lineTopPlusRX = new PVector(lineTop.x+r, lineTop.y, lineTop.z);
		PVector lineTopMinusRX = new PVector(lineTop.x-r, lineTop.y, lineTop.z);
		PVector lineTopPlusRY = new PVector(lineTop.x, lineTop.y+r, lineTop.z);
		
		PVector docSpaceExtents[] = new PVector[7];
		docSpaceExtents[0] = sceneData3D.world3DToDocSpace(lineBase); // the base point
		docSpaceExtents[1] = sceneData3D.world3DToDocSpace(lineTop); // the top point
		docSpaceExtents[2] = sceneData3D.world3DToDocSpace(lineBasePlusRX); // the base plus radius in x
		docSpaceExtents[3] = sceneData3D.world3DToDocSpace(lineBaseMinusRX); // the base minus radius in x
		docSpaceExtents[4] = sceneData3D.world3DToDocSpace(lineTopPlusRX); // the top plus radius in x
		docSpaceExtents[5] = sceneData3D.world3DToDocSpace(lineTopMinusRX); // the top minus radius in x
		docSpaceExtents[6] = sceneData3D.world3DToDocSpace(lineTopPlusRY); // the top point plus radius in y
		
		Rect shadowRectDocSpaceExtents = Rect.getExtents(docSpaceExtents);
		
		// check to see if this contributes to the image at all
		if( shadowRectDocSpaceExtents.intersects(theDoumentDocSpaceRect) == false ) return false;
		
		
		
		
		
		
		Rect boundingRectBufferSpace = DStoBS(shadowRectDocSpaceExtents);
		
		//if(debugFlag && sprite.getRandomStream().probabilityEvent(0.01f)) {
		if(debugFlag) {

			
			Color c = MOColor.getRandomRGB(255);
			//Color c = Color.darkGray;
			Util3D.drawLineWorldSpace(lineBase, lineTop, c, 3);
			GlobalSettings.getDocument().getMain().drawRect_DocSpace(shadowRectDocSpaceExtents, new Color(0,0,0,0), c, 3f);
			shadowRenderTarget.drawRect_DocSpace(shadowRectDocSpaceExtents, new Color(0,0,0,0), c, 3f);
		}

		float radiusSquared = radius*radius;
		PVector lightDirectioProjected = new PVector(lightDirection.x, 0, lightDirection.z).normalize();
		
		for (int y = (int) boundingRectBufferSpace.top; y <= boundingRectBufferSpace.bottom; y++) {

			for (int x = (int) boundingRectBufferSpace.left; x <= boundingRectBufferSpace.right; x++) {
				// get the current depth render depth at that point. 
				// while we are iterating over the image in buffer space, it may be best to do all the calculations in doc space
				// so convert back to docSpace

				if( shadowRenderTarget.getCoordinateSystem().isInsideBufferSpace(x, y)==false) continue;

				PVector shadowPixelDocSpace = BStoDS(x, y);

				// get the current depth render depth at that point. 
				float shadowRenderDepth = depthRenderTarget.getPixel(x,y);

				// check to see if there is something there to receive a shadow. Copy the surface z values in
				// first if you want the surface to receive shadows.
				if (shadowRenderDepth == 0) continue; 


				PVector shadowRenderPoint3D = sceneData3D.get3DVolumePoint(shadowPixelDocSpace, shadowRenderDepth);

				
				PVector clostestPointOnShadowLine = shadowLine.closestPointOnLine(shadowRenderPoint3D);
				
				float distanceSqFromShadowLine = shadowRenderPoint3D.distSq(clostestPointOnShadowLine);
				if(distanceSqFromShadowLine>radiusSquared) continue;
				
				
				float distanceFromShadowLine = (float) Math.sqrt(distanceSqFromShadowLine);
				float shadowAmount01 = MOMaths.map(distanceFromShadowLine, 0, radius, maxShadowContribution, 0);
				shadowAmount01 = MOMaths.constrain(shadowAmount01,0,1);
				
				// if the shadow intensity is very low, ignore
				if(shadowAmount01 < 0.0078f) continue;
				shadowAmount01 = MOMaths.constrain(shadowAmount01, 0.0078f,1);
				
				
				// add in the lightDirection here
				if(useLightDirection) {
					PVector thisRay  = PVector.sub(shadowRenderPoint3D, clostestPointOnShadowLine);
					thisRay.y = 0; // projected onto the y=0 plane
					thisRay.normalize();
					
					float amountOfBias = thisRay.dot(lightDirectioProjected);
					// when the ray is in-line with the light, the dot/cos value will be 1
					shadowAmount01 *= amountOfBias;
				}
				
				

				float shadowAmount01WithGamma = (float) Math.pow(shadowAmount01, dropOffGamma);
				contributeShadowToImage( x, y, shadowAmount01WithGamma);

			}
		}
		
		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.WHITE);
		depthRenderTarget.pasteFloatValues(sprite, depth);
		
		return true;
		
		//PVector lineBaseDs = sceneData3D.world3DToDocSpace(basePoint3D);
		//PVector lineTopDS = sceneData3D.world3DToDocSpace(topPoint3D);
		
		
		//GlobalSettings.getDocument().getMain().drawLine(lineBaseDs, lineTopDS, MOColor.getRandomRGB(255), 3);
		
	}
	
	void contributeShadowToImage(int x,int y, float shadowAmount01) {
		// the shadowAmout01 is between 0 and 1, where 1 would result in total shadow (black).
		// Shadow is added cumulatively to the image, in that it is "added" to the previous amount of shadow already there.
		//
		int existingPixelValue = shadowImageGetSet.getPixel(x, y);
		float existingShadowValue01 = 1-existingPixelValue*0.003922f;
		
		int newPixelValue = (int)  ((1-(existingShadowValue01 + shadowAmount01) ) * 255);
		newPixelValue = (int)MOMaths.constrain(newPixelValue,0,255);
		shadowImageGetSet.setPixel(x, y, newPixelValue);
	}
	
	
	float getRadialDistanceSquaredFromShadowLine(Line3D shadowLine, PVector pointOnSurface) {
		// treats the line as a sort of capsule. Above the top of the line, use distance to the top point
		// below the top point, use distance to line. As the line is straight up in Y, we can use distXZ
		PVector closestPointOnLine = shadowLine.closestPointOnLine(pointOnSurface);

		return pointOnSurface.distSq(closestPointOnLine);
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

	void println(String s) {
		if(debugFlag==false) return;
		System.out.println(s);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// debugging methods below here
	//
	//
	
	
	
	private void drawShadowExtentsVertices(PVector[] points, Color c) {

		GlobalSettings.getDocument().getMain().getVectorShapeDrawer().setDrawingStyle(c, c, 5);
		int n = 0;

		for(PVector p: points) {
			PVector bp = this.coordinateSystem.docSpaceToBufferSpace(p);
			System.out.println("shadow extents vertices " + n + " " + bp.toStr());
			GlobalSettings.getDocument().getMain().getVectorShapeDrawer().drawEllipse(bp.x, bp.y, 15, 15);
			n++;
		}

	}

	private void drawRectBufferSpace(Sprite sprite) {
		Rect spriteBufferRect = sprite.getDocumentBufferSpaceRect();
		Rect spriteDocSpaceRect = sprite.getDocSpaceRect();
		float aspect = spriteBufferRect.aspect();
		int id = sprite.getUniqueID();
		Color rc = MOColor.getKeyedRandomRGB(id,200);
		System.out.println("document docSpace rect " + GlobalSettings.getDocument().getCoordinateSystem().getDocumentRect().toStr());
		System.out.println("sprite id " + id + " cl " + rc.toString() + " buffer rect " + spriteBufferRect.toStr() + " doc Rect " + spriteDocSpaceRect.toStr());
		GlobalSettings.getDocument().getMain().drawRectBufferSpace(spriteBufferRect, new Color(0,0,0,0), rc, 10f);
	}



	///////////////////////////////////////////////////////////////////////////////////////////////
	// casts a hard shadow of THIS sprite onto previously pasted sprites by using the depthTexture to
	// calculated which pixels in the previous sprites are in shadow from the light source from THIS sprite.
	//
	public void castHardShadow(Sprite sprite, boolean contributes, Color nonContributingColor) {

		if(contributes) {
			castHardShadow(sprite);
		} else {
			depthRenderTarget.pasteFloatValues(sprite, sprite.getDepth());
			shadowRenderTarget.pasteSprite_ReplaceColour(sprite, nonContributingColor);
		}


	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// castHardShadow is called immediately after a sprite is pasted. In order to use this class, you first need to add a
	// FloatRendertarget and a greyscale render target . It works out the shadow cast by that sprite on all sprites previously pasted
	// using a kind of ray tracing. It iterates over the scene already pasted, and added to the depthRender, and for each pixel works out
	// if a ray cast from that pixel towards the light is obscured by the new sprite being pasted. If so then shadow is contributed to the shadow REnder buffer at that point. 
	// The light is assumed to be a parallel source.
	// There are a lot of optimisations used.

	public void castHardShadow(Sprite sprite) {
		boolean debug = false;
		progress.update();


		// paste the sprite into the depth buffer
		depthRenderTarget.pasteFloatValues(sprite, sprite.getDepth());

		// paste the sprite as white into the shadow buffer to remove any shadows behind this sprite
		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.WHITE);


		// get the sprites Rect3D
		BillboardRect3D spriteRectScene3D = sprite.getSpriteBillboardRect3D();

		BilinearBufferedImageSampler spriteImageSampler = new BilinearBufferedImageSampler(sprite.getMainImage());



		float shadowCastingSpriteDepth = sprite.getDepth();

		// for each of the 2 top sprite rect corners, project a ray in the lightDirection and see
		// where this intersects the basePlane
		PVector c1 = spriteRectScene3D.getCorners()[0];
		PVector c2 = spriteRectScene3D.getCorners()[1];

		Ray3D c1Ray = new Ray3D(c1, lightDirection);
		Ray3D c2Ray = new Ray3D(c2, lightDirection);

		PVector c1PointOnSurface = sceneData3D.raySurfaceIntersection(c1Ray);

		PVector c2PointOnSurface = sceneData3D.raySurfaceIntersection(c2Ray);


		// The rayBaseIntersectionPoints and the sprites base points c3,c4 give us the world extents of the possible shadow
		// project these back into doc space
		PVector docSpaceExtents[] = new PVector[6];
		docSpaceExtents[0] = sceneData3D.world3DToDocSpace(c1PointOnSurface);
		docSpaceExtents[1] = sceneData3D.world3DToDocSpace(c2PointOnSurface);
		docSpaceExtents[2] = sprite.getDocSpaceRect().getTopLeft();
		docSpaceExtents[3] = sprite.getDocSpaceRect().getTopRight();
		docSpaceExtents[4] = sprite.getDocSpaceRect().getBottomLeft();
		docSpaceExtents[5] = sprite.getDocSpaceRect().getBottomRight();

		if(debug) {
			Color ranCol = MOColor.getRandomRGB(255);
			Util3D.drawRect3D(spriteRectScene3D, ranCol, 5);
			drawShadowExtentsVertices(docSpaceExtents, ranCol);
		}



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



		for (int y = (int) (shadowBoundingRect.top-0.5f); y < shadowBoundingRect.bottom+0.5f; y++) {

			for (int x = (int) (shadowBoundingRect.left-0.5f); x < shadowBoundingRect.right+0.5f; x++) {

				// while we are iterating over the image in buffer space, it may be best to do all the calculations in doc space
				// so convert back to docSpace

				if( shadowRenderTarget.getCoordinateSystem().isInsideBufferSpace(x, y)==false) continue;

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
				int shadowval = shadowImageGetSet.getPixel(x, y);


				// now cast a ray from this pixel along the light ray (reverse direction).
				// does this light ray intersect the spriteRect3D?? If so, does it intersect a visible pixel?? If so draw a shadow at this point.
				Ray3D rayTowardsLight = new Ray3D(shadowRenderPoint3D, lightDirection);

				if ( spriteRectScene3D.intersects(rayTowardsLight )) {
					//debug("here4");
					// get the norm point within the rectangle
					PVector intersectionPoint = rayTowardsLight.intersectionPoint.copy();
					PVector normalisedLocationWithinSpriteRect3D = spriteRectScene3D.norm(intersectionPoint);


					float pixelLocationInSpriteImageX =  sprite.getImageWidth() * normalisedLocationWithinSpriteRect3D.x;
					float pixelLocationInSpriteImageY =  sprite.getImageHeight() * (1-normalisedLocationWithinSpriteRect3D.y);  // need to flip the normalised position as the Y's in 2D and 3D are in opposite directions

					int alpha = spriteImageSampler.getAlphaBilin(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY);

					if(alpha > 1) {
						// do a darken-only
						int existingValue = shadowImageGetSet.getPixel(x, y);
						int newValue =   ((255-alpha));
						if(newValue < existingValue) {
							shadowImageGetSet.setPixel(x, y,newValue);
						} 
					}

				}// end if

			}// end x loop

		}//end y loop

	}// end method


	
	
	
}
