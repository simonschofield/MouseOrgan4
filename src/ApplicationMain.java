

import java.awt.EventQueue;

import javax.swing.JFrame;

//////////////////////////////////////////////////////////////////
//
@SuppressWarnings({ "serial" })
public class ApplicationMain extends JFrame {

    public ApplicationMain() {
    	// this is where you declare your user session class
    	new UserSession(this);
    	
    	
    }

    public static void main(String[] args) {
    	
    	Runnable runnableApp = new Runnable() {
            @Override
            public void run() {
            	ApplicationMain ex = new ApplicationMain();
                ex.setVisible(true);
            }
        };
        
        EventQueue.invokeLater(runnableApp);
    }

	
}