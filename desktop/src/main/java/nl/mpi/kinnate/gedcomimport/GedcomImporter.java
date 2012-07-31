package nl.mpi.kinnate.gedcomimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityDate;
import nl.mpi.kinnate.kindata.EntityDateException;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : GedcomImporter
 * Created on : Aug 24, 2010, 2:40:21 PM
 * Author : Peter Withers
 */
public class GedcomImporter extends EntityImporter implements GenericImporter {

    public GedcomImporter(JProgressBar progressBarLocal, JTextArea importTextAreaLocal, boolean overwriteExistingLocal, SessionStorage sessionStorage) {
        super(progressBarLocal, importTextAreaLocal, overwriteExistingLocal, sessionStorage);
    }

    @Override
    public boolean canImport(String inputFileString) {
        return (inputFileString.toLowerCase().endsWith(".ged") || inputFileString.toLowerCase().endsWith(".gedcom"));
    }

    class SocialMemberElement {

        public SocialMemberElement(String typeString, EntityData memberEntity) {
            this.typeString = typeString;
            this.memberEntity = memberEntity;
        }
        String typeString;
        EntityData memberEntity;
    }

    protected ImportTranslator getImportTranslator() {
        ImportTranslator importTranslator = new ImportTranslator(true);
        // todo: add the translator values if required
        importTranslator.addTranslationEntry("SEX", "F", "Gender", "Female");
        importTranslator.addTranslationEntry("SEX", "M", "Gender", "Male");
        importTranslator.addTranslationEntry("NAME", null, "Name", null);
        importTranslator.addTranslationEntry("chro", null, "Chromosome", null);
        return importTranslator;
    }

    protected ImportLineStructure getImportLineStructure(String lineString, ArrayList<String> gedcomLevelStrings) throws ImportException {
        return new GedcomLineStructure(lineString, gedcomLevelStrings);
    }

    @Override
    public URI[] importFile(InputStreamReader inputStreamReader, String profileId) throws IOException, ImportException {
        ArrayList<URI> createdNodes = new ArrayList<URI>();
        HashMap<UniqueIdentifier, ArrayList<SocialMemberElement>> socialGroupRoleMap = new HashMap<UniqueIdentifier, ArrayList<SocialMemberElement>>(); // GroupID: @XX@, RoleType: WIFE HUSB CHIL, EntityData
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        ImportTranslator importTranslator = getImportTranslator();

        String strLine;
        ArrayList<String> gedcomLevelStrings = new ArrayList<String>();
        EntityDocument currentEntity = null;
        boolean skipFileEntity = false;
        while ((strLine = bufferedReader.readLine()) != null) {
            if (skipFileEntity) {
                skipFileEntity = false;
                while ((strLine = bufferedReader.readLine()) != null) {
                    if (strLine.startsWith("0")) {
                        break;
                    }
                }
            }
            ImportLineStructure lineStructure = getImportLineStructure(strLine, gedcomLevelStrings);
            if (lineStructure.isIncompleteLine()) {
                appendToTaskOutput("Incomplete line found");
            } else {
                System.out.println(strLine);
//                System.out.println("gedcomLevelString: " + gedcomLevelStrings);
//                appendToTaskOutput(importTextArea, strLine);
                boolean lastFieldContinued = false;
                if (lineStructure.isContinueLineBreak()) {
                    // todo: if the previous field is null this should be caught and handled as an error in the source file                
                    currentEntity.appendValueToLast("\n" + lineStructure.getLineContents());
                    lastFieldContinued = true;
                } else if (lineStructure.isContinueLine()) {
                    // todo: if the previous field is null this should be caught and handled as an error in the source file
                    currentEntity.appendValueToLast(lineStructure.getLineContents());
                    lastFieldContinued = true;
                }
                if (lastFieldContinued == false) {
                    while (lineStructure.hasCurrentField()) {
                        if (lineStructure.getGedcomLevel() == 0) {
                            if (lineStructure.isEndOfFileMarker()) {
                                appendToTaskOutput("End of file found");
                            } else {
//                        String gedcomXsdLocation = "/xsd/gedcom-import.xsd";
//                            String gedcomXsdLocation = "/xsd/gedcom-autogenerated.xsd";
                                String typeString;
                                if (lineStructure.hasLineContents()) {
                                    typeString = profileId; //   lineParts[2];
                                } else {
                                    typeString = profileId; //   lineParts[1];
                                }
                                // todo: the type string needs to determine if this is an entity or a metadata file
                                currentEntity = getEntityDocument(createdNodes, typeString, lineStructure.getCurrentID(), importTranslator);
                                if (lineStructure.isFileHeader()) {
                                    // because the schema specifies 1:1 of both head and entity we find rather than create the head and entity nodes
//                                appendToTaskOutput("Reading Gedcom Header");
                                    // todo: maybe replace this "Gedcom Header" string with the file name of the import file
                                    currentEntity.insertValue("Type", "Imported File Header"); // inserting a value will only add that value once, if the value already exists then no action is taken
                                    if (lineStructure.hasLineContents()) {
                                        currentEntity.insertValue(lineStructure.getCurrentName(), lineStructure.getLineContents());
                                    } else {
                                        currentEntity.appendValue(lineStructure.getCurrentName(), null, lineStructure.getGedcomLevel());
                                    }
                                } else {
                                    if (lineStructure.getEntityType() != null) {
                                        currentEntity.insertValue("Type", lineStructure.getEntityType());
                                    }
                                    if (lineStructure.hasLineContents()) {
                                        currentEntity.insertValue(lineStructure.getCurrentName(), lineStructure.getLineContents());
                                    }
                                }
                            } // end skip overwrite
                        } else {
                            // if the current line has a value then enter it into the node
                            if (!lineStructure.hasLineContents()) {
                                currentEntity.appendValue(lineStructure.getCurrentName(), null, lineStructure.getGedcomLevel());
                            } else {
                                boolean notConsumed = true;
                                if (gedcomLevelStrings.size() == 3) {
                                    if (gedcomLevelStrings.get(2).equals("DATE")) {
                                        if (gedcomLevelStrings.get(1).equals("BIRT") || gedcomLevelStrings.get(1).equals("DEAT")) {
                                            String dateText = lineStructure.getLineContents().trim();
                                            String qualifierString = null;
                                            String yearString = null;
                                            String monthString = null;
                                            String dayString = null;
                                            for (String prefixString : new String[]{"ABT", "BEF", "AFT"}) {
                                                if (dateText.startsWith(prefixString)) {
                                                    qualifierString = prefixString.toLowerCase();
//                                                appendToTaskOutput("Unsupported Date Type: " + dateText);
                                                    dateText = dateText.substring(prefixString.length()).trim();
                                                }
                                            }
                                            SimpleDateFormat formatter;
                                            try {
                                                if (dateText.matches("[0-9]{1,4}")) {
                                                    while (dateText.length() < 4) {
                                                        // make sure that 812 has four digits like 0812
                                                        dateText = "0" + dateText;
                                                    }
                                                    yearString = dateText;
//                                            formatter = new SimpleDateFormat("yyyy");
                                                } else if (dateText.matches("[a-zA-Z]{3} [0-9]{4}")) {
                                                    formatter = new SimpleDateFormat("MMM yyyy");
                                                    Date parsedDate = formatter.parse(dateText);
                                                    monthString = new SimpleDateFormat("MM").format(parsedDate);
                                                    yearString = new SimpleDateFormat("yyyy").format(parsedDate);
                                                } else {
                                                    formatter = new SimpleDateFormat("dd MMM yyyy");
                                                    Date parsedDate = formatter.parse(dateText);
                                                    dayString = new SimpleDateFormat("dd").format(parsedDate);
                                                    monthString = new SimpleDateFormat("MM").format(parsedDate);
                                                    yearString = new SimpleDateFormat("yyyy").format(parsedDate);
                                                }
                                                EntityDate entityDate = new EntityDate(yearString, monthString, dayString, qualifierString);
                                                if (gedcomLevelStrings.get(1).equals("BIRT")) {
                                                    currentEntity.insertValue("DateOfBirth", entityDate.getDateString());
                                                } else {
                                                    currentEntity.insertValue("DateOfDeath", entityDate.getDateString());
                                                }
                                                notConsumed = false;
                                            } catch (ParseException exception) {
                                                System.out.println(exception.getMessage());
                                                appendToTaskOutput("Failed to parse date: " + strLine);
                                            } catch (EntityDateException exception) {
                                                System.out.println(exception.getMessage());
                                                appendToTaskOutput("Failed to parse date: " + strLine + " " + exception.getMessage());
                                            }
                                        }
                                    }
                                }
                                if (gedcomLevelStrings.size() == 2) {
                                    if (gedcomLevelStrings.get(1).equals("SEX") || gedcomLevelStrings.get(1).equals("NAME")) {
                                        if (lineStructure.getGedcomLevel() == 1) {
                                            currentEntity.insertValue(lineStructure.getCurrentName(), lineStructure.getLineContents());
                                        } else {
                                            currentEntity.appendValue(lineStructure.getCurrentName(), lineStructure.getLineContents(), lineStructure.getGedcomLevel());
                                        }
                                        notConsumed = false;
                                    }
                                }
                                if (gedcomLevelStrings.size() == 2) {
                                    if (gedcomLevelStrings.get(1).equals("chro")) {
                                        if (lineStructure.getGedcomLevel() == 1) {
                                            currentEntity.insertValue(lineStructure.getCurrentName(), lineStructure.getLineContents());
                                            notConsumed = false;
                                        }
                                    }
                                }
                                if (gedcomLevelStrings.get(gedcomLevelStrings.size() - 1).equals("FILE")) {
                                    // todo: check if the FILE value can contain a path or just the file name and handle the path correctly if required
                                    // todo: copy the file or not according to user options
                                    if (lineStructure.getLineContents().toLowerCase().startsWith("mailto:")) {
                                        currentEntity.insertValue("mailto", lineStructure.getLineContents()); // todo: check that this is not already inserted
                                    } else {
                                        try {
                                            URI resolvedUri;
                                            if ("jar".equals(inputFileUri.getScheme())) { // "jar:file:"
                                                // when the application is running from a jar file the uri resolve fails as designed by Sun, also we do not include the media files in the jar, so for sample files we must replace the uri with the documentation uri example.net
                                                resolvedUri = URI.create("http://example.net/example/files/not/included/demo").resolve(lineStructure.getLineContents());
                                            } else {
                                                resolvedUri = inputFileUri.resolve(lineStructure.getLineContents());
                                            }
                                            currentEntity.entityData.addArchiveLink(resolvedUri);
                                            notConsumed = false;
                                        } catch (java.lang.IllegalArgumentException exception) {
                                            appendToTaskOutput("Unsupported File Path: " + lineStructure.getLineContents());
                                        }
                                    }
                                }
                                // create the link node when required
                                if (lineStructure.isRelation()) {
                                    // todo: move this into the gedcom line structure class and use lineStructure.getRelationList() to later retrieve the list
//                                appendToTaskOutput("--> adding social relation");
                                    RelationType targetRelation = RelationType.other;
                                    // here the following five relation types are mapped to the correct relation types after this the association is cretaed and later the indigiduals are linked with sanguine relations
                                    if (lineStructure.getCurrentName().equals("FAMS") || lineStructure.getCurrentName().equals("FAMC") || lineStructure.getCurrentName().equals("HUSB") || lineStructure.getCurrentName().equals("WIFE") || lineStructure.getCurrentName().equals("CHIL")) {
                                        UniqueIdentifier socialGroupIdentifier;
                                        EntityData socialGroupMember;
                                        if (lineStructure.getCurrentName().equals("FAMS") || lineStructure.getCurrentName().equals("FAMC")) {
                                            socialGroupIdentifier = getEntityDocument(createdNodes, profileId, lineStructure.getLineContents(), importTranslator).entityData.getUniqueIdentifier();
                                            socialGroupMember = currentEntity.entityData;
                                        } else {
                                            socialGroupIdentifier = currentEntity.entityData.getUniqueIdentifier();
                                            socialGroupMember = getEntityDocument(createdNodes, profileId, lineStructure.getLineContents(), importTranslator).entityData;
                                        }
                                        if (!socialGroupRoleMap.containsKey(socialGroupIdentifier)) {
                                            socialGroupRoleMap.put(socialGroupIdentifier, new ArrayList<SocialMemberElement>());
                                        }
                                        socialGroupRoleMap.get(socialGroupIdentifier).add(new SocialMemberElement(lineStructure.getCurrentName(), socialGroupMember));
                                    }
                                    // the fam relations to consist of associations with implied sanuine links to the related entities, these sangine relations are handled later when all members are known
                                    currentEntity.entityData.addRelatedNode(getEntityDocument(createdNodes, profileId, lineStructure.getLineContents(), importTranslator).entityData, targetRelation, null, null, null, lineStructure.getCurrentName());
                                    notConsumed = false;
                                }
                                if (notConsumed) {
                                    // any unprocessed elements should now be added as they are into the metadata
                                    currentEntity.appendValue(lineStructure.getCurrentName(), lineStructure.getLineContents(), lineStructure.getGedcomLevel());
                                }
                            }
                        }
                        lineStructure.moveToNextField();
                    }
                    for (ImportLineStructure.RelationEntry relationEntry : lineStructure.getRelationList()) {
                        getEntityDocument(createdNodes, profileId, relationEntry.egoIdString, importTranslator).entityData.addRelatedNode(getEntityDocument(createdNodes, profileId, relationEntry.alterIdString, importTranslator).entityData, relationEntry.relationType, null, null, null, relationEntry.customType);
                    }
                }
                super.incrementLineProgress();
            }
        }
        for (ArrayList<SocialMemberElement> currentSocialGroup : socialGroupRoleMap.values()) {
            for (SocialMemberElement outerMemberElement : currentSocialGroup) {
                for (SocialMemberElement innerMemberElement : currentSocialGroup) {
                    if (!innerMemberElement.memberEntity.equals(outerMemberElement.memberEntity)) {
                        if (innerMemberElement.typeString.equals("FAMC") || innerMemberElement.typeString.equals("CHIL")) {
                            if (outerMemberElement.typeString.equals("FAMC") || outerMemberElement.typeString.equals("CHIL")) {
                                innerMemberElement.memberEntity.addRelatedNode(outerMemberElement.memberEntity, RelationType.sibling, null, null, null, null);
                            } else {
                                innerMemberElement.memberEntity.addRelatedNode(outerMemberElement.memberEntity, RelationType.ancestor, null, null, null, null);
                            }
                        } else {
                            if (outerMemberElement.typeString.equals("FAMC") || outerMemberElement.typeString.equals("CHIL")) {
                                innerMemberElement.memberEntity.addRelatedNode(outerMemberElement.memberEntity, RelationType.descendant, null, null, null, null);
                            } else {
                                innerMemberElement.memberEntity.addRelatedNode(outerMemberElement.memberEntity, RelationType.union, null, null, null, null);
                            }
                        }
//                            appendToTaskOutput("--> adding sanguine relation");
                    }
                }
            }
        }
        // add the header to all entities
        saveAllDocuments();
        return createdNodes.toArray(new URI[]{});
    }
}
