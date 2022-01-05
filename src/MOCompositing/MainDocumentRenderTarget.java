package MOCompositing;

import MOUtils.ImageCoordinateSystem;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//All the methods that are required for a render target to be part of the main document.
//
//

public interface MainDocumentRenderTarget {
	
	public void setName(String name);
	
	public String getName();
	
	public void clearImage();
	
	public void pasteSprite(ImageSprite sprite);
	
	public void saveRenderToFile(String pathAndFilename);

	public ImageCoordinateSystem getCoordinateSystem();

}
