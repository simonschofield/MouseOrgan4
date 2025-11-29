package MOSimpleUI;

import java.awt.Color;

import MOMaths.MOMaths;
import MOMaths.PVector;
import MOVectorGraphics.VectorShapeDrawer;

/////////////////////////////////////////////////////////////////////////////
//Slider class stuff

/////////////////////////////////////////////////////////////////////////////
//Slider Class
//
//calls back with value on  both release and drag

public class Slider extends Widget {

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

	@Override
	public void handleMouseEvent(String mouseEventType, int x, int y) {
		PVector p = new PVector(x, y);

		if (mouseLeave(p)) {
			UIEventData uied = new UIEventData(UIManagerName, UIComponentType, UILabel, mouseEventType, x, y);
			uied.sliderValue = currentValue;
			parentManager.handleUIEvent(uied);
			// println("mouse left sider");
		}

		if (!bounds.isPointInside(p)) {
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
			//System.out.println("slider val " + val);
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

	public float getSliderValue() {
		return currentValue;
	}

	public void setSliderValue(float val) {
		currentValue = MOMaths.constrain(val, 0, 1);
	}

	boolean mouseLeave(PVector p) {
		// is only true, if the mouse has been in the widget, has been depressed
		if (mouseEntered && !bounds.isPointInside(p)) {
			mouseEntered = false;
			return true;
		}

		return false;
	}

	@Override
	public void drawMe(VectorShapeDrawer drawer) {
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

	void drawSliderHandle(VectorShapeDrawer drawer, int loc) {
		drawer.setDrawingStyle(new Color(255, 255, 255, 100), new Color(0, 0, 0, 255), 1);
		drawer.drawEllipse(loc, bounds.top + 5, 10, 10);
	}

}