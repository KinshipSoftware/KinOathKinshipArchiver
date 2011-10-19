package nl.mpi.kinnate.ui.kintypeeditor;

import java.util.ArrayList;
import java.util.Arrays;
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
public class KinTypeTableModel extends AbstractTableModel {

    SavePanel savePanel;
    DataStoreSvg dataStoreSvg;

    public KinTypeTableModel(SavePanel savePanel, DataStoreSvg dataStoreSvg) {
        this.savePanel = savePanel;
        this.dataStoreSvg = dataStoreSvg;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public int getColumnCount() {
        return 4;
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
            default:
                throw new UnsupportedOperationException("Too many columns");
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
                default:
                    throw new UnsupportedOperationException("Too many columns");
            }
        } else {
            return ""; // add a blank row at the end
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String stringValue = aValue.toString();
        final KinType[] kinTypeDefinitions = dataStoreSvg.getKinTypeDefinitions();
        if (rowIndex < dataStoreSvg.getKinTypeDefinitions().length) {
            KinType kinType = kinTypeDefinitions[rowIndex];
            String codeString = kinType.getCodeString();
            RelationType relationType = kinType.getRelationType();
            SymbolType symbolType = kinType.getSymbolType();
            String displayString = kinType.getDisplayString();
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
                default:
                    throw new UnsupportedOperationException("Too many columns");
            }
            kinTypeDefinitions[rowIndex] = new KinType(codeString, relationType, symbolType, displayString);
            dataStoreSvg.setKinTypeDefinitions(kinTypeDefinitions);
        } else {
            String codeString = "undefined";
            RelationType relationType = DataTypes.RelationType.ancestor;
            SymbolType symbolType = EntityData.SymbolType.square;
            String displayString = "";
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
                default:
                    throw new UnsupportedOperationException("Too many columns");
            }
            ArrayList<KinType> kinTypesList = new ArrayList<KinType>(Arrays.asList(kinTypeDefinitions));
            kinTypesList.add(new KinType(codeString, relationType, symbolType, displayString));
            dataStoreSvg.setKinTypeDefinitions(kinTypesList.toArray(new KinType[]{}));
        }
        savePanel.updateGraph();
        savePanel.requiresSave();
        super.setValueAt(aValue, rowIndex, columnIndex);
    }
}
