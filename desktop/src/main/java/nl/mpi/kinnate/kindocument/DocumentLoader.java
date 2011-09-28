package nl.mpi.kinnate.kindocument;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : DocumentLoader
 *  Created on : Sep 28, 2011, 2:21:15 PM
 *  Author     : Peter Withers
 */
public class DocumentLoader {

    protected EntityDocument getEntityDocuments(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers, HashMap<UniqueIdentifier, EntityDocument> entityMap, ArrayList<EntityDocument> entityDocumentList) throws ImportException, URISyntaxException {
        EntityDocument leadEntityDocument = null;
        for (EntityData alterEntity : graphPanel.getEntitiesById(selectedIdentifiers).values()) {
            EntityDocument entityDocument = new EntityDocument(new URI(alterEntity.getEntityPath()), new ImportTranslator(true));
            if (leadEntityDocument == null) {
                leadEntityDocument = entityDocument;
            } else {
                entityDocumentList.add(entityDocument);
            }
            entityMap.put(entityDocument.entityData.getUniqueIdentifier(), entityDocument);
        }
        for (EntityDocument alterEntity : entityMap.values().toArray(new EntityDocument[]{})) {
            for (EntityRelation entityRelation : alterEntity.entityData.getDistinctRelateNodes()) {
                EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
                if (relatedDocument == null) {
                    // get the path from the database
                    final URI entityUri = new URI(new EntityCollection().getEntityPath(entityRelation.alterUniqueIdentifier));
                    relatedDocument = new EntityDocument(entityUri, new ImportTranslator(true));
                    entityMap.put(entityRelation.alterUniqueIdentifier, relatedDocument);
//                    entityRelation.setAlterNode(relatedDocument.entityData);
                }
            }
        }
        // set the alter entity for each relation if not already set (based on the known unique identifier)
        for (EntityDocument entityDocument : entityMap.values()) {
            for (EntityRelation nodeRelation : entityDocument.entityData.getRelatedNodesToBeLoaded()) {
                nodeRelation.setAlterNode(entityMap.get(nodeRelation.alterUniqueIdentifier).entityData);
            }
        }
        return leadEntityDocument;
    }
}
