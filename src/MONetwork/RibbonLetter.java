package MONetwork;

import MOMaths.Line2;
import MOMaths.PVector;
//////////////////////////////////////////////////////////////////////////////////////////////////////
// Contains the description of an individual letter (character) contained within a "TextRibbon"
// The position of the letter is defined by the line between charStartPos and charEndPos, which contains no kerning.
// Letter kerning is taken care of by the text ribbon, which generates the letter locations in th first place
//
public class RibbonLetter{
	public TextRibbon theOwningTextRibbon;
	public String theChar;
	public int wordNumber; // this is the set by the text ribbon, and is the numerical order of the words within the ribbon 0...upward

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
		if( this.getCentre().dist(otherLetter.getCentre()) < collsionRadius) {
			return true;
		}
		return false;
	}

	boolean isOtherLetterPartOfThisRibbon(RibbonLetter otherLetter) {
		return ( otherLetter.theOwningTextRibbon == this.theOwningTextRibbon );
	}

	public float getLength_DocSpace() {
		return getCharLine().getLength();
	}




}
