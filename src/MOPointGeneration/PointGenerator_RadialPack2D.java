package MOPointGeneration;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import MOImage.KeyImageSampler;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Rect;
////////////////////////////////////////////////////////////////////////////
//This class will return a list of random points packed to a specified radius
//the algorithm keeps going until it fails to find a new packing point after a number of attempts.
//There is an option which packs according to the tone of an image
//There is an option which uses a list of pixels to pack from
//
//The radius is defined as a document space number (0...1), so a radius of 0.1 would give 10 points across the image
// It can be set with a fixed radius, or a range of radii set in response to a distributionImage
// if it is a fixed radius, then radiusLow carries the setting
public class PointGenerator_RadialPack2D extends PointGenerator_Random {

	// specific data to help point generation
	PackingInterpolationScheme packingInterpolationScheme = new PackingInterpolationScheme();
	float fixedRadius = 1;

	KeyImageSampler distributionKeyImageSampler;
	boolean fitDistributionImageToGenerationAreaRect = false;

	int maxPointsPlacedLimit = 1000000; // never needed a million
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
	public void setPackingRadius(float r) {
		fixedRadius = r;
	}
	
	// default is 300 attempts, but you can alter it 0..1 -> 10...600, so 0.5 is about the default.
	void setPackingSearchTenacity(float t) {
		attemptsCounter = (int)	MOMaths.lerp(t, 10,600);
	}
	
	public void setPackingInterpolationScheme(PackingInterpolationScheme is,  BufferedImage distImg) {
		packingInterpolationScheme = is;
		distributionKeyImageSampler = new KeyImageSampler(distImg);
	}
	
	public void setMaxNumPointsLimit(int n) {
		maxPointsPlacedLimit = n;
		
	}
	
	
	public ArrayList<PVector> generatePoints() {
		if(distributionKeyImageSampler == null) return generateUniformlyDistributedPoints();
		return generateImageResponsiveDistributedPoints();
	}
		
		
	public ArrayList<PVector> generateUniformlyDistributedPoints(){	
		int attempts = 0;
		
		while (true) {
			PVector thisPt = getRandomDocSpacePoint();
			System.out.println("generateUniformlyDistributedPoints trying point " +  thisPt.toStr() + " fixedRadius " + fixedRadius);
			boolean success = tryAddDistributedPoint(thisPt, fixedRadius); // for 3D LOD needs to take Z into account
			if (!success) {
				attempts++;
			} else {
				attempts = 0;
			}
			if (attempts > attemptsCounter) {
				System.out.println("SeedPacking: placed " +  getNumItems() + " after attempts " + attempts);
				break;
			}
		}

		return points;
	}

	
    public void fitDistributionImageToGenerationAreaRect(boolean fitToRect) {
    	fitDistributionImageToGenerationAreaRect = fitToRect;
    }
	
	ArrayList<PVector> generateImageResponsiveDistributedPoints() {
		int previousBiggestNumberOfAttempts = 0;
		
		
		
		
		int attempts = 0;
		while (true) {
			PVector thisPt = getRandomDocSpacePoint();
			
			
			// check against the bitmap lowDistributionThreshold
			boolean success;
			float v;
			
			
			// should be in own method.... float getDistributionImageValue(PVector p)
			//
			//
			if(fitDistributionImageToGenerationAreaRect) {
				// Then map the point normalised within the GenerationAreaRect to the image
				PVector normalisedToGenerationRect = generationAreaRect.norm(thisPt);
				
				if(generationAreaRect.isPointInside(normalisedToGenerationRect)) {
					v = distributionKeyImageSampler.getValue01NormalisedSpace(normalisedToGenerationRect);
				} else {
					v = 0;
				}
				
			} else {
			
			
			   v = distributionKeyImageSampler.getValue01DocSpace(thisPt);
			}
			//
			//
			//
			
			
			if(  packingInterpolationScheme.isExcluded(v) ) {
				success = false;
			} else {
				
				float radius = packingInterpolationScheme.getRadius(v); // for 3D LOD, this needs to be packingInterpolationScheme.getRadiusWithLOD(v,z);
				
				success = tryAddDistributedPoint(thisPt, radius);
				//System.out.println("generateImageResponsiveDistributedPoints trying point " +  thisPt.toStr() + " radius " + radius + " success = " + success);
			}
			
			
			if (!success) {
				attempts++;
			} else {
				
				if(attempts > previousBiggestNumberOfAttempts) {
					previousBiggestNumberOfAttempts = attempts;
					int percent = (int)((previousBiggestNumberOfAttempts/(float)attemptsCounter)*100);
					int numPoints = points.size();
					System.out.print( percent + "% (" + numPoints + "), " );
				}
				attempts = 0;
			}
			if (attempts > attemptsCounter || points.size()> maxPointsPlacedLimit) {
				System.out.println("100%: total placed " + getNumItems());
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

		Rect rectUnderConsideration = new Rect(new PVector(x1, y1), new PVector(x2, y2));

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

	

	
}// end of PointGenerator_RadialPack class


