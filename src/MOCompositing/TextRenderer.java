package MOCompositing;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import MOApplication.MainDocument;
import MOImage.ImageProcessing;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.QRandomStream;
import MOMaths.Rect;
import MOMaths.Vertices2;
import MOSpriteSeed.Sprite;
import MOUtils.GlobalSettings;
import MOUtils.WordBank;

public class TextRenderer {
	String fontName = "Times New Roman";
	int fontStyle = 0;
	int fontSize = 10;
	Font font;
	
	private Graphics2D graphics2D;
	Color outlineColor = Color.BLACK;
	Color fillColor = Color.WHITE;
	
	float outlineFractionalWidth = 0.05f;
	
	BufferedImage bufferedImage;
	int bufferWidth, bufferHeight;
	ArrayList<String> fontFamilies = new ArrayList<String>();
	
	Sprite backGroundSprite;
	float fontmetricMeanLine, fontmetricBaseLine;
	
	public TextRenderer(){
		
		font = new Font(fontName, fontStyle, fontSize);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] ff = ge.getAvailableFontFamilyNames();
		Collections.addAll(fontFamilies, ff);
		bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		setGraphics2D(bufferedImage.createGraphics());
	}
	
	public void setFont(String name, int style, int size, Color col) {
		
		fontStyle = style;
		fontSize = (int)(size * GlobalSettings.getSessionScale());
		outlineColor = col;
		if(  checkFontExists(name)==false ) {
			return;
		}
		fontName = name;
		
		font = new Font(fontName, fontStyle, fontSize);
		setBackgroundSpriteMargins();
	}
	
	void setOutlineStyle(float outlineFractWidth, Color outlineCol) {
		outlineFractionalWidth = outlineFractWidth;
		fillColor = outlineCol;
	}
	
	
	
	
	
	public Sprite getSprite(String text, float docSpaceFontHeight, Line2 line, Color backgrndCol) {
		//System.out.println("getSpriteText====" + text + "==");

		if(backgrndCol != null) {
			backGroundSprite = createBackgroundSprite( text,  docSpaceFontHeight,  line,  backgrndCol);
		}

		BufferedImage img = this.drawText(text);
		Sprite sprite = new Sprite(img);
		sprite = mapToLine( sprite,  docSpaceFontHeight,  line);
		return sprite;
	}
	
	
	Sprite createBackgroundSprite(String text, float docSpaceFontHeight, Line2 line, Color backgrndCol) {
		// so that the background sprite has the correct blocking of text
		// i.e. between the baseline and the meanline of the font
		//
		String trimmedText = text.trim();
		//System.out.println("createBackgroundSprite====" + text + "==");
		BufferedImage img = this.drawText(trimmedText);

		BufferedImage bkgrndImg = ImageProcessing.createEmptyCopy(img);
		Graphics2D backgroundGraphics = bkgrndImg.createGraphics();	
		backgroundGraphics.setColor(backgrndCol);

		backgroundGraphics.fillRect(0, (int)fontmetricMeanLine, bkgrndImg.getWidth(), (int)(fontmetricBaseLine - fontmetricMeanLine));
		Sprite sprt = new Sprite(bkgrndImg);
		return mapToLine( sprt,  docSpaceFontHeight,  line);
	}
	
	
	void setBackgroundSpriteMargins() {
		FontRenderContext frc = graphics2D.getFontRenderContext();
	    LineMetrics metrics = font.getLineMetrics("The quick brown fox jumps over the lazy dog", frc);
	    //float messageWidth = (float) font.getStringBounds(message, frc).getWidth();

	    // centre text
	    float ht = metrics.getHeight();
	    float ascent = metrics.getAscent();
	    float descent = metrics.getDescent();
	    
	    float topBottomMargin = (ht - (ascent+descent))/2;
	    // so from the top (y = 0) down
	    float topOfAscenders = topBottomMargin;
	    
	    fontmetricMeanLine = topOfAscenders + (descent*2f);
	    fontmetricBaseLine = ascent+(topBottomMargin*4);// seems to work ok for most
		 
	}
	
	Sprite mapToLine(Sprite sprite, float docSpaceFontHeight, Line2 line) {
		
		sprite.rotate(-90);
		sprite.data.origin = new PVector(0.5f,0.0f);
		sprite.scaleToSizeInDocSpace(docSpaceFontHeight, line.getLength());
		float r = line.getRotation();
		sprite.rotate(r);
		sprite.setDocPoint(line.p2);
		return sprite;
	}
	

	public Sprite getBackgroundSprite() {
		// this sprite is generated when getSprite is called. It is avaiable should you need it directly after 
		return backGroundSprite;
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
		
		 
		Rect textBounds = getStringBoundsBufferSpace(s, getGraphics2D());
		
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
			setGraphics2D(bufferedImage.createGraphics());
			getGraphics2D().setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		 
		 
	 }
	 
	 Rect getStringBoundsDocSpace(String s) {
		 Rect textBoundsBufferSpace =  getStringBoundsBufferSpace(s, getGraphics2D());
		 PVector docSpaceExtents = GlobalSettings.getTheDocumentCoordSystem().bufferSpaceToDocSpace((int)textBoundsBufferSpace.getWidth(), (int)textBoundsBufferSpace.getHeight());
		 return new Rect(0,0,docSpaceExtents.x,docSpaceExtents.y);
	 }
	 
	 
	 public Rect getStringBoundsBufferSpace(String s, Graphics2D g2d) {
		 FontMetrics fontMetrics = g2d.getFontMetrics(font);
		 Rectangle2D r2d = fontMetrics.getStringBounds(s, g2d);
		 double bufferSpaceWidth = r2d.getWidth();
		 double bufferSpaceHeight = r2d.getHeight();
		 //bufferSpaceHeight += bufferSpaceHeight*0.1; // to fit descenders
		 
		 int w = (int)Math.ceil(bufferSpaceWidth);
		 int h = (int)Math.ceil(bufferSpaceHeight);
		 
		 return new Rect(0,0,(float)w,(float)h);
		 
	 }
	
	 BufferedImage drawText(String str) {
		createRenderBuffer(str);  
		font = new Font(fontName, fontStyle, fontSize);
		
		getGraphics2D().setColor(outlineColor);
		getGraphics2D().setFont(font);
		getGraphics2D().drawString( str,  0,  font.getSize());
		
		return bufferedImage;
	}
	 
	 
	 
	
	 BufferedImage  drawOutlineText(String text) {
		createRenderBuffer(text); 
	    //Color outlineColor = Color.black;
	    //Color fillColor = Color.white;
	    float outlineActualWidth = outlineFractionalWidth * font.getSize();
	    BasicStroke outlineStroke = new BasicStroke(outlineActualWidth);

        
        // create a glyph vector from your text
        GlyphVector glyphVector = font.createGlyphVector(getGraphics2D().getFontRenderContext(), text);
        // get the shape object
        Shape textShape = glyphVector.getOutline();

        // activate anti aliasing for text rendering (if you want it to look nice)
        getGraphics2D().setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        getGraphics2D().setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);

        getGraphics2D().setStroke(new BasicStroke(outlineActualWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); 
        
        getGraphics2D().setColor(outlineColor);
        getGraphics2D().setStroke(outlineStroke);
        getGraphics2D().translate(0, font.getSize());
        getGraphics2D().draw(textShape); // draw outline

        getGraphics2D().setColor(fillColor);
        getGraphics2D().fill(textShape); // fill the shape

        
	    return bufferedImage;
	}
	 
	 
	float getTypicalCharLength() {
		String word = "abcdefghijklmnopqrstuvwxyz";
		createRenderBuffer(word);
		return bufferWidth/26.0f;
	}
	
	
	
	public Sprite renderTextInPolygon(WordBank wordBank,  Vertices2 polygonIn){

		QRandomStream randomStream = new QRandomStream(1);
		PVector topLeftDocSpace = polygonIn.getExtents().getTopLeft();
		Vertices2 bufferSpaceVertices = polygonIn.getInBufferSpace(true);
		bufferSpaceVertices.close();
		Rect bufferSpaceExtents = bufferSpaceVertices.getExtents();
		int w = (int)bufferSpaceExtents.getWidth() + 1000;
		int h = (int)bufferSpaceExtents.getHeight();
		createRenderBuffer( w,  h);
		
		getGraphics2D().setColor(outlineColor);
		getGraphics2D().setFont(font); 
		
		
		
		
		
	    FontMetrics fontMetrics = getGraphics2D().getFontMetrics(font);
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
	    			getGraphics2D().drawString(currentWord, currentX , currentY);
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

	    
		//ImageSprite sprite = new ImageSprite();
		//sprite.setImage(bufferedImage);
	    Sprite sprite = new Sprite(bufferedImage);
		sprite.data.origin = new PVector(0.0f,0.0f);
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
	
	public void paradeFonts(int size, MainDocument theDocument) {
		int numFonts =  fontFamilies.size();
		
		float currentY = 0;
		Graphics2D documentGraphicsContext = theDocument.getMain().getGraphics2D();
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

	public Graphics2D getGraphics2D() {
		return graphics2D;
	}

	public void setGraphics2D(Graphics2D graphics2d) {
		graphics2D = graphics2d;
		Map<?, ?> desktopHints =  (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
		if (desktopHints != null) {
			graphics2D.setRenderingHints(desktopHints);
			
				
			graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
				    RenderingHints.VALUE_RENDER_QUALITY);
			
			graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				    RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				    
		    graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				   RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		    
		    graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
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


