package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;

/**
 * Document : TipLineStructure
 * Created on : Jul 27, 2012, 6:03:51 PM
 * Author : Peter Withers
 */
public class TipLineStructure extends ImportLineStructure {

    public TipLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) throws ImportException {
        super(lineString, gedcomLevelStrings);
        isFileHeader = lineString.startsWith("*");
        System.out.println("lineString: " + lineString);
        if (isFileHeader) {
            currentID = "head";
            gedcomLevel = 0;
            String[] lineParts = lineString.substring(1).split(":", 2);
            currentName = lineParts[0];
            lineContents = "";
            if (lineParts.length > 1) {
                lineContents = lineParts[1];
            }
        } else {
            String[] lineParts = lineString.split("\t");
            if (lineParts.length < 2) {
                System.out.println("Incomplete line: " + lineString);
                System.out.println("lineParts:" + lineParts.length);
                incompleteLine = true;
                return;
            }
            currentID = lineParts[1];
            gedcomLevel = 0; //Integer.parseInt(lineParts[0]);
            if (lineParts[0].equals("1")) {
                System.out.println("1 Kinship line");
                // todo: add relations at this point
//                currentName = "A realation ID";
//                lineContents = lineParts[2];
            } else if (lineParts[0].equals("2")) {
                System.out.println("2 Property line");
                // todo: add notes etc at this point
                currentName = lineParts[2];
                lineContents = lineParts[3];
                // todo: handle cv envents
                /*
                 * d. the place (in case of cv events)
                 * e. the date (in case of cv events)
                 * f. alter's ID number (in case of cv events)
                 */
            } else if (lineParts[0].equals("0")) {
                System.out.println("0 Identity line");
                // add name and gender at this point
                currentName = "Name";
                if (lineParts.length > 3) {
                    lineContents = lineParts[3];
                }
            } else {
                throw new ImportException("Invalid TIP line type: " + lineString);
//                currentName = lineParts[2];
//                lineContents = "";
//                if (lineParts.length > 3) {
//                    lineContents = lineParts[3];
//                }
            }
            /*
             * Tip format (file extension .tip) [example]
             * This is the format most rapidly read by Puck. In this format a corpus is stored when pressing the "Save" button.
             * The tip format stores all information in three kind of lines, distinguished by the first number of the line. The different items of each entry are separated by tabs:
             *
             * 0 Identity line: contains
             *
             * a. the individual's ID number
             * b. gender number (0 male, 1 female, 2 unknown gender)
             * c. name (different parts being separated by a slash (/)
             *
             * 1 Kinship line: contains
             *
             * a. the individual's ID number
             * b. alter's ID number
             * c. the code of the kinsip relation (0 father, 1 mother, 2 spouse)
             *
             * 2 Property line: contains
             *
             * a. the individual's ID number
             * b. the label of the property, using individual property codes
             * c. the property value
             * d. the place (in case of cv events)
             * e. the date (in case of cv events)
             * f. alter's ID number (in case of cv events)
             *
             * There is no particular order prescribed. Although Puck exports data ordered by individuals, any information can be added manually at the end of the file.
             */
        }
        while (gedcomLevelStrings.size() > gedcomLevel) {
            gedcomLevelStrings.remove(gedcomLevelStrings.size() - 1);
        }
        gedcomLevelStrings.add(currentName);
        System.out.println("currentID: " + currentID);
        System.out.println("currentName: " + currentName);
        System.out.println("lineContents: " + lineContents);
    }

    public boolean isRelation() {
        return lineContents != null && lineContents.startsWith("@") && lineContents.endsWith("@");
    }
}