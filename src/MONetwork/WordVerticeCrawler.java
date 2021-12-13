package MONetwork;
import java.util.ArrayList;
// Given a Vertices2, map a sequence of words (from the WordBank) to this, with a specific document-space letter-height.
// Usually the vertices are extracted from a vertex crawler.
// The words that make up a particular word-ribbon need to be pre-calculated for the vertex run under consideration;
// this is to balance out the words so they all fit neatly into the vertices, without over/under-runningEach new vertex run.
// So,when setVertxRun is called, it pre calculates the words to be rendered, and the lines that they will use as two separate corresponding array-lists. There is
// some inevitable aliasing between the initial vertices set and the sequence of word-lines this is broken down into, as the word lengths are unknown and unpredictable.
// However the start and end points are always accurate.
//

import MOCompositing.TextRenderer;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOUtils.WordBank;



public class WordVerticeCrawler {
	WordBank theWordBank;
	Vertices2 currentVertices;
	
	float characterHeightDocSpace = 0.001f;
	float currentVericesTraversalPosition = 0;
	
	Line2 currentPrecalculatedWordLine;
	String currentPrecalculatedWord;
	
	int precalculatedListIterator = 0;
	ArrayList<String> precalculatedWords = new ArrayList<String>();
	ArrayList<Line2> precalculatedLines = new ArrayList<Line2>();
	
	TextRenderer textRenderer = null;
	
	//FontCharacterWidths fontCharacterWidths; 
	
	
	public WordVerticeCrawler(WordBank wb, TextRenderer tr){
		theWordBank = wb;
		textRenderer = tr;
		
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////
	// This is set with the vertices you want to render as a "text ribbon"
	// so called beofre you start a new text ribbon
	public void setVerticesRun(Vertices2 v) {
		currentVertices = v;
		currentVericesTraversalPosition = 0;
		precalculateWordsOnCurrentVertices();
	}
	
	///////////////////////////////////////////////////////////////////////////////
	// usually called once at the start of a series of text ribbons
	public void setCharacterHeight(float s) {
		characterHeightDocSpace = s;
	}
	
	
	
	public boolean nextWord() {
		int wordListSize = getNumWordsOnCurrentVertices();
		//System.out.println("nextWord: wordListSize " + wordListSize + " precalculatedListIterator " + precalculatedListIterator);
		if(precalculatedListIterator+1 >= wordListSize) {
			precalculatedListIterator = 0;
			return false;
		}
		precalculatedListIterator++;
		return true;
	}
	
	
	public Line2 getCurrentWordLine() {
		return precalculatedLines.get(precalculatedListIterator);
	}
	
	
	public String getCurrentWord() {
		return precalculatedWords.get(precalculatedListIterator);
	}
	
	
	int getNumWordsOnCurrentVertices() {
		return precalculatedWords.size();
	}
	
	public boolean isConcertinaed(String thisWord,Line2 wordLine, float minAspect) {
		// if a word is concertinaed over a particular amount, then return true.
		// Used to remove such words, or replace them
		// The aspect is the rectangular aspect of each letter
		int numCharacters = thisWord.length();
		float linelength = wordLine.getLength();
		float averageCharacterlength = linelength/numCharacters;
		float thisCharacterAspect = averageCharacterlength/characterHeightDocSpace;
		
		if(thisCharacterAspect < minAspect) return true;
		
		return false;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	// testing only
	void printWordsAndLines() {
		// for testing
		int numItems = precalculatedWords.size();
		for(int n = 0; n < numItems; n++) {
			String s = precalculatedWords.get(n);
			Line2 l = precalculatedLines.get(n);
			float charlen = l.getLength()/s.length();
			System.out.println("words =  " + s + " line len = " + l.getLength() + " char len = " + charlen);
			
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	// private methods
	//
	
	//////////////////////////////////////////////////////////////////////////////
	// you call this once per vertices traversed
	// The words never run out as they loop
	private void precalculateWordsOnCurrentVertices() {
		precalculatedWords = new ArrayList<String>();
		precalculatedLines = new ArrayList<Line2>();
		boolean wordFound = true;
		while(wordFound) {
			wordFound = nextWordCrawl();// returns false on finding the word that goes over the end of the current vertices run
			precalculatedWords.add(currentPrecalculatedWord);
			precalculatedLines.add(currentPrecalculatedWordLine);
		}
		// so, now we have a run of lines, that probably goes slightly over the end of the edge run
		// so we need to refit the words to the lines.

		precalculatedLines = refitVerticesToWords();
		//printWordsAndLines(); 
		//System.out.println("precalculateWordsOnCurrentVertices: num words =  " + precalculatedWords.size());

	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// this called repeatedly by precalculateWordsOnCurrentVertices() , 
	// it calculates the line for the next word in the word bank
	// based on the assumed word physical length. The crawler marches along the vertices until the distance from the 
	// startPoint is === to the word physical length. So, works best with straightish runs.
	
	// The words never run out as they loop
	private boolean nextWordCrawl() {
		// this advances to the next word, and finds the next word line
		// These are then retrievable via getCurrentWordLine and() getCurrentWord()
		currentPrecalculatedWord = theWordBank.getNextWord();
		if(currentPrecalculatedWord == null) {
			// The words never run out as they loop
			System.out.println("WordVerticeTraverser:nextWord words have expired");
			theWordBank.resetItemIterator();
			currentPrecalculatedWord = theWordBank.getNextWord();
		}
		
		// add a space at the end;
		currentPrecalculatedWord = currentPrecalculatedWord + " ";
		
		float thisWordLength = getWordDocSpaceLength(currentPrecalculatedWord);
		
		if(currentVertices==null) {
			System.out.println("WordVerticeTraverser:nextWord currentVertices == null");
			return false;
		}
		
		PVector startPoint = currentVertices.lerp(currentVericesTraversalPosition);
		PVector endPoint;
		
		// work out the traversal step in parametric form
		float totalLengthOfRun = currentVertices.getTotalLength();
		float numCharsWholeLength = totalLengthOfRun/getTypicalCharacterWidthDocSpace();
		float subCharStep = 1/(numCharsWholeLength * 5); // This gives 5 steps per character, hopefully fine enough
		
		float currentVericesTraversalSearchPosition = currentVericesTraversalPosition;
		
		// traverse onwards until the distance (startPoint->currentPoint) >= thisWordLength
		while(true) {
			currentVericesTraversalSearchPosition += subCharStep; //
			
			PVector currentTraversalPoint = currentVertices.lerp(currentVericesTraversalSearchPosition);
			
			if(currentVericesTraversalSearchPosition >= 1) {
				// The vertices have been fully traversed. so this line is curtailed
				
				//endPoint = currentVertices.lerp(1);
				endPoint = currentTraversalPoint;
				currentVericesTraversalPosition = currentVericesTraversalSearchPosition;
				currentPrecalculatedWordLine = new Line2(startPoint, endPoint);
				// now make it the correct length
				PVector v = currentPrecalculatedWordLine.getAsPVector();
				v.setMag(thisWordLength);
				endPoint = PVector.add(startPoint, v);
				currentPrecalculatedWordLine = new Line2(startPoint, endPoint);
				return false; 
			}
			
			if( startPoint.dist(currentTraversalPoint) >= thisWordLength) {
				// the length of traversal of the vertices has been found
				// you now have currentWord, currentWordLine.
				endPoint = currentTraversalPoint;
				currentVericesTraversalPosition = currentVericesTraversalSearchPosition;
				currentPrecalculatedWordLine = new Line2(startPoint, endPoint);
				return true;
			}
			
			
		}

	}
	
	private ArrayList<Line2> refitVerticesToWords() {
		// The first thing to do is make the final (too long) line of the 
		// current vertices end at the correct point
		int finalLineIndex = precalculatedLines.size()-1;
		Line2 lastLine = precalculatedLines.get(finalLineIndex);
		lastLine.p2 = currentVertices.getEndPoint();
		precalculatedLines.set(finalLineIndex, lastLine);
		
		// Turn this into a Vertices2, and interpolate over it, using the word bank
		// to rebuild a correctly distributed vertices2
		Vertices2 precalculatedLinesVerts = new Vertices2();
		precalculatedLinesVerts.setWithLine2List(precalculatedLines);
		
		Vertices2 refitWordVerts = new Vertices2();
		
		WordBank precalcWordBank = new WordBank();
		precalcWordBank.addAll(precalculatedWords);
		
		// add the start point
		PVector startPoint = precalculatedLinesVerts.lerp(0);
		refitWordVerts.add(startPoint);
		
		
		for(int n = 0; n < precalcWordBank.getNumItems(); n++) {
			float param = precalcWordBank.getWordStartAsParametric(n);
			
			PVector thisPoint = precalculatedLinesVerts.lerp(param);
			//System.out.println("refitVerticesToWords: n" + n + " param = " + param + " adding point " + n + " to refit vertices =  " + thisPoint);
			refitWordVerts.add(thisPoint);
		}
		
		
		return refitWordVerts.getAsLine2List();
	}
	
	private float getWordDocSpaceLength(String word) {
		// returns the DocSpace length of a particular word
		Rect bounds = textRenderer.getStringBoundsBufferSpace(word, textRenderer.getGraphics2D());
		//System.out.println("getWordDocSpaceLength bounds " + bounds.toStr());
		// now scale the rect so that it's height = characterHeightDocSpace
		float scaler = characterHeightDocSpace/bounds.getHeight();
		return bounds.getWidth()*scaler;
		
	}
	
	
	
	private float getTypicalCharacterWidthDocSpace() {
		return characterHeightDocSpace * 0.52f;
	}
	
	
	
}



