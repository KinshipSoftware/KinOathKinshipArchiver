package nl.mpi.kinnate.ui;

import nl.mpi.kinnate.ui.menu.DocumentNewMenu;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.CsvImporter;
import nl.mpi.kinnate.gedcomimport.GedcomImporter;
import nl.mpi.kinnate.gedcomimport.GenericImporter;
import nl.mpi.kinnate.ui.window.AbstractDiagramManager;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : GedcomImportPanel
 *  Created on : Mar 14, 2011, 8:38:36 AM
 *  Author     : Peter Withers
 */
public class GedcomImportPanel extends JPanel {

    private EntityCollection entityCollection;
    private AbstractDiagramManager abstractDiagramManager;
    private JTextArea importTextArea;
    private JProgressBar progressBar;
    private JCheckBox overwriteOnImport;
    private JCheckBox validateImportedXml;
    private JButton startButton;
    private JPanel endPagePanel;
    private SessionStorage sessionStorage;
    private ArbilWindowManager dialogHandler;
    private BugCatcher bugCatcher;
    private ArbilDataNodeLoader dataNodeLoader;
    private ArbilTreeHelper treeHelper;

    public GedcomImportPanel(AbstractDiagramManager abstractDiagramManager, EntityCollection entityCollection, SessionStorage sessionStorage, ArbilWindowManager dialogHandler, BugCatcher bugCatcher, ArbilDataNodeLoader dataNodeLoader, ArbilTreeHelper treeHelper) {
        this.setPreferredSize(new Dimension(500, 500));
        this.abstractDiagramManager = abstractDiagramManager;
        this.entityCollection = entityCollection;
        this.sessionStorage = sessionStorage;
        this.dialogHandler = dialogHandler;
        this.bugCatcher = bugCatcher;
        this.dataNodeLoader = dataNodeLoader;
        this.treeHelper = treeHelper;

//        private ImdiTree leftTree;
////    private GraphPanel graphPanel;
////    private JungGraph jungGraph;
//    private ImdiTable previewTable;
//    private ImdiTableModel imdiTableModel;
//    private DragTransferHandler dragTransferHandler;
    }

    protected JPanel getCreatedNodesPane(final GenericImporter gedcomImporter) {
        JPanel createdNodesPanel = new JPanel();
        createdNodesPanel.setLayout(new BoxLayout(createdNodesPanel, BoxLayout.PAGE_AXIS));
        if (gedcomImporter.getCreatedNodeIds().isEmpty()) {
            createdNodesPanel.add(new JLabel("No data was imported, nothing to show in the graph."));
        } else {
            final ArrayList<JCheckBox> checkBoxArray = new ArrayList<JCheckBox>();
            for (String typeString : gedcomImporter.getCreatedNodeIds().keySet()) {
                JCheckBox currentCheckBox = new JCheckBox(typeString + " ( x " + gedcomImporter.getCreatedNodeIds().get(typeString).size() + ")");
                currentCheckBox.setActionCommand(typeString);
                checkBoxArray.add(currentCheckBox);
                createdNodesPanel.add(currentCheckBox);
            }
            JButton showButton = new JButton("Show selected types in graph");
            showButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    final KinDiagramPanel egoSelectionTestPanel = new KinDiagramPanel(DocumentNewMenu.DocumentType.Simple, sessionStorage, dialogHandler, bugCatcher, dataNodeLoader, treeHelper, entityCollection);
//                    egoSelectionTestPanel.setDisplayNodes("X", selectedIds.toArray(new String[]{}));
                    abstractDiagramManager.createDiagramContainer("Imported Entities", egoSelectionTestPanel);
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            final HashSet<UniqueIdentifier> selectedIds = new HashSet<UniqueIdentifier>();
                            for (final JCheckBox currentCheckBox : checkBoxArray) {
                                if (currentCheckBox.isSelected()) {
                                    selectedIds.addAll((gedcomImporter.getCreatedNodeIds().get(currentCheckBox.getActionCommand())));
                                }
                            }
                            egoSelectionTestPanel.addRequiredNodes(selectedIds.toArray(new UniqueIdentifier[]{}));
                            egoSelectionTestPanel.loadAllTrees();
                        }
                    });
                }
            });
            createdNodesPanel.add(showButton);
        }
        return createdNodesPanel;
    }

    public void startImport(File importFile) {
        startImport(importFile, null, importFile.getName());
    }

    public void startImport(String importUriString) {
        File cachedFile = sessionStorage.updateCache(importUriString, 30, true);
        startImport(cachedFile, null, importUriString);
    }

    public void startImportJar(String importFileString) {
        startImport(null, importFileString, importFileString);
    }

    private void startImport(final File importFile, final String importFileString, String importLabel) {
        if (importFile != null && !importFile.exists()) {
            GedcomImportPanel.this.add(new JLabel("File not found"));
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
            abstractDiagramManager.createDiagramContainer(titleString, GedcomImportPanel.this);
            progressBar = new JProgressBar(0, 100);
            endPagePanel = new JPanel(new BorderLayout());
            endPagePanel.add(progressBar, BorderLayout.PAGE_START);
            GedcomImportPanel.this.add(endPagePanel, BorderLayout.PAGE_END);
            progressBar.setVisible(true);
            JPanel topPanel = new JPanel();
            // todo: any existing files are always being overwritten and the entity id also being changed so existing relations will be broken, maybe prevent overwritting all entities for an import file?
            // todo: it might be better to check for a file already exsiting and if it does load it and strip out the relations and metadata that would be replaced by the import?
            overwriteOnImport = new JCheckBox("Overwrite Existing");
            overwriteOnImport.setEnabled(false);
            startButton = new JButton("Start");
            topPanel.add(overwriteOnImport);
            validateImportedXml = new JCheckBox("Validate Xml");
            topPanel.add(validateImportedXml);
            topPanel.add(startButton);
            JPanel topOuterPanel = new JPanel(new BorderLayout());
            topOuterPanel.add(new JLabel(importLabel, JLabel.CENTER), BorderLayout.PAGE_START);
            topOuterPanel.add(topPanel, BorderLayout.CENTER);
            GedcomImportPanel.this.add(topOuterPanel, BorderLayout.PAGE_START);
            startButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    startButton.setEnabled(false);
                    overwriteOnImport.setEnabled(false);
                    validateImportedXml.setEnabled(false);
                    new Thread() {

                        @Override
                        public void run() {
                            boolean overwriteExisting = overwriteOnImport.isSelected();
                            GenericImporter genericImporter = new GedcomImporter(progressBar, importTextArea, overwriteExisting, sessionStorage, bugCatcher);
                            if (importFileString != null) {
                                if (!genericImporter.canImport(importFileString)) {
                                    genericImporter = new CsvImporter(progressBar, importTextArea, overwriteExisting, sessionStorage, bugCatcher);
                                }
                            } else {
                                if (!genericImporter.canImport(importFile.toString())) {
                                    genericImporter = new CsvImporter(progressBar, importTextArea, overwriteExisting, sessionStorage, bugCatcher);
                                }
                            }
                            URI[] treeNodesArray;
                            importTextArea.append("Importing the kinship data (step 1/4)\n");
                            importTextArea.setCaretPosition(importTextArea.getText().length());
                            if (importFileString != null) {
                                treeNodesArray = genericImporter.importFile(importFileString);
                            } else {
                                treeNodesArray = genericImporter.importFile(importFile);
                            }
                            boolean checkFilesAfterImport = validateImportedXml.isSelected();
                            if (treeNodesArray != null && checkFilesAfterImport) {
//                    ArrayList<ImdiTreeObject> tempArray = new ArrayList<ImdiTreeObject>();                    
                                int maxXsdErrorToShow = 3;
                                importTextArea.append("Checking XML of imported data  (step 3/4)\n");
                                importTextArea.setCaretPosition(importTextArea.getText().length());
                                progressBar.setValue(0);
                                progressBar.setMaximum(treeNodesArray.length + 1);
                                for (URI currentNodeUri : treeNodesArray) {
                                    progressBar.setValue(progressBar.getValue() + 1);
                                    if (maxXsdErrorToShow > 0) {
//                        try {
//                            ImdiTreeObject currentImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString));
//                            tempArray.add(currentImdiObject);
//                            JTextPane fileText = new JTextPane();
                                        XsdChecker xsdChecker = new XsdChecker();
                                        if (xsdChecker.simpleCheck(new File(currentNodeUri), currentNodeUri) != null) {
                                            abstractDiagramManager.createDiagramSubPanel("XSD Error on Import", xsdChecker);
                                            xsdChecker.checkXML(dataNodeLoader.getArbilDataNode(null, currentNodeUri));
                                            xsdChecker.setDividerLocation(0.5);
                                            maxXsdErrorToShow--;
                                            if (maxXsdErrorToShow <= 0) {
                                                importTextArea.append("maximum xsd errors shown, no more files will be tested" + "\n");
                                            }
                                        }
//                            currentImdiObject.reloadNode();
//                            try {
//                                fileText.setPage(currentNodeString);
//                            } catch (IOException iOException) {
//                                fileText.setText(iOException.getMessage());
//                            }
//                            jTabbedPane1.add("ImportedFile", fileText);
//                        } catch (URISyntaxException exception) {
//                            GuiHelper.linorgBugCatcher.logError(exception);
//                        }
                                        // todo: possibly create a new diagram with a sample of the imported entities for the user
                                    }
                                }
//                    leftTree.rootNodeChildren = tempArray.toArray(new ImdiTreeObject[]{});
//                    imdiTableModel.removeAllImdiRows();
//                    imdiTableModel.addImdiObjects(leftTree.rootNodeChildren);
                            } else {
                                importTextArea.append("Skipping check XML of imported data  (step 3/4)\n");
                            }
                            progressBar.setIndeterminate(true);
                            // todo: it might be more efficient to only update the new files
                            importTextArea.append("Starting update of entity database (step 4/4)\n");
                            importTextArea.setCaretPosition(importTextArea.getText().length());
                            entityCollection.updateDatabase(treeNodesArray, progressBar);
                            importTextArea.append("Import complete" + "\n");
                            importTextArea.setCaretPosition(importTextArea.getText().length());
//                            System.out.println("added the imported files to the database");
                            progressBar.setIndeterminate(false);
                            progressBar.setVisible(false);
//                leftTree.requestResort();
//                GraphData graphData = new GraphData();
//                graphData.readData();
//                graphPanel.drawNodes(graphData);
                            GedcomImportPanel.this.endPagePanel.add(GedcomImportPanel.this.getCreatedNodesPane(genericImporter), BorderLayout.CENTER);
                            GedcomImportPanel.this.revalidate();
                        }
                    }.start();
                }
            });
        }
    }
}
