package MOImage;

import java.awt.Color;
import java.util.ArrayList;



class MONamedColors {

    /**
     * Initialize the color list that we have.
     */
	
	
	static ArrayList<NamedColor> colorList;
	
	
	static {
		
		colorList = new ArrayList<NamedColor>();
        colorList.add(new NamedColor("AliceBlue", 0xF0, 0xF8, 0xFF));
        colorList.add(new NamedColor("AntiqueWhite", 0xFA, 0xEB, 0xD7));
        colorList.add(new NamedColor("Aqua", 0x00, 0xFF, 0xFF));
        colorList.add(new NamedColor("Aquamarine", 0x7F, 0xFF, 0xD4));
        colorList.add(new NamedColor("Azure", 0xF0, 0xFF, 0xFF));
        colorList.add(new NamedColor("Beige", 0xF5, 0xF5, 0xDC));
        colorList.add(new NamedColor("Bisque", 0xFF, 0xE4, 0xC4));
        colorList.add(new NamedColor("Black", 0x00, 0x00, 0x00));
        colorList.add(new NamedColor("BlanchedAlmond", 0xFF, 0xEB, 0xCD));
        colorList.add(new NamedColor("Blue", 0x00, 0x00, 0xFF));
        colorList.add(new NamedColor("BlueViolet", 0x8A, 0x2B, 0xE2));
        colorList.add(new NamedColor("Brown", 0xA5, 0x2A, 0x2A));
        colorList.add(new NamedColor("BurlyWood", 0xDE, 0xB8, 0x87));
        colorList.add(new NamedColor("CadetBlue", 0x5F, 0x9E, 0xA0));
        colorList.add(new NamedColor("Chartreuse", 0x7F, 0xFF, 0x00));
        colorList.add(new NamedColor("Chocolate", 0xD2, 0x69, 0x1E));
        colorList.add(new NamedColor("Coral", 0xFF, 0x7F, 0x50));
        colorList.add(new NamedColor("CornflowerBlue", 0x64, 0x95, 0xED));
        colorList.add(new NamedColor("Cornsilk", 0xFF, 0xF8, 0xDC));
        colorList.add(new NamedColor("Crimson", 0xDC, 0x14, 0x3C));
        colorList.add(new NamedColor("Cyan", 0x00, 0xFF, 0xFF));
        colorList.add(new NamedColor("DarkBlue", 0x00, 0x00, 0x8B));
        colorList.add(new NamedColor("DarkCyan", 0x00, 0x8B, 0x8B));
        colorList.add(new NamedColor("DarkGoldenRod", 0xB8, 0x86, 0x0B));
        colorList.add(new NamedColor("DarkGray", 0xA9, 0xA9, 0xA9));
        colorList.add(new NamedColor("DarkGreen", 0x00, 0x64, 0x00));
        colorList.add(new NamedColor("DarkKhaki", 0xBD, 0xB7, 0x6B));
        colorList.add(new NamedColor("DarkMagenta", 0x8B, 0x00, 0x8B));
        colorList.add(new NamedColor("DarkOliveGreen", 0x55, 0x6B, 0x2F));
        colorList.add(new NamedColor("DarkOrange", 0xFF, 0x8C, 0x00));
        colorList.add(new NamedColor("DarkOrchid", 0x99, 0x32, 0xCC));
        colorList.add(new NamedColor("DarkRed", 0x8B, 0x00, 0x00));
        colorList.add(new NamedColor("DarkSalmon", 0xE9, 0x96, 0x7A));
        colorList.add(new NamedColor("DarkSeaGreen", 0x8F, 0xBC, 0x8F));
        colorList.add(new NamedColor("DarkSlateBlue", 0x48, 0x3D, 0x8B));
        colorList.add(new NamedColor("DarkSlateGray", 0x2F, 0x4F, 0x4F));
        colorList.add(new NamedColor("DarkTurquoise", 0x00, 0xCE, 0xD1));
        colorList.add(new NamedColor("DarkViolet", 0x94, 0x00, 0xD3));
        colorList.add(new NamedColor("DeepPink", 0xFF, 0x14, 0x93));
        colorList.add(new NamedColor("DeepSkyBlue", 0x00, 0xBF, 0xFF));
        colorList.add(new NamedColor("DimGray", 0x69, 0x69, 0x69));
        colorList.add(new NamedColor("DodgerBlue", 0x1E, 0x90, 0xFF));
        colorList.add(new NamedColor("FireBrick", 0xB2, 0x22, 0x22));
        colorList.add(new NamedColor("FloralWhite", 0xFF, 0xFA, 0xF0));
        colorList.add(new NamedColor("ForestGreen", 0x22, 0x8B, 0x22));
        colorList.add(new NamedColor("Fuchsia", 0xFF, 0x00, 0xFF));
        colorList.add(new NamedColor("Gainsboro", 0xDC, 0xDC, 0xDC));
        colorList.add(new NamedColor("GhostWhite", 0xF8, 0xF8, 0xFF));
        colorList.add(new NamedColor("Gold", 0xFF, 0xD7, 0x00));
        colorList.add(new NamedColor("GoldenRod", 0xDA, 0xA5, 0x20));
        colorList.add(new NamedColor("Gray", 0x80, 0x80, 0x80));
        colorList.add(new NamedColor("Green", 0x00, 0x80, 0x00));
        colorList.add(new NamedColor("GreenYellow", 0xAD, 0xFF, 0x2F));
        colorList.add(new NamedColor("HoneyDew", 0xF0, 0xFF, 0xF0));
        colorList.add(new NamedColor("HotPink", 0xFF, 0x69, 0xB4));
        colorList.add(new NamedColor("IndianRed", 0xCD, 0x5C, 0x5C));
        colorList.add(new NamedColor("Indigo", 0x4B, 0x00, 0x82));
        colorList.add(new NamedColor("Ivory", 0xFF, 0xFF, 0xF0));
        colorList.add(new NamedColor("Khaki", 0xF0, 0xE6, 0x8C));
        colorList.add(new NamedColor("Lavender", 0xE6, 0xE6, 0xFA));
        colorList.add(new NamedColor("LavenderBlush", 0xFF, 0xF0, 0xF5));
        colorList.add(new NamedColor("LawnGreen", 0x7C, 0xFC, 0x00));
        colorList.add(new NamedColor("LemonChiffon", 0xFF, 0xFA, 0xCD));
        colorList.add(new NamedColor("LightBlue", 0xAD, 0xD8, 0xE6));
        colorList.add(new NamedColor("LightCoral", 0xF0, 0x80, 0x80));
        colorList.add(new NamedColor("LightCyan", 0xE0, 0xFF, 0xFF));
        colorList.add(new NamedColor("LightGoldenRodYellow", 0xFA, 0xFA, 0xD2));
        colorList.add(new NamedColor("LightGray", 0xD3, 0xD3, 0xD3));
        colorList.add(new NamedColor("LightGreen", 0x90, 0xEE, 0x90));
        colorList.add(new NamedColor("LightPink", 0xFF, 0xB6, 0xC1));
        colorList.add(new NamedColor("LightSalmon", 0xFF, 0xA0, 0x7A));
        colorList.add(new NamedColor("LightSeaGreen", 0x20, 0xB2, 0xAA));
        colorList.add(new NamedColor("LightSkyBlue", 0x87, 0xCE, 0xFA));
        colorList.add(new NamedColor("LightSlateGray", 0x77, 0x88, 0x99));
        colorList.add(new NamedColor("LightSteelBlue", 0xB0, 0xC4, 0xDE));
        colorList.add(new NamedColor("LightYellow", 0xFF, 0xFF, 0xE0));
        colorList.add(new NamedColor("Lime", 0x00, 0xFF, 0x00));
        colorList.add(new NamedColor("LimeGreen", 0x32, 0xCD, 0x32));
        colorList.add(new NamedColor("Linen", 0xFA, 0xF0, 0xE6));
        colorList.add(new NamedColor("Magenta", 0xFF, 0x00, 0xFF));
        colorList.add(new NamedColor("Maroon", 0x80, 0x00, 0x00));
        colorList.add(new NamedColor("MediumAquaMarine", 0x66, 0xCD, 0xAA));
        colorList.add(new NamedColor("MediumBlue", 0x00, 0x00, 0xCD));
        colorList.add(new NamedColor("MediumOrchid", 0xBA, 0x55, 0xD3));
        colorList.add(new NamedColor("MediumPurple", 0x93, 0x70, 0xDB));
        colorList.add(new NamedColor("MediumSeaGreen", 0x3C, 0xB3, 0x71));
        colorList.add(new NamedColor("MediumSlateBlue", 0x7B, 0x68, 0xEE));
        colorList.add(new NamedColor("MediumSpringGreen", 0x00, 0xFA, 0x9A));
        colorList.add(new NamedColor("MediumTurquoise", 0x48, 0xD1, 0xCC));
        colorList.add(new NamedColor("MediumVioletRed", 0xC7, 0x15, 0x85));
        colorList.add(new NamedColor("MidnightBlue", 0x19, 0x19, 0x70));
        colorList.add(new NamedColor("MintCream", 0xF5, 0xFF, 0xFA));
        colorList.add(new NamedColor("MistyRose", 0xFF, 0xE4, 0xE1));
        colorList.add(new NamedColor("Moccasin", 0xFF, 0xE4, 0xB5));
        colorList.add(new NamedColor("NavajoWhite", 0xFF, 0xDE, 0xAD));
        colorList.add(new NamedColor("Navy", 0x00, 0x00, 0x80));
        colorList.add(new NamedColor("OldLace", 0xFD, 0xF5, 0xE6));
        colorList.add(new NamedColor("Olive", 0x80, 0x80, 0x00));
        colorList.add(new NamedColor("OliveDrab", 0x6B, 0x8E, 0x23));
        colorList.add(new NamedColor("Orange", 0xFF, 0xA5, 0x00));
        colorList.add(new NamedColor("OrangeRed", 0xFF, 0x45, 0x00));
        colorList.add(new NamedColor("Orchid", 0xDA, 0x70, 0xD6));
        colorList.add(new NamedColor("PaleGoldenRod", 0xEE, 0xE8, 0xAA));
        colorList.add(new NamedColor("PaleGreen", 0x98, 0xFB, 0x98));
        colorList.add(new NamedColor("PaleTurquoise", 0xAF, 0xEE, 0xEE));
        colorList.add(new NamedColor("PaleVioletRed", 0xDB, 0x70, 0x93));
        colorList.add(new NamedColor("PapayaWhip", 0xFF, 0xEF, 0xD5));
        colorList.add(new NamedColor("PeachPuff", 0xFF, 0xDA, 0xB9));
        colorList.add(new NamedColor("Peru", 0xCD, 0x85, 0x3F));
        colorList.add(new NamedColor("Pink", 0xFF, 0xC0, 0xCB));
        colorList.add(new NamedColor("Plum", 0xDD, 0xA0, 0xDD));
        colorList.add(new NamedColor("PowderBlue", 0xB0, 0xE0, 0xE6));
        colorList.add(new NamedColor("Purple", 0x80, 0x00, 0x80));
        colorList.add(new NamedColor("Red", 0xFF, 0x00, 0x00));
        colorList.add(new NamedColor("RosyBrown", 0xBC, 0x8F, 0x8F));
        colorList.add(new NamedColor("RoyalBlue", 0x41, 0x69, 0xE1));
        colorList.add(new NamedColor("SaddleBrown", 0x8B, 0x45, 0x13));
        colorList.add(new NamedColor("Salmon", 0xFA, 0x80, 0x72));
        colorList.add(new NamedColor("SandyBrown", 0xF4, 0xA4, 0x60));
        colorList.add(new NamedColor("SeaGreen", 0x2E, 0x8B, 0x57));
        colorList.add(new NamedColor("SeaShell", 0xFF, 0xF5, 0xEE));
        colorList.add(new NamedColor("Sienna", 0xA0, 0x52, 0x2D));
        colorList.add(new NamedColor("Silver", 0xC0, 0xC0, 0xC0));
        colorList.add(new NamedColor("SkyBlue", 0x87, 0xCE, 0xEB));
        colorList.add(new NamedColor("SlateBlue", 0x6A, 0x5A, 0xCD));
        colorList.add(new NamedColor("SlateGray", 0x70, 0x80, 0x90));
        colorList.add(new NamedColor("Snow", 0xFF, 0xFA, 0xFA));
        colorList.add(new NamedColor("SpringGreen", 0x00, 0xFF, 0x7F));
        colorList.add(new NamedColor("SteelBlue", 0x46, 0x82, 0xB4));
        colorList.add(new NamedColor("Tan", 0xD2, 0xB4, 0x8C));
        colorList.add(new NamedColor("Teal", 0x00, 0x80, 0x80));
        colorList.add(new NamedColor("Thistle", 0xD8, 0xBF, 0xD8));
        colorList.add(new NamedColor("Tomato", 0xFF, 0x63, 0x47));
        colorList.add(new NamedColor("Turquoise", 0x40, 0xE0, 0xD0));
        colorList.add(new NamedColor("Violet", 0xEE, 0x82, 0xEE));
        colorList.add(new NamedColor("Wheat", 0xF5, 0xDE, 0xB3));
        colorList.add(new NamedColor("White", 0xFF, 0xFF, 0xFF));
        colorList.add(new NamedColor("WhiteSmoke", 0xF5, 0xF5, 0xF5));
        colorList.add(new NamedColor("Yellow", 0xFF, 0xFF, 0x00));
        colorList.add(new NamedColor("YellowGreen", 0x9A, 0xCD, 0x32));
        
		
		
	}
	
	MONamedColors(){
		
        
    }

    
    public static Color getColor(String name) {

    	NamedColor nc = getNamedColor( name);
    	return nc.getColor();
    }
    
    private static NamedColor getNamedColor(String name) {
    	String lowercaseSearchString = name.toLowerCase();
    	// is deliberately case-insensitive
    	for (NamedColor c : colorList) {
    		String lowercaseColorName = c.getName().toLowerCase();
    		if( lowercaseColorName.equals(lowercaseSearchString) ) return c;
    	}
    	System.out.println("NamedColors:: cannot find a color called " + name + " returning BLACK");
    	return new NamedColor();
    }
    
    
    public static String getColorNameFromRgb(int r, int g, int b) {
        
        NamedColor closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (NamedColor c : colorList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            return closestMatch.getName();
        } else {
            return "No matched color name.";
        }
    }

    /**
     * Convert hexColor to rgb, then call getColorNameFromRgb(r, g, b)
     * 
     * @param hexColor
     * @return
     
    public String getColorNameFromHex(int hexColor) {
        int r = (hexColor & 0xFF0000) >> 16;
        int g = (hexColor & 0xFF00) >> 8;
        int b = (hexColor & 0xFF);
        return getColorNameFromRgb(r, g, b);
    }

    public int colorToHex(Color c) {
        return Integer.decode("0x"
                + Integer.toHexString(c.getRGB()).substring(2));
    }

    public String getColorNameFromColor(Color color) {
        return getColorNameFromRgb(color.getRed(), color.getGreen(),
                color.getBlue());
    }
    */

    /**
     * inner class the named color type
     * 
     */
    private static class NamedColor {
        public int r, g, b;
        public String name;

        public NamedColor() {
        	this.r = 0;
            this.g = 0;
            this.b = 0;
            this.name = "NOCOLOR";
        	
        }
        
        public NamedColor(String name, int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
        }

        public int computeMSE(int pixR, int pixG, int pixB) {
        	// computes the Mean Squared Error between the incoming value, and this rgb value
            return (int) (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b)) / 3);
        }
        
        public Color getColor() {
        	int pc = getPackedInt();
        	return MOPackedColor.packedIntToColor(pc,false);
        }
        
        public int getPackedInt() {
        	return MOPackedColor.packARGB(255, this.r, this.g, this.b);
        }

        public int getR() {
            return r;
        }

        public int getG() {
            return g;
        }

        public int getB() {
            return b;
        }

        public String getName() {
            return name;
        }
    }
}


