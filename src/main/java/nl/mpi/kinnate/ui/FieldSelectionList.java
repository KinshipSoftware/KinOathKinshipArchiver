package nl.mpi.kinnate.ui;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.IndexerParameters.IndexerParam;

/**
 *  Document   : FieldSelectionList
 *  Created on : Feb 11, 2011, 2:05:55 PM
 *  Author     : Peter Withers
 */
public class FieldSelectionList extends JPanel {

    SavePanel savePanel;
    private IndexerParam indexerParam;

    public FieldSelectionList(SavePanel savePanelLocal, IndexerParam indexerParamLocal) {
        savePanel = savePanelLocal;
        indexerParam = indexerParamLocal;
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        populateSelectionList();
    }

    private void populateSelectionList() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.removeAll();
        for (String fieldArray[] : indexerParam.getValues()) {
            JLabel fieldPathLabel = new JLabel(fieldArray[0]);
            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
            fieldPanel.add(fieldPathLabel);
            fieldPanel.add(new JPanel());
            if (fieldArray.length > 1) {
                String[] availableValues = indexerParam.getAvailableValues();
                if (availableValues != null) {
                    JComboBox valueSelect = new JComboBox(availableValues);
                    valueSelect.setSelectedItem(fieldArray[1]);
                    fieldPanel.add(valueSelect);
                } else {
                    JTextField valueField = new JTextField(fieldArray[1]);
                    fieldPanel.add(valueField);
                }
            }
            JButton removeButton = new JButton("x");
            removeButton.setActionCommand(fieldArray[0]);
            removeButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    indexerParam.removeValue(evt.getActionCommand());
                    populateSelectionList();
                    revalidate();
                    savePanel.updateGraph();
                }
            });
            fieldPanel.add(removeButton);
            this.add(fieldPanel);
        }
        this.add(new JPanel());
    }

//    private void populateSelectionList(String[][] fieldListArray, String[] availableValues) {
//        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//        this.removeAll();
//        for (String[] fieldArray : fieldListArray) {
//            JLabel fieldPathLabel = new JLabel(fieldArray[0]);
//            JComboBox valueSelect = new JComboBox(availableValues); // todo: get the string list from the svg: new String[]{"circle", "triangle", "square", "resource", "union"}
//            valueSelect.setSelectedItem(fieldArray[1]);
//            JPanel fieldPanel = new JPanel();
//            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
//            fieldPanel.add(fieldPathLabel);
//            fieldPanel.add(valueSelect);
//            this.add(fieldPanel);
//        }
//    }
}
