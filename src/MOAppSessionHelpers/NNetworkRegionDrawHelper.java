package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOCompositing.BufferedImageRenderTarget;
import MOImage.ImageProcessing;
import MOImage.MONamedColors;
import MOImage.MOPackedColor;
import MOImage.KeyImageSampler;
import MOImage.MOColor;
import MOImage.MOColorImagePalette;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Vertices2;
import MONetwork.NNetwork;
import MONetwork.NRegion;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;
import MOUtils.ObjectWithValueList;

/**
 * @author User
 *
 */
public class NNetworkRegionDrawHelper {
	
	
	
	/**
	 * draws the edges of a regions as closed polygon. As it is dericed from a closed polygon, rather than a collection of edges, you have the
	 * opportunity to dilate the regions polygon before drawing
	 * @param ntwk
	 * @param dilation
	 * @param width
	 * @param dashPattern
	 */
	public static void drawRegionEdges(NNetwork ntwk, float dilation, float width, float[] dashPattern) {

		ArrayList<Vertices2> verts  = convertRegionsToVertices2(ntwk.getRegions());
		BufferedImageRenderTarget rt = GlobalSettings.getDocument().getMain();
		if(dilation != 0) {
			verts = dilateVertices2(verts, dilation, false);
		}
		for(Vertices2 v: verts) {
			rt.drawVertices2NoFill(v, Color.BLACK, width, dashPattern);
		}
	}

	
	
	
	/**
	 * draws regions in a solid colour defined by region color pairs and hsv variance
	 * @param ntwk
	 * @param regionTypeColorPairs - These associate the "REGIONTYPE" attribute with a named color, as defined in the MONamedColor class
	 * @param hsvVariance - uses a list of 3 float values to create ranges around the centre value as defined by the named color
	 * @param saveImage - saves the image using the naming convention "ColoredRegions_" + combinedNames + ".png", where combined names are the names of the REGIONTYPES
	 * @param clearImage - clears the image after saving
	 */
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
		
		GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
		
		if(saveImage) {
			String combinedNames = getCombinedRegionNames(regionTypeColorPairs);
    		GlobalSettings.getDocument().getMain().saveRenderToFile(GlobalSettings.getUserSessionPath() + "ColoredRegions_" + combinedNames + ".png");
    	}
		
		if(clearImage && saveImage) {
    		GlobalSettings.getDocument().getMain().clearImage();
    	}
		
		
	}
	
	
	public static void drawRegionFillWithPalette(NNetwork ntwk, String[] regionTypes, MOColorImagePalette palette, float[] hsvVariance,  boolean saveImage, boolean clearImage) {
		// give a particular region type This will fill it with random colors selected from an image-based palette
		
		KeyValuePairList kvpl = createRegionTypeKVP(regionTypes);
		ArrayList<NRegion> regions = getRegionsMatchingKVPList( ntwk,  kvpl);


		for(NRegion r: regions) {
			
			Color c = palette.getRandomColor();
			
			if(hsvVariance!=null) {
				c =  MOColor.perturbHSV(c, hsvVariance[0], hsvVariance[1], hsvVariance[2]);
			}
			
			drawRegionFill(r,  c, GlobalSettings.getDocument().getMain()) ;
		}
		
		GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
		
		if(saveImage) {
			String combinedNames = getCombinedRegionNames(kvpl);
    		GlobalSettings.getDocument().getMain().saveRenderToFile(GlobalSettings.getUserSessionPath() + "ColoredRegions_" + combinedNames + ".png");
    	}
		
		if(clearImage && saveImage) {
    		GlobalSettings.getDocument().getMain().clearImage();
    	}
		
		
	}
	
	
	/**
	 * draws regions which are darker the further they are away from the centre
	 * @param ntwk - the network being passed in. This is unaltered by the process so is not copied
	 * @param centre - the brightest point in the vignette
	 * @param hsvVariance - for each separate region; the amount of variance from the calculated darkness
	 * @param saveImage - if true save image called "regions_vignetted.png"
	 * 
	 */
	public static void drawRegionsVignetted(NNetwork ntwk, PVector centre, float[] hsvVariance,  boolean saveImage ) {
		ArrayList<NRegion> regions = ntwk.getRegions();
		for(NRegion r: regions) {
			
			PVector rpos = r.getVertices().getExtents().getCentre();
			float d = centre.dist(rpos);
			
			// max dist = 0.707 in a square image
			float edgeProximity = MOMaths.map(d,0,0.707f,1,0);
			int tone = (int) (edgeProximity * 255);
			int packedTone = MOPackedColor.packARGB(255, tone, tone, tone) ;
			Color c = new Color(packedTone);
			
			if(hsvVariance!=null) {
				c =  MOColor.perturbHSV(c, hsvVariance[0], hsvVariance[1], hsvVariance[2]);
			}
			
			drawRegionFill(r,  c, GlobalSettings.getDocument().getMain()) ;
		}
		
		
		if(saveImage) {
			String imageName = "regions_vignetted.png";
    		GlobalSettings.getDocument().getMain().saveRenderToFile(GlobalSettings.getUserSessionPath() + imageName);
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


	public static void drawRegionFill(NRegion r, Color c, BufferedImageRenderTarget rt) {
		Vertices2 verts = r.getVertices();
		rt.drawVertices2(verts, c, c, 1);
	}

	public static void fillRegionsRandomColor(NNetwork ntwk) {
		// just for debug, really crude
		ArrayList<NRegion> regions = ntwk.getRegions();

		int colNum = 0;
		for(NRegion r: regions) {
			Color c = MOColor.getRandomRGB(255);

			drawRegionFill(r, c, GlobalSettings.getDocument().getMain());
			if(colNum>10) colNum = 0;
		}


	}

	static private KeyValuePairList createRegionTypeColorKVP(String[] regionTypeColorPairs) {
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
	
	
	static private KeyValuePairList createRegionTypeKVP(String[] regionTypeColorPairs) {
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
	
	
	static private String getCombinedRegionNames(String[] regionTypeColorPairs) {
		// from a list of pairs "PARK","GREEN","RIVER","BLUE", creates a list of strings in the form "PARK","RIVER"....
		String combinedNames = "";
		for(int n = 0; n < regionTypeColorPairs.length; n+=2) {
			int keyIndex = n;
			
			String regionName = regionTypeColorPairs[keyIndex];
			combinedNames += regionName + "_";
		}
		return combinedNames;
	}
	
	static private String getCombinedRegionNames(KeyValuePairList kvpl) {
		// from a list of pairs "PARK","GREEN","RIVER","BLUE", creates a list of strings in the form "PARK","RIVER"....
		String combinedNames = "";
		int num = kvpl.getNumItems();
		for(int n = 0; n < num; n++) {
			String regionName = kvpl.getItem(n).getString();
			combinedNames += regionName + "_";
		}
		return combinedNames;
	}
	
	
	public static Color getRegionDefaultColour(NRegion r, boolean randomiseUrbanColours) {
		// is old, for debug only
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
	
	
	
	
	/**
	 * adds in a region marker to each unattributed region based on a "density map". There are 3 levels of density:-
	 * "URBAN_DENSITY_LOW", "URBAN_DENSITY_MEDIUM", "URBAN_DENSITY_HIGH" generated as the value for the "REGIONTYPE" attribute kvp 
	 * @param ntwk - adds the density KVP attribute to the regions, so does alter the network
	 * @param densityImagePathAndName
	 */
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
	
	
	
	
	
	static private ArrayList<NRegion> getUnattributedRegions(NNetwork ntwk) {
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
