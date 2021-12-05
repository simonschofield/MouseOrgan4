import java.io.FileWriter;
import java.util.ArrayList;

import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
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
// NAttributes class
// 
// Attributes and other variables common to all NNetwork items
//
class NAttributes {
  private int id;
  static final int TYPE_UNDEFINED = 0;
  static final int TYPE_NPOINT = 1;
  static final int TYPE_NEDGE = 2;
  static final int TYPE_NREGION = 3;

  NNetwork theNetwork;
  //UniqueID uniqueID;

  private int thingInt = TYPE_UNDEFINED; 
  String thingString = "UNDEFINED";

  protected KeyValuePairList attributes = new KeyValuePairList();



  NAttributes(int thng, NNetwork networkref) {

    thingInt = thng;
    //uniqueID = theNetwork.getUniqueIDGenerator();
    theNetwork = networkref;
    if (thingInt == TYPE_NPOINT) thingString = "NPOINT";
    if (thingInt == TYPE_NEDGE) thingString = "NEDGE";
    if (thingInt == TYPE_NREGION) thingString = "NREGION";
    setID();
  }

  int getThing() {
    return thingInt;
  }

  String getThingString() {
    return thingString;
  }



  void setID() {
    // you can only se the id automatically with this 
    // method, whihc uses the auto-inremented number
    id = theNetwork.getUniqueID();
    // System.out.println(" setting id " + id);
  }

  void setID_Override(int i) {
    // the only occasions when you should call this
    // is on file load (to set this ID to the same as the ID in the file)
    id = i;
  }

  int getID() {
    return id;
  }



  void addAttribute( KeyValuePair kvp ) {
	// Items in the network are only allowed to have 1 instance
	// of any specific key, so old instances get removed before adding a new one
	attributes.removeKeyValue(kvp.getKey());
    attributes.addKeyValuePair(kvp);
  }

  void removeAttribute( String k ) {
    attributes.removeKeyValue(k);
  }

  KeyValuePairList getAttributes() {
    return attributes;
  }

  void setAttributes(KeyValuePairList kvpl) {
    // must do a deep copy here to avoid duplication
    attributes = kvpl.copy();
  }

  KeyValuePair getAttribute(String k) {
    return attributes.findKeyValue(k);
  }

  int getNumAttributes() {
    return attributes.getNumItems();
  }

  boolean equals(NAttributes other) {
    return this==other;
  }
  
  void cleanRedundantAttributes(){
    attributes.removeKeyValue("NONE");
    String nameString = attributes.getString("NAME");
    if( nameString.equals("")) {
      attributes.removeKeyValue("NAME");
    }
  }
  
  void printAttributes() {
	  attributes.printMe();
  }
}// end of NAttributes class



//////////////////////////////////////////////////////////////////////////////////////
// NPoint
//
//
// class NPoint extends NAttributes implements SpatialIndexItem {
class NPoint extends NAttributes {

  PVector coordinates;

  // not sure points need attribues, as they can be shared between networks
  // Maybe placename
  // The network types belong to the connecting edges

  ArrayList<NEdge> connectedEdges = new ArrayList<NEdge>();

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
    theNetwork.uniqueIDGenerator.setMinNewID(getID());
    coordinates = new PVector(x, y);
    attributes = kvp;

    // do not want attributes to have copies of the core values
    attributes.removeKeyValue("THING");
    attributes.removeKeyValue("XLOC");
    attributes.removeKeyValue("YLOC");
    attributes.removeKeyValue("ID");
  }
}

//////////////////////////////////////////////////////////////////////////////////////
// NEdge
//
//
class NEdge extends NAttributes {

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

//////////////////////////////////////////////////////////////////////////////////////
// NRegion
// To make a region, you submit a list of NEdges to it. 
// If the list is valid, in that it forms a closed loop, then the region is "valid".
// You can make in-valid regions, but these should be rejected by the NNetwork class, and not added to the regions list.
// Any stray edges, not required to make the loop are not stored in the region.
// Any bizarr topology (such as figure-eights) will probably result in only the first loop found being stored.
//
// The initialisation process, from a bag of unsorted edges, results in these edges being sorted sequentially
// This may then be used to get the sequential vertices of the region.

class NRegion  extends NAttributes {
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
  
  boolean isPointInside(PVector p){
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
  
  Vertices2 getVertices(){
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
      System.out.println("loop not found - region is invalid ");
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
        System.out.println("Network findLoop problem - edges in " + edgesIn.size() + " edges out " + loopedEdges.size() + " strating edge = " + startingEdgeNum);
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
}


//////////////////////////////////////////////////////////////////////////////////////
// NNetwork
//
//
class NNetwork {

  ArrayList<NPoint> points = new ArrayList<NPoint>();
  ArrayList<NEdge> edges = new ArrayList<NEdge>();
  ArrayList<NRegion> regions = new ArrayList<NRegion>();
  //SpatialIndex pointsSpatialIndex;

  UniqueID uniqueIDGenerator;

  NNetwork() {
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

  //void setSpatialIndex(float w, float h, int numInX, int numInY) {
  // pointsSpatialIndex = new SpatialIndex(w, h, numInX, numInY);
  //}

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

  void save(String pathandfilename) {
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

  void load(String pathandfilename) {
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

  void applyROI(Rect roi) {
	  for (NPoint np : points) {
		  	PVector p = np.getPt();
	        PVector nomPoint = roi.norm(p);
	        PVector docSpcPt = GlobalObjects.theDocument.coordinateSystem.normalisedSpaceToDocSpace(nomPoint);
	        np.setPt(docSpcPt);
	      }
  }
  
  
  
  void addItemFromCSV_KVP(KeyValuePairList kvpl) {
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
    
  

  ArrayList<NEdge> getEdges() {
    return edges;
  }

  ArrayList<NPoint> getPoints() {
    return points;
  }

  ArrayList<NRegion> getRegions() {
    return regions;
  }


  /////////////////////////////////////////////////////////////////////////////

  boolean tryCreateRegion(ArrayList<NEdge> edges) {
    NRegion reg = new NRegion(this, edges);
    if ( reg.isValid()) {
      regions.add(reg);
      return true;
    }
    // was not a valid region
    return false;
  }
  
  
}// end of NNetwork class















