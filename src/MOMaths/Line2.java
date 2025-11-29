package MOMaths;

//////////////////////////////////////////////////////////////////////////////////
//Line2 is a line segment class
//Java compliant
public class Line2 {

	public static final int DIRECTION_UP=0;
	public static final int DIRECTION_LEFT=1;
	public static final int DIRECTION_DOWN=2;
	public static final int DIRECTION_RIGHT=3;

	// the end points
	public PVector p1;
	public PVector p2;

	// intersection with other line
	public boolean intersection_segmentsIntersect;
	public PVector intersection_point;
	float intersection_t1 = -1;
	float intersection_t2 = -1;

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

	public Line2 copy() {

		return new Line2(p1,p2);
	}

	public String toStr() {
		return p1.toStr() + "," + p2.toStr();
	}


	/**
	 * @param direction - this is DIRECTION_UP,DIRECTION_LEFT,DIRECTION_DOWN,DIRECTION_RIGHT and represents the general running direction
	 * p1->p2.
	 * It will swap the points in-place to meet the criteria
	 */
	public void enforceDirection(int direction) {
		if(direction==DIRECTION_UP) {
			if(p1.y < p2.y) {
				swapEndPoints();
				return;
			}
		}

		if(direction==DIRECTION_LEFT) {
			if(p1.x < p2.x) {
				swapEndPoints();
				return;
			}
		}

		if(direction==DIRECTION_DOWN) {
			if(p1.y > p2.y) {
				swapEndPoints();
				return;
			}
		}

		if(direction==DIRECTION_RIGHT) {
			if(p1.x > p2.x) {
				swapEndPoints();
				return;
			}
		}

	}



	public void swapEndPoints() {
		PVector tp1 = p1.copy();
		PVector tp2 = p2.copy();
		p1 = tp2;
		p2 = tp1;
	}

	float distancePointToLine(PVector p)
	{
		return (float)Math.sqrt( distancePointToLineSq(p) );
	}

	public float distancePointToLineSq(PVector p)
	{
		// for closest tests - avoids sqrt
		PVector npl =  nearestPointOnLine(p);
		float dx = npl.x-p.x;
		float dy = npl.y-p.y;
		return (dx*dx)+(dy*dy);
	}

	public PVector nearestPointOnLine(PVector p)
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

	public PVector getIntersectionPoint(Line2 l) {
		// returns null if no intersection, otherwise returns the point
		if( calculateIntersection(l) ) {
			return intersection_point;
		}
		return null;
	}

	public boolean calculateIntersection(Line2 l)
	{
		// returns true if this line segment intersects with other line2
		// To remove any ambiguity, lines that touch because they are "connected" at an end are dismissed as NOT intersecting
		// The intersection point can be recovered after the test via intersection_point
		// Even if the two segments do not intersect intersection_point will be set to the
		// intersection of the two infinite lines, unless they are parallel, in which case
		// intersection_point is set to null
		//
		//if(isConnected(l)) return false;


		intersection_segmentsIntersect = false;

		PVector p3 = l.p1.copy();
		PVector p4 = l.p2.copy();

		// Get the segments' parameters.
		float dx12 = p2.x - p1.x;
		float dy12 = p2.y - p1.y;
		float dx34 = p4.x - p3.x;
		float dy34 = p4.y - p3.y;

		intersection_t1 = -1;
		intersection_t2 = -1;

		// Solve for t1 and t2
		float denominator = (dy12 * dx34 - dx12 * dy34);

		// check if lines are parallel
		if (Math.abs(denominator) < 0.00000001)
		{
			// The lines are parallel (or close enough to it).
			intersection_segmentsIntersect = false;
			intersection_point = null;
			//System.out.println("parallel " + denominator);
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

	boolean isParallel(Line2 otherLine) {


		PVector p3 = otherLine.p1.copy();
		PVector p4 = otherLine.p2.copy();

		// Get the segments' parameters.
		float dx12 = p2.x - p1.x;
		float dy12 = p2.y - p1.y;
		float dx34 = p4.x - p3.x;
		float dy34 = p4.y - p3.y;

		float denominator = (dy12 * dx34 - dx12 * dy34);

		// check if lines are parallel
		if (Math.abs(denominator) < 0.000001)
		{
			return true;
		}

		return false;
	}

	boolean isJustTouching() {
		// you can call this after a successful intersection calculation with the other line to determine if the lines were
		// justTouching (returns true) or properly crossing (returns false)
		float tol = Float.MIN_VALUE * 2; // just to give the calculations some headroom
		if( MOMaths.isClose(intersection_t1, 0, tol) ||
			MOMaths.isClose(intersection_t1, 1, tol) ||
			MOMaths.isClose(intersection_t2, 0, tol) ||
			MOMaths.isClose(intersection_t2, 1, tol)) {
			return true;
		}
		return false;
	}

	public boolean isIntersectionPossible(Line2 otherLine) {
		// this is a trivial check to see if 2 lines can intersect by using their bounding boxes
		float thisMinX = Math.min(p1.x, p2.x);
		float thisMaxX = Math.max(p1.x, p2.x);
		float otherMinX = Math.min(otherLine.p1.x, otherLine.p2.x);
		float otherMaxX = Math.max(otherLine.p1.x, otherLine.p2.x);

		float thisMinY = Math.min(p1.y, p2.y);
		float thisMaxY = Math.max(p1.y, p2.y);
		float otherMinY = Math.min(otherLine.p1.y, otherLine.p2.y);
		float otherMaxY = Math.max(otherLine.p1.y, otherLine.p2.y);


	    if((otherMaxX < thisMinX) || // other is wholly left of this
	       (otherMinX > thisMaxX) || // other is wholly right of this
	       (otherMaxY < thisMinY) || // other is wholly above of this
	       (otherMinY > thisMaxY)    // other is wholly below of this
	       ) {
			return false;
		}

	       return true;
	}

	boolean equals(Line2 otherLine) {
		if ( p1.equals(otherLine.p1) && p2.equals(otherLine.p2)) {
			return true;
		}
		return false;
	}

	boolean isCoincident(Line2 otherLine) {
		// will return true for a line with same end points
		if ( equals(otherLine) || (  p1.equals(otherLine.p2) && p2.equals(otherLine.p1)  )  ) {
			return true;
		}
		return false;
	}

	public boolean isConnected(Line2 otherLine) {
		if( isCoincident(otherLine) ) {
			return true;
		}
		if( p1.equals(otherLine.p1) || p1.equals(otherLine.p2) || p2.equals(otherLine.p1) || p2.equals(otherLine.p2)) {
			return true;
		}
		return false;
	}

	PVector getOtherPoint(PVector thisPoint) {
		if (this.p1.equals(thisPoint)) {
			return this.p2;
		}
		if (this.p2.equals(thisPoint)) {
			return this.p1;
		}
		return null;
	}

	public PVector interpolate(float a) {
		float x = MOMaths.lerp(a, p1.x, p2.x);
		float y = MOMaths.lerp(a, p1.y, p2.y);
		return new PVector(x, y);
	}


	public PVector getNormalisedVector() {
		return PVector.sub(p2, p1).normalize();
	}



	public float getLength() {
		return p1.dist(p2);
	}

	public float getRotation() {
		// returns the rotation in degrees clockwise, 0 being straight up.(-y)
		// a line (north) straight up, from p1(0,0) to p2(0,-1)
		// a line east (right) from p1(0,0) to p2(1,0)
		// A line north has a rotation of 0
		// A line east has a rotation of 90
		PVector v =  getAsPVector();
		float degrees = (float) (Math.atan2(v.y,v.x)*180/Math.PI) + 90;
		return degrees;
	}





	public float getAngleBetween(Line2 otherLine) {
		// returns the radians between the two lines.
		// returns a lower number for a "straighter" pair of lines. The lines do not need to join
		// but are regarded as vectors in 2D space
		PVector vThis = getAsPVector();
		PVector vOther = otherLine.getAsPVector();
		return PVector.angleBetween(vThis, vOther);
	}


	public float getHingedAngleBetween(Line2 otherLine) {
		// if the two vectors are "hinged" around the end point of this line, and the start point of the other line
		// the the angle returned (in radians) is the angle between the two lines, where a n acute angle is a low number < PI and
		// an obtuse angle is a high number > PI. Two lines forming one straight line would be == PI.
		// Two lines -- would be 180 degrees, two lines _| would be 90, and two lines /| would be 45

		if( !p2.equals(otherLine.p1) ) {
			System.out.println("Line2:: hingedAngleBetween lines are not joined at this.p2-other.p1");
			return 0;
		}
		PVector p1 = this.p1;
		PVector join = this.p2;
		PVector p2 = otherLine.p2;

		return MOMaths.getHingedAngleBetween(p1,join, p2);

	}


	public PVector getAsPVector() {
		return PVector.sub(p2, p1);
	}
}// end Line2 class
