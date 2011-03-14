package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import nl.mpi.arbil.ImdiTree;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.XsdChecker;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;
import nl.mpi.kinnate.gedcomimport.GedcomImporter;

/**
 *  Document   : GedcomImportPanel
 *  Created on : Mar 14, 2011, 8:38:36 AM
 *  Author     : Peter Withers
 */
public class GedcomImportPanel extends JPanel {

    private ImdiTree leftTree;
    private JTabbedPane jTabbedPane1;

    public GedcomImportPanel(ImdiTree leftTreeLocal, JTabbedPane jTabbedPaneLocal) {
        jTabbedPane1 = jTabbedPaneLocal;
        leftTree = leftTreeLocal;

//        private ImdiTree leftTree;
////    private GraphPanel graphPanel;
////    private JungGraph jungGraph;
//    private ImdiTable previewTable;
//    private ImdiTableModel imdiTableModel;
//    private DragTransferHandler dragTransferHandler;
    }

    public void startImport(File importFile) {
        startImport(importFile, null);
    }

    public void startImport(String importFileString) {
        startImport(null, importFileString);
    }

    public void startImport(final File importFile, final String importFileString) {
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
                GedcomImporter gedcomImporter = new GedcomImporter();
                gedcomImporter.setProgressBar(progressBar);
                if (importFileString != null) {
                    gedcomImporter.importTestFile(importTextArea, importFileString);
                } else {
                    gedcomImporter.importTestFile(importTextArea, importFile);
                }
                progressBar.setVisible(false);
                String[] treeNodesArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
                if (treeNodesArray != null) {
                    ArrayList<ImdiTreeObject> tempArray = new ArrayList<ImdiTreeObject>();
                    for (String currentNodeString : treeNodesArray) {
                        try {
                            ImdiTreeObject currentImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString));
                            tempArray.add(currentImdiObject);
//                            JTextPane fileText = new JTextPane();
                            XsdChecker xsdChecker = new XsdChecker();
                            if (xsdChecker.simpleCheck(currentImdiObject.getFile(), currentImdiObject.getURI()) != null) {
                                jTabbedPane1.add("XSD Error on Import", xsdChecker);
                                xsdChecker.checkXML(currentImdiObject);
                                xsdChecker.setDividerLocation(0.5);
                            }
                            currentImdiObject.reloadNode();
//                            try {
//                                fileText.setPage(currentNodeString);
//                            } catch (IOException iOException) {
//                                fileText.setText(iOException.getMessage());
//                            }
//                            jTabbedPane1.add("ImportedFile", fileText);
                        } catch (URISyntaxException exception) {
                            System.err.println(exception.getMessage());
                            exception.printStackTrace();
                        }
                    }
                    leftTree.rootNodeChildren = tempArray.toArray(new ImdiTreeObject[]{});
//                    imdiTableModel.removeAllImdiRows();
//                    imdiTableModel.addImdiObjects(leftTree.rootNodeChildren);
                }
                leftTree.requestResort();
//                GraphData graphData = new GraphData();
//                graphData.readData();
//                graphPanel.drawNodes(graphData);
            }
        }.start();
    }
}
