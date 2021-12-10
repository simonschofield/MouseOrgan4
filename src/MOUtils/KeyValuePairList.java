package MOUtils;
import java.util.ArrayList;

///////////////////////////////////////////////////////////////////////////////////////////
// Useful for storing data and saving/loading to file as CSV strings
// If a duplicate key is added, then the old value is replaced by the new value
//
public class KeyValuePairList{

  ArrayList<KeyValuePair> keyValuePairs = new ArrayList<KeyValuePair>();

  public KeyValuePairList() {}

  public KeyValuePairList copy(){
    // this is a deep copy to avoid sharing references between objects
    KeyValuePairList kvplCopy = new KeyValuePairList();
    for(KeyValuePair kvp: keyValuePairs){
      kvplCopy.addKeyValuePair( kvp.copy() );
      
    }
    return kvplCopy;
  }
  
  // for comparing a KVPList against this one, to see if it contains an equal KVP - i.e. both K and V are the same
  public boolean containsEqual(KeyValuePairList otherKvpList) {
	  for(KeyValuePair otherKvp: otherKvpList.keyValuePairs) {
		  if(containsEqual(otherKvp)) return true;
	  }
	  return false;
  }
  
  public boolean containsEqual(KeyValuePair otherKvp) {
	  for(KeyValuePair thisKvp: keyValuePairs) {
		  if(thisKvp.equals(otherKvp)) return true;
	  }
	  return false;
  }
  
  public void printMe(){
    for(KeyValuePair kvp: keyValuePairs){
     System.out.println(kvp.getStr());
    }
  }
  //////////////////////////////////////////////////////
  // setting methods

  public void addKeyValue(String k, boolean v){
    KeyValuePair kv = new KeyValuePair();
    kv.set(k,v);
    addKeyValuePair(kv);
  }

  void addKeyValue(String k, int v){
    KeyValuePair kv = new KeyValuePair();
    kv.set(k,v);
    addKeyValuePair(kv);
  }

  public void addKeyValue(String k, float v){
    KeyValuePair kv = new KeyValuePair();
    kv.set(k,v);
    addKeyValuePair(kv);
  }

  public void addKeyValue(String k, String v){
    KeyValuePair kv = new KeyValuePair();
    kv.set(k,v);
    addKeyValuePair(kv);
  }


  public void addKeyValuePair(KeyValuePair kv){
    keyValuePairs.add(kv);
  }

  public void removeKeyValue(String kquery){
    ArrayList<KeyValuePair> temp = new ArrayList<KeyValuePair>();
    for(KeyValuePair kv: keyValuePairs){
      if(kv.keyIs(kquery)==false) temp.add(kv);
    }
    keyValuePairs = temp;
  }
  
  public void removeAll(){
    //
    keyValuePairs = new ArrayList<KeyValuePair>();
  }
  
  //////////////////////////////////////////////////////
  // general information  methods
  public int getNumItems(){
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
  public boolean getBoolean(String kquery) {
    KeyValuePair kv = findKeyValue(kquery);
    if(kv==null) return false;
    return kv.getBoolean();
  }

  public int getInt(String kquery) {
    KeyValuePair kv = findKeyValue(kquery);
    if(kv==null) return 0;
    return kv.getInt();
  }

  public float getFloat(String kquery) {
    KeyValuePair kv = findKeyValue(kquery);
    if(kv==null) return 0;
    return kv.getFloat();
  }

  public String getString(String kquery) {
    KeyValuePair kv = findKeyValue(kquery);
    if(kv==null) return "";
    return kv.getString();
  }



  public KeyValuePair findKeyValue(String kquery){
    for(KeyValuePair kv: keyValuePairs){
      if( kv.keyIs(kquery) ) return kv;
    }
    return null;
  }

  //////////////////////////////////////////////////////
  // CSV-based data retreval and setting
  //
  public void ingestCSVLine(String csvLine){
    String[] pairs = csvLine.split(",");
    for(String pair: pairs){
      KeyValuePair kv = new KeyValuePair();
      kv.setWithPairString(pair);
      addKeyValuePair(kv);
    }
  }
  
  public String getAsCSVString(boolean addEndComma){
    String outStr = "";
    for(KeyValuePair kv: keyValuePairs){
      if(outStr.equals("")==false) outStr = outStr + ",";
      outStr = outStr + kv.getAsKeyValueString();
    }
    if(addEndComma) outStr = outStr + ",";
    return outStr;
  }
  

  public String getAsCSVLine(){
    String outStr = getAsCSVString(false);
    outStr = outStr + "\n";
    return outStr;
  }

}


