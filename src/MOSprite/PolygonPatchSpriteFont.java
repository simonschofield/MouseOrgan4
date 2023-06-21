package MOSprite;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOImageCollections.ScaledImageAssetGroup;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.GlobalSettings;

//implements SpriteSourceInterface
public class PolygonPatchSpriteFont extends SpriteFont{

    
	float scaleToFit = 0;

	public PolygonPatchSpriteFont(String sdFontName, String imageSampleGroupName, int rseed) {
		super( sdFontName,  imageSampleGroupName, 1,  false,  new PVector(0,0),  rseed);
		

	}
	
	// if s == 0, then no scaling involved. You get the unscaled texture
	// if s == 0.5 the the textures is scaled 50% towards exact fit
	// if s == 1; then texture is scaled to fit the mask - 
	public void setScaleToFit(float s) {
		
		scaleToFit = s;
		
	}
	
	
	
	BufferedImage scaleImageToFitMask(BufferedImage img, BufferedImage mask, float amt) {
		
		if(amt==0) return img;
		
		
		float maskW = mask.getWidth();
		float maskH = mask.getHeight();
		float maskAspect = maskW/maskH;
		
		float imageW = img.getWidth();
		float imageH = img.getHeight();
		float aspectImg = imageW/imageH;
		
		// if the patch is bigger than the image, return the unscaled image
		if(maskW > imageW || maskH > imageH) return img;
		
		// so now the image is bigger than the patch
		float scalefactor = 1f;
		if(aspectImg > maskAspect) {
			// make the height of the image tend towards the height of the patch with scaleToFit parametric
			float targetImageHeight = MOMaths.lerp(amt, imageH, maskH);
			scalefactor = targetImageHeight/imageH;
		}else {
			// make the width of the image tend towards the width of the patch with scaleToFit parametric
			float targetImageWidth = MOMaths.lerp(amt, imageW, maskW);
			scalefactor = targetImageWidth/imageW;
		}

		return ImageProcessing.scaleImage(img, scalefactor, scalefactor);
	}


	
	public Sprite getSpriteInstance(Vertices2 polygon) {
		Rect inSceneExtents = polygon.getExtents();
		
		// make a translated copy, with its TLHC at (0,0)
		Vertices2 spriteBufferSpacePolygon = polygon.getInBufferSpace(true);
		
		if( spriteBufferSpacePolygon.getExtents().getWidth() < 1 || spriteBufferSpacePolygon.getExtents().getHeight() < 1) return null;
		
		BufferedImage polygonMask = createMask(spriteBufferSpacePolygon);

		Sprite sprite = getSpriteInstance(false);
		sprite.setSpriteFontDataAndSelectImage(this);
		
		BufferedImage chosenTexture = sprite.getImage();
		
		// tbd
		//chosenTexture = scaleImageToFitMask(chosenTexture, polygonMask, scaleToFit);
		BufferedImage croppedTexture = cropTextureToMask(chosenTexture, polygonMask, true);
		
		BufferedImage extractedPatch = ImageProcessing.extractImageUsingGrayscaleMask(croppedTexture, polygonMask);

		sprite.setPivotPoint( new PVector(0,0));
		sprite.setDocPoint(inSceneExtents.getTopLeft());
		sprite.setImage(extractedPatch);
		
		return sprite;
	}
	
	
	BufferedImage createMask(Vertices2 spriteBufferSpacePolygon) {
		Rect bufferSpaceExtents = spriteBufferSpacePolygon.getExtents();
		RenderTarget rt = new RenderTarget((int)bufferSpaceExtents.getWidth(), (int)bufferSpaceExtents.getHeight(), BufferedImage.TYPE_INT_ARGB);
		rt.fillBackground(Color.BLACK);
		rt.getVectorShapeDrawer().setDrawingStyle(Color.WHITE,  Color.WHITE, 1);
		rt.getVectorShapeDrawer().drawVertices2(spriteBufferSpacePolygon);
		return rt.getImage();
	}
	
	BufferedImage  cropTextureToMask(BufferedImage chosenTexture, BufferedImage polygonMask, boolean randomDisplace) {
		
		int xlatitude = chosenTexture.getWidth()-polygonMask.getWidth();
		int ylatitude = chosenTexture.getHeight()-polygonMask.getHeight();
		
		if( xlatitude < 0 || ylatitude < 0) {
			System.out.println("Warning, PolygonPatchSpriteFont:: mask is larger than texture ");
			//chosenTexture = ImageProcessing.resizeTo(chosenTexture, polygonMask.getWidth(), polygonMask.getHeight());
			Rect maskRect = ImageProcessing.getImageBufferRect(polygonMask);
			chosenTexture = ImageProcessing.scaleImageToFitRect(chosenTexture,maskRect);
			xlatitude = 0;
			ylatitude = 0;
			//return chosenTexture;
		}
		
		
		int xoff = 0;
		int yoff = 0;
		
		if(randomDisplace) {
			xoff = randomStream.randRangeInt(0, xlatitude);
			yoff = randomStream.randRangeInt(0, ylatitude);
		}
		
		Rect cropRect = new Rect(xoff,yoff,polygonMask.getWidth(),polygonMask.getHeight());
		BufferedImage croppedTexture = ImageProcessing.deepCropImage(chosenTexture, cropRect);
		
		
		return croppedTexture;
	}

	 
}
