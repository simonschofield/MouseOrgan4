package MOScene3D;

import java.awt.Color;

import MOImage.BilinearBufferedImageSampler;
import MOImage.MOColor;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.Progress;

public class Lighting_HardShadow3D  extends Lighting_CommonUtils {



	PVector lightDirection, negativeLightDirection;



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
	public Lighting_HardShadow3D(SceneData3D scene3D, String nameOfShadowRender, String nameOfDepthRender,  PVector lightDir,   boolean addSceneSurfaceToDepth){
		super(scene3D, nameOfShadowRender);

		Range worldY = sceneData3D.depthBuffer3d.worldYExtrema;
		System.out.println("worldY extrama " + worldY.toStr() );
		float sceneYMin = sceneData3D.depthBuffer3d.worldYExtrema.getUpper();

		initialiseDepthRender( nameOfDepthRender,  addSceneSurfaceToDepth, lightDirection) ;

		progress = new Progress("Lighting_HardShadow3D: ");
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

				if( !shadowRenderTarget.getCoordinateSystem().isInsideBufferSpace(x, y)) {
					continue;
				}

				PVector shadowPixelDocSpace = BStoDS(x, y);

				// get the current depth render depth at that point.
				float shadowRenderDepth = depthRenderTarget.getPixel(shadowPixelDocSpace);
				PVector shadowRenderPoint3D = sceneData3D.get3DVolumePoint(shadowPixelDocSpace, shadowRenderDepth);

				// check to see if there is something there to receive a shadow. Copy the surface z values in
				// first if you want the surface to receive shadows.
				//debug("here1");
				// check to see if you are trying to shade the new sprite
				if ((shadowRenderDepth == 0) || (shadowRenderDepth == shadowCastingSpriteDepth)) {
					continue;
				}
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
