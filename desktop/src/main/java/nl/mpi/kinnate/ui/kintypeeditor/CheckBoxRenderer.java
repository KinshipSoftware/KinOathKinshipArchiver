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

    private KinTypeTableModel kinTypeTableModel;
    private JComboBox comboBoxRelationType;
    private ArrayList<String> selectedItems;
    private int checkBoxRow;
    private int checkBoxColumn;

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
        final JCheckBox editorCheckBox = new JCheckBox(value.toString(), selectedItems.contains(value.toString()));
        editorCheckBox.addActionListener(this);
        return editorCheckBox;
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println(((JCheckBox) e.getSource()).isSelected());
        System.out.println(((JCheckBox) e.getSource()).getText());
    }
}
