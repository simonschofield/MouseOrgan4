package MONetwork;

import java.util.ArrayList;

import MOCompositing.TextRenderer;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.TextBank;
import MOUtils.UniqueID;

public class TextRibbon{
	static TextRibbonManager theTextRibbonManager;
	static UniqueID uniqueID;
	public int id=0;

	// a list of ribbons linked to the END of this ribbon
	boolean processed = false;
	public ArrayList<TextRibbon> tailLinkedRibbonsList = new ArrayList<>();

	TextBank theTextBank;
	TextRenderer theTextRenderer = null;


	private Vertices2 theVertices;

	float characterHeightDocSpace = 0.001f;
	float letterKerning = 0.0001f;

	float currentVericesTraversalPosition = 0;
	int currentRibbonLetterIndex = 0;

	ArrayList<RibbonLetter> theRibbonLetters = new ArrayList<>();

	//float spaceLength;
	// for debug
	//float sumLetterlength;
	static float meanLetterLength;

	public TextRibbon(TextRibbonManager manager, TextBank tb, TextRenderer tr, Vertices2 v, float docSpaceFontHeight){

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
		setTheVertices(v);
		currentVericesTraversalPosition = 0;


		if(theTextRibbonManager==null) {
			theTextRibbonManager = manager;
			meanLetterLength = getMeanLetterLength() + letterKerning;
		}
		//precalculateTextOnVertices();
	}






	public int getNumLetters() {
		return theRibbonLetters.size();
	}

	////////////////////////////////////////////////////////////
	// tail linked ribbon stuff
	//
	public void findTailLinkedRibbons(ArrayList<TextRibbon> otherRibbons) {
		// will only work if the ribbons' vertices have NOT been clipped
		//
		tailLinkedRibbonsList = new ArrayList<>();
		PVector myEndPoint = getTheVertices().getEndPoint();
		for(int n = 0; n < otherRibbons.size(); n++) {
			TextRibbon tr = otherRibbons.get(n);
			if(tr==this) {
				continue;
			}
			PVector otherStartPt = tr.getTheVertices().getStartPoint();

			if(  myEndPoint.equals(otherStartPt)) {

				tailLinkedRibbonsList.add(tr);

			}

		}
	}


	public TextRibbon getUnprocessedTailLinkedRibbon() {
		// return an unprocessed linked ribbon, and flag as processed
		// tbd return the most co-linear
		if(tailLinkedRibbonsList.size()==0) {
			return null;
		}
		for(TextRibbon linkedRib: tailLinkedRibbonsList) {
			if(!linkedRib.isProcessed()) {
				linkedRib.setProcessed(true);
				return linkedRib;
			}
		}
		return null;

	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean p) {
		processed = p;
	}






	public void clipVertices(float amt) {
		setTheVertices(getTheVertices().getClipped_Length(amt, amt));
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
		// the smart way of doing this is to
		// 0/ The manager processes the text ribbons in order using tailLinkedRibbonsList as the first preference
		// 1/ find the next word start
		// 2/ see how many words the vertices will fully accommodate
		// 3/ use these words with justification to pad out to the ends of the vertices, so theTextbank
		// given a (clipped) vertices returns the full string to be rendered
		//


		// estimate the number of characters you need to fill the vertices
		float totalVertexLength = getTheVertices().getTotalLength();

		int targetNumChrs = (int)(totalVertexLength/meanLetterLength);

		ArrayList<String> proposedTextList = theTextBank.getWordListToFit(targetNumChrs);




		//float spacePadLength = calculateSpacePadLength(targetNumChrs, proposedTextList);
		String proposedText = stringListToString(proposedTextList);

		int charShortfall = targetNumChrs - proposedText.length();


		//System.out.println("length " + totalVertexLength + proposedText );

		//theTextBank.seekNextSentanceStart();
		theRibbonLetters.clear();

		float currentTextPosition = 0;
		int proposedtextCounter = 0;
		while(  proposedtextCounter <   proposedText.length()   ) {
			// keeps getting the next character from the text bank
			String c = proposedText.substring(proposedtextCounter, proposedtextCounter+1);
			proposedtextCounter++;
			RibbonLetter rl = getRibbonLetter( totalVertexLength,  currentTextPosition,  c);
			theRibbonLetters.add(rl);
			currentTextPosition += (rl.getLength_DocSpace() + letterKerning);

		}
		float actualMean = currentTextPosition/proposedtextCounter;
		if(targetNumChrs > 10) {
			//System.out.println(" estimated chars " + targetNumChrs +   " actual char num " + proposedText.length()  + " estimated mean " + meanLetterLength + " actual mean " + actualMean );
		//System.out.println("v length " + totalVertexLength + " final text pos " + currentTextPosition + " estimated chars " + targetNumChrs +  " estimated mean " + meanLetterlength +  " actual char num " + proposedText.length()  + " actual mean " + actualMean );
		}
	}




	String stringListToString(ArrayList<String> stringList) {
		String strOut = new String();

		for(String s : stringList) {

			strOut += (s + " ");

		}
		// remove the final space
		int lastSpacePos = strOut.length()-1;
		return strOut.substring(0, lastSpacePos);

	}










private RibbonLetter getRibbonLetter(float totalVertexLength, float currentTextPosition, String c) {

		float thisCharLength = getLetterDocSpaceLength(c); // currently returns the length + kerning

		float normalisedPositionOnVertexCharStartPt = currentTextPosition/totalVertexLength;
		float normalisedPositionOnVertexCharEndPt = (currentTextPosition+thisCharLength)/totalVertexLength;

		PVector charStartPos = getTheVertices().lerp(normalisedPositionOnVertexCharStartPt);
		PVector charEndPos = getTheVertices().lerp(normalisedPositionOnVertexCharEndPt);
		RibbonLetter  rl = new RibbonLetter(this, c, charStartPos, charEndPos);

		return rl;
	}


	private float getLetterDocSpaceLength(String s) {
		// returns the DocSpace length of a particular word
		Rect bounds = theTextRenderer.getStringBoundsBufferSpace(s);

		// now scale the rect so that it's height = characterHeightDocSpace
		float scaler = characterHeightDocSpace/bounds.getHeight();
		return bounds.getWidth()*scaler;

	}



	private float getMeanLetterLength() {
		float sum = 0;

		int textPos = theTextBank.getIteratorPos();
		int sizeOfTextList = (int) (theTextBank.getTextCharLength()/5f);
		ArrayList<String> text = theTextBank.getWordListToFit(sizeOfTextList);
		String allLetters =stringListToString(text);
		for(int n =0; n < allLetters.length(); n++) {
			String s = allLetters.substring(n, n+1);
			sum +=getLetterDocSpaceLength(s);
		}
		float mean = sum/allLetters.length();


		theTextBank.setIterator(textPos);
		return mean;
	}






	public Vertices2 getTheVertices() {
		return theVertices;
	}






	public void setTheVertices(Vertices2 theVertices) {
		this.theVertices = theVertices;
	}

}






