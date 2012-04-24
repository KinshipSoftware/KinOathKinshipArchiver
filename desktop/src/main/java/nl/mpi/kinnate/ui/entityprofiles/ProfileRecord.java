package nl.mpi.kinnate.ui.entityprofiles;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Document : ProfileRecord
 * Created on : Mar 21, 2012, 2:45:30 PM
 * Author : Peter Withers
 */
public class ProfileRecord {

    @XmlAttribute(name = "ProfileName", namespace = "http://mpi.nl/tla/kin")
    public String profileName;
    @XmlAttribute(name = "ProfileId", namespace = "http://mpi.nl/tla/kin")
    public String profileId;

    private ProfileRecord() {
    }

    public ProfileRecord(String profileName, String profileId) {
        this.profileName = profileName;
        this.profileId = profileId;
    }

    @Override
    public String toString() {
        return profileName;
    }

    static public ProfileRecord getDefaultImportProfile() {
        return new ProfileRecord("<default>", "clarin.eu:cr1:p_1332345811039");
    }

    static public ProfileRecord[] getDefaultProfiles() {
        return new ProfileRecord[]{new ProfileRecord("Individual", "clarin.eu:cr1:p_1320657629627"), new ProfileRecord("Chromosome_Example", "clarin.eu:cr1:p_1332345811038")};
    }
}
