package MOUtils;

import java.util.ArrayList;
import java.util.Comparator;



public class SortObjectWithValue {
	
	
	
	class ObjectWithValue{
		
		Object obj;
		float val;
		public ObjectWithValue(Object o, float v) {
			obj = o;
			val = v;
		}
			
		float getValue() { return val;}
		
	}
	
	ArrayList<ObjectWithValue> objlist = new ArrayList<ObjectWithValue>();
	
	
	public void add(Object o, float v) {
		
		objlist.add(new ObjectWithValue(o,v));
	}
	
	public ArrayList<Object> getSorted(){
		objlist.sort(Comparator.comparing(ObjectWithValue::getValue));
		return getStripped();
	}
	
	public ArrayList<Object> getReverseSorted(){
		objlist.sort(Comparator.comparing(ObjectWithValue::getValue).reversed());
		return getStripped();
	}
	
	public Object getLargest() {
		if(getNum()==0) return null;
		getSorted();
		return objlist.get(getNum()-1);
	}
	
	public Object getSmallest() {
		if(getNum()==0) return null;
		getSorted();
		return objlist.get(0);
	}
	
	public int getNum() {
		return objlist.size();
	}
	
	private ArrayList<Object> getStripped(){
		ArrayList<Object> stripped = new ArrayList<Object>();
		for(ObjectWithValue owv: objlist) {
			stripped.add(owv.obj);
		}
		return stripped;
		
	}
}
