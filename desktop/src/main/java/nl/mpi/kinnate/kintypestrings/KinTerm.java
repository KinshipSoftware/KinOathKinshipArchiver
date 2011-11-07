package nl.mpi.kinnate.kintypestrings;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *  Document   : KinTerm
 *  Created on : Apr 15, 2011, 11:49:26 AM
 *  Author     : Peter Withers
 */
public class KinTerm {

    public KinTerm() {
    }

    public KinTerm(String kinTermLocal, String kinTermDescriptionLocal, String egoTypeLocal, String alterKinTypeStringsLocal, String propositusKinTypeStringsLocal) {
        kinTerm = kinTermLocal;
        kinTermDescription = kinTermDescriptionLocal;
        alterKinTypeStrings = alterKinTypeStringsLocal;
        propositusKinTypeStrings = propositusKinTypeStringsLocal;
    }
    @XmlAttribute(name = "Term", namespace = "http://mpi.nl/tla/kin")
    public String kinTerm = null;
    @XmlAttribute(name = "Description", namespace = "http://mpi.nl/tla/kin")
    public String kinTermDescription = null;
    @XmlAttribute(name = "Referent", namespace = "http://mpi.nl/tla/kin") // this attribute name has been changed from alter to referent
    public String alterKinTypeStrings = null;
    @XmlAttribute(name = "Propositus", namespace = "http://mpi.nl/tla/kin")
    public String propositusKinTypeStrings = null;

    @XmlAttribute(name = "Alter", namespace = "http://mpi.nl/tla/kin") // Alter has been replaced by Referent so this just reads the older format value and converts it into the current format
    public void setAlter(String alterKinTypeStrings) {
        this.alterKinTypeStrings = alterKinTypeStrings;
    }

    // alter has been replaced by Referent and this function is just here to update old files
    public String getAlter() {
        return null;
    }
}
