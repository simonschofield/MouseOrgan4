import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;

public class WordBank extends CollectionIterator{

	ArrayList<String> wordList = new ArrayList<String>();
	
	
	
	void addAll(ArrayList<String> wordsIn){
		wordList.addAll(wordsIn);
		this.resetItemIterator();
	}
	
	void set(ArrayList<String> wordsIn){
		wordList.clear();
		wordList.addAll(wordsIn);
		this.resetItemIterator();
	}
	
	void load(String pathandfilename) {
		    // there should be a directory in the project folder called seeds
		    try {
		      BufferedReader textFileReader = new BufferedReader(new FileReader(pathandfilename));
		      String textLine;
		      
		      while ((textLine = textFileReader.readLine()) != null) {

		        // do something with the data
		    	  wordList.addAll( splitLine( textLine) );
		        
		      }
		      textFileReader.close();
		    } 
		    catch(Exception e) {
		      System.out.println("csv reader failed");
		    }
		  }
	
	 ArrayList<String> splitLine(String line){
		 ArrayList<String> wordList = new ArrayList<String>();
		 //String[] wordArray = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		 
		 String[] wordArray = line.replaceAll("\\p{P}", "").split("\\s+");
		 
		 for(String s: wordArray) {
			 if(s.isBlank()) continue;
			 wordList.add(s);
		 }
		 return wordList;
	 }
	 
	 void testPrint() {
		 int n = 0;
		 for(String s: wordList) {
			 System.out.println("before " + n + " " + s + " after " + n);
		 }
		 System.out.println("Total number of words " + wordList.size());
	 }

	@Override
	int getNumItems() {
		// TODO Auto-generated method stub
		return wordList.size();
	}

	@Override
	Object getItem(int n) {
		// TODO Auto-generated method stub
		return wordList.get(n);
	}
	
	String getNextWord() {
		Object obj = this.getNextItem();
		if(obj==null) {
			//System.out.println("getNextItem is null");
			return null;
		}
		return (String) obj;
	}
	
	
	int getWordLength(int wordNum) {
	 String word = wordList.get(wordNum);
	 int len = word.length();
	 return len;
	}
	
	
	int getCharsUpToWord(int wordNum) {
		int total = 0;
		if(wordNum >= wordList.size()) {
			System.out.println("Wordbank:getCharsUpToWord index out of range " + wordNum + ", max is "+ wordList.size());
			wordNum = wordList.size()-1;
		}
		for(int n = 0; n <= wordNum; n++) {
			total += getWordLength(n);
		}
		return total;
		
	}
	
	int getTotalChars() {
		int total = 0;
		for(String s: wordList) {
			total += s.length();
			
		}
		return total;
	}
	
	float getWordStartAsParametric(int wordNum) {
		// used to re-fit the words to the word vertices
		// returns a number between 0..1 representing the position of the start of
		// the word in the total list of words
		int totalChars = getTotalChars();
		int charsUpToThisWord = getCharsUpToWord(wordNum);
		return charsUpToThisWord/(float)totalChars;
	}
	
	
	String getNextWords(int numWords, boolean addSpaces) {
		String strOut = "";
		for(int n = 0; n < numWords; n++) {
			strOut = strOut + getNextWord();
			if(addSpaces) strOut = strOut + " ";
		}
		return strOut;
		
	}
	

}


