package MOSimpleUI;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JFileChooser;

import MOApplication.Surface;
import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Rect;
import MOUtils.MOStringUtils;
import MOVectorGraphics.VectorShape;
import MOVectorGraphics.VectorShapeDrawer;


// SimpleUI_Classes version 4.0
// Started Dec 12th 2018
// This update November 2019
// Simon Schofield
// Totally self contained by using a rectangle class called UIRect

//////////////////////////////////////////////////////////////////
// SimpleUIManager() is the only class you have to  create in your 
// application to build the UI. 
// With it you can add buttons (simple only at the moment,  toggle and radio groups coming later)
// and Menus. later release will have text Input and Output, Canvas Widgets and FileIO dialogs
// Later still - sliders and Color pickers.
//
//
// You need to pass all the mouse events into the SimpleUIManager
// e.g. 
// void mousePressed(){ uiManager.handleMouseEvent("mousePressed",mouseX,mouseY); }
// and for all the other mouse actions
//
// Once a mouse event has been received by a UI item (button, menu etc) it calls a function called
// simpleUICallback(...) which you have to include in the 
// main part of the project (below setup() and draw() etc.)
//
// Also, you need to call uiManager.drawMe() in the main draw() function
//

public class SimpleUI {

	
	Surface parentSurface;

	Rect canvasRect;

	ArrayList<Widget> widgetList = new ArrayList<Widget>();

	String UIManagerName;

	Rect backgroundRect = null;
	Color backgroundRectColor;

	String fileDialogPrompt = "";
	String filaDialogTargetDirectory = "c:\\Users\\cmp3schofs\\Desktop";
	VectorShapeDrawer drawer;
	ArrayList<VectorShape> canvasOverlayShapes = new ArrayList<VectorShape>();
	
	public SimpleUI(Surface surface) {
		UIManagerName = "";

		parentSurface = surface;
	}

	public void setGraphicsContext(Graphics2D g2d) {
		
		// this bit of weird code anti-aliases the text
		Map<?, ?> desktopHints =  (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
		if (desktopHints != null) {
			g2d.setRenderingHints(desktopHints);
		}
		
		drawer = new VectorShapeDrawer(g2d);
		Widget.setParent(this);
	}

	public void handleMouseEvent(MouseEvent me, String mouseEventType) {

		int x = me.getX();
		int y = me.getY();
		handleMouseEvent(mouseEventType, x, y);
	}

	void handleMouseEvent(String mouseEventType, int x, int y) {
		
		// this avoids any concurrency issues if a button press results in a removal of a widget
		// also, widgets get priority over canvas events (open menus over the canvas region)
		Widget receivingWidget = null;
		for (Widget w : widgetList) {
			if(w.isInMe(x, y)) {
				receivingWidget = w;
			}	
	    }
		if(receivingWidget != null) {
			receivingWidget.handleMouseEvent(mouseEventType, x, y);
			return;
		}
		
		checkForCanvasEvent(mouseEventType, x, y);
		

	}

	void handleKeyEvent(char k, int kcode, String keyEventType) {
		for (Widget w : widgetList) {
			w.handleKeyEvent(k, kcode, keyEventType);
		}
	}

	void handleUIEvent(UIEventData uied) {
		parentSurface.handleUIEvent(uied);
	}

	////////////////////////////////////////////////////////////////////////////
	// file save dialogue
	//

	public void setFileDialogTargetDirectory(String dir) {
		filaDialogTargetDirectory = dir;
	}
	
	public void openFileSaveDialog(String prompt) {

		final JFileChooser fc = new JFileChooser(filaDialogTargetDirectory);
		String suggestedName = MOStringUtils.getDateStampedImageFileName("savedImage");
		fc.setSelectedFile(new File(suggestedName));
		int returnVal = fc.showSaveDialog(parentSurface.parentApp);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			UIEventData uied = new UIEventData(UIManagerName, "fileSaveDialog", "fileSaveDialog", "mouseReleased", 0,
					0);
			uied.fileSelection = file.getPath();
			uied.fileDialogPrompt = prompt;
			handleUIEvent(uied);
		} else {
			// log.append("Open command cancelled by user." + newline);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// canvas creation and stuff
	//
	public void addCanvas(int x, int y, int w, int h) {

		canvasRect = new Rect(x, y, w, h);
	}

	public boolean checkForCanvasEvent(String mouseEventType, int x, int y){
       if(canvasRect==null) return false;
       if(   canvasRect.isPointInside(x,y)) {
         UIEventData uied = new UIEventData(UIManagerName, "canvas" , "canvas", mouseEventType,x,y);
         uied.canvasX = (int) (x - canvasRect.left);
         uied.canvasY = (int) (y - canvasRect.top);
         
	     //the document space location of the canvas event
	     uied.docSpacePt = canvasCoordToDocSpace(uied.canvasX, uied.canvasY);
         handleUIEvent(uied);
         return true;
       }
       return false;
    }
	
	
	PVector canvasCoordToDocSpace(int x, int y) {

	    return parentSurface.theViewControl.appWindowCoordinateToDocSpace(x,y);
	   
	}
	
	PVector docSpaceToCanvasCoord(PVector docSpace) {
		
		
		PVector canvasPt = parentSurface.theViewControl.docSpaceToAppWindowCoordinate(docSpace);

		// now clamp them to legal values
		//float nx = MOMaths.constrain(canvasPt.x,  canvasRect.left,  canvasRect.right);
		//float ny = MOMaths.constrain(canvasPt.y,  canvasRect.top,  canvasRect.bottom);

		//return new PVector(nx,ny);
		return canvasPt;
	}
	

	
	
	public void drawCanvas() {
		if (canvasRect == null)
			return;
		
		// probably draw the canvas fill colour here
		// draw over stuff surrounding the canvas
		drawer.setDrawingStyle(new Color(0, 0, 0, 0), new Color(0, 0, 0, 255), 1);
		drawer.drawRect(canvasRect.left, canvasRect.top, canvasRect.getWidth(), canvasRect.getHeight());
		
		
		for(VectorShape ds: canvasOverlayShapes) {
			drawCanvasOverlayShape(ds);
		}
		
		
		
	}
	
	
	void drawUISurrounds() {
		// now draw "surrounds"
		int appWidth = parentSurface.windowWidth();
		int appHeight = parentSurface.windowHeight();

	    
	    // left, top, right, bottom 
		drawer.setDrawingStyle(new Color(200, 200, 200, 255), new Color(0, 0, 0, 0), 0);
		drawer.drawRect(0,0, canvasRect.left, appHeight);
		drawer.drawRect(canvasRect.left,0, canvasRect.getWidth(), canvasRect.top);
		drawer.drawRect(canvasRect.right,0, appWidth-canvasRect.left, appHeight);
		drawer.drawRect(canvasRect.left, canvasRect.bottom,  canvasRect.getWidth(), appHeight-canvasRect.getHeight());
	   
		// fine line round the outside of canvas rect
		drawer.setDrawingStyle(new Color(0, 0, 0, 0), new Color(0, 0, 0, 255), 1);
		drawer.drawRect(canvasRect.left, canvasRect.top, canvasRect.getWidth(), canvasRect.getHeight());
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// canvas overlay shape methods
	//
	void clearCanvasOverlayShapes() {
		canvasOverlayShapes.clear();
	}
	
	
	public void deleteCanvasOverlayShapes(String id) {
		
		ArrayList<VectorShape> tempShapes = new ArrayList<VectorShape>();
		for(VectorShape s: canvasOverlayShapes) {
			if( s.isID(id) ) continue;
			tempShapes.add(s);
		}
		canvasOverlayShapes = tempShapes;
		
	}
	
	public void addCanvasOverlayShape(String idName, PVector docPt1, PVector docPt2, String shapeType, Color fillC, Color lineC, int lineWt) {
		//System.out.println("addShape idName" + idName + " docPt1 " + docPt1 + " docPt2 " + docPt2 + " shapeType " + shapeType );
		VectorShape ds = new VectorShape();
		ds.setShape(docPt1.x, docPt1.y, docPt2.x, docPt2.y, shapeType, fillC, lineC, lineWt);
		
		addCanvasOverlayShape(ds,idName);
		
	}
	
	public void addCanvasOverlayText(String idName, PVector docPt1, String content,  Color textColor, int txtSz) {
		
		VectorShape ds = new VectorShape();
		ds.setTextShape(docPt1.x, docPt1.y, content, textColor, txtSz);

		addCanvasOverlayShape(ds,idName);
	}
	
	void addCanvasOverlayShape(VectorShape ds, String id) {
		ds.setID(id);
		canvasOverlayShapes.add(ds);
	}
	

	void drawCanvasOverlayShape(VectorShape ds) {
		// converts a doc space shape to a canvas space shape via the view controller then draws it
		PVector canvasPt1 = parentSurface.theViewControl.docSpaceToAppWindowCoordinate(ds.p1);
		PVector canvasPt2 = parentSurface.theViewControl.docSpaceToAppWindowCoordinate(ds.p2);
		VectorShape viewScaledShape = new VectorShape();
		if(ds.shapeType.contentEquals("text")) {
			viewScaledShape.setTextShape(canvasPt1.x, canvasPt1.y, ds.textContent, ds.style.fillColor, ds.textSize);
		} else {
			viewScaledShape.setShape(canvasPt1.x, canvasPt1.y, canvasPt2.x, canvasPt2.y, ds.shapeType, ds.style.fillColor, ds.style.strokeColor, ds.style.strokeWeight);
		}
		drawer.drawDrawnShape(viewScaledShape);
	}
	
	////////////////////////////////////////////////////////////////////////////
	// widget creation
	//

	// button creation
	public ButtonBaseClass addPlainButton(String label, int x, int y) {
		ButtonBaseClass b = new ButtonBaseClass(UIManagerName, x, y, label);
		widgetList.add(b);
		return b;
	}

	public ButtonBaseClass addToggleButton(String label, int x, int y) {
		ButtonBaseClass b = new ToggleButton(UIManagerName, x, y, label);
		widgetList.add(b);
		return b;
	}

	public ButtonBaseClass addToggleButton(String label, int x, int y, boolean initialState) {
		ButtonBaseClass b = new ToggleButton(UIManagerName, x, y, label);
		b.selected = initialState;
		widgetList.add(b);
		return b;
	}

	public ButtonBaseClass addRadioButton(String label, int x, int y, String groupID) {
		ButtonBaseClass b = new RadioButton(UIManagerName, x, y, label, groupID, this);
		widgetList.add(b);
		return b;
	}

	// label creation
	public SimpleLabel addLabel(String label, int x, int y, String txt) {
		SimpleLabel sl = new SimpleLabel(UIManagerName, label, x, y, txt);
		widgetList.add(sl);
		return sl;
	}

	// menu creation
	public Menu addMenu(String label, int x, int y, String[] menuItems) {
		Menu m = new Menu(UIManagerName, label, x, y, menuItems, this);
		widgetList.add(m);
		return m;
	}

	// slider creation
	public Slider addSlider(String label, int x, int y) {
		Slider s = new Slider(UIManagerName, label, x, y);
		widgetList.add(s);
		return s;
	}

	// text input box creation
	public TextInputBox addTextInputBox(String label, int x, int y) {
		int maxNumChars = 14;
		TextInputBox tib = new TextInputBox(UIManagerName, label, x, y, maxNumChars);
		widgetList.add(tib);
		return tib;
	}

	public TextInputBox addTextInputBox(String label, int x, int y, String content) {
		TextInputBox tib = addTextInputBox(label, x, y);
		tib.setText(content);
		return tib;
	}

	void removeWidget(String uilabel) {
		Widget w = getWidget(uilabel);
		widgetList.remove(w);
	}

	// getting widget data by lable
	//
	Widget getWidget(String uilabel) {
		for (Widget w : widgetList) {
			if (w.UILabel.equals(uilabel))
				return w;
		}
		System.out.println(" getWidgetByLabel: cannot find widget with label " + uilabel);
		return new Widget(UIManagerName);
	}

	// get toggle state
	public boolean getToggleButtonState(String uilabel) {
		Widget w = getWidget(uilabel);
		if (w.UIComponentType.equals("ToggleButton"))
			return w.selected;
		System.out.println(" getToggleButtonState: cannot find widget with label " + uilabel);
		return false;
	}

	// get selected radio button in a group - returns the label name
	public String getRadioGroupSelected(String groupName) {
		for (Widget w : widgetList) {
			if (w.UIComponentType.equals("RadioButton")) {
				if (((RadioButton) w).radioGroupName.equals(groupName) && w.selected)
					return w.UILabel;
			}
		}
		return "";
	}

	public float getSliderValue(String uilabel) {
		Widget w = getWidget(uilabel);
		if (w.UIComponentType.equals("Slider"))
			return ((Slider) w).getSliderValue();
		return 0;
	}

	public void setSliderValue(String uilabel, float v) {
		Widget w = getWidget(uilabel);
		if (w.UIComponentType.equals("Slider"))
			((Slider) w).setSliderValue(v);

	}

	public String getText(String uilabel) {
		Widget w = getWidget(uilabel);

		if (w.UIComponentType.equals("TextInputBox")) {
			return ((TextInputBox) w).getText();
		}

		if (w.UIComponentType.equals("SimpleLabel")) {
			return ((SimpleLabel) w).getText();
		}
		return "";
	}

	public void setText(String uilabel, String content) {
		Widget w = getWidget(uilabel);
		if (w.UIComponentType.equals("TextInputBox")) {
			((TextInputBox) w).setText(content);
		}
		if (w.UIComponentType.equals("SimpleLabel")) {
			((SimpleLabel) w).setText(content);
		}

	}

	// setting a background Color region for the UI. This is drawn first.
	// to do: this should also set an offset for subsequent placement of the buttons

	void setBackgroundRect(int left, int top, int right, int bottom, int r, int g, int b) {
		backgroundRect = new Rect(new PVector(left, top), new PVector(right, bottom));
		backgroundRectColor = new Color(r, g, b);
	}

	void setRadioButtonOff(String groupName) {
		for (Widget w : widgetList) {
			if (w.UIComponentType.equals("RadioButton")) {
				if (((RadioButton) w).radioGroupName.equals(groupName))
					w.selected = false;
			}
		}
	}

	void setMenusOff() {
		for (Widget w : widgetList) {
			if (w.UIComponentType.equals("Menu")) {
				((Menu) w).visible = false;
			}
		}

	}

	public void update() {

		if (backgroundRect != null) {
			drawer.setDrawingStyle(backgroundRectColor, backgroundRectColor, 1);
			drawer.drawRect(backgroundRect.left, backgroundRect.top, backgroundRect.getWidth(),
					backgroundRect.getHeight());
		}

		drawCanvas();
		drawUISurrounds();
		for (Widget w : widgetList) {
			w.drawMe(drawer);
		}

	}

	void clearAll() {
		widgetList = new ArrayList<Widget>();
	}

}// end of SimpleUIManager














