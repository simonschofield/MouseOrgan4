package MOSprite;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.BendImage;
import MOImage.ImageProcessing;
import MOImageCollections.ImageAsset;

public class SpriteImages{
	
	ArrayList<ImageAsset> imageList = new ArrayList<ImageAsset>();

	
	
		
	public SpriteImages(){
		clear();
	}
	
	boolean isInitialised() {
		// The SpriteImages are considered as initialised if the there is an image set in the list;
		if(imageList.size()==0) return false;
		return true;
	}
	
	public void clear() {
		imageList = new ArrayList<ImageAsset>();
	}
	
	
	
	public BufferedImage getImage(String nm) {
		return getImageAsset(nm).image;
	}
	
	public BufferedImage getImage(int i) {
		return imageList.get(i).image ;
	}
	
	public int getNumImages() {
		return imageList.size();
	}
	
	public boolean imageNameExists(String nm) {
		for (ImageAsset thisImage : imageList) {
			if (thisImage.name.contentEquals(nm))
				return true;
		}
		return false;
	}
	
	public void setImage(int i, BufferedImage img) {
		// 
		imageList.get(i).image = img;
		
	}
	
	
	public void setImage(String nm, BufferedImage img) {
		// if there are no images, add the "main" image
		// if the image exists then the image is replaced, 
		// if the image does not exist then the image is added
		if ( isInitialised() == false ) {
			addImage("main", img);
			return;
		}
		
		img = sizeToMainImageDims(img);
		
		if(imageNameExists(nm)) {
			getImageAsset(nm).image = img;
			return;
		}
		
		addImage(nm, img);
		
	}
	
	private void addImage(String name, BufferedImage img) {
		img = ImageProcessing.assertImageTYPE_INT_ARGB(img);
		imageList.add(new ImageAsset(img,name));
	}

	private ImageAsset getImageAsset(String nm) {
		int n = 0;
		for (ImageAsset thisImage : imageList) {
			if (thisImage.name.contentEquals(nm))
				return thisImage;
			n++;
		}
		System.out.println("SpriteImageStack:getImageStackElement - cannot find element called " + nm);
		return null;
	}
	
	BufferedImage sizeToMainImageDims(BufferedImage img) {
		if ( isInitialised() == false ) return img;
		int w = getImage(0).getWidth();
		int h = getImage(0).getHeight();
		return ImageProcessing.resizeTo(img, w, h);
	}
	
	
	public void scale(float scaleW, float scaleH) {
		for (ImageAsset thisImageAsset : imageList) {
			if(scaleW==scaleH) {
				// chance to use double scaling on very big scale reductions
				thisImageAsset.image = ImageProcessing.scaleImage(thisImageAsset.image, scaleW);
			}else {
				thisImageAsset.image = ImageProcessing.scaleImage(thisImageAsset.image, scaleW, scaleH);
			}
		}
		
	}
	
	public void rotate(float degrees) {
		for (ImageAsset thisImageAsset : imageList) {
			thisImageAsset.image = ImageProcessing.rotateImage(thisImageAsset.image, degrees);
		}
	}
	
	public void mirror(boolean inX) {
		for (ImageAsset thisImageAsset : imageList) {
			if (inX) {
				thisImageAsset.image = ImageProcessing.mirrorImage(thisImageAsset.image, true, false);
			} else {
				// in Y
				thisImageAsset.image = ImageProcessing.mirrorImage(thisImageAsset.image,false, true);
			}
		}
		
	}
	
	public void bend(float startBend, float bendAmt, float severity) {
		
		BendImage bendImage = new BendImage();
		for (ImageAsset thisImageAsset : imageList) {
			thisImageAsset.image = bendImage.bendImage(thisImageAsset.image, startBend, bendAmt, severity);
		}
	}
	
	public void removeAllImages() {
		// this should be called after the sprite has "finished" so as to to avoid
		// taking up loads of memory.
		for (ImageAsset thisImageAsset : imageList) {
			thisImageAsset.image = null;
		}
	}
	
	
	
	
}