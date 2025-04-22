package MOImage;

import java.awt.Color;

import MOMaths.MOMaths;

public class MOPackedColor {
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// pixel transforms
	//	

	static int[] unpackARGB(int packedCol) {
		int[] col = new int[4];
		col[0] = (packedCol >> 24) & 0xFF;// alpha
		col[1] = (packedCol >> 16) & 0xFF;// red
		col[2] = (packedCol >> 8) & 0xFF; // green
		col[3] = packedCol & 0xFF; // blue
		return col;
	}


	static int getRed(int packedCol) {
		return (packedCol >> 16) & 0xFF;// red
	}

	static int getGreen(int packedCol) {
		return (packedCol >> 8) & 0xFF; // green
	}

	static int getBlue(int packedCol) {
		return packedCol & 0xFF; // blue
	}

	public static int getAlpha(int packedCol) {
		return (packedCol >> 24) & 0xFF;// alpha
	}

	public static void unpackARGB(int packedCol, int[] col) {

		col[0] = (packedCol >> 24) & 0xFF;// alpha
		col[1] = (packedCol >> 16) & 0xFF;// red
		col[2] = (packedCol >> 8) & 0xFF; // green
		col[3] = packedCol & 0xFF; // blue
	}

	static int getChannelFromARGB(int packedCol, int channel) {
		return unpackARGB(packedCol)[channel];

	}

	public static Color packedIntToColor(int packedCol, boolean hasAlpha) {
		return new Color(packedCol, hasAlpha);
	}

	static float packedIntToVal01(int packedCol, boolean hasAlpha) {
		Color c = packedIntToColor(packedCol, hasAlpha);
		return (c.getRed() + c.getGreen() + c.getBlue()) / 765f;
	}

	public static int packARGB(int a, int r, int g, int b) {
		// Packs four 8 bit numbers into one 32 bit number

		a = a << 24; // Binary: 11111111000000000000000000000000
		r = r << 16; // Binary: 00000000110011000000000000000000
		g = g << 8; // Binary: 00000000000000001100110000000000
		// b remains untouched

		int argb = a | r | g | b;
		return argb;
	}

	

}
