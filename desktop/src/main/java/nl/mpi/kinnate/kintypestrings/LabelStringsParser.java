package nl.mpi.kinnate.kintypestrings;

import java.util.Date;
import nl.mpi.kinnate.kindata.EntityData;

/**
 *  Document   : LabelStringsParser
 *  Created on : Jun 28, 2011, 11:30:00 AM
 *  Author     : Peter Withers
 */
public class LabelStringsParser {

    String userDefinedIdentifier = null;
    String labelsStrings[] = new String[]{};
    Date dateOfBirth = null; // todo: read in the dates and if found set the in the entities
    Date dateOfDeath = null;
    String remainingInputString;

    protected LabelStringsParser(String inputString, EntityData parentData, String currentKinTypeString) {
        if (inputString.startsWith(":")) {
            String[] inputStringParts = inputString.split(":", 3);
            if (inputStringParts.length > 0) {
                // todo: document and give examples of how these user defined identifiers work and that if the #0-9 type is not provided then the entire label string is used as the identifier
                labelsStrings = inputStringParts[1].split(";");
                if (labelsStrings[0].matches("^#[0-9]*")) {
                    userDefinedIdentifier = "id:" + labelsStrings[0];
                }
                if (userDefinedIdentifier == null) {
                    userDefinedIdentifier = "label:" + inputStringParts[1];
                }
            }
            if (inputStringParts.length > 2) {
                remainingInputString = inputStringParts[2];
            } else {
                remainingInputString = "";
            }
        }
    }
}
