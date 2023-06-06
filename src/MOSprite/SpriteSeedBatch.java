package MOSprite;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;


import MOMaths.PVector;
import MOMaths.Range;
import MOUtils.CollectionIterator;
import MOUtils.MOStringUtils;


///////////////////////////////////////////////////////////////////////////
// A SpriteDataBatch is an iterable collection of type SpriteData.
// When a helper produces seeds it is this form.
// Used when iterating through seeds in the main loop
// Can save/load to file
// adds a "unique ID" - this is to seed events in the Sprite, so as to secure repeatability.

public class SpriteSeedBatch extends CollectionIterator{
	
	
	private ArrayList<SpriteSeed> spriteSeedList = new ArrayList<SpriteSeed>();
	String seedBatchName = "";
	
	
	public SpriteSeedBatch(String name){
		seedBatchName = name;
	}
	
	public String getName() {
		return seedBatchName;
	}
	
	
	public SpriteSeedBatch copy() {
		SpriteSeedBatch cpy = new SpriteSeedBatch(seedBatchName + "_copy");
		cpy.spriteSeedList = (ArrayList<SpriteSeed>) this.spriteSeedList.clone();
		return cpy;
	}
	
	public void addSpriteSeed(SpriteSeed s) {
		spriteSeedList.add(s);
	}


	public ArrayList<SpriteSeed> getSpriteSeeds(){
		return spriteSeedList;
	}
	
	
	public void setSpriteSeeds(ArrayList<SpriteSeed> sds) {
		spriteSeedList = sds;
	}
	
	
	
	public void append(SpriteSeedBatch otherBatch) {
		ArrayList<SpriteSeed> otherSeeds = otherBatch.getSpriteSeeds();
		for(SpriteSeed s : otherSeeds) {
			spriteSeedList.add(s);
		}
		
	}
	
	public void depthSort() {
		spriteSeedList.sort(Comparator.comparing(SpriteSeed::getDepth).reversed());
	}
	
	public Range getDepthExtrema() {
		Range depthExtrema = new Range();
		depthExtrema.initialiseForExtremaSearch();
		
		for(SpriteSeed s : spriteSeedList) {
			float d = s.getDepth();
			depthExtrema.addExtremaCandidate(d);
		}
		System.out.println("getting sprite batch depth extrema " + depthExtrema.toStr());
		return depthExtrema;
	}

	public ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(SpriteSeed s : spriteSeedList) {
			points.add(s.getDocPoint());
		}
		return points;
	}

	
	public void copySeedBatchNameToSeeds() {
		// overwrites the seedbatch name with this name
		for(SpriteSeed s: spriteSeedList){
			s.SeedBatchName = this.seedBatchName;
		}
		
	}

	///////////////////////////////////////////////////////
	// load and save seeds using csv
	public void saveSeeds(String fileAndPath, boolean setSeedBatchName) {
		// there should be a directory in the project folder called seeds
		
		
		if(setSeedBatchName) {
			copySeedBatchNameToSeeds();
		}

		FileWriter csvWriter = null;
		try{
			csvWriter = new FileWriter(fileAndPath);


			for(SpriteSeed s: spriteSeedList){
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
				spriteSeedList.add(s);
			}
			csvReader.close();
		} catch(Exception e){

			System.out.println("SpriteSeedBatch.loadSeedsAsCSV: csv reader failed - " + fileAndPath + e);
		}

	}

	

	

	@Override
	public int getNumItems() {
		// TODO Auto-generated method stub
		return spriteSeedList.size();
	}

	@Override
	public Object getItem(int n) {
		// TODO Auto-generated method stub
		return spriteSeedList.get(n);
	}


	public SpriteSeed getNextSeed() {
		return (SpriteSeed) getNextItem();
	}

}



