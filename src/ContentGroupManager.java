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

	ImageSprite getSprite(Seed seed) {
		// got to get the correct contentItemGroup first
		ImageContentGroup dig = getImageContentGroup(seed.contentItemDescriptor.contentGroupName);

		if (dig == null)
			return null;
		return dig.getSprite(seed);
	}
	
	

	void setSpriteOrigins(String name, PVector origin) {
		ImageContentGroup cc = getImageContentGroup(name);
		if (cc == null)
			return;
		cc.setSpriteOrigins(origin);
	}

	
	/////////////////////////////////////////////////////////////////////////////
	// this is the short-hand method of establishing an image-collection
	// arguments 3 and after are all nullable, and will result in defaults
	void loadImageContentGroup(String name, String targetDirectory, Integer from, Integer to, PVector origin, Float sizeInScene) {
		addImageContentGroupNameAndPath(name, targetDirectory);
		if (from == null || to == null) {
			loadImageContentGroup(name);
		} else {
			loadImageContentGroup(name, from, to);
		}

		if (origin == null) {
			origin = new PVector(0.5f, 0.5f);
		}
		setSpriteOrigins(name, origin);

		if (sizeInScene == null) {
			// do nothing
		} else {
			setSizeInScene(name, sizeInScene);
		}

	}

	/////////////////////////////////////////////////////////////////////////////
	// these are the long-hand method of establishing an image-collection
	// If you are doing it long hand - then the method below needs to be called
	///////////////////////////////////////////////////////////////////////////// first
	
	void addImageContentGroupNameAndPath(String name, String targetDirectory) {
		ImageContentGroup ic = new ImageContentGroup(name, targetDirectory, parentSurface);
		imageContentGroups.add(ic);
	}

	void loadImageContentGroup(String name, int from, int to) {
		ImageContentGroup ic = getImageContentGroup(name);
		if (ic == null)
			return;
		ic.loadContent(from, to);
	}

	void loadImageContentGroup(String name) {
		ImageContentGroup ic = getImageContentGroup(name);
		if (ic == null)
			return;
		ic.loadContent();
	}

	void setSizeInScene(String name, float size) {
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
	 * 
	 * @param groupname the ImageContentGroup to ajust
	 * @param function the color function string either "hsv", "brightnessNoClip", "brightness", "contrast"
	 * @param p1
	 * @param p2
	 * @param p3
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
		super(targetDirectory);
		parentSurface = parent;
		groupName = collectionName;

	}

	boolean isNamed(String name) {
		if (groupName.contentEquals(name))
			return true;
		return false;
	}

	void setSpriteOrigins(PVector orig) {
		spriteOrigin = orig;
	}

	void setSizeInScene(float h) {
		sizeInScene = h;
		
	}

	float getSizeInScene(int itemNum) {
		return sizeInScene;
		
	}

	void loadContent(int fromIndex, int toIndex) {
	// this version does caching and loading of previously scaled images
	//
		float scl = 1;
		if (parentSurface != null) {
			scl = parentSurface.getSessionScale();
		}

		if (scl > 0.99) {
			super.loadContent(fromIndex, toIndex);
			assertImageTYPE_INT_ARGB();
			return;
		}

		// if needs rescaling
		// check to see if a folder called targetDirectory//cached_scaled_*percentile*
		// exists
		String cachedImagesFolderName = getCachedScaledImagesFolderName(scl);
		if (checkCacheExists(cachedImagesFolderName, fromIndex, toIndex)) {
			// if it exists load those images, end.
			loadContent(cachedImagesFolderName, fromIndex, toIndex);
			assertImageTYPE_INT_ARGB();
		} else {

			boolean ok = createDirectory(cachedImagesFolderName);
			if (!ok)
				System.out.println("problem creating cache folder ..." + cachedImagesFolderName);
			super.loadContent(fromIndex, toIndex);
			// then scale them and save them to a folder called
			// targetDirectory//cached_scaled_*percentile*
			scaleAll(scl, scl);
			assertImageTYPE_INT_ARGB();
			saveAll(cachedImagesFolderName);
		}

	}
	
	
	
	void assertImageTYPE_INT_ARGB() {
		int numItems = getNumItems();
		for (int n = 0; n < numItems; n++) {
			BufferedImage thisImage = getImage(n);
			if(thisImage.getType() != BufferedImage.TYPE_INT_ARGB) {
				thisImage = ImageProcessing.convertColorModel(thisImage, BufferedImage.TYPE_INT_ARGB);
				setImage(thisImage,n);
			}
		}
		
	}
	
	

	boolean checkCacheExists(String directory, int fromIndex, int toIndex) {
		if (checkDirectoryExist(directory) == false)
			return false;
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();
		int numFilesInDir = listOfFiles.length;
		int numFilesRequested = (toIndex - fromIndex) + 1;

		if (numFilesInDir < numFilesRequested)
			return false;
		return true;
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
	
	void scaleAll(float x, float y) {
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage scaled = ImageProcessing.scaleImage(img, x, y);
			imageList.set(n, scaled);
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

		float world3Dheight = getSizeInScene(n);
		ImageSprite sprite = new ImageSprite(seed, getImage(n), spriteOrigin.copy(), world3Dheight);

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





