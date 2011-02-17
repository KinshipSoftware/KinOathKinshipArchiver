package nl.mpi.kinnate;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import nl.mpi.kinnate.EntityIndexer.IndexerParameters.IndexerParam;

/**
 *  Document   : FieldSelectionList
 *  Created on : Feb 11, 2011, 2:05:55 PM
 *  Author     : Peter Withers
 */
public class FieldSelectionList extends JPanel {

    KinTypeEgoSelectionTestPanel egoSelectionTestPanel;
    private IndexerParam indexerParam;

    public FieldSelectionList(KinTypeEgoSelectionTestPanel egoSelectionTestPanelLocal, IndexerParam indexerParamLocal) {
        egoSelectionTestPanel = egoSelectionTestPanelLocal;
        indexerParam = indexerParamLocal;
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        populateSelectionList();
    }

    private void populateSelectionList() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.removeAll();
        for (String fieldArray : indexerParam.getValues()) {
            JLabel fieldPathLabel = new JLabel(fieldArray);
            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
            fieldPanel.add(fieldPathLabel);
            JButton removeButton = new JButton("x");
            removeButton.setActionCommand(fieldArray);
            removeButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    indexerParam.removeValue(evt.getActionCommand());
                    populateSelectionList();
                    revalidate();
                    egoSelectionTestPanel.drawGraph();
                }
            });
            fieldPanel.add(new JPanel());
            fieldPanel.add(removeButton);
            this.add(fieldPanel);
        }
        this.add(new JPanel());
    }

    private void populateSelectionList(String[][] fieldListArray, String[] availableValues) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.removeAll();
        for (String[] fieldArray : fieldListArray) {
            JLabel fieldPathLabel = new JLabel(fieldArray[0]);
            JComboBox valueSelect = new JComboBox(availableValues); // todo: get the string list from the svg: new String[]{"circle", "triangle", "square", "resource", "union"}
            valueSelect.setSelectedItem(fieldArray[1]);
            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
            fieldPanel.add(fieldPathLabel);
            fieldPanel.add(valueSelect);
            this.add(fieldPanel);
        }
    }
}
