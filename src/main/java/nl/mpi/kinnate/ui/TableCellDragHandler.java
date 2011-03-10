package nl.mpi.kinnate.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.ImdiTable;
import nl.mpi.kinnate.entityindexer.IndexerParameters.IndexerParam;

/**
 *  Document   : TableCellDragHandler
 *  Created on : Mar 10, 2011, 11:16:34 AM
 *  Author     : Peter Withers
 */
public class TableCellDragHandler extends TransferHandler implements Transferable {

    private ImdiField[] selectedFields = null;
    private DataFlavor dataFlavor = new DataFlavor(ImdiField[].class, "ImdiField");

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
        selectedFields = ((ImdiTable) jc).getSelectedFields();
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
        ArrayList<String[]> paramValues = new ArrayList<String[]>(Arrays.asList(indexerParam.getValues()));
        //for (ImdiField imdiField : (ImdiField[]) ts.getTransferable().getTransferData(dataFlavor)) {
        for (ImdiField imdiField : selectedFields) {
            paramValues.add(new String[]{imdiField.getFullXmlPath(), imdiField.getFieldValue()});
        }
        indexerParam.setValues(paramValues.toArray(new String[][]{}));
        ((FieldSelectionList) ts.getComponent()).updateUiList();
        return true;
    }
}
