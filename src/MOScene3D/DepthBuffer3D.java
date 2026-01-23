package MOScene3D;

import java.awt.Color;
import java.awt.image.BufferedImage;

import MOImage.FloatImage;
import MOImage.MOPackedColor;
import MOMaths.PVector;
import MOMaths.Range;
import MOUtils.ImageCoordinateSystem;


/**
 * Given a depth buffer and a cameraDistanceToViewingPlane, this class returns 3D eyespace points in both buffer, image quadrant, and doc spaces
 */
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

		//public boolean flipY = true;


		/**
		 * Given a depth buffer and a cameraDistanceToViewingPlane, this class returns 3D eyespace points in both buffer, image quadrant, and doc spaces
		 * @param depthBuff - a depth buffer of actual 3D depth values
		 * @param distancecamToVP - the distance of the camera to the viewing plane in 3D units
		 */
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

			// Replace the current -Float.MAX_VALUE values in the depth buffer with a
			// "less toxic" value for when non-substance pixels may get included in bi-linear interpolation calculations
			depthBuffer.replaceMaskValue(farDepth+1);

			calculateWorld3DExtents();

			System.out.println("depth buffer extrema are " + depthBufferExtrema.limit1 + " " + depthBufferExtrema.limit2);
			System.out.println("worldZExtrema extrema are " + worldZExtrema.limit1 + " " + worldZExtrema.limit2);
		}


		/**
		 * Calculates the world-space surface 3D point at a given document-space 2D point
		 * @param docSpace - the point on the scene in document-space
		 * @return -  the 3D world-space surface point at docSpace as a PVector
		 */
		public PVector docSpaceToWorld3D(PVector docSpace) {
			PVector bufferSpace = docSpaceToBufferSpace(docSpace);
			return bufferSpaceToWorld3D(bufferSpace);
		}
		
		
		/**
		 * Calculates the world-space surface 3D point at a given buffer-space 2D point
		 * @param x - a depth buffer pixel x coordinate
		 * @param y - a depth buffer pixel y coordinate
		 * @return -  the 3D world-space surface point represented at the buffer location at x,y, as a PVector
		 */
		public PVector bufferSpaceToWorld3D(int x, int y) {
			PVector bufferSpace = new PVector(x,y);
			return bufferSpaceToWorld3D(bufferSpace);
		}

		/**
		 * Calculates the world-space surface 3D point at a given buffer-space 2D point, passed as a vector
		 * @param bufferSpace - the point on the scene in buffer-space, as the vector's x and y components
		 * @return -  the 3D world-space point at bufferSpace as a PVector
		 */
		public PVector bufferSpaceToWorld3D(PVector bufferSpace) {
			float depth = getDepthBilinear(bufferSpace);
			//System.out.println("V2_GeometryBuffer::bufferSpaceToWorld3D at " + bufferSpace.x + "," + bufferSpace.y + " is " + depth);
			return getWorld3DPoint(bufferSpace,  depth);
		}

		
		/**
		 * Given an arbitrary docSpace point, and an arbitrary depth, a 3D world space is calculated and returned. 
		 * @param docSpace - the point on the scene in document-space
		 * @param depth - The depth of the 3D point to be calculated
		 * @return - the 3D world-space point at docSpace and depth as a PVector
		 */
		public PVector docSpaceToWorld3D(PVector docSpace, float depth) {
			// this is the method to call when using DepthRenders
			PVector bufferSpace = docSpaceToBufferSpace(docSpace);
			return getWorld3DPoint( bufferSpace, depth);
		}

		/**
		 * Given an arbitrary depth buffer pixel location, and an arbitrary depth, a 3D world space is calculated and returned. 
		 * @param bufferSpace - the point on the scene in buffer-space
		 * @param depth - The depth of the 3D point to be calculated
		 * @return - the 3D world-space point at buffer point, and depth as a PVector
		 */
		private PVector getWorld3DPoint(PVector bufferSpace, float depth) {
			PVector imgQuadSpc = bufferSpaceToImageQuadrantSpace(bufferSpace);

			float ex = (imgQuadSpc.x * depth)/distanceCameraToViewingPlane;
			float ey = (imgQuadSpc.y * depth)/distanceCameraToViewingPlane;
			return new PVector(ex,ey,depth);
		}
		

		/**
		 * Simple conversion between buffer-space and document-space
		 * @param x  - the buffer space x coordinate
		 * @param y  - the buffer space y coordinate
		 * @return - the document space equivalent of x,y in buffer space, as a PVector
		 */
		public PVector bufferSpaceToDocSpace(int x, int y) {
			return depthBufferCoordinateSystem.bufferSpaceToDocSpace(new PVector(x,y));
		}
		
		/**
		 * Simple conversion between buffer-space and document-space
		 * @param bufferSpace  - the point on the scene in buffer-space, as the vector's x and y components
		 * @return - the document space equivalent of x,y in buffer space, as a PVector
		 */
		private PVector bufferSpaceToDocSpace(PVector bufferSpace) {
			return depthBufferCoordinateSystem.bufferSpaceToDocSpace(bufferSpace);
		}
		
		/**
		 * Simple conversion between document-space and buffer-space
		 * @param docSpace - the point on the scene in document-space
		 * @return - the buffer space equivalent of x,y in buffer space, as a PVector's x and y components
		 */
		public PVector docSpaceToBufferSpace(PVector docSpace) {
			return depthBufferCoordinateSystem.docSpaceToBufferSpace(docSpace);
		}
		
		
		
		/**
		 * Returns the (normalised) surface normal of doc-space point as a 3D vector. In normal conditions, the X will vary +-, the Y will be +ve (as the vector is pointing up in 3D), and the Z will be "out of the scene", therefore -ve.
		 * @param docSpace - the point on the scene in document-space
		 * @return the normalised surface normal at p
		 */
		public PVector docSpaceToSurfaceNormal3D(PVector docSpace) {
			PVector bufferSpace = docSpaceToBufferSpace(docSpace);
			int x = (int) bufferSpace.x;
			int y = (int) bufferSpace.y;
			
			// these should be OK for edge values as the depth reading is clamped within the extents of the depth buffer
			PVector above = bufferSpaceToWorld3D( x,  y-1);
			PVector below = bufferSpaceToWorld3D( x,  y+1);
			PVector vertical = PVector.sub(below, above);
			
			PVector left = bufferSpaceToWorld3D( x-1,  y);
			PVector right = bufferSpaceToWorld3D( x+1,  y);
			PVector horizontal = PVector.sub(right, left);
			
			PVector sn = horizontal.cross(vertical); 
			return sn.normalize();
		}



		/**
		 * Returns if the is "substance" (or "sky") at the document space point. Substance will have a varying depth value. "Sky" values are fixed at -Float.MAX_VALUE
		 * @param docSpace
		 * @return boolean. True if there is "substance"
		 */
		public boolean isSubstance(PVector docSpace) {
			// using docSpace
			PVector coord = docSpaceToBufferSpace(docSpace);
			return isSubstance((int)coord.x, (int)coord.y);
		}

		/**
		 * * Returns if the is "substance" (or "sky") at the document space point. Substance will have a varying depth value. "Sky" values are fixed at -Float.MAX_VALUE
		 * @param x - the depth buffer x coordinate
		 * @param y - the depth buffer y coordinate
		 * @return @return boolean. True if there is "substance"
		 */
		public boolean isSubstance(int x, int y) {
			//using absolute master buffer coords
			if(!depthBufferCoordinateSystem.isInsideBufferSpace(x, y)) {
				return false;
			}
			int packedCol = substanceImage.getRGB(x, y);
			Color c = MOPackedColor.packedIntToColor(packedCol, true);
			if( c.getRed() > 0) {
				return true;
			}
			return false;
		}

		/**
		 * @param bufferSpace
		 * @return
		 */
		public float getDepthBilinear(PVector bufferSpace) {
			return depthBuffer.getPixelBilin(bufferSpace.x, bufferSpace.y);
		}


		/**
		 * @param x
		 * @param y
		 * @return
		 */
		public float getDepth(int x, int y) {
			if(!depthBufferCoordinateSystem.isInsideBufferSpace(x, y)) {
				return worldZExtrema.getUpper();
			}
			return depthBuffer.get(x,y);
		}

		/**
		 * @param worldDepth
		 * @return
		 */
		public float normaliseDepth(float worldDepth) {
			return worldZExtrema.norm(worldDepth);
		}

		/**
		 * @return
		 */
		public Range getDepthExtrema() {
			return worldZExtrema;
		}

		/**
		 * @param docPt
		 * @param displacement
		 * @return
		 */
		public PVector get3DDisplacedDocPoint(PVector docPt, PVector displacement) {
			// Returns the document-space point of a displaced 3D point found at docPt, that is the unit 3D distance (1.0) at that document space point in the 3D scene.
			// so for points further away the distance will be shorter etc.
			// Used for scaling things accurately against the scene
			PVector this3DPoint = docSpaceToWorld3D(docPt);
			PVector displaced3DPoint = PVector.add(this3DPoint, displacement);

			PVector shiftedDocPt = world3DToDocSpace(displaced3DPoint);
			return shiftedDocPt;
		}

		/**
		 * @param docPt
		 * @return
		 */
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

		/**
		 * @param world3dPt
		 * @return
		 */
		public PVector world3DToBufferSpace(PVector world3dPt) {
			// given an arbitrary 3D point in world space, return where that point would project onto
			// in doc space
			float z = world3dPt.z;

			float x = (world3dPt.x * distanceCameraToViewingPlane)/z;
			float y = (world3dPt.y * distanceCameraToViewingPlane)/z;

			PVector screenPointQuadSpace = new PVector(x,y,z);

			return imageQuadrantSpaceToBufferSpace(screenPointQuadSpace);
		}

		/**
		 * @param world3dPt
		 * @return
		 */
		public PVector world3DToDocSpace(PVector world3dPt) {
			// given an arbitrary 3D point in world space, return where that point would project onto
			// in doc space
			PVector bufferSpace = world3DToBufferSpace(world3dPt);
			return depthBufferCoordinateSystem.bufferSpaceToDocSpace(bufferSpace);
		}

		/**
		 * @return
		 */
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
		
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////
		// private below here
		//
		//
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

		//////////////////////////////////////////////////////////////////////////////////////
		// Image Quadrant Space places (0,0) at the centre of the image. So that +v Y is in 3D is going "up"
		// we flip the buffer-space Y to be +ve going up.
		// Quadrant space is therefore described (xyz)
		//
		// (-ve X ,+ve Y, depth)	(+ve X ,+ve Y, depth)
		// (-ve X ,-ve Y, depth)	(+ve X ,-ve Y, depth)
		//


		private PVector bufferSpaceToImageQuadrantSpace(PVector bufferSpace) {
			float i = bufferSpace.x - widthBy2;
			float j;
			//if(flipY) {
			j = (height-bufferSpace.y) - heightBy2;
			//}else {
			//	j = (bufferSpace.y) - heightBy2;
			//}
			return new PVector(i,j);
		}

		private PVector imageQuadrantSpaceToBufferSpace(PVector imageQuadrantSpace) {
			float x = imageQuadrantSpace.x + widthBy2;
			float y;
			//if(flipY) {
			y = height-(imageQuadrantSpace.y + heightBy2);
			//}else {
			//	y = (imageQuadrantSpace.y + heightBy2);
			//}

			return new PVector(x,y);
		}


		

		private void calculateWorld3DExtents() {
			worldXExtrema = new Range(); worldXExtrema.initialiseForExtremaSearch();
			worldYExtrema = new Range(); worldYExtrema.initialiseForExtremaSearch();
			worldZExtrema = new Range(); worldZExtrema.initialiseForExtremaSearch();

			for(int y = 0; y < depthBuffer.getHeight(); y++) {

				for(int x = 0; x < depthBuffer.getWidth(); x++) {
					if( !isSubstance( x,  y) ) {
						continue;
					}

					PVector p3 = bufferSpaceToWorld3D(x,y);
					worldXExtrema.addExtremaCandidate(p3.x);
					worldYExtrema.addExtremaCandidate(p3.y);
					worldZExtrema.addExtremaCandidate(p3.z);
				}
			}


		}




}
