package MOImage;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import MOMaths.MOMaths;
import MOMaths.PVector;
import MOMaths.Range;



///////////////////////////////////////////////////////////
//
//FloatImage
//very useful for storing depth buffers
//many of the operations expect the range of floats to be 0...1
//
// Values that are at the extreme ends of the float range (+/- Float.MAX_VALUE) are treated as "NaN" and ignored in equaliszation ,and extrema finding.
// so can be used as Mask values.
///////////////////////////////////////////////////////////

public class FloatImage{
  float [] floatArray;
  int xdim, ydim;
  Range extrema = new Range(0,1);
  ConvolutionFilter cf;

  // this allows for a particular value in the image to be ruled as a "Mask" value
  // and not taken into consideration for either extrema, or interpolation
  boolean maskValueSet = false;
  float maskValue = -Float.MAX_VALUE;


  //contructors
  public FloatImage(){}

  public FloatImage(int wid, int hig){
    createNew(wid,hig);
  }

  public FloatImage(FloatImage f){
    createNew(f.xdim,f.ydim);
    for(int i = 0; i < xdim*ydim; i++){
      this.set(i,f.get(i));
    }
  }

  public void setMaskValue(float val, boolean useMask) {
	  maskValueSet = useMask;
	  maskValue = val;
  }

  public float getMaskValue() {

	  return maskValue;
  }

  public FloatImage(String pathandfile){
	if(pathandfile.endsWith(".png")) {
		load16BitPNG(pathandfile);
	}
	if(pathandfile.endsWith(".data")) {
		loadFloatData(pathandfile);
	}
  }

  public void createNew(int wid, int hig){
    xdim = wid;
    ydim = hig;
    floatArray = new float[xdim * ydim];

  }





  public Range getExtrema(){
    updateExtrema();
    return extrema.copy();
  }

  void updateExtrema(){
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;


    for(int y = 0; y < ydim; y++) {
		for(int x = 0; x < xdim; x++) {

			float val = get(x,y);
			if(isMaskValue(val)) {
				continue;
			}

			if(val < min) { min = val; }
			if(val > max) { max = val; }
		}
    }
    extrema.limit1 = min;
    extrema.limit2 = max;
    System.out.println("FloatImage extrema are " + extrema.limit1 + " " + extrema.limit2);
  }


  boolean isMaskValue(float val) {

	  if((val == -Float.MAX_VALUE) || (val == Float.MAX_VALUE)) {
		return true;
	}

	  if(!maskValueSet) {
		return false;
	}
	  if(val == maskValue) {
		return true;
	}
	  return false;
  }


  public void replaceMaskValue(float replacementVal) {

	    for(int y = 0; y < ydim; y++) {
			for(int x = 0; x < xdim; x++) {

				float val = get(x,y);
				if(isMaskValue(val)) {
					set( x,  y,  replacementVal);
				}


			}
	    }
  }


  public float get(int x, int y){
    int loc = xdim*y + x;
    return floatArray[loc];
  }


  float getClamped(int x, int y){
    x = (int) MOMaths.constrain(x,0f,xdim-1);
    y = (int) MOMaths.constrain(y,0f,ydim-1);
    return get(x,y);
  }

  float get(int index){
    return floatArray[index];
  }



  public void set(int x, int y, float val){
    int index = xdim*y + x;

    set(index, val);
  }

  void set(int index, float val){

    floatArray[index] = val;
  }

  public int getWidth(){ return xdim;}

  public int getHeight(){ return ydim;}

  public int getArrayLength(){ return xdim * ydim;}

  public void setAll(float val){

    int len = getArrayLength();
    for(int i = 0; i < len; i++) {
		floatArray[i] = val;
	}

  }




  FloatImage getCrop(int left, int top, int right, int bottom){
    // make sure the dims are legal. The crop includes the right and bottom row/column
    left = Math.max(0,left);
    top = Math.max(0,top);
    right = Math.min(right,xdim-1);
    bottom = Math.min(bottom,ydim-1);


    int w = right-left;
    int h = bottom-top;
    if(w < 1 || h < 1){
      //println("Crop Illegal cropped image size");
      return new FloatImage();
    }
    //println("cropping img:ltrbwh",left,top,right,bottom,w,h);
    FloatImage cropImg = new FloatImage(w+1, h+1);
    int cropIndex = 0;
    for(int y = top; y <= bottom; y++){
      for(int x = left; x <= right; x++){
        float val = get(x,y);
        cropImg.set(cropIndex, val);
        cropIndex++;
      }

    }
    //println("Cropping fininshed");
    return cropImg;
  }

  public float getPixelBilinearDocSpace(float docSpaceX, float docSpaceY, float aspect) {
	  //converts to normalised space
	  PVector normalizedSpace= docSpaceToNormalizedSpace(new PVector(docSpaceX, docSpaceY), aspect);
      return getPixelBilin_Normalized( normalizedSpace.x,  normalizedSpace.y);
  }

  public static PVector docSpaceToNormalizedSpace(PVector docSpace, float aspect) {
	  if(aspect>1) {
		  return new PVector( docSpace.x,  docSpace.y*aspect);
	  }
	  return new PVector( docSpace.x/aspect,  docSpace.y);
  }

  public float getPixelBilin_Normalized(float x, float y) {
	  // x and y in Normalized Space (both in the range 0...1)
	  return getPixelBilin(x/xdim, y/ydim);
  }

  // bilinier interpolation
  public float getPixelBilin(float x, float y){
    // works in image pixel coordinates, but floating point accuracy,

    // regarding the 4 pixels we are concerned with
    // A B
    // C D
    // ((int)x,(int)y) is the coordinate at the top left of A
    // B,C and D are ventured into as the mantissa of x and y move between 0...1
    // This algorithm works out the average Color of them based on the degree of area overlap of each pixel

    int xLow = (int)x;
    int yLow = (int)y;
    float offsetX = x - xLow;
    float offsetY = y - yLow;

    int xLowPlus1 = Math.min(xLow+1, getWidth()-1);
    int yLowPlus1 = Math.min(yLow+1, getHeight()-1);



    // get the four pixels
    float pixelA = this.getClamped(xLow,yLow);

    // if there is no mantissa, then don't bother to interpolate
    if(offsetX == 0 && offsetY == 0) {
		return pixelA;
	}

    float pixelB = this.getClamped(xLowPlus1,yLow);
    float pixelC = this.getClamped(xLow,yLowPlus1);
    float pixelD = this.getClamped(xLowPlus1,yLowPlus1);

    // if any are ignored values return the pixelA value as interpolation won't work
    if( isMaskValue(pixelA) || isMaskValue(pixelB) ||
    	isMaskValue(pixelC) || isMaskValue(pixelD)) {
    	//System.out.println("interpolation prob at " + xLow + "," + yLow + " A " + pixelA + " B " + pixelB + " C " + pixelC + " D " + pixelD);
    	return pixelA;
    }

    // if they happen to be all the same anyway return the value ...
    if(pixelA == pixelB && pixelA == pixelC && pixelA == pixelD) {
		return pixelA;
	}

    // ... otherwise work out the foating point bit of the pixel location


    // use this work out the overlap for each pixel
    float amountA = (1-offsetX) * (1-offsetY);
    float amountB = (offsetX) * (1-offsetY);
    float amountC = (1-offsetX) * (offsetY);
    float amountD = (offsetX) * (offsetY);

    // sanity chack that all the areas add up to 1
    //float sumShouldEqual1 = amountA + amountB + amountC + amountD;
    // if( !near(sumShouldEqual1,1) ) println("sums = ", sumShouldEqual1);
    // now average all the red Colors based on their relative amounts in A,B,C & D
    float aveR = (pixelA*amountA + pixelB*amountB +pixelC*amountC + pixelD*amountD);


    //println(aveR,aveG,aveB);

    return aveR;

  }

  ////////////////////////////////
  // bi-cubic interpolation
  ////////////////////////////////

  float cubicHermite(float A, float B, float C, float D, float t)
    {
    float a = -A / 2.0f + (3.0f*B) / 2.0f - (3.0f*C) / 2.0f + D / 2.0f;
    float b = A - (5.0f*B) / 2.0f + 2.0f*C - D / 2.0f;
    float c = -A / 2.0f + C / 2.0f;
    float d = B;

    return a*t*t*t + b*t*t + c*t + d;
    }

  float getPixelBicubic (float u, float v)
    {
    // works in the parametric coordinate space of 0..1 in x (u) and y (v)
    // calculate coordinates -> also need to offset by half a pixel
    // to keep image from shifting down and left half a pixel
    float x = (float) ((u * xdim) - 0.5);
    int xint = (int)x;
    float xfract = x - xint;

    float y = (v * ydim) - 0.5f;
    int yint = (int)y;
    float yfract = y - yint;

    // 1st row
    float p00 = getClamped(xint - 1, yint - 1);
    float p10 = getClamped(xint + 0, yint - 1);
    float p20 = getClamped(xint + 1, yint - 1);
    float p30 = getClamped(xint + 2, yint - 1);

    // 2nd row
    float p01 = getClamped(xint - 1, yint + 0);
    float p11 = getClamped(xint + 0, yint + 0);
    float p21 = getClamped(xint + 1, yint + 0);
    float p31 = getClamped(xint + 2, yint + 0);

    // 3rd row
    float p02 = getClamped(xint - 1, yint + 1);
    float p12 = getClamped(xint + 0, yint + 1);
    float p22 = getClamped(xint + 1, yint + 1);
    float p32 = getClamped(xint + 2, yint + 1);

    // 4th row
    float p03 = getClamped(xint - 1, yint + 2);
    float p13 = getClamped(xint + 0, yint + 2);
    float p23 = getClamped(xint + 1, yint + 2);
    float p33 = getClamped(xint + 2, yint + 2);

    // interpolate bi-cubically!
    float row0 = cubicHermite(p00, p10, p20, p30, xfract);
    float row1 = cubicHermite(p01, p11, p21, p31, xfract);
    float row2 = cubicHermite(p02, p12, p22, p32, xfract);
    float row3 = cubicHermite(p03, p13, p23, p33, xfract);
    float value = cubicHermite(row0, row1, row2, row3, yfract);

    // constrin to minmax as interpolation can result in overflow to less than zero
    return MOMaths.constrain(value, 0f,1f);

  }

  // currently the only method availble, as the other is too complex for all cases
  // eg sale down in x but up in y
  FloatImage getResizedImageCopy(float scale){
    int newXDim = (int)(xdim*scale);
    int newYDim = (int)(ydim*scale);
    return getResizedImageCopy(newXDim, newYDim);
  }



  private FloatImage getResizedImageCopy(int newXDim, int newYDim){
    String mode = "";
    if((newXDim < 1) || (newXDim == xdim)) {
		return null;
	}
    if(newXDim < xdim){ mode = "downscale";}
    if(newXDim > xdim){ mode = "upscale";}

    if(mode == "upscale"){
      return upScaleImage(newXDim,newYDim);
    }

    if(mode == "downscale"){
      return downScaleImage(newXDim,newYDim);
    }

    return null;
  }

  FloatImage upScaleImage(int newXDim, int newYDim){
    FloatImage targetImage = new FloatImage(newXDim, newYDim);
      for(int y = 0; y < newYDim; y++){
        for(int x = 0; x < newXDim; x++){
          float px = x / (float) newXDim;
          float py = y / (float) newYDim;
          float newval = this.getPixelBicubic(px,py);
          targetImage.set(x,y,newval);

        }
      }

    return targetImage;
  }



  FloatImage downScaleImage(int newXDim, int newYDim){
      // use aggrigated approach
      FloatImage smallerImage = this.createHalfSizeCopy();
      FloatImage biggerImage = new FloatImage(this);

      while(true){
        if( smallerImage.xdim > newXDim){
          biggerImage = new FloatImage(smallerImage);
          smallerImage = smallerImage.createHalfSizeCopy();
        } else {
          break;
        }
        if( smallerImage.xdim < 1){ break; }
      }

      // so by now biggerImage is either a copy of the sourceimage, or double the size of the smaller image
      // and the smaller image is the nearest divide by 2 size of the soure image which
      // is smaller than the source image

      //int smallerXDim  = smallerImage.getWidth();
      //int biggerXDim   = biggerImage.getWidth();
      //println("This width ", xdim, " target width ", newXDim, " smaller width ", smallerXDim, " Bigger width ", biggerXDim);
      float blendVal = MOMaths.norm( newXDim, smallerImage.xdim, biggerImage.xdim);

      // now create a new image of the desired size and
      // iterate over it using blinier (to start with)
      FloatImage targetImage = new FloatImage(newXDim, newYDim);
      for(int y = 0; y < newYDim; y++){
        for(int x = 0; x < newXDim; x++){
          float px = x / (float) newXDim;
          float py = y / (float) newYDim;
          float smallerImageVal = smallerImage.getPixelBilin_Normalized(px,py);
          float biggerImageVal = biggerImage.getPixelBilin_Normalized(px,py);

          float blendedVal = MOMaths.lerp(blendVal,smallerImageVal,biggerImageVal);
          targetImage.set(x,y,blendedVal);

        }
      }

      return targetImage;

  }


  FloatImage createHalfSizeCopy(){
    // will round down odd sized dims to make an exact pixel num
    int newXDim = (int)(xdim/2.0);
    int newYDim = (int)(ydim/2.0);
    FloatImage halfSizeImage = new FloatImage(newXDim, newYDim);
    for(int y = 0; y < newYDim; y++){
      for(int x = 0; x < newXDim; x++){
        float boxsamp = get2by2BoxSample(x*2,y*2);
        halfSizeImage.set(x,y,boxsamp);
      }
    }
  return halfSizeImage;
  }

  float get2by2BoxSample(int x, int y){
    float v00 = getClamped(x,y);
    float v10 = getClamped(x+1,y);
    float v01 = getClamped(x,y+1);
    float v11 = getClamped(x+1,y+1);
    return (v00+v10+v01+v11)/4.0f;
  }
  ////////////////////////////////
  // other filtering
  ////////////////////////////////
  FloatImage getConvolved(String type){
    ConvolutionFilter conv = new ConvolutionFilter(type);
    FloatImage outputImage = new FloatImage(this.xdim,this.ydim);
    for(int y = 0; y < this.ydim; y++){
      //println("convolving line ",y, " of ",this.ydim);
      for(int x = 0; x < this.xdim; x++){
          float convVal = conv.convolveFloatPixel(x,y,this);
          outputImage.set(x,y,convVal);
        }
      }// end y loop

    return outputImage;
  }

  FloatImage getGaussianBlur(int kernalsize, float sd){
    ConvolutionFilter conv = new ConvolutionFilter("gaussianblur");
    conv.createGaussianKernal(kernalsize,sd);
    FloatImage outputImage = new FloatImage(this.xdim,this.ydim);
    for(int y = 0; y < this.ydim; y++){
      //println("convolving line ",y, " of ",this.ydim);
      for(int x = 0; x < this.xdim; x++){
          float convVal = conv.convolveFloatPixel(x,y,this);
          outputImage.set(x,y,convVal);
        }
      }// end y loop

    return outputImage;
  }


  public void normalize(){
    // equalises the image so all values are in range 0..1

    updateExtrema();
    //println("Equalising before ", extrema.toStr());
    int len = getArrayLength();
    for(int i = 0; i < len; i++){
      float originalval = floatArray[i];
      if(isMaskValue(originalval)) {
		continue;
	}
      float norm = MOMaths.map(originalval,extrema.limit1,extrema.limit2,0.0f,1.0f);
      floatArray[i] = norm;
      //if( i%1000 == 0) println("val is", minval, maxval, originalval, eqval);
    }
    updateExtrema();
    //println("Equalising after ", extrema.toStr());
  }

  FloatImage copy(){
    FloatImage cpy = new FloatImage(this);
    return cpy;
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // load save  floating point data. The first two numbers are the width and height of the image
  //
  public void saveFloatData(String fileName){
	 // SecondsTimer t = new SecondsTimer();

    float[] tempArray = null;
    try{
        RandomAccessFile aFile = new RandomAccessFile(fileName, "rw");
        FileChannel outChannel = aFile.getChannel();

        //one float 4 bytes
        ByteBuffer buf = ByteBuffer.allocate(4* (xdim * ydim + 2));
        buf.clear();

        tempArray = new float[xdim*ydim + 2];
        tempArray[0] = xdim;
        tempArray[1] = ydim;
        int len = xdim*ydim;
        for(int i = 0; i < len; i++)
            {
            tempArray[i+2] = floatArray[i];
            }

        buf.asFloatBuffer().put(tempArray);
        outChannel.write(buf);
        outChannel.close();
        aFile.close();
    }
    catch (IOException ex) {
        System.err.println(ex.getMessage());
        return;
    }
    //println("saved float array back ", xdim, ydim, " value index 0,1,15 = ", tempArray[0],",",tempArray[1],",",tempArray[15]);
    //println("float data saved took ", t.getTimeSinceStart() );
  }

  void loadFloatData(String fileName){
	  //SecondsTimer t = new SecondsTimer();

    try{
       InputStream input = new FileInputStream(fileName);
       DataInputStream inst = new DataInputStream(input);


       int w = (int)inst.readFloat();
       int h = (int)inst.readFloat();

       createNew(w,h);
       inst.close();
       input.close();

       RandomAccessFile rFile = new RandomAccessFile(fileName, "rw");
       FileChannel inChannel = rFile.getChannel();
       ByteBuffer buf_in = ByteBuffer.allocate((w*h+2)*4);

       //buf_in.clear();

       inChannel.read(buf_in);
       //FloatBuffer fb = FloatBuffer.allocate(w*h + 2);
       float[] tempArray = new float[w*h + 2];
       buf_in.rewind();
       buf_in.asFloatBuffer().get(tempArray);

       inChannel.close();
       rFile.close();


       for(int n = 0; n < w*h; n++){
            floatArray[n] = tempArray[n+2];
        }

    } catch (IOException e) {
      System.err.println(e.getMessage());
      return;
    }

    updateExtrema();
    //println("loaded the float array back ", xdim, ydim, " value index 13 = ", floatArray[13]);
    //println("float data load took ", t.getTimeSinceStart() );
  }


  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // load 16it greyscale png
  //
  void load16BitPNG(String fileName){
    BufferedImage img = null;
    try {
    img = ImageIO.read(new File(fileName));
    } catch (IOException e) {
      //println("problems loading", fileName);
      return;
    }

    int w = img.getWidth();
    int h = img.getHeight();
    //int type = img.getType();
    //println("Loaded Depth image - image dims", w, h, "of type", type);

    createNew(w, h);// creates a new FloatImage buffer
    DataBufferUShort buffer = (DataBufferUShort) img.getRaster().getDataBuffer(); // Safe cast as img is of type TYPE_USHORT_GRAY

    // Conveniently, the buffer already contains the data array
    //short[] arrayUShort = buffer.getData();
    //int[][] array = new int[w][h];

    //Histogram hist = new Histogram(10,0.0,1.0);


    int i = 0;
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            //array[x][y] = buffer.getElem(x + y * w);
            int val = buffer.getElem(x + y * w);
            float fraw = val/65536.0f;
            floatArray[i] = fraw;
            i++;

        }
    }
    //hist.printReport();

  }// end of load16BitPNG


  ///////////////////////////////////////////////////////////////////////////////////////////////////
  // save 16it greyscale png
  //
  void save16BitPNG(String fileName){

    File fpointer = new File(fileName);
    DataBufferUShort shortbuffer = new DataBufferUShort(xdim * ydim);

    //Histogram hist = new Histogram(10,0.0,1.0);
    for (int y = 0; y < ydim; y++) {
       //println("copying line ", y);
        for (int x = 0; x < xdim; x++) {
          float fval = get(x,y);
          //hist.set(fval);

          char ival = (char)(fval*65535.0);
          int index = y*xdim + x;
          shortbuffer.setElem(index,ival);

        }

    }

    // Create signed 16 bit data buffer, and compatible sample model
    SampleModel sampleModel = new ComponentSampleModel(DataBuffer.TYPE_USHORT, xdim, ydim, 1, xdim, new int[] {0});

    // Create a raster from sample model and data buffer
    WritableRaster raster = Raster.createWritableRaster(sampleModel, shortbuffer, null);

    // Create a 16 bit signed gray color model
    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    ColorModel colorModel = new ComponentColorModel(colorSpace, false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);

    // Finally create the signed 16 bit image
    BufferedImage image = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);


    try {
        ImageIO.write(image, "png", fpointer);
    } catch (IOException e) {
        e.printStackTrace();
    }
    //hist.printReport();

  }// end save 16bit png





}// end float image class

