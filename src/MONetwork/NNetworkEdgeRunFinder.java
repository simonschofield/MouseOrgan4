package MONetwork;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import MOCompositing.BufferedImageRenderTarget;
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
// If no more edges can be found then returns null

public class NNetworkEdgeRunFinder{

	NNetwork theNetwork; 
	ArrayList<NEdge> theEdgeList;

	float angleTolleranceDegrees = 45;
	boolean runsCanOverlap = true;

	RandomStream randomStream = new RandomStream(1);

	ArrayList<Vertices2> extractedVertices;

	// this is only used to check for overlap
	ArrayList<NEdge> extractedEdges;
	
	
	public NNetworkEdgeRunFinder(NNetwork ntwk, KeyValuePairList searchCriteria){
		
		theNetwork = ntwk;
		theNetwork.setSearchAttribute(searchCriteria);
		initialiseEdgeList();
	} 

	// common to this and region extractor
	private void initialiseEdgeList() {
		// gets called just before the runs are collected
		// after all parameters are set up.

		if(theNetwork.currentSearchAttributes == null) {
			// makes a copy because theEdgeList gets destroyed by the search
			theEdgeList = (ArrayList)theNetwork.getEdges().clone();
			System.out.println("NetworkEdgeMarcher:fcurrentSearchAttribute is null");
		} else {
			theEdgeList = theNetwork.getEdgesMatchingSearchAttributes(true);
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

	public void setRandomSeed(int s) {
		randomStream = new RandomStream(s);
	}
	
	
	public void setRunsCanOverlap(boolean overlap) {
		// If set to false, then runs will end when they find another point that has been used in a previous edge run.
		// Hence all the runs are broken up.
		runsCanOverlap = overlap;
	}
	
	
	public ArrayList<Vertices2> extractAllEdgeRunVertices(){
		// The most common method to find all the edge runs as vertices 2
		// Use this method to get all the edge runs with
		// the existing search criteria, as a list of Vertices2
		//
		
		extractedVertices = new ArrayList<Vertices2>();
		extractedEdges = new ArrayList<NEdge>();
		
		while(true) {
			Vertices2 verts = extractEdgeRunVertices();
			if(verts==null) {
				System.out.println("preExtractEdgeRuns: fininshed collecting edge runs ");
				break;
			}

			extractedVertices.add(verts);
			//System.out.println("extractNetworkEdgeVertices: found " + edgeRunVertices.size() + " runs");
		}

		System.out.println("preExtractEdgeRuns: found " + extractedVertices.size() + " runs");
		
		return extractedVertices;
	}
	
	public ArrayList<Vertices2> extractAllEdgeRunVertices(KeyValuePairList searchCriteria){
		// Use this method to get all the edge runs with
		// a new (and changed) searchCriteria from the previous search
		//
		theNetwork.clearSearchAttributes();
		theNetwork.setSearchAttribute(searchCriteria);
		initialiseEdgeList();
		return extractAllEdgeRunVertices();
	}
	
	ArrayList<Vertices2> getEdgeRunVertices(){
		if(isInitialised()==false) return null;
		return extractedVertices;
	}
	
	
	public void sortEdgeRuns(boolean shortestFirst) {
		if(isInitialised()==false) return;

		if(shortestFirst) {
			extractedVertices.sort(Comparator.comparing(Vertices2::getTotalLength));
		} else {
			extractedVertices.sort(Comparator.comparing(Vertices2::getTotalLength).reversed());
		}

	}
	
	public void setRunDirectionPreference(int preference) {
		if(isInitialised()==false) return;
		
		for(Vertices2 v: extractedVertices) {
			v.setRunDirectionPreference(preference);
		}
	}
	
	public void removeShortEdgeRuns(float minLength) {
		if(isInitialised()==false) return;
		if(minLength==0) return;
		
		ArrayList<Vertices2> toBeRemoved = new ArrayList<Vertices2>();
		for(Vertices2 v: extractedVertices) {
			if( v.getTotalLength() < minLength) toBeRemoved.add(v);
		}
		extractedVertices.removeAll(toBeRemoved);

	}
	
	
	private boolean isInitialised() {
		if(extractedVertices==null) {
			System.out.println("ERROR: EdgeRunVerticesCrawler: is not initilased -  call extractNetworkEdgeVertices or extractRegionEdgeVertices first");
			return false;
		}
		return true;
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// private??? methods 
	//
	//This method returns an ordered list of connected edges, starting at a random
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
		extractedEdges.addAll(edgeRun);
		
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
		// extracts an edge run in BOTH directions from the proposed startEdge
		//
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
		// given an edge, and a particular end to that edge, find connected edges
		// and keep going
		ArrayList<NEdge> connectedEdges = new ArrayList<NEdge>();
		NEdge currentEdge =  startEdge;
		NPoint otherPointInConnectedEdge = whichPoint;

		while(true) {
			NEdge  connectedEdge = getConnectedEdge(currentEdge, otherPointInConnectedEdge);// this pops the result from theEdgeList 

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
			
			if(runsCanOverlap == false && (pointIntersectsPreviouslyExtractedRun(otherPointInConnectedEdge) || pointIntersectsPreviouslyExtractedRun(connectionPoint)) ) break;
			
		}
		return connectedEdges;
	}

	boolean pointIntersectsPreviouslyExtractedRun(NPoint np) {
		if(extractedEdges==null) return false;
		for(NEdge e: extractedEdges) {
			if(e.containsPoint(np)) return true;
		}
		return false;
	}

	private NEdge getConnectedEdge(NEdge thisEdge, NPoint usingThisPoint) {
		//finds an single edge connected to point usingThisPoint of thisEdge
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
	
	
	
	///////////////////////////////////////////////////////////////////
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
	

}

