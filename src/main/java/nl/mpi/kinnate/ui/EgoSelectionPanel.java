package nl.mpi.kinnate.ui;

import java.awt.BorderLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.ui.ArbilTreeRenderer;

/**
 *  Document   : EgoSelectionPanel
 *  Created on : Sep 29, 2010, 13:12:01 PM
 *  Author     : Peter Withers
 */
public class EgoSelectionPanel extends JPanel {

    private JTree egoTree;
    DefaultTreeModel egoModel;
    DefaultMutableTreeNode rootEgoNode;

    public EgoSelectionPanel() {
        rootEgoNode = new DefaultMutableTreeNode("Selected Ego Nodes");
        egoModel = new DefaultTreeModel(rootEgoNode);
        egoTree = new JTree(egoModel);
        egoTree.setCellRenderer(new ArbilTreeRenderer());
        egoTree.setRootVisible(false);
        this.setLayout(new BorderLayout());
        JScrollPane outerScrolPane = new JScrollPane();
        this.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Ego"));
        outerScrolPane.getViewport().add(egoTree);
        this.add(outerScrolPane, BorderLayout.CENTER);
    }

    public void setEgoNodes(String[] egoNodes) {
        if (egoNodes != null) {
            ArrayList<ArbilDataNode> tempArray = new ArrayList<ArbilDataNode>();
            for (String currentNodeString : egoNodes) {
                System.out.println("ego tree: " + currentNodeString);
                try {
                    tempArray.add(ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(currentNodeString)));
                } catch (URISyntaxException exception) {
                    System.err.println(exception.getMessage());
                }
            }
            setEgoNodes(tempArray.toArray(new ArbilDataNode[]{}));
        }
    }

    public void setEgoNodes(URI[] egoNodes) {
        if (egoNodes != null) {
            ArrayList<ArbilDataNode> tempArray = new ArrayList<ArbilDataNode>();
            for (URI currentNodeUri : egoNodes) {
                System.out.println("ego tree: " + currentNodeUri);
                tempArray.add(ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, currentNodeUri));
            }
            setEgoNodes(tempArray.toArray(new ArbilDataNode[]{}));
        }
    }

    public void setEgoNodes(ArbilDataNode[] egoNodes) {
        rootEgoNode.removeAllChildren();
        egoModel.nodeStructureChanged(rootEgoNode);
        if (egoNodes != null) {
            for (ArbilDataNode imdiTreeObject : egoNodes) {
                System.out.println("adding ego: " + imdiTreeObject.toString());
                egoModel.insertNodeInto(new DefaultMutableTreeNode(imdiTreeObject), rootEgoNode, 0);
            }
            egoTree.expandPath(new TreePath(rootEgoNode.getPath()));
        }
    }
}
