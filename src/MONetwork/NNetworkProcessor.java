package MONetwork;

import java.awt.Color;
import java.util.ArrayList;

import MOImage.RenderTarget;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;

//base class containing common useful methods used in processing NNetwork
public class NNetworkProcessor{
	
	NNetwork theNetwork;
	KeyValuePairList currentSearchAttributes = new KeyValuePairList();
	KeyValuePair hasBeenProcessedFlag;
	
	NNetworkProcessor(NNetwork ntwk){
		theNetwork = ntwk;
		hasBeenProcessedFlag = new KeyValuePair();
		hasBeenProcessedFlag.set("PROCESSED", true);
	}
	
	
	
	
	
	void setAsProcessed(NAttributes e) {
		e.getAttributes().addKeyValuePair(hasBeenProcessedFlag.copy());
	}
	
	boolean hasBeenProcessed(NAttributes e) {
		return e.getAttributes().containsEqual(hasBeenProcessedFlag);
		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////
	// attribute matching
	// currently only uses a single attribute, so cannot do complex checks (like if contains K:ROAD and V: A OR B)
	// the attributes e contains a KVP list, so if currentSearchAttribute was also a list
	
	void setSearchAttribute(KeyValuePair kvp) {
		if(kvp == null) return;
		currentSearchAttributes.addKeyValuePair(kvp.copy());
		System.out.println("NNetworkProcessor: added to currentSearchAttribute = " + kvp.getKey());
	}
	
	void setSearchAttribute(KeyValuePairList kvpl) {
		if(kvpl == null) return;
		currentSearchAttributes=kvpl.copy();
		//System.out.println("NNetworkProcessor: added to currentSearchAttribute = " + kvp.theKey);
	}
	
	void clearSarchAttributes() {
		currentSearchAttributes.removeAll();
	}
	
	
	
	
	boolean isMatchingSearchAttribute(NAttributes e) {
		 if(currentSearchAttributes==null) return true;
		 boolean result = e.getAttributes().containsEqual(currentSearchAttributes);
		 
		 System.out.println("comparing incoming item");
		 e.printAttributes();
		 System.out.println("to current attributes");
		 currentSearchAttributes.printMe();
		 System.out.println("result = " + result);
		 
		 
		 return result;
	}
	
	
	
	
	
	ArrayList<NEdge> getEdgesMatching(){
		
		ArrayList<NEdge> edges = theNetwork.getEdges();
		ArrayList<NEdge> matchingEdges = new ArrayList<NEdge>();
		for (int n = 0; n < edges.size(); n++) {
			NEdge e = edges.get(n);
			if(isMatchingSearchAttribute(e) ) matchingEdges.add(e);
		}
		return matchingEdges;
	}
	

	ArrayList<NPoint> getPointsMatching(){
		
		ArrayList<NPoint> points = theNetwork.getPoints();
		ArrayList<NPoint> matchingPoints = new ArrayList<NPoint>();
		for (int n = 0; n < points.size(); n++) {
			NPoint p = points.get(n);
			if(isMatchingSearchAttribute(p) ) matchingPoints.add(p);
		}
		return matchingPoints;
	}
	
	
	ArrayList<NRegion> getRegionsMatching(){
		
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
		
		NPoint connectingPoint = e1.getConnectingPoint(e2);
		
		NPoint otherPointE1 = e1.getOtherPoint(connectingPoint);
		NPoint otherPointE2 = e2.getOtherPoint(connectingPoint);
		Line2 l1 = new Line2(otherPointE1.getPt(), connectingPoint.getPt());
		Line2 l2 = new Line2(connectingPoint.getPt(), otherPointE2.getPt());
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
	
	// turns an array list of NEdges, which should be sequential (edges link, but by either end, so, the raw end points are not sequential), into an ordered
	// list of PVector points by finding the joining points between each edge pair.
	Vertices2 getVertices(ArrayList<NEdge> edges){
		if( checkEdgesAreOrdered(edges)==false) {
			System.out.println("getVertices - edges are not ordered");
			return null;
		}
		ArrayList<PVector> pointList = new ArrayList<PVector>();
		
		NEdge thisEdge = edges.get(0);
		if(edges.size()==1) {
			pointList.add(thisEdge.getEndPt(0));
			pointList.add(thisEdge.getEndPt(1));
			return new Vertices2(pointList);
		}
		
		
		
		NEdge nextEdge = edges.get(1);
		
		// find first dangling vertex
		NPoint connectionPoint = getEdgeJoinPoint(thisEdge, nextEdge);
		NPoint firstPoint = getOtherEdgeEnd(thisEdge, connectionPoint);
		pointList.add(firstPoint.getPt());
		
		
		
		int numEdges = edges.size();
		// find all the connecting points in order
		for(int n = 1; n < numEdges; n++) {
			nextEdge = edges.get(n);
			connectionPoint = getEdgeJoinPoint(thisEdge, nextEdge);
			pointList.add(connectionPoint.getPt());
			thisEdge = nextEdge;
		}
		
		// find final dangling point
		NPoint finalPoint = getOtherEdgeEnd(nextEdge, connectionPoint);
		pointList.add(finalPoint.getPt());
		
		return new Vertices2(pointList);
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// draw-all -type methods
	//
	void draw(RenderTarget rt) {
		drawPoints(rt);
		drawEdges(rt);
	}
	
		
	void drawPoints(RenderTarget rt)	{
		ArrayList<NPoint> points = theNetwork.getPoints();
		for (int n = 0; n < points.size(); n++) {
			NPoint np = points.get(n);
			float radiusDocSpace = 0.0002f;
			drawPoint( np, Color.RED, radiusDocSpace, rt);
		}
	}

	void drawEdges(RenderTarget rt) {
		ArrayList<NEdge> edges = theNetwork.getEdges();
	
		for (int n = 0; n < edges.size(); n++) {
			NEdge e = edges.get(n);
			Color c = getEdgeColor(e);
			int lineWt = getEdgeLineWeight(e)*3;
			drawEdge(e,c,lineWt,rt);
		}

	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// more specific drawing 
	//
	void drawPoint(NPoint np, Color c, float radiusDocSpace, RenderTarget rt) {
		//Rect viewPort = GlobalObjects.theSurface.getViewPortDocSpace();
		float lineThicknessDocSpace = radiusDocSpace/3f;
		PVector docPoint = np.getPt();
		//if(viewPort.isPointInside(docPoint)==false) return;
		rt.drawCircle(docPoint, radiusDocSpace, c, c, lineThicknessDocSpace );
	}
	

	void drawEdge(NEdge e, Color c, int width, RenderTarget rt) {
		//Rect viewPort = GlobalObjects.theSurface.getViewPortDocSpace();
		PVector p1 = e.getEndPt(0);
	    PVector p2 = e.getEndPt(1);
		//if(viewPort.isPointInside(p1)==false && viewPort.isPointInside(p2)==false) return;
		rt.drawLine(p1, p2, c, width);
	}
	
	void drawEdges(ArrayList<NEdge> edges, Color c, int width, RenderTarget rt) {
		//System.out.println("drawing num edges " + edges.size());
		for(NEdge e: edges) {
			//System.out.println(e.toStr());
			drawEdge(e,  c,  width, rt);
		}
	}
	
	
	void drawRegion(NRegion r, Color c, int width, RenderTarget rt) {
		int numEdges = r.getNumEdges();
		for(int n = 0; n < numEdges; n++) {
			NEdge e = r.getEdge(n);
			drawEdge( e, c, width, rt);
		}
	}
	
	void drawRegions(ArrayList<NRegion> regions, Color c, int width, RenderTarget rt) {
		for(NRegion r: regions) {
			drawRegion(r,  c,  width, rt);
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
	    if(roadType.equals("F")){
		      return new Color(78,209,127);
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
	    if(roadType.equals("F")){
		      return 2;
		    }
	    return 2;
	  }

}

