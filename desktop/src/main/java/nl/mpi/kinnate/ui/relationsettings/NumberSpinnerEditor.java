package nl.mpi.kinnate.ui.relationsettings;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;

/**
 *  Document   : NumberSpinnerEditor
 *  Created on : Jan 17, 2012, 2:40:31 PM
 *  Author     : Peter Withers
 */
public class NumberSpinnerEditor extends DefaultCellEditor implements TableCellEditor, ChangeListener {

    private final JSpinner jSpinner;
    private RelationTypesTableModel relationTypesTableModel;
    private int row;
    private int column;

    public NumberSpinnerEditor(RelationTypesTableModel relationTypesTableModel) {
        super(new JTextField());
        this.relationTypesTableModel = relationTypesTableModel;
        jSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 100, 1));
        jSpinner.setBorder(null);
        jSpinner.addChangeListener(this);
    }

    @Override
    public Object getCellEditorValue() {
        return jSpinner.getValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.row = row;
        this.column = column;
        try {
            jSpinner.setValue(value);
        } catch (IllegalArgumentException argumentException) {
            jSpinner.setValue(0);
        }
        return jSpinner;
    }

    public void stateChanged(ChangeEvent e) {
        relationTypesTableModel.setValueAt(jSpinner.getValue(), row, column);
    }
}
