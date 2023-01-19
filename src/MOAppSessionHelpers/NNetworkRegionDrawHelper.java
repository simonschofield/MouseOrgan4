package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOImage.MONamedColors;
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
	public static void drawRegionEdges(NNetwork ntwk, float dilation, float width, float[] dashPattern) {

		ArrayList<Vertices2> verts  = convertRegionsToVertices2(ntwk.getRegions());
		RenderTarget rt = GlobalSettings.getDocument().getMain();
		if(dilation != 0) {
			verts = dilateVertices2(verts, dilation, false);
		}
		for(Vertices2 v: verts) {
			rt.drawVertices2NoFill(v, Color.BLACK, width, dashPattern);
		}
	}

	
	
	
	public static void drawRegionFillsByType(NNetwork ntwk, String[] regionTypeColorPairs, float[] hsvVariance,  boolean saveImage, boolean clearImage) {
		// enables the drawing of regions with a color fill based on regionType/namedColor pairs
		// After which the render may be saved
		// hsvVariance is nullable
		
		
		KeyValuePairList regionTypeColors = createRegionTypeColorKVP(regionTypeColorPairs);// these are in the KV form ["PARK", "GREEN"],["RIVER", "BLUE"],...
		regionTypeColors.printMe();
		
		KeyValuePairList regionTypes = createRegionTypeKVP(regionTypeColorPairs);// these are in the KV form ["REGIONTYPE", "PARK"],["REGIONTYPE", "RIVER"],...
		regionTypes.printMe();
		// for each regionTypeColors, find all the regions that match. Color regions with new method, save layer
		ArrayList<NRegion> regions = getRegionsMatchingKVPList( ntwk,  regionTypes);


		for(NRegion r: regions) {
			String colorname = getNamedColorOfRegion(r,  regionTypeColors);
			Color c = MONamedColors.getColor(colorname);
			
			if(hsvVariance!=null) {
				c =  MOColor.perturbHSV(c, hsvVariance[0], hsvVariance[1], hsvVariance[2]);
			}
			
			drawRegionFill(r,  c, GlobalSettings.getDocument().getMain()) ;
		}
		
		if(saveImage) {
			String combinedNames = getCombinedRegionNames(regionTypeColorPairs);
    		GlobalSettings.getDocument().getMain().saveRenderToFile(GlobalSettings.getUserSessionPath() + "ColoredRegions_" + combinedNames + ".png");
    	}
		
		if(clearImage) {
    		GlobalSettings.getDocument().getMain().clearImage();
    	}
		
		
	}
	
	static String getNamedColorOfRegion(NRegion r, KeyValuePairList regionTypeColors) {
		String v = r.getAttributeStringVal("REGIONTYPE");
		if(v == null) return "NOCOLOR";
		String colName = regionTypeColors.getString(v);
		if(colName.equals("")) return "NOCOLOR";
		return colName;
	}
	
	
	
	public static ArrayList<NRegion> getRegionsMatchingKVPList(NNetwork ntwk, KeyValuePairList regionTypes){
		
		ArrayList<NRegion> regions = new ArrayList<NRegion>();
		for(int n = 0; n < regionTypes.getNumItems(); n++) {
			KeyValuePair kvp = regionTypes.getItem(n);
			ArrayList<NRegion> thisKVPRegions = ntwk.getRegionsMatchingQuery(kvp);
			regions.addAll(thisKVPRegions);
		}
		return regions;
	}


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

	static KeyValuePairList createRegionTypeColorKVP(String[] regionTypeColorPairs) {
		// from a list of pairs "PARK","GREEN","RIVER","BLUE", creates a KVPList with the form ["PARK", "GREEN"],["RIVER", "BLUE"],...
		KeyValuePairList regionTypeColorKVPList= new KeyValuePairList();
		for(int n = 0; n < regionTypeColorPairs.length; n+=2) {
			int keyIndex = n;
			int valIndex = n+1;
			String k = regionTypeColorPairs[keyIndex];
			String v = regionTypeColorPairs[valIndex];
			regionTypeColorKVPList.addKeyValue(k, v);
		}
		return regionTypeColorKVPList;
	}
	
	
	static KeyValuePairList createRegionTypeKVP(String[] regionTypeColorPairs) {
		// from a list of pairs "PARK","GREEN","RIVER","BLUE", creates a KVPList with the form ["REGIONTYPE", "PARK"],["REGIONTYPE", "RIVER"],...
		KeyValuePairList regionTypeColorKVPList= new KeyValuePairList();
		for(int n = 0; n < regionTypeColorPairs.length; n+=2) {
			int keyIndex = n;
			int valIndex = n+1;
			String k = "REGIONTYPE";
			String v = regionTypeColorPairs[keyIndex];
			regionTypeColorKVPList.addKeyValue(k, v);
		}
		return regionTypeColorKVPList;
	}
	
	
	static String getCombinedRegionNames(String[] regionTypeColorPairs) {
		// from a list of pairs "PARK","GREEN","RIVER","BLUE", creates a KVPList with the form ["PARK", "GREEN"],["RIVER", "BLUE"],...
		String combinedNames = "";
		for(int n = 0; n < regionTypeColorPairs.length; n+=2) {
			int keyIndex = n;
			
			String regionName = regionTypeColorPairs[keyIndex];
			combinedNames += regionName + "_";
		}
		return combinedNames;
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
