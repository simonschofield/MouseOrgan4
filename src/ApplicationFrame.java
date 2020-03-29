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

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {

            	// size of window and image
            	//
            	
            	ApplicationFrame ex = new ApplicationFrame();
                ex.setVisible(true);
            }
        });
    }

	
}