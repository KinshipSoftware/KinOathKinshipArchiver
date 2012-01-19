package nl.mpi.kinnate.ui.entityprofiles;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile;

/**
 *  Document   : ProfileTableModel
 *  Created on : Jan 19, 2012, 4:57:20 PM
 *  Author     : Peter Withers
 */
public class ProfileTableModel extends AbstractTableModel {

    private ArrayList<CmdiProfile> cmdiProfileArray = null;
    private String[] columnNames = new String[]{"Name", "Description", "Registration Date", "Creator Name", "ID", "href"};

    public void setCmdiProfileReader(CmdiProfileReader cmdiProfileReader) {
        cmdiProfileArray = cmdiProfileReader.cmdiProfileArray;
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

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return cmdiProfileArray.get(rowIndex).name;
            case 1:
                return cmdiProfileArray.get(rowIndex).description;
            case 2:
                return cmdiProfileArray.get(rowIndex).registrationDate;
            case 3:
                return cmdiProfileArray.get(rowIndex).creatorName;
            case 4:
                return cmdiProfileArray.get(rowIndex).id;
            case 5:
                return cmdiProfileArray.get(rowIndex).href;
            default:
                return "";
        }
    }
}
