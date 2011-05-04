package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.ui.ArbilTree;

/**
 * Document   : DragTransferHandler
 * Created on : Wed Feb 02 11:17:40 CET 2011
 * @author Peter.Withers@mpi.nl
 */
public class DragTransferHandler extends TransferHandler implements Transferable {

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
        if (dropLocation instanceof KinTypeEgoSelectionTestPanel) {
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
        if (dropLocation instanceof KinTypeEgoSelectionTestPanel) {
            System.out.println("dropped to KinTypeEgoSelectionTestPanel");
            ArrayList<URI> slectedUris = new ArrayList<URI>();
            ArrayList<String> slectedIdentifiers = new ArrayList<String>();
            for (ArbilDataNode currentArbilNode : selectedNodes) {
                slectedUris.add(currentArbilNode.getURI());
                for (String currentIdentifierType : new String[]{"Kinnate.Gedcom.UniqueIdentifier.LocalIdentifier", "Kinnate.Entity.UniqueIdentifier.LocalIdentifier", "Kinnate.Gedcom.UniqueIdentifier.PersistantIdentifier", "Kinnate.Entity.UniqueIdentifier.PersistantIdentifier"}) {
                    if (currentArbilNode.getFields().containsKey(currentIdentifierType)) {
                        slectedIdentifiers.add(currentArbilNode.getFields().get(currentIdentifierType)[0].getFieldValue());
                        break;
                    }
                }
            }
            ((KinTypeEgoSelectionTestPanel) dropLocation).addEgoNodes(slectedUris.toArray(new URI[]{}), slectedIdentifiers.toArray(new String[]{}));
            return true;
        }
        return false;
    }
}
