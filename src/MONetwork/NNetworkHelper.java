package MONetwork;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOImage.KeyImageSampler;
import MOImage.MOColor;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOSprite.Sprite;
import MOSprite.SpriteFont;
import MOSprite.SpriteSourceInterface;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;
import MOUtils.MOStringUtils;
import MOUtils.SortObjectWithValue;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// common network operations
//
//
//
public class NNetworkHelper {
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//This is use to draw the roads as lines
	//
	static QRandomStream randomStream = new QRandomStream(1);
	
	static NNetwork theCurrentNetwork;
	
	public static void setCurrentNetwork(NNetwork ntwk) {
		theCurrentNetwork = ntwk.copy();
	}
	
	public static KeyValuePair getDescriptor(String key, String val) {
		return new KeyValuePair(key, val);
	}
	
	public static void drawEdges(NNetwork ntwk, KeyValuePair descriptor, Color c, float fullScaleWidth) {
		// in this method the width is set as the full-scale width of the 
		
		
		ArrayList<NEdge> edges;
		if(descriptor != null) {
			edges = ntwk.getEdgesMatchingQuery(descriptor);
		} else {
			edges = theCurrentNetwork.getEdges();
		}
		
		
		float scaledWidth = fullScaleWidth * GlobalSettings.getSessionScale();
		drawEdges(edges, c, scaledWidth, GlobalSettings.getMainDocument().getMain());
	}
	
	public static void drawEdges(ArrayList<NEdge> edges, Color c, float width, RenderTarget rt) {
		float dash[] = { 4.0f };
		rt.getVectorShapeDrawer().setStrokeCapJoin(BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		//System.out.println("drawing num edges " + edges.size());
		for(NEdge e: edges) {
			//System.out.println(e.toStr());
			drawEdge(e,  c,  width, rt);
		}
		
	}
	
	public static void drawEdge(NEdge e, Color c, float width, RenderTarget rt) {
		//Rect viewPort = GlobalObjects.theSurface.getViewPortDocSpace();
		PVector p1 = e.getEndCoordinate(0);
	    PVector p2 = e.getEndCoordinate(1);
		//if(viewPort.isPointInside(p1)==false && viewPort.isPointInside(p2)==false) return;
		rt.drawLine(p1, p2, c, width);
	}
	
	public static void drawPoint(NPoint np, Color c, float radiusDocSpace, RenderTarget rt) {
		//Rect viewPort = GlobalObjects.theSurface.getViewPortDocSpace();
		float lineThicknessDocSpace = radiusDocSpace/3f;
		PVector docPoint = np.getPt();
		//if(viewPort.isPointInside(docPoint)==false) return;
		rt.drawCircle(docPoint, radiusDocSpace, c, c, lineThicknessDocSpace );
	}
	
	
	
	public ArrayList<NRegion> sortRegionsByArea(ArrayList<NRegion> regionsIn, boolean smallestFirst) {
		// if smallestFirst == true, the regions are sorted with smallest regions first
		// if smallestFirst == false, the regions are sorted with largest regions first
		SortObjectWithValue objectValueSorter = new SortObjectWithValue();
		
		for(NRegion nr : regionsIn) {
			float area = nr.getVertices().getArea();
			
			objectValueSorter.add(nr,area);
		}

		if(smallestFirst) {
			return objectValueSorter.getSorted();
		} else {
			return objectValueSorter.getReverseSorted();
		}

	}
	
	public static void drawRegions(NNetwork ntwk, float dilation, float width, float[] dashPattern) {
		
		ArrayList<Vertices2> verts  = convertRegionsToVertices2(ntwk.getRegions());
		RenderTarget rt = GlobalSettings.getMainDocument().getMain();
		if(dilation != 0) {
			verts = dilateVertices2(verts, dilation, false);
		}
		for(Vertices2 v: verts) {
			rt.drawVertices2NoFill(v, Color.BLACK, width, dashPattern);
		}
	}
	
	public static void drawVertices2NoFill(ArrayList<Vertices2> verts, float width, Color c, float[] dashPattern) {
		// if dashPattern is nulled, then uses default line
		RenderTarget rt = GlobalSettings.getMainDocument().getMain();
		for(Vertices2 v: verts) {
			rt.drawVertices2NoFill(v, c, width, dashPattern);
		}
	}
	
	public static void drawVertices2NoFill(Vertices2 verts, float width, Color c, float[] dashPattern) {
		// if dashPattern is nulled, then uses default line
		RenderTarget rt = GlobalSettings.getMainDocument().getMain();
		rt.drawVertices2NoFill(verts, c, width, dashPattern);
	}
	
	public static void renderEdgesAndSave(NNetwork theNetwork, float Awidth, float Bwidth, float Cwidth, float regionEdgesWidth, float boundaryWidth, Rect boundaryRect, boolean saveAsLayers) {

		String sessPth = GlobalSettings.getUserSessionPath();
		String sessName = GlobalSettings.mainSessionName;
		int CwidthI = (int)Cwidth ;
		int BwidthI = (int)Bwidth;
		int AwidthI = (int)Awidth;
		int otherWidthI = (int)regionEdgesWidth;
		String edgeWidths = AwidthI + "_" + BwidthI + "_" + CwidthI + "_" + otherWidthI;
		//String directoryname = sessPth + "Edges_" + edgeWidths; 

		//boolean res = MOStringUtils.createDirectory(directoryname);
		
		//String directoryPath = directoryname + "\\";
		
		KeyValuePair descriptorC = NNetworkHelper.getDescriptor("ROAD", "C");  
		drawEdges(theNetwork, descriptorC, Color.BLACK, Cwidth);
		
		

		KeyValuePair descriptoB = NNetworkHelper.getDescriptor("ROAD", "B");  
		drawEdges(theNetwork, descriptoB, Color.BLACK, Bwidth);
		
		

		KeyValuePair descriptorA = NNetworkHelper.getDescriptor("ROAD", "A");
		drawEdges(theNetwork, descriptorA, Color.BLACK, Awidth);
		
		

		KeyValuePair seaDescriptor = NNetworkHelper.getDescriptor("REGIONEDGE", "SEA");
		drawEdges(theNetwork, seaDescriptor, Color.BLACK, regionEdgesWidth); 
		KeyValuePair riverDescriptor = NNetworkHelper.getDescriptor("REGIONEDGE", "RIVER");
		drawEdges(theNetwork, riverDescriptor, Color.BLACK, regionEdgesWidth); 
		KeyValuePair parksDescriptor = NNetworkHelper.getDescriptor("REGIONEDGE", "PARK");
		drawEdges(theNetwork, parksDescriptor, Color.BLACK, regionEdgesWidth); 
		KeyValuePair lakeDescriptor = NNetworkHelper.getDescriptor("REGIONEDGE", "LAKE");
		drawEdges(theNetwork, lakeDescriptor, Color.BLACK, regionEdgesWidth); 
		
		// now remove everything outside the boundaryRect (gets rid of wide-line cap-butts extending over the boundary line)
		GlobalSettings.getMainDocument().getMain().clearOutsideRect(boundaryRect);
		
		KeyValuePair docEdge = NNetworkHelper.getDescriptor("REGIONEDGE", "document");
		drawEdges(theNetwork, docEdge, Color.BLACK, boundaryWidth); 
		
		if(saveAsLayers) {
			GlobalSettings.getMainDocument().getMain().saveRenderToFile(sessPth + sessName+ "_edges_" + edgeWidths + "_.png");
		}

	}
	
	public static KeyValuePairList getDocumentEdgesKVPList(boolean getDocEdges) {
		KeyValuePairList kvpl = new KeyValuePairList();
		
		if(getDocEdges==false) {
			kvpl.addKeyValue("ROAD", "C");  
			kvpl.addKeyValue("ROAD", "B");  
			kvpl.addKeyValue("ROAD", "A");
			kvpl.addKeyValue("REGIONEDGE", "SEA");
			kvpl.addKeyValue("REGIONEDGE", "RIVER");
			kvpl.addKeyValue("REGIONEDGE", "PARK");
			kvpl.addKeyValue("REGIONEDGE", "LAKE");
		} else {
			kvpl.addKeyValue("REGIONEDGE", "document");
		}
		
    	return kvpl;
    	
    }

	
	public static void setRegionAttributeUbanDensity(NNetwork ntwk, String densityImagePathAndName) {
		// first, find all the regions that have not yet been defined,
		ArrayList<NRegion> regions = getUnattributedRegions( ntwk);
		BufferedImage densityImage = ImageProcessing.loadImage(densityImagePathAndName);
		KeyImageSampler densitySampler = new KeyImageSampler(densityImage);
		
		//System.out.println("total region num after subtraction " + regions.size());
		for(NRegion r: regions) {
			
			PVector centre = r.getVertices().getExtents().getCentre();
			float val = densitySampler.getValue01DocSpace(centre);
			String density = "URBAN_DENSITY_LOW";
			if(val < 0.666f) density = "URBAN_DENSITY_MEDIUM";
			if(val < 0.333f) density = "URBAN_DENSITY_HIGH";
			KeyValuePair newRegionAttribute = new KeyValuePair("REGIONTYPE", density);
			r.addAttribute(newRegionAttribute);
		}
		
	}
	
	
	
	
	
	static ArrayList<NRegion> getUnattributedRegions(NNetwork ntwk) {
		// returns a fresh list of all the regions in the network that do not have a REGIONTYPE attribute
		ArrayList<NRegion> regions = (ArrayList<NRegion>) ntwk.getRegions().clone();
		
		KeyValuePair alreadyDefinedAtt = new KeyValuePair("REGIONTYPE", "*");
		ArrayList<NRegion> alreadyDefinedRegions = ntwk.getRegionsMatchingQuery(alreadyDefinedAtt);
		
		regions.removeAll(alreadyDefinedRegions);
		return regions;
	}
	
	
	////
	
	public static ArrayList<Vertices2> convertRegionsToVertices2(ArrayList<NRegion> regions) {
		ArrayList<Vertices2> vertices = new ArrayList<Vertices2>();
		int n=0;
		for(NRegion  r : regions) {

			Vertices2 v = r.getVertices();
			if(n < 10) {
				System.out.println(v.getStats());

			}
			n++;
			v.setPolygonWindingDirection(Vertices2.CLOCKWISE);
			vertices.add( v );
		}
		return vertices;

	}
	
	public static ArrayList<Vertices2> dilateVertices2(ArrayList<Vertices2> vertsIn, float dilation, boolean relativeToOwnSize) {
		// if relativeToOwnWidth == false, the dilation is in docSpace units
		// if relativeToOwnWidth == true, the diagonal size is measured. This gives an approximate measure of width and height of the region
		// This is TBD and is very experimental
		ArrayList<Vertices2> vertsOut = new ArrayList<Vertices2>();
	
	    for(Vertices2  v : vertsIn) {
	    	
	    	if(relativeToOwnSize) {
	    		
	    	
	    	}
	    	
	    	
	    	Vertices2 dilated = v.getDilated( dilation);
	    	vertsOut.add( dilated );
	    }
	    return vertsOut;
	
	}
	
	
	public static void drawEdgesRandomColorWithPoints(ArrayList<NEdge> edges, float w, RenderTarget rt) {
		for(NEdge e: edges) {
			drawEdgeRandomColorWithPoints( e,  w,  rt);
		}
		
	}
	
	public static void drawEdgeRandomColorWithPoints(NEdge e, float w, RenderTarget rt) {
		
		
		Color c = MOColor.getRandomRGB();		
		drawEdge(e, c, w, rt);
		
		drawPoint(e.p1, Color.RED, 0.001f, rt);
		drawPoint(e.p2, Color.RED, 0.001f, rt);
		
	}
	
	static boolean updateDrawRoadLines(SpriteSourceInterface spriteFont, EdgeRunVerticesCrawler edgeCrawler, float crawlStep, float overlap1, float overlap2) {
		// draws the item on the line of the road


		edgeCrawler.setCrawlStep(crawlStep);
		Line2 line = edgeCrawler.updateCrawl();

		if(line == null){
			//endUserSession();
			return false;
		}

		Sprite sprite =  spriteFont.getSpriteInstance();

		if(randomStream.randomEvent(0.5f)) sprite.mirror(true);

		sprite.mapToLine2(line,  overlap1, overlap2, 0.005f);


		if( GlobalSettings.getMainDocument().cropSpriteToBoarder(sprite) ) GlobalSettings.getMainDocument().getMain().pasteSprite(sprite);

		return true;
		//theDocument.getMain().drawLine(line.p1, line.p2, Color.RED, 5);

	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//This is use to place items on the road
	//
	static void updateDrawRoadItems(SpriteFont seedFont, EdgeRunVerticesCrawler edgeCrawler, float crawlStep, float probability, float scaleModifierLo, float scaleModifierHi, float rotationLo, float rotationHi) {
		// quite specific this one.... if the item contains the string "side"
		// the it is rotated as if growing out of the road. else it draws it
		// on the road.
		SpriteFont currentSpriteFont = seedFont;
		edgeCrawler.setCrawlStep(crawlStep);
		Line2 line = edgeCrawler.updateCrawl();
		if(line == null){
			//endUserSession();
			return;
		}



		Sprite sprite = currentSpriteFont.getSpriteInstance();

		// do this by loaded 2 different sprite fonts, one for side on
		//if( sprite.data.spriteImageGroupItemShortName.contains("side")) {
		//	sprite.data.origin = new PVector(0.5f,1f);
		//}


		float scaleModifier = randomStream.randRangeF(scaleModifierLo, scaleModifierHi);
		sprite.scaleToSizeInScene(scaleModifier);



		if(randomStream.randomEvent(probability)) {
			float rot = line.getRotation();  
			rot += randomStream.randRangeF(rotationLo, rotationHi);
			sprite.rotate(rot);
			sprite.setDocPoint(line.p1);
			//SceneHelper.addRandomHSV( sprite,  0.1f,  0.2f,  0.0f);
			if( GlobalSettings.getMainDocument().cropSpriteToBoarder(sprite) ) GlobalSettings.getMainDocument().getMain().pasteSprite(sprite);

		}

	}




}
