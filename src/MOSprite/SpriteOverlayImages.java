package MOSprite;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.BendImage;
import MOImage.ImageProcessing;
import MOImageCollections.ImageAsset;


//////////////////////////////////////////////////////////////////////////////////////////////////////////////
// THis class is contained by the Sprite Class
// Overlay images are RGBA images that are kept "in step" with the main image though any geometric transformations.
// They contain elements which can be treated differently from the main sprite (such as flowers on a larger plant, or a shirt on a person), but are in-register with the sprite through all its
// different geometric transforms, and use the Sprite's pivot-point when pasting etc. 

// All overlay images must be the same dimensions as the owning sprite's main sprite image. This is enforced in this class.
// All overlay images are kept in step with any subsequent GEOMETRIC image operations to he main sprite image 
// They can then be pasted, and will use the owning sprites pivot point to do so. using pasteSpriteAltImage(sprite, blendedImage,1f);
//
//

public class SpriteOverlayImages {
	
	ArrayList<ImageAsset> imageList = new ArrayList<ImageAsset>();
	
	Sprite owningSprite;
	
	public SpriteOverlayImages(Sprite s){
		owningSprite = s;
	}
	
	public void addOverlayImage(BufferedImage img, String name) {
		
		img = ImageProcessing.assertImageTYPE_INT_ARGB(img);
		
		
		
		img = sizeToMainImageDims( img);
		imageList.add(new ImageAsset(img,name));
	}
	
	public BufferedImage getImage(String nm) {
		return getImageAsset(nm).image;
	}
	
	public BufferedImage getImage(int i) {
		return imageList.get(i).image;
	}
	
	public int getNumOverlayImage() {
		return imageList.size();
	}
	
	
	public void setImage(String nm, BufferedImage img) {
		img = sizeToMainImageDims( img);
		getImageAsset(nm).image = img;
	}
	
	public void setImage(int i, BufferedImage img) {
		img = sizeToMainImageDims( img);
		imageList.get(i).image = img;
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
		return ImageProcessing.resizeTo(img, owningSprite.getImageWidth(), owningSprite.getImageHeight());
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
	
	
}



