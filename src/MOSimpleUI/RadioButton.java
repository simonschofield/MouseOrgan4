package MOSimpleUI;

//////////////////////////////////////////////////////////////////
//RadioButton

public class RadioButton extends ToggleButton {

	// these have to be part of the base class as is accessed by manager
	public String radioGroupName = "";

	public RadioButton(String uiname, int x, int y, String labelString, String groupName, SimpleUI manager) {
		super(uiname, x, y, labelString);
		radioGroupName = groupName;
		UIComponentType = "RadioButton";
		parentManager = manager;
	}

	@Override
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