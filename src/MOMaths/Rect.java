package MOMaths;

public class Rect{

	public float left;
	public float top;
	public float right;
	public float bottom;
	
	
	public Rect(){
		setWithExtents(0,0,1,1);
	}



	public Rect(float x1, float y1, float w, float h) {
		setWithDimensions(x1, y1, w, h);
	}

	public Rect(PVector tl, PVector br) {
		setWithExtents(tl.x, tl.y, br.x, br.y);
	}

	public Rect(PVector upperleft, float w, float h) {
		setWithDimensions(upperleft.x, upperleft.y, w, h);
	}

	

	public void setWithExtents(float x1, float y1, float x2, float y2) {
		this.left = Math.min(x1, x2);
		this.top = Math.min(y1, y2);
		this.right = Math.max(x1, x2);
		this.bottom = Math.max(y1, y2);
	}
	
	

	public void setWithDimensions(float x, float y, float w, float h) {
		left = x;
		top = y;
		right = left + w;
		bottom = top + h;

	}

	public Rect copy() {
		return new Rect(left, top, getWidth(), getHeight());
	}



	boolean equals(Rect other){
		if(left == other.left && top == other.top && 
				right == other.right && bottom == other.bottom) return true;
		return false;
	}
	
	
	
	

	
	//////////////////////////////////////////////////////////////////////////////////
	// getting 
	//

	public PVector getCentre(){
		float cx = this.left + (this.right - this.left)/2.0f;
		float cy = this.top + (this.bottom - this.top)/2.0f;
		return new PVector(cx,cy);
	}


	public float getWidth(){
		return (this.right - this.left);
	}

	public float getHeight(){
		return (this.bottom - this.top);
	}

	public PVector getTopLeft() {
		return new PVector(left, top);
	}

	public PVector getTopRight() {
		return new PVector(right, top);
	}

	public PVector getBottomLeft() {
		return new PVector(left, bottom);
	}

	public PVector getBottomRight() {
		return new PVector(right, bottom);
	}

	public float aspect(){
		// returns the simple single number aspect width/height
		return getWidth()/getHeight();
	}


	float xAspect() {
		// returns 1 if x is the longer edge, < 1 if not
		if(aspect()>=1) return 1;
		return aspect();
	}

	float yAspect() {
		// returns 1 if y is the longer edge, < 1 if not
		if(aspect()>=1) return 1/aspect();
		return 1;
	}

	public float area(){
		return getWidth()*getHeight();
	}
	
	public String toStr() {
		return new String("x " + left + ", y " + top + ", width " + getWidth() + ", height " + getHeight()) ;
	}
	
	public static Rect getExtents(PVector[] pointList) {
		Range xrange = new Range();
		xrange.initialiseForExtremaSearch();
		Range yrange = new Range();
		yrange.initialiseForExtremaSearch();
		
		
		for(PVector p: pointList) {
			xrange.addExtremaCandidate(p.x);
			yrange.addExtremaCandidate(p.y);
		}
		
		PVector p1 = new PVector(xrange.getLower(), yrange.getLower());
		PVector p2 = new PVector(xrange.getUpper(), yrange.getUpper());
		
		Rect extents = new Rect(p1,p2);
		return extents;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////
	// translation and scale
	//
	
	Rect getTranslated(float dx, float dy){
		// returns a translated copy
		return new Rect(left+dx, top+dy, getWidth(), getHeight());
	}
	
	public Rect getScaled(float sx, float sy) {
		// translates in-place 
		Rect scaled = new Rect(left*sx, top*sy, getWidth()*sx, getHeight()*sy);
		return scaled;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////
	// in-place geometric alterations
	//

	public void translate(float dx, float dy) {
		// translates in-place 
		left+=dx;
		top+=dy;
		right+=dx;
		bottom+=dy; 
	}
	
	

	void moveTopLeftTo(float x, float y) {
		float w = getWidth();
		float h = getHeight();
		left = x;
		top = y;
		right = left+w;
		bottom = top+h; 
	}

	public void setCentreTo(float x, float y) {
		float w = getWidth();
		float h = getHeight();
		left = x - w/2;;
		top = y - h/2;
		right = left+w;
		bottom = top+h; 
	}
	
	public void dilate(float inX, float inY) {
		// can be used to grow and shrink the rect about its centre
		// inX and in Y are added to each side of the rect
		// +ve nums increase the size, -ve nums decrease the size
		left = left - inX;
		right = right + inX;
		top = top - inY;
		bottom = bottom + inY;
	}
	

	//////////////////////////////////////////////////////////////////////////////////
	// interpolation
	//
	public PVector interpolate(PVector parametric){
		float x = MOMaths.lerp(parametric.x,this.left,this.right);
		float y = MOMaths.lerp(parametric.y,this.top,this.bottom );
		return new PVector(x,y);
	}

	public PVector norm(PVector n) {
		// returns the normalised position of p within this rect
		float x = MOMaths.norm(n.x,this.left,this.right);
		float y = MOMaths.norm(n.y,this.top,this.bottom ); 
		return new PVector(x,y);
	}

	public Rect norm(Rect r) {
		// returns the normalised version of r within this rect
		PVector topleftNormalised = norm(r.getTopLeft());
		PVector bottomRightNormalised = norm(r.getBottomRight());
		return new Rect(topleftNormalised,bottomRightNormalised);
	}
	
	public static PVector map(PVector p, Rect inThis, Rect toThis) {
		float px = MOMaths.map(p.x, inThis.left, inThis.right, toThis.left, toThis.right);
		float py = MOMaths.map(p.y, inThis.top, inThis.bottom, toThis.top, toThis.bottom);

		return new PVector(px,py);
	}

	

	//////////////////////////////////////////////////////////////////////////////////
	// point-rect intersection
	//
	
	public boolean isPointInside(PVector p){
		// inclusive of the boundaries
		if(   MOMaths.isBetweenInc(p.x, this.left, this.right) && MOMaths.isBetweenInc(p.y, this.top, this.bottom) ) return true;
		return false;
	}
	
	public boolean isPointInside(PVector p, float tollerance){
		// inclusive of the boundaries plus a tollerance
		if(   MOMaths.isBetweenInc(p.x, this.left-tollerance, this.right+tollerance) && MOMaths.isBetweenInc(p.y, this.top-tollerance, this.bottom+tollerance) ) return true;
		return false;
	}

	public boolean isPointInside(float x, float y){
		PVector v = new PVector(x,y);
		return isPointInside(v);
	}
	
	int getQuadrant(PVector p){
		// Returns the quadant of a point relative to the rect. Only quadrant 4 is inside the rect.
		// The rectangle divides the space into 9 quadrants
		// 0,1,2
		// 3,4,5
		// 6,7,8
		// where quadrant 4 is inside the rect.

		if( p.x <= left && p.y <= top) return 0;
		if( p.x >= left && p.x <= right && p.y <= top) return 1;
		if( p.x >= right && p.y <= top) return 2;
		if( p.x <= left && p.y >= top && p.y <= bottom) return 3;
		if(isPointInside(p)) return 4;
		if( p.x >= right && p.y >= top && p.y <= bottom) return 5;
		if( p.x <= left && p.y >= bottom) return 6;
		if( p.x >= left &&  p.x <= right && p.y >= bottom) return 7;
		if( p.x >= right && p.y >= bottom) return 8;

		System.out.println("Rect getQuadrant something is wrong" + p + " in " + toStr());
		return -1;
	}

	public PVector constrain(PVector p) {
		
		float x = MOMaths.constrain(p.x, this.left, this.right);
		float y = MOMaths.constrain(p.y, this.top, this.bottom);
		return new PVector(x,y);
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// rect-rect intersection
	//
	
	public boolean intersects(Rect otherRect) {
		if((  this.left   >  otherRect.right  ) ||  (  this.right  <  otherRect.left   ) ) return false;
		if((  this.bottom <  otherRect.top    ) ||  (  this.top    >  otherRect.bottom ) ) return false;   
		return true;
	}

	public boolean isWhollyInsideOther(Rect otherRect) {
		return otherRect.isPointInside(getTopLeft()) && otherRect.isPointInside(getBottomRight());
	}


	public String reportIntersection(Rect otherRect) {
		// reports THIS rect's intersection with OTHER
		// so, WHOLLYINSIDE means THIS rect is WHOLLYINSIDE other, and LEFT means THIS LEFT intersects OTHER RECT
		//System.out.println("reportIntersection: this rect: " + this.toStr() + " Other rect "  + otherRect.toStr());
		// The report results are Strings:
		// NONE : no intersection between THIS rect and the OTHER rect
		// WHOLLYINSIDE : THIS rect is wholly inside the OTHER rect
		// WHOLLYSURROUNDING : THIS rect wholly surrounds the OTHER rect
		// LEFT : THIS rect intersects the left side of the other rect, same for TOP, RIGHT and BOTTOM
		// However, THIS rect may intersect a combination of sides of the other rect, for instance if THIS rect may straddle the left and right edges of the other rect.
		// In the above example the returned string would be "LEFT,RIGHT". 
		if( intersects( otherRect) == false) return "NONE";
		if( isWhollyInsideOther( otherRect) ) return "WHOLLYINSIDE";
		if( otherRect.isWhollyInsideOther( this) ) return "WHOLLYSURROUNDING";

		// reports the intersected side of the other rect
		String intersections= ""; 
		if(this.left<otherRect.left && this.right>otherRect.right) {
			// straddles other rect lengthwise
			intersections = addIntersectionString( intersections,"LEFT,RIGHT");
		}else {
			if(this.right >= otherRect.left && this.left <= otherRect.left)  intersections = addIntersectionString( intersections,"LEFT");
			if(this.left <= otherRect.right && this.right >= otherRect.right) intersections = addIntersectionString( intersections,"RIGHT");
		}

		if(this.top<otherRect.top && this.bottom>otherRect.bottom) {
			// straddles other rect hieghtwise
			intersections = addIntersectionString( intersections,"TOP,BOTTOM");
		}else {
			if(this.bottom >= otherRect.top && this.top <= otherRect.top)  intersections = addIntersectionString( intersections,"TOP");
			if(this.top <= otherRect.bottom && this.bottom >= otherRect.bottom) intersections = addIntersectionString( intersections,"BOTTOM");
		}
		
		
		if(intersections.equals("")) {
			// just in case there is a problem
			System.out.println("Rect::reportIntersection has generated no intersections " + intersections);
			System.out.println("this rect " + this.toStr());
			System.out.println("other rect " + otherRect.toStr());
		}
		
		
		return intersections;
	}

	private String addIntersectionString(String intersections, String intersection) {
		if(intersections.contentEquals("")) {
			intersections += intersection;
			return intersections;
		}
		intersections += ",";
		intersections += intersection;
		return intersections;
	}


	public Rect getBooleanIntersection(Rect otherRect) {
		if( intersects( otherRect) == false) return null; 

		float tx1 = this.left;
		float ty1 = this.top;
		float tx2 = this.right;
		float ty2 = this.bottom;
		float rx1 = otherRect.left;
		float ry1 = otherRect.top;
		float rx2 = otherRect.right;
		float ry2 = otherRect.bottom;

		if (tx1 < rx1) tx1 = rx1;
		if (ty1 < ry1) ty1 = ry1;
		if (tx2 > rx2) tx2 = rx2;
		if (ty2 > ry2) ty2 = ry2;

		return new Rect(new PVector(tx1, ty1), new PVector(tx2, ty2));
	}
	
	public boolean canLineIntersect(PVector startPt, PVector endPt){
	    // this is a trivial check to see if a line is wholly above, right, left or below the rect.
	    // To see if the line actually intersects, you have to do a more thorough check...
	    Rect lineRect = new Rect(startPt, endPt);
	    return intersects(lineRect );
	  }

	
	



}// end Rect class




