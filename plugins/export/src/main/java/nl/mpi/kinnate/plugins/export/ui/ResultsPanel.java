package nl.mpi.kinnate.plugins.export.ui;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Document : ResultsPanel
 * Created on : Jul 19, 2012, 12:00:44 PM
 * Author : Peter Withers
 */
public class ResultsPanel extends JPanel {

    JTable resultsTable;
    String[][] tableData = null;
    AbstractTableModel resultsModel = new AbstractTableModel() {

        public int getRowCount() {
            if (tableData == null) {
                return 0;
            }
            return tableData.length;
        }

        public int getColumnCount() {
            if (tableData == null || tableData.length < 1) {
                return 0;
            }
            return tableData[0].length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return tableData[rowIndex][columnIndex];
        }
    };

    public ResultsPanel() {
        resultsTable = new JTable(resultsModel);
        this.add(resultsTable);
    }

    public void updateTable(String resultsString) {
        String[] resultsRows = resultsString.split("\n");
        if (resultsRows.length > 0) {
            final String fieldSeparator = "\",\"";
            String[] resultsHeader = resultsRows[0].split(fieldSeparator);
            tableData = new String[resultsRows.length][resultsHeader.length];
            tableData[0] = resultsHeader;
            for (int rowCounter = 1; rowCounter < resultsRows.length; rowCounter++) {
                tableData[rowCounter] = resultsRows[rowCounter].split(fieldSeparator);
            }
        } else {
            tableData = new String[0][0];
        }
        resultsModel.fireTableDataChanged();
    }
}
