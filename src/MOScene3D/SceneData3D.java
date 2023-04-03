package MOScene3D;

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

public class SceneData3D {
	
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
		public GeometryBuffer3D geometryBuffer3d;
		
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
		
		
		public SceneData3D() {
			
			
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
			
			geometryBuffer3d = new GeometryBuffer3D(distanceImage, fov,   originalViewWidth, originalViewHeight, originalViewCropRect);
			
			setCurrentRenderImage(0);
		}
	    

		public void setROIRect(Rect r, boolean maintainRelativeAssetScale) {
			// The roiRect is in normalised extents of the master view
			roiRect = r.copy();
			maintainRelativeScaling = maintainRelativeAssetScale;
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
		
		public Range getROIDepthExtrema(boolean forceRecalculation) {
			
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
					if(isSubstance(x,y)) roiDepthExtrema.addExtremaCandidate(d);
				}
			}
			return roiDepthExtrema;
		}
		
		public float getROINormalisedDepth(PVector docSpace) {
			float masterNormalisedDepth = getDepthNormalised(docSpace);
			Range normalisedDepthExtrema = getROIDepthExtrema(false);
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
			return isSubstance((int)coord.x, (int)coord.y);
		}
		
		
		public boolean isSubstance(int x, int y) {
			//using absolute master buffer coords
			int packedCol = geometryBuffer3d.substanceImage.getRGB(x, y);
			Color c = MOPackedColor.packedIntToColor(packedCol, true);
			if( c.getRed() > 0) return true;
			return false;
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
		
		Fustrum3D getViewFustrum() {
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





/*****
package MOScene3D;
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
import MOUtils.MOStringUtils;
import MOUtils.GlobalSettings;

///////////////////////////////////////////////////////////////////////////////
// Scene3D and DepthBuffer data image dimensions are generated by another package
// You will probably have them roughly the same aspect as your Main Document Image.
// To keep the interface working betweenthe two sets of dimensions, access to
// Scene3D data is using a converter which first converts MO Doc space
// into Normalized space (both x and y in the range 0..1)
//
// To keep the interface easy within the MouseOrgan user session, Document Space
// (0..1 in the longest edge, 0.. <1 in the shorter edge of the Mouse Organ Document)
// is used, then conveted to Normalized space for further use, hence you initialise the 
// SceneDtat3D class with the Mouse Organ Document Aspect

public class SceneData3D {
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
	public GeometryBuffer3D geometryBuffer3d;
	DistanceBufferFilter distanceFilter;
	FloatImage distanceImage;
	float fov;
	
	// if you want to focus in on a region of the 
	// sceneData to be the extents of the image you
	// are rendering then set the roiRect
	Rect roiRect = new Rect();
	boolean maintainRelativeScaling = true;
	
	public SceneData3D(DistanceBufferFilter dFilter) {
		
		distanceFilter = dFilter;
	}
	
	public SceneData3D() {
		
		distanceFilter = new DistanceBufferFilter();
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
		
		geometryBuffer3d = new GeometryBuffer3D(distanceImage, fov,  distanceFilter, originalViewWidth, originalViewHeight, originalViewCropRect);
		
		setCurrentRenderImage(0);
	}
    
    
    public void setDistanceBufferGamma(float g) {
    	distanceFilter.setDistanceGamma(g);
    	geometryBuffer3d = new GeometryBuffer3D(distanceImage, fov,  distanceFilter, originalViewWidth, originalViewHeight, originalViewCropRect);
    }
    
	
	public void setROIRect(Rect r, boolean maintainRelativeAssetScale) {
		roiRect = r.copy();
		maintainRelativeScaling = maintainRelativeAssetScale;
	}
	
	public Rect getROIRect() {
		return roiRect.copy();
	}
	
	
	float getWholeSceneAspect() {
		// this is the apsect of the whole scene, no roi applied
		return renderWidth/(float)renderHeight;
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
	
	
	// used to convert the doc space of the section you are re-rendering into the correct
	// point within the ROI
	private PVector getROILoc(PVector docSpace) {
		//PVector normalisedPoint = MOMaths.docSpaceToNormalisedSpace(docSpace, this.getWholeSceneAspect()); // this might be ROI aspect needed
		PVector normalisedPoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToNormalisedSpace(docSpace);
		// scale down docspace into the roi
		// then re-interpolate that across the roi to get back to the ROI as a sort of crop-rect within the larger image 
		PVector roiIterpolatedPoint = roiRect.interpolate(normalisedPoint); // so this scrunches the point which is in DocSpace (0..1,0..1) down into the roiRect (l,t,r,b)
		
		//return MOMaths.normalisedSpaceToDocSpace(roiIterpolatedPoint, this.getWholeSceneAspect());
		return GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(roiIterpolatedPoint);
		
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
		PVector roiSpace = getROILoc(docSpace);
		
		PVector coord = distanceBufferKeyImageSampler.docSpaceToBufferSpace(roiSpace);
		int packedCol = currentRenderKeyImage.getRGB((int)coord.x, (int)coord.y);
		
		return MOPackedColor.packedIntToColor(packedCol, currentRenderKeyImageHasAlpha);
	}
	
	public PVector getCurrentRenderGradiant(PVector docSpace) {
		PVector roiSpace = getROILoc(docSpace);
		ConvolutionFilter cf = new ConvolutionFilter();
		
		PVector grad = cf.getGradient(roiSpace, currentRenderKeyImage);
		return grad;
	}
	
	public boolean isSubstance(PVector docSpace) {
		PVector roiSpace = getROILoc(docSpace);
		
		PVector coord = distanceBufferKeyImageSampler.docSpaceToBufferSpace(roiSpace);
		int packedCol = geometryBuffer3d.substanceImage.getRGB((int)coord.x, (int)coord.y);
		Color c = MOPackedColor.packedIntToColor(packedCol, true);
		if( c.getRed() > 0) return true;
		return false;
	}
	
	public PVector get3DSurfacePoint(PVector docSpace) {
		PVector roiSpace = getROILoc(docSpace);
		
		return geometryBuffer3d.docSpaceToWorld3D(roiSpace);
	}
	
	PVector get3DVolumePoint(PVector docSpace, float normDepth) {
		PVector roiSpace = getROILoc(docSpace);
		
		// needs to take angle into consideration but OK for the moment
		float realDistance = geometryBuffer3d.normalisedDepthToRealDepth(normDepth);
		return geometryBuffer3d.docSpaceToWorld3D( roiSpace, realDistance);
	}
	
	Fustrum3D getViewFustrum() {
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
		PVector roiSpace = getROILoc(docSpace);
		
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
		PVector roiSpace = getROILoc(docSpace);
		
		return geometryBuffer3d.getDepth(roiSpace);
	}
	
	
	public float getDepthNormalised(PVector docSpace) {
		PVector roiSpace = getROILoc(docSpace);
		
		return geometryBuffer3d.getDepthNormalised(roiSpace);
	}
	
	
	
	public float getDistance(PVector docSpace) {
		PVector roiSpace = getROILoc(docSpace);
		
		return geometryBuffer3d.getDistance(roiSpace);
	}
	
	

}


****/





