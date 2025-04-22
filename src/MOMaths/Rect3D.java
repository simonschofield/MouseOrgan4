package MOMaths;

////////////////////////////////////////////////////////////////////
// Stores a rect with depth for use in 3D scenes. This is suitable for creating flat bounding boxes in 3D space.
// They can react to rays. Used in Shadow Tracing

public class Rect3D {
	PVector topLeftCorner;
	float width, height, depth;
	
	PVector corners[] = new PVector[4];
	
	public Rect3D(PVector corner1, PVector corner2) {
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
	
	public Rect3D(PVector tLCorner, float w, float h) {
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
	

