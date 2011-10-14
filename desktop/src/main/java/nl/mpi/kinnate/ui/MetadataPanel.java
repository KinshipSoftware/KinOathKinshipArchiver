package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNode;
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
    private ArbilTableModel arbilTableModel;
    private HidePane editorHidePane;
    private ArrayList<ArbilDataNode> tableNodes = new ArrayList<ArbilDataNode>();

    public MetadataPanel(GraphPanel graphPanel, HidePane editorHidePane, TableCellDragHandler tableCellDragHandler) {
                // todo: #1101	The metadata pane should always be available rather then for specific diagrams.
        this.kinTree =  new KinTree(graphPanel);
        this.arbilTableModel = new ArbilTableModel();
        ArbilTable imdiTable = new ArbilTable(arbilTableModel, "Selected Nodes");

        imdiTable.setTransferHandler(tableCellDragHandler);
        imdiTable.setDragEnabled(true);
        
        this.editorHidePane = editorHidePane;
        this.setLayout(new BorderLayout());
        JScrollPane tableScrollPane = new JScrollPane(imdiTable);
        this.add(tableScrollPane, BorderLayout.CENTER);
        this.add(kinTree, BorderLayout.LINE_START);

    }

    public void removeAllArbilDataNodeRows() {
        arbilTableModel.removeAllArbilDataNodeRows();
        tableNodes.clear();
    }

    public void addSingleArbilDataNode(ArbilDataNode arbilDataNode) {
        arbilTableModel.addSingleArbilDataNode(arbilDataNode);
        tableNodes.add(arbilDataNode);
    }

    public void addTab(String labelString, Component elementEditor) {
        editorHidePane.addTab(labelString, elementEditor);
    }

    public void removeTab(Component elementEditor) {
        editorHidePane.remove(elementEditor);
    }

    private void showMetadata() {
        addTab("Metadata", this);
    }

    private void hideMetadata() {
        removeTab(this);
    }

    public void updateEditorPane() {
        // todo: add only imdi nodes to the tree and the root node of them
        // todo: maybe have a table for entities and one for achive metdata
        if (arbilTableModel.getArbilDataNodeCount() > 0) {
            showMetadata();
        } else {
            hideMetadata();
        }
        boolean showEditor = editorHidePane.getComponentCount() > 0;
        kinTree.rootNodeChildren = tableNodes.toArray(new ArbilDataNode[]{});
        kinTree.requestResort();
        if (showEditor && editorHidePane.isHidden()) {
            editorHidePane.toggleHiddenState();
        }
        editorHidePane.setVisible(showEditor);
    }
}
