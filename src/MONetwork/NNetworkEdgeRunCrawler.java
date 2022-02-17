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
//this class uses the base class to implement the extraction of "connected" edge runs as Vertices2
//This can be done on the fly using MODE_EXTRACTED, or all the edge runs Vertices2's can be pre-calculated at start
//using MODE_PRE_EXTRACTED. IN either case, the network is traversed in line2 segments of a defined length crawlStepDistance,
//once the current Vertices2 has been exhausted, the next one is used, until the entire network has been traversed.

//once pr extracted you can process the vertices further (e.g. add fractal detail)


public class NNetworkEdgeRunCrawler  extends NNetworkEdgeRunExtractor{
	static final int MODE_EDGES = 0;
	static final int MODE_REGIONS = 1;

	int mode = MODE_EDGES;

	// the most recently extracted edge run
	Vertices2 currentVertices = null;

	// to store pre-extracted edge runs, so you can sort them
	ArrayList<Vertices2> preExtractedEdgeRuns = new ArrayList<Vertices2>();


	// this is the user-set size of the crawl step in document space
	float crawlStepDistance = 0.01f;

	// these are the normalised state of the crawl
	float crawlStepParametric = 0.01f;
	float currentVerticesProgressParametric = 0;

	NNetworkItemIterator regionIterator;

	int runCounter = 0;
	//int lineInRunCounter = 0;

	public NNetworkEdgeRunCrawler(NNetwork ntwk, KeyValuePairList searchCriteria){
		super(ntwk, searchCriteria);

		regionIterator = new NNetworkItemIterator();
		regionIterator.setRegions(ntwk.getRegions());


	}

	public void setMode(int m) {

		mode = m;
	}

	//////////////////////////////////////////////////////////////////////////////
	// repeatedly calling updateCrawl will eventually traverse the entire extracted set of edges
	//
	//
	public Line2 updateCrawl() {

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



	//////////////////////////////////////////////////////////////////////////////
	// to extract a particular edge into currentVertices, call nextEdgeRun.
	// This returns true if there is another edge run found.
	// Then, you can inspect the edge run vertices, and interpolate over them by getting
	// getCurrentVertices()

	public Vertices2 nextEdgeRun() {
		if(mode == MODE_EDGES) currentVertices =  getNextEdgeRun_PreExtracted() ;
		if(mode == MODE_REGIONS) currentVertices =  getNextEdgeRunVertices_NextRegion() ;

		if(currentVertices==null) return null; // no more runs to be found

		float totalLen = currentVertices.getTotalLength();
		//System.out.println("nextEdgeRun : total currentVertices = " + currentVertices.size());
		float numSteps = (int)(totalLen/crawlStepDistance);
		if(numSteps == 0) numSteps = 1;
		crawlStepParametric = 1/numSteps;
		currentVerticesProgressParametric = 0;


		runCounter++;
		return currentVertices;
	}



	Vertices2 getNextEdgeRun_PreExtracted() {
		if(runCounter >= preExtractedEdgeRuns.size()) return null;
		currentVertices = preExtractedEdgeRuns.get(runCounter);
		return currentVertices;
	}


	public ArrayList<Vertices2> findEdgeRuns(){
		preExtractedEdgeRuns.clear();
		while(true) {
			Vertices2 verts = new Vertices2();
			if(mode == MODE_EDGES) verts =  getNextEdgeRunVertices_RandomStart() ;
			if(mode == MODE_REGIONS) verts =  getNextEdgeRunVertices_NextRegion() ;
			if(verts==null) {
				System.out.println("preExtractEdgeRuns: fininshed collecting edge runs ");
				break;
			}
			preExtractedEdgeRuns.add(verts);

		}
		System.out.println("preExtractEdgeRuns: found " + preExtractedEdgeRuns.size() + " runs");
		runCounter = 0;
		return preExtractedEdgeRuns;
	}

	Vertices2 getNextEdgeRunVertices_RandomStart() {
		ArrayList<NEdge> thisRun = extractEdgeRun_RandomStart();
		if(thisRun == null) return null;
		return getVertices(thisRun);

	}


	Vertices2 getNextEdgeRunVertices_NextRegion() {
		NRegion nextRegion = regionIterator.getNextRegion();
		if(nextRegion == null) return null;
		Vertices2 v = nextRegion.getVertices();
		v.close();
		return v;
	}

	public void sortEdgeRuns(boolean shortestFirst) {
		if(preExtractedEdgeRuns==null) return;


		if(shortestFirst) {
			preExtractedEdgeRuns.sort(Comparator.comparing(Vertices2::size));
		} else {
			preExtractedEdgeRuns.sort(Comparator.comparing(Vertices2::size).reversed());
		}

	}


	Vertices2 getCurrentVertices() {
		return currentVertices;
	}

	float getCurrentVerticesInterpolationControlValue() {
		return currentVerticesProgressParametric;
	}

	void setCurrentVerticesInterpolationControlValue(float v) {
		currentVerticesProgressParametric = v;
	}

	void advanceCurrentVerticesInterpolationControlValue(float v) {
		currentVerticesProgressParametric += v;
	}

	public NRegion getCurrentRegion() {
		return regionIterator.getCurrentRegion();
	}


	public void setCrawlStep(float s) {
		crawlStepDistance = s;
	}

	Line2 getNextCrawlLine() {
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



	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// post=prossesing the pre-extracted edge runs
	//

	ArrayList<Vertices2> getExtractEdgeRuns(){
		return preExtractedEdgeRuns;
	}


	public void displaceExtractedEdgeRuns(int pointdoubling, float wobble, BufferedImage displacementImage) {
		KeyImageSampler kis = new KeyImageSampler(displacementImage);
		int numEdgeRuns  = preExtractedEdgeRuns.size();
		for(int n = 0; n < numEdgeRuns; n++) {
			Vertices2 verts = preExtractedEdgeRuns.get(n);
			verts.doubleVertices(pointdoubling);
			float imageSampleYPoint = n/(float)numEdgeRuns;
			verts = getDisplacedVerticesPoints( verts,  wobble,  kis,  imageSampleYPoint);
			preExtractedEdgeRuns.set(n, verts);
		}


	}


	public void drawExtractedEdgeRuns(Color c, int width, RenderTarget rt) {

		int numEdgeRuns  = preExtractedEdgeRuns.size();
		for(int n = 0; n < numEdgeRuns; n++) {
			Vertices2 verts = preExtractedEdgeRuns.get(n);
			rt.drawVerticesWithPoints(verts,  c, width);
		}

	}


	Vertices2 getDisplacedVerticesPoints(Vertices2 verts, float wobble, KeyImageSampler displacementImage, float imageSampleYPoint) {

		// wobble is scaled by the total vertices length; so a wobble of 0.5, would become 0.5*verticeslength()
		//wobble *= verts.getTotalLength();

		ArrayList<PVector> verticesOut = new ArrayList<PVector> ();

		int numPoints = verts.size();
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




