import java.util.Collections;
import java.util.ArrayList;




//////////////////////////////////////////////////////////////////////////////////
// Line2 is a line segment class
// Java complient
class Line2 {

  // the end points
  PVector p1;
  PVector p2;

  // intersection with other line
  boolean intersection_segmentsIntersect;
  PVector intersection_point;

  public Line2() {
    p1 = new PVector(0, 0);
    p2 = new PVector(0, 0);
  }

  public Line2(PVector point1, PVector point2) {
    this.p1 = point1.copy();
    this.p2 = point2.copy();
  }

  public Line2(float startX, float startY, float endX, float endY) {
    this.p1 = new PVector(startX, startY);
    this.p2 = new PVector(endX, endY);
  }

  

  float distancePointToLine(PVector p)
  {
    return (float)Math.sqrt( distancePointToLineSq(p) );
  }

  float distancePointToLineSq(PVector p)
  {
    // for closest tests - avoids sqrt
    PVector npl =  nearestPointOnLine(p);
    float dx = npl.x-p.x;
    float dy = npl.y-p.y;
    return (dx*dx)+(dy*dy);
  }

  PVector nearestPointOnLine(PVector p)
  {

    float px=p2.x-p1.x;
    float py=p2.y-p1.y;
    float div=(px*px)+(py*py);
    float u=((p.x - p1.x) * px + (p.y - p1.y) * py) / div;
    //println("point in", p, " p1 ", p1, " p2 ", p2, " u ", u);
    if (u>1) {
      return p2.copy();
    } else if (u<0) {
      return p1.copy();
    }
    float x = p1.x + u * px;
    float y = p1.y + u * py;

    return new PVector(x, y);
  }

  boolean calculateIntersection(Line2 l)
  {
    // returns true if this line segment intersects with other line segemnt
    // The intersection point can be recovered after the test via intersection_point
    // Even if the two segments do not intersect intersection_point will be set to the
    // intersection of the two infinte lines, unless they are parallel, in which case
    // intersection_point is set to null
    intersection_segmentsIntersect = false;

    PVector p3 = l.p1.copy();
    PVector p4 = l.p2.copy();

    // Get the segments' parameters.
    float dx12 = p2.x - p1.x;
    float dy12 = p2.y - p1.y;
    float dx34 = p4.x - p3.x;
    float dy34 = p4.y - p3.y;

    float intersection_t1 = -1;
    float intersection_t2 = -1; 

    // Solve for t1 and t2
    float denominator = (dy12 * dx34 - dx12 * dy34);

    // check if lines are parallel
    if (denominator < 0.00000001)
    {
      // The lines are parallel (or close enough to it).
      intersection_segmentsIntersect = false;
      intersection_point = null;
      return false;
    }
    intersection_t1 = ((p1.x - p3.x) * dy34 + (p3.y - p1.y) * dx34) / denominator;
    intersection_t2 = ((p3.x - p1.x) * dy12 + (p1.y - p3.y) * dx12) / -denominator;

    // Find the point of intersection.
    intersection_point = new PVector(p1.x + dx12 * intersection_t1, p1.y + dy12 * intersection_t1);

    // The segments intersect if t1 and t2 are between 0 and 1.
    intersection_segmentsIntersect = ((intersection_t1 >= 0) && (intersection_t1 <= 1) && (intersection_t2 >= 0) && (intersection_t2 <= 1));
    return intersection_segmentsIntersect;
  }



  boolean equals(Line2 otherLine) {
    if ( p1.equals(otherLine.p1) && p2.equals(otherLine.p2)) return true;
    return false;
  }

  boolean isSimilar(Line2 otherLine) {
    // will return true for a line with similar end points
    if ( equals(otherLine) || (  p1.equals(otherLine.p2) && p2.equals(otherLine.p1)  )  ) return true;
    return false;
  }

  PVector getOtherPoint(PVector thisPoint) {
    if (this.p1.equals(thisPoint)) return this.p2;
    if (this.p2.equals(thisPoint)) return this.p1;
    return null;
  }

  PVector interpolate(float a) {
	    float x = MOMaths.lerp(a, p1.x, p2.x);
	    float y = MOMaths.lerp(a, p1.y, p2.y);
	    return new PVector(x, y);
	  }


  PVector getNormalised() {
    return PVector.sub(p2, p1).normalize();
  }

  float getLength() {
    return p1.dist(p2);
  }
  
  float getRotation() {
	  PVector v =  getAsPVector();
	  float rads = v.heading();
	  return rads*57.296f + 90; 
  }
  
  float getAngleBetween(Line2 otherLine) {
    // returns the radians between the two lines.
    PVector vThis = getAsPVector();
    PVector vOther = otherLine.getAsPVector();
    return PVector.angleBetween(vThis, vOther);
  }

  PVector getAsPVector() {
    return PVector.sub(p2, p1);
  }
}// end Line2 class


/////////////////////////////////////////////////////////////
// Vertices2
// is initialised with an unclosed set of points
// so this algorithm closes the fist and last point
class Vertices2 {

  ArrayList<PVector> vertices;

  Vertices2( ArrayList<PVector> verts) {
    vertices = (ArrayList)verts.clone();
  }
  
  
  int getNumVertices(){
    return vertices.size();
  }

  boolean isPointInside(PVector p) {
    //using the winding method
    float a = 0;
    int numPoints = vertices.size();
    for (int i =0; i< numPoints-1; ++i) {
      PVector v1 = vertices.get(i).copy();
      PVector v2 = vertices.get(i+1).copy();
      a += vAtan2cent180(p, v1, v2);
    }
    PVector v1 = vertices.get(numPoints-1).copy();
    PVector v2 = vertices.get(0).copy();
    a += vAtan2cent180(p, v1, v2);
    //  if (a < 0.001) println(degrees(a));

    if (Math.abs(Math.abs(a) - Math.PI*2) < 0.001) return true;
    else return false;
  }

  private float vAtan2cent180(PVector cent, PVector v2, PVector v1) {
    PVector vA = v1.copy();
    PVector vB = v2.copy();
    vA.sub(cent);
    vB.sub(cent);
    vB.mult(-1);
    float ang = (float)(Math.atan2(vB.x, vB.y) - Math.atan2(vA.x, vA.y));
    if (ang < 0) ang = (float) Math.PI*2 + ang;
    ang-=Math.PI;
    return ang;
  }
  // end point inside


  Rect getExtents() {
    float minx = Float.MAX_VALUE;
    float miny = Float.MAX_VALUE;
    float maxx = -Float.MAX_VALUE;
    float maxy = -Float.MAX_VALUE;

    for (PVector p : vertices) {
      if (p.x < minx) minx = p.x;
      if (p.y < miny) miny = p.y;

      if (p.x > maxx) maxx = p.x;
      if (p.y > maxy) maxy = p.y;
    }
    PVector minExtents = new PVector(minx, miny);
    PVector maxExtents = new PVector(maxx, maxy);
    return new Rect(minExtents, maxExtents);
  }
}
