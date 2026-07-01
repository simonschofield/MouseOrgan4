package MOSprite;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.BendImage;
import MOImage.ImageProcessing;
import MOImageCollections.ImageAsset;

public class SpriteImages{

	ArrayList<ImageAsset> imageList = new ArrayList<>();

	String currentImageName = "main";


	public SpriteImages(){
		clear();
	}

	boolean isInitialised() {
		// The SpriteImages are considered as initialised if the there is an image set in the list;
		if(imageList.size()==0) {
			return false;
		}
		return true;
	}

	public void clear() {
		imageList = new ArrayList<>();
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
		if(nm == null) return false;
		for (ImageAsset thisImage : imageList) {
			if (thisImage.name.contentEquals(nm)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean imageNameExistsContaining(String nm) {
		if(nm == null) return false;
		for (ImageAsset thisImage : imageList) {
			if (thisImage.name.contains(nm)) {
				return true;
			}
		}
		return false;
	}
	
	public String getImageName(int i) {
		if(i > getNumImages()) return null;
		return imageList.get(i).name;
	}

	public void setImage(int i, BufferedImage img) {
		//
		imageList.get(i).image = ImageProcessing.copyImage(img);

	}


	public void setImage(String nm, BufferedImage img) {
		// if there are no images, add the "main" image
		// if the image exists then the image is replaced,
		// if the image does not exist then the image is added
		if ( !isInitialised() ) {
			addImage("main", img);
			return;
		}

		//img = sizeToMainImageDims(img);
		//checkOverlayImagesMatchingDimensions("SpriteImages::setImage");
		if(imageNameExists(nm)) {
			getImageAsset(nm).image = ImageProcessing.copyImage(img);
			return;
		}

		addImage(nm, img);

	}

	private void addImage(String name, BufferedImage img) {
		BufferedImage copyImg = ImageProcessing.copyImage(img);
		copyImg = ImageProcessing.assertImageTYPE_INT_ARGB(copyImg);
		imageList.add(new ImageAsset(copyImg,name));
	}

	private ImageAsset getImageAsset(String nm) {
		int n = 0;
		for (ImageAsset thisImage : imageList) {
			if (thisImage.name.contentEquals(nm)) {
				return thisImage;
			}
			n++;
		}
		System.out.println("SpriteImageStack:getImageStackElement - cannot find element called " + nm);
		return null;
	}

	BufferedImage sizeToMainImageDims(BufferedImage img) {
		// DOES NOT WORK
		System.out.println("SpriteImages::sizeToMainImageDims being called, this method does not work - returning image unaltered");
		return img;
//		if ( !isInitialised() ) {
//			return img;
//		}
//		
//		int w = getImage(0).getWidth();
//		int h = getImage(0).getHeight();
//		return ImageProcessing.resizeTo(img, w, h);
	}
	
	
	public boolean checkOverlayImagesMatchingDimensions(String message) {
		int w = getImage(0).getWidth();
		int h = getImage(0).getHeight();
		int numImages = imageList.size();
		if(numImages==1) return true;
		for (int n = 1; n < numImages; n++) {
			int iw = getImage(n).getWidth();
			int ih = getImage(n).getHeight();
			
			if(w==iw && h== ih) {
				//System.out.println(message + " image sizes DO match - main image is " + w + " " + h + " while overlay image is " + iw + " " + ih );
				return true;
			}
			
			System.out.println(message + " image sizes do not match - main image is " + w + " " + h + " while overlay image is " + iw + " " + ih );
			return false;
		}
		return false;
	}


	public void scale(float scaleW, float scaleH) {
		
		//enforceSameSizeOnAllImages("before scale operation");
		
		for (ImageAsset thisImageAsset : imageList) {
			if(scaleW==scaleH) {
				// chance to use double scaling on very big scale reductions
				thisImageAsset.image = ImageProcessing.scaleImage(thisImageAsset.image, scaleW);
			}else {
				thisImageAsset.image = ImageProcessing.scaleImage(thisImageAsset.image, scaleW, scaleH);
			}
		}
		
		//enforceSameSizeOnAllImages("after scale operation");

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
		imageList.clear();
		
		
		//for (ImageAsset thisImageAsset : imageList) {
		//	thisImageAsset.image = null;
		//}
	}




}