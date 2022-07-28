package MOSprite;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;


import MOMaths.PVector;
import MOUtils.CollectionIterator;
import MOUtils.MOStringUtils;


///////////////////////////////////////////////////////////////////////////
// A SpriteDataBatch is an iterable collection of type SpriteData.
// When a helper produces seeds it is this form.
// Used when iterating through seeds in the main loop
// Can save/load to file
// adds a "unique ID" - this is to seed events in the Sprite, so as to secure repeatability.

public class SpriteDataBatch extends CollectionIterator{
	
	
	private ArrayList<SpriteData> seeds = new ArrayList<SpriteData>();
	String seedBatchName = "";
	
	public SpriteDataBatch(String name){
		seedBatchName = name;
	}
	
	public String getName() {
		return seedBatchName;
	}
	
	
	public SpriteDataBatch copy() {
		SpriteDataBatch cpy = new SpriteDataBatch(seedBatchName + "_copy");
		cpy.seeds = (ArrayList<SpriteData>) this.seeds.clone();
		return cpy;
	}
	
	public void addSpriteData(SpriteData s) {
		seeds.add(s);
	}


	public ArrayList<SpriteData> getSpriteDatas(){
		return seeds;
	}
	
	
	public void setSpriteDatas(ArrayList<SpriteData> sds) {
		seeds = sds;
	}
	
	
	
	public void append(SpriteDataBatch otherBatch) {
		ArrayList<SpriteData> otherSeeds = otherBatch.getSpriteDatas();
		for(SpriteData s : otherSeeds) {
			seeds.add(s);
		}
		
	}
	
	public void depthSort() {
		seeds.sort(Comparator.comparing(SpriteData::getDepth).reversed());
	}
	
	

	public ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(SpriteData s : seeds) {
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


			for(SpriteData s: seeds){
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
				SpriteData s = new SpriteData();
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


	public SpriteData getNextSeed() {
		return (SpriteData) getNextItem();
	}

}








