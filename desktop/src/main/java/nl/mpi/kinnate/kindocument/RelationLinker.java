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
import java.net.URISyntaxException;
import java.util.ArrayList;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.Document;

/**
 * Created on : Apr 12, 2011, 1:45:01 PM
 *
 * @author Peter Withers
 */
public class RelationLinker extends DocumentLoader {

    public RelationLinker(SessionStorage sessionStorage, MessageDialogHandler dialogHandler, EntityCollection entityCollection) {
        super(sessionStorage, dialogHandler, entityCollection);
    }

//    private void removeMatchingRelations(Document entityDocument, UniqueIdentifier[] selectedIdentifiers) {
//        // todo: complete the relation removal code
//        // todo: this should use the EntityData object to remove the relaitons and then save via jaxb as is done in linkEntities other wise if a user removes a sibling relation and the common parent relation persists then the data will be in a broken state
////        ArrayList<String> identifierList = new ArrayList<String>();
////        for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
////            identifierList.add(uniqueIdentifier.getQueryIdentifier());
////        }
////        Element roodNode = entityDocument.getDocumentElement();
////        NodeList entityNodeList = roodNode.getElementsByTagNameNS("http://mpi.nl/tla/kin", "Entity");
////        Element entityNode = ((Element) entityNodeList.item(0));
////        NodeList relationsNodeList = entityNode.getElementsByTagNameNS("http://mpi.nl/tla/kin", "Relations");
////        Node relationsGroupNode = relationsNodeList.item(0);
////        if (relationsGroupNode != null) {
////            for (Node relationNode = relationsGroupNode.getFirstChild(); relationNode != null; relationNode = relationNode.getNextSibling()) {
////                if ("Relation".equals(relationNode.getLocalName())) {
////                    if (identifierList.contains(relationNode.getFirstChild().getNodeValue())) {
////                        // remove the matching relations
////                        relationsGroupNode.removeChild(relationNode);
////                    }
////                }
////            }
////        }
////        try {
////            NodeList relationIdentifierNodeList = org.apache.xpath.XPathAPI.selectNodeList(entityDocument, "/Kinnate/kin:Entity/kin:Relations/kin:Relation/kin:Identifier", roodNode);
////            for (int nodeCounter = 0; nodeCounter < relationIdentifierNodeList.getLength(); nodeCounter++) {
////                Node identifierNode = relationIdentifierNodeList.item(nodeCounter);
////                if (identifierNode != null) {
////                    System.out.println(identifierNode.getLocalName() + " : " + identifierNode.getTextContent());
////                }
////            }
////        } catch (TransformerException transformerException) {
////            new ArbilBugCatcher().logError(transformerException);
////        }
//    }

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
        } catch (EntityServiceException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return getAffectedIdentifiers();
    }

    public UniqueIdentifier[] linkEntities(UniqueIdentifier[] selectedIdentifiers, DataTypes.RelationType relationType, String dcrType, String customType) throws ImportException {
        // this is only used from the context menu not drag handle actions
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
        } catch (EntityServiceException exception) {
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
        } catch (EntityServiceException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        return getAffectedIdentifiers();
    }

    public void deleteEntity(UniqueIdentifier[] selectedIdentifiers) throws ImportException {
        ArrayList<EntityDocument> entityDocumentList = new ArrayList<EntityDocument>();
        try {
            getEntityDocuments(selectedIdentifiers, entityDocumentList);
            for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
                EntityDocument currentDocument = entityMap.get(uniqueIdentifier);
                for (EntityRelation entityRelation : currentDocument.entityData.getAllRelations()) {
                    EntityDocument relatedDocument = entityMap.get(entityRelation.alterUniqueIdentifier);
//                    if (relatedDocument == null) {
                    // remove the relation
                    currentDocument.entityData.removeRelationsWithNode(relatedDocument.entityData);
                    relatedDocument.entityData.removeRelationsWithNode(currentDocument.entityData);
//                    }
                }
            }
            saveAllDocuments();
            for (UniqueIdentifier uniqueIdentifier : selectedIdentifiers) {
                EntityDocument currentDocument = entityMap.get(uniqueIdentifier);
                try {
                    deleteFromDataBase(currentDocument);
                } catch (IOException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    throw new ImportException("Error: " + exception.getMessage());
                }
            }
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        } catch (EntityServiceException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
    }
}
