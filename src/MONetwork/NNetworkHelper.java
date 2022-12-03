package MONetwork;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOAppSessionHelpers.SceneHelper;
import MOCompositing.RenderTarget;
import MOCompositing.TextRenderer;
import MOImage.ImageProcessing;
import MOImage.KeyImageSampler;
import MOImage.MOColor;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOSprite.Sprite;
import MOSprite.SpriteFont;
import MOSprite.SpriteSourceInterface;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;
import MOUtils.MOStringUtils;
import MOUtils.ObjectWithValueList;
import MOUtils.TextBank;
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
	

	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DRAWING
	//
	//
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Drawing Points
	//
	public static void drawPoint(NPoint np, Color c, float radiusDocSpace, RenderTarget rt) {
		//Rect viewPort = GlobalObjects.theSurface.getViewPortDocSpace();
		float lineThicknessDocSpace = radiusDocSpace/3f;
		PVector docPoint = np.getPt();
		//if(viewPort.isPointInside(docPoint)==false) return;
		rt.drawCircle(docPoint, radiusDocSpace, c, c, lineThicknessDocSpace );
	}

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Drawing Edges
	//
	public static void drawEdges(NNetwork ntwk, KeyValuePair descriptor, Color c, float fullScaleWidth) {

		ArrayList<NEdge> edges;
		if(descriptor != null) {
			edges = ntwk.getEdgesMatchingQuery(descriptor);
		} else {
			edges = ntwk.getEdges();
		}

		float scaledWidth = fullScaleWidth * GlobalSettings.getSessionScale();
		drawEdges(edges, c, scaledWidth, GlobalSettings.getDocument().getMain());
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
	
	public static void drawEdgesRandomColorWithPoints(ArrayList<NEdge> edges, float w, RenderTarget rt) {
		for(NEdge e: edges) {
			drawEdgeRandomColorWithPoints( e,  w,  rt);
		}
		
	}
	
	public static void drawEdgeRandomColorWithPoints(NEdge e, float w, RenderTarget rt) {
		Color c = MOColor.getRandomRGB(255);		
		drawEdge(e, c, w, rt);
		
		drawPoint(e.p1, Color.RED, 0.001f, rt);
		drawPoint(e.p2, Color.RED, 0.001f, rt);
	}
	
	public static void renderEdgesAndSave(NNetwork theNetwork, float Awidth, float Bwidth, float Cwidth, float regionEdgesWidth, float boundaryWidth, Rect boundaryRect, boolean saveRender) {
		// very high-level method for rendering out the whole 
		// of the edges in a network
		//
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
		
		KeyValuePair descriptorC = NNetworkHelper.getKVP("ROAD", "C");  
		drawEdges(theNetwork, descriptorC, Color.BLACK, Cwidth);
		
		

		KeyValuePair descriptoB = NNetworkHelper.getKVP("ROAD", "B");  
		drawEdges(theNetwork, descriptoB, Color.BLACK, Bwidth);
		
		

		KeyValuePair descriptorA = NNetworkHelper.getKVP("ROAD", "A");
		drawEdges(theNetwork, descriptorA, Color.BLACK, Awidth);
		
		

		KeyValuePair seaDescriptor = NNetworkHelper.getKVP("REGIONEDGE", "SEA");
		drawEdges(theNetwork, seaDescriptor, Color.BLACK, regionEdgesWidth); 
		KeyValuePair riverDescriptor = NNetworkHelper.getKVP("REGIONEDGE", "RIVER");
		drawEdges(theNetwork, riverDescriptor, Color.BLACK, regionEdgesWidth); 
		KeyValuePair parksDescriptor = NNetworkHelper.getKVP("REGIONEDGE", "PARK");
		drawEdges(theNetwork, parksDescriptor, Color.BLACK, regionEdgesWidth); 
		KeyValuePair lakeDescriptor = NNetworkHelper.getKVP("REGIONEDGE", "LAKE");
		drawEdges(theNetwork, lakeDescriptor, Color.BLACK, regionEdgesWidth); 
		
		KeyValuePair undefinedDescriptor = NNetworkHelper.getKVP("REGIONEDGE", "UNDEFINED");
		drawEdges(theNetwork, undefinedDescriptor, Color.BLACK, regionEdgesWidth); 
		
		GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
		
		// now remove everything outside the boundaryRect (gets rid of wide-line cap-butts extending over the boundary line)
		if(boundaryRect!=null) {
			GlobalSettings.getDocument().getMain().clearOutsideRect(boundaryRect);
		}
		
		KeyValuePair docEdge = NNetworkHelper.getKVP("REGIONEDGE", "document");
		drawEdges(theNetwork, docEdge, Color.BLACK, boundaryWidth); 
		
		if(saveRender) {
			GlobalSettings.getDocument().getMain().saveRenderToFile(sessPth + sessName+ "_edges_" + edgeWidths + "_.png");
		}

	}
	
	
	
	public static void renderDashedEdges(NNetwork theNetwork, float baseLineWidth, float dashScale,  boolean saveImage) {
		
		
	    	float[] singlePhaseA = { 11f,4f };
	    	float[] dashPatternA = stochasticDashPattern(singlePhaseA, 0.2f, 5, dashScale);
	    	
	    	float[] singlePhaseB = { 9f,4f,6f,4f };
	    	float[] dashPatternB = stochasticDashPattern(singlePhaseB, 0.2f, 5, dashScale);
	    	
	    	float[] singlePhaseC = { 7f,4f };
	    	float[] dashPatternC = stochasticDashPattern(singlePhaseC, 0.2f, 5, dashScale);
	    	
	    	float[] singlePhaseOthers = { 7f,3f,4f,3f };
	    	float[] dashPatternOthers = stochasticDashPattern(singlePhaseOthers, 0.2f, 5, dashScale);
	    	

	    	float lineWidth = baseLineWidth;
	    	float dashProjection = baseLineWidth * 2f;
	    	
	    	// draw the dashed lines
	    	NNetworkEdgeRunFinder edgeRunFinder = new NNetworkEdgeRunFinder(theNetwork, null);
	    	ArrayList<Vertices2> verts;
	    	KeyValuePairList edgeType = new KeyValuePairList();
	    	
	    	edgeType.addKeyValue("ROAD", "A");
	    	verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
	    	drawVertices2ListNoFill(verts, lineWidth*4 + dashProjection + 2, Color.BLACK, dashPatternA);
	    	
	    	edgeType.removeAll();
	    	edgeType.addKeyValue("ROAD", "B");
	    	verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
	    	drawVertices2ListNoFill(verts, lineWidth*3 + dashProjection, Color.BLACK, dashPatternB);
	    	
	    	edgeType.removeAll();
	    	edgeType.addKeyValue("ROAD", "C");
	    	verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
	    	 
	    	drawVertices2ListNoFill(verts, lineWidth*2 + dashProjection-2, Color.BLACK, dashPatternC);
	    	
	    	edgeType.removeAll();
	    	edgeType.addKeyValue("REGIONEDGE", "RIVER");
			edgeType.addKeyValue("REGIONEDGE", "PARK");
			edgeType.addKeyValue("REGIONEDGE", "LAKE");
			edgeType.addKeyValue("REGIONEDGE", "SEA");
			verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
			drawVertices2ListNoFill(verts, lineWidth*2 + dashProjection-2, Color.BLACK, dashPatternOthers);
	    	
	    	
	    	edgeType.removeAll();
	    	edgeType.addKeyValue("REGIONEDGE", "document");
	    	verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
	    	drawVertices2ListNoFill(verts, lineWidth*4 + dashProjection , Color.BLACK, dashPatternA);
	    	
	    	GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
	    	
	    	if(saveImage) {
	    		
	    		GlobalSettings.getDocument().getMain().saveRenderToFile(GlobalSettings.getUserSessionPath() + "dashed edges.png");
	    		GlobalSettings.getDocument().getMain().clearImage();
	    	
	    	}

	    	edgeType.removeAll();
	    	edgeType.addKeyValue("ROAD", "A");
	    	verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
	    	drawVertices2ListNoFill(verts, lineWidth*4, Color.WHITE, null);
	    	
	    	edgeType.removeAll();
	    	edgeType.addKeyValue("ROAD", "B");
	    	verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
	    	drawVertices2ListNoFill(verts, lineWidth*3, Color.WHITE, null);
	    	
	    	edgeType.removeAll();
	    	edgeType.addKeyValue("ROAD", "C");
	    	verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
	    	drawVertices2ListNoFill(verts, lineWidth*2, Color.WHITE, null);
	    	
	    	
	    	edgeType.removeAll();
	    	edgeType.addKeyValue("REGIONEDGE", "RIVER");
			edgeType.addKeyValue("REGIONEDGE", "PARK");
			edgeType.addKeyValue("REGIONEDGE", "LAKE");
			edgeType.addKeyValue("REGIONEDGE", "SEA");
			verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
			drawVertices2ListNoFill(verts, lineWidth*2 , Color.WHITE, null);
	    	
	    	
	    	edgeType.removeAll();
	    	edgeType.addKeyValue("REGIONEDGE", "document");
	    	verts = edgeRunFinder.extractAllEdgeRunVertices(edgeType);
	    	drawVertices2ListNoFill(verts, lineWidth*4, Color.WHITE, null);
	    	
	    	//GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
	    	
	    	
	    	if(saveImage) GlobalSettings.getDocument().getMain().saveRenderToFile(GlobalSettings.getUserSessionPath() + "dashed edges white background.png");
	    	
		
	}
	
	private static float[] stochasticDashPattern(float[] singlePhase, float perturb, int numRepeats, float masterScale) {
		RandomStream ran = new RandomStream(1);
		// perturbation is the fractional part of the dash, so min_length = dash - (dash*perturb) , max_len = dash + (dash*perturb)
		
		// first build the full pattern
		int lenSinglePhase = singlePhase.length;
		float[] pattern = new float[lenSinglePhase*numRepeats];
		
		for(int n=0; n < pattern.length; n++) {
			float dash = singlePhase[n%lenSinglePhase]*masterScale;
			
			float rdash = dash; //ran.perturb(dash, perturb);
			System.out.print(rdash + " ");
			pattern[n]=rdash;
		}
		System.out.println(" dashes");
		return pattern;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Doing text ribbons
	// 
	public static void drawTextRibbons(NNetwork theNetwork, KeyValuePairList searchCriteria, String textPath, float fontheight, float minimumRibbonLength, boolean updateGraphicsDuring) {
		TextBank textbank = new TextBank();
	    String textPathAndName = GlobalSettings.getDataAssetsPath(null) + "text documents\\Heart of darkness.txt";
	    
	    System.out.println("Drawing text ribbons ");	 
	    
	    textbank.load(textPathAndName);
	    textbank.setIterator(0);


	    TextRenderer textRenderer = new TextRenderer();
	    textRenderer.setFont("Serif", 0, 250, Color.BLACK);

	   // float backingRoadWidth = SceneHelper.docSpaceToFullScalePixels(fontheight)*0.4f;
	    //System.out.println("Road width is " + backingRoadWidth);
	    //System.out.println("search criteria is " + searchCriteria.getAsCSVLine());
	   // System.out.println("text bank is " + textbank);
	    
	    
	    
	    TextRibbonManager theTextRibbonManager = new TextRibbonManager();
	    ArrayList<Vertices2> edgeRunVertices = theTextRibbonManager.createVerticesFromNetworkEdges(theNetwork, searchCriteria, 0.00f);
		
		
	    
		boolean showUnderlyingVertices = false;
	    if(showUnderlyingVertices) {
	    	NNetworkHelper.drawVertices2ListNoFill(edgeRunVertices, 10, null, null);
	    	GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
	    	//return;
	    }
	    
	   
	    theTextRibbonManager.createTextRibbons(edgeRunVertices, textbank, textRenderer, fontheight, fontheight/2f);

	    
	      
	    int lastProgress=-1;

		while(true) {

			RibbonLetter ribbonLetter = theTextRibbonManager.getNextRibbonLetter();
			if(ribbonLetter==null) return;

			float len = ribbonLetter.theOwningTextRibbon.theVertices.getTotalLength();
			
			Sprite sprite =  textRenderer.getSprite(ribbonLetter.theChar, fontheight, ribbonLetter.getCharLine(), null);
			GlobalSettings.getDocument().getMain().pasteSprite(sprite);
			
			
			if(len < minimumRibbonLength) return;
			
			int progress = (int)(theTextRibbonManager.getRibbonLetterIterationProgress()*20);
			if(progress!=lastProgress) {
				System.out.println("progress "+ progress*5 + "%");
				if(updateGraphicsDuring) {
					GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
				}
				lastProgress= progress;
				
			}
		}
		
	}
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Drawing Regions
	//
	public static void drawRegions(NNetwork ntwk, float dilation, float width, float[] dashPattern) {

		ArrayList<Vertices2> verts  = convertRegionsToVertices2(ntwk.getRegions());
		RenderTarget rt = GlobalSettings.getDocument().getMain();
		if(dilation != 0) {
			verts = dilateVertices2(verts, dilation, false);
		}
		for(Vertices2 v: verts) {
			rt.drawVertices2NoFill(v, Color.BLACK, width, dashPattern);
		}
	}
	
	public static void drawRegionsByType(NNetwork ntwk,  boolean randomiseUrbanCol) {
		// for debug only
		
		ArrayList<NRegion> regions = ntwk.getRegions();

		int colNum = 0;
		for(NRegion r: regions) {
			Color c = getRegionDefaultColour(r, randomiseUrbanCol);
	    	
			drawRegionFill(r, c, GlobalSettings.getDocument().getMain());
			if(colNum>10) colNum = 0;
		}
	}
	
	
	public static void drawRegionFill(NRegion r, Color c, RenderTarget rt) {
		Vertices2 verts = r.getVertices();
		rt.drawVertices2(verts, c, c, 1);
	}

	public static void drawVertices2ListNoFill(ArrayList<Vertices2> verts, float width, Color c, float[] dashPattern) {
		// if dashPattern is nulled, then uses default line
		// if color is nulled the uses a random color (for debug probably)
		RenderTarget rt = GlobalSettings.getDocument().getMain();
		Color col;
		for(Vertices2 v: verts) {
			
			if(c==null) {
				col = MOColor.getRandomRGB(100);
			
			} else {
				col = c;
			}
			
			rt.drawVertices2NoFill(v, col, width, dashPattern);
			//rt.drawPoint(v.get(0), col, width+2);
		}
	}

	public static void drawVertices2NoFill(Vertices2 verts, float width, Color c, float[] dashPattern) {
		// if dashPattern is nulled, then uses default line
		RenderTarget rt = GlobalSettings.getDocument().getMain();
		
		if(c==null) c = MOColor.getRandomRGB(100);
		
		
		rt.drawVertices2NoFill(verts, c, width, dashPattern);
	}
	
	


	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Geometric stuff
	//
	public static ArrayList<NEdge> findCrossingEdges(NEdge thisEdge, ArrayList<NEdge> otherEdges) {
		ArrayList<NEdge> crossingEdges = new ArrayList<NEdge>();
		Line2 thisLine = thisEdge.getLine2();
		Line2 otherLine;
		for(NEdge e: otherEdges) {
			if(e == thisEdge) continue;
			otherLine = e.getLine2();
			if( thisLine.isIntersectionPossible(otherLine) == false ) continue;
			
			if( thisLine.calculateIntersection(otherLine) && thisLine.isConnected(otherLine) == false) {
				crossingEdges.add(e);
			}
		}
		
		return crossingEdges;
	}
	
	public ArrayList<NRegion> sortRegionsByArea(ArrayList<NRegion> regionsIn, boolean smallestFirst) {
		// if smallestFirst == true, the regions are sorted with smallest regions first
		// if smallestFirst == false, the regions are sorted with largest regions first
		ObjectWithValueList objectValueSorter = new ObjectWithValueList();
		
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

	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities to do with KVP types
	//
	
	public static KeyValuePair getKVP(String key, String val) {
		return new KeyValuePair(key, val);
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
	
	
	public static KeyValuePairList  getTaggedRegionsKVPList() {
		KeyValuePairList regType = new KeyValuePairList();
		regType.addKeyValue("REGIONEDGE", "RIVER");
		regType.addKeyValue("REGIONEDGE", "PARK");
		regType.addKeyValue("REGIONEDGE", "LAKE");
		regType.addKeyValue("REGIONEDGE", "SEA");
		return regType;
	}

	public static Color getRegionDefaultColour(NRegion r, boolean randomiseUrbanColours) {
		// for debug only
		Color c = Color.WHITE;
		
		KeyValuePair parkAttribute = new KeyValuePair("REGIONTYPE", "PARK");
		if(r.thisItemContainsMatch(parkAttribute)) {
			return Color.green;
		}
		KeyValuePair lakeAttribute = new KeyValuePair("REGIONTYPE", "LAKE");
		if(r.thisItemContainsMatch(lakeAttribute)) {
			return Color.blue;
		}
		KeyValuePair riverAttribute = new KeyValuePair("REGIONTYPE", "RIVER");
		if(r.thisItemContainsMatch(riverAttribute)) {
			return Color.blue;
		}
		
		
		
		KeyValuePair seaAttribute = new KeyValuePair("REGIONTYPE", "SEA");
		if(r.thisItemContainsMatch(seaAttribute)) {
			return Color.blue;

		}
		
		
		Color randomRGB = MOColor.getRandomRGB(255);
		float blendAmt = 0;
		if(randomiseUrbanColours) blendAmt = 0.5f;
		KeyValuePair densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_HIGH");
		if(r.thisItemContainsMatch(densityAttribute)) {
			return MOColor.blendColor(blendAmt, Color.red, randomRGB);
		}
		densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_MEDIUM");
		if(r.thisItemContainsMatch(densityAttribute)) {
			
			return MOColor.blendColor(blendAmt, Color.orange, randomRGB);
		}
		densityAttribute = new KeyValuePair("REGIONTYPE", "URBAN_DENSITY_LOW");
		if(r.thisItemContainsMatch(densityAttribute)) {
			return MOColor.blendColor(blendAmt, Color.yellow, randomRGB);
		}
		
		return c;
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
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods using edge crawler. 
	//
		
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//This is use to place items along the road
	//
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


		if( GlobalSettings.getDocument().cropSpriteToBoarder(sprite) ) GlobalSettings.getDocument().getMain().pasteSprite(sprite);

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
			if( GlobalSettings.getDocument().cropSpriteToBoarder(sprite) ) GlobalSettings.getDocument().getMain().pasteSprite(sprite);

		}

	}




}
