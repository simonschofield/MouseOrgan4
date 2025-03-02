package MOSprite;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;

import MOCompositing.BufferedImageRenderTarget;
import MOMaths.PVector;
import MOMaths.Range;
import MOUtils.CollectionIterator;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;

public class SpriteBatch extends CollectionIterator{
	
	
	private ArrayList<Sprite> spriteList = new ArrayList<Sprite>();

	
	public SpriteBatch(){
		
	}
	
	
	public SpriteBatch copy() {
		SpriteBatch cpy = new SpriteBatch();
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

	
	//public void setSeedBatchNameInSeeds(String newName) {
		// overwrites the seedbatch name with this name
	///	for(Sprite s: spriteList){
	//		s.setSpriteData(new KeyValuePair("SpriteBatchName", newName));
			//s.SeedBatchName = newName;
	//	}
	//	
	//}

	///////////////////////////////////////////////////////
	// load and save seeds using csv
	public void saveSpriteData(String fileAndPath) {
		// there should be a directory in the project folder called seeds
		FileWriter csvWriter = null;
		try{
			csvWriter = new FileWriter(fileAndPath);

			String[] include = {"DocPoint", "UniqueID", "RandomKey",  "Depth" ,"SpriteBatchName"};
			// String[] include = {"DocPoint", "UniqueID", "RandomKey", "PivotPoint", "Depth", "SizeInScene", "UseRelativeSizes", "ImageAssetGroupName", "ImageGroupItemShortName","SpriteBatchName"};
			for(Sprite s: spriteList){
				csvWriter.append(s.getSpriteDataAsCSVLine(include));
			}

			csvWriter.flush();
			csvWriter.close();

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
				Sprite s = new Sprite();
				s.setSpriteDataWithCSVLine(row);
				//System.out.println("Loading seed "+ s.getDocPoint().toStr());
				spriteList.add(s);
			}
			csvReader.close();
		} catch(Exception e){

			System.out.println("SpriteSeedBatch.loadSeedsAsCSV: csv reader failed - " + fileAndPath + e);
		}

	}

	
	//public void convertSeedsFromNormalisedSpaceToDocSpace() {
	//	// should the seeds be generated b y an external application, it is likely the seeds will have been saved
		// with normalised coordinates. This turns normalised into docspace points using the current global coordinate system
	//	for(Sprite s: spriteList){
	//		PVector normCoord = s.getDocPoint();
	//		PVector docSpaceCoord = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(normCoord);
	//		s.setDocPoint(docSpaceCoord);
	//	}
		
		
	//}
	

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

