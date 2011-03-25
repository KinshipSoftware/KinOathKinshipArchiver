package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URI;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.XsdChecker;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.gedcomimport.GedcomImporter;

/**
 *  Document   : GedcomImportPanel
 *  Created on : Mar 14, 2011, 8:38:36 AM
 *  Author     : Peter Withers
 */
public class GedcomImportPanel extends JPanel {

    private EntityCollection entityCollection;
    private JTabbedPane jTabbedPane1;

    public GedcomImportPanel(EntityCollection entityCollectionLocal, JTabbedPane jTabbedPaneLocal) {
        jTabbedPane1 = jTabbedPaneLocal;
        entityCollection = entityCollectionLocal;

//        private ImdiTree leftTree;
////    private GraphPanel graphPanel;
////    private JungGraph jungGraph;
//    private ImdiTable previewTable;
//    private ImdiTableModel imdiTableModel;
//    private DragTransferHandler dragTransferHandler;
    }

    public void startImport(File importFile, boolean overwriteExisting) {
        startImport(importFile, null, overwriteExisting);
    }

    public void startImport(String importFileString, boolean overwriteExisting) {
        startImport(null, importFileString, overwriteExisting);
    }

    public void startImport(final File importFile, final String importFileString, final boolean overwriteExisting) {
        new Thread() {

            @Override
            public void run() {
                JTextArea importTextArea = new JTextArea();
                JScrollPane importScrollPane = new JScrollPane(importTextArea);
                GedcomImportPanel.this.setLayout(new BorderLayout());
                GedcomImportPanel.this.add(importScrollPane, BorderLayout.CENTER);
                jTabbedPane1.add("Import", GedcomImportPanel.this);
                jTabbedPane1.setSelectedComponent(GedcomImportPanel.this);
                JProgressBar progressBar = new JProgressBar(0, 100);
                GedcomImportPanel.this.add(progressBar, BorderLayout.PAGE_END);
                progressBar.setVisible(true);
                GedcomImporter gedcomImporter = new GedcomImporter(overwriteExisting);
                gedcomImporter.setProgressBar(progressBar);
                URI[] treeNodesArray;
                if (importFileString != null) {
                    treeNodesArray = gedcomImporter.importTestFile(importTextArea, importFileString);
                } else {
                    treeNodesArray = gedcomImporter.importTestFile(importTextArea, importFile);
                }
                progressBar.setVisible(false);
                if (treeNodesArray != null) {
//                    ArrayList<ImdiTreeObject> tempArray = new ArrayList<ImdiTreeObject>();                    
                    int maxXsdErrorToShow = 3;
                    for (URI currentNodeUri : treeNodesArray) {
                        if (maxXsdErrorToShow > 0) {
//                        try {
//                            ImdiTreeObject currentImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString));
//                            tempArray.add(currentImdiObject);
//                            JTextPane fileText = new JTextPane();
                            XsdChecker xsdChecker = new XsdChecker();
                            if (xsdChecker.simpleCheck(new File(currentNodeUri), currentNodeUri) != null) {
                                jTabbedPane1.add("XSD Error on Import", xsdChecker);
                                xsdChecker.checkXML(ImdiLoader.getSingleInstance().getImdiObject(null, currentNodeUri));
                                xsdChecker.setDividerLocation(0.5);
                                maxXsdErrorToShow--;
                                if (maxXsdErrorToShow == 0) {
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
                    // todo: it might be more efficient to only update the new files
                    importTextArea.append("starting update of entity database" + "\n");
                    entityCollection.createDatabase();
                    importTextArea.append("updated entity database" + "\n");
                    importTextArea.setCaretPosition(importTextArea.getText().length());
                    System.out.println("created new database");
//                    leftTree.rootNodeChildren = tempArray.toArray(new ImdiTreeObject[]{});
//                    imdiTableModel.removeAllImdiRows();
//                    imdiTableModel.addImdiObjects(leftTree.rootNodeChildren);
                }
//                leftTree.requestResort();
//                GraphData graphData = new GraphData();
//                graphData.readData();
//                graphPanel.drawNodes(graphData);
            }
        }.start();
    }
}
