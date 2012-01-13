package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.Component;
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
public class CheckBoxRenderer extends DefaultCellEditor implements ListCellRenderer, TableCellEditor {

    KinTypeTableModel kinTypeTableModel;
    JComboBox comboBoxRelationType;
    ArrayList<String> selectedItems;

    public CheckBoxRenderer(KinTypeTableModel kinTypeTableModel, JComboBox comboBoxRelationType) {
        super(comboBoxRelationType);
        this.kinTypeTableModel = kinTypeTableModel;
        this.comboBoxRelationType = comboBoxRelationType;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        selectedItems = (ArrayList<String>) value;
        return comboBoxRelationType;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return new JCheckBox(value.toString(), selectedItems.contains(value.toString()));
    }
}
