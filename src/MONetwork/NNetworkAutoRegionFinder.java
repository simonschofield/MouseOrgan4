package MONetwork;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import MOApplication.Surface;
import MOCompositing.RenderTarget;
import MOImage.KeyImageSampler;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOSpriteSeed.SpriteSeed;
import MOUtils.ColorUtils;
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

	// used for sorting edges according to angle to other edge
	class NEdgeWithValue{
		Object edge;
		float val;
		
		public NEdgeWithValue(Object e, float degs) {
			// TODO Auto-generated constructor stub
			edge = e;
			val = degs;
		}
		
		public float getVal() {
			return val;
			
		}
		
	}
	
	//ArrayList<NEdge> theEdgeListCopy;

	RenderTarget renderTarget;
	Surface theSurface;

	// common to this and region extractor
	//NNetworkAutoRegionFinder(NNetwork ntwk, KeyValuePairList searchCriteria){
	public NNetworkAutoRegionFinder(NNetwork ntwk, RenderTarget testDraw, Surface thesurface){
		super(ntwk);
		
		renderTarget = testDraw;
		theSurface = thesurface;
		this.draw(renderTarget, Color.BLACK);
		theSurface.repaint();
		
		
		removePreExisitingRegions();
		System.out.println("NNetworkAutoRegionFinder: removedPreExisitingRegions ");
		//removeAllDanglingEdges();
		
		this.draw(renderTarget, Color.RED);
		theSurface.repaint();
		System.out.println("NNetworkAutoRegionFinder: removedAllDanglingEdges ");
		//setSearchAttribute(searchCriteria);

		
		//findRegions(1);

		
		//findRegions(2);
		
		angleTest();
	} 


	
	void angleTest() {
		NEdge startEdge = theNetwork.findEdgeWithID(6);
		this.drawEdge(startEdge,  Color.PINK, 30, renderTarget);
		theSurface.repaint();
		
		int endCount0 = getConnectedEdgeCount(startEdge, 0);
		int endCount1 = getConnectedEdgeCount(startEdge, 1);
		
		int startEdgeEndPointNum = 0;
		if(endCount0 < endCount1) startEdgeEndPointNum = 1;
		
		System.out.println("end count of start edge , end 0 : " + endCount0 + ", end 1 : " + endCount1);
		Color[] cols = ColorUtils.getBasic12ColorPalette();
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
		
		ArrayList<NEdge> connectedEdges =  getConnectedEdges(startEdge, startEdgeEndPointNum);
		ArrayList<NEdgeWithValue> edgesWithAngles = new ArrayList<NEdgeWithValue>();
		int colNum = 0;
		for(NEdge e: connectedEdges) {
			this.drawEdge(e,  cols[colNum++], 6, renderTarget);
			float rads = angleBetweenEdges(startEdge, e);
			float degs = (float) Math.toDegrees(rads);
			//KeyValuePair kvp = new KeyValuePair("angle", degs);
			//e.addAttribute(kvp);
			NEdgeWithValue edgeWithAngle = new NEdgeWithValue(e,degs);
			edgesWithAngles.add(edgeWithAngle);
			System.out.println("angle between start edge and edge id " + e.getID() + " is " + degs);
		}
		
		edgesWithAngles.sort(Comparator.comparing(NEdgeWithValue::getVal));
		theSurface.repaint();
	}
	
	
	
	
	
	void findRegions(int pass) {

		int bailCount=0;
		while(findRegion(pass)) {
			bailCount++;
			System.out.println("NNetworkAutoRegionFinder: findRegions found " + bailCount + " pass " + pass);
			if(bailCount > 10) {
				System.out.println("NNetworkAutoRegionFinder: findRegions seems stuck in a loop");
				return;
			}
		}
		System.out.println("NNetworkAutoRegionFinder: findRegions found " + bailCount + " regions on pass " + pass);
	}

	boolean findRegion(int pass) {

		ArrayList<NEdge> thisRegionEdges = new ArrayList<NEdge>();
		
		NEdge startEdge  = getStartEdge(pass);
		
		
		
		
		if(startEdge==null) {
			// assume you have found and processed all the edges
			System.out.println("NO more regions!");
			return false;
		}
		
		startEdge.regionAssociationCount++;
		System.out.println("NNetworkAutoRegionFinder: findRegion found startEdge " + startEdge.toStr());
		thisRegionEdges.add(startEdge);
		NPoint startPoint = startEdge.p1;
		
		int startEdgeID = startEdge.getID();

		
		NEdge thisEdge = startEdge;
		NPoint thisEndPoint = thisEdge.p2;
		
	
		//this.drawEdge(startEdge,  Color.RED, 4, renderTarget);
		//theSurface.repaint();
	
		

		while(true) {
			NEdge connectedEdge =  getMostClockwiseConnectedEdge(thisEdge, thisEndPoint, pass);
			connectedEdge.regionAssociationCount++;
			thisRegionEdges.add(connectedEdge);
			
			NPoint connectedEdgefarPoint = connectedEdge.getOtherPoint(thisEndPoint);
			//this.drawEdge(connectedEdge,  Color.GREEN, 4, renderTarget);
			//theSurface.repaint();
			
			System.out.println("NNetworkAutoRegionFinder: findRegion connected edge " + connectedEdge.getID() + " start ID " + startEdgeID);
			if( connectedEdgefarPoint == startPoint ) {
				System.out.println("Found complete region");
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
	int getConnectedEdgeCount(NEdge thisEdge, int usingThisEnd) {
		return getConnectedEdges( thisEdge,  usingThisEnd).size();
	}

	ArrayList<NEdge> getConnectedEdges(NEdge thisEdge, int usingThisEnd){
		NPoint usingThisPoint = thisEdge.getEndNPoint(usingThisEnd);
		return getConnectedEdges( thisEdge,  usingThisPoint);
	}
	ArrayList<NEdge> getConnectedEdges(NEdge thisEdge, NPoint usingThisPoint){
		if(thisEdge.containsPoint(usingThisPoint)==false) {
			System.out.println("getConnectedEdges:: the edge does not contain the point requested");
			return null;
		}
		ArrayList<NEdge> connectedEdges = (ArrayList)usingThisPoint.getEdgeReferences().clone();
		connectedEdges.remove(thisEdge);
		return connectedEdges;
	}



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

}


