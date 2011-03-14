package nl.mpi.kinnate.ui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
public class GedcomImportPanel {

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
                jTabbedPane1.add("Import", importScrollPane);
                jTabbedPane1.setSelectedComponent(importScrollPane);
                JProgressBar progressBar = new JProgressBar();
                progressBar.setVisible(true);
                if (importFileString != null) {
                    new GedcomImporter().importTestFile(importTextArea, importFileString);
                } else {
                    new GedcomImporter().importTestFile(importTextArea, importFile);
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
