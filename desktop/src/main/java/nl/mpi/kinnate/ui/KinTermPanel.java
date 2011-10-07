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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.kintypestrings.KinTerm;
import nl.mpi.kinnate.kintypestrings.KinTermGroup;

/**
 *  Document   : KinTermPanel
 *  Created on : Mar 8, 2011, 12:21:12 PM
 *  Author     : Peter Withers
 */
public class KinTermPanel extends JPanel {

    JTextField kinTypeGroupName;
    JTextField kinTypeGroupDescription;
    KinTermGroup kinTerms;
    SavePanel savePanel;
    JCheckBox autoGenerateCheckBox;
    JCheckBox showOnGraphCheckBox;
    JComboBox colourSelectBox;
    JPanel outerPanel;
    JTextField addNewKinTerm;
    JTextField addEgoKinType;
    JTextField addKinTermDescription;
    JTextField addAlterKinType;
    JTextField addPropositusKinType;
    JTextField addAnchorKinType;
    String defaultKinType = "";

    public KinTermPanel(SavePanel savePanelLocal, KinTermGroup kinTermsLocal, String defaultKinTypeLocal) {
        kinTerms = kinTermsLocal;
        savePanel = savePanelLocal;
        defaultKinType = defaultKinTypeLocal;
        kinTypeGroupName = new JTextField(kinTerms.titleString);
        kinTypeGroupName.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent ke) {
                super.keyReleased(ke);
                kinTerms.titleString = ((JTextField) ke.getComponent()).getText();
                Component parentComponent = KinTermPanel.this.getParent();
                if (parentComponent instanceof JTabbedPane) {
                    ((JTabbedPane) parentComponent).setTitleAt(((JTabbedPane) parentComponent).getSelectedIndex(), kinTerms.titleString);
                }
                savePanel.setRequiresSave();
            }
        });
        kinTypeGroupDescription = new JTextField(kinTerms.descriptionString);
        kinTypeGroupDescription.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent ke) {
                super.keyReleased(ke);
                kinTerms.descriptionString = ((JTextField) ke.getComponent()).getText();
                savePanel.setRequiresSave();
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
        showOnGraphCheckBox.setSelected(kinTerms.graphShow);
        showOnGraphCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinTerms.graphShow = showOnGraphCheckBox.isSelected();
                savePanel.updateGraph();
            }
        });
        autoGenerateCheckBox = new JCheckBox("Generate Example Entities");
        autoGenerateCheckBox.setSelected(kinTerms.graphGenerate);
        autoGenerateCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinTerms.graphGenerate = autoGenerateCheckBox.isSelected();
                savePanel.updateGraph();
            }
        });
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

    @Override
    public String getName() {
        return kinTerms.titleString;
    }

    private void populateKinTermList() {
        outerPanel.removeAll();
        outerPanel.add(kinTypeGroupName);
        outerPanel.add(kinTypeGroupDescription);
        outerPanel.add(showOnGraphCheckBox);
        outerPanel.add(colourSelectBox);
        outerPanel.add(autoGenerateCheckBox);

        populateAddForm();
//        this.add(new JLabel("KinTerms"));
        for (final KinTerm currentKinTerm : kinTerms.getKinTerms()) {
            JPanel termPanel = new JPanel();
            termPanel.setBorder(BorderFactory.createTitledBorder(currentKinTerm.kinTerm));
            termPanel.setLayout(new BoxLayout(termPanel, BoxLayout.Y_AXIS));
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BorderLayout());
//            labelPanel.add(new JLabel(currentKinTerm.kinTerm), BorderLayout.CENTER);
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
            if (currentKinTerm.kinTermDescription != null) {
                termPanel.add(new JLabel("Description"));
                JTextField kinTypeString = new JTextField(currentKinTerm.kinTermDescription);
                kinTypeString.addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyReleased(KeyEvent ke) {
                        super.keyReleased(ke);
                        currentKinTerm.kinTermDescription = ((JTextField) ke.getComponent()).getText();
                        savePanel.updateGraph();
                    }
                });
                termPanel.add(kinTypeString);
            }
            if (currentKinTerm.egoType != null) {
                termPanel.add(new JLabel("Ego Kin Type"));
                JTextField kinTypeString = new JTextField(currentKinTerm.egoType);
                kinTypeString.addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyReleased(KeyEvent ke) {
                        super.keyReleased(ke);
                        currentKinTerm.egoType = ((JTextField) ke.getComponent()).getText();
                        savePanel.updateGraph();
                    }
                });
                termPanel.add(kinTypeString);
            }
            if (currentKinTerm.alterKinTypeStrings != null) {
                termPanel.add(new JLabel("Alter Kin Type Strings"));
                JTextField kinTypeString = new JTextField(currentKinTerm.alterKinTypeStrings);
                kinTypeString.addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyReleased(KeyEvent ke) {
                        super.keyReleased(ke);
                        currentKinTerm.alterKinTypeStrings = ((JTextField) ke.getComponent()).getText();
                        savePanel.updateGraph();
                    }
                });
                termPanel.add(kinTypeString);
            }
            if (currentKinTerm.propositusKinTypeStrings != null) {
                termPanel.add(new JLabel("Propositus Kin Type Strings"));
                JTextField kinTypeString = new JTextField(currentKinTerm.propositusKinTypeStrings);
                kinTypeString.addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyReleased(KeyEvent ke) {
                        super.keyReleased(ke);
                        currentKinTerm.propositusKinTypeStrings = ((JTextField) ke.getComponent()).getText();
                        savePanel.updateGraph();
                    }
                });
                termPanel.add(kinTypeString);
            }
            if (currentKinTerm.anchorKinTypeStrings != null) {
                termPanel.add(new JLabel("Anchor Kin Type Strings"));
                JTextField kinTypeString = new JTextField(currentKinTerm.anchorKinTypeStrings);
                kinTypeString.addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyReleased(KeyEvent ke) {
                        super.keyReleased(ke);
                        currentKinTerm.anchorKinTypeStrings = ((JTextField) ke.getComponent()).getText();
                        savePanel.updateGraph();
                    }
                });
                termPanel.add(kinTypeString);
            }
            termPanel.add(labelPanel);
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
    }

    public void setDefaultKinType(String kinTypeString) {
        defaultKinType = kinTypeString;
        addAlterKinType.setText(defaultKinType);
    }

    private void populateAddForm() {
        addNewKinTerm = new JTextField();
        addKinTermDescription = new JTextField();
        addEgoKinType = new JTextField();
        addAlterKinType = new JTextField(defaultKinType);
        addPropositusKinType = new JTextField();
        addAnchorKinType = new JTextField();
        JPanel termPanel = new JPanel();
        termPanel.setBorder(BorderFactory.createTitledBorder("Create New Kin Term"));
        termPanel.setLayout(new BoxLayout(termPanel, BoxLayout.Y_AXIS));
        termPanel.add(new JLabel("Kin Term"));
        termPanel.add(addNewKinTerm);
        termPanel.add(new JLabel("Description"));
        termPanel.add(addKinTermDescription);
        termPanel.add(new JLabel("Ego Kin Type"));
        termPanel.add(addEgoKinType);
        termPanel.add(new JLabel("Alter Kin Type Strings"));
        termPanel.add(addAlterKinType);
        termPanel.add(new JLabel("Propositus Kin Type Strings"));
        termPanel.add(addPropositusKinType);
        termPanel.add(new JLabel("Anchor Kin Type Strings"));
        termPanel.add(addAnchorKinType);
        JButton addButton = new JButton("add");
        addButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KinTerm kinTerm = new KinTerm();
                if (addNewKinTerm.getText().length() > 0) {
                    kinTerm.kinTerm = addNewKinTerm.getText();
                }
//                if (addKinTermDescription.getText().length() > 0) {
                kinTerm.kinTermDescription = addKinTermDescription.getText();
//                }
                if (addEgoKinType.getText().length() > 0) {
                    kinTerm.egoType = addEgoKinType.getText();
                }
                // description and alter are basic requirements
//                if (addAlterKinType.getText().length() > 0) {
                kinTerm.alterKinTypeStrings = addAlterKinType.getText();
//                }
                if (addPropositusKinType.getText().length() > 0) {
                    kinTerm.propositusKinTypeStrings = addPropositusKinType.getText();
                }
                if (addAnchorKinType.getText().length() > 0) {
                    kinTerm.anchorKinTypeStrings = addAnchorKinType.getText();
                }
                kinTerms.addKinTerm(kinTerm);
                populateKinTermList();
                revalidate();
                savePanel.updateGraph();
            }
        });
        termPanel.add(addButton);
        outerPanel.add(termPanel);
    }

    public void exportKinTerms() {
        File[] exportFile = ArbilWindowManager.getSingleInstance().showFileSelectBox("Export Kin Terms", false, false, false);
        if (exportFile.length != 1) {
            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Export file not selected", "Export Kin Terms");
        } else {
            if (exportFile[0].exists()) {
                if (!ArbilWindowManager.getSingleInstance().showConfirmDialogBox("Export file already exists, overwrite?", "Export Kin Terms")) {
                    return;
                }
            }
            try {
                FileWriter fileWriter = new FileWriter(exportFile[0]);
                // todo: complete the export and resolve issues using the Arbil file select for export files
                fileWriter.write("wookies are lovely on toast.");
                fileWriter.close();
            } catch (IOException exception) {
                new ArbilBugCatcher().logError(exception);
            }
        }
    }

    public void importKinTerms() {
        File[] importFiles = ArbilWindowManager.getSingleInstance().showFileSelectBox("Import Kin Terms", false, true, false);
        if (importFiles.length == 0) {
            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No files selected for import", "Import Kin Terms");
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
                new ArbilBugCatcher().logError(exception);
            }
            // todo: resolve why this dialogue does not show
            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Imported " + importCount + " kin terms", "Import Kin Terms");
        }
    }
}
