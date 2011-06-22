package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.kinnate.entityindexer.IndexerParam;

/**
 *  Document   : TableCellDragHandler
 *  Created on : Mar 10, 2011, 11:16:34 AM
 *  Author     : Peter Withers
 */
public class TableCellDragHandler extends TransferHandler implements Transferable {

    private ArbilField[] selectedFields = null;
    private DataFlavor dataFlavor = new DataFlavor(ArbilField[].class, "ArbilField");

    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        //if (!df.match(dataFlavor)) {
//        if (df != dataFlavor) { // make sure that this is the same object not just the same type because we are using selectedFields from the source not the transfer handler because then it needs to be made serialiseable
//            throw new UnsupportedFlavorException(df);
//        } else {
//            return selectedFields;
//        }
        return ""; // the imdi fields are not passed here but selectedFields used instead
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{dataFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canImport(TransferSupport ts) {
        if (!ts.isDataFlavorSupported(dataFlavor)) {
            return false;
        }
        Component dropLocation = ts.getComponent(); // getDropLocation
        if (dropLocation instanceof FieldSelectionList) {
            return true;
        }
        return false;
    }

    @Override
    protected Transferable createTransferable(JComponent jc) {
        selectedFields = ((ArbilTable) jc).getSelectedFields();
        return this;
//        return super.createTransferable(jc);
    }

    @Override
    public int getSourceActions(JComponent jc) {
        return COPY;
    }

    @Override
    public boolean importData(TransferSupport ts) {
        IndexerParam indexerParam = ((FieldSelectionList) ts.getComponent()).indexerParam;
//        ArrayList<String[]> paramValues = new ArrayList<String[]>(Arrays.asList(indexerParam.getValues()));
        //for (ArbilField imdiField : (ArbilField[]) ts.getTransferable().getTransferData(dataFlavor)) {
        for (ArbilField imdiField : selectedFields) {
            indexerParam.setValue(imdiField.getFullXmlPath(), imdiField.getFieldValue());
        }
        ((FieldSelectionList) ts.getComponent()).updateUiList();
        return true;
    }
}
