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
	
	ArrayList<NEdge> theEdgeListCopy;
	

   
	
	// common to this and region extractor
	NNetworkAutoRegionFinder(NNetwork ntwk, KeyValuePairList searchCriteria){
		super(ntwk);
		
		removePreExisitingRegions();
		removeAllDanglingEdges();
		
		setSearchAttribute(searchCriteria);
		
		initEdgeList();
		findRegions(true);
		
		initEdgeList();
		findRegions(false);
	} 
	
	// common to this and region extractor
	private void initEdgeList() {
		// gets called just before the runs are collected
		// after all parameters are set up.
		
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
	}
	
	private void removePreExisitingRegions(){
		theNetwork.regions.clear();
		for(NEdge e: theNetwork.edges) {
			e.region1 = null;
			e.region2 = null;
		}
	}
	
	
	void findRegions(boolean clockwiseSearch) {
		
		int bailCount=0;
		while(findRegion(clockwiseSearch)) {
			bailCount++;
			if(bailCount > 1000000) {
				System.out.println("NNetworkAutoRegionFinder: findRegions seems stuck in a loop");
				return;
			}
		}
		
	}
	
	boolean findRegion(boolean clockwiseSearch) {
		
		NEdge startEdge  = theEdgeListCopy.get(0);
		
		NEdge thisEdge = startEdge;
		NPoint thisEndPoint = thisEdge.p1;
		ArrayList<NEdge> thisRegionEdges = new ArrayList<NEdge>();
		
		while(true) {
			NEdge connectedEdge =  getMostClockwiseConnectedEdge(thisEdge, thisEndPoint,  clockwiseSearch);
			if(connectedEdge == startEdge) {
				boolean result = theNetwork.tryCreateRegion(thisRegionEdges);
				if(result==false) {
					System.out.println("NNetworkAutoRegionFinder: in-valid region edges found - unable to make region");
					return false;
				}
				return true;	
			}
			thisRegionEdges.add(connectedEdge);
			thisEndPoint = getFarPointOfEdge2(thisEdge, connectedEdge);
			thisEdge = connectedEdge;
		}
		return false;
		
	}
	
	NEdge getEdgeWithAssociatedRegionCount(int associatedRegionCount) {
		
		for(NEdge e: theNetwork.edges) {
			if( e.getAssociatedRegionCount() == associatedRegionCount) return e;
		}
		return null;
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Dangling Edge removal
	//
	
	private void removeAllDanglingEdges() {
		while(removeDanglingEdges(theNetwork.getEdges()) != 0) {}
	}
	
	
	
	int removeDanglingEdges(ArrayList<NEdge> eList) {
		ArrayList<NEdge> tempList = new ArrayList<NEdge>();
		int found = 0;
		for(NEdge e: eList) {
			if(  isDanglingEdge(e)==false  ) {
				tempList.add(e);
			} else {
				found++;
			}
		}
		
		eList = tempList;
		return found;
	}
	
	
	boolean isDanglingEdge(NEdge e) {
			
			
			int numConnectedP1 = e.p1.getEdgeReferences().size();
			int numConnectedP2 = e.p2.getEdgeReferences().size();
			
			if(numConnectedP1 == 1 || numConnectedP2 == 1) return true;
			return false;
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// public methods This method returns an ordered list of connected edges, starting at a random
	// available edge. These edges are removed from the initial
	// edgeList, so cannot be re-used in any other search
	//
	// common to this and region extractor
	

	
	
	
	
	
	
	
	ArrayList<NEdge> getConnectedEdges(NEdge thisEdge, NPoint usingThisPoint){
		ArrayList<NEdge> connectedEdges = (ArrayList)usingThisPoint.getEdgeReferences().clone();
		connectedEdges.remove(thisEdge);
		return connectedEdges;
	}
	
	
	
	private NEdge getMostClockwiseConnectedEdge(NEdge thisEdge, NPoint usingThisPoint, boolean clockwiseSearch) {
		//finds an single edge connected to either end of thisEdge
		NEdge foundEdge = null;
		
		ArrayList<NEdge> connectedEdges = getConnectedEdges( thisEdge,  usingThisPoint);
		
		if(connectedEdges.size()==1) return connectedEdges.get(0);
		
		// if there is more than one edge connected find the most cw/acw
		foundEdge = getMostClockwise(thisEdge, connectedEdges, clockwiseSearch);
		if(foundEdge==null) {
			System.out.println("getConnectedEdge: no connecting edge found");
			return null;
		}
		
		return foundEdge;
		
	}
	
	
	private NEdge getMostClockwise(NEdge e, ArrayList<NEdge> connectedEdges, boolean clockwiseSearch) {
		
		
		NEdge smallestAngleEdge = null;
		float smallestAngle = 10000;
		
		NEdge largestAngleEdge = null;
		float largestAngle = 0;
		
		for(NEdge otherEdge: connectedEdges) {
			if( isInEdgeList(otherEdge) == false ) continue;
			float ang = angleBetweenEdges(e, otherEdge);
			
			
			if( ang < smallestAngle) {
				smallestAngle = ang;
				smallestAngleEdge = otherEdge;
			}
			
			if( ang > largestAngle) {
				largestAngle = ang;
				largestAngleEdge = otherEdge;
			}
			
			
		}
		/// return the smallest or largest angled-edge found
		if(clockwiseSearch) {
			return smallestAngleEdge;
		} else {
			return largestAngleEdge;
		}
	}
	
	
	
	
	boolean isCollinearWithinTollearance(NEdge e1, NEdge e2, float angleTolInDegrees) {
		//if(edgesConnect( e1, e2)== false) return false;
		float radiansBetween = angleBetweenEdges( e1,  e2);
		float degreesBetween = radiansBetween*57.2958f;
				
		if( ( MOMaths.isClose(degreesBetween, 0, angleTolInDegrees) || MOMaths.isClose(degreesBetween, 180, angleTolInDegrees) ) )  return true;
		return false;
	}
	
	boolean isInEdgeList(NEdge e) {
		return theEdgeListCopy.contains(e);
	}
	
	
	private void popFromEdgeList(NEdge e) {
		theEdgeListCopy.remove(e);
	}

}


