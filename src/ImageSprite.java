import java.awt.image.BufferedImage;
//////////////////////////////////////////////////////////////////////
// 

// Sprite
// A sprite is a class which wraps up a Seed, and combines with 
//
//
//



public class ImageSprite{
	
	Seed seed;
	
	BufferedImage image;
	
	PVector origin = new PVector(0.5f,0.5f);
	float sizeInScene = 1; // 
	
	
	int bufferWidth; 
	int bufferHeight;
	float aspect;
	QRandomStream qRandomStream;
	
	SceneData3D sceneData;
	
	ImageSprite(){
		
		
	}
	
	ImageSprite(Seed s, BufferedImage im, PVector orig) {
		
		
		init( s,  im,  orig, 1);
	}


	ImageSprite(Seed s, BufferedImage im, PVector orig, float sizeInScn) {
		
		init( s,  im,  orig, sizeInScn);
	}
	
	void setSceneData3D(SceneData3D sd3d) {
		sceneData = sd3d;
	}
	
	
	void init(Seed s, BufferedImage im, PVector orig, float sizeInScn) {
		seed = s;
		
		image = im;
		bufferWidth = image.getWidth();
	    bufferHeight = image.getHeight();
	    aspect = bufferWidth/(float)bufferHeight;
	    origin = orig.copy();
	    sizeInScene = sizeInScn;
	    qRandomStream = new QRandomStream(s.id);
	}
	
	
	PVector getOriginBufferCoords() {
	    return new PVector( (origin.x * bufferWidth), (origin.y* bufferHeight) );
	}
	
	
	QRandomStream getRandomStream() {
		return qRandomStream;
	}
	
	Rect getPasteRect(SceneData3D sceneData) {
		// returns a document space rect to paste this sprite into
		// using the sizeInWorld as a 3d calculation
		
		// tbd -= a 2d version of this
		
		PVector docPt = seed.docPoint;
		
		float unit3DatDocPt = sceneData.get3DScale(docPt);
		float heightDocSpace = sizeInScene*unit3DatDocPt;
		float widthDocSpace = heightDocSpace * aspect;
		
		float x1 = docPt.x - (widthDocSpace* origin.x);
		float y1 = docPt.y - (heightDocSpace * origin.y);
		float x2 = docPt.x + (widthDocSpace* (1.0f-origin.x));
		float y2 = docPt.y + (heightDocSpace* (1.0f-origin.y));
		
		
		return new Rect(new PVector(x1,y1), new PVector(x2,y2) );
	}
	
	
	void scaleToSizeInScene(SceneData3D sceneData, RenderTarget renderTarget, float scaleModifier) {
		// scales the ImageBuffer image to the correct size for the document paste
		PVector docPt = seed.docPoint;
		float unit3DatDocPt = sceneData.get3DScale(docPt);
		float heightDocSpace = sizeInScene*unit3DatDocPt;
		
		float y1 = docPt.y - (heightDocSpace * origin.y);
		float y2 = docPt.y + (heightDocSpace* (1.0f-origin.y));
		
		PVector docPt1 = new PVector(docPt.x, y1);
		PVector docPt2 = new PVector(docPt.x, y2);
		
		PVector bufferPt1 = renderTarget.docSpaceToBufferSpace(docPt1);
		PVector bufferPt2 = renderTarget.docSpaceToBufferSpace(docPt2);
		float heightInActualPixels = (float)Math.abs(bufferPt1.y - bufferPt2.y);
		
		float scale = (heightInActualPixels/bufferHeight) * scaleModifier;
		if(scale > 1) {
		 System.out.println(seed.contentItemDescriptor.contentGroupName + " overscaled, original size in pixels " + bufferHeight + " to be scale to " + heightInActualPixels + " scale " + scale);
		}
		
		
		scale(scale,scale);
		//System.out.println("target size in pixels " + heightInActualPixels + " scale " + scale + " resultant height " + bufferHeight);
	}
	
	void scale(float scaleW, float scaleH) {
		image = ImageProcessing.scaleImage(image, scaleW, scaleH);
		bufferWidth = image.getWidth();
		bufferHeight = image.getHeight();
	}
	
	void rotate(float degrees) {
		// this function has the visual effect of rotating the image of the sprite around the sprite origin
		// It does so by rotating the image and the origin point  around it's centre (as normal) 
		// so that, when the shape is pasted it is as if it has been rotated around the origin
		// Much more efficient than otherwise
		
	    double toRad = Math.toRadians(degrees);
	    // rotate rotation point around (0,0) in image-pixel space
	 	float  rx = bufferWidth * (origin.x-0.5f);
	 	float  ry = bufferHeight * (origin.y-0.5f);
	 		
		image = ImageProcessing.rotateImage(image,  degrees);

		float newX = (float)(rx * Math.cos(toRad) - ry * Math.sin(toRad));
	    float newY = (float)(ry * Math.cos(toRad) + rx * Math.sin(toRad));
	    
	    //shift rotation point back into parametric space of new image size
	    bufferWidth = image.getWidth();
	    bufferHeight = image.getHeight();
	    
	    origin.x = (newX/bufferWidth) + 0.5f;
	    origin.y = (newY/bufferHeight) + 0.5f;
	    
	   
	}
	
	
	void scaleRotateSprite(float scaleX, float scaleY, float degrees) {
		// this function has the visual effect of rotating the image of the sprite around the sprite origin
		// It does so by rotating the image and the origin point  around it's centre (as normal) 
		// so that, when the shape is pasted it is as if it has been rotated around the origin
		// Much more efficient than otherwise
		
	    double toRad = Math.toRadians(degrees);
	    
	    // rotate rotation point around (0,0) in image-pixel space
	 	float  rx = bufferWidth * (origin.x-0.5f) * scaleX;
	 	float  ry = bufferHeight * (origin.y-0.5f) * scaleY;

		image = ImageProcessing.scaleRotateImage(image,  scaleX, scaleY, degrees);

		float newX = (float)(rx * Math.cos(toRad) - ry * Math.sin(toRad));
	    float newY = (float)(ry * Math.cos(toRad) + rx * Math.sin(toRad));
	    
	    //shift rotation point back into parametric space of new image size
	    bufferWidth = image.getWidth();
	    bufferHeight = image.getHeight();
	    
	    origin.x = (newX/bufferWidth) + 0.5f;
	    origin.y = (newY/bufferHeight) + 0.5f;
	}
	
	
	
}


