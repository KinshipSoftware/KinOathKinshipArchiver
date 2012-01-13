package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *  Document   : ListCellRenderer
 *  Created on : Jan 12, 2012, 5:46:19 PM
 *  Author     : Peter Withers
 */
public class ListCellRenderer implements TableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String stringValue : (ArrayList<String>) value) {
            stringBuilder.append(stringValue);
            stringBuilder.append(" ");
        }
        return new JLabel(stringBuilder.toString());
    }
}
