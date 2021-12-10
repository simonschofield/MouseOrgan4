package MOMaths;


////////////////////////////////////////////////////////////////////////////////////////
// Really basic Ray class
//
public class Ray3D {
	
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

	public PVector getPointAtDistance(float d) {
		// returns the point on the ray at distance d from the origin
		PVector p1 = PVector.mult(direction, d);
		return PVector.add(p1, origin);
	}
	
	

}// end ray class
////////////////////////////////////////////////////////////////