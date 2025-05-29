package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.FloatImageRenderTarget;
import MOImage.BilinearBufferedImageSampler;
import MOImage.ByteImageGetterSetter;
import MOImage.ImageCoordinate;
import MOImage.ImageDimensions;
import MOImage.MOColor;
import MOImage.MOPackedColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
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
	
	ByteImageGetterSetter  shadowImageGetSet;
	
	SceneData3D sceneData3D;
	
	boolean debugFlag = true;
	int progress = 0;
	
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
		shadowImageGetSet = new ByteImageGetterSetter(shadowRenderTarget.getImage());
		
		
		coordinateSystem = depthRenderTarget.getCoordinateSystem();
		
		if(addSceneSurfaceToDepth) {
			addSceneSurfaceToDepth();
		}
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
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// casts a shadow of THIS sprite onto previously pasted sprites by using the depthTexture to
	// calculated which pixels in the previous sprites are in shadow from the light source.
	//
	public void castShadow(Sprite sprite) {
		boolean debug = false;
		if((progress%1000) == 0) {
			System.out.println("addCastShadowToShadowRender " + progress );
		}
		progress++;
		
		
		// paste the sprite into the depth buffer
		depthRenderTarget.pasteFloatValues(sprite, sprite.getDepth());
		
		// paste the sprite as white into the shadow buffer to remove any shadows behind this sprite
		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.WHITE);
		
		
		// get the sprites Rect3D
		BillboardRect3D spriteRectScene3D = sprite.getSpriteBillboardRect3D();
		
		BilinearBufferedImageSampler spriteImageSampler = new BilinearBufferedImageSampler(sprite.getMainImage());

		// get the sprite's base point. We will use this to form a "basePlane" (normal 0,1,0)
		PVector baseOfSprite = sceneData3D.get3DSurfacePoint(sprite.getDocPoint());

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

		
		float c1SearchLineLength = c1.dist(c1RayBasePlaneIntersectionPoint);
		PVector c1PointOnSurface = findClosestPointOnSurface(c1Ray, baseOfSprite, c1SearchLineLength, 200);
		
		float c2SearchLineLength = c2.dist(c2RayBasePlaneIntersectionPoint);
		PVector c2PointOnSurface = findClosestPointOnSurface(c2Ray, baseOfSprite, c2SearchLineLength, 200);
		
		
		
		
		
		
		
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
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// adds in the "projected light" shading, implied by a user-specified surface-texture, to THIS sprite. The shading is added to the shadowRender image using "darken-only".
	// This is done by calculating the light-ray from each pixel on the newly pasted sprite (THIS sprite) back to
	// the surface texture point in the surface-texture.
	// Finding the intersection point between the light-ray and the surface in 3D is tricky as the surface is non-planar (undulating). 
	// We could use ray-marching on each sprite pixel, but this is slow. Another faster approximation may be to use the "basePlane" idea using in casting Hard shadows.
	// I  that algorithm, a plane in Y is established at the base-point of the sprite in 3D. This is used to project the shadow extents onto, and thus calculate
	// the  bounding box in 2D of the part of the scene to be iterated over to calculate the cast shadow. We can use the same principle and establish
	// a ray-receiving plane as a proxy for the actual surface. Rather than using an axis-aligned in Y plane, which may be very inaccurate for some surfaces,
	// we could use a plane defined by three points: the base point of the sprite and the surface intersection of the light-ray through the top corners of the sprite's 3D rect.
	// The intersection in these cases is  approximated using an iterative technique.
	//
	public void projectedLight(Sprite sprite, String projectLightTextureName) {
		debugFlag=true;
		if((progress%1000) == 0) {
			System.out.println("addProjectedLightToShadowRender " + progress );
		}
		progress++;
		
		// dont need z render depth texture for this algorithm - just usesd the scenedata
		
		// set the projected shadow render
		sceneData3D.setCurrentRenderImage(projectLightTextureName);
		
		
		// paste the sprite as white into the shadow buffer 
		// Clear previous shadowcasts and projected-light within the sprites region by pasting white in sprite region
		// For each sprite...
		// Do shadow casting - this will not affect this sprite's region, so this sprite remains white
		// Do projected-light on this sprite, this only affects the sprites region
		// Do next sprite
		// so, as long as we do it in the order shadow-casts then projected-light, things should be OK
		// The sprites region may have already been cleared by the shadow-cast, but if we are using method this independently then we need to clear
		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.WHITE);
				
		// get the sprites Rect3D
		BillboardRect3D spriteRectScene3D = sprite.getSpriteBillboardRect3D();		
		
		// get the sprite's base point. We will use this to form a "basePlane" (normal 0,1,0)
		PVector baseOfSprite = sceneData3D.get3DSurfacePoint(sprite.getDocPoint());
				
		float spriteDepth = sprite.getDepth();

		// for each of the 2 top sprite rect corners, project a ray in the lightDirection and see
		// where this intersects the basePlane
		PVector c1 = spriteRectScene3D.getCorners()[0];
		PVector c2 = spriteRectScene3D.getCorners()[1];
		
		Ray3D c1Ray = new Ray3D(c1, lightDirection);
		Ray3D c2Ray = new Ray3D(c2, lightDirection);
		
		
		
		// calculate where these hit the basePlane
		PVector c1RayBasePlaneIntersectionPoint = Util3D.getPlaneInYIntersection(c1Ray, baseOfSprite);
		PVector c2RayBasePlaneIntersectionPoint = Util3D.getPlaneInYIntersection(c2Ray, baseOfSprite);
		
		
		
		// to construct the surface proxy plane, we need 3 points,
		// P1 = the sprites base point, which we already have
		// P2 = the closest point on the surface on the line3D c1-> c1RayBasePlaneIntersectionPoint
		// P3 = the closest point on the surface on the line3D c2-> c2RayBasePlaneIntersectionPoint
		// we will approximate these by taking a number of 3D points along the line3D and seeing how close
		// its 2D projection is to the surface at that point. The closest 3D point is captured.
		
		Line3D c1Line = new Line3D(c1, c1RayBasePlaneIntersectionPoint);
		Line3D c2Line = new Line3D(c2, c2RayBasePlaneIntersectionPoint);
		
		
		
		int numSamples = 300;
		
		// this is where the master and ROI drift apart.....
		PVector closestPtOnLineC1ToSurface = findClosestPointOnLineToSurface(c1Line, numSamples);
		PVector closestPtOnLineC2ToSurface = findClosestPointOnLineToSurface(c2Line, numSamples);
		
		
		//Color c = MOColor.getRandomRGB(255);
		//draw3DPoint(baseOfSprite, c, 10);
		//draw3DPoint(closestPtOnLineC1ToSurface, c, 10);
		//draw3DPoint(closestPtOnLineC2ToSurface, c, 10);
		
		
		Plane3D proxyPlane = new Plane3D(baseOfSprite, closestPtOnLineC1ToSurface,  closestPtOnLineC2ToSurface);
		
		
		if(this.debugFlag) {
			//System.out.println("Sprite BillBoard rect   " + spriteRectScene3D.toStr());
			//System.out.println("spriteRectScene3D corner 1  = " + c1.toStr());
			//System.out.println("spriteRectScene3D corner 2  = " + c2.toStr());
			//System.out.println("c1Ray  = " + c1Ray.toStr());
			//System.out.println("c2Ray  = " + c2Ray.toStr());
			//System.out.println("c1RayBasePlaneIntersectionPoint  = " + c1RayBasePlaneIntersectionPoint.toStr());
			//System.out.println("c2RayBasePlaneIntersectionPoint  = " + c2RayBasePlaneIntersectionPoint.toStr());
			//System.out.println("closestPtOnLineC1ToSurface  = " + closestPtOnLineC1ToSurface.toStr());
			//System.out.println("closestPtOnLineC2ToSurface  = " + closestPtOnLineC2ToSurface.toStr());
			//System.out.println("Proxy Plane  = " + proxyPlane.toStr());
		}
		
		
		BufferedImage spriteImage = sprite.getMainImage();
		int spriteBufferW = spriteImage.getWidth();
		int spriteBufferH = spriteImage.getHeight();
		Rect spriteBoundingRectBufferSpace = sprite.getDocumentBufferSpaceRect();
		
		ImageDimensions spriteImageDimensions = new ImageDimensions(spriteBufferW,spriteBufferH);
		
		
		
		// drawRectBufferSpace(sprite);
		//System.out.println("shadowBoundingRect " + shadowBoundingRect.toStr());
		
		// we are going to iterate over the newly pasted sprite now....
		// x and y are the document buffer space points being iterated though
		for (int y = (int) spriteBoundingRectBufferSpace.top; y <= spriteBoundingRectBufferSpace.bottom; y++) {

			for (int x = (int) spriteBoundingRectBufferSpace.left; x <= spriteBoundingRectBufferSpace.right; x++) {
				
				// check to see if there is any substance to this pixel
				int pixelLocationInSpriteImageX = x - (int)spriteBoundingRectBufferSpace.left;
				int pixelLocationInSpriteImageY = y - (int)spriteBoundingRectBufferSpace.top;
				
				if( spriteImageDimensions.isLegalIndex(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY) == false) {
					//
					ImageCoordinate ic = spriteImageDimensions.constrain(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY);
					pixelLocationInSpriteImageX = ic.x;
					pixelLocationInSpriteImageY = ic.y;
					
					if(debugFlag) {
						System.out.println("Sprite ID " + sprite.getID() + " illegal sprite image index  = " + pixelLocationInSpriteImageX + "," +  pixelLocationInSpriteImageY + " " + spriteImageDimensions.toStr());
						drawRectBufferSpace(sprite);
						debugFlag=false;
					}
					
					
				}
				
				int spriteRGBA = spriteImage.getRGB(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY);
				int alpha = MOPackedColor.getAlpha(spriteRGBA);
				
				
				if(alpha > 3) {
					
					PVector docSpacePt = BStoDS(x,y);
					
					
					
					PVector p3OnSprite = sceneData3D.get3DVolumePoint(docSpacePt, spriteDepth);
					Ray3D rayTowardsProxyPlane = new Ray3D(p3OnSprite, lightDirection);
					
					PVector rayPlaneIntersectionPoint = Util3D.getRayPlaneIntersection(rayTowardsProxyPlane, proxyPlane);
					PVector rayPlaneIntersectionPoint2D = sceneData3D.world3DToDocSpace(rayPlaneIntersectionPoint); // this returns the current doc space
					
					float shadowValue01 = sceneData3D.getCurrentRender01Value(rayPlaneIntersectionPoint2D);

					int shadowVal =  (int) (shadowValue01*255);
					
					
					if(shadowImageGetSet.isInsideImage(x, y)==false) continue;
					
					
					if(this.debugFlag) {
						
						//System.out.println("Buffer x, y  = " + x + "," + y);
						//System.out.println("BStoDS  = " + docSpacePt.toStr());
						//
						//this.debugFlag = false;
					}
					
					
					
					int existingValue  = shadowImageGetSet.getPixel(x, y);
				
					if(shadowVal < existingValue) {
						shadowImageGetSet.setPixel(x, y, shadowVal);
					} 
					
				}
			}
			
		}
		
		
		
		
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// To find the intersection point between a ray and a point on the SceneDat3D surface is tricky as the surface is undulating. 
	// This algorithm establishes a "basePlane" in Y that is definitely below the surface. It finds the ray-basePlane intersection point
	// and then creates a Line3D between this point and a point above the surface.
	// hence the line3d penetrates the surface. It then uses the ray-marching method findClosestPointOnLineToSurface(Line3D line3, int numSamples)
	// to return the closest point found.
	PVector findClosestPointOnSurface(Ray3D ray, PVector basePlanePoint, float searchLineLength, int iterations) {
			// constructs a base plane at the lowest point, and finds the intersection point.
			PVector basePlaneIntersectionPoint = Util3D.getPlaneInYIntersection(ray, basePlanePoint);
			PVector searchLineVector = Util3D.multV(ray.direction,searchLineLength); // creates a vector in the same direction as the ray
			
			PVector searchLineStart = Util3D.subV(basePlaneIntersectionPoint, searchLineVector); // we need to subtract searchLineVector from basePlaneIntersectionPoint to get the top of the line
			// 	
			Line3D searchLine = new Line3D(searchLineStart, basePlaneIntersectionPoint);
			
			
			
			return findClosestPointOnLineToSurface(searchLine, iterations);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// This finds the approximate intersection between a Line3D and a SceneDat3D surface. 
	// It uses ray marching with numSamples accuracy. To work well, the line is assumed to penetrate the surface at some point.
	// so some sort of pre-processing should be done to establish this.
	// This could be made more efficient and accurate with better design
	PVector findClosestPointOnLineToSurface(Line3D line3, int numSamples) {
		
		
		float closestDistance = 1000000f;
		PVector closestPointOnLineToSurface = null;
		float step = 1f/numSamples; 
		for(int n = 0; n <= numSamples; n++) {
			float control = step*n;
			PVector linePt3D = line3.lerp(control);
			
			
			PVector linePt2D = sceneData3D.world3DToDocSpace(linePt3D);
			PVector surfacePoint = sceneData3D.get3DSurfacePoint(linePt2D);
			
			float thisDistance = surfacePoint.dist(linePt3D);
			if(thisDistance < closestDistance) {
				closestDistance = thisDistance;
				closestPointOnLineToSurface = linePt3D;
			}
			
		}
		
		if(this.debugFlag) {
			//System.out.println(">>in method findClosestPointOnLineToSurface searchLine " + line3.toStr());
			//System.out.println(">>in method findClosestPointOnLineToSurface closestPointOnLineToSurface " + closestPointOnLineToSurface.toStr());
			}
		
		
		
		return closestPointOnLineToSurface;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// Darkens nearby existing substance within a set 3d radius of a defined point on the sprite.
	// The shadePointSource's height is defined parametrically by shadePointSourceHeight, where 0 is the very base of the sprite and 1 is the very top.
	// The radius is in 3D units, and has a drop-off of shade intensity defined by radius with dropOffGamma
	//
	// Because we only want lightening ABOVE the shade source point and not below it, we calculate d as the distanve to a line segment between the sprite base point, and the sprite shade source point. 
	//
	// The amount of shade is calculated for point at distance d from shadeSource. 
	// normalisedShade = norm(d, radius, 0); // so when d == radius,normalisedShade = 0,  and when  d == 0 ,normalisedShade = 1
	// shadeAmount = pow(normalisedShade, dropOffGamma) // so when gamma == 1, drop off in linear, gamma < 1, drops off to maximum quickly at start. Gamma > 1 drops off only toward the end
	// 
	
	//
	public void radialSoftShade(Sprite sprite, float shadowLineHeightProportion, float radius, float dropOffGamma) {
		
		if((progress%1000) == 0) {
			System.out.println("addProjectedLightToShadowRender " + progress );
		}
		progress++;

		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.WHITE);
		
		// paste the sprite into the depth buffer
		depthRenderTarget.pasteFloatValues(sprite, sprite.getDepth());
		
		float depth = sprite.getDepth();
		
		PVector basePoint3D = sceneData3D.get3DVolumePoint(sprite.docSpaceRectLerp(new PVector(0.5f,1)), depth);
		PVector topPoint3D = sceneData3D.get3DVolumePoint(sprite.docSpaceRectLerp(new PVector(0.5f,1-shadowLineHeightProportion)), depth);
		
		Line3D shadowLine = new Line3D(basePoint3D, topPoint3D);
		
		//System.out.println("shadowLine " + shadowLine.toStr());
		
		// establish screen-bounds of region to process
		// 
		PVector topleft3D = new PVector(topPoint3D.x - radius, topPoint3D.y + radius, topPoint3D.z);
		PVector bottomRight3D = new PVector(basePoint3D.x + radius, basePoint3D.y, basePoint3D.z);
		
		PVector topleftDS = sceneData3D.world3DToDocSpace(topleft3D);
		PVector bottomRightDS = sceneData3D.world3DToDocSpace(bottomRight3D);
		
		PVector topleftBS = DStoBS(topleftDS);
		PVector bottomRightBS = DStoBS(bottomRightDS);
		Rect boundingRectBufferSpace = new Rect(topleftBS, bottomRightBS);
		// loop over region pixels
		for (int y = (int) boundingRectBufferSpace.top; y <= boundingRectBufferSpace.bottom; y++) {

			for (int x = (int) boundingRectBufferSpace.left; x <= boundingRectBufferSpace.right; x++) {
				// get the current depth render depth at that point. 
				// while we are iterating over the image in buffer space, it may be best to do all the calculations in doc space
				// so convert back to docSpace
				
				if( shadowRenderTarget.getCoordinateSystem().isInsideBufferSpace(x, y)==false) continue;
				
				PVector shadowPixelDocSpace = BStoDS(x, y);

				// get the current depth render depth at that point. 
				float shadowRenderDepth = depthRenderTarget.getPixel(shadowPixelDocSpace);
				
				// check to see if there is something there to receive a shadow. Copy the surface z values in
				// first if you want the surface to receive shadows.
				if (shadowRenderDepth == 0) continue; 
				
				
				PVector shadowRenderPoint3D = sceneData3D.get3DVolumePoint(shadowPixelDocSpace, shadowRenderDepth);
				
				float distanceFromShadowLine = getRadialDistanceFromShadowLine( shadowLine, shadowRenderPoint3D);
				
				float shadowIntensity01 = MOMaths.map(distanceFromShadowLine, 0, radius, 1, 0);
				shadowIntensity01 = MOMaths.constrain(shadowIntensity01,0,1);
				
				float shadowIntensity01WithGamma = (float) Math.pow(shadowIntensity01, dropOffGamma);
				
				int shadowVal =  (int) (255-(shadowIntensity01WithGamma*255)); // so when the shadow intensity is high, the result is dark
				
				
				
				int existingValue = shadowImageGetSet.getPixel(x, y);
				
				
			
				if(shadowVal < existingValue) {
					shadowImageGetSet.setPixel(x, y, shadowVal);
				} 
				
				
			}
		}
		
	}
	
	float getRadialDistanceFromShadowLine(Line3D shadowLine, PVector pointOnSurface) {
		// treats the line as a sort of capsule. Above the top of the line, use distance to the top point
		// below the top point, use distance to line. As the line is straight up in Y, we can use distXZ
		if(pointOnSurface.y < shadowLine.p2.y) {
			
			return pointOnSurface.distXZ(shadowLine.p2);
			
		}
		
		return pointOnSurface.dist(shadowLine.p2);
	}
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// debugging methods below here
	//
	//
	void debug(String s) {
		if(debugFlag==false) return;
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

	
}
