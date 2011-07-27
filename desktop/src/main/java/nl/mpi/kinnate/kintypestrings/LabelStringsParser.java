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

    protected boolean userDefinedIdentifierFound = false;
    public UniqueIdentifier uniqueIdentifier;
    public String labelsStrings[] = new String[]{};
    public Date dateOfBirth = null; // todo: read in the dates and if found set the in the entities
    public Date dateOfDeath = null;
    protected String remainingInputString;

    protected LabelStringsParser(String inputString, EntityData parentData, String currentKinTypeString) {
        if (inputString.startsWith(":")) {
            String[] inputStringParts = inputString.split(":", 3);
            if (inputStringParts.length > 0) {
                // todo: document and give examples of how these user defined identifiers work and that if the #0-9 type is not provided then the entire label string is used as the identifier
                labelsStrings = inputStringParts[1].split(";");
                if (labelsStrings[0].matches("^#[0-9]*")) {
                    // use the user defined number to identify the entity
                    uniqueIdentifier = new UniqueIdentifier("id:" + labelsStrings[0], UniqueIdentifier.IdentifierType.tid);
                    userDefinedIdentifierFound = true;
                }
                if (uniqueIdentifier == null) {
                    // use the entire label sting to identify the entity
                    uniqueIdentifier = new UniqueIdentifier("label:" + inputStringParts[1], UniqueIdentifier.IdentifierType.tid);
                    userDefinedIdentifierFound = true;
                }
            }
            if (inputStringParts.length > 2) {
                remainingInputString = inputStringParts[2];
            } else {
                remainingInputString = "";
            }
        }
        if (uniqueIdentifier == null) {
            if (parentData != null) {
                // the parent id is used here to differentiate for instance M in the following two cases EmM EmFM, where if only M were used these two strings would get the same entity and be incorrect
                uniqueIdentifier = new UniqueIdentifier(parentData.getUniqueIdentifier().getAttributeIdentifier() + currentKinTypeString, UniqueIdentifier.IdentifierType.tid);
            } else {
                uniqueIdentifier = new UniqueIdentifier("label:" + "kintype:" + currentKinTypeString, UniqueIdentifier.IdentifierType.tid);
            }
        }
    }
}
