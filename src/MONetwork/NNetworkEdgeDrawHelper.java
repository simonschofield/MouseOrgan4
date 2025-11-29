package MONetwork;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.TextRenderer;
import MOImage.ImageProcessing;
import MOImage.MOColor;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOSprite.Sprite;
import MOSprite.SpriteFont;
//import MOSprite.SpriteSourceInterface;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;
import MOUtils.TextBank;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// common network operations
//
//
//
public class NNetworkEdgeDrawHelper {
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//This is use to draw the roads as lines
	//
	static QRandomStream randomStream = new QRandomStream(1);

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// General helper functions
	//
	//


	/**
	 * @param pathAndFilename  - the path and filename of the network. Should be a .csv file
	 * @param roi -  Nullable. The Region Of Interest expressed in docSpace Coordinates - as all networks are square, this is the same as normalised anyway.
	 * @return - The loaded NNetwork network cropped to the ROI, and mapped to fill the whole document
	 */
	public static NNetwork  loadNetwork(String pathAndFilename, Rect roi) {
		NNetwork theNetwork = new NNetwork();
		theNetwork.load(pathAndFilename);
		if(roi != null) {
			theNetwork.applyROI(roi);
		}
		return theNetwork;
	}

	/**
	 * Adds a closing rectangular boundary to the region
	 * @param networkIn - this remains unchanged
	 * @param boarderAmt - the border in docSpace coordinates. If set to 0, then the boundary rect is the same as the document boundary
	 * @param returnedBoundaryRect  - this can be used to get the boundaryRect (passed back by reference).
	 * @return A copy of the networkIn with boundary added, and any edges outside the boundary removed.
	 */
	public static NNetwork addBoundaryRect(NNetwork networkIn, float boarderAmt, Rect returnedBoundaryRect) {
		// boarder amt is in docSpace, so example would be 0.01f
		returnedBoundaryRect = GlobalSettings.getTheDocumentCoordSystem().getDocumentRect();
		returnedBoundaryRect.dilate(-boarderAmt,-boarderAmt);
		NNetworkAddBoundaryEdges addEdges = new NNetworkAddBoundaryEdges(networkIn);
		addEdges.addOuterBoundaryEdges(returnedBoundaryRect, true);
		NNetwork networkOut = addEdges.getNetworkWithAddedBoundary();
		return networkOut;
	}


	/**
	 * @param networkIn - this remains unchanged
	 * @param noiseImage -  an image on which to base displacements
	 * @param exisitingPointDispacement - existing points are displaced by up-to this amount. Does not displace boarder.
	 * @param newSectionLength - splits existing edges into sections of this length. If zero, then ignored
	 * @param newPointDisplacement - displaces new points from new sections by this amount. If zero then ignored
	 * @param displaceBoarder - boolean. If set to false then the outer boarder is left unaltered.
	 * @return a new NNetwork with the detail added.
	 */
	public static NNetwork addEdgeDetail(NNetwork networkIn, BufferedImage noiseImage, float exisitingPointDispacement, float newSectionLength, float newPointDisplacement, boolean displaceBoarder) {
		BufferedImage fractalImage = ImageProcessing.loadImage(GlobalSettings.getSampleLibPath() + "textures\\fractal noise\\lower freq.png");
		NNetworkAddEdgeDetail addEdgeDetail = new NNetworkAddEdgeDetail(networkIn, noiseImage);


		KeyValuePairList internalEdgesKVPL = NNetworkEdgeDrawHelper.getDocumentEdgesKVPList(false);
		KeyValuePairList documentEdgesKVPL = NNetworkEdgeDrawHelper.getDocumentEdgesKVPList(true);


		addEdgeDetail.displacePoints(exisitingPointDispacement);// was 0.001;

		//theNetwork = addEdgeDetail.getNetworkWithAddedEdgeDetail();

		///////////////////////////////////////////////////////////////////////////
		// 2b/ add in new points on edges and displace
		if(newSectionLength == 0 || newPointDisplacement==0) {
			return addEdgeDetail.getNetworkWithAddedEdgeDetail();
		}


		if(displaceBoarder) {
			addEdgeDetail.addEdgeDetail(newSectionLength, newPointDisplacement, null); //was 0.0005f, 0.0020f..... use internalEdgesKVPL instead of null
		}else {

			addEdgeDetail.addEdgeDetail(newSectionLength, newPointDisplacement, internalEdgesKVPL); //was 0.0005f, 0.0020f..... use internalEdgesKVPL instead of null
		}

		return addEdgeDetail.getNetworkWithAddedEdgeDetail();

		///////////////////////////////////////////////////////////////////////////
		// 2a/ crap from user session implementation
		/*
		System.out.println("adding edge detail; displacing points... ");


		NNetworkAddEdgeDetail addEdgeDetail = new NNetworkAddEdgeDetail(theNetwork, fractalImage);


		KeyValuePairList internalEdgesKVPL = NNetworkEdgeDrawHelper.getDocumentEdgesKVPList(false);
		KeyValuePairList documentEdgesKVPL = NNetworkEdgeDrawHelper.getDocumentEdgesKVPList(true);


		addEdgeDetail.displacePoints(0.001f);

		theNetwork = addEdgeDetail.getNetworkWithAddedEdgeDetail();

		///////////////////////////////////////////////////////////////////////////
		// 2b/ add in new points on edges and displace
		System.out.println("adding edge detail; adding extra points... ");
		if(GlobalSettings.sessionNameContains("InnerLondon")) addEdgeDetail.addEdgeDetail(0.0005f, 0.0020f, null); // use internalEdgesKVPL instead of null
		if(GlobalSettings.sessionNameContains("CityOfLondon")) addEdgeDetail.addEdgeDetail(0.0016f, 0.0040f,null);
		if(GlobalSettings.sessionNameContains("SohoLondon")) addEdgeDetail.addEdgeDetail(0.0004f, 0.0040f,null);
		theNetwork = addEdgeDetail.getNetworkWithAddedEdgeDetail();
		 */
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// DRAWING
	//
	//

	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Drawing Points
	//
	public static void drawPoint(NPoint np, Color c, float radiusDocSpace, BufferedImageRenderTarget rt) {
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

	public static void drawEdges(ArrayList<NEdge> edges, Color c, float width, BufferedImageRenderTarget rt) {
		float dash[] = { 4.0f };
		rt.getVectorShapeDrawer().setStrokeCapJoin(BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		//System.out.println("drawing num edges " + edges.size());
		for(NEdge e: edges) {
			//System.out.println(e.toStr());
			drawEdge(e,  c,  width, rt);
		}

	}

	public static void drawEdge(NEdge e, Color c, float width, BufferedImageRenderTarget rt) {
		//Rect viewPort = GlobalObjects.theSurface.getViewPortDocSpace();
		PVector p1 = e.getEndCoordinate(0);
		PVector p2 = e.getEndCoordinate(1);
		//if(viewPort.isPointInside(p1)==false && viewPort.isPointInside(p2)==false) return;
		rt.drawLine(p1, p2, c, width);
	}

	public static void drawEdgesRandomColorWithPoints(ArrayList<NEdge> edges, float w, BufferedImageRenderTarget rt) {
		for(NEdge e: edges) {
			drawEdgeRandomColorWithPoints( e,  w,  rt);
		}

	}

	public static void drawEdgeRandomColorWithPoints(NEdge e, float w, BufferedImageRenderTarget rt) {
		Color c = MOColor.getRandomRGB(255);
		drawEdge(e, c, w, rt);

		drawPoint(e.p1, Color.RED, 0.001f, rt);
		drawPoint(e.p2, Color.RED, 0.001f, rt);
	}

	/**
	 * Draws the network in black using a range of line thicknesses, and saves the render
	 * @param theNetwork - this remains unchanged
	 * @param Awidth - The width of A roads in pixels at full scale resolution - suggest 20
	 * @param Bwidth - The width of B roads in pixels at full scale resolution - suggest 10
	 * @param Cwidth - The width of C roads in pixels at full scale resolution - suggest 5
	 * @param regionEdgesWidth - The width of pre-defined regions such as parks in pixels at full scale resolution - suggest 5
	 * @param boundaryWidth - The width of the outer boundary in pixels at full scale resolution - suggest 5
	 * @param boundaryRect - This is the rect that was used to create the boundary. If set, then pixels outside this rect will be cleared. Removes line-cap artifacts.
	 * @param saveRender -if set to true, saves the render to file with a useful naming convention describing the road widths. (sessName+ "_edges_" + edgeWidths + "_.png")
	 */
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

		KeyValuePair descriptorC = NNetworkEdgeDrawHelper.getKVP("ROAD", "C");
		drawEdges(theNetwork, descriptorC, Color.BLACK, Cwidth);



		KeyValuePair descriptoB = NNetworkEdgeDrawHelper.getKVP("ROAD", "B");
		drawEdges(theNetwork, descriptoB, Color.BLACK, Bwidth);



		KeyValuePair descriptorA = NNetworkEdgeDrawHelper.getKVP("ROAD", "A");
		drawEdges(theNetwork, descriptorA, Color.BLACK, Awidth);



		KeyValuePair seaDescriptor = NNetworkEdgeDrawHelper.getKVP("REGIONEDGE", "SEA");
		drawEdges(theNetwork, seaDescriptor, Color.BLACK, regionEdgesWidth);
		KeyValuePair riverDescriptor = NNetworkEdgeDrawHelper.getKVP("REGIONEDGE", "RIVER");
		drawEdges(theNetwork, riverDescriptor, Color.BLACK, regionEdgesWidth);
		KeyValuePair parksDescriptor = NNetworkEdgeDrawHelper.getKVP("REGIONEDGE", "PARK");
		drawEdges(theNetwork, parksDescriptor, Color.BLACK, regionEdgesWidth);
		KeyValuePair lakeDescriptor = NNetworkEdgeDrawHelper.getKVP("REGIONEDGE", "LAKE");
		drawEdges(theNetwork, lakeDescriptor, Color.BLACK, regionEdgesWidth);

		KeyValuePair undefinedDescriptor = NNetworkEdgeDrawHelper.getKVP("REGIONEDGE", "UNDEFINED");
		drawEdges(theNetwork, undefinedDescriptor, Color.BLACK, regionEdgesWidth);


		GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();

		// now remove everything outside the boundaryRect (gets rid of wide-line cap-butts extending over the boundary line)
		if(boundaryRect!=null) {
			GlobalSettings.getDocument().getMain().clearOutsideRect(boundaryRect);
		}

		KeyValuePair docEdge = NNetworkEdgeDrawHelper.getKVP("REGIONEDGE", "document");
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


		if(saveImage) {
			GlobalSettings.getDocument().getMain().saveRenderToFile(GlobalSettings.getUserSessionPath() + "dashed edges white background.png");
		}


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
			NNetworkEdgeDrawHelper.drawVertices2ListNoFill(edgeRunVertices, 10, null, null);
			GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
			//return;
		}


		theTextRibbonManager.createTextRibbons(edgeRunVertices, textbank, textRenderer, fontheight, fontheight/2f);



		int lastProgress=-1;

		while(true) {

			RibbonLetter ribbonLetter = theTextRibbonManager.getNextRibbonLetter();
			if(ribbonLetter==null) {
				return;
			}

			float len = ribbonLetter.theOwningTextRibbon.getTheVertices().getTotalLength();

			Sprite sprite =  textRenderer.getSprite(ribbonLetter.theChar, fontheight, ribbonLetter.getCharLine(), null);
			GlobalSettings.getDocument().getMain().pasteSprite(sprite);


			if(len < minimumRibbonLength) {
				return;
			}

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





	public static void drawVertices2ListNoFill(ArrayList<Vertices2> verts, float width, Color c, float[] dashPattern) {
		// if dashPattern is nulled, then uses default line
		// if color is nulled the uses a random color (for debug probably)
		BufferedImageRenderTarget rt = GlobalSettings.getDocument().getMain();
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
		BufferedImageRenderTarget rt = GlobalSettings.getDocument().getMain();

		if(c==null) {
			c = MOColor.getRandomRGB(100);
		}


		rt.drawVertices2NoFill(verts, c, width, dashPattern);
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities to do with converting NEdge to Lines
	//
	public static ArrayList<Line2> getEdgesAsLines(ArrayList<NEdge> edges){
		ArrayList<Line2> lines = new ArrayList<>();
		for(NEdge e: edges) {
			lines.add(e.getLine2());
		}
		return lines;
	}






	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Utilities to do with KVP types
	//

	public static KeyValuePair getKVP(String key, String val) {
		return new KeyValuePair(key, val);
	}

	public static KeyValuePairList getDocumentEdgesKVPList(boolean getDocEdges) {
		KeyValuePairList kvpl = new KeyValuePairList();

		if(!getDocEdges) {
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


	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Methods using edge crawler.
	//


	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//This is use to place items along the road
	//
	static boolean updateDrawRoadLines(SpriteFont spriteFont, EdgeRunVerticesCrawler edgeCrawler, float crawlStep, float overlap1, float overlap2) {
		// draws the item on the line of the road


		edgeCrawler.setCrawlStep(crawlStep);
		Line2 line = edgeCrawler.updateCrawl();

		if(line == null){
			//endUserSession();
			return false;
		}

		Sprite sprite =  spriteFont.getSpriteInstance(true);

		if(randomStream.probabilityEvent(0.5f)) {
			sprite.mirror(true);
		}

		sprite.mapToLine2(line,  overlap1, overlap2, 0.005f);


		if( GlobalSettings.getDocument().cropSpriteToBorder(sprite) ) {
			GlobalSettings.getDocument().getMain().pasteSprite(sprite);
		}

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



		Sprite sprite = currentSpriteFont.getSpriteInstance(true);

		// do this by loaded 2 different sprite fonts, one for side on
		//if( sprite.data.spriteImageGroupItemShortName.contains("side")) {
		//	sprite.data.origin = new PVector(0.5f,1f);
		//}


		float scaleModifier = randomStream.randRangeF(scaleModifierLo, scaleModifierHi);
		sprite.scaleToSizeInScene(scaleModifier);



		if(randomStream.probabilityEvent(probability)) {
			float rot = line.getRotation();
			rot += randomStream.randRangeF(rotationLo, rotationHi);
			sprite.rotate(rot);
			sprite.setDocPoint(line.p1);
			//SceneHelper.addRandomHSV( sprite,  0.1f,  0.2f,  0.0f);
			if( GlobalSettings.getDocument().cropSpriteToBorder(sprite) ) {
				GlobalSettings.getDocument().getMain().pasteSprite(sprite);
			}

		}

	}




}
