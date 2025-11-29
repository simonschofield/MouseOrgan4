package MOImageCollections;

import java.awt.image.BufferedImage;
///////////////////////////////////////////////////////////////////////////
// This is the basic wrapper around any bitmap item that is loaded as part of a Mouse Organ session.
// Mainly used for the content of an image.
// contains permanent data about a loaded image. The image, it's short file name,
// and the ImageStats, all of which can be used to select the image
// In naming conventions of classes and methods, this is shortened to "Image"
//
public class ImageAsset{

	// the buffered image
	public BufferedImage image;

	// the user-set name, which may also be the short file name
	public String name = "";

	// the path to the asset on file
	public String fullPath;

	// image stats
	public ImageStats stats;

	public ImageAsset() {}

	public ImageAsset(BufferedImage img, String nm) {
		image = img;
		name = nm;
	}


	public void calculateStats() {
		if(image==null) {

		System.out.println("calculateStats: image not initialised");
		}
		stats = new ImageStats(image);
		stats.printStats(name);
	}




}

