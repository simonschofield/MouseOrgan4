package MOZZZ_Depricated;


import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOApplication.MainDocument;
//import MOCompositing.ContributingSpritesList;
import MOMaths.PVector;
import MOMaths.Rect;
import MOPointGeneration.PackingInterpolationScheme;
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
public class OLD_SpriteBatchMaker_Scene3D {
	String thisHelperName;
	
	//SceneData3D sceneData3D;
	
	//PointGenerator_RadialPackSurface3D pointGenerator;
	PointGenerator_SurfacePack3D pointGenerator;
	
	float depthSensitivePacking_FarMultiplier = 1f;
	float depthSensitivePacking_nearThreshold = 1f;;
	
	Rect extentsRect;
	
	
	// call this before creating the seedbatches. They should all have the same depth-sensitive packing
	public void setDepthThinning(float farMultiplier, float nearThreshold) {
		depthSensitivePacking_FarMultiplier = farMultiplier;
		depthSensitivePacking_nearThreshold = nearThreshold;
	}
	
	public void setExtentsRect(Rect r) {
		extentsRect = r;
	}
	
	
	

	// call this to return a new seedbatch
	public SpriteBatch createSpriteBatch3D(String name, String packingImage, float controlValMin, float controlValMax, float radAtControlMin, float radAtControlMax, int pointPackingRanSeed, int seedRandomKey, int maxNumPoints) {
		ensureSpriteBatchDirectoryExists(GlobalSettings.getUserSessionPath());
		PackingInterpolationScheme interpolationScheme = new PackingInterpolationScheme( controlValMin,  controlValMax,  radAtControlMin,  radAtControlMax, PackingInterpolationScheme.CLAMP,  PackingInterpolationScheme.EXCLUDE); 
		//SpriteBatchHelper_Scene3D seedBatchHelper = new SpriteBatchHelper_Scene3D(sceneData3D);
		thisHelperName = name;
		definePointPacking(packingImage, interpolationScheme, pointPackingRanSeed);
		pointGenerator.setDepthSensitivePacking(depthSensitivePacking_FarMultiplier, depthSensitivePacking_nearThreshold);
		
		if(extentsRect != null) {
			pointGenerator.setGenerationArea(extentsRect);
		}
		
		setMaxNumPoints(maxNumPoints);
		return generateSpriteBatch(seedRandomKey);
	} 
	
	

	
	
	public PointGenerator_SurfacePack3D definePointPacking(String namePointDisImage, PackingInterpolationScheme packingInterpolationScheme, int pointDistRSeed) {
		// sets up the pontGenerator for this seed batch
		// optionally returns the point generator if you need it
		SceneData3D sceneData3D = GlobalSettings.getSceneData3D();
		sceneData3D.setCurrentRenderImage(namePointDisImage);
		
		float maxRadius = packingInterpolationScheme.getMaxRadius();
		pointGenerator = new PointGenerator_SurfacePack3D(sceneData3D);
		pointGenerator.setPackingInterpolationScheme(packingInterpolationScheme);
		return pointGenerator;
	}
	
	
	void setMaxNumPoints(int n) {
		pointGenerator.setMaxNumPointsLimit(n);
	}
	

	public SpriteBatch generateSpriteBatch(int ranKey) {
		if(pointGenerator == null) {
			System.out.println("SeedBatchFactory_Scene3D::generateSpriteSeedBatch -  point packing is undefined , please call definePointPacking before using this method");
			return null;
		}
		
		
		SpriteBatch spriteBatch = new SpriteBatch(thisHelperName);
		
		System.out.println("Generating sprites " + thisHelperName );
		
		pointGenerator.setRandomStreamSeed(ranKey);
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
			kvpl.addKeyValue("SpriteBatchName", thisHelperName);
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

