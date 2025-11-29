package MONetwork;

import java.util.ArrayList;

import MOMaths.PVector;
import MOUtils.KeyValuePairList;

//////////////////////////////////////////////////////////////////////////////////////
//NPoint
//
//
//class NPoint extends NAttributes implements SpatialIndexItem {
/**
 *
 */
public class NPoint extends NAttributes {

	PVector coordinates;

	// not sure points need attribues, as they can be shared between networks
	// Maybe placename
	// The network types belong to the connecting edges

	ArrayList<NEdge> connectedEdges = new ArrayList<>();

	public NPoint( KeyValuePairList kvp, NNetwork ntwk ) {
		super(TYPE_NPOINT, ntwk);
		setWithKeyValuePairList( kvp);
	}

	public NPoint(PVector p, NNetwork ntwk ) {
		super(TYPE_NPOINT, ntwk);
		coordinates = p.copy();
	}

	String toStr() {

		return "NPOINT ID " + getID() + " XY " + PVector2DToString(coordinates);
	}

	String PVector2DToString(PVector v) {
		// because the processing pvector does not have toStr()
		// and this causes a portability issue
		return "[" + v.x + "," + v.y + "]";
	}

	public PVector getPt() {
		return coordinates;
	}

	public void setPt(PVector p) {
		coordinates = p.copy();
	}

	float getDistSq(PVector p) {
		float dx = p.x-coordinates.x;
		float dy = p.y-coordinates.y;
		return dx*dx + dy*dy;
	}

	float getDist(PVector p) {
		return p.dist(coordinates);
	}

	ArrayList<NEdge> getEdgeReferences() {
		return connectedEdges;
	}

	void addEdgeReference(NEdge e) {
		connectedEdges.add(e);
	}

	void removeEdgeReference(NEdge e) {
		connectedEdges.remove(e);
	}


	String getAsCSVLine() {
		KeyValuePairList coreVariables = new KeyValuePairList();
		coreVariables.addKeyValue("THING", "NPOINT");
		coreVariables.addKeyValue("XLOC", coordinates.x);
		coreVariables.addKeyValue("YLOC", coordinates.y);
		coreVariables.addKeyValue("ID", getID());

		cleanRedundantAttributes();
		if (attributes.getNumItems()==0) {
			return coreVariables.getAsCSVLine();
		}
		// if attributes exist...
		String coreVariableString = coreVariables.getAsCSVString(true);
		return coreVariableString + attributes.getAsCSVLine();
	}

	void setWithKeyValuePairList(KeyValuePairList kvp) {
		float x = kvp.getFloat("XLOC");
		float y = kvp.getFloat("YLOC");
		setID_Override( kvp.getInt("ID") );
		theNetwork.uniqueIDGenerator.grabID(getID());
		coordinates = new PVector(x, y);
		attributes = kvp;

		// do not want attributes to have copies of the core values
		attributes.removeKeyValue("THING");
		attributes.removeKeyValue("XLOC");
		attributes.removeKeyValue("YLOC");
		attributes.removeKeyValue("ID");
	}
}
