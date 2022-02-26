package MOCompositing;





import java.awt.Color;

import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Vertices2;
import MOUtils.GlobalSettings;

//////////////////////////////////////////////////////////////////////
//Enables the tracking of arbitrary points within a sprite after multiple transforms to the sprite
//The point is accessed using normalised coordinates within the sprite's image-space, as if the shape has not been transformed
//( (0,0) is top left (1,1) is bottom right), in the same way the sprite's origin is defined.
//So, after scaling, rotating, mirroring and bending a sprite, you can find where point (0.75,0.666) (using normalised coords) 
//is positioned within documentSpace
//This then enables you to 
//- sample a key image at transformedImagePoint to determine what the value is under that sprite at that point
//- link sprites together at known points to create linked and hinged items

//All internal units are in the pixel coordinate space (although with float accuracy).

//The transforms should be applied AFTER the BufferedImage has been transformed, as the calculation for ROT and SHEAR
//depend on the new BufferedImage Size
//

public class SpriteImageQuad{
	Sprite theSprite;


	// these are the points in BUFFER SPACE of the Sprite Image which are transformed
	// They form a Quad
	// VertA, VertB
	// VertC, VertD
	// and represent the bounding area for the sprite image under scale, rotation and shear
	PVector VertA, VertB, VertC, VertD;

	SpriteImageQuad(Sprite sprite){
		theSprite = sprite;


		VertA = PVector.ZERO();
		VertB = new PVector(theSprite.getImageWidth(),0);
		VertC = new PVector(0,theSprite.getImageHeight());
		VertD = new PVector(theSprite.getImageWidth(),theSprite.getImageHeight());
		
	}
	
	
	SpriteImageQuad copy(Sprite s) {
		SpriteImageQuad cpy = new SpriteImageQuad(s);
		
		cpy.VertA = VertA.copy();
		cpy.VertB = VertB.copy();
		cpy.VertC = VertC.copy();
		cpy.VertD = VertD.copy();
		
		return cpy;
	}
	

	public Vertices2 getSpriteBufferSpaceQuadVertices() {
		// note that the vertices are added in a clockwise way
		// THis returns the buffer space quad
		Vertices2 verts = new Vertices2();
		verts.add(VertA);
		verts.add(VertB);
		verts.add(VertD);
		verts.add(VertC);
		verts.close();
    	return verts;
    }
	
	
	public void printVertices() {
		System.out.println("CornerA " + VertA.toStr());
		System.out.println("CornerB " + VertB.toStr());
		System.out.println("CornerC " + VertC.toStr());
		System.out.println("CornerD " + VertD.toStr());
		System.out.println();
    }

	PVector getQuadDocumentPoint(PVector pastePoint, float normX, float normY) {
		// pastePoint is in document space
		// normx and normy are in normalised space within the original sprite image-space - so before any transforms are applied

		// this is the key function. It enables the user to track any point in the original image
		// after multiple transformations. 
		// so given a pastePoint in document space, you may wish to know where the top centre point of the image has got to
		// in documentSpace coordinates
		// PVector transformedImagePoint = getDocumentPointUsingNormalisedCoordinates( pastePoint, 0.5 , 0.0) would return that point

		PVector pixelLocOfEnquiryPtInQuad = getQuadPointBufferCoords( normX,  normY);
		//System.out.println(debugQuadPointsToStr());
		//System.out.println("Pixel Loc in sprite of Enquiry Nom Point " + normX + " " + normY + " is " + pixelLocOfEnquiryPtInQuad.toStr());
		//PVector spriteOriginPixelLoc = new PVector(theSprite.origin.x * getImageWidth(), theSprite.origin.y * getImageHeight());
		PVector spriteOriginPixelLoc = theSprite.getOriginInSpriteBufferSpace();
		PVector pixCoordOfDocumentPastePoint = GlobalSettings.getTheDocumentCoordSystem().docSpaceToBufferSpace(pastePoint);

		PVector pixelOffsetEnquryPointFomSpriteOrigin = new PVector(pixelLocOfEnquiryPtInQuad.x - spriteOriginPixelLoc.x, pixelLocOfEnquiryPtInQuad.y - spriteOriginPixelLoc.y);
		PVector pixelCoordinateInDocument = pixCoordOfDocumentPastePoint.add(pixelOffsetEnquryPointFomSpriteOrigin);
		return GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(pixelCoordinateInDocument);
	}



	PVector getQuadPointBufferCoords(float normX, float normY) {
		// returns the pixel location of a quad point within the 
		// sprite's image
		PVector topLinePoint = PVector.lerp(VertA, VertB, normX);
		PVector bottomLinePoint = PVector.lerp(VertC, VertD, normX);
		return PVector.lerp(topLinePoint, bottomLinePoint, normY);

	}

	void applyTranslation(PVector t) {
		VertA = VertA.add(t);
		VertB = VertB.add(t);
		VertC = VertC.add(t);
		VertD = VertD.add(t);

	}


	void applyScale(float sx, float sy) {
		// should always work even if the image had been previously rotated
		VertA = VertA.scale(sx, sy);
		VertB = VertB.scale(sx, sy);
		VertC = VertC.scale(sx, sy);
		VertD = VertD.scale(sx, sy);

	}

	void applyRotation(float degrees, PVector oldCentre) {
		// rotates round the centre of the image
		//System.out.println("applyRotation::just before rotation");
		//printVertices();
		//System.out.println("ImageQuad: applyRotation old centre" + oldCentre.toStr() );
		VertA = MOMaths.rotatePoint(VertA, oldCentre, degrees);
		VertB = MOMaths.rotatePoint(VertB, oldCentre, degrees);
		VertC = MOMaths.rotatePoint(VertC, oldCentre, degrees);
		VertD = MOMaths.rotatePoint(VertD, oldCentre, degrees);
		

		// then shift all the points to be round the new centre
		// i.e. shift all by newCentre-oldCentre
		PVector newCentre = getCentre();
		PVector centreOffset = PVector.sub(newCentre, oldCentre);
		applyTranslation(centreOffset);
		//System.out.println("ImageQuad: applyRotation new centre" + newCentre.toStr() );
		
	}


	void applyHorizontalTopShear(float dxTop) {
		// applies the shear to the image
		// so quad points are sheared according to their relative Y position within the image rectangle


		float tlYNorm = MOMaths.norm(VertA.y, getImageHeight(), 0);
		float trYNorm = MOMaths.norm(VertB.y,  getImageHeight(), 0);
		float blYNorm = MOMaths.norm(VertC.y, getImageHeight(), 0);
		float brYNorm = MOMaths.norm(VertD.y,  getImageHeight(), 0);

		VertA.x += (dxTop * tlYNorm);
		VertB.x += (dxTop * trYNorm);
		VertC.x += (dxTop * blYNorm);
		VertD.x += (dxTop * brYNorm);

	}

	void applyMirror(boolean inX) {

		if(inX) {
			// flip the x coords around the x axis
			float w = getImageWidth();
			VertA.x = w - VertA.x;
			VertB.x = w - VertB.x;
			VertC.x = w - VertC.x;
			VertD.x = w - VertD.x;
		} else {
			// flip the y coords around the Y axis
			float h = getImageHeight();
			VertA.y = h - VertA.y;
			VertB.y = h - VertB.y;
			VertC.y = h - VertC.y;
			VertD.y = h - VertD.y;
		}


	}

	float getImageWidth() {
		return theSprite.getImageWidth();
	}

	float getImageHeight() {
		return theSprite.getImageHeight();
	}

	PVector getCentre() {
		return new PVector(getImageWidth()/2f, getImageHeight()/2f);
	}


	////////////////////////////////////
	// debugging functions
	
	public void debugDrawQuadCorners(PVector pastePoint, RenderTarget rt) {
	debugDrawQuadPoint( pastePoint, 0, 0, Color.RED, rt);
	debugDrawQuadPoint( pastePoint, 1, 0, Color.GREEN, rt);
	debugDrawQuadPoint( pastePoint, 0, 1, Color.BLUE,rt);
	debugDrawQuadPoint( pastePoint, 1, 1, Color.BLACK, rt);
	}
	
	
	public void debugDrawQuadPoint(PVector pastePoint, float nx, float ny, Color c, RenderTarget rt) {
	PVector docSpacePoint = getQuadDocumentPoint( pastePoint, nx, ny);
	rt.drawPoint(docSpacePoint, c, 5);
	}
	
	
	public String debugQuadPointsToStr() {
	
	return "topLeft " + VertA.toStr() + ", topRight " + VertB.toStr() + "bottomLeft " + VertC.toStr() + ", bottomRight " + VertD.toStr();
	}



}












