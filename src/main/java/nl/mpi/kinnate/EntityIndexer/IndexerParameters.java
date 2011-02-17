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

        private String[] valuesArray;

        public IndexerParam(String[] valuesArrayLocal) {
            valuesArray = valuesArrayLocal;
        }

        public void setValues(String[] valuesArrayLocal) {
            valuesArray = valuesArrayLocal;
        }

        public void removeValue(String valuesToRemove) {
            ArrayList<String> tempArrayList = new ArrayList<String>(Arrays.asList(valuesArray));
            tempArrayList.remove(valuesToRemove);
            setValues(tempArrayList.toArray(new String[]{}));
        }

        public String[] getValues() {
            return valuesArray;
        }
    }
    public String linkPath = "/Kinnate/Relation/Link";
    public IndexerParam relevantEntityData = new IndexerParam(new String[]{"Kinnate/Gedcom/Entity/NoteText", "Kinnate/Gedcom/Entity/SEX", "Kinnate/Gedcom/Entity/GedcomType", "Kinnate/Gedcom/Entity/NAME/NAME", "Kinnate/Gedcom/Entity/NAME/NPFX"}); // todo: the relevantData array comes from the user via the svg
    public IndexerParam relevantLinkData = new IndexerParam(new String[]{"Type"});
    public IndexerParam labelFields = new IndexerParam(new String[]{"Kinnate/Gedcom/Entity/NAME/NAME", "Kinnate/Gedcom/Entity/GedcomType", "Kinnate/Gedcom/Entity/Text", "Kinnate/Gedcom/Entity/NAME/NPFX", "Kinnate/Gedcom/Entity/NoteText"});
    public IndexerParam symbolFieldsFields = new IndexerParam(new String[]{"Kinnate/Gedcom/Entity/SEX", "Kinnate/Gedcom/Entity/GedcomType"});
    public IndexerParam ancestorFields = new IndexerParam(new String[]{"Kinnate.Gedcom.Entity.FAMC", "Kinnate.Gedcom.Entity.HUSB", "Kinnate.Gedcom.Entity.WIFE"});
    public IndexerParam decendantFields = new IndexerParam(new String[]{"Kinnate.Gedcom.Entity.CHIL", "Kinnate.Gedcom.Entity.FAMS"});
}
