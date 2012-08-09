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
    private JTable table;
    final JPanel editorPanel;
    private boolean isEditable = false;
    private String cellValue = null;

    public ColourCellEditor() {
        super(new JTextField());
        editorPanel = new JPanel();
        colourChooser = new JColorChooser();
        colourPickerPanel = colourChooser.getChooserPanels()[0];
        popupMenu = new JPopupMenu();
        popupMenu.add(colourPickerPanel);
        colourChooser.setPreviewPanel(new JPanel());
        colourChooser.getSelectionModel().addChangeListener(this);


        editorPanel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (isEditable) {
                    popupMenu.show(table, e.getX(), e.getY());
                }
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
        this.table = table;
        isEditable = row < table.getRowCount() - 1;
//        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        try {
            editorPanel.setBackground(Color.decode(value.toString()));
        } catch (NumberFormatException exception) {
        }
        return editorPanel;
    }

    public void stateChanged(ChangeEvent e) {
        if (colourChooser.getColor() != null) {
            final Color selectedColor = colourChooser.getColor();
            cellValue = "#" + Integer.toHexString(selectedColor.getRGB()).substring(2);
            editorPanel.setBackground(selectedColor);
        }
        colourChooser.setColor(null);
        popupMenu.setVisible(false);
    }
}
