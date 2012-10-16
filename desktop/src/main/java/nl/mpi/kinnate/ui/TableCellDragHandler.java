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
        if (df.equals(DataFlavor.stringFlavor)) {
            String returnString = "";
            for (ArbilField imdiField : selectedFields) {
                returnString = returnString + convertToKinTypeStringQuery(imdiField.getFullXmlPath(), imdiField.getFieldValue());
            }
            return returnString;
        } else {
            throw new UnsupportedFlavorException(df); // the imdi fields are not passed here because importData uses the selectedFields instead
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{dataFlavor, DataFlavor.stringFlavor};
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
        if (dropLocation instanceof KinTypeStringInput) {
            // todo: it would be good to test if this is a kmdi file before allowing drag to the kin type string input
//            if (selectedFields.length>0 && selectedFields[0].getParentDataNode().is)
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

    private String convertToKinTypeStringQuery(String fieldName, String fieldValue) {
        fieldName = fieldName.replace(".Kinnate.CustomData.", "");
        String queryString = "[" + fieldName + "=" + fieldValue + "]";
        return queryString;
    }

    private String convertToBooleanQuery(String fieldName, String fieldValue) {
        final int lastIndexOf = fieldName.lastIndexOf(".");
        String queryNode = fieldName.substring(lastIndexOf + 1);
        String queryString = fieldName.substring(0, lastIndexOf);
        queryString = queryString.replace(".", "/*:");
        queryString = queryString.substring(1);
        queryString = queryString + "[*:" + queryNode + "='" + fieldValue + "']";
        return queryString;
    }

    private String convertToSelectQuery(String fieldName, String fieldValue) {
        String queryString = fieldName.replace(".", "/*:");
        queryString = queryString.substring(1);
        return queryString;
    }

    @Override
    public boolean importData(TransferSupport ts) {
        if (ts.getComponent() instanceof FieldSelectionList) {
            IndexerParam indexerParam = ((FieldSelectionList) ts.getComponent()).indexerParam;
            for (ArbilField imdiField : selectedFields) {
                String queryString;
                String paramValue = null;
                if (indexerParam.getAvailableValues() != null) {
                    queryString = convertToBooleanQuery(imdiField.getFullXmlPath(), imdiField.getFieldValue());
                    paramValue = "";
                } else {
                    queryString = convertToSelectQuery(imdiField.getFullXmlPath(), imdiField.getFieldValue());
                }
                indexerParam.setValue(queryString, paramValue);
            }
            ((FieldSelectionList) ts.getComponent()).updateUiList();
            return true;
        } else if (ts.getComponent() instanceof KinTypeStringInput) {
            return false; // the text area uses its own TransferHandler so this case should not occur
        } else {
            return false;
        }
    }
}
