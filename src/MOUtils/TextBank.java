package MOUtils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

import MOMaths.QRandomStream;

public class TextBank {

		String theText;
		int characterSearchPosition = 0;
		int loopCount = 0;
		
		float spacePadLength;
		
		// for finding random words
		QRandomStream random = new QRandomStream(1);
				
		public void load(String pathAndName) {

				File file = new File(pathAndName);
				StringBuilder fileContents = new StringBuilder((int)file.length()); 
				
				try (Scanner scanner = new Scanner(file)) {
					
				      while(scanner.hasNextLine()) {
				           //fileContents.append(scanner.nextLine() + System.lineSeparator());
				           fileContents.append(scanner.nextLine());
				      }
				      
				      theText =  fileContents.toString();

				}catch (IOException e) {
					
					System.out.println(" TextBank:: load - failed loading text " + pathAndName);	 
				}

		}
		
		public String getNextCharAsString() {
			// set to loop for ever.
			char c = theText.charAt(characterSearchPosition);
			characterSearchPosition++;
			if(characterSearchPosition >= theText.length()) {
				loopCount++;
				characterSearchPosition = 0;
			}
			return String.valueOf(c);
		}
		
		public int seekNextWordStart() {
			return seekNextCharInstance(" ", 100);
		}
		
		public int seekNextSentanceStart() {
			return seekNextCharInstance(".", 1000);
		}
		
		public String getNextWord() {
			int startIndex = seekNextWordStart();
			int endIndex = seekNextWordStart()-1;
			 //System.out.println("getNextWord st " + startIndex + " end " + endIndex);	 
			setIterator(startIndex);
			return theText.substring(startIndex, endIndex);
			
		}
		
		public String previewNextWord() {
			// does not alter the iterator
			int initialIndex = characterSearchPosition;
			String s = getNextWord();
			setIterator(initialIndex);
			return s;
		}
		
		public ArrayList<String> getWordListToFit(int targetNumChrs) {
			// returns a list of words (without any spaces)
			ArrayList<String> foundWords = new ArrayList<String>();
			
			// do something about short vertices < 7 ??
			
			int totalNumChrsFound = 0;
			
			// this bit keeps finding words until the next word takes you over the limit,
			// at which point it bails
			
			while(true) {
				String previewWord = previewNextWord();
				if( (previewWord.length() + totalNumChrsFound) > targetNumChrs) {
					break;
				}
				String nextWord = getNextWord();
				totalNumChrsFound += (nextWord.length()+1); // includes a space in the count, but not in the word stored in the list
				foundWords.add(nextWord);
			}
			
			
			// if no words found, then randomly seek a fitting single word without moving the iterator
			if(foundWords.size()==0) {
				String randomWord = findRandomWordToFit(targetNumChrs);
				totalNumChrsFound = randomWord.length();
				foundWords.add(randomWord);
			}
			
			return foundWords;
			
			
		}
		
		
		
		
		String findRandomWordToFit(int numchars) {
			int initialIndex = characterSearchPosition;
			
			if(numchars==0) return "";
			if(numchars==1) return "-";
			if(numchars==2) return "..";
			
			
			//setIterator(initialIndex); 
			String wordFound = new String();
			while(true) {
				String s = getRandomWord();
				if(s.length() == numchars-1) {
					wordFound = s;
					break;
					}
			}
			
			
			setIterator(initialIndex); 
			
			return wordFound;
		}
		
		
		String getRandomWord() {
			// finds a random word without altering the characterCounter
			int initialIndex = characterSearchPosition;
			
			characterSearchPosition = random.randRangeInt(0, theText.length()-30);// think 30 is enough at the end
			String wd = getNextWord();
			setIterator(initialIndex); 
			return wd;
		}
		
		private int seekNextCharInstance(String c, int bailCountMax) {
			
			int bailCount = 0;
			while(true) {
				String s = getNextCharAsString();
				if(s.contains(c)) break;
				
				bailCount++;
				if(bailCount>bailCountMax) {
					
					System.out.println(" TextBank:: seekNextCharInstance - failed to find the requested character ");	 
					break;
				}
			}
			return characterSearchPosition;
		}
		
		
		
		
		public void setIterator(int n) {
			//if(n < 0) {
			//	characterSearchPosition =  theText.length()+n;
			//	return;
			//}
			
			if(n < theText.length()) {
				characterSearchPosition = n;
				return;
			}

			if(n >= theText.length()){
				characterSearchPosition = n% theText.length();
				return;
			}
			
		}
		
		public int getTextCharLength() {
			return theText.length();
			
		}
		
		public int getIteratorPos() {
			return characterSearchPosition;
			
		}
		
		public void shiftIterator(int d) {
			int pos = characterSearchPosition+d;
			setIterator(pos);
		}
		
}
