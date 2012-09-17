package nl.mpi.kinnate.plugins.metadatasearch.db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Document : MetadataFileType Created on : Aug 6, 2012, 1:37:47 PM
 *
 * @author Peter Withers
 */
@XmlRootElement(name = "MetadataFileType")
public class MetadataFileType {

    @XmlElement(name = "ImdiType")
    private String imdiType = null;
    @XmlElement(name = "fieldName")
    private String fieldName = null;
    @XmlElement(name = "displayString")
    private String displayString = null;
    @XmlElement(name = "profileString")
    private String profileString = null;
    @XmlElement(name = "arbilPathString")
    private String arbilPathString = null;
    @XmlElement(name = "RecordCount")
    private int recordCount = 0;
//    @XmlElementWrapper(name = "childMetadataTypes")
    @XmlElement(name = "MetadataFileType")
    private MetadataFileType[] childMetadataTypes = null;

    public MetadataFileType() {
    }

//    public MetadataFileType(String rootXpath, String pathPart, String displayString) {
//        this.rootXpath = rootXpath;
//        this.pathPart = pathPart;
//        this.displayString = displayString;
//    }
    public MetadataFileType[] getChildMetadataTypes() {
        return childMetadataTypes;
    }

    public String getImdiType() {
        if (imdiType != null) {
            return imdiType.replaceAll("\"[^\"]*\":", "*:").replaceAll("\\[\\d*\\]", "");
        }
        return null;
    }

    public String getArbilPathString() {
        return arbilPathString.replaceAll("\"[^\"]*\":", "*:").replaceAll("\\[\\d*\\]", "");
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getProfileIdString() {
        if (profileString != null) {
            Pattern regexPattern = Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");
            Matcher matcher = regexPattern.matcher(profileString);
            while (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        if (displayString == null) {
            if (imdiType != null) {
                displayString = imdiType.replaceAll("\"[^\"]*\":", "").replaceAll("\\[\\d*\\]", "");
            } else if (fieldName != null) {
                displayString = fieldName;
            } else if (profileString != null) {
                displayString = getProfileIdString();
            }
        }
        return displayString + " (" + recordCount + ")";
    }
}
