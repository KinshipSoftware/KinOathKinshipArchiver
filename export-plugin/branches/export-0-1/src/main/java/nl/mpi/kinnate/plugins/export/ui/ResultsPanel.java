package nl.mpi.kinnate.plugins.export.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Document : ResultsPanel
 * Created on : Jul 19, 2012, 12:00:44 PM
 * Author : Peter Withers
 */
public class ResultsPanel extends JPanel {

    JTable resultsTable;
    String[] resultsHeader = null;
    String[][] tableData = null;
    AbstractTableModel resultsModel = new AbstractTableModel() {

        @Override
        public String getColumnName(int column) {
            if (resultsHeader == null) {
                return "";
            }
            return resultsHeader[column];
        }

        public int getRowCount() {
            if (tableData == null) {
                return 0;
            }
            return tableData.length;
        }

        public int getColumnCount() {
            if (tableData == null || tableData.length == 0) {
                return 0;
            }
            return resultsHeader.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
//            System.out.println(rowIndex + " : " + columnIndex);
            if (columnIndex < tableData[rowIndex].length) {
                return tableData[rowIndex][columnIndex];
            } else {
                return "<error>";
            }
        }
    };

    public ResultsPanel() {
        this.setLayout(new BorderLayout());
        resultsTable = new JTable(resultsModel);
        this.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
    }

    public void updateTable(String resultsString) {
        resultsString = resultsString.replaceAll("^\"", "");
        resultsString = resultsString.replaceAll("\"$", "");
        // include the quotes in the line separator so that line breaks in the data are ignored (not entirely reliable, but this is only a sample output)
        String[] resultsRows = resultsString.split("\"\\n\"");
        if (resultsRows.length > 0) {
            final String fieldSeparator = "\",\"";
//            final String fieldSeparator = ",";
            resultsHeader = resultsRows[0].split(fieldSeparator);
            tableData = new String[resultsRows.length - 1][resultsHeader.length];
//            tableData[0] = resultsHeader;
            boolean allRowsCorrectLength = true;
            for (int rowCounter = 1; rowCounter < resultsRows.length - 1; rowCounter++) {
                // add start and end quites so that the column count is correct 
                tableData[rowCounter] = ("\"" + resultsRows[rowCounter] + "\"").split(fieldSeparator);
                // trim the last and first quote 
                tableData[rowCounter][0] = tableData[rowCounter][0].replaceAll("^\"", "");
                tableData[rowCounter][tableData[rowCounter].length - 1] = tableData[rowCounter][tableData[rowCounter].length - 1].replaceAll("\"$", "");

                if (resultsHeader.length != tableData[rowCounter].length) {
                    allRowsCorrectLength = false;
                }
            }
            if (allRowsCorrectLength) {
                // todo: show error message
            }
        } else {
            tableData = new String[0][0];
        }
        resultsModel.fireTableStructureChanged();
    }
}
