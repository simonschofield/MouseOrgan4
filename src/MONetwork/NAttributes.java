package MONetwork;

import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;

//////////////////////////////////////////////////////////////////////////////////////
//NAttributes class
//
//Attributes and other variables common to all NNetwork items
//
public class NAttributes {
	private int id;
	static final int TYPE_UNDEFINED = 0;
	static final int TYPE_NPOINT = 1;
	static final int TYPE_NEDGE = 2;
	static final int TYPE_NREGION = 3;

	NNetwork theNetwork;
	//UniqueID uniqueID;

	private int thingInt = TYPE_UNDEFINED; 
	String thingString = "UNDEFINED";

	protected KeyValuePairList attributes = new KeyValuePairList();



	NAttributes(int thng, NNetwork networkref) {

		thingInt = thng;
		//uniqueID = theNetwork.getUniqueIDGenerator();
		theNetwork = networkref;
		if (thingInt == TYPE_NPOINT) thingString = "NPOINT";
		if (thingInt == TYPE_NEDGE) thingString = "NEDGE";
		if (thingInt == TYPE_NREGION) thingString = "NREGION";
		setID();
	}

	int getThing() {
		return thingInt;
	}

	String getThingString() {
		return thingString;
	}



	void setID() {
		// you can only set the id automatically with this 
		// method, which uses the auto-incremented number
		id = theNetwork.getUniqueID();
		// System.out.println(" setting id " + id);
	}

	void setID_Override(int i) {
		// the only occasions when you should call this
		// is on file load (to set this ID to the same as the ID in the file)
		id = i;
	}

	int getID() {
		return id;
	}



	public void addAttribute( KeyValuePair kvp ) {
		// Items in the network are only allowed to have 1 instance
		// of any specific key, so old instances get removed before adding a new one
		attributes.removeKeyValue(kvp.getKey());
		attributes.addKeyValuePair(kvp);
	}

	void removeAttribute( String k ) {
		attributes.removeKeyValue(k);
	}

	KeyValuePairList getAttributes() {
		return attributes;
	}

	void setAttributes(KeyValuePairList kvpl) {
		// must do a deep copy here to avoid duplication
		attributes = kvpl.copy();
	}

	KeyValuePair getAttribute(String k) {
		return attributes.findKeyValue(k);
	}
	
	
	int getAttributeInttVal(String k) {
		KeyValuePair kvp =  attributes.findKeyValue(k);
		return kvp.getInt();
	}
	
	float getAttributeFloatVal(String k) {
		KeyValuePair kvp =  attributes.findKeyValue(k);
		return kvp.getFloat();
	}
	
	boolean getAttributeBooleanVal(String k) {
		KeyValuePair kvp =  attributes.findKeyValue(k);
		return kvp.getBoolean();
	}
	
	String getAttributeStringVal(String k) {
		KeyValuePair kvp =  attributes.findKeyValue(k);
		return kvp.getString();
	}

	int getNumAttributes() {
		return attributes.getNumItems();
	}
	
	boolean thisItemContainsMatch( KeyValuePair query ) {
		// returns true if this item contains a KVP matching query
		return attributes.containsEqual(query);
	}

	boolean equals(NAttributes other) {
		return this==other;
	}

	void cleanRedundantAttributes(){
		attributes.removeKeyValue("NONE");
		String nameString = attributes.getString("NAME");
		if( nameString.equals("")) {
			attributes.removeKeyValue("NAME");
		}
	}

	void printAttributes() {
		attributes.printMe();
	}
}// end of NAttributes class


