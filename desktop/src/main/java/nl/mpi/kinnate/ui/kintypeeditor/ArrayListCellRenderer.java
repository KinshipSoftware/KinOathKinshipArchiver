package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *  Document   : ArrayListCellRenderer
 *  Created on : Jan 12, 2012, 5:46:19 PM
 *  Author     : Peter Withers
 */
public class ArrayListCellRenderer implements TableCellRenderer {

    static protected String anyOptionDisplayString = "<any>";

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel labelRenderer;
        StringBuilder stringBuilder = new StringBuilder();
        if (row == table.getRowCount() - 1) {
            labelRenderer = new JLabel();
        } else if (value == null) {
            labelRenderer = new JLabel(anyOptionDisplayString);
        } else {
            for (String stringValue : (ArrayList<String>) value) {
                stringBuilder.append(stringValue);
                stringBuilder.append(", ");
            }
            labelRenderer = new JLabel(stringBuilder.toString());
        }
        labelRenderer.setOpaque(true);
        if (isSelected) {
            labelRenderer.setBackground(table.getSelectionBackground());
            labelRenderer.setForeground(table.getSelectionForeground());
        } else {
            labelRenderer.setBackground(table.getBackground());
            labelRenderer.setForeground(table.getForeground());
        }
        return labelRenderer;
    }
}
