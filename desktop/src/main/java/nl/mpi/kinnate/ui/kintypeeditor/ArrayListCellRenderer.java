/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.Component;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Document : ArrayListCellRenderer
 * Created on : Jan 12, 2012, 5:46:19 PM
 * Author : Peter Withers
 */
public class ArrayListCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets");
    static protected String anyOptionDisplayString = widgets.getString("<ANY>");

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
