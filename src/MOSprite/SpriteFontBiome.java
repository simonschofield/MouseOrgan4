package MOSprite;

import java.util.ArrayList;

import MOImageCollections.ScaledMOImageGroupManager;
import MOMaths.PVector;
import MOMaths.QRandomStream;
//
//
// contains a number of seed fonts in a probability stack (may also contain only one). The individual seed fonts within 
//  can be named individually, so as to identify sprites from  and treat different fonts
//

public class SpriteFontBiome  implements SpriteSourceInterface{

	
	ScaledMOImageGroupManager theImageSampleGroupManager;
	ArrayList<SpriteFont> biomeItems = new ArrayList<SpriteFont>();
	boolean probabilitiesNormalised = false;
	QRandomStream randomStream;
	
	
	public SpriteFontBiome(int biomeRanSeed) {
		
		
		randomStream = new QRandomStream(biomeRanSeed);
	}

	public void addSpriteFont( String sdFontName, String imageSampleGroupName, float sizeInScene, boolean useRelativeSizes, PVector origin, int fontRanSeed, float probability) {
		SpriteFont spriteFont = new SpriteFont(sdFontName, imageSampleGroupName,  sizeInScene,  useRelativeSizes,  origin, fontRanSeed);
		spriteFont.SpriteFontBiomeProbability = probability;
		biomeItems.add(spriteFont);
		probabilitiesNormalised = false;
	}
	
	public void addSpriteFont(SpriteFont ssf) {
		biomeItems.add(ssf);
	}
	
	// uses stochastics to select a particular SpriteData
	public SpriteData getSpriteDataInstance() {
		SpriteFont ssf = getSpriteFontInstance();
		return ssf.getSpriteDataInstance();
	}
	
	public Sprite getSpriteInstance() {
		SpriteFont ssf = getSpriteFontInstance();
		return ssf.getSpriteInstance();
	}
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////
	// private methods
	//
	
	// uses stochastics to select a particular SpriteSeedFont
	private SpriteFont getSpriteFontInstance() {
		// called by the seed batch upon making a batch
		// only needs docPoint if an influenceImage is set
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
	
}
