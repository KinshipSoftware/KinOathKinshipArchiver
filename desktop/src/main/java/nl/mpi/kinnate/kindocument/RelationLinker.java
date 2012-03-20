package nl.mpi.kinnate.kindocument;

import java.net.URISyntaxException;
import java.util.ArrayList;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.Document;

/**
 *  Document   : RelationLinker
 *  Created on : Apr 12, 2011, 1:45:01 PM
 *  Author     : Peter Withers
 */
public class RelationLinker extends DocumentLoader {

    public RelationLinker(SessionStorage sessionStorage, MessageDialogHandler dialogHandler, EntityCollection entityCollection) {
        super(sessionStorage, dialogHandler, entityCollection);
    }

    private void removeMatchingRelations(Document entityDocument, UniqueIdentifier[] selectedIdentifiers) {
        // todo: complete the relation removal code
        // todo: this should use the EntityData object to remove the relaitons and then save via jaxb as is done in linkEntities other wise if a user removes a sibling relation and the common parent relation persists then the data will be in a broken state
//        ArrayList<String> identifierList = new ArrayList<String>();
//        for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
//            identifierList.add(uniqueIdentifier.getQueryIdentifier());
//        }
//        Element roodNode = entityDocument.getDocumentElement();
//        NodeList entityNodeList = roodNode.getElementsByTagNameNS("http://mpi.nl/tla/kin", "Entity");
//        Element entityNode = ((Element) entityNodeList.item(0));
//        NodeList relationsNodeList = entityNode.getElementsByTagNameNS("http://mpi.nl/tla/kin", "Relations");
//        Node relationsGroupNode = relationsNodeList.item(0);
//        if (relationsGroupNode != null) {
//            for (Node relationNode = relationsGroupNode.getFirstChild(); relationNode != null; relationNode = relationNode.getNextSibling()) {
//                if ("Relation".equals(relationNode.getLocalName())) {
//                    if (identifierList.contains(relationNode.getFirstChild().getNodeValue())) {
//                        // remove the matching relations
//                        relationsGroupNode.removeChild(relationNode);
//                    }
//                }
//            }
//        }
//        try {
//            NodeList relationIdentifierNodeList = org.apache.xpath.XPathAPI.selectNodeList(entityDocument, "/Kinnate/kin:Entity/kin:Relations/kin:Relation/kin:Identifier", roodNode);
//            for (int nodeCounter = 0; nodeCounter < relationIdentifierNodeList.getLength(); nodeCounter++) {
//                Node identifierNode = relationIdentifierNodeList.item(nodeCounter);
//                if (identifierNode != null) {
//                    System.out.println(identifierNode.getLocalName() + " : " + identifierNode.getTextContent());
//                }
//            }
//        } catch (TransformerException transformerException) {
//            new ArbilBugCatcher().logError(transformerException);
//        }
    }

    public UniqueIdentifier[] linkEntities(UniqueIdentifier leadIdentifier, UniqueIdentifier[] otherIdentifiers, DataTypes.RelationType relationType, String dcrType, String customType) throws ImportException {
        try {
            EntityDocument leadEntityDocument = getEntityDocument(leadIdentifier);
            for (UniqueIdentifier uniqueIdentifier : otherIdentifiers) {
                EntityDocument entityDocument = getEntityDocument(uniqueIdentifier);
                // add the new relation
                leadEntityDocument.entityData.addRelatedNode(entityDocument.entityData, relationType, null, null, dcrType, customType);
            }
            saveAllDocuments();
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return getAffectedIdentifiers();
    }

    public UniqueIdentifier[] linkEntities(UniqueIdentifier[] selectedIdentifiers, DataTypes.RelationType relationType, String dcrType, String customType) throws ImportException {
        ArrayList<EntityDocument> nonLeadEntityDocuments = new ArrayList<EntityDocument>();
        try {
            EntityDocument leadEntityDocument = getEntityDocuments(selectedIdentifiers, nonLeadEntityDocuments);
            for (EntityDocument entityDocument : nonLeadEntityDocuments) {
                // add the new relation
                leadEntityDocument.entityData.addRelatedNode(entityDocument.entityData, relationType, null, null, dcrType, customType);
            }
            saveAllDocuments();
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return getAffectedIdentifiers();
    }

    public UniqueIdentifier[] unlinkEntities(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers) throws ImportException {
        ArrayList<EntityDocument> nonLeadEntityDocuments = new ArrayList<EntityDocument>();
        try {
            EntityDocument leadEntityDocument = getEntityDocuments(selectedIdentifiers, nonLeadEntityDocuments);
            while (!nonLeadEntityDocuments.isEmpty()) {
                for (EntityDocument entityDocument : nonLeadEntityDocuments) {
                    // remove the relation
                    leadEntityDocument.entityData.removeRelationsWithNode(entityDocument.entityData);
                    entityDocument.entityData.removeRelationsWithNode(leadEntityDocument.entityData);
                }
                leadEntityDocument = nonLeadEntityDocuments.remove(0);
            }
            saveAllDocuments();
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return getAffectedIdentifiers();
    }
}
