package MOPointGeneration;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

import MOCompositing.BufferedImageRenderTarget;
import MOImage.MOColor;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.RandomStream;
import MOScene3D.AABox3D;
import MOScene3D.EyeSpaceVolume3D;
import MOUtils.CollectionIterator;
import MOUtils.GlobalSettings;

////////////////////////////////////////////////////////////////////////////
//Generates a set of randomly scattered points in 3D
//then preserves the visible ones as a depth-enhanced set of document points
//The depth is in actual eye-space units

public class PointGenerator_Volume3D extends CollectionIterator {

	RandomStream randomStream;

	// a list of 3d points for the 3d packing algorithm
	EyeSpaceVolume3D eyeSpaceVolume;
	AABox3D aaBox;

	ArrayList<PVector> points3d = new ArrayList<>();
	ArrayList<PVector> points2d = new ArrayList<>();
	float nearDepth = 0;
	float farDepth = 1;

	public PointGenerator_Volume3D(int rseed, float vfov) {
		randomStream = new RandomStream(rseed);
		eyeSpaceVolume = new EyeSpaceVolume3D(GlobalSettings.getTheDocumentCoordSystem().getDocumentAspect(),vfov);
	}

	void generateRandomPoints(int numPoints3D, float nearZ, float farZ) {
		nearDepth = nearZ;
		farDepth = farZ;
		aaBox = eyeSpaceVolume.getBoundingBox( nearDepth,  farDepth);
		PVector minXYZ = aaBox.getMin();
		PVector maxXYZ = aaBox.getMax();
		for(int n = 0; n < numPoints3D; n++) {
			float rx = randomStream.randRangeF(minXYZ.x, maxXYZ.x);
			float ry = randomStream.randRangeF(minXYZ.y, maxXYZ.y);
			float rz = randomStream.randRangeF(minXYZ.z, maxXYZ.z);


			PVector thisCandidatePoint = new PVector(rx,ry,rz);
			points3d.add(thisCandidatePoint);
			if(eyeSpaceVolume.isPoint3DInView(thisCandidatePoint)) {

				PVector docSpcPt = eyeSpaceVolume.getDocSpacePoint(thisCandidatePoint);
				points2d.add(docSpcPt);
			}
		}
		System.out.println(" generateRandomPoints made " + points2d.size() + " out of a possible " + numPoints3D);
		depthSort();
	}

	// In case you need to sort the depth of the points on the z component of the point
	// More used by subclasses
	void depthSort() {
		points2d.sort(Comparator.comparing(PVector::getZ).reversed());
	}





	ArrayList<PVector> getDocSpacePoints(){
		return points2d;
	}

	@Override
	public int getNumItems() {
		// TODO Auto-generated method stub
		return points2d.size();
	}

	@Override
	public Object getItem(int n) {
		// TODO Auto-generated method stub
		return points2d.get(n);
	}


	PVector getNextPoint() {
		return (PVector) super.getNextItem();
	}


	void drawPoints(Color c, BufferedImageRenderTarget rt) {
		rt.drawPoints(points2d, c, 3);
	}

	void drawPoints(Color nearCol, Color farCol, BufferedImageRenderTarget rt) {
		for(PVector p : points2d) {

			float n = MOMaths.norm(p.z, nearDepth, farDepth);

			//System.out.println("in draw points near depth " + nearDepth + " far " + farDepth + " p "+ p + " n "+ n + " " );
			Color thisCol = MOColor.blendColor(n, nearCol, farCol);
			rt.drawPoint(p, thisCol, 10);
		}
	}


}// end of PointGenerator_RadialPackVolume3D class

