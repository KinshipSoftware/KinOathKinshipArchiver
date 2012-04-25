package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.arbil.ui.ArbilTreeRenderer;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.data.KinTreeNode;
import nl.mpi.kinnate.entityindexer.EntityCollection;
import nl.mpi.kinnate.entityindexer.IndexerParameters;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : EgoSelectionPanel
 * Created on : Sep 29, 2010, 13:12:01 PM
 * Author : Peter Withers
 */
public class EgoSelectionPanel extends JPanel implements ActionListener {

    private KinTree egoTree;
    private ContainerNode egoNode;
    private KinTree requiredTree;
    private ContainerNode requiredNode;
    private KinTree impliedTree;
    private ContainerNode impliedNode;
    private KinTree transientTree;
    private ContainerNode transientNode;
    JPanel labelPanel3;
    JScrollPane metadataNodeScrolPane;
    JScrollPane transientNodeScrolPane;
    private MessageDialogHandler dialogHandler;
    private EntityCollection entityCollection;
    private ArbilDataNodeLoader dataNodeLoader;

    public EgoSelectionPanel(KinDiagramPanel kinDiagramPanel, GraphPanel graphPanel, MessageDialogHandler dialogHandler, EntityCollection entityCollection, ArbilDataNodeLoader dataNodeLoader) {
        this.dialogHandler = dialogHandler;
        this.entityCollection = entityCollection;
        this.dataNodeLoader = dataNodeLoader;
        JPanel metadataNodePanel;
        JPanel transientNodePanel;
        transientNodePanel = new JPanel(new BorderLayout());
        transientNodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transient Entities"));

        JButton convertTransientButton = new JButton("Convert to Database Diagram");
        convertTransientButton.setActionCommand("convert");
        convertTransientButton.addActionListener(this);
        convertTransientButton.setEnabled(false);
        transientNodePanel.add(convertTransientButton, BorderLayout.PAGE_END);

        transientNode = new ContainerNode("transient", null, new ArbilNode[]{});
        transientTree = new KinTree(kinDiagramPanel, graphPanel, transientNode);
        transientTree.setBackground(transientNodePanel.getBackground());
        transientNodePanel.add(transientTree, BorderLayout.CENTER);
        transientNodeScrolPane = new JScrollPane(transientNodePanel);

        metadataNodePanel = new JPanel(new BorderLayout());
        egoNode = new ContainerNode("ego", null, new ArbilNode[]{});
        egoTree = new KinTree(kinDiagramPanel, graphPanel, egoNode);
        egoTree.setCellRenderer(new ArbilTreeRenderer());
        requiredNode = new ContainerNode("required", null, new ArbilNode[]{});
        requiredTree = new KinTree(kinDiagramPanel, graphPanel, requiredNode);
        requiredTree.setCellRenderer(new ArbilTreeRenderer());
        impliedNode = new ContainerNode("implied", null, new ArbilNode[]{});
        impliedTree = new KinTree(kinDiagramPanel, graphPanel, impliedNode);
        impliedTree.setCellRenderer(new ArbilTreeRenderer());

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
//        egoTree.setCustomPreviewTable(previewTable);
//        requiredTree.setCustomPreviewTable(previewTable);
//        impliedTree.setCustomPreviewTable(previewTable);
//        transientTree.setCustomPreviewTable(previewTable);
    }

    public void setTransferHandler(KinDragTransferHandler dragTransferHandler) {
        for (KinTree currentTree : new KinTree[]{egoTree, requiredTree, impliedTree, transientTree}) {
            currentTree.setTransferHandler(dragTransferHandler);
            currentTree.setDragEnabled(true);
        }
    }

    public void setTreeNodes(GraphPanel graphPanel) {
        HashSet<UniqueIdentifier> egoIdentifiers = graphPanel.dataStoreSvg.egoEntities;
        HashSet<UniqueIdentifier> requiredEntityIdentifiers = graphPanel.dataStoreSvg.requiredEntities;
        EntityData[] allEntities;
        if (graphPanel.dataStoreSvg.graphData != null) {
            allEntities = graphPanel.dataStoreSvg.graphData.getDataNodes();
        } else {
            allEntities = new EntityData[0];
        }
        IndexerParameters indexerParameters = graphPanel.getIndexParameters();

        this.remove(transientNodeScrolPane);
        this.add(metadataNodeScrolPane, BorderLayout.CENTER);
        this.revalidate();
        HashSet<KinTreeNode> egoNodeArray = new HashSet<KinTreeNode>();
        HashSet<KinTreeNode> requiredNodeArray = new HashSet<KinTreeNode>();
        HashSet<KinTreeNode> remainingNodeArray = new HashSet<KinTreeNode>();
        for (EntityData entityData : allEntities) {
            if (entityData.isVisible) {
                KinTreeNode kinTreeNode = new KinTreeNode(entityData, indexerParameters, dialogHandler, entityCollection, dataNodeLoader);
                if (entityData.isEgo || egoIdentifiers.contains(entityData.getUniqueIdentifier())) {
                    egoNodeArray.add(kinTreeNode);
                } else if (requiredEntityIdentifiers.contains(entityData.getUniqueIdentifier())) {
                    requiredNodeArray.add(kinTreeNode);
                } else {
                    remainingNodeArray.add(kinTreeNode);
                }
            }
        }
        System.out.println("egoNodeArray: " + egoNodeArray.size());
        System.out.println("requiredNodeArray: " + requiredNodeArray.size());
        System.out.println("remainingNodeArray: " + remainingNodeArray.size());
        egoNode.setChildNodes(egoNodeArray.toArray(new ArbilNode[]{}));
        requiredNode.setChildNodes(requiredNodeArray.toArray(new ArbilNode[]{}));
        impliedNode.setChildNodes(remainingNodeArray.toArray(new ArbilNode[]{}));
        transientNode.setChildNodes(new ArbilNode[]{});
        egoTree.requestResort();
        requiredTree.requestResort();
        impliedTree.requestResort();
        transientTree.requestResort();
    }

    public void setTransientNodes(EntityData[] allEntities) {
        this.remove(metadataNodeScrolPane);
        this.add(transientNodeScrolPane, BorderLayout.CENTER);
        this.revalidate();
        HashSet<KinTreeNode> transientNodeArray = new HashSet<KinTreeNode>();
        for (EntityData entityData : allEntities) {
            KinTreeNode kinTreeNode = new KinTreeNode(entityData, null, dialogHandler, entityCollection, dataNodeLoader);
            transientNodeArray.add(kinTreeNode);
        }
        transientNode.setChildNodes(transientNodeArray.toArray(new ArbilNode[]{}));
        egoNode.setChildNodes(new ArbilNode[]{});
        requiredNode.setChildNodes(new ArbilNode[]{});
        impliedNode.setChildNodes(new ArbilNode[]{});
        transientTree.requestResort();
        egoTree.requestResort();
        requiredTree.requestResort();
        impliedTree.requestResort();
    }

    public void actionPerformed(ActionEvent e) {
        if ("convert".equals(e.getActionCommand())) {
            // todo: Ticket #1114 Button to convert freeform diagram to metadata diagram
            throw new UnsupportedOperationException("Ticket #1114 Button to convert freeform diagram to metadata diagram");
        }
    }
}
