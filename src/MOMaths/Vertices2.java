package MOMaths;

import java.util.Collections;

import MOUtils.MOUtilGlobals;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;





/////////////////////////////////////////////////////////////
// Vertices2
// is initialised with collection of points.
// You can close the lines, which inserts an end point the same as the start point.
// Once closed you can re-open it.
// Needs to be closed to use the isPointInside() method
public class Vertices2 {

  ArrayList<PVector> vertices;

  public Vertices2(){
	  vertices = new ArrayList<PVector>();
  }
  
  public Vertices2( ArrayList<PVector> verts) {
    vertices = (ArrayList)verts.clone();
  }
  
  
  
  public Vertices2 copy() {
	  return new Vertices2(this.vertices);
  }
  
  
  
  public boolean setWithLine2List(ArrayList<Line2> lines) {
	  // assumes lines are connected
	  vertices = new ArrayList<PVector>();
	  PVector lastLineEndPoint = lines.get(0).p1;
	  for(int n = 0; n < lines.size(); n++) {
		  Line2 line = lines.get(n);
		  if(n > 0) {
			 if( lastLineEndPoint.equals(line.p1) ==  false) {
				 System.out.println(" Vertices2: setWithLine2List -  line " + (n-1) + " and " + n + " are not contiguous");
				 return false;
			 }
			  
		  }
		  vertices.add(line.p1);
		  
		  if(n == lines.size()-1) {
			  vertices.add(line.p2); 
		  }
		  
		  lastLineEndPoint = line.p2;
		  
	  }
	  return true;
  }
  

  public ArrayList<Line2> getAsLine2List(){
	  ArrayList<Line2> linesOut = new ArrayList<Line2>();
	  int numLines = getNumLines();
	  for(int n = 0; n < numLines; n++) {
		  
		  linesOut.add( getLine(n) );
	  }
	 return  linesOut;
  }
  
  
  
  
  
  public int size(){ return vertices.size();}
  
  public void add(PVector p){ vertices.add(p.copy());}
  
  public void addAt(int i, PVector p){ vertices.add(i, p.copy());}
  
  public void add(Vertices2 vin){
	  Vertices2 v = vin.copy();
    // adds a new set of vertices on to the end. If it finds that the end point of the existing verts
    // and the start point of the added verts are the same, then it ignores the duplicated point
    if(  v.getStartPoint().equals( this.getEndPoint() )  )  {
      v.remove(0);
    }
    
    this.vertices.addAll(  v.vertices );
  }
  
  public void addAt(int i, Vertices2 vin){
    Vertices2 v = vin.copy();
    // adds a new set of vertices on to the end. If it finds that the end point of the existing verts
    // and the start point of the added verts are the same, then it ignores the duplicated point
    if(  v.getStartPoint().equals( this.get(i) )  )  {
      v.remove(0);
    }
    
    this.vertices.addAll( i, v.vertices );
  }
  
  public PVector get(int n){ return vertices.get(n);}
  
  public void set(int n, PVector p){ vertices.set(n,p.copy()); }
  
  public void clear(){ vertices.clear(); }
  
  public void set(ArrayList<PVector> points){ 
    for(PVector p:points){
      this.add(p);
     }
  }
  
  public void remove(int i){
	  vertices.remove(i);
  }
  
  public void reverse(){
    Collections.reverse(vertices);
  }
  
  //////////////////////////////////////////////////////////////////////////
  // open and closed, and closing a vertices. Normally a vertices2 would be open, in that
  // the fist and last points would not be co-incident. A Closed line, means that the start and end point
  // are co-incident (not a reference to the same point, but separate points equal in x and y coordinate value).
  // Therefore closing a vertices adds a new point onto the end which is equal to the start point. Opening a closed vertices, removes then co-incident end point.
  //
  public void open() {
	  if(isClosed()==false) return;
	  int lastElement = vertices.size()-1;
	  vertices.remove( lastElement );
  }
  
  public void close() {
	  if(isClosed()) return;
	  PVector startP = getStartPoint();
	  vertices.add(startP);
  }
  
  public boolean isClosed() {
	  PVector startP = getStartPoint();
	  PVector endP = getEndPoint();
	  
	  if(startP.equals(endP)) return true;
	  return false;
  }
  
  
  
  
  public PVector getStartPoint(){
    return vertices.get(0);
  }
  
  public PVector getEndPoint(){
    if( vertices.size() == 0) return null;
    return vertices.get( vertices.size()-1 );
  }
  
  public PVector popStartPoint(){
    PVector p = vertices.get(0);
    vertices.remove(0);
    return p;
  }

  public boolean isPointInside(PVector p) {
    //using the winding method
	if(isClosed()==false) {
		System.out.println("Vertices2: isPointInside shape is not closed");
		return false;
	}
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


  public Rect getExtents() {
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
  
  public int getNumLines() {
	  return this.size()-1;
  }
  
  public Line2 getLine(int n) {
	  // there are NumVertices - 1 lines.
	  PVector p1 = vertices.get(n);
	  PVector p2 = vertices.get(n+1);
	  return new Line2(p1,p2);
  }
  
  public void translate(float dx, float dy) {
	  for(PVector p: vertices) {
		  p.x += dx;
		  p.y += dy;
	  }
  }
  
  public void scale(float sx, float sy) {
	  for(PVector p: vertices) {
		  p.x *= sx;
		  p.y *= sy;
	  }
	  
  }
  
  public void scaleAbout(float origX, float origY, float scaleX, float scaleY) {
	  translate(-origX, -origY );
	  scale(scaleX,scaleY);
	  translate(origX, origY );
  }
  
  public PVector lerp(float param) {
	 // traverses the vertices, where 0 returns the first point
	 // 1 returns the last point
	 float totalLength =  getTotalLength();
	 float targetLength = totalLength*param;
	 
	 float traversedLength = 0;
	  for(int n = 0; n < getNumLines(); n++) {
		  Line2 thisLine = getLine(n);
		  traversedLength += thisLine.getLength();
		  if(traversedLength > targetLength) {
			  // then the targetlength lies within this line. So...
			  // get the length as it was before this line...
			  float traversedLengthAtStartOfThisLine = traversedLength- thisLine.getLength();
			  // get the length along this line to get to targetlength
			  float targetLengthAlongThisLine = targetLength - traversedLengthAtStartOfThisLine;
			  // nromalize this 0..1
			  float thisLineParam = targetLengthAlongThisLine/thisLine.getLength();
			  return thisLine.interpolate(thisLineParam);
		  }
	  }
	  return getEndPoint();
  }
  
  
  
  public float getTotalLength() {
	  float totalLength = 0;
	  for(int n = 0; n < getNumLines(); n++) {
		  Line2 thisLine = getLine(n);
		  totalLength += thisLine.getLength();
	  }
	  return totalLength;
  }
  
  
  
  
  public Vertices2 getInBufferSpace(boolean shiftToTopLeft) {
	  // used by TextRenderer, render text in polygon
	  Vertices2 bufferSpaceVerts = new Vertices2();
	    int numPoints = this.size();
	    
	    PVector topleft = getExtents().getTopLeft();
	    
	    
	    
	    PVector p = vertices.get(0).copy();

	    for (int i = 0; i < numPoints; i++) {
	      p = vertices.get(i).copy();
	      
	      if(shiftToTopLeft) {
	    	  p = p.sub(topleft);
	      }

	      p = MOUtilGlobals.getTheDocumentCoordSystem().docSpaceToBufferSpace(p);
	      bufferSpaceVerts.add(p);
	    }
	    
	    return bufferSpaceVerts;
	  
  }
  
  public Path2D getAsPath2D(boolean convertToBufferSpace, boolean shiftToTopLeft) {
	  Path2D path = new Path2D.Float();
	    int numPoints = this.size();
	    
	    PVector topleft = getExtents().getTopLeft();
	    
	    
	    
	    PVector p = vertices.get(0).copy();
	    path.moveTo(p.x, p.y);
	    for (int i = 1; i < numPoints; i++) {
	      p = vertices.get(i).copy();
	      
	      if(shiftToTopLeft) {
	    	  p = p.sub(topleft);
	      }
	      
	      
	      if(convertToBufferSpace) {
	    	  p = MOUtilGlobals.getTheDocumentCoordSystem().docSpaceToBufferSpace(p);
	      }

	      path.lineTo(p.x, p.y);
	    }
	    if(isClosed()) path.closePath();
	    return path;
	  
  }
  
  
  
  
  public PVector getOrthogonallyDisplacedPoint(int n, float dist) {
	  // returns a point that is moved "orthogonally" to the lines connecting
	  PVector orthogVector = new PVector(0,1);
	  
	  if( (n >0 && n < size()-1) && size()>2) {
		  // the mid points
		 PVector vbefore =  getLine(n-1).getNormalisedVector();
		 PVector vafter =  getLine(n).getNormalisedVector();
		 orthogVector =  MOMaths.bisector(vbefore, vafter);
	  }
	  if(n == 0 ) {
		  // find the vector orthogonal to the start 
		  PVector lineVector = getLine(0).getNormalisedVector();
		  orthogVector = MOMaths.orthogonal(lineVector);
	  } 
	  if(n == size()-1) {
		  // find the vector orthogonal to the end line
		  PVector lineVector = getLine(n-1).getNormalisedVector();
		  orthogVector = MOMaths.orthogonal(lineVector);
	  }
	  PVector p = this.get(n);
	  PVector displacement = PVector.mult(orthogVector, dist);
	  return PVector.add(p, displacement);
  }
  
  
  public void doubleVertices(int octaves) {
	  if(octaves == 0) return;
	  for(int n = 0; n < octaves; n++) {
		  addHalwayVertices();
	  }
	  
	  
  }
  
  public void addHalwayVertices() {
	// effectively breaks every connecting line into two and then displaces each point. Keeps end points un-moved.
	  ArrayList<PVector> verticesOut = new ArrayList<PVector> ();
	  
	  int numLines = getNumLines();
	    
	  
	  PVector startPoint = this.getStartPoint();
	  verticesOut.add(startPoint);
	  
	  for (int i = 0; i < numLines; i++) {
		  // then get each line an add the split point and then end point
	      Line2 line = this.getLine(i);
	      PVector lineStart = line.p1;
	      PVector lineEnd = line.p2;
	      PVector midPoint = lineStart.lerp(lineEnd, 0.5f);

	      verticesOut.add(midPoint);
	      verticesOut.add(lineEnd);
	    }
	  
	  vertices = verticesOut;
	  
  }
  
  
}








