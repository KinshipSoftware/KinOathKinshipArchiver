package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;

/**
 * Document : GedcomLineStructure
 * Created on : Jul 27, 2012, 6:03:36 PM
 * Author : Peter Withers
 */
public class GedcomLineStructure extends ImportLineStructure {

    public GedcomLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) {
        super(lineString, gedcomLevelStrings);
        isFileHeader = lineString.startsWith("0 HEAD");
        String[] lineParts = lineString.split(" ", 3);
        gedcomLevel = Integer.parseInt(lineParts[0]);
        currentName = lineParts[1];
        while (gedcomLevelStrings.size() > gedcomLevel) {
            gedcomLevelStrings.remove(gedcomLevelStrings.size() - 1);
        }
        lineContents = "";
        if (lineParts.length > 2) {
            lineContents = lineParts[2];
        }
        gedcomLevelStrings.add(currentName);
    }

    public boolean isRelation() {
        return lineContents != null && lineContents.startsWith("@") && lineContents.endsWith("@");
    }
}
