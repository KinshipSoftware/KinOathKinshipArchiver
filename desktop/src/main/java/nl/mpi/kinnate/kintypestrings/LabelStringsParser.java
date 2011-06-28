package nl.mpi.kinnate.kintypestrings;

import java.util.Date;

/**
 *  Document   : LabelStringsParser
 *  Created on : Jun 28, 2011, 11:30:00 AM
 *  Author     : Peter Withers
 */
public class LabelStringsParser {

    boolean identifierFound = false;
    String idString;
    String labelsStrings[] = new String[]{};
    Date dateOfBirth = null;
    Date dateOfDeath = null;
    String remainingInputString;

    protected LabelStringsParser(String inputString, String fullKinTypeString) {
        if (inputString.startsWith(":")) {
            String[] inputStringParts = inputString.split(":");
            if (inputStringParts.length > 0) {
                labelsStrings = inputStringParts[1].split("\\|");
                identifierFound = true;
            }
            if (inputStringParts.length > 2) {
                remainingInputString = inputStringParts[2];
            } else {
                remainingInputString = "";
            }
        }
        if (!identifierFound) {
            idString = fullKinTypeString;
        }
    }
}
