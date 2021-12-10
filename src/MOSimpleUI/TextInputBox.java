package MOSimpleUI;

import java.awt.Color;

import MOMaths.PVector;
import MOVectorGraphics.VectorShapeDrawer;

////////////////////////////////////////////////////////////////////////////////
//self contained simple txt ox input
//simpleUICallback is called after every character insertion/deletion, enabling immediate udate of the system
//
public class TextInputBox extends Widget {
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

	public void drawMe(VectorShapeDrawer drawer) {

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
