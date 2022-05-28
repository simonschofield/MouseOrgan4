package MOVectorGraphics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import MOMaths.PVector;
import MOMaths.Rect;
import MOMaths.Vertices2;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This is the final class before calling the native Java drawing methods.
// All measurements in this class are in Render Target (whatever buffer that sets the graphics2D) buffer space, but are with floating point accuracy
//
//
public class VectorShapeDrawer{
  
  Graphics2D graphics2D; 
  

  // the current drawing filling status
  VectorDrawingStyle currentDrawingStyle = new VectorDrawingStyle();
  VectorDrawingStyle cachedDrawingStyle = new VectorDrawingStyle();
  

  
  
  public VectorShapeDrawer(){
   
   
  }
  
  public VectorShapeDrawer(Graphics2D g){
	  setGraphicContext(g);   
  }
  
  public void setGraphicContext(Graphics2D g){
    graphics2D = g;
    setTextStyle(8);
  }
  
  public Color getFillColor() {
	  return currentDrawingStyle.fillColor;
  }
  
  public void setFillColor(Color fillC) {
	  currentDrawingStyle.setFillColor( fillC);
  }
  
  public void setStrokeColor(Color strokeC) {
	  currentDrawingStyle.setStrokeColor( strokeC);
  }
  
  public void setStrokeWeight(float lineWt) {
	  currentDrawingStyle.setStrokeWeight( lineWt);
  }
  
  public void setDrawingStyle(Color fillC, Color lineC, float lineWt){
    
	currentDrawingStyle.setStyle( fillC, lineC, lineWt);
    setStrokeStyle(currentDrawingStyle.strokeWeight);
    
    //sets the processing drawing style to the settings of this shape
    //handles a bug in processing where an alpha of 0 results in a solid fill

    

  }
  
  public void setTextStyle(int size) {
	  currentDrawingStyle.textSize = size;
	  
  }
  
  public void cacheCurrentDrawingStyle() {
	  cachedDrawingStyle =  currentDrawingStyle.copy();
  }
  
  public void restoreCachedDrawingStyle() {
	  currentDrawingStyle = cachedDrawingStyle.copy();
	  
  }
  
  public Color copyColor(Color c) {
	  return new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	  
  }
  
  private void setStrokeStyle(float w){
	  //currentDrawingStyle.strokeStyle = new BasicStroke(w);
	  float dash[] = { 4.0f };
	  currentDrawingStyle.strokeStyle = new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, null, 0.0f);
	  graphics2D.setStroke(currentDrawingStyle.strokeStyle);
	  }
  
  
  public void drawDrawnShape(VectorShape ds) {
	  cacheCurrentDrawingStyle();
	  currentDrawingStyle = ds.style.copy();
	  if(ds.shapeType.contentEquals("text")) {
		  //setFillColor(currentDrawingStyle.fillColor);
		  setTextStyle(ds.textSize);
		  drawText(ds.textContent, (int)ds.textLocation.x, (int)ds.textLocation.y);
	  } else {
		  setStrokeStyle(currentDrawingStyle.strokeWeight);
		  drawShape(ds.shape);
	  }
	  restoreCachedDrawingStyle();
  }
  
  public void drawEllipse( float x, float y, float w, float h){
    Shape el = new Ellipse2D.Float(x,y, w,h);
    drawShape(el);
  }
  
  public void drawLine( float x1, float y1, float x2, float y2){
    Shape ln = new Line2D.Float(x1,y1,x2,y2);
    drawShape(ln);
  }
  
  
  public void drawRect(Rect r) {
	  drawRect( r.left, r.top, r.getWidth(), r.getHeight()); 
  }
  
  public void drawRect( float x1, float y1, float w, float h){
   Shape r = new Rectangle.Float(x1,y1,w,h);
   drawShape(r);
  }
  
  
  
  
  public void drawText(String str, int x, int y) {
	  Font font = new Font("Arial", Font.PLAIN, currentDrawingStyle.textSize);
	  graphics2D.setColor(currentDrawingStyle.fillColor);
	  graphics2D.setFont(font);
	  graphics2D.drawString( str,  x,  y);
  }

  public void drawVertices2(Vertices2 v){
	  
	  //System.out.println("drawVertices2 is closed " + v.isClosed());
	  
	  
       Path2D pth = makePath2D(v, v.isClosed());
       drawShape(pth);
  }
  
  private Path2D makePath2D(Vertices2 v, boolean closed) {
    Path2D path = new Path2D.Float();
    int numPoints = v.getNumVertices();
    for (int i = 0; i < numPoints; i++) {
      PVector p = v.get(i);
      
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