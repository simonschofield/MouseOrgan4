package MOAppSessionHelpers;
// to replace the need for sizeInScene, origin and useRelativeSize in the imageSampleGroup (ISG) (where they have no real place)
// and will also replace the ImageSampleSelector class
// Also replace the getSprite() functionality in the ISGManager 
// It also has various methods to randomly select from the ISGs (need to add name filter functionality)
// "Biome" is not a great description. Often the user will only need one ISG in the biome, so then the purpose becomes to 
// associate an ISG with a size in the scene.
// Maybe ImageSpriteImageSource, ImageSampleSpriteSelector

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOCompositing.ImageSprite;
import MOImageCollections.ImageSampleDescription;
import MOImageCollections.ImageSampleGroup;
import MOImageCollections.ImageSampleGroupManager;

import MOMaths.PVector;
import MOMaths.RandomStream;

// A ISBiome associates ISG with size and origin data, and also does the stochastics in selecting and delivering the ImageBuffer, with size and origin etc to the sprite.

// this class is used in to ways. 
// 1/ as a table of the ISG and their sizes added to the biome, 
// 2/ as the return type from selecting a specific item from this ISG, whihc includes the BufferedImage
//
//
class SpriteImageSampleGroup {
	// Interface between ImageSampleGroup and Sprite
	// Associates size and origin with an ISG
	// Stores the lightweight data only for a sprite, so is suitable for making into seeds
	
	// can be used individually to create sprites from an ISG
	// or they can be added to a SpriteImageSampleBiome, used in seed manufacture
	static ImageSampleGroupManager theImageSampleGroupManager;
	
	
	String imageSampleGroupName;
	float sizeInScene = 1;
	boolean useRelativeSizes = false;
	PVector origin = new PVector(0.5f, 0.5f);
	
	// used in a specific item instance
	int indexNum = 0;
	String shortName;
	
	// used in the context of a biome
	String imageSampleBiomeItemName;
	public float biomeProbability = 1;

	public SpriteImageSampleGroup(String imageSampleBiomeNm, String isgName, float sizeInScn, boolean useRelSizes, PVector orig, float prob) {
		
		imageSampleBiomeItemName = imageSampleBiomeNm;
		imageSampleGroupName = isgName;
		sizeInScene = sizeInScn;
		useRelativeSizes = useRelSizes;
		origin = orig.copy();
		biomeProbability = prob;
	}
	
	public SpriteImageSampleGroup copy() {
		return new SpriteImageSampleGroup(this.imageSampleBiomeItemName, this.imageSampleGroupName, this.sizeInScene, this.useRelativeSizes, this.origin, this.biomeProbability);
	}
	
	// this needs to be called once somewhere before you actually create a sprite
	public void setImageSampleGroupManager(ImageSampleGroupManager isgManager) {
		theImageSampleGroupManager = isgManager;
	}
	
	public ImageSampleGroup getImageSampleGroup() {
		return theImageSampleGroupManager.getImageSampleGroup(imageSampleGroupName);
	}
	
	public String getShortImageName(int n) {
		return getImageSampleGroup().getImageName(n);
		
	}
	
	public ImageSprite getSprite(int n) {
		if(theImageSampleGroupManager==null) {
			System.out.println("SpriteImageSampleGroup::getSprite - the theImageSampleGroupManager has not been set, returning null");
			return null;
		}
		SpriteImageSampleGroup instance = getSpriteImageSampleInstance(n);
		BufferedImage  image = getImageSampleGroup().getImage(n);
		// make the sprite now....
		return null;
	}
	
	// returns all the lightweight data in the parent PLUS a specific number and name of a single ImageSample
	// This can then be used to either make a sprite or a seed
	public SpriteImageSampleGroup getSpriteImageSampleInstance(int n) {
		SpriteImageSampleGroup instance = copy();
		instance.shortName = instance.getImageSampleGroup().getImageName(n);
		instance.indexNum = n;
		return instance;
	}
	
	
}



// this is a collection of SpriteImageSampleGroups in a probability stack 
public class SpriteImageSampleBiome {
	
	
	
	ImageSampleGroupManager theImageSampleGroupManager;
	ArrayList<SpriteImageSampleGroup> biomeItems = new ArrayList<SpriteImageSampleGroup>();
	boolean probabilitiesNormalised = false;
	RandomStream randomStream;
	
	
	public void addImageSampleBiomeItem(String imageSampleBiomeNm, String isgName, float sizeInScn, boolean useRelSizes, PVector orig, float prob) {
		SpriteImageSampleGroup isbItem = new SpriteImageSampleGroup(imageSampleBiomeNm, isgName,  sizeInScn,  useRelSizes,  orig,  prob);
		biomeItems.add(isbItem);
		probabilitiesNormalised = false;
	}
	
	public SpriteImageSampleGroup selectImageSampleBiomeItem() {
		// called by the seed batch upon making a batch
		// only needs docPoint if an influenceImage is set
		if (probabilitiesNormalised == false)
			normaliseProbabilities();

		if (biomeItems.size() == 1) {
			return getImageSampleBiomeItemFromGroup(biomeItems.get(0).imageSampleGroupName);
		}

		
			float r = randomStream.randRangeF(0f, 1f);
			return getImageSampleBiomeItemFromProbabilityStack(r);
	}
	
	
	
	ImageSprite convertToImageSprite(SpriteImageSampleGroup isbi) {
		
		return null;
	}

	

	private SpriteImageSampleGroup getImageSampleBiomeItemFromProbabilityStack(float f) {
		float sumOfProbs = 0f;

		for (SpriteImageSampleGroup isbi : biomeItems) {
			sumOfProbs += isbi.biomeProbability;
			if (f <= sumOfProbs) {
				return getImageSampleBiomeItemFromGroup(isbi.imageSampleBiomeItemName);
			}
		}
		// should not get here
		return null;
	}

	private SpriteImageSampleGroup getImageSampleBiomeItemFromGroup(String groupName) {
		ImageSampleGroup thisGroup = theImageSampleGroupManager.getImageSampleGroup(groupName);
		if(thisGroup==null) {
			System.out.println("ImageSampleSelector::getImageSampleDescriptionFromGroup - cannot find an ImageSampleGroup in the Manager called " + groupName );
			return null;
		}
		int numItems = thisGroup.getNumImages();
		//System.out.println("getImageSampleDescriptionFromGroup - numItemsInContentGroup " + groupName + " is " + numItemsInContentGroup);
		int itemNum = randomStream.randRangeInt(0, numItems - 1);
		
		String shortName = thisGroup.getImageName(itemNum);
		
		return null;
		
		//return new ImageSampleDescription(groupName, itemNum, shortName);

	}
	
	
	
	private SpriteImageSampleGroup getImageSampleBiomeItem(String imageSampleBiomeItemName) {
		
		return null;
	}
	
	
	
	
	private void normaliseProbabilities() {

		float sumOfProbs = 0f;
		for (SpriteImageSampleGroup isbi : biomeItems) {
			sumOfProbs += isbi.biomeProbability;
		}

		for (SpriteImageSampleGroup isbi : biomeItems) {
			isbi.biomeProbability = isbi.biomeProbability / sumOfProbs;
		}

		probabilitiesNormalised = true;
	}
	
	
	
}
