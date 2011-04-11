package nl.mpi.kinnate.kintypestrings;

import java.util.ArrayList;

/**
 *  Document   : KinTerms
 *  Created on : Mar 8, 2011, 2:13:30 PM
 *  Author     : Peter Withers
 */
public class KinTerms {

    public String titleString;
    public String graphColour;
    ArrayList<String[]> kinTermArray;

    public KinTerms() {
        titleString="Sample Kin Terms";
        graphColour = "blue";
        //todo: sample kin terms to put into a sensible defaults place
        kinTermArray = new ArrayList<String[]>();
        kinTermArray.add(new String[]{"MM", "Grand Mother"});
        kinTermArray.add(new String[]{"MZ" + "|" + "FZ", "Aunt"});
        kinTermArray.add(new String[]{"MB" + "|" + "FB", "Uncle"});
        kinTermArray.add(new String[]{"FF", "Grand Father"});
    }

    public void addKinTerm(String kinTypeStrings, String kinTermLabel) {
        kinTermArray.add(new String[]{kinTypeStrings, kinTermLabel});
    }

    public void updateKinTerm(String kinTypeStrings, String kinTermLabel) {
        for (String[] kinTermItem : kinTermArray) {
            if (kinTermItem[1].equals(kinTermLabel)) {
                kinTermItem[0] = kinTypeStrings;
            }
        }
    }

    public void removeKinTerm(String kinTermLabel) {
        ArrayList<String[]> removeTermArray = new ArrayList<String[]>();
        for (String[] kinTermItem : kinTermArray) {
            if (kinTermItem[1].equals(kinTermLabel)) {
                removeTermArray.add(kinTermItem);
            }
        }
        kinTermArray.removeAll(removeTermArray);
    }

    public String[][] getKinTerms() {
        return kinTermArray.toArray(new String[][]{});
    }

    public String getTermLabel(String kinTypeString) {
        for (String[] kinTermItem : kinTermArray) {
            for (String kinType : kinTermItem[0].split("\\|")) {
                if (kinTypeString.trim().equals(kinType.trim())) {
                    return kinTermItem[1];
                }
            }
        }
        return null;
    }
}
