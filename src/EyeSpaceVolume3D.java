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

	
	
	EyeSpaceVolume3D(float aspect, float vfov){
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
	
	
	
	PVector get3DPointAtDistance(PVector docSpacePt, float distance) {
		// gets a point into the scene, where the distance along the line is Distance 
		// cast a ray from eye pos to the docspace pos (with a z of 0)
		// normalise it. The multiply by the distance.
		PVector pointOnViewingPlane = new PVector(docSpacePt.x,docSpacePt.y,0);
		Ray3D ray = new Ray3D(eyePosition, pointOnViewingPlane);
		return ray.getPointAtDistance(distance);
	}
	
	PVector get3DPointAtDepth(PVector docSpacePt, float depth) {
		// gets a point into the scene, where the depth of the point is depth 
		// cast a line from eye pos to the docspace pos (with a z of 0)
		// find the parametric of that line at depth (will probably be outside of line segment)
		// lerp using this paramteric
		PVector pointOnViewingPlane = new PVector(docSpacePt.x,docSpacePt.y,0);
		Line3D line = new Line3D(eyePosition, pointOnViewingPlane);
		float n = line.normZ(depth);
		return line.lerp(n);
	}
	
	
	PVector getDocSpacePoint(PVector p3d) {
		// returns a z-enhanced doc space point from a 3d point
		// Returns null if the point is outside the view
		PVector pOnVP = project3DPointOntoVP(p3d);
		if( isPoint2DInView(pOnVP) == false) return null;
		pOnVP.z = p3d.z;
		return pOnVP;
	}
	

	PVector project3DPointOntoVP(PVector p) {
		// This are no front or rear clipping planes considered
		// find out doc space XY at z=0
		// can produce a point outside the doc space vp
		Line3D line = new Line3D(eyePosition, p);
		float nz = line.normZ(0);
		PVector pointAtZ0 = line.lerp(nz);
		return pointAtZ0;
	}
	
	boolean isPoint3DInView(PVector p3d) {
		PVector pOnVP = project3DPointOntoVP(p3d);
		return isPoint2DInView(pOnVP);
	}
	
	boolean isPoint2DInView(PVector pointAtZ0) {
		// Returns true if the point is within doc space
		// the point sent in should be a point on the viewing plane (i.e. z = 0)
		// although this only looks at the x and y coord
		if(pointAtZ0.x < 0 || pointAtZ0.x > fvpwidth) return false;
		if(pointAtZ0.y < 0 || pointAtZ0.y > fvpheight) return false;
		return true;
	}
	
	
	
	float getEyeDistanceFromZ0() {
		return Math.abs(eyePosition.z);
	}
	
	
	AABox3D getBoundingBox(float nearZ, float farZ) {
		// returns an AA bounding box of the view frustum between
		// near and farZ
		PVector farTLCorner = get3DPointAtDepth(new PVector(0,0), farZ);
		PVector farBRCorner = get3DPointAtDepth(new PVector(fvpwidth,fvpheight), farZ);
		
		PVector nearTLCorner = new PVector(farTLCorner.x,farTLCorner.y,nearZ);
		
		return new AABox3D(nearTLCorner, farBRCorner);
	}
	
	
}



class Line3D{
	
	PVector p1 = new PVector();
	PVector p2 = new PVector();
	
	Line3D(PVector p1, PVector p2){
		this.p1 = p1;
		this.p2 = p2;
	}
	
	
    PVector lerp(float p) {
    	return PVector.lerp(p1, p2, p);
    }
    
    float normZ(float z) {
    	return MOMaths.norm(z,  p1.z,  p2.z);
    }
	
}




class AABox3D{
	
	PVector minXYZ;
	PVector maxXYZ;
	
	public AABox3D(PVector p1, PVector p2) {
		init(p1,p2);
	}
	
	public AABox3D(float x1, float y1, float z1,float x2, float y2, float z2) {
		init( new PVector(x1,y1,z1), new PVector(x2,y2,z2));
		
	}
	
	void init(PVector p1, PVector p2) {
		float minX = Math.min(p1.x, p2.x);
		float minY = Math.min(p1.y, p2.y);
		float minZ = Math.min(p1.z, p2.z);
		
		float maxX = Math.max(p1.x, p2.x);
		float maxY = Math.max(p1.y, p2.y);
		float maxZ = Math.max(p1.z, p2.z);
		minXYZ = new PVector(minX,minY,minZ);
		maxXYZ = new PVector(maxX,maxY,maxZ);
		//System.out.println(" AABBox min xyz " + minXYZ + " maxXYZ " + maxXYZ);
	}
	
	boolean isPointInside(PVector p) {
		if( MOMaths.isBetweenInc(p.x, minXYZ.x, maxXYZ.x) &&
			MOMaths.isBetweenInc(p.y, minXYZ.y, maxXYZ.y) &&
			MOMaths.isBetweenInc(p.z, minXYZ.z, maxXYZ.z) ) return true;
		return false;
		
		
	}
	
	PVector getMin() {
		return minXYZ.copy();
	}
	
	PVector getMax() {
		return maxXYZ.copy();
	}

}



////////////////////////////////////////////////////////////////////////////////////////
// Really basic Ray class
//
class Ray3D {
	
	PVector origin = new PVector();
	PVector direction = new PVector(0, 0, -1);
	private PVector intersectionPoint = new PVector(0, 0, 0);

	boolean isIntersection = false;
	
	
	
	public Ray3D(PVector v1, PVector v2) {
		origin = v1.copy();
		direction = PVector.sub(v2, v1);
		direction.normalize();
	}
	
	public Ray3D() {}
	
	static Ray3D fromTwoPoints(PVector v1, PVector v2) {
		return new Ray3D(v1,v2);
	}
	
	public static Ray3D fromOriginAndVector(PVector o, PVector v) {
		PVector point2 = PVector.add(o, v);
		return new Ray3D(o,point2);
	}


	Ray3D copy() {
		Ray3D sr = new Ray3D(this.origin, this.direction);
		return sr;
	}

	PVector getPointAtDistance(float d) {
		// returns the point on the ray at distance d from the origin
		PVector p1 = PVector.mult(direction, d);
		return PVector.add(p1, origin);
	}
	
	

}// end ray class
////////////////////////////////////////////////////////////////
