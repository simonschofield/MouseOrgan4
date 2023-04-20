package MOImage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOMaths.QRandomStream;

public class MOColorImagePalette{
	
	BufferedImage thePalette;
	QRandomStream  random;
	public MOColorImagePalette(BufferedImage colorPalette){
		// uses a vertical image of color bands to create a palete
		thePalette = colorPalette;
		random = new QRandomStream(1);
		
	}
	
	
	public void setRandomState(int i) {
		random.setState(i);
	}
	
	
	
	
	
	public Color getRandomColor() {
		int x = random.randRangeInt(3, thePalette.getWidth()-3);
		int y = random.randRangeInt(3, thePalette.getHeight()-3);
		int ci =  thePalette.getRGB(x, y);
		return new Color(ci);
		
	}
	
	
	
	
	
}
