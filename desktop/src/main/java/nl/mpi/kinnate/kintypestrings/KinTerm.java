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
    @XmlAttribute(name = "Alter", namespace = "http://mpi.nl/tla/kin") // todo: this should be changed to referent rather than alter, but it will affect exiting diagrams
    public String alterKinTypeStrings = null;
    @XmlAttribute(name = "Propositus", namespace = "http://mpi.nl/tla/kin")
    public String propositusKinTypeStrings = null;
}
