package MOScene3D;



import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import MOImage.ConvolutionFilter;
import MOImage.FloatImage;
import MOImage.ImageProcessing;
import MOImage.KeyImageSampler;
import MOImage.MOPackedColor;
import MOImageCollections.DirectoryFileNameScanner;
import MOImageCollections.ImageAssetGroup;

import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOSprite.SpriteSeed;
import MOUtils.GlobalSettings;
import MOUtils.ImageCoordinateSystem;
import MOUtils.KeyValuePairList;
import MOUtils.MOStringUtils;

public class SceneData3D {
	// path to the specified input scenedata folder
	String directoryPath;

	// this reads in and contains all the png files within the input folder
	ImageAssetGroup textureMapImages;
	BufferedImage currentTextureMapImage;
	boolean currentRenderKeyImageHasAlpha;

	// Depth Buffer and Eye point calculations are made using the Geometry Buffer
	public DepthBuffer3D depthBuffer3d;

	
	//
	//
	public ROIManager roiManager;

	// contains the min and max normalised depth values from the master within the
	// roi
	

	public SceneData3D() {

	}

	public void load(String targetDirectory) {
		directoryPath = targetDirectory;

		DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(directoryPath, "png");
		textureMapImages = new ImageAssetGroup();
		textureMapImages.setDirectoryFileNameScanner(dfns);
		textureMapImages.loadImages();

		// this is a "distance" image, it is converted to a proper depth image
		// in the DepthBuffer object
		FloatImage depthImage = new FloatImage(directoryPath + "\\depth.data");
		float farDepth = depthImage.getExtrema().getUpper();
		
		

		float cameraDistToVP = getCameraDistToVPFromFile(directoryPath + "\\cameraDistance.csv");

		depthBuffer3d = new DepthBuffer3D(depthImage, cameraDistToVP);
		setCurrentRenderImage(0);
	}

	private float getCameraDistToVPFromFile(String fileAndPath) {
		// there should be a file in the SceneData called "cameraDistance.csv" exported from
		// TerrainMaker v1_2
		float camDist = 0;
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(fileAndPath));
			String row;
			while ((row = csvReader.readLine()) != null) {
				KeyValuePairList kvlist = new KeyValuePairList();
				kvlist.ingestCSVLine(row);
				camDist = kvlist.getFloat("CAMDIST_TO_VP");
			}
			csvReader.close();
		} catch (Exception e) {
			System.out.println("SpriteSeedBatch.loadSeedsAsCSV: csv reader failed - " + fileAndPath + e);
		}
		return camDist;
	}

	

	public Rect getROIRect() {
		return roiManager.getCurrentROIDocRect();
	}

	private ImageCoordinateSystem getDepthBufferKeyImageSampler() {
		return depthBuffer3d.depthBufferCoordinateSystem;
	}
	
	

	/////////////////////////////////////////////////////////////////////////////////////
	// The mask image is an image reflecting the sky/land pixels
	// land pixels are set to white, sky to black

	public ArrayList<String> getRenderImageNames() {
		return textureMapImages.getImageAssetNamesList();
	}

	public BufferedImage setCurrentRenderImage(String shortName) {
		currentTextureMapImage = textureMapImages.getImage(shortName);
		currentRenderKeyImageHasAlpha = ImageProcessing.hasAlpha(currentTextureMapImage);
		return currentTextureMapImage;
	}

	public void setCurrentRenderImage(int n) {
		currentTextureMapImage = textureMapImages.getImage(n);
		String shortName = textureMapImages.getImageAssetName(n);
		System.out.println("curren render image is " + shortName);
		currentRenderKeyImageHasAlpha = ImageProcessing.hasAlpha(currentTextureMapImage);
	}

	////////////////////////////////////////////////////////////////
	//
	// get images
	//
	public BufferedImage getSubstanceMaskImage(boolean cropToRoi) {
		if (cropToRoi)
			return cropToROI(depthBuffer3d.substanceImage);
		return depthBuffer3d.substanceImage;
	}

	public BufferedImage getCurrentRenderImage(boolean cropToRoi) {
		if (cropToRoi)
			return cropToROI(currentTextureMapImage);
		return currentTextureMapImage;
	}

	public BufferedImage getRenderImage(String shortName, boolean cropToRoi) {
		BufferedImage renderImage = textureMapImages.getImage(shortName);
		if (cropToRoi)
			return cropToROI(renderImage);
		return renderImage;
	}

	////////////////////////////////////////////////////////////////
	//
	// when using the ROI, you may wish to see the portion of the render in the SceneData texture collection
	// This method crops the image accordingly

	BufferedImage cropToROI(BufferedImage uncropped) {
		// returns the roi cropped image from whatever function
		// System.out.println("cropping to roi rect " + roiRect.toStr());
		Rect currentROIDocSpaceRect = roiManager.getCurrentROIDocRect();
		Rect masterDocSpaceRect = roiManager.getMasterDocumentRect();
		// this needs to be normalised within the master rect
		PVector topLeft = masterDocSpaceRect.norm(   currentROIDocSpaceRect.getTopLeft()   );
		PVector bottomRight = masterDocSpaceRect.norm(   currentROIDocSpaceRect.getBottomRight()   );
		Rect cropRect = new Rect(topLeft, bottomRight);
		System.out.println("warning using untested cropToROI in scenedata class");
		return ImageProcessing.cropImageWithNormalisedRect(uncropped, cropRect);
	}

	//////////////////////////////////////////////////////////////////
	// Converting from LocalROIDocSpace to MasterDocSpace
	// When this method is used, you assume that the local ROI is in-play as the current document space, hence
	// you can use the GlobalSettings coordinate space conversions
	// IN the V2 version, the MasterRect is stored as a documentSpace rect, and the
	// ROIRect is stored as a wholly contained, sub-rect of the masterRect. Therefore conversion of point localDocPoint in localROIDocSpace to
	// the MasterDocSpace is as follows
	// localNormalised = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(roiLocalDocSpace)
	// masterDocSpace = ROIRect.interpolate(localNormalised)
	
	//
	public PVector subROIDocSpaceToMasterDocSpace(PVector roiLocalDocSpace) {

			return roiManager.subROIDocSpaceToMasterDocSpace(roiLocalDocSpace);
	}

	///////////////////////////////////////////////////////////////
	// less used than above. If you happen to have calculated anything in Master Doc
	// space, and want to convert that to
	// the ROIs docSpace...
	// Assume that the local ROI is in-play as the current document space
	public PVector masterDocSpaceToROIDocSpace(PVector masterDocSpace) {
		return roiManager.masterDocSpaceToSubROIDocSpace(masterDocSpace);

	}

	public boolean isUsingROI() {
		return roiManager.isUsingROI();
	}

	public Range getFullSceneDepthExtrema() {
		return depthBuffer3d.depthBufferExtrema.copy();
	}

	////////////////////////////////////////////////////////////////
	//
	// get pixel data
	//
	public float getCurrentRender01Value(PVector docSpace) {
		Color rgb = getCurrentRenderColor(docSpace);
		int r = rgb.getRed();
		int g = rgb.getGreen();
		int b = rgb.getBlue();
		return (r + g + b) / 765f;
	}

	Color getCurrentRenderColor(PVector docSpace) {
		PVector masterSpace = subROIDocSpaceToMasterDocSpace(docSpace);

		PVector coord = getDepthBufferKeyImageSampler().docSpaceToBufferSpace(masterSpace);
		int packedCol = currentTextureMapImage.getRGB((int) coord.x, (int) coord.y);

		return MOPackedColor.packedIntToColor(packedCol, currentRenderKeyImageHasAlpha);
	}

	public PVector getCurrentRenderGradiant(PVector docSpace) {
		PVector masterSpace = subROIDocSpaceToMasterDocSpace(docSpace);
		ConvolutionFilter cf = new ConvolutionFilter();

		PVector grad = cf.getGradient(masterSpace, currentTextureMapImage);
		return grad;
	}

	public boolean isSubstance(PVector docSpace) {
		PVector masterSpace = subROIDocSpaceToMasterDocSpace(docSpace);
		PVector coord = getDepthBufferKeyImageSampler().docSpaceToBufferSpace(masterSpace);
		return depthBuffer3d.isSubstance((int) coord.x, (int) coord.y);
	}

	public PVector get3DSurfacePoint(PVector docSpace) {
		PVector masterSpace = subROIDocSpaceToMasterDocSpace(docSpace);
		return depthBuffer3d.docSpaceToWorld3D(masterSpace);
	}
	
	public Color testGrid3D(PVector p3d, float gridSpace, float gridLineThickness, boolean xLines, boolean yLines,boolean zLines) {
		PVector offset = new PVector(-10000,-10000,-10000);
		offset.add(p3d);
		float xMod = Math.abs(p3d.x % gridSpace);
		float yMod = Math.abs(p3d.y % gridSpace);
		float zMod = Math.abs(p3d.z % gridSpace);
		
		if(xLines && xMod < gridLineThickness) return Color.BLACK;
		if(yLines && yMod < gridLineThickness) return Color.BLACK;
		if(zLines && zMod < gridLineThickness) return Color.BLACK;
		return Color.WHITE;
	}

	/*
	PVector get3DVolumePoint(PVector docSpace, float normDepth) {
		PVector masterSpace = subROIDocSpaceToMasterDocSpace(docSpace);

		// needs to take angle into consideration but OK for the moment
		float realDistance = depthBuffer3d.normalisedDepthToRealDepth(normDepth);

		return depthBuffer3d.docSpaceToWorld3D(masterSpace, realDistance);
	}
	*/

	public float get3DScale(PVector ROIdocSpace) {
		// Returns the document-space distance in master units, that is the unit 3D distance (1.0) at master document space point in the 3D scene.
		// so for points further away the distance will be shorter etc.
		// Used for scaling things accurately against the scene
		
		PVector masterSpace = subROIDocSpaceToMasterDocSpace(ROIdocSpace);
		PVector displacedPointMasterSpace = depthBuffer3d.get3DDisplacedDocPoint(masterSpace, new PVector(0, 1,0));
		PVector displacedPointROIDocSpace =  this.masterDocSpaceToROIDocSpace(displacedPointMasterSpace);
		
		//if( unitSizeMasterDocSpace > 0.002) System.out.println("get3DScale : ROI doc point " + docSpace.toStr() + " masterSpace doc point " +  masterSpace.toStr() + " p3d " + original3DPoint + " unit size " + unitSizeMasterDocSpace);

		float distanceInROIDocSpace = ROIdocSpace.dist(displacedPointROIDocSpace);
		return distanceInROIDocSpace;
	}

	

	public float getDepth(PVector docSpace) {
		PVector roiSpace = subROIDocSpaceToMasterDocSpace(docSpace);
		PVector bufferSpace = depthBuffer3d.docSpaceToBufferSpace(roiSpace);
		return depthBuffer3d.getDepthBilinear(bufferSpace);
	}
	
	/*
	public float getDepthNormalised(PVector docSpace) {
		PVector roiSpace = subROIDocSpaceToMasterDocSpace(docSpace);
		PVector bufferSpace = depthBuffer3d.docSpaceToBufferSpace(roiSpace);
		return depthBuffer3d.getDepthNormalised(bufferSpace);
	}
	*/

	public PVector world3DToDocSpace(PVector world3dPt) {

		PVector docSpaceInMasterView = depthBuffer3d.world3DToDocSpace(world3dPt);
		return masterDocSpaceToROIDocSpace(docSpaceInMasterView);

	}
	
	
	

}












/*
 * 
 * Old SceneData using old geometry 3D
 * 
 * 
 * 
 * 
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


import MOImage.ConvolutionFilter;
import MOImage.FloatImage;
import MOImage.ImageProcessing;
import MOImage.KeyImageSampler;
import MOImage.MOPackedColor;
import MOImageCollections.DirectoryFileNameScanner;
import MOImageCollections.ImageAssetGroup;
import MOMaths.Fustrum3D;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOUtils.GlobalSettings;
import MOUtils.MOStringUtils;


public class V1_SceneData3D {
	
	// path to the specified input scenedata folder
		String directoryPath;
		
		
		// view data from the original 3D scene
		PVector cameraPosition;
		PVector cameraLookat;
		
		
		//CoordinateSpaceConverter coordinateSpaceConverter;
		KeyImageSampler distanceBufferKeyImageSampler;
		// document aspect is the aspect of the
		// output compositer render, not the input roi
		//float mouseOrganDocAspect;
		
		// the with and height of the renders and depth images (should all be the same)
		int renderWidth;
		int renderHeight;
		
		// The renders in the scenedata are (probably) a crop of a larger view (the "original view"). This data is used to add offsets back into the geometry calculation.
		// Specifically, the FOV is for the whole original view before it was cropped. We need to recover the correct vector into the scene
		// for 3D and depth calculations. The viewROICrop is used to add offsets to the X and Y of pixels to enable this.
		// 
		Rect originalViewCropRect;
		int originalViewWidth;
		int originalViewHeight;
		
		
		
		// this reads in and contains all the png files within the input folder
		ImageAssetGroup renderImages;
		BufferedImage currentRenderKeyImage;
		boolean currentRenderKeyImageHasAlpha;
		
		// depth stuff. The depthImage is normalised to 0..1 after loading
		// but the original min and max depth are kept so the
		// original depths can be recalculated
		
		Range originalDepthExtrema;
		public V1_GeometryBuffer3D geometryBuffer3d;
		
		FloatImage distanceImage;
		float fov;
		
		// if you want to focus in on a region of the 
		// sceneData to be the extents of the image you
		// are rendering then set the roiRect. 
		// The roiRect is in normalised extents of the master view and is usually
		// got from the ROIhelper class
		Rect roiRect = new Rect();
		boolean maintainRelativeScaling = true;
		// contains the min and max normalised depth values from the master within the roi
		Range roiDepthExtrema=null;
		
		
		public V1_SceneData3D() {
			
			
		}

		public void load(String targetDirectory) {
			directoryPath = targetDirectory;
			load();
		}
			
	    public void load(){
	    	DirectoryFileNameScanner dfns = new DirectoryFileNameScanner(directoryPath, "png");
	    	renderImages = new ImageAssetGroup();
	    	renderImages.setDirectoryFileNameScanner(dfns);
			renderImages.loadImages();
			
	    	
	    	
	    	
			//renderImages = new DirectoryImageGroup(directoryPath, ".png", "");
			//renderImages.loadImages();
			
			// this is a "distance" image, it is converted to a proper depth image
			// in the DepthBuffer object
			distanceImage = new FloatImage(directoryPath + "\\distance.data");

			// the render width of the images in the scenedata folder should be all the same
			renderWidth = distanceImage.getWidth();
			renderHeight = distanceImage.getHeight();

			// from the view.txt file....
			// read the FOV
			ArrayList<String> strList = MOStringUtils.readTextFile(directoryPath + "\\view.txt");
			String fovString = strList.get(2);
			fov = Float.parseFloat(fovString);
			
			// read the original view width and height
			String originalViewWidthString = strList.get(3);
			originalViewWidth = Integer.parseInt(originalViewWidthString);
			
			String originalViewHeightString = strList.get(4);
			originalViewHeight = Integer.parseInt(originalViewHeightString);
			
			// read the crop of the original view
			String topleftSt = strList.get(5);
			String botRighSt = strList.get(6);
			
			PVector topLeftV = new PVector();
			topLeftV.fromString(topleftSt);
			
			PVector botRighV = new PVector();
			botRighV.fromString(botRighSt);
			
			
			originalViewCropRect = new Rect(topLeftV,botRighV);
			
			distanceBufferKeyImageSampler = new KeyImageSampler(distanceImage);
			
			geometryBuffer3d = new V1_GeometryBuffer3D(distanceImage, fov,   originalViewWidth, originalViewHeight, originalViewCropRect);
			
			setCurrentRenderImage(0);
		}
	    

		public void setROIRect(Rect r, boolean maintainRelativeAssetScale) {
			// The roiRect is in normalised extents of the master view
			roiRect = r.copy();
			maintainRelativeScaling = maintainRelativeAssetScale;
			
			
			
			
			
		}
		
		public float getCroppedAngleOfView() {
			// this returns the angle of view that remains when taking into consideration the 
			// the initial cropRect from the 3D SceneData3D file, and the ROI rect from the MouseOrgan session
			float originalFOV = geometryBuffer3d.getOriginalAngleOfView();
			float originalCropFOV = geometryBuffer3d.getCroppedAngleOfView();
			
			float roiNormalisedHeight = roiRect.getHeight();
			float residualAngleOfView =  originalCropFOV * roiNormalisedHeight;
			
			System.out.println("getCroppedAngleOfView originalFOV   " + originalFOV);
			System.out.println("getCroppedAngleOfView originalCropFOV   " + originalCropFOV);
			System.out.println("getCroppedAngleOfView ROI Cropped FOV   " + residualAngleOfView);
			
			return residualAngleOfView;
		}
		
		public Rect getROIRect() {
			return roiRect.copy();
		}
		
		
		
	    
		/////////////////////////////////////////////////////////////////////////////////////
		// The mask image is an image reflecting the sky/land pixels
		// land pixels are set to white, sky to black
		
		
		
		public ArrayList<String> getRenderImageNames(){
			return renderImages.getImageAssetNamesList();
		}

		public BufferedImage setCurrentRenderImage(String shortName) {
			currentRenderKeyImage = renderImages.getImage(shortName);
			currentRenderKeyImageHasAlpha = ImageProcessing.hasAlpha(currentRenderKeyImage);
			return currentRenderKeyImage;
		}
		
		public void setCurrentRenderImage(int  n) {
			currentRenderKeyImage = renderImages.getImage(n);
			String shortName = renderImages.getImageAssetName(n);
			System.out.println("curren render image is " + shortName);
			currentRenderKeyImageHasAlpha = ImageProcessing.hasAlpha(currentRenderKeyImage);
		}

		////////////////////////////////////////////////////////////////
		//
		// get images
		//
		public BufferedImage getSubstanceMaskImage(boolean cropToRoi) {
			if(cropToRoi) return cropToROI(geometryBuffer3d.substanceImage);
			return geometryBuffer3d.substanceImage;
		}
		
		public BufferedImage getCurrentRenderImage(boolean cropToRoi) {
			if(cropToRoi)  return cropToROI(currentRenderKeyImage);
			return currentRenderKeyImage;
		}
		
		public BufferedImage getRenderImage(String shortName, boolean cropToRoi) {
			BufferedImage renderImage = renderImages.getImage(shortName);
			if(cropToRoi)  return cropToROI(renderImage);
			return renderImage;
		}
		
		
		////////////////////////////////////////////////////////////////
		//
		// when using the ROI
		//
			
		BufferedImage cropToROI(BufferedImage uncropped) {
			// returns the roi cropped image from whatever function
			//System.out.println("cropping to roi rect " + roiRect.toStr());
			return ImageProcessing.cropImageWithNormalisedRect(uncropped,roiRect);
		}
		
		
		// IF you are using a ROI as a smaller region of the MASTER document, then this method converts between the
		// docSpace in the ROI to the docSpace in the MASTER
		public PVector ROIDocSpaceToMasterDocSpace(PVector docSpace) {
			
			PVector normalisedPoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(docSpace);
			// scale down docspace into the roi
			// then re-interpolate that across the roi to get back to the ROI as a sort of crop-rect within the larger image 
			PVector roiIterpolatedPoint = roiRect.interpolate(normalisedPoint); // so this scrunches the point which is in DocSpace (0..1,0..1) down into the roiRect (l,t,r,b)
			
			//return MOMaths.normalisedSpaceToDocSpace(roiIterpolatedPoint, this.getWholeSceneAspect());
			return GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(roiIterpolatedPoint);
			
		}
		
		// less used than above. If you happen to have calculated anything in Master Doc space, and want to convert that to
		// the ROIs docSpace...
		public PVector masterDocSpaceToROIDocSpace(PVector docSpace) {
			PVector normalisedPoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(docSpace);
			PVector newROIPoint = roiRect.norm(normalisedPoint); // convert to normalised space within the roi
			return GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(newROIPoint);
			
		}
		
		public boolean isUsingROI() {
			Rect identityRect = new Rect();
			return !(roiRect.equals(identityRect));
		}
		
		public Range getFullSceneDepthExtrema() {
			return geometryBuffer3d.depthBufferExtrema.copy();
		}
		
		public Range getFullSceneDistanceExtrema() {
			return geometryBuffer3d.distanceBufferExtrema.copy();
		}
		
		public Range getROINormalisedDepthExtrema(boolean forceRecalculation) {
			//
			// return the normalised depth extrema within the ROI
			//
			if(roiDepthExtrema==null) forceRecalculation = true;
			if(forceRecalculation==false) return roiDepthExtrema;
			
			int left = (int)(roiRect.left*renderWidth);
			int top = (int)(roiRect.top*renderHeight);
			int right = (int)(roiRect.right*renderWidth);
			int bottom = (int)(roiRect.bottom*renderHeight);
			roiDepthExtrema = new Range();
			roiDepthExtrema.initialiseForExtremaSearch();
			for(int y=top; y<bottom; y++) {
				for(int x = left; x<right; x++) {
					float d = geometryBuffer3d.getDepthNormalised(x, y);
					if(geometryBuffer3d.isSubstance(x,y)) roiDepthExtrema.addExtremaCandidate(d);
				}
			}
			return roiDepthExtrema;
		}
		
		public Range getROIDepthExtrema() {
			//
			// return the original depth extrema within the ROI
			//
			int left = (int)(roiRect.left*renderWidth);
			int top = (int)(roiRect.top*renderHeight);
			int right = (int)(roiRect.right*renderWidth);
			int bottom = (int)(roiRect.bottom*renderHeight);
			Range depthExtrema = new Range();
			depthExtrema.initialiseForExtremaSearch();
			for(int y=top; y<bottom; y++) {
				for(int x = left; x<right; x++) {
					float d = geometryBuffer3d.getDepth(x, y);
					if(geometryBuffer3d.isSubstance(x,y)) depthExtrema.addExtremaCandidate(d);
				}
			}
			return depthExtrema;
		}
		
		public float getROINormalisedDepth(PVector docSpace) {
			float masterNormalisedDepth = getDepthNormalised(docSpace);
			Range normalisedDepthExtrema = getROINormalisedDepthExtrema(false);
			return normalisedDepthExtrema.norm(masterNormalisedDepth);
		}
		
		////////////////////////////////////////////////////////////////
		//
		// get pixel data 
		//
		public float getCurrentRender01Value(PVector docSpace) {
			Color rgb = getCurrentRenderColor(docSpace);
			int r = rgb.getRed();
			int g = rgb.getGreen();
			int b = rgb.getBlue();
			return (r+g+b)/765f;
		}
		
		
		
		Color getCurrentRenderColor(PVector docSpace) {
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			
			PVector coord = distanceBufferKeyImageSampler.docSpaceToBufferSpace(roiSpace);
			int packedCol = currentRenderKeyImage.getRGB((int)coord.x, (int)coord.y);
			
			return MOPackedColor.packedIntToColor(packedCol, currentRenderKeyImageHasAlpha);
		}
		
		public PVector getCurrentRenderGradiant(PVector docSpace) {
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			ConvolutionFilter cf = new ConvolutionFilter();
			
			PVector grad = cf.getGradient(roiSpace, currentRenderKeyImage);
			return grad;
		}
		
		public boolean isSubstance(PVector docSpace) {
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			PVector coord = distanceBufferKeyImageSampler.docSpaceToBufferSpace(roiSpace);
			return geometryBuffer3d.isSubstance((int)coord.x, (int)coord.y);
		}

		public PVector get3DSurfacePoint(PVector docSpace) {
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			
			return geometryBuffer3d.docSpaceToWorld3D(roiSpace);
		}
		
		PVector get3DVolumePoint(PVector docSpace, float normDepth) {
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			
			// needs to take angle into consideration but OK for the moment
			float realDistance = geometryBuffer3d.normalisedDepthToRealDepth(normDepth);
			return geometryBuffer3d.docSpaceToWorld3D( roiSpace, realDistance);
		}
		
		
		
		
		public Fustrum3D getViewFustrum() {
			// returns the 8 points that represnt the extents of the viewing fustrum
			// in this order starting at index 
			// Front plane Top Left
			Fustrum3D fustrum = new Fustrum3D();
			float docWidth = GlobalSettings.getTheDocumentCoordSystem().getDocumentWidth();
			float docHeight = GlobalSettings.getTheDocumentCoordSystem().getDocumentHeight();
			
			System.out.println("doc width and height " + docWidth + " " + docHeight);
			
			
			fustrum.farTopLeft = get3DVolumePoint(new PVector(0,0), 1);
			fustrum.farTopRight = get3DVolumePoint(new PVector(docWidth,0), 1);
			fustrum.farBottomLeft = get3DVolumePoint(new PVector(0,docHeight), 1);
			fustrum.farBottomRight = get3DVolumePoint(new PVector(docWidth,docHeight), 1);
			
			fustrum.nearTopLeft = get3DVolumePoint(new PVector(0,0), 0);
			fustrum.nearTopRight = get3DVolumePoint(new PVector(docWidth,0), 0);
			fustrum.nearBottomLeft = get3DVolumePoint(new PVector(0,docHeight), 0);
			fustrum.nearBottomRight = get3DVolumePoint(new PVector(docWidth,docHeight), 0);
			
			return fustrum;
		}
		
		public float get3DScale(PVector docSpace) {
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			
			float relativeAssetScale = 1;
			if(maintainRelativeScaling) {
				float verticalCropProportion = roiRect.bottom-roiRect.top;
				relativeAssetScale = 1/verticalCropProportion;
			}
			
			float geomScale3D =  geometryBuffer3d.get3DScale(roiSpace) * relativeAssetScale;
			//System.out.println("get3DScale: docSpace " + docSpace.toString() + " roiPoint point " + roiSpace.toString() + " relativeAssetScale = " + relativeAssetScale + "geom svcale 3d " + geomScale3D);
			
			return geomScale3D;
		}
		
		
		public float getDepth(PVector docSpace) {
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			
			return geometryBuffer3d.getDepth(roiSpace);
		}
		
		
		public float getDepthNormalised(PVector docSpace) {
			// this returns the normalised depth of the master view - not the roi.
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			
			return geometryBuffer3d.getDepthNormalised(roiSpace);
		}
		
		
		
		
		
		public float getDistance(PVector docSpace) {
			PVector roiSpace = ROIDocSpaceToMasterDocSpace(docSpace);
			
			return geometryBuffer3d.getDistance(roiSpace);
		}
		
		
		
		public PVector  world3DToDocSpace(PVector world3dPt) {
			
			PVector docSpaceInMasterView = geometryBuffer3d.world3DToDocSpace(world3dPt);
			return masterDocSpaceToROIDocSpace(docSpaceInMasterView);
			
		}
		
		

	}


*/






