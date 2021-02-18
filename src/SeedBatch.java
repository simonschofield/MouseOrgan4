
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;


///////////////////////////////////////////////////////////////////////////
// A seed is a light-weight data object that can be generated in large numbers
// to pre-calculated the population of an image by a pre-render process.
// Generally, during image production, seeds are given to sprites one at a time and contain
// enough data to recreate the same outcome from one session to another.
//
// The SeedBatch class is the most basic a container for a collection of seeds.
// A seed batch can farm seeds out, and can save/load collections of seeds.
// Collections of seeds can be saved and loaded between sessions using serialisation within THIS environment, in which case the seeds
// contain documentspace coordinates.
// When saving/loading seeds between different systems CSV is used as the interchange file format. When doing so the coordinates are
// saved in NORMALISED space, so that different aspects in the source & destination systems is factored out
//
//
//
@SuppressWarnings("serial")
class Seed implements Serializable{
	
	// the name of the Seedbatch this seed is made in
	// this enables the user to identify seeds from different batches and treat them differently
	String batchName;
	
	/////////////////////////////////////////////////////
	// the name of the image sample group to be used by
	// and the number of the item within that group
	String imageSampleGroupName = "";
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
	
	PVector getDocPointWithDepth() {
		return new PVector(docPointX, docPointY, depth);
	}
	
	void setDocPointWithNormalisedCoordinate(PVector normPt) {
		PVector dp = GlobalObjects.theDocument.normalisedSpaceToDocSpace( normPt);
		docPointX = dp.x;
		docPointY = dp.y;
	}
	
	PVector getDocPointAsNormalisedCoordinate() {
		return GlobalObjects.theDocument.docSpaceToNormalisedSpace(getDocPoint());
	}
	
	float getDepth() {
		return depth;
	}
	
	void setDepth(float d) {
		depth = d;
	}
	
	String getAsCSVStr(){
	    /*
	    String batchName = "";
	    String imageSampleGroupName = "";
	    int imageSampleGroupItemNumber = 0;
	    float docPointX;
	    float docPointY;
	    float scale = 1;
	    float rotation = 0;
	    boolean flipX = false;
	    boolean flipY = false;
	    float depth;
	    int id;
	  
	    */
		PVector np = getDocPointAsNormalisedCoordinate();
	    return 		cm(batchName) + cm(imageSampleGroupName) + cm(imageSampleGroupItemNumber) +
	                cm(np.x) + cm(np.y) + cm(scale) + cm(rotation) +
	                cm(flipX) + cm(flipY) + cm(depth) + id + "\n";
	  }
	  
	  String cm(String item){
	    return item + ",";
	  }
	  
	  String cm(int item){
	    return item + ",";
	  }
	  
	  String cm(float item){
	     return item + ",";
	  }
	  
	  String cm(boolean item){
	     return item + ",";
	  }
	  
	  void setWithCSVStr(String csvStr) {
		 // set using the whole unsplit string 
		  String[] data = csvStr.split(","); 
		  
		  int i = 0;
		  batchName = data[i++];
		  imageSampleGroupName = data[i++];
		  imageSampleGroupItemNumber = Integer.parseInt(data[i++]);
		  float npX = Float.parseFloat(data[i++]);
		  float npY = Float.parseFloat(data[i++]);
		  scale = Float.parseFloat(data[i++]);
		  rotation = Float.parseFloat(data[i++]);
		  flipX = Boolean.parseBoolean(data[i++]);
		  flipY = Boolean.parseBoolean(data[i++]);
		  depth = Float.parseFloat(data[i++]);
		  id = Integer.parseInt(data[i]);
		  
		  setDocPointWithNormalisedCoordinate(new PVector(npX,npY));
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
	
	
	
	ArrayList<Seed> generateSeeds(ImageSampleSelector cc, PointGenerator pg){
		imageSampleSelector = cc;
		PointGenerator pointGenerator = pg;
		if(pointGenerator.getNumItems()==0) {
			pointGenerator.generatePoints();
		}
		while(pointGenerator.areItemsRemaining()) {
			PVector p = pointGenerator.getNextPoint();
			ImageSampleDescription isd =  imageSampleSelector.selectImageSampleDescription(p);
			Seed seed = new Seed(p, isd.imageSampleGroupName, isd.itemNumber);
			seed.batchName = this.batchName;
			seed.id = uniqueSeedIDCounter++;
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
	// these automatically save and load using the name of the seed batch
	void saveSeeds(String path) {
		// there should be a directory in the project folder called seeds
		ensureSeedsDirectoryExists(path);
		String pathandname = path + "seeds\\" + batchName + ".sds";
		SerializableFile.save(pathandname, seeds);
	}
	
	@SuppressWarnings("unchecked")
	void loadSeeds(String path) {
		String seedsDirectoryPath = path + "seeds\\";
		String pathandname = seedsDirectoryPath + batchName + ".sds";
		System.out.println("loading seed layer " + pathandname);
		seeds = (ArrayList<Seed>)(SerializableFile.load(pathandname));
		if(seeds==null) {
			System.out.println("load failed, does file exist?");
		}else {
			System.out.println("number seed loaded " + seeds.size());
		}
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
	        System.out.println("csv writer failed");
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
	        	System.out.println("csv reader failed");
	        }
	      
	    }
	  
	
	
	
	
	
	void ensureSeedsDirectoryExists(String path) {
		String alledgedDirectory = path + "seeds";
		if(MOUtils.checkDirectoryExist(alledgedDirectory)) return;
		MOUtils.createDirectory(alledgedDirectory);
	}

	@Override
	int getNumItems() {
		// TODO Auto-generated method stub
		return seeds.size();
	}

	@Override
	Object getItem(int n) {
		// TODO Auto-generated method stub
		return seeds.get(n);
	}
	
	
	Seed getNextSeed() {
		return (Seed) getNextItem();
	}

}
















