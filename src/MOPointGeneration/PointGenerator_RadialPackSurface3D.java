package MOPointGeneration;

import java.util.ArrayList;

import MOMaths.AABox3D;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOScene3D.SceneData3D;

////////////////////////////////////////////////////////////////////////////
//Generates a set of evenly distributed points on the 3D surface
//then preserves them as a depth-enhanced set of document points
//The depth is normalised 0...1
////////////////////////////////////////////////////////////////////////////
//The points are packed to a specified radius in 3D
//on the surface of the 3D in SceneData
//the algorithm keeps going until it fails to find a new packing point after a number of attempts.
//There is an option which packs according to the tone of an image, by adjusting the radius against the image tone,
//inherited from the super class, but using 3D packing in this class,
//The radius is defined in world 3D units
//
//
//Includes LOD on Z packing modification
//



public class PointGenerator_RadialPackSurface3D extends PointGenerator_RadialPack2D {

	float farDistanceMultiplier = 1;
	float nearDistanceThreshold = 0.5f;
	
	


	// a list of 3d points for the 3d packing algorithm
	ArrayList<PVector> points3d = new ArrayList<PVector>();
	SceneData3D sceneData;

	boolean useSurfaceArea = true;


	public PointGenerator_RadialPackSurface3D(int rseed, SceneData3D sd) {
		super(rseed);
		sceneData = sd;
	}
	
	
	
	public void setDepthSensitivePacking(float farMultiplier, float nearThreshold) {
		// when the distance is 1, the value out == radiusIn*farMultimpler
		// when the distance is nearDistanceThreshold value out == radius

		farDistanceMultiplier = farMultiplier;
		nearDistanceThreshold = nearThreshold;
	}
	
	float getDepthSensitiveRadius(float radiusIn, float normDepth) {
		if(farDistanceMultiplier==1) return radiusIn;
		if(normDepth < nearDistanceThreshold) return radiusIn;
		// when the distance is 1, the value out == radiusIn*farMultimpler
		// when the distance is nearDistanceThreshold value out == radius
		
		return MOMaths.map(normDepth, 1, nearDistanceThreshold, radiusIn*farDistanceMultiplier, radiusIn);
	}


	boolean tryAddDistributedPoint(PVector docSpcPt, float radius) {
		// the radius is coming in from an image based-calculation
		// we have the opportunity to impose a tweak to the radius here based on z depth interpolation.
	
		
		
		


		PVector thisPoint3d = sceneData.get3DSurfacePoint(docSpcPt);

		// this is where you would invent a depth for volume distribution
		// using sceneData.get3DVolumePoint(docSpcPt, invented depth);
		float normDepth = sceneData.getDepthNormalised(docSpcPt);

		radius = getDepthSensitiveRadius(radius, normDepth);
		
		

		if (pointExistsWithinRadius3d(thisPoint3d, radius))
			return false;

		// if suitably far from any other point, add both the 2d docspace point
		// and the 3d point
		PVector depthEnhancedDocSpacePt = docSpcPt.copy();
		depthEnhancedDocSpacePt.z = normDepth;
		points.add(depthEnhancedDocSpacePt);
		points3d.add(thisPoint3d);

		return true;

	}


	private boolean pointExistsWithinRadius3d(PVector p3d, float radius) {


		// If there are no points within the optimising rect, then returns true
		float x1 = p3d.x - radius;
		float y1 = p3d.y - radius;
		float z1 = p3d.z - radius;
		float x2 = p3d.x + radius;
		float y2 = p3d.y + radius;
		float z2 = p3d.z + radius;

		AABox3D boxUnderConsideration = new AABox3D(x1, y1, z1, x2, y2, z2);

		for (int n = 0; n < points.size(); n++) {
			PVector thisPoint3d = points3d.get(n);
			if (boxUnderConsideration.isPointInside(thisPoint3d)) {
				float dist = p3d.dist(thisPoint3d);
				if (dist < radius) {
					return true;
				}
			}

		}

		return false;

	}




}// end of PointGenerator_RadialPackSurface3D class



