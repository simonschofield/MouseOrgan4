package MOScene3D;
import java.awt.image.BufferedImage;

import MOImage.ImageProcessing;
import MOMaths.PVector;
import MOUtils.GlobalSettings;
// solid texture 3D uses an image to project a texture into 3D
// The image is regarded as being in the plane x,z
// The image is sampled by asking for the value at a docPoint within the image at a certain depth
// TBD tilting the image - this could be done by having a "light" set at a distance
// For any sample point (docx,docy, depth) get the 3D point required and draw a ray back to the light
// through the mask
public class SolidTexture3D {
	
	BufferedImage textureImage;
	
	
	SolidTexture3D(BufferedImage textureImg){
		textureImage = textureImg;
		
	}
	
	
	public float getValue01(PVector docPt, float normalizedDepth) {
		
		PVector normalizedPt = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(docPt);
		int bx = (int)( normalizedPt.x * textureImage.getWidth());
		int by = (int)( (1-normalizedDepth) * textureImage.getHeight());
		
		return ImageProcessing.getValue01Clamped(textureImage, bx, by);
		
	}
	
	
	
	
}
