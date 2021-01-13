import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
//the algorithm keeps going until it fails to find a new packing point after a number of attempts.
//There is an option which packs according to the tone of an image
//There is an option which uses a list of pixels to pack from
//
//The radius is defined as a document space number (0...1), so a radius of 0.1 would give 10 points across the image
//



class PointGenerator_RadialPack3D extends PointGenerator_RadialPack2D {

	
	
	// a list of 3d points for the 3d packing algorithm
	ArrayList<PVector> points3d = new ArrayList<PVector>();
	SceneData3D sceneData;

	public PointGenerator_RadialPack3D(int rseed, SceneData3D sd) {
		super(rseed);
		sceneData = sd;
	}

	
	Seed getNextSeed() {
		Seed s = super.getNextSeed();
		if(sceneData==null) return s;
		s.depth = sceneData.getDepthNormalised(s.docPoint);
		
		return s;
	}

	
	boolean tryAddDistributedPoint(PVector thisPt, float radius) {
		// just tries to add 1 point, returns true if added, false if not added
		
		if (pointExistsWithinRadius3d(thisPt, radius))
				return false;
		points.add(thisPt);
		PVector thisPoint3d = sceneData.get3DPoint(thisPt);
		points3d.add(thisPoint3d);
		
		return true;

	}


	private boolean pointExistsWithinRadius3d(PVector p, float radius) {

		
		// If there are no points within the optimising rect, then returns true

		PVector p3d = sceneData.get3DPoint(p);

		float x1 = p3d.x - radius;
		float y1 = p3d.y - radius;
		float z1 = p3d.z - radius;
		float x2 = p3d.x + radius;
		float y2 = p3d.y + radius;
		float z2 = p3d.z + radius;

		AABox boxUnderConsideration = new AABox(x1, y1, z1, x2, y2, z2);

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

	
}// end of PointGenerator_RadialPack class

