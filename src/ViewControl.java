import java.awt.Color;
import java.awt.event.KeyEvent;

// The view control provides and manipulates a Document Space
// rectangle which is the portion of the documents viewed under  
// a particular zoom and pan
// It also controls other view aspects, such are canvas window colour
// to set a "background" against whihc to preview renders
//
class ViewControl{
  
  // View control 2 is in Document Space
  Rect viewRect = new Rect(0,0,1,1);
  private float aspect = 1;
  float maxRight = 1;
  float maxBottom = 1;
  
  Color viewBackgroundColor = new Color(0,0,0,0);
  
  public ViewControl(){
      
    }
  
  Color getViewBackgroundColor() {
	  return viewBackgroundColor;
  }
  
  void setViewBackgroundColor(Color c) {
	  viewBackgroundColor = c;
  }

    
  Rect getZoomRectDocSpace(){
    // name needs changing when working
    return viewRect.copy();
  }

  void setDocumentAspect(float asp){
    // called upon loading a document
    aspect = asp;
    reset();
  }
  
  void reset(){
    maxRight = 1;
    maxBottom = 1;
    if(aspect >= 1.0){
      maxBottom = 1/aspect;
      viewRect = new Rect(0,0,1,maxBottom);
    } else {
      maxRight = 1*aspect;
      viewRect = new Rect(0,0,maxRight,1);
    }
    
  }
  
  float getScale(){
    if(aspect >= 1.0){
      return viewRect.getWidth();
    } else {
      return viewRect.getHeight();
    }
    
  }
  
  void zoom(int sign){
    // translates a crude request to zoom in or out into a meaningful scaling
    float currentScale = getScale();
    float newAttempedScale = 1.0f;
    if(sign == 1){
      // zoom in, so make the scale smaller
      newAttempedScale = currentScale * 0.75f;
    }
    if(sign == -1){
      // zoom out, so make the scale bigger
      newAttempedScale = currentScale * 1.25f;
    }
    setScaleAboutCentre( newAttempedScale );
  }
  
  void setScaleAboutCentre(float absScale){
    absScale = limitScale(absScale);
    PVector currentCentre = viewRect.getCentre();
    Rect r = createRectAboutPoint( absScale, currentCentre);
    viewRect = shuntInside(r);
  }
  
  Rect createRectAboutPoint(float scl, PVector cen){
    float halfWidth = (maxRight*scl)/2f;
    float halfHeight = (maxBottom*scl)/2f;
    float x1 = cen.x - halfWidth;
    float y1 = cen.y - halfHeight;
    float x2 = cen.x + halfWidth;
    float y2 = cen.y + halfHeight;
    return new Rect(x1,y1,x2,y2);
    
  }
  
  
  void shiftXY(float dx, float dy){
    // the amount of shift is scaled here to be a proportion of the current view
    dx = dx*getScale();
    dy = dy*getScale();
    
    Rect shiftedRect = viewRect.getTranslated(dx,dy);
    viewRect = shuntInside(shiftedRect);
    
  }
  
  
  Rect shuntInside(Rect r){
    if( r.getWidth() > maxRight || r.getHeight() > maxBottom ){
      //println("shuntInside: the rect r ", r, "is larger than the bounds of the view ");
      reset();
      return viewRect;
    }

    Rect shiftedRect = r.copy();
    // check shifting in x
    if( r.left < 0){
      float dif = r.left; // generates a -ve num
      shiftedRect.left = 0;
      shiftedRect.right = r.right - dif; // shifts to the right
    }
    if( r.right > maxRight){
      float dif = maxRight - r.right; // generates a -ve num
      shiftedRect.left = r.left + dif; // shifts to the left
      shiftedRect.right = maxRight;
    }
    
    if( r.top < 0){
      float dif = r.top; // generates a -ve num
      shiftedRect.top = 0;
      shiftedRect.bottom = r.bottom - dif; // shifts down (+veley)
    }
    if( r.bottom > maxBottom){
      float dif = maxBottom - r.bottom; // generates a -ve num
      shiftedRect.top = r.top + dif; // shifts up (-veley)
      shiftedRect.bottom = maxBottom;
    }
    
    return shiftedRect;
    
  }
  
  
  float limitScale(float s){
    float lscale = MOMaths.constrain(s,0.01f,1f);
    if(lscale > 0.9){ lscale = 1.0f;}
    return lscale;
  }
  
 
 
  // called from UI
  //
  //
  public void keyboardZoom(KeyEvent e){
    //println("keyboard zoom", theKey, theKeyCode);
    // - + zoom keys
	
    if(e.getKeyChar() == '-'){
      // this is the - key (zoom out)
      zoom(-1);
      
    }
    if(e.getKeyChar() == '='){
      // this is the + key (zoom in)
      zoom(1);
      
     }
     
    if(e.getKeyCode() == KeyEvent.VK_LEFT){
      // track left
      shiftXY(-0.2f,0f);
    }
    
    if( e.getKeyCode() == KeyEvent.VK_UP ){
      // track up
      shiftXY(0f,-0.2f);
    }
    
    if(e.getKeyCode() == KeyEvent.VK_RIGHT){
      // track right
      shiftXY(0.2f,0);
    }
    
    if(e.getKeyCode() == KeyEvent.VK_DOWN){
      // track down
      shiftXY(0f,0.2f);
    }
    
    System.out.println("new view rect " + viewRect.left + ", " + viewRect.top + ", " + viewRect.getWidth() + ", " + viewRect.getHeight());
    
  }
  

  
  
}
