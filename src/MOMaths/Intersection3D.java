package MOMaths;

public class Intersection3D {
	
	
	static public PVector rayPlaneIntersectionPoint(Ray3D ray, Plane3D plane) {

	    // calculate plane
	    float d = plane.surfaceNormal.dot(plane.pointOnPlane);

	    if (plane.surfaceNormal.dot(ray.direction)==0) {
	        return null; // avoid divide by zero
	    }

	    // Compute the t value for the directed line ray intersecting the plane
	    float t = (d - plane.surfaceNormal.dot(ray.origin)) / plane.surfaceNormal.dot(ray.direction);

	    // scale the ray by t
	    PVector newRay = PVector.mult(ray.direction,t);

	    // calc contact point
	    PVector intrsectionPt = PVector.add(ray.origin,newRay);

	    //if (t >= 0.0f && t <= 1.0f) {
	        return intrsectionPt; // line intersects plane
	    //}
	    //return null; // line does not
	}
	
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
