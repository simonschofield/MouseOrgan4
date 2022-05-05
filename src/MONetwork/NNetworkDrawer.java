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

// a class containing common useful methods used in processing NNetwork
// Attribute searching, so you can select a sub-group of the network for processing based on attribute.
// Drawing the network - this has been primarily for debugging, but could be extended to me more polished.
public class NNetworkDrawer{
	
	NNetwork theNetwork;
	
	public NNetworkDrawer(NNetwork ntwk){
		theNetwork = ntwk;
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

	public void drawEdges(RenderTarget rt, Color c) {
		ArrayList<NEdge> edges = theNetwork.getEdges();
	
		for (int n = 0; n < edges.size(); n++) {
			NEdge e = edges.get(n);
			if(c==null)c = getEdgeColor(e);
			int lineWt = getEdgeLineWeight(e)*3;
			drawEdge(e,c,lineWt,rt);
		}

	}
	
	public void drawRegionsRandomColors(RenderTarget rt) {
		
		
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
	    
	    	
			drawRegionFill(r, cols[colNum++], rt);
			if(colNum>10) colNum = 0;
		}
		
		for(NRegion r: regions) {
			drawRegionEdges(r, Color.black, 5, rt);
		}
		//draw(rt, Color.WHITE);
		//theNetwork.save("C:\\simon\\Artwork\\MouseOrgan4\\Maps\\Network Maps\\London Flowers\\regionFindTest.csv");
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
		
		rt.drawVertices2NoFill(verts, c, width);
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

