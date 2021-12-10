package MOMaths;

public class Line3D{
	
	PVector p1 = new PVector();
	PVector p2 = new PVector();
	
	public Line3D(PVector p1, PVector p2){
		this.p1 = p1;
		this.p2 = p2;
	}
	
	
    public PVector lerp(float p) {
    	return PVector.lerp(p1, p2, p);
    }
    
    public float normZ(float z) {
    	return MOMaths.norm(z,  p1.z,  p2.z);
    }
	
}


