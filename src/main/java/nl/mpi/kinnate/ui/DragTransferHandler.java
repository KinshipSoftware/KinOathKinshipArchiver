package nl.mpi.kinnate.ui;

import nl.mpi.kinnate.ui.KinTypeEgoSelectionTestPanel;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import nl.mpi.arbil.ImdiTree;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 * Document   : DragTransferHandler
 * Created on : Wed Feb 02 11:17:40 CET 2011
 * @author Peter.Withers@mpi.nl
 */
public class DragTransferHandler extends TransferHandler implements Transferable {

    ImdiTreeObject[] selectedNodes;
    DataFlavor[] dataFlavors = new DataFlavor[]{/*new DataFlavor("ImdiTreeObject"),*/DataFlavor.stringFlavor};

    public int getSourceActions(JComponent comp) {
        return COPY;
    }

    public Transferable createTransferable(JComponent comp) {
        selectedNodes = ((ImdiTree) comp).getSelectedNodes();
        if (selectedNodes.length == 0) {
            return null;
        }
        return this;
    }

    public void exportDone(JComponent comp, Transferable trans, int action) {
//        if (action != MOVE) {
//            return;
//        }
    }
    //////////////////////////////////////

    public Object /*ImdiTreeObject[]*/ getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        return ""; //selectedNodes;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
        return true;
    }
    //////////////////////////////////////

    public boolean canImport(TransferHandler.TransferSupport support) {
//        if (!support.isDrop()) {
//            return false;
//        }
//        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
//            return false;
//        }

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
            for (ImdiTreeObject currentImdiNode : selectedNodes) {
                slectedUris.add(currentImdiNode.getURI());
            }
            ((KinTypeEgoSelectionTestPanel) dropLocation).addEgoNodes(slectedUris.toArray(new URI[]{}));
            return true;
        }
        return false;
    }
}
