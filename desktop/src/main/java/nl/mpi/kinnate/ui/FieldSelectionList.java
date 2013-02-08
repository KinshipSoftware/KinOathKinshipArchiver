/**
 * Copyright (C) 2012 The Language Archive
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.IndexerParam;
import nl.mpi.kinnate.entityindexer.ParameterElement;

/**
 * Document : FieldSelectionList Created on : Feb 11, 2011, 2:05:55 PM
 *
 * @author Peter Withers
 */
public class FieldSelectionList extends JPanel {

    private SavePanel savePanel;
    private JPanel paddingPanel;
    protected IndexerParam indexerParam;
    final private EntityCollection entityCollection;

    public FieldSelectionList(EntityCollection entityCollection, SavePanel savePanelLocal, IndexerParam indexerParamLocal, TableCellDragHandler tableCellDragHandler) {
        this.entityCollection = entityCollection;
        // keep the panel items pushed to the top of the page
        paddingPanel = new JPanel();
        this.setLayout(new BorderLayout());
        this.add(paddingPanel, BorderLayout.PAGE_START);
        savePanel = savePanelLocal;
        indexerParam = indexerParamLocal;
        populateSelectionList();
        this.setTransferHandler(tableCellDragHandler);
    }

    private void populateSelectionList() {
        paddingPanel.setLayout(new BoxLayout(paddingPanel, BoxLayout.PAGE_AXIS));
        paddingPanel.removeAll();
        for (final ParameterElement parameterElement : indexerParam.getValues()) {
            final JTextField fieldPathLabel = new JTextField(parameterElement.getXpathString());
            fieldPathLabel.setBackground(paddingPanel.getBackground());
            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
            fieldPanel.add(fieldPathLabel);
            fieldPanel.add(new JPanel());
            if (parameterElement.hasSelectedValue()) {
                String[] availableValues = indexerParam.getAvailableValues();
                if (availableValues != null) {
                    JComboBox valueSelect = new JComboBox(availableValues);
                    valueSelect.setSelectedItem(parameterElement.getSelectedValue());
                    valueSelect.setActionCommand(parameterElement.getXpathString());
                    fieldPanel.add(valueSelect);
                    valueSelect.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            parameterElement.setSelectedValue(((JComboBox) evt.getSource()).getSelectedItem().toString());
                            indexerParam.setChangedFlag();
//                            indexerParam.setValue(evt.getActionCommand(), ((JComboBox) evt.getSource()).getSelectedItem().toString());
                            populateSelectionList();
                            revalidate();
                            savePanel.updateGraph();
                            savePanel.requiresSave();
                        }
                    });
                } else {
                    JTextField valueField = new JTextField(parameterElement.getSelectedValue());
                    fieldPanel.add(valueField);
                }
            }
            fieldPathLabel.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent fe) {
                    fieldPathLabel.setBackground(Color.WHITE);
                }

                public void focusLost(FocusEvent fe) {
                    if (!parameterElement.getXpathString().equals(fieldPathLabel.getText())) {
                        parameterElement.setXpathString(fieldPathLabel.getText());
                        indexerParam.setChangedFlag();
                        populateSelectionList();
                        revalidate();
                        savePanel.updateGraph();
                        savePanel.requiresSave();
                    }
                    fieldPathLabel.setBackground(paddingPanel.getBackground());
                }
            });
            JButton removeButton = new JButton("x");
            removeButton.setToolTipText("delete item");
            int removeButtonSize = removeButton.getFontMetrics(removeButton.getFont()).getHeight();
            removeButton.setPreferredSize(new Dimension(removeButtonSize, removeButtonSize));
            removeButton.setActionCommand(parameterElement.getXpathString());
            removeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    indexerParam.removeValue(parameterElement);
                    populateSelectionList();
                    revalidate();
                    savePanel.updateGraph();
                    savePanel.requiresSave();
                }
            });
            fieldPanel.add(removeButton);
            paddingPanel.add(fieldPanel);
        }

        final String[] availableValues = indexerParam.getAvailableValues();
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.LINE_AXIS));
        final JComboBox fieldSelectComboBox = new JComboBox(entityCollection.getAllFieldNames());
        addPanel.add(fieldSelectComboBox);
        final JButton addButton = new JButton("Add");
        addButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                String initialValue = null;
                if (availableValues != null) {
                    if (availableValues.length > 0) {
                        initialValue = availableValues[0];
                    } else {
                        initialValue = "";
                    }
                };
                String initialParameter = String.format(indexerParam.getDefaultValueFormat(), fieldSelectComboBox.getSelectedItem().toString());
                indexerParam.setValue(initialParameter, initialValue);
                populateSelectionList();
                revalidate();
                savePanel.updateGraph();
                savePanel.requiresSave();
            }
        });
        addPanel.add(addButton);
        addPanel.add(new JPanel());
        paddingPanel.add(addPanel);
        paddingPanel.add(new JPanel());

        if (availableValues != null) {
            JPanel defaultValuePanel = new JPanel();
            defaultValuePanel.setLayout(new BoxLayout(defaultValuePanel, BoxLayout.LINE_AXIS));
            defaultValuePanel.add(new JLabel("Default Symbol"));
            final JComboBox defaultSymbolComboBox = new JComboBox(availableValues);
            String currentDefault = savePanel.getGraphPanel().dataStoreSvg.defaultSymbol;
            for (String currentValue : availableValues) {
                if (currentValue.equals(currentDefault)) {
                    defaultSymbolComboBox.setSelectedItem(currentValue);
                }
            }
            defaultSymbolComboBox.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent ae) {
                    savePanel.getGraphPanel().dataStoreSvg.defaultSymbol = defaultSymbolComboBox.getSelectedItem().toString();
                    savePanel.updateGraph();
                    savePanel.requiresSave();
                }
            });
            defaultValuePanel.add(defaultSymbolComboBox);
            defaultValuePanel.add(new JPanel());
            paddingPanel.add(defaultValuePanel);
        }
    }
//    private void populateSelectionList(String[][] fieldListArray, String[] availableValues) {
//        paddingPanel.setLayout(new BoxLayout(paddingPanel, BoxLayout.PAGE_AXIS));
//        paddingPanel.removeAll();
//        for (String[] fieldArray : fieldListArray) {
//            JLabel fieldPathLabel = new JLabel(fieldArray[0]);
//            JComboBox valueSelect = new JComboBox(availableValues); // todo: get the string list from the svg: new String[]{"circle", "triangle", "square", "resource", "union"}
//            valueSelect.setSelectedItem(fieldArray[1]);
//            JPanel fieldPanel = new JPanel();
//            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
//            fieldPanel.add(fieldPathLabel);
//            fieldPanel.add(valueSelect);
//            paddingPanel.add(fieldPanel);
//        }
//    }

    protected void updateUiList() {
        populateSelectionList();
        revalidate();
        savePanel.updateGraph();
        savePanel.requiresSave();
    }
}
