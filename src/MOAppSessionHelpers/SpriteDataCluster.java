package MOAppSessionHelpers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;


import MOImage.KeyImageSampler;
import MOMaths.PVector;
import MOMaths.Rect;
import MOPointGeneration.PointGenerator_Random;
import MOScene3D.SceneData3D;
import MOSprite.SpriteData;
import MOSprite.SpriteDataBatch;
import MOSprite.SpriteFontBiome;
import MOUtils.GlobalSettings;

public class SpriteDataCluster {
	String clusterName = "";
	
	
	
	SpriteFontBiome spriteFontBiome;
	
	SceneData3D  sceneData3D;
	
	
	public SpriteDataCluster(String clusterName, SpriteFontBiome spriteFontBiome, SceneData3D  sceneData3D){
		
		this.clusterName = clusterName;
		this.spriteFontBiome = spriteFontBiome;
				
		
		
		this.sceneData3D = sceneData3D;
		
		
	}
	
	
	public SpriteDataBatch generateCluster(PVector scenePoint, BufferedImage distributionImage, float distributionImage3DsizeInScene, int numPoints, int rseed) {
		
		
		
		float clusterDepth = sceneData3D.getDepthNormalised(scenePoint);
		
		
		PointGenerator_Random pointField = new PointGenerator_Random(rseed);

		pointField.setMaskImage(sceneData3D.getSubstanceMaskImage(true));
		
		
		Rect docSpaceRect = getClumpRectIn3DScene( scenePoint, sceneData3D, distributionImage, distributionImage3DsizeInScene);
		pointField.setGenerationArea(docSpaceRect);
		
		
		ArrayList<PVector> points = pointField.generatePoints(numPoints);
		
		points = maskPointsWithDistributionImage(points, distributionImage, docSpaceRect);
		//System.out.println("generateCluster::docSpaceRect  " + docSpaceRect.toStr());
		SpriteDataBatch seedbatch = new SpriteDataBatch(clusterName);
		//System.out.println("generateCluster::has made  " + points.size() + " points ");
		
		
		for(PVector p: points) {
			SpriteData seedInstance = spriteFontBiome.getSpriteDataInstance();
			seedInstance.setDocPoint(p);
			seedInstance.SpriteDataBatchName = clusterName;
			seedInstance.setDepth(clusterDepth);
			seedbatch.addSpriteData(seedInstance);
		}
		return seedbatch;
		
	}
	
	ArrayList<PVector> maskPointsWithDistributionImage(ArrayList<PVector> pointsIn, BufferedImage distributionImage, Rect docSpaceRect){
		ArrayList<PVector> pointsOut = new ArrayList<PVector>();
		KeyImageSampler kis = new KeyImageSampler(distributionImage);
		
		for(PVector thisPIn: pointsIn) {
			
			// convert to distributionImage points
			// first get the point normalised within the docSpaceRect
			PVector pnorm = docSpaceRect.norm(thisPIn);
			float v = kis.getValue01NormalisedSpace(pnorm);
			
			if(v > 0.5f) pointsOut.add(thisPIn);
			
		}
		
		
		return pointsOut;
		
	}
	
	
	public Rect getClumpRectIn3DScene(PVector scenePoint, SceneData3D sceneData, BufferedImage distributionImage, float distributionImage3DsizeInScene) {
		// this is based on notionally scaling the distributionImage based on the depth at p
		
		// first work out the scaled dimensions of the distributionImage based purely on the 
		// distributionImage3DsizeInScene parameter, which is in 3D scene units
		float bufferSpaceHeight = getHeightInRenderTargetPixels3D(sceneData3D,scenePoint, distributionImage3DsizeInScene);
		float aspect = distributionImage.getWidth()/(float) distributionImage.getHeight();
		
		float bufferSpaceWidth = bufferSpaceHeight*aspect;
		
		
		// the image has been scaled to the correct pixel size at this point, so we are working in buffer-space points
		// for the purposes of this prototype the "anchor point" is (0.5,1)
		PVector bufferSpaceScenePoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(scenePoint);
		float halfWidth = bufferSpaceWidth/2f;
		float left = bufferSpaceScenePoint.x - halfWidth;
		float top = bufferSpaceScenePoint.y - bufferSpaceHeight;
		Rect bufferSpaceRect = new Rect(left,top, bufferSpaceWidth,bufferSpaceHeight);
		
		return GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(bufferSpaceRect);
		
		
	}
	
	
	
	float getHeightInRenderTargetPixels3D(SceneData3D sceneData, PVector p, float distributionImage3DsizeInScene) {
		float scale3D = sceneData.get3DScale(p);
		float heightDocSpace = distributionImage3DsizeInScene * scale3D;
		
				//System.out.println("getHeightInRenderTargetPixels3D: scale3D " + scale3D );
		float docSizeInPixels =  docSizeToRenderTargetPixels2D(heightDocSpace);
		//System.out.println("sprite id " + this.id + " doc point " + docPoint.toString() + " height doc space = " + heightDocSpace + "  size pixels " + docSizeInPixels);
		//System.out.println();
		return docSizeInPixels;
	}
	
	
	float docSizeToRenderTargetPixels2D(float size) {

		PVector heightDocSpaceVector = new PVector(0, size);
		PVector heightInPixelsVector = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(heightDocSpaceVector);
		return (float) Math.abs(heightInPixelsVector.y);

	}
	
	
	
	
	
	
	
	
	
	
}
