package MOApplication;



import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;


import MOCompositing.RenderTargetInterface;
import MOImage.ImageDimensions;
import MOMaths.Rect;
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
// The type of image-buffer class contained is defined by the interface RenderTargetInterface. This allows the addition of bespoke image-classes, such as floating point (for depth information) and integer for 
// item-id that can all handle the pasting of ImageSprites in various ways to create images and mask-type images.
//
// These are the Java Image Types used in MouseOrgan
//	public static final int TYPE_CUSTOM = 0; -- in this implementation we use this to indicate FLOAT image, which is NOT a buffered image type, but wrapped in a RenderTargetInterface
//	public static final int TYPE_INT_ARGB = 2; -- A buffered image type, Used for colour + alpha images in, may also be used to provide INT images if required
//	public static final int TYPE_BYTE_GRAY = 10; -- A buffered image type,used for 8 bit grey scale images
//	public static final int TYPE_USHORT_GRAY = 11; - A buffered image type, used for 16 bit greyscale images



public class MainDocument{
	//public SessionSettings sessionSettings;
	
	
	ArrayList<RenderTargetInterface> renderTargets = new ArrayList<RenderTargetInterface>();
	public RenderBorder renderBorder;
	int width, height;
	
	ImageCoordinateSystem documentImageCordinateSystem;
	
	
	public MainDocument(int fullScaleWidth, int fullScaleHeight, int mainRenderType) {
		// simple non-roi based document
		
		// session scaling of the document image happens here....
		float scl = GlobalSettings.getSessionScale();
		width = (int)Math.round(fullScaleWidth * scl);
		height = (int)Math.round(fullScaleHeight * scl);
		
		
		// The documentImageCordinateSystem is now used for all Document Render Targets (a reference is shared between them)
		documentImageCordinateSystem = new ImageCoordinateSystem(width,height);
		GlobalSettings.setTheDocumentCoordSystem(this);
		renderBorder = new RenderBorder();
		
		
		addRenderTarget("main", mainRenderType);
	}
	
	public MainDocument(ROIManager roiManager, int mainRenderType) {
		// for a roi based document
		
		// session scaling of the document image happens here....
		// The documentImageCordinateSystem is now used for all Document Render Targets (a reference is shared between them)
		documentImageCordinateSystem = roiManager.getCurrentROIImageCoordinateSystem_SessionScaled().copy();
		width = documentImageCordinateSystem.getBufferWidth();
		height = documentImageCordinateSystem.getBufferHeight();

		GlobalSettings.setTheDocumentCoordSystem(this);
		renderBorder = new RenderBorder();
		
		System.out.println(">>" + documentImageCordinateSystem.toStr());
		addRenderTarget("main", mainRenderType);
	}
	
	
	// they should all have the same coordinate system within a document
	public ImageCoordinateSystem getCoordinateSystem() {
		return documentImageCordinateSystem;
	}
	
	public void addRenderTarget(String name, int type) {
			BufferedImageRenderTarget rt = new BufferedImageRenderTarget(width, height,type);
			rt.setCoordinateSystem(documentImageCordinateSystem);
			rt.setName(name);
			renderTargets.add(rt);
	}
	
	public void addFloatRenderTarget(String name, boolean saveTYPE_USHORT_GRAYcopy, float imageCopyGamma) {
		if(imageCopyGamma == 0 ) imageCopyGamma = 1;

		FloatImageRenderTarget rt = new FloatImageRenderTarget(width, height,  saveTYPE_USHORT_GRAYcopy,  imageCopyGamma);// deferred
		rt.setCoordinateSystem(documentImageCordinateSystem);

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
	
	public boolean renderTargetExists(String name) {
		for(RenderTargetInterface rt: renderTargets) {
			if( rt.getName().equals(name)) return true;
		}
		return false;
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
		renderBorder = rb;
	}
	
	public RenderBorder getRenderBorder() {
		return renderBorder;
	}
	
	public boolean cropSpriteToBorder(Sprite sprite) {
		return renderBorder.cropSprite(sprite);
	}
	
	
	
	public void pasteSprite(String renderTargetName, Sprite sprite) {
		RenderTargetInterface rt = getRenderTarget(renderTargetName);
		rt.pasteSprite(sprite);
	}
	
	
}
