package MOUtils;

import java.util.ArrayList;

//use like this ArrayListIterator<ThingType> entries = new ArrayListIterator<ThingType>();


public class ArrayListIterator<T>{
	
	
	public int counter=0;
	
	public ArrayList<T> arrayList;
	
	
	public ArrayListIterator() {
		arrayList = new ArrayList<T>();
		counter=0;
	}
	
	public ArrayListIterator(ArrayList<T> list) {
		arrayList = list;
		counter=0;
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
		if(n < 0 || n >= size() ) return null;
		return arrayList.get(n);
	}
	
	public T getNext() {
		
		T obj = get(counter);
		counter++;
		return obj;
		
	}
	
	
	public int size() {
		return arrayList.size();
	}
	
	public int getCurrentItemNum() {
		// once an item has been retrieved by getNext
		// the iterator counter gets incremented
		return counter;
	}
	
	
	
	public String toStr() {
		
		return "Iterator has " + size() + " elements, the counter is currently at " + counter;
	}
	

	
}
