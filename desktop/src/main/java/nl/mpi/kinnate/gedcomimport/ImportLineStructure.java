package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;

/**
 * Document : ImportLineStructure
 * Created on : Jul 30, 2012, 9:23:36 AM
 * Author : Peter Withers
 */
public abstract class ImportLineStructure {

    int gedcomLevel = 0;
    String currentID = null;
    String entityType = null;
    boolean isFileHeader = false;
    boolean incompleteLine = false;
    private int currentFieldIndex = 0;

    protected class RelationEntry {

        protected String egoIdString;
        protected String alterIdString;
        protected DataTypes.RelationType relationType;
        protected String customType;

        public RelationEntry(String egoIdString, String alterIdString, RelationType relationType, String customType) {
            this.egoIdString = egoIdString;
            this.alterIdString = alterIdString;
            this.relationType = relationType;
            this.customType = customType;
        }
    }

    protected class FieldEntry {

        protected String lineContents = null;
        protected String currentName = null;

        protected FieldEntry(String currentName, String lineContents) throws ImportException {
            if (currentName == null) {
                throw new ImportException("Cannot have null names to a field.");
            }
            this.currentName = currentName.trim();
            if (lineContents != null) {
                this.lineContents = lineContents.trim();
            }
        }
    }
    ArrayList<FieldEntry> fieldEntryList = new ArrayList<FieldEntry>();
    ArrayList<RelationEntry> relationEntryList = new ArrayList<RelationEntry>();

    public ImportLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) {
    }

    protected void addFieldEntry(String currentName, String lineContents) throws ImportException {
        fieldEntryList.add(new FieldEntry(currentName, lineContents));
    }

    protected void addRelationEntry(String egoIdString, String alterIdString, RelationType relationType, String customType) throws ImportException {
        relationEntryList.add(new RelationEntry(egoIdString, alterIdString, relationType, customType));
    }

    protected FieldEntry getCurrentField() {
        return fieldEntryList.get(currentFieldIndex);
    }

    protected void moveToNextField() {
        currentFieldIndex++;
    }

    protected boolean hasCurrentField() {
        return currentFieldIndex < fieldEntryList.size();
    }

    public RelationEntry[] getRelationList() {
        return relationEntryList.toArray(new RelationEntry[0]);
    }

    public String getCurrentID() throws ImportException {
        if (currentID == null) {
//            new Exception().printStackTrace();
            throw new ImportException("CurrentID has not been set");
        }
        return currentID;
    }

    public String getCurrentName() throws ImportException {
        if (getCurrentField().currentName == null) {
//            new Exception().printStackTrace();
            throw new ImportException("CurrentName has not been set");
        }
        return getCurrentField().currentName;
    }

    public int getGedcomLevel() {
        return gedcomLevel;
    }

    public boolean hasLineContents() {
        return hasCurrentField() && getCurrentField().lineContents != null;
    }

    public String getLineContents() throws ImportException {
        if (!hasCurrentField() || getCurrentField().lineContents == null) {
//            new Exception().printStackTrace();
            throw new ImportException("LineContents has not been set");
        }
        return getCurrentField().lineContents;
    }

    public String getEntityType() {
        return entityType;
    }

    public boolean isFileHeader() {
        return isFileHeader;
    }

    public boolean isContinueLine() {
        return false;
    }

    public boolean isContinueLineBreak() {
        return false;
    }

    public boolean isEndOfFileMarker() {
        return false;
    }

    public boolean isIncompleteLine() {
        return incompleteLine;
    }

    abstract boolean isRelation();
}
