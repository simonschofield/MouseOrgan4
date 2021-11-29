import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import MOUtils.Counter;
import MOUtils.GenericArrayListUtils;
import MOUtils.MOMaths;
import MOUtils.MOUtils;
import MOUtils.PVector;
import MOUtils.Rect;

//////////////////////////////////////////////////////////////////////////////////////////////
//
//
//
//
//
//
//
//ImageSampleGroup
//A list of content items held in memory as BufferedImages
//They are pre-scaled to the session scale at load time, and then the scaled items cached for rapid reload next time.
//They have a shared spriteOrigin
//and a shared world3DHeight

class ImageSampleGroup extends DirectoryImageGroup {
	String groupName = "";
	Counter uniqueID = new Counter();
	// sprite based settings applied to the whole group
	PVector spriteOrigin = new PVector(0.5f, 0.5f);
	
	// this defines the size in the scene. If in 2D, then the unit is in documentSpace
	// if in 3D, then the unit is in 3D space measurements
	float groupSizeInScene = 1.0f;
	
	// if this is false, all items have the same size in scene
	// if this is set to true, then the size applies only to the largest item in the group;
	// all the other items are scaled by their relative pixel size to this item.
    boolean useIndividualItemSizeInScene = false;
    
    
    
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
	
	
	public ImageSampleGroup(String collectionName) {
		// this is for saving arbitrary collections loaded from elsewhere
		super(null, ".png", "");
		
	}
	
	
	// not tested yet
	ImageSampleGroup copyToNewGroup(String newGroupName, PVector groupOrigin, float sizeInScene) {
		ImageSampleGroup newGroup = new ImageSampleGroup(newGroupName, directoryPath);
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage copyImg =  ImageProcessing.copyImage(img);
			newGroup.imageList.add(copyImg);
		}
		newGroup.setGroupOrigins(groupOrigin);
		newGroup.setGroupSizeInScene(sizeInScene, this.useIndividualItemSizeInScene);
		newGroup.directoryContentShortNameList = (ArrayList)(this.directoryContentPathAndNamesList.clone());
		return newGroup;
	}
	
	void appendOtherSampleGroupItems(ImageSampleGroup other) {
		int numNewImages = other.getNumItems();
		for(int n = 0; n < numNewImages; n++) {
			BufferedImage img = other.getImage(n);
			String shortName = other.getShortNameOfItem(n);
			addImage( img,  shortName);
		}
		
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
		if (checkCacheOK(cachedImagesFolderName, sampleLibFileNames, fromIndex, toIndex)) {
			// if it exists load those images, end.
			// they are already scaled
			super.loadImages(cachedImagesFolderName, fileStringEndsWith, fileStringContains, fromIndex, toIndex, 1, new Rect());
			assertImageTYPE_INT_ARGB();
		} else {
			// if these is a problem with the cache (it doesn't exist or the files in the
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
	boolean checkCacheOK(String directory, ArrayList<String> sampleLibFileNames, int fromIndex, int toIndex) {
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
	
	void rotateAll(float rot) {
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage rotated = ImageProcessing.rotateImage(img, rot);
			imageList.set(n, rotated);
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
			BufferedImage cropped = ImageProcessing.cropImageWithNormalisedRect(img, cropRect);
			imageList.set(n, cropped);
		}
	}
	
	
	void addBoarderProportionAll(float left, float top, float right, float bottom) {
		// calculates the new additions as a proportion of the existing width or height
		
		int numImage = imageList.size();
		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			int w = img.getWidth();
			int h = img.getHeight();
			int leftAddition 	= 	(int)( w * left);
			int topAddition  	= 	(int)( h * top);
			int rightAddition  	= 	(int)( w * right);
			int bottomAddition  = 	(int)( h * bottom);
			
			
			BufferedImage withBoarder = ImageProcessing.addBoarder(img, leftAddition, topAddition, rightAddition, bottomAddition);
			imageList.set(n, withBoarder);
		}
		
	}

	
	void colorTransformAll(int function, float p1, float p2, float p3) {
		int numImage = imageList.size();

		for (int n = 0; n < numImage; n++) {
			BufferedImage img = imageList.get(n);
			BufferedImage colTransformedImage = ImageProcessing.colorTransform( img,  function,  p1,  p2,  p3);
			imageList.set(n, colTransformedImage);
		}

	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Values set by the user in the group to establish size and origin of image when used in a sprite context

	void setGroupOrigins(PVector orig) {
		spriteOrigin = orig;
	}

	// This is a user-defined size that is used in both 2 and 3d situations.
	// In 2d context the units are in document space
	// in 3d context the units are in world space size
	// It is not until you call sprite.scaleToSizeInScene(...) that the value is used to actually scale the sprite
	//void setGroupSizeInScene(float h) {
	//	groupSizeInScene = h;
//
	//}
	
	void setGroupSizeInScene(float h, boolean useInividualHeights) {
		groupSizeInScene = h;
		useIndividualItemSizeInScene = useInividualHeights;
	}

	float getGroupSizeInScene() {
		return groupSizeInScene;

	}
	
	float getItemSizeInScene(int num) {
		// returns the size of the specific item in the scene
		// if useIndividualItemSizeInScene == true, then the group size is scaled against the relative
		// height of this item (i.e. item num) to the largest item in the group
		// if useIndividualItemSizeInScene == false then all the items' sizes are the same i.e. groupSizeInScene
		if(useIndividualItemSizeInScene) {
			int imgHght = getImage(num).getHeight();
			float heightMaxItem = heightExtrema.getUpper(); 
			return groupSizeInScene * (imgHght/heightMaxItem);
		} else {
			return groupSizeInScene;
		}
		
	}

	ImageSprite getSprite(Seed seed) {
		// creating a sprite from a seed, the ImageSampleGroupManager has already determined
		// to pass the seed to this ImageSampleGroup
		//System.out.println("getSprite:: seed" + seed.getAsCSVStr());
		//System.out.println("there are " + this.getNumItems() + " items available");
		ImageSprite sprite = getSprite(seed.imageSampleGroupItemNumber);
		sprite.setID_RandomSeed(seed.id);
		sprite.setDocPoint(seed.getDocPoint());
		//sprite.depthFromSeed = seed.depth;
		return sprite;
	}
	
	

	ImageSprite getSprite(int num) {
		// creating a sprite from a simple item number within this group
		// sets up almost everything except the document point
		if (getNumItems() == 0) {
			System.out.println("getSprite:: ImageGroup has no images ");
			return null;
		}
		if (num >= getNumItems() || num < 0) {
			System.out.println("getSprite:: index out of range - setting to uppermost available image");
			num = getNumItems() - 1;
		}

		float sizeInScene = getItemSizeInScene(num);
		
		BufferedImage img = getImage(num);
		
		
		ImageSprite sprite = new ImageSprite(img, spriteOrigin.copy(), sizeInScene, uniqueID.next());
		sprite.shortImageFileName = getShortNameOfItem(num);
		sprite.imageSampleGroupName = this.groupName;
		return sprite;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// allows the user to get a random sprite or image from the group
	// and apply a filter to that name; the name returned must contain the filter expression
	// if set to null or "", allows all image to be returned
	// Needs a reference to a rand number generator to work.
	ImageSprite getRandomSprite(QRandomStream ranStream, String nameContainsFilter) {
		String foundName = getRandomImageName( ranStream,  nameContainsFilter);
		int imgNum = getNumOfImageShortName(foundName);
		return getSprite(imgNum);
	}
	
	
	BufferedImage getRandomImage(QRandomStream ranStream, String nameContainsFilter) {
		
		String foundName = getRandomImageName( ranStream,  nameContainsFilter);
		return getImage(foundName);
	}
	
	
	private String getRandomImageName(QRandomStream ranStream, String nameContainsFilter) {
		
		if(nameContainsFilter == null) nameContainsFilter = "";
		if(nameContainsFilter == "") {
			int rnum = ranStream.randRangeInt(0, this.getNumItems()-1);
			return getShortNameOfItem(rnum);
		}
		
		ArrayList<String> filteredNamesFound = new ArrayList<String>();
		for (String thisName : directoryContentShortNameList) {
			if (thisName.contains(nameContainsFilter))
				filteredNamesFound.add(thisName);
		}
		
		int rnum = ranStream.randRangeInt(0, filteredNamesFound.size()-1);
		String foundName = filteredNamesFound.get(rnum);
		return foundName;
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
		//System.out.println("getImageSampleDescriptionFromGroup - numItemsInContentGroup " + groupName + " is " + numItemsInContentGroup);
		int itemNum = randomStream.randRangeInt(0, numItemsInContentGroup - 1);
		return new ImageSampleDescription(groupName, itemNum);

	}

}// end class ContentItemSelector
