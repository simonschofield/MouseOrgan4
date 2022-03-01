package MOMaths;

public class Plane3D {
	public PVector  pointOnPlane;
	public PVector surfaceNormal;
	
	
	public Plane3D(PVector snorm, PVector pointOnPln){
		pointOnPlane = pointOnPln.copy();
		surfaceNormal = snorm.copy();
	}
	
	public Plane3D copy() {
		return new Plane3D(this.surfaceNormal, this.pointOnPlane);
	}
	
}
