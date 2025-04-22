package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOImage.FloatImage;
import MOImage.KeyImageSampler;
import MOImage.MOPackedColor;
import MOMaths.AABox3D;
import MOMaths.PVector;
import MOMaths.Range;
import MOMaths.Rect;
import MOUtils.ImageCoordinateSystem;

///////////////////////////////////////////////////////////////////////////////////////////////////
// Given a depth buffer and a cameraDistanceToViewingPlane, this class returns 3D eyespace points
// in both buffer, image quadrant, and doc spaces
//
// All the data access methods work in BufferSpace units, therefore any conversion to document space happens before in the calling function.
//
//
public class DepthBuffer3D {
		// substance image is the black/white mask image for substance/sky
		public BufferedImage substanceImage;
		
		// the orthogonal distance from the object to the camera plane
		// in original viewing application units
		FloatImage depthBuffer;
		
		
		Range worldXExtrema, worldYExtrema, worldZExtrema;
		
		
		int width, height;
		float widthBy2, heightBy2;
		
		float distanceCameraToViewingPlane;
		
		public ImageCoordinateSystem depthBufferCoordinateSystem;
		
		public DepthBuffer3D(FloatImage depthBuff, float distancecamToVP) {
			
			depthBuffer = depthBuff;
			
			width = depthBuffer.getWidth();
			height = depthBuffer.getHeight();
			
			widthBy2 = width/2f;
			heightBy2 = height/2f;
			
			distanceCameraToViewingPlane = distancecamToVP;
			
			//System.out.println(" width heigh of scene data   " + width + " " + height);
			//System.out.println(" pixel 3d distanceCameraToViewingPlane   " + distanceCameraToViewingPlane);
			
			depthBufferCoordinateSystem = new ImageCoordinateSystem(width,height);
			// make this now based on finding -Float.MAX_VALUE values exported
			// from the viewing application for "sky" etc.
			makeSubstanceBuffer();
			
			
			Range depthBufferExtrema = depthBuffer.getExtrema();
			float farDepth = depthBufferExtrema.getUpper();
			
			// Replace the current -Float.MAX_VALUE vaulues in the depth buffer with a 
			// "less toxic" value for when non-substance pixels may get included in bi-linear interpolation calculations
			depthBuffer.replaceMaskValue(farDepth+1);
			
			calculateWorld3DExtents();
			
			System.out.println("depth buffer extrema are " + depthBufferExtrema.limit1 + " " + depthBufferExtrema.limit2);
			System.out.println("worldZExtrema extrema are " + worldZExtrema.limit1 + " " + worldZExtrema.limit2);
		}
		
		private void makeSubstanceBuffer() {
			// creates an image that represents "substance" and "sky", 
			int BLACK = MOPackedColor.packARGB(255, 0, 0, 0);
			int WHITE = MOPackedColor.packARGB(255, 255, 255, 255);
			substanceImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);

			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
					
					float depth = depthBuffer.get(x, y);
					
					if (depth == -Float.MAX_VALUE) {
						substanceImage.setRGB(x, y, BLACK);
					} else {
						substanceImage.setRGB(x, y, WHITE);
					}
				}
			}
			
		}
		
		public PVector docSpaceToWorld3D(PVector docSpace) {
			PVector bufferSpace = docSpaceToBufferSpace(docSpace);
			return bufferSpaceToWorld3D(bufferSpace);
		}
		
		public PVector bufferSpaceToWorld3D(int x, int y) {
			PVector bufferSpace = new PVector(x,y);
			return bufferSpaceToWorld3D(bufferSpace);
		}
		
		public PVector bufferSpaceToWorld3D(PVector bufferSpace) {
			float depth = getDepthBilinear(bufferSpace);
			//System.out.println("V2_GeometryBuffer::bufferSpaceToWorld3D at " + bufferSpace.x + "," + bufferSpace.y + " is " + depth);
			return getWorld3DPoint(bufferSpace,  depth);
		}
		
		public PVector docSpaceToWorld3D(PVector docSpace, float depth) {
			// this is the method to call when using DepthRenders
			PVector bufferSpace = docSpaceToBufferSpace(docSpace);
			return getWorld3DPoint( bufferSpace, depth);
		}
		
		
		private PVector getWorld3DPoint(PVector bufferSpace, float depth) {
			PVector imgQuadSpc = bufferSpaceToImageQuadrantSpace(bufferSpace);
			
			float ex = (imgQuadSpc.x * depth)/distanceCameraToViewingPlane;
			float ey = (imgQuadSpc.y * depth)/distanceCameraToViewingPlane;
			return new PVector(ex,ey,depth);
		}
		
		public PVector bufferSpaceToDocSpace(int x, int y) {
			return depthBufferCoordinateSystem.bufferSpaceToDocSpace(new PVector(x,y));
			//return depthBufferKeyImageSampler.bufferSpaceToDocSpace(new PVector(x,y));
		}
		
		private PVector bufferSpaceToDocSpace(PVector bufferSpace) {
			return depthBufferCoordinateSystem.bufferSpaceToDocSpace(bufferSpace);
			//return depthBufferKeyImageSampler.bufferSpaceToDocSpace(bufferSpace);
			
		}
		
		public PVector docSpaceToBufferSpace(PVector docSpace) {
			return depthBufferCoordinateSystem.docSpaceToBufferSpace(docSpace);
			//return depthBufferKeyImageSampler.docSpaceToBufferSpace(docSpace);
		}
		
		
		
		public boolean isSubstance(PVector docPt) {
			// using docSpace
			PVector coord = docSpaceToBufferSpace(docPt);
			return isSubstance((int)coord.x, (int)coord.y);
		}
		
		public boolean isSubstance(int x, int y) {
			//using absolute master buffer coords
			if(depthBufferCoordinateSystem.isInsideBufferSpace(x, y)==false) return false;
			int packedCol = substanceImage.getRGB(x, y);
			Color c = MOPackedColor.packedIntToColor(packedCol, true);
			if( c.getRed() > 0) return true;
			return false;
		}
		
		private PVector bufferSpaceToImageQuadrantSpace(PVector bufferSpace) {
			float i = bufferSpace.x - widthBy2;
			float j = bufferSpace.y - heightBy2;
			return new PVector(i,j);
		}
		
		private PVector imageQuadrantSpaceToBufferSpace(PVector imageQuadrantSpace) {
			float x = imageQuadrantSpace.x + widthBy2;
			float y = imageQuadrantSpace.y + heightBy2;
			return new PVector(x,y);
		}
		
		
		public float getDepthBilinear(PVector bufferSpace) {
			return depthBuffer.getPixelBilin(bufferSpace.x, bufferSpace.y);
		}
		
		
		
		public float getDepth(int x, int y) {
			if(depthBufferCoordinateSystem.isInsideBufferSpace(x, y)==false) return worldZExtrema.getUpper();
			return depthBuffer.get(x,y);
		}
		
		public float normaliseDepth(float worldDepth) {
			return worldZExtrema.norm(worldDepth);
		}
		
		public Range getDepthExtrema() {
			return worldZExtrema;
		}
		
		public PVector get3DDisplacedDocPoint(PVector docPt, PVector displacement) {
			// Returns the document-space point of a displaced 3D point found at docPt, that is the unit 3D distance (1.0) at that document space point in the 3D scene.
			// so for points further away the distance will be shorter etc.
			// Used for scaling things accurately against the scene
			PVector this3DPoint = docSpaceToWorld3D(docPt);
			PVector displaced3DPoint = PVector.add(this3DPoint, displacement);

			PVector shiftedDocPt = world3DToDocSpace(displaced3DPoint);
			return shiftedDocPt;
		}
		
		public float get3DScale(PVector docPt) {
			// Returns the document-space distance, that is the unit 3D distance (1.0) at that document space point in the 3D scene.
			// so for points further away the distance will be shorter etc.
			// Used for scaling things accurately against the scene
			PVector displacedDocPt = get3DDisplacedDocPoint( docPt, new PVector(0,1,0));
			
			// now work out the difference in doc space, this returns the doc space distance between 
			// 2 points, with a difference of 1 unit in Y, at that distance in 3D space
			// This can than  be used to scale any asset by multiplying this amount, by the Y size of the asset.gfj
			return docPt.dist(displacedDocPt);
			
		}
		
		public PVector world3DToBufferSpace(PVector world3dPt) {
			// given an arbitrary 3D point in world space, return where that point would project onto
			// in doc space
			float z = world3dPt.z;
			
			float x = (world3dPt.x * distanceCameraToViewingPlane)/z;
			float y = (world3dPt.y * distanceCameraToViewingPlane)/z;
			
			PVector screenPointQuadSpace = new PVector(x,y,z);
			
			return imageQuadrantSpaceToBufferSpace(screenPointQuadSpace);
		}
		
		public PVector world3DToDocSpace(PVector world3dPt) {
			// given an arbitrary 3D point in world space, return where that point would project onto
			// in doc space
			PVector bufferSpace = world3DToBufferSpace(world3dPt);
			return depthBufferCoordinateSystem.bufferSpaceToDocSpace(bufferSpace);
		}
		
		public AABox3D getExtents() {
			float xLo = worldXExtrema.getLower();
			float yLo = worldYExtrema.getLower();
			float zLo = worldZExtrema.getLower();
			
			float xHi = worldXExtrema.getUpper();
			float yHi = worldYExtrema.getUpper();
			float zHi = worldZExtrema.getUpper();
			
			PVector lo = new PVector(xLo,yLo,zLo);
			PVector hi = new PVector(xHi,yHi,zHi);
			
			return new AABox3D(lo,hi);
		}
		
		private void calculateWorld3DExtents() {
			worldXExtrema = new Range(); worldXExtrema.initialiseForExtremaSearch();
			worldYExtrema = new Range(); worldYExtrema.initialiseForExtremaSearch();
			worldZExtrema = new Range(); worldZExtrema.initialiseForExtremaSearch();
			
			for(int y = 0; y < depthBuffer.getHeight(); y++) {
				
				for(int x = 0; x < depthBuffer.getWidth(); x++) {
					if( isSubstance( x,  y) == false ) continue;
					
					PVector p3 = bufferSpaceToWorld3D(x,y);
					worldXExtrema.addExtremaCandidate(p3.x);
					worldYExtrema.addExtremaCandidate(p3.y);
					worldZExtrema.addExtremaCandidate(p3.z);
				}
			}
			
			
		}
		
		
		
		
}
