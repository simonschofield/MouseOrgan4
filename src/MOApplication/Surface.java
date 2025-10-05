package MOApplication;
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

import MOAppSessionHelpers.Scene3DHelper;
import MOAppSessionHelpers.SceneInformationInspector;
import MOImage.ImageDimensions;
import MOImage.ImageProcessing;
import MOMaths.PVector;
import MOMaths.Rect;
import MOSimpleUI.Menu;
import MOSimpleUI.RadioButton;
import MOSimpleUI.SimpleUI;
import MOSimpleUI.UIEventData;

import MOUtils.KeepAwake;

import MOUtils.GlobalSettings;



//////////////////////////////////////////////////////////////////
//
@SuppressWarnings("serial")
public abstract class Surface extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener {
	
	
	enum UserSessionState {
		  INITIALISE,
		  LOAD,
		  UPDATE,
		  FINALISE,
		  FINISHED, 
		}
	
	UserSessionState theUserSessionState = UserSessionState.INITIALISE;
	
	public JFrame parentApp = null;

	//Graphics theGraphics;
	
	
	//private float globalSessionScale = 1.0f;

	//protected MainDocumentRenderTarget theDocument;
	protected MainDocument theDocument;
	public ViewController theViewControl = new ViewController();
	public SimpleUI theUI;

	// size of the window for the application
	int windowWidth;
	int windowHeight;

	// the fixed UI rectangle for the view onto the document image
	private Rect canvasWindowRect;
	
	// the object that drives the main loop
	private Timer updateTimer;
	

	boolean userSessionPaused = false;
	protected boolean userSessionAborted = false;
	private int userSessionUpdateCount = 0;
	
	// this is the frequency with which the canvas is updated
	// in respect to userSessionUpdates. 50 has been arrived at through trial and
	// error
	// but the number can be dropped during interactions for a smoother update rate.
	private int canvasUpdateFrequency = 50;
	//public String infoToolMode;
	
	
	KeepAwake keepAwake = new KeepAwake();

	public Surface(JFrame papp) {
		parentApp = papp;
		parentApp.add(this);

		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		

		setWindowSize();
		
		//buildUI();
		
		// here the javax swing timer instance gets passed the ActionListener part of this Surface class
		// This calls the actionPerformed method of the ActionListener interface
		// This drives the automated part of the Mouse Organ
		updateTimer = new Timer(0, this);
		updateTimer.start();
				
	}
	
	


	/////////////////////////////////////////////////////////////////////
	// Initialisation method called from initialise Session in User Session
	// Straight forward session initialisation, not using a ROI manager
	// 
	//
	public void initialiseDocument(int fullScaleRenderW, int fullScaleRenderH, int mainDocumentRenderType) {

		fullScaleRenderW = (int)(fullScaleRenderW * GlobalSettings.getSessionScale());
		fullScaleRenderH = (int)(fullScaleRenderH * GlobalSettings.getSessionScale());
		

		theDocument = new MainDocument(fullScaleRenderW, fullScaleRenderH, mainDocumentRenderType);
		
		initialiseView();
	}
	

	/////////////////////////////////////////////////////////////////////
	// Initialisation method called from initialise Session in User Session
	// Initialising using a ROImanager. This is presumed to become the dominant way
	// to initialise the system.
	//
	public void initialiseDocument(ROIManager roiManager, int mainDocumentRenderType) {

		GlobalSettings.mainSessionName = roiManager.getCurrentROIName();
		GlobalSettings.setROIManager(roiManager);
		
		System.out.println("initialiseDocument:: session name = " + GlobalSettings.mainSessionName);
			
		theDocument = new  MainDocument( roiManager, mainDocumentRenderType);

		initialiseView();
	}
	
	private void setWindowSize() {
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
		
		//canvasWindowRect = new Rect(new PVector(100, 20), new PVector(windowWidth - 20,  windowHeight - 45));
		int left = 100;
		int top = 30;
		canvasWindowRect = new Rect(left, top, windowWidth - (left+20),  windowHeight - (top+40));
	}
	
	private void initialiseView() {

		// ok to do these now
		theViewControl.init(this);
		

		// some default settings
		setCanvasUpdateFrequency(500);
		if(GlobalSettings.getSessionScale()>= 0.5f) {
			setCanvasUpdateFrequency(10);
		}

		//sets the canvas colour in the ui panel only, not in the image
		setCanvasBackgroundColor(new Color(255,255,255,255));


		
		
		
		
		
	}
	
	
	/////////////////////////////////////////////////////////////////////
	// The main loop called by actionPerformed(ActionEvent e)
	//
	private void updateUserSession_All() {
		// this is called by the Action Thread of the app via
		// the automatically called actionPerformed method below
		
		if(theUserSessionState == UserSessionState.INITIALISE) {
			initialiseUserSession();
			// the ui is built after initialisation, so it can include all render targets
			buildUI();
			if(theDocument == null) {
				System.out.println("Fatal::You must initilase the session with initialiseSession() in initialiseUserSession");
				System.exit(0);
			}
			theUserSessionState = UserSessionState.LOAD;
			return;
		}
		//System.out.println("User session state in updateUserSession_All = " + theUserSessionState);
		if(theUserSessionState == UserSessionState.LOAD) {
			theUserSessionState = UserSessionState.UPDATE;
			loadContentUserSession();
			return;
		}
		
		if(theUserSessionState == UserSessionState.UPDATE) {
			if (!userSessionPaused) {
				for(int n = 0; n < canvasUpdateFrequency; n++) {
				   if(theUserSessionState == UserSessionState.FINALISE) break;	
				   updateUserSession();
				   userSessionUpdateCount++;
				}
			}
		}
		
		if(theUserSessionState == UserSessionState.FINALISE) {
				finaliseUserSession();
				canvasUpdateFrequency = 1;
				theUserSessionState = UserSessionState.FINISHED;
		}
		
		if (theUserSessionState == UserSessionState.FINISHED) {
			userSessionFinished();
		}

	}
	
	protected final void endUpdateUserSession() {
		// call this when the updateUserSession has finished
		// Does some tidying up, then Moves the state machine on to FINALISE mode
		// where the finaliseUserSession() is called once, which the user overrides, then goes into idle state
		theUserSessionState = UserSessionState.FINALISE;
		this.forceRefreshDisplay();
		//System.out.println("User session state in endUserSession = " + theUserSessionState);
		keepAwake.setActive(false);
	}
	

	Rect getCanvasWindowRect() {
		return canvasWindowRect.copy();
	}
	
	

	/////////////////////////////////////////////////////////////////////
	// UI related methods
	//
	private void buildUI() {

		theUI = new SimpleUI(this);

		String[] itemList = { "save render", "save all", "quit" };
		
		theUI.addToggleButton("Pause", 0, 200);
		theUI.addPlainButton("End", 0, 230);
		theUI.addMenu("File", 0, 2, itemList);
		
		
		
		
		
		Menu rendertargetViewMenu = theUI.addMenu("Render View", 60, 2, itemList);
		
		//theUI.addRadioButton("3d info", 0, 430, "info tools").setSelected(true);
		//theUI.addRadioButton("sprite info", 0, 460, "info tools");
		//infoToolMode = "3d info";
		theDocument.setRenderTargetViewMenu(rendertargetViewMenu);
		
		
		theUI.addCanvas((int) canvasWindowRect.left, (int) canvasWindowRect.top, (int) canvasWindowRect.getWidth(), (int) canvasWindowRect.getHeight());
		theUI.setFileDialogTargetDirectory(GlobalSettings.getUserSessionPath());
		
	}

	public void handleUIEvent(UIEventData uied) {
		// you subclass this and add your own code here
		//uied.print(1);
		if (uied.eventIsFromWidget("canvas")) {
			handleCanvasMouseEvent(uied);
			return;
		}
		
		
		if (uied.menuItem.contentEquals("save render")) {
			theUI.openFileSaveDialog("save render");
		}

		if (uied.eventIsFromWidget("fileSaveDialog") && uied.fileDialogPrompt.equals("save render")) {
			theDocument.saveRenderTargetToFile("main",uied.fileSelection);
		}
		
		
		if (uied.menuItem.contentEquals("save all")) {
			theUI.openDirectoryChooseDialog("save all");
			
		}

		if (uied.eventIsFromWidget("directorySelectDialog") && uied.fileDialogPrompt.equals("save all")) {
			GlobalSettings.getRenderSaver().saveAllImagesInUserSpecifiedLocation(uied.fileSelection);
		}
		
		if (uied.menuItem.contentEquals("quit")) {
			System.exit(0);
		}

		if (uied.eventIsFromWidget("Pause")) {
			userSessionPaused = uied.toggleSelectState;
		}
		
		if (uied.eventIsFromWidget("End")) {
			userSessionAborted = true;
			endUpdateUserSession();
		}


		if (uied.eventIsFromWidget("ruler size slider")) {
			//setMeasuringToolSize(uied.sliderValue);
		}
		
		if (uied.eventIsFromWidget("Render View")) {
			theViewControl.setRenderTargetAsMainView(uied.menuItem);
		}
		
		

		// if there are any special UI's added by the user session handle them in the
		// user session
		handleUserSessionUIEvent(uied);

	}

	/////////////////////////////////////////////////////////////////////
	// other general purpose methods
	//
	protected void setCanvasBackgroundColor(Color c) {
		theViewControl.setViewDisplayRectBackgroundColor(c);
	}
	
	// only draws behind the image, not to the document
	public void setCanvasBackgroundImage(BufferedImage img) {
		theViewControl.setBackgroundImage(img);
	}

	public int windowWidth() {
		return windowWidth;
	}

	public int windowHeight() {
		return windowHeight;
	}

	public int getCanvasUpdateFrequency() {
		return canvasUpdateFrequency;
	}
	
	protected void setCanvasUpdateFrequency(int cuf) {
		canvasUpdateFrequency = cuf;
	}
	
	public int getUserSessionUpdateCount() {
		
		return userSessionUpdateCount;
	}

	public SimpleUI getSimpleUI() {
		return theUI;
	}

	
	/////////////////////////////////////////////////////////////////////
	// session graphics update methods
	//
	private void updateCanvasDisplay(Graphics g) {

		Graphics2D g2d = (Graphics2D) g.create();
		
		//if(theGraphics==null) theGraphics = g;
		//System.out.println("updating canvas");
		
		if(theViewControl!=null) theViewControl.updateDisplay(g2d);
		g2d.dispose();
	}
	

	@Override
	public void paintComponent(Graphics g) {

		// only update the canvas every canvasUpdateFrequency updates
		//if (userSessionUpdateCount %  == 0) {
			super.paintComponent(g);
			if(theUI!=null) updateCanvasDisplay(g);
			keepAwake.update();
		//}

		// update the ui
		if(theUI!=null) {
			if(theUI.isGraphicsContextSet()==false) {
				Graphics2D g2d = (Graphics2D) g.create();
				theUI.setGraphicsContext(g2d);
			}
			theUI.update();
		}
		
		
		//g2d.dispose();
		
		this.setFocusable(true);
	}

	@Override
	/////////////////////////////////////////////////////////////////////////
	// called by the Timer daemon
	// This drives the whole main loop
	public void actionPerformed(ActionEvent e) {
		updateUserSession_All();
		repaint();
	}

	
	// the user can call this to force redraw the display
	// from inside a tight operation. 
	public void forceRefreshDisplay() {
		//theGraphics = getGraphics();
		updateCanvasDisplay(getGraphics());
	}
	
	
	////////////////////////////////////////////////////////////////////////
	// overridden functions in the user's project
	// - i.e. YOUR UserSession CODE

	
	protected abstract void initialiseUserSession();

	protected abstract void loadContentUserSession();

	protected abstract void updateUserSession();
	
	// optional
	protected void finaliseUserSession() {}
	
	protected  abstract void handleCanvasMouseEvent(UIEventData uied);

	protected abstract void handleUserSessionUIEvent(UIEventData uied);
	
	////////////////////////////////////////////////////////////////////////
	// for using a SessionSequncer, these methods are expected by that class...
	// overload these in your code to utilise the SessionSequncer class
	
	public void initialiseSequence(String sqname) {
		
	}
	
	public boolean updateSequence(String sqname) {
		return false;
	}
	
	public void finaliseSequence(String sqname) {
		
	}

	/////////////////////////////////////////////////////////////////////
	// private event methods
	//
	private void userSessionFinished() {
		//
		userSessionUpdateCount++;
	}
	
	
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
		canvasUpdateFrequency = 500;
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



