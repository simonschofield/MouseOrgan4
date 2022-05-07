package MONetwork;


import java.awt.Color;
import java.util.ArrayList;
import MOApplication.Surface;
import MOCompositing.RenderTarget;
import MOImage.MOColor;
import MOMaths.Range;
import MOUtils.SortObjectWithValue;

//////////////////////////////////////////////////////////////////////////////////////
// 
// This network processor extracts regions from the list of edges
// It then adds them to the network as NRegions.
// Before it can start, it needs to remove "dangling" edges - i.e. those edges not in a loop but maybe in a run.
// Also accidental co-incident edges need to be removed.
// It works in two passes. (Pass 0 and Pass 1) First pass, the first edge with no associated region count (ARCs) is found and a left-most-turn search made to find the loop. Pass 1 uses the p1 direction of 
// the edge to establish the search direction.
// Depending on the orientation of the edge, and the nature of the loop to be found it is not possible to determine whether to not the search is in a Clockwise or Anti clockwise fashion; 
// you only know after the loop has been found, then it is too late.
//
// In pass 0 connectingEdges with no ARC are added to the list of edges making this loop. Once a loop has been found, all the edges' ARC are incremented. The start edge is added to a startEdgeList, this has 2 purposes; 
// 1 so that if we bail on this edge, we do not attempt the same edge again,
// and 2/ the start Edge list is used in the second pass (pass 1) to search using the p2 direction to find many missing loops from the first search.
//
// In pass 1 the the startEdge list is used, to get the same start edges as in pass 1, but direct the search the other way (using p2 direction of the statEdge) to find loops in the other direction. 
// In this pass connecting edges with any ARC are permitted.
//  
// In pass 2 there are some scattered regions and some edges to whole map still left unused. They all have an ARC of 1. This pass mops them up.
//
// This seems to get about 99.9% of regions. The reason that some regions are left unfound is that these region has some edges with an ARC of 2, due to finding larger surrounding regions. TBD. These might be mopped up later.
// This algorithm is purely deterministic with one conclusion; all the regions in the network are found and stored in the loaded network. 
// It removes any previously stored regions. This can then be saved for later use.
// 

 public class NNetworkRegionFinder{

	NNetwork theNetwork;
	public NNetworkDrawer networkDrawer;
	
	
	
	ArrayList<NEdge> startEdgeList = new ArrayList<NEdge>();
	
	int startEdgeListCounter = 0;
	
	// just for debugging
	int foundRegionCount = 0;
	Range largestRegion = new Range();
	
	
	// common to this and region extractor
	//NNetworkAutoRegionFinder(NNetwork ntwk, KeyValuePairList searchCriteria){
	public NNetworkRegionFinder(NNetwork ntwk){
		//super(ntwk);
		// for debug only
		largestRegion.initialiseForExtremaSearch();
		
		theNetwork = ntwk;
		
		networkDrawer = new NNetworkDrawer(theNetwork);
		
		
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
		
	} 
	
	
	
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
		
		System.out.println("removed " + toRemove.size() + " identical edges");
		theNetwork.removeEdges(toRemove);
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// debug methods
	//
	
	public void drawEdgeRegionStatus(RenderTarget rt) {
		// for debug only
		NNetworkDrawer drawer = new NNetworkDrawer(theNetwork);
		for(NEdge e: theNetwork.edges) {
			Color c = Color.black;
			int n = e.getAssociatedRegionCount();
			if(n == 0) c = Color.red;
			if(n == 1) c = Color.blue;
			if(n == 2) c = Color.black;
				
				drawer.drawEdge(e, c, 6, rt);
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

	
	
	
	
	
}
 
 
 
 
 
 
 
