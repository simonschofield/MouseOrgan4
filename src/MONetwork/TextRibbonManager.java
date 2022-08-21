package MONetwork;

import java.awt.Color;
import java.util.ArrayList;

import MOCompositing.TextRenderer;
import MOMaths.Rect;
import MOMaths.Vertices2;

import MOUtils.KeyValuePairList;
import MOUtils.TextBank;
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// TextRibbonManager contains all the TextRibbons, textRibbons are made up of RibbonLetters
// The whole point is to have the RibbonLetters as persistent objects, that can interact. Specifically delete one another in a priority war to clean up crowded areas.
//
// All the RibbonLetters get created when createTextRibbons(...) is called. They are stored in TextRibbons, but also stored "loose" in theRibbonLetters.
// After which you call getNextRibbonLetter()
public class TextRibbonManager {
	//TextRenderer theTextRenderer;
	///float theDocSpaceFontHeight;
	
	ArrayList<TextRibbon> theTextRibbonList = new ArrayList<TextRibbon>();
	
	// this is a copy of all the RibbonLetters that are generated
	ArrayList<RibbonLetter> theRibbonLetterList = new ArrayList<RibbonLetter>();
	//TextBank theTextBank;
	//TextRenderer theTextRenderer;
	int currentTextRibbonIndex = 0;
	
	//////////////////////////////////
	int currrentRibbonLetterListIndex = 0;
	
	
	
	
	public ArrayList<Vertices2> createVerticesFromNetworkEdges(NNetwork ntwk, KeyValuePairList searchCriteria, Float shortestlength) {
		// Convenience method for creating the correct type of vertices from a nework
		// 45 degree run-angle tolerance
		// sets canOverlap to false, so runs do not inter-penetrate
		// sorts longest to shortest
		// removes too-short edge runs
		// sets the run direction left to right
	    NNetworkEdgeRunFinder edgeRunExtractor = new NNetworkEdgeRunFinder(ntwk, searchCriteria);
		edgeRunExtractor.setAngleTollerance(45f);
		edgeRunExtractor.setRunsCanOverlap(false);
		edgeRunExtractor.extractAllEdgeRunVertices();
		
		edgeRunExtractor.sortEdgeRuns(false); // longest first
		edgeRunExtractor.setRunDirectionPreference(Vertices2.LEFTRIGHT); // running in the human-readable direction!
		if(shortestlength!=null) edgeRunExtractor.removeShortEdgeRuns(shortestlength); // suggest 0.005f
	    
		
		return edgeRunExtractor.getEdgeRunVertices();
		
		

	}
	

	public void createTextRibbons(ArrayList<Vertices2> unclippedVertices, TextBank textBank, TextRenderer textRenderer, float docSpaceFontHeight, float verticesClippingAmt) {
		// RibbonVerts should be assed in un-clipped, as the association between continuous ribbons works on the
		// They should also be set in their run direction Left to right, as this is used in associating continuous text across joined runs
		// basis of connected ribbons sharing a similar end points. If they are pre-clipped then no association can be made.
		//theTextRenderer = textRenderer;
		//theDocSpaceFontHeight = docSpaceFontHeight;
		
		// create the text ribbons
		// they have NOT calculated the text on the vertices yet
	    for(Vertices2 verts: unclippedVertices) {
	    	// this makes the TextRibbon, and populated it with RibbonLetters
	    	TextRibbon rib = new TextRibbon(textBank, textRenderer, verts, docSpaceFontHeight);
	    	theTextRibbonList.add(rib);
	    }
	    
	    // now associate all the linked ribbons
	    for(TextRibbon rib: theTextRibbonList) {
	    	rib.findTailLinkedRibbons(theTextRibbonList);
	    }
	    
	    // now clip the vertices (optional) and then create the text on the ribbon
	    for(TextRibbon rib: theTextRibbonList) {
	    	rib.clipVertices(verticesClippingAmt);
	    	rib.precalculateTextOnVertices();
	    	theRibbonLetterList.addAll(rib.theRibbonLetters);
	    	System.out.println("adding text ribbon size " + rib.getNumLetters());
	    }
	    

	}
	

	public RibbonLetter getNextRibbonLetter() {
		if(currrentRibbonLetterListIndex >= theRibbonLetterList.size()) {
			
			
			return null;
		}
		return theRibbonLetterList.get(currrentRibbonLetterListIndex++);
	}
	
	public TextRibbon getCurrentTextRibbon() {
		if(currentTextRibbonIndex >= theTextRibbonList.size()) return null;
		return theTextRibbonList.get(currentTextRibbonIndex);
	}
	
	public int getCurrentTextRibbonNumber() {
		return currentTextRibbonIndex;
	}
	
	public int getCurrentTextRibbonLetterNumber() {
		return currrentRibbonLetterListIndex;
	}
	
	public int getCurrentTextRibbonNumLetters() {
		return theRibbonLetterList.size();
	}
	
	
	public TextRibbon getNextTextRibbon() {
		// returns null if no more
		if(currentTextRibbonIndex >= theTextRibbonList.size()) return null;
		TextRibbon tr =  theTextRibbonList.get(currentTextRibbonIndex);
		currentTextRibbonIndex++;
		return tr;
	}
	
	/*
	private float getStringDocSpaceLength(String s) {
		// returns the DocSpace length of a particular word
		Rect bounds = theTextRenderer.getStringBoundsBufferSpace(s);
		//System.out.println("getWordDocSpaceLength bounds " + bounds.toStr());
		// now scale the rect so that it's height = characterHeightDocSpace
		float scaler = theDocSpaceFontHeight/bounds.getHeight();
		return bounds.getWidth()*scaler;
		
	}*/
	
	
}





