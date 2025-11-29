package MOSimpleUI;

import java.awt.Color;

import MOVectorGraphics.VectorShapeDrawer;

//////////////////////////////////////////////////////////////////
//Base button class, functions as a simple button, and is the base class for
//toggle and radio buttons
public class ButtonBaseClass extends Widget {

	int textPad = 2;
	int textSize = 8;

	public ButtonBaseClass(String uiname, int x, int y, String uilable) {
		super(uiname, uilable, x, y, 60, 20);

		UIComponentType = "ButtonBaseClass";
	}

	public void setButtonDims(int w, int h) {
		setBounds(locX, locY, w, h);
	}

	@Override
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

	@Override
	public void drawMe(VectorShapeDrawer drawer) {

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