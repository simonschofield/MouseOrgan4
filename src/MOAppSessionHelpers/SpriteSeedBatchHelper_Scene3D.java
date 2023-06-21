package MOAppSessionHelpers;


import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOApplication.MainDocument;
import MOCompositing.SpriteCropDecisionList;
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
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// This is sort-of stand alone method that used to belong to an overcomplex class called SeedBatchManager
	// It is used to adjust the sees locations into the ROI space defined in the SceneData3D
	//
	
	public SpriteSeedBatch applyROIToSpriteSeedBatch(ROIHelper roiHelper, SpriteSeedBatch seedbatch) {
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
		
		
		
		seedbatchOut.resetItemIterator();

		System.out.println("applyROIToSeeds: seeds before appplication of ROI " + seedbatch.getNumItems() + ". Adjusted number of seeds in ROI " + seedbatchOut.getNumItems());
		return seedbatchOut;
	}
	
	
	
	
	public boolean removeNoncontributingSpritesInROI(ROIHelper roiHelper, SpriteSeedBatch seedbatch) {
		// this only removed seeds if a "contributing sprite" file has been saved for this ROI (i.e. with the ROI's name) in the seeds folder
		// if the file cannot be found, then the class is alerted to save one out at the end of this session
		if(roiHelper.isUsingMaster()) return false;
		//SpriteCropDecisionList spriteCropList = theDocument.getRenderBorder().getSpriteCropDecisionList();
		SpriteCropDecisionList spriteCropList = new SpriteCropDecisionList();
		
		String fname = getContributingSpritesFilePathAndName(roiHelper);
		boolean loadResult = spriteCropList.load(fname);
		if(loadResult == false) {
			saveOutContributingSeedReport = true;
			return false;
		}
		spriteCropList.removeNonContributingSprites(seedbatch);
		return true;
	}
	
	public void saveContributingSpritesReport(ROIHelper roiHelper, MainDocument theDocument, boolean forcesave) {
		// called at the end of the session
		System.out.println("saveContributingSpritesReport:" + thisHelperName + "here1");
		if(roiHelper.isUsingMaster()) return;
		System.out.println("saveContributingSpritesReport:" + thisHelperName + "here2");
		if(forcesave) saveOutContributingSeedReport = true;
		System.out.println("saveContributingSpritesReport:" + thisHelperName + "here3");
		if(saveOutContributingSeedReport==false) return;
		
		System.out.println("saveContributingSpritesReport:" + thisHelperName + "here4");
		String fname = getContributingSpritesFilePathAndName(roiHelper);
		
		System.out.println("saveContributingSpritesReport: saving" + fname);
		
		
		theDocument.getRenderBorder().getSpriteCropDecisionList().save( fname );
		
	}
	
	
	private String getContributingSpritesFilePathAndName(ROIHelper roiHelper) {
		String roiname = roiHelper.getCurrentROIName();
		return GlobalSettings.getUserSessionPath() + "seeds//contributingSprites_" + thisHelperName + "_" + roiname + ".csv";
	}
	
	
}

