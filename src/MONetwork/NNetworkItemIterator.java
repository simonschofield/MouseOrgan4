package MONetwork;
import java.util.ArrayList;

public class NNetworkItemIterator{
	
	ArrayList<NPoint> pointList;
	ArrayList<NEdge> edgeList;
	ArrayList<NRegion> regionList;
	NRegion currentRegion;
	int pointCounter = 0;
	int edgeCounter = 0;
	int regionCounter = 0;
	
	void setPoints(ArrayList<NPoint> listIn){
		pointList = listIn;
		pointCounter = 0;
	}
	
	void setEdges(ArrayList<NEdge> listIn){
		edgeList = listIn;
		edgeCounter = 0;
	}
	
	public void setRegions(ArrayList<NRegion> listIn){
		regionList = listIn;
		regionCounter = 0;
	}
	
	int getNumPoints() {
		return pointList.size();
	}
	
	int getNumEdges() {
		return pointList.size();
	}
	
	int getNumRegions() {
		return pointList.size();
	}
	
	void setPointsCounter(int n) {
		pointCounter = n;
	}
	
	void setEdgesCounter(int n) {
		edgeCounter = n;
	}
	
	void setRegionsCounter(int n) {
		regionCounter = n;
	}
	
	int getPointsCounter() {
		return pointCounter;
	}
	
	int getEdgesCounter() {
		return edgeCounter;
	}
	
	int getRegionsCounter() {
		return regionCounter;
	}
	
	NPoint getNextPoint(){
		if(pointCounter >= pointList.size()) return null;
		return pointList.get(pointCounter++);
	}
	
	NEdge getNextEdge(){
		if(edgeCounter >= edgeList.size()) return null;
		return edgeList.get(edgeCounter++);
	}
	
	NRegion getNextRegion(){
		if(regionCounter >= regionList.size()) return null;
		
		NRegion r = regionList.get(regionCounter);
		regionCounter+=1;
		currentRegion = r;
		return r;
	}
	
	NRegion getCurrentRegion() {
		return currentRegion;
	}
	
}
