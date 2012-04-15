package nl.mpi.kinnate.kintypestrings;

import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import nl.mpi.kinnate.ui.KinTypeStringProvider;

/**
 * Document : KinTerms
 * Created on : Mar 8, 2011, 2:13:30 PM
 * Author : Peter Withers
 */
public class KinTermGroup implements KinTypeStringProvider {

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
            for (String kinType : kinTermItem.alterKinTypeStrings.split("\\" + KinType.separator)) {
                if (kinTypeString.trim().equals(kinType.trim())) {
                    foundKinTerms.add(kinTermItem.kinTerm);
                }
            }
        }
        return foundKinTerms.toArray(new String[]{});
    }

    public String[] getCurrentStrings() {
        ArrayList<String> kinTypeStringList = new ArrayList<String>();
        if (graphGenerate) {
            for (KinTerm kinTerm : this.getKinTerms()) {
                // todo: if these do not state E then maybe they should continue on from the Alter kin type string, iow the propositus could be specified from alter rather than ego
                String[] alterKinTypeStrings = kinTerm.alterKinTypeStrings.split("\\" + KinType.separator);
                kinTypeStringList.addAll(Arrays.asList(alterKinTypeStrings));
                if (kinTerm.propositusKinTypeStrings != null) {
                    String[] propositusKinTypeStrings = kinTerm.propositusKinTypeStrings.split("\\" + KinType.separator);
                    kinTypeStringList.addAll(Arrays.asList(propositusKinTypeStrings));
                }
            }
        }
        return kinTypeStringList.toArray(new String[]{});
    }

    public int getTotalLength() {
        int totalLength = 0;
        if (graphGenerate) {
            for (KinTerm kinTerm : this.getKinTerms()) {
                totalLength = totalLength + kinTerm.alterKinTypeStrings.split("\\" + KinType.separator).length;
            }
        }
        return totalLength;
    }

    public void highlightKinTypeStrings(ParserHighlight[] parserHighlight, String[] kinTypeStrings) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
}
