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
// Is initialised with a copy of the network's edges as a list theEdgeList (filtered by search criteria if required).
// 
// Then, given a start-edge, this class finds a continuous run of edges based on angleTollerance.
// call extractEdgeRun(NEdge startEdge) or extractEdgeRun_RandomStart() to get a list of continuous edges.

// Once found, these edges are removed from theEdgeList, so successive calls eventually exhaust the edgeList.  
// 
// If no more edfes can be found then returns null

public class NNetworkEdgeRunExtractor extends NNetworkProcessor{

	ArrayList<NEdge> theEdgeList;

	float angleTolleranceDegrees = 45;
	//ArrayList<NEdge> currentEdgeRunList;

	RandomStream randomStream = new RandomStream(1);


	// common to this and region extractor
	NNetworkEdgeRunExtractor(NNetwork ntwk, KeyValuePairList searchCriteria){
		super(ntwk);
		setSearchAttribute(searchCriteria);
		initialise();
	} 

	// common to this and region extractor
	private void initialise() {
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

	public void setAngleTollerance(float angleDegrees) {
		angleTolleranceDegrees = angleDegrees;
	}

	void setRandomSeed(int s) {
		randomStream = new RandomStream(s);
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// public methods This method returns an ordered list of connected edges, starting at a random
	// available edge. These edges are removed from the initial
	// edgeList, so cannot be re-used in any other search
	//
	// common to this and region extractor
	Vertices2 extractEdgeRunVertices() {

		ArrayList<NEdge> edgeRun = extractEdgeRun_RandomStart();
		if(edgeRun==null){
			System.out.println("NNetworkEdgeRunExtractor:extractEdgeRunVertices  - no more edge runs");
			return null;
		}
		return getVertices(edgeRun);
	}
	
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

	


	// turns an array list of NEdges, which should be sequential (edges link, but by either end, so, the raw end points are not sequential), into an ordered
	// list of PVector points by finding the joining points between each edge pair.
	Vertices2 getVertices(ArrayList<NEdge> edges){
		if( checkEdgesAreOrdered(edges)==false) {
			System.out.println("getVertices - edges are not ordered");
			return null;
		}
		ArrayList<PVector> pointList = new ArrayList<PVector>();

		NEdge thisEdge = edges.get(0);

		// if the edges lists only has one edge return this as a short vertices2...
		if(edges.size()==1) {
			pointList.add(thisEdge.getEndCoordinate(0));
			pointList.add(thisEdge.getEndCoordinate(1));
			return new Vertices2(pointList);
		}


		// .... otherwise get on with connecting the run of edges by their sequential points.
		NEdge nextEdge = edges.get(1);

		// find first dangling vertex

		NPoint connectionPoint =  thisEdge.getConnectingPoint(nextEdge);
		NPoint firstPoint =  thisEdge.getOtherPoint(connectionPoint);
		pointList.add(firstPoint.getPt());



		int numEdges = edges.size();
		// find all the connecting points in order
		for(int n = 1; n < numEdges; n++) {
			nextEdge = edges.get(n);

			connectionPoint =  thisEdge.getConnectingPoint(nextEdge);
			pointList.add(connectionPoint.getPt());
			thisEdge = nextEdge;
		}

		// find final dangling point
		NPoint finalPoint = nextEdge.getOtherPoint(connectionPoint);


		pointList.add(finalPoint.getPt());

		return new Vertices2(pointList);

	}



	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// private methods
	//


	private NEdge findRandomStartEdge() {
		if(isEdgeListOK()==false) {
			//System.out.println("NetworkEdgeMarcher:findAStartEdge edge = null");
			return null;
		}

		int ind = 0;
		int listSize = theEdgeList.size();
		if(listSize > 1) ind = randomStream.randRangeInt(0,theEdgeList.size()-1);

		NEdge runStartEdge =  theEdgeList.get(ind);
		popFromEdgeList(runStartEdge);
		return runStartEdge;
	}

	private boolean isEdgeListOK() {
		if(theEdgeList == null) {
			//System.out.println("NetworkEdgeMarcher:theEdgeList = null");
			return false;
		}
		if(theEdgeList.size()==0) {
			//System.out.println("NetworkEdgeMarcher:isEdgeListOK edge list empty");
			return false;
		}
		return true;

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
			if( currentEdge.isUsingIdenticalPoints(connectedEdge) ) continue; // rare event of infinitely short line. It gets ignored

			// find "far" connecting point of current Edge
			NPoint connectionPoint =  connectedEdge.getConnectingPoint(currentEdge);
			otherPointInConnectedEdge = connectedEdge.getOtherPoint(connectionPoint);
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
			float ang = e.getColiniarity(otherEdge);
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
		float radiansBetween = e1.getColiniarity(e2); //getColiniarityOfEdges( e1,  e2);
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



	private boolean checkEdgesAreOrdered(ArrayList<NEdge> edges) {
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

}

