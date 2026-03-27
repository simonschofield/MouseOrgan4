package MOSprite;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;

import MOApplication.ROIManager;
import MOCompositing.BufferedImageRenderTarget;
import MOImage.MOColor;
import MOMaths.PVector;
import MOMaths.Range;
import MOUtils.CollectionIterator;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePairList;
import MOUtils.MOStringUtils;

public class SpriteBatch extends CollectionIterator{


	public String thisSpriteBatchName = "";
	private ArrayList<Sprite> spriteList = new ArrayList<>();


	/**
	 * A SpriteBatch is a list of sprites, that are created ahead of the render. Typically, when a SpriteBatch is created, each sprite not contain the full set of information required to completes it's life cycle, but is used
	 * to establish the location of sprites (DocPoint), their scene depth (depth), the SpriteBatch 's name, so this can be used to associate the sprite with other assets, a UniqueID (each sprite has a unique integer ID) and a RandomKey.
	 * The RandomKey, which is non-unique,  is added to each sprite and used to guarantee that stochastic processes applied to a sprite are consistent b etween sessions.
	 * @param name - the name of the sprite batch. This is preserved against each sprite generated, and is maintained when collated into a larger sprite batch.
	 */
	public SpriteBatch(String name) {
		thisSpriteBatchName = name;

	}

	/**
	 * @return a copy of the sprite batch. The copy is semi-deep, in that list is a new list, but the sprites are copied by reference
	 */
	public SpriteBatch copy() {
		SpriteBatch cpy = new SpriteBatch(thisSpriteBatchName);
		cpy.spriteList = (ArrayList<Sprite>) this.spriteList.clone();
		return cpy;
	}

	/**
	 * Adds a sprite to the sprite batch. This will have had a selection of data already set in the sprite. E.g. SpriteBatchName, ID, RanKey, doc point, depth as part
	 * of the sprites generation process, perhaps in a point generator.
	 * @param s
	 */
	public void addSprite(Sprite s) {
		//s.SeedBatchName = seedBatchName;
		spriteList.add(s);
	}


	/**
	 * @return - The list of sprites contained as an array list
	 */
	public ArrayList<Sprite> getSpriteList(){
		return spriteList;
	}

	/**
	 * Called by the SceneInformationInspector to recover a sprite's full information from a Sprite's ID stored in the SpriteID RenderTargwet (if used).
	 * @param searchID - the unique sprite ID
	 * @return - the sprite of this ID
	 */
	public Sprite getSpriteFromID(int searchID) {

		for(Sprite s : spriteList) {
			int thisID = s.getID();
			if(thisID == searchID) {
				return s;
			}
		}

		return null;

	}

	/**
	 * Add the otherSpriteBatch to tend of the existing sprites in this batch. The name of the sprite batch maker, contained in otherSpriteBatch are maintained. 
	 * @param otherBatch
	 */
	public void append(SpriteBatch otherSpriteBatch) {
		// when appending the sprites maintain their original sprite batch names.
		// If you want to update their names to this spriteBatch then call
		// updateAllSpriteBatchName(String name)
		ArrayList<Sprite> otherSprites = otherSpriteBatch.getSpriteList();
		for(Sprite s : otherSprites) {
			spriteList.add(s);
		}

	}

	/**
	 * Legacy. Appends this sprite batch to the end of appendingToThisBatch. Sprite Batch Maker names are maintained.
	 * @param appendingToThisBatch
	 */
	public void appendTo(SpriteBatch appendingToThisBatch) {
		if(appendingToThisBatch==null) {
			System.out.println("SpriteBatch:appendTo ERROR -  the appendingToThisBatch is null - please instantiate beofore using" );
			return;
		}
		appendingToThisBatch.append(this);
		//System.out.println(" appended seedbatch " + appendingToThisBatch + " num seeds = " + appendingToThisBatch.getNumItems());
	}

	/**
	 * depth sorts the list of sprites on the depth (high depth -> low depth), so further sprites are earlier in the list
	 */
	public void depthSort() {
		spriteList.sort(Comparator.comparing(Sprite::getDepth).reversed());
	}
	

	/**
	 * @return - the depth extrema of the sprites in this sprite batch as a Range
	 */
	public Range getDepthExtrema() {
		Range depthExtrema = new Range();
		depthExtrema.initialiseForExtremaSearch();

		for(Sprite s : spriteList) {
			float d = s.getDepth();
			depthExtrema.addExtremaCandidate(d);
		}
		System.out.println("Sprite batch depth extrema " + depthExtrema.toStr());
		return depthExtrema;
	}



	/**
	 * @return - return an array list of all the document points (without depth, so p.z == 0)
	 */
	public ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<>();
		for(Sprite s : spriteList) {
			points.add(s.getDocPoint());
		}
		return points;
	}

	/**
	 * Legacy. Not sure this is ever used. Overwites the names of the Sprite Batch Makes in this sprite batch with name
	 * @param name
	 */
	public void setAllSpriteBatchName(String name){
		//
		KeyValuePairList kvpl = new KeyValuePairList();
		kvpl.addKeyValue("SpriteBatchName", name);
		for(Sprite s : spriteList) {
			s.setSpriteData(kvpl);
		}

	}

	
	/**
	 * @return - the path to the SpriteBatches folder used in each UserSession directory
	 */
	public String getSpriteBatchDirectoryPath() {
		return GlobalSettings.getUserSessionPath() + "SpriteBatches";
	}

	
	/**
	 * Given a specific ROI setting from the ROIManager, return the name of the ROISpriteBatch file name associated with this render
	 * @param rm
	 * @return
	 */
	public String getROISpriteBatchDataFileName(ROIManager rm) {
		//String roiname = rm.getCurrentROIName();
		String sessionname = GlobalSettings.getDocumentName();
		return getSpriteBatchDirectoryPath() + "//ROISpriteBatchData_" + sessionname + ".csv";
	}


	/**
	 * Given a specific current ROI name, get the full path its associated SpriteBatch file.
	 * @param shortname
	 * @return
	 */
	public String getROIFullFilePath(String shortname) {

		return getSpriteBatchDirectoryPath() + "//ROISpriteBatchData_" + shortname + ".csv";

	}


	/**
	 * @return the path and filename to the MasterSpriteBatchdata.csv file for this session
	 */
	public String getMasterSpriteBatchDataFileName() {
		return getSpriteBatchDirectoryPath() + "//MasterSpriteBatchData.csv";
	}

	
	/**
	 * saves the sprite batch data as a CSV file at the specified location. The data saved in each sprite is 
	 * "DocPoint", "UniqueID", "RandomKey",  "Depth"  and "SpriteBatchName"
	 * @param fileAndPath
	 */
	public void saveSpriteBatch(String fileAndPath, String[] include) {
		// there should be a directory in the project folder called seeds
		FileWriter csvWriter = null;
		try{
			csvWriter = new FileWriter(fileAndPath);

			
			//include = {"DocPoint", "UniqueID", "RandomKey", "PivotPoint", "Depth", "SizeInScene", "UseRelativeSizes", "ImageAssetGroupName", "ImageGroupItemShortName","SpriteBatchName"};
			int activeSprites = 0;
			for(Sprite s: spriteList){
				if(s.isActive)  {
					csvWriter.append(s.getSpriteDataAsCSVLine(include));
					activeSprites++;
				}
			}

			csvWriter.flush();
			csvWriter.close();
			//System.out.println("SeedBatch.saveSeedsAsCSV: saved " + activeSprites + " spritedata out of a total of " + spriteList.size());
		}// end try
		catch(Exception ex){
			System.out.println("SeedBatch.saveSeedsAsCSV: csv writer failed - "  + fileAndPath + ex);
		}


	}

	/**
	 * Loads a csv sprite batch file into this sprite batch. It will append to existing sprites, rather than overwrite them
	 * @param fileAndPath
	 */
	public void loadSpriteBatch(String fileAndPath) {
		// there should be a directory in the project folder called SpriteBatches
		try{
			BufferedReader csvReader = new BufferedReader(new FileReader(fileAndPath));

			String row;

			while ((row = csvReader.readLine()) != null) {

				// do something with the data

				Sprite s = new Sprite(false);
				s.setSpriteDataWithCSVLine(row);
				//System.out.println("Loading seed "+ s.getDocPoint().toStr());
				spriteList.add(s);
			}
			csvReader.close();
		} catch(Exception e){

			System.out.println("SpriteSeedBatch.loadSeedsAsCSV: csv reader failed - " + fileAndPath + e);
		}

	}


	/**
	 * This attempts to load a previously saved ROISpriteBatchData file, saved as an efficiency measure as only "contributing" sprites are saved to this file. 
	 * It may not exist (yet) as it needs to be generated, per-roi render, once the MasterSriteBatchData has been generated. If it does not exist then the MasterSriteBatchData file 
	 * is loaded, and the ROI rendered there of. The ROISpriteBatchData is then saved out for next time speed-ups.
	 * @param roiManager
	 */
	public void loadROISprites(ROIManager roiManager) {
		

		String MasterSpriteBatchFile = getMasterSpriteBatchDataFileName();
		String ROISpriteBatchFile = getROISpriteBatchDataFileName(roiManager);



		if(  MOStringUtils.checkFileExists(ROISpriteBatchFile)   ) {
			System.out.println("this ROI file exists " + ROISpriteBatchFile);
			loadSpriteBatch(ROISpriteBatchFile);
		} else {

			if( !MOStringUtils.checkFileExists(MasterSpriteBatchFile)  ) {
				System.out.println("this MasterSriteBatchData file does not exist - please generate ");
				return;
			}

			System.out.println("there is no ROI file so loading the master file  ");
			loadSpriteBatch(MasterSpriteBatchFile);
		}


	}



	/**
	 * Any changes to the MasterSpriteBatchDataFile will invalidate any associated Sub-ROI renders. So this should be called from the user session
	 * when any changes are made to the master-render. It will automatically delete all the sub ROI sprite batch files, so they have to be re-generated.
	 * @param roiManager
	 */
	public void deleteROISpriteBatchFiles(ROIManager roiManager) {
		// This should be called after the MasterSpriteBatch file has been re-saved, as, in doing so, any previously
		// saved ROI spritebatches are invalidated
		//

		System.out.println("removeROISpriteBatchFiles  ");

		ArrayList<String> roiNames = roiManager.getROINames();



		for(String thisROIName: roiNames ) {


			String target = getROIFullFilePath(thisROIName);

			//System.out.println("removing  " + target);

			if(  MOStringUtils.checkFileExists(target)   ) {

				MOStringUtils.deleteFile(target);

			}


		}

	}


	/**
	 * returns the number of sprites in this sprite batch
	 */
	@Override
	public int getNumItems() {
		// TODO Auto-generated method stub
		return spriteList.size();
	}

	/**
	 * internally used as part of the iterator base class
	 */
	@Override
	public Object getItem(int n) {
		// TODO Auto-generated method stub
		return spriteList.get(n);
	}


	/**
	 * called repeatedly in the UserSession to get the next sprite to process
	 * @return
	 */
	public Sprite getNextSprite() {
		return (Sprite) getNextItem();
	}



	
	/**
	 * Some crappy debug from years ago
	 * @param num
	 * @param message
	 */
	public void printSpriteLocations(int num, String message) {

		int n = 0;
		for(Sprite s : spriteList) {
			System.out.println(message  + s.getDocPoint().toStr());
			n++;
			if(n>=num) {
				break;
			}
		}


	}



	/**
	 * A debug method. Draws a dot for each sprite in the batch to the specified RenderTarget.
	 * @param col - colour of the dot
	 * @param dotDiameter - the dots pixel diameter. Not session scaled
	 * @param rt - the render target
	 */
	public void drawDocPoints(Color col, float dotDiameter, BufferedImageRenderTarget rt) {

		for(Sprite s : spriteList) {
			rt.drawPoint(s.getDocPoint(), col, dotDiameter);
		}
	}
	
	/**
	 * A debug method. Draws a dot for each sprite in the batch to the specified RenderTarget. Each different sprite batch source contained, will be drawn in 
	 * different colour (up to 12 different colours, using the MOColor.getBasic12ColorPalette(), anything over 12 different colours, and it re-uses the colours again)
	 * @param dotDiameter  - the dots pixel diameter. Not session scaled
	 * @param rt - the render target
	 */
	public void drawDifferentiatedDocPonts(float dotDiameter, BufferedImageRenderTarget rt) {
		ArrayList<String> uniquebatchNames = new ArrayList<String>();
		
		Color[] basicColors = MOColor.getBasic12ColorPalette();
		
		ArrayList<Sprite> seedList = getSpriteList();
		for(int n = 0; n < seedList.size(); n++) {
			Sprite seed = seedList.get(n);
			
			String spriteBatchName = seed.getSpiteDataString("SpriteBatchName");
			
			
			if( MOStringUtils.stringArrayListContains(uniquebatchNames, spriteBatchName)==false) {
				uniquebatchNames.add(spriteBatchName);
			}
			
			int ind = MOStringUtils.getIndexOfThisString(uniquebatchNames, spriteBatchName);
			if(ind==-1) {
				System.out.println("drawSeedPonts:: issue with unique batch name index -1");
				ind = 0;
			}
			if(ind >= 12) {
				// cannot draw more than 12 colours, so if the index is higher than 11, use the modular number
				ind = ind%12;
			}
			Color c = basicColors[ind];
	
			PVector p = seed.getDocPoint();
		
			rt.drawPoint(p, c, dotDiameter);
		}
		
		
	}


}

