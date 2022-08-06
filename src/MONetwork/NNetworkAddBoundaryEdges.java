package MONetwork;

import java.util.ArrayList;

import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.Rect;
import MOUtils.GlobalSettings;
import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;

public class NNetworkAddBoundaryEdges {


	///////////////////////////////////////////////////////////////////////////////////////
	// addOuterBoundaryEdges
	// New edges are added to the NNetwork on the precise addOuterBoundaryEdges rect before the region search is undertaken. These are "welded" to the existing network 
	// edges with Attribute REGIONEDGE: "Document". One obvious use it to use the documentRect as the boundary.
	// These are useful for finding the outer areas of the network that would not otherwise be complete regions, but composed of dangling edges, so left blank.
	// Method - 
	//		add 4 new document edges to the NNetwork representing the sides of the boundary rect.
	//	    find all the intersections of the exiting network with the new document edges - store them in allIntersectionsList
	//      get the first edge in the allIntersectionsList - thisEdge
	// 		get all the documentEdges in another list i.e. all edges with attribute "REGIONEDGE", "document" - documentEdgesList (this is 4 at first but will increase with each "weld")
	// 		find the documentEdge in the documentEdgesList that intersects with thisEdge
	//		Weld(thisEdge, intersectingDocumentEdge) - this will split both edges to make 4 new edges connected to the intersection point
	//	    remove thisEdge from the allIntersectionsList
	//      continue until allIntersectionsList is empty.
	// addOuterBoundaryEdges
	//
	//
	//
	
	
	NNetwork theNetwork;
	KeyValuePair documentEdgeAttribute = new KeyValuePair("REGIONEDGE", "document");
	
	
	Rect theBoundaryRect;
	
	public NNetworkAddBoundaryEdges(NNetwork ntwk) {
		
		theNetwork = ntwk.copy();
	}
	
	public NNetwork getNetworkWithAddedBoundary() {
		return theNetwork;
	}
	
	public void addOuterBoundaryEdges(Rect boundaryRect, boolean removeEdgesOutsideBoundary) {

		
		theBoundaryRect = boundaryRect.copy();
		
		
		// first create the new boundary edges
		NPoint topLeft = new NPoint(theBoundaryRect.getTopLeft(),theNetwork);
		theNetwork.addPoint(topLeft);

		NPoint topRight = new NPoint(theBoundaryRect.getTopRight(),theNetwork);
		theNetwork.addPoint(topRight);

		NPoint bottomRight = new NPoint(theBoundaryRect.getBottomRight(),theNetwork);
		theNetwork.addPoint(bottomRight);

		NPoint bottomLeft = new NPoint(theBoundaryRect.getBottomLeft(),theNetwork);
		theNetwork.addPoint(bottomLeft);


		NEdge topEdge = theNetwork.addEdge(topLeft, topRight);
		topEdge.addAttribute(documentEdgeAttribute);
		NEdge rightEdge = theNetwork.addEdge(topRight, bottomRight);
		rightEdge.addAttribute(documentEdgeAttribute);
		NEdge bottomEdge = theNetwork.addEdge(bottomRight, bottomLeft);
		bottomEdge.addAttribute(documentEdgeAttribute);
		NEdge leftEdge = theNetwork.addEdge(bottomLeft,topLeft);
		leftEdge.addAttribute(documentEdgeAttribute);

		//System.out.println("addOuterBoundaryEdges here1");

		// get all the intersections of the network with these edges
		ArrayList<NEdge> allIntersectionsList = new ArrayList<NEdge>();
		allIntersectionsList.addAll(findIntersections(topEdge));
		allIntersectionsList.addAll(findIntersections(rightEdge));
		allIntersectionsList.addAll(findIntersections(bottomEdge));
		allIntersectionsList.addAll(findIntersections(leftEdge));

		//System.out.println("addOuterBoundaryEdges here2");
		// so now we have all the intersecting edges with the documentEdges
		int bailCount = 0;

		while( edgesToWeld(allIntersectionsList) ) {
			bailCount++;
			if(bailCount>1000000) {
				System.out.println("ERROR addOuterBoundaryEdges::bail count exceeded");
				break;
			}
			//if(bailCount%1000 == 0) System.out.println("addOuterBoundaryEdges welding" + bailCount);
		}


		theNetwork.refreshIDs();
		
		if(removeEdgesOutsideBoundary) {
			
			removeEdgesOutsideBoundary();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// private below here
	//
	//
	void removeEdgesOutsideBoundary() {
		if(theBoundaryRect==null) {
			System.out.println("removeItemsOutsideBoundary::boundary rect has not been specified - action aborted");
			return;
		}
		
		
		 
		ArrayList<NEdge> allEdges  = theNetwork.getEdges();
		
		ArrayList<NEdge> toRemove  = new ArrayList<NEdge>();
		for(NEdge ne: allEdges) {
			
			if( ne.getAttributes().containsEqual(documentEdgeAttribute)) continue;

			PVector p1 = ne.p1.coordinates;
			PVector p2 = ne.p2.coordinates;
			if(theBoundaryRect.isPointInside(p1,0.0001f) == false || theBoundaryRect.isPointInside(p2,0.0001f) == false) {
				toRemove.add(ne);
			}

		}

		theNetwork.removeEdges(toRemove);
		
		
		
	}

	
	boolean edgesToWeld(ArrayList<NEdge> allIntersectionsList) {

		if(allIntersectionsList.size()<1) return false;
		NEdge thisEdge = allIntersectionsList.get(0);

		ArrayList<NEdge> documentEdges = getAllDocumentEdges();
		//System.out.println("edgesToWeld documentEdges " + documentEdges.size());
		if(documentEdges.size() == 0) return false;
		for(NEdge docEdge: documentEdges) {
			if( isIntersection(thisEdge, docEdge) ) {
				weldEdges(thisEdge, docEdge);
				allIntersectionsList.remove(thisEdge);
				//System.out.println("edgesToWeld welded edges still to process " + allIntersectionsList.size());
				return true;
			}
		}

		return true;
	}



	ArrayList<NEdge> getAllDocumentEdges(){
		ArrayList<NEdge> documentEdges = new ArrayList<NEdge>();

		ArrayList<NEdge> allEdges = theNetwork.getEdges();
		// find all the possible lines intersecting
		for(NEdge e: allEdges) {
			if( e.getAttributes().containsEqual(documentEdgeAttribute)) {
				documentEdges.add(e);
			}
		}

		return documentEdges;
	}


	ArrayList<NEdge> getAllNonDocumentEdges(){
		ArrayList<NEdge> allEdges = (ArrayList<NEdge>) theNetwork.getEdges().clone();

		ArrayList<NEdge> docEdges = getAllDocumentEdges();
		allEdges.removeAll(docEdges);
		return allEdges;
	}



	ArrayList<NEdge> findIntersections(NEdge theEdge) {
		// first find all the intersecting edges
		ArrayList<NEdge> allEdges = getAllNonDocumentEdges();
		ArrayList<NEdge> intersectingEdges = new ArrayList<NEdge>();

		// find all the possible lines intersecting
		for(NEdge e: allEdges) {
			//if( e.getAttributes().containsEqual(documentEdgeAttribute)) continue;
			if( isIntersection(theEdge,e) ) {
				intersectingEdges.add(e);
			}
		}
		//System.out.println("findAllWelds found " + intersectingEdges.size() + " intersecting edges");

		return intersectingEdges;


	}


	boolean isIntersection(NEdge e1, NEdge e2) {
		Line2 l1 = e1.getLine2();
		Line2 l2 = e2.getLine2();
		//if( l1.isIntersectionPossible(l2)== false) return false;
		boolean result =  l1.calculateIntersection(l2);	
		//System.out.println("line1 " + l1.toStr() + " line2 " + l2.toStr() + " intersection = " + result);
		return result;
	}

	void weldEdges(NEdge e1, NEdge e2) {
		// assumes that they have already been determined as intersecting
		PVector intersectionPoint = e1.getLine2().getIntersectionPoint(e2.getLine2());
		if(intersectionPoint == null) return;

		NPoint newSplitPoint = splitEdge(e1, intersectionPoint);
		splitEdge(e2,  newSplitPoint);

	}

	NPoint splitEdge(NEdge oldEdge, PVector splitPoint){
		if( oldEdge == null || splitPoint == null) return null;
		NPoint newSplitPoint = new NPoint(splitPoint, theNetwork);
		splitEdge( oldEdge,  newSplitPoint);
		return newSplitPoint;
	}

	void splitEdge(NEdge oldEdge, NPoint newSplitPoint) {  
		if( oldEdge == null || newSplitPoint == null) return;
		NPoint np1 = oldEdge.getEndNPoint(0);
		NPoint np2 = oldEdge.getEndNPoint(1);

		KeyValuePairList attr1 = oldEdge.getAttributes().copy();
		KeyValuePairList attr2 = oldEdge.getAttributes().copy();
		theNetwork.addPoint(newSplitPoint);

		// give the new edges a copy of the old one's attributes
		// deep copy is required to avoid reference sharing
		NEdge newEdge1 = theNetwork.addEdge(np1, newSplitPoint);
		newEdge1.setAttributes(attr1);
		NEdge newEdge2 = theNetwork.addEdge(newSplitPoint, np2);
		newEdge2.setAttributes(attr2);
		updateDependentRegions_SplitEdge( oldEdge,  newEdge1,  newEdge2);
		theNetwork.deleteEdge(oldEdge);
	}

	// not sure we need to do this as this process happens before any regions are found
	void updateDependentRegions_SplitEdge(NEdge oldEdge, NEdge newEdge1, NEdge newEdge2){
		ArrayList<NRegion> regions = theNetwork.getRegions();

		for(NRegion reg: regions){
			reg.splitEdge( oldEdge,  newEdge1,  newEdge2);
		}
	}


}
