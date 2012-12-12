/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.gedcomimport;

import java.util.ArrayList;
import nl.mpi.kinnate.kindata.DataTypes;

/**
 * Document : TipLineStructure
 * Created on : Jul 27, 2012, 6:03:51 PM
 * Author : Peter Withers
 */
public class TipLineStructure extends ImportLineStructure {

    public TipLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) throws ImportException {
        super(lineString, gedcomLevelStrings);
        isFileHeader = lineString.startsWith("*");
//        System.out.println("lineString: " + lineString);
        if (isFileHeader) {
            currentID = "head";
            gedcomLevel = 0;
            String[] lineParts = lineString.substring(1).split(":", 2);
            if (lineParts.length > 1) {
                addFieldEntry(lineParts[0], lineParts[1]);
            } else {
                addFieldEntry("Comment", lineParts[0]);
            }
        } else {
            String[] lineParts = lineString.split("\t");
            if (lineParts.length < 2) {
//                System.out.println("Incomplete line: \"" + lineString + "\"");
//                System.out.println("lineParts:" + lineParts.length);
                incompleteLine = true;
                return;
            }
            currentID = lineParts[1];
            gedcomLevel = 0; //Integer.parseInt(lineParts[0]);
            if (lineParts[0].equals("1")) {
//                System.out.println("1 Kinship line");
                if (lineParts.length != 4) {
                    throw new ImportException("Incorrect number of fields in line:\n" + lineString);
                }
                DataTypes.RelationType relationType = DataTypes.RelationType.other;
                if ("0".equals(lineParts[3])) {
                    relationType = DataTypes.RelationType.ancestor;
                } else if ("1".equals(lineParts[3])) {
                    relationType = DataTypes.RelationType.ancestor;
                } else if ("2".equals(lineParts[3])) {
                    relationType = DataTypes.RelationType.union;
                }
                addRelationEntry(lineParts[1], lineParts[2], relationType, null);
            } else if (lineParts[0].equals("2")) {
//                System.out.println("2 Property line");
                if (lineParts.length != 7) {
                    throw new ImportException("Incorrect number of fields in line:\n" + lineString);
                }
                if (!lineParts[3].trim().isEmpty()) {
                    addFieldEntry(lineParts[2], lineParts[3]);
                }
                if (!lineParts[4].trim().isEmpty()) {
                    addFieldEntry(lineParts[2].trim() + "_Place", lineParts[4]);
                }
                if (!lineParts[5].trim().isEmpty()) {
                    addFieldEntry(lineParts[2].trim() + "_Date", lineParts[5]);
                }
                if (!lineParts[6].trim().isEmpty()) {
                    // add the relation type here
                    addFieldEntry(lineParts[2].trim() + "_AlterID", lineParts[6]);
                    addRelationEntry(lineParts[1], lineParts[5], DataTypes.RelationType.other, lineParts[2].trim());
                }
            } else if (lineParts[0].equals("0")) {
//                System.out.println("0 Identity line");
                if (lineParts.length != 4) {
                    throw new ImportException("Incorrect number of fields in line:\n" + lineString);
                }
                // add name and gender at this point
                final String[] nameParts = lineParts[3].split("/");
                if (nameParts.length == 1) {
                    addFieldEntry("Name", nameParts[0]);
                } else {
                    for (int nameCount = 0; nameCount < nameParts.length; nameCount++) {
                        addFieldEntry("Name" + (nameCount + 1), nameParts[nameCount]);
                    }
                }
                if ("0".equals(lineParts[2])) {
                    addFieldEntry("Gender", "Male");
                } else if ("1".equals(lineParts[2])) {
                    addFieldEntry("Gender", "Female");
                } else if ("2".equals(lineParts[2])) {
                    addFieldEntry("Gender", "Unknown");
                } else {
                    addFieldEntry("Gender", lineParts[2]);
                }
                /*
                 * 0 Identity line: contains
                 * a. the individual's ID number
                 * b. gender number (0 male, 1 female, 2 unknown gender)
                 * c. name (different parts being separated by a slash (/)
                 */
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
        if (hasCurrentField()) {
            gedcomLevelStrings.add(getCurrentField().currentName);
//            System.out.println("currentID: " + currentID);
//            System.out.println("currentName: " + getCurrentField().currentName);
//            System.out.println("lineContents: " + getCurrentField().lineContents);
        }
    }

    public boolean isRelation() {
        return false;
    }
}
