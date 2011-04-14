package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.IndexerParam;
import nl.mpi.kinnate.entityindexer.ParameterElement;

/**
 *  Document   : FieldSelectionList
 *  Created on : Feb 11, 2011, 2:05:55 PM
 *  Author     : Peter Withers
 */
public class FieldSelectionList extends JPanel {

    private SavePanel savePanel;
    private JPanel paddingPanel;
    protected IndexerParam indexerParam;

    public FieldSelectionList(SavePanel savePanelLocal, IndexerParam indexerParamLocal, TableCellDragHandler tableCellDragHandler) {
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
        for (ParameterElement parameterElement : indexerParam.getValues()) {
            JLabel fieldPathLabel = new JLabel(parameterElement.getXpathString());
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
                            indexerParam.setValue(evt.getActionCommand(), ((JComboBox) evt.getSource()).getSelectedItem().toString());
                            populateSelectionList();
                            revalidate();
                            savePanel.updateGraph();
                        }
                    });



                } else {
                    JTextField valueField = new JTextField(parameterElement.getSelectedValue());
                    fieldPanel.add(valueField);
                }
            }
            JButton removeButton = new JButton("x");
            removeButton.setToolTipText("delete item");
            int removeButtonSize = removeButton.getFontMetrics(removeButton.getFont()).getHeight();
            removeButton.setPreferredSize(new Dimension(removeButtonSize, removeButtonSize));
            removeButton.setActionCommand(parameterElement.getXpathString());
            removeButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    indexerParam.removeValue(evt.getActionCommand());
                    populateSelectionList();
                    revalidate();
                    savePanel.updateGraph();
                }
            });
            fieldPanel.add(removeButton);
            paddingPanel.add(fieldPanel);
        }
        paddingPanel.add(new JPanel());
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
    }
}
