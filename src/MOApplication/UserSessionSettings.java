package MOApplication;

public class UserSessionSettings{
	boolean isDraft = true;
	public int fullScaleRenderWidth;
	public int fullScaleRenderHeight;
	float draftRenderScale = 0.2f;
	String currentSchemea = "";
	String userSessionPath = "";
	
	public UserSessionSettings(boolean isFinalCopy, float draftRendScale, String currentSchm){
		
		isDraft = !isFinalCopy;
		draftRenderScale = draftRendScale;
		currentSchemea = currentSchm;
	}
	
	public UserSessionSettings(int fullRenderW, int fullRenderH, boolean isFinalCopy, float draftRendScale, String currentSchm){
		fullScaleRenderWidth = fullRenderW;
		fullScaleRenderHeight = fullRenderH;
		isDraft = !isFinalCopy;
		draftRenderScale = draftRendScale;
		currentSchemea = currentSchm;
	}
	
	public float getRenderScale() {
		if(isDraft) return draftRenderScale;
		return 1.0f;
	}
	
	public int getRenderQuality() {
		if(isDraft) return 0;
		return 2;
	}
	
	public boolean isSchema(String s) {
		return currentSchemea.contentEquals(s);
	}
	
	public boolean schemaContains(String s) {
		return currentSchemea.contains(s);
	
	}
	
	public boolean isDraft() { return isDraft;}
	
	
	public float millimeterToDocspace(float mm) {
		// you give it a dimension in the printed version (e.g. 5mm)
		// and it returns the doc space dimension that is that size when printed at full size. Gives the same answer at all
		// draft resolutions.
		// Assumes print resolution is 300dpi, (or 11.811 pixels per mm)
		if(fullScaleRenderWidth > fullScaleRenderHeight) {
			// use width
			float numMMAcrossLongestEdgeOfImage = fullScaleRenderWidth/11.811f; // this is the number of MM (at full size res) that are equal to a doc space of 1,
			// Therefore 1/numMMAcrossLongestEdgeOfImage equals the doc space occupied by 1mm
			return mm/numMMAcrossLongestEdgeOfImage;
		}else {
			float numMMAcrossLongestEdgeOfImage = fullScaleRenderHeight/11.811f;
			return mm/numMMAcrossLongestEdgeOfImage;
		}
	}
	
}
