package MOCompositing;

////////////////////////////////////////////////////////////////////////////////
// Render target class
// The documentROI is in real document space coordinates
// This is a Java Graphics2D implementation of the render target to
// hopefully open up all of Java's graphics functionality

import java.awt.*;
//import java.awt.event.*;
//import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

//import GlobalObjects;
import MOImage.ImageProcessing;
import MOImage.RenderTarget;
import MOImageCollections.ImageSampleGroup;
import MOMaths.PVector;
import MOMaths.Rect;
import MOUtils.MOStringUtils;
import MOUtils.MOUtilGlobals;



//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// This is a render target with sprite and permitted paste area functionality added
//
//
//


public class MainDocumentRenderTarget extends RenderTarget{
    
	MainDocumentMaskImage maskImage;
	
	public RenderBoarder renderBoarder;
	
	public MainDocumentRenderTarget() {

	}

	public void setRenderBufferSize(int w, int h) { 

		super.setRenderBuffer(w, h, BufferedImage.TYPE_INT_ARGB);
		MOUtilGlobals.setTheDocumentCoordSystem(super.getCoordinateSystem());
		renderBoarder = new RenderBoarder();
	}


	public void addMaskImage(String seedBatchName) {
		
		maskImage = new MainDocumentMaskImage(seedBatchName);
		
	}
	
	public void saveRenderToFile(String pathAndFilename) {
		System.out.println("MainDocumentRenderTarget:saveRenderToFile  " + pathAndFilename);
		super.saveRenderToFile(pathAndFilename);
		if(maskImage != null) {
			maskImage.saveRenderToFile(pathAndFilename);
		}
	}
	

	public void setRenderBoarder(RenderBoarder rb) {
		renderBoarder = rb;
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// Called by the  user to paste a sprite, and any mask images to the main document
	// The sprite has been scales rotated and translated correctly by this point, but 
	// still has cropping against the permittedPasteArea to do
	// pastes the topleft of the image at docSpacePoint
	//
	public void pasteSprite(ImageSprite sprite, float alpha) {
		boolean result = true;
		if(renderBoarder != null) {
			result = renderBoarder.cropSprite(sprite);
		}
		if(result) pasteCroppedSpriteImageAndMasks(sprite, sprite.getDocSpaceRect().getTopLeft(),  alpha);
	}
	

	////////////////////////////////////////////////////////////////////////////////////
	// Finally pastes the image to the render target after all the cropping has been 
	// worked out.
	// and works out if it needs to be pasted to a mask as well.
	private void pasteCroppedSpriteImageAndMasks(ImageSprite sprite, PVector docSpacePoint, float alpha) {

		// this pastes the image to the main document render target image
		super.pasteImage(sprite.getImage(), docSpacePoint, alpha);

		if(maskImage != null) {
			maskImage.pasteToMask(sprite, docSpacePoint, alpha);
		}

	}

}




