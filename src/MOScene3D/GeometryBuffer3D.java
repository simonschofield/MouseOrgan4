package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOImage.FloatImage;
import MOImage.ImageProcessing;
import MOImage.KeyImageSampler;
import MOImage.MOPackedColor;
import MOMaths.AABox3D;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;



//GeometryBuffer3D is initialised with a "distance image" and a vertical FOV
//This is then converted to a depthBuffer of perpendicular distances to the viewing plane
//so you have a distanceBuffer and a depthBuffer, which are slightly different
//The buffers contains -Float.MAX_VAL values which are ignored by normalisation
//which contain the "sky" parts.
//
//
//For pictorial purposes, you can gamma-bend the distance buffer. The results are stored in the filteredDistanceBuffer.
//If no filter is applied then the filteredDistanceBuffer is the same as the distanceBuffer.
//All depth and other geometric calculations are based on the results of the filteredDistance.
//

public class GeometryBuffer3D{
	// substance image is the black/white mask image for substance/sky
	public BufferedImage substanceImage;
	
	// the original distance data gathered from the scene, left in its
	// original units. -Float.MAX_VAL is set for "sky"
	private FloatImage distanceBuffer;

	// the orthogonal distance to the viewing plane in the the range 0..1
	FloatImage depthBuffer;
	
	int width, height;
	
	double verticalFOVover2; // vertical fov
	float distanceCameraToViewingPlane;
	
	int originalROICropLeft;
	int originalROICropTop;
	int originalROICropRight;
	int originalROICropBottom;
	float originalViewWidthOver2, originalViewHeightOver2;
	
	
	Range depthBufferExtrema;
	Range distanceBufferExtrema;
	
	
	// document aspect is the aspect of the
	// output compositer render, not the input roi
	float documentAspect;
	KeyImageSampler distanceBufferKeyImageSampler;
	//CoordinateSpaceConverter coordinateSpaceConverter;
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// This constructor is the one used by SceneData3D
	//
	//
	public GeometryBuffer3D(FloatImage distanceBuff, float vfov, int origViewWidth, int origViewHeight, Rect ROIcrop) {
		distanceBufferKeyImageSampler = new KeyImageSampler(distanceBuff);
		
		distanceBuffer = distanceBuff;
		verticalFOVover2 = (vfov/2.0)*(Math.PI/180.0);
		//documentAspect = docAspect;
		
		//coordinateSpaceConverter = csc;
		
		width = distanceBuffer.getWidth();
		height = distanceBuffer.getHeight();
		originalViewWidthOver2 = origViewWidth/2f;
		originalViewHeightOver2 = origViewHeight/2f;
		distanceCameraToViewingPlane = (float) (originalViewHeightOver2 / Math.tan(verticalFOVover2));
		
		originalROICropLeft = (int)ROIcrop.left;
		originalROICropTop = (int)ROIcrop.top;
		originalROICropRight = (int)ROIcrop.right;
		originalROICropBottom = (int)ROIcrop.bottom;
		
		
		System.out.println(" width heigh of scene data   " + width + " " + height);
		System.out.println(" cropRect   " + ROIcrop.toStr());
		System.out.println(" pixel 3d distanceCameraToViewingPlane   " + distanceCameraToViewingPlane);
		
		
		
		makeDepthAndSubstanceBuffers();
		
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	// This constructor is the one used by ZTexture class
	// This does not need any ROI stuff, so the ROI and width are set to the full extent of the depth image
	// The distanceCameraToViewingPlane is set by hand (firm coded) at the moment to avoid an interim file format
	public GeometryBuffer3D(FloatImage distanceBuff,float FOVOver2) {
		
		distanceBufferKeyImageSampler = new KeyImageSampler(distanceBuff);

		distanceBuffer = distanceBuff;
		
		//documentAspect = docAspect;

		//coordinateSpaceConverter = csc;

		width = distanceBuffer.getWidth();
		height = distanceBuffer.getHeight();
		originalViewWidthOver2 = width/2f;
		originalViewHeightOver2 = height/2f;
		
		verticalFOVover2 = FOVOver2;
		
		distanceCameraToViewingPlane =  (float) (originalViewHeightOver2 / Math.tan(verticalFOVover2));
		
		
		

		originalROICropLeft = 0;
		originalROICropTop = 0;
		originalROICropRight = width;
		originalROICropBottom = height;


		System.out.println("ZTexture width heigh of scene data   " + width + " " + height);
		System.out.println("ZTexture distanceCameraToViewingPlane   " + distanceCameraToViewingPlane);
		System.out.println("ZTexture verticalFOVover2   " + verticalFOVover2);



		makeDepthAndSubstanceBuffers();

	}
	
	float getOriginalAngleOfView() {
		return (float)verticalFOVover2;
	}

	
	float getCroppedAngleOfView() {
		float portionOfOriginalView = height/(originalViewHeightOver2*2);
		
		return (float) (verticalFOVover2 *portionOfOriginalView);
		
	}
	
	private void makeDepthAndSubstanceBuffers() {
		// this makes the depthBuffer - a floatimage containing the perpendicular distances
		// it also contains -1 values for "sky" (infinitely distant)
		depthBufferExtrema = new Range();
		depthBufferExtrema.initialiseForExtremaSearch();
		
		int BLACK = MOPackedColor.packARGB(255, 0, 0, 0);
		int WHITE = MOPackedColor.packARGB(255, 255, 255, 255);
		substanceImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		
		depthBuffer = new FloatImage(width,height);
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				
				// this gets dz from the filteredDistanceBuffer
				float dz = distanceBuffer.get(x, y);
				
				if(dz== -Float.MAX_VALUE) 
					{
					substanceImage.setRGB(x, y, BLACK);
					depthBuffer.set(x, y, -Float.MAX_VALUE);
					}
				else {
					substanceImage.setRGB(x, y, WHITE);
					float depth = distanceBufferToDepthBufferValue( x,  y);
					depthBufferExtrema.addExtremaCandidate(depth);
					depthBuffer.set(x, y, depth);
					}
			}
		}
		
		System.out.println("depth buffer extrema are " + depthBufferExtrema.limit1 + " " + depthBufferExtrema.limit2);
		
	}
	
	
	public AABox3D get3DExtents() {
		Range xExtrema = new Range();
		xExtrema.initialiseForExtremaSearch();
		
		Range yExtrema = new Range();
		yExtrema.initialiseForExtremaSearch();
		
		Range zExtrema = new Range();
		zExtrema.initialiseForExtremaSearch();
		
		
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				PVector docSpace = distanceBufferKeyImageSampler.bufferSpaceToDocSpace(new PVector(x,y));
				PVector p3d = docSpaceToWorld3D(docSpace);
				
				xExtrema.addExtremaCandidate(p3d.x);
				yExtrema.addExtremaCandidate(p3d.y);
				zExtrema.addExtremaCandidate(p3d.z);
			}
		}
		
		float minX = xExtrema.getLower();
		float minY = yExtrema.getLower();
		float minZ = zExtrema.getLower();
		
		float maxX = xExtrema.getUpper();
		float maxY = yExtrema.getUpper();
		float maxZ = zExtrema.getUpper();

		return new AABox3D(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	
	
	
	
	//
	public float get3DScale(PVector docPt) {
		// Returns the document-space distance, that is the unit 3D distance (1.0) at that document space point in the 3D scene.
		// so for points further away the distance will be shorter etc.
		// Used for scaling things accurately against the scene
		PVector this3DPoint = docSpaceToWorld3D(docPt);
		PVector unitDistance = this3DPoint.copy();
		
		
		unitDistance.y = unitDistance.y + 1;// move the point by a distance of 1 unit
		PVector shiftedDocPt = world3DToDocSpace(unitDistance);
		
		return docPt.dist(shiftedDocPt);
		
	}
	
	public PVector bufferSpaceToDocSpace(int x, int y) {
		return distanceBufferKeyImageSampler.bufferSpaceToDocSpace(new PVector(x,y));
	}
	
	public boolean isSubstance(PVector docPt) {
		PVector coord = distanceBufferKeyImageSampler.docSpaceToBufferSpace(docPt);
		
		return isSubstance((int)coord.x, (int)coord.y);
	}
	
	public boolean isSubstance(int x, int y) {
		//using absolute master buffer coords
		int packedCol = substanceImage.getRGB(x, y);
		Color c = MOPackedColor.packedIntToColor(packedCol, true);
		if( c.getRed() > 0) return true;
		return false;
	}
	

	public PVector docSpaceToWorld3D(PVector docSpace) {
		// returns the 3D point at a point in the scene using the original
		// distance values
		float distance = getDistance(docSpace);
		//System.out.println(" Geom Buffer docSpaceToWorld3D distance " + distance);
		return docSpaceToWorld3D( docSpace,  distance);
	}
	
	public PVector docSpaceToWorld3D(PVector docSpace, float distance) {
		PVector vIntoScene = getVectorIntoScene(docSpace);
		
		vIntoScene.normalize();
		return  PVector.mult(vIntoScene, distance);
	}
	
	
 
	public PVector world3DToDocSpace(PVector world3dPt) {
		// given an arbitrary 3D point in world space, return where that point would project onto
		// in doc space
		float z = world3dPt.z;
		PVector divByZ = new PVector(world3dPt.x/z, world3dPt.y/z, 1f);
		PVector pointOnVPEyeSpace = PVector.mult(divByZ, distanceCameraToViewingPlane);
		return eyeSpaceWindowCoordToDocSpace(pointOnVPEyeSpace);
	}
	
	public float getDistance(PVector docSpace) {
		// returns the filtered distance
		PVector coord = distanceBufferKeyImageSampler.docSpaceToBufferSpace(docSpace);
		
		float filteredDistance =  distanceBuffer.getPixelBilin(coord.x, coord.y); 
		//if(GlobalSettings.printOn) System.out.println(" Geom Buffer getDistance docspace in" + docSpace.toString() + " coord " + coord.toString() + " filteredDistance " + filteredDistance);
		return filteredDistance;
	}
	
	
	float getDistance(float x, float y) {
		// returns the filtered distance
		// using actual buffer pixel coords
		return distanceBuffer.getPixelBilin(x, y); 
	}
	
	

	public float getDepth(PVector docSpace) {
		PVector coord = distanceBufferKeyImageSampler.docSpaceToBufferSpace(docSpace);
		float d =  depthBuffer.getPixelBilin(coord.x, coord.y); 
		return d;
	}
	
	public float getDepth(float x, float y) {
		float d =  depthBuffer.getPixelBilin(x, y); 
		return d;
	}
	
	
	
	public float getDepthNormalised(PVector docSpace) {
		float d = getDepth( docSpace);
		return depthBufferExtrema.norm(d);
	}
	
	public float getDepthNormalised(int x, int y) {
		// using absolute buffer coords
		 float d = depthBuffer.get(x, y); 
		 return depthBufferExtrema.norm(d);
	}
	
	public float normalisedDepthToRealDepth(float normDist) {
		return  depthBufferExtrema.lerp(normDist);
	}
	
	///////////////////////////////////////////////////////////////////
	// private methods
	//
	
	
	
	
	PVector docSpaceToEyeSpaceWindowCoord(PVector docSpace) {
		PVector imgeCoord = distanceBufferKeyImageSampler.docSpaceToBufferSpace(docSpace);
		
		float originalViewImageCoordX = imgeCoord.x + originalROICropLeft;
		float originalViewImageCoordY = imgeCoord.y + originalROICropTop;
		
		float wx = (originalViewImageCoordX-originalViewWidthOver2);
		float wy = (originalViewImageCoordY-originalViewHeightOver2);
		return new PVector(wx,wy);
	}
	
	PVector eyeSpaceWindowCoordToDocSpace(PVector eyeSpaceWinCoord) {
		
		float eyeSpaceWinCoordXOffset = eyeSpaceWinCoord.x - originalROICropLeft;
		float eyeSpaceWinCoordYOffset = eyeSpaceWinCoord.y - originalROICropTop;
		
		float wx = (eyeSpaceWinCoordXOffset+originalViewWidthOver2);
		float wy = (eyeSpaceWinCoordYOffset+originalViewHeightOver2);
		PVector wp = new PVector(wx,wy);
		return distanceBufferKeyImageSampler.bufferSpaceToDocSpace(wp);
	}
	
	PVector getVectorIntoScene(PVector docSpace) {
		//returns the vector into the scene from the position (0,0,0)
		PVector eyeCoord =  docSpaceToEyeSpaceWindowCoord(docSpace);
		eyeCoord.z = distanceCameraToViewingPlane;
		return eyeCoord;
	}
	
	float getCosineVectorIntoScene(PVector docSpace) {
		PVector vIntoScene = getVectorIntoScene(docSpace);
		vIntoScene.normalize();
		PVector camVector = new PVector(0f,0f,1);
		return vIntoScene.dot(camVector);
	}
	

	// calculates the orthogonal depth at the x,y of the filtered distance buffer
	private float distanceBufferToDepthBufferValue(int x, int y) {
		PVector p = new PVector(x,y);
		PVector docSpace = distanceBufferKeyImageSampler.bufferSpaceToDocSpace(p);
		float cos = getCosineVectorIntoScene( docSpace);
		float d = getDistance(x,y);
		return d*cos;
	}

}



