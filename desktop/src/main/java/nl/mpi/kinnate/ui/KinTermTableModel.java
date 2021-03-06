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
package nl.mpi.kinnate.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
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
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/kinoath/localisation/Widgets");

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
                return widgets.getString("KIN TERM");
            case 1:
                return widgets.getString("REFERENT KIN TYPE STRINGS");
            case 2:
                return widgets.getString("PROPOSITUS KIN TYPE STRINGS");
            case 3:
                return widgets.getString("DESCRIPTION");
            case 4:
                return "";
            default:
                throw new UnsupportedOperationException(widgets.getString("TOO MANY COLUMNS"));
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

    public int getColumnCount() {
        return 5;
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
                case 4:
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
            case 2:
                return kinTerm.propositusKinTypeStrings;
            case 3:
                return kinTerm.kinTermDescription;
            case 4:
                return checkBoxSet.contains(kinTerm);
            default:
                throw new UnsupportedOperationException(widgets.getString("TOO MANY COLUMNS"));
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        KinTerm kinTerm;
        if (kinTerms.getKinTerms().length <= rowIndex) {
            switch (columnIndex) {
                case 4:
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
            case 2:
                kinTerm.propositusKinTypeStrings = aValue.toString();
                break;
            case 3:               
                kinTerm.kinTermDescription = aValue.toString();
                break;
            case 4:
                if ((Boolean) aValue) {
                    checkBoxSet.add(kinTerm);
                } else {
                    checkBoxSet.remove(kinTerm);
                }
                break;
            default:
                throw new UnsupportedOperationException(widgets.getString("TOO MANY COLUMNS"));
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
