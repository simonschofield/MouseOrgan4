package MONetwork;

import java.util.ArrayList;

import MOCompositing.TextRenderer;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.TextBank;
import MOUtils.UniqueID;

public class TextRibbon{
	
	static UniqueID uniqueID;
	public int id=0;
	// a list of ribbons linked to the END of this ribbon
	public ArrayList<Integer> tailLinkedRibbonsList = new ArrayList<Integer>();
	
	TextBank theTextBank;
	TextRenderer theTextRenderer = null;
	
	
	Vertices2 theVertices;
	
	float characterHeightDocSpace = 0.001f;
	float letterKerning = 0.0001f;
	
	float currentVericesTraversalPosition = 0;
	int currentRibbonLetterIndex = 0;
	
	ArrayList<RibbonLetter> theRibbonLetters = new ArrayList<RibbonLetter>();
	
	//float spaceLength;
	
	
	public TextRibbon(TextBank tb, TextRenderer tr, Vertices2 v, float docSpaceFontHeight){
		
		// the unique ID is to help associate ribbons with other ribbons that connect
		// thereby helping with text-continuity across separate ribbons
		if(uniqueID == null) {
			uniqueID = new UniqueID();
		}
		this.id = uniqueID.getUniqueID();
		
		
		
		theTextBank = tb;
		theTextRenderer = tr;
		characterHeightDocSpace = docSpaceFontHeight;
		currentRibbonLetterIndex=0;
		theVertices = v;
		currentVericesTraversalPosition = 0;
		
		//precalculateTextOnVertices();
	}
	
	
	
	
	
	
	public int getNumLetters() {
		return theRibbonLetters.size();
	}
	
	public void findTailLinkedRibbons(ArrayList<TextRibbon> otherRibbons) {
		// will only work if the ribbons' vertices have NOT been clipped
		tailLinkedRibbonsList = new ArrayList<Integer>();
		PVector myEndPoint = theVertices.getEndPoint();
		for(int n = 0; n < otherRibbons.size(); n++) {
			TextRibbon tr = otherRibbons.get(n);
			if(tr==this) continue;
			PVector otherStartPt = tr.theVertices.getStartPoint();
			
			if(  myEndPoint.equals(otherStartPt)) {
				
				tailLinkedRibbonsList.add(tr.id);
				
			}
			
		}
	}
	
	public void clipVertices(float amt) {
		theVertices = theVertices.getClipped_Length(amt, amt);
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
	
	
	

	public void precalculateTextOnVertices(){
		theTextBank.seekNextSentanceStart();
		theRibbonLetters.clear();
		
		//spaceLength = getStringDocSpaceLengthPlusKerning(" ");
		
		
		float totalVertexLength = theVertices.getTotalLength();
		float currentTextPosition = 0;
		
		while(currentTextPosition < totalVertexLength) {
			// keeps getting the next character from the text bank
			String c = theTextBank.getNextCharAsString();
			
			RibbonLetter rl = getRibbonLetter( totalVertexLength,  currentTextPosition,  c);
			theRibbonLetters.add(rl);
			currentTextPosition += rl.getDocSpaceLength() + letterKerning;
	
		}
		
		// set the word numbers, spaces etc have the number 0
		int wordCount = 0;
		for(RibbonLetter rl: theRibbonLetters) {
			if(rl.theChar.contains(" ")) {
				rl.wordNumber = 0;
				wordCount++;
				continue;
			}
			rl.wordNumber = wordCount;
		}
		
		
	}
	

	
private RibbonLetter getRibbonLetter(float totalVertexLength, float currentTextPosition, String c) {
		
		float thisCharLength = getStringDocSpaceLength(c); // currently returns the length + kerning

		float normalisedPositionOnVertexCharStartPt = currentTextPosition/totalVertexLength;
		float normalisedPositionOnVertexCharEndPt = (currentTextPosition+thisCharLength)/totalVertexLength;
		
		PVector charStartPos = theVertices.lerp(normalisedPositionOnVertexCharStartPt);
		PVector charEndPos = theVertices.lerp(normalisedPositionOnVertexCharEndPt);
		RibbonLetter  rl = new RibbonLetter(this, c, charStartPos, charEndPos);
		
		return rl;
	}
	

	private float getStringDocSpaceLength(String s) {
		// returns the DocSpace length of a particular word
		Rect bounds = theTextRenderer.getStringBoundsBufferSpace(s);

		// now scale the rect so that it's height = characterHeightDocSpace
		float scaler = characterHeightDocSpace/bounds.getHeight();
		return bounds.getWidth()*scaler;
		
	}
	
}






