/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.util.TreeHelper;

/**
 * Extension of ArbilTree that keeps track of its ArbilNode contents and can quickly look up the TreeNode for any ArbilNode
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTrackingTree extends ArbilTree {

    public ArbilTrackingTree(ArbilTreeController treeController, TreeHelper treeHelper, PreviewSplitPanel previewPanel) {
	super(treeController, treeHelper, previewPanel);
    }
    private HashMap<ArbilNode, TreeNode> treeNodeMap = new HashMap<ArbilNode, TreeNode>();

    @Override
    public void setModel(TreeModel newModel) {
	// If model already set with ArbilNode root node (unlikely), remove this as container and remove from map
	if (getModel() != null) {
	    if (getModel().getRoot() instanceof DefaultMutableTreeNode) {
		Object rootUserObject = ((DefaultMutableTreeNode) getModel().getRoot()).getUserObject();
		if (rootUserObject instanceof ArbilNode) {
		    treeNodeMap.remove((ArbilNode) rootUserObject);
		    ((ArbilNode) rootUserObject).removeContainer(this);
		}
	    }
	}

	// Do default model setting
	super.setModel(newModel);

	// If model has ArbilRootNode root object, add it to map and register as container
	if (newModel.getRoot() instanceof DefaultMutableTreeNode) {
	    Object rootUserObject = ((DefaultMutableTreeNode) newModel.getRoot()).getUserObject();
	    if (rootUserObject instanceof ArbilNode) {
		treeNodeMap.put((ArbilNode) rootUserObject, (TreeNode) newModel.getRoot());
		((ArbilNode) rootUserObject).registerContainer(this);
	    }
	}
    }

    /**
     * A new child node has been added to the destination node. Tries to expand tree to show the new node and select it.
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    @Override
    public void dataNodeChildAdded(final ArbilNode destination, final ArbilNode newNode) {
	jumpToNode(destination, newNode);
    }

    /**
     *
     * @param startingNode Leave null to search from root
     * @param targetNode Node to jump to
     * @return Whether node was found
     */
    public boolean jumpToNode(ArbilNode startingNode, final ArbilNode targetNode) {
	// Find treeNode for destination ArbilNode
	TreeNode startingTreeNode = null;
	if (startingNode == null) {
	    if (getModel().getRoot() instanceof TreeNode) {
		startingTreeNode = (TreeNode) getModel().getRoot();
		startingNode = getRootNodeObject();
	    }
	} else {
	    startingTreeNode = treeNodeMap.get(startingNode);
	}
	if (startingTreeNode != null) {
	    // Find path from destination node to the newly added node
	    List<ArbilNode> nodePath = createArbilNodePath(startingNode, targetNode);
	    if (nodePath != null) {
		// Create a tree path from this node path
		final TreePath newNodePath = findTreePathForNodePath(startingTreeNode, nodePath);

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			// Select the new node
			setSelectionPath(newNodePath);
			scrollPathToVisible(newNodePath);
		    }
		});
		return true;
	    }
	}
	return false;
    }

    @Override
    protected void nodeAdded(DefaultMutableTreeNode addableNode, ArbilNode addedDataNode) {
	treeNodeMap.put(addedDataNode, addableNode);
    }

    @Override
    protected void nodeRemoved(final DefaultMutableTreeNode toRemove) {
	for (int i = 0; i < toRemove.getChildCount(); i++) {
	    TreeNode child = toRemove.getChildAt(i);
	    if (child instanceof DefaultMutableTreeNode) {
		nodeRemoved((DefaultMutableTreeNode) toRemove.getChildAt(i));
	    }
	}
	if (toRemove.getUserObject() instanceof ArbilNode) {
	    treeNodeMap.remove((ArbilNode) toRemove.getUserObject());
	}
    }

    /**
     * Takes arbil node path and maps to TreePath starting mapping from specified root tree node
     *
     * @param rootTreeNode TreeNode to start with
     * @param arbilNodePath Path of arbil nodes that correspond to children of rootTreeNode
     * @return TreePath (from tree root) for the arbilNodePath
     */
    private TreePath findTreePathForNodePath(final TreeNode rootTreeNode, List<ArbilNode> arbilNodePath) {
	// Create root tree path
	TreePath treePath = createTreePathForTreeNode(rootTreeNode);
	// Start mapping node path to tree path
	for (ArbilNode currentTargetNode : arbilNodePath) {
	    final TreePath currentTreePath = treePath;
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {

		    // Expand current node so children will become available...
		    expandPath(currentTreePath);
		}
	    });
	    // ...and give the sort runner some time to react to the expansion before proceeding...
	    synchronized (sortRunnerLock) {
		try {
		    sortRunnerLock.wait(250);
		    // TODO: better wait until node has actually been added
		} catch (InterruptedException ex) {
		}
	    }
	    treePath = extendTreePathWithChildNode(treePath, currentTargetNode);
	}
	return treePath;
    }

    /**
     * Extends a tree path by adding the TreeNode that has a specified ArbilNode as user object
     *
     * @param treePath TreePath to extend
     * @param currentTargetNode
     * @return Extend tree path (if target not found, returns original)
     */
    private TreePath extendTreePathWithChildNode(TreePath treePath, ArbilNode currentTargetNode) {
	// Get current tree node
	final Object lastPathComponent = treePath.getLastPathComponent();
	if (lastPathComponent instanceof TreeNode) {
	    final TreeNode currentTreeNode = (TreeNode) lastPathComponent;
	    // Traverse current node children to find match for target node
	    for (int i = 0; i < currentTreeNode.getChildCount(); i++) {
		TreeNode currentTreeNodeChild = currentTreeNode.getChildAt(i);
		if (currentTreeNodeChild instanceof DefaultMutableTreeNode) {
		    // Check if tree node has current target arbil node as user object
		    if (((DefaultMutableTreeNode) currentTreeNodeChild).getUserObject().equals(currentTargetNode)) {
			// Extend tree path and continue to next child
			treePath = treePath.pathByAddingChild(currentTreeNodeChild);
			break;
		    }
		}
	    }
	}
	return treePath;
    }

    /**
     *
     * @param treeNode
     * @return Complete treepath for the specified treeNode
     */
    private static TreePath createTreePathForTreeNode(TreeNode treeNode) {
	ArrayList pathList = new ArrayList();
	TreeNode node = treeNode;
	// Construct tree path
	while (node != null) {
	    pathList.add(node);
	    node = node.getParent();
	}
	Collections.reverse(pathList);
	return new TreePath(pathList.toArray());
    }

    /**
     * Creates list that represents path from a root node to a target node (not including root node)
     *
     * @param rootNode Departure node
     * @param targetNode Target node
     * @return List starting at first child of rootnode on path and ending with targetnode, or null if not found
     */
    private static List<ArbilNode> createArbilNodePath(final ArbilNode rootNode, final ArbilNode targetNode) {
	for (ArbilNode child : rootNode.getChildArray()) {
	    if (child.equals(targetNode)) {
		// This child is the target node, path consists of single node
		return Collections.singletonList(targetNode);
	    } else {
		final List<ArbilNode> childList = createArbilNodePath(child, targetNode);
		if (childList != null) {
		    // Target found in child path, append to child and return
		    final LinkedList<ArbilNode> list = new LinkedList<ArbilNode>();
		    list.add(child);
		    list.addAll(childList);
		    return list;
		}
	    }
	}
	return null;
    }

    /**
     *
     * @return ArbilNode that is object of tree root. If tree root object not an ArbilNode, null.
     */
    private ArbilNode getRootNodeObject() {
	Object localTreeRoot = getModel().getRoot();
	if (localTreeRoot instanceof DefaultMutableTreeNode) {
	    Object userObject = ((DefaultMutableTreeNode) localTreeRoot).getUserObject();
	    if (userObject instanceof ArbilNode) {
		return (ArbilNode) userObject;
	    }
	}
	return null;
    }
}
