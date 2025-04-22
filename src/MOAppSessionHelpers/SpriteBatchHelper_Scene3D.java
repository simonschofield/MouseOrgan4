package MOAppSessionHelpers;





import java.util.ArrayList;

import MOApplication.MainDocument;
//import MOCompositing.ContributingSpritesList;
import MOMaths.PVector;
import MOMaths.Rect;
import MOPointGeneration.PackingInterpolationScheme;
import MOPointGeneration.OLD_PointGenerator_RadialPackSurface3D;
import MOPointGeneration.PointGenerator_SurfacePack3D;
import MOScene3D.SceneData3D;
import MOSprite.Sprite;
import MOSprite.SpriteBatch;
//import MOSprite.SpriteSeed;
//import MOSprite.SpriteSeedBatch;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePairList;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// A helper class. Produces a sprite batch from scene 3D data using a packing algorithm
// A sprite batch is a list of sprites. The spites have a hash-field called SpriteBatchName
// When sprite batches are appended together the sprites maintain their original SpriteBatchName 
//
// There is one long-lived pointGenerator utilised by the class, which is re-purposed if used multiple times. This enables the user to set any of the packing settings
// before point generation happens.
public class SpriteBatchHelper_Scene3D {
	
	PointGenerator_SurfacePack3D pointGenerator;
	String currentSpriteBatchName;
	
	
	public SpriteBatchHelper_Scene3D(SceneData3D sd) {
		pointGenerator = new PointGenerator_SurfacePack3D(sd);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Packing settings
	//
	
	public void setDepthThinning(float farMultiplier, float nearThreshold) {
		pointGenerator.setDepthSensitivePacking(farMultiplier, nearThreshold);
	}
	
	public void setExtentsRect(Rect r) {
		pointGenerator.setGenerationArea(r);
	}
	
	public void setMaxNumPoints(int n) {
		pointGenerator.setMaxNumPointsLimit(n);
	}
	
	public void setRandomStreamSeed(int rk) {
		pointGenerator.setRandomStreamSeed(rk);
		
	}
	
	///////////////////////////////////////////////////////////////////////
	// returns a sprite batch, using a default packing radius
	//
	//
	public SpriteBatch createSpriteBatch3D(String name, float radius, int seedRandomKey, int maxNumPoints) {
		currentSpriteBatchName = name;
		pointGenerator.setDefaultPackingRadius(radius);
		pointGenerator.setMaxNumPointsLimit(maxNumPoints);
		pointGenerator.setRandomStreamSeed(seedRandomKey);
		return generateSpriteBatch();
	}
	
	///////////////////////////////////////////////////////////////////////
	// returns a sprite batch, using a Packing Interpolation Scheme based on an image
	//
	//
	public SpriteBatch createSpriteBatch3D(String name, String packingImage, float controlValMin, float controlValMax, float radAtControlMin, float radAtControlMax, int pointPackingRanSeed, int seedRandomKey, int maxNumPoints) {
			
			PackingInterpolationScheme interpolationScheme = new PackingInterpolationScheme( controlValMin,  controlValMax,  radAtControlMin,  radAtControlMax, PackingInterpolationScheme.CLAMP,  PackingInterpolationScheme.EXCLUDE); 
			//SpriteBatchHelper_Scene3D seedBatchHelper = new SpriteBatchHelper_Scene3D(sceneData3D);
			currentSpriteBatchName = name;
			
			pointGenerator.setRandomStreamSeed(seedRandomKey);
			pointGenerator.setPackingImage(packingImage);
			pointGenerator.setPackingInterpolationScheme(interpolationScheme);
	
			pointGenerator.setMaxNumPointsLimit(maxNumPoints);
			return generateSpriteBatch();
		} 
	
	
	

	
	///////////////////////////////////////////////////////////////////////
	// private methods
	//
	//
	private SpriteBatch generateSpriteBatch() {
		if(pointGenerator == null) {
			System.out.println("SeedBatchFactory_Scene3D::generateSpriteSeedBatch -  point packing is undefined , please call definePointPacking before using this method");
			return null;
		}
		
		ensureSpriteBatchDirectoryExists(GlobalSettings.getUserSessionPath());
		
		SpriteBatch spriteBatch = new SpriteBatch(currentSpriteBatchName);
		
		System.out.println("Generating sprites " + currentSpriteBatchName );
		
		
		ArrayList<PVector> points = pointGenerator.generatePoints();
		
		
		for(PVector p: points) {
			Sprite sprite = new Sprite(true);
			// sprites create their own UniqueID and randomKey on instantiation, then add in
			//"DocPoint", "Depth" ,"SpriteBatchName";
			KeyValuePairList kvpl = new KeyValuePairList();
			float depth = p.z;
			p.z = 0;
			
			kvpl.addKeyValue("DocPoint", p.array());
			kvpl.addKeyValue("Depth", depth);
			kvpl.addKeyValue("SpriteBatchName", currentSpriteBatchName);
			sprite.setSpriteData(kvpl);
	
			spriteBatch.addSprite(sprite);

		}

		return spriteBatch;
		
	}


	private void ensureSpriteBatchDirectoryExists(String path) {
		String alledgedDirectory = path + "SpriteBatches";
		if(MOStringUtils.checkDirectoryExist(alledgedDirectory)) return;
		MOStringUtils.createDirectory(alledgedDirectory);
	}
	

	
}


