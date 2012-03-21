package nl.mpi.kinnate.ui.entityprofiles;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile;
import nl.mpi.kinnate.kindocument.ProfileManager;

/**
 *  Document   : ProfileTableModel
 *  Created on : Jan 19, 2012, 4:57:20 PM
 *  Author     : Peter Withers
 */
public class ProfileTableModel extends AbstractTableModel {

    private ArrayList<CmdiProfile> cmdiProfileArray = null;
    private String[] columnNames = new String[]{"Name", "Description", "Registration Date", "Creator Name", "Use Entity Type"}; //, "ID", "href"};
    private ProfileManager profileManager;

    public void setCmdiProfileReader(CmdiProfileReader cmdiProfileReader, ProfileManager profileManager) {
        cmdiProfileArray = cmdiProfileReader.cmdiProfileArray;
        this.profileManager = profileManager;
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        if (cmdiProfileArray == null) {
            return 0;
        } else {
            return cmdiProfileArray.size();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 4);
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

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 4) {
            if (aValue.equals(true)) {
                profileManager.addProfileSelection(cmdiProfileArray.get(rowIndex).id, cmdiProfileArray.get(rowIndex).name);
            } else {
                profileManager.removeProfileSelection(cmdiProfileArray.get(rowIndex).id);
            }
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return cmdiProfileArray.get(rowIndex).name;
            case 1:
                return cmdiProfileArray.get(rowIndex).description;
            case 2:
                return cmdiProfileArray.get(rowIndex).registrationDate.substring(0, 10);
            case 3:
                return cmdiProfileArray.get(rowIndex).creatorName;
            case 4:
                return profileManager.profileIsSelected(cmdiProfileArray.get(rowIndex).id);
//            case 4:
//                return cmdiProfileArray.get(rowIndex).id;
//            case 5:
//                return cmdiProfileArray.get(rowIndex).href;
            default:
                return "";
        }
    }
}
