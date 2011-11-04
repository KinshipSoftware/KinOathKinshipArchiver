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
    public String titleString = "Kin Term Group (unnamed)";
    @XmlAttribute(name = "GroupDescription", namespace = "http://mpi.nl/tla/kin")
    public String descriptionString;
    @XmlAttribute(name = "GroupColour", namespace = "http://mpi.nl/tla/kin")
    public String graphColour = "#0000FF";
    @XmlAttribute(name = "Show", namespace = "http://mpi.nl/tla/kin")
    public boolean graphShow = true;
    @XmlAttribute(name = "Generate", namespace = "http://mpi.nl/tla/kin")
    public boolean graphGenerate = true;
    @XmlElement(name = "KinTerm", namespace = "http://mpi.nl/tla/kin")
    private ArrayList<KinTerm> kinTermArray;

    public KinTermGroup() {
        kinTermArray = new ArrayList<KinTerm>();
    }

    public void addKinTerm(String kinTypeStrings, String kinTerm) {
        kinTermArray.add(new KinTerm(kinTerm, null, null, kinTypeStrings, null));
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

    public void removeKinTerm(KinTerm kinTerm) {
        kinTermArray.remove(kinTerm);
    }

    public KinTerm[] getKinTerms() {
        return kinTermArray.toArray(new KinTerm[]{});
    }

    public String[] getTermLabel(String kinTypeString) {
        // todo: add the propositus to the selection criteria
        ArrayList<String> foundKinTerms = new ArrayList<String>();
        for (KinTerm kinTermItem : kinTermArray) {
            for (String kinType : kinTermItem.alterKinTypeStrings.split("\\|")) {
                if (kinTypeString.trim().equals(kinType.trim())) {
                    foundKinTerms.add(kinTermItem.kinTerm);
                }
            }
        }
        return foundKinTerms.toArray(new String[]{});
    }
}
