import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

public class PointGenerator extends CollectionIterator{
	RandomStream randomStream;
	float aspect;
	Rect generationAreaRect;
	// the mask image is set to black/white specifiying where points may be
	// generated
	BufferedImage maskImage;
	float maskThreshold = 0.5f;
	CoordinateSpaceConverter maskImageCoordinateSpaceConverter;

	// the list of points eventiallu calculated
	ArrayList<PVector> points = new ArrayList<PVector>();

	// so this class isn't entirely abstract
	int numberOfPointsRequest = 1000;
	
	

	public PointGenerator(int rseed) {
		aspect = GlobalObjects.theDocument.getDocumentAspect();
		randomStream = new RandomStream(rseed);
		float w = GlobalObjects.theDocument.getDocumentWidth();
		float h = GlobalObjects.theDocument.getDocumentHeight();
		setGenerationArea(new Rect(0,0,w,h));
	}
	

	void setGenerationArea(Rect r) {
		generationAreaRect = r;
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
	
	void appendPoints(ArrayList<PVector> otherPoints) {
		points.addAll(otherPoints);
	}

	void clearPoints() {
		points.clear();
	}

	void setMaskImage(BufferedImage mask) {

		maskImage = mask;
		maskImageCoordinateSpaceConverter =  new CoordinateSpaceConverter(maskImage.getWidth(), maskImage.getHeight(), aspect);
		//System.out.println("setting mask image" + mask);
	}
	
	

	void perturbPoints(float a) {
		for(PVector p : points) {
			p.x += randomStream.randRangeF(-a, a);
			p.y += randomStream.randRangeF(-a, a);
			}
	}
	
	
	PVector getRandomDocSpacePoint() {
		PVector p = randomStream.randomPoint2(generationAreaRect);
		
		if (maskImage == null)
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
		if(maskImage==null) return true;
		
		PVector imgPixelLoc = maskImageCoordinateSpaceConverter.docSpaceToImageCoord(p);
		
		float t =  ImageProcessing.getValue01(maskImage, (int) imgPixelLoc.x, (int) imgPixelLoc.y);
		
		if (t < maskThreshold) return false;
		
		return true;
	}
	
	// In case you need to sort the depth of the points on the z component of the point
	// More used by subclasses
	void depthSort() {
		points.sort(Comparator.comparing(PVector::getZ).reversed());
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
	
	
	PVector getNextPoint() {
		return (PVector)(super.getNextItem());
	}
	
	

}// end of PointGenerator class
