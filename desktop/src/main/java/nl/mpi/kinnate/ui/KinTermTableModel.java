package nl.mpi.kinnate.ui;

import java.util.HashSet;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTerm;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;

/**
 *  Document   : KinTermTableModel
 *  Created on : Oct 17, 2011, 2:50:02 PM
 *  Author     : Peter Withers
 */
public class KinTermTableModel extends AbstractTableModel implements TableModelListener {

    KinTermGroup kinTerms;
    SavePanel savePanel;
    HashSet<KinTerm> checkBoxSet = new HashSet<KinTerm>();

    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);

    }

    public int getColumnCount() {
        return 6;
    }

    public int getRowCount() {
        return kinTerms.getKinTerms().length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        KinTerm kinTerm = kinTerms.getKinTerms()[rowIndex];
        switch (columnIndex) {
            case 0:
                return kinTerm.kinTerm;
            case 1:
                return kinTerm.alterKinTypeStrings;
            case 2:
                return kinTerm.propositusKinTypeStrings;
            case 3:
                return kinTerm.anchorKinTypeStrings;
            case 4:
                return kinTerm.kinTermDescription;
            case 5:
                return false;
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        KinTerm kinTerm = kinTerms.getKinTerms()[rowIndex];
        super.setValueAt(aValue, rowIndex, columnIndex);
        switch (columnIndex) {
            case 0:
                kinTerm.kinTerm = aValue.toString();
            case 1:
                kinTerm.alterKinTypeStrings = aValue.toString();
            case 2:
                kinTerm.propositusKinTypeStrings = aValue.toString();
            case 3:
                kinTerm.anchorKinTypeStrings = aValue.toString();
            case 4:
                kinTerm.kinTermDescription = aValue.toString();
            case 5:
                // todo: check the value
                checkBoxSet.remove(kinTerm);
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
    }
}
