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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityDate;
import nl.mpi.kinnate.kindata.EntityDateException;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.projects.ProjectRecord;
import nl.mpi.kinnate.ui.entityprofiles.ProfileRecord;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : GedcomImporter Created on : Aug 24, 2010, 2:40:21 PM
 *
 * @author Peter Withers
 */
public class GedcomImporter extends EntityImporter implements GenericImporter {

    public GedcomImporter(JProgressBar progressBarLocal, JTextArea importTextAreaLocal, boolean overwriteExistingLocal, SessionStorage sessionStorage, ProjectRecord projectRecord) {
        super(progressBarLocal, importTextAreaLocal, overwriteExistingLocal, sessionStorage, projectRecord);
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

    class FamGroupElement {

        public FamGroupElement(String typeString, EntityDocument famEntity, EntityDocument memberEntity) {
            this.typeString = typeString;
            this.famEntity = famEntity;
            this.memberEntity = memberEntity;
        }
        final String typeString;
        final EntityDocument famEntity;
        final EntityDocument memberEntity;
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
    public UniqueIdentifier[] importFile(InputStreamReader inputStreamReader, String profileId) throws IOException, ImportException {
        HashSet<UniqueIdentifier> createdNodes = new HashSet<UniqueIdentifier>();
        HashMap<UniqueIdentifier, ArrayList<SocialMemberElement>> socialGroupRoleMap = new HashMap<UniqueIdentifier, ArrayList<SocialMemberElement>>(); // GroupID: @XX@, RoleType: WIFE HUSB CHIL, EntityData
        ArrayList<FamGroupElement> famGroupList = new ArrayList<FamGroupElement>();
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        ImportTranslator importTranslator = getImportTranslator();

        String strLine;
        ArrayList<String> gedcomLevelStrings = new ArrayList<String>();
        ArrayList<EntityDocument> documentsToDeleteIfNoFieldsAdded = new ArrayList<EntityDocument>();
        EntityDocument currentEntity = null;
        EntityDocument fileHeaderEntity = null;
        boolean skipFileEntity = false;
        String currentEntityType = "";
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
                    currentEntity.appendValueToLast(currentEntityType, "\n" + lineStructure.getEscapedLineContents());
                    lastFieldContinued = true;
                } else if (lineStructure.isContinueLine()) {
                    // todo: if the previous field is null this should be caught and handled as an error in the source file
                    currentEntity.appendValueToLast(currentEntityType, lineStructure.getEscapedLineContents());
                    lastFieldContinued = true;
                }
                if (lastFieldContinued == false) {
                    if (lineStructure.getGedcomLevel() == 0) {
                        if (lineStructure.isEndOfFileMarker()) {
                            appendToTaskOutput("End of file found");
                        } else {
                            // set the profile from the entity type
                            // todo: this profile setting is failing because entities are created via links from other entities without the required information 
                            String typeString = lineStructure.getProfileForEntityType(profileId, ProfileRecord.getDefaultImportProfile().profileId);
                            currentEntity = getEntityDocument(createdNodes, typeString, lineStructure.getCurrentID(), importTranslator);
                            currentEntityType = lineStructure.entityType;
                            if (lineStructure.isFileHeader()) {
                                fileHeaderEntity = currentEntity;
                                // because the schema specifies 1:1 of both head and entity we find rather than create the head and entity nodes
//                                appendToTaskOutput("Reading Gedcom Header");
                                // todo: maybe replace this "Gedcom Header" string with the file name of the import file
                                currentEntity.insertValue("Type", "Imported File Header"); // inserting a value will only add that value once, if the value already exists then no action is taken
                                if (lineStructure.hasLineContents()) {
//                                    throw new ImportException("Unexpeted header parameter: " + lineStructure.getCurrentName() + " " + lineStructure.getLineContents());
                                    // TIP files provide comments in the header and they are adde here
                                    currentEntity.insertValue(lineStructure.getCurrentName(), lineStructure.getLineContents().trim());
                                } else {
//                                    currentEntity.appendValue(lineStructure.getCurrentName(), null, lineStructure.getGedcomLevel());
                                }
                            } else {
                                fileHeaderEntity.entityData.addRelatedNode(currentEntity.entityData, RelationType.other, null, null, null, "source");
                                if (lineStructure.getEntityType() != null) {
                                    currentEntity.insertValue("Type", lineStructure.getEntityType());
                                }
                                if (lineStructure.getDeleteIfNoFeildsAdded()) {
                                    documentsToDeleteIfNoFieldsAdded.add(currentEntity);
                                }
                                if (lineStructure.hasLineContents()) {
                                    currentEntity.insertValue(lineStructure.getCurrentName(), lineStructure.getLineContents());
                                }
                            }
                        }
                        while (lineStructure.hasCurrentField()) {
                            if (lineStructure.hasLineContents()) {
                                currentEntity.insertValue(lineStructure.getCurrentName(), lineStructure.getLineContents());
                            }
                            lineStructure.moveToNextField();
                        } // end skip overwrite
                    } else {
                        while (lineStructure.hasCurrentField()) {
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
                                                appendToTaskOutput("The date data will been imported anyway and can be corrected manually later.");
                                            } catch (EntityDateException exception) {
                                                System.out.println(exception.getMessage());
                                                appendToTaskOutput("Failed to parse date: " + strLine + " " + exception.getMessage());
                                                appendToTaskOutput("The date data will been imported anyway and can be corrected manually later.");
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
                                            currentEntity.entityData.addExternalLink(resolvedUri, null);
                                            notConsumed = false;
                                        } catch (java.lang.IllegalArgumentException exception) {
                                            appendToTaskOutput("Unsupported File Path: " + lineStructure.getLineContents());
                                        }
                                    }
                                }
                                // create the socal link node when required from gedcom file format
                                if (lineStructure.isRelation()) {
                                    // todo: move this into the gedcom line structure class and use lineStructure.getRelationList() to later retrieve the list
//                                appendToTaskOutput("--> adding social relation");
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
                                        // store all the sanguine relations to link later
                                        socialGroupRoleMap.get(socialGroupIdentifier).add(new SocialMemberElement(lineStructure.getCurrentName(), socialGroupMember));
                                        // the fam relations to consist of associations with implied sanuine links to the related entities, these sangine relations are handled later when all members are known
                                        // store all the family object relations to add later or not depending on adequate fields being added
                                        famGroupList.add(new FamGroupElement(lineStructure.getCurrentName(), currentEntity, getEntityDocument(createdNodes, profileId, lineStructure.getLineContents(), importTranslator)));
                                        notConsumed = false;
                                    } else {
                                        // todo: check this change
                                        // capture the custom types from .kinoath format export files
                                        String currentName = lineStructure.getCurrentName();
                                        String customType = null;
                                        String dcrString = null;
                                        RelationType targetRelation = RelationType.other;
                                        String[] currentNameParts = currentName.split(":");
                                        if (currentNameParts.length > 0) {
                                            try {
                                                targetRelation = RelationType.valueOf(currentNameParts[0]);
                                            } catch (IllegalArgumentException exception) {
                                                if (currentNameParts.length == 3) {
                                                    // if there are three parts then the file should be an export from kinoath, in which case we presumably should support all types
                                                    appendToTaskOutput("Unsupported Relation Type: " + currentName);
                                                }
                                                targetRelation = RelationType.other;
                                                customType = currentNameParts[0];
                                            }
                                        }
                                        if (currentNameParts.length == 3) {
                                            customType = currentNameParts[1];
                                            dcrString = currentNameParts[2];
                                        }
                                        currentEntity.entityData.addRelatedNode(getEntityDocument(createdNodes, profileId, lineStructure.getLineContents(), importTranslator).entityData, targetRelation, null, null, dcrString, customType);
                                        notConsumed = false;
                                    }
                                }
                                if (notConsumed) {
                                    // any unprocessed elements should now be added as they are into the metadata
                                    // any gedcom chars should be escaped such as @@ etc.
                                    currentEntity.appendValue(lineStructure.getCurrentName(), lineStructure.getEscapedLineContents(), lineStructure.getGedcomLevel());
                                }
                            }
                            lineStructure.moveToNextField();
                        }
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
        int omittedFamGroupCount = 0;
        for (FamGroupElement famGroupElement : famGroupList) {
            EntityDocument famGroup = null;
            if (documentsToDeleteIfNoFieldsAdded.contains(famGroupElement.famEntity)) {
                famGroup = famGroupElement.famEntity;
            } else if (documentsToDeleteIfNoFieldsAdded.contains(famGroupElement.memberEntity)) {
                famGroup = famGroupElement.memberEntity;
            }
            if (famGroup == null || famGroup.getAddedFieldCount() > 1) {
                // keep the fam groups that have information in them
                famGroupElement.famEntity.entityData.addRelatedNode(famGroupElement.memberEntity.entityData, RelationType.other, null, null, null, famGroupElement.typeString);
            } else {
                // discard any family group that does not contain any fields
                fileHeaderEntity.entityData.removeRelationsWithNode(famGroup.entityData);
                famGroup.entityData.removeRelationsWithNode(fileHeaderEntity.entityData);
                deleteEntityDocument(famGroup);
                createdNodes.remove(famGroup.getUniqueIdentifier());
                omittedFamGroupCount++;
            }
        }
        appendToTaskOutput("Omitted " + omittedFamGroupCount + " FAM groups that contained no field data (all relations have been preserved)");

//        for (EntityDocument deleteableDocument : documentsToDeleteIfNoFieldsAdded) {
//            // delete any documents (fam groups) that are flagged and do not have any fields added
//            if (deleteableDocument.getAddedFieldCounter() < 1) {
//                for (EntityRelation entityRelation : deleteableDocument.entityData.getAllRelations()) {
//
////                        protected HashMap<String, HashSet<UniqueIdentifier>> createdNodeIds;
////    HashMap<String, EntityDocument> createdDocuments = new HashMap<String, EntityDocument>();
//
//
//                    EntityDocument relatedDocument = createdDocuments.get(createdNodeIds.(entityRelation.alterUniqueIdentifier));
////                    if (relatedDocument == null) {
//                    // remove the relation
//                    deleteableDocument.entityData.removeRelationsWithNode(relatedDocument.entityData);
//                    relatedDocument.entityData.removeRelationsWithNode(deleteableDocument.entityData);
////                    }
//                }
//            }
//        }
        saveAllDocuments();
        return createdNodes.toArray(new UniqueIdentifier[]{});
    }
}
