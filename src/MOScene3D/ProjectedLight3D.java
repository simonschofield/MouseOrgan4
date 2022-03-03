package MOScene3D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.ImageProcessing;
import MOMaths.Fustrum3D;
import MOMaths.Intersection3D;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Plane3D;
import MOMaths.Range;
import MOMaths.Ray3D;
import MOUtils.GlobalSettings;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Maps 3D points in space onto a "texture plane" using parallel rays (on the planes surface normal)
// This produces a UV value within the texture plane, whihc is then used to map into the texture image
//
// Also builds a texture image from an existing texture (from the 3D scene). This then becomes a "light" from an angle (the normal of the plane)
// so the user can interactively "paint" the resultant light from the texture, and then this becomes the light.
//
public class ProjectedLight3D {
	
	SceneData3D sceneData3D;
	BufferedImage textureImage;
	Plane3D texturePlane;
	
	
	Fustrum3D sceneFustrum;
	Extents2D extentsImageMap2D;
	
	public ProjectedLight3D(SceneData3D scene3d,  Plane3D imgPlane){
		sceneData3D = scene3d;
		texturePlane = imgPlane.copy();
		
	}
	
	
	public float getValue01(PVector docPt, float normalizedDepth) {
		
		normalizedDepth = MOMaths.constrain(normalizedDepth, 0, 1);
		PVector p3d = sceneData3D.get3DVolumePoint(docPt, normalizedDepth);
		
		//System.out.println("docPt " + docPt.toStr() + " depth " + normalizedDepth + " p3d " + p3d.toStr());
		
		return getValue01(p3d);
		
		
	}
	
	public float getValue01(PVector p3d) {
		
		
		PVector uv = point3DToPlaneUV(p3d);
		PVector bufferLoc = UVToTextureImageBufferLoc(uv);
		return ImageProcessing.getValue01Clamped(textureImage, (int) bufferLoc.x, (int) bufferLoc.y);
		
		
	}
	
	public PVector getLightDirection() {
		return texturePlane.surfaceNormal;
	}
	
	// maps a point in the plane to a UV coordinate. Works with points not on the plave by projecting them (along the plane surfaceNormal)
	// onto the plane
	PVector point3DToPlaneUV(PVector p) {
		
		
		PVector n = texturePlane.surfaceNormal; 
		
		// find some other vector, not normal to the plane
		PVector V0 = new PVector(0, 0, 1);
		if(n.equals(V0)) {
			V0 = new PVector(1, 0, 0);
		}
		
		// finds a vector e1 in the plane
		PVector e1 = n.cross(V0);
		e1.normalize();
		
		// find a vector orthogonal to e1 in the plane
		PVector e2 = n.cross(e1);
		e2.normalize();

		float u = e1.dot(p);
		float v = e2.dot(p);
		return new PVector(u,v);
		
	}
	
	
	public void buildMapFromSceneImage(BufferedImage sceneImage) {
		// This builds a map the same size as the range in x and y

		sceneFustrum = sceneData3D.getViewFustrum();
		extentsImageMap2D = new Extents2D();
		for(int n = 0; n < 8; n++) {
			PVector thisFustrumVertex = sceneFustrum.getViaIndex(n);
			PVector pointOnPlane = point3DToPlaneUV(thisFustrumVertex);
			extentsImageMap2D.addPoint(pointOnPlane);
		}
		
		PVector lowerPt = extentsImageMap2D.getMinXY();
		PVector upperPt = extentsImageMap2D.getMaxXY();
		//System.out.println("Fustrum extents on the plane are " + lowerPt.toStr() + " to " + upperPt.toStr());
		
		int lightingTextureWidth = (int)(extentsImageMap2D.getWidth()+1);
		int lightingTextureHeight = (int)(extentsImageMap2D.getHeight()+1);
		textureImage = new BufferedImage(lightingTextureWidth,lightingTextureHeight, BufferedImage.TYPE_INT_ARGB);
		// now you have the extents in bufferspace of all possible projected 3D points within the scene
		
		
		int texImgW = textureImage.getWidth();
		int texImgH = textureImage.getHeight();
		// Now visit every point on the scene image
		Range depthExtents = new Range();
		depthExtents.initialiseForExtremaSearch();
		for(int y = 0; y < sceneImage.getHeight(); y++) {
			//System.out.println("done line " + y);
			for(int x = 0; x < sceneImage.getWidth(); x++) {
				
				// get the colour of the sceneImage
				int packedCol = sceneImage.getRGB(x, y);
				
				// get the docPoint from the normalised point in the sceneImage
				float normXinSceneImage = x/(float)sceneImage.getWidth();
				float normYinSceneImage = y/(float)sceneImage.getHeight();
				PVector pNorminSceneImage = new PVector(normXinSceneImage, normYinSceneImage);
				PVector docPt = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(pNorminSceneImage);
				
				
				if( sceneData3D.isSubstance(docPt) == false) continue;
				
				// get the nomralised depth
				float ndepth = sceneData3D.getDepthNormalised(docPt);

				// get the 3D point
				PVector p3d = sceneData3D.get3DVolumePoint(docPt, ndepth);
				
				// get the UV point on the texture plane
				PVector uv = point3DToPlaneUV(p3d);
				
				// find the texture buffer point and write the colour here
				PVector bufferLoc = UVToTextureImageBufferLoc(uv);
				//System.out.println("ndepth " + ndepth + " p3d " + p3d.toStr() + "map to x y " + mapBufferPointX + "," + mapBufferPointY);
				textureImage.setRGB((int) bufferLoc.x, (int) bufferLoc.y,packedCol);
				
				
			}
			
			
			
		}
		
		// debug - draw the fustrum points on the texture image
		for(int n = 0; n < 8; n++) {
			PVector thisFustrumVertex = sceneFustrum.getViaIndex(n);
			PVector uv = point3DToPlaneUV(thisFustrumVertex);
			PVector bufferLoc = UVToTextureImageBufferLoc(uv);
			//System.out.println("normPoint " + normPoint + "map to x y " + mapBufferPointX + "," + mapBufferPointY);
			int red = ImageProcessing.packARGB(255, 255, 0, 0); 
			textureImage.setRGB((int) bufferLoc.x, (int) bufferLoc.y,red);
		}
		
		
		
		ImageProcessing.saveImage(GlobalSettings.getUserSessionPath() + "texturetest.png", textureImage);
	}
	
	
	PVector UVToTextureImageBufferLoc(PVector uv) {
		
		PVector norm = extentsImageMap2D.normClamped(uv.x, uv.y);
		int texBufferPointX = (int)(norm.x * (textureImage.getWidth()-1));
		
		int texBufferPointY = (int)(norm.y * (textureImage.getHeight()-1));
		
		texBufferPointX = MOMaths.constrain(texBufferPointX, 0, textureImage.getWidth()-1);
		texBufferPointY = MOMaths.constrain(texBufferPointY, 0, textureImage.getHeight()-1);
		
		return new PVector(texBufferPointX, texBufferPointY);
	}
	
	
}// end ProjectedLight class



///////////////////////////////////////////////////////////////////////
// given a set of points, return extents data
//
class Extents2D{
	ArrayList<PVector> points = new ArrayList<PVector>();
	
	private Range extentsX;
	private Range extentsY;
	
	
	void addPoint(PVector p) {
		points.add(p);
		
	}
	
	private void calcExtents() {
		extentsX = new Range();
		extentsX.initialiseForExtremaSearch();
		
		extentsY = new Range();
		extentsY.initialiseForExtremaSearch();
		
		for(PVector p : points) {
			extentsX.addExtremaCandidate(p.x);
			extentsY.addExtremaCandidate(p.y);
		}
		
		
		
	}
	
	int getNumPoints() {
		return points.size();
	
	}
	
	PVector getPoint(int n) {
		return points.get(n);
		
	}
	
	Range getExtentsX() {
		calcExtents();
		return extentsX;
		
	}
	
	Range getExtentsY() {
		calcExtents();
		return extentsY;
		
	}
	
	PVector getMinXY() {
		calcExtents();
		return new PVector(extentsX.getLower(), extentsY.getLower());
	}


	PVector getMaxXY() {
		calcExtents();
		return new PVector(extentsX.getUpper(), extentsY.getUpper());
	}
	
	PVector lerp(float inx, float iny) {
		// where inx and iny should be in the range 0..1
		PVector out = new PVector();
		out.x = extentsX.lerp(inx);
		out.y = extentsY.lerp(iny);
		return out;
	}
	
	
	PVector norm(float inx, float iny){
		// where inx and iny should be in the range minX..maxX, minY..maxY
		PVector out = new PVector();
		out.x = extentsX.norm(inx);
		out.y = extentsY.norm(iny);
		return out;
	}
	
	
	PVector normClamped(float inx, float iny){
		// where inx and iny should be in the range minX..maxX, minY..maxY
		PVector out = norm( inx,  iny);
		out.x = MOMaths.constrain(out.x,0,1);
		out.y = MOMaths.constrain(out.y,0,1);
		return out;
	}
	
	float getWidth() {
		return extentsX.getDifference();
	}
	
	float getHeight() {
		return extentsY.getDifference();
	}
	
	
}

