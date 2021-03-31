import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;



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
		Line2 l1 = e1.line2;
		Line2 l2 = e2.line2;
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
	
////////////////////////////////////////////////////////////////////////////////////
//
//
	
	void draw() {
		
		
		ArrayList<NPoint> points = theNetwork.getPoints();
		for (int n = 0; n < points.size(); n++) {
			NPoint np = points.get(n);
			float radiusDocSpace = 0.001f;
			drawPoint( np, Color.RED, radiusDocSpace);
		}

		ArrayList<NEdge> edges = theNetwork.getEdges();
		for (int n = 0; n < edges.size(); n++) {
			NEdge e = edges.get(n);
			Color c = getEdgeColor(e);
			int lineWt = getEdgeLineWeight(e);
			drawEdge(e,c,lineWt);
		}

	}
	
	
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
	    return 2;
	  }

}





class NetworkEdgeMarcher extends NNetworkProcessor{
	
	ArrayList<NEdge> theEdgeList;
	
	boolean isStarted = false;
	boolean isFinished = false;
	
	PVector currentStrideEndPoint;
	float theMarchStride;
	NEdge currentEdge;
	NEdge runStartEdge;
	//ArrayList<NEdge> currentEdgeRunList;
	
	RandomStream randomStream = new RandomStream(6);
	
	
	NetworkEdgeMarcher(NNetwork ntwk){
		super(ntwk);
		
	} 
	
	
	void startEdgeMarch(float marchStride) {
		theMarchStride = marchStride;
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
		
		isStarted = true;
	}
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// This method returns an ordered list of connected edges, starting at a random
	// available edge. These edges are removed from the initial
	// edgeList, so cannot be re-used in any other search
	//
	ArrayList<NEdge> collectEdgeRun() {
		ArrayList<NEdge> currentEdgeRunList= new ArrayList<NEdge>();
		runStartEdge = findRandomStartEdge();
		if(runStartEdge == null) {
			// there are no more edges to be processed
			return null;
		}

		ArrayList<NEdge> edgesP1Direction = getConnectedRun(runStartEdge, runStartEdge.p1); 
		ArrayList<NEdge> edgesP2Direction = getConnectedRun(runStartEdge, runStartEdge.p2);

		if(edgesP1Direction!=null) {
			Collections.reverse(edgesP1Direction);
			currentEdgeRunList.addAll(edgesP1Direction);
		}
		if(runStartEdge!=null) currentEdgeRunList.add(runStartEdge);
		if(edgesP2Direction!=null) currentEdgeRunList.addAll(edgesP2Direction);
		
		/*
		int edgesP1DirectionSize = 0;
		int edgesP2DirectionSize = 0;
		if(edgesP1Direction!=null) edgesP1DirectionSize = edgesP1Direction.size();
		if(edgesP2Direction!=null)  edgesP2DirectionSize = edgesP2Direction.size();
		System.out.println("This run is composed of " + edgesP1DirectionSize + " + 1 + " + edgesP2DirectionSize);
		*/
		boolean result = checkEdgesAreOrdered(currentEdgeRunList);

		return currentEdgeRunList;
	}
	
	
	
	NEdge findRandomStartEdge() {
		if(theEdgeList == null) {
			System.out.println("NetworkEdgeMarcher:findAStartEdge edge = null");
			return null;
		}
		if(theEdgeList.size()==0) {
			System.out.println("NetworkEdgeMarcher:findAStartEdge no more edges found");
			isFinished = true;
			return null;
		}
		
		int ind = 0;
		int listSize = theEdgeList.size();
		if(listSize > 1) ind = randomStream.randRangeInt(0,theEdgeList.size()-1);
		
		NEdge runStartEdge =  theEdgeList.get(ind);
		popFromEdgeList(runStartEdge);
		return runStartEdge;
	}
	
	
	ArrayList<NEdge> getConnectedRun(NEdge startEdge, NPoint whichPoint) {
		
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
	
	
	
	NEdge getConnectedEdge(NEdge thisEdge, NPoint usingThisPoint) {
		//finds an single edge connected to either end of thisEdge
		NEdge foundEdge = null;
		//System.out.println("getBestConnection this edge ID = " + thisEdge.getID());
		ArrayList<NEdge> connectedEdges = (ArrayList)usingThisPoint.getEdgeReferences().clone();
		foundEdge = getBestConnection(thisEdge, connectedEdges);
		
		if(foundEdge==null) {
			//System.out.println("getConnectedEdge: no connecting edge found");
			return null;
		}
		popFromEdgeList(foundEdge);
		return foundEdge;
		
	}
	
	
	NEdge getBestConnection(NEdge e, ArrayList<NEdge> connectedEdges) {
		
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
	
	boolean isInEdgeList(NEdge e) {
		return theEdgeList.contains(e);
	}
	
	
	void popFromEdgeList(NEdge e) {
		theEdgeList.remove(e);
	}
	
	
	void testRun() {
		Color col;
		
		int totalEdges = theEdgeList.size();
		int numEdgesFound = 0;
		
		while(true) {
			ArrayList<NEdge> thisRun = collectEdgeRun();
			
			
			if(thisRun == null) break;
			
			int thisRunSize = thisRun.size();
			numEdgesFound += thisRunSize;
			//System.out.println("found run of " + thisRunSize + " total edges found" + numEdgesFound + " out of " + totalEdges);
			int r = randomStream.randRangeInt(20, 255);
			int g = randomStream.randRangeInt(20, 255);
			int b = randomStream.randRangeInt(20, 255);
			col = new Color(r,g,b);
			drawEdges(thisRun, col , 4);
			
		}
		
		
		
	}
	
	
	
	
	Line2 update() {
		
		// starting at currentStrideEndPoint, if the march end point is within this line
		//Line2 currentEdgeLine = 
		//if()
		return null;
	}
	
	
	
}

class NNetworkEdgeJoiner extends NNetworkProcessor{
	// loads a network
	// then iterates over the edges of each type of edge (a-road, b-road, river..)
	// defined by the search attribute
	// and joins up edges that are not dissimilar in angle
	// This class is destructive to the network, so do not save back over itself.
	
	// The process starts with an iterative search through the edge list until it finds
	// a matching type to the search attribute, that has not been processed - this is the candidate edge.
	// if a matching edge is found that can extend the candidate edge, then the matching edge end the current edge are effectively deleted, and replaced by a new merged edge
	// A new candidate edge is then selected. 
	// if the search returns a null, then the candidate edge is set to having been processed - this is either because there was no
	// connectable edge, or the edge is too long to connect to a new one.
	// A new candidate edge is selected.
	// if there are no more candidate edges, the search is complete.
	
	// this is in degrees
	float angleTollerance = 5;
	// this is in document space units i.e. 1 = long edge of document, 0.05 == 1/20th of this
	float maxLengthJoinedLines = 0.1f;
	
	
	
	RandomStream random = new RandomStream();   
	
	ArrayList<NEdge> currentEdges;
	
	NNetworkEdgeJoiner(NNetwork ntwk){
		super(ntwk);
	}
	
	
	
	
	
	void simplifyEdges(KeyValuePair searchCriteria, int bailCount) {
		setSearchAttribute(searchCriteria);
		gatherCurrentEdges();

		int num = currentEdges.size();
		System.out.println("NNetworkEdgeJoiner::simplifyEdges total number of edges of this type = " + num);

		int count = 0;
	    for(int n = 0; n < bailCount; n++) {
	    	
	    	NEdge ne = this.findStartEdge();
	    	
	    	if(ne==null) { 
	    		System.out.println("NNetworkEdgeJoiner::simplifyEdges cannot find any new edges after " + count);
	    		// this means there are no more start edges to find
	    		break;
	    	}
	    	
	    	
	    	NEdge ce = this.findConnectingEdge(ne);
	    	if( ce == null ) {
	    		// This edge has no more connecting edges, so mark as processed
	    		this.setAsProcessed(ne);
	    		//System.out.println("cannot find connecting edge " + count);
	    		count++;
	    		continue;
	    	}
	    	if( ne.isUsingIdenticalPoints(ce) ) {
	    		// rare, but sometimes the edge has been entered twice over itself
	    		deleteEdge(ce);
	    		continue;
	    	}
	    	
	    	this.connectEdges(ne, ce);
	    }
	    System.out.println("NNetworkEdgeJoiner::simplifyEdges out" + count);
	}
	
	void gatherCurrentEdges() {
		if(currentSearchAttribute==null) {
			System.out.println("Search attributes have not been set - everything will be selected");
			
		}
		currentEdges = getEdgesMatching(currentSearchAttribute);
	}
	

	void setTollerances(float angle, float maxLength) {
		angleTollerance = angle;
		maxLengthJoinedLines = maxLength;
	}
	
	NEdge findStartEdge() {
		
		for (int n = 0; n < currentEdges.size(); n++) {
			NEdge e = currentEdges.get(n);
			if(isMatchingSearchAttribute(e) && !hasBeenProcessed(e)) return e;
		}
		return null;
	}
	
	
	NEdge findConnectingEdge(NEdge thisEdge) {
		// this finds a connecting edge, that can be merged, at either end of the candidate edge
		NEdge otherEdge = findConnectedEdge( thisEdge, 0);
		
		if(otherEdge == null)  otherEdge = findConnectedEdge( thisEdge, 1);
		return otherEdge;
	}
	
	NEdge findConnectedEdge(NEdge thisEdge, int end) {
		// this finds a connecting edge, that can be merged, at one end of the candidate edge
		//System.out.println("findConnecting edge " + end);
		NPoint np = thisEdge.getEndNPoint(end);
		ArrayList<NEdge> connectedEdges = np.getEdgeReferences();
		
		for(NEdge otherEdge: connectedEdges) {
			//System.out.println("here 1");
			if(thisEdge == otherEdge) continue;
			//System.out.println("here 2");
			if(isMatchingSearchAttribute(otherEdge) == false) continue;
			//System.out.println("here 3");
			if(hasBeenProcessed(otherEdge)) continue;
			//System.out.println("here 4");
			if(isLinedUp(thisEdge, otherEdge, angleTollerance) == false) continue;
			//System.out.println("here 5");
			if(isMergedTooLong(thisEdge, otherEdge,  maxLengthJoinedLines)) {
				return null;
			} else {
				//System.out.println("here 6");
				return otherEdge;
			}
		}
		//System.out.println("here 7");
		return null;
	}
	
	
	
	
	NEdge connectEdges(NEdge e1, NEdge e2){
		// should have already been tested that these two edges do indeed connect
		// but just to be safe we check the results of the unconnectedEnds
		// so these two edges can be connected
		// returns the edge just in case you need it
		
		if(e1.isUsingIdenticalPoints(e2)) {
			//theNetwork.deleteEdge(e2);	
			return null;
		}
		
		
		NPoint[] endPts = getUnconnectedEnds(e1, e2);
		if(endPts[0] == null || endPts[1] == null)	 return null;
		if(endPts[0] == endPts[1]) return null;
		
		
		
		KeyValuePairList attr1 = e1.getAttributes().copy();
		deleteEdge(e1);
		deleteEdge(e2);		
		
		NEdge newEdge =  addEdge(endPts[0], endPts[1]);
		//setCurrentEdges();
		if(newEdge == null) return  null;
		newEdge.setAttributes(attr1);
		//setAsJoined(newEdge);
		return newEdge;
	}
	
	NEdge addEdge(NPoint np1, NPoint np2) {
		NEdge newEdge =   theNetwork.addEdge(np1,np2);
		currentEdges.add(newEdge);
		return newEdge;
	}
	
	void deleteEdge(NEdge e) {
		currentEdges.remove(e);
		theNetwork.deleteEdge(e);
	}
	
	
	
	
	NPoint[] getUnconnectedEnds(NEdge e1, NEdge e2) {
		if(e1 == e2) System.out.println("getUnconnectedEnds: identical edges error");
		NPoint[] ends = new NPoint[2];
		
		
		NPoint joinPt = getEdgeJoinPoint(e1,e2);
		ends[0] = getOtherEdgeEnd(e1,joinPt);
		ends[1] = getOtherEdgeEnd(e2,joinPt);
		
		if(ends[0] == ends[1]) {
			System.out.println("getUnconnectedEnds: returning identical points error" + e1.getID() + " " + e2.getID());
		}
		
		return ends;
	}
	
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
		Line2 l1 = e1.line2;
		Line2 l2 = e2.line2;
		return l1.getAngleBetween(l2);
	}
	
	
	boolean isLinedUp(NEdge e1, NEdge e2, float angleTol) {
		//if(edgesConnect( e1, e2)== false) return false;
		float radiansBetween = angleBetweenEdges( e1,  e2);
		float degreesBetween = radiansBetween*57.2958f;
				
		if( ( MOMaths.isClose(degreesBetween, 0, angleTol) || MOMaths.isClose(degreesBetween, 180, angleTol) ) )  return true;
		return false;
	}
	
	boolean isMergedTooLong(NEdge e1, NEdge e2, float cumulativeLengthMax) {
		
		float len1 = e1.getLength();
		float len2 = e2.getLength();
		
		if( (len1 + len2) > cumulativeLengthMax) return true;
		
		return false;
	}
	
	
	Color getEdgeColor(NEdge ne){
	    
		if( hasBeenProcessed(ne) ) {
			int r = random.randRangeInt(0, 255);
			int g = random.randRangeInt(0, 255);
			int b = random.randRangeInt(0, 255);
			return  new Color(r,g,b);
		}
		
		
	    if(isMatchingSearchAttribute(ne)){
	      return new Color(255,100,100);
	    }
		
		return new Color(240,240,240);
	    //return super.getEdgeColor(ne);
	}
}
