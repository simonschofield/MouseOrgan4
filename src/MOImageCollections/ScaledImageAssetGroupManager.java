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
// ScaledImageAssetGroupManager contains many ScaledImageAssetGroups
// These then can be made visible to any class through the GlobalSettings.getImageGroupManager() method
// 
//
public class ScaledImageAssetGroupManager {

	
	
	ArrayList<ScaledImageAssetGroup> theScaledImageAssetGroupList = new ArrayList<ScaledImageAssetGroup>();
	//Surface parentSurface;
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// The individual sclaed image asset groups have a boolean deferCache.
	// The flags below determine how they are to be set en-masse
	//
	
	
	public ScaledImageAssetGroupManager() {
		GlobalSettings.setImageAssetGroupManager(this);
	}

	//////////////////////////////////////////////////////////////////////////////
	//
	//
	public ScaledImageAssetGroup getScaledImageAssetGroup(String name) {
		for (ScaledImageAssetGroup cc : theScaledImageAssetGroupList) {
			if (cc.isNamed(name))
				return cc;
		}
		System.out.println("ScaledImageAssetGroupManager::getScaledImageAssetGroup cannot find a group called " + name + ". Returning null ");
		return null;
	}
	
	public int getNumGroups() {
		return theScaledImageAssetGroupList.size();
	}
	

	int getNumImageAssetsInGroup(String name) {
		ScaledImageAssetGroup cc = getScaledImageAssetGroup(name);
		if (cc == null)
			return 0;
		return cc.getNumImageAssets();
	}

	
	/////////////////////////////////////////////////////////////////////////////
	// short-hand methods of the one below
	//
	
	// the most default setting, using CACHEMODE_ADAPTIVE_LOADANDSAVE
	public ScaledImageAssetGroup loadImageAssetGroup(String name, String sourceDirectory) {
		return loadImageAssetGroup(name, sourceDirectory, ".png", "", null, null, ScaledImageAssetGroup.CACHEMODE_ADAPTIVE_LOADANDSAVE);
	}
	
	// the most default setting, using user-set CACHE MODE, for caching post-load processing
	public ScaledImageAssetGroup loadImageAssetGroup(String name, String sourceDirectory, int cacheMode) {
		return loadImageAssetGroup(name, sourceDirectory, ".png", "", null, null, cacheMode);
	}
	
	
	

	/**
	 * @param assetGroupName,    this is the name you give the content group for
	 *                             further access via the manager
	 * @param sourceDirectory,     this is the directory containing the files you
	 *                             wish to load
	 * @param fileNameMustEndWith, this is a file name filter, set to ".png" to load
	 *                             all png file. Set to "" or "*" if you dont want
	 *                             to filter on endings
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
	 * @return            		   (Use is optional) Returns the ScaledImageAsssetGroup just created                
	 */
	public ScaledImageAssetGroup loadImageAssetGroup(String assetGroupName, String sourceDirectory, String fileNameMustEndWith,
			String fileNameMustContain, Integer from, Integer to, int cacheMode) {

		
		DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(sourceDirectory);
		dfns.setFileType(fileNameMustEndWith);
    	dfns.setFileNameContains(fileNameMustContain);
    	dfns.setFileListRange(from, to);
    	
    	int n = dfns.getNumFiles();
    	System.out.println("loadImageAssetGroup loading " + n + " images " + assetGroupName );
    	
    	
    	ScaledImageAssetGroup scaledImageAssetGroup  = new ScaledImageAssetGroup(assetGroupName);
		
		scaledImageAssetGroup.setDirectoryFileNameScanner(dfns);
		scaledImageAssetGroup.setCacheMode(cacheMode);
		scaledImageAssetGroup.loadSessionScaledImages();

		theScaledImageAssetGroupList.add(scaledImageAssetGroup);
		return scaledImageAssetGroup;
	}

	
	
	/////////////////////////////////////////////////////////////////////////////
	// creates a completely independent copy of an already loaded sample group, 
	// the new group has a different name,so users can rescale or colour treat this group separately
	///////////////////////////////////////////////////////////////////////////// first
    public void cloneImageAssetGroup(String existingGroupname, String newGroupName) {
    	ScaledImageAssetGroup cc = getScaledImageAssetGroup(existingGroupname);
    	if(cc==null) {

    	}
    	ScaledImageAssetGroup newGroup = cc.copy(newGroupName);

    	System.out.println("Cloned " + existingGroupname + " with " + cc.getNumImageAssets() + " into " + newGroupName + " with " + newGroup.getNumImageAssets());
    	theScaledImageAssetGroupList.add(newGroup);
    }
    
    
	

	
     public void addImageAssetGroup(ScaledImageAssetGroup sig) {
    	 // TBD check unique name
    	 theScaledImageAssetGroupList.add(sig);
     }
     
     public void removeImageAssetGroup(String toBeRemovedGroupName) {
    	 ScaledImageAssetGroup toBeRemoved = this.getScaledImageAssetGroup(toBeRemovedGroupName);
    	 theScaledImageAssetGroupList.remove(toBeRemoved);
     }
	

	/////////////////////////////////////////////////////////////////////////////
	// Image Processing Effects applied to the entire content group
	//
	///////////////////////////////////////////////////////////////////////////// first

	public void scaleImageAssetGroup(String name, float inx, float iny) {
		ScaledImageAssetGroup ic = getScaledImageAssetGroup(name);
		if (ic == null)
			return;
		ic.scaleAll(inx, iny);
	}
	
	public String[] getLoadedImageAssetGroupNames() {
		int n = this.getNumGroups();
		String[] names = new String[n];
		int i = 0;
		for (ScaledImageAssetGroup cc : theScaledImageAssetGroupList) {
			names[i] = cc.getGroupName();
			i++;
		}
		return names;
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
	public void colorTransformImageAssetGroup(String groupname, MOColorTransform colTransform) {
		ScaledImageAssetGroup ic = getScaledImageAssetGroup(groupname);
		if (ic == null)
			return;
		ic.colorTransformAll(colTransform);
	}
	
	public void colorTransformAll(MOColorTransform colTransform) {
		for (ScaledImageAssetGroup cc : theScaledImageAssetGroupList) {
			cc.colorTransformAll(colTransform);
		}
	}

	/**
	 * to be called after a deferred cach session... after you have done all your post-load processing
	 * ImageContentGroup
	 */
	 public void cacheAll() {
		 for (ScaledImageAssetGroup cc : theScaledImageAssetGroupList) {
				cc.cacheImages();
			}
		 
	 }
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	public void paradeContent(String groupName, RenderTarget rt) {
		paradeContent(groupName,null, rt);
	}

	void paradeContent(String groupName, MOColorTransform colTransform, RenderTarget rt) {
		ScaledImageAssetGroup sampleGroup = this.getScaledImageAssetGroup(groupName).copy(groupName + "copy");
		
		int numItems = sampleGroup.getNumImageAssets();
		// get them in portrait mode
		for(int n = 0; n < numItems; n++) {
			BufferedImage img = sampleGroup.getImage(n);
			String exisitingName = sampleGroup.getImageAssetName(n);
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

					if(colTransform != null) {
						img = colTransform.doColorTransforms(img);
					}

					String itemName = sampleGroup.getImageAssetName(itemCounter);
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


}// end of class 











