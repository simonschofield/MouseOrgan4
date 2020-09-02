import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;


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
	ShapeDrawer drawer;
	ArrayList<DrawnShape> canvasOverlayShapes = new ArrayList<DrawnShape>();
	
	public SimpleUI(Surface surface) {
		UIManagerName = "";

		parentSurface = surface;
	}

	void setGraphicsContext(Graphics2D g2d) {
		drawer = new ShapeDrawer(g2d);
		Widget.setParent(this);
	}

	void handleMouseEvent(MouseEvent me, String mouseEventType) {

		int x = me.getX();
		int y = me.getY();
		handleMouseEvent(mouseEventType, x, y);
	}

	void handleMouseEvent(String mouseEventType, int x, int y) {
		checkForCanvasEvent(mouseEventType, x, y);
		for (Widget w : widgetList) {
			w.handleMouseEvent(mouseEventType, x, y);
		}

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

	void setFileDialogTargetDirectory(String dir) {
		filaDialogTargetDirectory = dir;
	}
	
	public void openFileSaveDialog(String prompt) {

		final JFileChooser fc = new JFileChooser(filaDialogTargetDirectory);
		String suggestedName = MOUtils.getDateStampedImageFileName("savedImage");
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

		canvasRect = new Rect(x, y, x + w, y + h);
	}

	public void checkForCanvasEvent(String mouseEventType, int x, int y){
       if(canvasRect==null) return;
       if(   canvasRect.isPointInside(x,y)) {
         UIEventData uied = new UIEventData(UIManagerName, "canvas" , "canvas", mouseEventType,x,y);
         uied.canvasX = (int) (x - canvasRect.left);
         uied.canvasY = (int) (y - canvasRect.top);
         
	     //the document space location of the canvas event
	     uied.docSpacePt = canvasCoordToDocSpace(uied.canvasX, uied.canvasY);
         handleUIEvent(uied);
       }

    }
	
	
	PVector canvasCoordToDocSpace(int x, int y) {
		// the canvas is always set to be the same aspect as the document, so it can therefore calculate the
		// doc space point 
		
		// get the point as normized canvas space ...
		float nx = x / canvasRect.getWidth();
		float ny = y / canvasRect.getHeight();
		
		PVector normalizedCanvasPoint = new PVector(nx,ny);

		// ... then find where this is within the current view rect
	    Rect zoomRectDocSpace = parentSurface.theViewControl.getZoomRectDocSpace();	
	    PVector docSpace = zoomRectDocSpace.interpolate(normalizedCanvasPoint);
	    return docSpace;
	}
	
	PVector docSpaceToCanvasCoord(PVector docSpace) {
		
		Rect zoomRectDocSpace = parentSurface.theViewControl.getZoomRectDocSpace();	
		// this calculates how much you are "through" the rect in x and y
		PVector normalised = zoomRectDocSpace.norm(docSpace);
		
		float nx = (normalised.x * canvasRect.getWidth()) + canvasRect.left;
		float ny = (normalised.y * canvasRect.getHeight()) + + canvasRect.top;
		
		// now clamp them to legal values
		nx = MOMaths.constrain(nx,  canvasRect.left,  canvasRect.right);
		ny = MOMaths.constrain(ny,  canvasRect.top,  canvasRect.bottom);
		
		
		
		return new PVector(nx,ny);
		
	}
	

	
	
	public void drawCanvas() {
		if (canvasRect == null)
			return;
		drawer.setDrawingStyle(new Color(0, 0, 0, 0), new Color(0, 0, 0, 255), 1);
		drawer.drawRect(canvasRect.left, canvasRect.top, canvasRect.getWidth(), canvasRect.getHeight());
		
		for(DrawnShape ds: canvasOverlayShapes) {
			drawer.drawDrawnShape(ds);
		}
		
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////
	// canvas overlay shape methods
	//
	void clearCanvasOverlayShapes() {
		canvasOverlayShapes.clear();
	}
	
	
	void deleteCanvasOverlayShapes(String id) {
		
		ArrayList<DrawnShape> tempShapes = new ArrayList<DrawnShape>();
		for(DrawnShape s: canvasOverlayShapes) {
			if( s.isID(id) ) continue;
			tempShapes.add(s);
		}
		canvasOverlayShapes = tempShapes;
		
	}
	
	void addCanvasOverlayShape(String idName, PVector docPt1, PVector docPt2, String shapeType, Color fillC, Color lineC, int lineWt) {
		PVector canvasPt1 = docSpaceToCanvasCoord(docPt1);
		PVector canvasPt2 = docSpaceToCanvasCoord(docPt2);
		DrawnShape ds = new DrawnShape();
		ds.setShape(canvasPt1.x, canvasPt1.y, canvasPt2.x, canvasPt2.y, shapeType, fillC, lineC, lineWt);
		
		addCanvasOverlayShape(ds,idName);
	}
	
	void addCanvasOverlayText(String idName, PVector docPt1, String content,  Color textColor, int txtSz) {
		PVector canvasPt1 = docSpaceToCanvasCoord(docPt1);
		DrawnShape ds = new DrawnShape();
		ds.setTextShape(canvasPt1.x, canvasPt1.y, content, textColor, txtSz);

		addCanvasOverlayShape(ds,idName);
	}
	
	void addCanvasOverlayShape(DrawnShape ds, String id) {
		ds.setID(id);
		canvasOverlayShapes.add(ds);
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
		backgroundRect = new Rect(left, top, right, bottom);
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

	void update() {

		if (backgroundRect != null) {
			drawer.setDrawingStyle(backgroundRectColor, backgroundRectColor, 1);
			drawer.drawRect(backgroundRect.left, backgroundRect.top, backgroundRect.getWidth(),
					backgroundRect.getHeight());
		}

		drawCanvas();
		for (Widget w : widgetList) {
			w.drawMe(drawer);
		}

	}

	void clearAll() {
		widgetList = new ArrayList<Widget>();
	}

}// end of SimpleUIManager

//////////////////////////////////////////////////////////////////
// UIEventData
// when a UI component calls the simpleUICallback() function, it passes this object back
// which contains EVERY CONCEIVABLE bit of extra information about the event that you could imagine
//
class UIEventData {
	// set by the constructor
	public String callingUIManager; // this is the name of the UIManager, because you might have more than one
	public String uiComponentType; // this is the type of widet e.g. ButtonBaseClass, ToggleButton, Slider - it is
									// identical to the class name
	public String uiLabel; // this is the unique shown label for each widget, and is used to idetify the
							// calling widget
	public String mouseEventType;
	public int mousex; // this is the x location of the recieved mouse event, in window space
	public int mousey;

	// extra stuff, which is specific to particular widgets
	public boolean toggleSelectState = false;
	public String radioGroupName = "";
	public String menuItem = "";
	public float sliderValue = 0.0f;
	public String fileDialogPrompt = "";
	public String fileSelection = "";

	// the pixel location relative to the canvas, not the application
	public int canvasX;
	public int canvasY;
	// the document space location of the canvas event
	public PVector docSpacePt;
	

	// key press and text content information for text widgets
	public char keyPress;
	public String textContent;

	public UIEventData() {
	}

	public UIEventData(String uiname, String thingType, String label, String mouseEvent, int x, int y) {
		initialise(uiname, thingType, label, mouseEvent, x, y);

	}

	void initialise(String uiname, String thingType, String label, String mouseEvent, int x, int y) {

		callingUIManager = uiname;
		uiComponentType = thingType;
		uiLabel = label;
		mouseEventType = mouseEvent;
		mousex = x;
		mousey = y;

	}

	boolean eventIsFromWidget(String lab) {
		if (uiLabel.equals(lab))
			return true;
		return false;

	}

	void print(int verbosity) {
		if (verbosity != 3 && this.mouseEventType.equals("mouseMoved"))
			return;
		if (verbosity == 0)
			return;

		if (verbosity >= 1) {
			System.out.println("UIEventData:" + this.uiComponentType + " " + this.uiLabel);

			if (this.uiComponentType.equals("canvas")) {
				System.out.println(
						"mouse event:" + this.mouseEventType + " at (" + this.mousex + "," + this.mousey + ")");
			}
		}

		if (verbosity >= 2) {
			System.out.println("toggleSelectState " + this.toggleSelectState);
			System.out.println("radioGroupName " + this.radioGroupName);
			System.out.println("sliderValue " + this.sliderValue);
			System.out.println("menuItem " + this.menuItem);
			System.out.println("keyPress " + keyPress);
			System.out.println("textContent " + textContent);
			System.out.println("fileDialogPrompt " + this.fileDialogPrompt);
			System.out.println("fileSelection " + this.fileSelection);
			// add others as they come along....
		}

		if (verbosity == 3) {
			if (this.mouseEventType.equals("mouseMoved")) {
				System.out.println("mouseMove at (" + this.mousex + "," + this.mousey + ")");
			}
		}

		System.out.println(" ");
	}

}

//////////////////////////////////////////////////////////////////
// Everything below here is stuff wrapped up by the UImanager class
// so you don't need to to look at it, or use it directly. But you can if you
// want to!
// 

//////////////////////////////////////////////////////////////////
// Base class to all components

class Widget {
	static SimpleUI parentManager;

	// Color for overall application
	Color SimpleUIAppBackgroundColor = new Color(240, 240, 240);// the light neutralgrey of the overall application
																// surrounds

	// Color for UI components
	Color SimpleUIBackgroundRectColor = new Color(230, 230, 240); // slightly purpley background rect Color for
																	// alternative UI's
	Color SimpleUIWidgetFillColor = new Color(200, 200, 200);// darker grey for butttons
	Color SimpleUIWidgetRolloverColor = new Color(215, 215, 215);// slightly lighter rollover Color
	Color SimpleUITextColor = new Color(0, 0, 0);

	// can be used to memorise the mouse loc
	int mouseX, mouseY;

	// Because you can have more than one UIManager in a system,
	// e.g. a seperate one for popups, or tool modes
	String UIManagerName;

	// this should be the best way to identify a widget, so make sure
	// that all UILabels are unique
	String UILabel;

	// type of component e.g. "UIButton", should be absolutely same as class name
	public String UIComponentType = "WidgetBaseClass";

	// location and size of widget
	int widgetWidth, widgetHeight;
	int locX, locY;
	public Rect bounds;

	// needed by most, but not all widgets
	boolean rollover = false;

	// needed by some widgets but not all
	boolean selected = false;

	public Widget(String uiname) {

		UIManagerName = uiname;
	}

	public Widget(String uiname, String uilabel, int x, int y, int w, int h) {

		UIManagerName = uiname;
		UILabel = uilabel;
		setBounds(x, y, w, h);
	}

	static void setParent(SimpleUI p) {
		parentManager = p;
	}

	// virtual functions
	//
	public void setBounds(int x, int y, int w, int h) {
		locX = x;
		locY = y;
		widgetWidth = w;
		widgetHeight = h;
		bounds = new Rect(x, y, x + w, y + h);
	}

	public boolean isInMe(int x, int y) {
		if (bounds.isPointInside(x, y))
			return true;
		return false;
	}

	public void setParentManager(SimpleUI manager) {
		parentManager = manager;
	}

	public void setWidgetDims(int w, int h) {
		setBounds(locX, locY, w, h);
	}

	// "virtual" functions here
	//
	public void drawMe(ShapeDrawer drawer) {
	}

	public void handleMouseEvent(String mouseEventType, int x, int y) {
	}

	void handleKeyEvent(char k, int kcode, String keyEventType) {
	}

	void setSelected(boolean s) {
		selected = s;
	}

}

//////////////////////////////////////////////////////////////////
// Simple Label widget - uneditable text
// It displays label:text, where text is changeable in the widget's lifetime, but label is not

class SimpleLabel extends Widget {

	int textPad = 5;
	String text;
	int textSize = 8;
	boolean displayLabel = true;

	public SimpleLabel(String uiname, String uilable, int x, int y, String txt) {
		super(uiname, uilable, x, y, 60, 20);
		UIComponentType = "SimpleLabel";
		this.text = txt;

	}

	public void drawMe(ShapeDrawer drawer) {

		drawer.setDrawingStyle(SimpleUIBackgroundRectColor, new Color(100, 100, 100, 255), 1);
		drawer.drawRect(locX, locY, widgetWidth, widgetHeight);

		String seperator = ":";
		if (this.text.equals(""))
			seperator = " ";
		String displayString;

		if (displayLabel) {

			displayString = this.UILabel + seperator + this.text;

		} else {

			displayString = this.text;
		}

		if (displayString.length() < 20) {
			textSize = 8;
		} else {
			textSize = 7;
		}
		drawer.setFillColor(SimpleUITextColor);
		drawer.setTextStyle(textSize);
		drawer.drawText(displayString, locX + textPad, locY + textPad);

	}

	void setText(String txt) {
		this.text = txt;
	}

	String getText() {
		return this.text;
	}

}

//////////////////////////////////////////////////////////////////
// Base button class, functions as a simple button, and is the base class for
// toggle and radio buttons
class ButtonBaseClass extends Widget {

	int textPad = 2;
	int textSize = 8;

	public ButtonBaseClass(String uiname, int x, int y, String uilable) {
		super(uiname, uilable, x, y, 60, 20);

		UIComponentType = "ButtonBaseClass";
	}

	public void setButtonDims(int w, int h) {
		setBounds(locX, locY, w, h);
	}

	public void handleMouseEvent(String mouseEventType, int x, int y) {
		if (isInMe(x, y) && (mouseEventType.equals("mouseMoved") || mouseEventType.equals("mousePressed"))) {
			rollover = true;
		} else {
			rollover = false;
		}

		if (isInMe(x, y) && mouseEventType.equals("mouseReleased")) {
			UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x, y);
			parentManager.handleUIEvent(uied);
		}

	}

	public void drawMe(ShapeDrawer drawer) {

		if (rollover) {
			drawer.setDrawingStyle(SimpleUIWidgetRolloverColor, new Color(0, 0, 0, 255), 1);
		} else {
			drawer.setDrawingStyle(SimpleUIWidgetFillColor, new Color(0, 0, 0, 255), 1);
		}

		drawer.drawRect(locX, locY, widgetWidth, widgetHeight);
		drawer.setFillColor(SimpleUITextColor);
		if (this.UILabel.length() < 10) {
			textSize = 8;
		} else {
			textSize = 7;
		}

		drawer.setFillColor(SimpleUITextColor);
		drawer.setTextStyle(textSize);
		drawer.drawText(this.UILabel, locX + textPad, locY + textPad + textSize);

	}

}

//////////////////////////////////////////////////////////////////
// ToggleButton

class ToggleButton extends ButtonBaseClass {

	public ToggleButton(String uiname, int x, int y, String labelString) {
		super(uiname, x, y, labelString);

		UIComponentType = "ToggleButton";
	}

	public void handleMouseEvent(String mouseEventType, int x, int y) {
		if (isInMe(x, y) && (mouseEventType.equals("mouseMoved") || mouseEventType.equals("mousePressed"))) {
			rollover = true;
		} else {
			rollover = false;
		}

		if (isInMe(x, y) && mouseEventType.equals("mouseReleased")) {
			swapSelectedState();
			UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x, y);
			uied.toggleSelectState = selected;
			parentManager.handleUIEvent(uied);
		}

	}

	public void swapSelectedState() {
		selected = !selected;
	}

	public void drawMe(ShapeDrawer drawer) {
		if (rollover) {
			drawer.setDrawingStyle(SimpleUIWidgetRolloverColor, new Color(0, 0, 0, 255), 1);
		} else {
			drawer.setDrawingStyle(SimpleUIWidgetFillColor, new Color(0, 0, 0, 255), 1);
		}

		if (selected) {
			drawer.setStrokeWeight(2);
			drawer.drawRect(locX + 1, locY + 1, widgetWidth - 2, widgetHeight - 2);
		} else {
			drawer.setStrokeWeight(2);
			drawer.drawRect(locX, locY, widgetWidth, widgetHeight);
		}

		drawer.setFillColor(SimpleUITextColor);
		drawer.setTextStyle(textSize);
		drawer.drawText(this.UILabel, locX + textPad, locY + textPad + textSize);
	}

}

//////////////////////////////////////////////////////////////////
// RadioButton

class RadioButton extends ToggleButton {

	// these have to be part of the base class as is accessed by manager
	public String radioGroupName = "";

	public RadioButton(String uiname, int x, int y, String labelString, String groupName, SimpleUI manager) {
		super(uiname, x, y, labelString);
		radioGroupName = groupName;
		UIComponentType = "RadioButton";
		parentManager = manager;
	}

	public void handleMouseEvent(String mouseEventType, int x, int y) {
		if (isInMe(x, y) && (mouseEventType.equals("mouseMoved") || mouseEventType.equals("mousePressed"))) {
			rollover = true;
		} else {
			rollover = false;
		}

		if (isInMe(x, y) && mouseEventType.equals("mouseReleased")) {

			parentManager.setRadioButtonOff(this.radioGroupName);
			selected = true;
			UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x, y);
			uied.toggleSelectState = selected;
			uied.radioGroupName = this.radioGroupName;
			parentManager.handleUIEvent(uied);
		}

	}

	public void turnOff(String groupName) {
		if (groupName.equals(radioGroupName)) {
			selected = false;
		}

	}

}

/////////////////////////////////////////////////////////////////////////////
// menu stuff
//
//

/////////////////////////////////////////////////////////////////////////////
// the menu class
//
class Menu extends Widget {

	int textPad = 2;
	// String title;
	int textSize = 8;

	int numItems = 0;
	SimpleUI parentManager;
	public boolean visible = false;

	ArrayList<String> itemList = new ArrayList<String>();

	public Menu(String uiname, String uilabel, int x, int y, String[] menuItems, SimpleUI manager) {
		super(uiname, uilabel, x, y, 60, 20);
		parentManager = manager;
		UIComponentType = "Menu";

		for (String s : menuItems) {
			itemList.add(s);
			numItems++;
		}
	}

	public void drawMe(ShapeDrawer drawer) {
		// println("drawing menu " + title);
		drawTitle(drawer);
		if (visible) {
			drawItems(drawer);
		}

	}

	void drawTitle(ShapeDrawer drawer) {

		if (rollover) {
			drawer.setDrawingStyle(SimpleUIWidgetRolloverColor, new Color(0, 0, 0, 255), 1);
		} else {
			drawer.setDrawingStyle(SimpleUIWidgetFillColor, new Color(0, 0, 0, 255), 1);
		}

		drawer.drawRect(locX, locY, widgetWidth, widgetHeight);
		drawer.setFillColor(SimpleUITextColor);
		drawer.setTextStyle(textSize);

		drawer.drawText(this.UILabel, locX + textPad, locY + textPad + textSize, widgetWidth, widgetHeight);

	}

	void drawItems(ShapeDrawer drawer) {
		if (rollover) {
			drawer.setDrawingStyle(SimpleUIWidgetRolloverColor, new Color(0, 0, 0, 255), 1);
		} else {
			drawer.setDrawingStyle(SimpleUIWidgetFillColor, new Color(0, 0, 0, 255), 1);
		}

		int thisY = locY + widgetHeight;
		drawer.drawRect(locX, thisY, widgetWidth, (widgetHeight * numItems));

		if (isInItems(mouseX, mouseY)) {
			hiliteItem(drawer, mouseY);
		}

		drawer.setFillColor(SimpleUITextColor);

		drawer.setTextStyle(textSize);

		for (String s : itemList) {

			if (s.length() > 14) {
				drawer.setTextStyle(textSize - 1);
			} else {
				drawer.setTextStyle(textSize);
			}

			drawer.drawText(s, locX + textPad, thisY + textPad + textSize, widgetWidth, widgetHeight);
			thisY += widgetHeight;
		}

	}

	void hiliteItem(ShapeDrawer drawer, int y) {

		int topOfItems = this.locY + widgetHeight;
		float distDown = y - topOfItems;
		int itemNum = (int) distDown / widgetHeight;
		Color cc = drawer.getFillColor();
		drawer.setFillColor(new Color(230, 210, 210));

		drawer.drawRect(locX, topOfItems + itemNum * widgetHeight, widgetWidth, widgetHeight);
		drawer.setFillColor(cc);
	}

	public void handleMouseEvent(String mouseEventType, int x, int y) {
		rollover = false;
		mouseX = x;
		mouseY = y;
		// println("here1 " + mouseEventType);
		if (isInMe(x, y) == false) {
			visible = false;
			return;
		}
		if (isInMe(x, y)) {
			rollover = true;
		}

		// println("here2 " + mouseEventType);
		if (mouseEventType.equals("mousePressed") && visible == false) {
			// println("mouseclick in title of " + title);
			parentManager.setMenusOff();
			visible = true;
			rollover = true;
			return;
		}
		if (mouseEventType.equals("mousePressed") && isInItems(x, y)) {
			String pickedItem = getItem(y);

			UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x, y);
			uied.menuItem = pickedItem;
			parentManager.handleUIEvent(uied);

			parentManager.setMenusOff();

			return;
		}
	}

	String getItem(int y) {
		int topOfItems = this.locY + widgetHeight;
		float distDown = y - topOfItems;
		int itemNum = (int) distDown / widgetHeight;
		// println("picked item number " + itemNum);
		return itemList.get(itemNum);
	}

	public boolean isInMe(int x, int y) {
		if (isInTitle(x, y)) {
			// println("mouse in title of " + title);
			return true;
		}
		if (isInItems(x, y)) {
			return true;
		}
		return false;
	}

	boolean isInTitle(int x, int y) {
		if (x >= this.locX && x < this.locX + this.widgetWidth && y >= this.locY && y < this.locY + this.widgetHeight)
			return true;
		return false;

	}

	boolean isInItems(int x, int y) {
		if (visible == false)
			return false;
		if (x >= this.locX && x < this.locX + this.widgetWidth && y >= this.locY + this.widgetHeight
				&& y < this.locY + (this.widgetHeight * (this.numItems + 1)))
			return true;

		return false;
	}

}// end of menu class

/////////////////////////////////////////////////////////////////////////////
// Slider class stuff

/////////////////////////////////////////////////////////////////////////////
// Slider Class
//
// calls back with value on  both release and drag

class Slider extends Widget {

	public float currentValue = 0.0f;
	boolean mouseEntered = false;
	int textPad = 5;
	int textSize = 8;
	boolean rollover = false;

	public String HANDLETYPE = "ROUND";

	public Slider(String uiname, String label, int x, int y) {
		super(uiname, label, x, y, 70, 30);
		UIComponentType = "Slider";
	}

	public void handleMouseEvent(String mouseEventType, int x, int y) {
		PVector p = new PVector(x, y);

		if (mouseLeave(p)) {
			UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x, y);
			uied.sliderValue = currentValue;
			parentManager.handleUIEvent(uied);
			// println("mouse left sider");
		}

		if (bounds.isPointInside(p) == false) {
			mouseEntered = false;
			return;
		}

		if ((mouseEventType.equals("mouseMoved") || mouseEventType.equals("mousePressed"))) {
			rollover = true;
		} else {
			rollover = false;
		}

		if (mouseEventType.equals("mousePressed") || mouseEventType.equals("mouseReleased")
				|| mouseEventType.equals("mouseDragged")) {
			mouseEntered = true;
			float val = getSliderValueAtMousePos(x);
			// println("slider val",val);
			setSliderValue(val);
			UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x, y);
			uied.sliderValue = val;
			parentManager.handleUIEvent(uied);
		}

	}

	float getSliderValueAtMousePos(int pos) {
		float val = MOMaths.map(pos, bounds.left, bounds.right, 0, 1);
		return val;
	}

	float getSliderValue() {
		return currentValue;
	}

	void setSliderValue(float val) {
		currentValue = MOMaths.constrain(val, 0, 1);
	}

	boolean mouseLeave(PVector p) {
		// is only true, if the mouse has been in the widget, has been depressed
		if (mouseEntered && bounds.isPointInside(p) == false) {
			mouseEntered = false;
			return true;
		}

		return false;
	}

	public void drawMe(ShapeDrawer drawer) {
		if (rollover) {
			drawer.setDrawingStyle(SimpleUIWidgetRolloverColor, new Color(0, 0, 0, 255), 1);
		} else {
			drawer.setDrawingStyle(SimpleUIWidgetFillColor, new Color(0, 0, 0, 255), 1);
		}

		drawer.drawRect(bounds.left, bounds.top, bounds.getWidth(), bounds.getHeight());
		drawer.setFillColor(SimpleUITextColor);
		drawer.setTextStyle(textSize);
		drawer.drawText(this.UILabel, (int) (bounds.left + textPad), (int) (bounds.top + 26));
		int sliderHandleLocX = (int) MOMaths.map(currentValue, 0, 1, bounds.left, bounds.right);
		sliderHandleLocX = (int) MOMaths.constrain(sliderHandleLocX, bounds.left + 10, bounds.right - 10);
		drawer.setStrokeColor(new Color(127, 127, 127));
		float lineHeight = bounds.top + (bounds.getHeight() / 2.0f) - 5f;
		drawer.drawLine(bounds.left + 5, lineHeight, bounds.left + bounds.getWidth() - 5, lineHeight);
		drawer.setStrokeColor(new Color(0, 0, 0));
		drawSliderHandle(drawer, sliderHandleLocX);

	}

	void drawSliderHandle(ShapeDrawer drawer, int loc) {
		drawer.setDrawingStyle(new Color(255, 255, 255, 100), new Color(0, 0, 0, 255), 1);
		drawer.drawEllipse(loc, bounds.top + 5, 10, 10);
	}

}

////////////////////////////////////////////////////////////////////////////////
// self contained simple txt ox input
// simpleUICallback is called after every character insertion/deletion, enabling immediate udate of the system
//
class TextInputBox extends Widget {
	String contents = "";
	int maxNumChars = 14;

	boolean rollover;

	Color textBoxBackground = new Color(235, 235, 255);

	public TextInputBox(String uiname, String uilabel, int x, int y, int maxNumChars) {
		super(uiname, uilabel, x, y, 100, 30);
		UIComponentType = "TextInputBox";
		this.maxNumChars = maxNumChars;

		rollover = false;

	}

	public void handleMouseEvent(String mouseEventType, int x, int y) {
		// can only type into an input box if the mouse is hovering over
		// this way we avoid sending text input to multiple widgets
		PVector mousePos = new PVector(x, y);
		rollover = bounds.isPointInside(mousePos);

	}

	void handleKeyEvent(char k, int kcode, String keyEventType) {
		if (keyEventType.equals("released"))
			return;
		if (rollover == false)
			return;

		UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, "textInputEvent", 0, 0);
		uied.keyPress = k;

		if (isValidCharacter(k)) {
			addCharacter(k);
		}

		// if(k == BACKSPACE){
		// deleteCharacter();
		// }

		parentManager.handleUIEvent(uied);
	}

	void addCharacter(char k) {
		if (contents.length() < this.maxNumChars) {
			contents = contents + k;

		}

	}

	void deleteCharacter() {
		int l = contents.length();
		if (l == 0)
			return; // string already empty
		if (l == 1) {
			contents = "";
		} // delete the final character
		String cpy = contents.substring(0, l - 1);
		contents = cpy;

	}

	boolean isValidCharacter(char k) {
		// e.getKeyChar() == KeyEvent.VK_BACKSPACE;
		// if(k == BACKSPACE) return false;
		return true;

	}

	String getText() {
		return contents;
	}

	void setText(String s) {
		contents = s;
	}

	public void drawMe(ShapeDrawer drawer) {

		if (rollover) {
			drawer.setDrawingStyle(SimpleUIWidgetRolloverColor, new Color(255, 0, 0), 1);
		} else {
			drawer.setDrawingStyle(textBoxBackground, new Color(0, 0, 0), 1);
		}

		drawer.drawRect(locX, locY, widgetWidth, widgetHeight);

		drawer.setDrawingStyle(SimpleUITextColor, new Color(0, 0, 0), 1);
		int textPadX = 5;
		int textPadY = 20;
		drawer.setTextStyle(8);
		drawer.drawText(contents, locX + textPadX, locY + textPadY);
		drawer.drawText(UILabel, locX + widgetWidth + textPadX, locY + textPadY);

	}
}
