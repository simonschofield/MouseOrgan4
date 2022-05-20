package MOImage;

import java.awt.Color;

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
	
	public static Color getRandomRGB() {
		int r = ranStream.randRangeInt(0, 255);
		int g = ranStream.randRangeInt(0, 255);
		int b = ranStream.randRangeInt(0, 255);
		
		return new Color(r,g,b);
	}
	
}
