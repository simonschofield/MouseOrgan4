
class Rect{
  
  float left,top,right,bottom;
  public Rect(){
	  setWithExtents(0,0,1,1);
  }
  
  
  
	public Rect(float x1, float y1, float x2, float y2) {
		setWithExtents(x1, y1, x2, y2);
	}

	public Rect(PVector tl, PVector br) {
		setWithExtents(tl.x, tl.y, br.x, br.y);
	}

	public Rect(PVector upperleft, float w, float h) {
		setWithDimensions(upperleft.x, upperleft.y, w, h);
	}

	void setRect(Rect other) {
		setWithExtents(other.left, other.top, other.right, other.bottom);
	}

	void setWithExtents(float x1, float y1, float x2, float y2) {
		this.left = Math.min(x1, x2);
		this.top = Math.min(y1, y2);
		this.right = Math.max(x1, x2);
		this.bottom = Math.max(y1, y2);
	}

	void setWithDimensions(float x, float y, float w, float h) {
		left = x;
		top = y;
		right = left + w;
		bottom = top + h;

	}

	Rect copy() {
		return new Rect(left, top, right, bottom);
	}
  
  
  
  boolean equals(Rect other){
    if(left == other.left && top == other.top && 
       right == other.right && bottom == other.bottom) return true;
    return false;
  }
  
  Rect getTranslated(float dx, float dy){
	  // returns a translated copy
    return new Rect(left+dx, top+dy, right+dx, bottom+dy);
  }
  
  void translate(float dx, float dy) {
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
  
  void setCentreTo(float x, float y) {
	  float w = getWidth();
	  float h = getHeight();
	  left = x - w/2;;
	  top = y - h/2;
	  right = left+w;
	  bottom = top+h; 
  }
  
  /*
  Rect getNormalised(){
    // this function makes the rect a square, by lengthening the shortest edge
    // this is useful for certain image-related operations, where the x and y
    // need to be in the normal form
    Rect normRect = this.copy();
    float aspect = aspect();
    if( aspect > 1.0 ){
      // leave w alone, scale height up
      normRect.top=top*aspect;
      normRect.bottom=bottom*aspect; 
    }else{
      // leave h alone, scale up width
      normRect.left=left/aspect;
      normRect.right=right/aspect;
    }
    return normRect;
  }*/
  
  PVector getCentre(){
    float cx = this.left + (this.right - this.left)/2.0f;
    float cy = this.top + (this.bottom - this.top)/2.0f;
    return new PVector(cx,cy);
  }
  
  
  
  boolean isPointInside(PVector p){
    // inclusive of the boundries
    if(   MOMaths.isBetweenInc(p.x, this.left, this.right) && MOMaths.isBetweenInc(p.y, this.top, this.bottom) ) return true;
    return false;
  }
  
  boolean isPointInside(float x, float y){
    PVector v = new PVector(x,y);
    return isPointInside(v);
  }
  
  float getWidth(){
    return (this.right - this.left);
  }
  
  float getHeight(){
    return (this.bottom - this.top);
  }
  
	PVector getTopLeft() {
		return new PVector(left, top);
	}

	PVector getTopRight() {
		return new PVector(right, top);
	}

	PVector getBottomLeft() {
		return new PVector(left, bottom);
	}

	PVector getBottomRight() {
		return new PVector(right, bottom);
	}
  
  float aspect(){
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
  
  float area(){
    return getWidth()*getHeight();
  }
  
  PVector interpolate(PVector parametric){
    float x = MOMaths.lerp(parametric.x,this.left,this.right);
    float y = MOMaths.lerp(parametric.y,this.top,this.bottom );
    return new PVector(x,y);
  }
  
  PVector norm(PVector n) {
	  float x = MOMaths.norm(n.x,this.left,this.right);
	  float y = MOMaths.norm(n.y,this.top,this.bottom ); 
	  return new PVector(x,y);
  }
  
  String toStr() {
	 return new String(left + "," + top + "," + right + "," + bottom) ;
  }
  
  
  boolean intersects(Rect otherRect) {
      if((  this.left   >  otherRect.right  ) ||  (  this.right  <  otherRect.left   ) ) return false;
      if((  this.bottom <  otherRect.top    ) ||  (  this.top    >  otherRect.bottom ) ) return false;   
      return true;
  }
  
  boolean isWhollyInsideOther(Rect otherRect) {
	  return otherRect.isPointInside(getTopLeft()) && otherRect.isPointInside(getBottomRight());
  }
  
  
  String reportIntersection(Rect otherRect) {
	  // reports THIS rect's intersection with OTHER
	  // so, WHOLLYINSITE means THIS is WHOLLYINSIDE other, and LEFT means THIS LEFT intersects OTHER RECT
	  //System.out.println("reportIntersection: this Rect: " + this.toStr() + " Other rect "  + otherRect.toStr());
	  if( intersects( otherRect) == false) return "NONE";
	  if( isWhollyInsideOther( otherRect) ) return "WHOLLYINSIDE";
	  if( otherRect.isWhollyInsideOther( this) ) return "WHOLLYSURROUNDING";
	  
	  // reports the intersected side of the other rect
	  String intersections= ""; 
	  if(this.left<otherRect.left && this.right>otherRect.right) {
		  // straddles other rect lengthwise
		  intersections = addIntersectionString( intersections,"LEFT,RIGHT");
	  }else {
		  if(this.right > otherRect.left && this.left < otherRect.left)  intersections = addIntersectionString( intersections,"LEFT");
		  if(this.left < otherRect.right && this.right > otherRect.right) intersections = addIntersectionString( intersections,"RIGHT");
	  }
	  
	  if(this.top<otherRect.top && this.bottom>otherRect.bottom) {
		  // straddles other rect hieghtwise
		  intersections = addIntersectionString( intersections,"TOP,BOTTOM");
	  }else {
		  if(this.bottom > otherRect.top && this.top < otherRect.top)  intersections = addIntersectionString( intersections,"TOP");
		  if(this.top < otherRect.bottom && this.bottom > otherRect.bottom) intersections = addIntersectionString( intersections,"BOTTOM");
	  }
	  return intersections;
  }
  
  String addIntersectionString(String intersections, String intersection) {
		if(intersections.contentEquals("")) {
			intersections += intersection;
			return intersections;
		}
		intersections += ",";
		intersections += intersection;
		return intersections;
	}
  
  
  Rect getBooleanIntersection(Rect otherRect) {
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
 
	  return new Rect(tx1, ty1, tx2, ty2);
  }
  
  static PVector map(PVector p, Rect inThis, Rect toThis) {
	  float px = MOMaths.map(p.x, inThis.left, inThis.right, toThis.left, toThis.right);
	  float py = MOMaths.map(p.y, inThis.top, inThis.bottom, toThis.top, toThis.bottom);
	  
	  return new PVector(px,py);
  }
  
  int getQuadrant(PVector p){
	   // The rectange divides the space into 9 quadrants
	   // 0,1,2
	   // 3,4,5
	   // 6,7,8
	   // where quadant 4 is inside the rect.

	   if( p.x <= left && p.y <= top) return 0;
	   if( p.x >= left && p.x <= right && p.y <= top) return 1;
	   if( p.x >= right && p.y <= top) return 2;
	   if( p.x <= left && p.y >= top && p.y <= bottom) return 3;
	   if(isPointInside(p)) return 4;
	   if( p.x >= right && p.y >= top && p.y <= bottom) return 5;
	   if( p.x <= left && p.y >= bottom) return 6;
	   if( p.x >= left &&  p.x <= right && p.y >= bottom) return 7;
	   if( p.x >= right && p.y >= bottom) return 8;
	   
	   System.out.println("Rect get quadrant something is wrong" + p + " in " + getStr());
	   return -1;
	 }
  
  String getStr(){ 
	  return "Rect: L:" + left + " T:" + top + " R:" + right + " B:" + bottom; 
	  }
  
  
  
  
}// end Rect class




