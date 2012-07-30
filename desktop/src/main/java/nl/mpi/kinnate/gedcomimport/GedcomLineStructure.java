package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;

/**
 * Document : GedcomLineStructure
 * Created on : Jul 27, 2012, 6:03:36 PM
 * Author : Peter Withers
 */
public class GedcomLineStructure extends ImportLineStructure {

    public GedcomLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) throws ImportException {
        super(lineString, gedcomLevelStrings);
        isFileHeader = lineString.startsWith("0 HEAD");
        String[] lineParts = lineString.split(" ", 3);
        gedcomLevel = Integer.parseInt(lineParts[0]);
        if (!isFileHeader && gedcomLevel == 0) {
            currentID = lineParts[1];
            if (lineParts.length > 2) {
                setType(lineParts[2]);
                if (!currentID.startsWith("@") || !currentID.endsWith("@")) {
                    throw new ImportException("Incorrect gedcom identifier format: " + currentID);
                }
            }
        } else {
            currentID = lineParts[1];
            currentName = lineParts[1];
            lineContents = "";
            if (lineParts.length > 2) {
                lineContents = lineParts[2];
            }
        }
        while (gedcomLevelStrings.size() > gedcomLevel) {
            gedcomLevelStrings.remove(gedcomLevelStrings.size() - 1);
        }
        gedcomLevelStrings.add(currentName);
    }

    private void setType(String getdomType) {
        if (getdomType.equals("NOTE")) {
            entityType = "Gedcom Note";
        } else if (getdomType.equals("FAM")) {
            entityType = "Gedcom Family Group";
        } else if (getdomType.equals("INDI")) {
            // do not set a type for individuals
        } else if (getdomType.equals("OBJE")) {
            entityType = "Resource File";
        } else if (getdomType.equals("REPO")) {
            entityType = "Repository";
        } else if (getdomType.equals("SUBN")) {
            entityType = "Submission";
        } else if (getdomType.equals("SOUR")) {
            entityType = "Source";
        } else if (getdomType.equals("SUBM")) {
            entityType = "Submitter";
        } else {
            entityType = getdomType;
        }
    }

    @Override
    public boolean isContineLine() {
        return currentName != null && currentName.equals("CONC");
    }

    @Override
    public boolean isContineLineBreak() {
        return currentName != null && currentName.equals("CONT");
    }

    @Override
    public boolean isEndOfFileMarker() {
        return currentID != null && currentID.equals("TRLR");
    }

    public boolean isRelation() {
        return lineContents != null && lineContents.startsWith("@") && lineContents.endsWith("@");
    }
}
