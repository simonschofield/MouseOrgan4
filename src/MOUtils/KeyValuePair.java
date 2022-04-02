package MOUtils;

///////////////////////////////////////////////////////////////////////////////////////////
//A KeyValuePair contains a Key string, which identifies the contained value
//The value can be Boolean, Integer, Float or String
//The KeyValuePair contains just one valid value, of the type specified by TYPE
public class KeyValuePair {
	static final int NOTSET = 0;
	static final int BOOLEAN = 1;
	static final int INTEGER = 2;
	static final int FLOAT = 3;
	static final int STRING = 4;

	String theKey;

	// the type of data set
	private int TYPE = NOTSET;

	// the values; only one of these should be set
	boolean bval = false;
	int ival = 0;
	float fval = 0;
	String sval = "";

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

	public KeyValuePair copy() {
		KeyValuePair kvpcopy = new KeyValuePair();
		kvpcopy.theKey = new String(this.theKey);
		kvpcopy.TYPE = this.TYPE;
		kvpcopy.bval = this.bval;
		kvpcopy.ival = this.ival;
		kvpcopy.fval = this.fval;
		kvpcopy.sval = new String(this.sval);
		return kvpcopy;

	}

	public String getStr() {
		String s = "Key " + theKey;
		if (TYPE == BOOLEAN)
			s = s + " Value : Boolean : " + this.bval;
		if (TYPE == INTEGER)
			s = s + " Value : Int : " + this.ival;
		if (TYPE == FLOAT)
			s = s + " Value : Float : " + this.fval;
		if (TYPE == STRING)
			s = s + " Value : String : " + this.sval;
		return s;
	}

	boolean equals(KeyValuePair other) {
		if (this.theKey.equals(other.theKey) == false)
			return false;
		if (TYPE == BOOLEAN && this.bval == other.bval)
			return true;
		if (TYPE == INTEGER && this.ival == other.ival)
			return true;
		if (TYPE == FLOAT && this.fval == other.fval)
			return true;
		if (TYPE == STRING && stringValueEquals(other.sval))
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
		if (v.contentEquals(""))
			v = " ";

		theKey = k;
		sval = v;

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
		return TYPE;
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

	String getAsKeyValueString() {
		if (TYPE == NOTSET) {
			return "";
		}
		if (TYPE == BOOLEAN) {
			return theKey + ":" + bval;
		}
		if (TYPE == INTEGER) {
			return theKey + ":" + ival;
		}
		if (TYPE == FLOAT) {
			return theKey + ":" + fval;
		}
		if (TYPE == STRING) {
			return theKey + ":" + sval;
		}
		return "";
	}

///////////////////////////////////////////////////
// private methods
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

	String[] splitPairIntoKeyValue(String pair) {
		return pair.split(":");
	}

}// end class