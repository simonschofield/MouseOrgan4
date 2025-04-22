package MOMaths;

public class Util3D {
	
	static public PVector getRayPlaneIntersection(Ray3D r, Plane3D pl) {
		// returns the point, but also sets the intersection point and normal of plane in the ray
		// from https://stackoverflow.com/questions/23975555/how-to-calculate-a-ray-plane-intersection/53437900#53437900
		/*
		 Vector3 Intersect(Vector3 planeP, Vector3 planeN, Vector3 rayP, Vector3 rayD)
				{
					var d = Vector3.Dot(planeP, -planeN);
					var t = -(d + Vector3.Dot(rayP, planeN)) / Vector3.Dot(rayD, planeN);
					return rayP + t * rayD;
				}
		 * 
		 */
			
			
		 float d = dotV(pl.pointOnPlane, negV(pl.surfaceNormal) );
		 float t = -(d + dotV(r.origin, pl.surfaceNormal)) / dotV(r.direction, pl.surfaceNormal);
		 PVector intersectPoint = r.getPointAtDistance(t);
		 r.intersectionPoint = intersectPoint;
		 r.intersectionNormal = pl.surfaceNormal.copy();
		 return intersectPoint;
	}
	
	
	
	// a very specific methods that works with axis aligned planes.
	//
	//
	
	static public PVector getPlaneInXIntersection(Ray3D ray, PVector pointOnPlane) {
		if(ray.direction.x == 0) {
			// the ray is parallel with the sprite plane, so will never intersect
			return null;
		}
		float t = (pointOnPlane.x - ray.origin.x) / ray.direction.x;
		PVector ip = ray.getPointAtDistance(t);
		ray.intersectionPoint = ip.copy();
		ray.intersectionNormal = new PVector(1,0,0);
		return ip;
			
	}
	
	static public PVector getPlaneInYIntersection(Ray3D ray, PVector pointOnPlane) {
		if(ray.direction.y == 0) {
			// the ray is parallel with the sprite plane, so will never intersect
			return null;
		}
		float t = (pointOnPlane.y - ray.origin.y) / ray.direction.y;
		PVector ip = ray.getPointAtDistance(t);
		ray.intersectionPoint = ip.copy();
		ray.intersectionNormal = new PVector(0,1,0);
		return ip;
			
	}
	
	
	static public PVector getPlaneInZIntersection(Ray3D ray, PVector pointOnPlane) {
		if(ray.direction.z == 0) {
			// the ray is parallel with the sprite plane, so will never intersect
			return null;
		}
		float t = (pointOnPlane.z - ray.origin.z) / ray.direction.z;
		PVector ip = ray.getPointAtDistance(t);
		ray.intersectionPoint = ip.copy();
		ray.intersectionNormal = new PVector(0,0,1);
		return ip;
			
	}
	
	
		
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	//shorthand PVector methods to make your geometry code more readable
	//
	static public PVector vec(float x, float y, float z) {
		return new PVector(x, y, z);
	}
	
	
	static public PVector negV(PVector a) {
		return multV(a,-1);
	}


	static public PVector addV(PVector a, PVector b){
		return PVector.add(a,b);
	}

	static public PVector subV(PVector a, PVector b){
		return PVector.sub(a,b);
	}


	static public PVector crossV(PVector v1, PVector v2){
		return v1.cross(v2);
	}

	static public float dotV(PVector v1, PVector v2){
		return v1.dot(v2);
	}

	static public PVector multV(PVector v, float f){
		return PVector.mult(v,f);
	}


	
//	static public PVector rayPlaneIntersectionPoint(Ray3D ray, Plane3D plane) {
//
//	    // calculate plane
//	    float d = plane.surfaceNormal.dot(plane.pointOnPlane);
//
//	    if (plane.surfaceNormal.dot(ray.direction)==0) {
//	        return null; // avoid divide by zero
//	    }
//
//	    // Compute the t value for the directed line ray intersecting the plane
//	    float t = (d - plane.surfaceNormal.dot(ray.origin)) / plane.surfaceNormal.dot(ray.direction);
//
//	    // scale the ray by t
//	    PVector newRay = PVector.mult(ray.direction,t);
//
//	    // calc contact point
//	    PVector intrsectionPt = PVector.add(ray.origin,newRay);
//
//	    //if (t >= 0.0f && t <= 1.0f) {
//	        return intrsectionPt; // line intersects plane
//	    //}
//	    //return null; // line does not
//	}
	
	
	
	
	//Vector3 Intersect(Vector3 planeP, Vector3 planeN, Vector3 rayP, Vector3 rayD)
	//{
	//    var d = Vector3.Dot(planeP, -planeN);
	//    var t = -(d + rayP.z * planeN.z + rayP.y * planeN.y + rayP.x * planeN.x) / (rayD.z * planeN.z + rayD.y * planeN.y + rayD.x * planeN.x);
	//    return rayP + t * rayD;
	//}
	
	/*
	 * 
	 bool linePlaneIntersection(Vector& contact, Vector rayDirection, Vector rayOrigin, Vector planeNormal, Vector pointOnPlane) {

    // calculate plane
    float d = Dot(planeNormal, pointOnPlane);

    if (Dot(planeNormal, rayDirection )) {
        return false; // avoid divide by zero
    }

    // Compute the t value for the directed line ray intersecting the plane
    float t = (d - Dot(planeNormal, rayOrigin)) / Dot(planeNormal, rayDirection);

    // scale the ray by t
    Vector newRay = rayDirection * t;

    // calc contact point
    contact = rayOrigin + newRay;

    if (t >= 0.0f && t <= 1.0f) {
        return true; // line intersects plane
    }
    return false; // li
}
	 * 
	 * 
	 */

}
