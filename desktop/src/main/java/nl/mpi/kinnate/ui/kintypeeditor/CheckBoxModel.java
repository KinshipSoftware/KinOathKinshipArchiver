package nl.mpi.kinnate.ui.kintypeeditor;

import java.util.ArrayList;

/**
 * Document : CheckBoxModel
 * Created on : Jun 14, 2012, 12:04:00 PM
 * Author : Peter Withers
 */
public interface CheckBoxModel {

    public void setListValueAt(ArrayList<String> valuesList, int rowIndex, int columnIndex);

    public void setValueAt(Object aValue, int rowIndex, int columnIndex);

    public ArrayList<String> getValueRangeAt(int columnIndex);
}
