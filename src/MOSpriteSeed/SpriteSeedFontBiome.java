package MOSpriteSeed;

import java.util.ArrayList;


import MOImageCollections.SpriteImageGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;
//
//
// contains a number of seed fonts in a probability stack (may also contain only one). The individual seed fonts within 
//  can be named individually, so as to identify sprites from  and treat different fonts
//

public class SpriteSeedFontBiome {

	
	SpriteImageGroupManager theImageSampleGroupManager;
	ArrayList<SpriteSeedFont> biomeItems = new ArrayList<SpriteSeedFont>();
	boolean probabilitiesNormalised = false;
	QRandomStream randomStream;
	
	
	public SpriteSeedFontBiome(int biomeRanSeed) {
		
		
		randomStream = new QRandomStream(biomeRanSeed);
	}

	public void addSpriteSeedFont( String sdFontName, String imageSampleGroupName, float sizeInScene, boolean useRelativeSizes, PVector origin, int fontRanSeed, float probability) {
		SpriteSeedFont seedFont = new SpriteSeedFont(sdFontName, imageSampleGroupName,  sizeInScene,  useRelativeSizes,  origin, fontRanSeed);
		seedFont.spriteSeedFontBiomeProbability = probability;
		biomeItems.add(seedFont);
		probabilitiesNormalised = false;
	}
	
	public void addSpriteSeedFont(SpriteSeedFont ssf) {
		biomeItems.add(ssf);
	}
	
	// uses stochastics to select a particular SpriteSeed
	public SpriteSeed getSpriteSeedInstance() {
		SpriteSeedFont ssf = getSpriteSeedFontInstance();
		return ssf.getRandomSpriteSeedInstance();
	}
	
	
	// uses stochastics to select a particular SpriteSeedFont
	public SpriteSeedFont getSpriteSeedFontInstance() {
		// called by the seed batch upon making a batch
		// only needs docPoint if an influenceImage is set
		if (probabilitiesNormalised == false) normaliseProbabilities();

		if (biomeItems.size() == 1) {
			return biomeItems.get(0);
		}

		
		float r = randomStream.randRangeF(0f, 1f);
		return getSpriteSeedFontFromProbabilityStack(r);
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	// private methods
	//

	private SpriteSeedFont getSpriteSeedFontFromProbabilityStack(float f) {
		float sumOfProbs = 0f;

		for (SpriteSeedFont isbi : biomeItems) {
			sumOfProbs += isbi.spriteSeedFontBiomeProbability;
			if (f <= sumOfProbs) {
				return isbi;
			}
		}
		// should not get here
		return null;
	}

	
	private void normaliseProbabilities() {

		float sumOfProbs = 0f;
		for (SpriteSeedFont isbi : biomeItems) {
			sumOfProbs += isbi.spriteSeedFontBiomeProbability;
		}

		for (SpriteSeedFont isbi : biomeItems) {
			isbi.spriteSeedFontBiomeProbability = isbi.spriteSeedFontBiomeProbability / sumOfProbs;
		}

		probabilitiesNormalised = true;
	}
	
}
