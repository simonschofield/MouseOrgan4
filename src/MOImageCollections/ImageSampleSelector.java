package MOImageCollections;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

import MOImage.ImageProcessing;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.RandomStream;
import MOMaths.Rect;
import MOSceneData.SceneData3D;
import MOUtils.Counter;
import MOUtils.GenericArrayListUtils;
import MOUtils.MOStringUtils;



/////////////////////////////////////////////////////////////////////////////
//given a number of ContentItemProbability types
//select one using randomness
//
// ContentItemProbabilities are added to a list. Their combined probabilities are
// normalised 0..1, and form a "probability stack". A random number 0..1 is generated and
// and the item at that point in the probability stack gets selected.
//
// Experimental: an influencer image can be added to influence the decision
// 
public class ImageSampleSelector {

	// inner class ContentItemProbability
	class ImageSampleGroupProbability {
		// class used only by ContentItemSelector
		String imageSampleGroupName;
		float probability;

		public ImageSampleGroupProbability(String name, float prob) {
			imageSampleGroupName = name;
			probability = prob;
		}
	}

	ImageSampleGroupManager theImageSampleGroupManager;
	ArrayList<ImageSampleGroupProbability> groupProbabilities = new ArrayList<ImageSampleGroupProbability>();
	boolean normalised = false;
	RandomStream randomStream;

	

	public ImageSampleSelector(ImageSampleGroupManager imageSampleGroupManager, int rseed) {
		randomStream = new RandomStream(rseed);
		theImageSampleGroupManager = imageSampleGroupManager;
	}

	

	// could pass in the image collection instead to get the name and numItems
	public void addContentItemProbability(String imageSampleGroupName, float prob) {

		ImageSampleGroupProbability cip = new ImageSampleGroupProbability(imageSampleGroupName, prob);
		groupProbabilities.add(cip);
	}
	
	public void clearContentItemProbabilities() {
		groupProbabilities.clear();
	}

	void normaliseProbabilities() {

		float sumOfProbs = 0f;
		for (ImageSampleGroupProbability cip : groupProbabilities) {
			sumOfProbs += cip.probability;
		}

		for (ImageSampleGroupProbability cip : groupProbabilities) {
			cip.probability = cip.probability / sumOfProbs;
		}

		normalised = true;
	}
	
	public int getNumCandidateSampleGroups() {
		return groupProbabilities.size();
	}

	public ImageSampleDescription selectImageSampleDescription() {
		// called by the seed batch upon making a batch
		// only needs docPoint if an influenceImage is set
		if (normalised == false)
			normaliseProbabilities();

		if (groupProbabilities.size() == 1) {
			return getImageSampleDescriptionFromGroup(groupProbabilities.get(0).imageSampleGroupName);
		}

		
			float r = randomStream.randRangeF(0f, 1f);
			return getImageSampleDescriptionFromProbabilityStack(r);
	}

	

	private ImageSampleDescription getImageSampleDescriptionFromProbabilityStack(float f) {
		float sumOfProbs = 0f;

		for (ImageSampleGroupProbability cip : groupProbabilities) {
			sumOfProbs += cip.probability;
			if (f <= sumOfProbs) {
				return getImageSampleDescriptionFromGroup(cip.imageSampleGroupName);
			}
		}
		// should not get here
		return new ImageSampleDescription("", 0, "");
	}

	private ImageSampleDescription getImageSampleDescriptionFromGroup(String groupName) {
		ImageSampleGroup thisGroup = theImageSampleGroupManager.getImageSampleGroup(groupName);
		if(thisGroup==null) {
			System.out.println("ImageSampleSelector::getImageSampleDescriptionFromGroup - cannoyt find an ImageSampleGroup in the Manager called " + groupName );
			return null;
		}
		int numItems = thisGroup.getNumImages();
		//System.out.println("getImageSampleDescriptionFromGroup - numItemsInContentGroup " + groupName + " is " + numItemsInContentGroup);
		int itemNum = randomStream.randRangeInt(0, numItems - 1);
		
		String shortName = thisGroup.getImageName(itemNum);
		
		
		
		return new ImageSampleDescription(groupName, itemNum, shortName);

	}

}// end class ContentItemSelector














