/////////////////////////////////////////////////////////////////////////
// To help with establishing depth layers and triggering events (like save image and clear) when a depth-layer is fully rendered
//Depth is defined in normalised units 1..0, where 1 is farthest away.
//You specify a number of depth layers via a series of DECREASING depth values - do not include 1 or 0 in these
//as these values are added in by the class.
//During run-time, sample depths should be given to the class in DECREASING order. This enables a "trigger event" by polling isNextlayer(). 
//
public class DepthLayerManager {

	boolean isActive = true;
	
	float[] depthLayerDemarkers;
	boolean[] activeLayers;
	int numLayers = 1;

	int currentLayer = 0;

	// just for checking that depth is always decreasing
	float previousDepthSample = 1.1f;
	
	// these are in normalised depth, biggest depth to least depth
	public DepthLayerManager(float[] depthDemarkers) {
	
		// copy over the demarker array and insert 1 at the start and 0 at the end
		depthLayerDemarkers = new float[depthDemarkers.length + 2];
		depthLayerDemarkers[0] = 1f;
		for (int n = 0; n < depthDemarkers.length; n++) {
			depthLayerDemarkers[n + 1] = depthDemarkers[n];
		}
		depthLayerDemarkers[depthLayerDemarkers.length - 1] = 0f;
		numLayers = depthLayerDemarkers.length - 1; // i.e the gaps between the posts
	
		activeLayers = new boolean[numLayers];
		for (int n = 0; n < numLayers; n++) {
			activeLayers[n] = true;
		}
	
	}
	
	void setActive(boolean active) {
		isActive = active;
	}
	
	boolean isActive() {
		return isActive;
	}
	
	void setLayerActive(int layerNum, boolean isActive) {
		activeLayers[layerNum] = isActive;
	}
	
	boolean isLayerActive(int layerNum) {
		return activeLayers[layerNum];
	}
	
	void setStartLayer(float startDepth) {
		// to be called if you are "jumping" to a layer, rather than working through them all
		previousDepthSample = startDepth;
		currentLayer = findCurrentLayer(startDepth);
	}
	
	void setStartLayer(int layerNum) {
	// to be called if you are "jumping" to a layer, rather than working through them all
		currentLayer = layerNum;
	}
	
	boolean isNextLayer(float depthSample) {
		if (depthSample > previousDepthSample) {
			System.out.println("DepthLayerManager: isNextLayer - depth values are not sorted in decreasing order!");
			// equal depth values are allowed, as two equal depth values may conceivably occur in large sample numbers.
			return false;
		}
		previousDepthSample = depthSample;
	
		Range thisLayerExtents = getLayerExtents(currentLayer);
	
		if (depthSample < thisLayerExtents.getLower()) {
			currentLayer = findCurrentLayer(depthSample);
			return true;
		}
		return false;
	}
	
	int getCurrentLayerNum() {
		return currentLayer;
	}
	
	Range getLayerExtents(int layerNum) {
		float far = depthLayerDemarkers[layerNum];
		float near = depthLayerDemarkers[layerNum + 1];
		return new Range(near, far);
	
	}
	
	int findCurrentLayer(float depthSample) {
	// given a depth sample, return the layer that it is in
	
		for (int n = 0; n <= numLayers; n++) {
			Range thisLayerExtents = getLayerExtents(n);
			if (thisLayerExtents.isBetweenInc(depthSample)) {
				return n;
			}
		}
	
		System.out.println(
				"DepthLayerManager: findCurrentLayer - depth value " + depthSample + " cannot be found in layers!");
			return 0;
	}

}
