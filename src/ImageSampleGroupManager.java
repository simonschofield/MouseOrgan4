import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import MOUtils.PVector;
import MOUtils.Rect;



//////////////////////////////////////////////////////////////////////////////////////////////
//
// ContentGroupManager contains many ContentItemGroup, and provides the main user-interface to them
// via the ContentCollection's name. This same name is used to reference iterms within that collection
// in other pars of the system
//
class ImageSampleGroupManager {

	ArrayList<ImageSampleGroup> imageSampleGroups = new ArrayList<ImageSampleGroup>();
	//Surface parentSurface;

	public ImageSampleGroupManager() {
		//parentSurface = GlobalObjects.theSurface;
	}

	//////////////////////////////////////////////////////////////////////////////
	//
	//
	ImageSampleGroup getImageSampleGroup(String name) {
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
		return cc.getNumItems();
	}

	// generates an ImageSprite from a seed
	// the seed defines the ConentGroup and item within
	ImageSprite getSprite(Seed seed) {
		// got to get the correct contentItemGroup first
		ImageSampleGroup dig = getImageSampleGroup(seed.imageSampleGroupName);

		if (dig == null)
			return null;
		return dig.getSprite(seed);
	}

	ImageSprite getSprite(String contentGroupName, int num) {
		// got to get the correct contentItemGroup first
		ImageSampleGroup dig = getImageSampleGroup(contentGroupName);

		if (dig == null)
			return null;
		return dig.getSprite(num);
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

		ImageSampleGroup thisImageContentGroup = addImageSampleGroupNameAndPath(contentGroupName, targetDirectory,
				fileNameMustEndWith, fileNameMustContain);

		if (from == null)
			from = 0;
		if (to == null) {
			to = thisImageContentGroup.getNumFileTypeInTargetDirectory(fileNameMustEndWith, fileNameMustContain) - 1;
		}

		thisImageContentGroup.loadSamples(from, to, preScale, cropRect);

		if (origin == null) {
			origin = new PVector(0.5f, 0.5f);
		}
		setImageSampleGroupOrigin(contentGroupName, origin);

		if (sizeInScene == null) {
			// do nothing
		} else {
			setImageSampleGroupSizeInScene(contentGroupName, sizeInScene, useInividualSizes);
		}

	}

	/////////////////////////////////////////////////////////////////////////////
	// this is the short-hand method of the above
	void loadImageSampleGroup(String name, String targetDirectory, Integer from, Integer to, PVector origin,
			Float sizeInScene) {

		loadImageSampleGroup(name, targetDirectory, ".png", "", from, to, 1, new Rect(), origin, sizeInScene, false);

	}
	
	/////////////////////////////////////////////////////////////////////////////
	// creates a completely independent copy of an already loaded sample group, 
	// the new group has a different name
	///////////////////////////////////////////////////////////////////////////// first
    void cloneImageSampleGroup(String existingGroupname, String newGroupName, PVector orig, Float sizeInScn) {
    	ImageSampleGroup cc = getImageSampleGroup(existingGroupname);
    	PVector origin = cc.spriteOrigin;
    	if(orig != null  ) origin = orig;
    	float sizeInScene = cc.groupSizeInScene;
    	if(sizeInScn != null  ) sizeInScene = sizeInScn;
    	ImageSampleGroup newGroup = cc.copyToNewGroup(newGroupName, origin, sizeInScene);
    	//System.out.println("Cloned " + existingGroupname + " with " + cc.getNumItems() + " into " + newGroupName + " with " + newGroup.getNumItems());
    	imageSampleGroups.add(newGroup);
    }
	/////////////////////////////////////////////////////////////////////////////
	// these are the long-hand method of establishing an image-collection
	// If you are doing it long hand - then the method below needs to be called
	///////////////////////////////////////////////////////////////////////////// first

	ImageSampleGroup addImageSampleGroupNameAndPath(String name, String targetDirectory, String filesStrEndWith,
			String fileStrContains) {
		ImageSampleGroup ic = new ImageSampleGroup(name, targetDirectory, filesStrEndWith, fileStrContains);
		imageSampleGroups.add(ic);
		return ic;
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
		ic.setGroupSizeInScene(size, useIndividualSizes);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Image Processing Effects applied to the entire content group
	//
	///////////////////////////////////////////////////////////////////////////// first

	void scaleImageSampleGroup(String name, float inx, float iny) {
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
	void colorTransformImageSampleGroup(String groupname, int function, float p1, float p2, float p3) {
		ImageSampleGroup ic = getImageSampleGroup(groupname);
		if (ic == null)
			return;
		ic.colorTransformAll(function, p1, p2, p3);
	}

	

	void paradeContent(String groupName, int effect, float p1, float p2, float p3 ) {
		ImageSampleGroup sampleGroup = this.getImageSampleGroup(groupName);
		sampleGroup.paradeContent(effect, p1, p2, p3);
	}

}// end of class ImageSampleGroupManager










