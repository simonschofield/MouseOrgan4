package MOAppSessionHelpers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOApplication.MainDocument;
import MOCompositing.SpriteCropDecisionList;
import MOImageCollections.ScaledImageAssetGroupManager;
import MOMaths.PVector;
import MOMaths.Rect;
import MOPointGeneration.PackingInterpolationScheme;
import MOPointGeneration.PointGenerator_RadialPackSurface3D;
import MOScene3D.SceneData3D;
import MOSprite.SpriteData;
import MOSprite.SpriteDataBatch;
import MOSprite.SpriteFont;
import MOSprite.SpriteFontBiome;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Bundles a single seed font biome with a 3D point generator
// This can then generate a seed batch from that biome, that can be used directly or saved and reloaded by this class
// If you need more than one biome, then declare more than one of this class
//

public class SpriteDataBatchHelper_Scene3D {
	SceneData3D sceneData3D;
	
	PointGenerator_RadialPackSurface3D pointGenerator;
	SpriteFontBiome seedFontBiome;
	
	boolean saveOutContributingSeedReport = false;

	public SpriteDataBatchHelper_Scene3D( SceneData3D sd3d, int biomeRanSeed) {
		seedFontBiome = new SpriteFontBiome(biomeRanSeed);
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
	
	
	
	public SpriteFontBiome getSeedFontBiome() {
		return seedFontBiome;
	}
	
	
	public void addSpriteFont(String sdFontName, String imageSampleGroupName, float sizeInScene, boolean useRelativeSizes, PVector origin, int fontRanSeed, float probability) {
	
		seedFontBiome.addSpriteFont(sdFontName, imageSampleGroupName, sizeInScene, useRelativeSizes, origin, fontRanSeed, probability);
	
	}
	
	public void addSpriteFont(SpriteFont ssf) {
		
		seedFontBiome.addSpriteFont(ssf);
	
	}
	
	//public void clearSampleGroupCandidates() {
	//	imageSampleSelector.clearContentItemProbabilities();
	//}
	
	
	public SpriteDataBatch generateSpriteDataBatch(String batchName) {
		if(pointGenerator == null) {
			System.out.println("SeedBatchFactory_Scene3D::generateSeedBatch -  point packing is undefined , please call definePointPacking before using this method");
			return null;
		}
		
		//SpriteSeedFont seedFont = imageSampleSelector.getSpriteSeedFontInstance();
		
		
		SpriteDataBatch seedbatch = new SpriteDataBatch(batchName);
		
		String pathAndFileName = GlobalSettings.getUserSessionPath() + "seeds\\" + batchName + ".sds";
		
		
		ArrayList<PVector> points = pointGenerator.generatePoints();
		
		int n=0;
		for(PVector p: points) {
			
			SpriteData seedInstance = seedFontBiome.getSpriteDataInstance();
			seedInstance.setDocPoint(p);
			seedInstance.SpriteDataBatchName = batchName;
			seedInstance.setDepth(p.z);
			seedbatch.addSpriteData(seedInstance);
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
	public SpriteDataBatch applyROIToSpriteDataBatch(ROIHelper roiHelper, SpriteDataBatch seedbatch) {
		if(roiHelper.isUsingMaster()) return seedbatch;
		
		
		removeNoncontributingSpritesInROI( roiHelper,   seedbatch);
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
		
		System.out.println("apply ROI to seeds " + theROI.toStr());
		SpriteDataBatch seedbatchOut = new SpriteDataBatch(seedbatch.getName());
		seedbatch.resetItemIterator();
		while(seedbatch.areItemsRemaining()) {
			
			SpriteData s = seedbatch.getNextSeed().copy();
			PVector newSceneDocPoint = s.getDocPoint();
			PVector normalisedPoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(newSceneDocPoint);
			//if(theROI.isPointInside(normalisedPoint)==false) continue;

			PVector newROIPoint = theROI.norm(normalisedPoint); // convert to normalised space within the roi
			PVector newDocSpacePt = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(newROIPoint);
			//System.out.println("applyROIToSeeds: seeds docpoint before appplication of ROI " + newSceneDocPoint.toString() + ". Adjusted by ROI " + newDocSpacePt.toString());
			s.setDocPoint(newDocSpacePt);
			
			seedbatchOut.addSpriteData(s);
			
		}
		
		
		
		seedbatchOut.resetItemIterator();

		System.out.println("applyROIToSeeds: seeds before appplication of ROI " + seedbatch.getNumItems() + ". Adjusted number of seeds in ROI " + seedbatchOut.getNumItems());
		return seedbatchOut;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// call this if you alter the depth-gamma of the scene after the seeds have been made
	//
	public void updateSpriteDataBatchDepthsAgainstScene(SpriteDataBatch seedbatch) {
		
		// call this if you are changing to a different depth filter
		seedbatch.resetItemIterator();
		while(seedbatch.areItemsRemaining()) {
			
			SpriteData s = seedbatch.getNextSeed();
			float d = sceneData3D.getDepthNormalised(s.getDocPoint());
			s.setDepth(d);
		}
		seedbatch.resetItemIterator();
		
	}
	
	
	public boolean removeNoncontributingSpritesInROI(ROIHelper roiHelper, SpriteDataBatch seedbatch) {
		// this only removed seeds if a "contributing sprite" file has been saved for this ROI (i.e. with the ROI's name) in the seeds folder
		// if the file cannot be found, then the class is alerted to save one out at the end of this session
		if(roiHelper.isUsingMaster()) return false;
		String roiname = roiHelper.getCurrentROIName();
		//SpriteCropDecisionList spriteCropList = theDocument.getRenderBorder().getSpriteCropDecisionList();
		SpriteCropDecisionList spriteCropList = new SpriteCropDecisionList();
		boolean loadResult = spriteCropList.load(GlobalSettings.getUserSessionPath() + "seeds//contributingSprites_" + roiname + ".csv");
		if(loadResult == false) {
			saveOutContributingSeedReport = true;
			return false;
		}
		spriteCropList.removeNonContributingSprite(seedbatch);
		return true;
	}
	
	public void saveContributingSpritesReport(ROIHelper roiHelper, MainDocument theDocument, boolean forcesave) {
		// called at the end of the session
		if(roiHelper.isUsingMaster()) return;
		if(forcesave) saveOutContributingSeedReport = true;
		if(saveOutContributingSeedReport==false) return;
		String roiname = roiHelper.getCurrentROIName();
		theDocument.getRenderBorder().getSpriteCropDecisionList().save(GlobalSettings.getUserSessionPath() + "seeds//contributingSprites_" + roiname + ".csv");
		
	}
	
	
	
	
}

