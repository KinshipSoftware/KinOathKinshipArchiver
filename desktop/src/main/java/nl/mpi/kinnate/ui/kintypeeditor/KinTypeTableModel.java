package nl.mpi.kinnate.ui.kintypeeditor;

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
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityData.SymbolType;
import nl.mpi.kinnate.kintypestrings.KinType;
import nl.mpi.kinnate.svg.DataStoreSvg;

/**
 *  Document   : KinTypeTableModel
 *  Created on : Oct 18, 2011, 11:20:55 AM
 *  Author     : Peter Withers
 */
public class KinTypeTableModel extends AbstractTableModel implements ActionListener {

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
                return "Kin Type String";
            case 1:
                return "Relation Type";
            case 2:
                return "Symbol Type";
            case 3:
                return "Display Name";
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

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < dataStoreSvg.getKinTypeDefinitions().length) {
            KinType kinType = dataStoreSvg.getKinTypeDefinitions()[rowIndex];
            switch (columnIndex) {
                case 0:
                    return kinType.getCodeString();
                case 1:
                    return kinType.getRelationType();
                case 2:
                    return kinType.getSymbolType();
                case 3:
                    return kinType.getDisplayString();
                case 4:
                    return checkBoxSet.contains(kinType);
                default:
                    throw new UnsupportedOperationException("Too many columns");
            }
        } else {
            switch (columnIndex) {
                case 4:
                    return false;
                default:
                    return ""; // add a blank row at the end
            }
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String stringValue = aValue.toString();
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
        String codeString = "undefined";
        RelationType relationType = DataTypes.RelationType.ancestor;
        SymbolType symbolType = EntityData.SymbolType.square;
        String displayString = "";
        KinType kinType = null;
        if (rowIndex < dataStoreSvg.getKinTypeDefinitions().length) {
            kinType = kinTypeDefinitions[rowIndex];
            codeString = kinType.getCodeString();
            relationType = kinType.getRelationType();
            symbolType = kinType.getSymbolType();
            displayString = kinType.getDisplayString();
        }
        switch (columnIndex) {
            case 0:
                codeString = stringValue;
                break;
            case 1:
                relationType = DataTypes.RelationType.valueOf(stringValue);
                break;
            case 2:
                symbolType = EntityData.SymbolType.valueOf(stringValue);
                break;
            case 3:
                displayString = stringValue;
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
