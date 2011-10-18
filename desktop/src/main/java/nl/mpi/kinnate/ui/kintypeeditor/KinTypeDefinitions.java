package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.BorderLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.svg.DataStoreSvg;

/**
 *  Document   : KinTypeDefinitions
 *  Created on : Oct 18, 2011, 11:13:08 AM
 *  Author     : Peter Withers
 */
public class KinTypeDefinitions extends JPanel {

    public KinTypeDefinitions(String panelName, SavePanel savePanel, DataStoreSvg dataStoreSvg) {
        this.setName(panelName);
        this.setLayout(new BorderLayout());
        JTable kinTypeTable = new JTable(new KinTypeTableModel(savePanel, dataStoreSvg));

        TableColumn columnRelationType = kinTypeTable.getColumnModel().getColumn(1);
        TableColumn columnSymbolType = kinTypeTable.getColumnModel().getColumn(2);

        JComboBox comboBoxRelationType = new JComboBox();
        for (EntityData.SymbolType symbolType : EntityData.SymbolType.values()) {
            comboBoxRelationType.addItem(symbolType);
        }
        columnRelationType.setCellEditor(new DefaultCellEditor(comboBoxRelationType));

        JComboBox comboBoxSymbolType = new JComboBox();
        for (DataTypes.RelationType relationType : DataTypes.RelationType.values()) {
            comboBoxSymbolType.addItem(relationType);
        }
        columnSymbolType.setCellEditor(new DefaultCellEditor(comboBoxSymbolType));

        this.add(new JScrollPane(kinTypeTable), BorderLayout.CENTER);
    }
}
