package nl.mpi.kinnate;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.ImdiTable;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.ImdiTree;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.PreviewSplitPanel;
import nl.mpi.arbil.XsdChecker;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;
import nl.mpi.kinnate.EntityIndexer.EntityIndex;

/*
 *  Document   : MainFrame
 *  Author     : Peter Withers
 *  Created on : Aug 16, 2010, 5:20:20 PM
 */
public class MainFrame extends javax.swing.JFrame {

    private ImdiTree leftTree;
//    private GraphPanel graphPanel;
//    private JungGraph jungGraph;
    private ImdiTable previewTable;
    private ImdiTableModel imdiTableModel;
    private EntityIndex entityIndex;

    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();
        entityIndex = new EntityIndex();
        leftTree = new ImdiTree();
//        GraphPanel0 graphPanel0Deprecated;
//        graphPanel0Deprecated = new GraphPanel0();
//        graphPanel = new GraphPanel();
        // this data load should be elsewhere
//        GraphData graphData = new GraphData();
//        graphData.readData();
//        graphPanel.drawNodes(graphData);
//        jungGraph = new JungGraph();
        imdiTableModel = new ImdiTableModel();
        previewTable = new ImdiTable(imdiTableModel, "Preview Table");

        JScrollPane tableScrollPane = new JScrollPane(previewTable);
        jScrollPane1.getViewport().add(leftTree);
        KinTypeEgoSelectionTestPanel egoSelectionTestPanel = new KinTypeEgoSelectionTestPanel(entityIndex, new File("/Users/petwit/Documents/SharedInVirtualBox/mpi-co-svn-mpi-nl/LAT/Kinnate/trunk/src/main/resources/EgoSelection.svg"));
        jTabbedPane1.add("EgoSelection", egoSelectionTestPanel);
        jTabbedPane1.add("KinTypes", new KinTypeStringTestPanel());
        jTabbedPane1.add("Kin Term Mapping for KinType Strings", new KinTypeStringTestPanel());
        jTabbedPane1.add("Archive Entity Linker", new ArchiveEntityLinkerPanel());
//        jTabbedPane1.add("Graph", graphPanel);
//        jTabbedPane1.add("SVG2  (deprecated)", new GraphPanel1());
//        jTabbedPane1.add("Jung", jungGraph);
        jTabbedPane1.add("Table", tableScrollPane);
//        jTabbedPane1.add("SVG (deprecated)", graphPanel0Deprecated);
        ArrayList<URI> allEntityUris = new ArrayList<URI>();
        leftTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Test Tree"), true));
        String[] treeNodesArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
        if (treeNodesArray != null) {
            ArrayList<ImdiTreeObject> tempArray = new ArrayList<ImdiTreeObject>();
            for (String currentNodeString : treeNodesArray) {
                try {
                    ImdiTreeObject currentImdiNode = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString));
                    tempArray.add(currentImdiNode);
                    allEntityUris.add(currentImdiNode.getURI());
                } catch (URISyntaxException exception) {
                    System.err.println(exception.getMessage());
                    exception.printStackTrace();
                }
            }
            ImdiTreeObject[] allEntities = tempArray.toArray(new ImdiTreeObject[]{});
            leftTree.rootNodeChildren = allEntities;
            entityIndex.indexEntities(allEntityUris.toArray(new URI[]{}));
            imdiTableModel.removeAllImdiRows();
            imdiTableModel.addImdiObjects(leftTree.rootNodeChildren);
        } //else {
        //   leftTree.rootNodeChildren = new ImdiTreeObject[]{graphPanel.imdiNode};
        // }
        leftTree.requestResort();

        PreviewSplitPanel.previewTable = previewTable;
        PreviewSplitPanel.previewTableShown = true;

        jSplitPane1.setDividerLocation(0.25);

//        System.out.println();
        DragTransferHandler dragTransferHandler = new DragTransferHandler();
        leftTree.setTransferHandler(dragTransferHandler);
        egoSelectionTestPanel.setTransferHandler(dragTransferHandler);

        this.doLayout();
        this.pack();
    }

    private void startImport(final String importFileString) {
        new Thread() {

            @Override
            public void run() {
                JTextArea importTextArea = new JTextArea();
                JScrollPane importScrollPane = new JScrollPane(importTextArea);
                jTabbedPane1.add("Import", importScrollPane);
                jTabbedPane1.setSelectedComponent(importScrollPane);
                JProgressBar progressBar = new JProgressBar();
                progressBar.setVisible(true);
                new GedcomImporter().importTestFile(importTextArea, importFileString);
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
                    imdiTableModel.removeAllImdiRows();
                    imdiTableModel.addImdiObjects(leftTree.rootNodeChildren);
                }
                leftTree.requestResort();
//                GraphData graphData = new GraphData();
//                graphData.readData();
//                graphPanel.drawNodes(graphData);
            }
        }.start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        importGedcomTorture = new javax.swing.JMenuItem();
        importGedcomSimple = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        openDiagram = new javax.swing.JMenuItem();
        openRecentMenu = new javax.swing.JMenu();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        saveDiagram = new javax.swing.JMenuItem();
        saveDiagramAs = new javax.swing.JMenuItem();
        exitApplication = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane1.setLeftComponent(jScrollPane1);
        jSplitPane1.setRightComponent(jTabbedPane1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");
        fileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuActionPerformed(evt);
            }
        });

        importGedcomTorture.setText("Import Gedcom Torture File");
        importGedcomTorture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importGedcomTortureActionPerformed(evt);
            }
        });
        fileMenu.add(importGedcomTorture);

        importGedcomSimple.setText("Import Gedcom Simple File");
        importGedcomSimple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importGedcomSimpleActionPerformed(evt);
            }
        });
        fileMenu.add(importGedcomSimple);
        fileMenu.add(jSeparator1);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("New");
        fileMenu.add(jMenuItem1);

        openDiagram.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openDiagram.setText("Open");
        openDiagram.setActionCommand("open");
        openDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDiagramActionPerformed(evt);
            }
        });
        fileMenu.add(openDiagram);

        openRecentMenu.setText("Open Recent Diagram");
        openRecentMenu.setEnabled(false);
        openRecentMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openRecentMenuActionPerformed(evt);
            }
        });
        fileMenu.add(openRecentMenu);
        fileMenu.add(jSeparator2);

        saveDiagram.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveDiagram.setText("Save");
        saveDiagram.setActionCommand("save");
        saveDiagram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDiagramActionPerformed(evt);
            }
        });
        fileMenu.add(saveDiagram);

        saveDiagramAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveDiagramAs.setText("Save As");
        saveDiagramAs.setActionCommand("saveas");
        saveDiagramAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDiagramAsActionPerformed(evt);
            }
        });
        fileMenu.add(saveDiagramAs);

        exitApplication.setText("Exit");
        exitApplication.setActionCommand("exit");
        exitApplication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitApplicationActionPerformed(evt);
            }
        });
        fileMenu.add(exitApplication);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");
        jMenuBar1.add(editMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void importGedcomTortureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importGedcomTortureActionPerformed
        // TODO add your handling code here:
        startImport("/TestGED/TGC55C.ged");
    }//GEN-LAST:event_importGedcomTortureActionPerformed

    private void importGedcomSimpleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importGedcomSimpleActionPerformed
        // TODO add your handling code here:
        startImport("/TestGED/wiki-test-ged.ged");
    }//GEN-LAST:event_importGedcomSimpleActionPerformed

    private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fileMenuActionPerformed

    private void openDiagramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDiagramActionPerformed
        for (File selectedFile : LinorgWindowManager.getSingleInstance().showFileSelectBox("Open Kin Diagram", false, true, false)) {
            KinTypeEgoSelectionTestPanel egoSelectionTestPanel = new KinTypeEgoSelectionTestPanel(entityIndex, selectedFile);
            jTabbedPane1.add(selectedFile.getName(), egoSelectionTestPanel);
        }
    }//GEN-LAST:event_openDiagramActionPerformed

    private void openRecentMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openRecentMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_openRecentMenuActionPerformed

    private void saveDiagramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDiagramActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveDiagramActionPerformed

    private void saveDiagramAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDiagramAsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saveDiagramAsActionPerformed

    private void exitApplicationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitApplicationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_exitApplicationActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitApplication;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem importGedcomSimple;
    private javax.swing.JMenuItem importGedcomTorture;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JMenuItem openDiagram;
    private javax.swing.JMenu openRecentMenu;
    private javax.swing.JMenuItem saveDiagram;
    private javax.swing.JMenuItem saveDiagramAs;
    // End of variables declaration//GEN-END:variables
}
