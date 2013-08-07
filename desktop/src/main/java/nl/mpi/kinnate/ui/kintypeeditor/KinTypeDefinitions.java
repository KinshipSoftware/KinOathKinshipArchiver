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
package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.svg.DataStoreSvg;

/**
 *  Document   : KinTypeDefinitions
 *  Created on : Oct 18, 2011, 11:13:08 AM
 *  Author     : Peter Withers
 */
public class KinTypeDefinitions extends JPanel {
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets");

    public KinTypeDefinitions(String panelName, SavePanel savePanel, DataStoreSvg dataStoreSvg) {
        this.setName(panelName);
        this.setLayout(new BorderLayout());
        JButton deleteButton = new JButton(widgets.getString("DELETE SELECTED"));
//        JButton insertDefaultTypesButton = new JButton("Insert Defaults");
//        JButton insertOtherTypesButton = new JButton("Insert Other");
        final KinTypeTableModel kinTypeTableModel = new KinTypeTableModel(savePanel, dataStoreSvg, deleteButton);
        JTable kinTypeTable = new JTable(kinTypeTableModel) {

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                final JComponent preparedRenderer = (JComponent) super.prepareRenderer(renderer, row, column);
                if (row == getRowCount() - 1 && column != getColumnCount() - 1) {
                    // add a border to the last row (that is empty and used to create new records), to alert the user that it exists
                    preparedRenderer.setBorder(new MatteBorder(1, 1, 1, 1, Color.lightGray));
                }
                return preparedRenderer;
            }
        };
        TableColumn columnRelationType = kinTypeTable.getColumnModel().getColumn(1);
        final JComboBox relationTypeComboBox = new JComboBox(kinTypeTableModel.getValueRangeAt(1).toArray());
        final CheckBoxRenderer relationTypeCheckBoxRenderer = new CheckBoxRenderer(kinTypeTableModel, relationTypeComboBox);
        relationTypeComboBox.addActionListener(relationTypeCheckBoxRenderer);
        relationTypeComboBox.setRenderer(relationTypeCheckBoxRenderer);
        columnRelationType.setCellEditor(relationTypeCheckBoxRenderer);
        columnRelationType.setCellRenderer(new ArrayListCellRenderer());

        TableColumn columnSymbolType = kinTypeTable.getColumnModel().getColumn(2);
        final JComboBox symbolTypeComboBox = new JComboBox(kinTypeTableModel.getValueRangeAt(2).toArray());
        final CheckBoxRenderer symbolTypeCheckBoxRenderer = new CheckBoxRenderer(kinTypeTableModel, symbolTypeComboBox);
        symbolTypeComboBox.addActionListener(symbolTypeCheckBoxRenderer);
        symbolTypeComboBox.setRenderer(symbolTypeCheckBoxRenderer);
        columnSymbolType.setCellEditor(symbolTypeCheckBoxRenderer);
        columnSymbolType.setCellRenderer(new ArrayListCellRenderer());

        this.add(new JScrollPane(kinTypeTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(deleteButton, BorderLayout.PAGE_START);
        this.add(buttonPanel, BorderLayout.LINE_END);
    }
}
