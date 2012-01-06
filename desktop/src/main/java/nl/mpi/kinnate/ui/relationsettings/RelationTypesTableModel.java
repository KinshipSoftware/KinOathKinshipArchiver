package nl.mpi.kinnate.ui.relationsettings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.RelationTypeDefinition;
import nl.mpi.kinnate.svg.DataStoreSvg;

/**
 *  Document   : RelationTypesTableModel
 *  Created on : Jan 2, 2012, 2:05:08 PM
 *  Author     : Peter Withers
 */
public class RelationTypesTableModel extends AbstractTableModel implements ActionListener {

    SavePanel savePanel;
    DataStoreSvg dataStoreSvg;
    HashSet<RelationTypeDefinition> checkBoxSet = new HashSet<RelationTypeDefinition>();
    JButton deleteSelectedButton;

    public RelationTypesTableModel(SavePanel savePanel, DataStoreSvg dataStoreSvg, JButton deleteSelectedButton) {
        this.savePanel = savePanel;
        this.dataStoreSvg = dataStoreSvg;
        this.deleteSelectedButton = deleteSelectedButton;
        deleteSelectedButton.setEnabled(false);
        deleteSelectedButton.addActionListener(this);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 3; // prevent non colour data being entered into the colour field
    }

    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Display Name";
            case 1:
                return "Data Category";
            case 2:
                return "Relation Type";
            case 3:
                return "Line Colour";
            case 4:
                return "Line Width";
            case 5:
                return "Line Stye";
            case 6:
                return "";
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 6:
                return Boolean.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    public int getRowCount() {
        return dataStoreSvg.getRelationTypeDefinitions().length + 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < dataStoreSvg.getRelationTypeDefinitions().length) {
            RelationTypeDefinition kinType = dataStoreSvg.getRelationTypeDefinitions()[rowIndex];
            switch (columnIndex) {
                case 0:
                    return kinType.getDisplayName();
                case 1:
                    return kinType.getDataCategory();
                case 2:
                    return kinType.getRelationType();
                case 3:
                    return kinType.getLineColour();
                case 4:
                    return kinType.getLineWidth();
                case 5:
                    return kinType.getLineStye();
                case 6:
                    return checkBoxSet.contains(kinType);
                default:
                    throw new UnsupportedOperationException("Too many columns");
            }
        } else {
            switch (columnIndex) {
                case 6:
                    return false;
                default:
                    return ""; // add a blank row at the end
            }
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String stringValue = aValue.toString();
        final RelationTypeDefinition[] kinTypeDefinitions = dataStoreSvg.getRelationTypeDefinitions();
        if (rowIndex >= dataStoreSvg.getRelationTypeDefinitions().length && columnIndex == 6) {
            if (checkBoxSet.isEmpty()) {
                checkBoxSet.addAll(Arrays.asList(kinTypeDefinitions));
            } else {
                checkBoxSet.clear();
            }
            deleteSelectedButton.setEnabled(!checkBoxSet.isEmpty());
            fireTableDataChanged();
            return;
        }
        String displayName = "undefined";
        RelationType relationType = DataTypes.RelationType.ancestor;
        String dataCategory = "";
        String lineColour = "#999999";
        int lineWidth = 2;
        String lineStye = null;


        RelationTypeDefinition kinType = null;
        if (rowIndex < dataStoreSvg.getRelationTypeDefinitions().length) {
            kinType = kinTypeDefinitions[rowIndex];
            displayName = kinType.getDisplayName();
            relationType = kinType.getRelationType();
            dataCategory = kinType.getDataCategory();
            lineColour = kinType.getLineColour();
            lineWidth = kinType.getLineWidth();
            lineStye = kinType.getLineStye();
        }
        switch (columnIndex) {
            case 0:
                displayName = stringValue;
                break;
            case 1:
                dataCategory = stringValue;
                break;
            case 2:
                relationType = DataTypes.RelationType.valueOf(stringValue);
                break;
            case 3:
                lineColour = stringValue;
                fireTableCellUpdated(rowIndex, columnIndex); // update the colour in the modified table cell
                break;
            case 4:
                lineWidth = Integer.parseInt(stringValue.replaceAll("[^0-9]", ""));
                break;
            case 5:
                lineStye = stringValue;
                break;
            case 6:
                if ((Boolean) aValue) {
                    checkBoxSet.add(kinType);
                } else {
                    checkBoxSet.remove(kinType);
                }
                deleteSelectedButton.setEnabled(!checkBoxSet.isEmpty());
                fireTableDataChanged();
                return;
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
        if (rowIndex < dataStoreSvg.getRelationTypeDefinitions().length) {
            kinTypeDefinitions[rowIndex] = new RelationTypeDefinition(displayName, dataCategory, relationType, lineColour, lineWidth, lineStye);
            dataStoreSvg.setRelationTypeDefinitions(kinTypeDefinitions);
        } else {
            if ("".equals(aValue)) {
                // ignore if no text has been entered
                return;
            }
            ArrayList<RelationTypeDefinition> kinTypesList = new ArrayList<RelationTypeDefinition>(Arrays.asList(kinTypeDefinitions));
            kinTypesList.add(new RelationTypeDefinition(displayName, dataCategory, relationType, lineColour, lineWidth, lineStye));
            dataStoreSvg.setRelationTypeDefinitions(kinTypesList.toArray(new RelationTypeDefinition[]{}));
        }
        savePanel.updateGraph();
        savePanel.requiresSave();
        super.setValueAt(aValue, rowIndex, columnIndex);
    }

    public void actionPerformed(ActionEvent e) {
        ArrayList<RelationTypeDefinition> kinTypesList = new ArrayList<RelationTypeDefinition>(Arrays.asList(dataStoreSvg.getRelationTypeDefinitions()));
        for (RelationTypeDefinition kinType : checkBoxSet) {
            kinTypesList.remove(kinType);
        }
        dataStoreSvg.setRelationTypeDefinitions(kinTypesList.toArray(new RelationTypeDefinition[]{}));
        checkBoxSet.clear();
        fireTableDataChanged();
        savePanel.updateGraph();
    }
}
