import java.util.ArrayList;

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
	
	
	WordVerticeCrawler(WordBank wb, TextRenderer tr){
		theWordBank = wb;
		textRenderer = tr;
		//fontCharacterWidths = new FontCharacterWidths();
		//fontCharacterWidths.setForArial();
	}
	
	
	
	
	void setVerticesRun(Vertices2 v) {
		currentVertices = v;
		currentVericesTraversalPosition = 0;
	}
	
	
	void setCharacterHeight(float s) {
		characterHeightDocSpace = s;
	}
	
	
	// you call this once per vertices traversed
	// The words never run out as they loop
	void precalculateWordsOnCurrentVertices() {
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
	
	ArrayList<Line2> refitVerticesToWords() {
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
	
	
	boolean nextWord() {
		int wordListSize = precalculatedListSize();
		//System.out.println("nextWord: wordListSize " + wordListSize + " precalculatedListIterator " + precalculatedListIterator);
		if(precalculatedListIterator+1 >= wordListSize) {
			precalculatedListIterator = 0;
			return false;
		}
		precalculatedListIterator++;
		return true;
	}
	
	
	Line2 getCurrentWordLine() {
		return precalculatedLines.get(precalculatedListIterator);
	}
	
	
	String getCurrentWord() {
		return precalculatedWords.get(precalculatedListIterator);
	}
	
	
	
	
	//// private
	
	int precalculatedListSize() {
		return precalculatedWords.size();
	}
	
	
	
	
	
	/// this called repeatedly by precalculateWordsOnCurrentVertices() , 
	// it calculates the line for the next word in the word bank
	// based on the assumed word physical length. The crawler marches along the vertices until the distance from the 
	// startPoint is === to the word physical length. So, works best with straightish runs.
	
	// The words never run out as they loop
	boolean nextWordCrawl() {
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
	
	
	float getWordDocSpaceLength(String word) {
		
		
		
		
		Rect bounds = textRenderer.getStringBoundsBufferSpace(word, textRenderer.graphics2D);
		//System.out.println("getWordDocSpaceLength bounds " + bounds.toStr());
		// now scale the rect so that it's height = characterHeightDocSpace
		float scaler = characterHeightDocSpace/bounds.getHeight();
		return bounds.getWidth()*scaler;
		
	}
	
	
	
	float getTypicalCharacterWidthDocSpace() {
		return characterHeightDocSpace * 0.52f;
	}
	
	
	
}


// not used - use font metric instead
class FontCharacterWidths{
	// returns a value between 0... around2 , where 1 is the average char width of this font
	KeyValuePairList characterWidthList = new KeyValuePairList();
	float average = 1;
	
	void setForArial() {
		add(" ", 1); 
		add("a", 1.73f); add("b", 1.8f); add("b", 1.51f); add("d", 1.8f); add("e", 1.73f); add("f", 1.05f); add("g", 1.8f); add("h", 1.82f); add("i", 0.78f); add("j", 0.93f); add("k", 1.64f); add("l", 0.78f); add("m", 2.73f);
		add("n", 1.82f); add("o", 1.78f); add("p", 1.8f); add("q", 1.73f); add("r", 1.2f); add("s", 1.45f); add("t", 1.11f); add("u", 1.82f); add("v", 1.8f); add("w", 2.9f); add("x", 1.62f); add("y", 1.64f); add("z", 1.45f);
		add("A", 1.96f); add("B", 1.91f); add("C", 1.96f); add("D", 2.2f); add("E", 1.84f); add("F", 1.71f); add("G", 2.18f); add("H", 2.2f); add("I", 1.22f); add("J", 1.36f); add("K", 1.91f); add("L", 1.63f); add("M", 2.51f);
		add("N", 2.16f); add("O", 2.31f); add("P", 1.8f); add("Q", 2.2f); add("R", 2.02f); add("S", 1.82f); add("T", 2.09f); add("U", 2.15f); add("V", 1.91f); add("W", 2.93f); add("X", 1.91f); add("Y", 1.89f); add("Z", 1.82f);
		add("0", 1.78f); add("1", 1.78f); add("2", 1.78f); add("3", 1.78f); add("4", 1.78f); add("5", 1.78f); add("6", 1.78f); add("7", 1.78f); add("8", 1.78f); add("9", 1.78f); 
		average = getAverage();
	}
	
	void add(String ch, float wdth) {
		characterWidthList.addKeyValue(ch, wdth);
	}
	
	float getWidth(String ch) {
		float w = characterWidthList.getFloat(ch);
		return w/average;
	}
	
	float getAverage() {
		int numItems = characterWidthList.getNumItems();
		float total = 0;
		for(int n = 0; n < numItems; n++) {
			KeyValuePair kvp = characterWidthList.getItem(n);
			total += kvp.getFloat();
		}
		return total/numItems;
	}
	
}




