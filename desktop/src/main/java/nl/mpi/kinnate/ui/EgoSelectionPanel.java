package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilTreeRenderer;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : EgoSelectionPanel
 *  Created on : Sep 29, 2010, 13:12:01 PM
 *  Author     : Peter Withers
 */
public class EgoSelectionPanel extends JPanel {

    private ArbilTree egoTree;
//    DefaultTreeModel egoModel;
//    DefaultMutableTreeNode egoRootNode;
    private ArbilTree requiredTree;
//    DefaultTreeModel requiredModel;
//    DefaultMutableTreeNode requiredRootNode;
    private ArbilTree impliedTree;
//    DefaultTreeModel impliedModel;
//    DefaultMutableTreeNode impliedRootNode;
    JPanel labelPanel3;
    JLabel transientLabel;

    public EgoSelectionPanel(ArbilTable previewTable) {

//        egoRootNode = new DefaultMutableTreeNode(new JLabel("Ego", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
//        egoModel = new DefaultTreeModel(egoRootNode);
//        egoTree = new JTree(egoModel);
        egoTree = new ArbilTree();
        // todo: add trac task to modify the arbil tree such that each tree can have its own preview table
//        egoTree.getModel().
//        egoTree.setRootVisible(false);
        egoTree.setCellRenderer(new ArbilTreeRenderer());

//        requiredRootNode = new DefaultMutableTreeNode(new JLabel("Required", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
//        requiredModel = new DefaultTreeModel(requiredRootNode);
//        requiredTree = new JTree(requiredModel);
        requiredTree = new ArbilTree();
//        requiredTree.setRootVisible(false);
        requiredTree.setCellRenderer(new ArbilTreeRenderer());

//        impliedRootNode = new DefaultMutableTreeNode(new JLabel("Implied", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
//        impliedModel = new DefaultTreeModel(impliedRootNode);
//        impliedTree = new JTree(impliedModel);
        impliedTree = new ArbilTree();
//        impliedTree.setRootVisible(false);
        impliedTree.setCellRenderer(new ArbilTreeRenderer());

//        egoTree.setRootVisible(false);
        this.setLayout(new BorderLayout());
        JPanel treePanel1 = new JPanel(new BorderLayout());
        JPanel treePanel2 = new JPanel(new BorderLayout());
//        treePanel.setLayout(new BoxLayout(treePanel, BoxLayout.PAGE_AXIS));
        JScrollPane outerScrolPane = new JScrollPane(treePanel1);
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
        treePanel1.add(labelPanel1, BorderLayout.PAGE_START);
        treePanel1.add(treePanel2, BorderLayout.CENTER);
        treePanel2.add(labelPanel2, BorderLayout.PAGE_START);
        treePanel2.add(labelPanel3, BorderLayout.CENTER);
        this.add(outerScrolPane, BorderLayout.CENTER);
        egoTree.setCustomPreviewTable(previewTable);
        requiredTree.setCustomPreviewTable(previewTable);
        impliedTree.setCustomPreviewTable(previewTable);
    }

    public void setTreeNodes(HashSet<UniqueIdentifier> egoIdentifiers, HashSet<UniqueIdentifier> requiredEntityIdentifiers, EntityData[] allEntities) {
        ArrayList<ArbilDataNode> egoNodeArray = new ArrayList<ArbilDataNode>();
        ArrayList<ArbilDataNode> requiredNodeArray = new ArrayList<ArbilDataNode>();
        ArrayList<ArbilDataNode> remainingNodeArray = new ArrayList<ArbilDataNode>();
        for (EntityData entityData : allEntities) {
            if (entityData.isVisible) {
                try {
                    String entityPath = entityData.getEntityPath();
                    if (entityPath != null) {
                        ArbilDataNode arbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(entityPath));
                        if (entityData.isEgo || egoIdentifiers.contains(entityData.getUniqueIdentifier())) {
                            egoNodeArray.add(arbilDataNode);
                        } else if (requiredEntityIdentifiers.contains(entityData.getUniqueIdentifier())) {
                            requiredNodeArray.add(arbilDataNode);
                        } else {
                            remainingNodeArray.add(arbilDataNode);
                        }
                    }
                } catch (URISyntaxException exception) {
                    System.err.println(exception.getMessage());
                }
            }
        }
//            setEgoNodes(egoNodeArray, egoTree, egoModel, egoRootNode);
        egoTree.rootNodeChildren = egoNodeArray.toArray(new ArbilDataNode[]{});
        egoTree.requestResort();
//            setEgoNodes(requiredNodeArray, requiredTree, requiredModel, requiredRootNode);
        requiredTree.rootNodeChildren = requiredNodeArray.toArray(new ArbilDataNode[]{});
        requiredTree.requestResort();
//            setEgoNodes(remainingNodeArray, impliedTree, impliedModel, impliedRootNode);
        impliedTree.rootNodeChildren = remainingNodeArray.toArray(new ArbilDataNode[]{});
        impliedTree.requestResort();
        if (transientLabel != null) {
            labelPanel3.remove(transientLabel);
        }
    }

    public void setTransientNodes(EntityData[] allEntities) {
        egoTree.rootNodeChildren = new ArbilDataNode[]{};
        egoTree.requestResort();
//            setEgoNodes(requiredNodeArray, requiredTree, requiredModel, requiredRootNode);
        requiredTree.rootNodeChildren = new ArbilDataNode[]{};
        requiredTree.requestResort();
//            setEgoNodes(remainingNodeArray, impliedTree, impliedModel, impliedRootNode);
        impliedTree.rootNodeChildren = new ArbilDataNode[]{};
        impliedTree.requestResort();
        transientLabel = new JLabel(allEntities.length + " transient entities have been generated");
        labelPanel3.add(transientLabel, BorderLayout.PAGE_START);
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
