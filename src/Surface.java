import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

//////////////////////////////////////////////////////////////////
// static global access to important long-lived classes
//
//
class GlobalObjects{
	static Surface theSurface;
	static MainDocumentRenderTarget theDocument;
	
	static String sampleLibPath = "C:\\simon\\sample lib\\";
	
}

//////////////////////////////////////////////////////////////////
//
@SuppressWarnings("serial")
abstract class Surface extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener {
	
	enum UserSessionState {
		  INITIALISE,
		  LOAD,
		  UPDATE,
		  FINALISE,
		  FINISHED
		}
	
	UserSessionState theUserSessionState = UserSessionState.INITIALISE;
	
	JFrame parentApp = null;

	private float globalSessionScale = 1.0f;

	MainDocumentRenderTarget theDocument;
	ViewController theViewControl = new ViewController();
	SimpleUI theUI;

	// size of the window for the application
	int windowWidth;
	int windowHeight;

	// the fixed UI rectangle for the view onto the document image
	private Rect viewDisplayRect;
	
	
	private Timer updateTimer;
	private final int DELAY = 0;
	//SecondsTimer secondsTimer;

	boolean userSessionPaused = false;
	private int userSessionUpdateCount = 0;
	// this is the path to the user's session folder
	private String userSessionPath = "";

	// this is the frequency with which the canvas is updated
	// in respect to userSessionUpdates. 50 has been arrived at through trial and
	// error
	// but the number can be dropped during interactions for a smoother update rate.
	private int canvasUpdateFrequency = 50;

	

	KeepAwake keepAwake = new KeepAwake();

	public Surface(JFrame papp) {
		parentApp = papp;
		parentApp.add(this);

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		GlobalObjects.theSurface = this;
		
		setWindowSize();

		initialiseUserSession();
		theUserSessionState = UserSessionState.LOAD;
	}
	
	void setWindowSize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) screenSize.getWidth();
		int h = (int) screenSize.getHeight();

		windowWidth = w - 300;
		windowHeight = h - 100;

		parentApp.setTitle("Mouse Organ 4");
		parentApp.setSize(windowWidth, windowHeight);
		parentApp.setLocationRelativeTo(null);
		parentApp.setPreferredSize(new Dimension(windowWidth, windowHeight));
		parentApp.pack();
		parentApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setFocusable(true);
		
		viewDisplayRect = new Rect(100, 5, windowWidth - 20,  windowHeight - 45);
	}


	/////////////////////////////////////////////////////////////////////
	// Initialisation methods
	//

	public void initialiseDocument(int dw, int dh, float sessionScale) {
		globalSessionScale = sessionScale;
		theDocument = new MainDocumentRenderTarget();
		GlobalObjects.theDocument = theDocument;
		theDocument.setRenderBufferSize((int) (dw * globalSessionScale), (int) (dh * globalSessionScale));
		
		//setWindowSize();
		
		updateTimer = new Timer(DELAY, this);
		updateTimer.start();
		
		theViewControl.init();
		buildUI();
		
	}
	
	private void updateUserSession_All() {
		// this is called by the Action Thread of the app via
		// the automatically called actionPerformed method below
		
		//if(theUserSessionState == UserSessionState.INITIALISE) {
		//	initialiseUserSession();
		//	theUserSessionState = UserSessionState.LOAD;
		//	return;
		//}
		
		if(theUserSessionState == UserSessionState.LOAD) {
			loadContentUserSession();
			theUserSessionState = UserSessionState.UPDATE;
		}
		
		if(theUserSessionState == UserSessionState.UPDATE) {
			if (!userSessionPaused) {
				updateUserSession();
				userSessionUpdateCount++;
			}
		}
		
		if(theUserSessionState == UserSessionState.FINALISE) {
				finishUserSession();
				canvasUpdateFrequency = 1;
				theUserSessionState = UserSessionState.FINISHED;
		}
		
		if (theUserSessionState == UserSessionState.FINISHED) {
			// do nothing
		}

	}
	
	void endUserSession() {
		// call this if you need to end the user session
		// It automatically called userSessionFinishe() method once, then
		// goes into idle state
		theUserSessionState = UserSessionState.FINALISE;
		keepAwake.setActive(false);
	}
	

	Rect getViewDisplayRegion() {
		return viewDisplayRect.copy();
	}
	
	Rect getViewPortDocSpace() {
		return theViewControl.getCurrentViewPortDocSpace();
	}

	/////////////////////////////////////////////////////////////////////
	// UI related methods
	//
	private void buildUI() {

		theUI = new SimpleUI(this);

		String[] itemList = { "save render", "quit" };
		
		theUI.addToggleButton("Pause", 0, 200);
		theUI.addMenu("File", 0, 2, itemList);
		theUI.addCanvas((int) viewDisplayRect.left, (int) viewDisplayRect.top, (int) viewDisplayRect.getWidth(), (int) viewDisplayRect.getHeight());

	}

	void handleUIEvent(UIEventData uied) {
		// you subclass this and add your own code here
		// uied.print(1);
		if (uied.eventIsFromWidget("canvas")) {
			handleCanvasMouseEvent(uied);
			return;
		}
		
		
		if (uied.menuItem.contentEquals("save render")) {
			theUI.openFileSaveDialog("");
		}

		if (uied.eventIsFromWidget("fileSaveDialog")) {
			theDocument.saveRenderToFile(uied.fileSelection);
		}
		
		if (uied.menuItem.contentEquals("quit")) {
			System.exit(0);
		}

		if (uied.eventIsFromWidget("Pause")) {
			userSessionPaused = uied.toggleSelectState;
		}

		

		if (uied.eventIsFromWidget("ruler size slider")) {
			//setMeasuringToolSize(uied.sliderValue);
		}

		// if there are any special UI's added by the user session handle them in the
		// user session
		handleUserSessionUIEvent(uied);

	}

	/////////////////////////////////////////////////////////////////////
	// other general purpose methods
	//
	void setCanvasBackgroundColor(Color c) {
		theViewControl.setViewDisplayRectBackgroundColor(c);
	}
	
	void setCanvasBackgroundImage(BufferedImage img) {
		theViewControl.setBackgroundImage(img);
	}

	public int windowWidth() {
		return windowWidth;
	}

	public int windowHeight() {
		return windowHeight;
	}

	public float getSessionScale() {
		return globalSessionScale;
	}
	
	public int getCanvasUpdateFrequency() {
		return canvasUpdateFrequency;
	}
	
	void setCanvasUpdateFrequency(int cuf) {
		canvasUpdateFrequency = cuf;
	}
	
	
	
	float fullScalePixelsToDocSpace(float pixels) {
		// Allows the user to get the doc space measurement
		// for a number of pixels in the full scale image. This is useful for defining 
		// certain drawing operations, such as defining line thickness and circle radius. 
		// To be resolution independent, these operations take measurement in document space.
		// But the user may wish to think in pixel size in the final 100% scale image.
		float pixelsScaled = pixels*globalSessionScale;
		return (pixelsScaled/theDocument.getLongestBufferEdge());
	}

	SimpleUI getSimpleUI() {
		return theUI;
	}

	
	/////////////////////////////////////////////////////////////////////
	// session graphics update methods
	//
	private void updateCanvasDisplay(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();

		if(theViewControl!=null) theViewControl.updateDisplay(g2d);
		
		g2d.dispose();

	}
	

	@Override
	public void paintComponent(Graphics g) {

		// only update the canvas every canvasUpdateFrequency updates
		if (userSessionUpdateCount % canvasUpdateFrequency == 0) {
			super.paintComponent(g);
			if(theUI!=null) updateCanvasDisplay(g);
			keepAwake.update();
		}

		// update the ui
		
		Graphics2D g2d = (Graphics2D) g.create();
		
		if(theUI!=null) {
			theUI.setGraphicsContext(g2d);
			theUI.update();
		}
		
		
		g2d.dispose();
		
		this.setFocusable(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// this calls your session code
		// and then updates the graphics
		updateUserSession_All();
		repaint();
	}


	////////////////////////////////////////////////////////////////////////
	// user-session related methods
	// -
	void setUserSessionPath(String dir) {
		userSessionPath = dir;
		theUI.setFileDialogTargetDirectory(userSessionPath);
	}

	String getUserSessionPath() {
		return userSessionPath;
	}
	
	String getUserSessionDirectoryName() {
		// returns the final part of the path, so if
		// C://aaa///bbb//ccc is the user session path,
		// it returns "ccc"
		File file = new File(userSessionPath);
		return file.getName().toString();
		
	}
	
	String getSessionTimeStampedFileName(String enhancement) {
		String sessionName = getUserSessionDirectoryName();
		String fullPathAndFileName = userSessionPath + sessionName;
		return MOUtils.getDateStampedImageFileName(fullPathAndFileName + enhancement);
	}

	////////////////////////////////////////////////////////////////////////
	// overridden functions in the user's project
	// - i.e. YOUR CODE

	
	abstract void initialiseUserSession();

	abstract void loadContentUserSession();

	abstract void updateUserSession();
	
	// optional
	void finishUserSession() {}
	
	abstract void handleCanvasMouseEvent(UIEventData uied);

	abstract void handleUserSessionUIEvent(UIEventData uied);

	/////////////////////////////////////////////////////////////////////
	// private event methods
	//
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		if(theUI!=null) theUI.handleMouseEvent(e, "mouseDragged");
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(e.toString());
		if(theUI!=null) theUI.handleMouseEvent(e, "mouseMoved");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(e.toString());
		this.setFocusable(true);
		if(theUI!=null) theUI.handleMouseEvent(e, "mouseClicked");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		if(theUI!=null) theUI.handleMouseEvent(e, "mousePressed");
		canvasUpdateFrequency = 1;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if(theUI!=null) theUI.handleMouseEvent(e, "mouseReleased");
		canvasUpdateFrequency = 50;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("keyTyped");
	}

	@Override
	public void keyPressed(KeyEvent e) {

		//System.out.println("keyPressed");
		theViewControl.keyboardViewInput(e);
		//System.out.println("zoom scale = " + theViewControl.getCurrentScale());
		canvasUpdateFrequency = 1;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("keyReleased");
		canvasUpdateFrequency = 50;
	}

}



class UserSessionSettings{
	boolean isDraft = true;
	int fullScaleRenderWidth, fullScaleRenderHeight;
	float draftRenderScale = 0.2f;
	String currentSchemea = "";
	String userSessionPath = "";
	
	UserSessionSettings(boolean isFinalCopy, float draftRendScale, String currentSchm){
		
		isDraft = !isFinalCopy;
		draftRenderScale = draftRendScale;
		currentSchemea = currentSchm;
	}
	
	UserSessionSettings(int fullRenderW, int fullRenderH, boolean isFinalCopy, float draftRendScale, String currentSchm){
		fullScaleRenderWidth = fullRenderW;
		fullScaleRenderHeight = fullRenderH;
		isDraft = !isFinalCopy;
		draftRenderScale = draftRendScale;
		currentSchemea = currentSchm;
	}
	
	float getRenderScale() {
		if(isDraft) return draftRenderScale;
		return 1.0f;
	}
	
	int getRenderQuality() {
		if(isDraft) return 0;
		return 2;
	}
	
	boolean isSchema(String s) {
		return currentSchemea.contentEquals(s);
	}
	
	boolean isDraft() { return isDraft;}
}
