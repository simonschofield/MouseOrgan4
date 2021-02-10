import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
////////////////////////////////////////////////////////////////////////////
//This class will return a list of random points packed to a specified radius
//the algorithm keeps going until it fails to find a new packing point after a number of attempts.
//There is an option which packs according to the tone of an image
//There is an option which uses a list of pixels to pack from
//
//The radius is defined as a document space number (0...1), so a radius of 0.1 would give 10 points across the image
// It can be set with a fixed radius, or a range of radii set in response to a distributionImage
// if it is a fixed radius, then radiusLow carries the setting
class PointGenerator_RadialPack2D extends PointGenerator {

	// specific data to help point generation
	float radiusLow = 0.001f;
    float radiusHigh= 0.01f;
    float lowDistributionThreshold = 0;
	BufferedImage distributionImage;
	CoordinateSpaceConverter distributionImageCoordinateSpaceConverter;
	
	
	// this is the number of attempts to try and find a point available for adding before the packing
	// gives up
	int attemptsCounter = 300;

	public PointGenerator_RadialPack2D(int rseed) {
		super(rseed);
	}
	

	/////////////////////////////////////////////////////////////////////////////////
	// Evenly distributed points with a spacing of radius
	// (as a proportion of 1, so 0.001 == 1/1000th of the long edge)
	//
	void setPackingRadius(float r) {
		radiusLow = r;
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// Evenly distributed points with a spacing radius dependednt on the value of a bitmap
	// rLo maps to black in the bit map, rHi maps to white in the bitmap
	//
	void setPackingRadius(float rLo, float rHi, BufferedImage distImg) {
		radiusLow = rLo;
		radiusHigh = rHi;
		distributionImage = distImg;
	}
	
	void setPackingRadius(float rLo, float rHi, float lowLimit, BufferedImage distImg) {
		radiusLow = rLo;
		radiusHigh = rHi;
		lowDistributionThreshold = lowLimit;
		distributionImage = distImg;
	}
	
	ArrayList<PVector> generatePoints() {
		if(distributionImage == null) return generateDistributedPoints(radiusLow);
		return generateDistributedPoints(radiusLow, radiusHigh);
	}
		
		
	ArrayList<PVector> generateDistributedPoints(float radius){	
		int attempts = 0;
		while (true) {
			PVector thisPt = getRandomDocSpacePoint();
			boolean success = tryAddDistributedPoint(thisPt, radius);
			if (!success) {
				attempts++;
			} else {
				attempts = 0;
			}
			if (attempts > attemptsCounter) {
				System.out.println("SeedPacking: placed " +  getNumItems());
				break;
			}
		}

		return points;
	}

	

	
	ArrayList<PVector> generateDistributedPoints(float rLo, float rHi) {
		int previousBiggestNumberOfAttempts = 0;
		
		
		distributionImageCoordinateSpaceConverter = new CoordinateSpaceConverter(distributionImage.getWidth(), distributionImage.getHeight(),  aspect);
		
		int attempts = 0;
		while (true) {
			PVector thisPt = getRandomDocSpacePoint();
			
			
			// check against the bitmap lowDistributionThreshold
			boolean success;
			if( getBitmapValue01(thisPt) < lowDistributionThreshold ) {
				success = false;
			} else {
				// it's passed the low threshold, so claculate a point
				float radius = lerpRadiusOnImage(thisPt, rLo, rHi );
				success = tryAddDistributedPoint(thisPt, radius);
			}
			
			
			if (!success) {
				attempts++;
			} else {
				
				if(attempts > previousBiggestNumberOfAttempts) {
					previousBiggestNumberOfAttempts = attempts;
					System.out.println("SeedPacking:attempts used  " + previousBiggestNumberOfAttempts + " out of a maximum of " + attemptsCounter + " placed " +  getNumItems());
				}
				attempts = 0;
			}
			if (attempts > attemptsCounter) {
				System.out.println("exceeded max number of attempts: total placed " + getNumItems());
				break;
			}
		}
		
		
		return points;

	}

	boolean tryAddDistributedPoint(PVector thisPt, float radius) {
		// just tries to add 1 point, returns true if added, false if not added
		if (pointExistsWithinRadius(thisPt, radius))
				return false;
		points.add(thisPt);
		return true;

	}

	private boolean pointExistsWithinRadius(PVector p, float radius) {

		// returns the index of the nearest point in the current point list
		// if optimise == true, the uses the optimiseSearchRect
		// If there are no points whithin the optimising rect, then returns false

		float x1 = MOMaths.constrain(p.x - radius, 0, 1);
		float y1 = MOMaths.constrain(p.y - radius, 0, 1);
		float x2 = MOMaths.constrain(p.x + radius, 0, 1);
		float y2 = MOMaths.constrain(p.y + radius, 0, 1);

		Rect rectUnderConsideration = new Rect(x1, y1, x2, y2);

		for (int n = 0; n < points.size(); n++) {
			PVector thisPoint = points.get(n);
			if (rectUnderConsideration.isPointInside(thisPoint)) {
				float dist = p.distXY(thisPoint);
				if (dist < radius) {
					return true;
				}
			}

		}

		return false;

	}

	
	
	private float lerpRadiusOnImage(PVector docPt, float radiusAtLowImageValue, float radiusAtHighImageValue) {
		
		float t = getBitmapValue01( docPt);
		
		// so what you want is the densest distribution at white, the thinnest distribution at the cuttoff
		t = MOMaths.lerp(t,lowDistributionThreshold,1.0f);
		float thisSurfaceArea = MOMaths.lerp(t, radiusAtLowImageValue, radiusAtHighImageValue);
		//float r = (float) Math.sqrt(thisSurfaceArea/Math.PI);
		return thisSurfaceArea;
	}
	
	float getBitmapValue01(PVector docPt) {
		PVector imgPixelLoc = distributionImageCoordinateSpaceConverter.docSpaceToImageCoord(docPt);
		return ImageProcessing.getValue01(distributionImage, (int) imgPixelLoc.x, (int) imgPixelLoc.y);
	}

	
}// end of PointGenerator_RadialPack class

////////////////////////////////////////////////////////////////////////////
//This class will return a list of random points packed to a specified radius in 3D
//on the surface of the 3D in SceneData
//the algorithm keeps going until it fails to find a new packing point after a number of attempts.
//There is an option which packs according to the tone of an image
//There is an option which uses a list of pixels to pack from
//
//The radius is defined as a document space number (0...1), so a radius of 0.1 would give 10 points across the image
//



class PointGenerator_RadialPackSurface3D extends PointGenerator_RadialPack2D {


	
	
	// a list of 3d points for the 3d packing algorithm
	ArrayList<PVector> points3d = new ArrayList<PVector>();
	SceneData3D sceneData;

	
	
	public PointGenerator_RadialPackSurface3D(int rseed, SceneData3D sd) {
		super(rseed);
		sceneData = sd;
	}

	
	boolean tryAddDistributedPoint(PVector docSpcPt, float radius) {
		// just tries to add 1 point, returns true if added, false if not added
		
		PVector thisPoint3d = sceneData.get3DSurfacePoint(docSpcPt);;
		
		// this is where you would invent a depth for volume distribution
		// using sceneData.get3DVolumePoint(docSpcPt, invented depth);
		float normDepth = sceneData.getDepthNormalised(docSpcPt);
		
		
		if (pointExistsWithinRadius3d(thisPoint3d, radius))
				return false;
		
		// if suitably far from any other point, add both the 2d docspace point
		// and the 3d point
		PVector depthEnhancedDocSpacePt = docSpcPt.copy();
		//depthEnhancedDocSpacePt.z = normDepth;
		points.add(depthEnhancedDocSpacePt);
		points3d.add(thisPoint3d);
		
		return true;

	}


	private boolean pointExistsWithinRadius3d(PVector p3d, float radius) {

		
		// If there are no points within the optimising rect, then returns true
		float x1 = p3d.x - radius;
		float y1 = p3d.y - radius;
		float z1 = p3d.z - radius;
		float x2 = p3d.x + radius;
		float y2 = p3d.y + radius;
		float z2 = p3d.z + radius;

		AABox3D boxUnderConsideration = new AABox3D(x1, y1, z1, x2, y2, z2);

		for (int n = 0; n < points.size(); n++) {
			PVector thisPoint3d = points3d.get(n);
			if (boxUnderConsideration.isPointInside(thisPoint3d)) {
				float dist = p3d.dist(thisPoint3d);
				if (dist < radius) {
					return true;
				}
			}

		}

		return false;

	}
	
	
	
	
}// end of PointGenerator_RadialPackSurface3D class



////////////////////////////////////////////////////////////////////////////
//
//
class PointGenerator_Volume3D extends CollectionIterator {

	RandomStream randomStream;
		
	// a list of 3d points for the 3d packing algorithm
	EyeSpaceVolume3D eyeSpaceVolume;
	AABox3D aaBox;
	
	ArrayList<PVector> points3d = new ArrayList<PVector>();
	ArrayList<PVector> points2d = new ArrayList<PVector>();
	float nearDepth = 0;
	float farDepth = 1;

	public PointGenerator_Volume3D(int rseed, float vfov) {
		randomStream = new RandomStream(rseed);
		eyeSpaceVolume = new EyeSpaceVolume3D(GlobalObjects.theDocument.getDocumentAspect(),vfov);
	}

	void generateRandomPoints(int numPoints3D, float nearZ, float farZ) {
		nearDepth = nearZ;
		farDepth = farZ;
		aaBox = eyeSpaceVolume.getBoundingBox( nearDepth,  farDepth);
		PVector minXYZ = aaBox.getMin();
		PVector maxXYZ = aaBox.getMax();
		for(int n = 0; n < numPoints3D; n++) {
			float rx = randomStream.randRangeF(minXYZ.x, maxXYZ.x);
			float ry = randomStream.randRangeF(minXYZ.y, maxXYZ.y);
			float rz = randomStream.randRangeF(minXYZ.z, maxXYZ.z);
			
			
			PVector thisCandidatePoint = new PVector(rx,ry,rz);
			points3d.add(thisCandidatePoint);
			if(eyeSpaceVolume.isPoint3DInView(thisCandidatePoint)) {
				
				PVector docSpcPt = eyeSpaceVolume.getDocSpacePoint(thisCandidatePoint);
				points2d.add(docSpcPt);
			}
		}
		System.out.println(" generateRandomPoints made " + points2d.size() + " out of a possible " + numPoints3D);
		depthSort();
	}
	
	// In case you need to sort the depth of the points on the z component of the point
	// More used by subclasses
	void depthSort() {
		points2d.sort(Comparator.comparing(PVector::getZ).reversed());
	}
		
	
	

	
	ArrayList<PVector> getDocSpacePoints(){
		return points2d;
	}

	@Override
	int getNumItems() {
		// TODO Auto-generated method stub
		return points2d.size();
	}

	@Override
	Object getItem(int n) {
		// TODO Auto-generated method stub
		return points2d.get(n);
	}
	
	
	PVector getNextPoint() {
		return (PVector) super.getNextItem();
	}
	
	
	void drawPoints(Color c) {
		GlobalObjects.theDocument.drawPoints(points2d, c);
	}
	
	void drawPoints(Color nearCol, Color farCol) {
		for(PVector p : points2d) {
			
			float n = MOMaths.norm(p.z, nearDepth, farDepth);
			
			//System.out.println("in draw points near depth " + nearDepth + " far " + farDepth + " p "+ p + " n "+ n + " " );
			Color thisCol = ImageProcessing.blendColor(nearCol, farCol, n);
			GlobalObjects.theDocument.drawPoint(p, thisCol, 10);
		}
	}
	

}// end of PointGenerator_RadialPackVolume3D class

