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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Document   : KinDragTransferHandler
 * Created on : Wed Feb 02 11:17:40 CET 2011
 * @author Peter.Withers@mpi.nl
 */
public class KinDragTransferHandler extends TransferHandler implements Transferable {

    ArbilDataNode[] selectedNodes;
    DataFlavor dataFlavor = new DataFlavor(ArbilDataNode[].class, "ArbilTreeObject");
    DataFlavor[] dataFlavors = new DataFlavor[]{dataFlavor};

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
//        String data;
//        try {
//            data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
//        } catch (UnsupportedFlavorException e) {
//            return false;
//        } catch (java.io.IOException e) {
//            return false;
//        }

        Component dropLocation = support.getComponent();
        if (dropLocation instanceof KinDiagramPanel) {
            System.out.println("dropped to KinTypeEgoSelectionTestPanel");
            ArrayList<UniqueIdentifier> slectedIdentifiers = new ArrayList<UniqueIdentifier>();
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(UniqueIdentifier.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                for (ArbilDataNode currentArbilNode : selectedNodes) {
                    try {
                        Document metadataDom = ArbilComponentBuilder.getDocument(currentArbilNode.getURI());
                        Node uniqueIdentifierNode = org.apache.xpath.XPathAPI.selectSingleNode(metadataDom, "/:Kinnate/kin:Entity/kin:Identifier"); // note that this is using the name space prefix not the namespace url
                        try {
                            UniqueIdentifier uniqueIdentifier = (UniqueIdentifier) unmarshaller.unmarshal(uniqueIdentifierNode, UniqueIdentifier.class).getValue();
                            slectedIdentifiers.add(uniqueIdentifier);
                        } catch (JAXBException exception) {
                            new ArbilBugCatcher().logError(exception);
                        }
                    } catch (IOException exception) {
                        new ArbilBugCatcher().logError(exception);
                    } catch (ParserConfigurationException exception) {
                        new ArbilBugCatcher().logError(exception);
                    } catch (SAXException exception) {
                        new ArbilBugCatcher().logError(exception);
                    } catch (TransformerException exception) {
                        new ArbilBugCatcher().logError(exception);
                    }
                    // todo: new UniqueIdentifier could take ArbilDataNode as a constructor parameter, which would move this code out of this class
//                    for (String currentIdentifierType : new String[]{"Kinnate.Entity.Identifier.LocalIdentifier", "Kinnate.Gedcom.UniqueIdentifier.PersistantIdentifier", "Kinnate.Entity.UniqueIdentifier.PersistantIdentifier"}) {
//                        if (currentArbilNode.getFields().containsKey(currentIdentifierType)) {
//                            slectedIdentifiers.add(new UniqueIdentifier(currentArbilNode.getFields().get(currentIdentifierType)[0]));
//                            break;
//                        }
//                    }

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
