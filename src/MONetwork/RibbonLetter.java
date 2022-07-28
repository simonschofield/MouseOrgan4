package MONetwork;

import MOCompositing.TextRenderer;
import MOMaths.Line2;
import MOMaths.PVector;
import MOMaths.Rect;

public class RibbonLetter{
	TextRibbon theOwningTextRibbon;
	public String theChar;
	
	PVector charStartPos;
	PVector charEndPos; // i.e. just the character no kerning

	PVector centrePos; // some position in the "centre" of the letter
	float collsionRadius = 0.005f; // to define a bounding circle around the letter
	public boolean isVisible = true;
	
	
	public RibbonLetter(TextRibbon owningRibbon, String c, PVector baseLineStartPos, PVector baseLineEndPos) {
		theChar = c;
		theOwningTextRibbon = owningRibbon;
		charStartPos = baseLineStartPos;
		charEndPos = baseLineEndPos;
		collsionRadius = theOwningTextRibbon.characterHeightDocSpace/2f;
	}
	
	public Line2 getCharLine() {
		return new Line2(charStartPos, charEndPos);
	}
	
	
	
	public PVector getCentre() {
		if(centrePos==null) {
			//
			centrePos = PVector.lerp(charStartPos, charEndPos, 0.5f);
		}
		return centrePos;
	}
	
	public boolean intersects(RibbonLetter otherLetter) {
		if( this.getCentre().dist(otherLetter.getCentre()) < collsionRadius) return true;
		return false;
	}
	
	boolean isOtherLetterPartOfThisRibbon(RibbonLetter otherLetter) {
		return ( otherLetter.theOwningTextRibbon == this.theOwningTextRibbon );
	}
	
	public float getDocSpaceLength() {
		return getCharLine().getLength();
	}
	
	public float getPriority() {
		return theOwningTextRibbon.ribbonPriority;
	}
	
	
}
