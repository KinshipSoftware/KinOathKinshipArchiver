package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;

/**
 * Document : ImportLineStructure
 * Created on : Jul 30, 2012, 9:23:36 AM
 * Author : Peter Withers
 */
public abstract class ImportLineStructure {

    String lineContents = null;
    int gedcomLevel = 0;
    String currentName = null;
    String currentID = null;
    String entityType = null;
    boolean isFileHeader = false;
    boolean incompleteLine = false;

    public ImportLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) {
    }

    public String getCurrentID() throws ImportException {
        if (currentID == null) {
//            new Exception().printStackTrace();
            throw new ImportException("CurrentID has not been set");
        }
        return currentID;
    }

    public String getCurrentName() throws ImportException {
        if (currentName == null) {
//            new Exception().printStackTrace();
            throw new ImportException("CurrentName has not been set");
        }
        return currentName;
    }

    public int getGedcomLevel() {
        return gedcomLevel;
    }

    public boolean hasLineContents() {
        return lineContents != null;
    }

    public String getLineContents() throws ImportException {
        if (lineContents == null) {
//            new Exception().printStackTrace();
            throw new ImportException("LineContents has not been set");
        }
        return lineContents;
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
