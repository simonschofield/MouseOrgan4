package MOSpriteSeed;

import MOImageCollections.ImageSampleGroup;
import MOImageCollections.ImageSampleGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;

////////////////////////////////////////////////////////////////////
// Can generate an instance of a SpriteSeed from the association of
// of an ISG with Size data
// 
// Does not have any positional data after generation, so this needs another process afterwards

// TBD impose constraints on the item chosen via regex
//
//
public class SpriteSeedFont {
	

	ImageSampleGroupManager isgManager;
	QRandomStream randomStream;
	
	// font data
	public String seedFontName = "";
	public String imageSampleGroupName;
	public float sizeInScene = 1;
	public boolean useRelativeSizes = false;
	public PVector origin = new PVector(0.5f, 0.5f);
	
	public float spriteSeedFontBiomeProbability = 1f;
	
	public SpriteSeedFont(ImageSampleGroupManager isgManager, String sdFontName, String imageSampleGroupName,
			float sizeInScene, boolean useRelativeSizes, PVector origin) {
		
		randomStream = new QRandomStream(1);
		this.isgManager = isgManager;
		this.seedFontName = sdFontName;
		this.imageSampleGroupName = imageSampleGroupName;
		this.sizeInScene = sizeInScene;
		this.useRelativeSizes = useRelativeSizes;
		this.origin = origin;
	}
	
	SpriteSeed getRandomSpriteSeedInstance() {
		int n = getRandomImageSampleGroupItemNumber();
		return getSpriteSeedInstance(n);
	}
	
	SpriteSeed getSpriteSeedInstance(int n) {
		SpriteSeed spriteSeed = new SpriteSeed();
		spriteSeed.seedFontName = seedFontName;
		spriteSeed.imageSampleGroupName = imageSampleGroupName;
		spriteSeed.sizeInScene = sizeInScene;
		spriteSeed.useRelativeSizes = useRelativeSizes;
		spriteSeed.origin = origin.copy();
		
		spriteSeed.imageSampleGroupItemNumber = n;
		spriteSeed.imageSampleGroupItemShortName = getImageSampleGroup().getImageName(n);
		return spriteSeed;
	}
	
	
	private ImageSampleGroup getImageSampleGroup() {
		return isgManager.getImageSampleGroup(imageSampleGroupName);
	}
	
	
	
	private int getRandomImageSampleGroupItemNumber() {
		int numItems = getImageSampleGroup().getNumImages();
		//System.out.println("getImageSampleDescriptionFromGroup - numItemsInContentGroup " + groupName + " is " + numItemsInContentGroup);
		return randomStream.randRangeInt(0, numItems - 1);
	}
}
