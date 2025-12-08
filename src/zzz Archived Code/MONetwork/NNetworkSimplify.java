package MONetwork;

import java.util.ArrayList;

import MOUtils.KeyValuePair;
import MOUtils.KeyValuePairList;
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Essentially removes the C-roads from the map. Sounds simple? Some of the C-roads are also important structural elements for rivers, lakes, parks etc
// so deleting those would destroy other important features of the map. Here, the class assumes that all "important" region features are tagged with
// attribute tag (i.e. an NPoint) with the KVP attribute ("REGIONMARKER", "whatever").
// The process is as follows.
// 1/ auto-detect regions
// 2/ identify regions that are tagged.
// 3/ From the tagged regions, build a list of the C-roads that cannot be removed
// 4/ The other c-roads can be removed
// returns a copy of the original network with all the dispensible edges removed. While the process needs to auto-detect regions, these are removed before returning as they are no longer useful.
//
public class NNetworkSimplify {

	NNetwork theOriginalNetwork;
	NNetwork networkCopy;



	public NNetworkSimplify(NNetwork ntwk) {

		theOriginalNetwork = ntwk;


	}

	public NNetwork getSimplifiedNetwork() {

		ArrayList<NEdge> dispensibleEdges = getDispensibleEdges();

		for(NEdge e: dispensibleEdges) {
			 networkCopy.deleteEdge( e);
		}

		// remove the regions because they are the small ones, many of whihc have bee deleted
		// The new larger regions have to be re-found using the auto-region finder.
		networkCopy.regions = new ArrayList<>();

		return networkCopy;
	}


	public ArrayList<NEdge> getDispensibleEdges() {
		ArrayList<NEdge> importantEdges = getImportantEdges();

		KeyValuePair cRoadCriteria = new KeyValuePair("ROAD","C");
		ArrayList<NEdge> dispensibleEdges = networkCopy.getEdgesMatchingQuery(cRoadCriteria);

		System.out.println("found " + dispensibleEdges.size() + " C Roads in total");

		dispensibleEdges.removeAll(importantEdges);
		return dispensibleEdges;
	}







	//// private

	private ArrayList<NEdge> getImportantEdges() {
		ArrayList<NRegion> importantRegions = getImportantRegions();
		ArrayList<NEdge> importantEdges = new ArrayList<> ();

		for(NRegion r: importantRegions) {
			ArrayList<NEdge> edges = (ArrayList<NEdge>) r.edgeReferences.clone();
			importantEdges.addAll(edges);
		}
		return importantEdges;
	}



	private ArrayList<NRegion> getImportantRegions(){

		networkCopy = theOriginalNetwork.copy();

		KeyValuePairList searchCriteria = new KeyValuePairList();
		NNetworkRegionFinder autoRegionFinder = new NNetworkRegionFinder(networkCopy, searchCriteria);


		autoRegionFinder.findAllRegions();

		autoRegionFinder.setRegionAttributeWithRegionMarker("REGIONTYPE","PARK");
		autoRegionFinder.setRegionAttributeWithRegionMarker("REGIONTYPE","RIVER");
		autoRegionFinder.setRegionAttributeWithRegionMarker("REGIONTYPE","LAKE");
		autoRegionFinder.setRegionAttributeWithRegionMarker("REGIONTYPE","SEA");



		networkCopy = autoRegionFinder.getNetworkWithFoundRegions();

		KeyValuePairList taggedRegionSearch = new KeyValuePairList();
		taggedRegionSearch.addKeyValue("REGIONTYPE", "PARK");
		taggedRegionSearch.addKeyValue("REGIONTYPE", "RIVER");
		taggedRegionSearch.addKeyValue("REGIONTYPE", "LAKE");
		taggedRegionSearch.addKeyValue("REGIONTYPE", "SEA");
		networkCopy.setSearchAttribute(taggedRegionSearch);
		ArrayList<NRegion> importantRegions = networkCopy.getRegionsMatchingSearchAttributes(true);
		networkCopy.clearSearchAttributes();


		return importantRegions;

	}






}
