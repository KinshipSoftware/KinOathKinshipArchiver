package nl.mpi.kinnate.ui.relationsettings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.ui.kintypeeditor.ArrayListCellRenderer;
import nl.mpi.kinnate.ui.kintypeeditor.CheckBoxRenderer;

/**
 * Document : RelationSettingsPanel
 * Created on : Jan 2, 2012, 1:30:21 PM
 * Author : Peter Withers
 */
public class RelationSettingsPanel extends JPanel implements ActionListener {

    private AbstractColorChooserPanel colourPickerPanel;
    private DataStoreSvg dataStoreSvg;
    private RelationTypesTableModel relationTypesTableModel;
    private ArbilWindowManager dialogHandler;
    private final String Scan_For_Types = "Scan For Types";

    public RelationSettingsPanel(String panelName, SavePanel savePanel, DataStoreSvg dataStoreSvg, ArbilWindowManager dialogHandler) {
        this.dataStoreSvg = dataStoreSvg;
        this.dialogHandler = dialogHandler;
        this.setName(panelName);
        this.setLayout(new BorderLayout());
        final JButton deleteButton = new JButton("Delete Selected");
        final JButton scanButton = new JButton(Scan_For_Types);
        final JColorChooser colourChooser = new JColorChooser();
        colourPickerPanel = colourChooser.getChooserPanels()[0];
        scanButton.setEnabled(true);
        scanButton.setActionCommand("scan");
        scanButton.addActionListener(this);
        relationTypesTableModel = new RelationTypesTableModel(savePanel, dataStoreSvg, deleteButton);
        final JTable kinTypeTable = new JTable(relationTypesTableModel) {

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
            // todo: resolve issue with the jcombobox consuming the mouse clicks before the colour picker gets them, might be better to get rid of the jcombobox
//            @Override
//            public TableCellEditor getCellEditor(int row, int column) {
//                if (column == 3 && row < this.getRowCount() - 1) {
//                    final JComboBox comboBox = new JComboBox(new String[]{""});
//                    comboBox.addActionListener(RelationSettingsPanel.this); // requires ActionListener to be implemented in RelationSettingsPanel
//                    comboBox.setRenderer(new ListCellRenderer() {
//
//                        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                            return colourPickerPanel;
//                        }
//                    });
//                    return new DefaultCellEditor(comboBox);
//                } else {
//                    return super.getCellEditor(row, column);
//                }
//            }
        };
        TableColumn columnRelationType = kinTypeTable.getColumnModel().getColumn(2);
        final JComboBox relationTypeComboBox = new JComboBox(relationTypesTableModel.getValueRangeAt(2).toArray());
        final CheckBoxRenderer relationTypeCheckBoxRenderer = new CheckBoxRenderer(relationTypesTableModel, relationTypeComboBox);
        relationTypeComboBox.addActionListener(relationTypeCheckBoxRenderer);
        relationTypeComboBox.setRenderer(relationTypeCheckBoxRenderer);
        columnRelationType.setCellEditor(relationTypeCheckBoxRenderer);
        columnRelationType.setCellRenderer(new ArrayListCellRenderer());

        kinTypeTable.getColumnModel().getColumn(4).setCellEditor(new NumberSpinnerEditor(relationTypesTableModel));
        kinTypeTable.getColumnModel().getColumn(5).setCellEditor(new NumberSpinnerEditor(relationTypesTableModel));

        TableColumn columnCurveLineOrientation = kinTypeTable.getColumnModel().getColumn(6);
        JComboBox comboBoxCurveLineOrientation = new JComboBox();
        for (RelationTypeDefinition.CurveLineOrientation curveLineOrientation : RelationTypeDefinition.CurveLineOrientation.values()) {
            comboBoxCurveLineOrientation.addItem(curveLineOrientation.name());
        }
        columnCurveLineOrientation.setCellEditor(new DefaultCellEditor(comboBoxCurveLineOrientation));

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
        buttonPanel.add(scanButton, BorderLayout.PAGE_END);
        buttonPanel.add(new JScrollPane(colourPickerPanel), BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.LINE_END);
    }

    public void actionPerformed(ActionEvent e) {
        if ("scan".equals(e.getActionCommand())) {
            int initalTypeCount = dataStoreSvg.getRelationTypeDefinitions().length;
            for (EntityData entityData : dataStoreSvg.graphData.getDataNodes()) {
                for (EntityRelation entityRelation : entityData.getAllRelations()) {
                    dataStoreSvg.addRelationTypeDefinition(new RelationTypeDefinition(entityRelation.customType, entityRelation.dcrType, new DataTypes.RelationType[]{entityRelation.getRelationType()}, "#999999", 2, 0, RelationTypeDefinition.CurveLineOrientation.horizontal));
                }
            }
            final int foundTypesCount = dataStoreSvg.getRelationTypeDefinitions().length - initalTypeCount;
            if (foundTypesCount > 0) {
                relationTypesTableModel.fireTableDataChanged();
                dialogHandler.addMessageDialogToQueue("Added " + foundTypesCount + " new types from the diagram", Scan_For_Types);
            } else {
                dialogHandler.addMessageDialogToQueue("No new types found on the diagram", Scan_For_Types);
            }
        }
    }
}
