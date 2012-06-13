package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.kinnate.SavePanel;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.CsvImporter;
import nl.mpi.kinnate.gedcomimport.GedcomImporter;
import nl.mpi.kinnate.gedcomimport.GenericImporter;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.ui.entityprofiles.ProfileRecord;
import nl.mpi.kinnate.ui.menu.DocumentNewMenu;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : GedcomImportPanel
 * Created on : Mar 14, 2011, 8:38:36 AM
 * Author : Peter Withers
 */
public class GedcomImportPanel extends JPanel {

    private EntityCollection entityCollection;
    private AbstractDiagramManager abstractDiagramManager;
    private SavePanel originatingSavePanel;
    private JTextArea importTextArea;
    private JProgressBar progressBar;
    private JCheckBox overwriteOnImport;
    private JCheckBox validateImportedXml;
    private JButton cancelButton;
    private JButton startButton;
    private JButton closeButton;
    private JPanel endPagePanel;
    private SessionStorage sessionStorage;
    private ArbilWindowManager dialogHandler;
    private ArbilDataNodeLoader dataNodeLoader;
    private ArbilTreeHelper treeHelper;
    private JDialog dialoguePanel;
    private Component errorPanel = null;

    public GedcomImportPanel(AbstractDiagramManager abstractDiagramManager, SavePanel originatingSavePanel, EntityCollection entityCollection, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper) {
        this.setPreferredSize(new Dimension(500, 500));
        this.abstractDiagramManager = abstractDiagramManager;
        this.originatingSavePanel = originatingSavePanel;
        this.entityCollection = entityCollection;
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
        this.treeHelper = treeHelper;
    }

    protected String getCreatedNodesMessage(final GenericImporter gedcomImporter) {
        if (gedcomImporter.getCreatedNodeIds().isEmpty()) {
            return "No data was imported, nothing to show in the graph.";
        } else {
            closeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    KinDiagramPanel egoSelectionTestPanel;
                    if (originatingSavePanel instanceof KinDiagramPanel) {
                        egoSelectionTestPanel = (KinDiagramPanel) originatingSavePanel;
                        egoSelectionTestPanel.updateGraph();
                    } else {
                        egoSelectionTestPanel = new KinDiagramPanel(DocumentNewMenu.DocumentType.Simple, sessionStorage, dialogHandler, dataNodeLoader, treeHelper, entityCollection, abstractDiagramManager);
                        egoSelectionTestPanel.setName("Imported Entities");
                        abstractDiagramManager.createDiagramContainer(egoSelectionTestPanel);
                    }
                    final KinDiagramPanel kinDiagramPanel = egoSelectionTestPanel;
//                    SwingUtilities.invokeLater(new Runnable() {
//
//                        public void run() {
                    final HashSet<UniqueIdentifier> selectedIds = new HashSet<UniqueIdentifier>();
                    for (HashSet<UniqueIdentifier> identifiers : gedcomImporter.getCreatedNodeIds().values()) {
                        selectedIds.addAll(identifiers);
                    }
                    kinDiagramPanel.addNodeCollection(selectedIds.toArray(new UniqueIdentifier[]{}), "Imported Entities");
                    kinDiagramPanel.loadAllTrees();
//                        }
//                    });
                    dialoguePanel.dispose();
                }
            });
            closeButton.setEnabled(true);
        }
        return "Import complete.";
    }

    public void startImport(File importFile) throws ImportException {
        startImport(importFile, null, importFile.getName());
    }

    public void startImport(String importUriString) throws ImportException {
        File cachedFile = sessionStorage.updateCache(importUriString, 30, true);
        startImport(cachedFile, null, importUriString);
    }

    public void startImportJar(String importFileString) throws ImportException {
        startImport(null, importFileString, importFileString);
    }

    private void startImport(final File importFile, final String importFileString, String importLabel) throws ImportException {
        if (importFile != null && !importFile.exists()) {
            throw new ImportException("The import file was not found.");
        } else {
            importTextArea = new JTextArea();
            JScrollPane importScrollPane = new JScrollPane(importTextArea);
            GedcomImportPanel.this.setLayout(new BorderLayout());
            GedcomImportPanel.this.add(importScrollPane, BorderLayout.CENTER);
            String titleString;
            if (importFile != null) {
                titleString = "Import: " + importFile.getName();
            } else {
                titleString = "Import: " + importFileString.substring(importFileString.lastIndexOf("/") + 1);
            }
            setName(titleString);
            progressBar = new JProgressBar(0, 100);
            endPagePanel = new JPanel(new BorderLayout());
            endPagePanel.add(progressBar, BorderLayout.PAGE_START);
            GedcomImportPanel.this.add(endPagePanel, BorderLayout.PAGE_END);
            progressBar.setVisible(true);
            JPanel bottomPanel = new JPanel();
            JPanel topPanel = new JPanel();
            // todo: any existing files are always being overwritten and the entity id also being changed so existing relations will be broken, maybe prevent overwritting all entities for an import file?
            // todo: it might be better to check for a file already exsiting and if it does load it and strip out the relations and metadata that would be replaced by the import?
            // todo: add a label and a better default for gedcom (non INDI does not need DOB etc.)
            final JComboBox profileSelectBox = new JComboBox(new DataStoreSvg().selectedProfiles); // todo: changee this to use <default> for the gedcom profile
            final ProfileRecord defaultImportProfile = ProfileRecord.getDefaultImportProfile();
            profileSelectBox.addItem(defaultImportProfile);
            profileSelectBox.setSelectedItem(defaultImportProfile);
            topPanel.add(profileSelectBox);
            overwriteOnImport = new JCheckBox("Overwrite Existing");
            overwriteOnImport.setEnabled(false);
            cancelButton = new JButton("Cancel");
            startButton = new JButton("Start Import");
            closeButton = new JButton("Close");
            closeButton.setEnabled(false);
            topPanel.add(overwriteOnImport);
            validateImportedXml = new JCheckBox("Validate Xml");
            topPanel.add(validateImportedXml);
            bottomPanel.add(cancelButton);
            bottomPanel.add(startButton);
            bottomPanel.add(closeButton);
            JPanel topOuterPanel = new JPanel(new BorderLayout());
            final JLabel messageLabel = new JLabel(importLabel, JLabel.CENTER);
            endPagePanel.add(messageLabel, BorderLayout.CENTER);
            topOuterPanel.add(topPanel, BorderLayout.CENTER);
            endPagePanel.add(bottomPanel, BorderLayout.PAGE_END);
            GedcomImportPanel.this.add(topOuterPanel, BorderLayout.PAGE_START);

            startButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    cancelButton.setEnabled(false);
                    startButton.setEnabled(false);
                    overwriteOnImport.setEnabled(false);
                    validateImportedXml.setEnabled(false);
                    profileSelectBox.setEnabled(false);
                    final String profileId = ((ProfileRecord) profileSelectBox.getSelectedItem()).profileId;
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                boolean overwriteExisting = overwriteOnImport.isSelected();
                                GenericImporter genericImporter = new GedcomImporter(progressBar, importTextArea, overwriteExisting, sessionStorage);
                                if (importFileString != null) {
                                    if (!genericImporter.canImport(importFileString)) {
                                        genericImporter = new CsvImporter(progressBar, importTextArea, overwriteExisting, sessionStorage);
                                    }
                                } else {
                                    if (!genericImporter.canImport(importFile.toString())) {
                                        genericImporter = new CsvImporter(progressBar, importTextArea, overwriteExisting, sessionStorage);
                                    }
                                }
                                URI[] treeNodesArray;
                                importTextArea.append("Importing the kinship data (step 1/4)\n");
                                importTextArea.setCaretPosition(importTextArea.getText().length());
                                if (importFileString != null) {
                                    treeNodesArray = genericImporter.importFile(importFileString, profileId);
                                } else {
                                    treeNodesArray = genericImporter.importFile(importFile, profileId);
                                }
                                boolean checkFilesAfterImport = validateImportedXml.isSelected();
                                if (treeNodesArray != null && checkFilesAfterImport) {
                                    int maxXsdErrorToShow = 3;
                                    importTextArea.append("Checking XML of imported data  (step 3/4)\n");
                                    importTextArea.setCaretPosition(importTextArea.getText().length());
                                    final int maxProgress = treeNodesArray.length + 1;
                                    SwingUtilities.invokeLater(new Runnable() {

                                        public void run() {
                                            progressBar.setValue(0);
                                            progressBar.setMaximum(maxProgress);
                                        }
                                    });
                                    for (final URI currentNodeUri : treeNodesArray) {
                                        SwingUtilities.invokeLater(new Runnable() {

                                            public void run() {
                                                progressBar.setValue(progressBar.getValue() + 1);
                                            }
                                        });
                                        if (maxXsdErrorToShow > 0) {
                                            SwingUtilities.invokeLater(new Runnable() {

                                                public void run() {
                                                    XsdChecker xsdChecker = new XsdChecker();
                                                    if (xsdChecker.simpleCheck(new File(currentNodeUri)) != null) {
                                                        xsdChecker.checkXML(dataNodeLoader.getArbilDataNode(null, currentNodeUri));
                                                        xsdChecker.setDividerLocation(0.5);
                                                        if (errorPanel == null) {
                                                            xsdChecker.setName("XSD Error on Import");
                                                            errorPanel = abstractDiagramManager.createDiagramContainer(xsdChecker);
                                                        } else {
                                                            abstractDiagramManager.createDiagramSubPanel("XSD Error on Import", xsdChecker, errorPanel);
                                                        }
                                                    }
                                                }
                                            });
                                            maxXsdErrorToShow--;
                                            if (maxXsdErrorToShow <= 0) {
                                                importTextArea.append("maximum xsd errors shown, no more files will be tested" + "\n");
                                            }
                                        }
                                    }
                                    if (errorPanel instanceof JFrame) {
                                        ((JFrame) errorPanel).pack();
                                    }
                                } else {
                                    importTextArea.append("Skipping check XML of imported data  (step 3/4)\n");
                                }
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        progressBar.setIndeterminate(true);
                                    }
                                });
                                // todo: it might be more efficient to only update the new files
                                importTextArea.append("Starting update of entity database (step 4/4)\n");
                                importTextArea.setCaretPosition(importTextArea.getText().length());
                                entityCollection.updateDatabase(treeNodesArray, progressBar);
                                importTextArea.append("Import complete" + "\n");
                                importTextArea.setCaretPosition(importTextArea.getText().length());
//                            System.out.println("added the imported files to the database");
                                progressBar.setIndeterminate(false);
                                progressBar.setVisible(false);
                                messageLabel.setText(GedcomImportPanel.this.getCreatedNodesMessage(genericImporter));
                                GedcomImportPanel.this.revalidate();
                            } catch (IOException exception) {
                                importTextArea.append("Import Failed: " + exception.getMessage() + "\n");
                            } catch (ImportException exception) {
                                importTextArea.append("Import Failed:" + exception.getMessage() + "\n");
                            }
                        }
                    }.start();
                }
            });
        }
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dialoguePanel.dispose();
            }
        });
        if (originatingSavePanel instanceof Component) {
            dialoguePanel = abstractDiagramManager.createDialogueContainer(GedcomImportPanel.this, (Component) originatingSavePanel);
        } else {
            dialoguePanel = abstractDiagramManager.createDialogueContainer(GedcomImportPanel.this, null);
        }
        dialoguePanel.setVisible(true);
    }
}
