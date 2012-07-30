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
    String currentName;
    boolean isFileHeader;

    public ImportLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) {
    }

    public String getCurrentName() {
        return currentName;
    }

    public int getGedcomLevel() {
        return gedcomLevel;
    }

    public boolean hasLineContents() {
        return lineContents != null;
    }

    public String getLineContents() {
        return lineContents;
    }

    public boolean isFileHeader() {
        return isFileHeader;
    }

    abstract boolean isRelation();
}
