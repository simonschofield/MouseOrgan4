package MOImageCollections;

import java.io.Serializable;


@SuppressWarnings("serial")
//a seed factory generates seeds. These have points in documentspace and associates
//some asset with that point
///////////////////////////////////////////////////////////////////////////
//ContentItemSelection is full description of the asset residing in a seed
//and returned from ContentItemSelector

public class ImageSampleDescription implements Serializable {
	public String imageSampleGroupName;
	public int itemNumber;

	public ImageSampleDescription(String collectionName, int itemNum) {
		imageSampleGroupName = collectionName;
		itemNumber = itemNum;
	}

	String toStr() {

		return " content group name " + imageSampleGroupName + ", itemNumber " + itemNumber;
	}
}
