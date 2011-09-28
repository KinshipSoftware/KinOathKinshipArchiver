package nl.mpi.kinnate.kindocument;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : DocumentLoader
 *  Created on : Sep 28, 2011, 2:21:15 PM
 *  Author     : Peter Withers
 */
public class DocumentLoader {

    HashMap<UniqueIdentifier, EntityDocument> entityMap = new HashMap<UniqueIdentifier, EntityDocument>();

    protected EntityDocument getEntityDocument(UniqueIdentifier selectedIdentifier) throws ImportException, URISyntaxException {
        EntityDocument entityDocument = new EntityDocument(new URI(new EntityCollection().getEntityPath(selectedIdentifier)), new ImportTranslator(true));
        entityMap.put(entityDocument.entityData.getUniqueIdentifier(), entityDocument);
        for (EntityRelation entityRelation : entityDocument.entityData.getDistinctRelateNodes()) {
            EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
            if (relatedDocument == null) {
                // get the path from the database
                final URI entityUri = new URI(new EntityCollection().getEntityPath(entityRelation.alterUniqueIdentifier));
                relatedDocument = new EntityDocument(entityUri, new ImportTranslator(true));
                entityMap.put(entityRelation.alterUniqueIdentifier, relatedDocument);
            }
        }
        linkLoadedEntities();
        return entityDocument;
    }

    protected EntityDocument getEntityDocuments(UniqueIdentifier[] selectedIdentifiers, ArrayList<EntityDocument> entityDocumentList) throws ImportException, URISyntaxException {
        EntityDocument leadEntityDocument = null;
        for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
            EntityDocument entityDocument = getEntityDocument(uniqueIdentifier);
            if (leadEntityDocument == null) {
                leadEntityDocument = entityDocument;
            } else {
                entityDocumentList.add(entityDocument);
            }
            entityMap.put(entityDocument.entityData.getUniqueIdentifier(), entityDocument);
        }
        return leadEntityDocument;
    }

    private void linkLoadedEntities() {
        // set the alter entity for each relation if not already set (based on the known unique identifier)
        for (EntityDocument entityDocument : entityMap.values()) {
            for (EntityRelation nodeRelation : entityDocument.entityData.getRelatedNodesToBeLoaded()) {
                nodeRelation.setAlterNode(entityMap.get(nodeRelation.alterUniqueIdentifier).entityData);
            }
        }
    }

    protected UniqueIdentifier[] getAffectedIdentifiers() {
        return entityMap.keySet().toArray(new UniqueIdentifier[]{});
    }

    protected void saveAllDocuments() throws ImportException {
        for (EntityDocument entityDocument : entityMap.values()) {
            entityDocument.saveDocument();
            new EntityCollection().updateDatabase(entityDocument.getFile().toURI());
        }
    }
}
