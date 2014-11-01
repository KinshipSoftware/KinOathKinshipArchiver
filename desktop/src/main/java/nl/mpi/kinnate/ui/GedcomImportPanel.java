/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.EntityServiceException;
import nl.mpi.kinnate.gedcomimport.CsvImporter;
import nl.mpi.kinnate.gedcomimport.GedcomImporter;
import nl.mpi.kinnate.gedcomimport.GenericImporter;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.gedcomimport.KinOathImporter;
import nl.mpi.kinnate.gedcomimport.TipImporter;
import nl.mpi.kinnate.ui.entityprofiles.ProfileRecord;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Created on : Mar 14, 2011, 8:38:36 AM
 *
 * @author Peter Withers
 */
public class GedcomImportPanel extends JPanel {

    private final EntityCollection entityCollection;
    private final AbstractDiagramManager abstractDiagramManager;
    private final KinDiagramPanel kinDiagramPanel;
    private JTextArea importTextArea;
    private JProgressBar progressBar;
    private JCheckBox overwriteOnImport;
    private JCheckBox validateImportedXml;
    private JButton cancelButton;
    private JButton startButton;
    private JButton closeButton;
    private JPanel endPagePanel;
    private final SessionStorage sessionStorage;
    private final ArbilWindowManager dialogHandler;
    private final ArbilDataNodeLoader dataNodeLoader;
    private final ArbilTreeHelper treeHelper;
    private JDialog dialoguePanel;
    private Component errorPanel = null;

    public GedcomImportPanel(AbstractDiagramManager abstractDiagramManager, KinDiagramPanel kinDiagramPanel, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper) {
        this.setPreferredSize(new Dimension(500, 500));
        this.abstractDiagramManager = abstractDiagramManager;
        this.kinDiagramPanel = kinDiagramPanel;
        this.entityCollection = kinDiagramPanel.getEntityCollection();
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
//                    final SavePanel currentSavePanel = abstractDiagramManager.getCurrentSavePanel(parentComponent);
//                    KinDiagramPanel kinDiagramPanel = null;
//                    if (currentSavePanel instanceof KinDiagramPanel) {
//                        kinDiagramPanel = (KinDiagramPanel) currentSavePanel;
//                        if (kinDiagramPanel.getGraphPanel().dataStoreSvg.diagramMode != DataStoreSvg.DiagramMode.KinTypeQuery) {
//                            kinDiagramPanel = null;
//                        }
//                        kinDiagramPanel.updateGraph(); // there is no point updating the graph at this point
//                    }
//                    try {
//                        if (kinDiagramPanel == null) {
//                            kinDiagramPanel = new KinDiagramPanel(DocumentNewMenu.DocumentType.Simple, sessionStorage, dialogHandler, dataNodeLoader, treeHelper, entityCollection, abstractDiagramManager);
//                            kinDiagramPanel.setName("Imported Entities");
//                            abstractDiagramManager.createDiagramContainer(kinDiagramPanel, null);
//                        }
                    final HashSet<UniqueIdentifier> selectedIds = new HashSet<UniqueIdentifier>();
                    for (HashSet<UniqueIdentifier> identifiers : gedcomImporter.getCreatedNodeIds().values()) {
                        selectedIds.addAll(identifiers);
                    }
                    kinDiagramPanel.addNodeCollection(selectedIds.toArray(new UniqueIdentifier[]{}), "Imported Entities");
//                        kinDiagramPanel.loadAllTrees();
//                    } catch (EntityServiceException exception) {
//                        importTextArea.append("Creating a new document failed, cannot show the imported entitys." + "\n");
//                        dialogHandler.addMessageDialogToQueue("Creating a new document failed, cannot show the imported entitys.", "Import Entities");
//                    }
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
            final JComboBox profileSelectBox = new JComboBox(kinDiagramPanel.getGraphPanel().dataStoreSvg.selectedProfiles); // todo: changee this to use <default> for the gedcom profile
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
                                GenericImporter genericImporter = null;
                                for (GenericImporter testImporter : new GenericImporter[]{new GedcomImporter(progressBar, importTextArea, overwriteExisting, sessionStorage, entityCollection.getProjectRecord()),
                                    new CsvImporter(progressBar, importTextArea, overwriteExisting, sessionStorage, entityCollection.getProjectRecord()),
                                    new KinOathImporter(progressBar, importTextArea, overwriteExisting, sessionStorage, entityCollection.getProjectRecord()),
                                    new TipImporter(progressBar, importTextArea, overwriteExisting, sessionStorage, entityCollection.getProjectRecord())}) {
                                    if (importFileString != null) {
                                        if (testImporter.canImport(importFileString)) {
                                            genericImporter = testImporter;
                                            break;
                                        }
                                    } else {
                                        if (testImporter.canImport(importFile.toString())) {
                                            genericImporter = testImporter;
                                            break;
                                        }
                                    }
                                }
                                if (genericImporter == null) {
                                    importTextArea.append("No importers found for this file\n");
                                    return;
                                }
                                UniqueIdentifier[] importedIdentifierArray;
                                importTextArea.append("Importing the kinship data (step 1/4)\n");
                                importTextArea.setCaretPosition(importTextArea.getText().length());
                                if (importFileString != null) {
                                    importedIdentifierArray = genericImporter.importFile(importFileString, profileId);
                                } else {
                                    importedIdentifierArray = genericImporter.importFile(importFile, profileId);
                                }
                                boolean checkFilesAfterImport = validateImportedXml.isSelected();
                                if (importedIdentifierArray != null && checkFilesAfterImport) {
                                    int maxXsdErrorToShow = 3;
                                    importTextArea.append("Checking XML of imported data  (step 3/4)\n");
                                    importTextArea.setCaretPosition(importTextArea.getText().length());
                                    final int maxProgress = importedIdentifierArray.length + 1;
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            progressBar.setValue(0);
                                            progressBar.setMaximum(maxProgress);
                                        }
                                    });
                                    for (final UniqueIdentifier currentUniqueIdentifier : importedIdentifierArray) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                progressBar.setValue(progressBar.getValue() + 1);
                                            }
                                        });
                                        if (maxXsdErrorToShow > 0) {
                                            SwingUtilities.invokeLater(new Runnable() {
                                                public void run() {
                                                    XsdChecker xsdChecker = new XsdChecker();
                                                    if (xsdChecker.simpleCheck(new File(entityCollection.getProjectRecord().getFileInProject(currentUniqueIdentifier).toURI())) != null) {
                                                        xsdChecker.checkXML(dataNodeLoader.getArbilDataNode(null, entityCollection.getProjectRecord().getFileInProject(currentUniqueIdentifier).toURI()));
                                                        xsdChecker.setDividerLocation(0.5);
                                                        if (errorPanel == null) {
                                                            xsdChecker.setName("XSD Error on Import");
                                                            errorPanel = abstractDiagramManager.createDiagramContainer(xsdChecker, null);
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
                                entityCollection.updateDatabase(importedIdentifierArray, progressBar);
                                importTextArea.append("Import complete" + "\n");
                                importTextArea.setCaretPosition(importTextArea.getText().length());
//                            System.out.println("added the imported files to the database");
                                progressBar.setIndeterminate(false);
                                messageLabel.setText(GedcomImportPanel.this.getCreatedNodesMessage(genericImporter));
                                GedcomImportPanel.this.revalidate();
                            } catch (IOException exception) {
                                importTextArea.append("Import Failed: " + exception.getMessage() + "\n");
                            } catch (ImportException exception) {
                                importTextArea.append("Import Failed:" + exception.getMessage() + "\n");
                            } catch (EntityServiceException exception) {
                                importTextArea.append("Import Failed:" + exception.getMessage() + "\n");
                            }
                            progressBar.setVisible(false);
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
        dialoguePanel = abstractDiagramManager.createDialogueContainer(GedcomImportPanel.this, kinDiagramPanel);
        dialoguePanel.setVisible(true);
    }
}
