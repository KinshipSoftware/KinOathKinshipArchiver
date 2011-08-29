package nl.mpi.kinnate.kindocument;

import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : EntityMerger
 *  Created on : Aug 29, 2011, 3:47:29 PM
 *  Author     : Peter Withers
 */
public class EntityMerger {
    public void mergeEntities(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers, DataTypes.RelationType relationType) throws ImportException {
        throw new UnsupportedOperationException("todo...");
//        EntityDocument leadEntityDocument = null;
//        ArrayList<EntityDocument> entityDocumentList = new ArrayList<EntityDocument>();
//        try {
//            for (EntityData alterEntity : graphPanel.getEntitiesById(selectedIdentifiers).values()) {
//                EntityDocument entityDocument = new EntityDocument(new URI(alterEntity.getEntityPath()), new ImportTranslator(true));
//                if (leadEntityDocument == null) {
//                    leadEntityDocument = entityDocument;
//                } else {
//                    entityDocumentList.add(entityDocument);
//                }
//            }
//            for (EntityDocument alterEntity : entityDocumentList) {
//                // add the new relation
//                leadEntityDocument.entityData.addRelatedNode(alterEntity.entityData, relationType, DataTypes.RelationLineType.sanguineLine, null, null);
//            }
//            leadEntityDocument.saveDocument();
//            for (EntityDocument entityDocument : entityDocumentList) {
//                entityDocument.saveDocument();
//            }
//        } catch (URISyntaxException exception) {
//            new ArbilBugCatcher().logError(exception);
//            throw new ImportException("Error: " + exception.getMessage());
//        }
    }
}
