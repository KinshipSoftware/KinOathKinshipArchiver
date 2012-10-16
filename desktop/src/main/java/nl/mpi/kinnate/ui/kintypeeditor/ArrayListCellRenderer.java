package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Document : ArrayListCellRenderer
 * Created on : Jan 12, 2012, 5:46:19 PM
 * Author : Peter Withers
 */
public class ArrayListCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    static protected String anyOptionDisplayString = "<any>";

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        DefaultTableCellRenderer labelRenderer = this;
        StringBuilder stringBuilder = new StringBuilder();
        if (row == table.getRowCount() - 1) {
            labelRenderer.setText("");
        } else if (value == null) {
            labelRenderer.setText(anyOptionDisplayString);
        } else {
            for (String stringValue : (ArrayList<String>) value) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(stringValue);
            }
            labelRenderer.setText(stringBuilder.toString());
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
