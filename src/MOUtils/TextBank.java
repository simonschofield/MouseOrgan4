package MOUtils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

public class TextBank {

		String theText;
		int characterCounter = 0;
		int loopCount = 0;
		
		
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
			char c = theText.charAt(characterCounter);
			characterCounter++;
			if(characterCounter >= theText.length()) {
				loopCount++;
				characterCounter = 0;
			}
			return String.valueOf(c);
		}
		
		public void seekNextWordStart() {
			seekNextCharInstance(" ", 100);
		}
		public void seekNextSentanceStart() {
			seekNextCharInstance(".", 1000);
		}
		
		private void seekNextCharInstance(String c, int bailCountMax) {
			
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
			
		}
		
		public void setIterator(int n) {
			if(n < theText.length()) {
				characterCounter = n;
			}
			
		}
		
}
