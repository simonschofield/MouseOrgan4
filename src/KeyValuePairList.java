import java.util.ArrayList;

///////////////////////////////////////////////////////////////////////////////////////////
// Useful for storing data and saving/loading to file as CSV strings
// If a duplicate key is added, then the old value is replaced by the new value
//
class KeyValuePairList{

  ArrayList<KeyValuePair> keyValuePairs = new ArrayList<KeyValuePair>();


  KeyValuePairList copy(){
    // this is a deep copy to avoid sharing references between objects
    KeyValuePairList kvplCopy = new KeyValuePairList();
    for(KeyValuePair kvp: keyValuePairs){
      kvplCopy.addKeyValuePair( kvp.copy() );
      
    }
    return kvplCopy;
  }
  
  boolean containsEqual(KeyValuePair otherKvp) {
	  for(KeyValuePair thisKvp: keyValuePairs) {
		  if(thisKvp.equals(otherKvp)) return true;
	  }
	  return false;
  }
  
  void printMe(){
    for(KeyValuePair kvp: keyValuePairs){
     // tbd
      
    }
  }
  //////////////////////////////////////////////////////
  // setting methods

  void addKeyValue(String k, boolean v){
    KeyValuePair kv = new KeyValuePair();
    kv.set(k,v);
    addKeyValuePair(kv);
  }

  void addKeyValue(String k, int v){
    KeyValuePair kv = new KeyValuePair();
    kv.set(k,v);
    addKeyValuePair(kv);
  }

  void addKeyValue(String k, float v){
    KeyValuePair kv = new KeyValuePair();
    kv.set(k,v);
    addKeyValuePair(kv);
  }

  void addKeyValue(String k, String v){
    KeyValuePair kv = new KeyValuePair();
    kv.set(k,v);
    addKeyValuePair(kv);
  }


  void addKeyValuePair(KeyValuePair kv){
    removeKeyValue( kv.getKey() );
    keyValuePairs.add(kv);
  }

  void removeKeyValue(String kquery){
    ArrayList<KeyValuePair> temp = new ArrayList<KeyValuePair>();
    for(KeyValuePair kv: keyValuePairs){
      if(kv.keyIs(kquery)==false) temp.add(kv);
    }
    keyValuePairs = temp;
  }
  
  void removeAll(){
    //
    keyValuePairs = new ArrayList<KeyValuePair>();
  }
  
  //////////////////////////////////////////////////////
  // general information  methods
  int getNumItems(){
    return keyValuePairs.size();
  }
  
  KeyValuePair getItem(int n){
    return keyValuePairs.get(n);
  }

  boolean keyExists(String kquery){
    for(KeyValuePair kv: keyValuePairs){
      if(kv.keyIs(kquery)) return true;
    }
    return false;
  }

  //////////////////////////////////////////////////////
  // get  methods
  boolean getBoolean(String kquery) {
    KeyValuePair kv = findKeyValue(kquery);
    if(kv==null) return false;
    return kv.getBoolean();
  }

  int getInt(String kquery) {
    KeyValuePair kv = findKeyValue(kquery);
    if(kv==null) return 0;
    return kv.getInt();
  }

  float getFloat(String kquery) {
    KeyValuePair kv = findKeyValue(kquery);
    if(kv==null) return 0;
    return kv.getFloat();
  }

  String getString(String kquery) {
    KeyValuePair kv = findKeyValue(kquery);
    if(kv==null) return "";
    return kv.getString();
  }



  KeyValuePair findKeyValue(String kquery){
    for(KeyValuePair kv: keyValuePairs){
      if( kv.keyIs(kquery) ) return kv;
    }
    return null;
  }

  //////////////////////////////////////////////////////
  // CSV-based data retreval and setting
  //
  void ingestCSVLine(String csvLine){
    String[] pairs = csvLine.split(",");
    for(String pair: pairs){
      KeyValuePair kv = new KeyValuePair();
      kv.setWithPairString(pair);
      addKeyValuePair(kv);
    }
  }
  
  String getAsCSVString(boolean addEndComma){
    String outStr = "";
    for(KeyValuePair kv: keyValuePairs){
      if(outStr.equals("")==false) outStr = outStr + ",";
      outStr = outStr + kv.getAsKeyValueString();
    }
    if(addEndComma) outStr = outStr + ",";
    return outStr;
  }
  

  String getAsCSVLine(){
    String outStr = getAsCSVString(false);
    outStr = outStr + "\n";
    return outStr;
  }

}


///////////////////////////////////////////////////////////////////////////////////////////
// A KeyValuePair contains a Key string, which identifies the contained value
// The value can be Boolean, Integer, Float or String
// The KeyValuePair contains just one valid value, of the type specified by TYPE
class KeyValuePair {
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


  KeyValuePair() {
  }

  KeyValuePair copy(){
    KeyValuePair kvpcopy = new KeyValuePair();
    kvpcopy.theKey = new String(this.theKey);
    kvpcopy.TYPE = this.TYPE;
    kvpcopy.bval = this.bval;
    kvpcopy.ival = this.ival;
    kvpcopy.fval = this.fval;
    kvpcopy.sval = new String(this.sval);
    return kvpcopy;
    
  }
  
  void printMe(){
    
  }
  
  boolean equals(KeyValuePair other) {
	  if(this.theKey.equals(other.theKey)==false) return false;
	  if (TYPE == BOOLEAN && this.bval == other.bval) return true;
	  if (TYPE == INTEGER  && this.ival == other.ival) return true;
	  if (TYPE == FLOAT  && this.fval == other.fval) return true; 
	  if (TYPE == STRING  && this.sval.equals(other.sval)) return true; 
	  return false;
  }
  
  //////////////////////////////////////////////////////
  // setting methods
  void set(String k, boolean v) {
    TYPE = BOOLEAN;
    theKey = k;
    bval = v;
    
  }

  void set(String k, int v) {
    TYPE = INTEGER;
    theKey = k;
    ival = v;
    
  }

  void set(String k, float v) {
    TYPE = FLOAT;
    theKey = k;
    fval = v;
    
  }

  void set(String k, String v) {
    TYPE = STRING;
    if(v.contentEquals("true") || v.contentEquals("false")) {
      System.out.println("KeyValuePair:setString - cannot set a string to *true* or *false*, as these are reserved for booleans");
      return;
    }
    if(v.contentEquals("")) v = " ";
    
    theKey = k;
    sval = v;
    
  }

  void setWithPairString(String keyVal){
    String[] s_pair = splitPairIntoKeyValue(keyVal);
    theKey = s_pair[0];
    String v = s_pair[1];
    setValueWithUnknownDataType(v);

  }
  
  
  //////////////////////////////////////////////////////
  // getting methods
  String getKey(){
    return theKey;
  }
  
  boolean keyIs(String query){
    return theKey.equals(query);
  }

  int getType() {
    return TYPE;
  }

  
  boolean getBoolean() {
    return bval;
  }

  int getInt() {
    return ival;
  }

  float getFloat() {
    return fval;
  }

  String getString() {
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

    if(isBooleanDataType(v)) {
      bval = Boolean.parseBoolean(v);
      TYPE = BOOLEAN;
      return;
    }
    if(isIntegerDataType(v)) {
      ival = Integer.parseInt(v);  
      TYPE = INTEGER;
      return;
    }
    if(isFloatDataType(v)) {
      fval = Float.parseFloat(v); 
      TYPE = FLOAT;
      return;
    }

    sval = v;
    TYPE = STRING;

  }


  boolean isBooleanDataType(String str) {
    if(str.equals("true") || str.equals("false")) return true;
    return false;
  }


  boolean isIntegerDataType(String str) {
    try {
      int i = Integer.valueOf(str);
      return true;
    } catch(NumberFormatException e) {
      // Not an Integer, try the next one
    }
    return false;
  }


  boolean isFloatDataType(String str) {
    try {
      float f = Float.valueOf(str);
      return true;
    } catch(NumberFormatException e) {
      // Not an Integer, try the next one
    }
    return false;
  }


  String[] splitPairIntoKeyValue(String pair) {
    return pair.split(":");
  }
  
  
}// end class