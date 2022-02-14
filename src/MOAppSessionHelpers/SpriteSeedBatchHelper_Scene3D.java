package MOAppSessionHelpers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.SceneData3D;

import MOImageCollections.SpriteImageGroupManager;
import MOMaths.PVector;
import MOMaths.Rect;
import MOPointGeneration.PackingInterpolationScheme;
import MOPointGeneration.PointGenerator_RadialPackSurface3D;

import MOSpriteSeed.SpriteSeed;
import MOSpriteSeed.SpriteSeedBatch;
import MOSpriteSeed.SpriteSeedFont;
import MOSpriteSeed.SpriteSeedFontBiome;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Bundles together any SeedBatch making/altering operations that are to do with a 3D scene
//



public class SpriteSeedBatchHelper_Scene3D {


	SceneData3D sceneData3D;
	
	PointGenerator_RadialPackSurface3D pointGenerator;
	SpriteSeedFontBiome imageSampleSelector;
	
	

	public SpriteSeedBatchHelper_Scene3D( SceneData3D sd3d, SpriteImageGroupManager isgm, int sampleSelectorRSeed) {
		imageSampleSelector = new SpriteSeedFontBiome(isgm, sampleSelectorRSeed);
		sceneData3D = sd3d;
		ensureSeedsDirectoryExists(GlobalSettings.getUserSessionPath());
	}
	
	
	public PointGenerator_RadialPackSurface3D definePointPacking(String namePointDisImage, PackingInterpolationScheme packingInterpolationScheme, int pointDistRSeed) {
		// sets up the pontGenerator for this seed batch
		// optionally returns the point generator if you need it
		sceneData3D.setCurrentRenderImage(namePointDisImage);
		BufferedImage pointDistributionImage = sceneData3D.getCurrentRenderImage();
		PointGenerator_RadialPackSurface3D pointField = new PointGenerator_RadialPackSurface3D(pointDistRSeed, sceneData3D);

		pointField.setMaskImage(sceneData3D.getSubstanceMaskImage());
		pointField.setPackingInterpolationScheme(packingInterpolationScheme, pointDistributionImage);
		pointGenerator = pointField;
		return pointField;
	}
	
	
	
	public SpriteSeedFontBiome getImageSampleSelector() {
		return imageSampleSelector;
	}
	
	
	public void addSpriteSeedFont(String sdFontName, String imageSampleGroupName, float sizeInScene, boolean useRelativeSizes, PVector origin, float probability) {
	
		imageSampleSelector.addSpriteSeedFont(sdFontName, imageSampleGroupName, sizeInScene, useRelativeSizes, origin, probability);
	
	}
	
	//public void clearSampleGroupCandidates() {
	//	imageSampleSelector.clearContentItemProbabilities();
	//}
	
	
	public SpriteSeedBatch generateSpriteSeedBatch(String batchName) {
		if(pointGenerator == null) {
			System.out.println("SeedBatchFactory_Scene3D::generateSeedBatch -  point packing is undefined , please call definePointPacking before using this method");
			return null;
		}
		
		//SpriteSeedFont seedFont = imageSampleSelector.getSpriteSeedFontInstance();
		
		
		SpriteSeedBatch seedbatch = new SpriteSeedBatch();
		
		String pathAndFileName = GlobalSettings.getUserSessionPath() + "seeds\\" + batchName + ".sds";
		
		
		ArrayList<PVector> points = pointGenerator.generatePoints();
		
		int n=0;
		for(PVector p: points) {
			
			SpriteSeed seedInstance = imageSampleSelector.getSpriteSeedInstance();
			seedInstance.setDocPoint(p);
			seedInstance.seedFontName = batchName;
			seedInstance.setDepth(p.z);
			seedbatch.addSpriteSeed(seedInstance);
			n++;
		}
		System.out.println("generateSeedBatch::has made a batch called " + batchName + " of " + seedbatch.getNumItems() + " seeds ");
		
		//ensureSeedsDirectoryExists(MOUtilGlobals.userSessionPath);

		return seedbatch;
		
	}
	
	
	private void ensureSeedsDirectoryExists(String path) {
		String alledgedDirectory = path + "seeds";
		if(MOStringUtils.checkDirectoryExist(alledgedDirectory)) return;
		MOStringUtils.createDirectory(alledgedDirectory);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// This is sort-of stand alone method that used to belong to an overcomplex class called SeedBatchManager
	// It is used to adjust the sees locations into the ROI space defined in the SceneData3D
	//
	public SpriteSeedBatch applyROIToSeeds(SpriteSeedBatch seedbatch) {
		// adjusts the document point of seeds from a seed batch of a whole scene (no ROI)
		// to a specific ROI within that scene by mapping the original doc points into the nw
		// doc space represented by the ROI
		
		// as this is quite destructive this method returns a new SeedBatch
		
		// when seeds are saved the locations are saved in normalised form
		// This is converted to the doc space of the host session upon loading
		
		// The ROIRect is stored in normalised form
		// so to convert into the the ROI of the host session
		// 1/ covert back to normalised form
		// 2/ 
		
		Rect theROI = sceneData3D.getROIRect();
		
		
		SpriteSeedBatch seedbatchOut = new SpriteSeedBatch();
		seedbatch.resetItemIterator();
		while(seedbatch.areItemsRemaining()) {
			
			SpriteSeed s = seedbatch.getNextSeed().copy();
			PVector newSceneDocPoint = s.getDocPoint();
			PVector normalisedPoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(newSceneDocPoint);
			//if(theROI.isPointInside(normalisedPoint)==false) continue;

			PVector newROIPoint = theROI.norm(normalisedPoint); // convert to normalised space within the roi
			PVector newDocSpacePt = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(newROIPoint);
			//System.out.println("applyROIToSeeds: seeds docpoint before appplication of ROI " + newSceneDocPoint.toString() + ". Adjusted by ROI " + newDocSpacePt.toString());
			s.setDocPoint(newDocSpacePt);
			
			seedbatchOut.addSpriteSeed(s);
			
		}
		
		
		


		System.out.println("applyROIToSeeds: seeds before appplication of ROI " + seedbatch.getNumItems() + ". Adjusted number of seeds in ROI " + seedbatchOut.getNumItems());
		return seedbatchOut;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// call this if you alter the depth-gamma of the scene after the seeds have been made
	//
	public void updateSeedDepthsAgainstScene(SpriteSeedBatch seedbatch) {
		
		// call this if you are changing to a different depth filter
		seedbatch.resetItemIterator();
		while(seedbatch.areItemsRemaining()) {
			
			SpriteSeed s = seedbatch.getNextSeed();
			float d = sceneData3D.getDepthNormalised(s.getDocPoint());
			s.setDepth(d);
		}
		seedbatch.resetItemIterator();
		
	}
	
	
	
	
}

