package MONetwork;

import java.util.ArrayList;

import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.KeyValuePairList;

//////////////////////////////////////////////////////////////////////////////////////
//NRegion
//To make a region, you submit a list of NEdges to it. 
//If the list is valid, in that it forms a closed loop, then the region is "valid".
//You can make in-valid regions, but these should be rejected by the NNetwork class, and not added to the regions list.
//Any stray edges, not required to make the loop are not stored in the region.
//Any bizarr topology (such as figure-eights) will probably result in only the first loop found being stored.
//
//The initialisation process, from a bag of unsorted edges, results in these edges being sorted sequentially
//This may then be used to get the sequential vertices of the region.

public class NRegion  extends NAttributes {
	ArrayList<NEdge> edgeReferences = new ArrayList<NEdge>();
	Vertices2 vertices;
	Rect extents;

	boolean isValid = false;

	NRegion(NNetwork ntwk, ArrayList<NEdge> edges) {
		super(TYPE_NREGION, ntwk);
		isValid = tryToConstructFromEdges(edges);
	}

	public NRegion( KeyValuePairList kvp, NNetwork ntwk  ) {
		super(TYPE_NREGION, ntwk);
		setWithKeyValuePairList( kvp);
	}

	boolean isValid() {
		return isValid;
	}

	int getNumEdges() {
		return edgeReferences.size();
	}

	NEdge getEdge(int n) {
		return edgeReferences.get(n);
	}

	boolean containsEdge(NEdge e){
		return edgeReferences.contains(e);
	}

	public boolean isPointInside(PVector p){
		if(vertices == null) return false;
		if(extents.isPointInside(p)==false) return false;
		if( vertices.isClosed()==false ) vertices.close();
		return vertices.isPointInside(p);
	}

	NPoint getNPointVertex(int n){
		int rearEdgeIndex = n-1;
		if(rearEdgeIndex == -1) rearEdgeIndex = edgeReferences.size()-1;
		int frontEdgeIndex = n;
		NEdge rearEdge = edgeReferences.get(rearEdgeIndex);
		NEdge frontEdge = edgeReferences.get(frontEdgeIndex);
		return rearEdge.getConnectingPoint(frontEdge);
	}

	public Vertices2 getVertices(){
		ArrayList<PVector> verts = new ArrayList<PVector>();
		for(int n = 0; n < getNumEdges(); n++){
			NPoint np =  getNPointVertex(n);
			verts.add(np.getPt());
		}
		return new Vertices2(verts);
	}





	void splitEdge(NEdge oldEdge, NEdge newEdge1, NEdge newEdge2){
		if( containsEdge(oldEdge) == false) return;
		ArrayList<NEdge> newEdges = (ArrayList)edgeReferences.clone();
		newEdges.remove(oldEdge);
		newEdges.add(newEdge1);
		newEdges.add(newEdge2);
		isValid = tryToConstructFromEdges(newEdges);
	}

	///////////////////////////////////////////////////////////////////
	//
	//
	boolean tryToConstructFromEdges(ArrayList<NEdge> edgesIn) {
		//System.out.println("building a loop from these....K ");
		//printEdges(edgesIn);
		//System.out.println();
		ArrayList<NEdge> loopedEdges = new ArrayList<NEdge>();
		//int startingEdge = 0;
		int numEdgesIn = edgesIn.size();

		if (numEdgesIn < 3) {
			System.out.println("NRegion: tryToConstructFromEdges - not enough edges, need at least 3 ");
			return false;
		}

		boolean result = findLoop(edgesIn, 0, loopedEdges);

		edgeReferences = loopedEdges;

		if (result) {
			//System.out.println("loop found - region is OK ");
			vertices = getVertices();
			vertices.close();
			extents = vertices.getExtents();
			//printEdges(edgeReferences);
		} else {
			//System.out.println("loop not found - region is invalid ");
		}


		return result;
	}

	void printEdges(ArrayList<NEdge> edges) {
		System.out.println("Num edges " + edges.size());
		for (NEdge e : edges) {
			System.out.println(" Edge " + e.toStr());
		}
	}

	boolean findLoop(ArrayList<NEdge> edgesIn, int startingEdgeNum, ArrayList<NEdge> loopedEdges) {
		// the looped edges are returned via the third parameter.
		// we need to make a copy so that we can pop unsortedEdges as we search
		ArrayList<NEdge> unsortedEdges = (ArrayList)edgesIn.clone();

		NEdge startEdge = edgesIn.get(startingEdgeNum);
		unsortedEdges.remove(startEdge);
		NEdge currentEdge = startEdge;
		loopedEdges.add(currentEdge);
		boolean loopFound = false;

		while ( loopFound == false ) {
			if ( unsortedEdges.size() <= 0) return false;
			NEdge connectedToCurrent = popConnectedEdgeInList(currentEdge, unsortedEdges);
			loopedEdges.add(connectedToCurrent);
			if ( connectedToCurrent.connectsWith(startEdge)  && currentEdge != startEdge) break;
			currentEdge = connectedToCurrent;
		}

		if( edgesIn.size() != loopedEdges.size() ){
			// this is to stop a fatal bug TBD
			//System.out.println("Network findLoop problem - edges in " + edgesIn.size() + " edges out " + loopedEdges.size() + " starting edge = " + startingEdgeNum);
			return false;
		}

		return true;
	}


	NEdge popConnectedEdgeInList(NEdge e, ArrayList<NEdge> edgeList) {
		int n = 0;
		NEdge thisEdge = null;
		for (; n < edgeList.size(); n++) {
			thisEdge= edgeList.get(n);
			if (thisEdge == e) continue;
			if ( e.connectsWith(thisEdge) ) break;
		}
		if ( thisEdge != null) {
			edgeList.remove(thisEdge);
			return thisEdge;
		}
		return null;
	}


	///////////////////////////////////////////////////////////////////
	//
	//
	String getAsCSVLine() {
		int numEdges = edgeReferences.size();
		KeyValuePairList coreVariables = new KeyValuePairList();
		coreVariables.addKeyValue("THING", "NREGION");
		coreVariables.addKeyValue("NUMEDGES", numEdges);
		coreVariables.addKeyValue("ID", getID());
		// add al the edges in
		for (int n = 0; n < numEdges; n++) {
			String EdgeKeyString = "EDGE_" + n;
			NEdge thisEdge = edgeReferences.get(n);
			coreVariables.addKeyValue(EdgeKeyString, thisEdge.getID());
		}

		if (attributes.getNumItems()==0) {
			return coreVariables.getAsCSVLine();
		}
		cleanRedundantAttributes();
		// if attributes exist...
		String coreVariableString = coreVariables.getAsCSVString(true);
		return coreVariableString + attributes.getAsCSVLine();
	}



	void setWithKeyValuePairList(KeyValuePairList kvp) {
		int numEdges = kvp.getInt("NUMEDGES");
		ArrayList<NEdge> edges = new ArrayList<NEdge>();

		for (int n = 0; n < numEdges; n++) {
			String EdgeKeyString = "EDGE_" + n;
			int edgeID =  kvp.getInt(EdgeKeyString);
			NEdge thisEdge = theNetwork.findEdgeWithID(edgeID);
			edges.add(thisEdge);
		}

		setID_Override( kvp.getInt("ID") ); 
		theNetwork.uniqueIDGenerator.setMinNewID(getID());
		attributes = kvp;

		//// do not want attributes to have copies of the core values
		attributes.removeKeyValue("NUMEDGES");
		attributes.removeKeyValue("ID");
		attributes.removeKeyValue("THING");
		for (int n = 0; n < numEdges; n++) {
			String EdgeKeyString = "EDGE_" + n;
			attributes.removeKeyValue(EdgeKeyString);
		}

		isValid = tryToConstructFromEdges(edges);
	}
	
	
	
	// region edge-reference stuff, used in automatic construction of regions
	public void setEdgesAssociatedRegion() {
		for(NEdge e: edgeReferences) {
			e.setAssociatedRegion(this);
		}
	}
	
	
	
	
}
