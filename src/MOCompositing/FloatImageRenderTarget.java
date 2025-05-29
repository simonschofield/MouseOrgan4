package MOCompositing;


import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;


import MOImage.FloatImage;
import MOImage.ImageProcessing;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;
import MOUtils.MOStringUtils;

public class FloatImageRenderTarget implements RenderTargetInterface {
	// this is used if required in saving float data (e.g. actual depth values).
	private FloatImage floatImage;
	
	private float floatImageCopyGamma;

	public ImageCoordinateSystem coordinateSystem;

	private boolean floatImageMake16BitCopyOnSave = false;
	public boolean saveRenderAtEndOfSession = true;
	
	private String renderTargetName = "";
	
	public FloatImageRenderTarget(int w, int h, boolean saveTYPE_USHORT_GRAYcopy, float imageCopyGamma) {
		floatImage = new FloatImage(w, h);
		floatImageMake16BitCopyOnSave = saveTYPE_USHORT_GRAYcopy;
		floatImageCopyGamma = imageCopyGamma;
		coordinateSystem = new ImageCoordinateSystem(w, h);
	}
	
	
	@Override
	public void setCoordinateSystem(ImageCoordinateSystem ics) {
		// TODO Auto-generated method stub
		coordinateSystem = ics;
	}
	

	

	@Override
	public void setName(String name) {
		renderTargetName = name;

	}
	
	
	public String getName() {
		return renderTargetName;
	}
	
	public int getType() {
		return 0;
	}

	public String getFullSessionName() {
		String sessname = GlobalSettings.mainSessionName + "_" + GlobalSettings.currentSchemea + "_" + renderTargetName;
		return sessname;
	}

	public String getFileExtension() {
		return ".data";
	}

	@Override
	public void clearImage() {
		
		floatImage.setAll(0f);
	}

	@Override
	public void pasteSprite(Sprite sprite) {
		// not sure what to do with this
		System.out.println("FloatImageRenderBuffer:: pasteSprite is not implemented");
	}

	@Override
	public void saveRenderToFile(String pathAndFilename) {
		if(saveRenderAtEndOfSession == false) {
			System.out.println("RenderTarget:saveRenderToFile  " + pathAndFilename + " save set to false ");
		} else {
			System.out.println("RenderTarget:saveRenderToFile  " + pathAndFilename);
			saveFloatImage(pathAndFilename);
		}
		
			
		if( floatImageMake16BitCopyOnSave ) {
			
			BufferedImage TYPE_USHORT_GRAY_BufferedImage = copyFloatDataToBufferedImage(floatImageCopyGamma);
			
			String  fnameAndPathNoExt = MOStringUtils.getFileNameWithoutExtension(pathAndFilename);
			String bufferedImageFileName = fnameAndPathNoExt + ".png";
			
			System.out.println("RenderTarget:saveRenderToFile  " + bufferedImageFileName);
			ImageProcessing.saveImage(bufferedImageFileName, TYPE_USHORT_GRAY_BufferedImage);
		} 


	}
	
	private BufferedImage copyFloatDataToBufferedImage(float gamma) {
		
		int w = floatImage.getWidth();
		int h = floatImage.getHeight();
		BufferedImage  TYPE_USHORT_GRAY_BufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);

		float oldmaskValue = floatImage.getMaskValue();
		
		floatImage.setMaskValue(0, true);
		
		Range extrema = floatImage.getExtrema();
		float loF = extrema.getLower();
		float hiF = extrema.getUpper();
		
		WritableRaster targetImageData = TYPE_USHORT_GRAY_BufferedImage.getRaster();
		
		//System.out.println(" UShort Depth  = " + shortval);
		int shortval;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				
				float f = floatImage.get(x, y);
				
				if(gamma!=1f) {
					f = MOMaths.norm(f, loF, hiF);
					f = (float) Math.pow(f,gamma);
					shortval = (int) MOMaths.lerp(f, 0 , 65535);
				}else {
					shortval = (int) MOMaths.map(f, loF,hiF, 0 , 65535);
				}
				// the shortval is large for far distances and small for near distances,
				// so to make more visually intuitive, we invert the number, so that far => black, and near => white
				targetImageData.setSample(x, y, 0,65535-shortval);
				
			}
		}

		floatImage.setMaskValue(oldmaskValue, true);
		return TYPE_USHORT_GRAY_BufferedImage;
	}
	
	
	private void saveFloatImage(String pathAndFilename) {
		String  fnameAndPathNoExt = MOStringUtils.getFileNameWithoutExtension(pathAndFilename);
		String fnameAndPath = fnameAndPathNoExt + ".data";
		
		floatImage.saveFloatData(fnameAndPath);
	}

	@Override
	public ImageCoordinateSystem getCoordinateSystem() {
		// TODO Auto-generated method stub
		return coordinateSystem;
	}
	
	// getting and setting in BufferSpace
	public void setPixel(int x, int y, float val) {
		floatImage.set(x, y, val);
		
	}
	
	public float getPixel(int x, int y) {
		return floatImage.get(x, y);
		
	}
	
	
	// getting pixel in doc space, using bilinear
	public float getPixel(PVector p) {
		PVector bufferSpacePt = coordinateSystem.docSpaceToBufferSpace(p);
		return floatImage.getPixelBilin(bufferSpacePt.x, bufferSpacePt.y);
		
	}
	
	public FloatImage getFloatImage() {
		
		return floatImage;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// The idea here is to preserve actual floating point values, and the only option for that is to have a 
	// separate FloatImage store and save the data. However, this cannot be saved as a viewable image.
	// To save as viewable image the nearest thing is a 16bit gray image TYPE_USHORT_GRAY. 
	// So to use this method 
	// you should initialise it as a TYPE_USHORT_GRAY. The TYPE_USHORT_GRAY is built from the float image, using the float images' extrema,
	// at save time.
	//
	//
	public void pasteFloatValues(Sprite sprite, float val) {

		if (floatImage == null) {
			System.out.println(
					"Rendertarget::pasteFloatValues - the float image has not been initilaised. Use mainRenderTarget.addFloatRenderTarget(...)");
			System.exit(0);
		}

		int targetWidth = floatImage.getWidth();
		int targetHeight = floatImage.getHeight();

		PVector topLeftDocSpace = sprite.getDocSpaceRect().getTopLeft();
		PVector bufferPt = coordinateSystem.docSpaceToBufferSpace(topLeftDocSpace);

		int sourceWidth = sprite.getImageWidth();
		int sourceHeight = sprite.getImageHeight();
		int targetOffsetX = (int) bufferPt.x;
		int targetOffsetY = (int) bufferPt.y;
		WritableRaster sourceImageAlphaData = sprite.getMainImage().getAlphaRaster();

		for (int y = 0; y < sourceHeight; y++) {
			for (int x = 0; x < sourceWidth; x++) {
				int targetX = x + targetOffsetX;
				int targetY = y + targetOffsetY;
				if (targetX < 0 || targetX >= targetWidth || targetY < 0 || targetY >= targetHeight)
					continue;

				int sourceImageAphaValue = sourceImageAlphaData.getSample(x, y, 0);
				if (sourceImageAphaValue > 8)
					floatImage.set(targetX, targetY, val);
			}
		}

	}

	@Override
	public void pasteSprite(Sprite sprite, String imageName) {
		// TODO Auto-generated method stub
		
	}

	

}
