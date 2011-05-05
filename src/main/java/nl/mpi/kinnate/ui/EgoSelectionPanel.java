package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilTreeRenderer;
import nl.mpi.kinnate.kindata.EntityData;

/**
 *  Document   : EgoSelectionPanel
 *  Created on : Sep 29, 2010, 13:12:01 PM
 *  Author     : Peter Withers
 */
public class EgoSelectionPanel extends JPanel {

    private JTree egoTree;
    DefaultTreeModel egoModel;
    DefaultMutableTreeNode egoRootNode;
    private JTree requiredTree;
    DefaultTreeModel requiredModel;
    DefaultMutableTreeNode requiredRootNode;
    private JTree impliedTree;
    DefaultTreeModel impliedModel;
    DefaultMutableTreeNode impliedRootNode;

    public EgoSelectionPanel() {
        DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();

        egoRootNode = new DefaultMutableTreeNode(new JLabel("Ego", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
        egoModel = new DefaultTreeModel(egoRootNode);
        egoTree = new JTree(egoModel);
        egoTree.setCellRenderer(new ArbilTreeRenderer());

        requiredRootNode = new DefaultMutableTreeNode(new JLabel("Required", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
        requiredModel = new DefaultTreeModel(requiredRootNode);
        requiredTree = new JTree(requiredModel);
        requiredTree.setCellRenderer(new ArbilTreeRenderer());

        impliedRootNode = new DefaultMutableTreeNode(new JLabel("Implied", cellRenderer.getDefaultOpenIcon(), JLabel.LEFT));
        impliedModel = new DefaultTreeModel(impliedRootNode);
        impliedTree = new JTree(impliedModel);
        impliedTree.setCellRenderer(new ArbilTreeRenderer());

//        egoTree.setRootVisible(false);
        this.setLayout(new BorderLayout());
        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BoxLayout(treePanel, BoxLayout.PAGE_AXIS));
        JScrollPane outerScrolPane = new JScrollPane(treePanel);
//        this.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Ego"));
        treePanel.add(egoTree, BorderLayout.PAGE_START);
        treePanel.add(requiredTree, BorderLayout.CENTER);
        treePanel.add(impliedTree, BorderLayout.PAGE_END);
        this.add(outerScrolPane, BorderLayout.CENTER);
    }

    public void setTreeNodes(HashSet<String> egoIdentifiers, HashSet<String> requiredEntityIdentifiers, EntityData[] allEntities) {
        ArrayList<ArbilDataNode> egoNodeArray = new ArrayList<ArbilDataNode>();
        ArrayList<ArbilDataNode> requiredNodeArray = new ArrayList<ArbilDataNode>();
        ArrayList<ArbilDataNode> remainingNodeArray = new ArrayList<ArbilDataNode>();
        for (EntityData entityData : allEntities) {
            try {
                ArbilDataNode arbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(entityData.getEntityPath()));
                if (entityData.isEgo || egoIdentifiers.contains(entityData.getUniqueIdentifier())) {
                    egoNodeArray.add(arbilDataNode);
                } else if (requiredEntityIdentifiers.contains(entityData.getUniqueIdentifier())) {
                    requiredNodeArray.add(arbilDataNode);
                } else {
                    remainingNodeArray.add(arbilDataNode);
                }
            } catch (URISyntaxException exception) {
                System.err.println(exception.getMessage());
            }
            setEgoNodes(egoNodeArray, egoTree, egoModel, egoRootNode);
            setEgoNodes(requiredNodeArray, requiredTree, requiredModel, requiredRootNode);
            setEgoNodes(remainingNodeArray, impliedTree, impliedModel, impliedRootNode);
        }
    }

    private void setEgoNodes(ArrayList<ArbilDataNode> selectedNodes, JTree currentTree, DefaultTreeModel currentModel, DefaultMutableTreeNode currentRootNode) {
        currentRootNode.removeAllChildren();
        currentModel.nodeStructureChanged(currentRootNode);
        if (selectedNodes != null) {
            for (ArbilDataNode imdiTreeObject : selectedNodes) {
//                System.out.println("adding node: " + imdiTreeObject.toString());
                currentModel.insertNodeInto(new DefaultMutableTreeNode(imdiTreeObject), currentRootNode, 0);
            }
            currentTree.expandPath(new TreePath(currentRootNode.getPath()));
        }
    }
}
