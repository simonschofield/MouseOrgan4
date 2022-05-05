package MOUtils;

import java.util.ArrayList;

public class GenericArrayListUtils{
	
	
	public static <T> ArrayList<T> trimList(ArrayList<T> listIn, Integer from, Integer to){
		ArrayList<T> trimmedList = new ArrayList<T>();
		if(from == null) from = 0;
		if(to == null) to = listIn.size()-1;
		if(to > listIn.size()-1) to = listIn.size()-1;
		
		for(int n = from; n <= to; n++) {
			T t = listIn.get(n);
			trimmedList.add(t);
		}
		
		return trimmedList;
	}
	
	public static <T> boolean listsContentsAreEqual(ArrayList<T> listA, ArrayList<T> listB) {
		if(listA.size() != listB.size()) return false;
		for(int n = 0; n < listB.size(); n++) {
			T sA = listA.get(n);
			T sB = listB.get(n);
			if( sA.equals(sB)==false ) return false;
		}
		return true;
	}
	
	// returns true if all the elements of list A are present in list B
	public static <T> boolean listsAContainedInB(ArrayList<T> listA, ArrayList<T> listB) {
		
		for(int na = 0; na < listA.size(); na++) {
			T sA = listA.get(na);
			boolean matchFound = false;
			for(int nb = 0; nb < listB.size(); nb++) {
			    T sB = listB.get(nb);
			    if( sA.equals(sB) ) {
			    	matchFound = true;
			    	break;
			    }
			}
			if( matchFound==false ) return false;
		}
		return true;
	}
	
}


