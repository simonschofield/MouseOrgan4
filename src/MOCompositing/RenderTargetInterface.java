package MOCompositing;

import java.awt.image.BufferedImage;

import MOMaths.Rect;
import MOSprite.Sprite;
import MOUtils.ImageCoordinateSystem;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//All the methods that are required for a render target to be part of the main document.
//
//

public interface RenderTargetInterface {
	
	public void setName(String name);
	
	public String getName();
	
	public String getFullSessionName();
	
	public String getFileExtension();
	
	public void clearImage();
	
	public void pasteSprite(Sprite sprite);
	
	public void pasteSprite(Sprite sprite, String imageName);
	
	public void saveRenderToFile(String pathAndFilename);

	public ImageCoordinateSystem getCoordinateSystem();
	
	public void setCoordinateSystem(ImageCoordinateSystem ics);
	
	public int getImageType();
	
	public BufferedImage getBufferedImage();
	
	//public int getPixel(float x, float y);
	
	//public void setPixel()

}
