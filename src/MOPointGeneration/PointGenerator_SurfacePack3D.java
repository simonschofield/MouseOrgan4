package MOPointGeneration;

import java.util.ArrayList;

import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOScene3D.AABox3D;
import MOScene3D.Plane3D;
import MOScene3D.SceneData3D;
///////////////////////////////////////////////////////////////////////////////////////////////////////////
// The idea is to iterate in floating point 2D to generate 3D points on the surface of the scene
// The store them is a simple spatial box collection
//
// The points are added with a packing radius in 3D units. If any other point is within that radius, then
// the point cannot be added, and proceed to the next point.
// Hence this is a monte-carlo type process
// To check any point's neighbours, the (possibly) 9 surrounding boxes' points are checked only. Therefore the maximum packing radius
// must be less than the width of a box, otherwise we need to be considering points beyond the 9 immediate neighbouring boxes
//
// Many boxes - that do not contain areas of the scene's surface -  will never contain points, but this does not matter, as new points will never index these boxes
//
// Iterating in 2D has the effect of there being many more "attempts" at adding points for near (visually larger) parts (or boxes) than further parts (or boxes).
// However the radius packing will come into play more visibly in these near areas. Distant areas may not receive so many visits, so will be unfairly under-packed. However,
// visually this may be OK and may reduce the need to "thin out" distant parts, as has been practised.
//
// The placed 3D points are stored in the boxMesh structure, and can be extracted wholesale using getAll3DPoints()
// The successfully placed 2D points are added to a list points2d, with a depth added into the z component.
//
//
import MOUtils.GlobalSettings;
import MOUtils.Progress;
import MOUtils.SecondsTimer;

/**
 * The point surface packer will pack points on a surface using a radius-per-point. When attempting to place the next point, if any other previous point
 * is already placed is within the packing radius, the new point cannot be placed (failed), so moves to the next one. Hence is a 
 * Monte-carlo type process. The algorithm ends when the desired number of points have been placed, or the consecutive failed number of attempts has exceeded a threshold (default is 300).
 * The point-placement radius can be made to respond to the brightness of an image, so can achieve clustering and thinning of points in
 * response to an image pixel values. It can also be responsive to depth, to achieve "depth-thinning".
 * 
 * The image sampling occurs on one of the SceneData's texture images, and is specified by the user. The relationship between the value of the pixel and the radius is set using
 * a PackingInterpolationScheme, which correlates a particular pixel brightness to a packing radius so that there is a linear relationship between density of points and the pixel value.
 * 
 * Packing requires that new points check neighbouring points, and this process is accelerated by using a spatial index of placed points.
 * 
 * When packing points there are two modes available: PACKINGMODE_2D_VISITATION mode iterates in screen-space to place new points. This is siple to implement but has the effect of iterating over near areas more than far areas, 
 * so has an in-built bias toward populating near areas over far areas.
 */
public class PointGenerator_SurfacePack3D {

	
	SpatiallyIndexedPointCollection spatiallyIndexedOutputPoints;
	AABox3D fullExtents;
	Rect generationArea_2d;

	
	ArrayList<PVector> points2DWithDepth = new ArrayList<>();
	SceneData3D sceneData;


	RandomStream ranStream;

	public int maxNumPoints = 2500000; // seems like a reasonable number!

	public float defaultPackingRadius = 0.2f;

	SecondsTimer timer;



	//////////////////////////////////////////////
	// radius response
	PackingInterpolationScheme packingInterpolationScheme;

	// These are set in normalised units
	float farDistanceMultiplier = 1;
	float nearDistanceThreshold = 0.5f;

	//



	Progress progress;
	
	public final static int PACKINGMODE_3D_VISITATION = 0;
	public final static int PACKINGMODE_2D_VISITATION = 1;
	int packingVisitationMode = PACKINGMODE_2D_VISITATION;
	
	SpatiallyIndexedPointCollection spatiallyIndexedSceneDataSurfacePoints;
	
	/**
	 * The point surface packer will pack points on a surface using a packing 3D radius. When attempting to place a new point, if any other previous point
	 * is already placed is within the packing radius, the new point cannot be placed (failed), so moves to the next one. Hence this is a 
	 * Monte-carlo type process. The algorithm ends when the desired number of points have been placed, or the consecutive failed number of attempts has exceeded a threshold (default is 300).
	 * The point-placement radius can be made to respond to the brightness of an image, so can achieve clustering and thinning of points in
	 * response to an image pixel values. It can also be responsive to depth, to achieve "depth-thinning".<p>
	 * 
	 * The image sampling occurs on one of the SceneData's texture images, and is specified by the user. The relationship between the value of the pixel and the radius is set using
	 * a PackingInterpolationScheme, which correlates a particular pixel brightness to a packing radius so that there is a good visual relationship between density of points and the pixel value.<p>
	 * 
	 * Hence packing requires that new points check neighbouring points, and this process is accelerated by using a spatial index of placed points.<p>
	 * 
	 * When packing points there are two modes available: PACKINGMODE_2D_VISITATION mode iterates in screen-space to place new points. 
	 * This is simple to implement but has the consequence of iterating over near areas more than far areas (as they occupy more screen space)
	 * so has an in-built bias toward populating near areas over far areas. This may suit, but it is not very controllable.<p>
	 * 
	 * When packing in PACKINGMODE_3D_VISITATION mode, it makes use of the Scenedata3D's in-built spatially indexed surface points. A random surface-point-box is selected, and a random 3D point (P3) generated 
	 * within. The nearest point on the surface is found (SP). A new surface point is created using the XZ of the random 3D point (P3) and the Y of SP.<p>
	 *
	 * The default visitationMode is PACKINGMODE_2D_VISITATION 
	 */
	public PointGenerator_SurfacePack3D() {

		sceneData = GlobalSettings.getSceneData3D();
		fullExtents = sceneData.depthBuffer3d.getExtents();
		// create a little bit of headroom
		float exp = fullExtents.getWidth()/100f;
		fullExtents.expand(exp);
		generationArea_2d = GlobalSettings.getTheDocumentCoordSystem().getDocumentRect();
		
		spatiallyIndexedSceneDataSurfacePoints  = sceneData.getSpatiallyIndexedSurfacePoints();
		
		packingVisitationMode = PACKINGMODE_2D_VISITATION;
	}
	
	
	
	
	
	/**
	 * @param vistationMode - either PACKINGMODE_2D_VISITATION or PACKINGMODE_3D_VISITATION
	 */
	public void setPointPackingVisitationMode(int vistationMode) {
		
		packingVisitationMode = vistationMode;
	}

	
	/**
	 * @param name - the short-name of the texture-image within the loaded SceneData textures
	 */
	public void setPackingImage(String name) {
		sceneData.setCurrentRenderImage(name);

	}

	/**
	 * @param n - the specified maximum number of placed points that are placed. The packing algorithm may bail before this number is achieved
	 */
	public void setMaxNumPointsLimit(int n) {
		maxNumPoints = n;
	}

	/**
	 * @param r - the 3D radius for the packing. use this method if you are not using an image-responsive PackingInterpolationScheme
	 */
	public void setDefaultPackingRadius(float r) {
		defaultPackingRadius = r;
		initialiseSpatiallyIndexedPointCollection(defaultPackingRadius);
	}

	/**
	 * @param pis - sets the image-responive packing interpolation scheme for this packing. See PackingInterpolationScheme class
	 */
	public void setPackingInterpolationScheme(PackingInterpolationScheme pis) {
		packingInterpolationScheme = pis;
		float maxRadius = Math.max(packingInterpolationScheme.radiusAtControlMin, packingInterpolationScheme.radiusAtControlMax);
		initialiseSpatiallyIndexedPointCollection(maxRadius);
	}

	/**
	 * Changes the packing radius according to scene depth. Uses normalised depth with respect to whole (master) scene data, not the ROI.
	 * Set the farMultiplier > 1 to get thinning to happen in the distance. Thinning occurs between the nearThreshold (no thinning) and the far depth (full thinning)
	 * when the distance is far (1), the value out == radiusIn*farMultimpler when the distance is nearDistanceThreshold value out == radius
	 * 
	 * @param farMultiplier
	 * @param nearThreshold
	 */
	public void setDepthSensitivePacking(float farMultiplier, float nearThreshold) {
		farDistanceMultiplier = farMultiplier;
		nearDistanceThreshold = nearThreshold;
	}

	/**
	 * @param r - the doc space Rect within whihc points are generated. If not set the full document space rect is used.
	 */
	public void setGenerationArea_2DVisitation(Rect r) {
		generationArea_2d = r;
	}

	/**
	 * @param n - integer. the seed used by the random stream determining the random point generation in both 2 and 3d
	 */
	public void setRandomStreamSeed(int n) {
		ranStream = new RandomStream(n);
	}


	
	/**
	 * The main packing method. Call this once all the parameters are set up correctly. It is sometimes a long process. It can operate in 2 modes, PACKINGMODE_2D_VISITATION or PACKINGMODE_3D_VISITATION<p>
	 * 
	 * The PACKINGMODE_2D_VISITATION mode:- A random 2D screen space point is projected back into a 3D surface point, and then a packing algorithm is used to see if 
	 * there is "space" for the new 3D point. This method has an inherent bias toward packing more densely in near areas, as those near areas occupy more screen-space than far points
	 * and so there are more points generated in near parts than far parts. <p>
	 * 
	 * The PACKINGMODE_3D_VISITATION mode:- A point packing method using a 3D random point generation as its basis.
	 * New random 3D surface points are generated based on a spatially indexed version of the depth buffer. For each point to be generated in 3D, a random (already populated) Spatial Box is selected.  
	 * An existing surface point EP is randomly selected from this Spatial Box, and a surface-plane established at this point, using the 3D scene surface normal at EP. 
	 * A new point NP is calculated on this surface plane, within the packing radius of the existing point. 
	 * Once the new 3D point NP is generated, it is added to the output point list if the packing algorithm determines that 
	 * there is "space" for this new 3D point NP. This method hopefully will counteract the inherent bias toward packing more densely in near areas using 2D visitation. <p>
	 * 
	 * @return - an array list of packed docSpace points that are DEPTH ENHANCED. I.e. have the z component set to the depth in the scene at that point.
	 */
	public ArrayList<PVector> generatePoints(){
		
		if(spatiallyIndexedOutputPoints == null) {
			System.out.println("Error - PointGenerator_SurfacePack3D::generatePoints() - the packing radius has not been defined, and so the spatial index has not been initialised - retuning null ");
			return null;
		}

		progress = new Progress("Generating Points ", maxNumPoints);
		points2DWithDepth.clear();
		spatiallyIndexedOutputPoints.clearAllPoints();
		progress.reset();
		timer = new SecondsTimer();

		int numPlaced = 0;
		int bailAtNumTries = 300;
		int failedSequentialTries = 0;
		while(true) {
			boolean isPlaced = false;
			
			PVector p3 = null;
			PVector p2 = null;
			
			
			if(packingVisitationMode == PACKINGMODE_2D_VISITATION) {
			
				p3 = null;
				p2 = getRandomDocSpacePoint();
			}
			
			if(packingVisitationMode == PACKINGMODE_3D_VISITATION) {
				
				p3 = getRandom3DSurfacePoint();
				p2 = sceneData.world3DToDocSpace(p3);
			}
			
			
			
			float controlValue = sceneData.getCurrentRender01Value(p2);
			//System.out.println("value at docx = ")
			if( isExcluded(controlValue)) {
				isPlaced = false;
			} else {

				float r = getRadiusRequired(controlValue);
				p3 = sceneData.get3DSurfacePoint(p2);

				r = applyDepthThinning(r,  p2);

				isPlaced = spatiallyIndexedOutputPoints.tryAddPointWithPackingRadius( p3,  r);
			}


			if(isPlaced) {
				// you have managed to place a 3D point!
				numPlaced++;
				failedSequentialTries = 0;
				p2.z = p3.z;
				points2DWithDepth.add(p2);

				progress.update();

			} else {
				failedSequentialTries++;

			}

			if( failedSequentialTries >= bailAtNumTries) {
				System.out.println("New Point Generator bailed at " + failedSequentialTries + " successive failed attempts, having placed " + points2DWithDepth.size() + " points");
				break;
			}



			if(numPlaced >= this.maxNumPoints) {
				System.out.println("New Point Generator successfully placed " + this.maxNumPoints + " points");
				break;
			}

		}

		System.out.println("Time taken " + timer.getElapsedTime() + " seconds");
		return PVector.deepCopy(points2DWithDepth);
	}
	
	

	


	///////////////////////////////////////////////////////////////////////////////////////////////
	// private methods
	//
	//
	
	/**
	 * This is kept separate from the main initialisation, as the user may not know the 
	 * @param spatialIndexBoxSize - the 3D dimension of the box in space. The full extents on the space in question is sub-divided into these boxes, so you have a number of boxes in x,y,z
	 */
	private void initialiseSpatiallyIndexedPointCollection(float spatialIndexBoxSize) {
		spatiallyIndexedOutputPoints = new SpatiallyIndexedPointCollection(fullExtents, spatialIndexBoxSize);
	}

	private boolean isExcluded(float v) {
		// returns false if the packing interpolation scheme determines that this
		// control value is excluded
		if( packingInterpolationScheme == null) {
			return false;
		}
		return packingInterpolationScheme.isExcluded(v);
		}

	private float getRadiusRequired(float controlValue) {
		// this is where you do your image sampling at p and using the
		// PackingInterpolationScheme return the radius required
		if( packingInterpolationScheme != null) {
			return packingInterpolationScheme.getRadius(controlValue);
		}
		return defaultPackingRadius;

	}




	private float applyDepthThinning(float radiusIn, PVector p) {

		if(farDistanceMultiplier != 1) {
			float normDepth = sceneData.getNormalisedDepth(p);


			if(normDepth < nearDistanceThreshold) {
				return radiusIn;
			}
			// when the normDepth is 1 (far), the radius out == radiusIn*farMultimpler
			// when the normDepth is < nearDistanceThreshold, radiusOut == radiusIn
			return MOMaths.map(normDepth, nearDistanceThreshold, 1f, radiusIn, radiusIn*farDistanceMultiplier);


			//return getDepthSensitiveRadius(radiusIn, nd);
		}
		return radiusIn;

	}
	
	
	/**
	 * Called by the 2D visitation packing algorithm. This method has a natural bias towards crrating points in near parts. This is becausenear points as the same 3D surface area occupies 
	 * more pixels in near points, than far points.
	 * @return - a doc-space point corresponding to a 3D surface point (of substance)
	 */
	private PVector getRandomDocSpacePoint() {

		PVector p = ranStream.randomPoint2(generationArea_2d);

		int bailCount = 0;

		while (true) {
			if (sceneData.isSubstance(p)) {
				return p;
			}
			p = ranStream.randomPoint2(generationArea_2d);
			bailCount++;
			if (bailCount > 1000000) {
				System.out.println("getRandomDocSpacePoint - cannot find any substance in the scene to add a point to - bailing with null");
				return null;
			}
		}

	}

	/**
	 * Called by the 3D visitation method. This generates a random 3D point on the surface of the scene. It then has to be converted to
	 * a doc-space point. This method avoids any spatial bias, and is as likely to produce a point in a distance 3D area, as a near 3D area.
	 * @return - A 3D point on the surface of the scene
	 */
	private PVector getRandom3DSurfacePoint() {
		// get some local points from a random location on the surface
		SpatiallyIndexedPointBox3D box = findRandomSpatiallyIndexedBox3D();
		ArrayList<PVector> surfacePoints = box.getLocalPoints();
		
		// select a random surface point from the above list and establish its plane
		int len = surfacePoints.size();
		int rindex = ranStream.randRangeInt(0, len);
		PVector p3d = surfacePoints.get(rindex).copy();
		
		
		
		// displace a new point from p3d, on the plane 
		PVector p2d = sceneData.world3DToDocSpace(p3d);
		PVector surfaceNormal = sceneData.getSurfaceNormal(p2d);
		Plane3D plane = new Plane3D(p3d,surfaceNormal);
		float displaceX = box.getWidth()/2;
		float displaceZ = box.getDepth()/2;
		
		PVector displacedP3d = p3d.copy();
		displacedP3d.x += ranStream.randRangeF(-displaceX, displaceX);
		displacedP3d.z += ranStream.randRangeF(-displaceZ, displaceZ);
		
		PVector pointOnPlane =  plane.nearestPointOnPlane(displacedP3d);
		
		
		
		//System.out.println("Based on found point " + p3d.toStr() + " displaced 3D point " + displacedP3d.toStr() + " point on plane " + pointOnPlane.toStr());
		return pointOnPlane;
	}
	
	
	/**
	 * @return and array list of 3D surface points that are local to a randomly selected spatially indexed box. Thereby cutting down the amount of 2D bias.
	 */
	SpatiallyIndexedPointBox3D findRandomSpatiallyIndexedBox3D() {
		
		int[] boxIndexDims = spatiallyIndexedSceneDataSurfacePoints.getBoxGridArrayDimensions();
		int bailCount = 0;
		
		while(true) {
		
		int rxi = ranStream.randRangeInt(0, boxIndexDims[0]);
		int ryi = ranStream.randRangeInt(0, boxIndexDims[1]);
		int rzi = ranStream.randRangeInt(0, boxIndexDims[2]);
		
		int numPoints = spatiallyIndexedSceneDataSurfacePoints.getBoxNumPoints(rxi, ryi, rzi);
		if(numPoints > 0) {
			
			return spatiallyIndexedSceneDataSurfacePoints.getBox(rxi, ryi, rzi);
			}
		bailCount++;
		if(bailCount>1000) {
			System.out.println("findRandomSpatiallyIndexedBox3DPoints - cannot find any spatially indexed boxes with points in after 1000 tries - bailing with null");
			return null;
		}
		
		}
		
		
	}
	
	
	
	
		
}// end class
	
	

	







