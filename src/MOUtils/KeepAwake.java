package MOUtils;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

public class KeepAwake{
	Robot hal;
	SecondsTimer timer;
	int mouseMoveDirection = 1;
	boolean isActive = true;

    public KeepAwake(){
        timer = new SecondsTimer();
    	try {
			hal = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

            timer.startDuration(60f);
        }

    public void update() {
    	if(!isActive || timer.isInDuration()) {
			return;
		}
    	Point pi = MouseInfo.getPointerInfo().getLocation();
    	hal.mouseMove(pi.x+mouseMoveDirection,pi.y);
    	mouseMoveDirection *= -1;
    	timer.startDuration(60f);
    	pi = MouseInfo.getPointerInfo().getLocation();
    	//System.out.println("KeepAwake mouse move x = " + pi.x);
    }

    public void setActive(boolean a) {
    	isActive = a;
    }

}

///
//