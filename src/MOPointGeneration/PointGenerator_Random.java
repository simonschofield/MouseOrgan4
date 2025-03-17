package MOPointGeneration;
import java.awt.image.BufferedImage;



import java.util.ArrayList;
import java.util.Comparator;


import MOImage.KeyImageSampler;
import MOMaths.PVector;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOUtils.CollectionIterator;
import MOUtils.GlobalSettings;
import MOUtils.CollectionIterator;

public class PointGenerator_Random extends CollectionIterator{
	RandomStream randomStream;
	float aspect;
	
	/////////////////////////////////////////////////////////////////////////
	// The generation area is a Rect, in which the points are evenly generated.
	// The points could be in any coordinate space, but the default setting
	// is to define the  generationAreaRect as the whole document space
	// Hence the points generated are in DocumentSpace
	Rect generationAreaRect = new Rect(0,0,1,1);
	// the mask image is set to black/white specifiying where points may be
	// generated
	
	KeyImageSampler maskKeyImageSampler;
	float maskThreshold = 0.5f;

	// the list of points eventiallu calculated
	ArrayList<PVector> points = new ArrayList<PVector>();
	
	
	int pointsCacheThreshold = 50000;
	ArrayList<PVector> pointsCache = new ArrayList<PVector>();
	// so this class isn't entirely abstract
	int numberOfPointsRequest = 1000;
	
	

	public PointGenerator_Random(int rseed) {
		aspect = GlobalSettings.getTheDocumentCoordSystem().getDocumentAspect();
		randomStream = new RandomStream(rseed);
		float w = GlobalSettings.getTheDocumentCoordSystem().getDocumentWidth();
		float h = GlobalSettings.getTheDocumentCoordSystem().getDocumentHeight();
		setGenerationArea(new Rect(0,0,w,h));
		System.out.println("PointGenerator_Random generation area " + generationAreaRect.toStr());
	}
	
	public void setRandomStreamSeed(int rseed) {
		randomStream = new RandomStream(rseed);
	}
	
	
	void clearPointsAndUPdatePointsCache() {
		pointsCache.addAll(points);
		points.clear();
	}
	
	
	int getTotalNumberPointsFound() {
		return pointsCache.size() + points.size();
	}
	
	void addBackCachePoints() {
		
		points.addAll(pointsCache);
	}
	

	public void setGenerationArea(Rect r) {
		generationAreaRect = r;
		//System.out.println("PointGenerator_Random generation area " + generationAreaRect.toStr());
	}


	void setNumberOfPointsRequest(int n) {
		numberOfPointsRequest = n;
	}
	
	ArrayList<PVector> generatePoints(){
		return generatePoints(numberOfPointsRequest);
	}
	
	public ArrayList<PVector> generatePoints(int num){
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
	
	void appendPoints(ArrayList<PVector> otherPoints) {
		points.addAll(otherPoints);
	}

	void clearPoints() {
		points.clear();
	}

	public void setMaskImage(BufferedImage mask) {
		maskKeyImageSampler = new KeyImageSampler(mask);
	}
	
	

	void perturbPoints(float a) {
		for(PVector p : points) {
			p.x += randomStream.randRangeF(-a, a);
			p.y += randomStream.randRangeF(-a, a);
			}
	}
	
	
	PVector getRandomDocSpacePoint() {
		PVector p = randomStream.randomPoint2(generationAreaRect);
		
		if (maskKeyImageSampler == null)
			return p;
		int bailCount = 0;

		while (true) {
			if (permittedByMaskImage(p)) return p;
			p = randomStream.randomPoint2(generationAreaRect);
			bailCount++;
			if (bailCount > 1000000)
				return null;
		}

	}
	
	void setMaskThreshold(float v) {
		maskThreshold = v;
	}

	boolean permittedByMaskImage(PVector p) {
		if(maskKeyImageSampler==null) return true;
		
		float t = maskKeyImageSampler.getValue01DocSpace(p);
		
		//float t =  ImageProcessing.getValue01(maskImage, (int) imgPixelLoc.x, (int) imgPixelLoc.y);
		
		if (t < maskThreshold) return false;
		
		return true;
	}
	
	// In case you need to sort the depth of the points on the z component of the point
	// More used by subclasses
	void depthSort() {
		points.sort(Comparator.comparing(PVector::getZ).reversed());
	}
	
	

	@Override
	public int getNumItems() {
		// TODO Auto-generated method stub
		return getTotalNumberPointsFound();
	}

	@Override
	public PVector getItem(int n) {
		// TODO Auto-generated method stub
		if(n >= points.size()) return null;
		return points.get(n);
	}
	
	
	public PVector getNextPoint() {
		return (PVector)(super.getNextItem());
	}
	
	

}// end of PointGenerator class
