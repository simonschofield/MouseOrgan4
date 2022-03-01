package MOScene3D;
import java.awt.image.BufferedImage;

import MOImage.ImageProcessing;
import MOMaths.Intersection3D;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Plane3D;
import MOMaths.Ray3D;
import MOUtils.GlobalSettings;
// ProjectedLight3D uses an image mapped 3D plane to project the image values into 3D space.
// For every point in 3D, a ray (along the plane surface normal) is cast onto the plane and the image point determined
// The scale and shift in UV of the texture can be set. The texture is repeated in U and V.
public class ProjectedLight3D {
	
	SceneData3D sceneData3D;
	BufferedImage textureImage;
	Plane3D imagePlane;
	PVector lightPosition;
	float uvScale;
	float shiftU, shiftV;
	
	public ProjectedLight3D(SceneData3D scene3d, BufferedImage lightingImg, Plane3D imgPlane, float scale, float shftU, float shftV){
		sceneData3D = scene3d;
		textureImage = lightingImg;
		imagePlane = imgPlane.copy();
		uvScale = scale;
		shiftU = shftU;
		shiftV = shftV;
	}
	
	
	public float getValue01(PVector docPt, float normalizedDepth) {
		
		normalizedDepth = MOMaths.constrain(normalizedDepth, 0, 1);
		PVector p3d = sceneData3D.get3DVolumePoint(docPt, normalizedDepth);
		
		System.out.println("docPt " + docPt.toStr() + " depth " + normalizedDepth + " p3d " + p3d.toStr());
		
		return getValue01(p3d);
		
		
	}
	
	public float getValue01(PVector p3d) {
		
		
		PVector uv = pointOnPlaneToUV(p3d, uvScale, uvScale, shiftU, shiftV);
		
		int w = textureImage.getWidth();
		int h = textureImage.getHeight();
		int pixelLocationX = (int)(uv.x%w);
		int pixelLocationY = (int)(uv.y%h);
		System.out.println("U V " + uv.toStr() + "Pixel Loc" + pixelLocationX + " " + pixelLocationY);
		System.out.println();
		return ImageProcessing.getValue01Clamped(textureImage, pixelLocationX, pixelLocationY);
		
		
	}
	
	public PVector getLightDirection() {
		return imagePlane.surfaceNormal;
	}
	
	// maps a point in the plane to a UV coordinate. Works with points not on the plave by projecting them (alonge the plane surfaceNormal)
	// onto the plane
	PVector pointOnPlaneToUV(PVector p, float sclU, float sclV, float shftU, float shftV) {
		
		
		PVector n = imagePlane.surfaceNormal; 
		
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

		
		
		//float u = e1.dist(p);
		//float v = e2.dist(p);
		
		System.out.println(" docPt " + p.toStr() + " sn " + n.toStr() + " e1 " +  e1.toStr() + " e2 " +  e2.toStr());
		
		
		float u = Math.abs((e1.dot(p) * sclU) + shftU);
		float v = Math.abs((e2.dot(p) * sclV) + shftV);
		return new PVector(u,v);
		
	}
	
	
	
	
}
