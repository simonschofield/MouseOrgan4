package MOSpriteSeed;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;


import MOMaths.PVector;
import MOUtils.CollectionIterator;
import MOUtils.MOStringUtils;


///////////////////////////////////////////////////////////////////////////
// A SpriteSeedBatch is an iterable collection of SpriteSeed.
// When a helper produces seeds it is this form.
// Used when iterating through seeds in the main loop
// Can save/load to file
// adds a "unique ID" - this is to seed events in the Sprite, so as to secure repeatability.

public class SpriteSeedBatch extends CollectionIterator{
	
	
	private ArrayList<SpriteSeed> seeds = new ArrayList<SpriteSeed>();
	String seedBatchName = "";
	
	public SpriteSeedBatch(String name){
		seedBatchName = name;
	}
	
	public String getName() {
		return seedBatchName;
	}
	
	
	public SpriteSeedBatch copy() {
		SpriteSeedBatch cpy = new SpriteSeedBatch(seedBatchName + "_copy");
		cpy.seeds = (ArrayList<SpriteSeed>) this.seeds.clone();
		return cpy;
	}
	
	public void addSpriteSeed(SpriteSeed s) {
		seeds.add(s);
	}


	public ArrayList<SpriteSeed> getSpriteSeeds(){
		return seeds;
	}
	
	
	public void setSpriteSeeds(ArrayList<SpriteSeed> sds) {
		seeds = sds;
	}
	
	
	
	public void append(SpriteSeedBatch otherBatch) {
		ArrayList<SpriteSeed> otherSeeds = otherBatch.getSpriteSeeds();
		for(SpriteSeed s : otherSeeds) {
			seeds.add(s);
		}
		
	}
	
	public void depthSort() {
		seeds.sort(Comparator.comparing(SpriteSeed::getDepth).reversed());
	}
	
	

	public ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(SpriteSeed s : seeds) {
			points.add(s.getDocPoint());
		}
		return points;
	}



	///////////////////////////////////////////////////////
	// load and save seeds using csv
	public void saveSeeds(String fileAndPath) {
		// there should be a directory in the project folder called seeds


		FileWriter csvWriter = null;
		try{
			csvWriter = new FileWriter(fileAndPath);


			for(SpriteSeed s: seeds){
				csvWriter.append(s.getAsCSVStr());
			}

			csvWriter.flush();
			csvWriter.close();

		}// end try
		catch(Exception ex){
			System.out.println("SeedBatch.saveSeedsAsCSV: csv writer failed - "  + fileAndPath + ex);
		}


	}

	public void loadSeeds(String fileAndPath) {
		// there should be a directory in the project folder called seeds
		try{
			BufferedReader csvReader = new BufferedReader(new FileReader(fileAndPath));

			String row;

			while ((row = csvReader.readLine()) != null) {

				// do something with the data
				SpriteSeed s = new SpriteSeed();
				s.setWithCSVStr(row);
				seeds.add(s);
			}
			csvReader.close();
		} catch(Exception e){

			System.out.println("SeedBatch.loadSeedsAsCSV: csv reader failed - " + fileAndPath + e);
		}

	}

	

	

	@Override
	public int getNumItems() {
		// TODO Auto-generated method stub
		return seeds.size();
	}

	@Override
	public Object getItem(int n) {
		// TODO Auto-generated method stub
		return seeds.get(n);
	}


	public SpriteSeed getNextSeed() {
		return (SpriteSeed) getNextItem();
	}

}








