import java.awt.image.BufferedImage;
import java.util.ArrayList;




public class PointGenerator extends CollectionIterator{
	RandomStream randomStream;
	float aspect;
	Rect generationAreaRect;
	// the mask image is set to black/white specifiying where points may be
	// generated
	BufferedImage maskImage;
	CoordinateSpaceConverter maskImageCoordinateSpaceConverter;

	// the list of points eventiallu calculated
	ArrayList<PVector> points = new ArrayList<PVector>();

	// so this class isn't entirely abstract
	int numberOfPointsRequest = 1000;
	
	public PointGenerator(float documentAspect) {
		aspect = documentAspect;
		randomStream = new RandomStream();
		setGenerationArea(aspect);
	}

	public PointGenerator(int rseed, float documentAspect) {
		aspect = documentAspect;
		randomStream = new RandomStream(rseed);
		setGenerationArea(aspect);
	}
	
	void setGenerationArea(float aspect) {
		generationAreaRect = new Rect();
		if(aspect > 1) {
			setGenerationArea(0f, 0f, 1f, 1f/aspect);
			
		}else {
			setGenerationArea(0f, 0f, aspect , 1f);
		}
		
		
	}
	
	void setGenerationArea(Float left, Float top, Float right, Float bottom) {
		if(left != null ) generationAreaRect.left = left;
		if(top != null ) generationAreaRect.top = top;
		if(right != null ) generationAreaRect.right = right;
		if(bottom != null ) generationAreaRect.bottom = bottom;
	}

	void setRandomSeed(int n) {
		randomStream = new RandomStream(n);
	}
	
	void setNumberOfPointsRequest(int n) {
		numberOfPointsRequest = n;
	}
	
	ArrayList<PVector> generatePoints(){
		return generateRandomPoints(numberOfPointsRequest);
	}
	
	ArrayList<PVector> generateRandomPoints(int num){
		for(int n = 0; n < num; n++) {
			PVector p = getRandomDocSpacePoint();
			points.add(p);
		}
	return points;	
	}

	ArrayList<PVector> getPoints() {
		return points;
	}

	PVector getPoint(int n) {
		return points.get(n);
	}

	void clearPoints() {
		points.clear();
	}

	void setMaskImage(BufferedImage mask) {

		maskImage = mask;
		maskImageCoordinateSpaceConverter =  new CoordinateSpaceConverter(maskImage.getWidth(), maskImage.getHeight(), aspect);
		//System.out.println("setting mask image" + mask);
	}
	
	

	
	
	
	PVector getRandomDocSpacePoint() {
		PVector p = randomStream.randomPoint2(generationAreaRect);
		
		if (maskImage == null)
			return p;
		int bailCount = 0;

		while (true) {
			if (permittedByMaskImage(p))
				return p;
			p = randomStream.randomPoint2(generationAreaRect);
			bailCount++;
			if (bailCount > 1000000)
				return null;
		}

	}

	boolean permittedByMaskImage(PVector p) {
		if(maskImage==null) return true;
		
		PVector imgPixelLoc = maskImageCoordinateSpaceConverter.docSpaceToImageCoord(p);
		int c = maskImage.getRGB((int) imgPixelLoc.x, (int) imgPixelLoc.y);
		boolean hasAlpha = ImageProcessing.hasAlpha(maskImage);
		float t = ImageProcessing.packedIntToVal01(c, hasAlpha);

		if (t < 0.5f) return false;
		
		return true;
	}

	@Override
	int getNumItems() {
		// TODO Auto-generated method stub
		return points.size();
	}

	@Override
	PVector getItem(int n) {
		// TODO Auto-generated method stub
		return points.get(n);
	}
	
	
	PVector getNextItem() {
		return (PVector)(super.getNextItem());
	}
	
	
	Seed getNextSeed() {
		PVector p = getNextItem();
		return new Seed(p);
		
	}

}// end of PointGenerator class

////////////////////////////////////////////////////////////////////////////
//This class will return a list of random points packed to a specified radius
//the algorithm keeps going until it fails to find a new packing point.
//There is an option which packs according to the tone of an image
//There is an option which uses a list of pixels to pack from
//
//The radius is defined as a document space number (0...1), so a radius of 0.1 would give 10 points across the image
//
class PointGenerator_RadialPack extends PointGenerator {

	// specific data to help point generation
	float radiusLow = 0.001f;
    float radiusHigh= 0.01f;
    float lowDistributionThreshold = 0;
	BufferedImage distributionImage;
	CoordinateSpaceConverter distributionImageCoordinateSpaceConverter;
	
	// a list of 3d points for the 3d packing algorithm
	ArrayList<PVector> points3d = new ArrayList<PVector>();
	SceneData3D sceneData;

	
	
	// this is the number of attempts a point can try to make before the packing
	// gives up
	int attemptsCounter = 300;

	

	public PointGenerator_RadialPack(float documentAspect) {
		super(documentAspect);
	}

	public PointGenerator_RadialPack(int rseed, float documentAspect) {
		super(rseed, documentAspect);
	}
	
	public PointGenerator_RadialPack(int rseed, float documentAspect, SceneData3D sd) {
		super(rseed, documentAspect);
		// if scene data is set, then the calculations on packing closeness are#
				// based on 3d locations from the scene data
		sceneData = sd;
	}

	
	
	void setMaskImage(BufferedImage mask) {
		maskImage = mask;
		if(sceneData!=null) {
			maskImageCoordinateSpaceConverter =  new CoordinateSpaceConverter(maskImage.getWidth(), maskImage.getHeight(), sceneData.getROI(), aspect);
		} else {
			maskImageCoordinateSpaceConverter =  new CoordinateSpaceConverter(maskImage.getWidth(), maskImage.getHeight(),  aspect);
		}
		//System.out.println("setting mask image" + mask);
	}

	void clearPoints() {
		points.clear();
		points3d.clear();
	}
	
	Seed getNextSeed() {
		Seed s = super.getNextSeed();
		if(sceneData==null) return s;
		s.depth = sceneData.getDepthNormalised(s.docPoint);
		
		return s;
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
		if(distributionImage == null) return generateDistributed(radiusLow);
		return generateBitmapDistributed(distributionImage, radiusLow, radiusHigh);
	}
		
		
	ArrayList<PVector> generateDistributed(float radius){	
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
				System.out.println("exceeded max number of attempts");
				break;
			}
		}

		return points;
	}

	

	
	ArrayList<PVector> generateBitmapDistributed(BufferedImage bitmap, float rLo, float rHi) {
		int previousBiggestNumberOfAttempts = 0;
		int lowDistributionThresholdAttempts = 0;
		if(sceneData!=null) {
			distributionImageCoordinateSpaceConverter = new CoordinateSpaceConverter(bitmap.getWidth(), bitmap.getHeight(), sceneData.getROI(), aspect);
		}else {
			distributionImageCoordinateSpaceConverter = new CoordinateSpaceConverter(bitmap.getWidth(), bitmap.getHeight(),  aspect);
		}
		SecondsTimer timer = new SecondsTimer();
		timer.printTimeSinceStart("generateBitmapDistributed making  points... ");
		int attempts = 0;
		while (true) {
			PVector thisPt = getRandomDocSpacePoint();
			
			float v = getBitmapValue01(thisPt,  bitmap);

			// check against the bitmap lowDistributionThreshold
			if(v < lowDistributionThreshold) {
				// don't add the point, and don't add the stats to the attempts counter
				// but we do need some sort of time out, so it has an independent counter
				lowDistributionThresholdAttempts++;
				if(lowDistributionThresholdAttempts > 10000) {
					System.out.println("generateBitmapDistributed: 10000 attemps all below bitmap threshold of " + lowDistributionThreshold);
					return points;
				}
				continue;
			}
			// if you get here, the the point has passed the lowDistributionThreshold, so reset counter
			lowDistributionThresholdAttempts = 0;	
			
			
			
			float radius = getClosestProximityUsingBitmap1(thisPt, bitmap, rLo, rHi);
			boolean success = tryAddDistributedPoint(thisPt, radius);
			
			
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
		int sz = points.size();
		timer.printTimeSinceStart("made " + sz + " points ");
		return points;

	}

	private boolean tryAddDistributedPoint(PVector thisPt, float radius) {
		// just tries to add 1 point, returns true if added, false if not added
		if (sceneData == null) {
			if (pointExistsWithinRadius(thisPt, radius))
				return false;
			points.add(thisPt);
		} else {
			if (pointExistsWithinRadius3d(thisPt, radius))
				return false;
			points.add(thisPt);
			PVector thisPoint3d = sceneData.get3DPoint(thisPt);
			points3d.add(thisPoint3d);
		}
		return true;

	}

	private boolean pointExistsWithinRadius(PVector p, float radius) {

		// returns the index of the nearest point in the current point list
		// if optimise == true, the uses the optimiseSearchRect
		// If there are no points whithin the optimising rect, then returns -1

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

	/////////////////////////////////////////////////////////////////////////////////
	// Evenly distributed points with a spacing radius dependednt on the value of a
	///////////////////////////////////////////////////////////////////////////////// bitmap
	// rLo maps to blacj=k in the bit map, rHi maps to white in the bitmap
	//

	private boolean pointExistsWithinRadius3d(PVector p, float radius) {

		// returns the index of the nearest point in the current point list
		// if optimise == true, the uses the optimiseSearchRect
		// If there are no points whithin the optimising rect, then returns -1

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
	
	private float getClosestProximityUsingBitmap1(PVector docPt, BufferedImage bitmap, float surfaceAraeBlack, float surfaceAreaWhite) {
		//PVector imgPixelLoc = distributionImageCoordinateSpaceConverter.docSpaceToImageCoord(p);
		//int c = bitmap.getRGB((int) imgPixelLoc.x, (int) imgPixelLoc.y);
		//boolean hasAlpha = ImageProcessing.hasAlpha(bitmap);
		//float t = ImageProcessing.packedIntToVal01(c, hasAlpha);
		float t = getBitmapValue01( docPt,  bitmap);
		float thisSurfaceArea = MOMaths.lerp(t, surfaceAraeBlack, surfaceAreaWhite);
		// System.out.println("tone = " + t + " radius "+ r);
		float r = (float) Math.sqrt(thisSurfaceArea/Math.PI);
		return r;
	}
	
	float getBitmapValue01(PVector docPt, BufferedImage bitmap) {
		PVector imgPixelLoc = distributionImageCoordinateSpaceConverter.docSpaceToImageCoord(docPt);
		int c = bitmap.getRGB((int) imgPixelLoc.x, (int) imgPixelLoc.y);
		boolean hasAlpha = ImageProcessing.hasAlpha(bitmap);
		return  ImageProcessing.packedIntToVal01(c, hasAlpha);
	}

	
}// end of PointGenerator_RadialPack class

/////////////////////////////////////////////////////////////////////////////////
//for session iteration - getting points generated one after another
//
abstract class CollectionIterator {
	int itemIteratorCounter = 0;

	boolean justfinishedFlag = false;
	
	boolean areItemsRemaining() {
		if (getNumItemsRemaining() <= 0)
			return false;
		return true;
	}
	
	boolean isJustFinished() {
		// returns true just once upon finishing the iteration
		if(areItemsRemaining()) return false;
		if(justfinishedFlag == false) {
			justfinishedFlag = true;
			return true;
		}
		return false;
	}

	int getNumItemsRemaining() {
		return getNumItems() - itemIteratorCounter;
	}

	Object getNextItem() {
		if (itemIteratorCounter >= getNumItems())
			return null;
		return getItem(itemIteratorCounter++);
	}
	
	void advanceIterator(int n) {
		setIterator(itemIteratorCounter +  n);
	}
	
	void setIterator(int n) {
		if(n < 0) itemIteratorCounter = 0;
		if(n >= getNumItems() ) itemIteratorCounter = getNumItems();
		if(areItemsRemaining()) justfinishedFlag = false;
	}
	
	
	int getIteratorCounter() {
		return itemIteratorCounter;
	}

	void resetItemIterator() {
		itemIteratorCounter = 0;
		justfinishedFlag = false;
	}
	
	abstract int getNumItems();
	
	abstract Object getItem(int n);

}
