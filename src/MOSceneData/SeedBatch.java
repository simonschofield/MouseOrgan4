package MOSceneData;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;

import MOImageCollections.ImageSampleDescription;
import MOImageCollections.ImageSampleSelector;
import MOMaths.PVector;
import MOUtils.CollectionIterator;
import MOUtils.MOStringUtils;


///////////////////////////////////////////////////////////////////////////
// A seed batch is a collection of seeds.
// When a helper produces seeds it is this form.
// Used when iterating through seeds in the main loop
// Can save/load to file
// adds a "unique ID" - this is to seed events in the Sprite, so as to secure repeatability.

public class SeedBatch extends CollectionIterator{
	
	
	private ArrayList<Seed> seeds = new ArrayList<Seed>();
	

	public SeedBatch(){
		
	}
	
	
	public SeedBatch copy() {
		SeedBatch cpy = new SeedBatch();
		cpy.seeds = (ArrayList<Seed>) this.seeds.clone();
		return cpy;
	}
	
	public void addSeed(Seed s) {
		seeds.add(s);
	}


	ArrayList<Seed> getSeeds(){
		return seeds;
	}
	
	
	public void append(SeedBatch otherBatch) {
		ArrayList<Seed> otherSeeds = otherBatch.getSeeds();
		for(Seed s : otherSeeds) {
			seeds.add(s);
		}
		
	}
	
	public void depthSort() {
		seeds.sort(Comparator.comparing(Seed::getDepth).reversed());
	}
	
	

	public ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(Seed s : seeds) {
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


			for(Seed s: seeds){
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
				Seed s = new Seed();
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


	public Seed getNextSeed() {
		return (Seed) getNextItem();
	}

}








