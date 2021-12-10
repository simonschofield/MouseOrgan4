package MOCompositing;
import MOImageCollections.ImageSampleGroup;
import MOMaths.PVector;
import MOMaths.Rect;

public class PermittedPasteArea {
	// The rect is SET using Normalized coordinates (0..1 in both x and y), but STORED using docSpace coordinates
	// If the PermittedPasteArea is active
	//	- then some cropping will occur. 
	// 	- if sprites are completely outside they will be EXCLUDED
	//  - if they are completely within they will be pasted as normal
	//  - if the fall on a boundary, then a decision is made as to the action
	//  	-- if the boundary is comprised of just one edge then that edge action takes place
	//      -- if the boundary has two edges, then EXCLUDED takes precedence over CROP or BESPOKE_CROP
	// the actions that can be done on an edge are
	
	// NONE : allow the paste in full
	// EXCLUDE : do not paste any sprite overlapping an edge with this setting
	// CROP : Crop any sprite to the hard edge, also default action
	// BESPOKE_CROP : Apply a bespoke crop to a sprite overlapping this edge
	boolean isActive = false;
	
	Rect permittedPasteAreaRect;
	
	String leftEdgeAction = "CROP";
	String topEdgeAction = "CROP";
	String rightEdgeAction = "CROP";
	String bottomEdgeAction = "CROP";
	
	ImageSampleGroup permittedPasteAreaCropImages;
	MainDocumentRenderTarget theRenderTarget;
	
	
	PermittedPasteArea(MainDocumentRenderTarget rt){
		theRenderTarget = rt;
		setPermittedPasteRectWithNomalizedCoords( 0,  0,  1,  1);
	}
	
	void set(float left, float top, float right, float bottom, String edgeAction,  ImageSampleGroup bespokeCropImage) {
		set( left,  top,  right,  bottom, edgeAction,edgeAction,edgeAction,edgeAction,  bespokeCropImage);
	}
	
	public void setActive(boolean active) {
		isActive = active;
	}
	
	boolean isActive() {
		return isActive;
	}
	
	void set(float left, float top, float right, float bottom, String leftAction, String topAction, String rightAction, String bottomAction, ImageSampleGroup bespokeCropImage){
		setPermittedPasteRectWithNomalizedCoords( left,  top,  right,  bottom);
		leftEdgeAction = leftAction;
		topEdgeAction = topAction;
		rightEdgeAction = rightAction;
		bottomEdgeAction = bottomAction;
		
		permittedPasteAreaCropImages = bespokeCropImage;
		isActive = true;
	}
	
	private void setPermittedPasteRectWithNomalizedCoords(float left, float top, float right, float bottom) {
		PVector topLeftNormSpace = new PVector(left,top);
		PVector bottomRightNormSpace = new PVector(right,bottom);
		PVector topLeft = theRenderTarget.coordinateSystem.normalisedSpaceToDocSpace(topLeftNormSpace);
		PVector bottomRight = theRenderTarget.coordinateSystem.normalisedSpaceToDocSpace(bottomRightNormSpace);
		//PVector bottomRight = theRenderTarget.coordinateSpaceCoverter.normalizedSpaceToDocSpace(bottomRightNormSpace);
		
		//normalisedSpaceToDocSpace
		permittedPasteAreaRect = new Rect(topLeft, bottomRight);
	}
	
	public Rect getRect() {
		return permittedPasteAreaRect.copy();
	}
	
	boolean isPointInside(PVector docSpacePt) {
		
		return permittedPasteAreaRect.isPointInside(docSpacePt);
	}
	
	boolean isFullyPermitted(Rect r) {
		// simple interface for drawn shapes based on their rectangle, either in or out!
		if(isActive == false) return true;
		return r.isWhollyInsideOther(permittedPasteAreaRect);
	}
	
	String cropEdgeDecision(String overlapReport) {
		// given an intersection edge report from the Sprite
		// decide what edge crop action to do.
		
		// if totally outside the ppa
		if(overlapReport.contentEquals("NONE")) return "EXCLUDE";
		
		String action = getActionByEdgeString(overlapReport);
		if(action.contentEquals("MULTIPLE_EDGES")) {
			// if you get this far then the overlapReport must contains two or more edges, and we have to make a decision on this
			return getDominantAction(overlapReport);
		}
		// ..then the overlapReport must contain only one edge, so return the edgeAction
		return action;
		
		//System.out.println("cropEdgeDecision on intersection: " + overlapReport);
		
		
	}
	
	String getDominantAction(String overlapReport) {
		// only allows for first two string components
		String[] edgeStrings = overlapReport.split(",");
		if( edgeStrings.length < 2) {
			System.out.println("PermittedPasteArea getDominantAction: problem with number of edges reported - num = " + edgeStrings.length);
			return "EXCLUDE";
		}
		
		String edgeString1 = edgeStrings[0];
		String edgeString2 = edgeStrings[1];
		String action1 = getActionByEdgeString(edgeString1);
		String action2 = getActionByEdgeString(edgeString2);
		if( action1.contentEquals(action2)) return action1;
		
		if(action1.contentEquals("EXCLUDE") || action2.contentEquals("EXCLUDE")) return "EXCLUDE";
		if(action1.contentEquals("BESPOKE_CROP") || action2.contentEquals("BESPOKE_CROP")) return "BESPOKE_CROP";
		// should cause a crash, but should never get here
		System.out.println("PermittedPasteArea getDominantAction: problem with decided action between " + action1 +" and "+ action2);
		return null;
	}
	
	String getActionByEdgeString(String edgeString) {
		
		if(edgeString.contentEquals("TOP")) return topEdgeAction;
		if(edgeString.contentEquals("LEFT")) return leftEdgeAction;
		if(edgeString.contentEquals("RIGHT")) return rightEdgeAction;
		if(edgeString.contentEquals("BOTTOM")) return bottomEdgeAction;
		return "MULTIPLE_EDGES";
	}
	
	String reportPermittedPasteAreaOverlap(ImageSprite sprite) {
		Rect r = sprite.getPasteRectDocSpace(theRenderTarget);
		return r.reportIntersection(permittedPasteAreaRect);

	}
	

	boolean isSpriteWhollyInside(ImageSprite sprite) {
		Rect r = sprite.getPasteRectDocSpace(theRenderTarget); 
		return r.isWhollyInsideOther( this.permittedPasteAreaRect );
		
	}
	
	
	
}