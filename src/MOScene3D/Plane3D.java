package MOScene3D;

import MOMaths.PVector;

public class Plane3D {
	public PVector  pointOnPlane;
	public PVector surfaceNormal;


	public Plane3D(PVector pointOnPln, PVector snorm){
		init(pointOnPln,snorm);
	}


	void init(PVector pointOnPln, PVector snorm){
		pointOnPlane = pointOnPln.copy();
		surfaceNormal = snorm.copy();
		surfaceNormal.normalize();
	}

	public Plane3D(PVector p1, PVector p2, PVector p3) {
		PVector edge1 = PVector.sub(p2, p1);
		PVector edge2 = PVector.sub(p3, p1);
		PVector snorm = edge1.cross(edge2);
		init(p1,snorm);
	}

	public Plane3D copy() {
		return new Plane3D(this.surfaceNormal, this.pointOnPlane);
	}

	public String toStr() {
		return "Plane3D:  PointOnPlane: " + pointOnPlane.toStr() + " SurfaceNormal: " + surfaceNormal.toStr();
	}


	public PVector nearestPointOnPlane(PVector pointPosition)
    {


	    PVector v1 = PVector.sub(pointPosition, pointOnPlane);
	    float sn = -PVector.dot(surfaceNormal, v1);
	    float sd = PVector.dot(surfaceNormal, surfaceNormal);
	    float sb = sn / sd;
	    PVector v2 = PVector.mult(surfaceNormal, sb);
	    return PVector.add(pointPosition, v2);
    }







}
