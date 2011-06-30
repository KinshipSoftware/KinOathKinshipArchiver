package nl.mpi.kinnate.kintypestrings;

import java.util.Date;
import nl.mpi.kinnate.kindata.EntityData;

/**
 *  Document   : LabelStringsParser
 *  Created on : Jun 28, 2011, 11:30:00 AM
 *  Author     : Peter Withers
 */
public class LabelStringsParser {

    public static String transientNodePrefix = "transient:";
    boolean identifierFound = false;
    String idString;
    String labelsStrings[] = new String[]{};
    Date dateOfBirth = null; // todo: read in the dates and if found set the in the entities
    Date dateOfDeath = null;
    String remainingInputString;

    protected LabelStringsParser(String inputString, EntityData parentData, String currentKinTypeString) {
        if (inputString.startsWith(":")) {
            String[] inputStringParts = inputString.split(":", 3);
            if (inputStringParts.length > 0) {
                labelsStrings = inputStringParts[1].split(";");
                idString = transientNodePrefix + "label:" + inputStringParts[1];
                identifierFound = true;
            }
            if (inputStringParts.length > 2) {
                remainingInputString = inputStringParts[2];
            } else {
                remainingInputString = "";
            }
        }
        if (!identifierFound) {
            if (parentData != null) {
                idString = parentData.getUniqueIdentifier() + currentKinTypeString;
            } else {
                idString = transientNodePrefix + "kintype:" + currentKinTypeString;
            }
        }
    }
}
