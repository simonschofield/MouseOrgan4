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
	int strokeCap = BasicStroke.CAP_ROUND;
	int strokeJoin = BasicStroke.JOIN_ROUND;
	public float strokeWeight = 1;
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

	public void setStyle(Color fillC, Color lineC, Float lineWt){
		// values if nulled will result in no change


		// set the fill colour
		if(fillC!=null) {
			fillColor = fillC;
			if(fillC.getAlpha() == 0){
				fillVisible = false;}
			else{
				fillVisible = true;
				}
		}

		// set the line colour
		if(lineC!=null) {
			strokeColor = lineC;

		}

		// set the line weight
		if(lineWt!=null) {
			strokeWeight = lineWt;
		}


		// have to wait til here to make this decision
		// as BOTH alpha and weight need to be non-zero
		// for the line to be visible.
		if(strokeColor.getAlpha()==0 || strokeWeight==0) {
			strokeVisible=false;
		} else {
			strokeVisible=true;
		}

	}


	void setStrokeCapJoin(int cap, int join) {
		strokeCap = cap;
		strokeJoin = join;

	}

	public void setFillColor(Color fillC) {
		setStyle( fillC, strokeColor, strokeWeight);
	}

	public void setStrokeColor(Color strokeC) {
		setStyle( fillColor, strokeC, strokeWeight);
	}

	public void setStrokeWeight(float lineWt) {
		setStyle( fillColor, strokeColor, lineWt);
	}



	public Color copyColor(Color c) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());

	}
}