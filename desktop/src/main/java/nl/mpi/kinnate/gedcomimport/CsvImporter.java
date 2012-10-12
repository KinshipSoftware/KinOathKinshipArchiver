package nl.mpi.kinnate.gedcomimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.ImportTranslator;

/**
 * Document : CsvImporter Created on : May 30, 2011, 10:29:24 AM
 *
 * @author Peter Withers
 */
public class CsvImporter extends EntityImporter implements GenericImporter {

    public CsvImporter(JProgressBar progressBarLocal, JTextArea importTextAreaLocal, boolean overwriteExistingLocal, SessionStorage sessionStorage) {
        super(progressBarLocal, importTextAreaLocal, overwriteExistingLocal, sessionStorage);
    }

    @Override
    public boolean canImport(String inputFileString) {
        return (inputFileString.toLowerCase().endsWith(".csv") || inputFileString.toLowerCase().endsWith(".txt"));
    }

    @Deprecated
    private String cleanCsvString(String valueString) {
        valueString = valueString.replaceAll("^\"", "");
        valueString = valueString.replaceAll("\"$", "");
        valueString = valueString.replaceAll("\"\"", "");
        return valueString;
    }

//    private char detectFieldDelimiter(BufferedReader bufferedReader) throws IOException {
//        int tabCounter = 0;
//        int commaCounter = 0;
//        outerLoop:
//        for (int charCount = 0; charCount < 1000; charCount++) {
//            try {
//                switch (bufferedReader.read()) {
//                    case -1:
//                    case '\n':
//                    case '\r':
//                        break outerLoop;
//                    case ',':
//                        commaCounter++;
//                        break;
//                    case '\t':
//                        tabCounter++;
//                        break;
//                }
//            } catch (IOException exception) {
//                appendToTaskOutput("Failed to test the file type");
//                throw exception;
//            }
//        }
//        char fieldSeparator;
//        if (commaCounter > tabCounter) {
//            fieldSeparator = ',';
//            appendToTaskOutput("Comma separated file detected");
//        } else {
//            fieldSeparator = '\t';
//            appendToTaskOutput("Tab separated file detected");
//        }
//        return fieldSeparator;
//    }
    private boolean isAnExcludedId(String idString, boolean firstLineIdZero) {
        if (firstLineIdZero) {
            return false;
        } else {
            return !"0".equals(idString);
        }
    }

    protected ArrayList<String> getFieldsForLineExcludingComments(BufferedReader bufferedReader /* , char fieldSeparator */) throws IOException {
        ArrayList<String> fieldsForLine = null;
        while (fieldsForLine == null || (fieldsForLine.size() > 0 && fieldsForLine.get(0).startsWith("*"))) {
            fieldsForLine = getFieldsForLine(bufferedReader);
        }
        return fieldsForLine;
    }

    protected ArrayList<String> getFieldsForLine(BufferedReader bufferedReader /* , char fieldSeparator */) throws IOException {
        ArrayList<String> lineFields = new ArrayList<String>();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            int readChar = bufferedReader.read();
            while (readChar != -1) {
                switch (readChar) {
                    case '\n':
                    case '\r':
                        if (stringBuilder.length() > 0) {
                            lineFields.add(stringBuilder.toString());
                        }
                        if (lineFields.isEmpty()) {
                            // if this is the first chars on the line then we can continue looking for more data
                            break;
                        }
                    case -1:
                        return lineFields;
                    case '"':
                        boolean insideQuotes = true;
                        int quotedCharsCount = 0;
                        while (insideQuotes) {
                            // ignore all the chars between quotes
                            final int readQuoted = bufferedReader.read();
                            if (readQuoted == -1) {
                                appendToTaskOutput("Warning: file ended within a quoted section");
                                return lineFields;
                            }
                            quotedCharsCount++;
                            insideQuotes = readQuoted != '"';
                            if (insideQuotes) {
                                // add the chars from within the quotes
                                stringBuilder.append((char) readQuoted);
                            }
                        }
                        if (quotedCharsCount < 1) {
                            // todo: test that escaped quotes pass into the output correctly
                            // allow "" excaped quotes to pass as a single quote into the output
                            stringBuilder.append((char) readChar);
                        }
                        break;
                    case ',':
                    case '\t':
                        lineFields.add(stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                        break;
                    default:
                        stringBuilder.append((char) readChar);
                }
//                if (readChar == fieldSeparator) {
//                    lineFields.add(stringBuilder.toString());
//                } else {
//                    stringBuilder.append(readChar);
//                }
                readChar = bufferedReader.read();
            }
        } catch (IOException exception) {
            appendToTaskOutput("Failed to read lines of input file");
            throw exception;
        }
        if (stringBuilder.length() > 0) {
            lineFields.add(stringBuilder.toString());
        }
        return lineFields;
    }

    @Override
    public URI[] importFile(InputStreamReader inputStreamReader, String profileId) {
        // output some text to explain which columns to use for parent,child,spouse,sibling etc and for Gender dateofbirth etc
        appendToTaskOutput("If a column called 'ID' exists it will be used as the identifier for each line.");
        appendToTaskOutput("If the ID column contains the same value twice, then the preceding entity will be updated/appended to.");
        appendToTaskOutput("If the ID column contains a UniqueIdentifier already in the project then that entity will be updated/appended to.");
        appendToTaskOutput("");
        appendToTaskOutput("Lines starting with * will be treated as comments and ignored.");
        appendToTaskOutput("");
        appendToTaskOutput("Recommended data columns are:");
        appendToTaskOutput("Name");
        appendToTaskOutput("Gender");
        appendToTaskOutput("DateOfBirth");
        appendToTaskOutput("DateOfDeath");
        appendToTaskOutput("");
        // todo:  #2394 In CSV import add support for ID column in any location and check that if a unique identifier is supplied that will preserve/add to an existing individual. Also add info text to explain the use of this in the CSV text output.
//        appendToTaskOutput("Recommended relation fields are:");
//        appendToTaskOutput("ParentID");
        appendToTaskOutput("Relation columns must map to the values in the ID column.");
        appendToTaskOutput("Recognised relation columns are:");
        String[] unionColumns = new String[]{"spouse", "union"};
        for (String nameString : unionColumns) {
            appendToTaskOutput(nameString);
        }
        String[] parentColumns = new String[]{"parent", "father", "mother"};
        for (String nameString : parentColumns) {
            appendToTaskOutput(nameString);
        }
        appendToTaskOutput("");
        appendToTaskOutput("Additional supported relation columns are:");
        appendToTaskOutput("Spouses<number>-ID");
        appendToTaskOutput("Parents<number>-ID");
        appendToTaskOutput("Children<number>-ID");
        appendToTaskOutput("");
        appendToTaskOutput("Any other columns will be added to the kindata but not automatically used in the subsequent diagrams.");
        appendToTaskOutput("");
        appendToTaskOutput("If the ID field exists and any row contains the text ID then the headers are replaced with current row values (for PUCK txt files).");
        appendToTaskOutput("If the first record does not start at zero then all relations to the ID of zero will be ignored.");
        appendToTaskOutput("");
        ArrayList<URI> createdNodes = new ArrayList<URI>();
        try {
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//            char fieldSeparator = detectFieldDelimiter(bufferedReader);
            // restart at the begining of the file
//            bufferedReader = new BufferedReader(inputStreamReader);
            // create the import translator
            ImportTranslator importTranslator = new ImportTranslator(true);
            importTranslator.addTranslationEntry("Gender", "0", "Gender", "Female");
            importTranslator.addTranslationEntry("Gender", "1", "Gender", "Male");
            importTranslator.addTranslationEntry("Gender", "f", "Gender", "Female");
            importTranslator.addTranslationEntry("Gender", "m", "Gender", "Male");
            importTranslator.addTranslationEntry("Sex", "m", "Gender", "Male");
            importTranslator.addTranslationEntry("Sex", "f", "Gender", "Female");
            importTranslator.addTranslationEntry("Sex", "h", "Gender", "Male");

            importTranslator.addTranslationEntry("Date_of_Birth", null, "DateOfBirth", null);
            importTranslator.addTranslationEntry("Date_of_Death", null, "DateOfDeath", null);

            ArrayList<String> allHeadings = getFieldsForLineExcludingComments(bufferedReader /* , fieldSeparator */);
            for (int columnCounter = 0; columnCounter < allHeadings.size(); columnCounter++) {
                String titleString = allHeadings.get(columnCounter);
                if (titleString.equals("")) {
                    appendToTaskOutput("Warning: No title found for column. Inserting \"Untitled\" as the column name.");
                    allHeadings.set(columnCounter, "Untitled");
                } else {
                    if (titleString.matches("$0-9.*")) {
                        appendToTaskOutput("Warning: Column title \"" + titleString + "\" starts with a number.");
                    }
                    if (!titleString.matches("[a-zA-Z0-9]+")) {
                        appendToTaskOutput("Warning: Column title \"" + titleString + "\" contains invalid characters. Punctuation and white space are not allowed (so that the data is compatable with XML and other applications).");
                    }
                }
            }
            super.incrementLineProgress();
//            int maxImportCount = 10;
//            "ID","Gender","Name1","Name2","Name3","Name4","Name5","Name6","Date of Birth","Date of Death","sub_group","Parents1-ID","Parents1-Gender","Parents1-Name1","Parents1-Name2","Parents1-Name3","Parents1-Name4","Parents1-Name5","Parents1-Name6","Parents1-Date of Birth","Parents1-Date of Death","Parents1-sub_group","Parents2-ID","Parents2-Gender","Parents2-Name1","Parents2-Name2","Parents2-Name3","Parents2-Name4","Parents2-Name5","Parents2-Name6","Parents2-Date of Birth","Parents2-Date of Death","Parents2-sub_group","Spouses1-ID","Spouses1-Gender","Spouses1-Name1","Spouses1-Name2","Spouses1-Name3","Spouses1-Name4","Spouses1-Name5","Spouses1-Name6","Spouses1-Date of Birth","Spouses1-Date of Death","Spouses1-sub_group","Spouses2-ID","Spouses2-Gender","Spouses2-Name1","Spouses2-Name2","Spouses2-Name3","Spouses2-Name4","Spouses2-Name5","Spouses2-Name6","Spouses2-Date of Birth","Spouses2-Date of Death","Spouses2-sub_group","Spouses3-ID","Spouses3-Gender","Spouses3-Name1","Spouses3-Name2","Spouses3-Name3","Spouses3-Name4","Spouses3-Name5","Spouses3-Name6","Spouses3-Date of Birth","Spouses3-Date of Death","Spouses3-sub_group","Spouses4-ID","Spouses4-Gender","Spouses4-Name1","Spouses4-Name2","Spouses4-Name3","Spouses4-Name4","Spouses4-Name5","Spouses4-Name6","Spouses4-Date of Birth","Spouses4-Date of Death","Spouses4-sub_group","Spouses5-ID","Spouses5-Gender","Spouses5-Name1","Spouses5-Name2","Spouses5-Name3","Spouses5-Name4","Spouses5-Name5","Spouses5-Name6","Spouses5-Date of Birth","Spouses5-Date of Death","Spouses5-sub_group","Spouses6-ID","Spouses6-Gender","Spouses6-Name1","Spouses6-Name2","Spouses6-Name3","Spouses6-Name4","Spouses6-Name5","Spouses6-Name6","Spouses6-Date of Birth","Spouses6-Date of Death","Spouses6-sub_group","Children1-ID","Children1-Gender","Children1-Name1","Children1-Name2","Children1-Name3","Children1-Name4","Children1-Name5","Children1-Name6","Children1-Date of Birth","Children1-Date of Death","Children1-sub_group","Children2-ID","Children2-Gender","Children2-Name1","Children2-Name2","Children2-Name3","Children2-Name4","Children2-Name5","Children2-Name6","Children2-Date of Birth","Children2-Date of Death","Children2-sub_group","Children3-ID","Children3-Gender","Children3-Name1","Children3-Name2","Children3-Name3","Children3-Name4","Children3-Name5","Children3-Name6","Children3-Date of Birth","Children3-Date of Death","Children3-sub_group","Children4-ID","Children4-Gender","Children4-Name1","Children4-Name2","Children4-Name3","Children4-Name4","Children4-Name5","Children4-Name6","Children4-Date of Birth","Children4-Date of Death","Children4-sub_group","Children5-ID","Children5-Gender","Children5-Name1","Children5-Name2","Children5-Name3","Children5-Name4","Children5-Name5","Children5-Name6","Children5-Date of Birth","Children5-Date of Death","Children5-sub_group","Children6-ID","Children6-Gender","Children6-Name1","Children6-Name2","Children6-Name3","Children6-Name4","Children6-Name5","Children6-Name6","Children6-Date of Birth","Children6-Date of Death","Children6-sub_group","Children7-ID","Children7-Gender","Children7-Name1","Children7-Name2","Children7-Name3","Children7-Name4","Children7-Name5","Children7-Name6","Children7-Date of Birth","Children7-Date of Death","Children7-sub_group","Children8-ID","Children8-Gender","Children8-Name1","Children8-Name2","Children8-Name3","Children8-Name4","Children8-Name5","Children8-Name6","Children8-Date of Birth","Children8-Date of Death","Children8-sub_group","Children9-ID","Children9-Gender","Children9-Name1","Children9-Name2","Children9-Name3","Children9-Name4","Children9-Name5","Children9-Name6","Children9-Date of Birth","Children9-Date of Death","Children9-sub_group","Children10-ID","Children10-Gender","Children10-Name1","Children10-Name2","Children10-Name3","Children10-Name4","Children10-Name5","Children10-Name6","Children10-Date of Birth","Children10-Date of Death","Children10-sub_group","Children11-ID","Children11-Gender","Children11-Name1","Children11-Name2","Children11-Name3","Children11-Name4","Children11-Name5","Children11-Name6","Children11-Date of Birth","Children11-Date of Death","Children11-sub_group","Children12-ID","Children12-Gender","Children12-Name1","Children12-Name2","Children12-Name3","Children12-Name4","Children12-Name5","Children12-Name6","Children12-Date of Birth","Children12-Date of Death","Children12-sub_group","Children13-ID","Children13-Gender","Children13-Name1","Children13-Name2","Children13-Name3","Children13-Name4","Children13-Name5","Children13-Name6","Children13-Date of Birth","Children13-Date of Death","Children13-sub_group","Children14-ID","Children14-Gender","Children14-Name1","Children14-Name2","Children14-Name3","Children14-Name4","Children14-Name5","Children14-Name6","Children14-Date of Birth","Children14-Date of Death","Children14-sub_group","Children15-ID","Children15-Gender","Children15-Name1","Children15-Name2","Children15-Name3","Children15-Name4","Children15-Name5","Children15-Name6","Children15-Date of Birth","Children15-Date of Death","Children15-sub_group","Children16-ID","Children16-Gender","Children16-Name1","Children16-Name2","Children16-Name3","Children16-Name4","Children16-Name5","Children16-Name6","Children16-Date of Birth","Children16-Date of Death","Children16-sub_group","Children17-ID","Children17-Gender","Children17-Name1","Children17-Name2","Children17-Name3","Children17-Name4","Children17-Name5","Children17-Name6","Children17-Date of Birth","Children17-Date of Death","Children17-sub_group","Children18-ID","Children18-Gender","Children18-Name1","Children18-Name2","Children18-Name3","Children18-Name4","Children18-Name5","Children18-Name6","Children18-Date of Birth","Children18-Date of Death","Children18-sub_group","Children19-ID","Children19-Gender","Children19-Name1","Children19-Name2","Children19-Name3","Children19-Name4","Children19-Name5","Children19-Name6","Children19-Date of Birth","Children19-Date of Death","Children19-sub_group","Children20-ID","Children20-Gender","Children20-Name1","Children20-Name2","Children20-Name3","Children20-Name4","Children20-Name5","Children20-Name6","Children20-Date of Birth","Children20-Date of Death","Children20-sub_group"
//            String headingLine = bufferedReader.readLine();
            try {
                boolean continueReading = true;
                int lineCounter = -1;
                int idColumn = -1;
                ArrayList<Integer> unionColumnIndexes = new ArrayList<Integer>();
                ArrayList<Integer> parentColumnIndexs = new ArrayList<Integer>();
                for (int headingCounter = 0; headingCounter < allHeadings.size(); headingCounter++) {
                    final String headingLowerCase = allHeadings.get(headingCounter).toLowerCase();
                    if (headingLowerCase.equals("id")) {
                        idColumn = headingCounter;
                    }
                    if (Arrays.binarySearch(unionColumns, headingLowerCase) > -1) {
                        unionColumnIndexes.add(headingCounter);
                    }
                    if (Arrays.binarySearch(parentColumns, headingLowerCase) > -1) {
                        parentColumnIndexs.add(headingCounter);
                    }
                }
                boolean firstLineIdZero = true;
                while (continueReading) {
                    lineCounter++;
                    ArrayList<String> lineFields = getFieldsForLineExcludingComments(bufferedReader);
                    continueReading = lineFields.size() > 0;
                    EntityDocument currentEntity = null;
                    EntityDocument relatedEntity = null;
                    String recordID;
                    if (idColumn == -1) {
                        recordID = Integer.toString(lineCounter);
                    } else {
                        if (idColumn < lineFields.size()) {
                            recordID = lineFields.get(idColumn);
                        } else {
                            appendToTaskOutput("Error: No ID value for line " + lineCounter);
                            recordID = "-1";
                        }
                    }
                    if (lineCounter == 0) {
                        // only test the first line for ID zero
                        firstLineIdZero = recordID.equals("0");
                    }
                    currentEntity = getEntityDocument(createdNodes, profileId, recordID, importTranslator);

                    String relatedEntityPrefix = null;
                    for (int fieldCounter = 0; fieldCounter < lineFields.size(); fieldCounter++) {
                        String entityField = lineFields.get(fieldCounter);
//                            String cleanValue = cleanCsvString(entityLineString);
                        String headingString;
                        if (allHeadings.size() > fieldCounter) {
                            headingString = allHeadings.get(fieldCounter);
                        } else {
                            headingString = "-unnamed-field-";
                            appendToTaskOutput("Warning more values than headers, using " + headingString + " for value: " + entityField);
                        }
                        if (idColumn == fieldCounter) {
                            // exclude the ID field from the entity data

                            // if the ID field contains the string ID then replace all the headers with the current lines fields and continue reading the file with the new fields
                            if (entityField.toLowerCase().equals("id")) {
                                unionColumnIndexes.clear();
                                parentColumnIndexs.clear();
                                allHeadings = lineFields;
                                break;
                            }
                        } else if (unionColumnIndexes.contains(fieldCounter)) {
                            if (isAnExcludedId(entityField, firstLineIdZero)) {
                                relatedEntity = getEntityDocument(createdNodes, profileId, entityField, importTranslator);
                                currentEntity.entityData.addRelatedNode(relatedEntity.entityData, RelationType.union, null, null, null, null);
                            }
                        } else if (parentColumnIndexs.contains(fieldCounter)) {
                            if (isAnExcludedId(entityField, firstLineIdZero)) {
                                relatedEntity = getEntityDocument(createdNodes, profileId, entityField, importTranslator);
                                currentEntity.entityData.addRelatedNode(relatedEntity.entityData, RelationType.ancestor, null, null, null, null);
                            }
                        } else { //if (entityField.length() > 0) { // there is no need to exclude empty fields the user might wish to insert data later
                            if (headingString.matches("Spouses[\\d]*-ID")) {
                                if (isAnExcludedId(entityField, firstLineIdZero)) {
                                    relatedEntity = getEntityDocument(createdNodes, profileId, entityField, importTranslator);
                                    currentEntity.entityData.addRelatedNode(relatedEntity.entityData, RelationType.union, null, null, null, null);
                                    relatedEntityPrefix = headingString.substring(0, headingString.length() - "ID".length());
                                }
                            } else if (headingString.matches("Parents[\\d]*-ID")) {
                                if (isAnExcludedId(entityField, firstLineIdZero)) {
                                    relatedEntity = getEntityDocument(createdNodes, profileId, entityField, importTranslator);
                                    currentEntity.entityData.addRelatedNode(relatedEntity.entityData, RelationType.ancestor, null, null, null, null);
                                    relatedEntityPrefix = headingString.substring(0, headingString.length() - "ID".length());
                                }
                            } else if (headingString.matches("Children[\\d]*-ID")) {
                                if (isAnExcludedId(entityField, firstLineIdZero)) {
                                    relatedEntity = getEntityDocument(createdNodes, profileId, entityField, importTranslator);
                                    currentEntity.entityData.addRelatedNode(relatedEntity.entityData, RelationType.descendant, null, null, null, null);
                                    relatedEntityPrefix = headingString.substring(0, headingString.length() - "ID".length());
                                }
                            } else if (relatedEntityPrefix != null && headingString.startsWith(relatedEntityPrefix)) {
                                relatedEntity.insertValue(headingString.substring(relatedEntityPrefix.length()), entityField);
//                                    appendToTaskOutput("Setting value in related entity: " + allHeadings.get(valueCount) + " : " + cleanValue);
//                                    appendToTaskOutput(importTextArea, "Ignoring: " + allHeadings.get(valueCount) + " : " + cleanValue);
                            } else {
                                currentEntity.insertValue(headingString, entityField);
//                                    appendToTaskOutput("Setting value: " + allHeadings.get(valueCount) + " : " + cleanValue);
                            }
                        }
                    }
//                        if (maxImportCount-- < 0) {
//                            appendToTaskOutput(importTextArea, "Aborting import due to max testing limit");
//                            break;
//                        }
                    super.incrementLineProgress();
                }
            } catch (ImportException exception) {
                appendToTaskOutput(exception.getMessage());
            }
            saveAllDocuments();
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            appendToTaskOutput("Error: " + exception.getMessage());
        }
        return createdNodes.toArray(new URI[]{});
    }
}
