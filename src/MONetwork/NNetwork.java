package MONetwork;
import java.io.FileWriter;
import java.util.ArrayList;


import MOMaths.PVector;
import MOMaths.Rect;
import MOUtils.KeyValuePairList;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.UniqueID;

import java.io.BufferedReader;
import java.io.FileReader;
//////////////////////////////////////////////////////////////////////////////////////
//
// This defines a bare-bones data structure for a Non-directed cyclical Graph
// There are three elements: NPoints, NEdges and NRegions
// An NPoint links NEdges, and may have many NEdges connected to it.
// An NEdge has two NPoints that make it. These have no order (no "end" and "start", just two points)
// An NRegion is (??) made of a number of NEdges that connect with shared NPoints
///////////////////////////////////////////////////////////////////////////////////////







//////////////////////////////////////////////////////////////////////////////////////
// NNetwork
//
//
public class NNetwork {

	ArrayList<NPoint> points = new ArrayList<NPoint>();
	ArrayList<NEdge> edges = new ArrayList<NEdge>();
	ArrayList<NRegion> regions = new ArrayList<NRegion>();
	//SpatialIndex pointsSpatialIndex;

	UniqueID uniqueIDGenerator;

	KeyValuePairList currentSearchAttributes = new KeyValuePairList();


	public NNetwork() {
		//GlobalPointers.theNNetwork = this;
		//setSpatialIndex( 1, 1, 10, 10);
		uniqueIDGenerator = new UniqueID();
	}

	boolean isInitialised() {
		if (points.size() > 0) return true;
		return false;
	}

	UniqueID getUniqueIDGenerator() {
		return uniqueIDGenerator;
	}

	int getUniqueID() {
		return uniqueIDGenerator.getUniqueID();
	}

	//////////////////////////////////////////////////////////////////////////
	// experimental: this is destructive to the other network
	// so is best used when the other network is very temporary, e.g. load from file and the dispose
	public void mergeNetwork(NNetwork other){
		points.addAll(other.points);
		edges.addAll(other.edges);
		regions.addAll(other.regions);
		for (NPoint np : other.points) {
			np.theNetwork = this;
		}
		uniqueIDGenerator.reset();
		for (NEdge ne : other.edges) {
			ne.theNetwork = this;
		}
		uniqueIDGenerator.reset();
		for (NRegion nr : other.regions) {
			nr.theNetwork = this;
		}
		refreshIDs();
	}

	void clearNetwork() {
		points = new ArrayList<NPoint>();
		edges = new ArrayList<NEdge>();
		uniqueIDGenerator.setMinNewID(0);
	}

	void refreshIDs() {
		uniqueIDGenerator.reset();
		for (NPoint np : points) {
			np.setID();
		}
		uniqueIDGenerator.reset();
		for (NEdge ne : edges) {
			ne.setID();
		}
		uniqueIDGenerator.reset();
		for (NRegion nr : regions) {
			nr.setID();
		}
	}

	public void save(String pathandfilename) {
		// there should be a directory in the project folder called seeds
		refreshIDs();
		System.out.println("saving " + pathandfilename);

		FileWriter csvWriter = null;
		try {
			csvWriter = new FileWriter(pathandfilename);

			// saves the points first
			for (NPoint p : points) {
				csvWriter.append(p.getAsCSVLine());
			}
			// then the edges
			for (NEdge e : edges) {
				csvWriter.append(e.getAsCSVLine());
			}
			// then the regions
			for (NRegion r : regions) {
				csvWriter.append(r.getAsCSVLine());
			}


			csvWriter.flush();
			csvWriter.close();
		}// end try
		catch(Exception ex) {
			System.out.println("csv writer failed");
		}
	}

	public void load(String pathandfilename) {
		// there should be a directory in the project folder called seeds
		try {
			BufferedReader csvReader = new BufferedReader(new FileReader(pathandfilename));
			String csvLine;
			clearNetwork();
			while ((csvLine = csvReader.readLine()) != null) {

				// do something with the data

				KeyValuePairList kvp = new KeyValuePairList();
				kvp.ingestCSVLine(csvLine);

				addItemFromCSV_KVP(kvp);
			}
			csvReader.close();
		} 
		catch(Exception e) {
			System.out.println("csv reader failed");
		}
	}


	//////////////////////////////////////////////////////////////////////////////////
	// attribute matching
	// currently only uses a single attribute, so cannot do complex checks (like if contains K:ROAD and V: A OR B)
	// the attributes e contains a KVP list, so if currentSearchAttribute was also a list

	void setSearchAttribute(KeyValuePair kvp) {
		if(kvp == null) return;
		currentSearchAttributes.addKeyValuePair(kvp.copy());
		System.out.println("NNetworkProcessor: added to currentSearchAttribute = " + kvp.getKey());
	}

	void setSearchAttribute(KeyValuePairList kvpl) {
		if(kvpl == null) return;
		currentSearchAttributes=kvpl.copy();
		//System.out.println("NNetworkProcessor: added to currentSearchAttribute = " + kvp.theKey);
	}

	void clearSearchAttributes() {
		currentSearchAttributes.removeAll();
	}

	boolean isMatchingSearchAttribute(NAttributes e) {
		if(currentSearchAttributes==null) return true;
		boolean result = e.getAttributes().containsEqual(currentSearchAttributes);
		return result;
	}

	ArrayList<NEdge> getEdgesMatchingSearchAttributes(){

		ArrayList<NEdge> edges = this.getEdges();
		ArrayList<NEdge> matchingEdges = new ArrayList<NEdge>();
		for (int n = 0; n < edges.size(); n++) {
			NEdge e = edges.get(n);
			if(isMatchingSearchAttribute(e) ) matchingEdges.add(e);
		}
		return matchingEdges;
	}


	ArrayList<NPoint> getPointsMatchingSearchAttributes(){

		ArrayList<NPoint> points = this.getPoints();
		ArrayList<NPoint> matchingPoints = new ArrayList<NPoint>();
		for (int n = 0; n < points.size(); n++) {
			NPoint p = points.get(n);
			if(isMatchingSearchAttribute(p) ) matchingPoints.add(p);
		}
		return matchingPoints;
	}


	ArrayList<NRegion> getRegionsMatchingSearchAttributes(){

		ArrayList<NRegion> regions = this.getRegions();
		ArrayList<NRegion> matchingRegions = new ArrayList<NRegion>();
		for (int n = 0; n < regions.size(); n++) {
			NRegion r = regions.get(n);
			if(isMatchingSearchAttribute(r) ) matchingRegions.add(r);
		}
		return matchingRegions;
	}




	//////////////////////////////////////////////////////////////////////////////////
	// applying a ROI to the network
	// 
	// 



	public void applyROI(Rect roi) {
		// remove edgs outside of roi
		ArrayList<NEdge> toRemove  = new ArrayList<NEdge>();
		for(NEdge ne: edges) {
			PVector p1 = ne.p1.coordinates;
			PVector p2 = ne.p2.coordinates;
			if(roi.canLineIntersect(p1,p2) == false) {
				toRemove.add(ne);
			}

		}

		removeEdges(toRemove);


		for (NPoint np : points) {
			PVector p = np.getPt();
			PVector nomPoint = roi.norm(p);
			PVector docSpcPt = GlobalSettings.getTheDocumentCoordSystem().normalisedSpaceToDocSpace(nomPoint);
			np.setPt(docSpcPt);
		}


	}

	public void removeEdges(ArrayList<NEdge> toRemove) {
		for(NEdge ne: toRemove) {
			deleteEdge(ne);
		}
	}



	private void addItemFromCSV_KVP(KeyValuePairList kvpl) {
		if (kvpl.getString("THING").equals("NPOINT")) {
			NPoint np = new NPoint( kvpl, this);
			addPoint(np);
		}

		if (kvpl.getString("THING").equals("NEDGE")) {
			NEdge ne = new NEdge( kvpl, this );
			edges.add(ne);
		}

		if (kvpl.getString("THING").equals("NREGION")) {
			NRegion nr = new NRegion( kvpl, this );
			if (nr.isValid()) regions.add(nr);
		}
	}

	// points must only be added via this method
	void addPoint(NPoint np) {
		points.add(np);
		//pointsSpatialIndex.addItem(np);
	}

	NPoint addPoint(PVector mapSpacePt) {
		NPoint np = new NPoint(mapSpacePt, this);
		addPoint(np);
		return np;
	}



	NEdge addEdge(NPoint p1, NPoint p2) {
		// the points must exist within the newtwork before this can be called
		if (p1 == p2) {
			System.out.println("NNetwork:addEdge - both points idetical - cannot add");
			return null;
		}
		NEdge ne = new NEdge(p1, p2, this);
		edges.add(ne);
		return ne;
	}


	void deletePoint(NPoint np) {

		int numEdgeRefs = np.connectedEdges.size();
		for (int n = numEdgeRefs-1; n >= 0; n--) {
			NEdge edgeref = np.connectedEdges.get(n);
			//System.out.println("connected edge of np " + np.getID() + " edge " + edgeref.toStr() );
			deleteEdge(edgeref);
		}
		//pointsSpatialIndex.removeItem(np);
		points.remove(np);
	}


	void deleteEdge(NEdge e) {
		NPoint p1 = e.p1;
		NPoint p2 = e.p2;
		p1.removeEdgeReference(e);
		p2.removeEdgeReference(e);
		removeDependentRegions(e);
		edges.remove(e);
	}


	void removeDependentRegions(NEdge e){
		int numRegions = regions.size();
		for (int n = numRegions-1; n >= 0; n--) {
			NRegion r = regions.get(n);
			if( r.containsEdge(e) ) regions.remove(n);
		}
	}

	NPoint findPointWithID(int searchID) {
		for (NPoint np : points) {
			if ( np.getID() == searchID) return np;
		}
		return null;
	}

	NEdge findEdgeWithID(int searchID) {
		for (NEdge e : edges) {
			if ( e.getID() == searchID) return e;
		}
		return null;
	}

	NPoint getNearestNPoint(PVector mapLoc) {
		NPoint currentClosestNPoint = null;
		//int ms1 = millis();
		//currentClosestNPoint = (NPoint) pointsSpatialIndex.getNearestItem(mapLoc);

		float currentClosestDist = 100000;

		for (NPoint np : points) {
			float d = np.getDistSq(mapLoc);
			if (d < currentClosestDist) {
				currentClosestDist = d;
				currentClosestNPoint = np;
			}
		}
		// end of slow

		//int ms2 = millis();
		//System.out.println("point search took " + ms1 + " " + ms2);
		return currentClosestNPoint;
	}

	NEdge getNearestNEdge(PVector mapLoc) {
		float currentClosestDist = 100000;
		NEdge currentClosestNEdge = null;
		for (NEdge ne : edges) {
			float d = ne.getDistSq(mapLoc);
			if (d < currentClosestDist) {
				currentClosestDist = d;
				currentClosestNEdge = ne;
			}
		}
		return currentClosestNEdge;
	}

	NRegion getNearestNRegion(PVector mapLoc){
		// finds 
		for (NRegion nr: regions) {
			if( nr.isPointInside(mapLoc) ){
				return nr;
			}
		}
		return null;
	}



	public ArrayList<NEdge> getEdges() {
		return edges;
	}

	ArrayList<NPoint> getPoints() {
		return points;
	}

	public ArrayList<NRegion> getRegions() {
		return regions;
	}


	/////////////////////////////////////////////////////////////////////////////

	boolean tryCreateRegion(ArrayList<NEdge> edges) {
		NRegion reg = new NRegion(this, edges);
		if ( reg.isValid()) {
			reg.setEdgesAssociatedRegion();
			regions.add(reg);
			return true;
		}
		// was not a valid region
		return false;
	}


}// end of NNetwork class















