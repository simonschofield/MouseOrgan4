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
	ArrayList<TextRibbon> theTextRibbonList = new ArrayList<TextRibbon>();
	
	// this is a copy of all the RibbonLetters that are generated
	ArrayList<RibbonLetter> theRibbonLetterList = new ArrayList<RibbonLetter>();
	//TextBank theTextBank;
	//TextRenderer theTextRenderer;
	int currentTextRibbonIndex = 0;
	
	//////////////////////////////////
	int currrentRibbonLetterListIndex = 0;
	
	public void createTextRibbons(NNetwork ntwk, KeyValuePairList searchCriteria, TextBank textBank, TextRenderer textRenderer, float docSpaceFontHeight, Float shotestLength) {
		
	    NNetworkEdgeRunFinder edgeRunExtractor = new NNetworkEdgeRunFinder(ntwk, searchCriteria);
		edgeRunExtractor.setAngleTollerance(45f);
		edgeRunExtractor.extractAllEdgeRunVertices();
		
		edgeRunExtractor.sortEdgeRuns(false); // longest first
		edgeRunExtractor.setRunDirectionPreference(Vertices2.LEFTRIGHT); // running in the human-readable direction!
		if(shotestLength!=null) edgeRunExtractor.removeShortEdgeRuns(shotestLength); // suggest 0.005f
	    
		
		ArrayList<Vertices2> edgeRunVertices = edgeRunExtractor.getEdgeRunVertices();
		float priority = edgeRunVertices.size();
	    for(Vertices2 verts: edgeRunVertices) {
	    	// this makes the TextRibbon, and populated it with RibbonLetters
	    	TextRibbon rib = new TextRibbon(this, textBank, textRenderer, verts, docSpaceFontHeight, priority--);
	    	theTextRibbonList.add(rib);
	    }

		// so now you have the entire population of RibbonLetters made
	}
	
	
	public RibbonLetter getNextRibbonLetter() {
		if(currrentRibbonLetterListIndex >= theRibbonLetterList.size()) return null;
		return theRibbonLetterList.get(currrentRibbonLetterListIndex++);
	}
	
	public TextRibbon getCurrentTextRibbon() {
		if(currentTextRibbonIndex >= theTextRibbonList.size()) return null;
		return theTextRibbonList.get(currentTextRibbonIndex);
	}
	
	
	public TextRibbon getNextTextRibbon() {
		// returns null if no more
		if(currentTextRibbonIndex >= theTextRibbonList.size()) return null;
		TextRibbon tr =  theTextRibbonList.get(currentTextRibbonIndex);
		currentTextRibbonIndex++;
		return tr;
	}
	
	
	
	
	
}





