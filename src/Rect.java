
class Rect{
  
  float left,top,right,bottom;
  public Rect(){
    
  }
  
  
  
  //public Rect(PVector upperleft, PVector lowerright){
  //  setRect(upperleft.x,upperleft.y,lowerright.x,lowerright.y);
  //}
  
  public Rect(float x1, float y1, float x2, float y2){
    setRect(x1,y1,x2,y2);
  }
  
  public Rect(PVector tl, PVector br) {
	  setRect(tl.x,tl.y,br.x,br.y);
  }
  
  void setRect(Rect other){
    setRect(other.left, other.top, other.right, other.bottom);
  }
  
  
  
  Rect copy(){
    return new Rect(left, top, right, bottom);
  }
  
  void setRect(float x1, float y1, float x2, float y2){
    this.left = Math.min(x1,x2);
    this.top = Math.min(y1,y2);
    this.right = Math.max(x1,x2);
    this.bottom = Math.max(y1,y2);
  }
  
  
  boolean equals(Rect other){
    if(left == other.left && top == other.top && 
       right == other.right && bottom == other.bottom) return true;
    return false;
  }
  
  Rect getTranslated(float dx, float dy){
    return new Rect(left+dx, top+dy, right+dx, bottom+dy);
  }
  
  
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
  }
  
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
  
  PVector getTopLeft(){
    return new PVector(left,top);
  }
  
  PVector getBottomRight(){
    return new PVector(right,bottom);
  }
  
  float aspect(){
    return getWidth()/getHeight();
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
	  if( intersects( otherRect) == false) return "NONE";
	  if( isWhollyInsideOther( otherRect) ) return "WHOLLYINSIDE";
	  if( otherRect.isWhollyInsideOther( this) ) return "WHOLLYSURROUNDING";
	  
	  // reports the intersected side of the other rect
	  String intersections= ""; 
	  if(this.left<otherRect.left && this.right>otherRect.right) {
		  // straddles other rect lengthwise
		  intersections = addIntersectionString( intersections,"LEFT_RIGHT");
	  }else {
		  if(this.right > otherRect.left && this.left < otherRect.left)  intersections = addIntersectionString( intersections,"LEFT");
		  if(this.left < otherRect.right && this.right > otherRect.right) intersections = addIntersectionString( intersections,"RIGHT");
	  }
	  
	  if(this.top<otherRect.top && this.bottom>otherRect.bottom) {
		  // straddles other rect hieghtwise
		  intersections = addIntersectionString( intersections,"TOP_BOTTOM");
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
  
  
}// end Rect class




class AABox{
	
	PVector corner;
	PVector opposite;
	
	public AABox(PVector p1, PVector p2) {
		corner = p1;
		opposite = p2;
		
	}
	
	public AABox(float x1, float y1, float z1,float x2, float y2, float z2) {
		corner = new PVector(x1,y1,z1);
		opposite = new PVector(x2,y2,z2);
		
	}
	
	boolean isPointInside(PVector p) {
		if( MOMaths.isBetweenInc(p.x, corner.x, opposite.x) &&
			MOMaths.isBetweenInc(p.y, corner.y, opposite.y) &&
			MOMaths.isBetweenInc(p.z, corner.z, opposite.z) ) return true;
		return false;
		
		
	}

}

