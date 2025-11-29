package MONetwork;

import java.util.ArrayList;

import MOCompositing.TextRenderer;
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

	ArrayList<TextRibbon> theTextRibbonList = new ArrayList<>();

	// this is a copy of all the RibbonLetters that are generated
	ArrayList<RibbonLetter> theRibbonLetterList = new ArrayList<>();
	//TextBank theTextBank;
	//TextRenderer theTextRenderer;
	int currentTextRibbonIndex = 0;

	//////////////////////////////////
	int currrentRibbonLetterListIndex = 0;




	public ArrayList<Vertices2> createVerticesFromNetworkEdges(NNetwork ntwk, KeyValuePairList searchCriteria, float shortestlength) {
		// Convenience method for creating the correct type of vertices from a set of text ribbons
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
		edgeRunExtractor.removeShortEdgeRuns(shortestlength); // suggest 0.005f


		return edgeRunExtractor.getEdgeRunVertices();



	}


	public void createTextRibbons(ArrayList<Vertices2> unclippedVertices, TextBank textBank, TextRenderer textRenderer, float docSpaceFontHeight, float verticesClippingAmt) {
		// RibbonVerts should be added in unclipped, as the association between continuous ribbons works on the
		// basis of connected ribbons sharing a similar end points. If they are preclipped then no association can be made.
		// They should also be set in their run direction Left to right, as this is used in associating continuous text across joined runs

		//theTextRenderer = textRenderer;
		//theDocSpaceFontHeight = docSpaceFontHeight;

		// create the text ribbons
		// they have NOT calculated the text on the vertices yet
	    for(Vertices2 verts: unclippedVertices) {
	    	// this makes the TextRibbon, and populated it with RibbonLetters
	    	TextRibbon rib = new TextRibbon(this, textBank, textRenderer, verts, docSpaceFontHeight);
	    	theTextRibbonList.add(rib);
	    }

	    // now associate all the linked ribbons
	    for(TextRibbon rib: theTextRibbonList) {
	    	rib.findTailLinkedRibbons(theTextRibbonList);
	    }
	    // now you can clip the vertices
	    for(TextRibbon rib: theTextRibbonList) {
	    	rib.clipVertices(verticesClippingAmt);
	    }

	    // This is the bit to alter to make tail-linked text work.
	    // Rather than doing it sequentially, you need to
	    // 1/ rib = getNextUnprocessedTextRibbon() (if null then you have finished)
	    // 2/ rib.precalculateTextOnVertices();
	    // 3/ if there is a tail linked next ribbon, get it and remove nextrib from all the textRibbonLists, else go to 1
	    // 4/ go to 2
	    TextRibbon rib = getNextUnprocessedTextRibbon();
	    while(rib!=null) {

	    	rib.precalculateTextOnVertices();

	    	theRibbonLetterList.addAll(rib.theRibbonLetters);

	    	TextRibbon linkedRib = rib.getUnprocessedTailLinkedRibbon();

	    	if(linkedRib==null) {
	    		rib = getNextUnprocessedTextRibbon();
	    	}else {
	    		rib = linkedRib;
	    	}

	    }



	}




	TextRibbon getNextUnprocessedTextRibbon() {
		for(TextRibbon rib: theTextRibbonList) {
	    	if(!rib.isProcessed()) {
	    		rib.setProcessed(true);
	    		return rib;
	    	}
	    	//System.out.println("adding text ribbon size " + rib.getNumLetters());
	    }
		return null;
	}


	public RibbonLetter getNextRibbonLetter() {
		if(currrentRibbonLetterListIndex >= theRibbonLetterList.size()) {


			return null;
		}
		return theRibbonLetterList.get(currrentRibbonLetterListIndex++);
	}

	public TextRibbon getCurrentTextRibbon() {
		if(currentTextRibbonIndex >= theTextRibbonList.size()) {
			return null;
		}
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



	public float getRibbonLetterIterationProgress() {
		// used just for seeing how far through the list we are to give visual feedback to user in long operations
		return getCurrentTextRibbonLetterNumber()/(float) getCurrentTextRibbonNumLetters();
	}


	public TextRibbon getNextTextRibbon() {
		// returns null if no more
		if(currentTextRibbonIndex >= theTextRibbonList.size()) {
			return null;
		}
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





