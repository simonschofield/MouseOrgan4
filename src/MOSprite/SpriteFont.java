package MOSprite;


import java.awt.image.BufferedImage;

import MOImageCollections.ScaledImageAssetGroup;
import MOImageCollections.ScaledImageAssetGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Range;
import MOUtils.GlobalSettings;

////////////////////////////////////////////////////////////////////
// Can generate an instance of a Sprite from its association with the SessionScaledImageGroup
// If it is generated by a SpriteSeed then this will have positional data so is complete
// If not, then the positional data needs to be set afterwards


public class SpriteFont implements SpriteSourceInterface{
	

	
	QRandomStream randomStream;
	
	// font data
	public String thisSpriteFontName = "";
	public String imageSampleGroupName;
	public float sizeInScene = 1;
	public boolean useRelativeSizes = false;
	public PVector spritePivotPoint = new PVector(0.5f, 0.5f);
	
	// only used when this SpriteFont is within a SpriteFontBiome
	public float SpriteFontBiomeProbability = 1f;
	
	private String instancenameMustContain = "";
	private Range hueMustBeBetween;
	
	boolean quickRenderMode = false;
	
	public SpriteFont(String thisSpriteFontName, String imageSampleGroupName,
			float sizeInScene, boolean useRelativeSizes, PVector pivotPoint, int rseed) {
		
		randomStream = new QRandomStream(rseed);
		this.thisSpriteFontName = thisSpriteFontName;
		this.imageSampleGroupName = imageSampleGroupName;
		this.sizeInScene = sizeInScene;
		this.useRelativeSizes = useRelativeSizes;
		this.spritePivotPoint = pivotPoint;
	}
	
	public void setQuickRenderMode(boolean b) {
		quickRenderMode = b;
		
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
	

	@Override
	public void setRandomStreamKeyPosition(int keyPos) {
		randomStream.startKeyedPosition(keyPos);
	}

	//////////////////////////////////////////////////////////////////////////
	// getting Sprite instances using stochastics
	//
	public Sprite getSpriteInstance(boolean setRandomStreamKeyPositionWithID) {
		// generates a sprite with image selected from the SpriteFont, sprite font data
		// but no positional data
		Sprite sprite = new Sprite();
		if(setRandomStreamKeyPositionWithID) setRandomStreamKeyPosition(sprite.randomKey);
		sprite.setSpriteFontDataAndSelectImage(this);
		return sprite;
	}
	
	
	public Sprite getSpriteInstance(SpriteSeed seed, boolean setRandomStreamKeyPositionWithID) {
		// generates the complete sprite with image selected from the SpriteFont, sprite font data
		// and positional data from he SpriteSeed
		if(setRandomStreamKeyPositionWithID) setRandomStreamKeyPosition(seed.getRandomKey());

		Sprite sprite = new Sprite(seed);
		
		sprite.setSpriteFontDataAndSelectImage(this);
		
		return sprite;
	}
	

	public int getNumImages() {
		return getSpriteImageGroup().getNumImageAssets();
		
	}
	
	
	protected ScaledImageAssetGroup getSpriteImageGroup() {
		//System.out.println("getSpriteImageGroup SpriteImage group is " + imageSampleGroupName );
		
		ScaledImageAssetGroup group =  GlobalSettings.getImageAssetGroupManager().getScaledImageAssetGroup(imageSampleGroupName);
		//System.out.println("getSpriteImageGroup SpriteImage group is " + imageSampleGroupName );
		return group;
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
			String shortName = getSpriteImageGroup().getImageAssetName(num);
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
			float hue = getSpriteImageGroup().getImageAsset(num).stats.dominantHue;
			if( hueMustBeBetween.isBetweenInc(hue)) return num;
			bailCount--;
		}
		System.out.println("ERROR: SpriteDataFont::getRandomSpriteImageGroupItemNumber_HueConstraint cannot fins an image containing a dominant hue between " + hueMustBeBetween.toStr());
		return num;
		
	}
	
	

	
	
	ScaledImageAssetGroup getScaledImageAssetGroup() {
			ScaledImageAssetGroupManager sigm = GlobalSettings.getImageAssetGroupManager();
			if(sigm==null) System.out.println("ERROR Sprite::SpriteImageGroupManager == null");
			return sigm.getScaledImageAssetGroup(this.imageSampleGroupName);
		}
		
	}
	

