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

    @XmlElement(name = "rootXpath")
    private String rootXpath = null;
    @XmlElement(name = "pathPart")
    private String pathPart = null;
    @XmlElement(name = "displayString")
    private String displayString = null;
    @XmlElement(name = "profileString")
    private String profileString = null;
    @XmlElement(name = "recordCount")
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

    @Override
    public String toString() {
        if (displayString == null) {
            if (rootXpath != null) {
                displayString = rootXpath.replaceAll("\"[^\"]*\":", "").replaceAll("\\[\\d*\\]", "");
            } else if (pathPart != null) {
                displayString = pathPart;
            } else if (profileString != null) {
                Pattern regexPattern = Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");
                Matcher matcher = regexPattern.matcher(profileString);
                while (matcher.find()) {
                    displayString = matcher.group(1);
                    break;
                }
            }
        }
        return displayString;
    }
}
