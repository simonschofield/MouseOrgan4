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

import MOSprite.SpriteSeed;
import MOSprite.SpriteSeedBatch;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Bundles a single SpriteFontBiome with a 3D point generator
// This can then generate a seed batch from that biome, that can be used directly or saved and reloaded by this class
// If you need more than one biome, then declare more than one of this class
//

public class SpriteSeedBatchHelper_Scene3D {
	String thisHelperName;
	
	SceneData3D sceneData3D;
	
	PointGenerator_RadialPackSurface3D pointGenerator;
	
	
	boolean saveOutContributingSeedReport = false;

	public SpriteSeedBatchHelper_Scene3D(String name,  SceneData3D sd3d) {
		this.thisHelperName = name;
		
		sceneData3D = sd3d;
		ensureSeedsDirectoryExists(GlobalSettings.getUserSessionPath());
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

	
	public SpriteSeedBatch generateSpriteSeedBatch(int randomKeySeed) {
		if(pointGenerator == null) {
			System.out.println("SeedBatchFactory_Scene3D::generateSpriteSeedBatch -  point packing is undefined , please call definePointPacking before using this method");
			return null;
		}
		
		
		SpriteSeedBatch seedbatch = new SpriteSeedBatch();
		
		System.out.println("Generating seeds " + thisHelperName );
		
		ArrayList<PVector> points = pointGenerator.generatePoints();
		
		
		for(PVector p: points) {
			// here1234
			SpriteSeed seedInstance = new SpriteSeed(randomKeySeed++);
			seedInstance.setDocPoint(p);
			seedInstance.setDepth(p.z);
			seedInstance.SeedBatchName = thisHelperName;
			seedbatch.addSpriteSeed(seedInstance);
			
			
		}
		
		
		
		

		return seedbatch;
		
	}
	

	
	private void ensureSeedsDirectoryExists(String path) {
		String alledgedDirectory = path + "seeds";
		if(MOStringUtils.checkDirectoryExist(alledgedDirectory)) return;
		MOStringUtils.createDirectory(alledgedDirectory);
	}
	

	
}

