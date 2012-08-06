package db;

/**
 * Document : MetadataFileType
 * Created on : Aug 6, 2012, 1:37:47 PM
 * Author : Peter Withers
 */
public class MetadataFileType {

    final private String rootXpath;
    final private String displayString;
    private int recordCount = 0;

    public MetadataFileType(String rootXpath, String displayString) {
        this.rootXpath = rootXpath;
        this.displayString = displayString;
    }

    @Override
    public String toString() {
        return displayString;
    }
}
