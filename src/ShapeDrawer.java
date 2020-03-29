import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;


////////////////////////////////////////////////////////////////////////////////
// a separate drawing class that could be used anywhere would look like this
//

class DrawingStyle{
	
	// the current drawing filling status
	  Color fillColor = new Color(255,255,255,255);
	  Color strokeColor = new Color(0,0,0,255);
	  boolean strokeVisible = true;
	  boolean fillVisible = true;
	  BasicStroke strokeStyle = new BasicStroke();
	  int strokeWeight = 1;
	  int textSize = 8;
	
	  
	  
	  void setStyle(Color fillC, Color lineC, int lineWt){
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
	  
	  void setFillColor(Color fillC) {
		  setStyle( fillC, strokeColor, strokeWeight);
	  }
	  
	  void setStrokeColor(Color strokeC) {
		  setStyle( fillColor, strokeC, strokeWeight);
	  }
	  
	  void setStrokeWeight(int lineWt) {
		  setStyle( fillColor, strokeColor, lineWt);
	  }

	  DrawingStyle copy() {
		  
		  DrawingStyle cpy = new DrawingStyle();

		  Color fc = copyColor(fillColor);
		  Color sc = copyColor(strokeColor);
		  
		  cpy.setStyle(fc, sc, strokeWeight);
		  cpy.textSize = textSize;
		  return cpy;
		  
	  }
	  
	  Color copyColor(Color c) {
		  return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		  
	  }
}


class DrawnShape{
	String shapeType;
	Shape shape; 
	DrawingStyle style = new DrawingStyle();
	
	PVector textLocation;
	String textContent;
	int textSize;
	
	void setShape(float x1, float y1, float x2, float y2, String shpType, Color fillC, Color lineC, int lineWt) {
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
	
	void setTextShape(float x1, float y1, String content, Color fillC, int txtSz) {
		textLocation = new PVector(x1,y1);
		shapeType = "text";
		textContent = content;
		textSize = txtSz;
		style.setFillColor(fillC);
	}
}

class ShapeDrawer{
  
  Graphics2D graphics2D; 
  

  // the current drawing filling status
  DrawingStyle currentDrawingStyle = new DrawingStyle();
  DrawingStyle cachedDrawingStyle = new DrawingStyle();
  

  
  
  public ShapeDrawer(){
   
   
  }
  
  public ShapeDrawer(Graphics2D g){
	  setGraphicContext(g);   
  }
  
  void setGraphicContext(Graphics2D g){
    graphics2D = g;
    setTextStyle(8);
  }
  
  Color getFillColor() {
	  return currentDrawingStyle.fillColor;
  }
  
  void setFillColor(Color fillC) {
	  currentDrawingStyle.setFillColor( fillC);
  }
  
  void setStrokeColor(Color strokeC) {
	  currentDrawingStyle.setStrokeColor( strokeC);
  }
  
  void setStrokeWeight(int lineWt) {
	  currentDrawingStyle.setStrokeWeight( lineWt);
  }
  
  void setDrawingStyle(Color fillC, Color lineC, int lineWt){
    
	currentDrawingStyle.setStyle( fillC, lineC, lineWt);
    setStrokeStyle(currentDrawingStyle.strokeWeight);
    
    //sets the processing drawing style to the settings of this shape
    //handles a bug in processing where an alpha of 0 results in a solid fill

    

  }
  
  void setTextStyle(int size) {
	  currentDrawingStyle.textSize = size;
	  
  }
  
  void cacheCurrentDrawingStyle() {
	  cachedDrawingStyle =  currentDrawingStyle.copy();
  }
  
  void restoreCachedDrawingStyle() {
	  currentDrawingStyle = cachedDrawingStyle.copy();
	  
  }
  
  Color copyColor(Color c) {
	  return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	  
  }
  
  private void setStrokeStyle(int w){
	  currentDrawingStyle.strokeStyle = new BasicStroke(w);
	  graphics2D.setStroke(currentDrawingStyle.strokeStyle);
	  }
  
  
  void drawDrawnShape(DrawnShape ds) {
	  cacheCurrentDrawingStyle();
	  currentDrawingStyle = ds.style.copy();
	  if(ds.shapeType.contentEquals("text")) {
		  setTextStyle(ds.textSize);
		  drawText(ds.textContent, (int)ds.textLocation.x, (int)ds.textLocation.y);
	  } else {
		  setStrokeStyle(currentDrawingStyle.strokeWeight);
		  drawShape(ds.shape);
	  }
	  restoreCachedDrawingStyle();
  }
  
  void drawEllipse( float x, float y, float w, float h){
    Shape el = new Ellipse2D.Float(x,y, w,h);
    drawShape(el);
  }
  
  void drawLine( float x1, float y1, float x2, float y2){
    Shape ln = new Line2D.Float(x1,y1,x2,y2);
    drawShape(ln);
  }
  
  
  void drawRect( float x1, float y1, float w, float h){
   Shape r = new Rectangle.Float(x1,y1,w,h);
   drawShape(r);
  }
  
  void drawText(String str, int x, int y) {
	  Font font = new Font("Arial", Font.PLAIN, currentDrawingStyle.textSize);
	  graphics2D.setFont(font);
	  graphics2D.drawString( str,  x,  y);
  }
  
  void drawText(String str, int x, int y, int w, int h) {
	  
	  graphics2D.drawString( str,  x,  y);
  }
  
  /*
  void drawJavaPoly(Polyline2D poly, boolean closed){
       // for drawing Polygon2Ds and polylines
       Path2D pth = makePath2D(poly, closed);
       drawJavaShape(pth);
  }
  
  public Path2D makePath2D(Polyline2D poly, boolean closed) {
    Path2D path = new Path2D.Float();
    int numPoints = poly.getNumPoints();
    for (int i = 0; i < numPoints; i++) {
      PVector p = poly.getPoint(i);
      
      if (i == 0) {
        path.moveTo(p.x, p.y);
      }
      else {
        path.lineTo(p.x, p.y);
      }
    }
    if(closed) path.closePath();
    return path;
   }
  */
  
  //////////////////////////////////////////////////////////
  //
  // setting the graphics2D drawing style
  //
  
   private void drawShape(Shape s){
    if(currentDrawingStyle.fillVisible){
      graphics2D.setColor(currentDrawingStyle.fillColor);
      graphics2D.fill(s);
    }
    if(currentDrawingStyle.strokeVisible){
      graphics2D.setColor(currentDrawingStyle.strokeColor);
      graphics2D.draw(s);
    }
  }
  
 
  
  

  
  void setLineStyle(String stl){
    
    float weight =  currentDrawingStyle.strokeWeight;
    if(stl.equals("none")){ currentDrawingStyle.strokeStyle = new BasicStroke(weight);}
    
    if(stl.equals("dash")){
    float dashSize =  3;
    float[] dash1 = { dashSize, 0f, dashSize };
    currentDrawingStyle.strokeStyle = new BasicStroke(weight, 
        BasicStroke.CAP_BUTT, 
        BasicStroke.JOIN_ROUND, 
        1.0f, 
        dash1,
        2f);
    }   
    graphics2D.setStroke(currentDrawingStyle.strokeStyle);
  }
  

}