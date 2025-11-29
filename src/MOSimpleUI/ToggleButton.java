package MOSimpleUI;

import java.awt.Color;

import MOVectorGraphics.VectorShapeDrawer;

//////////////////////////////////////////////////////////////////
//ToggleButton

public class ToggleButton extends ButtonBaseClass {

	public ToggleButton(String uiname, int x, int y, String labelString) {
		super(uiname, x, y, labelString);

		UIComponentType = "ToggleButton";
	}

	@Override
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

	@Override
	public void drawMe(VectorShapeDrawer drawer) {
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