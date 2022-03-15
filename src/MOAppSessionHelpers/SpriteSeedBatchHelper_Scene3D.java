package MOAppSessionHelpers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOApplication.MainDocument;
import MOCompositing.SpriteCropDecisionList;
import MOImageCollections.SpriteImageGroupManager;
import MOMaths.PVector;
import MOMaths.Rect;
import MOPointGeneration.PackingInterpolationScheme;
import MOPointGeneration.PointGenerator_RadialPackSurface3D;
import MOScene3D.SceneData3D;
import MOSpriteSeed.SpriteSeed;
import MOSpriteSeed.SpriteSeedBatch;
import MOSpriteSeed.SpriteSeedFont;
import MOSpriteSeed.SpriteSeedFontBiome;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Bundles and single seed font biome with a 3D point generator
// This can then generate a seed batch from the biome, that can be used directly or saved and reloaded by this class
//
//

public class SpriteSeedBatchHelper_Scene3D {


	SceneData3D sceneData3D;
	
	PointGenerator_RadialPackSurface3D pointGenerator;
	SpriteSeedFontBiome seedFontBiome;
	
	boolean saveOutContributingSeedReport = false;

	public SpriteSeedBatchHelper_Scene3D( SceneData3D sd3d, int biomeRanSeed) {
		seedFontBiome = new SpriteSeedFontBiome(biomeRanSeed);
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
	
	
	
	public SpriteSeedFontBiome getSeedFontBiome() {
		return seedFontBiome;
	}
	
	
	public void addSpriteSeedFont(String sdFontName, String imageSampleGroupName, float sizeInScene, boolean useRelativeSizes, PVector origin, int fontRanSeed, float probability) {
	
		seedFontBiome.addSpriteSeedFont(sdFontName, imageSampleGroupName, sizeInScene, useRelativeSizes, origin, fontRanSeed, probability);
	
	}
	
	public void addSpriteSeedFont(SpriteSeedFont ssf) {
		
		seedFontBiome.addSpriteSeedFont(ssf);
	
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
		
		
		SpriteSeedBatch seedbatch = new SpriteSeedBatch(batchName);
		
		String pathAndFileName = GlobalSettings.getUserSessionPath() + "seeds\\" + batchName + ".sds";
		
		
		ArrayList<PVector> points = pointGenerator.generatePoints();
		
		int n=0;
		for(PVector p: points) {
			
			SpriteSeed seedInstance = seedFontBiome.getSpriteSeedInstance();
			seedInstance.setDocPoint(p);
			seedInstance.spriteSeedBatchName = batchName;
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
		
		System.out.println("apply ROI to seeds " + theROI.toStr());
		SpriteSeedBatch seedbatchOut = new SpriteSeedBatch(seedbatch.getName());
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
	
	
	public boolean removeNoncontributingSeedsInROI(ROIHelper roiHelper, MainDocument theDocument, SpriteSeedBatch seedbatch) {
		
		if(roiHelper.isUsingMaster()) return false;
		String roiname = roiHelper.getCurrentROIName();
		SpriteCropDecisionList spriteCropList = theDocument.getRenderBorder().getSpriteCropDecisionList();
		boolean loadResult = spriteCropList.load(GlobalSettings.getUserSessionPath() + "seeds//spriteCropDecisions_" + roiname + ".csv");
		if(loadResult == false) {
			saveOutContributingSeedReport = true;
			return false;
		}
		spriteCropList.removeNonContributingSpriteSeeds(seedbatch);
		return true;
	}
	
	public void saveContributingSeedsReport(ROIHelper roiHelper, MainDocument theDocument, boolean forcesave) {
		if(roiHelper.isUsingMaster()) return;
		if(forcesave) saveOutContributingSeedReport = true;
		if(saveOutContributingSeedReport==false) return;
		String roiname = roiHelper.getCurrentROIName();
		theDocument.getRenderBorder().getSpriteCropDecisionList().save(GlobalSettings.getUserSessionPath() + "seeds//spriteCropDecisions_" + roiname + ".csv");
		
	}
	
	
	
	
}

