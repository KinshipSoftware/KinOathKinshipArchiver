package nl.mpi.kinnate.kindocument;

import java.net.URISyntaxException;
import java.util.ArrayList;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.svg.GraphPanel;
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

    public UniqueIdentifier[] linkEntities(UniqueIdentifier leadIdentifier, UniqueIdentifier[] otherIdentifiers, DataTypes.RelationType relationType) throws ImportException {
        try {
            EntityDocument leadEntityDocument = getEntityDocument(leadIdentifier);
            for (UniqueIdentifier uniqueIdentifier : otherIdentifiers) {
                EntityDocument entityDocument = getEntityDocument(uniqueIdentifier);
                // add the new relation
                leadEntityDocument.entityData.addRelatedNode(entityDocument.entityData, relationType, DataTypes.RelationLineType.sanguineLine, null, null);
            }
            saveAllDocuments();
        } catch (URISyntaxException exception) {
            new ArbilBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return getAffectedIdentifiers();
    }

    public UniqueIdentifier[] linkEntities(UniqueIdentifier[] selectedIdentifiers, DataTypes.RelationType relationType) throws ImportException {
        ArrayList<EntityDocument> nonLeadEntityDocuments = new ArrayList<EntityDocument>();
        try {
            EntityDocument leadEntityDocument = getEntityDocuments(selectedIdentifiers, nonLeadEntityDocuments);
            for (EntityDocument entityDocument : nonLeadEntityDocuments) {
                // add the new relation
                leadEntityDocument.entityData.addRelatedNode(entityDocument.entityData, relationType, DataTypes.RelationLineType.sanguineLine, null, null);
            }
            saveAllDocuments();
        } catch (URISyntaxException exception) {
            new ArbilBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return getAffectedIdentifiers();
    }

    public void unlinkEntities(GraphPanel graphPanel, UniqueIdentifier[] selectedIdentifiers) {
//        HashMap<UniqueIdentifier, EntityData> selectedEntityMap = graphPanel.getEntitiesById(selectedIdentifiers);
//        EntityData leadSelectionEntity = selectedEntityMap.get(selectedIdentifiers[0]);
//        for (EntityData selectedEntity : selectedEntityMap.values()) {
//            String targetPath = selectedEntity.getEntityPath();
//            try {
//                URI targetUri = new URI(targetPath);
//                Document metadataDom = ArbilComponentBuilder.getDocument(targetUri);
//                if (selectedEntity.equals(leadSelectionEntity)) {
//                    // remove all relations in the list (the lead selection is also in this list but there would not be any links to itself anyway)
//                    removeMatchingRelations(metadataDom, selectedIdentifiers);
//                } else {
//                    // only remove the lead selection from the other entities
//                    removeMatchingRelations(metadataDom, new UniqueIdentifier[]{leadSelectionEntity.getUniqueIdentifier()});
//                }
//                // save the xml file
//                ArbilComponentBuilder.savePrettyFormatting(metadataDom, new File(targetUri));
//                // update the database
//                saveAllDocuments();
//            } catch (URISyntaxException exception) {
//                // todo: inform the user if there is an error
//                new ArbilBugCatcher().logError(exception);
//            } catch (DOMException exception) {
//                new ArbilBugCatcher().logError(exception);
//            } catch (IOException exception) {
//                new ArbilBugCatcher().logError(exception);
//            } catch (ParserConfigurationException exception) {
//                new ArbilBugCatcher().logError(exception);
//            } catch (SAXException exception) {
//                new ArbilBugCatcher().logError(exception);
//            }
//        }
        throw new UnsupportedOperationException("todo...");
    }
}
