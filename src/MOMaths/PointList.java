package MOMaths;

import java.util.ArrayList;

public class PointList{



	  ArrayList<PVector> thePointsList = new ArrayList<>();



	  public PointList(){



	  }

	  public void add(PVector p){
	    thePointsList.add(p);

	  }




	  public int getIndexOfPoint(PVector p){
	    return thePointsList.indexOf(p);
	  }

	  public PVector get(int n){
	    return thePointsList.get(n);
	  }

	  public PVector getNearestPoint(PVector p){
	    return getNearestPoint(thePointsList, p);
	  }


	  public PVector getNearestPoint(ArrayList<PVector> plist, PVector p){
	    float nearestDistance = 10000000.0f;
	    PVector nearestPoint = null;
	    for(PVector thisPoint: plist){
	      float d = getDistSq(p, thisPoint);
	      if(d < nearestDistance){
	        nearestDistance = d;
	        nearestPoint = thisPoint;
	      }
	    }
	    return nearestPoint;
	  }

	  public float getNearestPointDistance(PVector p){
	    return getNearestPointDistance(thePointsList, p);
	  }

	  public float getNearestPointDistance(ArrayList<PVector> plist,PVector p){
	    float nearestDistance = 10000000.0f;

	    for(PVector thisPoint: plist){
	      float d = getDistSq(p, thisPoint);
	      if(d < nearestDistance){
	        nearestDistance = d;
	      }
	    }
	    return (float) Math.sqrt(nearestDistance);
	  }

	  public float getDistSq(PVector p1, PVector p2){
	    float dx = p1.x-p2.x;
	    float dy = p1.y-p2.y;
	    return (dx*dx)+(dy*dy);
	  }



	  public int getNumItems(){
	    return thePointsList.size();
	  }


	}
