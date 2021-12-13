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
	
	public PermittedPasteArea permittedPasteArea;
	
	public MainDocumentRenderTarget() {

	}

	public void setRenderBufferSize(int w, int h) { 

		super.setRenderBuffer(w, h, BufferedImage.TYPE_INT_ARGB);

		permittedPasteArea = new PermittedPasteArea(this);
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
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// 
	// All arguments are in Normalised space, as working in DocSpace for humans is difficult.
	//
	
	// old legacy way of setting margins
	void setPermittedPasteArea(float left, float top, float right, float bottom, boolean applyCrop, ImageSampleGroup cropImages) {
		// NO_CROP : Don't do anything, just allow the image to be pasted, also default action
		// EXCLUDE_OVERLAPPING : do not paste any sprite overlapping an edge with this setting
		// CROP : Crop any sprite to the hard edge
		// BESPOKE_CROP : Apply a bespoke crop to a sprite overlapping this edge
		String edgeCropAction = "EXCLUDE";
		if(applyCrop && cropImages==null) {
			edgeCropAction = "CROP";
		}
		if(applyCrop && cropImages!=null) {
			edgeCropAction = "BESPOKE_CROP";
		}
		permittedPasteArea.set(left, top, right, bottom, edgeCropAction, cropImages);
	}
	
	
	public void setPermittedPasteArea(float left, float top, float right, float bottom, String leftAct, String topAct, String rightAct, String bottomAct, ImageSampleGroup cropImages) {
		// NO_CROP : Don't do anything, just allow the image to be pasted, also default action
		// TBD: a crop that allows the whole to be pasted, but only if th start point is inside the PPA
		// EXCLUDE_OVERLAPPING : do not paste any sprite overlapping an edge with this setting
		// CROP : Crop any sprite to the hard edge
		// BESPOKE_CROP : Apply a bespoke crop to a sprite overlapping this edge
		
		permittedPasteArea.set(left, top, right, bottom, leftAct,  topAct,  rightAct,  bottomAct, cropImages);
	}
	
	void setPermittedPasteArea(boolean active) {
		
		permittedPasteArea.setActive(active);
	}
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	// Called by the  user to paste a sprite, and any mask images to the main document
	// The sprite has been scales rotated and translated correctly by this point, but 
	// still has cropping against the permittedPasteArea to do
	// pastes the topleft of the image at docSpacePoint
	//
	public void pasteSprite(ImageSprite sprite, float alpha) {
		// work out the offset in the image from the origin
		
		Rect r = sprite.getPasteRectDocSpace(this); 
		
		//System.out.println("pasteSprite pasteRectDocSpace = " + r.toStr());
		
		
		String overlapReport =  permittedPasteArea.reportPermittedPasteAreaOverlap(sprite);
		//System.out.println("overlap report " + overlapReport);
		
		if( overlapReport.contentEquals("WHOLLYINSIDE") || permittedPasteArea.isActive()==false){
			//System.out.println("here 1");
			pasteCroppedSpriteImageAndMasks(sprite, r.getTopLeft(),  alpha);
			return;
		}
		
		//System.out.println("here 2");
		// otherwise do some sort of crop
		// decide what to do
		// the options are
		// 1/ if EXCLUDE_OVERLAPPING don't allow any paste, so return
		// 2/ crop to the permitted area with bespoke crop
		// 3/ a hard geometric crop to the area
		//if( bespokeCropToPermittedPasteArea == false ) return;
		
		
		String cropDecision = permittedPasteArea.cropEdgeDecision(overlapReport);
		//System.out.println("cropDecision " + cropDecision);
		if( cropDecision.contentEquals("EXCLUDE") ) return;
		
		if( cropDecision.contentEquals("NO_CROP") ) {
			pasteCroppedSpriteImageAndMasks(sprite, r.getTopLeft(),  alpha);
			return;
		}
		
		
		//if( permittedPasteAreaClass.leftEdgeAction.contentEquals("EXCLUDE") ) return;
		// if you get this far, 
		boolean cropOK = sprite.cropToPermittedPasteArea(this);
		if(cropOK==false) return;
		
		
		pasteCroppedSpriteImageAndMasks(sprite, r.getTopLeft(),  alpha);
	}
	

	////////////////////////////////////////////////////////////////////////////////////
	// Finally pastes the image to the render target after all the cropping has been 
	// worked out.
	// and works out if it needs to be pasted to a mask as well.
	private void pasteCroppedSpriteImageAndMasks(ImageSprite sprite, PVector docSpacePoint, float alpha) {

		// this pastes the image to the main document render target image
		super.pasteImage(sprite.image, docSpacePoint, alpha);

		if(maskImage != null) {
			maskImage.pasteToMask(sprite, docSpacePoint, alpha);
		}

	}

}


class MainDocumentMaskImage extends RenderTarget{
	
	// this is the name of the seedBatch or imageSampleGroup items to create a mask for
	String itemToMaskIdentifier = "";
	
	MainDocumentMaskImage(String itemIdentifierToMask) {
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
			spriteMaskImage = ImageProcessing.replaceColor(sprite.image, Color.WHITE);
		} else {
			//convert to a black image
			spriteMaskImage = ImageProcessing.replaceColor(sprite.image, Color.BLACK);
			
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

