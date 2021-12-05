
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;

import MOMaths.PVector;
import MOUtils.CollectionIterator;
import MOUtils.MOStringUtils;


///////////////////////////////////////////////////////////////////////////
//A seed is a light-weight data object that can be generated in large numbers
//to pre-calculated the population of an image by a pre-render process.
//
//When saving/loading seeds between different sessions/systems, the saved coordinates are
//in NORMALISED space, so that different aspects in the source & destination systems is not an issue
//They contain enough data to recreate the rendered sprite identically each render session

//
//

class Seed {

	// the name of the Seedbatch this seed is made in
	// this enables the user to identify seeds from different batches and treat them differently
	String batchName = " ";

	/////////////////////////////////////////////////////
	// the name of the image sample group to be used by
	// and the number of the item within that group
	String imageSampleGroupName = " ";
	int imageSampleGroupItemNumber = 0;

	/////////////////////////////////////////////////////
	// Geometric transforms applied
	// the doc point of the seed
	// used to position (translate) the item
	float docPointX;
	float docPointY;

	// scale
	float scale = 1;

	//Rotation, in degrees clockwise
	//where 0 represent the "up" of the image
	float rotation = 0;

	// flip in x and y
	boolean flipX = false;
	boolean flipY = false;

	/////////////////////////////////////////////////////
	// the depth is set to the normalised depth in the 3D scene, 
	// usually used to sort the render order of the seeds
	// 
	float depth;

	// the id is a unique integer
	// This is used in seeding the sprite's random number generator, thereby ensuring the same random events happen to each seed
	// regardless of previous random events
	int id;

	public Seed() {
	}

	public Seed(PVector docpt, String imageSampleGroupNm, int imageSampleGroupItemNum) {
		docPointX = docpt.x;
		docPointY = docpt.y;
		depth = docpt.z;
		imageSampleGroupName = imageSampleGroupNm;
		imageSampleGroupItemNumber = imageSampleGroupItemNum;
	}

	public Seed(PVector docpt) {
		docPointX = docpt.x;
		docPointY = docpt.y;
		depth = docpt.z;
	}

	PVector getDocPoint() {
		return new PVector(docPointX, docPointY, 0);
	}
	
	
	void setDocPoint(PVector p) {
		docPointX = p.x;
		docPointY = p.y;
	}

	PVector getDocPointWithDepth() {
		return new PVector(docPointX, docPointY, depth);
	}

	float getDepth() {
		return depth;
	}

	void setDepth(float d) {
		depth = d;
	}



	String getAsCSVStr() {
		// in the processing side of things
		// seeds are always set using normalised x,y location
		// in the createSeeds() method

		// if environment == JAVA
		PVector np = docSpaceToNormalisedSpace(new PVector(docPointX, docPointY));
		// if environment == PROCESSING
		// PVector np = new PVector(docPointX, docPointY));


		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.addKeyValue("BatchName", batchName);
		kvlist.addKeyValue("ImageSampleGroup", imageSampleGroupName);
		kvlist.addKeyValue("imageSampleGroupItemNumber", imageSampleGroupItemNumber);
		kvlist.addKeyValue("DocPointX", np.x);
		kvlist.addKeyValue("DocPointY", np.y);
		kvlist.addKeyValue("Scale", scale);
		kvlist.addKeyValue("Rotation", rotation);
		kvlist.addKeyValue("FlipX", flipX);
		kvlist.addKeyValue("FlipY", flipY);
		kvlist.addKeyValue("Depth", depth);
		kvlist.addKeyValue("Id", id);
		String line =  kvlist.getAsCSVLine();
		//System.out.println(line);
		return line;

	}

	void setWithCSVStr(String csvStr) {
		// in the processing side of things
		// seeds are always set using normalised x,y location
		// in the createSeeds() method
		// It is then down to the application to translate this to the vehicle locations
		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.ingestCSVLine(csvStr);
		batchName = kvlist.getString("BatchName");
		imageSampleGroupName = kvlist.getString("ImageSampleGroup");
		imageSampleGroupItemNumber = kvlist.getInt("imageSampleGroupItemNumber");
		float npX = kvlist.getFloat("DocPointX");
		float npY = kvlist.getFloat("DocPointY");

		scale = kvlist.getFloat("Scale");
		rotation = kvlist.getFloat("Rotation");
		flipX = kvlist.getBoolean("FlipX");
		flipY = kvlist.getBoolean("FlipY");
		depth = kvlist.getFloat("Depth");
		id = kvlist.getInt("Id");

		// if environment == JAVA
		PVector dpt = normalisedSpaceToDocSpace(new PVector(npX, npY));
		// if environment == PROCESSING
		// PVector dpt = new PVector(npX, npY);

		setDocPoint(dpt);



	}

	PVector normalisedSpaceToDocSpace(PVector normPt) {
		return GlobalObjects.theDocument.coordinateSystem.normalisedSpaceToDocSpace( normPt);
	}

	PVector docSpaceToNormalisedSpace(PVector docPt) {
		return GlobalObjects.theDocument.coordinateSystem.docSpaceToNormalisedSpace(getDocPoint());
	}
}



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








