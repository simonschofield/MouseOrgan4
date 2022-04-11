package MONetwork;

import java.awt.Color;
import java.util.ArrayList;

import MOCompositing.RenderTarget;
import MOImage.MOColor;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;

// base class containing common useful methods used in processing NNetwork
// Attribute searching, so you can select a sub-group of the network for processing based on attribute.
// Drawing the network - this has been primarily for debugging, but could be extended to me more polished.
public class NNetworkProcessor{
	
	NNetwork theNetwork;
	KeyValuePairList currentSearchAttributes = new KeyValuePairList();
	
	
	public NNetworkProcessor(NNetwork ntwk){
		theNetwork = ntwk;
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
	// draw-all -type methods
	//
	public void draw(RenderTarget rt, Color c) {
		drawPoints(rt, c);
		drawEdges(rt, c);
	}
	
		
	void drawPoints(RenderTarget rt, Color c)	{
		ArrayList<NPoint> points = theNetwork.getPoints();
		for (int n = 0; n < points.size(); n++) {
			NPoint np = points.get(n);
			float radiusDocSpace = 0.0002f;
			drawPoint( np, c, radiusDocSpace, rt);
		}
	}

	void drawEdges(RenderTarget rt, Color c) {
		ArrayList<NEdge> edges = theNetwork.getEdges();
	
		for (int n = 0; n < edges.size(); n++) {
			NEdge e = edges.get(n);
			if(c==null)c = getEdgeColor(e);
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
		PVector p1 = e.getEndCoordinate(0);
	    PVector p2 = e.getEndCoordinate(1);
		//if(viewPort.isPointInside(p1)==false && viewPort.isPointInside(p2)==false) return;
		rt.drawLine(p1, p2, c, width);
	}
	
	public void drawEdges(ArrayList<NEdge> edges, Color c, int width, RenderTarget rt) {
		//System.out.println("drawing num edges " + edges.size());
		for(NEdge e: edges) {
			//System.out.println(e.toStr());
			drawEdge(e,  c,  width, rt);
		}
	}
	
	
	public void drawRegionEdges(NRegion r, Color c, int width, RenderTarget rt) {
		Vertices2 verts = r.getVertices();
		
		rt.drawVertices2(verts, c, width);
	}
	
	public void drawRegionListEdges(ArrayList<NRegion> regions, Color c, int width, RenderTarget rt) {
		for(NRegion r: regions) {
			drawRegionEdges(r,  c,  width, rt);
		}
		
	}
	
	
	public void drawRegionFill(NRegion r, Color c, RenderTarget rt) {
		Vertices2 verts = r.getVertices();
		rt.drawVertices2(verts, c, c, 1);
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

