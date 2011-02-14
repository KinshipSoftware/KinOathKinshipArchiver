package nl.mpi.kinnate;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  Document   : FieldSelectionList
 *  Created on : Feb 11, 2011, 2:05:55 PM
 *  Author     : Peter Withers
 */
public class FieldSelectionList extends JPanel {

    public FieldSelectionList() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public FieldSelectionList(String[] fieldListArray) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.removeAll();
        for (String fieldArray : fieldListArray) {
            JLabel fieldPathLabel = new JLabel(fieldArray);
            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
            fieldPanel.add(fieldPathLabel);
            this.add(fieldPanel);
        }
    }

    public FieldSelectionList(String[][] fieldListArray) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.removeAll();
        for (String[] fieldArray : fieldListArray) {
            JLabel fieldPathLabel = new JLabel(fieldArray[0]);
            JComboBox valueSelect = new JComboBox(new String[]{"circle", "triangle", "square", "resource", "union"}); // todo: remove the strings and get from the svg
            valueSelect.setSelectedItem(fieldArray[1]);
            JPanel fieldPanel = new JPanel();
            fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.LINE_AXIS));
            fieldPanel.add(fieldPathLabel);
            fieldPanel.add(valueSelect);
            this.add(fieldPanel);
        }
    }
}
