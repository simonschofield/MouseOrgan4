package MOUtils;

import java.util.ArrayList;

//use like this ArrayListIterator<ThingType> entries = new ArrayListIterator<ThingType>();


public class ArrayListIterator<T>{
	
	
	public int counter=0;
	
	public ArrayList<T> arrayList;
	
	
	public ArrayListIterator() {
		arrayList = new ArrayList<T>();
	}
	
	public ArrayListIterator(ArrayList<T> list) {
		arrayList = list;
	}
	
	public void add(T e)
	{
		// put the new item at the back of the array.
		arrayList.add( e );
	}
	
	public boolean hasNext() {
		if(counter < arrayList.size()) return true;
		return false;
	}
	
	public T get(int n) {
		return arrayList.get(n);
	}
	
	public T getNext() {
		return get(counter++);
		
	}
	

	
}
