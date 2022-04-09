package MONetwork;


import java.util.ArrayList;
import MOApplication.Surface;
import MOCompositing.RenderTarget;
import MOMaths.Range;
import MOUtils.SortObjectWithValue;

//////////////////////////////////////////////////////////////////////////////////////
// 
// This network processor extracts regions from the list of edges
// Before it can start, it needs to remove "dangling" edges - i.e. those edges not in a loop but maybe in a run.
// Also accidental co-incident edges need to be removed.
// It works in two passes. First pass, the first edge with no associated region count (ARCs) is found and a left-most-turn search made to find the loop. Pass 1 uses the p1 direction of 
// the edge to establish the search direction.
// Depending on the orientation of the edge, and the nature of the loop to be found it is not possible to determine whether to not the search is in a Clockwise or Anti clockwise fashion; 
// you only know after the loop has been found, then it is too late.
// In pass 1 connectingEdges with any ARC are added to the list of edges making this loop. Once a loop has been found, all the edges' ARC are incremented. The start edge is added to a startEdgeList, this has 2 purposes; 
// 1 so that if we bail on this edge, we do not attempt the same edge again,
// and 2/ the start Edge list is used to search using the p2 direction to find many missing loops from the first search.
//
//
// In pass 2 the the startEdge list is used, to get the sae start edges as in pass 1, but direct the search the other way (using p2 direction of the statEdge) to find loops in the other direction. 
// In this pass connecting edges with any ARC are permitted.
//
// This seems to get about 99% of regions.
// This algorithm is purely deterministic with one conclusion; all the regions in the network are found and stored in the loaded network. 
// It removes any previously stored regions. This can then be saved for later use.
// 


public class NNetworkAutoRegionFinder extends NNetworkProcessor{

	ArrayList<NEdge> startEdgeList = new ArrayList<NEdge>();
	int startEdgeListCounter = 0;
	
	// just for debugging
	int foundRegionCount = 0;
	Range largestRegion = new Range();
	
	
	// common to this and region extractor
	//NNetworkAutoRegionFinder(NNetwork ntwk, KeyValuePairList searchCriteria){
	public NNetworkAutoRegionFinder(NNetwork ntwk){
		super(ntwk);
		// for debug only
		largestRegion.initialiseForExtremaSearch();
		
		
		
		removePreExisitingRegions();
		removeAllDanglingEdges();
		removeCoincidentEdges();
		

		System.out.println("NNetworkAutoRegionFinder:  pass 0");
		findRegions(0);
		printEdgeRegionAssociateionCounts(0);
		
		System.out.println("NNetworkAutoRegionFinder:  pass 1");
		findRegions(1);
		printEdgeRegionAssociateionCounts(1);
		
		
		
	} 
	

	void printEdgeRegionAssociateionCounts(int pass) {
		// for debug only
		int countminus = 0;
		int count0=0;
		int count1=0;
		int count2=0;
		int count3=0;
		for(NEdge e: theNetwork.edges) {
			   if(e.getAssociatedRegionCount() < 0) countminus++;
			   if(e.getAssociatedRegionCount() == 0) count0++;
			   if(e.getAssociatedRegionCount() == 1) count1++;
			   if(e.getAssociatedRegionCount() == 2) count2++;
			   if(e.getAssociatedRegionCount() > 2) count3++;
		   }
		
		System.out.println("Pass " + pass + " final edge association counts 0: " + count0 + ", 1: " + count1 + ", 2: " + count2 + ", illegal -> too low: " +countminus + ", too high: " + count3);
		System.out.println("Found regions in this pass = " + foundRegionCount);
		foundRegionCount = 0;
		
		
		for(NRegion r: theNetwork.regions) {
			
			int n = r.getNumEdges();
			largestRegion.addExtremaCandidate(n);
		}
		
		System.out.println("largest regions has " + largestRegion.limit2 + " edges");
	}
	
	
	
	
	
	void findRegions(int pass) {

		int bailCount=0;
		while(findRegion(pass)) {
			bailCount++;
			//System.out.println("NNetworkAutoRegionFinder: findRegions found " + bailCount + " pass " + pass);
			if(bailCount > 1000000) {
				System.out.println("NNetworkAutoRegionFinder: findRegions seems stuck in a loop");
				return;
			}
		}
		//System.out.println("NNetworkAutoRegionFinder: findRegions found " + bailCount + " regions on pass " + pass);
	}

	boolean findRegion(int pass) {

		ArrayList<NEdge> thisRegionEdges = new ArrayList<NEdge>();
		
		NEdge startEdge  = getStartEdge(pass);

		if(startEdge==null) {
			// assume you have found and processed all the edges
			System.out.println("NO more regions!");
			return false;
		}
		
		
		thisRegionEdges.add(startEdge);
		NEdge thisEdge = startEdge;
		
		// Establish the direction of the search in this pass
		// this is important as it sets the direction of the search of this loop. The other pass uses this same start edge but goes
		// in the opposite direction, but with the same winding rule, so should find the "other region" of this edge, if there is one.
		NPoint startPoint = thisEdge.p1;
		NPoint thisEndPoint = thisEdge.p2;
		if(pass==1) {
			startPoint = thisEdge.p2;
			thisEndPoint = thisEdge.p1;
		}
		
		//System.out.println("going into region loop");
		int bailCount = 0;
		while(bailCount < 2000) {
			bailCount++;
			NEdge connectedEdge =  getMostClockwiseConnectedEdge(thisEdge, thisEndPoint);
			
			if(connectedEdge == null) return true;

			thisRegionEdges.add(connectedEdge);
			
			NPoint connectedEdgefarPoint = connectedEdge.getOtherPoint(thisEndPoint);
			
			
			//System.out.println("NNetworkAutoRegionFinder: findRegion connected edge " + connectedEdge.getID() + " start ID " + startEdgeID);
			if( connectedEdgefarPoint == startPoint ) {
				//System.out.println("Found complete region");
				///////////////////////////////////////////////
				// this where the region is added to the network
				boolean result = theNetwork.tryCreateRegion(thisRegionEdges);
				if(result==false) {
					//System.out.println("Warning:: NNetworkAutoRegionFinder:findRegion... in-valid region edges found - unable to make region");
					return true;
				}
				// all is good, a region has been formed
				foundRegionCount++;
				
				return true;	
			}
			
			thisEndPoint = getFarPointOfEdge2(thisEdge, connectedEdge);
			thisEdge = connectedEdge;
		}
		return true;


	}
	

   


	NEdge getStartEdge(int pass) {
		if(pass==0) {
			NEdge startEdge = getVirginEdge();
			if(startEdge!=null) startEdgeList.add(startEdge);
			return startEdge;
		}
		
		if(pass>0) return getEdgeFromStartEdgeList();
		// will never get here
		return null;
		
	}

	NEdge getVirginEdge() {
		// for pass 0
		for(NEdge e: theNetwork.edges) {
			if( e.getAssociatedRegionCount()==0 && startEdgeList.contains(e)==false ) {
				return e;
			}
		}
		return null;
	}
	
	NEdge getEdgeFromStartEdgeList() {
		// for pass 1
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

	private NEdge getMostClockwiseConnectedEdge(NEdge thisEdge, NPoint usingThisPoint) {
		ArrayList<NEdge> connectedEdges = thisEdge.getConnectedEdges(usingThisPoint);
		ArrayList<NEdge> sortedEdges = sortConnectedEdgesByClockwiseAngle(thisEdge, connectedEdges);
		
		int size = sortedEdges.size();
		if(size==0) return null;
		
	    return sortedEdges.get(0);
	}

	ArrayList<NEdge> sortConnectedEdgesByClockwiseAngle(NEdge referenceEdge, ArrayList<NEdge> connectedEdges) {
		
		SortObjectWithValue objectValueSorter = new SortObjectWithValue();
		
		for(NEdge e: connectedEdges) {
			float rads = angleBetweenEdges(referenceEdge, e);
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

}


