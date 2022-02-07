package MOImage;

import java.awt.*;
import java.awt.image.*;
public class ImageLoader
{
   final GraphicsConfiguration gc;
   public ImageLoader(GraphicsConfiguration gc)
   {
      if(gc==null)
      {
         gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      }
      this.gc = gc;
   }


   public BufferedImage convertToVolatileImage(BufferedImage imgIn) {
	   
	   BufferedImage dst = gc.createCompatibleImage(imgIn.getWidth(),imgIn.getHeight(),Transparency.TRANSLUCENT);
       Graphics2D g2d = dst.createGraphics();
       g2d.setComposite(AlphaComposite.Src);
       g2d.drawImage(imgIn,0,0,null);
       g2d.dispose();
       return dst;

   }
   
   //GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
   //GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
   //BufferedImage img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
   //img.setAccelerationPriority(1);

}
