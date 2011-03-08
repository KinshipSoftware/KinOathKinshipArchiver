package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTerms;

/**
 *  Document   : KinTermPanel
 *  Created on : Mar 8, 2011, 12:21:12 PM
 *  Author     : Peter Withers
 */
public class KinTermPanel extends JPanel {

    KinTerms kinTerms;
    SavePanel savePanel;
    JPanel outerPanel;

    public KinTermPanel(SavePanel savePanelLocal, KinTerms kinTermsLocal) {
        kinTerms = kinTermsLocal;
        savePanel = savePanelLocal;
        this.setLayout(new BorderLayout());
        outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        populateKinTermList();
        this.add(new JScrollPane(outerPanel));
    }

    private void populateKinTermList() {
        outerPanel.removeAll();
//        this.add(new JLabel("KinTerms"));
        for (String[] currentKinTerm : kinTerms.getKinTerms()) {
            JPanel termPanel = new JPanel();
            termPanel.setLayout(new BoxLayout(termPanel, BoxLayout.Y_AXIS));
            termPanel.add(new JLabel(currentKinTerm[1]));
            JTextArea kinTypeString = new JTextArea(currentKinTerm[0]);
            final String kinType = currentKinTerm[1];
            kinTypeString.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent ke) {
                    super.keyReleased(ke);
                    kinTerms.updateKinTerm(((JTextArea) ke.getComponent()).getText(), kinType);
                    savePanel.updateGraph();
                }
            });

            termPanel.add(kinTypeString);
            JButton removeButton = new JButton("remove");
            removeButton.setActionCommand(currentKinTerm[1]);
            removeButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    kinTerms.removeKinTerm(evt.getActionCommand());
                    populateKinTermList();
                    revalidate();
                    savePanel.updateGraph();
                }
            });
            termPanel.add(removeButton);
            outerPanel.add(termPanel);
            outerPanel.add(new JSeparator());
        }
        populateAddForm();
    }

    private void populateAddForm() {
        final JTextField addNewKinTerm = new JTextField();
        final JTextField addNewKinType = new JTextField();
        JPanel termPanel = new JPanel();
        termPanel.setLayout(new BoxLayout(termPanel, BoxLayout.Y_AXIS));
        termPanel.add(addNewKinTerm);
        termPanel.add(addNewKinType);
        JButton addButton = new JButton("add");
        addButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinTerms.addKinTerm(addNewKinType.getText(), addNewKinTerm.getText());
                populateKinTermList();
                revalidate();
                savePanel.updateGraph();
            }
        });
        termPanel.add(addButton);
        outerPanel.add(termPanel);
    }
}
