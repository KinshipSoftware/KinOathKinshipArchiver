package nl.mpi.kinnate.EntityIndexer;

/**
 *  Document   : IndexParameters
 *  Created on : Feb 14, 2011, 11:47:34 AM
 *  Author     : Peter Withers
 */
public class IndexerParameters {

    public String linkPath = "/Kinnate/Relation/Link";
    public String[] relevantEntityData = new String[]{"Kinnate/Gedcom/Entity/NoteText", "Kinnate/Gedcom/Entity/SEX", "Kinnate/Gedcom/Entity/GedcomType", "Kinnate/Gedcom/Entity/NAME/NAME", "Kinnate/Gedcom/Entity/NAME/NPFX"}; // todo: the relevantData array comes from the user via the svg
    public String[] relevantLinkData = new String[]{"Type"};
    public String[] labelFields = {"Kinnate/Gedcom/Entity/NAME/NAME", "Kinnate/Gedcom/Entity/GedcomType", "Kinnate/Gedcom/Entity/Text", "Kinnate/Gedcom/Entity/NAME/NPFX", "Kinnate/Gedcom/Entity/NoteText"};
    public String[] symbolFieldsFields = {"Kinnate/Gedcom/Entity/SEX", "Kinnate/Gedcom/Entity/GedcomType"};
    public String[] ancestorFields = {"Kinnate.Gedcom.Entity.FAMC", "Kinnate.Gedcom.Entity.HUSB", "Kinnate.Gedcom.Entity.WIFE"};
    public String[] decendantFields = {"Kinnate.Gedcom.Entity.CHIL", "Kinnate.Gedcom.Entity.FAMS"};
}
