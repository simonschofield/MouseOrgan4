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

public class SpriteDataBatch extends CollectionIterator{
	
	
	private ArrayList<SpriteData> spriteDataList = new ArrayList<SpriteData>();
	String seedBatchName = "";
	
	
	public SpriteDataBatch(String name){
		seedBatchName = name;
	}
	
	public String getName() {
		return seedBatchName;
	}
	
	
	public SpriteDataBatch copy() {
		SpriteDataBatch cpy = new SpriteDataBatch(seedBatchName + "_copy");
		cpy.spriteDataList = (ArrayList<SpriteData>) this.spriteDataList.clone();
		return cpy;
	}
	
	public void addSpriteData(SpriteData s) {
		spriteDataList.add(s);
	}


	public ArrayList<SpriteData> getSpriteDatas(){
		return spriteDataList;
	}
	
	
	public void setSpriteDatas(ArrayList<SpriteData> sds) {
		spriteDataList = sds;
	}
	
	
	
	public void append(SpriteDataBatch otherBatch) {
		ArrayList<SpriteData> otherSeeds = otherBatch.getSpriteDatas();
		for(SpriteData s : otherSeeds) {
			spriteDataList.add(s);
		}
		
	}
	
	public void depthSort() {
		spriteDataList.sort(Comparator.comparing(SpriteData::getDepth).reversed());
	}
	
	public Range getDepthExtrema() {
		Range depthExtrema = new Range();
		depthExtrema.initialiseForExtremaSearch();
		
		for(SpriteData s : spriteDataList) {
			float d = s.getDepth();
			depthExtrema.addExtremaCandidate(d);
		}
		System.out.println("getting sprite batch depth extrema " + depthExtrema.toStr());
		return depthExtrema;
	}

	public ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(SpriteData s : spriteDataList) {
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


			for(SpriteData s: spriteDataList){
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
				spriteDataList.add(s);
			}
			csvReader.close();
		} catch(Exception e){

			System.out.println("SeedBatch.loadSeedsAsCSV: csv reader failed - " + fileAndPath + e);
		}

	}

	

	

	@Override
	public int getNumItems() {
		// TODO Auto-generated method stub
		return spriteDataList.size();
	}

	@Override
	public Object getItem(int n) {
		// TODO Auto-generated method stub
		return spriteDataList.get(n);
	}


	public SpriteData getNextSeed() {
		return (SpriteData) getNextItem();
	}

}








