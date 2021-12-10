package MOVectorGraphics;

import java.awt.BasicStroke;
import java.awt.Color;

////////////////////////////////////////////////////////////////////////////////
//a separate drawing class that could be used anywhere would look like this
//

public class VectorDrawingStyle{

	// the current drawing filling status
	public Color fillColor = new Color(255,255,255,255);
	public Color strokeColor = new Color(0,0,0,255);
	boolean strokeVisible = true;
	boolean fillVisible = true;
	BasicStroke strokeStyle = new BasicStroke();
	public int strokeWeight = 1;
	int textSize = 8;

	public VectorDrawingStyle() {}

	public VectorDrawingStyle copy() {

		VectorDrawingStyle cpy = new VectorDrawingStyle();

		Color fc = copyColor(fillColor);
		Color sc = copyColor(strokeColor);

		cpy.setStyle(fc, sc, strokeWeight);
		cpy.textSize = textSize;
		return cpy;

	}

	public void setStyle(Color fillC, Color lineC, int lineWt){
		fillColor = fillC;
		strokeColor = lineC;
		strokeWeight = lineWt;

		if(lineC.getAlpha() == 0 || lineWt == 0){
			strokeVisible = false;}
		else 
		{ strokeVisible = true; }

		if(fillC.getAlpha() == 0){
			fillVisible = false;}
		else{
			fillVisible = true; }
	}

	public void setFillColor(Color fillC) {
		setStyle( fillC, strokeColor, strokeWeight);
	}

	public void setStrokeColor(Color strokeC) {
		setStyle( fillColor, strokeC, strokeWeight);
	}

	public void setStrokeWeight(int lineWt) {
		setStyle( fillColor, strokeColor, lineWt);
	}



	public Color copyColor(Color c) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());

	}
}