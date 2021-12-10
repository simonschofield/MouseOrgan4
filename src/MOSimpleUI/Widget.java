package MOSimpleUI;

import java.awt.Color;

import MOMaths.Rect;
import MOVectorGraphics.VectorShapeDrawer;

//////////////////////////////////////////////////////////////////
//Everything below here is stuff wrapped up by the UImanager class
//so you don't need to to look at it, or use it directly. But you can if you
//want to!
//

//////////////////////////////////////////////////////////////////
//Base class to all components

public class Widget {
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

	public static void setParent(SimpleUI p) {
		parentManager = p;
	}

	// virtual functions
	//
	public void setBounds(int x, int y, int w, int h) {
		locX = x;
		locY = y;
		widgetWidth = w;
		widgetHeight = h;
		bounds = new Rect(x, y,  w,  h);
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
	public void drawMe(VectorShapeDrawer drawer) {
	}

	public void handleMouseEvent(String mouseEventType, int x, int y) {
	}

	void handleKeyEvent(char k, int kcode, String keyEventType) {
	}

	void setSelected(boolean s) {
		selected = s;
	}

}