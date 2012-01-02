package nl.mpi.kinnate.ui.relationsettings;

import java.awt.BorderLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.svg.DataStoreSvg;

/**
 *  Document   : RelationSettingsPanel
 *  Created on : Jan 2, 2012, 1:30:21 PM
 *  Author     : Peter Withers
 */
public class RelationSettingsPanel extends JPanel {

    public RelationSettingsPanel(String panelName, SavePanel savePanel, DataStoreSvg dataStoreSvg) {
        this.setName(panelName);
        this.setLayout(new BorderLayout());
        JButton deleteButton = new JButton("Delete Selected");
        JTable kinTypeTable = new JTable(new RelationTypesTableModel(savePanel, dataStoreSvg, deleteButton));

        TableColumn columnRelationType = kinTypeTable.getColumnModel().getColumn(1);
        TableColumn columnLineType = kinTypeTable.getColumnModel().getColumn(2);

        JComboBox comboBoxRelationType = new JComboBox();
        for (DataTypes.RelationType relationType : DataTypes.RelationType.values()) {
            comboBoxRelationType.addItem(relationType);
        }
        columnRelationType.setCellEditor(new DefaultCellEditor(comboBoxRelationType));

        JComboBox comboBoxLineType = new JComboBox();
        for (DataTypes.RelationLineType relationLineType : DataTypes.RelationLineType.values()) {
            comboBoxLineType.addItem(relationLineType);
        }
        columnLineType.setCellEditor(new DefaultCellEditor(comboBoxLineType));
        this.add(new JScrollPane(kinTypeTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(deleteButton, BorderLayout.PAGE_START);
        this.add(buttonPanel, BorderLayout.LINE_END);
    }
}
