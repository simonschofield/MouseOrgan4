package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.FloatImageRenderTarget;
import MOImage.BilinearBufferedImageSampler;
import MOImage.ByteImageGetterSetter;
import MOImage.MOColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;
import MOUtils.Progress;

public class Lighting_RadialInterShadow  extends Lighting_CommonUtils{

	
	
	 int printLimit = 100;
	 int printCount = 0;
	
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
	public Lighting_RadialInterShadow(SceneData3D scene3D,  String nameOfShadowRender, String nameOfDepthRender,  boolean addSceneSurfaceToDepth, PVector lightDir){
		super(scene3D, nameOfShadowRender);
		if(scene3D == null) {
			System.out.println("ShadowCast3D  SceneData3D == null, please initialse first ");

		}
		sceneData3D = scene3D;

		Range worldY = sceneData3D.depthBuffer3d.worldYExtrema;
		System.out.println("worldY extrama " + worldY.toStr() );
		float sceneYMin = sceneData3D.depthBuffer3d.worldYExtrema.getUpper();

		initialiseDepthRender( nameOfDepthRender,  addSceneSurfaceToDepth, lightDir) ;

		progress = new Progress("Shadows: ");
	}
	

	public boolean radialInterShadowing(Sprite sprite, float maxShadowContribution, float shadowLineHeightProportion, float radius, float dropOffGamma, boolean useLightDirection) {
		debugFlag=false;
		
		
		// calculate the shadow-line
		//
		//
		float depth = sprite.getDepth();
	
		// get the document space sprite top and bottom points, mapped into the scene, using the sprite's image quad
		PVector mappedSpriteBasePointDocSpace = sprite.mapNormalisedLocalSpritePointToDocSpace( 0.5f, 1.0f);
		PVector mappedSpriteTopPointDocSpace = sprite.mapNormalisedLocalSpritePointToDocSpace( 0.5f, 0);
	
		PVector shadowLineBasePoint3D = sceneData3D.get3DVolumePoint(mappedSpriteBasePointDocSpace, depth);
		PVector mappedSpriteTopPoint3D = sceneData3D.get3DVolumePoint(mappedSpriteTopPointDocSpace, depth);

		// interpolate over this to get the shadowLineTopPoint
		PVector shadowLineTopPoint3D = PVector.lerp(shadowLineBasePoint3D, mappedSpriteTopPoint3D, shadowLineHeightProportion);
		
		Line3D shadowLine = new Line3D(shadowLineBasePoint3D, shadowLineTopPoint3D);
		
		
		

		

		// establish screen-bounds of region to process
		// 
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
		
		
		if(debugFlag) {
			// draw the shadow extents rect and shadow line
			Color c = MOColor.getRandomRGB(255);
			Util3D.drawLineWorldSpace(lineBase, lineTop, c, 3);
			GlobalSettings.getDocument().getMain().drawRect_DocSpace(shadowRectDocSpaceExtents, new Color(0,0,0,0), c, 3f);
		}

		float radiusSquared = radius*radius;
		PVector lightDirectioProjected = new PVector(lightDirection.x, 0, lightDirection.z).normalize();
		
		// iterate over the shadow extents rect in Buffer Space
		//
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
				
				// if the shadow intensity is < 1/255  ignore
				
				
				
				// add in the lightDirection here
				if(useLightDirection) {
					PVector thisRay  = PVector.sub(shadowRenderPoint3D, clostestPointOnShadowLine);
					thisRay.y = 0; // projected onto the y=0 plane
					thisRay.normalize();
					
					float amountOfBias = thisRay.dot(lightDirectioProjected);
					// when the ray is in-line with the light, the dot/cos value will be 1
					shadowAmount01 *= amountOfBias;
				}
				
				if(shadowAmount01 < 0.0078f) continue;
				shadowAmount01 = MOMaths.constrain(shadowAmount01, 0.0078f,1);

				float shadowAmount01WithGamma =  (float) Math.pow(shadowAmount01, dropOffGamma);
				//if( printCount < printLimit) {
				//	System.out.println(" shadowAmount01 in " + shadowAmount01 + " shadowAmount01WithGamma " + shadowAmount01WithGamma);
				//	printCount++;
				//}
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
		//if( printCount < printLimit) {
		//	System.out.println(" shadowAmount01 in " + shadowAmount01 + " pixelvalue " + newPixelValue);
		//	printCount++;
		//}
		shadowImageGetSet.setPixel(x, y, newPixelValue);
	}
	
	
	float getRadialDistanceSquaredFromShadowLine(Line3D shadowLine, PVector pointOnSurface) {
		// treats the line as a sort of capsule. Above the top of the line, use distance to the top point
		// below the top point, use distance to line. As the line is straight up in Y, we can use distXZ
		PVector closestPointOnLine = shadowLine.closestPointOnLine(pointOnSurface);

		return pointOnSurface.distSq(closestPointOnLine);
	}
	

	
}

