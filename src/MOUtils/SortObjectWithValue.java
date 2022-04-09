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
	
	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> getSorted(){
		objlist.sort(Comparator.comparing(ObjectWithValue::getValue));
		return (ArrayList<T>) getStripped(objlist);
	}
	
	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> getReverseSorted(){
		objlist.sort(Comparator.comparing(ObjectWithValue::getValue).reversed());
		return (ArrayList<T>) getStripped(objlist);
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
	
	
	
	@SuppressWarnings("unchecked")
	private <T> ArrayList<T> getStripped(ArrayList<T> listIn){
		ArrayList<T> stripped = new ArrayList<T>();
		
		for(ObjectWithValue owv: objlist) {
			stripped.add((T) owv.obj);
		}
		
		return stripped;
	}
}
