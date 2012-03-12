package nl.mpi.kinnate.kindocument;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
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

    protected SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler;
    protected HashMap<UniqueIdentifier, EntityDocument> entityMap = new HashMap<UniqueIdentifier, EntityDocument>();
    private EntityCollection entityCollection;

    public DocumentLoader(SessionStorage sessionStorage, MessageDialogHandler dialogHandler, EntityCollection entityCollection) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.entityCollection = entityCollection;
    }

    protected EntityDocument getEntityDocument(UniqueIdentifier selectedIdentifier) throws ImportException, URISyntaxException {
        EntityDocument entityDocument = new EntityDocument(new URI(new EntityCollection(sessionStorage, dialogHandler).getEntityPath(selectedIdentifier)), new ImportTranslator(true), sessionStorage);
//        System.out.println("Loaded 1: " + entityDocument.entityData.getUniqueIdentifier().getAttributeIdentifier());
        entityMap.put(entityDocument.entityData.getUniqueIdentifier(), entityDocument);
        for (EntityRelation entityRelation : entityDocument.entityData.getAllRelations()) {
            EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
            if (relatedDocument == null) {
                // get the path from the database
                final URI entityUri = new URI(new EntityCollection(sessionStorage, dialogHandler).getEntityPath(entityRelation.alterUniqueIdentifier));
                relatedDocument = new EntityDocument(entityUri, new ImportTranslator(true), sessionStorage);
//                System.out.println("Loaded 2: " + entityRelation.alterUniqueIdentifier.getAttributeIdentifier());
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
//            System.out.println("Loaded 3: " + uniqueIdentifier.getAttributeIdentifier());
//            entityMap.put(entityDocument.entityData.getUniqueIdentifier(), entityDocument);
        }
        return leadEntityDocument;
    }

    private void linkLoadedEntities() {
        // set the alter entity for each relation if not already set (based on the known unique identifier)
        for (EntityDocument entityDocument : entityMap.values()) {
            for (EntityRelation nodeRelation : entityDocument.entityData.getRelatedNodesToBeLoaded()) {
                final EntityDocument entityData = entityMap.get(nodeRelation.alterUniqueIdentifier);
                if (entityData != null) {
                    nodeRelation.setAlterNode(entityData.entityData);
                } else {
                    // this appears to be ok if a relation is missing because it is not one of the entites being linked
                    System.out.println("missing: " + nodeRelation.alterUniqueIdentifier.getAttributeIdentifier());
                }
            }
        }
    }

    protected UniqueIdentifier[] getAffectedIdentifiers() {
        return entityMap.keySet().toArray(new UniqueIdentifier[]{});
    }

    protected void saveAllDocuments() throws ImportException {
        for (EntityDocument entityDocument : entityMap.values()) {
            entityDocument.saveDocument();
            entityCollection.updateDatabase(entityDocument.getFile().toURI());
        }
    }
}
