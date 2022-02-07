package MOAppSessionHelpers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImageCollections.ImageSampleDescription;
import MOImageCollections.ImageSampleGroupManager;
import MOImageCollections.ImageSampleSelector;
import MOMaths.PVector;
import MOMaths.Rect;
import MOSceneData.PackingInterpolationScheme;
import MOSceneData.PointGenerator_RadialPackSurface3D;
import MOSceneData.SceneData3D;
import MOSceneData.Seed;
import MOSceneData.SeedBatch;
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Bundles together any SeedBatch making/altering operations that are to do with a 3D scene
//



public class SeedBatchHelper_Scene3D {


	SceneData3D sceneData3D;
	
	PointGenerator_RadialPackSurface3D pointGenerator;
	ImageSampleSelector imageSampleSelector;
	
	

	public SeedBatchHelper_Scene3D( SceneData3D sd3d, ImageSampleGroupManager isgm, int sampleSelectorRSeed) {
		imageSampleSelector = new ImageSampleSelector(isgm, sampleSelectorRSeed);
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
	
	
	
	public ImageSampleSelector getImageSampleSelector() {
		return imageSampleSelector;
	}
	
	public void addImageSampleGroupCandidate(String imageSampleGroupName, float probablity) {
	
		imageSampleSelector.addContentItemProbability(imageSampleGroupName, probablity);
	
	}
	
	public void clearSampleGroupCandidates() {
		imageSampleSelector.clearContentItemProbabilities();
	}
	
	
	public SeedBatch generateSeedBatch(String batchName) {
		if(pointGenerator == null) {
			System.out.println("SeedBatchFactory_Scene3D::generateSeedBatch -  point packing is undefined , please call definePointPacking before using this method");
			return null;
		}
		
		int candidates = imageSampleSelector.getNumCandidateSampleGroups();
		if(candidates == 0) {
			System.out.println("SeedBatchFactory_Scene3D::generateSeedBatch -  no image sample candidates added , please call addImageSampleGroupCandidate before using this method");
			return null;
		}
		
		SeedBatch seedbatch = new SeedBatch();
		
		String pathAndFileName = GlobalSettings.getUserSessionPath() + "seeds\\" + batchName + ".sds";
		
		
		ArrayList<PVector> points = pointGenerator.generatePoints();
		//PointGenerator_RadialPackSurface3D pointField = getPointGenerator3D(namePointDisImage, is, pointDistRSeed);
		//seedBatchBiome1.generateSeeds(cis, pointField);
		int n=0;
		for(PVector p: points) {
			ImageSampleDescription isd = imageSampleSelector.selectImageSampleDescription();
			
			Seed s = new Seed();
			//System.out.println("new seed at " + p.toString());
			s.setDocPoint(p);
			s.batchName = batchName;
			s.imageSampleGroupName = isd.imageSampleGroupName;
			s.imageSampleGroupShortName = isd.shortName;
			s.imageSampleGroupItemNumber = isd.itemNumber;
			s.setDepth(p.z);
			s.id = n;
			seedbatch.addSeed(s);
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
	public SeedBatch applyROIToSeeds(SeedBatch seedbatch) {
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
		
		
		SeedBatch seedbatchOut = new SeedBatch();
		seedbatch.resetItemIterator();
		while(seedbatch.areItemsRemaining()) {
			
			Seed s = seedbatch.getNextSeed().copy();
			PVector newSceneDocPoint = s.getDocPoint();
			PVector normalisedPoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(newSceneDocPoint);
			//if(theROI.isPointInside(normalisedPoint)==false) continue;

			PVector newROIPoint = theROI.norm(normalisedPoint); // convert to normalised space within the roi
			PVector newDocSpacePt = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(newROIPoint);
			//System.out.println("applyROIToSeeds: seeds docpoint before appplication of ROI " + newSceneDocPoint.toString() + ". Adjusted by ROI " + newDocSpacePt.toString());
			s.setDocPoint(newDocSpacePt);
			
			seedbatchOut.addSeed(s);
			
		}
		
		
		


		System.out.println("applyROIToSeeds: seeds before appplication of ROI " + seedbatch.getNumItems() + ". Adjusted number of seeds in ROI " + seedbatchOut.getNumItems());
		return seedbatchOut;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// call this if you alter the depth-gamma of the scene after the seeds have been made
	//
	public void updateSeedDepthsAgainstScene(SeedBatch seedbatch) {
		
		// call this if you are changing to a different depth filter
		seedbatch.resetItemIterator();
		while(seedbatch.areItemsRemaining()) {
			
			Seed s = seedbatch.getNextSeed();
			float d = sceneData3D.getDepthNormalised(s.getDocPoint());
			s.setDepth(d);
		}
		seedbatch.resetItemIterator();
		
	}
	
	
	
	
}
