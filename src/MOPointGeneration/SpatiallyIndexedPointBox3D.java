package MOPointGeneration;

import java.util.ArrayList;

import MOMaths.PVector;
import MOScene3D.AABox3D;

/**
 * SpatiallyIndexedPointBox3D extends AABox3D to provide a 3D bounding-box as part of a larger mesh of boxes used by the SpatiallyIndexedPointCollection class. To work
 * well, the box needs to return its own contained points, when requested, and also those of its immediate neighbours, as the nearest local point might be in a neighbouring box, not this one.
 * To further speed things up, this class caches the local points (this points + all neighbouring boxes points) if any new points have been added to this box, or neighbouring boxes, since the last
 * point request. This will be most effective is all the points are added at the start. 
 */
public class SpatiallyIndexedPointBox3D extends AABox3D{
	public ArrayList<PVector> points = new ArrayList<PVector>();
	public ArrayList<PVector> localPoints;
	int indexX, indexY, indexZ;
	
	boolean regatherLocalPoints = false;
	ArrayList<SpatiallyIndexedPointBox3D> neighboringBoxes = new ArrayList<SpatiallyIndexedPointBox3D>();

	/**
	 * SpatiallyIndexedPointBox3D extends AABox3D to provide a 3D bounding-box as part of a larger mesh of boxes used by the SpatiallyIndexedPointCollection class
	 * @param x1 - left-most in 3D units
	 * @param y1 - bottom-most in 3D units
	 * @param z1 - front-most in 3D units
	 * @param x2 - right-most in 3D units
	 * @param y2 - top-most in 3D units
	 * @param z2 - rear-most in 3D units
	 */
	public SpatiallyIndexedPointBox3D(float x1, float y1, float z1, float x2, float y2, float z2) {
		super(x1, y1, z1, x2, y2, z2);

	}

	/**
	 * Adds a point to this box's point list. It also informs the neighboring boxes that a point has been added.
	 * @param p
	 * @return - true if added. False if the point is outside the box for some reason
	 */
	public boolean addPoint(PVector p) {

		if( this.isPointInside(p)) {

			points.add(p);
			tellNeighborsToRegatherLocalPoints();
			return true;
		}

		return false;
	}
	
	/**
	 * @return the number of points in this box
	 */
	public int getNumPoints() {
		return points.size();
	}
	
	

	/**
	 * @return an array list of the points in this box
	 */
	public ArrayList<PVector> getPoints() {
		return points;
	}

	/**
	 * Called by owning SpatiallyIndexedPointCollection setNeighboringBoxes(..) method
	 * @param n
	 */
	public void addNeighbouringBox(SpatiallyIndexedPointBox3D n) {
		neighboringBoxes.add(n);
	}

	/**
	 * @return an Array List of PVector 3D points that are contained in this box, and its eight neighbours. If any of it's neighbours have had points added
	 * then all neighbours will have been set to re-gather local points. Otherwise return the cached local points.
	 */
	public ArrayList<PVector> getLocalPoints(){
		if(localPoints == null) return gatherLocalPoints();
		
		if(regatherLocalPoints) {
			this.regatherLocalPoints = false;
			return gatherLocalPoints();
		}
		return localPoints;
	}
	
	/**
	 * @return Array List of PVector 3D points that are contained in this box, and its eight neighbours, with no caching involved. i.e. long-hand
	 */
	private ArrayList<PVector> gatherLocalPoints(){
		
		// returns all the points in this box plus all the neighbouring points
		localPoints = new ArrayList<PVector>();
		localPoints.addAll( this.getPoints() );
		for(SpatiallyIndexedPointBox3D pb: neighboringBoxes) {
			if(pb == this) {
				continue;
			}
			localPoints.addAll( pb.getPoints() );
		}
		return localPoints;
		
		
	}
	
	
	/**
	 * informs the neighbouring boxes that a new point has been added to this box, therefore they need to re-cache local points
	 */
	void tellNeighborsToRegatherLocalPoints() {
		
		for(SpatiallyIndexedPointBox3D pb: neighboringBoxes) {
			pb.regatherLocalPoints = true;
		}
		
	}
	


}
