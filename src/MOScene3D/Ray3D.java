package MOScene3D;

import MOMaths.PVector;

////////////////////////////////////////////////////////////////////////////////////////
// Really basic Ray class
//
public class Ray3D {
	
	PVector origin = new PVector();
	PVector direction = new PVector(0, 0, -1);
	
	// data to be set in intersection algorithms and 
	// later recovered by the user if necessary
	public PVector intersectionPoint = new PVector(0, 0, 0);
	public PVector intersectionNormal = new PVector(0, 0, 0);

	boolean isIntersection = false;
	
	public Ray3D() {}
	
	public Ray3D(PVector orig, PVector dir) {
		origin = orig.copy();
		direction = dir.copy();
		direction.normalize();
	}
	
	
	
	void setFromTwoPoints(PVector orig, PVector pt2) {
		origin = orig.copy();
		direction =  PVector.sub(pt2,orig);
		direction.normalize();
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
	
	public String toStr() {
		return "Ray3D:  origin: " + origin.toStr() + " direction: " + direction.toStr();
	}
	
	
	
	
	

}// end ray class
////////////////////////////////////////////////////////////////