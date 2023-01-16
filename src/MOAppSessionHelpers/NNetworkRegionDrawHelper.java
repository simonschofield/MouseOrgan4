package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOImage.KeyImageSampler;
import MOImage.MOColor;
import MOMaths.PVector;
import MOMaths.Vertices2;
import MONetwork.NNetwork;
import MONetwork.NRegion;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;
import MOUtils.ObjectWithValueList;

public class NNetworkRegionDrawHelper {
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
		// want to set

		ArrayList<NRegion> regions = ntwk.getRegions();

		int colNum = 0;
		for(NRegion r: regions) {
			Color c = getRegionDefaultColour(r, randomiseUrbanCol);

			drawRegionFill(r, c, GlobalSettings.getDocument().getMain());
			if(colNum>10) colNum = 0;
		}
	}
	
	/*
	public static void drawRegionsByType(NNetwork ntwk, KeyValuePairList kvpl, boolean saveLayers) {
		// want to set

		ArrayList<NRegion> regions = ntwk.getRegions();

		int colNum = 0;
		for(NRegion r: regions) {
			Color c = getRegionDefaultColour(r, randomiseUrbanCol);

			drawRegionFill(r, c, GlobalSettings.getDocument().getMain());
			if(colNum>10) colNum = 0;
		}
	}
	*/


	public static void drawRegionFill(NRegion r, Color c, RenderTarget rt) {
		Vertices2 verts = r.getVertices();
		rt.drawVertices2(verts, c, c, 1);
	}

	public static void fillRegionsRandomColor(NNetwork ntwk) {
		// just for debug really
		ArrayList<NRegion> regions = ntwk.getRegions();

		int colNum = 0;
		for(NRegion r: regions) {
			Color c = MOColor.getRandomRGB(255);

			drawRegionFill(r, c, GlobalSettings.getDocument().getMain());
			if(colNum>10) colNum = 0;
		}


	}

		/*
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
	}*/
	
	
	
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
	
	
	public static ArrayList<Vertices2> convertRegionsToVertices2(ArrayList<NRegion> regions) {
		ArrayList<Vertices2> vertices = new ArrayList<Vertices2>();
		int n=0;
		for(NRegion  r : regions) {

			Vertices2 v = r.getVertices();
			
			n++;
			v.setPolygonWindingDirection(Vertices2.CLOCKWISE);
			vertices.add( v );
		}
		return vertices;

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
	
	public static ArrayList<Vertices2> dilateVertices2(ArrayList<Vertices2> vertsIn, float dilation, boolean relativeToOwnSize) {
		// if relativeToOwnWidth == false, the dilation is in docSpace units
		// if relativeToOwnWidth == true, the diagonal size is measured. This gives an approximate measure of width and height of the region
		// This is TBD and is very experimental
		ArrayList<Vertices2> vertsOut = new ArrayList<Vertices2>();

		for(Vertices2  v : vertsIn) {

			if(relativeToOwnSize) {


			}


			Vertices2 dilated = v.getDilatedVertices( dilation);
			vertsOut.add( dilated );
		}
		return vertsOut;

	}


}
