package MOUtils;

import MOMaths.PVector;

public class ImageCoordinateSystem {
		private int bufferWidth, bufferHeight;
		private float aspect = 1;
		private int longestBufferEdge;
		private float documentWidth, documentHeight;
		
		public ImageCoordinateSystem(int targetBufferWidth, int targetBufferHeight) {
			
			bufferWidth = targetBufferWidth;
			bufferHeight = targetBufferHeight;
			aspect = bufferWidth/(float)bufferHeight;
			longestBufferEdge = Math.max(bufferWidth, bufferHeight);
			
			documentWidth = bufferWidth / longestBufferEdge;
			documentHeight = bufferHeight / longestBufferEdge;
		}
		
		
		// GlobalObjects.theDocument.docSpaceToNormalisedSpace(docSpace);
		// GlobalObjects.theDocument.docSpaceToNormalisedSpace(docSpace);
		// GlobalObjects.theDocument.normalisedSpaceToDocSpace(normPt);
		
		
		public PVector docSpaceToBufferSpace(PVector docPt) {

			float bx = docPt.x * longestBufferEdge;
			float by = docPt.y * longestBufferEdge;
			return new PVector(bx, by);
		}

		public PVector bufferSpaceToDocSpace(PVector p) {
			return bufferSpaceToDocSpace((int) (p.x), (int) (p.y));
		}

		public PVector bufferSpaceToDocSpace(int bx, int by) {

			float docX = bx / longestBufferEdge;
			float docY = by / longestBufferEdge;
			return new PVector(docX, docY);
		}

		public PVector docSpaceToNormalisedSpace(PVector docPt) {

			PVector buffPt = docSpaceToBufferSpace(docPt);
			return new PVector(buffPt.x / bufferWidth, buffPt.y / bufferHeight);

		}

		public PVector normalisedSpaceToDocSpace(PVector normPt) {
			// Doesn't lose precision by avoiding bufferspace methods
			float bx = normPt.x * bufferWidth;
			float by = normPt.y * bufferHeight;

			float docX = bx / longestBufferEdge;
			float docY = by / longestBufferEdge;
			return new PVector(docX, docY);

		}
		
		// these are in image-buffer space
		public int getBufferWidth() {
			return bufferWidth;
		}

		public int getBufferHeight() {
			return bufferHeight;
		}
		
		public int getLongestBufferEdge() {
			return Math.max(bufferWidth, bufferHeight);
		}

		// these are in document space
		public float getDocumentWidth() {
			return documentWidth;
		}

		public float getDocumentHeight() {
			return documentHeight;
		}
		
		public float getDocumentAspect() {
			return documentWidth / documentHeight;
		}
		
		public boolean isInsideDocumentSpace(PVector p) {
	        
	        if(isInsideXDocumentSpace(p.x) && isInsideYDocumentSpace(p.y)) return true;
	        return false;
	    }
	    
		public boolean isInsideXDocumentSpace(float x) {
	    	float w = getDocumentWidth();
	    	if(x >= 0 && x <= w) return true;
	    	return false;
	    }
	    
		public boolean isInsideYDocumentSpace(float y) {
	    	float h = getDocumentHeight();
	    	if(y >= 0 && y <= h) return true;
	    	return false;
	    	
	    }
	    
		public boolean isLandscape() {
	    	if(bufferWidth > bufferHeight) return true;
	    	return false;
	    	
	    }
		

}
