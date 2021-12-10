package MOImage;
import java.awt.image.BufferedImage;

import MOMaths.PVector;
import MOUtils.MOUtilGlobals;

public class SolidTexture3D {
	
	BufferedImage textureImage;
	
	
	SolidTexture3D(BufferedImage textureImg){
		textureImage = textureImg;
		
	}
	
	
	float getValue01(PVector docPt, float normalizedDepth) {
		
		PVector normalizedPt = MOUtilGlobals.theDocumentCoordSystem.docSpaceToNormalisedSpace(docPt);
		int bx = (int)( normalizedPt.x * textureImage.getWidth());
		int by = (int)( (1-normalizedDepth) * textureImage.getHeight());
		
		return ImageProcessing.getValue01Clamped(textureImage, bx, by);
		
	}
	
	
	
	
}
