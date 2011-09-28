package nl.mpi.kinnate.kindocument;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : EntityMerger
 *  Created on : Aug 29, 2011, 3:47:29 PM
 *  Author     : Peter Withers
 */
public class EntityMerger extends DocumentLoader{

    public void mergeEntities(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers) throws ImportException {
        ArrayList<EntityDocument> nonLeadEntityDocuments = new ArrayList<EntityDocument>();
        HashMap<UniqueIdentifier, EntityDocument> entityMap = new HashMap<UniqueIdentifier, EntityDocument>();
        try {
            EntityDocument leadEntityDocument = getEntityDocuments(graphPanel, selectedIdentifiers, entityMap, nonLeadEntityDocuments);
            for (EntityDocument alterEntity : nonLeadEntityDocuments) {
                for (EntityRelation entityRelation : alterEntity.entityData.getDistinctRelateNodes()) {
                    EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
                    // add the new relation
                    leadEntityDocument.entityData.addRelatedNode(relatedDocument.entityData, entityRelation.relationType, entityRelation.relationLineType, entityRelation.lineColour, entityRelation.labelString);
                    // remove the old entity relation
                    // todo: check that the correct relations are being removed from the correct entities.
                    relatedDocument.entityData.removeRelationsWithNode(alterEntity.entityData);
                    alterEntity.entityData.removeRelationsWithNode(relatedDocument.entityData);
                }
                alterEntity.setAsDeletedDocument();
            }
            leadEntityDocument.saveDocument();
            new EntityCollection().updateDatabase(leadEntityDocument.getFile().toURI());
            for (EntityDocument entityDocument : nonLeadEntityDocuments) {
                entityDocument.saveDocument();
                new EntityCollection().updateDatabase(entityDocument.getFile().toURI());
            }
        } catch (URISyntaxException exception) {
            new ArbilBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
//        return selectedIdentifiers;
    }

    public UniqueIdentifier[] duplicateEntities(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers) throws ImportException {
        ArrayList<UniqueIdentifier> addedIdentifiers = new ArrayList<UniqueIdentifier>();
        ArrayList<EntityDocument> entityDocumentList = new ArrayList<EntityDocument>();
        HashMap<UniqueIdentifier, EntityDocument> entityMap = new HashMap<UniqueIdentifier, EntityDocument>();
        try {
            getEntityDocuments(graphPanel, selectedIdentifiers, entityMap, entityDocumentList);
            for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
                EntityDocument masterDocument = entityMap.get(uniqueIdentifier);
                EntityDocument duplicateEntityDocument = new EntityDocument(new ImportTranslator(true));
                addedIdentifiers.add(duplicateEntityDocument.getUniqueIdentifier());
                duplicateEntityDocument.createDocument(false);
                for (EntityRelation entityRelation : masterDocument.entityData.getDistinctRelateNodes()) {
                    EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
                    // copy the relations
                    duplicateEntityDocument.entityData.addRelatedNode(relatedDocument.entityData, entityRelation.relationType, entityRelation.relationLineType, entityRelation.lineColour, entityRelation.labelString);
                }
                // todo: the date and any other metadata not in the metadata node will be missed by this step, it would be best to move or modify the dates location in the file
                // copy the metadata
                duplicateEntityDocument.importNode(masterDocument.getMetadataNode());
                duplicateEntityDocument.saveDocument();
                new EntityCollection().updateDatabase(duplicateEntityDocument.getFile().toURI());
            }
            return addedIdentifiers.toArray(new UniqueIdentifier[]{});
        } catch (URISyntaxException exception) {
            new ArbilBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
    }
}
