package MOSimpleUI;

import java.awt.Color;

import MOVectorGraphics.VectorShapeDrawer;

//////////////////////////////////////////////////////////////////
//Simple Label widget - uneditable text
//It displays label:text, where text is changeable in the widget's lifetime, but label is not

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

	@Override
	public void drawMe(VectorShapeDrawer drawer) {

		drawer.setDrawingStyle(SimpleUIBackgroundRectColor, new Color(100, 100, 100, 255), 1);
		drawer.drawRect(locX, locY, widgetWidth, widgetHeight);

		String seperator = ":";
		if (this.text.equals("")) {
			seperator = " ";
		}
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


