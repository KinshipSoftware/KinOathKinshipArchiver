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
        System.out.println("lineString: " + lineString);
        if (isFileHeader) {
            gedcomLevel = 0;
            String[] lineParts = lineString.substring(1).split(":", 1);
            currentName = lineParts[0];
            lineContents = "";
            if (lineParts.length > 1) {
                lineContents = lineParts[1];
            }
        } else {
            String[] lineParts = lineString.split("\t", 4);
            System.out.println("lineParts:" + lineParts.length);
            System.out.println("lineParts:" + lineParts);
            currentID = lineParts[1];
            if (lineParts[0].equals("1")) {
                // todo: add relations at this point
            } else {
                gedcomLevel = Integer.parseInt(lineParts[0]);
                currentName = lineParts[1];
                lineContents = "";
                if (lineParts.length > 3) {
                    lineContents = lineParts[3];
                }
            }
        }
        while (gedcomLevelStrings.size() > gedcomLevel) {
            gedcomLevelStrings.remove(gedcomLevelStrings.size() - 1);
        }
        gedcomLevelStrings.add(currentName);
    }

    public boolean isRelation() {
        return lineContents != null && lineContents.startsWith("@") && lineContents.endsWith("@");
    }
}
