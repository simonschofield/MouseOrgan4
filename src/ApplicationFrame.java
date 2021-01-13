import java.awt.EventQueue;

import javax.swing.JFrame;

//////////////////////////////////////////////////////////////////
//
@SuppressWarnings({ "serial" })
public class ApplicationFrame extends JFrame {

    public ApplicationFrame() {
    	// this is where you declare your user session class
    	new UserSession(this);
    }

    public static void main(String[] args) {
    	
    	Runnable runnableApp = new Runnable() {
            @Override
            public void run() {
            	ApplicationFrame ex = new ApplicationFrame();
                ex.setVisible(true);
            }
        };
        
        EventQueue.invokeLater(runnableApp);
    }

	
}