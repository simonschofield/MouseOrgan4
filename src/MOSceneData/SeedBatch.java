package MOSceneData;

import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;


import MOImageCollections.ImageSampleDescription;
import MOImageCollections.ImageSampleSelector;
import MOMaths.PVector;
import MOUtils.CollectionIterator;
import MOUtils.KeyValuePairList;
import MOUtils.MOStringUtils;


///////////////////////////////////////////////////////////////////////////
// A seed batch is a collection of seeds.
// a SeedBatch takes a PointGenerator and a ContentItemSelector
// and makes a number of seeds with it
// These can then be added to the SeedRenderManager
// Normally, the use does not have to explicitly create SeedBatches, but interfaces directly wit the SeedBatchManager
public class SeedBatch extends CollectionIterator{
	String batchName = "";
	ImageSampleSelector imageSampleSelector;
	//PointGenerator pointGenerator;
	boolean isVisible = true;
	ArrayList<Seed> seeds = new ArrayList<Seed>();
	int uniqueSeedIDCounter = 0;

	SeedBatch(String name){
		batchName = name;
	}



	ArrayList<Seed> generateSeeds(ImageSampleSelector cc, PointGenerator_Random pg){
		imageSampleSelector = cc;
		PointGenerator_Random pointGenerator = pg;
		if(pointGenerator.getNumItems()==0) {
			pointGenerator.generatePoints();
		}
		while(pointGenerator.areItemsRemaining()) {
			PVector p = pointGenerator.getNextPoint();
			ImageSampleDescription isd =  imageSampleSelector.selectImageSampleDescription(p);
			Seed seed = new Seed(p, isd.imageSampleGroupName, isd.itemNumber);
			seed.batchName = this.batchName;
			seed.id = uniqueSeedIDCounter++;
			seed.depth = p.z;
			seeds.add(seed);
		}
		return seeds;
	}

	boolean nameEquals(String n) {
		return n.contentEquals(batchName);
	}

	ArrayList<Seed> getSeeds(){
		return seeds;
	}

	void setVisible(boolean vis) {
		isVisible = vis;
	}

	boolean isVisible() {
		return isVisible;
	}

	ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(Seed s : seeds) {
			points.add(s.getDocPoint());
		}
		return points;
	}



	///////////////////////////////////////////////////////
	// load and save seeds using serialisation
	// these automatically save and load using the name of the seed batch to a local folder called seeds
	void saveSeeds(String path) {
		// there should be a directory in the project folder called seeds
		ensureSeedsDirectoryExists(path);
		String pathandname = path + "seeds\\" + batchName + ".sds";
		saveSeedsAsCSV(pathandname);
	}

	@SuppressWarnings("unchecked")
	void loadSeeds(String path) {
		String seedsDirectoryPath = path + "seeds\\";
		String pathandname = seedsDirectoryPath + batchName + ".sds";
		System.out.println("loading seed layer " + pathandname);
		loadSeedsAsCSV(pathandname);

	}

	///////////////////////////////////////////////////////
	// load and save seeds using csv
	void saveSeedsAsCSV(String fileAndPath) {
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

	void loadSeedsAsCSV(String fileAndPath) {
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


	void scaleSeedPositions(float sx, float sy) {
		// scales the seed batch
		ArrayList<Seed> temp = new ArrayList<Seed>();
		for(Seed s : seeds) {
			s.docPointX *= sx;
			s.docPointY *= sy;
			//if(GlobalObjects.theDocument.isInsideDocumentSpace(new PVector(s.docPointX, s.docPointY))){
			//	temp.add(s);
			//}
		}
		//seeds = temp;
	}



	void ensureSeedsDirectoryExists(String path) {
		String alledgedDirectory = path + "seeds";
		if(MOStringUtils.checkDirectoryExist(alledgedDirectory)) return;
		MOStringUtils.createDirectory(alledgedDirectory);
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


	Seed getNextSeed() {
		return (Seed) getNextItem();
	}

}








