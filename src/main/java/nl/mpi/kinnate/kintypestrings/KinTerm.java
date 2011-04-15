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

    public KinTerm(String kinTermLocal, String kinTermDescriptionLocal, String egoTypeLocal, String alterKinTypeStringsLocal, String propositusKinTypeStringsLocal, String anchorKinTypeStringsLocal) {
        kinTerm = kinTermLocal;
        kinTermDescription = kinTermDescriptionLocal;
        egoType = egoTypeLocal;
        alterKinTypeStrings = alterKinTypeStringsLocal;
        propositusKinTypeStrings = propositusKinTypeStringsLocal;
        anchorKinTypeStrings = anchorKinTypeStringsLocal;
    }
    @XmlAttribute(name = "Term", namespace = "http://mpi.nl/tla/kin")
    public String kinTerm = null;
    @XmlAttribute(name = "Description", namespace = "http://mpi.nl/tla/kin")
    public String kinTermDescription = null;
    @XmlAttribute(name = "Ego", namespace = "http://mpi.nl/tla/kin")
    public String egoType = null;
    @XmlAttribute(name = "Alter", namespace = "http://mpi.nl/tla/kin")
    public String alterKinTypeStrings = null;
    @XmlAttribute(name = "Propositus", namespace = "http://mpi.nl/tla/kin")
    public String propositusKinTypeStrings = null;
    @XmlAttribute(name = "Anchor", namespace = "http://mpi.nl/tla/kin")
    public String anchorKinTypeStrings = null;
}
