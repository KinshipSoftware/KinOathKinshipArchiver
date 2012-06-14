package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;

/**
 *  Document   : CheckBoxRenderer
 *  Created on : Jan 12, 2012, 4:01:07 PM
 *  Author     : Peter Withers
 */
public class CheckBoxRenderer extends DefaultCellEditor implements ListCellRenderer, TableCellEditor, ActionListener {

    private CheckBoxModel kinTypeTableModel;
    private JComboBox comboBoxRelationType;
    private ArrayList<String> selectedItems;
    private int row;
    private int column;

    public CheckBoxRenderer(CheckBoxModel kinTypeTableModel, JComboBox comboBoxRelationType) {
        super(comboBoxRelationType);
        this.kinTypeTableModel = kinTypeTableModel;
        this.comboBoxRelationType = comboBoxRelationType;
        comboBoxRelationType.addItem(ArrayListCellRenderer.anyOptionDisplayString);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        selectedItems = (ArrayList<String>) value;
        this.row = row;
        this.column = column;
        return comboBoxRelationType;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        boolean checkBoxSelected;
        if (selectedItems == null) {
            checkBoxSelected = ArrayListCellRenderer.anyOptionDisplayString.equals(value.toString());
        } else {
            checkBoxSelected = selectedItems.contains(value.toString());
        }
        final JCheckBox editorCheckBox = new JCheckBox(value.toString(), checkBoxSelected);
        editorCheckBox.addActionListener(this);
        return editorCheckBox;
    }

    public void actionPerformed(ActionEvent e) {
        String selectedItem = comboBoxRelationType.getSelectedItem().toString();
        if (ArrayListCellRenderer.anyOptionDisplayString.equals(selectedItem)) {
            if (selectedItems == null) {
                kinTypeTableModel.setListValueAt(new ArrayList<String>(), row, column);
            } else {
                kinTypeTableModel.setValueAt(null, row, column);
            }
        } else {
            if (selectedItems == null) {
                selectedItems = new ArrayList<String>();
            }
            if (selectedItems.contains(selectedItem)) {
                selectedItems.remove(selectedItem);
            } else {
                selectedItems.add(selectedItem);
            }
            kinTypeTableModel.setListValueAt(selectedItems, row, column);
        }
    }
}
