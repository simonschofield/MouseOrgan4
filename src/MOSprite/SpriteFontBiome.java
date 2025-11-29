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

public class SpriteFontBiome{

	String spriteFontBiomeName;

	ScaledImageAssetGroupManager theImageSampleGroupManager;
	ArrayList<SpriteFont> biomeItems = new ArrayList<>();
	boolean probabilitiesNormalised = false;
	QRandomStream randomStream;

	boolean quickRenderMode = false;

	public SpriteFontBiome(String name, int biomeRanSeed) {

		spriteFontBiomeName = name;
		randomStream = new QRandomStream(biomeRanSeed);
	}

	public void addSpriteFont(  String imageSampleGroupName, float sizeInScene, float relativeSizeEQ , PVector pivotPoint, int fontRanSeed, float probability) {
		SpriteFont spriteFont = new SpriteFont(spriteFontBiomeName, imageSampleGroupName,  sizeInScene,  relativeSizeEQ,  pivotPoint, fontRanSeed);
		spriteFont.SpriteFontBiomeProbability = probability;
		biomeItems.add(spriteFont);
		probabilitiesNormalised = false;
	}

	public void addSpriteFont(SpriteFont ssf) {
		biomeItems.add(ssf);
	}


	public Sprite getSpriteInstance(boolean setRandomStreamKeyPositionWithID) {
		SpriteFont spriteFont = getRandomSpriteFontInstance();
		Sprite s =  spriteFont.getSpriteInstance(setRandomStreamKeyPositionWithID);


		return s;
	}



	public void addImageData(Sprite s, boolean setRandomStreamKeyPositionWithID)  {


		//if(s.getID() == 18) {


		//	System.out.println("Sprite ID 18 has a random key of " + s.randomKey);

		//}



		if(setRandomStreamKeyPositionWithID) {
			setRandomStreamKeyPosition(s.randomKey);
		}
		SpriteFont spriteFont = getRandomSpriteFontInstance();
		spriteFont.setRandomStreamKeyPosition(s.randomKey);
		s.setSpriteFontDataAndSelectImage(spriteFont);
	}




	///////////////////////////////////////////////////////////////////////////////
	// private methods
	//

	// uses stochastics to select a particular SpriteSeedFont
	private SpriteFont getRandomSpriteFontInstance() {
		// called by the seed batch upon making a batch
		// only needs docPoint if an influenceImage is set
		//System.out.println("SpriteFontBiome::getSpriteFontInstance ... num biomeItems = " + biomeItems.size());
		if (!probabilitiesNormalised) {
			normaliseProbabilities();
		}

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


	public void setRandomStreamKeyPosition(int keyPos) {
		randomStream.startKeyedPosition(keyPos);
	}



}
