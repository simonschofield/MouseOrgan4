package MOCompositing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import MOMaths.PVector;
import MOSpriteSeed.SpriteSeed;
import MOSpriteSeed.SpriteSeedBatch;
import MOUtils.KeyValuePairList;

public class SpriteCropDecisionList{
	ArrayList<SpriteCropDecision> cropDecisionList = new ArrayList<SpriteCropDecision>();

	void addDecision(int id, boolean contributes) {
		SpriteCropDecision scd = new SpriteCropDecision(id, contributes);
		cropDecisionList.add(scd);
	}


	public void removeNonContributingSpriteSeeds(SpriteSeedBatch seedbatch) {
		// this removes the seeds from the incoming list so is destructive
		ArrayList<SpriteSeed> seeds = seedbatch.getSpriteSeeds();
		ArrayList<SpriteSeed> croppedSeeds = new ArrayList<SpriteSeed>();
		for(SpriteSeed seed: seeds) {
			if(cropDecisionList.stream().anyMatch(o -> o.spriteID == seed.id)) {
				croppedSeeds.add(seed);
			} 
		}
		System.out.println("removeNonContributingSpriteSeeds before " + seeds.size() + " after " + croppedSeeds.size());
		seedbatch.setSpriteSeeds(croppedSeeds);
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

	int spriteID;
	boolean contributes;



	public SpriteCropDecision(int spriteID, boolean contributes) {
		super();
		this.spriteID = spriteID;
		this.contributes = contributes;
	}


	public String getAsCSVStr() {
		// in the processing side of things
		// seeds are always set using normalised x,y location
		// in the createSeeds() method

		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.addKeyValue("SpriteID", spriteID);
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

		this.spriteID = kvlist.getInt("SpriteID");
		this.contributes = kvlist.getBoolean("Contributes");
	}

}

