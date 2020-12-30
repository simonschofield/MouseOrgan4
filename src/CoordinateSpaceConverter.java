///////////////////////////////////////////////////////////////////////////////////
//Given a document of aspect mouseOrganAspect
//and a TargetBuffer buffer of width, height
//produce the following coordinate transforms
// 
public class CoordinateSpaceConverter{
	
	int targetWidth;
	int targetHeight;
	float mouseOrganDocAspect;

	public CoordinateSpaceConverter(int renderWidth, int renderHeight,float mouseOrganDocAsp) {
		targetWidth = renderWidth;
		targetHeight = renderHeight;
		
		mouseOrganDocAspect = mouseOrganDocAsp;
	}
	
	

	// for any point in the mouse organ doc space
	// return the image coordinate of the render buffer
	public PVector docSpaceToImageCoord(PVector docSpace) {
		PVector normSpace = docSpaceToNormalizedSpace(docSpace);
		return normalizedSpaceToImageCoord(normSpace);
	}


	// for any image coordinate of the render buffer
	// return the document mouse organ document space. Some will be out of the range 0..1
	
	public PVector renderImageCoordToDocSpace(float x, float y) {

		float dx = MOMaths.norm(x, 0, targetWidth);
		float dy = MOMaths.norm(y, 0, targetHeight);
		if(mouseOrganDocAspect > 1f ) {
			dy /= mouseOrganDocAspect;
		}else {
			dx *= mouseOrganDocAspect;
		}

		return new PVector(dx,dy);
	}

	PVector docSpaceToNormalizedSpace(PVector docSpace) {

		float x = 0;
		float y = 0;
		if(mouseOrganDocAspect > 1f ) {
			x = docSpace.x;
			y = docSpace.y*mouseOrganDocAspect;
		}else {
			x = docSpace.x/mouseOrganDocAspect;
			y = docSpace.y;
		}
		return new PVector(x,y);
	}
	
	PVector normalizedSpaceToDocSpace(PVector normalSpace) {

		float x = 0;
		float y = 0;
		if(mouseOrganDocAspect > 1f ) {
			x = normalSpace.x;
			y = normalSpace.y/mouseOrganDocAspect;
		}else {
			x = normalSpace.x*mouseOrganDocAspect;
			y = normalSpace.y;
		}
		return new PVector(x,y);
	}


	private PVector normalizedSpaceToImageCoord(PVector normSpace) {
		float x = normSpace.x * targetWidth;
		float y = normSpace.y * targetHeight;
		x = MOMaths.constrain(x, 0, targetWidth-1);
		y = MOMaths.constrain(y, 0, targetHeight-1);
		return new PVector(x,y);
	}

	

}
