package MOScene3D;

import MOMaths.MOMaths;
import MOMaths.PVector;

public class Line3D {

	PVector p1 = new PVector();
	PVector p2 = new PVector();

	public Line3D(PVector p1, PVector p2) {
		this.p1 = p1.copy();
		this.p2 = p2.copy();
	}

	public PVector lerp(float p) {
		return PVector.lerp(p1, p2, p);
	}

	public float normZ(float z) {
		return MOMaths.norm(z, p1.z, p2.z);
	}

	void moveBy(PVector d) {
		p1.add(d);
		p2.add(d);
	}

	PVector getDirectionVector() {
		return PVector.sub(p2, p1);
	}

	float length() {
		return p1.dist(p2);
	}
	
	public String toStr() {
		
		return "Line3D: p1: " + p1.toStr() + ", p2: " + p2.toStr();
	}

	PVector closestPointOnLine(PVector p) {
		// closest point on line AB to other point P
		// Calculate the dot product of the vector from A to P and the line direction
		// vector: dot((P - A), (B - A)).
		// Divide the dot product by the square of the magnitude of the line direction
		// vector: t = dot((P - A), (B - A)) / dot((B - A), (B - A))
		// To find the closest point (C) on the line to P, use the parameter "t" to
		// calculate a point on the line: C = A + t * (B - A)

		PVector BA = getDirectionVector();
		PVector PA = PVector.sub(p, p1);
		float dotPABA = PA.dot(BA);
		float t = dotPABA / BA.dot(BA);

		t = MOMaths.constrain(t, 0, 1);

		PVector c = p1.add(PVector.mult(BA, t));
		return c;
	}

	float distanceBetweenPointAndLine(PVector p) {
		PVector c = closestPointOnLine(p);
		return c.dist(p);
	}

	float shortestDistanceBetweenTwoLines(Line3D a, Line3D b) {
		Line3D lineBetween = shortestLineBetweenTwoLines(a, b);
		return lineBetween.length();
	}

	Line3D shortestLineBetweenTwoLines(Line3D a, Line3D b) {

		// Direction vectors of the lines
		PVector dirA = a.getDirectionVector();
		PVector dirB = b.getDirectionVector();

		PVector normalToBothLines = dirA.cross(dirB);


		float tA;
		float tB;


		float angleBetween = PVector.angleBetween(dirA, dirB);
		if ( MOMaths.nearZero(angleBetween)  ) {
			// they are parallel, set tA and tB to be 0.5 i.e. half way along the lines
			// btw the nearZero() method is in SimFunctions tab
			tA = 0.5f;
			tB = 0.5f;
		} else {
			// they are skew lines


			PVector aP1tobP1 = PVector.sub(b.p1, a.p1);
			PVector dirACrossN = dirA.cross(normalToBothLines);
			PVector dirBCrossN = dirB.cross(normalToBothLines);
			float dotNN = PVector.dot(normalToBothLines, normalToBothLines);
			tA = PVector.dot( dirACrossN, aP1tobP1 ) / dotNN;
			tB = PVector.dot( dirBCrossN, aP1tobP1 ) / dotNN;
		}

		// limit the T values to between 0..1, so the results are always on the line segments,
		// not the infinite line
		tA = MOMaths.constrain(tA, 0, 1);
		tB = MOMaths.constrain(tB, 0, 1);


		PVector pointOnB = b.lerp(tB);
		PVector pointOnA = a.lerp(tA);


		return new Line3D(pointOnA, pointOnB);
	}
	
}// end of class
