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
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : EgoSelectionPanel
 *  Created on : Sep 29, 2010, 13:12:01 PM
 *  Author     : Peter Withers
 */
public class EgoSelectionPanel extends JPanel {

    private KinTree egoTree;
    private KinTree requiredTree;
    private KinTree impliedTree;
    private KinTree transientTree;
    JPanel labelPanel3;
    JScrollPane metadataNodeScrolPane;
    JScrollPane transientNodeScrolPane;

    public EgoSelectionPanel(ArbilTable previewTable, GraphPanel graphPanel) {
        JPanel metadataNodePanel;
        JPanel transientNodePanel;
        transientNodePanel = new JPanel(new BorderLayout());
        transientNodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transient Entities"));
        transientTree = new KinTree(graphPanel);
        transientTree.setBackground(transientNodePanel.getBackground());
        transientNodePanel.add(transientTree, BorderLayout.CENTER);
        transientNodeScrolPane = new JScrollPane(transientNodePanel);

        metadataNodePanel = new JPanel(new BorderLayout());
        egoTree = new KinTree(graphPanel);
//        egoTree.setRootVisible(false);
        egoTree.setCellRenderer(new ArbilTreeRenderer());

        requiredTree = new KinTree(graphPanel);
//        requiredTree.setRootVisible(false);
        requiredTree.setCellRenderer(new ArbilTreeRenderer());

        impliedTree = new KinTree(graphPanel);
//        impliedTree.setRootVisible(false);
        impliedTree.setCellRenderer(new ArbilTreeRenderer());

//        egoTree.setRootVisible(false);
        this.setLayout(new BorderLayout());
        JPanel treePanel2 = new JPanel(new BorderLayout());
        metadataNodeScrolPane = new JScrollPane(metadataNodePanel);
        JPanel labelPanel1 = new JPanel(new BorderLayout());
        JPanel labelPanel2 = new JPanel(new BorderLayout());
        labelPanel3 = new JPanel(new BorderLayout());
        labelPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Ego"));
        labelPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Required"));
        labelPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Implied"));
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

    public void setTransferHandler(KinDragTransferHandler dragTransferHandler) {
        for (KinTree currentTree : new KinTree[]{egoTree, requiredTree, impliedTree, transientTree}) {
            currentTree.setTransferHandler(dragTransferHandler);
            currentTree.setDragEnabled(true);
        }
    }

    public void setTreeNodes(HashSet<UniqueIdentifier> egoIdentifiers, HashSet<UniqueIdentifier> requiredEntityIdentifiers, EntityData[] allEntities, IndexerParameters indexerParameters) {
        this.remove(transientNodeScrolPane);
        this.add(metadataNodeScrolPane, BorderLayout.CENTER);
        this.revalidate();
        ArrayList<KinTreeNode> egoNodeArray = new ArrayList<KinTreeNode>();
        ArrayList<KinTreeNode> requiredNodeArray = new ArrayList<KinTreeNode>();
        ArrayList<KinTreeNode> remainingNodeArray = new ArrayList<KinTreeNode>();
        for (EntityData entityData : allEntities) {
            if (entityData.isVisible) {
                KinTreeNode kinTreeNode = new KinTreeNode(entityData, indexerParameters);
                if (entityData.isEgo || egoIdentifiers.contains(entityData.getUniqueIdentifier())) {
                    egoNodeArray.add(kinTreeNode);
                } else if (requiredEntityIdentifiers.contains(entityData.getUniqueIdentifier())) {
                    requiredNodeArray.add(kinTreeNode);
                } else {
                    remainingNodeArray.add(kinTreeNode);
                }
            }
        }
        egoTree.rootNodeChildren = egoNodeArray.toArray(new ArbilNode[]{});
        egoTree.requestResort();
        requiredTree.rootNodeChildren = requiredNodeArray.toArray(new ArbilNode[]{});
        requiredTree.requestResort();
        impliedTree.rootNodeChildren = remainingNodeArray.toArray(new ArbilNode[]{});
        impliedTree.requestResort();
        transientTree.rootNodeChildren = new ArbilNode[]{};
        transientTree.requestResort();
    }

    public void setTransientNodes(EntityData[] allEntities) {
        this.remove(metadataNodeScrolPane);
        this.add(transientNodeScrolPane, BorderLayout.CENTER);
        this.revalidate();
        ArrayList<KinTreeNode> transientNodeArray = new ArrayList<KinTreeNode>();
        for (EntityData entityData : allEntities) {
            KinTreeNode kinTreeNode = new KinTreeNode(entityData, null);
            transientNodeArray.add(kinTreeNode);
        }
        transientTree.rootNodeChildren = transientNodeArray.toArray(new ArbilNode[]{});
        transientTree.requestResort();

        egoTree.rootNodeChildren = new ArbilNode[]{};
        egoTree.requestResort();
        requiredTree.rootNodeChildren = new ArbilNode[]{};
        requiredTree.requestResort();
        impliedTree.rootNodeChildren = new ArbilNode[]{};
        impliedTree.requestResort();
    }
}
