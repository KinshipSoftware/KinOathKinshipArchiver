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
import nl.mpi.kinnate.kindata.RelationTypeDefinition.CurveLineOrientation;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.ui.kintypeeditor.CheckBoxModel;

/**
 * Document : RelationTypesTableModel
 * Created on : Jan 2, 2012, 2:05:08 PM
 * Author : Peter Withers
 */
public class RelationTypesTableModel extends AbstractTableModel implements ActionListener, CheckBoxModel {

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
//        return columnIndex != 3; // prevent non colour data being entered into the colour field
        return true;
    }

    public int getColumnCount() {
        return 8;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Custom Name";
            case 1:
                return "Data Category";
            case 2:
                return "Relation Type";
            case 3:
                return "Line Colour";
            case 4:
                return "Line Width";
            case 5:
                return "Line/Dash";
            case 6: // todo: add this to the relation definitions table
                return "Curve Line Orientation";
            case 7:
                return "";
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 7:
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
                    ArrayList<String> valuesList = new ArrayList<String>();
                    for (DataTypes.RelationType relationType : kinType.getRelationType()) {
                        valuesList.add(relationType.name());
                    }
                    return valuesList;
                case 3:
                    return kinType.getLineColour();
                case 4:
                    return kinType.getLineWidth();
                case 5:
                    return kinType.getLineDash();
                case 6:
                    return kinType.getCurveLineOrientation().name();
                case 7:
                    return checkBoxSet.contains(kinType);
                default:
                    throw new UnsupportedOperationException("Too many columns");
            }
        } else {
            switch (columnIndex) {
                case 7:
                    return false;
                default:
                    return ""; // add a blank row at the end
            }
        }
    }

    public ArrayList<String> getValueRangeAt(int columnIndex) {
        ArrayList<String> valuesList = new ArrayList<String>();
        switch (columnIndex) {
            case 0:
            case 1:
                throw new UnsupportedOperationException("Not a list row type");
            case 2:
                for (DataTypes.RelationType relationType : DataTypes.RelationType.values()) {
                    valuesList.add(relationType.name());
                }
                break;
            default:
                throw new UnsupportedOperationException("Not a list row type");
        }
        return valuesList;
    }

    public void setListValueAt(ArrayList<String> valuesList, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
                throw new UnsupportedOperationException("Not a list type");
            case 2:
                ArrayList<DataTypes.RelationType> relationTypeList = new ArrayList<RelationType>();
                for (String stringValue : valuesList) {
                    relationTypeList.add(DataTypes.RelationType.valueOf(stringValue));
                }
                setValueAt(relationTypeList.toArray(new DataTypes.RelationType[]{}), rowIndex, columnIndex);
                break;
            default:
                throw new UnsupportedOperationException("Not a list type");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        final RelationTypeDefinition[] kinTypeDefinitions = dataStoreSvg.getRelationTypeDefinitions();
        if (rowIndex >= dataStoreSvg.getRelationTypeDefinitions().length && columnIndex == 7) {
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
        RelationType[] relationType = new RelationType[0]; //{DataTypes.RelationType.ancestor, DataTypes.RelationType.descendant, DataTypes.RelationType.sibling, DataTypes.RelationType.union};
        String dataCategory = "";
        String lineColour = "#999999";
        int lineWidth = 2;
        int lineDash = 0;
        CurveLineOrientation curveLineOrientation = RelationTypeDefinition.CurveLineOrientation.horizontal;

        RelationTypeDefinition kinType = null;
        if (rowIndex < dataStoreSvg.getRelationTypeDefinitions().length) {
            kinType = kinTypeDefinitions[rowIndex];
            displayName = kinType.getDisplayName();
            relationType = kinType.getRelationType();
            dataCategory = kinType.getDataCategory();
            lineColour = kinType.getLineColour();
            lineWidth = kinType.getLineWidth();
            lineDash = kinType.getLineDash();
            curveLineOrientation = kinType.getCurveLineOrientation();
        }
        String stringValue = aValue.toString();
        switch (columnIndex) {
            case 0:
                displayName = stringValue;
                break;
            case 1:
                dataCategory = stringValue;
                break;
            case 2:
                // this will only be set by setValueAt(ArrayList<String>)
                if (aValue instanceof DataTypes.RelationType[]) {
                    relationType = (DataTypes.RelationType[]) aValue;
                } else if (aValue == null) {
                    relationType = null;
                }
                break;
            case 3:
                lineColour = stringValue;
                fireTableCellUpdated(rowIndex, columnIndex); // update the colour in the modified table cell
                break;
            case 4:
                lineWidth = Integer.parseInt(stringValue.replaceAll("[^0-9]", ""));
                break;
            case 5:
                lineDash = Integer.parseInt(stringValue.replaceAll("[^0-9]", ""));
                break;
            case 6:
                curveLineOrientation = RelationTypeDefinition.CurveLineOrientation.valueOf(stringValue);
                break;
            case 7:
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
            kinTypeDefinitions[rowIndex] = new RelationTypeDefinition(displayName, dataCategory, relationType, lineColour, lineWidth, lineDash, curveLineOrientation);
            dataStoreSvg.setRelationTypeDefinitions(kinTypeDefinitions);
        } else {
            if ("".equals(aValue)) {
                // ignore if no text has been entered
                return;
            }
            ArrayList<RelationTypeDefinition> kinTypesList = new ArrayList<RelationTypeDefinition>(Arrays.asList(kinTypeDefinitions));
            kinTypesList.add(new RelationTypeDefinition(displayName, dataCategory, relationType, lineColour, lineWidth, lineDash, curveLineOrientation));
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
