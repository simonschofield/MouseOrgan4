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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

//////////////////////////////////////////////////////////////////
// static global access to important long-lived classes
//
//
class GlobalObjects{
	static Surface theSurface;
	static RenderTarget theDocument;
	
	static String sampleLibPath = "C:\\simon\\sample lib\\";
	
}

//////////////////////////////////////////////////////////////////
//
@SuppressWarnings("serial")
abstract class Surface extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

	JFrame parentApp = null;

	private float globalSessionScale = 1.0f;

	RenderTarget theDocument;
	ViewControl theViewControl = new ViewControl();
	SimpleUI theUI;

	// size of the window for the application
	int windowWidth;
	int windowHeight;

	// the rectangles for the view onto the document image
	Rect canvasRect_ViewAll;
	Rect canvasRect_Zoomed;
	
	private Timer updateTimer;

	private final int DELAY = 1;
	SecondsTimer secondsTimer;

	boolean userSessionPaused = false;
	boolean userSessionContentLoaded = false;
	int userSessionUpdateCount = 0;
	// this is the path to the user's session folder
	private String userSessionPath = "C:\\simon\\Artwork\\MouseOrgan4\\field 01\\";

	// this is the frequency with which the canvas is updated
	// in respect to userSessionUpdates. 50 has been arrived at through trial and
	// error
	// but the number can be dropped during interactions for a smoother update rate.
	int canvasUpdateFrequency = 50;

	float measuringToolSize = 1f;

	KeepAwake keepAwake = new KeepAwake();

	BufferedImage alternateView;

	public Surface(JFrame papp) {
		parentApp = papp;
		parentApp.add(this);

		// needs to be called here as it sets the window size
		initialiseUserSession();

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	/////////////////////////////////////////////////////////////////////
	// Initialisation methods
	//

	public void initialiseDocument(int dw, int dh, float sessionScale) {
		globalSessionScale = sessionScale;
		theDocument = new RenderTarget();
		theDocument.setRenderBufferSize((int) (dw * globalSessionScale), (int) (dh * globalSessionScale));
		
		setWindowSize();
		setCanvasSize();
		updateTimer = new Timer(DELAY, this);
		updateTimer.start();
		buildUI();

		theViewControl.setDocumentAspect(theDocument.getDocumentAspect());
		secondsTimer = new SecondsTimer();
		
		GlobalObjects.theDocument = theDocument;
		GlobalObjects.theSurface = this;
		
	}

	void setWindowSize() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) screenSize.getWidth();
		int h = (int) screenSize.getHeight();

		windowWidth = w - 100;
		windowHeight = h - 50;

		parentApp.setTitle("Mouse Organ 4");
		parentApp.setSize(windowWidth, windowHeight);
		parentApp.setLocationRelativeTo(null);
		parentApp.setPreferredSize(new Dimension(windowWidth, windowHeight));
		parentApp.pack();
		parentApp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setFocusable(true);
	}

	void setCanvasSize() {
		int canvasMaxWidth = windowWidth - 50;
		int canvasMaxHeight = windowHeight - 50;

		float possibleScaleW = canvasMaxWidth / (float) theDocument.getBufferWidth();
		float possibleScaleH = canvasMaxHeight / (float) theDocument.getBufferHeight();
		float scaledDisplayImageScale = Math.min(possibleScaleW, possibleScaleH);

		int canvasWidth = (int) (theDocument.getBufferWidth() * scaledDisplayImageScale);
		int canvasHeight = (int) (theDocument.getBufferHeight() * scaledDisplayImageScale);
		System.out.println("creating canvas " + canvasWidth + canvasHeight);
		canvasRect_ViewAll = new Rect(100, 0, 100 + canvasWidth, canvasHeight);
		canvasRect_Zoomed = new Rect(100,0,100+canvasMaxWidth, canvasMaxHeight);
	}

	/////////////////////////////////////////////////////////////////////
	// UI related methods
	//
	void buildUI() {

		theUI = new SimpleUI(this);

		String[] itemList = { "file open", "file save", "quit" };
		theUI.addMenu("File", 0, 2, itemList);
		theUI.addToggleButton("Pause", 0, 200);

		theUI.addCanvas((int) canvasRect_ViewAll.left, (int) canvasRect_ViewAll.top, (int) canvasRect_ViewAll.getWidth(), (int) canvasRect_ViewAll.getHeight());

	}

	void handleUIEvent(UIEventData uied) {
		// you subclass this and add your own code here
		// uied.print(1);
		if (uied.eventIsFromWidget("canvas")) {
			handleCanvasMouseEvent(uied);
			return;
		}
		
		
		if (uied.menuItem.contentEquals("file save")) {
			theUI.openFileSaveDialog("");
		}

		if (uied.eventIsFromWidget("fileSaveDialog")) {
			theDocument.saveRenderToFile(uied.fileSelection);
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
		theViewControl.setViewBackgroundColor(c);
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

	void setAlternateView(BufferedImage img) {
		alternateView = img;
	}

	/////////////////////////////////////////////////////////////////////
	// session graphics update methods
	//
	private void updateCanvasDisplay(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();

		// this where we need to get the portion of the image defined by theViewControl
		Rect zoomRect = theViewControl.getZoomRectDocSpace();
		g2d.setColor(theViewControl.getViewBackgroundColor());
		g2d.fillRect((int) canvasRect_ViewAll.left, (int) canvasRect_ViewAll.top, (int) canvasRect_ViewAll.getWidth(),
				(int) canvasRect_ViewAll.getHeight());

		if (alternateView != null) {
			g2d.drawImage(alternateView, (int) canvasRect_ViewAll.left, (int) canvasRect_ViewAll.top, (int) canvasRect_ViewAll.getWidth(),
					(int) canvasRect_ViewAll.getHeight(), null);
		} else {
			Rect currentCanvasRect = getCurrentCanvasRect();
			g2d.drawImage(theDocument.getCropDocSpace(zoomRect), (int) currentCanvasRect.left, (int) currentCanvasRect.top,
					(int) currentCanvasRect.getWidth(), (int) currentCanvasRect.getHeight(), null);
		}
		g2d.dispose();

	}
	
	Rect getCurrentCanvasRect() {
		 return canvasRect_ViewAll;
		//if(theViewControl.getScale()==1.0f) return canvasRect_ViewAll;
		//return canvasRect_Zoomed;
	}

	@Override
	public void paintComponent(Graphics g) {

		// only update the canvas every canvasUpdateFrequency updates
		if (userSessionUpdateCount % canvasUpdateFrequency == 0) {
			super.paintComponent(g);
			updateCanvasDisplay(g);
		}

		// update the ui
		Graphics2D g2d = (Graphics2D) g.create();
		theUI.setGraphicsContext(g2d);

		theUI.update();
		keepAwake.update();
		g2d.dispose();
		this.setFocusable(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// this calls your session code
		updateUserSession_All();
		repaint();
	}

	private void updateUserSession_All() {

		if (userSessionContentLoaded == false) {
			loadContentUserSession();
			userSessionContentLoaded = true;
		}

		if (!userSessionPaused) {
			updateUserSession();
			userSessionUpdateCount++;
		}

	}
	
	
	//////////////////////////////////////////////////////////////////
	// call this to tidy up
	void userSessionFinished() {
		keepAwake.setActive(false);
	}

	//////////////////////////////////////////////////////////////////
	// canvas overlay drawing, maybe should be moved to UI class
	//

	
	void drawOverlayRect(Rect r) {
		//theUI.clearCanvasOverlayShapes();
		//theUI.addCanvasOverlayShape(r.getTopLeft(), r.getBottomRight(), "rect", new Color(0, 0, 0, 0), Color.gray, 2);

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

	////////////////////////////////////////////////////////////////////////
	// overridden functions in the user's project
	// - i.e. YOUR CODE

	/*
	public void initialiseUserSession() {
		// overridden in UserSession sub class

	}

	public void loadContentUserSession() {
		// overridden in UserSession sub class
	}

	public void updateUserSession() {
		// overridden in UserSession sub class

	}
	

	void handleCanvasMouseEvent(UIEventData uied) {
		// System.out.println("canvas mouse event " + mouseEventType + " detected at " +
		// canvasX + " " + canvasY);

	}

	void handleUserSessionUIEvent(UIEventData uied) {
		// System.out.println("user session event ");

	}
	*/
	
	abstract void initialiseUserSession();

	abstract void loadContentUserSession();

	abstract void updateUserSession();
	
	abstract void handleCanvasMouseEvent(UIEventData uied);

	abstract void handleUserSessionUIEvent(UIEventData uied);

	/////////////////////////////////////////////////////////////////////
	// private event methods
	//
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		theUI.handleMouseEvent(e, "mouseDragged");
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(e.toString());
		theUI.handleMouseEvent(e, "mouseMoved");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(e.toString());
		this.setFocusable(true);
		theUI.handleMouseEvent(e, "mouseClicked");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		theUI.handleMouseEvent(e, "mousePressed");
		canvasUpdateFrequency = 1;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		theUI.handleMouseEvent(e, "mouseReleased");
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
		theViewControl.keyboardZoom(e);
		System.out.println("zoom scale = " + theViewControl.getScale());
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
