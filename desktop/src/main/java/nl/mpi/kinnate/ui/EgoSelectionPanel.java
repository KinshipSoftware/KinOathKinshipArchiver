package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTreeRenderer;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : EgoSelectionPanel
 *  Created on : Sep 29, 2010, 13:12:01 PM
 *  Author     : Peter Withers
 */
public class EgoSelectionPanel extends JPanel {

    private KinTree egoTree;
//    DefaultTreeModel egoModel;
//    DefaultMutableTreeNode egoRootNode;
    private KinTree requiredTree;
//    DefaultTreeModel requiredModel;
//    DefaultMutableTreeNode requiredRootNode;
    private KinTree impliedTree;
    private KinTree transientTree;
//    DefaultTreeModel impliedModel;
//    DefaultMutableTreeNode impliedRootNode;
    JPanel labelPanel3;
//    JLabel transientLabel;
    JScrollPane metadataNodeScrolPane;
    JScrollPane transientNodeScrolPane;

    public EgoSelectionPanel(ArbilTable previewTable) {
        JPanel metadataNodePanel;
        JPanel transientNodePanel;
        transientNodePanel = new JPanel(new BorderLayout());
        transientNodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transient Entities"));
        transientTree = new KinTree();
        transientTree.setBackground(transientNodePanel.getBackground());
        transientNodePanel.add(transientTree, BorderLayout.CENTER);
        transientNodeScrolPane = new JScrollPane(transientNodePanel);

        metadataNodePanel = new JPanel(new BorderLayout());
//        egoRootNode = new DefaultMutableTreeNode(new JLabel("Ego", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
//        egoModel = new DefaultTreeModel(egoRootNode);
//        egoTree = new JTree(egoModel);
        egoTree = new KinTree();
        // todo: add trac task to modify the arbil tree such that each tree can have its own preview table
//        egoTree.getModel().
//        egoTree.setRootVisible(false);
        egoTree.setCellRenderer(new ArbilTreeRenderer());

//        requiredRootNode = new DefaultMutableTreeNode(new JLabel("Required", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
//        requiredModel = new DefaultTreeModel(requiredRootNode);
//        requiredTree = new JTree(requiredModel);
        requiredTree = new KinTree();
//        requiredTree.setRootVisible(false);
        requiredTree.setCellRenderer(new ArbilTreeRenderer());

//        impliedRootNode = new DefaultMutableTreeNode(new JLabel("Implied", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
//        impliedModel = new DefaultTreeModel(impliedRootNode);
//        impliedTree = new JTree(impliedModel);
        impliedTree = new KinTree();
//        impliedTree.setRootVisible(false);
        impliedTree.setCellRenderer(new ArbilTreeRenderer());

//        egoTree.setRootVisible(false);
        this.setLayout(new BorderLayout());
        JPanel treePanel2 = new JPanel(new BorderLayout());
//        treePanel.setLayout(new BoxLayout(treePanel, BoxLayout.PAGE_AXIS));
        metadataNodeScrolPane = new JScrollPane(metadataNodePanel);
//        this.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Ego"));
        JPanel labelPanel1 = new JPanel(new BorderLayout());
        JPanel labelPanel2 = new JPanel(new BorderLayout());
        labelPanel3 = new JPanel(new BorderLayout());
//        JPanel labelPanel4 = new JPanel(new BorderLayout());
        labelPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Ego"));
        labelPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Required"));
        labelPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Implied"));
//        labelPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Transient"));
        labelPanel1.add(egoTree, BorderLayout.CENTER);
        labelPanel2.add(requiredTree, BorderLayout.CENTER);
        labelPanel3.add(impliedTree, BorderLayout.CENTER);
        egoTree.setBackground(labelPanel1.getBackground());
        requiredTree.setBackground(labelPanel1.getBackground());
        impliedTree.setBackground(labelPanel1.getBackground());
        metadataNodePanel.add(labelPanel1, BorderLayout.PAGE_START);
        metadataNodePanel.add(treePanel2, BorderLayout.CENTER);
        treePanel2.add(labelPanel2, BorderLayout.PAGE_START);
        treePanel2.add(labelPanel3, BorderLayout.CENTER);
        this.add(metadataNodeScrolPane, BorderLayout.CENTER);
        egoTree.setCustomPreviewTable(previewTable);
        requiredTree.setCustomPreviewTable(previewTable);
        impliedTree.setCustomPreviewTable(previewTable);
        transientTree.setCustomPreviewTable(previewTable);
    }

    public void setTreeNodes(HashSet<UniqueIdentifier> egoIdentifiers, HashSet<UniqueIdentifier> requiredEntityIdentifiers, EntityData[] allEntities) {
        this.remove(transientNodeScrolPane);
        this.add(metadataNodeScrolPane, BorderLayout.CENTER);
        this.revalidate();
        ArrayList<KinTreeNode> egoNodeArray = new ArrayList<KinTreeNode>();
        ArrayList<KinTreeNode> requiredNodeArray = new ArrayList<KinTreeNode>();
        ArrayList<KinTreeNode> remainingNodeArray = new ArrayList<KinTreeNode>();
        for (EntityData entityData : allEntities) {
            if (entityData.isVisible) {
                KinTreeNode kinTreeNode = new KinTreeNode(entityData);
                if (entityData.isEgo || egoIdentifiers.contains(entityData.getUniqueIdentifier())) {
                    egoNodeArray.add(kinTreeNode);
                } else if (requiredEntityIdentifiers.contains(entityData.getUniqueIdentifier())) {
                    requiredNodeArray.add(kinTreeNode);
                } else {
                    remainingNodeArray.add(kinTreeNode);
                }
            }
        }
//            setEgoNodes(egoNodeArray, egoTree, egoModel, egoRootNode);
        egoTree.rootNodeChildren = egoNodeArray.toArray(new ArbilNode[]{});
        egoTree.requestResort();
//            setEgoNodes(requiredNodeArray, requiredTree, requiredModel, requiredRootNode);
        requiredTree.rootNodeChildren = requiredNodeArray.toArray(new ArbilNode[]{});
        requiredTree.requestResort();
//            setEgoNodes(remainingNodeArray, impliedTree, impliedModel, impliedRootNode);
        impliedTree.rootNodeChildren = remainingNodeArray.toArray(new ArbilNode[]{});
        impliedTree.requestResort();
        transientTree.rootNodeChildren = new ArbilNode[]{};
        transientTree.requestResort();
//        if (transientLabel != null) {
//            labelPanel3.remove(transientLabel);
//        }
    }

    public void setTransientNodes(EntityData[] allEntities) {
        this.remove(metadataNodeScrolPane);
        this.add(transientNodeScrolPane, BorderLayout.CENTER);
        this.revalidate();
        ArrayList<KinTreeNode> transientNodeArray = new ArrayList<KinTreeNode>();
        for (EntityData entityData : allEntities) {
            KinTreeNode kinTreeNode = new KinTreeNode(entityData);
            transientNodeArray.add(kinTreeNode);
        }
        transientTree.rootNodeChildren = transientNodeArray.toArray(new ArbilNode[]{});
        transientTree.requestResort();

        egoTree.rootNodeChildren = new ArbilNode[]{};
        egoTree.requestResort();
//            setEgoNodes(requiredNodeArray, requiredTree, requiredModel, requiredRootNode);
        requiredTree.rootNodeChildren = new ArbilNode[]{};
        requiredTree.requestResort();
//            setEgoNodes(remainingNodeArray, impliedTree, impliedModel, impliedRootNode);
        impliedTree.rootNodeChildren = new ArbilNode[]{};
        impliedTree.requestResort();


//        transientLabel = new JLabel(allEntities.length + " transient entities have been generated");
//        labelPanel3.add(transientLabel, BorderLayout.PAGE_START);
    }
//    private void setEgoNodes(ArrayList<ArbilDataNode> selectedNodes, JTree currentTree, DefaultTreeModel currentModel, DefaultMutableTreeNode currentRootNode) {
//        currentRootNode.removeAllChildren();
//        currentModel.nodeStructureChanged(currentRootNode);
//        if (selectedNodes != null) {
//            for (ArbilDataNode imdiTreeObject : selectedNodes) {
////                System.out.println("adding node: " + imdiTreeObject.toString());
//                currentModel.insertNodeInto(new DefaultMutableTreeNode(imdiTreeObject), currentRootNode, 0);
//            }
//            currentTree.expandPath(new TreePath(currentRootNode.getPath()));
//        }
//    }
}
