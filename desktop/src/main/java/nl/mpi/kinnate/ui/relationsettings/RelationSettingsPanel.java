package nl.mpi.kinnate.ui.relationsettings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
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
        final JButton deleteButton = new JButton("Delete Selected");
        final JColorChooser colourChooser = new JColorChooser();
        final AbstractColorChooserPanel colourPickerPanel = colourChooser.getChooserPanels()[0];
        final JTable kinTypeTable = new JTable(new RelationTypesTableModel(savePanel, dataStoreSvg, deleteButton)) {

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 3 && row < this.getRowCount() - 1) {
                    final DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {

                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            JPanel panel = new JPanel();
                            try {
                                panel.setBackground(Color.decode(value.toString()));
                            } catch (NumberFormatException exception) {
                            }
                            return panel;
                        }
                    };
                    return cellRenderer;
                }
                final TableCellRenderer standardCellRenderer = super.getCellRenderer(row, column);
                return standardCellRenderer;
            }
        };
        TableColumn columnRelationType = kinTypeTable.getColumnModel().getColumn(2);

        JComboBox comboBoxRelationType = new JComboBox();
        for (DataTypes.RelationType relationType : DataTypes.RelationType.values()) {
            comboBoxRelationType.addItem(relationType);
        }
        columnRelationType.setCellEditor(new DefaultCellEditor(comboBoxRelationType));
        colourChooser.setPreviewPanel(new JPanel());
        colourChooser.getSelectionModel().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                for (int selectedRow : kinTypeTable.getSelectedRows()) {
                    if (selectedRow < kinTypeTable.getRowCount() - 1) {
                        kinTypeTable.getModel().setValueAt("#" + Integer.toHexString(colourChooser.getColor().getRGB()).substring(2), selectedRow, 3);
                    }
                }
            }
        });
        this.add(new JScrollPane(kinTypeTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(deleteButton, BorderLayout.PAGE_START);
        buttonPanel.add(colourPickerPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.LINE_END);
    }
}
