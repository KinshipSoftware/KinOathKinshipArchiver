package nl.mpi.kinnate.ui.entityprofiles;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Document   : ProfileRecord 
 * Created on : Mar 21, 2012, 2:45:30 PM 
 * Author     : Peter Withers
 */
public class ProfileRecord {

    @XmlAttribute(name = "ProfileName", namespace = "http://mpi.nl/tla/kin")
    public String profileName;
    @XmlAttribute(name = "ProfileId", namespace = "http://mpi.nl/tla/kin")
    public String profileId;
}
