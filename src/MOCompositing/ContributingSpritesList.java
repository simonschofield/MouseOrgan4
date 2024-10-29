package MOCompositing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import MOMaths.PVector;

import MOSprite.SpriteSeed;
import MOSprite.SpriteSeedBatch;
import MOUtils.KeyValuePairList;
/////////////////////////////////////////////////////////////////////////////////////////////////
// This is used to speed up the rendering process of ROIs within a previously larger image,
// by culling non-contributing SpriteData (seeds).
// The sprite crop decision and contribution result is registered and saved by the RenderBoarder class. However, for ease of use, this functionality
// is wrapped inside SpriteDataBatchHelper_Scene3D, which does both saving and loading and culling upon application of the ROI to the sprite data.
//
public class ContributingSpritesList{
	ArrayList<Integer> contributingSpritesUniqueIDs = new ArrayList<Integer>();

	
	
	public void addContributingSpriteID(int uniqueID) {
		// This is called by the RenderBorder based on the decision made
		// so the 
		contributingSpritesUniqueIDs.add(uniqueID);
	}


	
	
	
	public SpriteSeedBatch removeNonContributingSprites(SpriteSeedBatch seedbatch) {
		
		
		// Faster apparoach
		// using the cropDecisionList, build an array (activeSeedsArray) of booleans or length maxUniqueIDNumber. Set all to false
		// using the ID Number of each element of the cropDecisionList, set that array element true
		
		// iterate through the allSeedsList. Mark each seed inactive
		// iterate through the allSeedsList. For thisSeed, get the thisSeed.uniqueID.  Set thisSeed.isActive = activeSeedsArray[thisSeed.uniqueID];
		
		// Now copy out the active seeds into another array.
		
		
		int maxUniqueIDOfContributingSprites = getMaxIntegerCropDecisionList();
		System.out.println("building LUT: contributing sprites count " + contributingSpritesUniqueIDs.size() + " max unique ID = " + maxUniqueIDOfContributingSprites);
		boolean[] contributingLUT = new boolean[maxUniqueIDOfContributingSprites+1]; // assumes all set to false
		for(Integer included: contributingSpritesUniqueIDs) {
			contributingLUT[included]=true;
		}
		
		
		ArrayList<SpriteSeed> allSeedsList = seedbatch.getSpriteSeeds();
		
		
		for(SpriteSeed thisSeed: allSeedsList) {
			thisSeed.isActive = false;
		}
		
		int count = 0;
		for(SpriteSeed thisSeed: allSeedsList) {
			
			int thisSeedUniqueID = thisSeed.getUniqueID();
			if(thisSeedUniqueID >= maxUniqueIDOfContributingSprites) continue;
			if( contributingLUT[thisSeedUniqueID]==true ) {
				thisSeed.isActive = true;
				count++;
			}
		}
		
		System.out.println("setting seeds in allSeedsList to active: " + count);
		
		
		
		ArrayList<SpriteSeed> croppedSeeds = new ArrayList<SpriteSeed>();
		for(SpriteSeed thisSeed: allSeedsList) {
			if(thisSeed.isActive) croppedSeeds.add(thisSeed);
		}
		
		SpriteSeedBatch croppedDataBatch = new SpriteSeedBatch();
		croppedDataBatch.setSpriteSeeds(croppedSeeds);
		return croppedDataBatch;
	}

	
	  
	
	///////////////////////////////////////////////////////
	// load and save seeds using csv
	public void save(String fileAndPath){

		
		
		//cropDecisionList.sort(null);
		int maxInt = getMaxIntegerCropDecisionList();
		System.out.println("saving crop decision list of length " + contributingSpritesUniqueIDs.size() + " max int = " + maxInt);
		FileWriter writer = null;
		try {
			writer = new FileWriter(fileAndPath);
			int len = contributingSpritesUniqueIDs.size();
	         for (int i = 0; i < len; i++) {
					writer.write(contributingSpritesUniqueIDs.get(i) + "\n");
	         }
	         writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
         
         
        
        
	}
	
	int getMaxIntegerCropDecisionList() {
		
		return Collections.max(contributingSpritesUniqueIDs);
		
	}

	public boolean load(String fileAndPath) {
		
		contributingSpritesUniqueIDs.clear();
		Scanner scanner;
		try {
			scanner = new Scanner(new File(fileAndPath));
			int [] tall = new int [100];
			int i = 0;
			while(scanner.hasNextInt())
				{
				contributingSpritesUniqueIDs.add(scanner.nextInt());
				}
			
			
			
			int maxInt = getMaxIntegerCropDecisionList();
			System.out.println("loaded crop decision list of length " + contributingSpritesUniqueIDs.size() + " max int = " + maxInt);
			
			
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("No crop decision list file exists yet for " + fileAndPath);
			return false;
		}
		
		

	}


}


