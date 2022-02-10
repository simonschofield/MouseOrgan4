package MOSpriteSeed;

import MOMaths.PVector;
import MOSceneData.Seed;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePairList;

public class SpriteSeed {
	// A lightweight representation of everything you need to make a Sprite
	// Can be saved as seedbatches


	// hello this is an update to check the repository
	int testVariable = 52;


	/////////////////////////////////////////////////////
	// This part: Sprite "Font" data, all instances are set with  this data from a specific SpriteSeedMaker
	//
	//
	// this enables the user to identify seeds from different batches (biomes) and treat them differently
	public String seedFontName = "";
	public String imageSampleGroupName;
	public float sizeInScene = 1;
	public boolean useRelativeSizes = false;
	public PVector origin = new PVector(0.5f, 0.5f);

	/////////////////////////////////////////////////////
	// Below here : SpriteSeed instance data, different for each seed
	/////////////////////////////////////////////////////
	// the name of the image sample group to be used by
	// the number of the item within that group
	// The short name, which is derived usually from the file name (without extension)
	public int imageSampleGroupItemNumber = 0;
	public String imageSampleGroupItemShortName= "";

	/////////////////////////////////////////////////////
	// Geometric transforms applied
	// the doc point of the seed
	// used to position (translate) the item
	public float docPointX;
	public float docPointY;

	// scale
	public float scale = 1;

	//Rotation, in degrees clockwise
	//where 0 represent the "up" of the image
	public float rotation = 0;

	// flip in x and y
	public boolean flipX = false;
	public boolean flipY = false;

	/////////////////////////////////////////////////////
	// the depth is set to the normalised depth in the 3D scene, 
	// usually used to sort the render order of the seeds
	// 
	public float depth;

	// the id is a unique integer
	// This is used in seeding the sprite's random number generator, thereby ensuring the same random events happen to each seed
	// regardless of previous random events
	public int id;
	
	
	//public SpriteSeed() {}
	
	public SpriteSeed copy() {
		SpriteSeed cpy = new SpriteSeed();
		cpy.seedFontName = this.seedFontName;
		cpy.imageSampleGroupName = this.imageSampleGroupName;
		cpy.sizeInScene = this.sizeInScene;
		cpy.useRelativeSizes = this.useRelativeSizes;
		cpy.origin = this.origin.copy();
		
		cpy.imageSampleGroupItemNumber = this.imageSampleGroupItemNumber;
		cpy.imageSampleGroupItemShortName= this.imageSampleGroupItemShortName;
		
		cpy.docPointX = this.docPointX;
		cpy.docPointY = this.docPointY;
		cpy.scale  = this.scale;
		cpy.rotation  = this.rotation;
		cpy.flipX  = this.flipX;
		cpy.flipY  = this.flipY;
		cpy.depth = this.depth;
		cpy.id = this.id;
		return cpy;
		
	}

	public PVector getDocPoint() {
		return new PVector(docPointX, docPointY, 0);
	}


	public void setDocPoint(PVector p) {
		docPointX = p.x;
		docPointY = p.y;
	}

	PVector getDocPointWithDepth() {
		return new PVector(docPointX, docPointY, depth);
	}

	float getDepth() {
		return depth;
	}

	public void setDepth(float d) {
		depth = d;
	}



	public String getAsCSVStr() {
		// in the processing side of things
		// seeds are always set using normalised x,y location
		// in the createSeeds() method

		// if environment == JAVA
		PVector np = docSpaceToNormalisedSpace(new PVector(docPointX, docPointY));
		// if environment == PROCESSING
		// PVector np = new PVector(docPointX, docPointY));


		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.addKeyValue("SeedFontName", seedFontName);
		kvlist.addKeyValue("ImageSampleGroupName", imageSampleGroupName);
		
		kvlist.addKeyValue("SizeInScene",sizeInScene);
		kvlist.addKeyValue("UseRelativeSizes",useRelativeSizes);
		kvlist.addKeyValue("OriginX",origin.x);
		kvlist.addKeyValue("OriginY",origin.y);
		kvlist.addKeyValue("ImageSampleGroupItemNumber", imageSampleGroupItemNumber);
		kvlist.addKeyValue("ImageSampleGroupItemShortName", imageSampleGroupItemShortName);
		kvlist.addKeyValue("DocPointX", np.x);
		kvlist.addKeyValue("DocPointY", np.y);
		kvlist.addKeyValue("Scale", scale);
		kvlist.addKeyValue("Rotation", rotation);
		kvlist.addKeyValue("FlipX", flipX);
		kvlist.addKeyValue("FlipY", flipY);
		kvlist.addKeyValue("Depth", depth);
		kvlist.addKeyValue("Id", id);
		String line =  kvlist.getAsCSVLine();
		//System.out.println("made seed " + line);
		return line;

	}

	void setWithCSVStr(String csvStr) {
		// in the processing side of things
		// seeds are always set using normalised x,y location
		// in the createSeeds() method
		// It is then down to the application to translate this to the vehicle locations
		KeyValuePairList kvlist = new KeyValuePairList();
		kvlist.ingestCSVLine(csvStr);
		seedFontName = kvlist.getString("SeedFontName");
		imageSampleGroupName = kvlist.getString("ImageSampleGroupName");
		
		sizeInScene = kvlist.getFloat("SizeInScene");
		useRelativeSizes = kvlist.getBoolean("UseRelativeSizes");
		origin.x = kvlist.getFloat("OriginX");
		origin.y = kvlist.getFloat("OriginY");
		
		
		
		imageSampleGroupItemNumber = kvlist.getInt("ImageSampleGroupItemNumber");
		//System.out.println("Loading seed: imageSampleGroupItemNumber " + imageSampleGroupItemNumber);
		
		
		
		imageSampleGroupItemShortName = kvlist.getString("ImageSampleGroupItemShortName");
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

		//System.out.println("Loading seed: " + this.getAsCSVStr());

	}

	PVector normalisedSpaceToDocSpace(PVector normPt) {
		return GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace( normPt);
	}

	PVector docSpaceToNormalisedSpace(PVector docPt) {
		return GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(getDocPoint());
	}
	

}
