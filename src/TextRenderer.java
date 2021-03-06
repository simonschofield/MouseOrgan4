import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

class TextRenderer {
	String fontName = "Arial";
	int fontStyle = 0;
	int fontSize = 10;
	Font font;
	
	Graphics2D graphics2D;
	Color outlineColor = Color.BLACK;
	Color fillColor = Color.WHITE;
	
	float outlineFractionalWidth = 0.05f;
	
	BufferedImage bufferedImage;
	int bufferWidth, bufferHeight;
	ArrayList<String> fontFamilies = new ArrayList<String>();
	
	
	TextRenderer(){
		
		font = new Font(fontName, fontStyle, fontSize);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] ff = ge.getAvailableFontFamilyNames();
		Collections.addAll(fontFamilies, ff);
		bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		graphics2D = bufferedImage.createGraphics();
	}
	
	void setFont(String name, int style, int size, Color col) {
		
		fontStyle = style;
		fontSize = (int)(size * GlobalObjects.theSurface.getSessionScale());
		outlineColor = col;
		if(  checkFontExists(name)==false ) {
			return;
		}
		fontName = name;
		
		font = new Font(fontName, fontStyle, fontSize);
	}
	
	void setOutlineStyle(float outlineFractWidth, Color outlineCol) {
		outlineFractionalWidth = outlineFractWidth;
		fillColor = outlineCol;
	}
	
	
	ImageSprite getSprite(String text, float docSpaceFontHeight, Line2 line) {
		BufferedImage img = this.drawText(text);
		ImageSprite sprite = new ImageSprite();
		
		sprite.setImage(img);
		sprite.rotate(-90);
		sprite.origin = new PVector(0.5f,0.0f);
		
		
		
		sprite.scaleToDocSpace(docSpaceFontHeight, line.getLength());
		
		float r = line.getRotation();
		sprite.rotate(r);
		sprite.setDocPoint(line.p2);
		//sprite.mapToLine2(line);
		return sprite;
	}
	
	
	
	
	boolean checkFontExists(String fontName) {
		for(String f: fontFamilies) {
			//System.out.println(f);
			if( f.equals(fontName)) return true;
		}
		System.out.println("TextRenderer:checkFontExists font " + fontName + " is not available ");

		return false;
	}
	
	 void createRenderBuffer(String s) {
		
		 
		Rect textBounds = getStringBoundsBufferSpace(s, graphics2D);
		
		int w = (int)textBounds.getWidth();
		int h = (int)textBounds.getHeight();
	    //System.out.println("font size = " + font.getSize() + " wdth = " + w + "  heigh= " + h);

		createRenderBuffer( w,  h);

	}
	 
	 void createRenderBuffer(int w, int h) {
		 //bufferedImage = ImageProcessing.fill(bufferedImage, Color.PINK);
		 bufferedImage = ImageProcessing.clearImage(bufferedImage);
		 bufferedImage = ImageProcessing.resizeTo(bufferedImage, w, h);
			
		bufferWidth = w;
		bufferHeight = h;
			//System.out.println("created buffered image width " + bufferedImage.getWidth() + " height " + + bufferedImage.getHeight());
			graphics2D = bufferedImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		 
		 
	 }
	 
	 Rect getStringBoundsDocSpace(String s) {
		 Rect textBoundsBufferSpace =  getStringBoundsBufferSpace(s, graphics2D);
		 PVector docSpaceExtents = GlobalObjects.theDocument.bufferSpaceToDocSpace((int)textBoundsBufferSpace.getWidth(), (int)textBoundsBufferSpace.getHeight());
		 return new Rect(0,0,docSpaceExtents.x,docSpaceExtents.y);
	 }
	 
	 
	 Rect getStringBoundsBufferSpace(String s, Graphics2D g2d) {
		 FontMetrics fontMetrics = g2d.getFontMetrics(font);
		 Rectangle2D r2d = fontMetrics.getStringBounds(s, g2d);
		 double bufferSpaceWidth = r2d.getWidth();
		 double bufferSpaceHeight = r2d.getHeight();
		 bufferSpaceHeight += bufferSpaceHeight*0.1; // to fit descenders
		 
		 int w = (int)Math.ceil(bufferSpaceWidth);
		 int h = (int)Math.ceil(bufferSpaceHeight);
		 
		 return new Rect(0,0,(float)w,(float)h);
		 
	 }
	
	 BufferedImage drawText(String str) {
		createRenderBuffer(str);  
		//font = new Font(fontName, fontStyle, fontSize);
		graphics2D.setColor(outlineColor);
		graphics2D.setFont(font);
		graphics2D.drawString( str,  0,  font.getSize());
		return bufferedImage;
	}
	
	 BufferedImage  drawOutlineText(String text) {
		createRenderBuffer(text); 
	    //Color outlineColor = Color.black;
	    //Color fillColor = Color.white;
	    float outlineActualWidth = outlineFractionalWidth * font.getSize();
	    BasicStroke outlineStroke = new BasicStroke(outlineActualWidth);

        
        // create a glyph vector from your text
        GlyphVector glyphVector = font.createGlyphVector(graphics2D.getFontRenderContext(), text);
        // get the shape object
        Shape textShape = glyphVector.getOutline();

        // activate anti aliasing for text rendering (if you want it to look nice)
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setStroke(new BasicStroke(outlineActualWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); 
        
        graphics2D.setColor(outlineColor);
        graphics2D.setStroke(outlineStroke);
        graphics2D.translate(0, font.getSize());
        graphics2D.draw(textShape); // draw outline

        graphics2D.setColor(fillColor);
        graphics2D.fill(textShape); // fill the shape

        
	    return bufferedImage;
	}
	 
	 
	float getTypicalCharLength() {
		String word = "abcdefghijklmnopqrstuvwxyz";
		createRenderBuffer(word);
		return bufferWidth/26.0f;
	}
	
	
	
	public ImageSprite renderTextInPolygon(WordBank wordBank,  Vertices2 polygonIn){

		QRandomStream randomStream = new QRandomStream(1);
		PVector topLeftDocSpace = polygonIn.getExtents().getTopLeft();
		Vertices2 bufferSpaceVertices = polygonIn.getInBufferSpace(true);
		bufferSpaceVertices.close();
		Rect bufferSpaceExtents = bufferSpaceVertices.getExtents();
		int w = (int)bufferSpaceExtents.getWidth() + 1000;
		int h = (int)bufferSpaceExtents.getHeight();
		createRenderBuffer( w,  h);
		
		graphics2D.setColor(outlineColor);
		graphics2D.setFont(font); 
		
		
		
		
		
	    FontMetrics fontMetrics = graphics2D.getFontMetrics(font);
	    int lineHeight = (int)(fontMetrics.getHeight()*0.75f);
	    int currentLine = 0;
	    
	    
	    String currentWord = wordBank.getNextWord() + " ";
	   
	    while(true) {
	    	// while the current line is within the Y bounds of the polygon
	    	
	    	
	    	int currentY = currentLine * lineHeight;
	    	if(currentY > h) break;
	    	int currentX = 0;
	    	boolean firstInNewLine = true;
	    	// do a scan line in X
	    	while(true) {
	    		
	    		
	    		int wordWidth = fontMetrics.stringWidth(currentWord);
	    		
	    		
	    		
	    		
	    		PVector topLeftOfWord = new PVector(currentX, currentY-lineHeight);
	    		Rect rect = new Rect(topLeftOfWord, wordWidth, lineHeight);
	    		//PVector centrePoint = rect.getCentre();
	    		//if(bufferSpaceVertices.isPointInside(centrePoint)) {
	    		if( canFitWord(rect, bufferSpaceVertices) ) {
	    			
	    			
	    			if(firstInNewLine) {
	    				// jiggle a bit as the left-hand side is
	    				// is otherwise conspiculously accurate, and the right hand side is
	    				// jagged
		    			currentX += randomStream.randRangeInt(0, (int)(wordWidth/2f + 0.5f));
		    			firstInNewLine = false;
		    		}
	    			
	    			
	    			// write the word
	    			graphics2D.drawString(currentWord, currentX , currentY);
	    			//move the X to the end of the rect
	    			currentX += wordWidth;
	    			currentWord = wordBank.getNextWord() + " ";
	    		} else {
	    			// move on to next x, 
	    			currentX += 1;
	    		}
	    		if( currentX > w) {
	    			break;
	    		}
	    		
	    	}
	    	currentLine++;
	    	
	    	
	    }

	    
		ImageSprite sprite = new ImageSprite();
		sprite.setImage(bufferedImage);
		sprite.origin = new PVector(0.0f,0.0f);
		sprite.setDocPoint(topLeftDocSpace);
		
		return sprite;
	    
	    
	    
	}// end of method
	
	boolean canFitWord(Rect r, Vertices2 vert) {
		PVector wordStart = r.interpolate(new PVector(0.0f, 0.5f));
		PVector oneThird = r.interpolate(new PVector(0.333f, 0.5f));
		PVector halfWay = r.interpolate(new PVector(0.5f, 0.5f));
		PVector twoThirds = r.interpolate(new PVector(0.666f, 0.5f));
		if(vert.isPointInside(wordStart) && vert.isPointInside(halfWay)) return true;
		return false;
		
		
	}
	
	void paradeFonts(int size) {
		int numFonts =  fontFamilies.size();
		
		float currentY = 0;
		Graphics2D documentGraphicsContext = GlobalObjects.theDocument.graphics2D;
		for(int n = 0; n < numFonts; n++) {
			String fnm = fontFamilies.get(n);
			Font thisFont = new Font(fnm, 0, size);
			
			documentGraphicsContext.setColor(Color.BLACK);
			documentGraphicsContext.setFont(thisFont);
			
			String str = fnm + " The quick brown fox jumped over the lazy hen";

			FontMetrics fontMetrics = documentGraphicsContext.getFontMetrics(font);
			 Rectangle2D r2d = fontMetrics.getStringBounds(str, documentGraphicsContext);
			 double bufferSpaceHeight = r2d.getHeight();
			 bufferSpaceHeight *= 0.5f;
			
			
			
			currentY += bufferSpaceHeight;
			//currentY += 30;
			
			documentGraphicsContext.drawString( str,  100,  currentY);
		}
		
		
	}
		
}
	  
	  



/* Fonts available are
  
Arial
Arial Black
Bahnschrift
Calibri
Calibri Light
Cambria
Cambria Math
Candara
Candara Light
Comic Sans MS
Consolas
Constantia
Corbel
Corbel Light
Courier New
Dialog
DialogInput
Ebrima
Franklin Gothic Medium
Gabriola
Gadugi
Georgia
HoloLens MDL2 Assets
Impact
Ink Free
Javanese Text
Leelawadee UI
Leelawadee UI Semilight
Lucida Console
Lucida Sans Unicode
Malgun Gothic
Malgun Gothic Semilight
Marlett
Microsoft Himalaya
Microsoft JhengHei
Microsoft JhengHei Light
Microsoft JhengHei UI
Microsoft JhengHei UI Light
Microsoft New Tai Lue
Microsoft PhagsPa
Microsoft Sans Serif
Microsoft Tai Le
Microsoft YaHei
Microsoft YaHei Light
Microsoft YaHei UI
Microsoft YaHei UI Light
Microsoft Yi Baiti
MingLiU-ExtB
MingLiU_HKSCS-ExtB
Mongolian Baiti
Monospaced
MS Gothic
MS PGothic
MS UI Gothic
MV Boli
Myanmar Text
Nirmala UI
Nirmala UI Semilight
NSimSun
Palatino Linotype
PMingLiU-ExtB
SansSerif
Segoe MDL2 Assets
Segoe Print
Segoe Script
Segoe UI
Segoe UI Black
Segoe UI Emoji
Segoe UI Historic
Segoe UI Light
Segoe UI Semibold
Segoe UI Semilight
Segoe UI Symbol
Serif
SimSun
SimSun-ExtB
Sitka Banner
Sitka Display
Sitka Heading
Sitka Small
Sitka Subheading
Sitka Text
Sylfaen
Symbol
Tahoma
Times New Roman
Trebuchet MS
Verdana
Webdings
Wingdings
Yu Gothic
Yu Gothic Light
Yu Gothic Medium
Yu Gothic UI
Yu Gothic UI Light
Yu Gothic UI Semibold
Yu Gothic UI Semilight
 * *
 */


