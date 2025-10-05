package MOUtils;

import java.util.Arrays;

///////////////////////////////////////////////////////////////////////////////////////////
//A KeyValuePair contains a Key string, which identifies the contained value
//The value can be Boolean, Integer, Float, String or Vector
//A Vector is a list of float values of any length.
//The KeyValuePair contains just one key and an initialised Data Type , of the type specified by TYPE
//Rules about storing strings - you cannot use the words 'true' or 'false' or the '%' character when storing strings, as these are used to determine booleans and vector types
public class KeyValuePair {
	public static final int NOTSET = 0;
	public static final int BOOLEAN = 1;
	public static final int INTEGER = 2;
	public static final int FLOAT = 3;
	public static final int STRING = 4;
	public static final int VECTOR = 5;
	
	String theKey;

	// the type of data set
	int TYPE = NOTSET;

	// the values; only one of these should be set
	boolean bval = false;
	int ival = 0;
	float fval = 0;
	String sval = "";
	float[] vval;
	
	public KeyValuePair() {
	}

	public KeyValuePair(String k, boolean v) {
		set(k, v);
	}

	public KeyValuePair(String k, int v) {
		set(k, v);
	}

	public KeyValuePair(String k, float v) {
		set(k, v);
	}

	public KeyValuePair(String k, String v) {
		set(k, v);
	}
	
	public KeyValuePair(String k, float[] v) {
		set(k, v);
	}

	public KeyValuePair copy() {
		KeyValuePair kvpcopy = new KeyValuePair();
		kvpcopy.theKey = new String(this.theKey);
		kvpcopy.TYPE = this.getTYPE();
		kvpcopy.bval = this.bval;
		kvpcopy.ival = this.ival;
		kvpcopy.fval = this.fval;
		kvpcopy.sval = new String(this.sval);
		if(vval!=null) kvpcopy.vval = vval.clone();
		return kvpcopy;

	}

	public String getStr() {
		String s = "Key " + theKey;
		if (getTYPE() == BOOLEAN)
			s = s + " Value : Boolean : " + this.bval;
		if (getTYPE() == INTEGER)
			s = s + " Value : Int : " + this.ival;
		if (getTYPE() == FLOAT)
			s = s + " Value : Float : " + this.fval;
		if (getTYPE() == STRING)
			s = s + " Value : String : " + this.sval;
		if (getTYPE() == VECTOR)
			s = s + " Value : Vector : " + getVectorValueString();
		
		return s;
	}
	
	

	boolean equals(KeyValuePair other) {
		if (this.theKey.equals(other.theKey) == false)
			return false;
		if (getTYPE() == BOOLEAN && this.bval == other.bval)
			return true;
		if (getTYPE() == INTEGER && this.ival == other.ival)
			return true;
		if (getTYPE() == FLOAT && this.fval == other.fval)
			return true;
		if (getTYPE() == STRING && stringValueEquals(other.sval))
			return true;
		if (getTYPE() == VECTOR && Arrays.equals(vval, other.vval))
			return true;
		return false;
	}

	boolean stringValueEquals(String otherString) {
		// we need this more complex expression for strings because they can support a wild-card "*"
		// if either of the strings is a wild card, then the test returns true. 
		if (sval.equals(otherString))
			return true;
		if (sval.equals("*"))
			return true;
		if (otherString.equals("*"))
			return true;
		return false;
	}
	
	public boolean isSameVariable(KeyValuePair other) {
		// returns true if the key is the same as other ,and they are of the same type.
		// Used in replacing existing values in KVPLists
		if(other.theKey.equals(this.theKey)  && other.getTYPE() == this.getTYPE() ) return true;
		return false;
	}

//////////////////////////////////////////////////////
// setting methods
	public void set(String k, boolean v) {
		TYPE = BOOLEAN;
		theKey = k;
		bval = v;

	}

	void set(String k, int v) {
		//System.out.println("setting integer " + k + " ," + v);
		TYPE = INTEGER;
		theKey = k;
		ival = v;

	}

	void set(String k, float v) {
		//System.out.println("setting float " + k + " ," + v);
		TYPE = FLOAT;
		theKey = k;
		fval = v;

	}

	void set(String k, String v) {
		TYPE = STRING;
		if (v.contentEquals("true") || v.contentEquals("false")) {
			System.out.println(
					"KeyValuePair:setString - cannot set a string to *true* or *false*, as these are reserved for booleans");
			return;
		}
		if (v.contains("%")) {
			System.out.println(
					"KeyValuePair:setString - cannot set a using the *%* symbol, as these are reserved for seperating vector values");
			return;
		}
		if (v.contentEquals(""))
			v = " ";

		theKey = k;
		sval = v;

	}
	
	void set(String k, float[] f) {
		TYPE = VECTOR;
		theKey = k;
		vval = f.clone();
	}

	void setWithPairString(String keyVal) {
		String[] s_pair = splitPairIntoKeyValue(keyVal);
		theKey = s_pair[0];
		String v = s_pair[1];
		setValueWithUnknownDataType(v);

	}

	//////////////////////////////////////////////////////
	// getting methods
	public String getKey() {
		return theKey;
	}

	boolean keyIs(String query) {
		return theKey.equals(query);
	}

	public int getType() {
		return getTYPE();
	}

	public boolean getBoolean() {
		return bval;
	}

	public int getInt() {
		return ival;
	}

	public float getFloat() {
		return fval;
	}

	public String getString() {
		return sval;
	}
	
	public float[] getVector() {
		return vval;
	}
	
///////////////////////////////////////////////////
//private methods

	String getAsKeyValueString() {
		if (getTYPE() == NOTSET) {
			return "";
		}
		if (getTYPE() == BOOLEAN) {
			return theKey + ":" + bval;
		}
		if (getTYPE() == INTEGER) {
			return theKey + ":" + ival;
		}
		if (getTYPE() == FLOAT) {
			return theKey + ":" + fval;
		}
		if (getTYPE() == STRING) {
			return theKey + ":" + sval;
		}
		if (getTYPE() == VECTOR) {
			
			return theKey + ":" + getVectorValueString();
		}
		return "";
	}
	
	
	String getVectorValueString() {
		String fstring = "";
		for(int n = 0; n < vval.length; n++) {
			float thisFloat = vval[n];
			fstring += thisFloat;
			if(n < vval.length - 1) fstring += "%";
		}
		return fstring;
	}


	String[] splitLineIntoPairs(String assembled) {
		return assembled.split(",");
	}

	void setValueWithUnknownDataType(String v) {

		if (isBooleanDataType(v)) {
			bval = Boolean.parseBoolean(v);
			TYPE = BOOLEAN;
			return;
		}
		if (isIntegerDataType(v)) {
			ival = Integer.parseInt(v);
			TYPE = INTEGER;
			return;
		}
		if (isFloatDataType(v)) {
			fval = Float.parseFloat(v);
			TYPE = FLOAT;
			return;
		}
		if (isVectorDataType(v)) {
			vval = parseVectorData(v);
			TYPE = VECTOR;
			return;
		}

		// if you get here, it is a string
		sval = v;
		TYPE = STRING;

	}

	boolean isBooleanDataType(String str) {
		if (str.equals("true") || str.equals("false"))
			return true;
		return false;
	}

	boolean isIntegerDataType(String str) {
		try {
			int i = Integer.valueOf(str);
			return true;
		} catch (NumberFormatException e) {
			// Not an Integer, try the next one
		}
		return false;
	}

	boolean isFloatDataType(String str) {
		try {
			float f = Float.valueOf(str);
			return true;
		} catch (NumberFormatException e) {
			// Not an Integer, try the next one
		}
		return false;
	}
	
	boolean isVectorDataType(String str) {
		// Vectors are stored thus, using the percent symbol to separate the numbers
		// "KeyName:3.141%1245.6%5678.234"
		 return str.contains("%");
	}
	
	float[] parseVectorData(String vstring) {
		String[] floatstrings = vstring.split("%");
		int num = floatstrings.length;
		float [] outfloats= new float[num];
		for(int n = 0; n < num; n++) {
			outfloats[n] = Float.valueOf(  floatstrings[n] );
		}
		return outfloats;
	}

	String[] splitPairIntoKeyValue(String pair) {
		// because some strings may contain ':' char, especially paths, we need to catch these.
		String[] initialSplits = pair.split(":");
		if(initialSplits.length==2) return initialSplits;
		
		// if you get here, there is a value string containing ':'
		String[] split2 = new String[2];
		split2[0] = initialSplits[0];
		int chlen = split2[0].length() + 1;// need to remove the key and the first ':' in the pair string
		split2[1] = pair.substring(chlen);
		return split2;
		
	}
	public void setType(int t) {
		TYPE = t;
	}

	public int getTYPE() {
		return TYPE;
	}

}// end class