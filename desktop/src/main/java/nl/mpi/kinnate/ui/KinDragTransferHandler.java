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
package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindocument.EntityDocument;
import nl.mpi.kinnate.kindocument.ImportTranslator;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Created on : Wed Feb 02 11:17:40 CET 2011
 *
 * @author Peter.Withers@mpi.nl
 */
public class KinDragTransferHandler extends TransferHandler implements Transferable {

    private ArbilNode[] selectedNodes;
    private DataFlavor dataFlavor = new DataFlavor(ArbilNode[].class, "ArbilObject");
    private DataFlavor[] dataFlavors = new DataFlavor[]{dataFlavor, DataFlavor.stringFlavor};
    final private KinDiagramPanel kinDiagramPanel;
    private EntityData targetEntity = null;
    final private SessionStorage sessionStorage;
    final private EntityCollection entityCollection;

    public KinDragTransferHandler(KinDiagramPanel kinDiagramPanel, SessionStorage sessionStorage, EntityCollection entityCollection) {
        this.kinDiagramPanel = kinDiagramPanel;
        this.sessionStorage = sessionStorage;
        this.entityCollection = entityCollection;
    }

    @Override
    public int getSourceActions(JComponent comp) {
        return COPY;
    }

    @Override
    public Transferable createTransferable(JComponent comp) {
        selectedNodes = ((ArbilTree) comp).getAllSelectedNodes();
        if (selectedNodes.length == 0) {
            return null;
        }
        for (ArbilNode arbilNode : selectedNodes) {
            if (arbilNode instanceof ArbilDataNode) {
                if (((ArbilDataNode) arbilNode).getParentDomNode().isCorpus() || ((ArbilDataNode) arbilNode).getParentDomNode().isCatalogue() || ((ArbilDataNode) arbilNode).getParentDomNode().isDirectory()) {
                    return null;
                }
            }
        }
        return this;
    }

    @Override
    public void exportDone(JComponent comp, Transferable trans, int action) {
        kinDiagramPanel.redrawIfKinTermsChanged();
    }
    //////////////////////////////////////

    public Object /* ArbilTreeObject[] */ getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        if (df.equals(DataFlavor.stringFlavor)) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ArbilNode arbilNode : selectedNodes) {
                if (arbilNode instanceof KinTreeNode) {
                    if (stringBuilder.length() == 0) {
                        stringBuilder.append("\n");
                    }
                    stringBuilder.append("[Entity.Identifier=");
                    stringBuilder.append(((KinTreeNode) arbilNode).getUniqueIdentifier().getQueryIdentifier());
                    stringBuilder.append("]");
                }
            }
            return stringBuilder.toString();
        } else {
            return ""; //selectedNodes;
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
        return true;
    }
    //////////////////////////////////////

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        targetEntity = null;
//        if (!support.isDrop()) {
//            return false;
//        }
        if (!support.isDataFlavorSupported(dataFlavor)) {
            return false;
        }

        Component dropLocation = support.getComponent(); // getDropLocation
//        System.out.println("dropLocation: " + dropLocation.toString());
        if (dropLocation instanceof GraphPanel) {
            return true;
        }
        if (dropLocation instanceof KinTree) {
            if (selectedNodes != null && selectedNodes.length > 0 && selectedNodes[0] instanceof ArbilDataNode) {
                ArbilNode dropNode = ((KinTree) dropLocation).getLeadSelectionNode();
                if (dropNode == null) {
                    return true; //support.setDropAction(NONE);
                } else if (dropNode instanceof KinTreeNode) {
                    targetEntity = ((KinTreeNode) dropNode).getEntityData(); //  final KinTreeNode kinTreeNode = (KinTreeNode) dropNode;
                    if (targetEntity == null || targetEntity.getUniqueIdentifier().isTransientIdentifier()) {
                        // only allow imdi and cmdi nodes to be droped to a kin entity that is permanent (having metadata)
                        return false; //support.setDropAction(NONE);
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean addEntitiesToGraph(Point defaultLocation) {
        ArrayList<UniqueIdentifier> slectedIdentifiers = new ArrayList<UniqueIdentifier>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(UniqueIdentifier.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            for (ArbilNode currentArbilNode : selectedNodes) {
                if (currentArbilNode instanceof KinTreeNode) {
                    KinTreeNode kinTreeNode = (KinTreeNode) currentArbilNode;
                    slectedIdentifiers.add(kinTreeNode.getUniqueIdentifier());
                    // the following methods use either the xml file or the arbil tree node to get the entity identifier
                    // while they are no longer used it is probably good to keep them for reference
//                    try {
//                        Document metadataDom = ArbilComponentBuilder.getDocument(currentArbilNode.getURI());
//                        Node uniqueIdentifierNode = org.apache.xpath.XPathAPI.selectSingleNode(metadataDom, "/:Kinnate/kin:Entity/kin:Identifier"); // note that this is using the name space prefix not the namespace url
//                        try {
//                            UniqueIdentifier uniqueIdentifier = (UniqueIdentifier) unmarshaller.unmarshal(uniqueIdentifierNode, UniqueIdentifier.class).getValue();
//                            slectedIdentifiers.add(uniqueIdentifier);
//                        } catch (JAXBException exception) {
//                            new ArbilBugCatcher().logError(exception);
//                        }
//                    } catch (IOException exception) {
//                        new ArbilBugCatcher().logError(exception);
//                    } catch (ParserConfigurationException exception) {
//                        new ArbilBugCatcher().logError(exception);
//                    } catch (SAXException exception) {
//                        new ArbilBugCatcher().logError(exception);
//                    } catch (TransformerException exception) {
//                        new ArbilBugCatcher().logError(exception);
//                    }
                    // todo: new UniqueIdentifier could take ArbilDataNode as a constructor parameter, which would move this code out of this class
//                    for (String currentIdentifierType : new String[]{"Kinnate.Entity.Identifier.LocalIdentifier", "Kinnate.Gedcom.UniqueIdentifier.PersistantIdentifier", "Kinnate.Entity.UniqueIdentifier.PersistantIdentifier"}) {
//                        if (currentArbilNode.getFields().containsKey(currentIdentifierType)) {
//                            slectedIdentifiers.add(new UniqueIdentifier(currentArbilNode.getFields().get(currentIdentifierType)[0]));
//                            break;
//                        }
//                    }
                    // end identifier getting code
                }
            }
        } catch (JAXBException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
        kinDiagramPanel.addRequiredNodes(slectedIdentifiers.toArray(new UniqueIdentifier[]{}), defaultLocation);
        return true;
    }

    private boolean importMetadata(Point defaultLocation) {
        System.out.println("importMetadata");
        final ImportTranslator importTranslator = new ImportTranslator(true);
        importTranslator.addTranslationEntry("Sex", "Male", "Gender", "Male");
        importTranslator.addTranslationEntry("Sex", "Female", "Gender", "Female");
        importTranslator.addTranslationEntry("BirthDate", null, "DateOfBirth", null);
        try {
            ArrayList<EntityDocument> entityDocumentList = new ArrayList<EntityDocument>();
            for (ArbilNode draggedNode : selectedNodes) {
                // todo: allow the user to set EntityDocument.defaultDragType some where
                EntityDocument entityDocument = new EntityDocument(EntityDocument.defaultDragType, importTranslator, sessionStorage, entityCollection.getProjectRecord());
                entityDocument.insertValue("Name", draggedNode.toString());
                if (draggedNode instanceof ArbilDataNode) {
                    for (String fieldOfInterest : new String[]{"Sex", "BirthDate"}) {
                        final ArbilField[] arbilFeildsArray = ((ArbilDataNode) draggedNode).getFields().get(fieldOfInterest);
                        if (arbilFeildsArray != null && arbilFeildsArray.length > 0) {
                            entityDocument.insertValue(fieldOfInterest, arbilFeildsArray[0].getFieldValue().toLowerCase());
                        }
                    }
                    entityDocument.entityData.addArchiveLink(((ArbilDataNode) draggedNode).getURI());
                }
                // todo: based on the DCR entries the relevant data could be selected and inserted, or the user could specify which fields to insert
//            entityDocument.insertDefaultMetadata(); // todo: insert copy of metadata from source node
                //attachMetadata(entityDocument.entityData); // if a node is dragged to the graph then the odds are that they are all different actors so we create one entity for each rather than using attachMetadata
                entityDocumentList.add(entityDocument);
            }
            for (EntityDocument entityDocument : entityDocumentList) {
                entityDocument.saveDocument();
                URI addedEntityUri = entityDocument.getFile().toURI();
                entityCollection.updateDatabase(entityDocument.getFile().toURI(), entityDocument.getUniqueIdentifier());
                kinDiagramPanel.addRequiredNodes(new UniqueIdentifier[]{entityDocument.getUniqueIdentifier()}, defaultLocation);
            }
            return true;
        } catch (ImportException exception) {
            // todo: warn user with a dialog
            BugCatcherManager.getBugCatcher().logError(exception);
            return false;
        } catch (EntityServiceException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            return false;
            // dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Get Entity Path");
        }
    }

    private boolean attachMetadata(EntityData entityData) {
        for (ArbilNode currentArbilNode : selectedNodes) {
            final ArbilDataNode currentArbilDataNode = (ArbilDataNode) currentArbilNode;
            entityData.addArchiveLink(currentArbilDataNode.getURI());
            // todo: insert the archive handle here also
            // todo: insert the entity identifier into the attached metadata



//                Document metadataDom = ArbilComponentBuilder.getDocument(addedNodePath);
//                        if (createdEntity) {
//                            // set the name node
//                            Node uniqueIdentifierNode = org.apache.xpath.XPathAPI.selectSingleNode(metadataDom, "/:Kinnate/:Entity/:Name");
//                currentArbilDataNode.get
//currentArbilNode
            // todo: if the dropped archive node alreay is linked in the kin database then show that kin entity, if the node is dragged from the archive tree or from one kin entity to another then add or move it to the new target
            // todo: if multiple are draged then add them all to the same entity
//                                    entityDocument.
//currentArbilNode.getUrlString()
            // create and set the link node
            // todo: this might well be updated at a later date, or even use the cmdi link type although that is more complex than required
            // cmdi link types have been considered here but they are very complex and not well suited to kinship needs so we are using the corpus link type for now
            // save the changes
        }
        return false;
    }

    private boolean attachMetadata() {
        System.out.println("importMetadata");
        try {
            EntityDocument entityDocument = new EntityDocument(targetEntity.getUniqueIdentifier().getFileInProject(entityCollection.getProjectRecord()).toURI(), new ImportTranslator(true), sessionStorage, entityCollection.getProjectRecord());
            attachMetadata(entityDocument.entityData);
            entityDocument.saveDocument();
            URI addedEntityUri = entityDocument.getFile().toURI();
            entityCollection.updateDatabase(entityDocument.getFile().toURI(), entityDocument.getUniqueIdentifier());
            kinDiagramPanel.entityRelationsChanged(new UniqueIdentifier[]{entityDocument.getUniqueIdentifier()});
            return true;
        } catch (ImportException exception) {
            // todo: warn user with a dialog
            BugCatcherManager.getBugCatcher().logError(exception);
            return false;
//        } catch (URISyntaxException exception) {
//            // todo: warn user with a dialog
//            BugCatcherManager.getBugCatcher().logError(exception);
//            return false;
        } catch (EntityServiceException exception) {
            // todo: warn user with a dialog
            BugCatcherManager.getBugCatcher().logError(exception);
//            dialogHandler.addMessageDialogToQueue(exception.getMessage(), "Update Database");
            return false;
        }
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        // if we can't handle the import, say so
        if (!canImport(support)) {
            return false;
        }

        boolean isImportingMetadata = (selectedNodes != null && selectedNodes.length > 0 && selectedNodes[0] instanceof ArbilDataNode);

        Component dropLocation = support.getComponent();
        // todo: in the case of dropping to the ego tree add the entity to the ego list
        // todo: in the case of dropping to the required tree add the entity to the required list and remove from the ego list (if in that list)
        // todo: in the case of dragging from the transient tree offer to make the entity permanent and create metadata
        if (dropLocation instanceof GraphPanel) {
            Point defaultLocation = support.getDropLocation().getDropPoint();
            System.out.println("dropped to GraphPanel");
            if (isImportingMetadata) {
                return importMetadata(defaultLocation);
            } else {
                return addEntitiesToGraph(defaultLocation);
            }
        } else if (dropLocation instanceof KinTree) {
            System.out.println("dropped to KinTree");
            if (isImportingMetadata) {
                return attachMetadata();
            }
        }
        return false;
    }
}
