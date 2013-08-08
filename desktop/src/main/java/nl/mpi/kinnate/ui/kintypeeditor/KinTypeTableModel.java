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
package nl.mpi.kinnate.ui.kintypeeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityData.SymbolType;
import nl.mpi.kinnate.kintypestrings.KinType;
import nl.mpi.kinnate.svg.DataStoreSvg;

/**
 *  Document   : KinTypeTableModel
 *  Created on : Oct 18, 2011, 11:20:55 AM
 *  Author     : Peter Withers
 */
public class KinTypeTableModel extends AbstractTableModel implements ActionListener, CheckBoxModel {
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets");

    SavePanel savePanel;
    DataStoreSvg dataStoreSvg;
    HashSet<KinType> checkBoxSet = new HashSet<KinType>();
    JButton deleteSelectedButton;

    public KinTypeTableModel(SavePanel savePanel, DataStoreSvg dataStoreSvg, JButton deleteSelectedButton) {
        this.savePanel = savePanel;
        this.dataStoreSvg = dataStoreSvg;
        this.deleteSelectedButton = deleteSelectedButton;
        deleteSelectedButton.setEnabled(false);
        deleteSelectedButton.addActionListener(this);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return widgets.getString("KinTypeTable_KIN TYPE STRING");
            case 1:
                return widgets.getString("KinTypeTable_RELATION TYPE");
            case 2:
                return widgets.getString("KinTypeTable_SYMBOL TYPE");
            case 3:
                return widgets.getString("KinTypeTable_DISPLAY NAME");
            case 4:
                return "";
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 4:
                return Boolean.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    public int getRowCount() {
        return dataStoreSvg.getKinTypeDefinitions().length + 1;
    }

    public ArrayList<String> getValueRangeAt(int columnIndex) {
        ArrayList<String> valuesList = new ArrayList<String>();
        switch (columnIndex) {
            case 0:
                throw new UnsupportedOperationException("Not a list row type");
            case 1:
                for (DataTypes.RelationType relationType : DataTypes.RelationType.values()) {
                    valuesList.add(relationType.name());
                }
                break;
            case 2:
                for (EntityData.SymbolType symbolType : EntityData.SymbolType.values()) {
                    valuesList.add(symbolType.name());
                }
                break;
            default:
                throw new UnsupportedOperationException("Not a list row type");
        }
        return valuesList;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < dataStoreSvg.getKinTypeDefinitions().length) {
            KinType kinType = dataStoreSvg.getKinTypeDefinitions()[rowIndex];
            switch (columnIndex) {
                case 0:
                    return kinType.getCodeString();
                case 1:
                    final RelationType[] relationTypes = kinType.getRelationTypes();
                    if (relationTypes == null) {
                        return null;
                    }
                    ArrayList<String> valuesList = new ArrayList<String>();
                    for (DataTypes.RelationType relationType : relationTypes) {
                        valuesList.add(relationType.name());
                    }
                    return valuesList;
                case 2:
                    final SymbolType[] symbolTypes = kinType.getSymbolTypes();
                    if (symbolTypes == null) {
                        return null;
                    }
                    ArrayList<String> valuesList1 = new ArrayList<String>();
                    for (EntityData.SymbolType symbolType : symbolTypes) {
                        valuesList1.add(symbolType.name());
                    }
                    return valuesList1;
                case 3:
                    return kinType.getDisplayString();
                case 4:
                    return checkBoxSet.contains(kinType);
                default:
                    throw new UnsupportedOperationException("Too many columns");
            }
        } else {
            switch (columnIndex) {
                case 1:
                    return null;
                case 2:
                    return null;
                case 4:
                    return false;
                default:
                    return ""; // add a blank row at the end
            }
        }
    }

    public void setListValueAt(ArrayList<String> valuesList, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                throw new UnsupportedOperationException("Not a list type");
            case 1:
                ArrayList<DataTypes.RelationType> relationTypeList = new ArrayList<RelationType>();
                for (String stringValue : valuesList) {
                    relationTypeList.add(DataTypes.RelationType.valueOf(stringValue));
                }
                setValueAt(relationTypeList.toArray(new DataTypes.RelationType[]{}), rowIndex, columnIndex);
                break;
            case 2:
                ArrayList<EntityData.SymbolType> symbolTypeList = new ArrayList<EntityData.SymbolType>();
                for (String stringValue : valuesList) {
                    symbolTypeList.add(EntityData.SymbolType.valueOf(stringValue));
                }
                setValueAt(symbolTypeList.toArray(new EntityData.SymbolType[]{}), rowIndex, columnIndex);
                break;
            default:
                throw new UnsupportedOperationException("Not a list type");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        final KinType[] kinTypeDefinitions = dataStoreSvg.getKinTypeDefinitions();
        if (rowIndex >= dataStoreSvg.getKinTypeDefinitions().length && columnIndex == 4) {
            if (checkBoxSet.isEmpty()) {
                checkBoxSet.addAll(Arrays.asList(kinTypeDefinitions));
            } else {
                checkBoxSet.clear();
            }
            deleteSelectedButton.setEnabled(!checkBoxSet.isEmpty());
            fireTableDataChanged();
            return;
        }
        String codeString = widgets.getString("KinTypeTable_UNDEFINED");
        RelationType[] relationType = null;
        SymbolType[] symbolType = null;
        String displayString = "";
        KinType kinType = null;
        if (rowIndex < dataStoreSvg.getKinTypeDefinitions().length) {
            kinType = kinTypeDefinitions[rowIndex];
            codeString = kinType.getCodeString();
            relationType = kinType.getRelationTypes();
            symbolType = kinType.getSymbolTypes();
            displayString = kinType.getDisplayString();
        }
        switch (columnIndex) {
            case 0:
                codeString = aValue.toString();
                break;
            case 1:
                // this will only be set by setValueAt(ArrayList<String>)
                if (aValue instanceof DataTypes.RelationType[]) {
                    relationType = (DataTypes.RelationType[]) aValue;
                } else if (aValue == null) {
                    relationType = null;
                }
                break;
            case 2:
                // this will only be set by setValueAt(ArrayList<String>)
                if (aValue instanceof EntityData.SymbolType[]) {
                    symbolType = (EntityData.SymbolType[]) aValue;
                } else if (aValue == null) {
                    symbolType = null;
                }
                break;
            case 3:
                displayString = aValue.toString();
                break;
            case 4:
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
        if (rowIndex < dataStoreSvg.getKinTypeDefinitions().length) {
            kinTypeDefinitions[rowIndex] = new KinType(codeString, relationType, symbolType, displayString);
            dataStoreSvg.setKinTypeDefinitions(kinTypeDefinitions);
        } else {
            if ("".equals(aValue)) {
                // ignore if no text has been entered
                return;
            }
            ArrayList<KinType> kinTypesList = new ArrayList<KinType>(Arrays.asList(kinTypeDefinitions));
            kinTypesList.add(new KinType(codeString, relationType, symbolType, displayString));
            dataStoreSvg.setKinTypeDefinitions(kinTypesList.toArray(new KinType[]{}));
        }
        savePanel.updateGraph();
        savePanel.requiresSave();
        super.setValueAt(aValue, rowIndex, columnIndex);
    }

    public void actionPerformed(ActionEvent e) {
        ArrayList<KinType> kinTypesList = new ArrayList<KinType>(Arrays.asList(dataStoreSvg.getKinTypeDefinitions()));
        for (KinType kinType : checkBoxSet) {
            kinTypesList.remove(kinType);
        }
        dataStoreSvg.setKinTypeDefinitions(kinTypesList.toArray(new KinType[]{}));
        checkBoxSet.clear();
        fireTableDataChanged();
        savePanel.updateGraph();
    }
}
