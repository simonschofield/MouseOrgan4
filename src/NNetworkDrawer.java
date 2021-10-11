import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



// base class containing common useful methods used in processing NNetwork
class NNetworkProcessor{
	
	NNetwork theNetwork;
	KeyValuePair currentSearchAttribute;
	KeyValuePair hasBeenProcessedFlag;
	
	NNetworkProcessor(NNetwork ntwk){
		theNetwork = ntwk;
		hasBeenProcessedFlag = new KeyValuePair();
		hasBeenProcessedFlag.set("PROCESSED", true);
	}
	
	
	
	void setSearchAttribute(KeyValuePair kvp) {
		currentSearchAttribute = kvp.copy();
	}
	
	void setAsProcessed(NAttributes e) {
		e.getAttributes().addKeyValuePair(hasBeenProcessedFlag.copy());
	}
	
	boolean hasBeenProcessed(NAttributes e) {
		return e.getAttributes().containsEqual(hasBeenProcessedFlag);
		
	}
	
	boolean isMatchingSearchAttribute(NAttributes e) {
		 if(currentSearchAttribute==null) return true;
		 return e.getAttributes().containsEqual(currentSearchAttribute);
	}
	
	
	ArrayList<NEdge> getEdgesMatching(KeyValuePair searchCriteria){
		setSearchAttribute(searchCriteria);
		ArrayList<NEdge> edges = theNetwork.getEdges();
		ArrayList<NEdge> matchingEdges = new ArrayList<NEdge>();
		for (int n = 0; n < edges.size(); n++) {
			NEdge e = edges.get(n);
			if(isMatchingSearchAttribute(e) ) matchingEdges.add(e);
		}
		
		
		
		
		return matchingEdges;
	}
	
	
	
	
	ArrayList<NPoint> getPointsMatching(KeyValuePair searchCriteria){
		setSearchAttribute(searchCriteria);
		ArrayList<NPoint> points = theNetwork.getPoints();
		ArrayList<NPoint> matchingPoints = new ArrayList<NPoint>();
		for (int n = 0; n < points.size(); n++) {
			NPoint p = points.get(n);
			if(isMatchingSearchAttribute(p) ) matchingPoints.add(p);
		}
		return matchingPoints;
	}
	
	
	ArrayList<NRegion> getRegionsMatching(KeyValuePair searchCriteria){
		setSearchAttribute(searchCriteria);
		ArrayList<NRegion> regions = theNetwork.getRegions();
		ArrayList<NRegion> matchingRegions = new ArrayList<NRegion>();
		for (int n = 0; n < regions.size(); n++) {
			NRegion r = regions.get(n);
			if(isMatchingSearchAttribute(r) ) matchingRegions.add(r);
		}
		return matchingRegions;
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//
	// general useful methods that are specific for searching
	
	NPoint getOtherEdgeEnd(NEdge e, NPoint oneEnd) {
		if(e.p1 == oneEnd) return e.p2;
		if(e.p2 == oneEnd) return e.p1;
		return null;
	}
	
	NPoint getEdgeJoinPoint(NEdge e1, NEdge e2) {
		if(e1.p1 == e2.p1) {
			return e1.p1;
		}
		if(e1.p2 == e2.p2) {
			return e1.p2;
		}
		if(e1.p1 == e2.p2) {
			return e1.p1;
		}
		if(e1.p2 == e2.p1) {
			return e1.p2;
		}
		return null;
	}
	
	
	float angleBetweenEdges(NEdge e1, NEdge e2) {
		
		NPoint connectingPoint = e1.getConnectingPoint(e2);
		
		NPoint otherPointE1 = e1.getOtherPoint(connectingPoint);
		NPoint otherPointE2 = e2.getOtherPoint(connectingPoint);
		Line2 l1 = new Line2(otherPointE1.getPt(), connectingPoint.getPt());
		Line2 l2 = new Line2(connectingPoint.getPt(), otherPointE2.getPt());
		return l1.getAngleBetween(l2);
	}
	
	boolean checkEdgesAreOrdered(ArrayList<NEdge> edges) {
		//System.out.println("drawing num edges " + edges.size());
		if(edges == null) {
			System.out.println("checkEdgesAreOrdered - null edges list");
			return false;
		}
		NEdge thisEdge = edges.get(0);
		int numEdges = edges.size();
		for(int n = 1; n < numEdges; n++) {
			NEdge nextEdge = edges.get(n);

			if(thisEdge.connectsWith(nextEdge) == false) {
				System.out.println("checkEdgesAreOrdered - failed at edge " + n + " out of " + numEdges);
				return false;
			}
			thisEdge = nextEdge;
		}
		return true;
	}
	
	// turns an array list of NEdges, which should be sequential (edges link, but by either end, so, the raw end points are not sequential), into an ordered
	// list of PVector points by finding the joining points between each edge pair.
	ArrayList<PVector> getVertices(ArrayList<NEdge> edges){
		if( checkEdgesAreOrdered(edges)==false) {
			System.out.println("getVertices - edges are not ordered");
			return null;
		}
		ArrayList<PVector> vertices = new ArrayList<PVector>();
		
		NEdge thisEdge = edges.get(0);
		if(edges.size()==1) {
			vertices.add(thisEdge.getEndPt(0));
			vertices.add(thisEdge.getEndPt(1));
			return vertices;
		}
		
		
		
		NEdge nextEdge = edges.get(1);
		
		// find first dangling vertex
		NPoint connectionPoint = getEdgeJoinPoint(thisEdge, nextEdge);
		NPoint firstPoint = getOtherEdgeEnd(thisEdge, connectionPoint);
		vertices.add(firstPoint.getPt());
		
		
		
		int numEdges = edges.size();
		// find all the connecting points in order
		for(int n = 1; n < numEdges; n++) {
			nextEdge = edges.get(n);
			connectionPoint = getEdgeJoinPoint(thisEdge, nextEdge);
			vertices.add(connectionPoint.getPt());
			thisEdge = nextEdge;
		}
		
		// find final dangling point
		NPoint finalPoint = getOtherEdgeEnd(nextEdge, connectionPoint);
		vertices.add(finalPoint.getPt());
		
		return vertices;
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// draw-all -type methods
	//
	void draw() {
		drawPoints();
		drawEdges();
	}
	
		
	void drawPoints()	{
		ArrayList<NPoint> points = theNetwork.getPoints();
		for (int n = 0; n < points.size(); n++) {
			NPoint np = points.get(n);
			float radiusDocSpace = 0.0002f;
			drawPoint( np, Color.RED, radiusDocSpace);
		}
	}

	void drawEdges() {
		ArrayList<NEdge> edges = theNetwork.getEdges();
	
		for (int n = 0; n < edges.size(); n++) {
			NEdge e = edges.get(n);
			Color c = getEdgeColor(e);
			int lineWt = getEdgeLineWeight(e)*3;
			drawEdge(e,c,lineWt);
		}

	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// more specific drawing 
	//
	void drawPoint(NPoint np, Color c, float radiusDocSpace) {
		Rect viewPort = GlobalObjects.theSurface.getViewPortDocSpace();
		float lineThicknessDocSpace = radiusDocSpace/3f;
		PVector docPoint = np.getPt();
		if(viewPort.isPointInside(docPoint)==false) return;
		GlobalObjects.theDocument.drawCircle(docPoint, radiusDocSpace, c, c, lineThicknessDocSpace );
	}
	

	void drawEdge(NEdge e, Color c, int width) {
		Rect viewPort = GlobalObjects.theSurface.getViewPortDocSpace();
		PVector p1 = e.getEndPt(0);
	    PVector p2 = e.getEndPt(1);
		if(viewPort.isPointInside(p1)==false && viewPort.isPointInside(p2)==false) return;
		GlobalObjects.theDocument.drawLine(p1, p2, c, width);
	}
	
	void drawEdges(ArrayList<NEdge> edges, Color c, int width) {
		//System.out.println("drawing num edges " + edges.size());
		for(NEdge e: edges) {
			//System.out.println(e.toStr());
			drawEdge(e,  c,  width);
		}
	}
	
	
	void drawRegion(NRegion r, Color c, int width) {
		int numEdges = r.getNumEdges();
		for(int n = 0; n < numEdges; n++) {
			NEdge e = r.getEdge(n);
			drawEdge( e, c, width);
		}
	}
	
	void drawRegions(ArrayList<NRegion> regions, Color c, int width) {
		for(NRegion r: regions) {
			drawRegion(r,  c,  width);
		}
		
	}
	
	
	
	
	
	Color getEdgeColor(NEdge ne){
	    
	    KeyValuePair kvp = ne.getAttribute("ROAD");
	    if(kvp==null) return Color.GRAY;
	    String roadType = kvp.getString();
	    if(roadType.equals("A")){
	      return new Color(255,100,100);
	    }
	    if(roadType.equals("B")){
	    	return new Color(127,100,100);
	    }
	    if(roadType.equals("C")){
	      return new Color(127,200,200);
	    }
	    if(roadType.equals("F")){
		      return new Color(78,209,127);
		    }
	    return Color.GRAY;
	  }
	
	int getEdgeLineWeight(NEdge ne){
	    
	    KeyValuePair kvp = ne.getAttribute("ROAD");
	    if(kvp==null) return 2;
	    String roadType = kvp.getString();
	    if(roadType.equals("A")){
	      return 5;
	    }
	    if(roadType.equals("B")){
	    	return 4;
	    }
	    if(roadType.equals("C")){
	      return 3;
	    }
	    if(roadType.equals("F")){
		      return 2;
		    }
	    return 2;
	  }

}

//////////////////////////////////////////////////////////////////////////////////////
// This network processor extracts a continuous run of edges from a network
// The class keeps the found edge run in the form of and ArrayList of NEdges.
//
class NNetworkEdgeRunExtractor extends NNetworkProcessor{
	
	ArrayList<NEdge> theEdgeList;
	
	boolean isInitialised = false;
	
	float angleTolleranceDegrees = 45;
	//ArrayList<NEdge> currentEdgeRunList;
	
	RandomStream randomStream = new RandomStream(1);
	
	
	
	NNetworkEdgeRunExtractor(NNetwork ntwk){
		super(ntwk);
		
	} 
	
	
	private void initialiseRun() {
		// gets called just before the runs are collected
		// after all parameters are set up.
		if(isInitialised == true) return;
		if(currentSearchAttribute == null) {
			// makes a copy because theEdgeList gets destroyed by the search
			theEdgeList = (ArrayList)theNetwork.getEdges().clone();
		} else {
			theEdgeList = getEdgesMatching(currentSearchAttribute);
		}
		if(theEdgeList == null) {
			System.out.println("NetworkEdgeMarcher:startEdgeMarch edges = null");
			return;
		}
		if(theEdgeList.size()==0) {
			System.out.println("NetworkEdgeMarcher:startEdgeMarch no edges found");
			return;
		}
		
		isInitialised = true;
	}
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// public methods This method returns an ordered list of connected edges, starting at a random
	// available edge. These edges are removed from the initial
	// edgeList, so cannot be re-used in any other search
	//
	ArrayList<NEdge> extractEdgeRun_RandomStart() {
		initialiseRun();
		NEdge runStartEdge = findRandomStartEdge();
		if(runStartEdge == null) {
			// there are no more edges to be processed
			return null;
		}
		
		return extractEdgeRun(runStartEdge);

	}
	
	
	ArrayList<NEdge> extractEdgeRun(NEdge startEdge) {
		initialiseRun();
		ArrayList<NEdge> currentEdgeRunList= new ArrayList<NEdge>();

		ArrayList<NEdge> edgesP1Direction = getConnectedRun(startEdge, startEdge.p1); 
		ArrayList<NEdge> edgesP2Direction = getConnectedRun(startEdge, startEdge.p2);

		if(edgesP1Direction==null && edgesP2Direction==null) {
			if(startEdge!=null) currentEdgeRunList.add(startEdge);
			return currentEdgeRunList;
		}
		

		if(edgesP1Direction!=null) {
			Collections.reverse(edgesP1Direction);
			currentEdgeRunList.addAll(edgesP1Direction);
		}
		if(startEdge!=null) currentEdgeRunList.add(startEdge);
		if(edgesP2Direction!=null) currentEdgeRunList.addAll(edgesP2Direction);
		
		
		boolean result = checkEdgesAreOrdered(currentEdgeRunList);
	
		return currentEdgeRunList;
	}
	
	void setAngleTollerance(float angleDegrees) {
		angleTolleranceDegrees = angleDegrees;
	}
	
	void setRandomSeed(int s) {
		randomStream = new RandomStream(s);
	}
	
	
	
	
	void testRun() {
	
		while(true) {
			ArrayList<NEdge> thisRun = extractEdgeRun_RandomStart();
			
			
			if(thisRun == null) break;
			
			int thisRunSize = thisRun.size();
			
			//System.out.println("found run of " + thisRunSize + " total edges found" + numEdgesFound + " out of " + totalEdges);
			int r = randomStream.randRangeInt(20, 255);
			int g = randomStream.randRangeInt(20, 255);
			int b = randomStream.randRangeInt(20, 255);
			Color col = new Color(r,g,b);
			drawEdges(thisRun, col , 4);
			
		}

	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// private methods
	//
	private NEdge findRandomStartEdge() {
		if(theEdgeList == null) {
			System.out.println("NetworkEdgeMarcher:findAStartEdge edge = null");
			return null;
		}
		if(theEdgeList.size()==0) {
			System.out.println("NetworkEdgeMarcher:findAStartEdge no more edges found");
			return null;
		}
		
		int ind = 0;
		int listSize = theEdgeList.size();
		if(listSize > 1) ind = randomStream.randRangeInt(0,theEdgeList.size()-1);
		
		NEdge runStartEdge =  theEdgeList.get(ind);
		popFromEdgeList(runStartEdge);
		return runStartEdge;
	}
	
	
	private ArrayList<NEdge> getConnectedRun(NEdge startEdge, NPoint whichPoint) {
		
		ArrayList<NEdge> connectedEdges = new ArrayList<NEdge>();
		NEdge currentEdge =  startEdge;
		NPoint otherPointInConnectedEdge = whichPoint;

		while(true) {
			NEdge  connectedEdge = getConnectedEdge(currentEdge, otherPointInConnectedEdge);

			if(connectedEdge==null) {
				//no more edges can be found to connect
				break;
			}
			if( currentEdge.isUsingIdenticalPoints(connectedEdge) ) continue; // rare event, but it has been popped so ignore it
			
			// find "far" connecting point of current Edge
			NPoint connectionPoint = getEdgeJoinPoint(connectedEdge, currentEdge);
			otherPointInConnectedEdge = getOtherEdgeEnd(connectedEdge, connectionPoint);
			
			//System.out.println("connecting " + currentEdge.toStr() + " to " + connectedEdge.toStr());
			connectedEdges.add(connectedEdge);
			currentEdge = connectedEdge;
		}
		return connectedEdges;
	}
	
	
	
	private NEdge getConnectedEdge(NEdge thisEdge, NPoint usingThisPoint) {
		//finds an single edge connected to either end of thisEdge
		NEdge foundEdge = null;
		//System.out.println("getBestConnection this edge ID = " + thisEdge.getID());
		ArrayList<NEdge> connectedEdges = (ArrayList)usingThisPoint.getEdgeReferences().clone();
		foundEdge = getMostCollinearEdge(thisEdge, connectedEdges);
		if(foundEdge==null) {
			//System.out.println("getConnectedEdge: no connecting edge found");
			return null;
		}
		
		
		if(isCollinearWithinTollearance(thisEdge, foundEdge, angleTolleranceDegrees)== false){
			//System.out.println("getConnectedEdge: best connected edge is at too great an angle");
			return null;
		}
		
		popFromEdgeList(foundEdge);
		return foundEdge;
		
	}
	
	
	private NEdge getMostCollinearEdge(NEdge e, ArrayList<NEdge> connectedEdges) {
		// looks for the "most straight" edge to connect to
		connectedEdges.remove(e);
		
		NEdge bestEdge = null;
		float bestAngle = 10000;
		for(NEdge otherEdge: connectedEdges) {
			if( isInEdgeList(otherEdge) == false ) continue;
			float ang = angleBetweenEdges(e, otherEdge);
			if( ang < bestAngle) {
				bestAngle = ang;
				bestEdge = otherEdge;
			}
		}
		/// you can work out the most straight edge connection here
		return bestEdge;
	}
	
	
	
	
	boolean isCollinearWithinTollearance(NEdge e1, NEdge e2, float angleTolInDegrees) {
		//if(edgesConnect( e1, e2)== false) return false;
		float radiansBetween = angleBetweenEdges( e1,  e2);
		float degreesBetween = radiansBetween*57.2958f;
				
		if( ( MOMaths.isClose(degreesBetween, 0, angleTolInDegrees) || MOMaths.isClose(degreesBetween, 180, angleTolInDegrees) ) )  return true;
		return false;
	}
	
	boolean isInEdgeList(NEdge e) {
		return theEdgeList.contains(e);
	}
	
	
	private void popFromEdgeList(NEdge e) {
		theEdgeList.remove(e);
	}

}

///////////////////////////////////////////////////////////////////////////////////////////////
// this class uses the base class to implement the extraction of "connected" edge runs as Vertices2
// This can be done on the fly using MODE_EXTRACTED, or all the edge runs Vertices2's can be pre-calculated at start
// using MODE_PRE_EXTRACTED. IN either case, the network is traversed in line2 segments of a defined length crawlStepDistance,
// once the current Vertices2 has been exhausted, the next one is used, until the entire network has been traversed.

// once pr extracted you can process the vertices further (e.g. add fractal detail)


class NNetworkEdgeRunCrawler  extends NNetworkEdgeRunExtractor{
	static final int MODE_EXTRACTED = 0;
	static final int MODE_REGIONS = 1;
	static final int MODE_PRE_EXTRACTED = 2;
	int mode = MODE_EXTRACTED;
	
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
	int lineInRunCounter = 0;
	
	NNetworkEdgeRunCrawler(NNetwork ntwk){
		super(ntwk);
		regionIterator = new NNetworkItemIterator();
		regionIterator.setRegions(ntwk.getRegions());
	}
	
	void setMode(int m) {
		
	 mode = m;
	}

	//////////////////////////////////////////////////////////////////////////////
	// repeatedly calling updateCrawl will eventually traverse the entire extracted set of edges
	//
	//
	Line2 updateCrawl() {
		//System.out.println("updateCrawl: run count = " + getRunCount());
		if(currentVertices==null) {
			boolean result = nextEdgeRun();
			if(result == false) {
				System.out.println("updateCrawl: cannot get an initial edge run with current vertices");
				return null;
			}
		}
		//System.out.println("updateCrawl: currentVertices num = " + currentVertices.size());
		Line2 line = getNextCrawlLine();
		if(line == null) {
			// System.out.println("updateCrawl: finished previous crawl, getting new edge run");
			boolean result = nextEdgeRun();
			
			if(result == false) {
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
		lineInRunCounter++;
		return line;
		
	}
	
	int getRunCount() {
		// this can be used to identify which run a line belongs to
		return runCounter;
	}
	
	
	int getLineInRunCount() {
		return lineInRunCounter;
		
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// to extract a particular edge into currentVertices, call nextEdgeRun.
	// This returns true if there is another edge run found.
	// Then, you can inspect the edge run vertices, and interpolate over them by getting
	// getCurrentVertices()
	
	boolean nextEdgeRun() {
		boolean result = false;
		if(mode == MODE_EXTRACTED) result =  nextEdgeRun_Extracted() ;
		if(mode == MODE_REGIONS) result =  getNextEdgeRun_NextRegion() ;
		if(mode == MODE_PRE_EXTRACTED) result = getNextEdgeRun_PreExtracted() ;
		if(result==false) return false; // no more runs to be found
		
		float totalLen = currentVertices.getTotalLength();
		//System.out.println("nextEdgeRun : total currentVertices = " + currentVertices.size());
		float numSteps = (int)(totalLen/crawlStepDistance);
		if(numSteps == 0) numSteps = 1;
		crawlStepParametric = 1/numSteps;
		currentVerticesProgressParametric = 0;
		
		lineInRunCounter=0;
		runCounter++;
		return true;
	}
	
	
	
	
	
	boolean nextEdgeRun_Extracted() {
		ArrayList<NEdge> thisRun = extractEdgeRun_RandomStart();
		if(thisRun == null) return false;
		ArrayList<PVector> verts = getVertices(thisRun);
		currentVertices = new Vertices2(verts);
		return true;
	}
	
	
	
	boolean getNextEdgeRun_NextRegion() {
		NRegion nextRegion = regionIterator.getNextRegion();
		if(nextRegion == null) return false;
		ArrayList<PVector> v = nextRegion.getVertices();
		currentVertices = new Vertices2(v);
		currentVertices.close();
		return true;
	}
	
	boolean getNextEdgeRun_PreExtracted() {
		if(runCounter >= preExtractedEdgeRuns.size()) return false;
		currentVertices = preExtractedEdgeRuns.get(runCounter);
		return true;
	}
	
	
	ArrayList<Vertices2> preExtractEdgeRuns(){
		preExtractedEdgeRuns.clear();
		while(true) {
			boolean result = false;
			if(mode == MODE_EXTRACTED) result =  nextEdgeRun_Extracted() ;
			if(mode == MODE_REGIONS) result =  getNextEdgeRun_NextRegion() ;
			if(result==false) break;
			preExtractedEdgeRuns.add(currentVertices);
			
		}
		System.out.println("preExtractEdgeRuns: found " + preExtractedEdgeRuns.size() + " runs");
		runCounter = 0;
		currentVertices=null;
		return preExtractedEdgeRuns;
	}
	
	void sortPreExtractedEdgeRuns(boolean shortestFirst) {
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
	
	NRegion getCurrentRegion() {
		return regionIterator.getCurrentRegion();
	}
	
	
	void setCrawlStep(float s) {
		crawlStepDistance = s;
	}
	
	Line2 getNextCrawlLine() {
		// the crawl line lies between currentVerticesProgressParametric and currentVerticesProgressParametric+crawlStepParametric
		// it will return a consistent line length, as the division of total steps in the line is pre-calculated as an integer
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
	
	
	void displaceExtractedEdgeRuns(int pointdoubling, float wobble, BufferedImage displacementImage) {
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
	
	
	void drawExtractedEdgeRuns(Color c, int width) {
		
		int numEdgeRuns  = preExtractedEdgeRuns.size();
		for(int n = 0; n < numEdgeRuns; n++) {
			Vertices2 verts = preExtractedEdgeRuns.get(n);
			GlobalObjects.theDocument.drawVerticesWithPoints(verts,  c, width);
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









