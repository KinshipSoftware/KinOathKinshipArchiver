package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document   : KinDragTransferHandler
 * Created on : Wed Feb 02 11:17:40 CET 2011
 * @author Peter.Withers@mpi.nl
 */
public class KinDragTransferHandler extends TransferHandler implements Transferable {

    ArbilNode[] selectedNodes;
    DataFlavor dataFlavor = new DataFlavor(ArbilNode[].class, "ArbilObject");
    DataFlavor[] dataFlavors = new DataFlavor[]{dataFlavor};

    @Override
    public int getSourceActions(JComponent comp) {
        return COPY;
    }

    @Override
    public Transferable createTransferable(JComponent comp) {
        selectedNodes = ((KinTree) comp).getAllSelectedNodes();
        if (selectedNodes.length == 0) {
            return null;
        }
        return this;
    }

    @Override
    public void exportDone(JComponent comp, Transferable trans, int action) {
//        if (action != MOVE) {
//            return;
//        }
    }
    //////////////////////////////////////

    public Object /*ArbilTreeObject[]*/ getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        return ""; //selectedNodes;
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
//        if (!support.isDrop()) {
//            return false;
//        }
        if (!support.isDataFlavorSupported(dataFlavor)) {
            return false;
        }

        Component dropLocation = support.getComponent(); // getDropLocation
        if (dropLocation instanceof KinDiagramPanel) {
            return true;
        }

//        boolean actionSupported = (COPY & support.getSourceDropActions()) == COPY;
//        if (actionSupported) {
//            support.setDropAction(COPY);
//            return true;
//        }
        return false;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        // if we can't handle the import, say so
        if (!canImport(support)) {
            return false;
        }

        Component dropLocation = support.getComponent();
        if (dropLocation instanceof KinDiagramPanel) {
            System.out.println("dropped to KinDiagramPanel");
            ArrayList<UniqueIdentifier> slectedIdentifiers = new ArrayList<UniqueIdentifier>();
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(UniqueIdentifier.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                for (ArbilNode currentArbilNode : selectedNodes) {
                    if (currentArbilNode instanceof KinTreeNode) {
                        KinTreeNode kinTreeNode = (KinTreeNode) currentArbilNode;
                        slectedIdentifiers.add(kinTreeNode.entityData.getUniqueIdentifier());
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
                new ArbilBugCatcher().logError(exception);
            }
            ((KinDiagramPanel) dropLocation).addRequiredNodes(slectedIdentifiers.toArray(new UniqueIdentifier[]{}));
            return true;
        }
        return false;
    }
}
