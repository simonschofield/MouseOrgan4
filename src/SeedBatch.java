
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;


///////////////////////////////////////////////////////////////////////////
//A seed is a light-weight data object that can be generated in large numbers
//to pre-calculated the population of an image by a pre-render process.
//
//It is serializable so collections of seeds can be saved and loaded between sessions within this environment
//When saving/loading seeds between different systems, we use CSV as the common file format, also the save coordinates are
//in NORMALISED space, so that different aspects in the source & destination systems is not an issue
//They contain enough data to recreate the rendered sprite identically each render session
//
//Generally, during image production, seeds are given to sprites one at a time
//
//
@SuppressWarnings("serial")
class Seed implements Serializable {

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

		PVector np = docSpaceToNormalisedSpace(new PVector(docPointX, docPointY));

		CSVKeyValue csvKV = new CSVKeyValue();
		csvKV.appendLine("BatchName", batchName);
		csvKV.appendLine("ImageSampleGroup", imageSampleGroupName);
		csvKV.appendLine("DocPointX", np.x);
		csvKV.appendLine("DocPointY", np.y);
		csvKV.appendLine("Scale", scale);
		csvKV.appendLine("Rotation", rotation);
		csvKV.appendLine("FlipX", flipX);
		csvKV.appendLine("FlipY", flipY);
		csvKV.appendLine("Depth", depth);
		csvKV.appendLine("Id", id);
		return csvKV.getLine();
	}

	void setWithCSVStr(String csvStr) {
		CSVKeyValue csvKV = new CSVKeyValue(csvStr);
		
		batchName = csvKV.getString("BatchName");
		imageSampleGroupName = csvKV.getString("ImageSampleGroup");
		float npX = csvKV.getFloat("DocPointX");
		float npY = csvKV.getFloat("DocPointY");
		scale = csvKV.getFloat("Scale");
		rotation = csvKV.getFloat("Rotation");
		flipX = csvKV.getBoolean("FlipX");
		flipY = csvKV.getBoolean("FlipY");
		depth = csvKV.getFloat("Depth");
		id = csvKV.getInt("Id");

		PVector dpt = normalisedSpaceToDocSpace(new PVector(npX, npY));
		setDocPoint(dpt);
		
	}

	PVector normalisedSpaceToDocSpace(PVector normPt) {
		return GlobalObjects.theDocument.normalisedSpaceToDocSpace( normPt);
	}

	PVector docSpaceToNormalisedSpace(PVector docPt) {
		return GlobalObjects.theDocument.docSpaceToNormalisedSpace(getDocPoint());
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
	// these automatically save and load using the name of the seed batch
	/*/// OLD Serialised saving
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
	*/
	
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


///////////////////////////////////////////////////////////////////////////////////////////
//converts between a Key Value pair of types (Boolean, int, float, String) - e.g. "X" : 3.56
//and a string containing this pair "X:3.56"
//Can be used to assemble a CSV line by calling appendLine(k,v) 
//
class CSVKeyValue {
	static final int NOTSET = 0;
	static final int BOOLEAN = 1;
	static final int INTEGER = 2;
	static final int FLOAT = 3;
	static final int STRING = 4;

	String key;
	private int TYPE = NOTSET;
	boolean bval;
	int ival;
	float fval;
	String sval;

	String assembledLine = "";
	String[] linePairs;

	CSVKeyValue() {
	}

	CSVKeyValue(String line) {
		setLine(line);
	}

	boolean getBoolean(String k) {
		String v = findValueInLinePairs(k);
		if (v == null)
			return false;
		return Boolean.parseBoolean(v);
	}

	int getInt(String k) {
		String v = findValueInLinePairs(k);
		if (v == null)
			return 0;
		return Integer.parseInt(v);
	}

	float getFloat(String k) {
		String v = findValueInLinePairs(k);
		if (v == null)
			return 0;
		return Float.parseFloat(v);
	}

	String getString(String k) {
		String v = findValueInLinePairs(k);
		if (v == null)
			return "";
		return v;
	}

	private String findValueInLinePairs(String k) {
		for (String s : linePairs) {
			String[] kv = splitPairIntoKeyValue(s);
			if (kv[0].equals(k)) {
				return kv[1];
			}
		}
		return null;
	}

	void setLine(String line) {
		assembledLine = line;
		linePairs = splitLineIntoPairs(assembledLine);
		
	}

	void appendLine(String k, boolean v) {
		String s = set(k, v);
		appendLine(s);
	}

	void appendLine(String k, int v) {
		String s = set(k, v);
		appendLine(s);
	}

	void appendLine(String k, float v) {
		String s = set(k, v);
		appendLine(s);
	}

	void appendLine(String k, String v) {
		String s = set(k, v);
		appendLine(s);
	}

	String getLine() {
		return (assembledLine + "\n");
	}

	void clearLine() {
		assembledLine = "";
	}

	void appendLine() {
// adds the current state of this object to the string
		String s = getAsKeyValueString();
		appendLine(s);
	}

	private void appendLine(String s) {
		if (assembledLine.contentEquals("")) {
			assembledLine = s;
			return;
		}
		assembledLine = assembledLine + "," + s;
	}

	String[] splitLineIntoPairs(String assembled) {
		return assembled.split(",");
	}

	void setWithPairString(String keyVal, int type) {
		String[] s_pair = splitPairIntoKeyValue(keyVal);
		String k = s_pair[0];
		String v = s_pair[1];

		if (type == NOTSET) {
			return;
		}
		if (type == BOOLEAN) {
			Boolean b = Boolean.parseBoolean(v);
			set(k, b);
		}
		if (type == INTEGER) {
			int i = Integer.parseInt(v);
			set(k, i);
		}
		if (type == FLOAT) {
			float f = Float.parseFloat(v);
			set(k, f);
		}
		if (type == STRING) {
			set(k, v);
		}
	}

	String[] splitPairIntoKeyValue(String pair) {
		return pair.split(":");
	}

	int getType() {
		return TYPE;
	}

// these are used to generate the sting sin the first instance from your data
	String set(String k, boolean v) {
		TYPE = BOOLEAN;
		key = k;
		bval = v;
		return getAsKeyValueString();
	}

	String set(String k, int v) {
		TYPE = INTEGER;
		key = k;
		ival = v;
		return getAsKeyValueString();
	}

	String set(String k, float v) {
		TYPE = FLOAT;
		key = k;
		fval = v;
		return getAsKeyValueString();
	}

	String set(String k, String v) {
		TYPE = STRING;
		if(v.contentEquals("")) v = " ";
		key = k;
		sval = v;
		return getAsKeyValueString();
	}

	String getAsKeyValueString() {
		if (TYPE == NOTSET) {
			return "";
		}
		if (TYPE == BOOLEAN) {
			return key + ":" + bval;
		}
		if (TYPE == INTEGER) {
			return key + ":" + ival;
		}
		if (TYPE == FLOAT) {
			return key + ":" + fval;
		}
		if (TYPE == STRING) {
			return key + ":" + sval;
		}
		return "";
	}
}









