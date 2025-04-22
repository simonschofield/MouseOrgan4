package MOApplication;



import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;


import MOCompositing.RenderTargetInterface;
import MOCompositing.RenderBorder;
import MOCompositing.BufferedImageRenderTarget;
import MOCompositing.FloatImageRenderTarget;
import MOSprite.Sprite;
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
	
	
	ArrayList<RenderTargetInterface> renderTargets = new ArrayList<RenderTargetInterface>();
	public RenderBorder renderBoarder;
	int width, height;
	
	public MainDocument(int wdth, int hght, int mainRenderType) {
		
		width = wdth;
		height = hght;
		addRenderTarget("main", mainRenderType);
		GlobalSettings.setTheDocumentCoordSystem(this);
		renderBoarder = new RenderBorder();
		
	}
	
	
	
	public ImageCoordinateSystem getCoordinateSystem() {
		return renderTargets.get(0).getCoordinateSystem();
		
	}
	
	public void addRenderTarget(String name, int type) {
			BufferedImageRenderTarget rt = new BufferedImageRenderTarget(width, height,type);
			rt.setName(name);
			renderTargets.add(rt);
	}
	
	public void addFloatRenderTarget(String name, boolean saveTYPE_USHORT_GRAYcopy, float imageCopyGamma) {
		if(imageCopyGamma == 0 ) imageCopyGamma = 1;
		FloatImageRenderTarget rt = new FloatImageRenderTarget(width, height,  saveTYPE_USHORT_GRAYcopy,  imageCopyGamma);// deferred
		rt.setName(name);
		renderTargets.add(rt);
}
	
	public BufferedImageRenderTarget getMain() {
		// because this one is the most used.. it has a special short-hand access method
		return (BufferedImageRenderTarget) renderTargets.get(0);
	}
	
	public int getNumRenderTargets() {
		return renderTargets.size();
	}
	
	public RenderTargetInterface getRenderTarget(int n) {
		if( n >= getNumRenderTargets() || n < 0) {
			System.out.println("MainRenderDocument::getRenderTarget - illegal index " + n);
			return null;
		}
		return (RenderTargetInterface) renderTargets.get(n);
		
	}
	
	
	public BufferedImageRenderTarget getBufferedImageRenderTarget(String name) {
		
		RenderTargetInterface rt =  getRenderTarget( name);
		if(rt.getFileExtension().equals(".png")) return (BufferedImageRenderTarget) rt;
		System.out.println("MainRenderDocument::getBufferedImageRenderTarget - cannot find buffered image render target called " + name);
		return null;
	}
	
	public FloatImageRenderTarget getFloatImageRenderTarget(String name) {
		
		RenderTargetInterface rt =  getRenderTarget( name);
		if(rt.getFileExtension().equals(".data")) return (FloatImageRenderTarget) rt;
		System.out.println("MainRenderDocument::getBufferedImageRenderTarget - cannot find buffered image render target called " + name);
		return null;
	}
	
	
	public RenderTargetInterface getRenderTarget(String name) {
		
		for(RenderTargetInterface rt: renderTargets) {
			if( rt.getName().equals(name)) return (RenderTargetInterface) rt;
		}
		System.out.println("MainRenderDocument::getRenderTarget - cannot find render target called " + name);
		return null;
	}
	
	
	
	
	
	public void saveRenderTargetToFile(String renderTargetName, String pathAndFilename) {
		RenderTargetInterface rt =  getRenderTarget(renderTargetName);
		rt.saveRenderToFile(pathAndFilename);
	}
	

	public void setRenderBorder(RenderBorder rb) {
		renderBoarder = rb;
	}
	
	public RenderBorder getRenderBorder() {
		return renderBoarder;
	}
	
	public boolean cropSpriteToBoarder(Sprite sprite) {
		return renderBoarder.cropSprite(sprite);
	}
	
	
	
	public void pasteSprite(String renderTargetName, Sprite sprite) {
		RenderTargetInterface rt = getRenderTarget(renderTargetName);
		rt.pasteSprite(sprite);
	}
	
	
}
