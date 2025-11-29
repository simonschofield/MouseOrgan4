package MOScene3D;

import MOMaths.PVector;

public class Fustrum3D {
	public PVector nearTopLeft, nearTopRight, nearBottomRight, nearBottomLeft;
	public PVector farTopLeft, farTopRight, farBottomRight, farBottomLeft;


	public PVector getViaIndex(int n) {
		// gets the points via indexing
		// in order of clockwise direction near TL, TR, BR, BL, far TL, TR, BR, BL
		switch(n) {
			case 0: return nearTopLeft;
			case 1: return nearTopRight;
			case 2: return nearBottomRight;
			case 3: return nearBottomLeft;

			case 4: return farTopLeft;
			case 5: return farTopRight;
			case 6: return farBottomRight;
			case 7: return farBottomLeft;
		}
		return new PVector(0,0,0);
	}

}
