package MOUtils;

import MOMaths.PVector;
import MOMaths.Rect;

public class ImageCoordinateSystem {
		private int bufferWidth, bufferHeight;
		private int longestBufferEdge;
		private float documentWidth, documentHeight;
		
		public ImageCoordinateSystem(int targetBufferWidth, int targetBufferHeight) {
			
			bufferWidth = targetBufferWidth;
			bufferHeight = targetBufferHeight;
			
			longestBufferEdge = Math.max(bufferWidth, bufferHeight);
			
			documentWidth = bufferWidth / (float)longestBufferEdge;
			documentHeight = bufferHeight / (float)longestBufferEdge;
		}
		
		
		
		public PVector docSpaceToBufferSpace(PVector docPt) {

			float bx = docPt.x * longestBufferEdge;
			float by = docPt.y * longestBufferEdge;
			return new PVector(bx, by);
		}

		public PVector bufferSpaceToDocSpace(PVector p) {
			return bufferSpaceToDocSpace((int) (p.x), (int) (p.y));
		}
		
		public PVector bufferSpaceToDocSpace(int bx, int by) {
			
			float docX = bx / (float)longestBufferEdge;
			float docY = by / (float)longestBufferEdge;
			//System.out.println("bufferSpaceToDocSpace bx by " + bx + "  " + by + " longestBufferEdge " + longestBufferEdge + "docX docY " + docX + " " + docY);
			return new PVector(docX, docY);
		}
		
		
		public Rect docSpaceToBufferSpace(Rect docSpaceRect) {
			PVector topLeftBufferSapce = docSpaceToBufferSpace(docSpaceRect.getTopLeft());
			PVector bottomRightBufferSapce = docSpaceToBufferSpace(docSpaceRect.getBottomRight());
			return new Rect(topLeftBufferSapce, bottomRightBufferSapce);
		}
		
		public Rect bufferSpaceToDocSpace(Rect bufferSpaceRect) {
			PVector topLeftDocSapce = bufferSpaceToDocSpace(bufferSpaceRect.getTopLeft());
			PVector bottomRightDocSapce = bufferSpaceToDocSpace(bufferSpaceRect.getBottomRight());
			return new Rect(topLeftDocSapce, bottomRightDocSapce);
			
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
		
		public Rect getBufferRect() {
			return new Rect(0,0,bufferWidth,bufferHeight);
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
		
		public boolean intersectsDocumentRect(Rect docSpaceRect) {
	        Rect theDocumentRect = new Rect(0,0,getDocumentWidth(), getDocumentHeight());
	        return docSpaceRect.intersects(theDocumentRect);
	        
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
