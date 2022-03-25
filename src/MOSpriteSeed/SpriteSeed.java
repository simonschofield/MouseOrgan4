package MOSpriteSeed;

import java.awt.image.BufferedImage;

import MOImageCollections.SpriteImageGroup;
import MOImageCollections.SpriteImageGroupManager;
import MOMaths.PVector;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePairList;
import MOUtils.UniqueID;
//////////////////////////////////////////////////////////////////////////////////
//A lightweight representation of everything you need to make a Sprite
// The strite has a SpriteSeed in it to store the "live sprite" data
// Can be saved as seedbatches
//

public class SpriteSeed {
	
	static UniqueID uniqueID;
	

	/////////////////////////////////////////////////////
	// This part: Sprite "Font" data, all instances are set with  this data from a specific SpriteSeedMaker
	//
	
	////////////////////////////////////////////////////
	// For Identification purposes
	// this enables the user to identify seeds from different batches, and sprite fonts within biomes (or individually) 
	public String spriteSeedBatchName = "";
	public String spriteSeedFontName = "";
	public String spriteImageGroupName = "";
	
	// the id is a unique integer. It is set by the SpriteSeed constructor from the static UniqueID class declared above. 
	// This is used in seeding the sprite's random number generator, thereby ensuring the same random events happen to each seed
	// regardless of previous random events
	// It is also used in optimisations such as registering whether or not a seed is used in a render.
	public int id;

	// the name of the image sample group to be used by
	// the number of the item within that group
	// The short name, which is derived usually from the file name (without extension)
	public int spriteImageGroupItemNumber = 0;
	public String spriteImageGroupItemShortName= "";
	
	
	
	////////////////////////////////////////////////////
	// Item size-in-scene data and origin (or pivot-point)
	//
	public float sizeInScene = 1;
	public boolean useRelativeSizes = false;
	public PVector origin = new PVector(0.5f, 0.5f);

	
	/////////////////////////////////////////////////////
	// Geometric transforms applied
	
	// the doc point of the seed
	// used to position (translate) the item
	public float docPointX = 0;
	public float docPointY = 0;

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
	public float depth = 1;

	// this is so that you can turn sprites on and off in clipping/culling processes
	// This is not saved/loaded to/from file
	public boolean isActive = true;
	
	public SpriteSeed() {
		if(uniqueID == null) {
			uniqueID = new UniqueID();
		}
		this.id = uniqueID.getUniqueID();
	}
	
	public SpriteSeed copy() {
		SpriteSeed cpy = new SpriteSeed();
		cpy.spriteSeedBatchName = this.spriteSeedBatchName;
		cpy.spriteSeedFontName = this.spriteSeedFontName;
		cpy.spriteImageGroupName = this.spriteImageGroupName;
		cpy.sizeInScene = this.sizeInScene;
		cpy.useRelativeSizes = this.useRelativeSizes;
		cpy.origin = this.origin.copy();
		
		cpy.spriteImageGroupItemNumber = this.spriteImageGroupItemNumber;
		cpy.spriteImageGroupItemShortName= this.spriteImageGroupItemShortName;
		
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
		kvlist.addKeyValue("SeedBatchName", spriteSeedBatchName);
		kvlist.addKeyValue("SeedFontName", spriteSeedFontName);
		kvlist.addKeyValue("spriteImageGroupName", spriteImageGroupName);
		kvlist.addKeyValue("spriteImageGroupItemNumber", spriteImageGroupItemNumber);
		kvlist.addKeyValue("spriteImageGroupItemShortName", spriteImageGroupItemShortName);
		
		kvlist.addKeyValue("SizeInScene",sizeInScene);
		kvlist.addKeyValue("UseRelativeSizes",useRelativeSizes);
		kvlist.addKeyValue("OriginX",origin.x);
		kvlist.addKeyValue("OriginY",origin.y);
		
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
		spriteSeedBatchName = kvlist.getString("SeedBatchName");
		spriteSeedFontName = kvlist.getString("SeedFontName");
		spriteImageGroupName = kvlist.getString("spriteImageGroupName");
		spriteImageGroupItemNumber = kvlist.getInt("spriteImageGroupItemNumber");
		spriteImageGroupItemShortName = kvlist.getString("spriteImageGroupItemShortName");
		
		sizeInScene = kvlist.getFloat("SizeInScene");
		useRelativeSizes = kvlist.getBoolean("UseRelativeSizes");
		origin.x = kvlist.getFloat("OriginX");
		origin.y = kvlist.getFloat("OriginY");
		

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
