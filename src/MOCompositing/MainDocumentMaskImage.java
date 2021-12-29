package MOCompositing;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOImage.ImageProcessing;
import MOImage.RenderTarget;
import MOMaths.PVector;
import MOUtils.MOStringUtils;
import MOUtils.MOUtilGlobals;

public class MainDocumentMaskImage extends RenderTarget{
	
	// this is the name of the seedBatch or imageSampleGroup items to create a mask for
	String itemToMaskIdentifier = "";
	
	public MainDocumentMaskImage(String itemIdentifierToMask) {
		int w = MOUtilGlobals.getTheDocumentCoordSystem().getBufferWidth();
		int h = MOUtilGlobals.getTheDocumentCoordSystem().getBufferHeight();
		setRenderBuffer(w,h,BufferedImage.TYPE_INT_ARGB);
		this.fillBackground(Color.BLACK);;
		this.itemToMaskIdentifier = itemIdentifierToMask;
	}
	
	
	void pasteToMask(ImageSprite sprite, PVector docSpacePoint, float alpha) {

		BufferedImage spriteMaskImage; 
		if(sprite.seedBatchEquals(itemToMaskIdentifier) || sprite.imageSampleGroupEquals(itemToMaskIdentifier)) {
			//convert to a white image
			spriteMaskImage = ImageProcessing.replaceColor(sprite.getImage(), Color.WHITE);
		} else {
			//convert to a black image
			spriteMaskImage = ImageProcessing.replaceColor(sprite.getImage(), Color.BLACK);
			
		}
		// this pastes the image to the main document render target image
		super.pasteImage(spriteMaskImage, docSpacePoint, alpha);
		//System.out.println("pasteImage in subclass");

		

	}
	
	public void saveRenderToFile(String pathAndFilename) {

		// add in the special mask name
		String maskName = "_" + itemToMaskIdentifier + "_Mask.png";
		String pathAndFileNameNoExt = MOStringUtils.getFileNameWithoutExtension(pathAndFilename);
		System.out.println("MainDocumentMaskImage:saveRenderToFile  " + pathAndFilename);
		super.saveRenderToFile(pathAndFileNameNoExt + maskName);
		

	}
	
	

}