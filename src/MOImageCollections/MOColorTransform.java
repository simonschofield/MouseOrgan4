package MOImageCollections;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import MOImage.AlphaEdgeDrawer;
import MOImage.ImageProcessing;
import MOImageCollections.MOColorTransformParameters;
import MOMaths.PVector;
import MOSprite.SpriteSeed;
import MOUtils.KeyValuePairList;
import MOUtils.MOStringUtils;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This is a wrapper for color transforms that can be passed into classes for execution.
// A way of defining and executing a range of colour transforms (any non-geometric pixel operations)
// This can then be passed to processed to be automatically executed, for instance upon loading a bunch of images
// The class will execute one or many color transforms
//
//

public class MOColorTransform {
	
	boolean initialMessagePrinted = false;
	
	ArrayList<MOColorTransformParameters> scriptableColorTransformParametersList = new ArrayList<MOColorTransformParameters>();
	
	
	public static final int	COLORTRANSFORM_NONE = 0;
	public static final int	COLORTRANSFORM_HSV = 1;
	public static final int	COLORTRANSFORM_BRIGHTNESS_NOCLIP = 2;
	public static final int	COLORTRANSFORM_BRIGHTNESS = 3;
	public static final int	COLORTRANSFORM_CONTRAST = 4;
	public static final int	COLORTRANSFORM_LEVELS = 5;
	public static final int	COLORTRANSFORM_BLENDWITHCOLOR = 6;
	public static final int	COLORTRANSFORM_SET_DOMINANT_HUE = 7;
	public static final int	COLORTRANSFORM_GREYSCALE = 8;
	public static final int	COLORTRANSFORM_DRAWEDGE = 9;
	public static final int	COLORTRANSFORM_ADDEDGE = 10;
	
	public MOColorTransform() {}
	
	public MOColorTransform(int colorTransformType, float p1, float p2,float p3, float p4){
		
		addColorTransform( colorTransformType,  p1,  p2, p3,  p4);
	}
	
	
	static MOColorTransformParameters getLevelsTransform(float shadowVal, float midtoneVal, float highlightVal, float outShadowVal,  float outHighlightVal){
		return new MOColorTransformParameters(COLORTRANSFORM_LEVELS,   shadowVal,  midtoneVal,  highlightVal,  outShadowVal,   outHighlightVal);
	}
	
	public void addColorTransform(int colorTransformType, float p1, float p2,float p3, float p4){
		
		scriptableColorTransformParametersList.add(new MOColorTransformParameters(colorTransformType,  p1,  p2, p3,  p4));
	}
	
	
	public void addGreyscaleTransform(){
		scriptableColorTransformParametersList.add(new MOColorTransformParameters(COLORTRANSFORM_GREYSCALE,  0,  0, 0,  0));
	}
	
	public void addLevelsTransform(float shadowVal, float midtoneVal, float highlightVal, float outShadowVal,  float outHighlightVal){
		scriptableColorTransformParametersList.add(new MOColorTransformParameters(COLORTRANSFORM_LEVELS,   shadowVal,  midtoneVal,  highlightVal,  outShadowVal,   outHighlightVal));
	}
	
	public void addEdgeTransform(float brushWidth, float brushHardness){
		MOColorTransformParameters params = new MOColorTransformParameters(COLORTRANSFORM_ADDEDGE);
		params.p1 = brushWidth;
		params.p2 = brushHardness;
		scriptableColorTransformParametersList.add(params);
	}
	
	
	public BufferedImage doColorTransforms(BufferedImage imageIn) {
		System.out.print("MOColorTransform.doColorTransforms:: doColorTransforms .. in list are " + scriptableColorTransformParametersList.size());
		updateProgressMessage();
		
		if(scriptableColorTransformParametersList.size()==0) {
			System.out.print("MOColorTransform.doColorTransforms:: none set!, returning input image");
			return imageIn;
		}
		
    	BufferedImage imageOut=null;
    	for(MOColorTransformParameters ipp: scriptableColorTransformParametersList) {
    		
    		if(imageOut==null) {
    			imageOut = colorTransform( imageIn,  ipp);
    		}else {
    			imageOut = colorTransform( imageOut,  ipp);
    		}
    		
    	}
    	
    	return imageOut;
    }
	
	void updateProgressMessage() {
		if(initialMessagePrinted==false) {
			String transformStrings = "Nos ";
			for(MOColorTransformParameters ipp: scriptableColorTransformParametersList) {
				transformStrings += ipp.colourTransformIdentifier + ", ";
	    	}
			
			System.out.print("Doing color transforms " + transformStrings);
		   initialMessagePrinted = true;
		} else {
			System.out.print(".");
			
		}
		
		
	}
    
    
	
	public void saveParameters(String fileAndPath) {
		// there should be a directory in the cache folder
		FileWriter csvWriter = null;
		try{
			csvWriter = new FileWriter(fileAndPath);


			for(MOColorTransformParameters sp: scriptableColorTransformParametersList){
				csvWriter.append(sp.getAsCSVStr());
			}

			csvWriter.flush();
			csvWriter.close();

		}// end try
		catch(Exception ex){
			System.out.println("ScriptableColorTransform.saveParameters: csv writer failed - "  + fileAndPath + ex);
		}


	}

	public void loadParameters(String fileAndPath) {
		// there should be a directory in the project folder called seeds
		scriptableColorTransformParametersList.clear();
		try{
			BufferedReader csvReader = new BufferedReader(new FileReader(fileAndPath));

			String row;

			while ((row = csvReader.readLine()) != null) {

				// do something with the data
				MOColorTransformParameters sp = new MOColorTransformParameters();
				sp.setWithCSVStr(row);
				//System.out.println("Loading seed "+row);
				scriptableColorTransformParametersList.add(sp);
			}
			csvReader.close();
		} catch(Exception e){

			System.out.println("SpriteSeedBatch.loadSeedsAsCSV: csv reader failed - " + fileAndPath + e);
		}

	}

	
	
	
	
	public boolean equals(MOColorTransform other) {
		
		if(this.scriptableColorTransformParametersList.size() != other.scriptableColorTransformParametersList.size()) return false;
		
		int len = this.scriptableColorTransformParametersList.size();
		for(int n = 0; n < len; n++) {
			
			MOColorTransformParameters thisSp = this.scriptableColorTransformParametersList.get(n);
			MOColorTransformParameters otherSp = other.scriptableColorTransformParametersList.get(n);
			
			if(thisSp.equals(otherSp)==false) return false;
		}

		return true;
	}

	
	
	public static BufferedImage colorTransform(BufferedImage img, MOColorTransformParameters params) {

		System.out.println("in colorTransform . Function = " + params.colourTransformIdentifier);
		float p1 = params.p1;
		float p2 = params.p2;
		float p3 = params.p3;
		
		
		
		switch (params.colourTransformIdentifier) {
		case COLORTRANSFORM_HSV: {
			return ImageProcessing.adjustHSV(img, p1, p2, p3);
		}
		case COLORTRANSFORM_BRIGHTNESS_NOCLIP: {
			return ImageProcessing.adjustBrightnessNoClip(img, p1);

		}
		case COLORTRANSFORM_BRIGHTNESS: {
			return ImageProcessing.adjustBrightness(img, p1);

		}
		case COLORTRANSFORM_CONTRAST: {
			return ImageProcessing.adjustContrast(img, p1);

		}
		case COLORTRANSFORM_LEVELS: {
			return ImageProcessing.adjustLevels(img, p1, p2, p3);

		}
		case COLORTRANSFORM_SET_DOMINANT_HUE:{
			return ImageProcessing.setDominantHue(img, p1);
		}
		case COLORTRANSFORM_GREYSCALE:{
			return ImageProcessing.makeGreyscale(img);
		}
		case COLORTRANSFORM_DRAWEDGE: {
    		AlphaEdgeDrawer edgeDrawer = new AlphaEdgeDrawer((int)p1, p2);
    		return edgeDrawer.drawEdgesFixedBrushStamps(img);
    	}
		case COLORTRANSFORM_ADDEDGE: {
    		AlphaEdgeDrawer edgeDrawer = new AlphaEdgeDrawer((int)p1, p2);
    		//BufferedImage edgeImg =  edgeDrawer.drawEdgesFixedBrushStamps(img);
    		//BufferedImage edgeImg = edgeDrawer.getAsRawEdge(img);
    		//return ImageProcessing.getCompositeImage(edgeImg, img, 0, 0 ,1);
    		return edgeDrawer.getJitteredEdge(img, 4);
    	}
		default:
			return img;
		}


	}

}// end of class








////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//
//
class MOColorTransformParameters {
	// for automated image-processing processes
	// where you want to pass a list of these to the ImageProcessing method
	// BufferedImage imgOut = applyProcess(ImageProcessParameters ipp );
	
	// The image in is passed as one of the parameters
	
	// use the enumerated colour transforms in ImageProcessing
	int colourTransformIdentifier;
	
	
	float p1,p2,p3,p4,p5,p6,p7,p8;
	int i1,i2,i3,i4;
	
	public MOColorTransformParameters() {
		
	}
	
	public MOColorTransformParameters(int identifier) {
		colourTransformIdentifier = identifier;
	}
	
	public MOColorTransformParameters(int identifier, float a1, float a2, float a3, float a4){
		colourTransformIdentifier = identifier;
		
		p1=a1; p2=a2; p3=a3; p4=a4;
	}
	
	public MOColorTransformParameters(int identifier, float a1, float a2, float a3, float a4, float a5){
		colourTransformIdentifier = identifier;
		
		p1=a1; p2=a2; p3=a3; p4=a4; p5=a5;
	}
	
	public boolean equals(MOColorTransformParameters other) {
		if(this.colourTransformIdentifier == other.colourTransformIdentifier &&
		   this.p1 == other.p1 && this.p2 == other.p2 && this.p3 == other.p3 && this.p4 == other.p4 &&
		   this.p5 == other.p5 && this.p6 == other.p6 && this.p7 == other.p7 && this.p8 == other.p8) return true;
		return false;
	}
	
	public String getAsCSVStr() {
		

		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.addKeyValue("COLORTRANSFORMTYPE", colourTransformIdentifier);
		kvlist.addKeyValue("P1", p1);
		kvlist.addKeyValue("P2", p2);
		kvlist.addKeyValue("P3", p3);
		kvlist.addKeyValue("P4", p4);
		kvlist.addKeyValue("P5", p5);
		kvlist.addKeyValue("P6", p6);
		kvlist.addKeyValue("P7", p7);
		kvlist.addKeyValue("P8", p8);
		
		
		
		String line =  kvlist.getAsCSVLine();
		
		return line;

	}

	void setWithCSVStr(String csvStr) {
		
		
		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.ingestCSVLine(csvStr);
		
		colourTransformIdentifier = kvlist.getInt("COLORTRANSFORMTYPE");
		
		
		p1 = kvlist.getFloat("P1");
		p2 = kvlist.getFloat("P2");
		p3 = kvlist.getFloat("P3");
		p4 = kvlist.getFloat("P4");
		p5 = kvlist.getFloat("P5");
		p6 = kvlist.getFloat("P6");
		p7 = kvlist.getFloat("P7");
		p8 = kvlist.getFloat("P8");
		
		
		

		//System.out.println("Loading seed: " + this.getAsCSVStr());

	}
	
	
}

