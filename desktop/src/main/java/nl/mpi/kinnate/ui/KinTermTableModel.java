package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTerm;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;

/**
 *  Document   : KinTermTableModel
 *  Created on : Oct 17, 2011, 2:50:02 PM
 *  Author     : Peter Withers
 */
public class KinTermTableModel extends AbstractTableModel implements ActionListener {

    SavePanel savePanel;
    KinTermGroup kinTerms;
    HashSet<KinTerm> checkBoxSet = new HashSet<KinTerm>();
    String defaultKinType = "";
    JButton deleteSelectedButton;

    public KinTermTableModel(SavePanel savePanel, KinTermGroup kinTerms, JButton deleteSelectedButton) {
        this.savePanel = savePanel;
        this.kinTerms = kinTerms;
        this.deleteSelectedButton = deleteSelectedButton;
        deleteSelectedButton.setEnabled(false);
        deleteSelectedButton.addActionListener(this);
    }

    public void setDefaultKinType(String defaultKinType) {
        this.defaultKinType = defaultKinType;
        setValueAt(defaultKinType, kinTerms.getKinTerms().length, 1);
        fireTableCellUpdated(kinTerms.getKinTerms().length, 1);
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Kin Term";
            case 1:
                return "Referent Kin Type Strings";
//            case 2:
//                return "Propositus Kin Type Strings";
            case 2:
                return "Description";
            case 3:
                return "";
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 3:
                return Boolean.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
        return kinTerms.getKinTerms().length + 1;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (kinTerms.getKinTerms().length <= rowIndex) {
            switch (columnIndex) {
                case 1:
                    return defaultKinType;
                case 3:
                    return false;
                default:
                    return "";
            }
        }
        KinTerm kinTerm = kinTerms.getKinTerms()[rowIndex];
        switch (columnIndex) {
            case 0:
                return kinTerm.kinTerm;
            case 1:
                return kinTerm.alterKinTypeStrings;
//            case 2:
//                return kinTerm.propositusKinTypeStrings;
            case 2:
                return kinTerm.kinTermDescription;
            case 3:
                return checkBoxSet.contains(kinTerm);
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        KinTerm kinTerm;
        if (kinTerms.getKinTerms().length <= rowIndex) {
            switch (columnIndex) {
                case 3:
                    if (checkBoxSet.isEmpty()) {
                        checkBoxSet.addAll(Arrays.asList(kinTerms.getKinTerms()));
                    } else {
                        checkBoxSet.clear();
                    }
                    deleteSelectedButton.setEnabled(!checkBoxSet.isEmpty());
                    fireTableDataChanged();
                    return;
                case 1:
                    if (defaultKinType.equals(aValue)) {
                        // skip if the value is unchanged
                        return;
                    } // otherwise add a kin type via the default case below
                default:
                    if ("".equals(aValue)) {
                        // ignore if no text has been entered
                        return;
                    }
                    kinTerm = new KinTerm();
                    kinTerm.alterKinTypeStrings = defaultKinType;
                    kinTerms.addKinTerm(kinTerm);
            }
        } else {
            kinTerm = kinTerms.getKinTerms()[rowIndex];
        }
        switch (columnIndex) {
            case 0:
                kinTerm.kinTerm = aValue.toString();
                break;
            case 1:
                kinTerm.alterKinTypeStrings = aValue.toString();
                break;
//            case 2:
//                kinTerm.propositusKinTypeStrings = aValue.toString();
//                break;
            case 2:               
                kinTerm.kinTermDescription = aValue.toString();
                break;
            case 3:
                if ((Boolean) aValue) {
                    checkBoxSet.add(kinTerm);
                } else {
                    checkBoxSet.remove(kinTerm);
                }
                break;
            default:
                throw new UnsupportedOperationException("Too many columns");
        }
        deleteSelectedButton.setEnabled(!checkBoxSet.isEmpty());
        super.setValueAt(aValue, rowIndex, columnIndex);
        savePanel.updateGraph();
    }

    public void actionPerformed(ActionEvent e) {
        for (KinTerm kinTerm : checkBoxSet) {
            kinTerms.removeKinTerm(kinTerm);
        }
        checkBoxSet.clear();
        fireTableDataChanged();
        savePanel.updateGraph();
    }
}
