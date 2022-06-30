package MONetwork;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

import MOApplication.Surface;
import MOCompositing.RenderTarget;
import MOImage.MOColor;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;
import MOUtils.SortObjectWithValue;

//////////////////////////////////////////////////////////////////////////////////////
// 
// This network processor finds all the implicit regions from the list of edges
// It works with a copy of the network passed in. It then adds them to the network as NRegions.
// 
// All the regions are found and stored in the copy of the original network (i.e. is non-destructive to the input network) that can be retrieved using getNetworkWithFoundRegions()
// or a separate ArrayList of sorted found regions can be retrieves using getRegionsSortedByArea()
//
// Notes on implementation:-
// At the start a copy of the input NNetwork is made, so that it is not destructive to the input NNetwork.
// BEFORE regions are extracted, the user may want to impose a rectangular boundary of border-edges to the network. This is done using addOuterBoundaryEdges(Rect boundaryRect);
//
// Before it can start finding regions, it needs to remove "dangling" edges - i.e. those edges not in a loop but maybe in a run.
// Also accidental co-incident edges need to be removed. It also removes any previously stored regions so the algorithm return only those regions found by the algorithm.
//
// Depending on the orientation of the edge, and the nature of the loop to be found it is not possible to determine whether the search is in a Clockwise or Anti clockwise fashion; 
// you only know after the complete loop has been found, then it is too late. So search may happen in either winding. The following system copes with this.
// Edges contain references to up to 2 regions that they are associated with. This can be returned as a count - 0== no associated regions, 1 == 1 associated region etc.
//
// In pass 0 connectingEdges with no Associated Region Count (ARC) are added to the list of edges making this loop using clockwise-most connecting edge search. Once a loop has been found, all the edges' ARC are incremented. 
// The start edge is added to a startEdgeList, this has 2 purposes; 
// 1 so that if we bail on this edge, we do not attempt the same edge again, and 2/ the start Edge list is used in the second pass (pass 1) to search using the p2 direction to find many missing loops from the first search.
//
// In pass 1 the the startEdge list is used to get the same start edges as in pass 1, but direct the search the other way (using p2 direction of the statEdge) to find loops in the other direction. 
// In this pass connecting edges with any ARC are permitted.
//  
// In pass 2 there are some scattered non-regions with residual edges with an ARC of 1. This pass mops them up.
//
// This seems to get about 99.9% of regions. The reason that some regions are left unfound is that these region has some edges with an ARC of 2, due to finding larger surrounding regions. TBD. These might be mopped up later.
// This algorithm is purely deterministic with one conclusion. 
// 

 public class NNetworkRegionFinder{

	NNetwork theNetwork;
	public NNetworkDrawer networkDrawer;
	
	
	ArrayList<NEdge> startEdgeList = new ArrayList<NEdge>();
	
	int startEdgeListCounter = 0;
	
	// just for debugging
	int foundRegionCount = 0;
	Range largestRegion = new Range();
	
	KeyValuePair documentEdgeAttribute = new KeyValuePair("REGIONEDGE", "document");
	
	int parkCounter = 0;
	int riverCounter = 0;
	int lakeCounter = 0;
	
	public NNetworkRegionFinder(NNetwork ntwk, KeyValuePairList searchCriteria){
		// for debug only
		largestRegion.initialiseForExtremaSearch();
		
		theNetwork = ntwk.copy();

		if(searchCriteria != null) {
			theNetwork.setSearchAttribute(searchCriteria);
			ArrayList<NEdge> toRemove = theNetwork.getEdgesMatchingSearchAttributes(false);
			theNetwork.removeEdges(toRemove);
		}
		networkDrawer = new NNetworkDrawer(theNetwork);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	// This is the big algorithm that finds all the regions according to the method described at the top of the class
	//
	public void findAllRegions() {	
		
		removePreExisitingRegions();
		removeAllDanglingEdges();
		removeCoincidentEdges();
		System.out.println("NNetworkAutoRegionFinder: finding regions please wait...");
		
		// using sequentially found start-edge with no associated regions
		findRegions(0);
		
		// using same start-edges as above, but winding the other way
		findRegions(1);
		
		// mopping up those remaining regions with ARCs == 1
		findRegions(2);
		
		/// now make all the region vertices clockwise
		NRegion bigRegion = null;
		for (NRegion nr : theNetwork.regions) {
				nr.getVertices().setPolygonWindingDirection(Vertices2.CLOCKWISE);
				
				if(nr.getVertices().getArea()> 0.9f) {
					bigRegion = nr;
				}
		}
		
		
		// and finally remove the big outer region
		if(bigRegion != null) {
			theNetwork.deleteRegion(bigRegion);
		}
		
	} 
	
	public NNetwork getNetworkWithFoundRegions() {
		return theNetwork;
	}
	
	public ArrayList<NRegion> getFoundRegions(){
		return theNetwork.regions;
	}
	
	public ArrayList<NRegion> getFoundRegionsSortedByArea(boolean smallestFirst) {
		// if smallestFirst == true, the regions are sorted with smallest regions first
		// if smallestFirst == false, the regions are sorted with largest regions first
		SortObjectWithValue objectValueSorter = new SortObjectWithValue();
		
		for(NRegion nr : theNetwork.regions) {
			float area = nr.getVertices().getArea();
			
			objectValueSorter.add(nr,area);
		}

		if(smallestFirst) {
			return objectValueSorter.getSorted();
		} else {
			return objectValueSorter.getReverseSorted();
		}

	}
	
	public void addOuterBoundaryEdges(Rect boundaryRect, boolean deleteOuterItems) {
		NNetworkAddBoundaryEdges addEdges = new NNetworkAddBoundaryEdges(theNetwork);
    	addEdges.addOuterBoundaryEdges(boundaryRect, deleteOuterItems);
    	theNetwork = addEdges.getNetworkWithAddedBoundary();
	}
	

	///////////////////////////////////////////////////////////////////////////////////////
	// addOuterBoundaryEdges
	// New edges are added to the NNetwork on the precise addOuterBoundaryEdges rect before the region search is undertaken. These are "welded" to the existing network 
	// edges with Attribute REGIONEDGE: "Document". One obvious use it to use the documentRect as the boundary.
	// These are useful for finding the outer areas of the network that would not otherwise be complete regions, but composed of dangling edges, so left blank.
	// Method - 
	//		add 4 new document edges to the NNetwork representing the sides of the boundary rect.
	//	    find all the intersections of the exiting network with the new document edges - store them in allIntersectionsList
	//      get the first edge in the allIntersectionsList - thisEdge
	// 		get all the documentEdges in another list i.e. all edges with attribute "REGIONEDGE", "document" - documentEdgesList (this is 4 at first but will increase with each "weld")
	// 		find the documentEdge in the documentEdgesList that intersects with thisEdge
	//		Weld(thisEdge, intersectingDocumentEdge) - this will split both edges to make 4 new edges connected to the intersection point
	//	    remove thisEdge from the allIntersectionsList
	//      continue until allIntersectionsList is empty.
	// addOuterBoundaryEdges
	//
	//
	//
	
	/*
	public void addOuterBoundaryEdges(Rect boundaryRect) {
		
		// first create the new boundary edges
		NPoint topLeft = new NPoint(boundaryRect.getTopLeft(),theNetwork);
		theNetwork.addPoint(topLeft);
		
		NPoint topRight = new NPoint(boundaryRect.getTopRight(),theNetwork);
		theNetwork.addPoint(topRight);
		
		NPoint bottomRight = new NPoint(boundaryRect.getBottomRight(),theNetwork);
		theNetwork.addPoint(bottomRight);
		
		NPoint bottomLeft = new NPoint(boundaryRect.getBottomLeft(),theNetwork);
		theNetwork.addPoint(bottomLeft);
		
		
		NEdge topEdge = theNetwork.addEdge(topLeft, topRight);
		topEdge.addAttribute(documentEdgeAttribute);
		NEdge rightEdge = theNetwork.addEdge(topRight, bottomRight);
		rightEdge.addAttribute(documentEdgeAttribute);
		NEdge bottomEdge = theNetwork.addEdge(bottomRight, bottomLeft);
		bottomEdge.addAttribute(documentEdgeAttribute);
		NEdge leftEdge = theNetwork.addEdge(bottomLeft,topLeft);
		leftEdge.addAttribute(documentEdgeAttribute);
		
		
		// get all the intersections of the network with these edges
		ArrayList<NEdge> allIntersectionsList = new ArrayList<NEdge>();
		allIntersectionsList.addAll(findIntersections(topEdge));
		allIntersectionsList.addAll(findIntersections(rightEdge));
		allIntersectionsList.addAll(findIntersections(bottomEdge));
		allIntersectionsList.addAll(findIntersections(leftEdge));
		
		
		// so now we have all the intersecting edges with the documentEdges
		int bailCount = 0;

		while( edgesToWeld(allIntersectionsList) ) {
			bailCount++;
			if(bailCount>1000000) {
				System.out.println("bail count exceeded");
				break;
			}
			
		}
		
		
		theNetwork.refreshIDs();
	}
	
	boolean edgesToWeld(ArrayList<NEdge> allIntersectionsList) {
		
		if(allIntersectionsList.size()<1) return false;
		NEdge thisEdge = allIntersectionsList.get(0);
		
		ArrayList<NEdge> documentEdges = getAllDocumentEdges();
		//System.out.println("edgesToWeld documentEdges " + documentEdges.size());
		if(documentEdges.size() == 0) return false;
		for(NEdge docEdge: documentEdges) {
			if( isIntersection(thisEdge, docEdge) ) {
				weldEdges(thisEdge, docEdge);
				allIntersectionsList.remove(thisEdge);
				//System.out.println("edgesToWeld welded edges still to process " + allIntersectionsList.size());
				return true;
			}
		}
		
		return true;
	}
	
	
	
	ArrayList<NEdge> getAllDocumentEdges(){
		ArrayList<NEdge> documentEdges = new ArrayList<NEdge>();
		
		ArrayList<NEdge> allEdges = theNetwork.getEdges();
		// find all the possible lines intersecting
		for(NEdge e: allEdges) {
			if( e.getAttributes().containsEqual(documentEdgeAttribute)) {
				documentEdges.add(e);
			}
		}
		
		return documentEdges;
	}
	
	
	ArrayList<NEdge> getAllNonDocumentEdges(){
		ArrayList<NEdge> allEdges = (ArrayList<NEdge>) theNetwork.getEdges().clone();
		
		ArrayList<NEdge> docEdges = getAllDocumentEdges();
		allEdges.removeAll(docEdges);
		return allEdges;
	}
	
	
	
	ArrayList<NEdge> findIntersections(NEdge theEdge) {
		// first find all the intersecting edges
		ArrayList<NEdge> allEdges = getAllNonDocumentEdges();
		ArrayList<NEdge> intersectingEdges = new ArrayList<NEdge>();

		// find all the possible lines intersecting
		for(NEdge e: allEdges) {
			//if( e.getAttributes().containsEqual(documentEdgeAttribute)) continue;
			if( isIntersection(theEdge,e) ) {
				intersectingEdges.add(e);
			}
		}
		//System.out.println("findAllWelds found " + intersectingEdges.size() + " intersecting edges");
		
		return intersectingEdges;
		
		
	}
	
	
	boolean isIntersection(NEdge e1, NEdge e2) {
		Line2 l1 = e1.getLine2();
		Line2 l2 = e2.getLine2();
		//if( l1.isIntersectionPossible(l2)== false) return false;
		boolean result =  l1.calculateIntersection(l2);	
		//System.out.println("line1 " + l1.toStr() + " line2 " + l2.toStr() + " intersection = " + result);
		return result;
	}
	
	void weldEdges(NEdge e1, NEdge e2) {
		// assumes that they have already been determined as intersecting
		PVector intersectionPoint = e1.getLine2().getIntersectionPoint(e2.getLine2());
		if(intersectionPoint == null) return;
		
		NPoint newSplitPoint = splitEdge(e1, intersectionPoint);
		splitEdge(e2,  newSplitPoint);
		
	}
	
	NPoint splitEdge(NEdge oldEdge, PVector splitPoint){
	    if( oldEdge == null || splitPoint == null) return null;
	    NPoint newSplitPoint = new NPoint(splitPoint, theNetwork);
	    splitEdge( oldEdge,  newSplitPoint);
	    return newSplitPoint;
	}
	    
	void splitEdge(NEdge oldEdge, NPoint newSplitPoint) {  
		if( oldEdge == null || newSplitPoint == null) return;
	    NPoint np1 = oldEdge.getEndNPoint(0);
	    NPoint np2 = oldEdge.getEndNPoint(1);
	    
	    KeyValuePairList attr1 = oldEdge.getAttributes().copy();
	    KeyValuePairList attr2 = oldEdge.getAttributes().copy();
	    theNetwork.addPoint(newSplitPoint);
	    
	    // give the new edges a copy of the old one's attributes
	    // deep copy is required to avoid reference sharing
	    NEdge newEdge1 = theNetwork.addEdge(np1, newSplitPoint);
	    newEdge1.setAttributes(attr1);
	    NEdge newEdge2 = theNetwork.addEdge(newSplitPoint, np2);
	    newEdge2.setAttributes(attr2);
	    updateDependentRegions_SplitEdge( oldEdge,  newEdge1,  newEdge2);
	    theNetwork.deleteEdge(oldEdge);
	  }
	
	// not sure we need to do this as this process happens before any regions are found
	void updateDependentRegions_SplitEdge(NEdge oldEdge, NEdge newEdge1, NEdge newEdge2){
	    ArrayList<NRegion> regions = theNetwork.getRegions();
	    
	    for(NRegion reg: regions){
	      reg.splitEdge( oldEdge,  newEdge1,  newEdge2);
	    }
	  }
	*/
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// Specifying the Attributes of automatically found regions. 
	// A once defined region will have one REGIONTYPE key, (if not, then it has not been defined).
	// Defining an automatically found region can be done by two methods:-
	// Explicit - where region-markers are placed within regions to identify their type. Region-markers are NPoints placed within the region they
	// wish to identify, and must have the Attribute ("REGIONMARKER" , regionType) where regionType is another string e.g. "LAKE".
	// Implicit - via some algorithm based on properties of the of the network or other data - e.g. density of points, using a key image
	// Only the explicit method setRegionAttributeWithRegionMarker() is part of this class. Because implicit methods could be almost any process, they are not contained within this class;
	// use the network helper instead
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// if a region contains a region marker, then the region has the regionAttribute added
	//
	//
	public void setRegionAttributeWithRegionMarker(String regionType) {
		// first find all the markers with marker attribute
		// does not copy network into current network as you want the network passed in to be permanently affected
		KeyValuePair markerDescriptor = new KeyValuePair("REGIONMARKER", regionType);
		KeyValuePair newRegionAttribute = new KeyValuePair("REGIONTYPE", regionType);

		ArrayList<NPoint> markers = theNetwork.getPointsMatchingQuery(markerDescriptor);
		System.out.println("setRegionAttributeWithRegionMarker:: found " + markers.size() + " markers matching " + regionType);
		// next loop through the markers finding the containing regions
		// once found, add the regionAttributeToAdd to the region
		int n = 0;
		for(NPoint marker: markers) {

			NRegion r = theNetwork.getNearestNRegion(marker.getPt());
			if(r != null) {
				r.addAttribute(newRegionAttribute);
				n++;
			}
		}
		
		System.out.println("setRegionAttributeWithRegionMarker:: found " + n + " regions matching " + regionType);
		
	}

	

	///////////////////////////////////////////////////////////////////////////////////////
	// all below is private apart from the debug methods at the bottom
	//
	
	private void findRegions(int pass) {

		if(pass==0 || pass==2) {
			// we reuse this afresh
			startEdgeList = new ArrayList<NEdge>();
		}
		foundRegionCount = 0;
		int bailCount=0;
		while(findRegion(pass)) {
			bailCount++;
			//if(pass==2)System.out.println("NNetworkAutoRegionFinder: findRegions found " + bailCount + " pass " + pass);
			if(bailCount > 1000000) {
				System.out.println("NNetworkAutoRegionFinder: findRegions seems stuck in a loop");
				return;
			}
		}
		System.out.println("NNetworkAutoRegionFinder: findRegions found " + foundRegionCount + " regions on pass " + pass);
	}

	boolean findRegion(int pass) {

		ArrayList<NEdge> thisRegionEdges = new ArrayList<NEdge>();
		
		NEdge startEdge  = getStartEdge(pass);

		if(startEdge==null) {
			// assume you have found and processed all the edges
			// System.out.println("NO more regions!");
			return false;
		}
		
		
		thisRegionEdges.add(startEdge);
		NEdge thisEdge = startEdge;
		
		// Establish the direction of the search in this pass
		// this is important as it sets the direction of the search of this loop. The other pass uses this same start edge but goes
		// in the opposite direction, but with the same winding rule, so should find the "other region" of this edge, if there is one.
		NPoint startPoint = thisEdge.p1;
		NPoint thisEndPoint = thisEdge.p2;
		if(pass==1 ) {
			startPoint = thisEdge.p2;
			thisEndPoint = thisEdge.p1;
		}
		
		
		int bailCount = 0;
		while(bailCount < 2000) {
			bailCount++;
			
			NEdge connectedEdge;
			if(pass<2) {
				connectedEdge =  getMostClockwiseConnectedEdge(thisEdge, thisEndPoint);
			} else {
				connectedEdge =  getConnectedEdgeWithARC1(thisEdge, thisEndPoint);
			}

			if(connectedEdge == null) {
				
				return true;
			}

			thisRegionEdges.add(connectedEdge);
			
			NPoint connectedEdgefarPoint = connectedEdge.getOtherPoint(thisEndPoint);
			
			
			//System.out.println("NNetworkAutoRegionFinder: findRegion connected edge " + connectedEdge.getID() + " start ID " + startEdgeID);
			if( connectedEdgefarPoint == startPoint ) {
				//if(pass==2)System.out.println("Found complete region " + foundRegionCount++);
				///////////////////////////////////////////////
				// this where the region is added to the network
				boolean result = theNetwork.tryCreateRegion(thisRegionEdges);
				if(result==false) {
					// failed to create a region.
					return true;
				}
				// all is good, a region has been formed
				foundRegionCount++;
				
				return true;	
			}
			
			//thisEndPoint = getFarPointOfEdge2(thisEdge, connectedEdge);  // 
			thisEndPoint =  thisEdge.getFarPointOtherEdge(connectedEdge);
			thisEdge = connectedEdge;
		}
		//if(pass==2)System.out.println("find region bail count reached");
		return true;


	}
	

   


	NEdge getStartEdge(int pass) {
		if(pass==0) return getVirginEdge();
			
		if(pass==1) return getEdgeFromStartEdgeList();
		
		if(pass==2) return getRemainingStartEdgeWith1ARC();
		
		// should never get here
		return null;
		
	}

	NEdge getVirginEdge() {
		// for pass 0
		// looks through the list of edges to find an edge with Associate Region Count == 0
		for(NEdge e: theNetwork.edges) {
			if( e.getAssociatedRegionCount()==0 && startEdgeList.contains(e)==false ) {
				startEdgeList.add(e);
				return e;
			}
		}
		// must have exhausted the list
		return null;
	}
	
	NEdge getEdgeFromStartEdgeList() {
		// for pass 1
		// uses the start-edges found in pass 0, but winds the search the other way
		for(int n = startEdgeListCounter; n < startEdgeList.size(); n++) {
			NEdge e = startEdgeList.get(n);
			if( e.getAssociatedRegionCount()==0 || e.getAssociatedRegionCount()==1 ) {
				startEdgeListCounter = n + 1;
				return e;
			}
		}
		// must have exhausted the list
		return null;
	}
	
	NEdge getRemainingStartEdgeWith1ARC() {
		// for pass 2
		// finds the remaining regions with only Associate Region Count == 1
		// a lot of these searches will be on junk data (dangling edges) that will never make a region, so we need to 
		// preclude this start edge being used again
		for(NEdge e: theNetwork.edges) {
			if( e.getAssociatedRegionCount()==1 && startEdgeList.contains(e)==false) {
				startEdgeList.add(e);
				return e;
			}
		}
		// must have exhausted the list
		return null;
	}

	private NEdge getMostClockwiseConnectedEdge(NEdge thisEdge, NPoint usingThisPoint) {
		ArrayList<NEdge> connectedEdges = thisEdge.getConnectedEdges(usingThisPoint);
		
		//if(pass==2) return findEdgeWithARC1(connectedEdges);
		
		if(connectedEdges==null) System.out.println("getMostClockwiseConnectedEdge:NULL1");
		if(thisEdge==null) System.out.println("getMostClockwiseConnectedEdge:NULL2");
		ArrayList<NEdge> sortedEdges = sortConnectedEdgesByClockwiseAngle(thisEdge, connectedEdges);
		
		int size = sortedEdges.size();
		if(size==0) return null;
		
	    return sortedEdges.get(0);
	}
	
	
	private NEdge getConnectedEdgeWithARC1(NEdge thisEdge, NPoint usingThisPoint) {
		ArrayList<NEdge> connectedEdges = thisEdge.getConnectedEdges(usingThisPoint);
		
		for(NEdge e: connectedEdges) {
    		if(e.getAssociatedRegionCount()==1) {
    			return e;
    		}
    	}
    	return null;
	}

	ArrayList<NEdge> sortConnectedEdgesByClockwiseAngle(NEdge referenceEdge, ArrayList<NEdge> connectedEdges) {
		
		SortObjectWithValue objectValueSorter = new SortObjectWithValue();
		
		for(NEdge e: connectedEdges) {
			//float rads = angleBetweenEdges(referenceEdge, e);
			float rads = referenceEdge.getHingedAngleBetween(e);
			float degs = (float) Math.toDegrees(rads);
			objectValueSorter.add(e,degs);
		}
		
		return objectValueSorter.getSorted();
	}

	//////////////////////////////////////////////////////////////////////////////
	// Preparation work before the region finding can take place.
	// 
	// Dangling Edge removal, identical edge removal, and removing previously existing regions
	//
	
	// iteratively erodes all dangling tips from all sequences of edges
	private void removeAllDanglingEdges() {
		while(true) {
			int danglingEdgesFoundAndRemoved = removeDanglingEdges();
			//System.out.println("removeAllDanglingEdges: danglingEdgesFoundAndRemoved " + danglingEdgesFoundAndRemoved);
			if(danglingEdgesFoundAndRemoved==0) break;
		}
	}


	// removes all dangling edges and
	// returns the number found. If the result is 0, then all dangling
	// edges have been removed.
	private int removeDanglingEdges() {
		ArrayList<NEdge> eList = theNetwork.getEdges();
		ArrayList<NEdge> toRemove = new ArrayList<NEdge>();
		int found = 0;
		for(NEdge e: eList) {
			if(  isDanglingEdge(e)  ) {
				toRemove.add(e);
				found++;
			} 
		}
		theNetwork.removeEdges(toRemove);
		return found;
	}


	boolean isDanglingEdge(NEdge e) {
		int numConnectedP1 = e.p1.getEdgeReferences().size();
		int numConnectedP2 = e.p2.getEdgeReferences().size();

		if(numConnectedP1 == 1 || numConnectedP2 == 1) return true;
		return false;

	}
	
	private void removePreExisitingRegions(){
		theNetwork.regions.clear();
		for(NEdge e: theNetwork.edges) {
			e.region1 = null;
			e.region2 = null;
		}
	}
	
	
	private void removeCoincidentEdges() {
		ArrayList<NEdge> eList = theNetwork.getEdges();
		ArrayList<NEdge> toRemove = new ArrayList<NEdge>();
		for(NEdge e: eList) {
			
			ArrayList<NEdge> connectedEdges = e.getAllConnectedEdges();
			
			for(NEdge connected: connectedEdges) {
				if(e.isUsingIdenticalPoints(connected)) {
					toRemove.add(connected);
				}
			}

		}
		
		//System.out.println("removed " + toRemove.size() + " identical edges");
		theNetwork.removeEdges(toRemove);
	}

	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// debug methods - can be removed upon finalisation
	//
	
	
	public void drawEdgesRandomColorWithPoints(RenderTarget rt) {
		// for debug only
		
		
		NNetworkDrawer drawer = new NNetworkDrawer(theNetwork);
		
		for(NEdge e: theNetwork.edges) {
			Color c = MOColor.getRandomRGB();
			
				
			drawer.drawEdge(e, c, 20, rt);
			
		}
		
		for(NPoint p: theNetwork.points) {
			
			drawer.drawPoint(p, Color.RED, 0.001f, rt);
		}
		
		printEdgePointNums(" in draw edges ");
		
	}
	
	void printEdgePointNums(String message) {
		// for debug only
		System.out.println(message + " Num edges " + theNetwork.edges.size() + " Num Points " + + theNetwork.points.size());
	}
	
	public void drawEdgeRegionStatus(RenderTarget rt) {
		// for debug only
		NNetworkDrawer drawer = new NNetworkDrawer(theNetwork);
		printEdgePointNums(" in drawEdgeRegionStatus ");
		for(NEdge e: theNetwork.edges) {
			Color c = Color.YELLOW;
			int n = e.getAssociatedRegionCount();
			if(n == 0) c = Color.red;
			if(n == 1) c = Color.blue;
			if(n == 2) c = Color.black;
				
				drawer.drawEdge(e, c, 20, rt);
		}
		
		
	}
	
	
	
	public void drawResult(RenderTarget rt) {
		// for debug only
		
		ArrayList<NRegion> regions = theNetwork.getRegions();
		
		Color[] cols = MOColor.getBasic12ColorPalette();
		//Color.BLACK;
		//Color.RED;
		//Color.GREEN;
		//Color.CYAN;
		//Color.DARK_GRAY;
		//Color.MAGENTA;
		//Color.YELLOW;
		//Color.BLUE;
		//Color.LIGHT_GRAY;
		//Color.ORANGE;
		//Color.GRAY;
		//Color.PINK;
		
		int colNum = 0;
		for(NRegion r: regions) {
	    
	    	
			networkDrawer.drawRegionFill(r, cols[colNum++], rt);
			if(colNum>10) colNum = 0;
		}
		
		for(NRegion r: regions) {
			networkDrawer.drawRegionEdges(r, Color.blue, 5, rt);
		}
		networkDrawer.draw(rt, Color.WHITE);
		//theNetwork.save("C:\\simon\\Artwork\\MouseOrgan4\\Maps\\Network Maps\\London Flowers\\regionFindTest.csv");
	}

	
	public void drawRegionsByType(RenderTarget rt) {
		// for debug only
		
		ArrayList<NRegion> regions = theNetwork.getRegions();
		
		
		
		int colNum = 0;
		for(NRegion r: regions) {
			Color c = getRegionDefaultColour(r);
	    	
			networkDrawer.drawRegionFill(r, c, rt);
			if(colNum>10) colNum = 0;
		}
		
		
		System.out.println("drawn " + parkCounter + " park regions ," + lakeCounter + " lake regions ," + riverCounter + " river regions ");
	}

	
	public Color getRegionDefaultColour(NRegion r) {
		// for debug only
		Color c = Color.WHITE;
		
		KeyValuePair parkAttribute = new KeyValuePair("REGIONTYPE", "PARK");
		if(r.thisItemContainsMatch(parkAttribute)) {
			parkCounter++;
			return Color.green;
		}
		KeyValuePair lakeAttribute = new KeyValuePair("REGIONTYPE", "LAKE");
		if(r.thisItemContainsMatch(lakeAttribute)) {
			lakeCounter++;
			return Color.CYAN;
		}
		KeyValuePair riverAttribute = new KeyValuePair("REGIONTYPE", "RIVER");
		if(r.thisItemContainsMatch(riverAttribute)) {
			riverCounter++;
			return Color.blue;
		}
		
		KeyValuePair densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_HIGH");
		if(r.thisItemContainsMatch(densityAttribute)) {
			riverCounter++;
			return Color.red;
		}
		densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_MEDIUM");
		if(r.thisItemContainsMatch(densityAttribute)) {
			riverCounter++;
			return Color.orange;
		}
		densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_LOW");
		if(r.thisItemContainsMatch(densityAttribute)) {
			riverCounter++;
			return Color.yellow;
		}
		
		return c;
	}
	
}
 
 /*
  * 
  * 
  * 
  public class NNetworkRegionFinder{

	NNetwork theNetwork;
	public NNetworkDrawer networkDrawer;
	
	
	ArrayList<NEdge> startEdgeList = new ArrayList<NEdge>();
	
	int startEdgeListCounter = 0;
	
	// just for debugging
	int foundRegionCount = 0;
	Range largestRegion = new Range();
	
	KeyValuePair documentEdgeAttribute = new KeyValuePair("REGIONEDGE", "document");
	
	int parkCounter = 0;
	int riverCounter = 0;
	int lakeCounter = 0;
	
	public NNetworkRegionFinder(NNetwork ntwk, KeyValuePairList searchCriteria){
		// for debug only
		largestRegion.initialiseForExtremaSearch();
		
		theNetwork = ntwk.copy();

		if(searchCriteria != null) {
			theNetwork.setSearchAttribute(searchCriteria);
			ArrayList<NEdge> toRemove = theNetwork.getEdgesMatchingSearchAttributes(false);
			theNetwork.removeEdges(toRemove);
		}
		networkDrawer = new NNetworkDrawer(theNetwork);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	// This is the big algorithm that finds all the regions according to the method described at the top of the class
	//
	public void findAllRegions() {	
		
		removePreExisitingRegions();
		removeAllDanglingEdges();
		removeCoincidentEdges();
		System.out.println("NNetworkAutoRegionFinder: finding regions please wait...");
		
		// using sequentially found start-edge with no associated regions
		findRegions(0);
		
		// using same start-edges as above, but winding the other way
		findRegions(1);
		
		// mopping up those remaining regions with ARCs == 1
		findRegions(2);
		
		/// now make all the region vertices clockwise
		NRegion bigRegion = null;
		for (NRegion nr : theNetwork.regions) {
				nr.getVertices().setPolygonWindingDirection(Vertices2.CLOCKWISE);
				
				if(nr.getVertices().getArea()> 0.9f) {
					bigRegion = nr;
				}
		}
		
		
		// and finally remove the big outer region
		if(bigRegion != null) {
			theNetwork.deleteRegion(bigRegion);
		}
		
	} 
	
	public NNetwork getNetworkWithFoundRegions() {
		return theNetwork;
	}
	
	public ArrayList<NRegion> getFoundRegions(){
		return theNetwork.regions;
	}
	
	public ArrayList<NRegion> getFoundRegionsSortedByArea(boolean smallestFirst) {
		// if smallestFirst == true, the regions are sorted with smallest regions first
		// if smallestFirst == false, the regions are sorted with largest regions first
		SortObjectWithValue objectValueSorter = new SortObjectWithValue();
		
		for(NRegion nr : theNetwork.regions) {
			float area = nr.getVertices().getArea();
			
			objectValueSorter.add(nr,area);
		}

		if(smallestFirst) {
			return objectValueSorter.getSorted();
		} else {
			return objectValueSorter.getReverseSorted();
		}

	}
	

	

	///////////////////////////////////////////////////////////////////////////////////////
	// addOuterBoundaryEdges
	// New edges are added to the NNetwork on the precise addOuterBoundaryEdges rect before the region search is undertaken. These are "welded" to the existing network 
	// edges with Attribute REGIONEDGE: "Document". One obvious use it to use the documentRect as the boundary.
	// These are useful for finding the outer areas of the network that would not otherwise be complete regions, but composed of dangling edges, so left blank.
	// Method - 
	//		add 4 new document edges to the NNetwork representing the sides of the boundary rect.
	//	    find all the intersections of the exiting network with the new document edges - store them in allIntersectionsList
	//      get the first edge in the allIntersectionsList - thisEdge
	// 		get all the documentEdges in another list i.e. all edges with attribute "REGIONEDGE", "document" - documentEdgesList (this is 4 at first but will increase with each "weld")
	// 		find the documentEdge in the documentEdgesList that intersects with thisEdge
	//		Weld(thisEdge, intersectingDocumentEdge) - this will split both edges to make 4 new edges connected to the intersection point
	//	    remove thisEdge from the allIntersectionsList
	//      continue until allIntersectionsList is empty.
	// addOuterBoundaryEdges
	//
	//
	//
	public void addOuterBoundaryEdges(Rect boundaryRect) {
		
		// first create the new boundary edges
		NPoint topLeft = new NPoint(boundaryRect.getTopLeft(),theNetwork);
		theNetwork.addPoint(topLeft);
		
		NPoint topRight = new NPoint(boundaryRect.getTopRight(),theNetwork);
		theNetwork.addPoint(topRight);
		
		NPoint bottomRight = new NPoint(boundaryRect.getBottomRight(),theNetwork);
		theNetwork.addPoint(bottomRight);
		
		NPoint bottomLeft = new NPoint(boundaryRect.getBottomLeft(),theNetwork);
		theNetwork.addPoint(bottomLeft);
		
		
		NEdge topEdge = theNetwork.addEdge(topLeft, topRight);
		topEdge.addAttribute(documentEdgeAttribute);
		NEdge rightEdge = theNetwork.addEdge(topRight, bottomRight);
		rightEdge.addAttribute(documentEdgeAttribute);
		NEdge bottomEdge = theNetwork.addEdge(bottomRight, bottomLeft);
		bottomEdge.addAttribute(documentEdgeAttribute);
		NEdge leftEdge = theNetwork.addEdge(bottomLeft,topLeft);
		leftEdge.addAttribute(documentEdgeAttribute);
		
		
		// get all the intersections of the network with these edges
		ArrayList<NEdge> allIntersectionsList = new ArrayList<NEdge>();
		allIntersectionsList.addAll(findIntersections(topEdge));
		allIntersectionsList.addAll(findIntersections(rightEdge));
		allIntersectionsList.addAll(findIntersections(bottomEdge));
		allIntersectionsList.addAll(findIntersections(leftEdge));
		
		
		// so now we have all the intersecting edges with the documentEdges
		int bailCount = 0;

		while( edgesToWeld(allIntersectionsList) ) {
			bailCount++;
			if(bailCount>1000000) {
				System.out.println("bail count exceeded");
				break;
			}
			
		}
		
		
		theNetwork.refreshIDs();
	}
	
	boolean edgesToWeld(ArrayList<NEdge> allIntersectionsList) {
		
		if(allIntersectionsList.size()<1) return false;
		NEdge thisEdge = allIntersectionsList.get(0);
		
		ArrayList<NEdge> documentEdges = getAllDocumentEdges();
		//System.out.println("edgesToWeld documentEdges " + documentEdges.size());
		if(documentEdges.size() == 0) return false;
		for(NEdge docEdge: documentEdges) {
			if( isIntersection(thisEdge, docEdge) ) {
				weldEdges(thisEdge, docEdge);
				allIntersectionsList.remove(thisEdge);
				//System.out.println("edgesToWeld welded edges still to process " + allIntersectionsList.size());
				return true;
			}
		}
		
		return true;
	}
	
	
	
	ArrayList<NEdge> getAllDocumentEdges(){
		ArrayList<NEdge> documentEdges = new ArrayList<NEdge>();
		
		ArrayList<NEdge> allEdges = theNetwork.getEdges();
		// find all the possible lines intersecting
		for(NEdge e: allEdges) {
			if( e.getAttributes().containsEqual(documentEdgeAttribute)) {
				documentEdges.add(e);
			}
		}
		
		return documentEdges;
	}
	
	
	ArrayList<NEdge> getAllNonDocumentEdges(){
		ArrayList<NEdge> allEdges = (ArrayList<NEdge>) theNetwork.getEdges().clone();
		
		ArrayList<NEdge> docEdges = getAllDocumentEdges();
		allEdges.removeAll(docEdges);
		return allEdges;
	}
	
	
	
	ArrayList<NEdge> findIntersections(NEdge theEdge) {
		// first find all the intersecting edges
		ArrayList<NEdge> allEdges = getAllNonDocumentEdges();
		ArrayList<NEdge> intersectingEdges = new ArrayList<NEdge>();

		// find all the possible lines intersecting
		for(NEdge e: allEdges) {
			//if( e.getAttributes().containsEqual(documentEdgeAttribute)) continue;
			if( isIntersection(theEdge,e) ) {
				intersectingEdges.add(e);
			}
		}
		//System.out.println("findAllWelds found " + intersectingEdges.size() + " intersecting edges");
		
		return intersectingEdges;
		
		
	}
	
	
	boolean isIntersection(NEdge e1, NEdge e2) {
		Line2 l1 = e1.getLine2();
		Line2 l2 = e2.getLine2();
		//if( l1.isIntersectionPossible(l2)== false) return false;
		boolean result =  l1.calculateIntersection(l2);	
		//System.out.println("line1 " + l1.toStr() + " line2 " + l2.toStr() + " intersection = " + result);
		return result;
	}
	
	void weldEdges(NEdge e1, NEdge e2) {
		// assumes that they have already been determined as intersecting
		PVector intersectionPoint = e1.getLine2().getIntersectionPoint(e2.getLine2());
		if(intersectionPoint == null) return;
		
		NPoint newSplitPoint = splitEdge(e1, intersectionPoint);
		splitEdge(e2,  newSplitPoint);
		
	}
	
	NPoint splitEdge(NEdge oldEdge, PVector splitPoint){
	    if( oldEdge == null || splitPoint == null) return null;
	    NPoint newSplitPoint = new NPoint(splitPoint, theNetwork);
	    splitEdge( oldEdge,  newSplitPoint);
	    return newSplitPoint;
	}
	    
	void splitEdge(NEdge oldEdge, NPoint newSplitPoint) {  
		if( oldEdge == null || newSplitPoint == null) return;
	    NPoint np1 = oldEdge.getEndNPoint(0);
	    NPoint np2 = oldEdge.getEndNPoint(1);
	    
	    KeyValuePairList attr1 = oldEdge.getAttributes().copy();
	    KeyValuePairList attr2 = oldEdge.getAttributes().copy();
	    theNetwork.addPoint(newSplitPoint);
	    
	    // give the new edges a copy of the old one's attributes
	    // deep copy is required to avoid reference sharing
	    NEdge newEdge1 = theNetwork.addEdge(np1, newSplitPoint);
	    newEdge1.setAttributes(attr1);
	    NEdge newEdge2 = theNetwork.addEdge(newSplitPoint, np2);
	    newEdge2.setAttributes(attr2);
	    updateDependentRegions_SplitEdge( oldEdge,  newEdge1,  newEdge2);
	    theNetwork.deleteEdge(oldEdge);
	  }
	
	// not sure we need to do this as this process happens before any regions are found
	void updateDependentRegions_SplitEdge(NEdge oldEdge, NEdge newEdge1, NEdge newEdge2){
	    ArrayList<NRegion> regions = theNetwork.getRegions();
	    
	    for(NRegion reg: regions){
	      reg.splitEdge( oldEdge,  newEdge1,  newEdge2);
	    }
	  }
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// Specifying the Attributes of automatically found regions. 
	// A once defined region will have one REGIONTYPE key, (if not, then it has not been defined).
	// Defining an automatically found region can be done by two methods:-
	// Explicit - where region-markers are placed within regions to identify their type. Region-markers are NPoints placed within the region they
	// wish to identify, and must have the Attribute ("REGIONMARKER" , regionType) where regionType is another string e.g. "LAKE".
	// Implicit - via some algorithm based on properties of the of the network or other data - e.g. density of points, using a key image
	// Only the explicit method setRegionAttributeWithRegionMarker() is part of this class. Because implicit methods could be almost any process, they are not contained within this class;
	// use the network helper instead
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// if a region contains a region marker, then the region has the regionAttribute added
	//
	//
	public void setRegionAttributeWithRegionMarker(String regionType) {
		// first find all the markers with marker attribute
		// does not copy network into current network as you want the network passed in to be permanently affected
		KeyValuePair markerDescriptor = new KeyValuePair("REGIONMARKER", regionType);
		KeyValuePair newRegionAttribute = new KeyValuePair("REGIONTYPE", regionType);

		ArrayList<NPoint> markers = theNetwork.getPointsMatchingQuery(markerDescriptor);
		System.out.println("setRegionAttributeWithRegionMarker:: found " + markers.size() + " markers matching " + regionType);
		// next loop through the markers finding the containing regions
		// once found, add the regionAttributeToAdd to the region
		int n = 0;
		for(NPoint marker: markers) {

			NRegion r = theNetwork.getNearestNRegion(marker.getPt());
			if(r != null) {
				r.addAttribute(newRegionAttribute);
				n++;
			}
		}
		
		System.out.println("setRegionAttributeWithRegionMarker:: found " + n + " regions matching " + regionType);
		
	}

	

	///////////////////////////////////////////////////////////////////////////////////////
	// all below is private apart from the debug methods at the bottom
	//
	
	private void findRegions(int pass) {

		if(pass==0 || pass==2) {
			// we reuse this afresh
			startEdgeList = new ArrayList<NEdge>();
		}
		foundRegionCount = 0;
		int bailCount=0;
		while(findRegion(pass)) {
			bailCount++;
			//if(pass==2)System.out.println("NNetworkAutoRegionFinder: findRegions found " + bailCount + " pass " + pass);
			if(bailCount > 1000000) {
				System.out.println("NNetworkAutoRegionFinder: findRegions seems stuck in a loop");
				return;
			}
		}
		System.out.println("NNetworkAutoRegionFinder: findRegions found " + foundRegionCount + " regions on pass " + pass);
	}

	boolean findRegion(int pass) {

		ArrayList<NEdge> thisRegionEdges = new ArrayList<NEdge>();
		
		NEdge startEdge  = getStartEdge(pass);

		if(startEdge==null) {
			// assume you have found and processed all the edges
			// System.out.println("NO more regions!");
			return false;
		}
		
		
		thisRegionEdges.add(startEdge);
		NEdge thisEdge = startEdge;
		
		// Establish the direction of the search in this pass
		// this is important as it sets the direction of the search of this loop. The other pass uses this same start edge but goes
		// in the opposite direction, but with the same winding rule, so should find the "other region" of this edge, if there is one.
		NPoint startPoint = thisEdge.p1;
		NPoint thisEndPoint = thisEdge.p2;
		if(pass==1 ) {
			startPoint = thisEdge.p2;
			thisEndPoint = thisEdge.p1;
		}
		
		
		int bailCount = 0;
		while(bailCount < 2000) {
			bailCount++;
			
			NEdge connectedEdge;
			if(pass<2) {
				connectedEdge =  getMostClockwiseConnectedEdge(thisEdge, thisEndPoint);
			} else {
				connectedEdge =  getConnectedEdgeWithARC1(thisEdge, thisEndPoint);
			}

			if(connectedEdge == null) {
				
				return true;
			}

			thisRegionEdges.add(connectedEdge);
			
			NPoint connectedEdgefarPoint = connectedEdge.getOtherPoint(thisEndPoint);
			
			
			//System.out.println("NNetworkAutoRegionFinder: findRegion connected edge " + connectedEdge.getID() + " start ID " + startEdgeID);
			if( connectedEdgefarPoint == startPoint ) {
				//if(pass==2)System.out.println("Found complete region " + foundRegionCount++);
				///////////////////////////////////////////////
				// this where the region is added to the network
				boolean result = theNetwork.tryCreateRegion(thisRegionEdges);
				if(result==false) {
					// failed to create a region.
					return true;
				}
				// all is good, a region has been formed
				foundRegionCount++;
				
				return true;	
			}
			
			//thisEndPoint = getFarPointOfEdge2(thisEdge, connectedEdge);  // 
			thisEndPoint =  thisEdge.getFarPointOtherEdge(connectedEdge);
			thisEdge = connectedEdge;
		}
		//if(pass==2)System.out.println("find region bail count reached");
		return true;


	}
	

   


	NEdge getStartEdge(int pass) {
		if(pass==0) return getVirginEdge();
			
		if(pass==1) return getEdgeFromStartEdgeList();
		
		if(pass==2) return getRemainingStartEdgeWith1ARC();
		
		// should never get here
		return null;
		
	}

	NEdge getVirginEdge() {
		// for pass 0
		// looks through the list of edges to find an edge with Associate Region Count == 0
		for(NEdge e: theNetwork.edges) {
			if( e.getAssociatedRegionCount()==0 && startEdgeList.contains(e)==false ) {
				startEdgeList.add(e);
				return e;
			}
		}
		// must have exhausted the list
		return null;
	}
	
	NEdge getEdgeFromStartEdgeList() {
		// for pass 1
		// uses the start-edges found in pass 0, but winds the search the other way
		for(int n = startEdgeListCounter; n < startEdgeList.size(); n++) {
			NEdge e = startEdgeList.get(n);
			if( e.getAssociatedRegionCount()==0 || e.getAssociatedRegionCount()==1 ) {
				startEdgeListCounter = n + 1;
				return e;
			}
		}
		// must have exhausted the list
		return null;
	}
	
	NEdge getRemainingStartEdgeWith1ARC() {
		// for pass 2
		// finds the remaining regions with only Associate Region Count == 1
		// a lot of these searches will be on junk data (dangling edges) that will never make a region, so we need to 
		// preclude this start edge being used again
		for(NEdge e: theNetwork.edges) {
			if( e.getAssociatedRegionCount()==1 && startEdgeList.contains(e)==false) {
				startEdgeList.add(e);
				return e;
			}
		}
		// must have exhausted the list
		return null;
	}

	private NEdge getMostClockwiseConnectedEdge(NEdge thisEdge, NPoint usingThisPoint) {
		ArrayList<NEdge> connectedEdges = thisEdge.getConnectedEdges(usingThisPoint);
		
		//if(pass==2) return findEdgeWithARC1(connectedEdges);
		
		if(connectedEdges==null) System.out.println("getMostClockwiseConnectedEdge:NULL1");
		if(thisEdge==null) System.out.println("getMostClockwiseConnectedEdge:NULL2");
		ArrayList<NEdge> sortedEdges = sortConnectedEdgesByClockwiseAngle(thisEdge, connectedEdges);
		
		int size = sortedEdges.size();
		if(size==0) return null;
		
	    return sortedEdges.get(0);
	}
	
	
	private NEdge getConnectedEdgeWithARC1(NEdge thisEdge, NPoint usingThisPoint) {
		ArrayList<NEdge> connectedEdges = thisEdge.getConnectedEdges(usingThisPoint);
		
		for(NEdge e: connectedEdges) {
    		if(e.getAssociatedRegionCount()==1) {
    			return e;
    		}
    	}
    	return null;
	}

	ArrayList<NEdge> sortConnectedEdgesByClockwiseAngle(NEdge referenceEdge, ArrayList<NEdge> connectedEdges) {
		
		SortObjectWithValue objectValueSorter = new SortObjectWithValue();
		
		for(NEdge e: connectedEdges) {
			//float rads = angleBetweenEdges(referenceEdge, e);
			float rads = referenceEdge.getHingedAngleBetween(e);
			float degs = (float) Math.toDegrees(rads);
			objectValueSorter.add(e,degs);
		}
		
		return objectValueSorter.getSorted();
	}

	//////////////////////////////////////////////////////////////////////////////
	// Preparation work before the region finding can take place.
	// 
	// Dangling Edge removal, identical edge removal, and removing previously existing regions
	//
	
	// iteratively erodes all dangling tips from all sequences of edges
	private void removeAllDanglingEdges() {
		while(true) {
			int danglingEdgesFoundAndRemoved = removeDanglingEdges();
			//System.out.println("removeAllDanglingEdges: danglingEdgesFoundAndRemoved " + danglingEdgesFoundAndRemoved);
			if(danglingEdgesFoundAndRemoved==0) break;
		}
	}


	// removes all dangling edges and
	// returns the number found. If the result is 0, then all dangling
	// edges have been removed.
	private int removeDanglingEdges() {
		ArrayList<NEdge> eList = theNetwork.getEdges();
		ArrayList<NEdge> toRemove = new ArrayList<NEdge>();
		int found = 0;
		for(NEdge e: eList) {
			if(  isDanglingEdge(e)  ) {
				toRemove.add(e);
				found++;
			} 
		}
		theNetwork.removeEdges(toRemove);
		return found;
	}


	boolean isDanglingEdge(NEdge e) {
		int numConnectedP1 = e.p1.getEdgeReferences().size();
		int numConnectedP2 = e.p2.getEdgeReferences().size();

		if(numConnectedP1 == 1 || numConnectedP2 == 1) return true;
		return false;

	}
	
	private void removePreExisitingRegions(){
		theNetwork.regions.clear();
		for(NEdge e: theNetwork.edges) {
			e.region1 = null;
			e.region2 = null;
		}
	}
	
	
	private void removeCoincidentEdges() {
		ArrayList<NEdge> eList = theNetwork.getEdges();
		ArrayList<NEdge> toRemove = new ArrayList<NEdge>();
		for(NEdge e: eList) {
			
			ArrayList<NEdge> connectedEdges = e.getAllConnectedEdges();
			
			for(NEdge connected: connectedEdges) {
				if(e.isUsingIdenticalPoints(connected)) {
					toRemove.add(connected);
				}
			}

		}
		
		//System.out.println("removed " + toRemove.size() + " identical edges");
		theNetwork.removeEdges(toRemove);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// debug methods
	//
	
	
	public void drawEdgesRandomColorWithPoints(RenderTarget rt) {
		// for debug only
		
		
		NNetworkDrawer drawer = new NNetworkDrawer(theNetwork);
		
		for(NEdge e: theNetwork.edges) {
			Color c = MOColor.getRandomRGB();
			
				
			drawer.drawEdge(e, c, 20, rt);
			
		}
		
		for(NPoint p: theNetwork.points) {
			
			drawer.drawPoint(p, Color.RED, 0.001f, rt);
		}
		
		printEdgePointNums(" in draw edges ");
		
	}
	
	void printEdgePointNums(String message) {
		System.out.println(message + " Num edges " + theNetwork.edges.size() + " Num Points " + + theNetwork.points.size());
	}
	
	public void drawEdgeRegionStatus(RenderTarget rt) {
		// for debug only
		NNetworkDrawer drawer = new NNetworkDrawer(theNetwork);
		printEdgePointNums(" in drawEdgeRegionStatus ");
		for(NEdge e: theNetwork.edges) {
			Color c = Color.YELLOW;
			int n = e.getAssociatedRegionCount();
			if(n == 0) c = Color.red;
			if(n == 1) c = Color.blue;
			if(n == 2) c = Color.black;
				
				drawer.drawEdge(e, c, 20, rt);
		}
		
		
	}
	
	
	
	public void drawResult(RenderTarget rt) {
		// for debug only
		
		ArrayList<NRegion> regions = theNetwork.getRegions();
		
		Color[] cols = MOColor.getBasic12ColorPalette();
		//Color.BLACK;
		//Color.RED;
		//Color.GREEN;
		//Color.CYAN;
		//Color.DARK_GRAY;
		//Color.MAGENTA;
		//Color.YELLOW;
		//Color.BLUE;
		//Color.LIGHT_GRAY;
		//Color.ORANGE;
		//Color.GRAY;
		//Color.PINK;
		
		int colNum = 0;
		for(NRegion r: regions) {
	    
	    	
			networkDrawer.drawRegionFill(r, cols[colNum++], rt);
			if(colNum>10) colNum = 0;
		}
		
		for(NRegion r: regions) {
			networkDrawer.drawRegionEdges(r, Color.blue, 5, rt);
		}
		networkDrawer.draw(rt, Color.WHITE);
		//theNetwork.save("C:\\simon\\Artwork\\MouseOrgan4\\Maps\\Network Maps\\London Flowers\\regionFindTest.csv");
	}

	
	public void drawRegionsByType(RenderTarget rt) {
		// for debug only
		
		ArrayList<NRegion> regions = theNetwork.getRegions();
		
		
		
		int colNum = 0;
		for(NRegion r: regions) {
			Color c = getRegionDefaultColour(r);
	    	
			networkDrawer.drawRegionFill(r, c, rt);
			if(colNum>10) colNum = 0;
		}
		
		
		System.out.println("drawn " + parkCounter + " park regions ," + lakeCounter + " lake regions ," + riverCounter + " river regions ");
	}

	
	public Color getRegionDefaultColour(NRegion r) {
		Color c = Color.WHITE;
		
		KeyValuePair parkAttribute = new KeyValuePair("REGIONTYPE", "PARK");
		if(r.thisItemContainsMatch(parkAttribute)) {
			parkCounter++;
			return Color.green;
		}
		KeyValuePair lakeAttribute = new KeyValuePair("REGIONTYPE", "LAKE");
		if(r.thisItemContainsMatch(lakeAttribute)) {
			lakeCounter++;
			return Color.CYAN;
		}
		KeyValuePair riverAttribute = new KeyValuePair("REGIONTYPE", "RIVER");
		if(r.thisItemContainsMatch(riverAttribute)) {
			riverCounter++;
			return Color.blue;
		}
		
		KeyValuePair densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_HIGH");
		if(r.thisItemContainsMatch(densityAttribute)) {
			riverCounter++;
			return Color.red;
		}
		densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_MEDIUM");
		if(r.thisItemContainsMatch(densityAttribute)) {
			riverCounter++;
			return Color.orange;
		}
		densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_LOW");
		if(r.thisItemContainsMatch(densityAttribute)) {
			riverCounter++;
			return Color.yellow;
		}
		
		return c;
	}
	
}
  * 
  * 
  */
 
