package MOSprite;


import MOImageCollections.ScaledMOImageGroup;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Range;
import MOUtils.GlobalSettings;

////////////////////////////////////////////////////////////////////
// Can generate an instance of a Sprite or SpriteData from its association with the SessionScaledImageGroup
// 
// Does not have any positional data after generation, so this needs another process afterwards

// 
//
//
// whether its a biome or a single SeedFont, the seedFontName is valid for this type of seed, the name extends to seedbatches as they have come from this font.


public class SpriteFont implements SpriteSourceInterface{
	

	
	QRandomStream randomStream;
	
	// font data
	public String seedFontName = "";
	public String imageSampleGroupName;
	public float sizeInScene = 1;
	public boolean useRelativeSizes = false;
	public PVector origin = new PVector(0.5f, 0.5f);
	
	// only used when this SpriteDataFont is within a SpriteDataFontBiome
	public float SpriteFontBiomeProbability = 1f;
	
	private String instancenameMustContain = "";
	private Range hueMustBeBetween;
	
	
	public SpriteFont(String sdFontName, String imageSampleGroupName,
			float sizeInScene, boolean useRelativeSizes, PVector origin, int rseed) {
		
		randomStream = new QRandomStream(rseed);
		this.seedFontName = sdFontName;
		this.imageSampleGroupName = imageSampleGroupName;
		this.sizeInScene = sizeInScene;
		this.useRelativeSizes = useRelativeSizes;
		this.origin = origin;
	}
	
	public void setInstanceNameMustContain(String mustContain) {
		hueMustBeBetween = null;
		instancenameMustContain = mustContain;
	}
	
	
	public void setInstanceMustHaveHueBetween(float lo, float hi) {
		instancenameMustContain = "";
		hueMustBeBetween = new Range(lo,hi);
		getSpriteImageGroup().calculateImageStats();
	}
	
	
	public void clearConstraints() {
		instancenameMustContain = "";
		hueMustBeBetween = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// getting SpriteData and Sprite instances using stochastics
	//
	public SpriteData getSpriteDataInstance() {
		int n = getRandomSpriteImageGroupItemNumber();
		return getSpriteDataInstance(n);
	}

	public Sprite getSpriteInstance() {
		int n = getRandomSpriteImageGroupItemNumber();
		return getSpriteInstance(n);
	}
	
	// if you wan to guarantee the exact same instance result, regardless of previous calls,
	// set the state with a reliable integer. e.g. the uniqueID of the sprite data, or the number of the polygon in a list your are compositing.
	// The same integer will always generate the same random result. 
	public void setRandomState(int i) {
		randomStream.setState(i);
	}
	
	//////////////////////////////////////////////////////////////////////////
	// getting specific SpriteData and Sprite instances based on their SpriteImageGroup number
	//
	public SpriteData getSpriteDataInstance(int n) {
		if(n >= getNumImages()) {
			System.out.println("SpriteDataFont::getSpriteDataInstance number asked for isout of index " + n + " out of maximum " + getNumImages());
			return null;
		}
		SpriteData SpriteData = new SpriteData();
		
		SpriteData.SpriteFontName = seedFontName;
		SpriteData.ImageGroupName = imageSampleGroupName;
		SpriteData.sizeInScene = sizeInScene;
		SpriteData.useRelativeSizes = useRelativeSizes;
		SpriteData.origin = origin.copy();
		
		SpriteData.ImageGroupItemNumber = n;
		SpriteData.ImageGroupItemShortName = getSpriteImageGroup().getImageName(n);
		return SpriteData;
	}
	
	
	public Sprite getSpriteInstance(int n) {
		SpriteData s = getSpriteDataInstance(n);
		return new Sprite(s);
	}
	
	public int getNumImages() {
		return getSpriteImageGroup().getNumMOImages();
		
	}
	
	
	protected ScaledMOImageGroup getSpriteImageGroup() {
		//System.out.println("getSpriteImageGroup SprietImage group is " + imageSampleGroupName );
		return GlobalSettings.getTheSpriteImageGroupManager().getMOImageGroup(imageSampleGroupName);
	}
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// private
	//
	
	
	protected int getRandomSpriteImageGroupItemNumber() {
		
		if(instancenameMustContain.length()>0) return getRandomSpriteImageGroupItemNumber_NameConstraint();
		if(hueMustBeBetween!=null) return getRandomSpriteImageGroupItemNumber_HueConstraint();
		
		return randomStream.randRangeInt(0, getNumImages() - 1);
		
	}
	
	
	
	protected int getRandomSpriteImageGroupItemNumber_NameConstraint() {
		int bailCount = getNumImages()*10; // should be more than enough to find all samples randomly (see Coupon collector's problem)
		int num = 0;
		while(bailCount > 0) {
			num =  randomStream.randRangeInt(0, getNumImages() - 1);
			String shortName = getSpriteImageGroup().getImageName(num);
			if( shortName.contains(instancenameMustContain)) return num;
			bailCount--;
		}
		System.out.println("ERROR: SpriteDataFont::getRandomSpriteImageGroupItemNumber_NameConstraint cannot fins an image with name containing the string " + instancenameMustContain);
		return num;
		
	}
	
	protected int getRandomSpriteImageGroupItemNumber_HueConstraint() {
		int bailCount = getNumImages()*10; // should be more than enough to find all samples randomly (see Coupon collector's problem)
		int num = 0;
		while(bailCount > 0) {
			num =  randomStream.randRangeInt(0, getNumImages() - 1);
			float hue = getSpriteImageGroup().getMOImage(num).stats.dominantHue;
			if( hueMustBeBetween.isBetweenInc(hue)) return num;
			bailCount--;
		}
		System.out.println("ERROR: SpriteDataFont::getRandomSpriteImageGroupItemNumber_HueConstraint cannot fins an image containing a dominant hue between " + hueMustBeBetween.toStr());
		return num;
		
	}
	
	
}
