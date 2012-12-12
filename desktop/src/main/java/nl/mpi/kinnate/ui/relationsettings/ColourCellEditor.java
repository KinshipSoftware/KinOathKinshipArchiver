/**
 * Copyright (C) 2012 The Language Archive
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
package nl.mpi.kinnate.ui.relationsettings;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Document : ColourCellEditor
 * Created on : Aug 9, 2012, 11:17:16 AM
 * Author : Peter Withers
 */
public class ColourCellEditor extends DefaultCellEditor implements ChangeListener {

    private AbstractColorChooserPanel colourPickerPanel;
    final JColorChooser colourChooser;
    final JPopupMenu popupMenu;
//    final private JTable table;
    final JPanel editorPanel;
//    private boolean isEditable = false;
    private String cellValue = null;
    private Color currentColour = null;

    public ColourCellEditor(final JTable table) {
        super(new JTextField());
//        this.table = table;
        editorPanel = new JPanel();
        colourChooser = new JColorChooser();
        colourPickerPanel = colourChooser.getChooserPanels()[0];
        popupMenu = new JPopupMenu();
        popupMenu.add(colourPickerPanel);
        colourChooser.setPreviewPanel(new JPanel());
        colourChooser.getSelectionModel().addChangeListener(this);


        editorPanel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
//                if (isEditable) {
                popupMenu.show(table, e.getX(), e.getY());
//                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
    }

    @Override
    public Object getCellEditorValue() {
        return cellValue;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        System.out.println("getTableCellEditorComponent");
//        this.table = table;
//        isEditable = row < table.getRowCount() - 1;
//        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        try {
            currentColour = Color.decode(value.toString());
            editorPanel.setBackground(currentColour);
            cellValue = value.toString();
        } catch (NumberFormatException exception) {
//            cellValue = "";
            currentColour = Color.GRAY;
            editorPanel.setBackground(currentColour);
            cellValue = "#" + Integer.toHexString(Color.GRAY.getRGB()).substring(2);
        }
        colourChooser.setColor(currentColour);
        return editorPanel;
    }

    public void stateChanged(ChangeEvent e) {
        if (colourChooser.getColor() != null && currentColour != colourChooser.getColor()) {
            final Color selectedColor = colourChooser.getColor();
            cellValue = "#" + Integer.toHexString(selectedColor.getRGB()).substring(2);
            editorPanel.setBackground(selectedColor);
            colourChooser.setColor(null);
            popupMenu.setVisible(false);
        }
    }
}
