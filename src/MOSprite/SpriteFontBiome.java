package MOSprite;

import java.util.ArrayList;

import MOImageCollections.ScaledImageAssetGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;
//
//
//  contains a number of SpriteFonts in a probability stack (may also contain only one). The individual seed fonts within 
//  can be named individually, so as to identify sprites from  and treat different fonts
//

public class SpriteFontBiome  implements SpriteSourceInterface{

	String spriteFontBiomeName;
	
	ScaledImageAssetGroupManager theImageSampleGroupManager;
	ArrayList<SpriteFont> biomeItems = new ArrayList<SpriteFont>();
	boolean probabilitiesNormalised = false;
	QRandomStream randomStream;
	
	boolean quickRenderMode = false;
	
	public SpriteFontBiome(String name, int biomeRanSeed) {
		
		spriteFontBiomeName = name;
		randomStream = new QRandomStream(biomeRanSeed);
	}

	public void addSpriteFont(  String imageSampleGroupName, float sizeInScene, boolean useRelativeSizes, PVector pivotPoint, int fontRanSeed, float probability) {
		SpriteFont spriteFont = new SpriteFont(spriteFontBiomeName, imageSampleGroupName,  sizeInScene,  useRelativeSizes,  pivotPoint, fontRanSeed);
		spriteFont.SpriteFontBiomeProbability = probability;
		biomeItems.add(spriteFont);
		probabilitiesNormalised = false;
	}
	
	public void addSpriteFont(SpriteFont ssf) {
		biomeItems.add(ssf);
	}
	

	public Sprite getSpriteInstance(boolean setRandomStreamKeyPositionWithID) {
		SpriteFont spriteFont = getSpriteFontInstance();
		Sprite s =  spriteFont.getSpriteInstance(setRandomStreamKeyPositionWithID);
		
		
		return s;
	}
	
	
	public Sprite getSpriteInstance(SpriteSeed s, boolean setRandomStreamKeyPositionWithID)  {
		if(setRandomStreamKeyPositionWithID) setRandomStreamKeyPosition(s.getRandomKey());
		SpriteFont spriteFont = getSpriteFontInstance();
		Sprite sprite =  spriteFont.getSpriteInstance(s, true);// could be false
		
		
		
		//System.out.println("randomKey of " + s.getRandomKey() + " results in image " + sprite.getImageName());
		return sprite;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	// private methods
	//
	
	// uses stochastics to select a particular SpriteSeedFont
	private SpriteFont getSpriteFontInstance() {
		// called by the seed batch upon making a batch
		// only needs docPoint if an influenceImage is set
		//System.out.println("SpriteFontBiome::getSpriteFontInstance ... num biomeItems = " + biomeItems.size());
		if (probabilitiesNormalised == false) normaliseProbabilities();

		if (biomeItems.size() == 1) {
			return biomeItems.get(0);
		}

		
		float r = randomStream.randRangeF(0f, 1f);
		return getSpriteFontFromProbabilityStack(r);
	}

	private SpriteFont getSpriteFontFromProbabilityStack(float f) {
		float sumOfProbs = 0f;

		for (SpriteFont isbi : biomeItems) {
			sumOfProbs += isbi.SpriteFontBiomeProbability;
			if (f <= sumOfProbs) {
				return isbi;
			}
		}
		// should not get here
		return null;
	}

	
	private void normaliseProbabilities() {

		float sumOfProbs = 0f;
		for (SpriteFont isbi : biomeItems) {
			sumOfProbs += isbi.SpriteFontBiomeProbability;
		}

		for (SpriteFont isbi : biomeItems) {
			isbi.SpriteFontBiomeProbability = isbi.SpriteFontBiomeProbability / sumOfProbs;
		}

		probabilitiesNormalised = true;
	}

	@Override
	public void setRandomStreamKeyPosition(int keyPos) {
		randomStream.startKeyedPosition(keyPos);
	}
	
	
	
}
