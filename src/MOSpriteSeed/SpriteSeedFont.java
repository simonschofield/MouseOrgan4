package MOSpriteSeed;


import MOImageCollections.SpriteImageGroup;
import MOImageCollections.SpriteImageGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOUtils.GlobalSettings;

////////////////////////////////////////////////////////////////////
// Can generate an instance of a SpriteSeed from the association of
// of an ISG with Size data
// 
// Does not have any positional data after generation, so this needs another process afterwards

// TBD impose constraints on the item chosen via regex
//
//
// whether its a biome or a singel SeedFont, the seedFontName is valid for this type of seed, the name extends to seedbatches as they have come from this font.


public class SpriteSeedFont {
	

	
	QRandomStream randomStream;
	
	// font data
	public String seedFontName = "";
	public String imageSampleGroupName;
	public float sizeInScene = 1;
	public boolean useRelativeSizes = false;
	public PVector origin = new PVector(0.5f, 0.5f);
	
	// only used when this SpriteSeedFont is within a SpriteSeedFontBiome
	public float spriteSeedFontBiomeProbability = 1f;
	
	private String instancenameMustContain = "";
	
	public SpriteSeedFont(String sdFontName, String imageSampleGroupName,
			float sizeInScene, boolean useRelativeSizes, PVector origin, int rseed) {
		
		randomStream = new QRandomStream(rseed);
		this.seedFontName = sdFontName;
		this.imageSampleGroupName = imageSampleGroupName;
		this.sizeInScene = sizeInScene;
		this.useRelativeSizes = useRelativeSizes;
		this.origin = origin;
	}
	
	public void setInstanceNameMustContain(String mustContain) {
		instancenameMustContain = mustContain;
	}

	public SpriteSeed getRandomSpriteSeedInstance() {
		int n = getRandomSpriteImageGroupItemNumber();
		
		return getSpriteSeedInstance(n);
	}
	
	public int getNumImages() {
		return getSpriteImageGroup().getNumImages();
		
	}
	
	public SpriteSeed getSpriteSeedInstance(int n) {
		SpriteSeed spriteSeed = new SpriteSeed();
		
		spriteSeed.spriteSeedFontName = seedFontName;
		spriteSeed.spriteImageGroupName = imageSampleGroupName;
		spriteSeed.sizeInScene = sizeInScene;
		spriteSeed.useRelativeSizes = useRelativeSizes;
		spriteSeed.origin = origin.copy();
		
		spriteSeed.spriteImageGroupItemNumber = n;
		spriteSeed.spriteImageGroupItemShortName = getSpriteImageGroup().getImageName(n);
		return spriteSeed;
	}
	
	
	private SpriteImageGroup getSpriteImageGroup() {
		//System.out.println("getSpriteImageGroup SprietImage group is " + imageSampleGroupName );
		return GlobalSettings.getTheSpriteImageGroupManager().getSpriteImageGroup(imageSampleGroupName);
	}
	
	
	
	private int getRandomSpriteImageGroupItemNumber() {
		if(instancenameMustContain==null) return randomStream.randRangeInt(0, getNumImages() - 1);
		if(instancenameMustContain.equals("")) return randomStream.randRangeInt(0, getNumImages() - 1);
		
	
		int bailCount = getNumImages()*10; // should be more than enough to find all samples randomly (see Coupon collector's problem)
		int num = 0;
		while(bailCount > 0) {
			num =  randomStream.randRangeInt(0, getNumImages() - 1);
			String shortName = getSpriteImageGroup().getImageName(num);
			if( shortName.contains(instancenameMustContain)) return num;
			bailCount--;
		}
		System.out.println("ERROR: SpriteSeedFont::getRandomSpriteImageGroupItemNumber cannot fins an image containing the string " + instancenameMustContain);
		return num;
	}
	
	
}
