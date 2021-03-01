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
class ImageSampleGroupManager {

	ArrayList<ImageSampleGroup> imageSampleGroups = new ArrayList<ImageSampleGroup>();
	Surface parentSurface;

	public ImageSampleGroupManager() {
		parentSurface = GlobalObjects.theSurface;
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
	 * @param sizeInScene,         the size in scene, in scene-units, if using 3d
	 *                             etc, as calculated by the sprite and scene data
	 */
	void loadImageSampleGroup(String contentGroupName, String targetDirectory, String fileNameMustEndWith,
			String fileNameMustContain, Integer from, Integer to, float preScale, Rect cropRect, PVector origin,
			Float sizeInScene) {

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
			setImageSampleGroupSizeInScene(contentGroupName, sizeInScene);
		}

	}

	/////////////////////////////////////////////////////////////////////////////
	// this is the short-hand method of the above
	void loadImageSampleGroup(String name, String targetDirectory, Integer from, Integer to, PVector origin,
			Float sizeInScene) {

		loadImageSampleGroup(name, targetDirectory, ".png", "", from, to, 1, new Rect(), origin, sizeInScene);

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
		cc.setSpriteOrigins(origin);
	}

	void setImageSampleGroupSizeInScene(String name, float size) {
		ImageSampleGroup ic = getImageSampleGroup(name);
		if (ic == null)
			return;
		ic.setSpriteSizeInScene(size);
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
	void colorAdjustImageSampleGroup(String groupname, int function, float p1, float p2, float p3) {
		ImageSampleGroup ic = getImageSampleGroup(groupname);
		if (ic == null)
			return;
		ic.colorAdjustAll(function, p1, p2, p3);
	}

	void hsvAdjustImageSampleGroup(String name, float dh, float ds, float dv) {
		ImageSampleGroup ic = getImageSampleGroup(name);
		if (ic == null)
			return;
		ic.hsvAdjustAll(dh, ds, dv);
	}

	void paradeContent(String groupName, int effect, float p1, float p2, float p3 ) {
		ImageSampleGroup sampleGroup = this.getImageSampleGroup(groupName);
		sampleGroup.paradeContent(effect, p1, p2, p3);
	}

}// end of class ContentManager

//////////////////////////////////////////////////////////////////////////////////////////////
//
//ImageContentGroup
//A list of content items held in memory as BufferedImages
//They are pre-scaled to the session scale at load time, and then the scaled items cached for rapid reload next time.
//They have a shared spriteOrigin
//and a shared world3DHeight

class ImageSampleGroup extends DirectoryImageGroup {
	String groupName = "";
	Counter uniqueID = new Counter();
	// sprite based settings applied to the whole group
	PVector spriteOrigin = new PVector(0.5f, 0.5f);
	float spriteSizeInScene = 1.0f;

	// this method is used for image content groups, so global scaling is applied
	public ImageSampleGroup(String collectionName, String targetDirectory) {
		super(targetDirectory, ".png", "");

		groupName = collectionName;

	}

	public ImageSampleGroup(String collectionName, String targetDirectory, String filesStrEndWith,
			String fileStrContains) {
		super(targetDirectory, filesStrEndWith, fileStrContains);

		groupName = collectionName;

	}

	boolean isNamed(String name) {
		if (groupName.contentEquals(name))
			return true;
		return false;
	}

	void loadSamples() {
		ArrayList<String> allNames = getFilesInDirectory(directoryPath, fileStringEndsWith, fileStringContains);
		int numImage = allNames.size();
		loadSamples(0, numImage - 1, 1, new Rect());
	}

	void loadSamples(int fromIndex, int toIndex, float preScale, Rect cropRect) {
		// this version does caching and loading of previously scaled images
		// if the cache load/save is involved then cropping only takes place after the
		// cache save/load so is never committed to the cache
		// however cropping takes place per image load at full scale
		float sessionScale = 1;

		sessionScale = GlobalObjects.theSurface.getSessionScale();

		if (sessionScale > 0.99) {
			// System.out.println("NOT using cache folder ...");

			// this base class method loads at 100% scale, and then does the preScaling and
			// rect cropping.
			super.loadImages(fromIndex, toIndex, preScale, cropRect);
			assertImageTYPE_INT_ARGB();
			return;
		}

		// if needs rescaling due to session scale, then the cache comes into play.
		// check to see if a folder called targetDirectory//cached_scaled_*percentile*
		// exists

		// first get the list of files, as they should be, in the non-cache directory
		// (the original files in the sample lib)
		ArrayList<String> sampleLibFileNames = getShortFileNamesInDirectory(directoryPath, fileStringEndsWith,
				fileStringContains);
		sampleLibFileNames = GenericArrayListUtils.trimList(sampleLibFileNames, fromIndex, toIndex);

		String cachedImagesFolderName = getCachedScaledImagesFolderName(sessionScale);
		// System.out.println("using cache folder ..." + cachedImagesFolderName);
		if (checkCacheExists(cachedImagesFolderName, sampleLibFileNames, fromIndex, toIndex)) {
			// if it exists load those images, end.
			// they are already scaled
			super.loadImages(cachedImagesFolderName, fileStringEndsWith, fileStringContains, fromIndex, toIndex, 1, new Rect());
			assertImageTYPE_INT_ARGB();
		} else {
			// if these is a problemns with the cache (it doesnt exist or the files in the
			// cache do not match the
			// load from the original source directory place, but create a cache
			boolean ok = MOUtils.createDirectory(cachedImagesFolderName);
			if (!ok)
				System.out.println("problem creating cache folder ..." + cachedImagesFolderName);

			super.loadImages(directoryPath, fileStringEndsWith, fileStringContains, fromIndex, toIndex, sessionScale, new Rect());
			// then scale them and save them to a folder called
			// targetDirectory//cached_scaled_*percentile*

			assertImageTYPE_INT_ARGB();
			saveAll(cachedImagesFolderName);
		}

		if (cropRect != null) {
			if (cropRect.equals(new Rect()) == false) {
				cropAll(cropRect);
			}
		}

		if (preScale < 1) {
			scaleAll(preScale, preScale);
		}

	}

	void assertImageTYPE_INT_ARGB() {
		// all image content items should be of type INT_ARGB for all the
		// operations to work OK. This makes sure they are.
		int numItems = getNumItems();
		for (int n = 0; n < numItems; n++) {
			BufferedImage thisImage = getImage(n);
			if (thisImage.getType() != BufferedImage.TYPE_INT_ARGB) {
				thisImage = ImageProcessing.convertColorModel(thisImage, BufferedImage.TYPE_INT_ARGB);
				setImage(thisImage, n);
			}
		}

	}

	///////////////////////////////////////////////////////////////////////////
	// cache related methods
	//

	// this checks to see if the directory exists, and if it does then it checks to
	// see if all the cached files exist also.
	// if not then return false.
	boolean checkCacheExists(String directory, ArrayList<String> sampleLibFileNames, int fromIndex, int toIndex) {
		if (MOUtils.checkDirectoryExist(directory) == false)
			return false;
		ArrayList<String> cachedFileNames = getShortFileNamesInDirectory(directory, fileStringEndsWith,
				fileStringContains);
		cachedFileNames = GenericArrayListUtils.trimList(cachedFileNames, fromIndex, toIndex);

		if (GenericArrayListUtils.listsContentsAreEqual(sampleLibFileNames, cachedFileNames))
			return true;

		return false;

	}

	String getCachedScaledImagesFolderName(float scale) {
		int scalePercentile = (int) (scale * 100);
		String cachFolderRoot = "C:\\mouseOrganImageCache\\";

		String strippedDirectoryPath = directoryPath.replace(GlobalObjects.sampleLibPath, "");
		// System.out.println("directoryPath ..." + directoryPath);
		// System.out.println("strippedDirectoryPath ..." + strippedDirectoryPath);
		String cachedFolderName = cachFolderRoot + strippedDirectoryPath + "\\cached_scaled_" + scalePercentile;

		// System.out.println("creating cach folder ..." + cachedFolderName);
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

	void resizeToAll(int x, int y) {
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage scaled = ImageProcessing.resizeTo(img, x, y);
			imageList.set(n, scaled);
		}
	}

	void cropAll(Rect cropRect) {
		if (cropRect.equals(new Rect()))
			return;
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage cropped = ImageProcessing.cropImageWithParametricRect(img, cropRect);
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

	void colorAdjustAll(int function, float p1, float p2, float p3) {
		int numImage = imageList.size();

		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage colTransformedImage = ImageProcessing.colorTransform( img,  function,  p1,  p2,  p3);
			imageList.set(n, colTransformedImage);
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

	void setSpriteOrigins(PVector orig) {
		spriteOrigin = orig;
	}

	void setSpriteSizeInScene(float h) {
		spriteSizeInScene = h;

	}

	float getSpriteSizeInScene() {
		return spriteSizeInScene;

	}

	ImageSprite getSprite(Seed seed) {
		int n = seed.imageSampleGroupItemNumber;
		if (getNumItems() == 0) {
			System.out.println("getSprite:: ImageGroup has no images ");
			return null;
		}
		if (n >= getNumItems() || n < 0) {
			System.out.println("getSprite:: index out of range - setting to uppermost available image");
			n = getNumItems() - 1;
		}

		float world3Dheight = getSpriteSizeInScene();
		// System.out.println("gImageSprite getSprite() world3Dheight " +
		// world3Dheight);
		ImageSprite sprite = new ImageSprite(getImage(n), spriteOrigin.copy(), world3Dheight, seed.id);
		sprite.setDocPoint(seed.getDocPoint());
		
		sprite.contentGroupName = seed.imageSampleGroupName;
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

		float world3Dheight = getSpriteSizeInScene();
		ImageSprite sprite = new ImageSprite(getImage(num), spriteOrigin.copy(), world3Dheight, uniqueID.next());
		sprite.contentGroupName = this.groupName;
		return sprite;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	void paradeContent() {
		paradeContent(ImageProcessing.COLORTRANSFORM_NONE, 0,0,0);
	}
	
	void paradeContent(int effect, float p1, float p2, float p3) {
		Surface parentSurface = GlobalObjects.theSurface;

		int numItems = this.getNumItems();
		int biggestItemWidth = (int) this.widthExtrema.getUpper();
		int biggestItemHeight = (int) this.heightExtrema.getUpper();
		float itemAspect = biggestItemWidth / (float) biggestItemHeight;
		int outImageW = parentSurface.theDocument.getBufferWidth();
		int outImageH = parentSurface.theDocument.getBufferHeight();

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
					BufferedImage img = this.getImage(itemCounter);
					
					img =  ImageProcessing.colorTransform( img,  effect,  p1,  p2,  p3);
					
					String itemName = this.getShortNameOfItem(itemCounter);
					int imgW = img.getWidth();
					int imgH = img.getHeight();
					float imgAspect = imgW / (float) imgH;
					int scaledHeight = (int) (boxHeight * 0.75f);
					int scaledWidth = (int) (boxHeight * imgAspect * 0.75f);
					img = ImageProcessing.resizeTo(img, scaledWidth, scaledHeight);
					parentSurface.theDocument.pasteImage_BufferCoordinates(img, thisItemX, thisItemY, 1.0f);

					parentSurface.theDocument.drawText(itemName, thisItemX, thisItemY + scaledHeight + 50, 50,
							Color.DARK_GRAY);
				}
				itemCounter++;
			}
		}

		String userSessionPath = parentSurface.getUserSessionPath();

		String suggestedName = MOUtils.getDateStampedImageFileName("Parade_" + groupName + "_");
		System.out.println("saveRenderLayer: saving " + suggestedName);
		parentSurface.theDocument.saveRenderToFile(userSessionPath + suggestedName);

	}

}

@SuppressWarnings("serial")
//a seed factory generates seeds. These have points in documentspace and associates
//some asset with that point
///////////////////////////////////////////////////////////////////////////
//ContentItemSelection is full description of the asset residing in a seed
//and returned from ContentItemSelector
class ImageSampleDescription implements Serializable {
	String imageSampleGroupName;
	int itemNumber;

	public ImageSampleDescription(String collectionName, int itemNum) {
		imageSampleGroupName = collectionName;
		itemNumber = itemNum;
	}

	String toStr() {

		return " content group name " + imageSampleGroupName + ", itemNumber " + itemNumber;
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
class ImageSampleSelector {

	// inner class ContentItemProbability
	class ImageSampleGroupProbability {
		// class used only by ContentItemSelector
		String imageSampleGroupName;
		float probability;

		public ImageSampleGroupProbability(String name, float prob) {
			imageSampleGroupName = name;
			probability = prob;
		}
	}

	ImageSampleGroupManager theImageSampleGroupManager;
	ArrayList<ImageSampleGroupProbability> groupProbabilities = new ArrayList<ImageSampleGroupProbability>();
	boolean normalised = false;
	RandomStream randomStream;

	// an image can be set to influence the outcome
	// the image value 0...1, is used to weight the decision to that part of the
	// item "probability stack"
	SceneData3D sceneData3D = null;

	
	String influencerImageName;
	float influencerWeighting = 0.5f;

	public ImageSampleSelector(ImageSampleGroupManager imageSampleGroupManager, int rseed) {
		randomStream = new RandomStream(rseed);
		theImageSampleGroupManager = imageSampleGroupManager;
	}

	public ImageSampleSelector(ImageSampleGroupManager imageSampleGroupManager, SceneData3D sd3d, String influencerImgName,
			float weighting, int rseed) {
		sceneData3D = sd3d;
		influencerImageName = influencerImgName;
		influencerWeighting = weighting;
		randomStream = new RandomStream(rseed);
		theImageSampleGroupManager = imageSampleGroupManager;
		// influencerImage = influence;
		// float aspect =
		// theContentManager.parentSurface.theDocument.getDocumentAspect();
		// influencerImageCoordinateSpaceConverter = new
		// CoordinateSpaceConverter(influencerImage.getWidth(),influencerImage.getHeight(),aspect);
	}

	// could pass in the image collection instead to get the name and numItems
	void addContentItemProbability(String imageSampleGroupName, float prob) {

		ImageSampleGroupProbability cip = new ImageSampleGroupProbability(imageSampleGroupName, prob);
		groupProbabilities.add(cip);
	}

	void normaliseProbabilities() {

		float sumOfProbs = 0f;
		for (ImageSampleGroupProbability cip : groupProbabilities) {
			sumOfProbs += cip.probability;
		}

		for (ImageSampleGroupProbability cip : groupProbabilities) {
			cip.probability = cip.probability / sumOfProbs;
		}

		normalised = true;
	}

	ImageSampleDescription selectImageSampleDescription(PVector docPt) {
		// called by the seed batch upon making a batch
		// only needs docPoint if an influenceImage is set
		if (normalised == false)
			normaliseProbabilities();

		if (groupProbabilities.size() == 1) {
			return getImageSampleDescriptionFromGroup(groupProbabilities.get(0).imageSampleGroupName);
		}

		if (sceneData3D == null) {
			float r = randomStream.randRangeF(0f, 1f);
			return getImageSampleDescriptionFromProbabilityStack(r);
		}
		return selectImageSampleDescriptionImageInfluence(docPt);
	}

	ImageSampleDescription selectImageSampleDescriptionImageInfluence(PVector docPt) {
		// PVector bufferLocation =
		// influencerImageCoordinateSpaceConverter.docSpaceToImageCoord(docPt);
		// float imageVal = ImageProcessing.getValue01(influencerImage,
		// (int)bufferLocation.x, (int)bufferLocation.y);
		sceneData3D.setCurrentRenderImage(influencerImageName);
		float imageVal = sceneData3D.getCurrentRender01Value(docPt);
		float r = randomStream.randRangeF(0f, 1f);
		float imageWeightedRandomNum = MOMaths.lerp(influencerWeighting, r, imageVal);

		return getImageSampleDescriptionFromProbabilityStack(imageWeightedRandomNum);
	}

	ImageSampleDescription getImageSampleDescriptionFromProbabilityStack(float f) {
		float sumOfProbs = 0f;

		for (ImageSampleGroupProbability cip : groupProbabilities) {
			sumOfProbs += cip.probability;
			if (f <= sumOfProbs) {
				return getImageSampleDescriptionFromGroup(cip.imageSampleGroupName);
			}
		}
		// should not get here
		return new ImageSampleDescription("", 0);
	}

	ImageSampleDescription getImageSampleDescriptionFromGroup(String groupName) {
		int numItemsInContentGroup = theImageSampleGroupManager.getNumItems(groupName);
		int itemNum = randomStream.randRangeInt(0, numItemsInContentGroup - 1);
		return new ImageSampleDescription(groupName, itemNum);

	}

}// end class ContentItemSelector