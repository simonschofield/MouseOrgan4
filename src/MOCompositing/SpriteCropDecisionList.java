package MOCompositing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

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
public class SpriteCropDecisionList{
	ArrayList<SpriteCropDecision> cropDecisionList = new ArrayList<SpriteCropDecision>();

	void addDecision(int id, boolean contributes) {
		SpriteCropDecision scd = new SpriteCropDecision(id, contributes);
		cropDecisionList.add(scd);
	}


	
	
	
	public SpriteSeedBatch removeNonContributingSprites(SpriteSeedBatch seedbatch) {
		// this removes the seeds from the incoming list so is destructive
		ArrayList<SpriteSeed> seeds = seedbatch.getSpriteSeeds();
		ArrayList<SpriteSeed> croppedSeeds = new ArrayList<SpriteSeed>();
		for(SpriteSeed seed: seeds) {
			if(cropDecisionList.stream().anyMatch(o -> o.spriteUniqueID == seed.getUniqueID())) {
				croppedSeeds.add(seed);
			} 
		}
		System.out.println("removeNonContributingSprites before " + seeds.size() + " after " + croppedSeeds.size());
		SpriteSeedBatch croppedDataBatch = new SpriteSeedBatch();
		croppedDataBatch.setSpriteSeeds(croppedSeeds);
		return croppedDataBatch;
	}

	
	  
	
	///////////////////////////////////////////////////////
	// load and save seeds using csv
	public void save(String fileAndPath) {
		// there should be a directory in the project folder called seeds


		FileWriter csvWriter = null;
		try{
			csvWriter = new FileWriter(fileAndPath);


			for(SpriteCropDecision cd: cropDecisionList){
				csvWriter.append(cd.getAsCSVStr());
			}

			csvWriter.flush();
			csvWriter.close();

		}// end try
		catch(Exception ex){
			System.out.println("SpriteCropDecisionList.save: csv writer failed - "  + fileAndPath + ex);
		}


	}

	public boolean load(String fileAndPath) {
		cropDecisionList.clear();
		boolean loadResult = false;
		// there should be a directory in the project folder called seeds
		try{
			BufferedReader csvReader = new BufferedReader(new FileReader(fileAndPath));

			String row;

			while ((row = csvReader.readLine()) != null) {

				// do something with the data
				SpriteCropDecision cd = new SpriteCropDecision(1,false);//temporary values get overwritten in next line
				cd.setWithCSVStr(row);
				cropDecisionList.add(cd);
				loadResult = true;
			}
			csvReader.close();
		} catch(Exception e){
			loadResult = false;
			System.out.println("SpriteCropDecisionList.load: csv reader failed - " + fileAndPath + e);
		}
		return loadResult;

	}


}


class SpriteCropDecision{

	int spriteUniqueID;
	boolean contributes;



	public SpriteCropDecision(int spriteUniqueID, boolean contributes) {
		super();
		this.spriteUniqueID = spriteUniqueID;
		this.contributes = contributes;
	}


	public String getAsCSVStr() {
		// in the processing side of things
		// seeds are always set using normalised x,y location
		// in the createSeeds() method

		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.addKeyValue("SpriteID", spriteUniqueID);
		kvlist.addKeyValue("Contributes", contributes);

		String line =  kvlist.getAsCSVLine();
		//System.out.println("made seed " + line);
		return line;

	}

	void setWithCSVStr(String csvStr) {
		// in the processing side of things
		// seeds are always set using normalised x,y location
		// in the createSeeds() method
		// It is then down to the application to translate this to the vehicle locations
		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.ingestCSVLine(csvStr);

		this.spriteUniqueID = kvlist.getInt("SpriteID");
		this.contributes = kvlist.getBoolean("Contributes");
	}

}


