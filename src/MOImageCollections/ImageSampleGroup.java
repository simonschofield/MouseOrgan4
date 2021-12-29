package MOImageCollections;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.ImageProcessing;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOUtils.MOUtilGlobals;
import MOUtils.GenericArrayListUtils;
import MOUtils.MOStringUtils;

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

public class ImageSampleGroup extends ImageItemGroup{
	// this is the global session scale as set by the userSession
	// it cannot be different between objects, so is a static
	static float sessionScale = 1;
	
	private String groupName = "";
	
	// groupSizeInScene defines the vertical size in the scene of all items in the group
	// If in 2D, then the unit is in documentSpace
	// if in 3D, then the unit is in 3D space measurements
	float groupSizeInScene = 1.0f;

	// if this is false, all items have the same size in scene
	// if this is set to true, then the size applies only to the largest vertically dimensioned item in the group;
	// all the other items are scaled by their relative pixel size to this item.
	boolean useIndividuaImageSizeInScene = false;
	
	
	PVector groupImageOrigins = new PVector(0.5f, 0.5f);
	
	public ImageSampleGroup(String name) {
		sessionScale = MOUtilGlobals.getSessionScale();
		groupName = name;
	}
	
	
	public ImageSampleGroup copy(String copyName) {
		ImageSampleGroup cpy = new ImageSampleGroup(copyName);
		
		
		cpy.widthExtrema = this.widthExtrema.copy();
		cpy.heightExtrema = this.heightExtrema.copy();

		
		cpy.cropRect = this.cropRect.copy();
		cpy.preScale = this.preScale;

		cpy.groupSizeInScene = this.getGroupSizeInScene();
		cpy.setGroupOrigins(this.groupImageOrigins);
		cpy.useIndividuaImageSizeInScene = this.useIndividuaImageSizeInScene;
		cpy.copyImagesFromOtherGroup(this);
		
		return cpy;
	}
	
	public boolean isNamed(String name) {
		if (groupName.contentEquals(name))
			return true;
		return false;
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	// This is a user-defined size that is used in both 2 and 3d situations.
	// In 2d context the units are in document space
	// in 3d context the units are in world space size
	// It is not until you call sprite.scaleToSizeInScene(...) that the value is used to actually scale the sprite
	public void setUseIndividuaImageSize(boolean useInividualHeights) {
		useIndividuaImageSizeInScene = useInividualHeights;
	}
	
	public void setGroupSizeInScene(float h) {
		groupSizeInScene = h;
	}

	public float getGroupSizeInScene() {
		return groupSizeInScene;

	}
	
	public PVector getGroupOrigin() {
		return groupImageOrigins;
	}
	
	public float getItemSizeInScene(int num) {
		// returns the size of the specific item in the scene
		// if useIndividualItemSizeInScene == true, then the group size is scaled against the relative
		// height of this item (i.e. item num) to the largest item in the group
		// if useIndividualItemSizeInScene == false then all the items' sizes are the same i.e. groupSizeInScene
		if (useIndividuaImageSizeInScene) {
			int imgHght = getImage(num).getHeight();
			float heightMaxItem = heightExtrema.getUpper();
			return groupSizeInScene * (imgHght / heightMaxItem);
		} else {
			return groupSizeInScene;
		}

	}
	
	public void setGroupOrigins(PVector orig) {
		groupImageOrigins = orig;
	}

	
	
	public void loadSamples() {
		// loadSamples is different to loadImages in that it applies the sessionScale to the image 
		// and deals with caching of sessionScaled versions of the images for subsequent uses, therefore speeding up the system.
		// 
		// pre-scaling and cropping only takes place after the cache save/load so is never committed to the cache
		// however preScaling and cropping takes place immediately per image load when loading images at full scale, or upon loading pre-cached images
		
		// If no session scale is applied (i.e.) the render is a Full-Size
		// just use the base-class load image. This will do preScaling and cropping if required
		if (sessionScale > 0.99) {
			// System.out.println("NOT using cache folder ...");

			// this base class method loads at 100% scale, and then does the preScaling and
			// rect cropping.
			super.loadImages();
			assertImageTYPE_INT_ARGB();
			return;
		}

		// if needs rescaling due to session scale, then the cache comes into play.
		// check to see if a folder called targetDirectory//cached_scaled_*percentile*
		// exists
		String cachedImagesFolderName = getCachedScaledImagesFolderName();
		// first get the list of files, as they should be, in the non-cache directory
		// (the original files in the sample lib)
		//ArrayList<String> sampleLibFileNames = getShortFileNamesInDirectory(directoryPath, fileStringEndsWith, fileStringContains);
				

				
		
		if (checkCacheOK(cachedImagesFolderName)) {
			// if the cache exists and  the images are all present in the cache then load those images using the base class load, end.
			// They are already scaled as that's the whole point of the cache...
			// The base class loadImage does any preScaling and cropping
			// System.out.println("loading from cache folder ..." + cachedImagesFolderName);
			DirectoryFileNameScanner cacheFileNameScanner = new DirectoryFileNameScanner(cachedImagesFolderName);
			cacheFileNameScanner.copyConstraints(directoryFileNameScanner);
			ArrayList<String> filesInCache = cacheFileNameScanner.getFullPathAndFileNamesList();
			super.loadImages(filesInCache);
			
			assertImageTYPE_INT_ARGB();
			return;
		}
		
		// If you get to here, then the cache needs making.  you need to 
		// 1/ load the full size image
		// 2/ apply theSessionScale, 
		// 3/ save this scaled version to the cache
		// 4/ apply any preScel or crop
		
		boolean ok = MOStringUtils.createDirectory(cachedImagesFolderName);
		if (!ok){
			// cannot create required cache folder
			System.out.println("problem creating cache folder ..." + cachedImagesFolderName);
			return;
		}
		
		ArrayList<String> filesToLoad = directoryFileNameScanner.getFullPathAndFileNamesList();		
		for(String thisImagePathAndName: filesToLoad) {
			loadFullSizeImage_SessionScale_Cache_PrescaleCrop(thisImagePathAndName, cachedImagesFolderName);
		}
		assertImageTYPE_INT_ARGB();
		
		// will write a comment to console if no images are loaded
		isLoaded();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	//content manipulation methods - affect all members of the group
	//
	public void scaleAll(float x, float y) {
		
		for (ImageItem imageSample: imageList) {
			imageSample.image = ImageProcessing.scaleImage(imageSample.image, x, y);
		}
	}

	public void rotateAll(float rot) {
		
		for (ImageItem imageSample: imageList) {
			imageSample.image = ImageProcessing.rotateImage(imageSample.image, rot);
		}
	}

	public void resizeToAll(int x, int y) {
		
		for (ImageItem imageSample: imageList) {
			imageSample.image = ImageProcessing.resizeTo(imageSample.image, x, y);
		}
	}

	public void cropAll(Rect cropRect) {
		if (cropRect.equals(new Rect())) return;
		
		for (ImageItem imageSample: imageList) {
			imageSample.image = ImageProcessing.cropImageWithNormalisedRect(imageSample.image, cropRect);
		}
	}

	public void addBoarderProportionAll(float left, float top, float right, float bottom) {
		//calculates the new additions as a proportion of the existing width or height

		for (ImageItem imageSample: imageList) {
			int w = imageSample.image.getWidth();
			int h = imageSample.image.getHeight();
			int leftAddition = (int) (w * left);
			int topAddition = (int) (h * top);
			int rightAddition = (int) (w * right);
			int bottomAddition = (int) (h * bottom);

			imageSample.image = ImageProcessing.addBoarder(imageSample.image, leftAddition, topAddition, rightAddition,bottomAddition);
		}

	}

	public void colorTransformAll(int function, float p1, float p2, float p3) {
		
		for (ImageItem imageSample: imageList) {
			imageSample.image = ImageProcessing.colorTransform(imageSample.image, function, p1, p2, p3);
		}
	}
	
	

	
	//////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Private methods below here
	//
	
	private void loadFullSizeImage_SessionScale_Cache_PrescaleCrop(String fullSizeImagePathAndName, String cacheDirectoryPath) {
		BufferedImage img = ImageProcessing.loadImage(fullSizeImagePathAndName);
		if(sessionScale != 1) {
			img = ImageProcessing.scaleImage(img,sessionScale,sessionScale);
		}
		String thisShortFileName = MOStringUtils.getShortFileNameFromFullPathAndFileName(fullSizeImagePathAndName);
		String fullCachePathAndName = cacheDirectoryPath + thisShortFileName + ".png";
		ImageProcessing.saveImage(fullCachePathAndName, img);
		
		
		// NOW apply preScale and crop if any
		img = applyPreScaleAndCrop(img);
		
		// NOW add the image to the list
		addImage( img, thisShortFileName);
	}
	
	
	
		
	
	
	private void assertImageTYPE_INT_ARGB() {
		// all image content items should be of type INT_ARGB for all the
		// operations to work OK. This makes sure they are.		
		for (ImageItem imageSample : imageList) {
			if (imageSample.image.getType() != BufferedImage.TYPE_INT_ARGB) {
				imageSample.image = ImageProcessing.convertColorModel(imageSample.image, BufferedImage.TYPE_INT_ARGB);
			}
		}
	}
	
	
	////////////
	private String getCachedScaledImagesFolderName() {
		int scalePercentile = (int) (sessionScale * 100);
		String cachFolderRoot = MOUtilGlobals.mouseOrganImageCachePath;
		String originalTargetDirectory = directoryFileNameScanner.getTargetDirectory();
		String strippedDirectoryPath = originalTargetDirectory.replace(MOUtilGlobals.sampleLibPath, "");
		// System.out.println("directoryPath ..." + directoryPath);
		// System.out.println("strippedDirectoryPath ..." + strippedDirectoryPath);
		String cachedFolderName = cachFolderRoot + strippedDirectoryPath + "\\cached_scaled_" + scalePercentile + "\\";
		
		System.out.println("creating cach folder ..." + cachedFolderName);
		return cachedFolderName;
	}
	
	// this checks to see if the directory exists, and if it does then it checks to
	// see if all the cached files exist also.
	// if not then return false.
	private boolean checkCacheOK(String cacheDirectory) {
		if (MOStringUtils.checkDirectoryExist(cacheDirectory) == false)
			return false;
		
		ArrayList<String> expectedFiles = directoryFileNameScanner.getShortNameList();
		
		DirectoryFileNameScanner cacheFileNameScanner = new DirectoryFileNameScanner(cacheDirectory);
		ArrayList<String> filesInCache = cacheFileNameScanner.getShortNameList();

		if (GenericArrayListUtils.listsAContainedInB(expectedFiles, filesInCache))
			return true;

		return false;

	}
	
}// end of class
	
	









/*





//////////////////////////////////////////////////////////////////////////////////////////////
//
// Values set by the user in the group to establish size and origin of image when used in a sprite context

	



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
		String foundName = getRandomImageName(ranStream, nameContainsFilter);
		int imgNum = getNumOfImageShortName(foundName);
		return getSprite(imgNum);
	}

	BufferedImage getRandomImage(QRandomStream ranStream, String nameContainsFilter) {

		String foundName = getRandomImageName(ranStream, nameContainsFilter);
		return getImage(foundName);
	}

	private String getRandomImageName(QRandomStream ranStream, String nameContainsFilter) {

		if (nameContainsFilter == null)
			nameContainsFilter = "";
		if (nameContainsFilter == "") {
			int rnum = ranStream.randRangeInt(0, this.getNumItems() - 1);
			return getShortNameOfItem(rnum);
		}

		ArrayList<String> filteredNamesFound = new ArrayList<String>();
		for (String thisName : directoryContentShortNameList) {
			if (thisName.contains(nameContainsFilter))
				filteredNamesFound.add(thisName);
		}

		int rnum = ranStream.randRangeInt(0, filteredNamesFound.size() - 1);
		String foundName = filteredNamesFound.get(rnum);
		return foundName;
	}

	
}

*/