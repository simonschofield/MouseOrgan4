package MONetwork;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import MOImage.KeyImageSampler;
import MOImage.RenderTarget;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;




//////////////////////////////////////////////////////////////////////////////////////
// This network processor extracts a continuous run of edges from a network
// The class keeps the found edge run in the form of and ArrayList of NEdges.
//
public class NNetworkEdgeRunExtractor extends NNetworkProcessor{
	
	ArrayList<NEdge> theEdgeList;
	
	float angleTolleranceDegrees = 45;
	//ArrayList<NEdge> currentEdgeRunList;
	
	RandomStream randomStream = new RandomStream(1);

	
	NNetworkEdgeRunExtractor(NNetwork ntwk, KeyValuePairList searchCriteria){
		super(ntwk);
		setSearchAttribute(searchCriteria);
		initialiseRun();
	} 
	
	
	private void initialiseRun() {
		// gets called just before the runs are collected
		// after all parameters are set up.
		
		if(currentSearchAttributes == null) {
			// makes a copy because theEdgeList gets destroyed by the search
			theEdgeList = (ArrayList)theNetwork.getEdges().clone();
			System.out.println("NetworkEdgeMarcher:fcurrentSearchAttribute is null");
		} else {
			theEdgeList = getEdgesMatching();
			//System.out.println("NetworkEdgeMarcher:found " + theEdgeList.size() + " edges matching the critera " + currentSearchAttribute.theKey);
		}
		if(theEdgeList == null) {
			System.out.println("NetworkEdgeMarcher:startEdgeMarch edges = null");
			return;
		}
		if(theEdgeList.size()==0) {
			System.out.println("NetworkEdgeMarcher:startEdgeMarch no edges found");
			return;
		}
		
		
	}
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// public methods This method returns an ordered list of connected edges, starting at a random
	// available edge. These edges are removed from the initial
	// edgeList, so cannot be re-used in any other search
	//
	ArrayList<NEdge> extractEdgeRun_RandomStart() {
		
		NEdge runStartEdge = findRandomStartEdge();
		if(runStartEdge == null) {
			// there are no more edges to be processed
			return null;
		}
		
		return extractEdgeRun(runStartEdge);

	}
	
	
	ArrayList<NEdge> extractEdgeRun(NEdge startEdge) {
		
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
	
	public void setAngleTollerance(float angleDegrees) {
		angleTolleranceDegrees = angleDegrees;
	}
	
	void setRandomSeed(int s) {
		randomStream = new RandomStream(s);
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

