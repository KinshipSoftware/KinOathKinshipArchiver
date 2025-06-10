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

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.menu.TreeContextMenu;
import nl.mpi.arbil.util.ArbilActionBuffer;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.TreeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interactive representation of a tree of arbil nodes
 *
 * Document : ArbilTree
 * Created on : Feb 16, 2009, 3:58:50 PM
 *
 * @author Peter.Withers@mpi.nl
 *
 * @see ArbilDataNode
 * @see ArbilTreeHelper
 * @see ArbilTreeRenderer
 * @see TreeContextMenu
 */
public class ArbilTree extends JTree implements ArbilDataNodeContainer, ClipboardOwner {

    private final static Logger logger = LoggerFactory.getLogger(ArbilTree.class);
    protected ArbilTable customPreviewTable = null;
    private boolean clearSelectionOnFocusLost = false;
    private final TreeHelper treeHelper;
    private final ArbilTreeController treeController;
    private final PreviewSplitPanel previewSplitPanel;

    public void setCustomPreviewTable(ArbilTable customPreviewTable) {
	this.customPreviewTable = customPreviewTable;
    }

    public void setClearSelectionOnFocusLost(boolean clearSelectionOnFocusLost) {
	this.clearSelectionOnFocusLost = clearSelectionOnFocusLost;
    }

    public ArbilTree(ArbilTreeController treeController, TreeHelper treeHelper, PreviewSplitPanel previewPanel) {
	this.treeController = treeController;
	this.treeHelper = treeHelper;
	this.previewSplitPanel = previewPanel;

	this.addMouseListener(new java.awt.event.MouseAdapter() {
//                public void mouseClicked(java.awt.event.MouseEvent evt) {
//                    treeMouseClick(evt);
//                }
	    @Override
	    public void mousePressed(java.awt.event.MouseEvent evt) {
		treeMousePressedReleased(evt);

		putSelectionIntoPreviewTable();
	    }

	    @Override
	    public void mouseReleased(java.awt.event.MouseEvent evt) {
		treeMousePressedReleased(evt);
	    }
	});

	this.addKeyListener(new java.awt.event.KeyAdapter() { // TODO: this is failing to get the menu key event
//                @Override
//                public void keyReleased(KeyEvent evt) {
//                    treeKeyTyped(evt);
//                }
	    @Override
	    public void keyTyped(java.awt.event.KeyEvent evt) {
		treeKeyTyped(evt);
	    }//                @Override
//                public void keyPressed(java.awt.event.KeyEvent evt) {
//                    treeKeyTyped(evt);
//                }
	});

	this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
	    @Override
	    public void mouseDragged(java.awt.event.MouseEvent evt) {
		if (evt.getModifiers() == 0 && evt.getButton() == MouseEvent.BUTTON1) {
		    JComponent c = (JComponent) evt.getSource();
		    TransferHandler th = c.getTransferHandler();
		    th.exportAsDrag(c, evt, TransferHandler.COPY);
		}
	    }
	});
	this.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
	    public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
		if (evt.getPath() == null) {
		    //There is no selection.
		} else {
		    // load imdi data if not already loaded
		    ArbilTree.this.requestResort();
		}
	    }

	    public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
	    }
	});
	this.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
	    public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
		if (evt.getPath().getPathCount() == 1) {
		    logger.debug("root node cannot be collapsed");
		    throw new ExpandVetoException(evt, "root node cannot be collapsed");
		}
	    }

	    public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
//                DefaultMutableTreeNode parentNode = null;
//                if (evt.getPath() == null) {
//                    //There is no selection.
//                } else {
//                    parentNode = (DefaultMutableTreeNode) (evt.getPath().getLastPathComponent());
//                    // load imdi data if not already loaded
//                    ArbilTreeHelper.getSingleInstance().addToSortQueue(parentNode);
//                }
	    }
	});

	this.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
	    public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
		putSelectionIntoPreviewTable();
	    }
	});

	this.addFocusListener(new FocusListener() {
	    public void focusGained(FocusEvent e) {
	    }

	    public void focusLost(FocusEvent e) {
		if (clearSelectionOnFocusLost) {
		    ArbilTree.this.clearSelection();
		}
	    }
	});

	// enable tool tips.
	ToolTipManager.sharedInstance().registerComponent(this);
	// enable the tree icons
	this.setCellRenderer(new ArbilTreeRenderer());
	((DefaultTreeModel) treeModel).setAsksAllowsChildren(true);
    }

    protected void putSelectionIntoPreviewTable() {
	ArbilTable targetPreviewTable = customPreviewTable;
	if (targetPreviewTable == null && previewSplitPanel.isPreviewTableShown() && previewSplitPanel.getPreviewTable() != null) {
	    // if a custom preview table has not been set then check for the application wide preview table and use that if it is enabled
	    targetPreviewTable = previewSplitPanel.getPreviewTable();
	}
	if (targetPreviewTable != null) {
	    TableCellEditor currentCellEditor = targetPreviewTable.getCellEditor(); // stop any editing so the changes get stored
	    if (currentCellEditor != null) {
		currentCellEditor.stopCellEditing();
	    }
	    final ArbilDataNode leadSelectionDataNode = ArbilTree.this.getLeadSelectionDataNode();
	    final ArbilTableModel arbilTableModel = targetPreviewTable.getArbilTableModel();
	    if (!(arbilTableModel.getArbilDataNodeCount() == 1 && arbilTableModel.containsArbilDataNode(leadSelectionDataNode))) {
		arbilTableModel.removeAllArbilDataNodeRows();
		arbilTableModel.addSingleArbilDataNode(leadSelectionDataNode);
	    }
	}
    }

    private void treeMousePressedReleased(java.awt.event.MouseEvent evt) {
	// test if click was over a selected node
	TreePath clickedNodePath = ((JTree) evt.getSource()).getPathForLocation(evt.getX(), evt.getY());

	boolean clickedPathIsSelected = (((JTree) evt.getSource()).isPathSelected(clickedNodePath));
	if (evt.isPopupTrigger()) {
	    // this is simplified and made to match the same type of actions as the imditable
	    if (!evt.isShiftDown() && !evt.isControlDown() && !clickedPathIsSelected) {
		((javax.swing.JTree) evt.getSource()).clearSelection();
		((javax.swing.JTree) evt.getSource()).addSelectionPath(clickedNodePath);
	    }
	    treeController.showContextMenu(this, new Point(evt.getX(), evt.getY()));
	}
    }

    private void treeKeyTyped(java.awt.event.KeyEvent evt) {
	if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
	    treeController.openSelectedNodesInTable(((ArbilTree) evt.getSource()));
	}
	if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE) {
//        GuiHelper.treeHelper.deleteNode(GuiHelper.treeHelper.getSingleSelectedNode((JTree) evt.getSource()));
	    treeHelper.deleteNodes((JTree) evt.getSource());
	}
	if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_CONTEXT_MENU) {
//        DefaultMutableTreeNode leadSelection = (DefaultMutableTreeNode) ((JTree) evt.getSource()).getSelectionPath().getLastPathComponent();
	    Rectangle selectionBounds = ((JTree) evt.getSource()).getRowBounds(((JTree) evt.getSource()).getLeadSelectionRow());
	    treeController.showContextMenu(this, new Point(selectionBounds.x, selectionBounds.y));
	}
    }

    @Override
    public JToolTip createToolTip() {
	listToolTip.updateList();
	return listToolTip;
    }

    @Override
    public int getRowHeight() {
	Graphics g = this.getGraphics();
	if (g != null) {
	    try {
		FontMetrics fontMetrics = g.getFontMetrics();
		int requiredHeight = fontMetrics.getHeight();
		return requiredHeight;
	    } catch (Exception exeption) {
	    } finally {
		g.dispose();
	    }
	}
	return super.getRowHeight();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
	String tip = null;
	java.awt.Point p = event.getPoint();
	if (getRowForLocation(event.getX(), event.getY()) == -1) {
	    listToolTip.setTartgetObject(null);
	} else {
	    TreePath curPath = getPathForLocation(event.getX(), event.getY());
	    Object targetObject = ((DefaultMutableTreeNode) curPath.getLastPathComponent()).getUserObject();

	    if (targetObject instanceof ArbilDataNode) {
		listToolTip.setTartgetObject(targetObject);
		tip = ((ArbilDataNode) targetObject).getUrlString(); // this is required to be unique to the node so that the tip is updated
	    } else {
		listToolTip.setTartgetObject(null);
	    }
	}
	return tip;
    }

    public ArbilNode[] getAllSelectedNodes() {
	return getSelectedNodesOfType(ArbilNode.class).toArray(new ArbilNode[]{});
    }

    public ArbilDataNode[] getSelectedNodes() {
	return getSelectedNodesOfType(ArbilDataNode.class).toArray(new ArbilDataNode[]{});
    }

    public <T> ArrayList<T> getSelectedNodesOfType(Class<T> type) {
	ArrayList<T> selectedNodes = new ArrayList<T>();
	for (int selectedCount = 0; selectedCount < this.getSelectionCount(); selectedCount++) {
	    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getSelectionPaths()[selectedCount].getLastPathComponent();
	    if (type.isAssignableFrom(parentNode.getUserObject().getClass())) {
		T currentTreeObject = (T) parentNode.getUserObject();
		selectedNodes.add(currentTreeObject);
	    }
	}
	return selectedNodes;
    }

    public ArbilNode getLeadSelectionNode() {
	DefaultMutableTreeNode selectedTreeNode = null;
	ArbilNode returnObject = null;
	javax.swing.tree.TreePath currentNodePath = this.getSelectionPath();
	if (currentNodePath != null) {
	    selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
	}
	if (selectedTreeNode != null && selectedTreeNode.getUserObject() instanceof ArbilNode) {
	    returnObject = (ArbilNode) selectedTreeNode.getUserObject();
	}
	return returnObject;
    }

    public ArbilDataNode getLeadSelectionDataNode() {
	ArbilNode returnObject = getLeadSelectionNode();
	if (returnObject != null && returnObject instanceof ArbilDataNode) {
	    return (ArbilDataNode) returnObject;
	}
	return null;
    }

    public void copyNodeUrlToClipboard(ArbilDataNode[] selectedNodes) {
	if (selectedNodes != null) {
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    //TODO: Use StringBuilder
	    String copiedNodeUrls = null;
	    for (ArbilDataNode currentNode : selectedNodes) {
		if (currentNode != null) {
		    if (copiedNodeUrls == null) {
			copiedNodeUrls = "";
		    } else {
			copiedNodeUrls = copiedNodeUrls.concat("\n");
		    }
		    try {
			if (currentNode.hasResource()) {
			    copiedNodeUrls = copiedNodeUrls.concat(URLDecoder.decode(currentNode.getFullResourceURI().toString(), "UTF-8"));
			} else {
			    copiedNodeUrls = copiedNodeUrls.concat(URLDecoder.decode(currentNode.getURI().toString(), "UTF-8"));
			}
		    } catch (UnsupportedEncodingException murle) {
			BugCatcherManager.getBugCatcher().logError(murle);
		    }
		}
	    }
	    StringSelection stringSelection = new StringSelection(copiedNodeUrls);
	    clipboard.setContents(stringSelection, this);
	    logger.debug("copied: \n{}", copiedNodeUrls);
	}
    }

    private void sortDescendentNodes(DefaultMutableTreeNode currentNode/* , TreePath[] selectedPaths */) {
	// todo: consider returning a list of tree paths for the nodes that are opened and have no open children
//        if (currentNode.getUserObject() instanceof JLabel) {
//            logger.debug("currentNode: " + ((JLabel) currentNode.getUserObject()).getText());
//        } else {
//            logger.debug("currentNode: " + currentNode);
//        }
	boolean isExpanded = true;
	ArbilNode[] childDataNodeArray = new ArbilNode[]{};
	if (currentNode.getUserObject() instanceof ArbilNode) {
	    ArbilNode currentDataNode = (ArbilNode) currentNode.getUserObject();
	    if (currentDataNode != null) {
		childDataNodeArray = currentDataNode.getChildArray();
		isExpanded = this.isExpanded(new TreePath((currentNode).getPath()));
		if (isExpanded && currentDataNode instanceof ArbilDataNode && !currentDataNode.isLoading() && !currentDataNode.isDataLoaded()) {
		    // Expanded nodes should be fully loaded
		    ((ArbilDataNode) currentDataNode).loadFullArbilDom();
		}
	    }
	}
	Arrays.sort(childDataNodeArray);
	DefaultTreeModel thisTreeModel = (DefaultTreeModel) treeModel;

	if (!isExpanded) {
	    currentNode.setAllowsChildren(childDataNodeArray.length > 0);
	    thisTreeModel.nodeChanged(currentNode);
	} else {
	    if (childDataNodeArray.length > 0) {
		// never disable allows children when there are child nodes!
		// but allows children must be set before nodes can be added (what on earth were they thinking!)
		currentNode.setAllowsChildren(true);
		thisTreeModel.nodeChanged(currentNode);
	    }
	    for (int childIndex = 0; childIndex < childDataNodeArray.length; childIndex++) {
		final ArbilNode currentDataNode = childDataNodeArray[childIndex];
		// search for an existing node and move it if required
		for (int modelChildIndex = 0; modelChildIndex < thisTreeModel.getChildCount(currentNode); modelChildIndex++) {
		    if (((DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, modelChildIndex)).getUserObject().equals(currentDataNode)) {
			if (childIndex != modelChildIndex) {
			    DefaultMutableTreeNode shiftedNode = (DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, modelChildIndex);
			    thisTreeModel.removeNodeFromParent(shiftedNode);
			    thisTreeModel.insertNodeInto(shiftedNode, currentNode, childIndex);
			} else {
			    thisTreeModel.nodeChanged((DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, modelChildIndex));
			}
			break;
		    }
		}
		// check if using an existing node failed and if so then add a new node
		if (childIndex >= thisTreeModel.getChildCount(currentNode) || !((DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, childIndex)).getUserObject().equals(currentDataNode)) {
		    currentDataNode.registerContainer(this);
//                    if (!SwingUtilities.isEventDispatchThread()) {
//                        throw new UnsupportedOperationException();
//                    }
		    DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(currentDataNode);
		    thisTreeModel.insertNodeInto(addableNode, currentNode, childIndex);
		    nodeAdded(addableNode, currentDataNode);
		}
	    }
	    // remove any extraneous nodes from the end
	    for (int childIndex = thisTreeModel.getChildCount(currentNode) - 1; childIndex >= childDataNodeArray.length; childIndex--) {
		final DefaultMutableTreeNode toRemove = (DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, childIndex);
		thisTreeModel.removeNodeFromParent(toRemove);
		nodeRemoved(toRemove);
	    }
	    for (int childIndex = 0; childIndex < thisTreeModel.getChildCount(currentNode); childIndex++) {
		//for (Enumeration<DefaultMutableTreeNode> childTreeNodeEnum = currentNode.children(); childTreeNodeEnum.hasMoreElements();) {
		sortDescendentNodes((DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, childIndex));
	    }
	}
    }

    protected void nodeAdded(DefaultMutableTreeNode addableNode, ArbilNode addedDataNode) {
    }

    protected void nodeRemoved(final DefaultMutableTreeNode toRemove) {
    }

    public void requestResort() {
	sortRunner.requestActionAndNotify();
    }

    public boolean isFullyLoadedNodeRequired() {
	return false;
    }

    /**
     * Data node is to be removed from this tree
     *
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode dataNode) {
	requestResort();
    }

    /**
     * Data node is clearing its icon
     *
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode dataNode) {
	requestResort();
    }
    protected final Object sortRunnerLock = new Object();
    // NOTE: This nameless ArbilActionBuffer class/object is not serializable, while ArbilTree is
    private final ArbilActionBuffer sortRunner = new ArbilActionBuffer("ArbilTree sort thread", 100, 150, sortRunnerLock) {
	@Override
	protected void executeAction() {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			sortDescendentNodes((DefaultMutableTreeNode) ArbilTree.this.getModel().getRoot());
		    }
		});
	    } catch (InterruptedException exception) {
		// todo: if this has failed then we could restart the resort (however this is probably going to be replaced by extending the DefaultMutableTreeNode).
		BugCatcherManager.getBugCatcher().logError(exception);
	    } catch (InvocationTargetException exception) {
		// todo: if this has failed then we could restart the resort (however this is probably going to be replaced by extending the DefaultMutableTreeNode).
		BugCatcherManager.getBugCatcher().logError(exception);
	    }
	}
    }; // ArbilActionBuffer
    private JListToolTip listToolTip = new JListToolTip();

    /**
     * A new child node has been added to the destination node.
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    public void dataNodeChildAdded(final ArbilNode destination, final ArbilNode newNode) {
	// Do nothing... (in contrast to ArbilTrackingTree!)
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
	logger.debug("lost clipboard ownership");
    }
}
