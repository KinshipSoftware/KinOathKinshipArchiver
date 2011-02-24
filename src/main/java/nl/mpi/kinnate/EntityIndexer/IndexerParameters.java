package nl.mpi.kinnate.EntityIndexer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *  Document   : IndexParameters
 *  Created on : Feb 14, 2011, 11:47:34 AM
 *  Author     : Peter Withers
 */
public class IndexerParameters {

    public class IndexerParam {

        private String[][] valuesArray;
        private String[] availableValuesArray = null;

        public IndexerParam(String[][] valuesArrayLocal) {
            valuesArray = valuesArrayLocal;
        }

        public void setValues(String[][] valuesArrayLocal) {
            valuesArray = valuesArrayLocal;
            IndexerParameters.this.relevantEntityData = null;
        }

        public void removeValue(String valueToRemove) {
            ArrayList<String[]> tempArrayList = new ArrayList<String[]>(Arrays.asList(valuesArray));
            for (String[] currentEntry : valuesArray) {
                if (currentEntry[0].equals(valueToRemove)) {
                    tempArrayList.remove(currentEntry); // todo: will array list remove use the value of the array or the pointer to the array?????
                }
            }
            setValues(tempArrayList.toArray(new String[][]{}));
        }

        public String[][] getValues() {
            return valuesArray;
        }

        public void setAvailableValues(String[] availableValuesArrayLocal) {
            availableValuesArray = availableValuesArrayLocal;
        }

        public String[] getAvailableValues() {
            return availableValuesArray;
        }
    }
    public String linkPath = "/Kinnate/Relation/Link";
//    public IndexerParam relevantEntityData = new IndexerParam(new String[][]{{"Kinnate/Gedcom/Entity/NoteText"}, {"Kinnate/Gedcom/Entity/SEX"}, {"Kinnate/Gedcom/Entity/GedcomType"}, {"Kinnate/Gedcom/Entity/NAME/NAME"}, {"Kinnate/Gedcom/Entity/NAME/NPFX"}}); // todo: the relevantData array comes from the user via the svg
    public IndexerParam relevantLinkData = new IndexerParam(new String[][]{{"Type"}});
    public IndexerParam labelFields = new IndexerParam(new String[][]{{"Kinnate/Gedcom/Entity/NAME/NAME"}, {"Kinnate/Gedcom/Entity/GedcomType"}, {"Kinnate/Gedcom/Entity/Text"}, {"Kinnate/Gedcom/Entity/NAME/NPFX"}, {"Kinnate/Gedcom/Entity/NoteText"}});
    public IndexerParam symbolFieldsFields = new IndexerParam(new String[][]{{"Kinnate/Gedcom/Entity/SEX[text()='M']", "triangle"}, {"Kinnate/Gedcom/Entity[SEX='F']", "circle"}, {"Kinnate/Gedcom/Entity/GedcomType[text()='FAM']", "union"}});
    public IndexerParam ancestorFields = new IndexerParam(new String[][]{{"Kinnate.Gedcom.Entity.FAMC"}, {"Kinnate.Gedcom.Entity.HUSB"}, {"Kinnate.Gedcom.Entity.WIFE"}});
//    public IndexerParam siblingFields = new IndexerParam(new String[]{{"Kinnate.Gedcom.Entity.CHIL"}, {"Kinnate.Gedcom.Entity.FAMS"}});
    public IndexerParam decendantFields = new IndexerParam(new String[][]{{"Kinnate.Gedcom.Entity.CHIL"}, {"Kinnate.Gedcom.Entity.FAMS"}});
    public IndexerParam showEntityFields = new IndexerParam(new String[][]{{"Kinnate/Gedcom/Entity/GedcomType=INDI"}, {"Kinnate/Gedcom/Entity/GedcomType=FAM"}}); // todo: add fields that can be used to controll which nodes are shown
    private String[] relevantEntityData = null;

    public String[] getRelevantEntityData() {
        if (relevantEntityData == null) {
            ArrayList<String> relevantDataList = new ArrayList<String>();
            for (IndexerParam currentIndexerParam : new IndexerParam[]{labelFields, symbolFieldsFields, showEntityFields}) {
                for (String[] currentData : currentIndexerParam.getValues()) {
                    relevantDataList.add(currentData[0]);
                }
            }
            relevantEntityData = relevantDataList.toArray(new String[]{});
        }
        return relevantEntityData;
    }
}
