package nl.mpi.kinnate.entityindexer;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;

/**
 *  Document   : IndexParameters
 *  Created on : Feb 14, 2011, 11:47:34 AM
 *  Author     : Peter Withers
 */
public class IndexerParameters {

    public boolean valuesChanged = false;
    public String linkPath = "/Kinnate/Relation/Link";
//    public IndexerParam relevantEntityData = new IndexerParam(new String[][]{{"Kinnate/Gedcom/Entity/NoteText"}, {"Kinnate/Gedcom/Entity/SEX"}, {"Kinnate/Gedcom/Entity/GedcomType"}, {"Kinnate/Gedcom/Entity/NAME/NAME"}, {"Kinnate/Gedcom/Entity/NAME/NPFX"}}); // todo: the relevantData array comes from the user via the svg
    public IndexerParam relevantLinkData = new IndexerParam(this, new String[][]{{"Type"}});
    @XmlElement(name = "kin:LabelFields")
    public IndexerParam labelFields = new IndexerParam(this, new String[][]{{"Kinnate/Gedcom/Entity/NAME/NAME"}, {"Kinnate/Gedcom/Entity[GedcomType='FAM']/GedcomType"}, {"Kinnate/Gedcom/Entity/Text"}, {"Kinnate/Gedcom/Entity/NAME/NPFX"}, {"Kinnate/Gedcom/Entity/NoteText"}});
    @XmlElement(name = "kin:SymbolFieldsFields")
    public IndexerParam symbolFieldsFields = new IndexerParam(this, new String[][]{{"Kinnate/Gedcom/Entity[SEX='M']", "triangle"}, {"Kinnate/Gedcom/Entity[SEX='F']", "circle"}, {"Kinnate/Gedcom/Entity[GedcomType='FAM']", "union"}});
    @XmlElement(name = "kin:AncestorFields")
    public IndexerParam ancestorFields = new IndexerParam(this, new String[][]{{"Kinnate.Gedcom.Entity.FAMC"}, {"Kinnate.Gedcom.Entity.BIRT.FAMC"}, {"Kinnate.Gedcom.Entity.CHR.FAMC"}, {"Kinnate.Gedcom.Entity.ADOP.FAMC"}, {"Kinnate.Gedcom.Entity.SLGC.FAMC"}, {"Kinnate.Gedcom.Entity.HUSB"}, {"Kinnate.Gedcom.Entity.WIFE"}});
//    public IndexerParam siblingFields = new IndexerParam(new String[]{{"Kinnate.Gedcom.Entity.CHIL"}, {"Kinnate.Gedcom.Entity.FAMS"}});
    @XmlElement(name = "kin:DecendantFields")
    public IndexerParam decendantFields = new IndexerParam(this, new String[][]{{"Kinnate.Gedcom.Entity.CHIL"}, {"Kinnate.Gedcom.Entity.FAMS"}});
    //    public IndexerParam showEntityFields = new IndexerParam(new String[][]{{"Kinnate/Gedcom/Entity/GedcomType=INDI"}, {"Kinnate/Gedcom/Entity/GedcomType=FAM"}}); // todo: add fields that can be used to controll which nodes are shown
    @Deprecated // I think this is no longer used or needed
    private String[][] relevantEntityData = null;

    @Deprecated // I think this is no longer used or needed
    public String[][] getRelevantEntityData() {
        if (relevantEntityData == null) {
            ArrayList<String[]> relevantDataList = new ArrayList<String[]>();
            for (IndexerParam currentIndexerParam : new IndexerParam[]{labelFields, symbolFieldsFields/*, showEntityFields*/}) {
                for (String[] currentData : currentIndexerParam.getValues()) {
                    relevantDataList.add(currentData);
                }
            }
            relevantEntityData = relevantDataList.toArray(new String[][]{});
        }
        return relevantEntityData;
    }
}
