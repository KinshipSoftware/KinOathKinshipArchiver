package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import nl.mpi.arbil.LinorgBugCatcher;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTerm;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;

/**
 *  Document   : KinTermPanel
 *  Created on : Mar 8, 2011, 12:21:12 PM
 *  Author     : Peter Withers
 */
public class KinTermPanel extends JPanel {

    JTextArea kinTypeGroupName;
    KinTermGroup kinTerms;
    SavePanel savePanel;
    JCheckBox autoGenerateCheckBox;
    JCheckBox showOnGraphCheckBox;
    JComboBox colourSelectBox;
    JPanel outerPanel;
    JTextField addNewKinTerm;
    JTextField addNewKinType;
    String defaultKinType = "";

    public KinTermPanel(SavePanel savePanelLocal, KinTermGroup kinTermsLocal, String defaultKinTypeLocal) {
        kinTerms = kinTermsLocal;
        savePanel = savePanelLocal;
        defaultKinType = defaultKinTypeLocal;
        kinTypeGroupName = new JTextArea(kinTerms.titleString);
        kinTypeGroupName.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent ke) {
                super.keyReleased(ke);
                kinTerms.titleString = ((JTextArea) ke.getComponent()).getText();
                Component parentComponent = KinTermPanel.this.getParent();
                if (parentComponent instanceof JTabbedPane) {
                    ((JTabbedPane) parentComponent).setTitleAt(((JTabbedPane) parentComponent).getSelectedIndex(), kinTerms.titleString);
                }
            }
        });
        colourSelectBox = new JComboBox(new String[]{"red", "blue", "#FF0000", "#FFAA00", "#00FF95", "#62D9A7", "#8000FF", "#FF00D4"});
        colourSelectBox.setSelectedItem(kinTerms.graphColour);
        colourSelectBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinTerms.graphColour = colourSelectBox.getSelectedItem().toString();
                savePanel.updateGraph();
            }
        });
        showOnGraphCheckBox = new JCheckBox("Show On Graph");
        autoGenerateCheckBox = new JCheckBox("Generate Example Entities");
        this.setLayout(new BorderLayout());
        outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        populateKinTermList();
        // keep the panel items pushed to the top of the page
        JPanel paddingPanel = new JPanel();
        paddingPanel.setLayout(new BorderLayout());
        paddingPanel.add(outerPanel, BorderLayout.PAGE_START);
        this.add(new JScrollPane(paddingPanel), BorderLayout.CENTER);
    }

    private void populateKinTermList() {
        outerPanel.removeAll();
        outerPanel.add(kinTypeGroupName);
        outerPanel.add(showOnGraphCheckBox);
        outerPanel.add(colourSelectBox);
        outerPanel.add(autoGenerateCheckBox);
//        this.add(new JLabel("KinTerms"));
        for (KinTerm currentKinTerm : kinTerms.getKinTerms()) {
            JPanel termPanel = new JPanel();
            termPanel.setBorder(BorderFactory.createTitledBorder(currentKinTerm.kinTerm));
            termPanel.setLayout(new BoxLayout(termPanel, BoxLayout.Y_AXIS));
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BorderLayout());
//            labelPanel.add(new JLabel(currentKinTerm.kinTerm), BorderLayout.CENTER);
            JTextArea kinTypeString = new JTextArea(currentKinTerm.alterKinTypeStrings);
            final String kinType = currentKinTerm.kinTerm;
            kinTypeString.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent ke) {
                    super.keyReleased(ke);
                    kinTerms.updateKinTerm(((JTextArea) ke.getComponent()).getText(), kinType);
                    savePanel.updateGraph();
                }
            });

            JButton removeButton = new JButton("x");
            removeButton.setToolTipText("delete kin term");
            int removeButtonSize = removeButton.getFontMetrics(removeButton.getFont()).getHeight();
            removeButton.setPreferredSize(new Dimension(removeButtonSize, removeButtonSize));
            removeButton.setActionCommand(currentKinTerm.kinTerm);
            removeButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    kinTerms.removeKinTerm(evt.getActionCommand());
                    populateKinTermList();
                    revalidate();
                    savePanel.updateGraph();
                }
            });
            labelPanel.add(removeButton, BorderLayout.LINE_END);
            termPanel.add(labelPanel);
            termPanel.add(kinTypeString);
            outerPanel.add(termPanel);
//            outerPanel.add(new JSeparator());
        }
//        JButton saveButton = new JButton("save");
//        saveButton.setToolTipText("save changes");
//        saveButton.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                savePanel.updateGraph();
//            }
//        });
//        outerPanel.add(saveButton, BorderLayout.LINE_END);
        populateAddForm();
    }

    public void setDefaultKinType(String kinTypeString) {
        defaultKinType = kinTypeString;
        addNewKinType.setText(defaultKinType);
    }

    private void populateAddForm() {
        addNewKinTerm = new JTextField();
        addNewKinType = new JTextField();
        addNewKinType.setText(defaultKinType);
        JPanel termPanel = new JPanel();
        termPanel.setBorder(BorderFactory.createTitledBorder("Create New Kin Term"));
        termPanel.setLayout(new BoxLayout(termPanel, BoxLayout.Y_AXIS));
        termPanel.add(new JLabel("Kin Term"));
        termPanel.add(addNewKinTerm);
        termPanel.add(new JLabel("Kin Type Strings"));
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

    public void exportKinTerms() {
        File[] exportFile = LinorgWindowManager.getSingleInstance().showFileSelectBox("Export Kin Terms", false, false, false);
        if (exportFile.length != 1) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Export file not selected", "Export Kin Terms");
        } else {
            if (exportFile[0].exists()) {
                if (!LinorgWindowManager.getSingleInstance().showMessageDialogBox("Export file already exists, overwrite?", "Export Kin Terms")) {
                    return;
                }
            }
            try {
                FileWriter fileWriter = new FileWriter(exportFile[0]);
                // todo: complete the export and resolve issues using the Arbil file select for export files
                fileWriter.write("wookies are lovely on toast.");
                fileWriter.close();
            } catch (IOException exception) {
                new LinorgBugCatcher().logError(exception);
            }
        }
    }

    public void importKinTerms() {
        File[] importFiles = LinorgWindowManager.getSingleInstance().showFileSelectBox("Import Kin Terms", false, true, false);
        if (importFiles.length == 0) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No files selected for import", "Import Kin Terms");
        }
        for (File currentFile : importFiles) {
            int importCount = 0;
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(currentFile));
                String currentLine = null;
                while ((currentLine = bufferedReader.readLine()) != null) {
                    StringTokenizer stringTokenizer = new StringTokenizer(currentLine, ",");
                    if (stringTokenizer.countTokens() > 1) {
                        String kinTypeStrings = stringTokenizer.nextToken();
                        String kinTermLabel = stringTokenizer.nextToken();
                        kinTypeStrings = kinTypeStrings.trim();
                        kinTermLabel = kinTermLabel.trim();
                        kinTerms.addKinTerm(kinTypeStrings, kinTermLabel);
                        importCount++;
                    }
                }
                bufferedReader.close();
                populateKinTermList();
                revalidate();
                savePanel.updateGraph();
            } catch (IOException exception) {
                new LinorgBugCatcher().logError(exception);
            }
            // todo: resolve why this dialogue does not show
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Imported " + importCount + " kin terms", "Import Kin Terms");
        }
    }
}
