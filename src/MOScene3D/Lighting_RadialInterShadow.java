package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.ImageCoordinate;
import MOImage.MOColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;

import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.Progress;


/**
 * RadialInterShadowing creates a soft shadow effect within a set 3d distance from sprite pasted in front of the existing substance. A light direction can be also used to give the shadows an accent
 * but the shadow must be pointing "into" the image (i.e. have positive z component) . Lighting_RadialInterShadow constructor be called in loadContentUserSession, after which 
 * rendering is done in two passes<p>
 * 
 * The First Pass: addShadowGeometry(...) is called for every sprite in the updateUserSession UserSession method <p>
 * The Second Pass: The method renderShadows(...) is called once in finaliseUserSession UserSession method<p>
 * 
 * 
 * The process involves calculating distances from a simple 3D "shadow line" from the base of the sprite, towards its tip, including rotation (accumulated in the first pass).
 * The amount of shade calculated for each pixel in the shadow render is based on its distance from adjacent shadow-lines (rendered in the second pass).
 */
public class Lighting_RadialInterShadow  extends Lighting_CommonUtils{


	 ShadowGeometrySpatialIndex shadowGeometrySpatialIndex;

	 	float shadowLineHeightProportionOfSpriteHeight = 1;
	 	
	 int shadowGeometryCount = 0;
	 
	 Rect renderExtentsDocSpace;
	
	 /**
	  * RadialInterShadowing creates a soft shadow effect within a set 3d distance from sprite pasted in front of the existing substance. A light direction can be also used to give the shadows an accent
	  * but the light direction must be pointing "into" the image (i.e. have positive z component). Lighting_RadialInterShadow constructor be called in loadContentUserSession, after which 
	  * rendering is done in two passes<p>
	  * 
	  * The First Pass: addShadowGeometry(...) is called for every sprite in the updateUserSession UserSession method <p>
	  * The Second Pass: The method renderShadows(...) is called once in finaliseUserSession UserSession method<p>
	  * 
	 *
	 * The return value relates to the contribution of the sprite's shadow to the image. Using a ROI, a sprite may be completely cropped from the ROI's image, but the shadow still contributes.
	 * Returns false if the shadow is completely outside the current doc space rect. Returns true otherwise.
	 * @param nameOfShadowRender - the render-target name for the shadowRender. This will be a BufferedImage.TYPE_BYTE_GRAY image.
	 * @param nameOfDepthRender - the render-target name for the depthRender. This will be a FloatImage type Render target image.
	 * @param addSceneSurfaceToDepth - If true, add the scene-data depth to the depth-render-target first, before any pasting happens. This is so the ground can "receive" shadows)
	 * @param lightDir - The direction of the light, must have +ve Z
	 */
	public Lighting_RadialInterShadow(String nameOfShadowRender, String nameOfDepthRender,  boolean addSceneSurfaceToDepth, float shadowLineHeightProportion, PVector lightDir){
		super(nameOfShadowRender);

		Range worldY = sceneData3D.depthBuffer3d.worldYExtrema;
		System.out.println("worldY extrama " + worldY.toStr() );
		float sceneYMin = sceneData3D.depthBuffer3d.worldYExtrema.getUpper();

		super.initialiseDepthRender( nameOfDepthRender,  addSceneSurfaceToDepth) ;

		
		setLightDirection(lightDir);
		
		
		shadowGeometrySpatialIndex = new ShadowGeometrySpatialIndex(100);
		shadowLineHeightProportionOfSpriteHeight = shadowLineHeightProportion;
	}
	
	
	
	/**
	 * The first pass. Do this for every sprite in updateUserSession(). <p>
	 * It adds the 3D shadow line for each sprite, and its docSpace extents rect to the list, pastes the depth to the depth buffer, and returns true if the sprite contributes to this ROI. If the sprite does not contribute to this ROI
	 * then the shadow line is not preserved, as there would be no point.
	 * @param sprite - The sprite currently being pasted, it adds to the depth buffer and creates a shadow geometry object to store its resultant shadow.
	 * @param shadowLineHeightProportion - The relative height of the shadow line (as a 0..1 proportion) to the sprites full vertical height, so smaller proportion - shorter shadows.
	 * @return  true if the sprite contributes to this ROI image.
	 */ 
	public boolean addShadowGeometry(Sprite sprite) {
		
		float depth = sprite.getDepth();
		depthRenderTarget.pasteSprite(sprite, depth, 127);

		ShadowGeometry thisShadowGeometry = new ShadowGeometry(sprite, shadowLineHeightProportionOfSpriteHeight);
			
		if( thisShadowGeometry.contributesToThisROI ) {
			shadowGeometrySpatialIndex.addShadowGeometryToIndex(thisShadowGeometry);
			
			
			
			
			shadowGeometryCount++;
		}
		if( sprite.getID()==371) {
			
			
			System.out.println("sprite 371 " + thisShadowGeometry.toStr());
			
		}
		return thisShadowGeometry.contributesToThisROI;
	}

	
	/**
	 * The second pass. Do this once in finaliseUserSession() method. 
	 *
	 * @param maxShadowContribution
	 * @param dropOffGamma
	 * @param useLightDirection
	 * @param renderRectDocSpace - defines the area you want rendered. This is nullable if you want the whole render doing
	 * @return - Returns TRUE if the shadow intersects the current documetSpaceREct. This will be the ROI rect is a ROI session. So, this is then used to help determine which sprites to preserve in a ROI's spriteBatch.
	 */
	public void renderShadows(float maxShadowContribution, float dropOffGamma, boolean useLightDirection, Rect renderRectDocSpace) {
		// Iterates over the image visiting each pixel only once, and renders the shadow of that pixelo based on the shadow line list.
		
		
		
		
		
		PVector lightDirectioProjected = new PVector(lightDirection.x, 0, lightDirection.z).normalize();
		
		// iterate over the render area
		//
		
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
				if (shadowRenderDepth == 0) {
					continue;
				}
				
				// get the docPoint of x,y
				PVector shadowPixelDocSpace = BStoDS(x, y);
				
				float shadowAmount = calculateShadowAmount(shadowPixelDocSpace, shadowRenderDepth,  maxShadowContribution,   dropOffGamma,  useLightDirection, lightDirectioProjected);
				
				contributeShadowToImage( x, y, shadowAmount);
				
				// end of more efficient loop

			}
			progress.update();
		}
		
		
		
	}
	
	
	private float calculateShadowAmount(PVector docPt, float pixelDepth, float maxShadowContribution,  float dropOffGamma, boolean useLightDirection, PVector lightDirectioProjected) {
		// normalisedShade = norm(d, radius, 0); // so when d == radius,normalisedShade = 0,  and when  d == 0 ,normalisedShade = 1
		// shadeAmount = pow(normalisedShade, dropOffGamma) // so when gamma == 1, drop off in linear, gamma < 1, drops off to maximum quickly at start. Gamma > 1 drops off only toward the end
		
		
		PVector shadowRenderPoint3D = sceneData3D.get3DVolumePoint(docPt, pixelDepth);
		//float radiusSquared = radius*radius;
		
		
		
		// get all the sprite shadows that intersect with this docPoint
		ArrayList<ShadowGeometry> intersectingShadows = getIntersectingShadows(docPt, pixelDepth);
		
		
		
		//setIntersectingShadows(docPt, pixelDepth);
		float accumulatedShadow = 0;
		
		
		
		for(ShadowGeometry thisShadow: intersectingShadows) {
			
			if(thisShadow.intersecting == false) continue;
			
			PVector clostestPointOnShadowLine = thisShadow.shadowLine.closestPointOnLine(shadowRenderPoint3D);

			float distanceSqFromShadowLine = shadowRenderPoint3D.distSq(clostestPointOnShadowLine);
			if(distanceSqFromShadowLine>thisShadow.shadowRadiusSq) {
				continue;
			}


			float distanceFromShadowLine = (float) Math.sqrt(distanceSqFromShadowLine);
			float shadowAmount01 = MOMaths.map(distanceFromShadowLine, 0, thisShadow.shadowRadius, maxShadowContribution, 0);
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

			if(shadowAmount01 < 0.0078f) {
				continue;
			}
			shadowAmount01 = MOMaths.constrain(shadowAmount01, 0.0078f,1);

			accumulatedShadow +=  (float) Math.pow(shadowAmount01, dropOffGamma);
			
			
		}
		
		//System.out.println("intersecting shadow num " + intersectingShadows.size() + " accumulated shadow " + accumulatedShadow);
		// returns shadow amount in the range 0..1
		if(accumulatedShadow>1) accumulatedShadow=1;
		return accumulatedShadow;
	}
	
	
	
	
	/**
	 * This method uses the spatial index to return only the ShadowGeometry that intersects this docSpace point
	 * @param docSpacePoint
	 * @param pixelDepth
	 * @return
	 */
	public ArrayList<ShadowGeometry> getIntersectingShadows(PVector docSpacePoint, float pixelDepth){
		
		ArrayList<ShadowGeometry> intersectingShadows = shadowGeometrySpatialIndex.getIntersectingShadowGeometry(docSpacePoint);
		for(ShadowGeometry ssi: intersectingShadows) {
			ssi.intersecting = false;
			if( ssi.shadowRectDocSpaceExtents.isPointInside(docSpacePoint) && MOMaths.isBetweenExc( ssi.shadowLine.p1.z , pixelDepth-ssi.shadowRadius, pixelDepth) ) {
				ssi.intersecting = true;
			}
			
		}
		
		if(pixelDepth == -1) {
			
			System.out.println("intersecting shadow num " + intersectingShadows.size() );
			
		}
		
		
		return intersectingShadows;
	}
	
	private void contributeShadowToImage(int x,int y, float shadowAmount01) {
		// the shadowAmout01 is between 0 and 1, where 1 would result in total shadow (black).
		// Shadow is added cumulatively to the image, in that it is "added" to the previous amount of shadow already there.
		//

		int existingPixelValue = shadowImageGetSet.getPixel(x, y);
		float existingShadowValue01 = 1-existingPixelValue*0.003922f;

		int newPixelValue = (int)  ((1-(existingShadowValue01 + shadowAmount01) ) * 255);
		newPixelValue = MOMaths.constrain(newPixelValue,0,255);
		//if( printCount < printLimit) {
		//	System.out.println(" shadowAmount01 in " + shadowAmount01 + " pixelvalue " + newPixelValue);
		//	printCount++;
		//}
		shadowImageGetSet.setPixel(x, y, newPixelValue);
	}

}


/**
 * Class that defines one sprites resultant shadow geometry. 
 * These are accumulated in the first pass, and then used to render the final image in the second pass.
 */
class ShadowGeometry{
	
	Line3D shadowLine;
	Rect shadowRectDocSpaceExtents;
	float shadowRadius, shadowRadiusSq;
	boolean intersecting = false;
	
	boolean contributesToThisROI = false;
	
	
	public ShadowGeometry(Sprite sprite, float shadowLineHeightProportion) {
		
			SceneData3D sceneData3D = GlobalSettings.getSceneData3D();
			float depth = sprite.depth;
			// all of this could be moved to the ShadowGeometry class
			// get the document space sprite top and bottom points, mapped into the scene, using the sprite's image quad
			PVector mappedSpriteBasePointDocSpace = sprite.mapNormalisedLocalSpritePointToDocSpace( 0.5f, 1.0f);
			PVector mappedSpriteTopPointDocSpace = sprite.mapNormalisedLocalSpritePointToDocSpace( 0.5f, 0);

			PVector shadowLineBasePoint3D = sceneData3D.get3DVolumePoint(mappedSpriteBasePointDocSpace, depth);
			PVector mappedSpriteTopPoint3D = sceneData3D.get3DVolumePoint(mappedSpriteTopPointDocSpace, depth);

			// interpolate over this to get the shadowLineTopPoint
			PVector shadowLineTopPoint3D = PVector.lerp(shadowLineBasePoint3D, mappedSpriteTopPoint3D, shadowLineHeightProportion);

			Line3D shadowLine = new Line3D(shadowLineBasePoint3D, shadowLineTopPoint3D);
			AABox3D shadowLineBoundingBox = shadowLine.getBoundingBox();
			
			// TBD:: radius should be a product to the lighting direction. When lit from completely above the shadow would be very short, when lit from a low angle
			// the shadow would be longer.
			float shadowRadius = shadowLineBoundingBox.getHeight();
			
			
			
			
			// establish screen-bounds of region to process
			//
			//
			float r = shadowRadius;
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

			Rect theDoumentDocSpaceRect = GlobalSettings.getDocument().getCoordinateSystem().getDocumentRect();
			contributesToThisROI =  shadowRectDocSpaceExtents.intersects(theDoumentDocSpaceRect) ;
		
			this.shadowLine = shadowLine;
			this.shadowRectDocSpaceExtents = shadowRectDocSpaceExtents;
			this.shadowRadius = shadowLine.getBoundingBox().getHeight();
			this.shadowRadiusSq = shadowRadius*shadowRadius;
		
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
		
		
	}
	
	
	/**
	 * Searches through all the spatially indexed boxes and adds this ShadowGeometry sg to those boxes that intersect it.
	 * @param sg
	 */
	void addShadowGeometryToIndex(ShadowGeometry sg) {
		
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








