/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.kindocument;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.arbil.data.ArbilDataNodeService;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Created on : Sep 28, 2011, 2:21:15 PM
 *
 * @author Peter Withers
 */
public class DocumentLoader {

    final protected SessionStorage sessionStorage;
    private MessageDialogHandler dialogHandler;
    protected HashMap<UniqueIdentifier, EntityDocument> entityMap = new HashMap<UniqueIdentifier, EntityDocument>();
    private EntityCollection entityCollection;
    final long startTime;

    public DocumentLoader(SessionStorage sessionStorage, MessageDialogHandler dialogHandler, EntityCollection entityCollection) {
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.entityCollection = entityCollection;
        startTime = System.currentTimeMillis();
    }

    protected EntityDocument getEntityDocument(UniqueIdentifier selectedIdentifier) throws ImportException, URISyntaxException, EntityServiceException {
        EntityDocument entityDocument = new EntityDocument(selectedIdentifier.getFileInProject(sessionStorage).toURI(), new ImportTranslator(true), sessionStorage);
//        System.out.println("Loaded 1: " + entityDocument.entityData.getUniqueIdentifier().getAttributeIdentifier());
        entityMap.put(entityDocument.entityData.getUniqueIdentifier(), entityDocument);
        for (EntityRelation entityRelation : entityDocument.entityData.getAllRelations()) {
            EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
            if (relatedDocument == null) {
                // get the path from the database
                final URI entityUri = entityRelation.alterUniqueIdentifier.getFileInProject(sessionStorage).toURI();
                relatedDocument = new EntityDocument(entityUri, new ImportTranslator(true), sessionStorage);
//                System.out.println("Loaded 2: " + entityRelation.alterUniqueIdentifier.getAttributeIdentifier());
                entityMap.put(entityRelation.alterUniqueIdentifier, relatedDocument);
            }
        }
        linkLoadedEntities();
        return entityDocument;
    }

    protected EntityDocument getEntityDocuments(UniqueIdentifier[] selectedIdentifiers, ArrayList<EntityDocument> entityDocumentList) throws ImportException, URISyntaxException, EntityServiceException {
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
        long lastTime = System.currentTimeMillis();
        for (EntityDocument entityDocument : entityMap.values()) {
            entityDocument.saveDocument();
            System.out.println("Saved file in " + (System.currentTimeMillis() - lastTime) + "ms");
            System.out.println("Total time " + (System.currentTimeMillis() - startTime) + "ms");
            lastTime = System.currentTimeMillis();
        }
        try {
            entityCollection.updateDatabase(entityMap.keySet().toArray(new UniqueIdentifier[0]), null);
            System.out.println("Updated DB in " + (System.currentTimeMillis() - lastTime) + "ms");
            System.out.println("Total time " + (System.currentTimeMillis() - startTime) + "ms");
        } catch (EntityServiceException exception) {
            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Update Database");
        }
    }

    protected void deleteFromDataBase(EntityDocument entityDocument) throws IOException {
        ArbilDataNodeService arbilDataNodeService = new ArbilDataNodeService(null, null, null, null, null);
        arbilDataNodeService.bumpHistory(entityDocument.getFile());
        try {
            entityCollection.deleteFromDatabase(entityDocument.getUniqueIdentifier());
        } catch (EntityServiceException exception) {
            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Update Database");
        }
    }
}
