package MOZZZ_Depricated;

import java.util.ArrayList;
import java.util.Comparator;



public class ObjectWithValueList {



	class ObjectWithValue{

		Object obj;
		float val;
		public ObjectWithValue(Object o, float v) {
			obj = o;
			val = v;
		}

		public float getValue() { return val; }
		public Object getObject() { return obj; }
	}

	ArrayList<ObjectWithValue> objWithValArrayList = new ArrayList<>();


	public void add(Object o, float v) {

		objWithValArrayList.add(new ObjectWithValue(o,v));
	}


	float getAssociatedValue(Object searchObject) {
		ObjectWithValue owv = objWithValArrayList.stream().filter(  objWVal-> searchObject.equals(objWVal.getObject())   ).findFirst().orElse(null);
		if(owv==null) {
			return 0;
		}
		return owv.getValue();
	}



	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> getSorted(){
		objWithValArrayList.sort(Comparator.comparing(ObjectWithValue::getValue));
		return (ArrayList<T>) getStripped(objWithValArrayList);
	}

	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> getReverseSorted(){
		objWithValArrayList.sort(Comparator.comparing(ObjectWithValue::getValue).reversed());
		return (ArrayList<T>) getStripped(objWithValArrayList);
	}

	public Object getLargest() {
		if(getNum()==0) {
			return null;
		}
		getSorted();
		return objWithValArrayList.get(getNum()-1);
	}

	public Object getSmallest() {
		if(getNum()==0) {
			return null;
		}
		getSorted();
		return objWithValArrayList.get(0);
	}

	public int getNum() {
		return objWithValArrayList.size();
	}



	@SuppressWarnings("unchecked")
	private <T> ArrayList<T> getStripped(ArrayList<T> listIn){
		ArrayList<T> stripped = new ArrayList<>();

		for(ObjectWithValue owv: objWithValArrayList) {
			stripped.add((T) owv.obj);
		}

		return stripped;
	}
}
