/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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

/**
 * Document : GedcomLineStructure Created on : Jul 27, 2012, 6:03:36 PM
 *
 * @author Peter Withers
 */
public class GedcomLineStructure extends ImportLineStructure {

    public GedcomLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) throws ImportException {
        super(lineString, gedcomLevelStrings);
        isFileHeader = lineString.startsWith("0 HEAD");
        String[] lineParts = lineString.split(" ", 3);
        gedcomLevel = Integer.parseInt(lineParts[0]);
        if (isFileHeader) {
            currentID = lineParts[1];
            addFieldEntry(lineParts[1], null);
        } else if (!isFileHeader && gedcomLevel == 0) {
            currentID = lineParts[1];
//            currentName = lineParts[2];
            if (lineParts.length > 2) {
                setType(lineParts[2]);
                if (!currentID.startsWith("@") || !currentID.endsWith("@")) {
                    throw new ImportException("Incorrect gedcom identifier format: " + currentID);
                }
            }
        } else {
            if (lineParts.length > 2) {
                addFieldEntry(lineParts[1], lineParts[2]);
            } else {
                addFieldEntry(lineParts[1], "");
            }
        }
        while (gedcomLevelStrings.size() > gedcomLevel) {
            gedcomLevelStrings.remove(gedcomLevelStrings.size() - 1);
        }
        gedcomLevelStrings.add(lineParts[1]);
    }

    private void setType(String getdomType) {
        if (getdomType.equals("NOTE")) {
            entityType = "Gedcom Note";
        } else if (getdomType.equals("FAM")) {
            entityType = "Gedcom Family Group";
            deleteIfNoFeildsAdded = true;
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
    public String getProfileForEntityType(String individualTypeProfile, String otherTypeProfile) {
        if (entityType == null && !isFileHeader) {
            return individualTypeProfile;
        } else {
            return otherTypeProfile;
        }
    }

    @Override
    public String getEscapedLineContents() throws ImportException {
        return getLineContents().replace("@@", "@"); // the testged file does not have any instances of &gt; or &lt; so we do not replace these (.replace("&gt;", ">").replace("&lt;", "<");)
    }

    @Override
    public boolean isContinueLine() {
        return hasCurrentField() && getCurrentField().currentName.equals("CONC");
    }

    @Override
    public boolean isContinueLineBreak() {
        return hasCurrentField() && getCurrentField().currentName.equals("CONT");
    }

    @Override
    public boolean isEndOfFileMarker() {
        return currentID != null && currentID.equals("TRLR");
    }

    public boolean isRelation() {
        return hasCurrentField() && getCurrentField().lineContents != null && getCurrentField().lineContents.startsWith("@") && getCurrentField().lineContents.endsWith("@");
    }
}
