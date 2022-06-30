package MOImageCollections;


import java.awt.Color;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOMaths.PVector;
import MOMaths.Rect;
import MOUtils.GlobalSettings;




//////////////////////////////////////////////////////////////////////////////////////////////
//
// SpriteImageGroupManager contains many SpriteImageGroups, and provides the main user-interface to them
// via the SpriteImageGroup's name. 
//
//
public class ScaledMOImageGroupManager {

	ArrayList<ScaledMOImageGroup> theMOImageGroupList = new ArrayList<ScaledMOImageGroup>();
	//Surface parentSurface;

	public ScaledMOImageGroupManager() {
		GlobalSettings.setImageGroupManager(this);
	}

	//////////////////////////////////////////////////////////////////////////////
	//
	//
	public ScaledMOImageGroup getMOImageGroup(String name) {
		for (ScaledMOImageGroup cc : theMOImageGroupList) {
			if (cc.isNamed(name))
				return cc;
		}
		return null;
	}
	
	
	
	

	int getNumItems(String name) {
		ScaledMOImageGroup cc = getMOImageGroup(name);
		if (cc == null)
			return 0;
		return cc.getNumMOImages();
	}

	
	
	

	/**
	 * @param contentGroupName,    this is the name you give the content group for
	 *                             further access via the manager
	 * @param targetDirectory,     this is the directory containing the files you
	 *                             wish to load
	 * @param fileNameMustEndWith, this is a file name filter, set to ".png" to load
	 *                             all png file. Set to "" or "*" if you dont want
	 *                             to filer on endings
	 * @param fileNameMustContain, this is a file name filter, set to "apple" to
	 *                             load all files containing the string"apple". Set
	 *                             to "" or "*" if you don't want any filter
	 * @param from,                load all files FROM this item number within the
	 *                             target directory that meet the filter criteria
	 *                             (can be nulled, in which case loads from first
	 *                             item found)
	 * @param to,                  load all files TO this item number within the
	 *                             target directory that meet the filter criteria
	 *                             (can be nulled, in which case loads TO the final
	 *                             item found)
	 * @param preScale,            apply a uniform scaling to all items. This is
	 *                             supplementary to the session scale.
	 * @param cropRect,            apply a uniform crop to all items. The rect is in
	 *                             normalised coords
	                     
	 */
	void loadImageGroup(String spriteImageGroupName, String targetDirectory, String fileNameMustEndWith,
			String fileNameMustContain, Integer from, Integer to, float preScale, Rect cropRect) {

		
		DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(targetDirectory);
		dfns.setFileType(fileNameMustEndWith);
    	dfns.setFileNameContains(fileNameMustContain);
    	dfns.setFileListRange(from, to);
    	
    	ScaledMOImageGroup newSpriteImageGroup  = new ScaledMOImageGroup(spriteImageGroupName);
		
		newSpriteImageGroup.setDirectoryFileNameScanner(dfns);
		newSpriteImageGroup.setPreScale(preScale);
		newSpriteImageGroup.setCrop(cropRect);
		
		newSpriteImageGroup.loadSessionScaledImages();
		theMOImageGroupList.add(newSpriteImageGroup);

	}

	/////////////////////////////////////////////////////////////////////////////
	// this is the short-hand method of the above
	public void loadImageGroup(String name, String targetDirectory, Integer from, Integer to) {

		loadImageGroup(name, targetDirectory, ".png", "", from, to, 1, new Rect());

	}
	
	/////////////////////////////////////////////////////////////////////////////
	// creates a completely independent copy of an already loaded sample group, 
	// the new group has a different name,so users can rescale or colour treat this group separately
	///////////////////////////////////////////////////////////////////////////// first
    public void cloneImageGroup(String existingGroupname, String newGroupName) {
    	ScaledMOImageGroup cc = getMOImageGroup(existingGroupname);
    	if(cc==null) {

    	}
    	ScaledMOImageGroup newGroup = cc.copy(newGroupName);

    	System.out.println("Cloned " + existingGroupname + " with " + cc.getNumMOImages() + " into " + newGroupName + " with " + newGroup.getNumMOImages());
    	theMOImageGroupList.add(newGroup);
    }
    
    
	/////////////////////////////////////////////////////////////////////////////
	// these are the long-hand method of establishing an image-collection
	// If you are doing it long hand - then the method below needs to be called
	///////////////////////////////////////////////////////////////////////////// first
    /*
    ScaledMOImageGroup addImageGroupNameAndPath(String name, String targetDirectory, String filesStrEndWith, String fileStrContains) {
    	DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(targetDirectory);
    	dfns.setFileNameContains(fileStrContains);
    	
    	
    	ScaledMOImageGroup newSpriteImageGroup  = new ScaledMOImageGroup(name);
		
		newSpriteImageGroup.setDirectoryFileNameScanner(dfns);
		newSpriteImageGroup.loadSessionScaledImages();
		
		theMOImageGroupList.add(newSpriteImageGroup);
		return newSpriteImageGroup;
	}
	*/

	
     public void addImageGroup(ScaledMOImageGroup sig) {
    	 // TBD check unique name
    	 theMOImageGroupList.add(sig);
     }
	

	/////////////////////////////////////////////////////////////////////////////
	// Image Processing Effects applied to the entire content group
	//
	///////////////////////////////////////////////////////////////////////////// first

	public void scaleImageGroup(String name, float inx, float iny) {
		ScaledMOImageGroup ic = getMOImageGroup(name);
		if (ic == null)
			return;
		ic.scaleAll(inx, iny);
	}

	/**
	 * Generalised color processing method applied to the entire contents of an
	 * ImageContentGroup
	 * 
	 * @param groupname the ImageContentGroup to adjust
	 * @param function  the color function string either "hsv", "brightnessNoClip",
	 *                  "brightness", "contrast", "levels"
	 * @param p1        first parameter if needed
	 * @param p2        second parameter if needed
	 * @param p3        third parameter if needed
	 * @brief adjusts the color of a whole ImageContentGroup
	 */
	public void colorTransformImageGroup(String groupname, int function, float p1, float p2, float p3) {
		ScaledMOImageGroup ic = getMOImageGroup(groupname);
		if (ic == null)
			return;
		ic.colorTransformAll(function, p1, p2, p3);
	}

	

	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	public void paradeContent(String groupName, RenderTarget rt) {
		paradeContent(groupName,ImageProcessing.COLORTRANSFORM_NONE, 0, 0, 0, rt);
	}

	void paradeContent(String groupName, int effect, float p1, float p2, float p3, RenderTarget rt) {
		ScaledMOImageGroup sampleGroup = this.getMOImageGroup(groupName).copy(groupName + "copy");
		
		int numItems = sampleGroup.getNumMOImages();
		// get them in portrait mode
		for(int n = 0; n < numItems; n++) {
			BufferedImage img = sampleGroup.getImage(n);
			String exisitingName = sampleGroup.getImageName(n);
			int w = img.getWidth();
			int h = img.getHeight();
			if(w > h) {
				
				img = ImageProcessing.rotate90(img, 1);
				String newName = exisitingName + "_rot90";
				sampleGroup.replaceImage(img, newName, n);
			}
			
		}
		
		
		//Surface parentSurface = GlobalObjects.theSurface;

		
		int biggestItemWidth = (int) sampleGroup.widthExtrema.getUpper();
		int biggestItemHeight = (int) sampleGroup.heightExtrema.getUpper();
		float itemAspect = biggestItemWidth / (float) biggestItemHeight;
		int outImageW = rt.coordinateSystem.getBufferWidth();
		int outImageH = rt.coordinateSystem.getBufferHeight();

		// assume they are all fit into shapes biggestWidth/Biggestheight - this will be
		// a good fit for most content of similar aspects
		//
		// assuming a portrait shaped item
		// first scale the height of the outImageH by the aspect of the shaped item.
		// Then you can deal with it as if they were all squares, which
		// is simpler to think about
		float outImageHScaled = outImageH * itemAspect;
		float outImageScaledAspect = outImageW / outImageHScaled;
		float idealNumRows = (float) Math.sqrt((double) (numItems / outImageScaledAspect));
		int numItemsInRow = (int) Math.ceil(idealNumRows * outImageScaledAspect);
		int numOfItemRows = numItems / numItemsInRow;
		int remainingInLastRow = numItems - (numOfItemRows * numItemsInRow);

		System.out.println("paradeContent : numItems = " + numItems + ", Num In Row " + numItemsInRow
				+ ", actualNumWholeRows " + numOfItemRows + ", with remaining " + remainingInLastRow);

		// so now we know the arrangement of the items in the larger image

		// we also need to scale the image to fit into the box
		int boxWidth = (int) (outImageW / (float) numItemsInRow);
		int boxHeight = (int) (outImageH / (float) (numOfItemRows + 1));
		int itemCounter = 0;
		for (int y = 0; y <= numOfItemRows; y++) {
			for (int x = 0; x < numItemsInRow; x++) {
				int thisItemX = x * boxWidth;
				int thisItemY = y * boxHeight + 20;
				if (itemCounter < numItems) {
					BufferedImage img = sampleGroup.getImage(itemCounter);

					img = ImageProcessing.colorTransform(img, effect, p1, p2, p3);

					String itemName = sampleGroup.getImageName(itemCounter);
					int imgW = img.getWidth();
					int imgH = img.getHeight();
					float imgAspect = imgW / (float) imgH;
					int scaledHeight = (int) (boxHeight * 0.75f);
					int scaledWidth = (int) (boxHeight * imgAspect * 0.75f);
					img = ImageProcessing.resizeTo(img, scaledWidth, scaledHeight);
					rt.pasteImage_BufferCoordinates(img, thisItemX, thisItemY, 1.0f);

					rt.drawText(itemName, thisItemX, thisItemY + scaledHeight + 50, 50,
							Color.DARK_GRAY);
				}
				itemCounter++;
			}
		}

		//String userSessionPath = GlobalSettings.getUserSessionPath();

		//String suggestedName = MOStringUtils.getDateStampedImageFileName("Parade_" + groupName + "_");
		//System.out.println("saveRenderLayer: saving " + suggestedName);
		//rt.saveRenderToFile(userSessionPath + suggestedName);

	}


}// end of class SpriteImageGroupManager











