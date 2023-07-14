package MOSprite;

import java.io.FileWriter;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;

import MOCompositing.RenderTarget;
import MOMaths.PVector;
import MOMaths.Range;
import MOUtils.CollectionIterator;



///////////////////////////////////////////////////////////////////////////
// A SpriteSeedBatch is an iterable collection of type SpriteSeed.
// It does not have a name, as a seed batch will always be created by some other process and just uses the seed batch as storeage.
//  So the name is set by the making process, not the seed batch itself.
//

public class SpriteSeedBatch extends CollectionIterator{
	
	
	private ArrayList<SpriteSeed> spriteSeedList = new ArrayList<SpriteSeed>();
	//String seedBatchName = "";
	
	
	public SpriteSeedBatch(){
		
	}
	
	
	
	public SpriteSeedBatch copy() {
		SpriteSeedBatch cpy = new SpriteSeedBatch();
		cpy.spriteSeedList = (ArrayList<SpriteSeed>) this.spriteSeedList.clone();
		return cpy;
	}
	
	public void addSpriteSeed(SpriteSeed s) {
		//s.SeedBatchName = seedBatchName;
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
	
	public void appendTo(SpriteSeedBatch appendingToThisBatch) {
		if(appendingToThisBatch==null) {
			System.out.println("SpriteSeedBatch:appendTo ERROR -  the appendingToThisBatch is null - please instantiate beofore using" );
			return;
		}
		appendingToThisBatch.append(this);
		//System.out.println(" appended seedbatch " + appendingToThisBatch + " num seeds = " + appendingToThisBatch.getNumItems());
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

	
	public void setSeedBatchNameInSeeds(String newName) {
		// overwrites the seedbatch name with this name
		for(SpriteSeed s: spriteSeedList){
			s.SeedBatchName = newName;
		}
		
	}

	///////////////////////////////////////////////////////
	// load and save seeds using csv
	public void saveSeeds(String fileAndPath) {
		// there should be a directory in the project folder called seeds
		
		
		

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
				//System.out.println("Loading seed "+row);
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
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	public void drawSeeds(Color col, float pixelradius, RenderTarget rt) {
		
		for(SpriteSeed s : spriteSeedList) {
			rt.drawPoint(s.getDocPoint(), col, pixelradius);
		}
	}


}



