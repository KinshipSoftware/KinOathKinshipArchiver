package nl.mpi.kinnate.ui.entityprofiles;

import javax.swing.table.AbstractTableModel;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile;
import nl.mpi.kinnate.kindocument.ProfileManager;

/**
 * Document : ProfileTableModel
 * Created on : Jan 19, 2012, 4:57:20 PM
 * Author : Peter Withers
 */
public class ProfileTableModel extends AbstractTableModel {

    private String[] columnNames = new String[]{"Name", "Description", "Registration Date", "Creator Name", "Use Entity Type"}; //, "ID", "href"};
    private ProfileManager profileManager;

    public void setProfileManager(ProfileManager profileManager) {
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
        if (profileManager == null) {
            return 0;
        }
        return profileManager.getProfileCount();
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
            final CmdiProfile selectedProfile = profileManager.getProfileAt(rowIndex);
            if (aValue.equals(true)) {
                profileManager.addProfileSelection(selectedProfile.id, selectedProfile.name);
            } else {
                profileManager.removeProfileSelection(selectedProfile.id);
            }
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        final CmdiProfile selectedProfile = profileManager.getProfileAt(rowIndex);
        switch (columnIndex) {
            case 0:
                return selectedProfile.name;
            case 1:
                return selectedProfile.description;
            case 2:
                return selectedProfile.registrationDate.substring(0, 10);
            case 3:
                return selectedProfile.creatorName;
            case 4:
                return profileManager.profileIsSelected(selectedProfile.id);
//            case 4:
//                return cmdiProfileArray.get(rowIndex).id;
//            case 5:
//                return cmdiProfileArray.get(rowIndex).href;
            default:
                return "";
        }
    }
}
