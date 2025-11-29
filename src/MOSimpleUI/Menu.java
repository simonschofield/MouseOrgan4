package MOSimpleUI;

import java.awt.Color;
import java.util.ArrayList;

import MOVectorGraphics.VectorShapeDrawer;

/////////////////////////////////////////////////////////////////////////////
//menu stuff
//
//

/////////////////////////////////////////////////////////////////////////////
//the menu class
//
public class Menu extends Widget {

	int textPad = 2;
	// String title;
	int textSize = 8;

	int numItems = 0;
	SimpleUI parentManager;
	public boolean visible = false;

	boolean showLastSelection = false;
	String lastSelection = "";

	ArrayList<String> itemList = new ArrayList<>();

	public Menu(String uiname, String uilabel, int x, int y, String[] menuItems, SimpleUI manager) {
		super(uiname, uilabel, x, y, 60, 20);
		parentManager = manager;
		UIComponentType = "Menu";

		setItems(menuItems);
	}

	public void setItems(String[] menuItems) {
		itemList.clear();
		numItems = 0;
		for (String s : menuItems) {
			itemList.add(s);
			numItems++;
		}
	}

	public void showLastSelected() {
		showLastSelection = true;
		lastSelection = itemList.get(0);
	}


	public void setLastSelected(String s) {

		for(String item: itemList) {

			if(item.equals(s)) {
				lastSelection = item;
				System.out.println("found grid as a menu item");
			}
		}
	}

	public String getLastSelected() {
		return lastSelection;
	}



	@Override
	public void drawMe(VectorShapeDrawer drawer) {
		// println("drawing menu " + title);
		drawTitle(drawer);
		if (visible) {
			drawItems(drawer);
		}

	}

	void drawTitle(VectorShapeDrawer drawer) {

		if (rollover) {
			drawer.setDrawingStyle(SimpleUIWidgetRolloverColor, new Color(0, 0, 0, 255), 1);
		} else {
			drawer.setDrawingStyle(SimpleUIWidgetFillColor, new Color(0, 0, 0, 255), 1);
		}

		drawer.drawRect(locX, locY, widgetWidth, widgetHeight);
		drawer.setFillColor(SimpleUITextColor);
		drawer.setTextStyle(6);




		if (!showLastSelection) {
				drawer.drawText(this.UILabel, locX + textPad, locY + textPad + textSize);
		    }else {
		    	drawer.drawText(lastSelection, locX + textPad, locY + textPad + textSize);
		    }
	}

	void drawItems(VectorShapeDrawer drawer) {
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

			drawer.drawText(s, locX + textPad, thisY + textPad + textSize);
			thisY += widgetHeight;
		}

	}

	void hiliteItem(VectorShapeDrawer drawer, int y) {

		int topOfItems = this.locY + widgetHeight;
		float distDown = y - topOfItems;
		int itemNum = (int) distDown / widgetHeight;
		Color cc = drawer.getFillColor();
		drawer.setFillColor(new Color(230, 210, 210));

		drawer.drawRect(locX, topOfItems + itemNum * widgetHeight, widgetWidth, widgetHeight);
		drawer.setFillColor(cc);
	}

	@Override
	public void handleMouseEvent(String mouseEventType, int x, int y) {
		rollover = false;
		mouseX = x;
		mouseY = y;
		// println("here1 " + mouseEventType);
		if (!isInMe(x, y)) {
			visible = false;
			return;
		}
		if (isInMe(x, y)) {
			rollover = true;
		}


		if (mouseEventType.equals("mousePressed") && !visible) {

			parentManager.setMenusOff();
			visible = true;
			rollover = true;
			return;
		}
		if (mouseEventType.equals("mousePressed") && isInItems(x, y)) {
			String pickedItem = getItem(y);
			lastSelection = pickedItem;
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

	@Override
	public boolean isInMe(int x, int y) {
		if (isInTitle(x, y) || isInItems(x, y)) {

			return true;
		}
		visible = false;
		return false;
	}

	boolean isInTitle(int x, int y) {
		if (x >= this.locX && x < this.locX + this.widgetWidth && y >= this.locY && y < this.locY + this.widgetHeight) {
			return true;
		}
		return false;

	}

	boolean isInItems(int x, int y) {
		if (!visible) {
			return false;
		}
		if (x >= this.locX && x < this.locX + this.widgetWidth && y >= this.locY + this.widgetHeight
				&& y < this.locY + (this.widgetHeight * (this.numItems + 1))) {
			return true;
		}

		return false;
	}

}// end of menu class
