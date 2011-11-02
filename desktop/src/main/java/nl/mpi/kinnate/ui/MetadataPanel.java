package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 *  Document   : MetadataPanel
 *  Created on : Oct 13, 2011, 5:06:28 PM
 *  Author     : Peter Withers
 */
public class MetadataPanel extends JPanel {

    private KinTree kinTree;
//    JScrollPane tableScrollPane;
    private ArbilTableModel kinTableModel;
    private ArbilTableModel archiveTableModel;
    private JScrollPane kinTableScrollPane;
    private HidePane editorHidePane;
    private ArrayList<ArbilDataNode> archiveNodes = new ArrayList<ArbilDataNode>();

    public MetadataPanel(GraphPanel graphPanel, HidePane editorHidePane, TableCellDragHandler tableCellDragHandler) {
        this.kinTree = new KinTree(graphPanel);
        this.kinTableModel = new ArbilTableModel();
        this.archiveTableModel = new ArbilTableModel();
        ArbilTable kinTable = new ArbilTable(kinTableModel, "Selected Nodes");
        ArbilTable archiveTable = new ArbilTable(archiveTableModel, "Selected Nodes");
        kinTable.setTransferHandler(tableCellDragHandler);
        kinTable.setDragEnabled(true);

        this.editorHidePane = editorHidePane;
        this.setLayout(new BorderLayout());
        kinTableScrollPane = new JScrollPane(kinTable);
        JScrollPane archiveTableScrollPane = new JScrollPane(archiveTable);
        this.add(archiveTableScrollPane, BorderLayout.CENTER);
        this.add(kinTree, BorderLayout.LINE_START);
    }

    public void removeAllArbilDataNodeRows() {
        kinTableModel.removeAllArbilDataNodeRows();
        archiveTableModel.removeAllArbilDataNodeRows();
        for (ArbilDataNode arbilDataNode : archiveNodes) {
            if (arbilDataNode.getParentDomNode().getNeedsSaveToDisk(false)) {
                // reloading will first check if a save is required then save and reload
                ArbilDataNodeLoader.getSingleInstance().requestReload((ArbilDataNode) arbilDataNode.getParentDomNode());
            }
        }
        archiveNodes.clear();
    }

    public void addSingleArbilDataNode(ArbilDataNode arbilDataNode) {
        kinTableModel.addSingleArbilDataNode(arbilDataNode);
//        if (arbilDataNode instanceof KinTreeNode)
        archiveTableModel.addSingleArbilDataNode(arbilDataNode);
        archiveNodes.add(arbilDataNode);
    }

    public void addTab(String labelString, Component elementEditor) {
        editorHidePane.addTab(labelString, elementEditor);
    }

    public void removeTab(Component elementEditor) {
        editorHidePane.removeTab(elementEditor);
    }

    public void updateEditorPane() {
        // todo: add only imdi nodes to the tree and the root node of them
        // todo: maybe have a table for entities and one for achive metdata
        if (archiveTableModel.getArbilDataNodeCount() > 0) {
            addTab("Archive Links", this);
        } else {
            removeTab(this);
        }
        if (kinTableModel.getArbilDataNodeCount() > 0) {
            addTab("Metadata", kinTableScrollPane);
        } else {
            removeTab(kinTableScrollPane);
        }
        kinTree.rootNodeChildren = archiveNodes.toArray(new ArbilDataNode[]{});
        kinTree.requestResort();
        editorHidePane.setHiddeState();
    }
}
