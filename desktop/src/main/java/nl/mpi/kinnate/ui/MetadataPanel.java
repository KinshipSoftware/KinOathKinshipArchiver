package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 * Document : MetadataPanel
 * Created on : Oct 13, 2011, 5:06:28 PM
 * Author : Peter Withers
 */
public class MetadataPanel extends JPanel {

    private ArbilTree arbilTree;
//    JScrollPane tableScrollPane;
    private ArbilTableModel kinTableModel;
    private ArbilTableModel archiveTableModel;
    private JScrollPane kinTableScrollPane;
    private HidePane editorHidePane;
    private ArrayList<ArbilDataNode> metadataNodes = new ArrayList<ArbilDataNode>();
    private ArrayList<ArbilDataNode> archiveTreeNodes = new ArrayList<ArbilDataNode>();
    private ArrayList<ArbilDataNode> archiveRootNodes = new ArrayList<ArbilDataNode>();
    private ArrayList<SvgElementEditor> elementEditors = new ArrayList<SvgElementEditor>();
    //private DateEditorPanel dateEditorPanel;
    private ArbilDataNodeLoader dataNodeLoader;
    private ContainerNode rootNode;

    public MetadataPanel(GraphPanel graphPanel, HidePane editorHidePane, TableCellDragHandler tableCellDragHandler, ArbilDataNodeLoader dataNodeLoader) {
        this.arbilTree = new ArbilTree();
        rootNode = new ContainerNode("links", null, new ArbilNode[]{});
        arbilTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode(rootNode)));
        this.kinTableModel = new ArbilTableModel();
        this.archiveTableModel = new ArbilTableModel();
        this.dataNodeLoader = dataNodeLoader;
        //dateEditorPanel = new DateEditorPanel();
        ArbilTable kinTable = new ArbilTable(kinTableModel, "Selected Nodes");
        ArbilTable archiveTable = new ArbilTable(archiveTableModel, "Selected Nodes");
        this.arbilTree.setCustomPreviewTable(archiveTable);
        kinTable.setTransferHandler(tableCellDragHandler);
        kinTable.setDragEnabled(true);

        this.editorHidePane = editorHidePane;
        this.setLayout(new BorderLayout());
        kinTableScrollPane = new JScrollPane(kinTable);
        JScrollPane archiveTableScrollPane = new JScrollPane(archiveTable);
        this.add(archiveTableScrollPane, BorderLayout.CENTER);
        this.add(arbilTree, BorderLayout.LINE_START);
    }

    public void removeAllEditors() {
        while (!elementEditors.isEmpty()) {
            editorHidePane.removeTab(elementEditors.remove(0));
        }
    }

    public void removeAllArbilDataNodeRows() {
        kinTableModel.removeAllArbilDataNodeRows();
        archiveTableModel.removeAllArbilDataNodeRows();
        for (ArbilDataNode arbilDataNode : metadataNodes) {
            if (arbilDataNode.getParentDomNode().getNeedsSaveToDisk(false)) {
                // reloading will first check if a save is required then save and reload
                dataNodeLoader.requestReload((ArbilDataNode) arbilDataNode.getParentDomNode());
            }
        }
        metadataNodes.clear();
    }

    public void addArbilDataNode(ArbilDataNode arbilDataNode) {
        archiveTableModel.addSingleArbilDataNode(arbilDataNode);
        archiveRootNodes.clear(); // do not show the tree for archive tree selections
        metadataNodes.add(arbilDataNode);
    }

    public void addEntityDataNode(KinDiagramPanel kinDiagramPanel, EntityData entityData) {
        String entityPath = entityData.getEntityPath();
        if (entityPath != null && entityPath.length() > 0) {
            try {
                ArbilDataNode arbilDataNode = dataNodeLoader.getArbilDataNode(null, new URI(entityPath));
                // register this node with the graph panel
                kinDiagramPanel.registerArbilNode(entityData.getUniqueIdentifier(), arbilDataNode);
                kinTableModel.addSingleArbilDataNode(arbilDataNode);
                metadataNodes.add(arbilDataNode);
                // add the corpus links to the other table
                if (entityData.archiveLinkArray != null) {
                    for (URI archiveLink : entityData.archiveLinkArray) {
                        ArbilDataNode archiveLinkNode = dataNodeLoader.getArbilDataNode(null, archiveLink);
                        // todo: we do not register this node with the graph panel because it is not rendered on the graph, but if the name of the node changes then it should be updated in the tree which is not yet handled
                        archiveTableModel.addSingleArbilDataNode(archiveLinkNode);
                        archiveTreeNodes.add(archiveLinkNode);
                        archiveRootNodes.add(archiveLinkNode.getParentDomNode());
                        metadataNodes.add(archiveLinkNode);
                    }
                }
            } catch (URISyntaxException urise) {
                BugCatcherManager.getBugCatcher().logError(urise);
            }
        }
    }

//    public void setDateEditorEntities(ArrayList<EntityData> selectedEntities) {
//        if (selectedEntities.isEmpty()) {
//            editorHidePane.removeTab(dateEditorPanel);
//        } else {
//            dateEditorPanel.setEntities(selectedEntities);
//            editorHidePane.addTab("Date Editor", dateEditorPanel);
//        }
//    }
    public void addTab(String labelString, SvgElementEditor elementEditor) {
        editorHidePane.addTab(labelString, elementEditor);
        elementEditors.add(elementEditor);
    }

    public void removeTab(Component elementEditor) {
        editorHidePane.removeTab(elementEditor);
    }

    public void updateEditorPane() {
        // todo: add only imdi nodes to the tree and the root node of them
        // todo: maybe have a table for entities and one for achive metdata
        if (archiveTableModel.getArbilDataNodeCount() > 0) {
            editorHidePane.addTab("External Links", this);
        } else {
            removeTab(this);
        }
        if (kinTableModel.getArbilDataNodeCount() > 0) {
            editorHidePane.addTab("Kinship Data", kinTableScrollPane);
            editorHidePane.setSelectedComponent(kinTableScrollPane);
        } else {
            removeTab(kinTableScrollPane);
        }
        if (!archiveRootNodes.isEmpty()) {
            // todo: highlight or select the sub nodes that are actually linked
            rootNode.setChildNodes(archiveRootNodes.toArray(new ArbilNode[]{}));
            arbilTree.requestResort();
        }
        arbilTree.setVisible(!archiveRootNodes.isEmpty());
        editorHidePane.setHiddeState();
    }
}
