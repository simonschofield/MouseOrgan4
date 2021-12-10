package MOVectorGraphics;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import MOMaths.PVector;

public class VectorShape{
	String idName;
	public String shapeType;
	Shape shape; 
	public VectorDrawingStyle style = new VectorDrawingStyle();
	
	PVector textLocation;
	public String textContent = "";
	public int textSize;
	
	// so you can store and recover the size of shape
	public PVector p1;
	public PVector p2;
	
	public VectorShape() {}
	
	
	public void setShape(float x1, float y1, float x2, float y2, String shpType, Color fillC, Color lineC, int lineWt) {
		p1 = new PVector(x1,y1);
		p2 = new PVector(x2,y2);
		float w = x2-x1;
		float h = y2-y1;
		switch(shpType) {
		
			case("line"):{
				shapeType = shpType;
				shape = new Line2D.Float(x1,y1, x2, y2 );
				break;
			}
			case("ellipse"):{
				shapeType = shpType;
				
				shape = new Ellipse2D.Float(x1,y1, w,h);		
				break;
			}
			case("rect"):{
				shapeType = shpType;
				shape = new Rectangle.Float(x1,y1,w,h);
				break;
			}
		}
		style.setStyle(fillC, lineC, lineWt); 
	}
	
	public void setTextShape(float x1, float y1, String content, Color fillC, int txtSz) {
		p1 = new PVector(x1,y1);
		p2 = new PVector(x1,y1);
		textLocation = new PVector(x1,y1);
		shapeType = "text";
		textContent = content;
		textSize = txtSz;
		style.setFillColor(fillC);
	}
	
	
	public void setID(String id) {
		idName = id;
	}
	
	public boolean isID(String id) {
		return idName.contentEquals(id);
	}
	
	
	
}