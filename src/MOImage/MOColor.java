package MOImage;

import java.awt.Color;

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
