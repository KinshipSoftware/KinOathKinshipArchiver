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
package nl.mpi.kinnate.ui.relationsettings;

import java.awt.Component;
import java.util.ResourceBundle;
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
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets");

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
