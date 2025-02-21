package MOImage;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.ComponentColorModel;

public class FloatBufferedImage {
	
		
	public BufferedImage floatBufferedImage;	
	
	public FloatBufferedImage(int w, int h)
	{
	

	ComponentSampleModel sm = new ComponentSampleModel(  DataBuffer.TYPE_FLOAT, w, h, 1, w, new int[] {0});
	
	DataBuffer db = new DataBufferFloat(w * h);
	WritableRaster wr = Raster.createWritableRaster(sm, db, null);
	ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	ComponentColorModel cm = new ComponentColorModel( cs, false, true, Transparency.OPAQUE, DataBuffer.TYPE_FLOAT);
	floatBufferedImage = new BufferedImage(cm, wr, true, null);
	} 
	
	/*
	Graphics2D g2 = bi.createGraphics();
	g2.setRenderingHint(
	RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	g2.setColor(Color.BLACK);
	g2.fillRect(0, 0, w, h);
	g2.setColor(Color.WHITE);
	g2.drawLine(0, 0, w, h);
	g2.drawLine(w, 0, 0, h);
	g2.drawOval(w/4, h/4, w/2, h/2);
	g2.dispose();

	Frame f = new Frame("Float Graphics");
	f.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent we) {
	System.exit(0);
	}
	});
	f.setLayout(new BorderLayout());
	Canvas c = new Canvas() {
	public void paint(Graphics g) {
	g.drawImage(bi, 0, 0, null);
	}
	};
	c.setSize(w, h);
	f.add(c, BorderLayout.CENTER);
	f.pack();
	f.show();
	}*/
}
