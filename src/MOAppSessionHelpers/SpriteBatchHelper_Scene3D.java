package MOAppSessionHelpers;


import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOApplication.MainDocument;
import MOCompositing.ContributingSpritesList;
import MOMaths.PVector;
import MOMaths.Rect;
import MOPointGeneration.PackingInterpolationScheme;
import MOPointGeneration.PointGenerator_RadialPackSurface3D;
import MOScene3D.SceneData3D;
import MOSprite.Sprite;
import MOSprite.SpriteBatch;
//import MOSprite.SpriteSeed;
//import MOSprite.SpriteSeedBatch;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePairList;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Bundles a single SpriteFontBiome with a 3D point generator
// This can then generate a seed batch from that biome, that can be used directly or saved and reloaded by this class
// If you need more than one biome, then declare more than one of this class
//

public class SpriteBatchHelper_Scene3D {
	String thisHelperName;
	
	SceneData3D sceneData3D;
	
	PointGenerator_RadialPackSurface3D pointGenerator;
	
	
	//boolean saveOutContributingSeedReport = false;

	public SpriteBatchHelper_Scene3D(SceneData3D sd3d) {
		sceneData3D = sd3d;
		ensureSeedsDirectoryExists(GlobalSettings.getUserSessionPath());
	}
	
//	public SpriteBatch createSpriteBatch3D(String name, String packingImage, float controlValMin, float controlValMax, float radAtControlMin, float radAtControlMax, int pointPackingRanSeed, int seedRandomKey, int maxNumPoints) {
//		PackingInterpolationScheme interpolationScheme = new PackingInterpolationScheme( controlValMin,  controlValMax,  radAtControlMin,  radAtControlMax, PackingInterpolationScheme.EXCLUDE,  PackingInterpolationScheme.CLAMP); 
//		SpriteBatchHelper_Scene3D seedBatchHelper = new SpriteBatchHelper_Scene3D(sceneData3D);
//		seedBatchHelper.thisHelperName = name;
//		seedBatchHelper.definePointPacking(packingImage, interpolationScheme, pointPackingRanSeed);
//		seedBatchHelper.setDepthSensitivePacking(0.5f, 125);
//		seedBatchHelper.setMaxNumPoints(maxNumPoints);
//		return seedBatchHelper.generateSpriteBatch(seedRandomKey);
//	} 
	
	
	public SpriteBatch createSpriteBatch3D(String name, String packingImage, float controlValMin, float controlValMax, float radAtControlMin, float radAtControlMax, int pointPackingRanSeed, int seedRandomKey, int maxNumPoints) {
		PackingInterpolationScheme interpolationScheme = new PackingInterpolationScheme( controlValMin,  controlValMax,  radAtControlMin,  radAtControlMax, PackingInterpolationScheme.EXCLUDE,  PackingInterpolationScheme.CLAMP); 
		//SpriteBatchHelper_Scene3D seedBatchHelper = new SpriteBatchHelper_Scene3D(sceneData3D);
		thisHelperName = name;
		definePointPacking(packingImage, interpolationScheme, pointPackingRanSeed);
		setDepthSensitivePacking(0.5f, 125);
		setMaxNumPoints(maxNumPoints);
		return generateSpriteBatch(seedRandomKey);
	} 
	
	
	public PointGenerator_RadialPackSurface3D definePointPacking(String namePointDisImage, PackingInterpolationScheme packingInterpolationScheme, int pointDistRSeed) {
		// sets up the pontGenerator for this seed batch
		// optionally returns the point generator if you need it
		sceneData3D.setCurrentRenderImage(namePointDisImage);
		BufferedImage pointDistributionImage = sceneData3D.getCurrentRenderImage(true);
		PointGenerator_RadialPackSurface3D pointField = new PointGenerator_RadialPackSurface3D(pointDistRSeed, sceneData3D);

		pointField.setMaskImage(sceneData3D.getSubstanceMaskImage(true));
		pointField.setPackingInterpolationScheme(packingInterpolationScheme, pointDistributionImage);
		pointGenerator = pointField;
		return pointField;
	}
	
	
	void setMaxNumPoints(int n) {
		pointGenerator.setMaxNumPointsLimit(n);
	}
	
	public void setDepthSensitivePacking(float farMultiplier, float nearThreshold) {
		pointGenerator.setDepthSensitivePacking(farMultiplier, nearThreshold);
	}
	
	
	public SpriteBatch generateSpriteBatch(int ranKey) {
		if(pointGenerator == null) {
			System.out.println("SeedBatchFactory_Scene3D::generateSpriteSeedBatch -  point packing is undefined , please call definePointPacking before using this method");
			return null;
		}
		
		
		SpriteBatch spriteBatch = new SpriteBatch();
		
		System.out.println("Generating sprites " + thisHelperName );
		
		pointGenerator.setRandomStreamSeed(ranKey);
		ArrayList<PVector> points = pointGenerator.generatePoints();
		
		
		for(PVector p: points) {
			// here1234
			Sprite sprite = new Sprite();
			//"DocPoint", "UniqueID", "RandomKey",  "Depth" ,"SpriteBatchName"};
			KeyValuePairList kvpl = new KeyValuePairList();
			float depth = p.z;
			p.z = 0;
			
			kvpl.addKeyValue("DocPoint", p.array());
			kvpl.addKeyValue("Depth", depth);
			kvpl.addKeyValue("SpriteBatchName", thisHelperName);
			
			
			sprite.setSpriteData(kvpl);
			
			//sprite.setDocPoint(p);
			//sprite.setDepth(p.z);
			//sprite.SeedBatchName = thisHelperName; 
			
			
			
			
			spriteBatch.addSprite(sprite);
			
			
		}
		
		
		// tbd - UniqueID and randomKey are set on sprite instantiation inside the sprite
		

		return spriteBatch;
		
	}

	

	

	
	private void ensureSeedsDirectoryExists(String path) {
		String alledgedDirectory = path + "seeds";
		if(MOStringUtils.checkDirectoryExist(alledgedDirectory)) return;
		MOStringUtils.createDirectory(alledgedDirectory);
	}
	

	
}

