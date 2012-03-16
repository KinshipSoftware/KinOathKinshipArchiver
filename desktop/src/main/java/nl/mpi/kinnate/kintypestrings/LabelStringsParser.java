package nl.mpi.kinnate.kintypestrings;

import java.util.Arrays;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityData.SymbolType;
import nl.mpi.kinnate.kindata.EntityDate;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : LabelStringsParser
 *  Created on : Jun 28, 2011, 11:30:00 AM
 *  Author     : Peter Withers
 */
public class LabelStringsParser {

    protected boolean userDefinedIdentifierFound = false;
    private UniqueIdentifier uniqueIdentifier;
    public boolean dateError = false;
    public int dateLocation = -1;
    public int dateEndLocation = -1;
    public int uidStartLocation = -1;
    public int uidEndLocation = -1;
    public String uidString = null;
    public String labelsStrings[] = new String[]{};
    public EntityDate dateOfBirth = new EntityDate("");
    public EntityDate dateOfDeath = new EntityDate("");
    protected String remainingInputString;

    protected LabelStringsParser(String inputString, String currentKinTypeString) {
        if (inputString.startsWith(":")) {
            String[] inputStringParts = inputString.split(":", 3);
            if (inputStringParts.length > 2) {
                remainingInputString = inputStringParts[2];
            } else {
                remainingInputString = "";
            }
            if (inputStringParts.length > 0) {
                // look for any date information
                // allow dates in the following formats "yyyy", "yyyy/mm", "yyyy/mm/dd"
                // allow date of birth followed by date of death eg "yyyy/mm/dd-yyyy/mm/dd" or "yyyy-yyyy" etc.
                // todo: it would be good to detect and show errors for more potential date format errors, currently they are just read as labels with no warning
                String remainingString = inputStringParts[1].replaceFirst(";[0-9]{4}(/[0-9]{2}){0,2}\\s?(abt|bef|aft){0,1}(-[0-9]{4}(/[0-9]{2}){0,2})?\\s?(abt|bef|aft){0,1}$", "");//(-[0-9]{4}(/[0-9]{2}){0,2}){1,2})?{1,2}
                if (remainingString.length() != inputStringParts[1].length()) {
                    String dateString = inputStringParts[1].substring(remainingString.length());
                    dateString = dateString.replaceFirst("^;", "");
                    dateLocation = dateString.length() + remainingInputString.length() + 1;
                    dateEndLocation = remainingInputString.length() + 1;
//                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                    String[] dateStringArray = dateString.split("-");
//                    try {
//                        while (dateStringArray[0].length() < "yyyy/MM/dd".length()) {
//                            dateStringArray[0] = dateStringArray[0] + "/01";
//                        }
//                        dateOfBirth = formatter.parse(dateStringArray[0]);
                    dateOfBirth = new EntityDate(dateStringArray[0]);
                    if (dateStringArray.length > 1) {
//                        while (dateStringArray[1].length() < "yyyy/MM/dd".length()) {
//                            dateStringArray[1] = dateStringArray[1] + "/01";
//                        }
//                            dateOfDeath = formatter.parse(dateStringArray[1]);
                        dateOfDeath = new EntityDate(dateStringArray[1]);
                    }
                    inputStringParts[1] = remainingString;
//                    } catch (EntityDateException exception) {
                    // not much to do here because we just ignore the date and leave the input as is
                    // todo: highlight the text to indicate the error
//                        System.out.println(exception.getMessage());
//                        dateError = true;
//                    }
                    if (dateOfBirth != null && !dateOfBirth.dateIsValid()) {
                        dateError = true;
                    }
                    if (dateOfDeath != null && !dateOfDeath.dateIsValid()) {
                        dateError = true;
                    }
                }
                // end look for any date information

                // todo: document and give examples of how these user defined identifiers work and that if the #0-9 type is not provided then the entire label string is used as the identifier
                labelsStrings = inputStringParts[1].split(";");
                if (labelsStrings[0].matches("^#[0-9]*")) {
                    // use the user defined number to identify the entity
                    uidString = labelsStrings[0];
                    uniqueIdentifier = new UniqueIdentifier("id:" + uidString, UniqueIdentifier.IdentifierType.tid);
                    userDefinedIdentifierFound = true;
                    uidStartLocation = inputString.length() - 1;
                    uidEndLocation = inputString.length() - labelsStrings[0].length() - 1;
                    labelsStrings = Arrays.copyOfRange(labelsStrings, 1, labelsStrings.length); // remove the user identifer string from the labels
                }
                if (uniqueIdentifier == null) {
                    // use the entire label sting to identify the entity
                    uniqueIdentifier = new UniqueIdentifier("label:" + inputString, UniqueIdentifier.IdentifierType.tid);
                    userDefinedIdentifierFound = true;
                }
            }
        }
    }

    public UniqueIdentifier getUniqueIdentifier() {
        if (uniqueIdentifier == null) {
            throw new UnsupportedOperationException("uniqueIdentifier has not been set, this should not occur");
        }
        return uniqueIdentifier;
    }

    public UniqueIdentifier getUniqueIdentifier(EntityData parentData, String kinTypeString, SymbolType symbolType) {

        if (uniqueIdentifier == null) {
            if (parentData != null) {
                // the parent id is used here to differentiate for instance M in the following two cases EmM EmFM, where if only M were used these two strings would get the same entity and be incorrect
                // the kinTypeString is used to make sure the id stays constant to the users expectations, if it chagnes then the entity will be given a new location on the diagram
                return new UniqueIdentifier(parentData.getUniqueIdentifier().getAttributeIdentifier() + "-" + kinTypeString + "-" + symbolType.name(), UniqueIdentifier.IdentifierType.tid);
            } else {
                return new UniqueIdentifier("label:" + "kintype:" + kinTypeString, UniqueIdentifier.IdentifierType.tid);
            }
        }
        return uniqueIdentifier;
    }
}
