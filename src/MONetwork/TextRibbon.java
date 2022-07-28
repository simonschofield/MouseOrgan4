package MONetwork;

import java.util.ArrayList;

import MOCompositing.TextRenderer;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.TextBank;

public class TextRibbon{
	TextRibbonManager theRibbonManager;
	TextBank theTextBank;
	TextRenderer theTextRenderer = null;
	
	
	Vertices2 theVertices;
	
	float characterHeightDocSpace = 0.001f;
	float letterKerning = 0.0001f;
	
	float currentVericesTraversalPosition = 0;
	int currentRibbonLetterIndex = 0;
	
	ArrayList<RibbonLetter> theRibbonLetters = new ArrayList<RibbonLetter>();
	
	float spaceLength;
	float ribbonPriority = 1;
	
	public TextRibbon(TextRibbonManager manager, TextBank tb, TextRenderer tr, Vertices2 v, float docSpaceFontHeight, float priority){
		theRibbonManager = manager;
		theTextBank = tb;
		theTextRenderer = tr;
		characterHeightDocSpace = docSpaceFontHeight;
		currentRibbonLetterIndex=0;
		theVertices = v;
		currentVericesTraversalPosition = 0;
		ribbonPriority = priority;
		precalculateTextOnVertices();
	}
	
	
	void setPriority(float p) {
		
		
	}
	

	// to replace the two above methods
	public RibbonLetter getNextRibbonLetter() {
		if(currentRibbonLetterIndex >= theRibbonLetters.size()) {
			// no more characters to return, so move on to the next vertices
			return null;
		}
		
		RibbonLetter ribLet = theRibbonLetters.get(currentRibbonLetterIndex);
		currentRibbonLetterIndex++;
		return ribLet;
	}
	
	
	public boolean hasFinishedCrawlingVertices() {
		if(theRibbonLetters.size()==0) return true;	
		if(currentRibbonLetterIndex >= theRibbonLetters.size()) return true;	
		return false;	
	}
	

	private void precalculateTextOnVertices(){
		theRibbonLetters.clear();
		
		spaceLength = getStringDocSpaceLength(" ");
		
		
		float totalVertexLength = theVertices.getTotalLength();
		float currentTextPosition = spaceLength;
		
		while(currentTextPosition < totalVertexLength) {
			// keeps getting the next character from the text bank
			String c = theTextBank.getNextCharAsString();
			
			RibbonLetter rl = getRibbonLetter( totalVertexLength,  currentTextPosition,  c);
			theRibbonLetters.add(rl);
			currentTextPosition += rl.getDocSpaceLength()+letterKerning;
	
		}
		// this adds the ribbonletters to the list in the manager
		// so the TextRibbons may be redundant after this....
		theRibbonManager.theRibbonLetterList.addAll(theRibbonLetters);
	}
	
	private RibbonLetter getRibbonLetter(float totalVertexLength, float currentTextPosition, String c) {
		
		float thisCharLength = getStringDocSpaceLength(c); // currently returns the length + kerning

		float normalisedPositionOnVertexCharStartPt = currentTextPosition/totalVertexLength;
		float normalisedPositionOnVertexCharEndPt = (currentTextPosition+thisCharLength-letterKerning)/totalVertexLength;
		
		PVector charStartPos = theVertices.lerp(normalisedPositionOnVertexCharStartPt);
		PVector charEndPos = theVertices.lerp(normalisedPositionOnVertexCharEndPt);
		RibbonLetter  rl = new RibbonLetter(this, c, charStartPos, charEndPos);
		
		return rl;
	}
	

	private float getStringDocSpaceLength(String s) {
		// returns the DocSpace length of a particular word
		Rect bounds = theTextRenderer.getStringBoundsBufferSpace(s, theTextRenderer.getGraphics2D());
		//System.out.println("getWordDocSpaceLength bounds " + bounds.toStr());
		// now scale the rect so that it's height = characterHeightDocSpace
		float scaler = characterHeightDocSpace/bounds.getHeight();
		return bounds.getWidth()*scaler + letterKerning;
		
	}
	
}






