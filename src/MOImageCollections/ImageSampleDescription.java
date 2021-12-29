package MOImageCollections;

import java.io.Serializable;



//a seed factory generates seeds. These have points in documentspace and associates
//some asset with that point
///////////////////////////////////////////////////////////////////////////
//ContentItemSelection is full description of the asset residing in a seed
//and returned from ContentItemSelector

public class ImageSampleDescription {
	public String imageSampleGroupName;
	public int itemNumber;
	public String shortName;
	
	public ImageSampleDescription(String collectionName, int itemNum, String shortNm) {
		imageSampleGroupName = collectionName;
		itemNumber = itemNum;
		shortName = shortNm;
	}

	String toStr() {

		return " content group name " + imageSampleGroupName + ", itemNumber " + itemNumber + " short name " + shortName;
	}
}
