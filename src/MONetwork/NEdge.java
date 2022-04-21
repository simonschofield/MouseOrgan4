package MONetwork;

import java.util.ArrayList;

import MOMaths.Line2;
import MOMaths.PVector;
import MOUtils.KeyValuePairList;

//////////////////////////////////////////////////////////////////////////////////////
// NEdge
//
//
public class NEdge extends NAttributes {

	NPoint p1 = null;
	NPoint p2 = null;

	Line2 line2 = null;
	
	// associated regions. Only 2 supported in this system.
	// used in automatically finding regions
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
	
	///////////////////////////////////////////////////////////////////////
	//
	// methods about THIS edge points
	//
	NPoint getEndNPoint(int n) {
		if (n==0) return p1;
		if (n==1) return p2;
		return null;
	}

	PVector getEndCoordinate(int n) {
		return getEndNPoint(n).getPt();
	}
	
	boolean isUsingIdenticalPoints(NEdge other) {
		// returns true if the edge happens to be using the same points at either end
		// but may be two separate, legitimate edges. Works with ID of points not the coordinates
		if (p1.getID() == other.p1.getID() && p2.getID() == other.p2.getID()) return true;
		if (p1.getID() == other.p2.getID() && p2.getID() == other.p1.getID()) return true;
		return false;
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
	
	boolean containsPoint(NPoint p) {
		//System.out.println("containsPoint: p " + p.toStr() +  " p1 + " + p1.toStr() + " p2 + " + p2.toStr());
		if (p == p1) return true;
		if (p == p2) return true;
		return false;
	}
	
	NPoint getOtherPoint(NPoint p) {
		if (p == p1) return p2;
		if (p == p2) return p1;
		return null;
	}
	
	void addThisEdgeToPoints() {
		p1.addEdgeReference(this);
		p2.addEdgeReference(this);
	}

	void removeThisEdgeFromPoints() {
		p1.removeEdgeReference(this);
		p2.removeEdgeReference(this);
	}

	///////////////////////////////////////////////////////////////////////
	//
	// accessing edge references of one end, or both ends of an edge
	//
	ArrayList<NEdge> getEdgeReferences(int whichEnd) {
		// returns all edge references of end point, including THIS edge
		return getEndNPoint(whichEnd).getEdgeReferences();
	}
	
	ArrayList<NEdge> getConnectedEdges(int whichEnd) {
		// returns edges references of end point, NOT including THIS edge
		NPoint np = getEndNPoint(whichEnd);
		return getConnectedEdges(np);
	}
	
	ArrayList<NEdge> getConnectedEdges(NPoint thisPoint) {
		// returns edges references of end point, NOT including THIS edge
		if( containsPoint(thisPoint)==false){
			System.out.println("Error:: NEdge: getConnectedEdges(thisPoint)  is not in this edge!");
			return null;
		}
		ArrayList<NEdge> connectedEdges = (ArrayList)thisPoint.getEdgeReferences().clone();
		connectedEdges.remove(this);
		return connectedEdges;
	}

	ArrayList<NEdge> getAllConnectedEdges(){
		// returns all the edges connected to this edge (except itself)
		ArrayList<NEdge> connections0 = getConnectedEdges(0);
		ArrayList<NEdge> connections1 = getConnectedEdges(1);
		connections0.addAll(connections1);
		return connections0;
	}

	///////////////////////////////////////////////////////////////////////
	//
	// methods involving THIS edge and an OTHER edge
	//
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

	
	

	NPoint getConnectingPoint(NEdge other){
		// returns the point joining two edges, if there is one.
		if(p1 == other.p1) return p1;
		if(p2 == other.p2) return p2;
		if(p1 == other.p2) return p1;
		if(p2 == other.p1) return p2;
		return null;
	}
	
	NPoint getFarPointOtherEdge(NEdge other) {
		NPoint joiningPoint =  getConnectingPoint(other);
		if(joiningPoint==null) return null;
		return other.getOtherPoint(joiningPoint);
	}

	
	

	////////////////////////////////////////////////////////////////////////////////////////////////
	// geometric methods - rotations and angles between edges, used in searching etc.
	//
	float getLength() {
		return line2.getLength();
	}

	float getDistSq(PVector p) {
		return line2.distancePointToLineSq( p);
	}

	PVector getNearestPointOnEdge(PVector p) {
		return line2.nearestPointOnLine(p);
	}

	
	float getRotation() {
		PVector v =  line2.getAsPVector();
		float rads = v.heading();
		return rads*57.296f + 90;
	}
	
	float getHingedAngleBetween(NEdge other) {
		// returns the clockwise angle between the two edges at the join point.
		// as if they were clock hands, then agle between the hour hand and the minute hand; if both at 12, then angle between is 0;
		// if 15.00, then angle between is 270. if 18.00 then angle is 180
		NPoint connectingPoint = this.getConnectingPoint(other);
		if(connectingPoint==null) {
			System.out.println("NEdge::getHingedAngleBetween - edges are not joined");
			return -1;
		}
		NPoint otherPointE1 = this.getOtherPoint(connectingPoint);
		NPoint otherPointE2 = other.getOtherPoint(connectingPoint);
		Line2 l1 = new Line2(otherPointE1.getPt(), connectingPoint.getPt());
		Line2 l2 = new Line2(connectingPoint.getPt(), otherPointE2.getPt());
		return l1.getHingedAngleBetween(l2);
	}
	
	
	float getColiniarity(NEdge other) {
		// returns a low number if the two edges are going in the same direction.
		NPoint connectingPoint = this.getConnectingPoint(other);
		if(connectingPoint==null) {
			System.out.println("NEdge::getColiniarity - edges are not joined");
			return -1;
		}
		NPoint otherPointE1 = this.getOtherPoint(connectingPoint);
		NPoint otherPointE2 = other.getOtherPoint(connectingPoint);
		Line2 l1 = new Line2(otherPointE1.getPt(), connectingPoint.getPt());
		Line2 l2 = new Line2(connectingPoint.getPt(), otherPointE2.getPt());
		
		return l1.getAngleBetween(l2);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// used in file reading /saving
	//
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
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// region stuff. Only used by automatic region finding/merging processes
	//
	
	public void setAssociatedRegion(NRegion r) {
		// TBD: called and already if full, the use the smaller of the regions.
		
		
		if(region1==null) {
			region1 = r;
			return;
		}
		if(region2==null) {
			region2 = r;
			return;
		}
		// should not get to here, if it does choose the smaller region on the same side and ditch the larger one.
		// System.out.println("NRegion::setRegion - already has both regions set ");
	}
	
	public int getAssociatedRegionCount() {
		int n = 0;
		if(region1 != null) n++;
		if(region2 != null) n++;
		return n;
		
		//return regionAssociationCount;
	}
	
	public boolean isPartOfRegion(NRegion r) {
		if(region1==r) {
			return true;
		}
		if(region2==r) {
			return true;
		}
		return false;
	}
	
	
}// end of class










