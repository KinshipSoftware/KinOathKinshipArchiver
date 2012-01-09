package nl.mpi.kinnate.kindocument;

import java.net.URISyntaxException;
import java.util.ArrayList;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : EntityMerger
 *  Created on : Aug 29, 2011, 3:47:29 PM
 *  Author     : Peter Withers
 */
public class EntityMerger extends DocumentLoader {

    public EntityMerger(SessionStorage sessionStorage, MessageDialogHandler dialogHandler, EntityCollection entityCollection, BugCatcher bugCatcher) {
        super(sessionStorage, dialogHandler, entityCollection, bugCatcher);
    }

    public UniqueIdentifier[] mergeEntities(UniqueIdentifier[] selectedIdentifiers) throws ImportException {
        ArrayList<EntityDocument> nonLeadEntityDocuments = new ArrayList<EntityDocument>();
        try {
            EntityDocument leadEntityDocument = getEntityDocuments(selectedIdentifiers, nonLeadEntityDocuments);
            for (EntityDocument alterEntity : nonLeadEntityDocuments) {
                for (EntityRelation entityRelation : alterEntity.entityData.getAllRelations()) {
                    EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
                    // add the new relation
                    leadEntityDocument.entityData.addRelatedNode(relatedDocument.entityData, entityRelation.relationType, entityRelation.lineColour, entityRelation.labelString, entityRelation.dcrType, entityRelation.customType);
                    // remove the old entity relation
                    // todo: check that the correct relations are being removed from the correct entities.
                    relatedDocument.entityData.removeRelationsWithNode(alterEntity.entityData);
                    alterEntity.entityData.removeRelationsWithNode(relatedDocument.entityData);
                }
                alterEntity.setAsDeletedDocument();
            }
            saveAllDocuments();
        } catch (URISyntaxException exception) {
            bugCatcher.logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return getAffectedIdentifiers();
    }

    public UniqueIdentifier[] duplicateEntities(UniqueIdentifier[] selectedIdentifiers) throws ImportException {
        // todo: complete this duplicate code based on mergeEntities and DocumentLoader
        ArrayList<UniqueIdentifier> addedIdentifiers = new ArrayList<UniqueIdentifier>();
        ArrayList<EntityDocument> entityDocumentList = new ArrayList<EntityDocument>();
        try {
            getEntityDocuments(selectedIdentifiers, entityDocumentList);
            for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
                EntityDocument masterDocument = entityMap.get(uniqueIdentifier);
                EntityDocument duplicateEntityDocument = new EntityDocument(masterDocument, new ImportTranslator(true), sessionStorage);
                addedIdentifiers.add(duplicateEntityDocument.getUniqueIdentifier());
                for (EntityRelation entityRelation : masterDocument.entityData.getAllRelations()) {
                    EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
                    // copy the relations
                    duplicateEntityDocument.entityData.addRelatedNode(relatedDocument.entityData, entityRelation.relationType, entityRelation.lineColour, entityRelation.labelString, entityRelation.dcrType, entityRelation.customType);
                }
                // todo: the date and any other metadata not in the metadata node will be missed by this step, it would be best to move or modify the dates location in the file
                entityMap.put(duplicateEntityDocument.getUniqueIdentifier(), duplicateEntityDocument);
                saveAllDocuments();
            }
            return addedIdentifiers.toArray(new UniqueIdentifier[]{});
        } catch (URISyntaxException exception) {
            bugCatcher.logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
    }
}
