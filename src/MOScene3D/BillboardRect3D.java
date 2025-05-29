package MOScene3D;

import MOMaths.MOMaths;
import MOMaths.PVector;


////////////////////////////////////////////////////////////////////
// Stores a flat 3d rect with a constant depth for use in 3D scenes. 
// If the constructor corners have different z, the average is taken.
// This is suitable for creating flat bounding boxes in 3D space.
// They can react to rays. Used in Shadow Tracing
//
// IN 3D Y is up, so is not a 3D analogy of a  2D rect.

public class BillboardRect3D {
	PVector bottomLeftCorner;
	float width, height, depth;

	// Corner order is TL,TR,BR,BL
	PVector corners[] = new PVector[4];

	public BillboardRect3D(PVector corner1, PVector corner2) {
		// corner min max is worked out internally
		float averageZ = (corner1.z + corner2.z)/2f;

		float minX = Math.min(corner1.x , corner2.x);
		float minY = Math.min(corner1.y , corner2.y);

		float maxX = Math.max(corner1.x , corner2.x);
		float maxY = Math.max(corner1.y , corner2.y);

		PVector bLCorner = new PVector(minX, minY, averageZ);
		float w = maxX-minX;
		float h = maxY-minY;
		init(bLCorner, w,h);
	}

	public BillboardRect3D(PVector bLCorner, float w, float h) {
		init(bLCorner, w,h);
	}



	private void init(PVector bLCorner, float w, float h){

		bottomLeftCorner = bLCorner.copy();
		width = w;
		height = h;
		depth = bLCorner.z;


		float x = bLCorner.x;
		float y = bLCorner.y;


		corners[0] = new PVector(x,y+height,depth);			// top left
		corners[1] = new PVector(x+width,y+height,depth);	// top right
		corners[2] = new PVector(x+width,y,depth);			// bottom right
		corners[3] = bottomLeftCorner.copy();         		// bottom left
	}




	public PVector[] getCorners() {
		// corners are returned clockwise from the topLeft
		return corners;

	}


	// a very specific method that works with sprite shadow casting.
	// As the rect plane is aligned in Z (has a constant z)
	// This is much faster than doing a generalised ray-plane intersection
	//
	public PVector getRayIntersectionPoint(Ray3D ray) {
		return Util3D.getPlaneInZIntersection(ray, bottomLeftCorner);
	}

	public boolean intersects(Ray3D ray) {
		PVector p = getRayIntersectionPoint(ray);
		if(p==null) return false;// the ray must have been parallel with the plane

		if( MOMaths.isBetweenInc(p.x, bottomLeftCorner.x, bottomLeftCorner.x+width)   &&
				MOMaths.isBetweenInc(p.y, bottomLeftCorner.y, bottomLeftCorner.y+height)	) return true;
		return false;
	}

	public PVector interpolate(PVector parametric){
		float x = MOMaths.lerp(parametric.x,bottomLeftCorner.x, bottomLeftCorner.x+width);
		float y = MOMaths.lerp(parametric.y,bottomLeftCorner.y, bottomLeftCorner.y+height );
		return new PVector(x,y);
	}

	public PVector norm(PVector n) {
		// returns the normalised position of p within this rect
		float x = MOMaths.norm(n.x,bottomLeftCorner.x, bottomLeftCorner.x+width);
		float y = MOMaths.norm(n.y,bottomLeftCorner.y, bottomLeftCorner.y+height ); 
		return new PVector(x,y);
	}
	
	public String toStr() {
		return "BillBoardRect3D:  clockwise from topleft: " + corners[0].toStr() + "," + corners[1].toStr() + "," + corners[2].toStr() + "," + corners[3].toStr();
	}




}




/*
////////////////////////////////////////////////////////////////////
// Stores a flat 3d rect with a constant depth for use in 3D scenes. This is suitable for creating flat bounding boxes in 3D space.
// They can react to rays. Used in Shadow Tracing

public class BillboardRect3D {
	PVector topLeftCorner;
	float width, height, depth;
	
	PVector corners[] = new PVector[4];
	
	public BillboardRect3D(PVector corner1, PVector corner2) {
		float averageZ = (corner1.z + corner2.z)/2f;
		float minX = Math.min(corner1.x , corner2.x);
		float minY = Math.min(corner1.y , corner2.y);
		
		float maxX = Math.max(corner1.x , corner2.x);
		float maxY = Math.max(corner1.y , corner2.y);
		
		PVector tLCorner = new PVector(minX, minY, averageZ);
		float w = maxX-minX;
		float h = maxY-minY;
		init(tLCorner, w,h);
	}
	
	public BillboardRect3D(PVector tLCorner, float w, float h) {
		init(tLCorner, w,h);
	}
	
	private void init(PVector tLCorner, float w, float h){

		topLeftCorner = tLCorner.copy();
		width = w;
		height = h;
		depth = tLCorner.z;
		
		
		float x = tLCorner.x;
		float y = tLCorner.y;
		
		
		corners[0] = topLeftCorner.copy();
		corners[1] = new PVector(x+width,y,depth);
		corners[2] = new PVector(x+width,y+height,depth);
		corners[3] = new PVector(x,y+height,depth);
	}
	
	
	public PVector[] getCorners() {
		// corners are returned clockwise from the topLeft
		return corners;
		
	}
	
	
	// a very specific method that works with sprite shadow casting.
	// As the rect plane is aligned in Z (has a constant z)
	// This is much faster than doing a generalised ray-plane intersection
	//
	public PVector getRayIntersectionPoint(Ray3D ray) {
		return Util3D.getPlaneInZIntersection(ray, topLeftCorner);
	}
	
	public boolean intersects(Ray3D ray) {
		PVector p = getRayIntersectionPoint(ray);
		if(p==null) return false;// the ray must have been parallel with the plane
		
		if( MOMaths.isBetweenInc(p.x, topLeftCorner.x, topLeftCorner.x+width)   &&
			MOMaths.isBetweenInc(p.y, topLeftCorner.y, topLeftCorner.y+height)	) return true;
		return false;
	}
	
	public PVector interpolate(PVector parametric){
		float x = MOMaths.lerp(parametric.x,topLeftCorner.x, topLeftCorner.x+width);
		float y = MOMaths.lerp(parametric.y,topLeftCorner.y, topLeftCorner.y+height );
		return new PVector(x,y);
	}

	public PVector norm(PVector n) {
		// returns the normalised position of p within this rect
		float x = MOMaths.norm(n.x,topLeftCorner.x, topLeftCorner.x+width);
		float y = MOMaths.norm(n.y,topLeftCorner.y, topLeftCorner.y+height ); 
		return new PVector(x,y);
	}
	
	
	
	
}
	

*/






