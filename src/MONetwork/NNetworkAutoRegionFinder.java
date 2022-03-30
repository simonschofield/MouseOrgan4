package MONetwork;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import MOCompositing.RenderTarget;
import MOImage.KeyImageSampler;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;

//////////////////////////////////////////////////////////////////////////////////////
// This network processor extracts regions from the list of edges
// It works in two passes. First pass, the first edge with no associated edge references (AERs) is found and a clockwise search made to find the loop.
// If an edge is found it is given an AER. Find the next edge without an AER, and continue until all edges have been processed. This pass may leave some
// regions unfound (bedded between other regions - all the edges have 1 AER, but the inner region is not found). The second pass finds an edge with 1 AER, and searches for attached edges also with 1 AER. 
// Edges which do not participate in loops (dangling edges) are removed at the start.

// This algorithm is purely deterministic with one conclusion; all the regions in the network are found and stored in the loaded network. 
// It removes any previously stored regions. This can then be saved for later use.
// 
// Each edge is the boundary between two regions, one found by searching clockwise, the other found by searching anti-clockwise. 
// Clockwise regions are found first. 
// The whole network is surrounded by one large region. Once an edge has been used
// to find its two regions it is removed from the theEdgeList, thereby optimising the remaining process.
// Because we want to store the found regions in the 

public class NNetworkAutoRegionFinder extends NNetworkProcessor{

	//ArrayList<NEdge> theEdgeListCopy;




	// common to this and region extractor
	//NNetworkAutoRegionFinder(NNetwork ntwk, KeyValuePairList searchCriteria){
	public NNetworkAutoRegionFinder(NNetwork ntwk){
		super(ntwk);

		removePreExisitingRegions();
		System.out.println("NNetworkAutoRegionFinder: removedPreExisitingRegions ");
		removeAllDanglingEdges();
		System.out.println("NNetworkAutoRegionFinder: removedAllDanglingEdges ");
		//setSearchAttribute(searchCriteria);

		initEdgeList();
		findRegions(1);

		initEdgeList();
		findRegions(2);
	} 

	// don't think we need this
	private void initEdgeList() {
		// gets called just before the runs are collected
		// after all parameters are set up.
		/*
		if(currentSearchAttributes == null) {
			// makes a copy because theEdgeList gets destroyed by the search
			theEdgeListCopy = (ArrayList)theNetwork.getEdges().clone();
			System.out.println("NNetworkAutoRegionFinder:fcurrentSearchAttribute is null");
		} else {
			theEdgeListCopy = getEdgesMatching();
			//System.out.println("NetworkEdgeMarcher:found " + theEdgeList.size() + " edges matching the critera " + currentSearchAttribute.theKey);
		}
		if(theEdgeListCopy == null) {
			System.out.println("NNetworkAutoRegionFinder:startEdgeMarch edges = null");
			return;
		}
		if(theEdgeListCopy.size()==0) {
			System.out.println("NNetworkAutoRegionFinder:startEdgeMarch no edges found");
			return;
		}
		*/
	}

	


	void findRegions(int pass) {

		int bailCount=0;
		while(findRegion(pass)) {
			bailCount++;
			System.out.println("NNetworkAutoRegionFinder: findRegions found " + bailCount + " pass " + pass);
			if(bailCount > 1000000) {
				System.out.println("NNetworkAutoRegionFinder: findRegions seems stuck in a loop");
				return;
			}
		}
		System.out.println("NNetworkAutoRegionFinder: findRegions found " + bailCount + " regions on pass " + pass);
	}

	boolean findRegion(int pass) {

		ArrayList<NEdge> thisRegionEdges = new ArrayList<NEdge>();
		
		NEdge startEdge  = getStartEdge(pass);
		
		
		
		System.out.println("NNetworkAutoRegionFinder: findRegion found startEdge " + startEdge.toStr());
		if(startEdge==null) {
			// assume you have found and processed all the edges
			return false;
		}
		
		thisRegionEdges.add(startEdge);
		NPoint startPoint = startEdge.p1;
		
		int startEdgeID = startEdge.getID();

		
		NEdge thisEdge = startEdge;
		NPoint thisEndPoint = thisEdge.p2;
		
	
	
	
		

		while(true) {
			NEdge connectedEdge =  getMostClockwiseConnectedEdge(thisEdge, thisEndPoint, pass);
			thisRegionEdges.add(connectedEdge);
			
			NPoint connectedEdgefarPoint = connectedEdge.getOtherPoint(thisEndPoint);
			
			
			System.out.println("NNetworkAutoRegionFinder: findRegion connected edge " + connectedEdge.getID() + " start ID " + startEdgeID);
			if( connectedEdgefarPoint == startPoint ) {
				
				///////////////////////////////////////////////
				// this where the region is added to the network
				boolean result = theNetwork.tryCreateRegion(thisRegionEdges);
				if(result==false) {
					System.out.println("NNetworkAutoRegionFinder: in-valid region edges found - unable to make region");
					return false;
				}
				return true;	
			}
			
			thisEndPoint = getFarPointOfEdge2(thisEdge, connectedEdge);
			thisEdge = connectedEdge;
		}


	}
	
	
   


	NEdge getStartEdge(int pass) {
		if(pass==1) return getEdgeWithAssociatedRegionCount(0);
		// if pass == 2
		return getEdgeWithAssociatedRegionCount(1);
	}

	NEdge getEdgeWithAssociatedRegionCount(int associatedRegionCount) {

		for(NEdge e: theNetwork.edges) {
			if( e.getAssociatedRegionCount() == associatedRegionCount) return e;
		}
		return null;
	}





	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// public methods This method returns an ordered list of connected edges, starting at a random
	// available edge. These edges are removed from the initial
	// edgeList, so cannot be re-used in any other search
	//
	// common to this and region extractor









	ArrayList<NEdge> getConnectedEdges(NEdge thisEdge, NPoint usingThisPoint, int pass){
		ArrayList<NEdge> connectedEdges = (ArrayList)usingThisPoint.getEdgeReferences().clone();
		connectedEdges.remove(thisEdge);

		if(pass == 0) {
			// remove edges with 1 or 2 region references
			removeEdgesWithRegionReference(connectedEdges, 1, 2);
		}

		if(pass == 1) {
			// remove edges with 0 or 2 region references
			removeEdgesWithRegionReference(connectedEdges, 0, 2);
		}


		return connectedEdges;
	}

	void removeEdgesWithRegionReference(ArrayList<NEdge> connectedEdges, int refCountA, int refCountB) {
		ArrayList<NEdge> tempList = new ArrayList<NEdge>();
		for(NEdge e: connectedEdges) {

			if(e.getAssociatedRegionCount()==refCountA  || e.getAssociatedRegionCount()==refCountB) {
				// don't add to temp
			}else {
				tempList.add(e);
			}
		}
		connectedEdges = tempList;
	}



	private NEdge getMostClockwiseConnectedEdge(NEdge thisEdge, NPoint usingThisPoint, int pass) {
		//finds an single edge connected to either end of thisEdge
		NEdge foundEdge = null;

		ArrayList<NEdge> connectedEdges = getConnectedEdges( thisEdge,  usingThisPoint, pass);
		System.out.println("getConnectedEdge: num connecting edge found " + connectedEdges.size());
		if(connectedEdges.size()==1) return connectedEdges.get(0);

		// if there is more than one edge connected find the most cw/acw
		foundEdge = getMostClockwise(thisEdge, connectedEdges);
		if(foundEdge==null) {
			System.out.println("getConnectedEdge: no connecting edge found");
			return null;
		}

		return foundEdge;

	}


	private NEdge getMostClockwise(NEdge e, ArrayList<NEdge> connectedEdges) {

		NEdge largestAngleEdge = null;
		float largestAngle = 0;

		for(NEdge otherEdge: connectedEdges) {
			//if( isInEdgeList(otherEdge) == false ) continue;
			float ang = angleBetweenEdges(e, otherEdge);
			System.out.println("angleBetweenEdges " + ang);
			if( ang > largestAngle) {
				largestAngle = ang;
				largestAngleEdge = otherEdge;
			}


		}

		return largestAngleEdge;
	}





	//////////////////////////////////////////////////////////////////////////////
	// Dangling Edge removal, and removing previoulsy existing regions
	//

	private void removeAllDanglingEdges() {
		while(true) {
			int danglingEdgesFoundAndRemoved = removeDanglingEdges();
			System.out.println("removeAllDanglingEdges: danglingEdgesFoundAndRemoved " + danglingEdgesFoundAndRemoved);
			if(danglingEdgesFoundAndRemoved==0) break;
		}
	}



	int removeDanglingEdges() {
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

}


