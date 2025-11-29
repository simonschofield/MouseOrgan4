package MOSimpleUI;

import MOMaths.PVector;

//////////////////////////////////////////////////////////////////
//UIEventData
//when a UI component calls the simpleUICallback() function, it passes this object back
//which contains EVERY CONCEIVABLE bit of extra information about the event that you could imagine
//
public class UIEventData {
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

	public boolean eventIsFromWidget(String lab) {
		if (uiLabel.equals(lab)) {
			return true;
		}
		return false;

	}

	public void print(int verbosity) {
		if ((verbosity != 3 && this.mouseEventType.equals("mouseMoved")) || (verbosity == 0)) {
			return;
		}

		if (verbosity >= 1) {
			System.out.println("UIEventData:" + this.uiComponentType + " " + this.uiLabel);

			if (this.uiComponentType.equals("canvas")) {
				System.out.println("mouse event:" + docSpacePt.toString());

			}
		}

		if (verbosity >= 2) {
			//System.out.println("doc space of event:" + this.mouseEventType + " at (" + this.mousex + "," + this.mousey + ")" + " doc space" + docSpacePt.toString());
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