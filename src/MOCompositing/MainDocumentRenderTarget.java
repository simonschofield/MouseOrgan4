package MOCompositing;

import MOSprite.Sprite;
import MOUtils.ImageCoordinateSystem;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//All the methods that are required for a render target to be part of the main document.
//
//

public interface MainDocumentRenderTarget {
	
	public void setName(String name);
	
	public String getName();
	
	public String getFullSessionName();
	
	public String getFileExtension();
	
	public void clearImage();
	
	public void pasteSprite(Sprite sprite);
	
	public void saveRenderToFile(String pathAndFilename);

	public ImageCoordinateSystem getCoordinateSystem();

}
