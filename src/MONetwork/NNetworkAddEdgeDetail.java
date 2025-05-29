package MONetwork;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import MOAppSessionHelpers.NNetworkEdgeDrawHelper;
import MOImage.KeyImageSampler;
import MOMaths.Line2;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.QRandomStream;

import MOUtils.KeyValuePairList;
import MOZZZ_Depricated.ObjectWithValueList;
/////////////////////////////////////////////////////////////////////////////////////////
//
// add edge detail using random sections across a  displacement image (up to you, but use a fractal one for nice results). 
// Displacement is orthogonal to the original line
// 
//
public class NNetworkAddEdgeDetail {
	NNetwork theNetwork;
	ArrayList<NEdge> theOriginalEdgeList;
	KeyImageSampler displacementImage;
	
	
	QRandomStream qrandom = new QRandomStream(1);
	
	KeyValuePairList docEdgeKVPL = new KeyValuePairList();
	
	
	public NNetworkAddEdgeDetail(NNetwork ntwk, BufferedImage displacemntImage) {
		
		theNetwork = ntwk.copy();
		displacementImage = new KeyImageSampler(displacemntImage);
		docEdgeKVPL.addKeyValue("REGIONEDGE", "document");
	}
	
	
	public NNetwork getNetworkWithAddedEdgeDetail() {
		return theNetwork;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// displace points
	//
	public void displacePoints(float amt) {
		// works on the basis that a point can be displaced (without changing network topology) is by half distance to nearest neighbour+
		System.out.println("Displacing points, please wait ...");
		int moveCount = 0;
		
		ArrayList<NPoint> npointList = theNetwork.getPointsWithEdges();
		int totalPoints = npointList.size();
		for(NPoint np: npointList) {
			if( isDocumentEdgePoint(np) ) continue;
			boolean move = tryDisplacePoint(np, amt);
			if(move) moveCount++;
			if(moveCount%100 == 0) System.out.println("done ..." + moveCount + " out of " + totalPoints);
		}
		System.out.println("moved " + moveCount + " out of " + npointList.size());
		
	}
	
	
	boolean isDocumentEdgePoint(NPoint np) {
		ArrayList<NEdge> connectedEdges = np.getEdgeReferences();
		for(NEdge e: connectedEdges) {
			if(e.getAttributes().containsEqual(docEdgeKVPL)) return true;
		}
		return false;
	}
	
	
	boolean tryDisplacePoint(NPoint np, float amt) {
		PVector undoMove = np.getPt().copy();
		float displaceX = qrandom.randRangeF(-amt, amt);
		float displaceY = qrandom.randRangeF(-amt, amt);
		ArrayList<NEdge> connectedEdges = np.getEdgeReferences();
		
			
		np.coordinates.x += displaceX;
		np.coordinates.y += displaceY;
			
		if( edgesCrossOtherEdges(connectedEdges) ) {
			
				np.setPt(undoMove);	
				return false;
		}
		
		return true;

	}
	
	
	boolean edgesCrossOtherEdges(ArrayList<NEdge> theseEdges) {
		ArrayList<NEdge> otherEdges = (ArrayList<NEdge>) theNetwork.edges.clone();
		otherEdges.remove(theseEdges);
		
		for(NEdge thisEdge: theseEdges) {
			ArrayList<NEdge> crossingEdges = findCrossingEdges( thisEdge,  otherEdges);
			if(crossingEdges.size()>0) return true;
		}
		return false;
	}
	
	
	private static ArrayList<NEdge> findCrossingEdges(NEdge thisEdge, ArrayList<NEdge> otherEdges) {
		// returns a list of all edges in otherEdges list that cross thisEdge
		ArrayList<NEdge> crossingEdges = new ArrayList<NEdge>();
		Line2 thisLine = thisEdge.getLine2();
		Line2 otherLine;
		for(NEdge e: otherEdges) {
			if(e == thisEdge) continue;
			otherLine = e.getLine2();
			if( thisLine.isIntersectionPossible(otherLine) == false ) continue;
			
			if( thisLine.calculateIntersection(otherLine) && thisLine.isConnected(otherLine) == false) {
				crossingEdges.add(e);
			}
		}
		
		return crossingEdges;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// add edge points and displace
	//
	public void addEdgeDetail(float sectionLength, float maxDisplacement, KeyValuePairList searchCriteria) {
		// anything under minLength is not modified
		// anything over is broken into the closest number of minSectionLengths (min 2)
		System.out.println("Adding edge detail, please wait ...");
		
		
		
		if(searchCriteria != null) {
			theNetwork.setSearchAttribute(searchCriteria);
			theOriginalEdgeList = theNetwork.getEdgesMatchingSearchAttributes(true);
			
		} else {
			
			theOriginalEdgeList = (ArrayList<NEdge>) theNetwork.getEdges().clone();
		}
		
		float meanSections = calculateMeanNumSections(sectionLength); 
		System.out.println(" Precalculated mean number of sections per edge is " + meanSections);
		
		
		
		for(int n = 0; n < theOriginalEdgeList.size(); n++) {
			//System.out.println("adding edges to orginal line " + n + " out of " + theOriginalEdgeList.size());
			NEdge e = theOriginalEdgeList.get(n);
			int sections = addDetailToEdge(e, sectionLength, maxDisplacement, meanSections );
			
		}
		
	}
	
	
	float calculateMeanNumSections(float sectionLength) {
		float sectionSum = 0;
		int numSections = 0;
		for(NEdge e: theOriginalEdgeList) {
			float len = e.getLength();
			if(len < sectionLength) continue;
			float thisNumSections = (len/sectionLength) ;
			sectionSum+=thisNumSections;
			numSections++;
		}
		return sectionSum/numSections;
	}
	
	private int addDetailToEdge(NEdge e, float minSectionLength, float maxDisplacement, float meanNumSections) {
		float len = e.getLength();
		
		// max displacement should be modified against the length....
		// shorter edges should not be displaced as much
		
		
		if(len<minSectionLength) return 0;
		
		int numNewPoints = (int) (Math.floor(len/minSectionLength)); //min 1
		
		// max displacement should be modified against the length....
		// shorter edges should not be displaced as much. This is more of a hint than anything, so it will come in four quartiles based 
		// around the meanNumber of section lengths (you only know this after you have run it once!)
		
		if(MOMaths.isBetweenInc(numNewPoints, 1, meanNumSections)) maxDisplacement *= 0.2f;
		if(MOMaths.isBetweenInc(numNewPoints, meanNumSections, meanNumSections*2)) maxDisplacement *= 0.4f;
		if(MOMaths.isBetweenInc(numNewPoints, meanNumSections*2, meanNumSections*4)) maxDisplacement *= 0.7f;
		
		
		
		float parametricStep = 1 / (float)(numNewPoints+1);
		float thisParametricPoint = parametricStep;
		ArrayList<PVector> pointsToInsert = new ArrayList<PVector>();
		//System.out.println(" need to add " + numNewPoints + " new points");
		Line2 edgeLine2 = e.getLine2().copy();
		
		// get the new points to be inserted by stepping along the line of the old edge
		for(int n = 0; n < numNewPoints; n++) {
			PVector thisP = edgeLine2.interpolate(thisParametricPoint);
			thisParametricPoint += parametricStep;
			//System.out.println(" interpolating with value " + over line " + thisP.toStr());
			pointsToInsert.add(thisP);
			
		}
		//System.out.println(" found the points they are " + pointsToInsert.size() + " new points");
		
		// insert those points into the old edge, and the leading new edge(s)
		// keep a list of the new NPoints, as they are to be displaced
		ArrayList<NPoint> nPointsToDisplace = new ArrayList<NPoint>();
		NEdge edgeToSplit = e;
		for(PVector p: pointsToInsert) {
			//System.out.println(" inserting new point at " + p.toStr());
			NEdge[] newSplitEdges = splitEdge(edgeToSplit, p);
			edgeToSplit = newSplitEdges[1];
			nPointsToDisplace.add(edgeToSplit.p1);
		}
		//System.out.println("adding and displacing " + pointsToInsert.size() + " points");
		
		// now displace the nPointsToDisplace
		PVector nv = edgeLine2.getNormalisedVector();
		PVector maxDisplacementVector = PVector.mult(new PVector(nv.y, -nv.x), maxDisplacement);
		float imageStep = parametricStep;
		float yval = qrandom.randRangeF(0, 1);
		for(NPoint np: nPointsToDisplace) {
			
			PVector imagePoint = new PVector(imageStep, yval); // tbd randomise the Y per different edge
			imageStep += parametricStep;
			float val = displacementImage.getValue01NormalisedSpace(imagePoint);
			float rangedDisplacement = MOMaths.lerp(val, -1,1);
			PVector thisDisplacementVector = PVector.mult(maxDisplacementVector, rangedDisplacement);
			np.coordinates.add(thisDisplacementVector);
		}
		return numNewPoints;
	}
	
	private NEdge[] splitEdge(NEdge oldEdge, PVector splitPoint){
		// return the two new new edges where the second edge may need more splitting
		if( oldEdge == null || splitPoint == null) return null;
		NPoint newSplitPoint = new NPoint(splitPoint, theNetwork);
		return splitEdge( oldEdge,  newSplitPoint);
	}

	private NEdge[] splitEdge(NEdge oldEdge, NPoint newSplitPoint) {  
		if( oldEdge == null || newSplitPoint == null) return null;
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
		//updateDependentRegions_SplitEdge( oldEdge,  newEdge1,  newEdge2);
		theNetwork.deleteEdge(oldEdge);
		NEdge[] newEdges = { newEdge1,  newEdge2 };
		
		
		
		return newEdges;
	}

	// not sure we need to do this as this process happens before any regions are found
	private void updateDependentRegions_SplitEdge(NEdge oldEdge, NEdge newEdge1, NEdge newEdge2){
		ArrayList<NRegion> regions = theNetwork.getRegions();

		for(NRegion reg: regions){
			reg.splitEdge( oldEdge,  newEdge1,  newEdge2);
		}
	}
}
