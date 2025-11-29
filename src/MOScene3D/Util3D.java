package MOScene3D;

import java.awt.Color;

import MOMaths.PVector;
import MOUtils.GlobalSettings;

public class Util3D {

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

	// shorthand converting world space point to doc space point
	static public PVector W3ToDS(PVector p3) {
		return GlobalSettings.getSceneData3D().world3DToDocSpace(p3);
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//handy drawing methods, more for debugging than anything else
	//
	static public void drawLineWorldSpace(PVector w1, PVector w2, Color c, float weight) {
		drawLineDocSpace(W3ToDS(w1), W3ToDS(w2),  c,  weight);
	}

	static public void drawLineDocSpace(PVector p1, PVector p2, Color c, float weight) {
		GlobalSettings.getDocument().getMain().getVectorShapeDrawer().setDrawingStyle(c, c, weight);

		PVector bp1 = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(p1);
		PVector bp2 = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(p2);
		GlobalSettings.getDocument().getMain().getVectorShapeDrawer().drawLine(bp1.x, bp1.y, bp2.x, bp2.y);
	}

	static public void drawRect3D(BillboardRect3D r3, Color c, float weight) {

		PVector[] corners = r3.getCorners();

		draw3DPoint(corners[0],  c,  weight*3);
		drawLineWorldSpace(corners[0], corners[1],  c,  weight);
		drawLineWorldSpace(corners[1], corners[2],  c,  weight);
		drawLineWorldSpace(corners[2], corners[3],  c,  weight);
		drawLineWorldSpace(corners[3], corners[0],  c,  weight);

	}


	public static void draw3DPoint(PVector p3, Color c, float weight) {
		GlobalSettings.getDocument().getMain().getVectorShapeDrawer().setDrawingStyle(c, c, weight);
		PVector p2 = GlobalSettings.getSceneData3D().world3DToDocSpace(p3);

		PVector bp = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(p2);
		GlobalSettings.getDocument().getMain().getVectorShapeDrawer().drawEllipse(bp.x, bp.y, 15, 15);
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//general 3D object intersection code
	//
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


}
