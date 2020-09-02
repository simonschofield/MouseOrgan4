import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;


//////////////////////////////////////////////////////////////////////////////////////////////
//
// ContentGroupManager contains many ContentItemGroup, and provides the main user-interface to them
// via the ContentCollection's name. This same name is used to reference iterms within that collection
// in other pars of the system
//
class ContentGroupManager {

	ArrayList<ImageContentGroup> imageContentGroups = new ArrayList<ImageContentGroup>();
	Surface parentSurface;
	float sessionScale = 1f;

	public ContentGroupManager(Surface parent) {
		parentSurface = parent;
		sessionScale = parentSurface.getSessionScale();
	}

	//////////////////////////////////////////////////////////////////////////////
	// 
	//
	ImageContentGroup getImageContentGroup(String name) {
		for (ImageContentGroup cc : imageContentGroups) {
			if (cc.isNamed(name))
				return cc;
		}
		return null;
	}

	int getNumItems(String name) {
		ImageContentGroup cc = getImageContentGroup(name);
		if (cc == null)
			return 0;
		return cc.getNumItems();
	}

	// generates an ImageSprite from a seed
	// the seed defines the ConentGroup and item within
	ImageSprite getSprite(Seed seed) {
		// got to get the correct contentItemGroup first
		ImageContentGroup dig = getImageContentGroup(seed.contentItemDescriptor.contentGroupName);

		if (dig == null)
			return null;
		return dig.getSprite(seed);
	}
	
	
	ImageSprite getSprite(String contentGroupName, int num) {
		// got to get the correct contentItemGroup first
		ImageContentGroup dig = getImageContentGroup(contentGroupName);

		if (dig == null)
			return null;
		return dig.getSprite(num);
	}
	

/**
 * @param contentGroupName, this is the name you give the content group for further access via the manager
 * @param targetDirectory, this is the directory containing the files you wish to load
 * @param fileNameMustEndWith, this is a file name filter, set to ".png" to load all png file. Set to "" or "*" if you dont want to filer on endings
 * @param fileNameMustContain, this is a file name filter, set to "apple" to load all files containing the string"apple". Set to "" or "*" if you don't want any filter
 * @param from, load all files FROM this item number within the target directory that meet the filter criteria (can be nulled, in which case loads from first item found)
 * @param to, load all files TO this item number within the target directory that meet the filter criteria (can be nulled, in which case loads TO the final item found)
 * @param preScale, apply a uniform scaling to all items. This is supplementary to the session scale.
 * @param cropRect, apply a uniform crop to all items. The rect is in normalised coords
 * @param origin, the origin of each sprite generated from this collection
 * @param sizeInScene, the size in scene, in scene-units,  if using 3d etc, as calculated by the sprite and scene data
 */
void loadImageContentGroup(String contentGroupName, String targetDirectory, String fileNameMustEndWith, String fileNameMustContain, Integer from, Integer to, float preScale, Rect cropRect, PVector origin, Float sizeInScene) {
		
		ImageContentGroup thisImageContentGroup = addImageContentGroupNameAndPath(contentGroupName, targetDirectory, fileNameMustEndWith, fileNameMustContain);
		
		if(from==null) from = 0;
		if(to == null) {
			to = thisImageContentGroup.getNumFileTypeInTargetDirectory(fileNameMustEndWith, fileNameMustContain) - 1;
		}

		thisImageContentGroup.loadContent(from, to, preScale,  cropRect);
		

		if (origin == null) {
			origin = new PVector(0.5f, 0.5f);
		}
		setImageContentGroupOrigin(contentGroupName, origin);

		if (sizeInScene == null) {
			// do nothing
		} else {
			setImageContentGroupSizeInScene(contentGroupName, sizeInScene);
		}

	}
	

	
	/////////////////////////////////////////////////////////////////////////////
	// this is the short-hand method of the above 
	void loadImageContentGroup(String name, String targetDirectory, Integer from, Integer to, PVector origin, Float sizeInScene) {
		
		loadImageContentGroup( name,  targetDirectory, ".png", "",  from,  to,  1,  new Rect(),  origin,  sizeInScene);
		
	}

	/////////////////////////////////////////////////////////////////////////////
	// these are the long-hand method of establishing an image-collection
	// If you are doing it long hand - then the method below needs to be called
	///////////////////////////////////////////////////////////////////////////// first
	
	ImageContentGroup addImageContentGroupNameAndPath(String name, String targetDirectory, String filesStrEndWith, String fileStrContains) {
		ImageContentGroup ic = new ImageContentGroup(name, targetDirectory, filesStrEndWith, fileStrContains, parentSurface);
		imageContentGroups.add(ic);
		return ic;
	}

	void setImageContentGroupOrigin(String name, PVector origin) {
		ImageContentGroup cc = getImageContentGroup(name);
		if (cc == null)
			return;
		cc.setOrigins(origin);
	}

	void setImageContentGroupSizeInScene(String name, float size) {
		ImageContentGroup ic = getImageContentGroup(name);
		if (ic == null)
			return;
		ic.setSizeInScene(size);
	}
	
	/////////////////////////////////////////////////////////////////////////////
	// Image Processing Effects applied to the entire content group
	// 
	///////////////////////////////////////////////////////////////////////////// first

	void scaleImageContentGroup(String name, float inx, float iny) {
		ImageContentGroup ic = getImageContentGroup(name);
		if (ic == null)
			return;
		ic.scaleAll(inx, iny);
	}
	
	
	/**
	 * Generalised color processing method applied to the entire contents of an ImageContentGroup
	 * @param groupname the ImageContentGroup to adjust
	 * @param function the color function string either "hsv", "brightnessNoClip", "brightness", "contrast", "levels"
	 * @param p1 first parameter if needed
	 * @param p2 second parameter if needed
	 * @param p3 third parameter if needed
	 * @brief adjusts the color of a whole ImageContentGroup
	 */
	void colorAdjustImageContentGroup(String groupname, String function, float p1, float p2, float p3) {
		ImageContentGroup ic = getImageContentGroup(groupname);
		if (ic == null)
			return;
		ic.colorAdjustAll(function,p1,p2,p3);
	}
	
	void hsvAdjustImageContentGroup(String name, float dh, float ds, float dv) {
		ImageContentGroup ic = getImageContentGroup(name);
		if (ic == null)
			return;
		ic.hsvAdjustAll(dh,ds,dv);
	}
	
	void paradeContent(String groupName) {
		
		ImageContentGroup contentGroup = this.getImageContentGroup(groupName);
		int numItems = contentGroup.getNumItems();
		int biggestItemWidth = (int) contentGroup.widthExtrema.getUpper();
		int biggestItemHeight = (int) contentGroup.heightExtrema.getUpper();
		float itemAspect = biggestItemWidth/(float)biggestItemHeight;
		int outImageW = parentSurface.theDocument.getBufferWidth();
		int outImageH = parentSurface.theDocument.getBufferHeight();
		
		// assume they are all fit into shapes biggestWidth/Biggestheight - this wil be a good fit for most content of similar aspects
		// 
		// assuming a portrait shaped item
		// first scale the height of the outImageH by the aspect of the shaped item. Then you can deal with it as if they were all squares, which
		// is simpler to think about
		float outImageHScaled = outImageH * itemAspect;
		float outImageScaledAspect = outImageW/outImageHScaled;
		float idealNumRows = (float) Math.sqrt( (double)(numItems/outImageScaledAspect) );
		int numItemsInRow = (int) Math.ceil(idealNumRows * outImageScaledAspect);
		int numOfItemRows = numItems/numItemsInRow;
		int remainingInLastRow = numItems - (numOfItemRows*numItemsInRow);
		
		System.out.println("paradeContent : numItems = " + numItems + ", Num In Row " + numItemsInRow + ", actualNumWholeRows "  + numOfItemRows + ", with remaining " + remainingInLastRow);
		
		// so now we know the arrangement of the items in the larger image
		
		// we also need to scale the image to fit into the box
		int boxWidth = (int) (outImageW/(float)numItemsInRow);
		int boxHeight = (int) (outImageH/(float)(numOfItemRows+1));
		int itemCounter = 0;
		for(int y= 0; y <= numOfItemRows; y++) {
			for(int x = 0; x < numItemsInRow; x++) {
				int thisItemX = x *  boxWidth;
				int thisItemY = y * boxHeight + 20;
				if(itemCounter < numItems) {
				BufferedImage img = contentGroup.getImage(itemCounter);
				String itemName = contentGroup.getShortNameOfItem(itemCounter);
				int imgW = img.getWidth();
				int imgH = img.getHeight();
				float imgAspect = imgW/(float)imgH;
				int scaledHeight = (int)(boxHeight * 0.75f);
				int scaledWidth = (int)(boxHeight*imgAspect * 0.75f);
					img = ImageProcessing.scaleTo(img, scaledWidth, scaledHeight);
					parentSurface.theDocument.pasteImage_BufferCoordinates( img, thisItemX, thisItemY, 1.0f);
	
					parentSurface.theDocument.drawText(itemName, thisItemX, thisItemY+scaledHeight+50, 50, Color.DARK_GRAY);
				}
				itemCounter++;
			}
		}
		
		String userSessionPath = parentSurface.getUserSessionPath();
		
		String suggestedName = MOUtils.getDateStampedImageFileName("Parade_" + groupName + "_");
		System.out.println("saveRenderLayer: saving " + suggestedName);
		parentSurface.theDocument.saveRenderToFile(userSessionPath + suggestedName);
		
	}

}// end of class ContentManager

//////////////////////////////////////////////////////////////////////////////////////////////
//
//ImageContentGroup
//A list of content items held in memory as BufferedImages
//They are pre-scaled to the session scale at load time, and then the scaled items cached for rapid reload next time.
//They have a shared spriteOrigin
//and a shared world3DHeight

class ImageContentGroup extends DirectoryImageGroup {
	String groupName = "";

	Surface parentSurface;
	PVector spriteOrigin = new PVector(0.5f, 0.5f);

	//
	float sizeInScene = 1.0f;
	
	// this method is used for image content groups, so global scaling is applied
	public ImageContentGroup(String collectionName, String targetDirectory, Surface parent) {
		super(targetDirectory, ".png", "");
		parentSurface = parent;
		groupName = collectionName;

	}
	
	public ImageContentGroup(String collectionName, String targetDirectory, String filesStrEndWith, String fileStrContains, Surface parent) {
		super(targetDirectory, filesStrEndWith, fileStrContains);
		parentSurface = parent;
		groupName = collectionName;

	}

	boolean isNamed(String name) {
		if (groupName.contentEquals(name))
			return true;
		return false;
	}

	void setOrigins(PVector orig) {
		spriteOrigin = orig;
	}

	void setSizeInScene(float h) {
		sizeInScene = h;
		
	}

	float getSizeInScene() {
		return sizeInScene;
		
	}
	
	
	
	void loadContent(int fromIndex, int toIndex, float preScale, Rect cropRect) {
	    // this version does caching and loading of previously scaled images
	    // if the cache load/save is involved then cropping only takes place after the cache save/load so is never committed to the cache
		// however cropping takes place per image load at full scale
		float sessionScale = 1;
		if (parentSurface != null) {
			sessionScale = parentSurface.getSessionScale();
		}

		if (sessionScale > 0.99) {
			//System.out.println("NOT using cache folder ...");
			
			// this base class method loads at 100% scale, and then does the preScaling and rect cropping.
			super.loadContent(fromIndex, toIndex, preScale, cropRect);
			assertImageTYPE_INT_ARGB();
			return;
		}

		// if needs rescaling due to session scale, then the cache comes into play.
		// check to see if a folder called targetDirectory//cached_scaled_*percentile*
		// exists
		
		// first get the list of files, as they should be, in the non-cache directory (the original files in the sample lib)
		ArrayList<String> sampleLibFileNames = getShortFileNamesInDirectory(directoryPath, fileStringEndsWith, fileStringContains);
		sampleLibFileNames = GenericArrayListUtils.trimList(sampleLibFileNames, fromIndex, toIndex);
		
		
		String cachedImagesFolderName = getCachedScaledImagesFolderName(sessionScale);
		//System.out.println("using cache folder ..." + cachedImagesFolderName);
		if (checkCacheExists(cachedImagesFolderName, sampleLibFileNames, fromIndex, toIndex)) {
			// if it exists load those images, end.
			loadContent(cachedImagesFolderName, fileStringEndsWith, fileStringContains, fromIndex, toIndex, 1, new Rect());
			assertImageTYPE_INT_ARGB();
		} else {
			// if these is a problemns with the cache (it doesnt exist or the files in the cache do not match the 
			// load from the original source directory place, but create a cache
			boolean ok = MOUtils.createDirectory(cachedImagesFolderName);
			if (!ok)
				System.out.println("problem creating cache folder ..." + cachedImagesFolderName);
			
			loadContent(directoryPath, fileStringEndsWith, fileStringContains, fromIndex, toIndex, 1, new Rect());
			// then scale them and save them to a folder called
			// targetDirectory//cached_scaled_*percentile*
			scaleAll(sessionScale, sessionScale);
			assertImageTYPE_INT_ARGB();
			saveAll(cachedImagesFolderName);
		}
		
		
		
		
		if(cropRect.equals(new Rect())==false) {
			cropAll(cropRect);
		}
		
		if(preScale < 1) {
			scaleAll(preScale,preScale);
		}

	}
	
	
	
	void assertImageTYPE_INT_ARGB() {
		// all image content items should be of type INT_ARGB for all the 
		// operations to work OK. This makes sure they are.
		int numItems = getNumItems();
		for (int n = 0; n < numItems; n++) {
			BufferedImage thisImage = getImage(n);
			if(thisImage.getType() != BufferedImage.TYPE_INT_ARGB) {
				thisImage = ImageProcessing.convertColorModel(thisImage, BufferedImage.TYPE_INT_ARGB);
				setImage(thisImage,n);
			}
		}
		
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// cache related methods 
	//
	
	// this checks to see if the directory exists, and if it does then it checks to see if all the cached files exist also.
	// if not then return false.
	boolean checkCacheExists(String directory, ArrayList<String> sampleLibFileNames, int fromIndex, int toIndex) {
		if (MOUtils.checkDirectoryExist(directory) == false)
			return false;
		ArrayList<String> cachedFileNames = getShortFileNamesInDirectory(directory, fileStringEndsWith, fileStringContains);
		cachedFileNames = GenericArrayListUtils.trimList(cachedFileNames, fromIndex, toIndex);

		if( GenericArrayListUtils.listsContentsAreEqual(sampleLibFileNames, cachedFileNames) ) return true;

		return false;
		
	}
	
	

	String getCachedScaledImagesFolderName(float scale) {
		int scalePercentile = (int) (scale * 100);
		String cachFolderRoot = "C:\\mouseOrganImageCache\\";

		String strippedDirectoryPath = directoryPath.replace("C:\\sample lib\\", "");
		//System.out.println("directoryPath ..." + directoryPath);
		//System.out.println("strippedDirectoryPath ..." + strippedDirectoryPath);
		String cachedFolderName = cachFolderRoot + strippedDirectoryPath + "\\cached_scaled_" + scalePercentile;

		//System.out.println("creating cach folder ..." + cachedFolderName);
		return cachedFolderName;
	}

	void saveAll(String dir) {
		int numItems = getNumItems();
		for (int n = 0; n < numItems; n++) {
			BufferedImage thisImage = getImage(n);
			String shortname = getShortNameOfItem(n);
			String fullPathAndName = dir + "\\" + shortname + ".png";
			ImageProcessing.saveImage(fullPathAndName, thisImage);
		}

	}
	
	///////////////////////////////////////////////////////////////////////////
	// content manipulation methods - affect all members of the group
	//
	void scaleAll(float x, float y) {
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage scaled = ImageProcessing.scaleImage(img, x, y);
			imageList.set(n, scaled);
		}
	}
	
	void cropAll(Rect cropRect) {
		if(cropRect.equals(new Rect())) return;
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage cropped = ImageProcessing.cropImageWithParametricRect(img,cropRect);
			imageList.set(n, cropped);
		}
	}
	
	void hsvAdjustAll(float dh, float ds, float dv) {
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage scaled = ImageProcessing.adjustHSV(img, dh, ds, dv);
			imageList.set(n, scaled);
		}
		
	}
	
	void brightnessAdjustAll(float db) {
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			img = ImageProcessing.adjustBrightness(img, db);
			imageList.set(n, img);
		}
		
	}
	
	void colorAdjustAll(String function, float p1, float p2, float p3) {
		int numImage = imageList.size();
		
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			System.out.println("in colorAdjustAll . Function = " + function);
			BufferedImage adjustedImage;
			switch(function){
				case "hsv": { adjustedImage = ImageProcessing.adjustHSV(img, p1,p2,p3); break; }
				case "brightnessNoClip": { adjustedImage = ImageProcessing.adjustBrightnessNoClip(img, p1); break; }
				case "brightness": { adjustedImage = ImageProcessing.adjustBrightness(img, p1); break; }
				case "contrast": { adjustedImage = ImageProcessing.adjustContrast(img, p1); break; } 
				case "levels" : { adjustedImage =  ImageProcessing.adjustLevels(img, p1, p2, p3); break; } 
				default: adjustedImage = img;
			}
			imageList.set(n, adjustedImage);
		}
		
		
	}

	void compressSizes(float amount) {
	// when amount == 0 no change in sizes
	// when amount == 1, all shapes scaled to fit into smallest
	// 0.5 == halfway between these two options

	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Sprite based stuff
	ImageSprite getSprite(Seed seed) {
		int n = seed.contentItemDescriptor.itemNumber;
		if (getNumItems() == 0) {
			System.out.println("getSprite:: ImageGroup has no images ");
			return null;
		}
		if (n >= getNumItems() || n < 0) {
			System.out.println("getSprite:: index out of range - setting to uppermost available image");
			n = getNumItems() - 1;
		}

		float world3Dheight = getSizeInScene();
		//System.out.println("gImageSprite getSprite() world3Dheight " + world3Dheight);
		ImageSprite sprite = new ImageSprite(seed, getImage(n), spriteOrigin.copy(), world3Dheight);

		return sprite;
	}
	
	ImageSprite getSprite(int num) {
		if (getNumItems() == 0) {
			System.out.println("getSprite:: ImageGroup has no images ");
			return null;
		}
		if (num >= getNumItems() || num < 0) {
			System.out.println("getSprite:: index out of range - setting to uppermost available image");
			num = getNumItems() - 1;
		}

		float world3Dheight = getSizeInScene();
		ImageSprite sprite = new ImageSprite(getImage(num), spriteOrigin.copy(), world3Dheight);

		return sprite;
	}

	

}





@SuppressWarnings("serial")
//a seed factory generates seeds. These have points in documentspace and associates
//some asset with that point
///////////////////////////////////////////////////////////////////////////
//ContentItemSelection is full description of the asset residing in a seed
//and returned from ContentItemSelector
class ContentItemDescription implements Serializable{
	String contentGroupName;
	int itemNumber;
	
	public ContentItemDescription(String collectionName, int itemNum) {
		contentGroupName = collectionName;
		itemNumber = itemNum;
	}
	
	String toStr() {
		
		return " content group name " + contentGroupName + ", itemNumber " + itemNumber;
	}
}



/////////////////////////////////////////////////////////////////////////////
//given a number of ContentItemProbability types
//select one using randomness
//
// ContentItemProbabilities are added to a list. Their combined probabilities are
// normalised 0..1, and form a "probability stack". A random number 0..1 is generated and
// and the item at that point in the probability stack gets selected.
//
// Experimental: an influencer image can be added to influence the decision
// 
class ContentItemSelector{
	
	
	// inner class ContentItemProbability
	class ContentItemProbability{
		// class used only by ContentItemSelector
		String contentGroupName;
		float probability;
		
		public ContentItemProbability(String name, float prob) {
			contentGroupName= name;
			probability = prob;
		}
	}


	ContentGroupManager theContentManager;
	ArrayList<ContentItemProbability> itemProbabilities = new ArrayList<ContentItemProbability>();
	boolean normalised = false;
	RandomStream randomStream;
	
	// an image can be set to influence the outcome
	// the image value 0...1, is used to weight the decision to that part of the item "probability stack"
	SceneData3D sceneData3D = null;
	
	
	String influencerImageName;
	float influencerWeighting = 0.5f;
	
	public ContentItemSelector(ContentGroupManager contentManager, int rseed) {
		randomStream = new RandomStream(rseed);
		theContentManager = contentManager;
	}
	
	public ContentItemSelector(ContentGroupManager contentManager, SceneData3D sd3d, String influencerImgName, float weighting, int rseed) {
		sceneData3D = sd3d;
		influencerImageName = influencerImgName;
		influencerWeighting = weighting;
		randomStream = new RandomStream(rseed);
		theContentManager = contentManager;
		//influencerImage = influence;
		//float aspect = theContentManager.parentSurface.theDocument.getDocumentAspect();
		//influencerImageCoordinateSpaceConverter = new CoordinateSpaceConverter(influencerImage.getWidth(),influencerImage.getHeight(),aspect);
	}
	
	
	// could pass in the image collection instead to get the name and numItems
	void addContentItemProbability(String contentGroupName, float prob) {
		
		ContentItemProbability cip = new ContentItemProbability(contentGroupName, prob);
		itemProbabilities.add(cip);
	}
	
	void normaliseProbabilities() {
		
		float sumOfProbs = 0f;
		for(ContentItemProbability cip: itemProbabilities) {
			sumOfProbs += cip.probability;
			}
		
		for(ContentItemProbability cip: itemProbabilities) {
			cip.probability = cip.probability/sumOfProbs;
			}
		
		normalised = true;
	}
	
	ContentItemDescription selectContentItemDescription(PVector docPt) {
		// called by the seed batch upon making a batch
		// only needs docPoint if an influenceImage is set
		if(normalised == false) normaliseProbabilities();
		
		if(itemProbabilities.size()==1) {
			return getContentItemDescriptionFromGroup(itemProbabilities.get(0).contentGroupName);
		}
		
		if(sceneData3D==null) {
			float r = randomStream.randRangeF(0f, 1f);
			return getContentItemDescriptionFromProbabilityStack(r);
			}
		return selectContentItemDescriptionImageInfluence(docPt);
	}

	ContentItemDescription selectContentItemDescriptionImageInfluence(PVector docPt) {
		//PVector bufferLocation = influencerImageCoordinateSpaceConverter.docSpaceToImageCoord(docPt);
		//float imageVal = ImageProcessing.getValue01(influencerImage, (int)bufferLocation.x, (int)bufferLocation.y);
		sceneData3D.setCurrentRenderImage(influencerImageName);
		float imageVal = sceneData3D.getCurrentRender01Value(docPt);
		float r = randomStream.randRangeF(0f, 1f);
		float imageWeightedRandomNum = MOMaths.lerp(influencerWeighting,r, imageVal);
		
		return getContentItemDescriptionFromProbabilityStack(imageWeightedRandomNum);
	}
	
	ContentItemDescription getContentItemDescriptionFromProbabilityStack(float f) {
		float sumOfProbs = 0f;
		
		for(ContentItemProbability cip: itemProbabilities) {
			sumOfProbs += cip.probability;
			if( f <= sumOfProbs) {
				return getContentItemDescriptionFromGroup(cip.contentGroupName);
				}
			}
		// should not get here
		return new ContentItemDescription("",  0);
	}
	
	
	
	
	ContentItemDescription getContentItemDescriptionFromGroup(String groupName) {
		int numItemsInContentGroup = theContentManager.getNumItems(groupName);
		int itemNum = randomStream.randRangeInt(0, numItemsInContentGroup-1);
		return new ContentItemDescription(groupName,  itemNum);
		
	}
	
	
}// end class ContentItemSelector





