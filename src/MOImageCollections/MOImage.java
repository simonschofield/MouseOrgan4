package MOImageCollections;

import java.awt.image.BufferedImage;

import MOMaths.PVector;
///////////////////////////////////////////////////////////////////////////
// This is the basic wrapper around any bitmap item that is loaded as part of a Mouse Organ session.
// Mainly used for the content of an image.
// contains permanent data about a loaded image. The image, it's short file name,
// and the ImageStats, all of which can be used to select the image
// In naming conventions of classes and methods, this is shortened to "Image"
//
public class MOImage{
	BufferedImage image;
	String name = "";
	public ImageStats stats;

	public void calculateStats() {
		if(image==null) {
			
		System.out.println("calculateStats: image not initialised");
		}
		stats = new ImageStats(image);
		stats.printStats(name);
	}
	
	
	
	
}

