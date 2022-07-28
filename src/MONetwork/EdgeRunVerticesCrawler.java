package MONetwork;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;

import MOCompositing.RenderTarget;
import MOImage.KeyImageSampler;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Vertices2;
import MOUtils.KeyValuePairList;

///////////////////////////////////////////////////////////////////////////////////////////////
// REDUNDANT - use NNetworkEdgeRunFinder to collect all the edges as Vertices2, then use a Vertices2Crawler on each vertices.
//
// Extracts the edge runs from a collection of edges using a NNetworkEdgeRunExtractor or from the regions
// stores them as a list of Vertices2
// Once extracted, the network is not consulted any more. You are just dealing with Vertices2



public class EdgeRunVerticesCrawler{
	
	
	
	// copy of the network and search criteria used in the extraction
	private NNetwork theNetwork;
	private KeyValuePairList theSearchCriteria;
	
	// The extracted edge run vertices
	ArrayList<Vertices2> edgeRunVertices; 

	// the current vertices being crawled
	Vertices2 currentVertices = null;


	// this is the user-set size of the crawl step in document space
	float targetCrawlStepDistance = 0.01f;

	// this is the nearest crawlstep to the target craw step for any particular run
	// calculated at the start for each found run
	float thisRunCrawlStep = 0.01f;

	// these are the normalised state of the crawl
	float crawlStepParametric = 0.01f;
	float currentVerticesProgressParametric = 0;

	int runCounter = 0;


	public EdgeRunVerticesCrawler(NNetwork ntwk, KeyValuePairList searchCriteria) {
		theNetwork = ntwk;
		theSearchCriteria = searchCriteria;
		
		}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Initialisations
	//
	//

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// extract general edge-runs from the network - so is suitable for roads and will find incomplete parts of regions
	//
	public void extractNetworkEdgeVertices(float angleTollerance){
		
		NNetworkEdgeRunFinder edgeRunExtractor = new NNetworkEdgeRunFinder(theNetwork, theSearchCriteria);
		edgeRunExtractor.setAngleTollerance(angleTollerance);
		edgeRunVertices = edgeRunExtractor.extractAllEdgeRunVertices();
		runCounter = 0;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	// for finding the vertices of regions only
	//
	public void extractRegionEdgeVertices(){
		
		ArrayList<NRegion> regions = theNetwork.getRegions();
		edgeRunVertices = new ArrayList<Vertices2>();
		for(NRegion r : regions) {
			Vertices2 verts = r.getVertices();
			edgeRunVertices.add(verts);
		}
		System.out.println("preExtractEdgeRuns: found " + edgeRunVertices.size() + " runs");
		runCounter = 0;
	}


	public void sortEdgeRuns(boolean shortestFirst) {
		if(isInitialised()==false) return;


		if(shortestFirst) {
			edgeRunVertices.sort(Comparator.comparing(Vertices2::getTotalLength));
		} else {
			edgeRunVertices.sort(Comparator.comparing(Vertices2::getTotalLength).reversed());
		}

	}
	
	public void setRunDirectionPreference(int preference) {
		for(Vertices2 v: edgeRunVertices) {
			v.setRunDirectionPreference(preference);
		}
	}
	
	public void removeShortEdgeRuns(float minLength) {
		ArrayList<Vertices2> toBeRemoved = new ArrayList<Vertices2>();
		for(Vertices2 v: edgeRunVertices) {
			if( v.getTotalLength() < minLength) toBeRemoved.add(v);
		}
		edgeRunVertices.removeAll(toBeRemoved);

	}

	public void setCrawlStep(float s) {
		targetCrawlStepDistance = s;
	}

	public float getCrawlStep() {
		return targetCrawlStepDistance;

	}

	// useful for other classes that might want to access the vertices for their own crawling (e.g. Text crawler)
	public Vertices2 getNextEdgeRunVertices() {
		if(runCounter >= edgeRunVertices.size()) return null;
		currentVertices = edgeRunVertices.get(runCounter);
		runCounter++;
		return currentVertices;
	}

	public ArrayList<Vertices2> getEdgeRunVertices(){
		return edgeRunVertices;
	}
	
	public void setEdgeRunVertices(ArrayList<Vertices2> vertList){
		edgeRunVertices = vertList;
	}


	//////////////////////////////////////////////////////////////////////////////
	// repeatedly calling updateCrawl will eventually traverse the entire extracted set of edges
	//
	public Line2 updateCrawl() {
		if(isInitialised()==false) return null;

		//System.out.println("updateCrawl: currentVertices num = " + currentVertices.size());
		Line2 line = getNextCrawlLine();
		if(line == null) {
			// System.out.println("updateCrawl: finished previous crawl, getting new edge run");
			Vertices2 v = nextEdgeRun();

			if(v == null) {
				// System.out.println("updateCrawl: no more edge runs");
				return null;
			}


			line = getNextCrawlLine();
			if(line == null) {
				// probably should not happen but ....
				// System.out.println("updateCrawl: cannot get a line from new edge run");
				return null;
			}
		}
		return line;

	}

	// private

	//////////////////////////////////////////////////////////////////////////////
	// to extract a particular edge into currentVertices, call nextEdgeRun.
	// This returns true if there is another edge run found.
	// Then, you can inspect the edge run vertices, and interpolate over them by getting
	// getCurrentVertices()

	private Vertices2 nextEdgeRun() {
		currentVertices =  getNextEdgeRunVertices() ;


		if(currentVertices==null) return null; // no more runs to be found

		// works out the nearest integer subdivision of the length to the targetCrawlStepDistance
		// and the number of steps this will take, therefore the crawlStepParametric
		float totalLen = currentVertices.getTotalLength();
		float numStepsLo = (int)(totalLen/targetCrawlStepDistance);
		float numStepsHi = numStepsLo+1;
		float candidateCrawlStepHi = totalLen/numStepsLo;
		float candidateCrawlStepLo = totalLen/numStepsHi;

		int numSteps = 1;

		if(MOMaths.diff(candidateCrawlStepHi, targetCrawlStepDistance) < MOMaths.diff(candidateCrawlStepLo, targetCrawlStepDistance)) {
			numSteps = (int)numStepsLo;
			thisRunCrawlStep = candidateCrawlStepHi;
		} else {
			numSteps = (int)numStepsHi;
			thisRunCrawlStep = candidateCrawlStepLo;
		}



		crawlStepParametric = 1.0f/(float)numSteps;
		currentVerticesProgressParametric = 0;

		return currentVertices;
	}



	private Line2 getNextCrawlLine() {
		// the crawl line lies between currentVerticesProgressParametric and currentVerticesProgressParametric+crawlStepParametric
		// it will return a consistent line length, as the division of total steps in the line is pre-calculated as an integer

		// for this to work currentVertices must have been initialised with getNextEdgeRun();
		if(currentVertices==null) return null; 

		if(currentVerticesProgressParametric >= 1) {
			//System.out.println("getNextCrawlLine: vertices have been fully traversed");
			return null;
		}

		PVector lineStart = currentVertices.lerp(currentVerticesProgressParametric);

		currentVerticesProgressParametric += crawlStepParametric;
		currentVerticesProgressParametric = MOMaths.constrain(currentVerticesProgressParametric, 0, 1);
		PVector lineEnd = currentVertices.lerp(currentVerticesProgressParametric);

		Line2 line = new Line2(lineStart, lineEnd);

		return line;
	}


	//////////////////////////////////////////////////////////////////////////////
	//
	//
	private boolean isInitialised() {
		if(edgeRunVertices==null) {
			System.out.println("ERROR: EdgeRunVerticesCrawler: is not initilased -  call extractNetworkEdgeVertices or extractRegionEdgeVertices first");
			return false;
		}
		return true;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// post processing the pre-extracted edge runs
	// Not sure about these......

	


	public void displaceExtractedEdgeRuns(int pointdoubling, float wobble, BufferedImage displacementImage) {
		KeyImageSampler kis = new KeyImageSampler(displacementImage);
		int numEdgeRuns  = edgeRunVertices.size();
		for(int n = 0; n < numEdgeRuns; n++) {
			Vertices2 verts = edgeRunVertices.get(n);
			verts.doubleVertices(pointdoubling);
			float imageSampleYPoint = n/(float)numEdgeRuns;
			verts = getDisplacedVerticesPoints( verts,  wobble,  kis,  imageSampleYPoint);
			edgeRunVertices.set(n, verts);
		}


	}


	void drawExtractedEdgeRuns(Color c, int width, RenderTarget rt) {

		int numEdgeRuns  = edgeRunVertices.size();
		for(int n = 0; n < numEdgeRuns; n++) {
			Vertices2 verts = edgeRunVertices.get(n);
			rt.drawVerticesWithPoints(verts,  c, width);
		}

	}


	Vertices2 getDisplacedVerticesPoints(Vertices2 verts, float wobble, KeyImageSampler displacementImage, float imageSampleYPoint) {

		// wobble is scaled by the total vertices length; so a wobble of 0.5, would become 0.5*verticeslength()
		//wobble *= verts.getTotalLength();

		ArrayList<PVector> verticesOut = new ArrayList<PVector> ();

		int numPoints = verts.getNumVertices();
		for(int n = 0; n < numPoints; n++) {
			PVector thisPt = verts.get(n);

			float parametric = n/(float)numPoints;
			PVector imageSamplePoint = new PVector(parametric,imageSampleYPoint);
			float imageValue = displacementImage.getValue01NormalisedSpace(imageSamplePoint); // this gives

			float displacement = MOMaths.lerp(imageValue, -1, 1) * wobble;

			if(n == 0 || n == numPoints-1) displacement = 0;

			PVector displacedPoint = verts.getOrthogonallyDisplacedPoint(n, displacement);

			verticesOut.add(displacedPoint);
		}


		return new Vertices2(verticesOut);
	}

}

