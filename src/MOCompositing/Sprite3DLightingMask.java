package MOCompositing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import MOMaths.PVector;
import MOScene3D.ProjectedLight3D;
import MOSprite.Sprite;

public class Sprite3DLightingMask {
	/*
	int maskScale;
	BufferedImage lightingMask;
	ProjectedLight3D solidTexture;
	Sprite3DLightingMask(ProjectedLight3D solidTex, int maskScale){
		this.solidTexture = solidTex;
		this.maskScale = maskScale;
		
	}
	
	// this to be called just before the lighting is applied - i.e. after all geometric transforms
	void calculateLightingMask(Sprite sprt) {
		int buffWdth = (int)(sprt.getImageWidth()*maskScale);
		int buffHght = (int)(sprt.getImageHeight()*maskScale);
		if(buffWdth < 2 || buffHght < 2) {
			// too small to bother
			
		}
		
		lightingMask = new BufferedImage(buffWdth, buffHght, BufferedImage.TYPE_BYTE_GRAY);
		byte[] dstBuff = ((DataBufferByte) lightingMask.getRaster().getDataBuffer()).getData();
		
		for(int y = 0; y < buffHght; y++) {
			for(int x = 0; x < buffWdth; x++) {
				PVector spriteBufferPoint = new PVector(x,y);
				PVector docSpace = sprt.spriteBufferSpaceToDocSpace(spriteBufferPoint);
				byte val = (byte)(solidTexture.getValue01(docSpace, sprt.depth)*255);
				
				dstBuff[y*buffWdth + x] = val;
			}
		}
		// https://stackoverflow.com/questions/221830/set-bufferedimage-alpha-mask-in-java
			
	}
	*/
	
}
