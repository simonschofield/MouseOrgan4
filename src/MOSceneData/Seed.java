package MOSceneData;

import MOMaths.PVector;
import MOUtils.KeyValuePairList;
import MOUtils.MOUtilGlobals;

//////////////////////////////////////////////////////////////////////////
//A seed is a light-weight data object that can be generated in large numbers
//to pre-calculated the population of an image by a pre-render process.
//
//When saving/loading seeds between different sessions/systems, the saved coordinates are
//in NORMALISED space, so that different aspects in the source & destination systems is not an issue
//They contain enough data to recreate the rendered sprite identically each render session

//
//

public class Seed {

	// the name of the Seedbatch this seed is made in
	// this enables the user to identify seeds from different batches and treat them differently
	String batchName = " ";

	/////////////////////////////////////////////////////
	// the name of the image sample group to be used by
	// and the number of the item within that group
	public String imageSampleGroupName = " ";
	public int imageSampleGroupItemNumber = 0;

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
	public int id;

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

	public PVector getDocPoint() {
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
		return MOUtilGlobals.theDocumentCoordSystem.normalisedSpaceToDocSpace( normPt);
	}

	PVector docSpaceToNormalisedSpace(PVector docPt) {
		return MOUtilGlobals.theDocumentCoordSystem.docSpaceToNormalisedSpace(getDocPoint());
	}
}

