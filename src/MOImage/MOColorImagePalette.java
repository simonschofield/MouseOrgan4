package MOImage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MOColorImagePalette extends MONamedColors{
	
	ArrayList<Color> imageBasedPalette = new ArrayList<Color>();
	
	public MOColorImagePalette(BufferedImage colorPalette, int numCols){
		// uses a vertical image of color bands to create a palete
		
		
		int x =  (int)(colorPalette.getWidth()/2f);
		int height = colorPalette.getHeight();
		
		int step = (int)(height/(float)numCols);
		
		for(int y = 0; y<height; y+= step) {
			int ci = colorPalette.getRGB(x, y);
			Color c = new Color(ci);
			imageBasedPalette.add(c);
		}
	}
	
	
	Color getImageBasedColor(int i) {
		
		return imageBasedPalette.get(i);
	}
	
	
	public Color getRandomImageBasedColor() {
		int limit = imageBasedPalette.size();
		int i = random.randRangeInt(0, limit-1);
		return imageBasedPalette.get(i);
		
	}
	
	
	
	
	
}
