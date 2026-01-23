package MOPointGeneration;

import java.util.ArrayList;

import MOMaths.MOMaths;
import MOMaths.PVector;
import MOScene3D.AABox3D;



/**
 * Creates a spatially indexed collection of points. You populate the collection using addPoint() or tryAddPointWithPackingRadius(). <p> 
 * Finding nearest points and packing new points within the collection is sped up enormously using this approach.
 */
public class SpatiallyIndexedPointCollection {
	
	AABox3D fullExtents;
	int inX,inY,inZ;
	SpatiallyIndexedPointBox3D[][][] boxMesh;
	
	/**
	 * Creates a spatially indexed collection of points. You populate the collection using addPoint() or tryAddPointWithPackingRadius(). <p> 
	 * Finding nearest points and packing new points within the collection is sped up enormously using this approach.
	 * @param extents - The 3D extents of the whole collection of points as a AABox3D. This is necessary to know up-front in order to spatially index them as they are added.
	 * @param boxDimension - The space defined by the extents is split up into a mesh of cubes (boxes), each box having the size boxDimension  x boxDimension x boxDimension. 
	 * The full grid of boxes is created to slightly exceed the extents, thereby guaranteeing that any point legitimately 
	 * added will be placed within a box. The maximum number of boxes in any one dimension is 50.  
	 */
	public SpatiallyIndexedPointCollection(AABox3D extents, float boxDimension){
		fullExtents = extents;
		int numBoxesX = (int) Math.ceil(fullExtents.getWidth()/boxDimension);
		int numBoxesY = (int) Math.ceil(fullExtents.getHeight()/boxDimension);
		int numBoxesZ = (int) Math.ceil(fullExtents.getDepth()/boxDimension);
	
		// This just stops a too=-high number of boxes occurring, so the max number of boxes is 50 in each dimension
		// If you need more than this you need an r-tree!
		inX = Math.min(numBoxesX, 50);
		inY = Math.min(numBoxesY, 50);
		inZ = Math.min(numBoxesZ, 50);
		
		
		boxMesh = new SpatiallyIndexedPointBox3D[inX][inY][inZ];


		float dx = fullExtents.getWidth()/inX;
		float dy = fullExtents.getHeight()/inY;
		float dz = fullExtents.getDepth()/inZ;

		float extentsLeft = fullExtents.getLeft();
		float extentsBottom = fullExtents.getBottom();
		float extentsFront = fullExtents.getFront();

		for(int z = 0; z < inZ; z++) {
			for(int y = 0; y < inY; y++) {
				for(int x = 0; x < inX; x++) {

					float loX = extentsLeft + (x * dx); // for 10 boxes in x, width of 0.1f, and a loWidth of 5. when x == 0, the lower value will be 5, the upper will be 5.1, when x == 9, the lower value will be 5.9
					float loY = extentsBottom + (y * dy);
					float loZ = extentsFront + (z * dz);

					float hiX = loX + dx;
					float hiY = loY + dy;
					float hiZ = loZ + dz;

					boxMesh[x][y][z] = new SpatiallyIndexedPointBox3D(loX,loY,loZ,hiX,hiY,hiZ);
				}
			}
		}

		setAllNeighbouringBoxes();
	}
	
	/**
	 * @return -the full extents of the spatial index box grid
	 */
	public AABox3D getExtents() {
		return fullExtents.copy();
	}
	
	/**
	 * @return - the number of spatial index boxes in x,y and z as an array of 3 integers
	 */
	public int[] getBoxGridArrayDimensions() {
		return new int[] {inX,inY,inZ};
	}
	
	/**
	 * Gets the specific spatial index box within the box grid
	 * @param x - index in x
	 * @param y - index in y
	 * @param z - index in z
	 * @return - The SpatiallyIndexedPointBox3D at x,y,z, or null if an invalid index
	 */
	public SpatiallyIndexedPointBox3D getBox(int x, int y, int z) {
		if( isValidBoxIndex( x,  y,  z) ) {
			return boxMesh[x][y][z];
		}
		return null;
	}
	
	/**
	 * Gets the extents of a specific spatial index box within the box grid
	 * @param x - index in x
	 * @param y - index in y
	 * @param z - index in z
	 * @return - The extents as an AABox3D, or null if an invalid index
	 */
	public AABox3D getBoxExtents(int x, int y, int z) {
		if( isValidBoxIndex( x,  y,  z) ) {
			return boxMesh[x][y][z].copy();
			
		}
		return null;
		
	}
	
	/**
	 * gets the points contained within a specific spatial index box within the box grid
	 * @param x - index in x
	 * @param y - index in y
	 * @param z - index in z
	 * @return -  the points contained within box{x][y][z], or null if an invalid index
	 */
	public ArrayList<PVector> getBoxPoints(int x, int y, int z) {
		if( isValidBoxIndex( x,  y,  z) ) {
			return boxMesh[x][y][z].points;
			
		}
		return null;
		
	}
	
	/**
	 * gets the number of points contained within a specific spatial index box within the box grid
	 * @param x - index in x
	 * @param y - index in y
	 * @param z - index in z
	 * @return -  Int: the number of points contained within box{x][y][z], or 0 if an invalid index
	 */
	public int getBoxNumPoints(int x, int y, int z) {
		if( isValidBoxIndex( x,  y,  z) ) {
			return boxMesh[x][y][z].points.size();
			
		}
		return 0;
		
	}
	
	
	/**
	 * @param x - index in x
	 * @param y - index in y
	 * @param z - index in z
	 * @return - Boolean. True if is a valid index, false if not
	 */
	public boolean isValidBoxIndex(int x, int y, int z) {
		if(x < 0  || x >= inX || y < 0 || y >= inY || z < 0 || z >= inZ) {
			System.out.println("SpatiallyIndexedPointCollection - isValidBoxIndex. invalid box index of " + x + "," + y + "," + z + ", extents are " + inX + "," + inY + "," + inZ);
			return false;
		}
		return true;
		
	}
	
	/**
	 * @param p - the point to be added
	 * @return - boolean, True if within the extents of the collection, False if outside the collection extents
	 */
	public boolean addPoint(PVector p) {
		// this can be sped up by working out the index directly from the coordinate
		int[] index = getBoxIndexFromPoint(p);
		int x = index[0];
		int y = index[1];
		int z = index[2];
		// will return true if the indexing is OK
		return boxMesh[x][y][z].addPoint(p);
	}
	
	/**
	 * Add a point to the collection IF there is no other already existing point within the packingRadius
	 * @param p - the point to be added if packing allows
	 * @param packingRadius - the 3D measure of the free area required to place a point
	 * @return - boolean, True if the point has been successfully placed, false if rejected on packing grounds, false if outside the collection extents
	 */
	public boolean tryAddPointWithPackingRadius(PVector p, float packingRadius) {
		// this can be sped up by working out the index directly from the coordinate
		int[] index = getBoxIndexFromPoint(p);
		int x = index[0];
		int y = index[1];
		int z = index[2];
		// get the neighbouring points to this index
		ArrayList<PVector> neighboringPoint = boxMesh[x][y][z].getLocalPoints();

		// see if any are within the radiusRequired
		// if any other point is within the radiusRequired, return false
		if( isSpaceAvailable(p, packingRadius, neighboringPoint) ) {
			// if OK then try to add....
			// The box checks to see if the point is within, so could potentially fail if indexing maths is wrong
			return boxMesh[x][y][z].addPoint(p);
		}
		// no space available
		return false;
	}
	
	/**
	 * Deletes all the points in the collection
	 */
	public void clearAllPoints() {
		for(int z = 0; z < inZ; z++) {
			for(int y = 0; y < inY; y++) {
				for(int x = 0; x < inX; x++) {
					boxMesh[x][y][z].points.clear();
				}
			}
		}
	}
	
	
	
	/**
	 * Returns an array list of all the points in the locality (target box plus it's 8 neighbours), to point p
	 * @param p - any 3D point. If p is outside the extents of the point collection, it is constrained to be within.
	 * @return - an array list of points. I.e all the points in the locality (target box plus it's 8 neighbours)
	 */
	public ArrayList<PVector> getLocalPoints(PVector p){
		int[] index = getBoxIndexFromPoint(p);
		
		// get the neighbouring points to this index
		return getLocalPoints(index[0],index[1],index[2]);
	}
	
	
	/**
	 * Returns an array list of all the points in the locality of the target box xi,yi,zi (plus it's 8 neighbours), to point p
	 * @param xi - the box index in x
	 * @param yi - the box index in y
	 * @param zi - the box index in z
	 * @return  - an array list of points. I.e all the points in the locality (target box plus it's 8 neighbours, or less on edges)
	 */
	public ArrayList<PVector> getLocalPoints(int xi, int yi, int zi){
		
		// get the neighbouring points to this index
		if( isValidBoxIndex(xi,yi,zi)) {
		return boxMesh[xi][yi][zi].getLocalPoints();
		}
		return null;
	}
	
	/**
	 * Returns the nearest point in the locality (target box plus it's 8 neighbours). 
	 * @param p - any 3D point. If p is outside the extents of the point collection, it is constrained to be within.
	 * @return - will return the nearest point in the collection to p
	 */
	public PVector getNearestLocalPoint(PVector p) {
		ArrayList<PVector> localPoints = getLocalPoints(p);
		PVector nearestPoint = null;
		float smallestDistance = Float.MAX_VALUE;
		for(PVector thisPoint: localPoints) {
			float thisDist = p.distSq(thisPoint);
			if( thisDist < smallestDistance) {
				nearestPoint = thisPoint;
				smallestDistance = thisDist;
			}
		}
		return nearestPoint.copy();
	}
	
	/**
	 * @return - all the 3D points within the collection as an array list
	 */
	public ArrayList<PVector> getWorldSpacePoints(){
		// returns a deep copy
		ArrayList<PVector> gethered3DPoints = new ArrayList<>();

		for(int z = 0; z < inZ; z++) {
			for(int y = 0; y < inY; y++) {
				for(int x = 0; x < inX; x++) {
					gethered3DPoints.addAll(boxMesh[x][y][z].points);
				}
			}
		}

		return PVector.deepCopy(gethered3DPoints);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// private below here
	//
	//
	private boolean isSpaceAvailable(PVector p, float radius, ArrayList<PVector> otherPoints) {
		float rsq = radius*radius;
		for(PVector otherPoint: otherPoints) {
			if(  p.distSq(otherPoint) < rsq ) {
				return false;
			}
		}
		return true;
	}
	
	private void setAllNeighbouringBoxes() {
		for(int z = 0; z < inZ; z++) {
			for(int y = 0; y < inY; y++) {
				for(int x = 0; x < inX; x++) {
					setNeighbouringBoxes(x,y,z);
				}
			}
		}

	}

	private void setNeighbouringBoxes(int cx, int cy, int cz) {

		// sets the neighbouring boxs for th box ar cx,cy,cz
		SpatiallyIndexedPointBox3D thisBox = boxMesh[cx][cy][cz];

		for(int z = cz-1; z <= cz+1; z++) {
			for(int y = cy-1; y <= cy+1; y++) {
				for(int x = cx-1; x <= cx+1; x++) {

					if(x==cx && y==cy && z==cz)
					 {
						continue; // don't want to add thisBox as neighbour
					}
					if(x < 0 || y < 0 || z < 0)
					 {
						continue; // outside boxMesh
					}
					if(x >= inX || y >= inY || z >= inZ)
					 {
						continue; // outside boxMesh
					}

					thisBox.addNeighbouringBox( boxMesh[x][y][z] );
				}
			}
		}

	}
	
	private int[] getBoxIndexFromPoint(PVector p) {

		if( !fullExtents.isPointInside(p)) {
			// this happens very occasionally due to small errors in 3D calculations
			//System.out.println("SpatiallyIndexedPointCollection.getBoxIndexFromPoint::point " + p.toStr() + " outside box " + fullExtents.toStr());
			p = fullExtents.constrain(p);
		}


		PVector pnorm = fullExtents.norm(p);
		int x = (int) (pnorm.x * inX); // if there where 10 boxes in X, then the index required is 0...9.  A p.x of 0.99 would correctly return  9
		int y = (int) (pnorm.y * inY);
		int z = (int) (pnorm.z * inZ);

		// probably need to clamp them
		x = MOMaths.constrain(x,0,inX-1);
		y = MOMaths.constrain(y,0,inY-1);
		z = MOMaths.constrain(z,0,inZ-1);

		return new int[] {x,y,z};

	}
	
	
	
	
}// end class




