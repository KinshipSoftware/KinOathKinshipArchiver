package nl.mpi.kinnate.kindocument;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.arbil.util.ArbilBugCatcher;
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
public class EntityMerger {

    public UniqueIdentifier[] mergeEntities(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers) throws ImportException {
        EntityDocument leadEntityDocument = null;
        ArrayList<EntityDocument> entityDocumentList = new ArrayList<EntityDocument>();
        HashMap<UniqueIdentifier, EntityDocument> entityMap = new HashMap<UniqueIdentifier, EntityDocument>();
        try {
            for (EntityData alterEntity : graphPanel.getEntitiesById(selectedIdentifiers).values()) {
                EntityDocument entityDocument = new EntityDocument(new URI(alterEntity.getEntityPath()), new ImportTranslator(true));
                if (leadEntityDocument == null) {
                    leadEntityDocument = entityDocument;
                } else {
                    entityDocumentList.add(entityDocument);
                }
                entityMap.put(entityDocument.entityData.getUniqueIdentifier(), entityDocument);
            }
            for (EntityDocument alterEntity : entityDocumentList) {
                for (EntityRelation entityRelation : alterEntity.entityData.getDistinctRelateNodes()) {
                    // add the new relation
                    leadEntityDocument.entityData.addRelatedNode(entityRelation.getAlterNode(), entityRelation.relationType, entityRelation.relationLineType, entityRelation.lineColour, entityRelation.labelString);
                    // remove the old entity relation
                    EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
                    if (relatedDocument == null) {
                        // todo: this might need to make sure the relate entity is loaded or just get the path from the database
                        relatedDocument = new EntityDocument(new URI(entityRelation.getAlterNode().getEntityPath()), new ImportTranslator(true));
                        entityMap.put(relatedDocument.entityData.getUniqueIdentifier(), relatedDocument);
                    }
                    relatedDocument.entityData.removeRelationsWithNode(alterEntity.entityData);
                }
                alterEntity.setAsDeleteDocument();
            }
            leadEntityDocument.saveDocument();
            for (EntityDocument entityDocument : entityDocumentList) {
                entityDocument.saveDocument();
            }
        } catch (URISyntaxException exception) {
            new ArbilBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return selectedIdentifiers;
    }

    public UniqueIdentifier[] duplicateEntities(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers) throws ImportException {
        return selectedIdentifiers;
    }
}
