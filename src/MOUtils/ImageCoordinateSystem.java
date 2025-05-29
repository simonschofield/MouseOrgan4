package MOUtils;

import MOImage.ImageDimensions;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Rect;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// The image coordinate system is initialised once at the start and does not change throughout the session.
// It contains the document space rect for the current session, the BufferSpace render width and height
// It has no notion of session scale. Any session scaling is done BEFORE and baked into the  coordinate system
//
// After that, it provides conversions between BufferSpace and Document space (and visa-versa) pixel coordinates
// And documentSpace to buffer space measurement conversion
//
public class ImageCoordinateSystem {
		private int bufferWidth, bufferHeight;
		private float documentWidth, documentHeight;
		private Rect documentSpaceRect;

		
		//
		// For normal or master images
		public ImageCoordinateSystem(int targetBufferWidth, int targetBufferHeight) {
			
			bufferWidth = targetBufferWidth;
			bufferHeight = targetBufferHeight;
			
			float longestBufferEdge = Math.max(bufferWidth, bufferHeight);
			
			documentWidth = bufferWidth / (float)longestBufferEdge;
			documentHeight = bufferHeight / (float)longestBufferEdge;
			
			documentSpaceRect = new Rect(0,0,documentWidth,documentHeight);
			
			
		}
		
		// for ROI images subserviant to a master render
		public ImageCoordinateSystem(int targetBufferWidth, Rect asRoi) {
			documentSpaceRect = asRoi.copy();
			
			bufferWidth = targetBufferWidth;
			bufferHeight = (int) (targetBufferWidth / documentSpaceRect.aspect()   + 0.5f);
			
			documentWidth = documentSpaceRect.getWidth();
			documentHeight = documentSpaceRect.getHeight();
		}
		
		
		public ImageCoordinateSystem copy() {
			return new ImageCoordinateSystem(bufferWidth, documentSpaceRect);
		}
		
		
		public String toStr() {
			String spc = " ";
		    String s1 = "ImageCoordinateSystem :: ";
		    String ROI = "ROI Rect" + spc + documentSpaceRect.toStr();
			String dims = "Buffer w h " + spc + bufferWidth + spc + bufferHeight;
			
			return s1 + spc + ROI + spc + dims;
		}
		
		
		public PVector docSpaceToBufferSpace(PVector docPt) {
			
			PVector norm = documentSpaceRect.norm(docPt);
			
			float bx = norm.x * bufferWidth;
			float by = norm.y * bufferHeight;
			return new PVector(bx, by);
		}
		
		public PVector docSpaceToBufferSpaceClamped(PVector docPt) {

			PVector bSpace = docSpaceToBufferSpace(docPt);
			bSpace.x = MOMaths.constrain(bSpace.x, 0, bufferWidth-1);
			bSpace.y = MOMaths.constrain(bSpace.y, 0, bufferHeight-1);
			return bSpace;
		}
		
		// These are used when making measurements, in that it is zeroed (docSpace 0 -> bufferSpace 0)
		public float docSpaceUnitToBufferSpaceUnit(float docSpaceMeasure) {
			// because (in ROI space) the doc space does not necessarily start at 0,0 anymore
			// we need to return the distance between two arbitrary doc space points apart by dsu
			PVector p1 = new PVector(0,0);
			PVector p2 = new PVector(0,docSpaceMeasure);
			PVector bsp1 = docSpaceToBufferSpace(p1);
			PVector bsp2 = docSpaceToBufferSpace(p2);
			
			return Math.abs(   bsp2.y - bsp1.y    );
		}
		
		// These are used when making measurements, in that it is zeroed (docSpace 0 -> bufferSpace 0)
		public Rect docSpaceUnitToBufferSpaceUnit(Rect docSpaceRect) {
			float bsL = GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(docSpaceRect.left);
			float bsT = GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(docSpaceRect.top);
			float bsW = GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(docSpaceRect.getWidth());
			float bsH = GlobalSettings.getTheDocumentCoordSystem().docSpaceUnitToBufferSpaceUnit(docSpaceRect.getHeight());
			return new Rect(bsL,bsT,bsW,bsH);
		}

		public PVector bufferSpaceToDocSpace(PVector p) {
			return bufferSpaceToDocSpace((int) (p.x), (int) (p.y));
		}
		
		public PVector bufferSpaceToDocSpace(int bx, int by) {
			
			// get the normalised coordinates for the buffer space
			float nx = bx/(float)bufferWidth;
			float ny = by/(float)bufferHeight;
			
			PVector normPoint = new PVector(nx, ny);
			
			return documentSpaceRect.interpolate(normPoint);
		}
		
		
		public Rect docSpaceToBufferSpace(Rect docSpaceRect) {
			PVector topLeftBufferSapce = docSpaceToBufferSpace(docSpaceRect.getTopLeft());
			PVector bottomRightBufferSapce = docSpaceToBufferSpace(docSpaceRect.getBottomRight());
			return new Rect(topLeftBufferSapce, bottomRightBufferSapce);
		}
		
		public Rect bufferSpaceToDocSpace(Rect bufferSpaceRect) {

			PVector topLeftDocSapce = bufferSpaceToDocSpace(bufferSpaceRect.getTopLeft());
			PVector bottomRightDocSapce = bufferSpaceToDocSpace(bufferSpaceRect.getBottomRight());
			//System.out.println("bufferSpaceToDocSpace rect in " + bufferSpaceRect.toStr() + " topLeftDocSapce " + topLeftDocSapce.toStr() + " bottomRightDocSapce " + bottomRightDocSapce.toStr());
			return new Rect(topLeftDocSapce, bottomRightDocSapce);
			
		}

		

		public PVector docSpaceToNormalisedSpace(PVector docPt) {

			return documentSpaceRect.norm(docPt);
			
		}

		public PVector normalisedSpaceToDocSpace(PVector normPoint) {
			
			return documentSpaceRect.interpolate(normPoint);

		}
		
		// these are in image-buffer space
		public int getBufferWidth() {
			return bufferWidth;
		}

		public int getBufferHeight() {
			return bufferHeight;
		}
		
		public ImageDimensions getBufferDimensions() {
			return new ImageDimensions(bufferWidth, bufferHeight);
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
		
		public Rect getDocumentRect() {
			//return new Rect(0,0,documentWidth,documentHeight);
			return documentSpaceRect.copy();
		}
		
		public boolean isInsideDocumentSpace(PVector p) {
	        
	        return documentSpaceRect.isPointInside(p);
	        
	    }
		
		
	    
		public boolean isInsideXDocumentSpace(float x) {
	    	float w = getDocumentWidth();
	    	float l = documentSpaceRect.left;
	    	if(x >= l && x <= l+w) return true;
	    	return false;
	    }
	    
		public boolean isInsideYDocumentSpace(float y) {
	    	float h = getDocumentHeight();
	    	float t = documentSpaceRect.top;
	    	if(y >= t && y <= t+h) return true;
	    	return false;
	    	
	    }
		
		public boolean isInsideBufferSpace(int x, int y) {
			if(x >= 0 && x < bufferWidth && y >= 0 && y < bufferHeight) return true;
			return false;
			
		}
		
		
	    
		public boolean isLandscape() {
	    	if(bufferWidth > bufferHeight) return true;
	    	return false;
	    	
	    }

}




