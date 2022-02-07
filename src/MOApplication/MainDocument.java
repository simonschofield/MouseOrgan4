package MOApplication;



import java.io.File;
import java.util.ArrayList;

import MOCompositing.ImageSprite;
import MOCompositing.MainDocumentRenderTarget;
import MOCompositing.RenderBoarder;
import MOCompositing.RenderTarget;


import MOUtils.ImageCoordinateSystem;
import MOUtils.GlobalSettings;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// The idea behind the MainDocument class is that it provides an organised way to create and manage output images.
// hopefully without constraining or second-guessing the use of the output images.
// The class presumes that there is a "main" render output document. Typically this is the in the form BufferedImage.TYPE_INT_ARGB, but could also be
// in the form of BufferedImage.TYPE_BYTE_GRAY. However, TYPE_BYTE_GRAY does not have an alpha channel so renders are against a "solid" background colour so are not suitable for layering in post-process operations.
//
// In terms of management, the MainDocument has no use of sessionScale, but does fix the width and height of the various contained images to be the same, after instantiation. This means that all image-buffers contained can use
// the same ImageCoordinateSystem.
// 
// 
// The type of image-buffer class contained is defined by the interface MainDocumentRenderTarget. This allows the addition of bespoke image-classes, such as floating point (for depth information) and integer for 
// item-id that can all handle the pasting of ImageSprites in various ways to create images and mask-type images.
//


public class MainDocument{
	//public SessionSettings sessionSettings;
	
	
	ArrayList<MainDocumentRenderTarget> renderTargets = new ArrayList<MainDocumentRenderTarget>();
	public RenderBoarder renderBoarder;
	int width, height;
	
	public MainDocument(int wdth, int hght, int mainRenderType) {
		
		width = wdth;
		height = hght;
		addRenderTarget("main", mainRenderType);
		GlobalSettings.setTheDocumentCoordSystem(this);
		renderBoarder = new RenderBoarder();
		
	}
	
	
	
	public ImageCoordinateSystem getCoordinateSystem() {
		return renderTargets.get(0).getCoordinateSystem();
		
	}
	
	public void addRenderTarget(String name, int type) {
			RenderTarget rt = new RenderTarget(width, height,type);
			rt.setName(name);
			renderTargets.add(rt);
	}
	
	public RenderTarget getMain() {
		// because this one is the most used.. it has a special access method
		return (RenderTarget) renderTargets.get(0);
	}
	
	public int getNumRenderTargets() {
		return renderTargets.size();
	}
	
	public RenderTarget getRenderTarget(int n) {
		if( n >= getNumRenderTargets() || n < 0) {
			System.out.println("MainRenderDocument::getRenderTarget - illegal index " + n);
			return null;
		}
		return (RenderTarget) renderTargets.get(n);
		
	}
	
	public RenderTarget getRenderTarget(String name) {
		for(MainDocumentRenderTarget rt: renderTargets) {
			if( rt.getName().equals(name)) return (RenderTarget) rt;
		}
		System.out.println("MainRenderDocument::getRenderTarget - cannot find render target called " + name);
		return null;
	}
	
	
	
	
	
	public void saveRenderToFile(String renderTargetName, String pathAndFilename) {
		RenderTarget rt = getRenderTarget(renderTargetName);
		rt.saveRenderToFile(pathAndFilename);
	}
	

	public void setRenderBoarder(RenderBoarder rb) {
		renderBoarder = rb;
	}
	
	public boolean cropSpriteToBoarder(ImageSprite sprite) {
		return renderBoarder.cropSprite(sprite);
	}
	
	public void pasteSprite(String renderTargetName, ImageSprite sprite) {
		RenderTarget rt = getRenderTarget(renderTargetName);
		rt.pasteSprite(sprite);
	}
	
	
}
