package MOAppSessionHelpers;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOCompositing.RenderTarget;
import MOCompositing.Sprite;
import MOImage.ImageProcessing;
import MOImage.KeyImageSampler;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Plane3D;
import MOScene3D.ProjectedLight3D;
import MOScene3D.SceneData3D;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;

public class Scene3DSpriteShading {
	
	ProjectedLight3D projectedLight3D;
	
	SceneData3D sceneData3D;
	
	public Scene3DSpriteShading(SceneData3D sd3d) {
		
		sceneData3D = sd3d;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// basePointImageBrightness is the basic shading of the whole sprite using key luminance image
	//
	public void basePointImageBrightness(Sprite sprite, String imageName, float dark, float light) {
		sceneData3D.setCurrentRenderImage(imageName);
		float lightVal = sceneData3D.getCurrentRender01Value(sprite.getDocPoint());
		float brightness = MOMaths.lerp(lightVal, dark, light);
		BufferedImage litImage = ImageProcessing.adjustBrightness(sprite.getImage(), brightness);
		sprite.setImage(litImage);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// sprite shading using a projectedTexture of a scenebased render
	//
	public void initialiseProjectedTexture(PVector planeNormal, BufferedImage sceneRenderImage) {
        // a normal of (1,0,1) is a good start
		Plane3D lightPlane = new Plane3D(planeNormal, new PVector(0,0,0));
		projectedLight3D = new ProjectedLight3D(sceneData3D,  lightPlane);
		projectedLight3D.buildMapFromSceneImage(sceneRenderImage);	
	}
	
	public void testProjectedTexture(RenderTarget rt) {
		ImageCoordinateSystem coordSystem = GlobalSettings.getTheDocumentCoordSystem();
			float ystep = coordSystem.getDocumentHeight()/200;
			float xstep = coordSystem.getDocumentWidth()/200;
			for(float y = 0; y < coordSystem.getDocumentHeight(); y+=ystep) {
				
				for(float x = 0; x < coordSystem.getDocumentWidth(); x+=xstep) {
					
					PVector docPt = new PVector(x,y);
					float radius = Math.max(xstep/2,ystep/2);
					if(sceneData3D.isSubstance(docPt)==false) {
						rt.drawCircle(docPt, radius, Color.blue, Color.blue, 0.001f);
						continue;
					}
					float depth = sceneData3D.getDepthNormalised(docPt);
					
					float lightval = projectedLight3D.getValue01(docPt, depth);
					//System.out.println("l val = " + lightval);
					Color fillColor = new Color(lightval,lightval,lightval);
					
					rt.drawCircle(docPt, radius, fillColor, fillColor, 0.001f);
					
				}
				
			
		}
		
		
	}
	
	public void shadeSpriteProjectedTexture(Sprite sprite, float dark, float light, int maskResolution) {
		BufferedImage lightMask = projectedLight3D.makeLightMask(sprite, maskResolution);
		BufferedImage partOfImageToLight = ImageProcessing.extractImageUsingGrayscaleMask(sprite.getImage(), lightMask);
		
		BufferedImage brightenedImage = ImageProcessing.adjustBrightness(partOfImageToLight, light);
		
		BufferedImage darkenedImage = ImageProcessing.adjustBrightness(sprite.getImage(), dark);
		sprite.setImage(darkenedImage);
		sprite.mergeMaskedImage(brightenedImage);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// sprite shading using a projectedTexture of a scenebased render
	//
	
}
