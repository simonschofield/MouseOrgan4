package MOScene3D;

import MOMaths.PVector;

//////////////////////////////////////////////////////////////////////
// 3D geometric elements
//


//////////////////////////////////////////////////////////////////////
// EyeSpaceVolume3D. The dimensions of the viewing plane are in document space.
// so TLH is (0,0,0) and BRH is (fvpwidth, fvphieght, 0) where fvpwidth or fvphieght is 1, and the other is the shorter of the aspect dims.
//
// The front viewing plane is the z = 0 plane. There is no rear viewing plane
// Going into the scene, away from the eye, is +ve Z
// The camera is placed behind the viewing plane at a -ve Z value, and positioned in the centre of the front vp.
//


public class EyeSpaceVolume3D {

	// the aspect of the front plane, and the front viewing plane repsective dimensions
	float viewPlaneAspect = 1;
	float fvpheight, fvpwidth;

	// is set and stored in degrees
	float verticalFieldOfView = 30;

	// the camera eye position set at the start
	PVector eyePosition = new PVector();



	public EyeSpaceVolume3D(float aspect, float vfov){
		viewPlaneAspect = aspect;

		if(viewPlaneAspect > 1) {
			fvpwidth = 1;
			fvpheight = 1/viewPlaneAspect;
		} else {
			fvpwidth = viewPlaneAspect;
			fvpheight = 1;
		}

		verticalFieldOfView = vfov;

		double fovRadians = Math.toRadians(verticalFieldOfView);
		float cameraZ = (float) ((fvpheight/2) / Math.tan(fovRadians/2));

		eyePosition.x = fvpwidth/2;
		eyePosition.y = fvpheight/2;
		eyePosition.z = -cameraZ;

	}



	public PVector get3DPointAtDistance(PVector docSpacePt, float distance) {
		// gets a point into the scene, where the distance along the line is Distance
		// cast a ray from eye pos to the docspace pos (with a z of 0)
		// normalise it. The multiply by the distance.
		PVector pointOnViewingPlane = new PVector(docSpacePt.x,docSpacePt.y,0);
		Ray3D ray = new Ray3D();
		ray.setFromTwoPoints(eyePosition, pointOnViewingPlane);
		return ray.getPointAtDistance(distance);
	}

	public PVector get3DPointAtDepth(PVector docSpacePt, float depth) {
		// gets a point into the scene, where the depth of the point is depth
		// cast a line from eye pos to the docspace pos (with a z of 0)
		// find the parametric of that line at depth (will probably be outside of line segment)
		// lerp using this paramteric
		PVector pointOnViewingPlane = new PVector(docSpacePt.x,docSpacePt.y,0);
		Line3D line = new Line3D(eyePosition, pointOnViewingPlane);
		float n = line.normZ(depth);
		return line.lerp(n);
	}


	public PVector getDocSpacePoint(PVector p3d) {
		// returns a z-enhanced doc space point from a 3d point
		// Returns null if the point is outside the view
		PVector pOnVP = project3DPointOntoVP(p3d);
		if( !isPoint2DInView(pOnVP)) {
			return null;
		}
		pOnVP.z = p3d.z;
		return pOnVP;
	}


	public PVector project3DPointOntoVP(PVector p) {
		// This are no front or rear clipping planes considered
		// find out doc space XY at z=0
		// can produce a point outside the doc space vp
		Line3D line = new Line3D(eyePosition, p);
		float nz = line.normZ(0);
		PVector pointAtZ0 = line.lerp(nz);
		return pointAtZ0;
	}

	public boolean isPoint3DInView(PVector p3d) {
		PVector pOnVP = project3DPointOntoVP(p3d);
		return isPoint2DInView(pOnVP);
	}

	public boolean isPoint2DInView(PVector pointAtZ0) {
		// Returns true if the point is within doc space
		// the point sent in should be a point on the viewing plane (i.e. z = 0)
		// although this only looks at the x and y coord
		if(pointAtZ0.x < 0 || pointAtZ0.x > fvpwidth || pointAtZ0.y < 0 || pointAtZ0.y > fvpheight) {
			return false;
		}
		return true;
	}



	public float getEyeDistanceFromZ0() {
		return Math.abs(eyePosition.z);
	}


	public AABox3D getBoundingBox(float nearZ, float farZ) {
		// returns an AA bounding box of the view frustum between
		// near and farZ
		PVector farTLCorner = get3DPointAtDepth(new PVector(0,0), farZ);
		PVector farBRCorner = get3DPointAtDepth(new PVector(fvpwidth,fvpheight), farZ);

		PVector nearTLCorner = new PVector(farTLCorner.x,farTLCorner.y,nearZ);

		return new AABox3D(nearTLCorner, farBRCorner);
	}


}


