package MOZZZ_Depricated;
import MOMaths.PVector;

public class Func {
	
	public static boolean printOn = true;
	
	public static PVector vec(float x, float y, float z) {
		
		return new PVector(x,y,z);
	}
	
	
	public static void println(String s) {
		if(printOn) System.out.println(s);
	}
	
	public static void print(String s) {
		if(printOn) System.out.print(s);
	}
	
	
}
