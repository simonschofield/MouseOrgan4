package MOSpriteSeed;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOImageCollections.SpriteImageGroup;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.GlobalSettings;

//implements SpriteSourceInterface
public class PolygonPatchSpriteFont extends SpriteSeedFont{

    


	public PolygonPatchSpriteFont(String sdFontName, String imageSampleGroupName, int rseed) {
		super( sdFontName,  imageSampleGroupName, 1,  false,  new PVector(0,0),  rseed);
		

	}

	// call this to get the patch
	public Sprite getSpriteInstance(Vertices2 polygon) {
		Rect inSceneExtents = polygon.getExtents();
		System.out.println("inSceneExtents " + inSceneExtents.toStr());
		// make a translated copy, with its TLHC at (0,0)
		Vertices2 spriteBufferSpacePolygon = polygon.getInBufferSpace(true);
		System.out.println("bufferSpaceExtents " + spriteBufferSpacePolygon.getExtents().toStr());
		
		
		if( spriteBufferSpacePolygon.getExtents().getWidth() < 1 || spriteBufferSpacePolygon.getExtents().getHeight() < 1) return null;
		
		
		
		BufferedImage polygonMask = createMask(spriteBufferSpacePolygon);

		int n = getRandomSpriteImageGroupItemNumber();
		BufferedImage chosenTexture  = getSpriteImageGroup().getImage(n);
		
		// tbd
		//chosenTexture = applyGeometricTransforms(chosenTexture, polygonMask);
		
		
		BufferedImage croppedTexture = cropTextureToMask(chosenTexture, polygonMask, true);
		
		System.out.println("croppedTexture " + croppedTexture.getWidth() + "," + croppedTexture.getHeight());
		System.out.println("polygonMask " + polygonMask.getWidth() + "," + polygonMask.getHeight());
		BufferedImage extractedPatch = ImageProcessing.extractImageUsingGrayscaleMask(croppedTexture, polygonMask);
		
		SpriteSeed seed = getSpriteSeedInstance(n);
		seed.origin = new PVector(0,0);
		seed.setDocPoint(inSceneExtents.getTopLeft());
		
		Sprite sprite = new Sprite(seed);
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
			chosenTexture = ImageProcessing.resizeTo(chosenTexture, polygonMask.getWidth(), polygonMask.getHeight());
			return chosenTexture;
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
