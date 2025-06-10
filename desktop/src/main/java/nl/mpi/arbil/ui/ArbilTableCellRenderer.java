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
package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilTableCell;

/**
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableCellRenderer extends DefaultTableCellRenderer {
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    final ArbilIconCellRenderer arbilIconCellRenderer;

    public int getRequiredWidth(FontMetrics fontMetrics, ArbilTableCell cellObject) {
        Object cellContent = getCellContent(cellObject);
        String currentCellString = getText();
        // Calculate width of text
        int width = fontMetrics.stringWidth(currentCellString);
        // ArbilField might have an icon
        if (cellContent instanceof ArbilField) {
            Icon icon = ArbilIcons.getSingleInstance().getIconForField((ArbilField) cellContent);
            // If there's an icon, add its width
            if (icon != null) {
                width += icon.getIconWidth();
            }
        }
        return width;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ArbilTableCell cellObject = ((ArbilTableCell) value);
        String textValue;
        if (cellObject == null) {
            textValue = "";
        } else {
            textValue = cellObject.toString();
        }
        super.getTableCellRendererComponent(table, textValue, isSelected, hasFocus, row, column); // this is needed to set the default colours on the cell
        Object cellContent = getCellContent(cellObject);
        setColours(cellContent, isSelected);
        Icon leftIcon = getIcon(cellContent);
        Icon rightIcon = null;
        // ArbilField may have to be decorated with an icon
        if (cellContent instanceof ArbilField) {
            rightIcon = ArbilIcons.getSingleInstance().getIconForField((ArbilField) cellContent);
            // we cannot wrap the cell renderer because doing so will remove it from the table casusing rendering issues, so we just set the icon for the current label renderer
//                // An icon exists for this field, so wrap component with icon panel
//                return new ArbilIconCellPanel(this, icon);
        }
        arbilIconCellRenderer.setIcons(leftIcon, rightIcon);
        return arbilIconCellRenderer;
    }

    public ArbilTableCellRenderer() {
        arbilIconCellRenderer = new ArbilIconCellRenderer(this);
    }

    protected final void setColours(Object cellContent, boolean isSelected) {
        if (cellContent instanceof ArbilField[]) {
            // Multi-valued field
            int greyTone = 150;
            super.setForeground(new Color(greyTone, greyTone, greyTone));
        }
        if (!isSelected) {
            if (cellContent instanceof ArbilFieldPlaceHolder || cellContent instanceof String && "".equals(cellContent)) {
                // Field does not exist in node OR childs column and node has no children of this type
                super.setBackground(new Color(230, 230, 230)/* Color.lightGray */);
            } else if (cellContent instanceof ArbilField && ((ArbilField) cellContent).fieldNeedsSaveToDisk()) {
                // Value has changed since last save
                super.setForeground(Color.blue);
            }
            if (cellContent instanceof ArbilField && ((ArbilField) cellContent).isRequiredField() && ((ArbilField) cellContent).getFieldValue().length() == 0) {
                super.setForeground(Color.RED);
            } else if (cellContent instanceof ArbilField && !((ArbilField) cellContent).fieldValueValidates()) {
                super.setForeground(Color.RED);
            }
        }
    }

    public Icon getIcon(Object cellContent) {
        if (cellContent instanceof ArbilDataNode) {
            return (((ArbilDataNode) cellContent).getIcon());
        } else if (cellContent instanceof ArbilDataNode[]) {
            return (ArbilIcons.getSingleInstance().getIconForNode((ArbilDataNode[]) cellContent));
        } else if (cellContent instanceof ArbilField[]) {
            return null;
//        } else if (cellObject instanceof ArbilField) {
//            return ArbilIcons.getSingleInstance().getIconForVocabulary((ArbilField) cellObject);
        } else {
            return (null);
        }
    }

    /**
     * @return the cellObject
     */
    private Object getCellContent(ArbilTableCell cellObject) {
        if (cellObject == null) {
            return null;
        } else {
            return cellObject.getContent();
        }
    }
}