package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;

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

    public ImportLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) {
    }

    protected void addFieldEntry(String currentName, String lineContents) throws ImportException {
        fieldEntryList.add(new FieldEntry(currentName, lineContents));
    }

    private FieldEntry getFirst() {
        currentFieldIndex = 0;
        return fieldEntryList.get(currentFieldIndex);
    }

    protected FieldEntry getCurrent() {
        return fieldEntryList.get(currentFieldIndex);
    }

    private FieldEntry getNext() {
        currentFieldIndex++;
        return fieldEntryList.get(currentFieldIndex);
    }

    protected boolean hasCurrent() {
        return currentFieldIndex < fieldEntryList.size();
    }

    public String getCurrentID() throws ImportException {
        if (currentID == null) {
//            new Exception().printStackTrace();
            throw new ImportException("CurrentID has not been set");
        }
        return currentID;
    }

    public String getCurrentName() throws ImportException {
        if (getCurrent().currentName == null) {
//            new Exception().printStackTrace();
            throw new ImportException("CurrentName has not been set");
        }
        return getCurrent().currentName;
    }

    public int getGedcomLevel() {
        return gedcomLevel;
    }

    public boolean hasLineContents() {
        return hasCurrent() && getCurrent().lineContents != null;
    }

    public String getLineContents() throws ImportException {
        if (!hasCurrent() || getCurrent().lineContents == null) {
//            new Exception().printStackTrace();
            throw new ImportException("LineContents has not been set");
        }
        return getCurrent().lineContents;
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
