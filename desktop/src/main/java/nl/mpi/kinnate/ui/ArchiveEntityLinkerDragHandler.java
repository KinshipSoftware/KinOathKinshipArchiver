package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Document   : ArchiveEntityLinkerDragHandler
 * Created on : Wed Feb 02 11:17:40 CET 2011
 * @author Peter.Withers@mpi.nl
 */
public class ArchiveEntityLinkerDragHandler extends TransferHandler implements Transferable {

    ArbilDataNode[] selectedNodes;
    DataFlavor dataFlavor = new DataFlavor(ArbilDataNode[].class, "ArbilTreeObject");
    DataFlavor[] dataFlavors = new DataFlavor[]{dataFlavor};
    ArbilTree kinTree;

    public ArchiveEntityLinkerDragHandler(ArbilTree kinTreeLocal) {
        kinTree = kinTreeLocal;
    }

    @Override
    public int getSourceActions(JComponent comp) {
        return COPY;
    }

    @Override
    public Transferable createTransferable(JComponent comp) {
        selectedNodes = ((ArbilTree) comp).getSelectedNodes();
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
        if (dropLocation.equals(kinTree)) {
            for (ArbilDataNode arbilDataNode : selectedNodes) {
                // if there is at least one valid node selected then allow the process to continue
                if (arbilDataNode.getParentDomNode().isSession()) {
                    return true;
                }
            }
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
//        String data;
//        try {
//            data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
//        } catch (UnsupportedFlavorException e) {
//            return false;
//        } catch (java.io.IOException e) {
//            return false;
//        }

        Component dropLocation = support.getComponent();
        URI addedNodePath = null;
        if (dropLocation.equals(kinTree)) {
            System.out.println("dropped to the kinTree");
            ArbilDataNode[] selectedArbilNodes = kinTree.getSelectedNodes();
            for (ArbilDataNode currentArbilNode : selectedNodes) {
                boolean createdEntity = false;
                // todo: if the dropped archive node alreay is linked in the kin database then show that kin entity, if the node is dragged from the archive tree or from one kin entity to another then add or move it to the new target
                // todo: if multiple are draged then add them all to the same entity
                try {
                    if (selectedArbilNodes != null && selectedArbilNodes.length == 1) {
                        addedNodePath = selectedArbilNodes[0].getURI();
                    } else {
                        String nodeType = ArchiveEntityLinkerDragHandler.class.getResource("/xsd/StandardEntity.xsd").toString();
                        URI targetFileURI = ArbilSessionStorage.getSingleInstance().getNewArbilFileName(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), nodeType);
                        ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                        addedNodePath = componentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
                        // set the unique idntifier
//                        String localIdentifier = new LocalIdentifier().setLocalIdentifier(new File(addedNodePath));
                        createdEntity = true;
                    }
                    try {
                        Document metadataDom = ArbilComponentBuilder.getDocument(addedNodePath);
                        if (createdEntity) {
                            // set the name node
                            Node uniqueIdentifierNode = org.apache.xpath.XPathAPI.selectSingleNode(metadataDom, "/:Kinnate/:Entity/:Name");
                            uniqueIdentifierNode.setTextContent(currentArbilNode.toString());
                        }
                        // create and set the link node
                        Element linkPathElement = metadataDom.createElement("CorpusLink"); // todo: this might well be updated at a later date, or even use the cmdi link type although that is more complex than required
                        linkPathElement.setTextContent(currentArbilNode.getUrlString());
                        metadataDom.getDocumentElement().appendChild(linkPathElement);
                        // save the changes
                        ArbilComponentBuilder.savePrettyFormatting(metadataDom, new File(addedNodePath));
                    } catch (DOMException exception) {
                        new ArbilBugCatcher().logError(exception);
                    } catch (TransformerException exception) {
                        new ArbilBugCatcher().logError(exception);
                    } catch (IOException exception) {
                        new ArbilBugCatcher().logError(exception);
                    } catch (ParserConfigurationException exception) {
                        new ArbilBugCatcher().logError(exception);
                    } catch (SAXException exception) {
                        new ArbilBugCatcher().logError(exception);
                    }
                    ArbilDataNode kinArbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, addedNodePath);
                    if (createdEntity) {
                        // cmdi link types have been considered here but they are very complex and not well suited to kinship needs so we are using the corpus link type for now
//                    new ArbilComponentBuilder().insertResourceProxy(kinArbilDataNode, currentArbilNode);
                        ArrayList<ArbilNode> kinTreeNodes = new ArrayList<ArbilNode>();
                        if (kinTree.rootNodeChildren != null) {
                            kinTreeNodes.addAll(Arrays.asList(kinTree.rootNodeChildren));
                        }
                        kinTreeNodes.add(kinArbilDataNode);
                        kinTree.rootNodeChildren = kinTreeNodes.toArray(new ArbilDataNode[]{});
                    } else {
                        kinArbilDataNode.reloadNode();
                    }
                    kinTree.requestResort();
                } catch (URISyntaxException ex) {
                    new ArbilBugCatcher().logError(ex);
                    // todo: warn user with a dialog
                    return false;
                }
            }
            if (addedNodePath != null) {
                new EntityCollection().updateDatabase(addedNodePath);
            }
            return true;
        }
        return false;
    }
}
