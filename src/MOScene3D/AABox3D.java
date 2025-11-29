package MOScene3D;

import MOMaths.MOMaths;
import MOMaths.PVector;

public class AABox3D{

	PVector minXYZ;
	PVector maxXYZ;

	public AABox3D(PVector p1, PVector p2) {
		init(p1,p2);
	}

	public AABox3D(float x1, float y1, float z1,float x2, float y2, float z2) {
		init( new PVector(x1,y1,z1), new PVector(x2,y2,z2));

	}

	void init(PVector p1, PVector p2) {
		float minX = Math.min(p1.x, p2.x);
		float minY = Math.min(p1.y, p2.y);
		float minZ = Math.min(p1.z, p2.z);

		float maxX = Math.max(p1.x, p2.x);
		float maxY = Math.max(p1.y, p2.y);
		float maxZ = Math.max(p1.z, p2.z);
		minXYZ = new PVector(minX,minY,minZ);
		maxXYZ = new PVector(maxX,maxY,maxZ);
		//System.out.println(" AABBox min xyz " + minXYZ + " maxXYZ " + maxXYZ);
	}

	public boolean isPointInside(PVector p) {
		if( MOMaths.isBetweenInc(p.x, minXYZ.x, maxXYZ.x) &&
			MOMaths.isBetweenInc(p.y, minXYZ.y, maxXYZ.y) &&
			MOMaths.isBetweenInc(p.z, minXYZ.z, maxXYZ.z) ) {
			return true;
		}
		return false;


	}

	public PVector norm(PVector p) {
		// returns the normalised coordinate of p within the box
		float nx = MOMaths.norm(p.x, getLeft(), getRight());
		float ny = MOMaths.norm(p.y, getBottom(), getTop());
		float nz = MOMaths.norm(p.z, getFront(), getBack());
		return new PVector(nx,ny,nz);
	}




	public PVector getMin() {
		return minXYZ.copy();
	}

	public PVector getMax() {
		return maxXYZ.copy();
	}

	public String toStr() {
		return "Min:" + minXYZ.toStr() + " Max:" + maxXYZ.toStr();
	}

	public float getWidth() {
		return maxXYZ.x-minXYZ.x;
	}

	public float getHeight() {
		return maxXYZ.y-minXYZ.y;
	}

	public float getDepth() {
		return maxXYZ.z-minXYZ.z;
	}

	public float getLeft() {
		// minX
		return minXYZ.x;
	}

	public float getRight() {
		// maxX
		return maxXYZ.x;
	}

	public float getTop() {
		// maxY
		return maxXYZ.y;
	}

	public float getBottom() {
		// minY
		return minXYZ.y;
	}

	public float getFront() {
		// minZ
		return minXYZ.z;
	}

	public float getBack() {
		// maxZ
		return maxXYZ.z;
	}

	public PVector constrain(PVector p) {


		float cx = MOMaths.constrain(p.x, getLeft(), getRight());
		float cy = MOMaths.constrain(p.y, getBottom(), getTop());
		float cz = MOMaths.constrain(p.x, getFront(), getBack());

		return new PVector(cx,cy,cz);


	}


}
