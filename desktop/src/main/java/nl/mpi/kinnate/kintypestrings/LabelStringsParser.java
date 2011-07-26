package nl.mpi.kinnate.kintypestrings;

import java.util.Date;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : LabelStringsParser
 *  Created on : Jun 28, 2011, 11:30:00 AM
 *  Author     : Peter Withers
 */
public class LabelStringsParser {

    boolean identifierFound = false;
    UniqueIdentifier transientIdentifier;
    String labelsStrings[] = new String[]{};
    Date dateOfBirth = null; // todo: read in the dates and if found set the in the entities
    Date dateOfDeath = null;
    String remainingInputString;

    protected LabelStringsParser(String inputString, EntityData parentData, String currentKinTypeString) {
        String remainderString = inputString.replaceFirst("^#[0-9]*", "");
        if (inputString.length() != remainderString.length()) {
            String idString = inputString.substring(1, remainderString.length());
            transientIdentifier = new UniqueIdentifier("id:" + idString, UniqueIdentifier.IdentifierType.tid);
            identifierFound = true;
            inputString = remainderString;
        }
        if (inputString.startsWith(":")) {
            String[] inputStringParts = inputString.split(":", 3);
            if (inputStringParts.length > 0) {
                labelsStrings = inputStringParts[1].split(";");
                if (transientIdentifier == null) {
                    transientIdentifier = new UniqueIdentifier("label:" + inputStringParts[1], UniqueIdentifier.IdentifierType.tid);
                    identifierFound = true;
                }
            }
            if (inputStringParts.length > 2) {
                remainingInputString = inputStringParts[2];
            } else {
                remainingInputString = "";
            }
        }
        if (transientIdentifier == null) {
            transientIdentifier = new UniqueIdentifier(UniqueIdentifier.IdentifierType.tid);
        }
    }
}
