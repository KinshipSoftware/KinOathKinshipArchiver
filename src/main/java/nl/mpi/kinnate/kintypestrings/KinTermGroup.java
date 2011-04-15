package nl.mpi.kinnate.kintypestrings;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *  Document   : KinTerms
 *  Created on : Mar 8, 2011, 2:13:30 PM
 *  Author     : Peter Withers
 */
public class KinTermGroup {

    @XmlAttribute(name = "GroupName", namespace = "http://mpi.nl/tla/kin")
    public String titleString;
    @XmlAttribute(name = "GroupColour", namespace = "http://mpi.nl/tla/kin")
    public String graphColour;
    @XmlElement(name = "KinTerm", namespace = "http://mpi.nl/tla/kin")
    private ArrayList<KinTerm> kinTermArray;

    public KinTermGroup() {
        titleString = "Unnamed Kin Term Group";
        graphColour = "blue";
        //todo: sample kin terms to put into a sensible defaults place
        kinTermArray = new ArrayList<KinTerm>();
        kinTermArray.add(new KinTerm("Grand Mother", null, null, "MM", null, null));
        kinTermArray.add(new KinTerm("Aunt", null, null, "MZ" + "|" + "FZ", null, null));
        kinTermArray.add(new KinTerm("Uncle", null, null, "MB" + "|" + "FB", null, null));
        kinTermArray.add(new KinTerm("Grand Father", null, null, "FF", null, null));
    }

    public void addKinTerm(String kinTypeStrings, String kinTerm) {
        kinTermArray.add(new KinTerm(kinTerm, null, null, kinTypeStrings, null, null));
    }

    public void addKinTerm(KinTerm kinTerm) {
        kinTermArray.add(kinTerm);
    }

    public void updateKinTerm(String kinTypeStrings, String kinTerm) {
        for (KinTerm kinTermItem : kinTermArray) {
            if (kinTermItem.kinTerm.equals(kinTerm)) {
                kinTermItem.alterKinTypeStrings = kinTypeStrings;
            }
        }
    }

    public void removeKinTerm(String kinTerm) {
        for (KinTerm kinTermItem : getKinTerms()) {
            if (kinTermItem.kinTerm.equals(kinTerm)) {
                kinTermArray.remove(kinTermItem);
            }
        }
    }

    public KinTerm[] getKinTerms() {
        return kinTermArray.toArray(new KinTerm[]{});
    }

    public String getTermLabel(String kinTypeString) {
        // todo: add the propositus to the selection criteria
        for (KinTerm kinTermItem : kinTermArray) {
            for (String kinType : kinTermItem.alterKinTypeStrings.split("\\|")) {
                if (kinTypeString.trim().equals(kinType.trim())) {
                    return kinTermItem.kinTerm;
                }
            }
        }
        return null;
    }
}
