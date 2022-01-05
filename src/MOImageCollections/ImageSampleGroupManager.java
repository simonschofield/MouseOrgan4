package MOImageCollections;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import MOCompositing.ImageSprite;
//import MOCompositing.MainDocumentRenderTarget;
import MOCompositing.RenderTarget;
import MOImage.ImageProcessing;
import MOMaths.PVector;
import MOMaths.Rect;
import MOSceneData.Seed;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;



//////////////////////////////////////////////////////////////////////////////////////////////
//
// ContentGroupManager contains many ContentItemGroup, and provides the main user-interface to them
// via the ContentCollection's name. This same name is used to reference iterms within that collection
// in other pars of the system
//
public class ImageSampleGroupManager {

	ArrayList<ImageSampleGroup> imageSampleGroups = new ArrayList<ImageSampleGroup>();
	//Surface parentSurface;

	public ImageSampleGroupManager() {
		//parentSurface = GlobalObjects.theSurface;
	}

	//////////////////////////////////////////////////////////////////////////////
	//
	//
	public ImageSampleGroup getImageSampleGroup(String name) {
		for (ImageSampleGroup cc : imageSampleGroups) {
			if (cc.isNamed(name))
				return cc;
		}
		return null;
	}
	
	
	
	

	int getNumItems(String name) {
		ImageSampleGroup cc = getImageSampleGroup(name);
		if (cc == null)
			return 0;
		return cc.getNumImages();
	}

	// generates an ImageSprite from a seed
	// the seed defines the ConentGroup and item within
	/*
	ImageSprite getSprite(Seed seed) {
		// got to get the correct contentItemGroup first
		NewImageSampleGroup dig = getImageSampleGroup(seed.imageSampleGroupName);

		if (dig == null)
			return null;
		return dig.getSprite(seed);
	}*/

	public ImageSprite getSprite(String contentGroupName, int num) {
		// got to get the correct contentItemGroup first
		ImageSampleGroup dig = getImageSampleGroup(contentGroupName);

		if (dig == null)
			return null;
		return getSprite(dig,num);
	}
	
	public ImageSprite getSprite(Seed seed) {
		
		ImageSampleGroup dig = getImageSampleGroup(seed.imageSampleGroupName);
		if (dig == null)
			return null;
		// creating a sprite from a seed, the ImageSampleGroupManager has already determined
		// to pass the seed to this ImageSampleGroup
		//System.out.println("getSprite:: seed" + seed.getAsCSVStr());
		//System.out.println("there are " + this.getNumItems() + " items available");
		ImageSprite sprite = getSprite(dig, seed.imageSampleGroupItemNumber);
		sprite.setID_RandomSeed(seed.id);
		sprite.setDocPoint(seed.getDocPoint());
		//sprite.depthFromSeed = seed.depth;
		return sprite;
	}
	
	

	ImageSprite getSprite(ImageSampleGroup thisSampleGroup, int num) {
		// creating a sprite from a simple item number within this group
		// sets up almost everything except the document point
		if (thisSampleGroup.getNumImages() == 0) {
			System.out.println("getSprite:: ImageGroup has no images ");
			return null;
		}
		if (num >= thisSampleGroup.getNumImages() || num < 0) {
			System.out.println("getSprite:: index out of range - setting to uppermost available image");
			num = thisSampleGroup.getNumImages() - 1;
		}

		float sizeInScene = thisSampleGroup.getItemSizeInScene(num);
		
		BufferedImage img = thisSampleGroup.getImage(num);
		
		
		//ImageSprite sprite = new ImageSprite(img, thisSampleGroup.getGroupOrigin().copy(), sizeInScene, uniqueID.next());
		ImageSprite sprite = new ImageSprite(img, thisSampleGroup.getGroupOrigin().copy(), sizeInScene, 1);
		sprite.shortImageFileName = thisSampleGroup.getImageName(num);
		sprite.imageSampleGroupName = thisSampleGroup.getGroupName();
		return sprite;
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
	 * @param origin,              the origin of each sprite generated from this
	 *                             collection
	 * @param sizeInScene,         the size in scene, if using 3d in scene-units, 
	 *                             if using 2d, as a document-space measurement
	 * @param useInividualSizes,   when set to true, takes the individual pixel height of the source image into consideration  
	 * 							   relative to the tallest asset which is calculated as having the size in scene  ==  sizeInScene                        
	 */
	void loadImageSampleGroup(String contentGroupName, String targetDirectory, String fileNameMustEndWith,
			String fileNameMustContain, Integer from, Integer to, float preScale, Rect cropRect, PVector origin,
			Float sizeInScene, boolean useInividualSizes) {

		
		DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(targetDirectory);
		dfns.setFileType(fileNameMustEndWith);
    	dfns.setFileNameContains(fileNameMustContain);
    	dfns.setFileListRange(from, to);
    	
    	ImageSampleGroup newImageSampleGroup  = new ImageSampleGroup(contentGroupName);
		
		newImageSampleGroup.setDirectoryFileNameScanner(dfns);
		newImageSampleGroup.setPreScale(preScale);
		newImageSampleGroup.setCrop(cropRect);
		newImageSampleGroup.setGroupOrigins(origin);
		newImageSampleGroup.setGroupSizeInScene(sizeInScene);
		newImageSampleGroup.setUseIndividuaImageSize(useInividualSizes);
		newImageSampleGroup.loadSamples();
		imageSampleGroups.add(newImageSampleGroup);

	}

	/////////////////////////////////////////////////////////////////////////////
	// this is the short-hand method of the above
	public void loadImageSampleGroup(String name, String targetDirectory, Integer from, Integer to, PVector origin,
			Float sizeInScene) {

		loadImageSampleGroup(name, targetDirectory, ".png", "", from, to, 1, new Rect(), origin, sizeInScene, false);

	}
	
	/////////////////////////////////////////////////////////////////////////////
	// creates a completely independent copy of an already loaded sample group, 
	// the new group has a different name, with the opportunity of making the new group a different size and origin
	///////////////////////////////////////////////////////////////////////////// first
    public void cloneImageSampleGroup(String existingGroupname, String newGroupName, PVector orig, Float sizeInScn) {
    	ImageSampleGroup cc = getImageSampleGroup(existingGroupname);
    	if(cc==null) {
    		
    		
    		
    	}
    	ImageSampleGroup newGroup = cc.copy(newGroupName);
    	
    	
    	if(orig!=null) newGroup.setGroupOrigins(orig);
    	if(sizeInScn!=null) newGroup.setGroupSizeInScene(sizeInScn);
    	
    	System.out.println("Cloned " + existingGroupname + " with " + cc.getNumImages() + " into " + newGroupName + " with " + newGroup.getNumImages());
    	imageSampleGroups.add(newGroup);
    }
	/////////////////////////////////////////////////////////////////////////////
	// these are the long-hand method of establishing an image-collection
	// If you are doing it long hand - then the method below needs to be called
	///////////////////////////////////////////////////////////////////////////// first

    ImageSampleGroup addImageSampleGroupNameAndPath(String name, String targetDirectory, String filesStrEndWith, String fileStrContains) {
    	DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(targetDirectory);
    	dfns.setFileNameContains(fileStrContains);
    	
    	
    	ImageSampleGroup newImageSampleGroup  = new ImageSampleGroup(name);
		
		newImageSampleGroup.setDirectoryFileNameScanner(dfns);
		newImageSampleGroup.loadSamples();
		
		imageSampleGroups.add(newImageSampleGroup);
		return newImageSampleGroup;
	}

	void setImageSampleGroupOrigin(String name, PVector origin) {
		ImageSampleGroup cc = getImageSampleGroup(name);
		if (cc == null)
			return;
		cc.setGroupOrigins(origin);
	}

	void setImageSampleGroupSizeInScene(String name, float size, boolean useIndividualSizes) {
		ImageSampleGroup ic = getImageSampleGroup(name);
		if (ic == null)
			return;
		ic.setGroupSizeInScene(size);
		ic.setUseIndividuaImageSize(useIndividualSizes);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Image Processing Effects applied to the entire content group
	//
	///////////////////////////////////////////////////////////////////////////// first

	public void scaleImageSampleGroup(String name, float inx, float iny) {
		ImageSampleGroup ic = getImageSampleGroup(name);
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
	public void colorTransformImageSampleGroup(String groupname, int function, float p1, float p2, float p3) {
		ImageSampleGroup ic = getImageSampleGroup(groupname);
		if (ic == null)
			return;
		ic.colorTransformAll(function, p1, p2, p3);
	}

	

	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	void paradeContent(String groupName, RenderTarget rt) {
		paradeContent(groupName,ImageProcessing.COLORTRANSFORM_NONE, 0, 0, 0, rt);
	}

	void paradeContent(String groupName, int effect, float p1, float p2, float p3, RenderTarget rt) {
		ImageSampleGroup sampleGroup = this.getImageSampleGroup(groupName);
		//Surface parentSurface = GlobalObjects.theSurface;

		int numItems = sampleGroup.getNumImages();
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

		String userSessionPath = GlobalSettings.getUserSessionPath();

		String suggestedName = MOStringUtils.getDateStampedImageFileName("Parade_" + groupName + "_");
		System.out.println("saveRenderLayer: saving " + suggestedName);
		rt.saveRenderToFile(userSessionPath + suggestedName);

	}


}// end of class ImageSampleGroupManager










