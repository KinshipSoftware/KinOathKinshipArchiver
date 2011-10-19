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
    public String graphColour = "blue";
    @XmlAttribute(name = "Show", namespace = "http://mpi.nl/tla/kin")
    public boolean graphShow = true;
    @XmlAttribute(name = "Generate", namespace = "http://mpi.nl/tla/kin")
    public boolean graphGenerate = false;
    @XmlElement(name = "KinTerm", namespace = "http://mpi.nl/tla/kin")
    private ArrayList<KinTerm> kinTermArray;

    public KinTermGroup() {
        kinTermArray = new ArrayList<KinTerm>();
    }

    public KinTermGroup(int type) {
        switch (type) {
            case 0:
                titleString = "English terms";
                graphColour = "blue";
                //todo: sample kin terms to put into a sensible defaults place
                kinTermArray = new ArrayList<KinTerm>();
                kinTermArray.add(new KinTerm("Grand Mother", null, null, "MM", null, null));
                kinTermArray.add(new KinTerm("Aunt", null, null, "MZ" + "|" + "FZ", null, null));
                kinTermArray.add(new KinTerm("Uncle", null, null, "MB" + "|" + "FB", null, null));
                kinTermArray.add(new KinTerm("Grand Father", null, null, "FF", null, null));
                break;
            case 1:
                titleString = "Hawaiian Kin Terms";
                descriptionString = "http://umanitoba.ca/faculties/arts/anthropology/tutor/kinterms/hawaiian.html";
                graphColour = "cyan";
                kinTermArray = new ArrayList<KinTerm>();
                kinTermArray.add(new KinTerm("Makuakane", "Father, Uncle", null, "F|FB|MB", null, null));
                kinTermArray.add(new KinTerm("Makuahini", "Mother, Aunt", null, "M|MZ|FZ", null, null));
                kinTermArray.add(new KinTerm("Kaikua'ana", "", null, "E(male)B|E(male)FBS|E(male)FZS|E(male)MBS|E(male)MZS", null, null));
                kinTermArray.add(new KinTerm("Kaikua'ana", "Brother/Sister, Cousin", null, "E(female)Z|E(female)FBD|E(female)FZD|E(female)MBD|E(female)MZD", null, null));
                kinTermArray.add(new KinTerm("Kaikuane", "Brother, Cousin", null, "E(female)B|E(female)FBS|E(female)FZS|E(female)MBS|E(female)MZS", null, null));
                kinTermArray.add(new KinTerm("Kaikuane", "Sister, Cousin", null, "E(male)Z|E(male)FBD|E(male)FZD|E(male)MBD|E(male)MZD", null, null));
                kinTermArray.add(new KinTerm("Keikikane", "Son, Nephew", null, "S|BS|ZS", null, null));
                kinTermArray.add(new KinTerm("Keikamahini", "Daughter, Niece", null, "D|BD|ZD", null, null));
                break;

//    <kin:KinTermGroups kin:Show="true" kin:GroupColour="blue" kin:GroupName="Japanese Kin Terms" kin:Generate="true">
//      <kin:KinTerm kin:Term="?" kin:Alter="B" kin:Propositus="^B" kin:Description="Elder Brother (when speaking to other than family)"/>
//      <kin:KinTerm kin:Term="?" kin:Alter="Z(younger)" kin:Propositus="^Z" kin:Description="Younger Sister (when speaking to other than family)"/>
//      <kin:KinTerm kin:Term="?" kin:Alter="F" kin:Propositus="^F" kin:Description="Father (when speaking to other than family)"/>
//      <kin:KinTerm kin:Term="?" kin:Alter="M" kin:Propositus="^M" kin:Description="Mother (when speaking to other than family)"/>
//      <kin:KinTerm kin:Term="?" kin:Alter="Z" kin:Propositus="^Z" kin:Description="Elder Sister (when speaking to other than family)"/>
//      <kin:KinTerm kin:Term="?" kin:Alter="B" kin:Propositus="^B (this needs to be able to indicate not family)" kin:Description="Yonger Brother (when speaking to other than family)"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Younger brother of parent (when speaking to other than family)" kin:Alter="PB"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Younger sister of parent (when speaking to other than family)" kin:Alter="PZ"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Older brother of parents (when speaking to other than family)" kin:Alter="PB"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Older sister of parents (when speaking to other than family)" kin:Alter="PZ"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Older male cousin (when speaking to other than family)" kin:Alter="PZS|PBS"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Older female cousin (when speaking to other than family)" kin:Alter="PZD|PBD"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Yonger male cousin (when speaking to other than family)" kin:Alter="PZS|PBS"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Yonger female cousin (when speaking to other than family)" kin:Alter="PZD|PBD"/>
//      <kin:KinTerm kin:Term="???" kin:Description="Second Cousin" kin:Alter="PPZCC|PPBCC"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Grand Father" kin:Alter="PF"/>
//      <kin:KinTerm kin:Term="??" kin:Description="Grand Mother" kin:Alter="PM"/>
//    </kin:KinTermGroups>

            default:
                kinTermArray = new ArrayList<KinTerm>();
        }
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
