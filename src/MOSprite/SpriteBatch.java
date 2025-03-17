package MOSprite;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;

import MOCompositing.BufferedImageRenderTarget;
import MOMaths.PVector;
import MOMaths.Range;
import MOScene3D.ROIManager;
import MOUtils.CollectionIterator;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePairList;
import MOUtils.MOStringUtils;

public class SpriteBatch extends CollectionIterator{
	
	public String thisSpriteBatchName = "";
	private ArrayList<Sprite> spriteList = new ArrayList<Sprite>();

	
	public SpriteBatch(){
		
	}
	
	public SpriteBatch(String name) {
		thisSpriteBatchName = name;
	}
	
	public SpriteBatch copy() {
		SpriteBatch cpy = new SpriteBatch(thisSpriteBatchName);
		cpy.spriteList = (ArrayList<Sprite>) this.spriteList.clone();
		return cpy;
	}
	
	public void addSprite(Sprite s) {
		//s.SeedBatchName = seedBatchName;
		spriteList.add(s);
	}


	public ArrayList<Sprite> getSpriteList(){
		return spriteList;
	}
	
	
	public void setSpriteList(ArrayList<Sprite> sprites) {
		spriteList = sprites;
	}
	
	
	
	public void append(SpriteBatch otherBatch) {
		// when appending the sprites maintain their original sprite batch names.
		// If you want to update their names to this spriteBatch then call
		// updateAllSpriteBatchName(String name)
		ArrayList<Sprite> otherSprites = otherBatch.getSpriteList();
		for(Sprite s : otherSprites) {
			spriteList.add(s);
		}
		
	}
	
	public void appendTo(SpriteBatch appendingToThisBatch) {
		if(appendingToThisBatch==null) {
			System.out.println("SpriteBatch:appendTo ERROR -  the appendingToThisBatch is null - please instantiate beofore using" );
			return;
		}
		appendingToThisBatch.append(this);
		//System.out.println(" appended seedbatch " + appendingToThisBatch + " num seeds = " + appendingToThisBatch.getNumItems());
	}
	
	public void depthSort() {
		spriteList.sort(Comparator.comparing(Sprite::getDepth).reversed());
	}
	
	public Range getDepthExtrema() {
		Range depthExtrema = new Range();
		depthExtrema.initialiseForExtremaSearch();
		
		for(Sprite s : spriteList) {
			float d = s.getDepth();
			depthExtrema.addExtremaCandidate(d);
		}
		System.out.println("getting sprite batch depth extrema " + depthExtrema.toStr());
		return depthExtrema;
	}

	public ArrayList<PVector> getPoints(){
		ArrayList<PVector> points = new ArrayList<PVector>();
		for(Sprite s : spriteList) {
			points.add(s.getDocPoint());
		}
		return points;
	}
	
	public void setAllSpriteBatchName(String name){
		// 
		KeyValuePairList kvpl = new KeyValuePairList();
		kvpl.addKeyValue("SpriteBatchName", name);
		for(Sprite s : spriteList) {
			s.setSpriteData(kvpl);
		}
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public String getSpriteBatchDirectoryPath() {
		return GlobalSettings.getUserSessionPath() + "SpriteBatches";
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public String getROISpriteBatchDataFileName(ROIManager rm) {
		String roiname = rm.getCurrentROIName();
		String sessionname = GlobalSettings.getDocumentName();
		return getSpriteBatchDirectoryPath() + "//ROISpriteBatchData_" + sessionname + ".csv";
	}
	
	
	public String getMasterSpriteBatchDataFileName() {
		return getSpriteBatchDirectoryPath() + "//MasterSpriteBatchData.csv";
	}

	///////////////////////////////////////////////////////
	// load and save seeds using csv
	public void saveSpriteData(String fileAndPath) {
		// there should be a directory in the project folder called seeds
		FileWriter csvWriter = null;
		try{
			csvWriter = new FileWriter(fileAndPath);

			String[] include = {"DocPoint", "UniqueID", "RandomKey",  "Depth" ,"SpriteBatchName"};
			// String[] include = {"DocPoint", "UniqueID", "RandomKey", "PivotPoint", "Depth", "SizeInScene", "UseRelativeSizes", "ImageAssetGroupName", "ImageGroupItemShortName","SpriteBatchName"};
			int activeSprites = 0;
			for(Sprite s: spriteList){
				if(s.isActive)  {
					csvWriter.append(s.getSpriteDataAsCSVLine(include));
					activeSprites++;
				}
			}

			csvWriter.flush();
			csvWriter.close();
			System.out.println("SeedBatch.saveSeedsAsCSV: saved " + activeSprites + " spritedata out of a total of " + spriteList.size());
		}// end try
		catch(Exception ex){
			System.out.println("SeedBatch.saveSeedsAsCSV: csv writer failed - "  + fileAndPath + ex);
		}


	}

	public void loadSpriteData(String fileAndPath) {
		// there should be a directory in the project folder called seeds
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

	//private void ensureSpriteBatchDirectoryExists(String path) {
	//	String alledgedDirectory = path + "SpriteBatches";
	//	if(MOStringUtils.checkDirectoryExist(alledgedDirectory)) return;
	//	MOStringUtils.createDirectory(alledgedDirectory);
	//}
	
	public void loadROISprites(ROIManager roiManager) {
		// called if not generating the master collection of sprite data
		// from the master session there should be a file called "MasterSriteBatchData.csv" in the seeds folder 
		// There may also be a previously saved ROISpriteBatchFile called ROISpriteBatchData_roiname.csv. .. If this exists then load this file
		// else load the masterSpriteBatchdata and shift into ROI space
		String MasterSpriteBatchFile = getMasterSpriteBatchDataFileName();
		String ROISpriteBatchFile = getROISpriteBatchDataFileName(roiManager);

		
		
		if(  MOStringUtils.checkFileExists(ROISpriteBatchFile)   ) {
			System.out.println("this ROI file exists " + ROISpriteBatchFile);
			loadSpriteData(ROISpriteBatchFile);
		} else {
			
			if( MOStringUtils.checkFileExists(MasterSpriteBatchFile) == false  ) {
				System.out.println("this MasterSriteBatchData file does not exist - please generate ");
				return;
			}
			
			System.out.println("there is no ROI file so loading the master file  ");
			loadSpriteData(MasterSpriteBatchFile);
			
			// only use if the file loaded the master seeds collated seeds
			SpriteBatch shiftedSpriteBatch = roiManager.applyROIToSpriteBatch(this);
			System.out.println("shifter master sprite batch has  " + shiftedSpriteBatch.getNumItems() + " sprites");
			this.spriteList = (ArrayList<Sprite>) shiftedSpriteBatch.spriteList.clone();
			this.resetItemIterator();
		}
		
		
	}
	

	@Override
	public int getNumItems() {
		// TODO Auto-generated method stub
		return spriteList.size();
	}

	@Override
	public Object getItem(int n) {
		// TODO Auto-generated method stub
		return spriteList.get(n);
	}


	public Sprite getNextSprite() {
		return (Sprite) getNextItem();
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////
	public void printSpriteLocations(int num, String message) {
		
		int n = 0;
		for(Sprite s : spriteList) {
			System.out.println(message  + s.getDocPoint().toStr());
			n++;
			if(n>=num) break;
		}
		
		
	}
	
	
	
	public void drawDocPoint(Color col, float pixelradius, BufferedImageRenderTarget rt) {
		
		for(Sprite s : spriteList) {
			rt.drawPoint(s.getDocPoint(), col, pixelradius);
		}
	}


}

