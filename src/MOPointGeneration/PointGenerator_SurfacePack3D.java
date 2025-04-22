package MOPointGeneration;

import java.util.ArrayList;

import MOMaths.AABox3D;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOScene3D.SceneData3D;
///////////////////////////////////////////////////////////////////////////////////////////////////////////
// The idea is to iterate in floating point 2D to generate 3D points on the surface of the scene
// The store them is a simple spatial box collection
//
// The points are added with a packing radius in 3D units. If any other point is within that radius, then 
// the point cannot be added, and proceed to the next point.
// Hence this is a monte-carlo type process
// To check any point's neighbours, the (possibly) 9 surrounding boxes' points are checked only. Therefore the maximum packing radius
// must be less than the width of a box, otherwise we need to be considering points beyond the 9 immediate neighbouring boxes
//
// Many boxes - that do not contain areas of the scene's surface -  will never contain points, but this does not matter, as new points will never index these boxes
//
// Iterating in 2D has the effect of there being many more "attempts" at adding points for near (visually larger) parts (or boxes) than further parts (or boxes).
// However the radius packing will come into play more visibly in these near areas. Distant areas may not receive so many visits, so will be unfairly under-packed. However, 
// visually this may be OK and may reduce the need to "thin out" distant parts, as has been practised.
//
// The placed 3D points are stored in the boxMesh structure, and can be extracted wholesale using getAll3DPoints()
// The successfully placed 2D points are added to a list points2d, with a depth added into the z component.
//
//
import MOUtils.GlobalSettings;
import MOUtils.Progress;
import MOUtils.SecondsTimer;
///////////////////////////////////////////////////////////////////////////////////////////
// The point surface packer will pack points on a surface using a radius-per-point. When attempting to place the next point, if any other previous point
// is already placed is within the packing radius, the new point cannot be placed (failed), so moves to the next one. Hence is 
// Monte-carlo method. The algorithm ends when the desired number of points have been placed, or the consecutive failed number of attempts has 
// exceeded a threshold (default is 300).
// This surface packing algorithm uses a "data cube" type of spatial indexing to speed up
// searching for neighbouring points.
//
// The point-placement radius can be made to respond to the brightness of an image, so can achieve clustering and thinning of points in
// response to an image pixel values. It can also be responsive to depth, to achieve "depth-thinning".
//
//
// The image sampling occurs on the SceneData's current render (using setCurrentRenderImage(name) )
// The radius response is set by adding in a set PackingInterpolationScheme
//
//
// It improves on the previous version by 1/ Using spatial indexing, 2/ Getting rid of the class hierarchy, which was too deep and cumbersome 
// e.g. this class does not need to be a collection Iterator; it just returns a list of points, either in world space, or as "depth enhanced" doc Space points..
//
public class PointGenerator_SurfacePack3D {
	
	Rect generationArea;
	
	AABox3D fullExtents;
	
	int inX,inY,inZ;
	SpatialIndexBox3D[][][] boxMesh;
	
	ArrayList<PVector> points2DWithDepth = new ArrayList<PVector>();
	SceneData3D sceneData;

	
	RandomStream ranStream;

	public int maxNumPoints = 2500000; // seems like a reasonable number!
	
	public float defaultPackingRadius = 0.2f;
	
	SecondsTimer timer;
	
	
	
	//////////////////////////////////////////////
	// radius response
	PackingInterpolationScheme packingInterpolationScheme;
	
	// These are set in normalised units
	float farDistanceMultiplier = 1;
	float nearDistanceThreshold = 0.5f;
	
	public PointGenerator_SurfacePack3D(SceneData3D sd, float maxRadius) {
		sceneData = sd;
		fullExtents = sceneData.depthBuffer3d.getExtents();
		int numBoxesX = (int) Math.ceil(fullExtents.getWidth()/maxRadius);
		int numBoxesY = (int) Math.ceil(fullExtents.getHeight()/maxRadius);
		int numBoxesZ = (int) Math.ceil(fullExtents.getDepth()/maxRadius);
		
		// in almost all cases the number will be 50,50,50
		numBoxesX = Math.min(numBoxesX, 50);
		numBoxesY = Math.min(numBoxesY, 50);
		numBoxesZ = Math.min(numBoxesZ, 50);
		
		//System.out.println("creating a boxMesh with " + numBoxesX + " " + numBoxesY + " " + numBoxesZ + " cells");
		init( 1,  sd,  numBoxesX,  numBoxesY,  numBoxesZ);
	}
	
	public PointGenerator_SurfacePack3D(SceneData3D sd) {
		init( 1,  sd,  50,  50,  50);
	}
	
	public void init(int rseed, SceneData3D sd, int numBoxesX, int numBoxesY, int numBoxesZ) {
		ranStream = new RandomStream(rseed);
		sceneData = sd;
		fullExtents = sceneData.depthBuffer3d.getExtents();
		inX = numBoxesX;
		inY = numBoxesY;
		inZ = numBoxesZ;
		boxMesh = new SpatialIndexBox3D[inX][inY][inZ];
		
		
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
					
					boxMesh[x][y][z] = new SpatialIndexBox3D(loX,loY,loZ,hiX,hiY,hiZ);
				}
			}
		}
		
		setAllNeighbours();
		
		generationArea = GlobalSettings.getTheDocumentCoordSystem().getDocumentRect();
		
	}
	
	
	
	
	
	public void setPackingImage(String name) {
		sceneData.setCurrentRenderImage(name);
		
	}
	
	public void setMaxNumPointsLimit(int n) {
		maxNumPoints = n;
	}
	
	public void setDefaultPackingRadius(float r) {
		defaultPackingRadius = r;
	}
	
	public void setPackingInterpolationScheme(PackingInterpolationScheme pis) {
		packingInterpolationScheme = pis;
	}
	
	public void setDepthSensitivePacking(float farMultiplier, float nearThreshold) {
		// set the farMultiplier > 1 to get thinning to happen in the distance
		// Thinning occurs between the nearThreshold (no thinning) and the far depth (full thinning)
		// when the distance is far (1), the value out == radiusIn*farMultimpler
		// when the distance is nearDistanceThreshold value out == radius

		farDistanceMultiplier = farMultiplier;
		nearDistanceThreshold = nearThreshold;
	}
	
	public void setGenerationArea(Rect r) {
		generationArea = r;
	}
	
	public void setRandomStreamSeed(int n) {
		ranStream = new RandomStream(n);
	}
	

	//////////////////////////////////////////////////////////////////////
	// This method is called when the user is ready to make the points. It used to take a long time
	// and now should be quicker, without losing integrity. 
	// It generates the points in both 2D and 3D. Not sure which is more useful.
	
	
	public ArrayList<PVector> generatePoints() {
		clearAllPoints();
		Progress.reset();
		timer = new SecondsTimer();
		timer.start();
		
		int numPlaced = 0;
		int bailAtNumTries = 300;
		int failedSequentialTries = 0;
		while(true) {
			boolean isPlaced = false;
			PVector p3 = null;
			PVector p2 = getRandomDocSpacePoint();
			float controlValue = sceneData.getCurrentRender01Value(p2);
			
			if( isExcluded(controlValue)) {
				isPlaced = false;
			} else {
			
				float r = getRadiusRequired(controlValue);
				p3 = sceneData.get3DSurfacePoint(p2);

				r = applyDepthThinning(r,  p2);
				
				isPlaced = tryAddPoint( p3,  r);
			}
			
			
			if(isPlaced == true) {
				// you have managed to place a 3D point!
				numPlaced++;
				failedSequentialTries = 0;
				p2.z = p3.z;
				points2DWithDepth.add(p2);

				Progress.print(points2DWithDepth.size(), this.maxNumPoints);

			} else {
				failedSequentialTries++;
				
			}
			
			if( failedSequentialTries >= bailAtNumTries) {
				System.out.println("New Point Generator bailed at " + failedSequentialTries + " successive failed attempts, having placed " + points2DWithDepth.size() + " points");
				break;
			}
			
			
			
			if(numPlaced >= this.maxNumPoints) {
				System.out.println("New Point Generator successfully placed " + this.maxNumPoints + " points");
				break;
			}
			
		}
		
		float s = timer.getElapsedTime();
		System.out.println("Time taken " + s + " seconds");
		System.out.println("Having used  " + inX + " " + inY + " " + inZ + " cells");
		System.out.println();
		return getDocSpacePoints(true);
	}
	
	
	
	

	
	
	public ArrayList<PVector> getDocSpacePoints(boolean includeDepth){
		// returns a deep copy
		ArrayList<PVector> copyOf2DPts = PVector.deepCopy(points2DWithDepth);
		if(includeDepth) {
			return copyOf2DPts;
		}
		
		for(PVector p: copyOf2DPts) {
			p.z = 0;
		}
		return copyOf2DPts;
		
	}
	
	public ArrayList<PVector> getWorldSpacePoints(){
		// returns a deep copy
		ArrayList<PVector> gethered3DPoints = new ArrayList<PVector>();
		
		for(int z = 0; z < inZ; z++) {
			for(int y = 0; y < inY; y++) {
				for(int x = 0; x < inX; x++) {
					gethered3DPoints.addAll(boxMesh[x][y][z].points);
				}
			}
		}
		
		return PVector.deepCopy(gethered3DPoints);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// private methods
	//
	//
	
	private boolean isExcluded(float v) {
		// returns false if the packing interpolation scheme determines that this
		// control value is excluded
		if( packingInterpolationScheme == null) return false;
		return packingInterpolationScheme.isExcluded(v);
		} 
	
	private float getRadiusRequired(float controlValue) {
		// this is where you do your image sampling at p and using the 
		// PackingInterpolationScheme return the radius required
		if( packingInterpolationScheme != null) {
			return packingInterpolationScheme.getRadius(controlValue);
		} 
		return defaultPackingRadius;
		
	}
	
	
	
	
	private float applyDepthThinning(float radiusIn, PVector p) {
		
		if(farDistanceMultiplier != 1) {
			float normDepth = sceneData.getNormalisedDepth(p);
			
			
			if(normDepth < nearDistanceThreshold) return radiusIn;
			// when the normDepth is 1 (far), the radius out == radiusIn*farMultimpler
			// when the normDepth is < nearDistanceThreshold, radiusOut == radiusIn
			return MOMaths.map(normDepth, nearDistanceThreshold, 1f, radiusIn, radiusIn*farDistanceMultiplier);
			
			
			//return getDepthSensitiveRadius(radiusIn, nd);
		}
		return radiusIn;
		
	}
	
	private void clearAllPoints() {
		points2DWithDepth.clear();
		for(int z = 0; z < inZ; z++) {
			for(int y = 0; y < inY; y++) {
				for(int x = 0; x < inX; x++) {
					boxMesh[x][y][z].points.clear(); 
				}
			}
		}
	}
	
	private void setAllNeighbours() {
		for(int z = 0; z < inZ; z++) {
			for(int y = 0; y < inY; y++) {
				for(int x = 0; x < inX; x++) {
					setNeighbours(x,y,z); 
				}
			}
		}
		
	}
	
	private void setNeighbours(int cx, int cy, int cz) {
		
		// sets the neighbouring boxs for th box ar cx,cy,cz
		SpatialIndexBox3D thisBox = boxMesh[cx][cy][cz];
		
		for(int z = cz-1; z <= cz+1; z++) {
			for(int y = cy-1; y <= cy+1; y++) {
				for(int x = cx-1; x <= cx+1; x++) {
					
					if(x==cx && y==cy && z==cz) continue; // don't want to add thisBox as neighbour
					if(x < 0 || y < 0 || z < 0) continue; // outside boxMesh
					if(x >= inX || y >= inY || z >= inZ) continue; // outside boxMesh
					
					thisBox.addNeighbour( boxMesh[x][y][z] );
				}
			}
		}

	}
	
	
	
	private boolean tryAddPoint(PVector p, float radiusRequired) {
		// this can be sped up by working out the index directly from the coordinate
		int[] index = getBoxIndexFromPoint(p);
		int x = index[0];
		int y = index[1];
		int z = index[2];
		// get the neighbouring points to this index
		ArrayList<PVector> neighboringPoint = boxMesh[x][y][z].getNeighboringPoints();
		
		// see if any are within the radiusRequired
		// if any other point is within the radiusRequired, return false
		if( isSpaceAvailable(p, radiusRequired, neighboringPoint) ) {
			// if OK then try to add....
			// The box checks to see if the point is within, so could potentially fail if indexing maths is wrong
			return boxMesh[x][y][z].tryAddPoint(p);
		}
		// no space available
		return false;
	}
	
	private int[] getBoxIndexFromPoint(PVector p) {
		
		if( fullExtents.isPointInside(p) ==  false) {
			// this happens very occasionally due to small errors in 3D calculations
			// System.out.println("point outside box " + p.toStr());
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
	
	
	
	
	
	private ArrayList<PVector> getNeighboringPoints(int cx, int cy, int cz){
		return boxMesh[cx][cy][cz].getNeighboringPoints();
	}
	
	
	private boolean isSpaceAvailable(PVector p, float radius, ArrayList<PVector> otherPoints) {
		float rsq = radius*radius;
		for(PVector otherPoint: otherPoints) {
			if(  p.distSq(otherPoint) < rsq ) return false;
		}
		return true;
	}
	
	private PVector getRandomDocSpacePoint() {
		
		PVector p = ranStream.randomPoint2(generationArea);
		
		int bailCount = 0;

		while (true) {
			if (sceneData.isSubstance(p)) return p;
			p = ranStream.randomPoint2(generationArea);
			bailCount++;
			if (bailCount > 1000000) {
				System.out.println("getRandomDocSpacePoint - cannot find any substance in the scene to add a point to - bailing with null");
				return null;
			}
		}

	}
	
	
}

class SpatialIndexBox3D extends AABox3D{
	public ArrayList<PVector> points = new ArrayList<PVector>();
	int indexX, indexY, indexZ;
	ArrayList<SpatialIndexBox3D> neighbors = new ArrayList<SpatialIndexBox3D>();
	
	public SpatialIndexBox3D(float x1, float y1, float z1, float x2, float y2, float z2) {
		super(x1, y1, z1, x2, y2, z2);
		
	}

	public boolean tryAddPoint(PVector p) {
		
		if( this.isPointInside(p)) {
			
			points.add(p);
			return true;
		}
		
		return false;
	}
	
	public ArrayList<PVector> getPoints() {
		return points;
	}
	
	public void addNeighbour(SpatialIndexBox3D n) {
		neighbors.add(n);
	}
	
	public ArrayList<PVector> getNeighboringPoints(){
		// returns all the points in this box plus all the neighbouring points
		ArrayList<PVector> collectedPoints = new ArrayList<PVector>();
		collectedPoints.addAll( this.getPoints() );
		for(SpatialIndexBox3D pb: neighbors) {
			if(pb == this) continue;
			collectedPoints.addAll( pb.getPoints() );
		}
		return collectedPoints;
	}
	
	
}



