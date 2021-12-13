package MONetwork;

import java.util.ArrayList;

import MOMaths.Line2;
import MOMaths.PVector;
import MOUtils.KeyValuePairList;

//////////////////////////////////////////////////////////////////////////////////////
//NEdge
//
//
public class NEdge extends NAttributes {

	NPoint p1 = null;
	NPoint p2 = null;

	Line2 line2 = null;
	NRegion region1;
	NRegion region2;

	NEdge(NPoint a, NPoint b, NNetwork ntwk ) {
		super(TYPE_NEDGE, ntwk);
		p1 = a;
		p2 = b;
		addThisEdgeToPoints();
		line2 = new Line2(p1.getPt(), p2.getPt());
	}

	public NEdge( KeyValuePairList kvp, NNetwork ntwk  ) {
		super(TYPE_NEDGE, ntwk);
		setWithKeyValuePairList( kvp);
	}

	String toStr() {
		return " NEdge ID" + getID() + " p1 " + p1.toStr() + " p2 " + p2.toStr();
	}


	boolean isUsingIdenticalPoints(NEdge other) {
		// returns true if the edge happens to be using the same points at either end
		// but may be two separate, legitimate edges. Works with ID of points not the coordinates
		if (p1.getID() == other.p1.getID() && p2.getID() == other.p2.getID()) return true;
		if (p1.getID() == other.p2.getID() && p2.getID() == other.p1.getID()) return true;
		return false;
	}


	NPoint getEndNPoint(int n) {
		if (n==0) return p1;
		if (n==1) return p2;
		return null;
	}


	PVector getEndPt(int n) {
		return getEndNPoint(n).getPt();
	}

	void setOneEnd(NPoint p) {
		if (p1 == null) {
			p1 = p;
			return;
		}
		if (p2 == null) {
			p2 = p;
			return;
		}
		if (p1 != null && p2 != null) addThisEdgeToPoints();
	}

	ArrayList<NEdge> getEdgeReferences(int whichEnd) {
		if (whichEnd == 0) return p1.getEdgeReferences();
		if (whichEnd == 1) return p2.getEdgeReferences();
		return null;
	}

	ArrayList<NEdge> getConnectedEdges(int whichEnd) {
		ArrayList<NEdge> edgeRefs = (ArrayList)getEdgeReferences(whichEnd).clone();
		if(edgeRefs == null) return null;
		edgeRefs.remove(this);
		return edgeRefs;
	}

	boolean connectsWith(NEdge other) {
		if (p1 == other.p1) return true;
		if (p2 == other.p2) return true;
		if (p1 == other.p2) return true;
		if (p2 == other.p1) return true;
		return false;
	}

	boolean connectsWithEnd(NEdge other, int whichEnd) {
		NPoint np = getEndNPoint(whichEnd);
		if (np == other.p1) return true;
		if (np == other.p2) return true;
		return false;
	}

	NPoint getOtherPoint(NPoint p) {
		if (p == p1) return p2;
		if (p == p2) return p1;
		return null;
	}

	NPoint getConnectingPoint(NEdge other){
		if(p1 == other.p1) return p1;
		if(p2 == other.p2) return p2;
		if(p1 == other.p2) return p1;
		if(p2 == other.p1) return p2;
		return null;
	}

	float getLength() {
		return line2.getLength();
	}

	float getRotation() {
		PVector v =  line2.getAsPVector();
		float rads = v.heading();
		return rads*57.296f + 90;
	}

	float getDistSq(PVector p) {
		return line2.distancePointToLineSq( p);
	}

	PVector getNearestPointOnEdge(PVector p) {
		return line2.nearestPointOnLine(p);
	}

	void addThisEdgeToPoints() {
		p1.addEdgeReference(this);
		p2.addEdgeReference(this);
	}

	void removeThisEdgeFromPoints() {
		p1.removeEdgeReference(this);
		p2.removeEdgeReference(this);
	}


	String getAsCSVLine() {
		KeyValuePairList coreVariables = new KeyValuePairList();
		coreVariables.addKeyValue("THING", "NEDGE");
		coreVariables.addKeyValue("P1_ID", p1.getID());
		coreVariables.addKeyValue("P2_ID", p2.getID());
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
		int p1ID = kvp.getInt("P1_ID");
		int p2ID = kvp.getInt("P2_ID");
		//p1 = theNetwork.findPointWithID(p1ID);
		//p2 = theNetwork.findPointWithID(p2ID);
		p1 = theNetwork.points.get(p1ID);
		p2 = theNetwork.points.get(p2ID);
		line2 = new Line2(p1.getPt(), p2.getPt());
		setID_Override( kvp.getInt("ID") ); 
		theNetwork.uniqueIDGenerator.setMinNewID(getID());
		attributes = kvp;

		// do not want attributes to have copies of the core values
		attributes.removeKeyValue("THING");
		attributes.removeKeyValue("P1_ID");
		attributes.removeKeyValue("P2_ID");
		attributes.removeKeyValue("ID");
		addThisEdgeToPoints();
	}
}