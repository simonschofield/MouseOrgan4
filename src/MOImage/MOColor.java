package MOImage;

import java.awt.Color;
import java.util.ArrayList;

import MOMaths.MOMaths;
import MOMaths.QRandomStream;

public class MOColor {
	static QRandomStream ranStream = new QRandomStream(1);
	
	static public Color[] getBasic12ColorPalette() {
		Color[] cols = new Color[12];
		int n = 0;
		cols[n++] = Color.BLACK;
		cols[n++] = Color.RED;
		cols[n++] = Color.GREEN;
		cols[n++] = Color.CYAN;
		cols[n++] = Color.DARK_GRAY;
		cols[n++] = Color.MAGENTA;
		cols[n++] = Color.YELLOW;
		cols[n++] = Color.BLUE;
		cols[n++] = Color.LIGHT_GRAY;
		cols[n++] = Color.ORANGE;
		cols[n++] = Color.GRAY;
		cols[n++] = Color.PINK;
		return cols;
	}
	
	public static Color invisibleCol() {
		return new Color(0,0,0,0);
	}
	
	public static Color getRandomRGB(int alpha) {
		int r = ranStream.randRangeInt(0, 255);
		int g = ranStream.randRangeInt(0, 255);
		int b = ranStream.randRangeInt(0, 255);
		
		return new Color(r,g,b, alpha);
	}
	
	public static Color getRandomRGB(int loR, int hiR, int loG, int hiG, int loB, int hiB) {
		int r = ranStream.randRangeInt(loR,hiR);
		int g = ranStream.randRangeInt(loG,hiG);
		int b = ranStream.randRangeInt(loB,hiB);
		
		return new Color(r,g,b);
	}
	
	
	public static Color perturbHSV(Color rgbIn, float dh, float ds, float dv) {
		// Perturbs the color using hsv values, as this is easier to comprehend, but returns
		// expects and returns a RGB color
		
		//System.out.println("pertubHSV rgb " + r + " " + g + " " + b);
		float[] hsvVals = new float[3];
		hsvVals = RGBtoHSV(rgbIn);
		float h = hsvVals[0];
		float s = hsvVals[1];
		float v = hsvVals[2];
		//System.out.println("pertubHSV before hsv " + h + " " + s + " " + v);
		if(dh>0) {
			h = ranStream.perturb(h, dh);
			//System.out.println("pertubHSV before wrap " + h );
			h = MOMaths.wrap01(h);
			//System.out.println("pertubHSV after wrap " + h );
		}
		
		if(ds>0) {
			s = ranStream.perturb(s, ds);
			s = MOMaths.constrain(s, 0, 1);
		}
		
		if(dv>0) {
			v = ranStream.perturb(v, dv);
			v = MOMaths.constrain(v, 0, 1);
		}
		
		//System.out.println("pertubHSV after hsv " + h + " " + s + " " + v);
		Color c = HSVtoRGB(h, s, v);
		float r = c.getRed();
		float g = c.getGreen();
		float b = c.getBlue();
		//System.out.println("pertubHSV back to rgb " + r + " " + g + " " + b);
		//System.out.println();
		return c;
	}
	
	
	// RGB to HSV
	//
	//
	public static float[] RGBtoHSV(Color rgb) {
		int r = rgb.getRed();
		int g = rgb.getGreen();
		int b = rgb.getBlue();
		return RGBtoHSV( r,  g,  b);
	}
	
	public static float[] RGBtoHSV(int r, int g, int b){
	  
	  
	  float minRGB = Math.min( r, Math.min(g, b));
	  float maxRGB = Math.max( r, Math.max(g, b));
	    
	    
	  float value = maxRGB/255.0f; 
	  float delta = maxRGB - minRGB;
	  float hue = 0;
	  float saturation;
	  
	  float[] returnVals = {0f,0f,0f};
	  

	   if( maxRGB != 0 ) {
	    // saturation is the difference between the smallest R,G or B value, and the biggest
	      saturation = delta / maxRGB; }
	   else { // it’s black, so we don’t know the hue
	       return returnVals;
	       }
	       
	  if(delta == 0){ 
	         hue = 0;
	        }
	   else {
	    // now work out the hue by finding out where it lies on the spectrum
	      if( b == maxRGB ) hue = 4 + ( r - g ) / delta;   // between magenta, blue, cyan
	      if( g == maxRGB ) hue = 2 + ( b - r ) / delta;   // between cyan, green, yellow
	      if( r == maxRGB ) hue = ( g - b ) / delta;       // between yellow, Red, magenta
	    }
	  // the above produce a hue in the range -6...6, 
	  // where 0 is magenta, 1 is red, 2 is yellow, 3 is green, 4 is cyan, 5 is blue and 6 is back to magenta 
	  // Multiply the above by 60 to give degrees
	   hue = hue * 60;
	   if( hue < 0 ) hue += 360;
	   
	   hue = hue/360f;
	   hue = MOMaths.wrap01(hue);
	   returnVals[0] = hue;
	   returnVals[1] = saturation;
	   returnVals[2] = value;
	   
	   return returnVals;
	}





	// HSV to RGB
	//
	//
	// expects values in range hue = [0,1], saturation = [0,1], value = [0,1]
	public static Color HSVtoRGB(float hue, float sat, float val)
	{
		System.out.println("HSVtoRGB hsv " + hue + " " + sat + " " + val);
	    
	    int h = (int)(hue * 6);
	    float f = hue * 6 - h;
	    float p = val * (1 - sat);
	    float q = val * (1 - f * sat);
	    float t = val * (1 - (1 - f) * sat);

	    float r,g,b;


	    switch (h) {
	      case 0: r = val; g = t; b = p; break;
	      case 1: r = q; g = val; b = p; break;
	      case 2: r = p; g = val; b = t; break;
	      case 3: r = p; g = q; b = val; break;
	      case 4: r = t; g = p; b = val; break;
	      case 5: r = val; g = p; b = q; break;
	      default: r = val; g = t; b = p;
	    }
	    System.out.println("HSVtoRGB rgb " + r + " " + g + " " + b);
	    return new Color((int)(r*255),(int)(g*255),(int)(b*255));
	}
	
	
	public static Color blendColor(float blendAmt, Color c1, Color c2) {
		//System.out.println("blend amt " + blendAmt);
		float c1r = c1.getRed();
		float c1g = c1.getGreen();
		float c1b = c1.getBlue();
		float c1a = c1.getAlpha();
		//System.out.println("color1 " + c1r + " " + c1g + " "+ c1b + " "+ c1a + " " );
		float c2r = c2.getRed();
		float c2g = c2.getGreen();
		float c2b = c2.getBlue();
		float c2a = c2.getAlpha();
		//System.out.println("color2 " + c2r + " " + c2g + " "+ c2b + " "+ c2a + " " );
		int r = (int) MOMaths.lerp(blendAmt, c1r, c2r);
		int g = (int) MOMaths.lerp(blendAmt, c1g, c2g);
		int b = (int) MOMaths.lerp(blendAmt, c1b, c2b);
		int a = (int) MOMaths.lerp(blendAmt, c1a, c2a);
		//System.out.println("blend color " + r + " " + g + " "+ b + " "+ a + " " );
		return new Color(r,g,b,a);
		
	}
	
}

