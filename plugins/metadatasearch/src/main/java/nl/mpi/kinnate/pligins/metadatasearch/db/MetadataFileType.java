package nl.mpi.kinnate.pligins.metadatasearch.db;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Document : MetadataFileType
 * Created on : Aug 6, 2012, 1:37:47 PM
 * Author : Peter Withers
 */
@XmlRootElement(name = "MetadataFileType")
public class MetadataFileType {

    @XmlElement(name = "rootXpath")
    private String rootXpath;
    @XmlElement(name = "displayString")
    private String displayString;
    @XmlElement(name = "recordCount")
    private int recordCount = 0;
    @XmlElement(name = "childMetadataTypes")
    private MetadataFileType[] childMetadataTypes = null;

    public MetadataFileType() {
    }

    public MetadataFileType(String rootXpath, String displayString) {
        this.rootXpath = rootXpath;
        this.displayString = displayString;
    }

    public MetadataFileType[] getChildMetadataTypes() {
        return childMetadataTypes;
    }

    @Override
    public String toString() {
        return displayString;
    }
}
