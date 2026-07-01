package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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
import MOSprite.SpriteBatch;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.Progress;
import MOUtils.SecondsTimer;


/**
 * RadialInterShadowing creates a soft shadow effect  The Lighting_RadialInterShadow constructor is called in loadContentUserSession, after which 
 * rendering is done in two passes<p>
 * 
 * The First Pass: addShadowGeometry(...) is called for every sprite in the updateUserSession UserSession method. <p>
 * 
 * The Second Pass: The method renderShadows(...) is called once in finaliseUserSession UserSession method. <p>
 * 
 */
public class Lighting_InterShadowing  extends Lighting_CommonUtils{


	 ShadowGeometrySpatialIndex shadowGeometrySpatialIndex;

	 
	 
	 	
	 int shadowGeometryCount = 0;
	 float maxSingleShadowContribution = 0.3333f;
	 
	 Rect renderExtentsDocSpace;
	
	 /**
	  * InterShadowing creates a soft shadow effect. A light direction is used to give the shadows a direction,
	  * and must be pointing "into" the image (i.e. have positive z component). The Lighting_RadialInterShadow constructor is called in loadContentUserSession, after which 
	  * rendering is done in two passes<p>
	  * 
	  * The First Pass: addShadowGeometry(...) is called for every sprite in the updateUserSession UserSession method. This adds the depth of the sprite to the depth buffer, and adds the 3D "shadow geometry" of the sprite to a list
	  * used in the second pass. The shadow geometry contains a 3D "shadow line", which is used as a proxy for the sprite's visual presence, a 2D doc-space shadow-extents, used in optimising the process, and a shadow image, which is mapped to the 
	  * shadow line to "flesh out" the shadow casting shape of the sprite. <p>
	  * 
	  * The Second Pass:  The user then two choices as to how the accumulated shadow-data is used to render shadows. 
	  * 1/ It can be used to render a full, accurate shadow image using a per-pixel method renderShadows(...). This is slow but shows how the shadows are
	  * falling within the image in a detailed way. The images may be too noisy to use nicely in final artworks.
	  * 2/ Approximate "shadow data" for each sprite can be calculated and and added to the sprite as supplementary sprite data. This is then saved in a sprite batch for  use in a subsequent render. The shadow data records how much shade is received by the sprite 
	  * using sparse sampling (e.g. 4 points on the shadow line of each sprite) . The preserved shadow-data is used in the subsequent session in the pasteShade(..) method, which is called on a per-sprite basis in updateUserSession(),
	  * or for the whole collated sprite batch (so long as the bitmap image data has been preserved in the sprite) in finaliseUserSession().
	  * 

	  * Implementation note: The 2D doc-space shadow-extents for each shadow geometry are used to to a spatially index the shadow-geometries against the screen buffer-space (into 100 x whatever-amount-in-Y screen space boxes),
	  * so that for every pixel being processed in the second pass, only those shadow-extents that overlap the pixel are retrieved (quickly), and used to accumulate 
	  * the final shadow value from the contributing shadow geometries accordingly<p>
	  * 
	  * Implementation note: The Second Pass using renderShadows(...). This is a scan-line algorithm for the whole image, so is slow. It uses the shadow lines that are previously calculated in
	  * the first pass, and the depth buffer to work out, for each pixel, the amount of shadow that pixel is receiving. The process is sped up by  spatially indexing the shadow lines against the pixels rough location, so that each pixel
	  * does not have to consider shadow lines that have no bearing on the that shadow amount <p>
	  *
	  * The return value relates to the contribution of the sprite's shadow to the image. Using a ROI, a sprite may be completely cropped from the ROI's image, but the shadow still contributes.
	  * Returns false if the shadow is completely outside the current doc space rect. Returns true otherwise.
	  * @param nameOfShadowRender - the render-target name for the shadowRender. This will be a BufferedImage.TYPE_BYTE_GRAY image.
	  * @param nameOfDepthRender - the render-target name for the depthRender. This will be a FloatImage type Render target image.
	  * @param addSceneSurfaceToDepth - If true, add the scene-data depth to the depth-render-target first, before any pasting happens. This is so the ground can "receive" shadows)
	  * @param lightDir - The direction of the light, must have +ve Z
	  */
	public Lighting_InterShadowing(String nameOfShadowRender, String nameOfDepthRender,  boolean addSceneSurfaceToDepth,  PVector lightDir){
		super(nameOfShadowRender, BufferedImage.TYPE_BYTE_GRAY);

		Range worldY = sceneData3D.depthBuffer3d.worldYExtrema;
		System.out.println("worldY extrama " + worldY.toStr() );
		
		super.initialiseDepthRender( nameOfDepthRender,  addSceneSurfaceToDepth) ;

		setLightDirection(lightDir);
		
		shadowGeometrySpatialIndex = new ShadowGeometrySpatialIndex(100);
	}
	
	/**
	 * Sets the maximum contribution for a single shadow (1 is max, default is 0.3333)
	 * @param c
	 */
	public void setMaximumSingleShadowContribution(float c) {
		
		maxSingleShadowContribution = c;
	}
	
	
	
	
	
	/**
	 * The first pass. Do this for every sprite in updateUserSession(). <p>
	 * It adds the 3D shadow line for each sprite, and its docSpace extents rect to the list, pastes the depth to the depth buffer, and returns true if the sprite contributes to this ROI. If the sprite does not contribute to this ROI
	 * then the shadow line is not preserved, as there would be no point.
	 * This does not need to be called if
	 * @param sprite - The sprite currently being pasted, it adds to the depth buffer and creates a shadow geometry object to store its resultant shadow.
	 * @param shadowImg - The shadow image. (nullable) Set this if you are using the shadow image approach. An RGBA image of tones of the shadow, where black => no shadow, white => full shadow contribution. 
	 * 
	 * @return  true if the sprite contributes to this ROI image.
	 */ 
	public boolean addShadowGeometry(Sprite sprite, BufferedImage shadowImg) {
		
		float depth = sprite.getDepth();
		depthRenderTarget.pasteSprite(sprite, depth, 127,true);

		ShadowGeometry thisShadowGeometry = new ShadowGeometry(sprite, shadowImg, lightDirection);
		
		
		
		
		if( thisShadowGeometry.contributesToThisROI ) {
			shadowGeometrySpatialIndex.addShadowGeometryToIndex(thisShadowGeometry);
			shadowGeometryCount++;
		}
		
		return thisShadowGeometry.contributesToThisROI;
	}

	
	/**
	 * One of the "second pass" methods, to be used once all the shadow-data has been accumulated. So is called once in finaliseUserSession(). This add supplementary shadow data to each sprite
	 * based on amount of shade each shadow line receives from the full collection of shadow lines. The method makes a small number of shade-samples on the shadow line of each sprite so
	 * should be quite fast (compared to renderShadow() method). This data is then added to each sprite and, presumably, saved out in a sprite batch.This is then re-loaded in the next session, so sprites have shadow data ahead of rendering.
	 */
	public void addShadowDataToSprites() {
		
		
		ArrayList<ShadowGeometry> shadowGeometryList = shadowGeometrySpatialIndex.getSimpleShadowGeometryList();
		
		for(ShadowGeometry sg: shadowGeometryList) {
			
			float shadowAmount = 0;
			
			// makes a set of samples along the shadow line, and stores the results in an array
			float[] samplePositionsOnLine = {0.01f,0.1f,0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f,0.9f,0.99f};
			int numSamples = samplePositionsOnLine.length;
			float[] shadowDataArray = new float[numSamples];
			
			
			for(int i = 0; i < numSamples; i++) {

				PVector linePoint = sg.shadowLine3D.lerp(  samplePositionsOnLine[i]   );
				PVector docPt = sceneData3D.world3DToDocSpace(linePoint);
				shadowDataArray[i] = calculateShadowAmount( docPt, linePoint.z, maxSingleShadowContribution);
				
				}
			
			shadowAmount = shadowAmount/numSamples;
			
			
			KeyValuePair shadowData = new KeyValuePair("ShadowData", shadowDataArray);
			
			
			sg.spriteRef.setSpriteData( shadowData );
		}
		
		
	}
	
	/**
	 * If the shadow-data has been added for each sprite AND the sprite batch still contains the image data then this can be called in in the finliseUserSession(), 
	 * to create the whole scene's shade image from the sprite batch.
	 * 
	 * 
	 * @param spriteBatch
	 */
	public void pasteShade(SpriteBatch spriteBatch) {
		int numSucessfulShadows = 0;
		int totalNumSprites = spriteBatch.getNumItems();
		System.out.println("Pasting the intershaded shade of " + totalNumSprites + " sprites");
		SecondsTimer timer = new SecondsTimer();
		
		addShadowDataToSprites();
		
		spriteBatch.resetItemIterator();
		
		while( true ) {
			
			Sprite sprite = spriteBatch.getNextSprite();
			if(spriteBatch.isFinished() || sprite==null) {
				break;
			}
			boolean success = pasteShade( sprite);
			if(success) numSucessfulShadows++;
			
		}
		
		float t = timer.getTimeSinceStart();
		System.out.println("There were " + numSucessfulShadows + " sprites with shadow data pasted. Total time for " + t);
	}

	
	/**
	 * Once the base point lighting has beens set up (via the constructor for the class, and optional use of setToneRamp(..)), the lighting calculation for each sprite is added immediately to the output lighting RenderTarget when
	 * this method is called (there is no deferred process)
	 * @param sprite - the sprite being added to the scene
	 * @return - return true is a paste has been successfully made, false if there was no sprite image to paste or shadow data in the sprite
	 */
	public boolean pasteShade(Sprite sprite) {
		
		if(sprite.getImage(0)==null) {
			//System.out.println("Lighting_RadialIntershadowing::pasteShadowAmount(...) - Sprite image bitmap data = null, returning");
			return false;
		}
		
		
		if( sprite.spriteDataKeyExists("ShadowData")==false  ) {
			//System.out.println("Lighting_RadialIntershadowing::pasteRampedShade(...) - Sprite has no shadow data, returning");
			return false;
		}
		

		// replace what might be already their with white
		shadowRenderTarget.pasteSprite_ReplaceColour(sprite, Color.white);

		
		KeyValuePair shadowDataKVP = sprite.getSpriteData("ShadowData");
		float[] shadowData = shadowDataKVP.getVector();
		
		// if there is no shade in this sprite then the job is done!
		float ave = MOMaths.mean(shadowData);
		if(ave >= 0.996f) {return true;}
		
		
		

		PVector spriteDocPt = sprite.getDocPoint();
		float spriteDepth = sprite.getDepth();
		PVector basePoint3D = 	sceneData3D.get3DSurfacePoint(spriteDocPt);

		// use current image as it might be pasting an overlay
		BufferedImage spriteImage = sprite.getCurrentImage();
		int spriteBufferW = spriteImage.getWidth();
		int spriteBufferH = spriteImage.getHeight();
		Rect spriteBoundingRectBufferSpace = sprite.getDocumentBufferSpaceRect();
		ImageDimensions spriteImageDimensions = new ImageDimensions(spriteBufferW,spriteBufferH);

		float heightOfSprite = sprite.getSizeInScene();
		
		toneRamp = new ToneRamp();
		// the base has full base tone + alpha
		toneRamp.addControlPoint(0, shadowData[0], 1);
		toneRamp.addControlPoint(heightOfSprite*0.333f, shadowData[1], 1);
		toneRamp.addControlPoint(heightOfSprite*0.666f, shadowData[2], 1);
		//toneRamp.addControlPoint(heightOfSprite, shadowData[3], 1);
		toneRamp.addControlPoint(heightOfSprite, 0, 1);
		
		
		
		
		
		// we do it bottom to top, so as to trap the bright point going upwards
		for(int y = (int) spriteBoundingRectBufferSpace.bottom; y >= spriteBoundingRectBufferSpace.top; y--) {


			//work out the doc space location of the pixel above the basepoint, in docSpace
			PVector docSpaceOfY = BStoDS(0,y);
			PVector aboveBasePointAtThisY = new PVector(spriteDocPt.x, docSpaceOfY.y);


			// convert this into a 3D point (at the sprite's depth)
			PVector y3D  = sceneData3D.get3DVolumePoint(aboveBasePointAtThisY, spriteDepth);


			// measure this distance between the basePoint, and this y3D point
			// This gives oyu the height of this row of ixels above the
			// base point in 3D units
			
			float thisYHeight3D = y3D.dist(basePoint3D);

			ToneRampControlPoint shadeAmountOnRamp = toneRamp.getValue(thisYHeight3D);
			// this returns the amount of shade at control value thisYHeight3D, raher
			
			float brightness = 1-shadeAmountOnRamp.toneValueAtControlPoint;
			
			int pixelToneValue = (int)  (brightness * 255);
			

			for (int x = (int) spriteBoundingRectBufferSpace.left; x <= spriteBoundingRectBufferSpace.right; x++) {

				if( !shadowRenderTarget.getCoordinateSystem().isInsideBufferSpace(x, y)) {
					continue;
				}

				int pixelLocationInSpriteImageX = x - (int)spriteBoundingRectBufferSpace.left;
				int pixelLocationInSpriteImageY = y - (int)spriteBoundingRectBufferSpace.top;

				if( !spriteImageDimensions.isLegalIndex(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY) ) {
					continue;
				}

				int spriteRGBA = spriteImage.getRGB(pixelLocationInSpriteImageX, pixelLocationInSpriteImageY);
				int alpha = MOPackedColor.getAlpha(spriteRGBA);
				if(alpha == 0) {
					continue;
				}
				int existingValue = shadowByteImageGetSet.getPixel(x, y);
				int blendedValue = lerpInt256(existingValue, pixelToneValue,  alpha);
				shadowByteImageGetSet.setPixel(x, y, blendedValue);

			}// for X
		}// for Y
		return true;
	}
	
	
	
	/**
	 * One of the "second pass" methods, to be used once all the shadow-data has been accumulated. So is called once in finaliseUserSession().
	 * RenderShadows will render the shadows of the sprites projected onto the depth-render on a per-pixel basis so this is a slow process, and a quicker alternative is to pre-calculted the shadow data and store int the sprite-batch
	 * across sessions, then use pasteShadow(...) for each sprite, as the image is being created in updateUserSession(). 
	 *
	 * @param maxShadowContribution
	 * @param renderRectDocSpace - defines the area you want rendered. This is nullable if you want the whole render doing
	 * @return - Returns TRUE if the shadow intersects the current documetSpaceREct. This will be the ROI rect is a ROI session. So, this is then used to help determine which sprites to preserve in a ROI's spriteBatch.
	 */
	public void renderShadows(float maxShadowContribution,  Rect renderRectDocSpace) {
		// Iterates over the image visiting each pixel only once, and renders the shadow of that pixelo based on the shadow line list.
		// iterate over the render area
		//
		maxSingleShadowContribution = maxShadowContribution;
		int imageHeight = shadowRenderTarget.coordinateSystem.getBufferHeight();
		int imageWidth = shadowRenderTarget.coordinateSystem.getBufferWidth();
		Rect renderRectBufferSpace = GlobalSettings.getTheDocumentCoordSystem().getBufferRect();
		
		
		if(renderRectDocSpace != null) {
			renderRectBufferSpace = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(renderRectDocSpace);
		}
		
		 
		int yLo = (int)renderRectBufferSpace.top;
		int xLo = (int)renderRectBufferSpace.left;
		int yHi = (int)renderRectBufferSpace.bottom;
		int xHi = (int)renderRectBufferSpace.right;
		System.out.println("Rendering radial intershadow :rect " + shadowGeometryCount + " render rect =  " + renderRectBufferSpace.toStr());
		
		Progress progress = new Progress("Rendering buffer lines " , imageHeight);
		
		for (int y = yLo; y < yHi; y++) {

			for (int x = xLo; x <  xHi; x++) {
	
				// get the current depth render depth at that point.
				float shadowRenderDepth = depthRenderTarget.getPixel(x,y);

				// check to see if there is something there to receive a shadow. Copy the surface z values in
				// first if you want the surface to receive shadows.
				if (shadowRenderDepth == 0 || shadowRenderDepth == Float.MAX_VALUE) {
					continue;
				}
				
				// get the docPoint of x,y
				PVector shadowPixelDocSpace = BStoDS(x, y);
				
				
				float shadowAmount = calculateShadowAmount(shadowPixelDocSpace, shadowRenderDepth,  maxSingleShadowContribution);
				
				contributeShadowToImage( x, y, shadowAmount);
				
			}
			progress.update();
		}
		
		
		
	}
	
	
	
	
	
	
	/**
	 * Returns the amount of shade generated by all the accumulated shadow geometry intersecting a certain 3D point in the scene. 
	 * The point is given as a 2D docspace point with depth, as this is more convenient for the spatially-indexed shadow geometries.
	 * The shadow amount is calculated by mapping the light-ray from the point to a "shadow image" stored in each shadow-geometry.
	 * 
	 * @param docPt -used to calculated the 3D point from the completed depth render
	 * @param pixelDepth -used to calculated the 3D point from the completed depth render
	 * @param maxShadowContribution - the maximum shadow contribution of each shadow line
	 * @return the amount of shade at this point
	 */
	private float calculateShadowAmount(PVector docPt, float pixelDepth, float maxShadowContribution) {
		// normalisedShade = norm(d, radius, 0); // so when d == radius,normalisedShade = 0,  and when  d == 0 ,normalisedShade = 1
		// shadeAmount = pow(normalisedShade, dropOffGamma) // so when gamma == 1, drop off in linear, gamma < 1, drops off to maximum quickly at start. Gamma > 1 drops off only toward the end
		
		// 1. Get the 3D location of the point you want to shade - the shadowRenderPoint3D
		PVector shadowRenderPoint3D = sceneData3D.get3DVolumePoint(docPt, pixelDepth);
		
		// 2. get all the sprite shadow-geometry that belongs to the image-section of this docPoint from the spatially indexed shadow geometry
		ArrayList<ShadowGeometry> intersectingShadows = getIntersectingShadows(docPt, pixelDepth);
		
		// this is the amount of accumulated shadow from all the shadow lines
		float accumulatedShadow = 0;
		
		// 3. Loop through the shadow lines and for each one....
		for(ShadowGeometry thisShadow: intersectingShadows) {
			
			// 3.1 some of the shadow geometry from above may not actually intersect this point
			if(thisShadow.intersecting == false) continue;
			
			// now choose the shadow method, using a radial distance approach, or a shadow map approach
			accumulatedShadow += calculateShadowMapShadowForThisShadowGeometry(shadowRenderPoint3D,  thisShadow,  maxShadowContribution);

		}
		
		//System.out.println("intersecting shadow num " + intersectingShadows.size() + " accumulated shadow " + accumulatedShadow);
		// returns shadow amount in the range 0..1
		if(accumulatedShadow>1) accumulatedShadow=1;
		return accumulatedShadow;
	}

	
	/**
	 * Returns the amount of shade cast by one ShadowGeometry object onto a specific 3D point in the scene.
	 * Projects a ray from the shadowRenderPoint3D along the negativeLightDirection and calculates where it intersects the shadow image (if at all). If so, then 
	 * uses the pixel value at that point to calculate the amount of shadow contributed.
	 * Working notes: if the shadow image plane is flat on to the viewer (billboard style) then the shadow will be forshortened in width at strong light angles.
	 * If the shadow image plane is perpendicular to the light direction, then this is avoided. So, this depends on significance of effect and speed of projection in both cases.
	 * @param shadowRenderPoint3D
	 * @param thisShadow - a single ShadowGeometry object
	 * @param maxShadowContribution
	 * @return
	 */
	private float calculateShadowMapShadowForThisShadowGeometry(PVector shadowRenderPoint3D, ShadowGeometry thisShadow, float maxShadowContribution) {
		// make the shadow plane. This is a BOUNDED PLANE. This can be done earlier when chached
		// The shadow plane has the shadowLine running up the centre (so deines the height) and uses the aspect of the shadow image to define the corners.
		if(thisShadow.shadowImageGetterSetter==null) {
			// ERROR
			return 0;
		}
		
		
		// specific to this point
		Ray3D lightRay = new Ray3D(shadowRenderPoint3D, negativeLightDirection);
		
		PVector intersectionPoint = thisShadow.shadowPlane.getRayIntersectionPoint_Bounded(lightRay);
		
		
		
		// point does not intersect the shadow plane (common outcome), or ray is parallel (only if there is no z component to the ray)
		if(intersectionPoint == null) return 0;

		// Take into consideration the distance of the shadow
		float distancePlaneToPoint = shadowRenderPoint3D.dist(intersectionPoint);
		float distanceShadowMultimplier = MOMaths.map(distancePlaneToPoint, 0, thisShadow.shadowRadius, 1, 0.1f); // should be 1 when near to the shadow, and 0.25 when far away
		
		
		
		// so the point intersectionPoint is in the bounded plane. Now get the intersectionPoint normalised within the bounded shadow plane
		PVector normalisedIntersectionPoint  = thisShadow.shadowPlane.norm(intersectionPoint);
		
		float shadowAmount01 = thisShadow.shadowImageGetterSetter.getPixelFromNormalisedCoordinate(normalisedIntersectionPoint.x, 1-normalisedIntersectionPoint.y) * 0.00392f * maxShadowContribution * distanceShadowMultimplier;
		
		// take into consideration distance; near points will receive stronger shadow than far points
		
		
		
		if(shadowAmount01 < 0.00392f) {
			return 0;
		}
		if(shadowAmount01 > 1) {
			return 1;
		}
		return shadowAmount01;
	}
	
	/**
	 * This method uses the spatial index to return only the ShadowGeometry that intersects this docSpace point
	 * @param docSpacePoint
	 * @param pixelDepth
	 * @return
	 */
	private ArrayList<ShadowGeometry> getIntersectingShadows(PVector docSpacePoint, float pixelDepth){
		
		ArrayList<ShadowGeometry> intersectingShadows = shadowGeometrySpatialIndex.getIntersectingShadowGeometry(docSpacePoint);
		for(ShadowGeometry ssi: intersectingShadows) {
			ssi.intersecting = false;
			if( ssi.shadowRectDocSpaceExtents.isPointInside(docSpacePoint) && MOMaths.isBetweenExc( ssi.shadowLine3D.p1.z , pixelDepth-ssi.shadowRadius, pixelDepth) ) {
				ssi.intersecting = true;
			}
			
		}
		
		return intersectingShadows;
	}
	
	
	
	
	
	
	/**
	 * Adds an amount of shade to the output render image on a per-pixel basis. Shade is added cumulatively to the image, in that it is "added" to the previous amount of shade already there.
	 * @param x - buffer-space pixel x coordinate
	 * @param y - buffer-space pixel y coordinate
	 * @param shadowAmount01 - shade amount needs to be between 0...1, where 0 is no-shade-added, and 1 is max-shade-added, and would immediately result in a black pixel.
	 */
	private void contributeShadowToImage(int x,int y, float shadowAmount01) {
		
		int existingPixelValue = shadowByteImageGetSet.getPixel(x, y);
		float existingShadowValue01 = 1-existingPixelValue*0.003922f;

		int newPixelValue = (int)  ((1-(existingShadowValue01 + shadowAmount01) ) * 255);
		newPixelValue = MOMaths.constrain(newPixelValue,0,255);
		
		shadowByteImageGetSet.setPixel(x, y, newPixelValue);
	}

}














/**
 * Shadow geometry Class
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * Class that calculates a sprites shadow geometry. 
 * These are accumulated within a spatial index as each sprite is pasted to the image (in update), and then used to render the final image in the second pass, (in finalise).
 */
class ShadowGeometry{
	// this is used to save out shadow data with each sprite
	Sprite spriteRef;
	
	
	
	SceneData3D sceneData3D;
	Line3D shadowLine3D;
	Rect shadowRectDocSpaceExtents;
	float shadowRadius;
	boolean intersecting = false;
	
	boolean contributesToThisROI = false;
	
	ByteImageGetterSetter shadowImageGetterSetter = null;
	float shadowImageAspect = 1;
	BillboardRect3D shadowPlane = null;
	PVector lightDirection;
	
	public ShadowGeometry(Sprite sprite, BufferedImage shadowImg, PVector lightDir) {
		
			spriteRef = sprite;
			
			sceneData3D = GlobalSettings.getSceneData3D();
			lightDirection = lightDir;
			float depth = sprite.depth;
			
			// Calculate the document space sprite top and bottom points, mapped into the scene, using the sprite's image quad
			// Take into consideration  the pivot point 
			//REMEMBER: a Y of 0 is the top of the sprite, a Y of 1 is the base of the sprite
			PVector mappedSpriteBasePointDocSpace = sprite.getDocPoint(); // this is the transformed base of the sprite
			PVector mappedSpriteTopPointDocSpace = sprite.mapNormalisedLocalSpritePointToDocSpace( 0.5f, 0); // this returns the transformed top of the sprite
			
			//System.out.println("sprite base " + mappedSpriteBasePointDocSpace.toStr() + " sprite top " + mappedSpriteTopPointDocSpace.toStr());

			PVector shadowLineBasePoint3D = sceneData3D.get3DSurfacePoint(mappedSpriteBasePointDocSpace);
			PVector mappedSpriteTopPoint3D = sceneData3D.get3DVolumePoint(mappedSpriteTopPointDocSpace, depth);

			this.shadowLine3D = new Line3D(shadowLineBasePoint3D, mappedSpriteTopPoint3D);
			this.shadowRadius = shadowLine3D.getBoundingBox().getHeight();
			
			// establish screen-bounds of region to process
			//
			//
			Rect theDoumentDocSpaceRect;

			shadowImageGetterSetter = new ByteImageGetterSetter(shadowImg);
			shadowImageAspect = shadowImg.getWidth()/((float) shadowImg.getHeight()); // should be a number  less that 1 for grasses etc.
			this.shadowRectDocSpaceExtents =  calculateShadowRectForShadowImage_UsingShadowLine(sprite,this.shadowLine3D);
			
			
			// Does this shadow contributes to the ROI?
			theDoumentDocSpaceRect = GlobalSettings.getDocument().getCoordinateSystem().getDocumentRect();
			contributesToThisROI =  shadowRectDocSpaceExtents.intersects(theDoumentDocSpaceRect) ;
		
	}
	
	
	/**
	 * In this version the shadow line is used to define the height of the shadow plane, The width of the shadow plane is defined by the height * shadowImageAspect
	 * @return
	 */
	private Rect calculateShadowRectForShadowImage_UsingShadowLine(Sprite sprite, Line3D shadLine) {

		PVector spriteDocPoint = sprite.getDocPoint();
		PVector spriteBasePoint3D = shadLine.p1;
		
		float shadowLineHeight = shadLine.getBoundingBox().getHeight();
		float shadowLineWidth = shadowLineHeight * this.shadowImageAspect;
		float shadowLineWidthOver2 = shadowLineWidth/2f;
	
		// calculate the shadowPlane - The shadowPlane has the same world-space height as the shadow line, and uses the aspect of the shadpw image to calculate the width.
		PVector bl = new PVector(spriteBasePoint3D.x-shadowLineWidthOver2, spriteBasePoint3D.y, spriteBasePoint3D.z);
		shadowPlane = new BillboardRect3D(bl,shadowLineWidth,shadowLineHeight);
		

		// for each of the 2 top sprite rect corners, project a ray in the lightDirection and see
		// where this intersects the basePlane
		PVector c1 = shadowPlane.getCorners()[0]; // top left corner
		PVector c2 = shadowPlane.getCorners()[1]; // top right corner

		Ray3D c1Ray = new Ray3D(c1, lightDirection);
		Ray3D c2Ray = new Ray3D(c2, lightDirection);

		PVector c1PointOnSurface = sceneData3D.raySurfaceIntersection(c1Ray);
		PVector c2PointOnSurface = sceneData3D.raySurfaceIntersection(c2Ray);

		// get the rect extents for the sprite from its base point(where it hits the surface) to its top. This then takes into consideration
		// any Y-shifting or pivot point deviations from Y = 1
		float bottom = spriteDocPoint.y;
		PVector top3D = shadLine.p2;
		float top = sceneData3D.world3DToDocSpace(top3D).y;
		float left = sceneData3D.world3DToDocSpace(c1).x;
		float right = sceneData3D.world3DToDocSpace(c2).x;
		
		// The rayBaseIntersectionPoints and the sprites base points c3,c4 give us the world extents of the possible shadow
		// project these back into doc space
		PVector docSpaceExtents[] = new PVector[6];
		docSpaceExtents[0] = sceneData3D.world3DToDocSpace(c1PointOnSurface);
		docSpaceExtents[1] = sceneData3D.world3DToDocSpace(c2PointOnSurface);
		docSpaceExtents[2] = new PVector(left,top);
		docSpaceExtents[3] = new PVector(right,top);
		docSpaceExtents[4] = new PVector(left,bottom);
		docSpaceExtents[5] = new PVector(right,bottom);

		// The hull of these vertices forms a 5-sided shape - A rectangle with a triangle attached to one side, depending on the light direction
		// We can call this the potential shadow region. Any previous sprite's pixels falling within the shadow region can receive shade. Any point
		// outside it cannot receive shade.
		// if the light ray direction is going left (-ve x) then the vertices of the shadow region are [2][3][5][4][2] for the sprite rect and [0][4][2][0] for the triangle
		// if the light ray direction is going right (+ve x) then the vertices of the shadow region are [2][3][5][4] for the sprite rect and [1][5][3][1] for the triangle
		// UNLESS the shape is cropped by the edge of the image!!!
		// anyway, the inside check for the triangles is more expensive than the ray casting

		// get the extents of these points
		return Rect.getExtents(docSpaceExtents);
	}
	
	
	
	String toStr() {
		
		
		return shadowRectDocSpaceExtents.toStr();
	}
	
	boolean intersects(PVector p) {
		return shadowRectDocSpaceExtents.isPointInside(p);
	}
	
	boolean intersects(Rect r) {
		return shadowRectDocSpaceExtents.intersects(r);
	}
	
	
	
	
	
}



class ShadowGeometrySpatialIndex{
	
	// this is a simple linera list of th shadowe geometries. Useful for 
	// assigning shadow data back to the sprite
	ArrayList<ShadowGeometry> simpleShadowGeometryList;
	
	
	ShadowGeometrySpatialIndexBox2D[][] docSpaceGrid;
	int numWidth, numHeight;
	Rect masterDocumentRect; 
	
	
	
	
	/**
	 * Initialise the ShadowGeometrySpatialIndex with the whole document docRect, and the number of screen-space boxes in x. It works out the rest.<p>
	 * The spatial index always uses the master ROI document rect to build its boxes, regardless of the current ROI in use. This means that when the spatial boxes
	 * are created, they will be the same in master and roi sessions.
	 * @param numBoxesWidth
	 */
	public ShadowGeometrySpatialIndex(int numBoxesWidth) {
		
		numWidth = numBoxesWidth;
		
		// if you are using a ROIManager, and using a ROI, you do not want to use the current ROI Master documentRect,
		// not the current documentRect. On the off chance you are not using a roi manager, then use the document rect.
		if(GlobalSettings.getROIManager() != null) {
			masterDocumentRect = GlobalSettings.getROIManager().getMasterDocumentRect();
		}else {
			masterDocumentRect = GlobalSettings.getDocument().getCoordinateSystem().getDocumentRect();
		}
		
		
		
		numHeight = (int) Math.round( (numWidth/ masterDocumentRect.aspect()) );
		
		
		docSpaceGrid = new ShadowGeometrySpatialIndexBox2D[numWidth][numHeight];
		
		float boxWidth = masterDocumentRect.getWidth()/numWidth;
		float boxHeight = masterDocumentRect.getHeight()/numHeight;
		
		
		
		for(int y = 0; y < numHeight; y++) {
			
			for(int x = 0; x < numWidth; x++) {
				 
				Rect thisDocSpaceRect = new Rect(boxWidth*x,boxHeight*y, boxWidth, boxHeight);
				
				docSpaceGrid[x][y] = new ShadowGeometrySpatialIndexBox2D(thisDocSpaceRect);
				
			}
			
		}
		
		simpleShadowGeometryList = new ArrayList<ShadowGeometry>();
	}
	
	
	/**
	 * Searches through all the spatially indexed boxes and adds this ShadowGeometry sg to those boxes that intersect it.
	 * @param sg
	 */
	void addShadowGeometryToIndex(ShadowGeometry sg) {
		
		simpleShadowGeometryList.add(sg);
		
		for(int y = 0; y < numHeight; y++) {
			for(int x = 0; x < numWidth; x++) {
				docSpaceGrid[x][y].tryAddShadowGeometry(sg);
			}
		}
		
	}
	
	
	/**
	 * Given a buffer-space coordinate in x,y. This method returns all the shadowGeometry that has been spatially indexed for that pixel.
	 * @param x
	 * @param y
	 * @return
	 */
	ArrayList<ShadowGeometry> getShadowGeometryFromBufferSpace(int x, int y){
		
		PVector docSpace = BStoDS(x,y);
		
		return getIntersectingShadowGeometry(docSpace);
		
	}
	
	ArrayList<ShadowGeometry> getIntersectingShadowGeometry(PVector docSpace){
		
		ImageCoordinate ic = docSpaceToBoxIndex(docSpace);
		
		return docSpaceGrid[ic.x][ic.y].shadows;
		
	}
	
	ImageCoordinate docSpaceToBoxIndex(PVector docSpace) {
		
		
		
		//PVector normalisedSpace = GlobalSettings.getROIManager().getMasterImageCoordinateSystem().docSpaceToNormalisedSpace(docSpace);
		PVector normalisedSpace = masterDocumentRect.norm(docSpace);
		
		int xIndex = (int)(normalisedSpace.x * numWidth);
		int yIndex = (int)(normalisedSpace.y * numHeight);
		
		
		xIndex = MOMaths.constrain(xIndex, 0, numWidth-1);
		yIndex = MOMaths.constrain(yIndex, 0, numHeight-1);
		
		if(GlobalSettings.debugFlag) {
			//System.out.println("docSpaceToBoxIndex:: docSpace passed in " + docSpace.toStr() + " box index " + xIndex + "," + yIndex);
			
		}
		
		return new ImageCoordinate(xIndex, yIndex);

	}
	
	PVector BStoDS(int x, int y) {
		return GlobalSettings.getDocument().getCoordinateSystem().bufferSpaceToDocSpace(x,y);
	}
	
	ArrayList<ShadowGeometry> getSimpleShadowGeometryList(){
		return simpleShadowGeometryList;
	}
	
}


class ShadowGeometrySpatialIndexBox2D{
	Rect myDocSpaceRect;
	public ArrayList<ShadowGeometry> shadows = new ArrayList<>();
	int indexX, indexY;
	

	/**
	 * Initialised with the docSpace rect representing the grid-box of the overall render that this box covers.
	 * @param myRect
	 */
	public ShadowGeometrySpatialIndexBox2D(Rect myRect) {
		myDocSpaceRect = myRect;

	}

	public boolean tryAddShadowGeometry(ShadowGeometry sg) {

		if( sg.intersects(myDocSpaceRect)) {

			shadows.add(sg);
			return true;
		}

		return false;
	}

	public ArrayList<ShadowGeometry> getIntersectingShadowGeometry() {
		return shadows;
	}

	
	
}








