package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;

/**
 * Document : TipLineStructure
 * Created on : Jul 27, 2012, 6:03:51 PM
 * Author : Peter Withers
 */
public class TipLineStructure extends ImportLineStructure {

    public TipLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) {
        super(lineString, gedcomLevelStrings);
        isFileHeader = lineString.startsWith("*");
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
