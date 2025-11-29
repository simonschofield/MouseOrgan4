package MOImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ByteImageGetterSetter {


	BufferedImage sourceImage;
	int width, height;
	byte[] byteBuffer;

	public ByteImageGetterSetter(BufferedImage img) {
		sourceImage = img;
		if(sourceImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
			System.out.println("Wrong image type, should be TYPE_BYTE_GRAY");
		}
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();

		byteBuffer = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
	}

	public int getWidth() { return width; }
	public int getHeight() { return height;}

	public boolean isInsideImage(int x, int y) {
		return (x >= 0 && x < width && y >= 0 && y < height);
	}

	public int getPixel(int x, int y) {
		int loc = x + y * width;
		return (byteBuffer[loc] & 0xFF);
	}


	public void setPixel(int x, int y, int val) {
		int loc = x + y * width;
		byteBuffer[loc] = (byte) val;
	}


}
