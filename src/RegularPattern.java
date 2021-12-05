import java.awt.image.BufferedImage;
import java.util.ArrayList;

import ImageCollectionClasses.ImageSampleGroup;
import MOImageClasses.KeyImageSampler;
import MOMaths.PVector;
import MOMaths.QRandomStream;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// a regular pattern is a repeating pattern defined by a string sequence (e.g. "a","b","a","c")
// called a pattern row, and a Drop Offset, which defines dropPatternShunt - the offset from this pattern
// in the row beneath (so a dropPatternShunt of 1 would produce the sequence "b","a","c","a" in the
// next row down) and a dropFractionalOffset, which is the "half drop" amount x if the next row down, where 0.5 would
// create a diagonal pattern, rather than a square one
// The pattern sequence strings are used to access content collections from the content manager,
// and so draw the pattern

// The pattern is drawn as a user controlled iteration over the target image. At each iteration
// the method getNextPattern() returns the next sprite in-place, or null if there is nothing to be placed
// The method hasFinished() returns true when the pattern has finished


public class RegularPattern {

	// contains the image-elements used in the pattern
	ImageSampleGroup imageSampleGroup;
	
	// this is used for all stochastics and image selection if required.
	QRandomStream qRandomStream;

	//list of strings used to define a pattern
	//The name contained is NOT the short name of the image, but
	// a filter used to select one image from multiple images in the imageSampleGroup
	// So, if you have a pattern that goes "tree", "tree", "grass"
	// the image is found based on if the short name contains the substring "tree" or "grass" - there may be only one,
	// but this allows multiple trees and multiple grass images, adding to the richness of the pattern.
	ArrayList<String> patternNameSequence = new ArrayList<String>();


	//dropPatternShunt defines the whole pattern steps difference
	//between subsequent rows of pattern
	// 0 has no effect, (so row 1 the same as row 2)
	//1 shunts the next row on by one element etc...
	int dropPatternShunt = 0;

	// dropFractionalOffset shifts the location of the next row by this amount in a modular way
	// so 0.5 would place the next row half-stepped with the row above
	float dropFractionalOffset = 0;
	
	
	
	// these variables contain the current state of the pattern as it is being made
	boolean isStarted = false;
	boolean isFinished = false;
	// an integer number representing the count of steps in x and y
	int currentPatternStepCountX = 0;
	int currentPatternStepCountY = 0;
	// document space step sises in x and y
	float currentPatternDX = 0;
	float currentPatternDY = 0;
	// currentPatternPosition is the current point on the image being considered, in normalised coordinates
	PVector currentPatternPosition = new PVector(0,0);
	

	// define the region within the larger image where the pattern is applied
	KeyImageSampler activeRegion;


	// stochastics
	// Jiggle in x and y, 0 has no effect, 1 will randomly displace
	// the position by -1.0.1 pattern step in x or y
	float jiggleX = 0;
	float jiggleY = 0;

	RegularPattern(ImageSampleGroup isg, int rseed){
		imageSampleGroup = isg;
		qRandomStream = new QRandomStream(rseed);
	}


	void setPatternRow(String[] rowNames) {
		for(String name: rowNames) {
			patternNameSequence.add(name);
		}
	}

	void setDropOffset(int patternShunt, float drpOffset) {
		// a yDropPatternShunt of 0 has no effect
		// 1 with shunt the next row along 1
		// 2 will shunt the next row along 2 etc...
		dropPatternShunt = patternShunt;

		// the dropoffset us used to physically shift along one from from the next
		// and should be in the range of 0...1 only
		// where 0.5 will shift the position of the next row by 0.5 of a step in x
		dropFractionalOffset = drpOffset;
	}
	
	void setJiggle(float jigX, float jigY) {
		//copies the settings for the stochastic numbers
		// but uses THIS rand stream to initialise them, thereby maintaining random stream independence
		jiggleX = jigX;
		jiggleY = jigY;
		
	}
	
	void setActiveRegion(BufferedImage img) {
		activeRegion = new KeyImageSampler(img);
	}
	
	void setPatternDensity(int numInX, int numInY) {
		currentPatternDX = 1f/numInX;
		currentPatternDY = 1f/numInY;
	}
	
	ImageSprite getNextPatternStep() {
		if(isStarted == false) startPattern();
		
		//if( isInActiveRegion(currentPatternPosition) == false ) {
		//	advancePattern();
        //    return null;
        //  }
		
		
		while( isInActiveRegion(currentPatternPosition) == false ) {
			advancePattern();
			if(isFinished) return null;
          }
		
		
		String currentPatternStr = getCurrentPatternElement();
		
        //BufferedImage img = imageSampleGroup.getRandomImage(qRandomStream, currentPatternStr);
		int numImgs = imageSampleGroup.getNumImages();
		
		BufferedImage img = null;
		//imageSampleGroup.getRandomImage(qRandomStream, currentPatternStr);
		
		String nameContainsFilter = currentPatternStr;
		if(currentPatternStr == null) nameContainsFilter = "";
		if(nameContainsFilter == "") {
			int rnum = (int)qRandomStream.randRangeInt(0, numImgs-1);
			img = imageSampleGroup.getImage(rnum);
		} else {
		
		ArrayList<String> imageNames = imageSampleGroup.getImageNameList();
		ArrayList<String> filteredNamesFound = new ArrayList<String>();
		for (String thisName : imageNames) {
			if (thisName.contains(nameContainsFilter))
				filteredNamesFound.add(thisName);
		}
		
		int rnum = qRandomStream.randRangeInt(0, filteredNamesFound.size()-1);
		String foundName = filteredNamesFound.get(rnum);
		img = imageSampleGroup.getImage(foundName);
		}
		
		
		
        PVector p = currentPatternPosition.copy();
        p = addFractionalOffset(p);
        p = addJiggle(p);
        
        ImageSprite sprite = new ImageSprite(img, imageSampleGroup.getGroupOrigin(), imageSampleGroup.getGroupSizeInScene(), 1);
        sprite.setDocPoint(p);
        advancePattern();
        
        return sprite;
	}
	
	boolean isFinished() {
		return isFinished;
	}
	
	private void startPattern() {
		currentPatternPosition = new PVector(0,0);
		isStarted = true;
		isFinished = false;
	}
	
	void advancePattern() {
		
		currentPatternPosition.x += currentPatternDX;
		incrementPatternStepCount(1,0);
		if(currentPatternPosition.x > 1) {
			resetPatternStepCountX();
			currentPatternPosition.x = 0;
			currentPatternPosition.y += currentPatternDX;
			incrementPatternStepCount(0,1);
		}
		
		
		if(currentPatternPosition.y >= 1) {
			isFinished = true;
		}
		
	}
	
	
	PVector addJiggle(PVector currentPos) {
		float rdx = qRandomStream.randRangeF(-currentPatternDX/2, currentPatternDX/2) * jiggleX;
		float rdy = qRandomStream.randRangeF(-currentPatternDY/2, currentPatternDY/2) * jiggleY;
		return PVector.add(currentPos, new PVector(rdx,rdy));
	}
	
	
	PVector addFractionalOffset(PVector currentPos) {
	      //takes into consideration point shift due to dropOffset
	      float offsetX = (float) (((currentPatternStepCountY*dropFractionalOffset)%0.9999)*currentPatternDX);

	      return new PVector(currentPos.x + offsetX, currentPos.y);
	}
	    		  
	    		  
	String getCurrentPatternElement(){ 
		// calculated a cyclical number in the range 0... len(patternKeyList)-1
		//and returns that elemnt from the patternKeyList
		int patternLen = patternNameSequence.size();

		if(patternLen == 0) return "";

		if(patternLen == 1) {
			return patternNameSequence.get(0);
		}

		int offsetY = 0;
		if(dropPatternShunt==0) {
			offsetY = 0;
		}
		else{
			offsetY = (int)(currentPatternStepCountY+(dropPatternShunt)-1);
		}
		int currentPatternIndex = (currentPatternStepCountX+offsetY)%patternLen;

		return patternNameSequence.get(currentPatternIndex);
	}

	boolean isInActiveRegion(PVector p) {
		if(activeRegion == null) return true;
		float v = activeRegion.getValue01NormalisedSpace(p);
		if(v > 0.5f) return true;
		return false;
	}

	void resetPatternStepCountX() {
		currentPatternStepCountX = 0;
	}

	void incrementPatternStepCount(int dx,int dy){
		currentPatternStepCountY += dy;
		currentPatternStepCountX += dx;
	}

}

/******


## class RegularPattern draws a regular pattern (see below) to the mouseorgan canvas image
## The extent of the pattern and its overall shape is defined by a MOKeyImageRegionRect. i.e. a rect
## in P-space, and a bit map whose coordinate space is the extent of that rect.
## The desnity of the pattern is defined in terms of the WHOLE canvas image.

## a regular pattern is a repeating pattern defined by a string sequence (e.g. "a","b","a","c")
## called a pattern row, and a Drop Offset, whihc defines yDropPatternShunt - the offset from this pattern
## in the row benieth (so a yDropPatternShunt of 1 would produce the sequence "b","a","c","a" in the
## next row down) and a drpOffset, which is the "half drop" amount x if the next row down, where 0.5 would
## create a diagonal pattern, rather than a square one
## The pattern sequence strings are used to access content collections from the content manager,
## and so draw the pattern

class RegularPattern:
   
   def __init__(self, mo, contentManager):
      self.mouseOrganPointer = mo
      self.contentManagerPointer = contentManager

      # list of strings used to define a pattern
      # must be content name strings
      self.patternKeyList = []

      # YDropPatternShunt defines the whole pattern steps difference
      # between subsequent rows of pattern
      # 0 has no effect, (so row 1 the same as row 2)
      # 1 shunts the next row on by one element etc...
      self.YDropPatternShunt = 0

      # shifts the location of the next row by this amount in a modular way
      #so 0.5 would place the next row half-stepped with the row above
      self.dropOffset = 0
      
      self.currentPatternStep = [0,0]

      # define the region within the larger image where the pattern is applied
      self.activeRegion = 0
      
      
      # stochastics

      # Jiggle in x and y, 0 has no effect, 1 will randomly displace
      # the position by -1.0.1 pattern step in x or y
      self.jiggleX = 0
      self.jiggleY = 0

   def setPatternRow(self, patternList ):
      self.patternKeyList = patternList

   def setDropOffset(self, yDropPatternShunt, drpOffset):
      # a yDropPatternShunt of 0 has no effect
      # 1 with shunt the next row along 1
      # 2 will shunt the next row along 2 etc...
      self.YDropPatternShunt = yDropPatternShunt

      # the dropoffset us uswed to physicaly shift along one frow from the next
      # and hould be in the range of 0...1 only
      # where 0.5 will shift the postion of the next row by o.5 of a step in x
      self.dropOffset = drpOffset

   def setStochastics(self, jigX, jigY, varyScale = 0, varyRotation = 0):
      self.jiggleX = jigX
      self.jiggleY = jigY

   def setActiveRegion(self, rgnRect = (0,0,1,1), rgnBitmapFileName = None, ):
      self.activeRegion = MOKeyImages.MOKeyImageRegionRect()
      if(rgnBitmapFileName != None):
         self.activeRegion.loadImage(rgnBitmapFileName)
      self.activeRegion.setRegionRect(rgnRect)

   
   def drawPattern(self, canvasNumInX, canvasNumInY):
      
      ## iterate over regionRect
      regionRect = self.activeRegion.getRegionRect()
      startX = regionRect[0]
      startY = regionRect[1]
      endX = regionRect[2]
      endY = regionRect[3]

      # canvasNumInX and Y is the number of dots across the WHOLE IMAGE,
      # not the region rect
      # This is to make the desity of the pattern independednt of the
      # particular region size, and consistent over the whole image
      numInX = (int)(canvasNumInX * ((endX-startX)))
      numInY = (int)(canvasNumInY * ((endY-startY)))

      stepInX = 1/canvasNumInX
      stepInY = 1/canvasNumInY
      
      thisY = startY
      thisX = startX
      while(thisY < endY):
         self.resetPatternStepX()
         while(thisX < endX):
            currentPatternStr = self.getCurrentPatternElement()
            item = self.contentManagerPointer.getRandomRawImageFromContentGroup(currentPatternStr)
            p = self.adjustPoint(thisX,thisY, stepInX, stepInY)

            if( self.checkAgainstRegionBitmap(p) ):
               self.mouseOrganPointer.pasteImage(item, p)
            
            #print(str(p))
            self.incrementPatternStep(1,0)
            thisX += stepInX
         thisX = startX
         thisY += stepInY
         self.incrementPatternStep(0,1)


         
   ## private functions 
   def incrementPatternStep(self, inX,inY):
      #inX and inY should only ever be 0, or 1
       self.currentPatternStep[0] += inX
       self.currentPatternStep[1] += inY

   def resetPatternStepX(self):
      self.currentPatternStep[0] = 0

   def getCurrentPatternElement(self):
      # calculated a cyclical number in the range 0... len(patternKeyList)-1
      # and returns that elemnt from the patternKeyList
      patternLen = len(self.patternKeyList)

      if(patternLen == 1):
         return self.patternKeyList[0]
      
      if(self.YDropPatternShunt==0):
         offsetY = 0
      else:
         #offsetY = (int)((self.currentPatternStep[1]+(self.YDropPatternShunt-1))%patternLen)
         offsetY = (int)((self.currentPatternStep[1]+(self.YDropPatternShunt)-1))
      
      #print("offsetY",offsetY)
      
      currentPattern = (self.currentPatternStep[0]+offsetY)%patternLen
      
      return self.patternKeyList[currentPattern]

   def adjustPoint(self, x, y, stepInX, stepInY):
      # takes into consideration point shift due to dropOffset
      # and any stochastic jiggle
      numYStep = self.currentPatternStep[1]
      offsetX = ((numYStep*self.dropOffset)%0.9999)*stepInX
      x = x+offsetX

      # jiggle the points
      x = self.jiggleVal(x, self.jiggleX, stepInX)
      y = self.jiggleVal(y, self.jiggleY, stepInY)
      return (x,y)

   def jiggleVal(self, v, amt, maxAmt):
      jiggleAmt = amt*maxAmt
      r = MOUtils.randRangeF(-jiggleAmt,jiggleAmt)
      return v+r

   def checkAgainstRegionBitmap(self, cp):
      ##convert from parametric canvas space to
      ##parametric space within the regionRect
      ## if no bitmap has been defined then allow the point
      if(self.activeRegion.isImageSet() == False):
         return True
      
      pix = self.activeRegion.getTone(cp)
      if(pix < 0.5):
         return False
      return True
      

   ### useage
   #contentManager = MOContentManager.MOContentManager(mo)

   #grassclumpPath = "C:\\sample lib\\maps\\symbols\\grassclump"
   #dotlinesPath = "C:\\sample lib\\maps\\symbols\\dot lines"
   #contentManager.loadImageList(grassclumpPath, "grassclump")
   #contentManager.loadImageList(dotlinesPath, "dot lines")

   #patternmaker1 = RegularPattern(mo,contentManager)
   #patternList = ["grassclump", "grassclump"]
   #patternmaker1.setPatternRow(patternList)

   #rgnRect = (0.25,0.25,0.75,0.75)
   #regionFlNm = "blob.png"

   #patternmaker1.setActiveRegion(rgnRect,regionFlNm)
   #patternmaker1.setDropOffset(1, 0)

   #jiggleX = 0.2
   #jiggleY = 0.2
   #patternmaker1.setStochastics(jiggleX, jiggleY)

   #patternmaker1.drawPattern(20, 20)

   #mo.saveCanvasImage()






*/