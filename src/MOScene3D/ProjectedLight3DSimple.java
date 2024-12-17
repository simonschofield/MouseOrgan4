package MOScene3D;




import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOImage.BilinearBufferedImageSampler;
import MOImage.ImageProcessing;
import MOImage.MOPackedColor;
import MOMaths.AABox3D;
import MOMaths.Fustrum3D;
import MOMaths.Intersection3D;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Plane3D;
import MOMaths.Range;
import MOMaths.Ray3D;
import MOSprite.Sprite;
import MOUtils.GlobalSettings;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Creates a lighting volume based on a bitmap projected from a plane in 3D. Any 3D point within the viewing fustrum can be mapped to this 
// image-plane and the lighting value found.
// So the angle of the light can be set
// Also builds a texture image from an existing texture (from the 3D scene). This then becomes a "light" from an angle (the normal of the plane)
// so the user can interactively "paint" the resultant light from the texture, and then this becomes the light.
//
//
// Usage as follows
//
//	Plane3D plane3d = new Plane3D(new PVector(0,1,0), new PVector(0,0,0));
//	BufferedImage lightImage = ImageProcessing.loadImage(GlobalSettings.getSampleLibPath() + "textures//noise based textures//texturemap_patches.png");
//	ProjectedLight3DSimple projectedLight3DSimple = new ProjectedLight3DSimple(sceneData3D,  plane3d,   lightImage);
//
//
//	projectedLight3DSimple.setTextureScale(0.01f);
//	projectedLight3DSimple.setUVOffset(1000, 1000);
//	projectedLight3DSimple.testLighting();
//// Then check out testLighting method
//
public class ProjectedLight3DSimple {
	/*
	OLD_GeometryBuffer3D geometryBuffer3D;
	BufferedImage textureImage;
	BilinearBufferedImageSampler bilinearSampler;
	Plane3D texturePlane;
	float textureScale = 1f;

	//Fustrum3D sceneFustrum;
	//Extents2D extentsImageMap2D;
	float offsetU, offsetV;

	public Range uExtrema, vExtrema;

	public ProjectedLight3DSimple(OLD_GeometryBuffer3D scene3d,  Plane3D imgPlane,  BufferedImage lightImage){
		geometryBuffer3D = scene3d;
		texturePlane = imgPlane.copy();
		textureImage = lightImage;

		uExtrema = new Range();
		uExtrema.initialiseForExtremaSearch();

		vExtrema = new Range();
		vExtrema.initialiseForExtremaSearch();

		bilinearSampler = new BilinearBufferedImageSampler(lightImage);
	}

	public void setTextureScale(float s) {
		textureScale = s;

	}
	
	
	

	public void setUVOffset(float du, float dv) {
		// so we can shift the texture away from the match-boxing centre
		offsetU = du;
		offsetV = dv;

	}
	
	public void setPlaneNormal(float x, float y, float z) {
		texturePlane.surfaceNormal = new PVector(x,y,z);
	}

	// use this method for volume lighting
	public float getValue01(PVector docPt, float realDepth) {
		PVector p3d = geometryBuffer3D.docSpaceToWorld3D(docPt, realDepth);
		return getValue01(p3d);
	}

	// use this method for surface lighting
	public float getValue01(PVector p3d) {

		PVector uv = point3DToPlaneUV(p3d);
		uExtrema.addExtremaCandidate(uv.x);
		vExtrema.addExtremaCandidate(uv.y);

		


		PVector bufferLoc = UVToTextureImageBufferLoc(uv);

		//System.out.println("p3 " + p3d.toStr() + " uv " + uv.toStr() + " bufferLoc " + bufferLoc.toStr());

		//return bilinearSampler.getPixelBilin01(bufferLoc.x,bufferLoc.y);

		return bilinearSampler.getPixelNearest01(bufferLoc.x,bufferLoc.y);
	}


    

	private PVector UVToTextureImageBufferLoc(PVector uv) {
		
		uv.x += offsetU;
		uv.y += offsetV;

		float texBufferPointX = Math.abs(uv.x * (textureImage.getWidth()) * textureScale);

		float texBufferPointY = Math.abs(uv.y * (textureImage.getHeight()) * textureScale);

		float bufferX =  texBufferPointX % (textureImage.getWidth());
		float bufferY =  texBufferPointY % (textureImage.getHeight());


		return new PVector(bufferX, bufferY);
	}


	public PVector getLightDirection() {
		return texturePlane.surfaceNormal;
	}






	// maps a point in the plane to a UV coordinate. Works with points not on the plane by projecting them (along the plane surfaceNormal)
	// onto the plane
	PVector point3DToPlaneUV(PVector p) {


		PVector n = texturePlane.surfaceNormal; 

		// find some other vector, not normal to the plane
		PVector V0 = new PVector(1, 0, 0);
		if(n.equals(V0)) {
			V0 = new PVector(0, 0, 1);
		}

		// finds a vector e1 in the plane
		PVector e1 = n.cross(V0);
		e1.normalize();

		// find a vector orthogonal to e1 in the plane
		PVector e2 = n.cross(e1);
		e2.normalize();

		PVector zero = new PVector(0,0,0);
		
		float u = e1.dot(p);
		float v = e2.dot(p);
		return new PVector(u,v);

	}


	public void testLighting() {
		// Displays the lighting projeted onto the current sceneData3D based on the current settings
		int maxY = GlobalSettings.getTheDocumentCoordSystem().getBufferHeight();
		int maxX = GlobalSettings.getTheDocumentCoordSystem().getBufferWidth();

		for(int y = 0; y < maxY; y++) {
			for(int x = 0; x < maxX; x++) {

				PVector docSpace = GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace(x, y);
				PVector pSurface3d = geometryBuffer3D.docSpaceToWorld3D(docSpace);
				PVector basicOffset = new PVector(100,100,0);
				pSurface3d.add(basicOffset);
				float lv = this.getValue01(pSurface3d);


				Color c = new Color(lv,lv,lv);

				GlobalSettings.getDocument().getMain().setPixel(x, y, c);

				
			}
			if( y%100==0) GlobalSettings.getTheApplicationSurface().forceRefreshDisplay();
		}


	}

*/
}// end ProjectedLight class


