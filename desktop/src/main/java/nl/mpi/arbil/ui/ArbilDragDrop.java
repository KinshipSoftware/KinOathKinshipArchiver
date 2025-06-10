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

import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilRootNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.favourites.ArbilFavourites;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilDragDrop
 * Created on : Tue Sep 09 15:02:56 CEST 2008
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilDragDrop {

    private final static Logger logger = LoggerFactory.getLogger(ArbilDragDrop.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private final SessionStorage sessionStorage;
    private final ArbilTreeHelper treeHelper;
    private final WindowManager windowManager;
    private final MessageDialogHandler dialogHandler;
    private final TableController tableController;
    // There are numerous limitations of drag and drop in 1.5 and to overcome the resulting issues we need to share the same transferable object on both the drag source and the drop target
    private final DataFlavor dataNodeFlavour;
    private final ArbilNodeSelection arbilNodeSelection;

    public ArbilDragDrop(SessionStorage sessionStorage, ArbilTreeHelper treeHelper, WindowManager windowManager, MessageDialogHandler dialogHandler, TableController tableController) {
	this.sessionStorage = sessionStorage;
	this.treeHelper = treeHelper;
	this.windowManager = windowManager;
	this.dialogHandler = dialogHandler;
	this.tableController = tableController;
	this.dataNodeFlavour = new DataFlavor(ArbilDataNode.class, "ArbilDataNode");
	this.arbilNodeSelection = new ArbilNodeSelection();
    }

    public void addDrag(JTable tableSource) {
	tableSource.setDragEnabled(true);
	setTransferHandlerOnComponent(tableSource);
    }

    public void addDrag(JTree treeSource) {
	treeSource.setDragEnabled(true);
	setTransferHandlerOnComponent(treeSource);
	treeSource.addTreeSelectionListener(arbilNodeSelection);
	DropTarget target = treeSource.getDropTarget();
	try {
	    target.addDropTargetListener(new DropTargetAdapter() {
		@Override
		public void dragOver(DropTargetDragEvent dtdEvent) {
		    logger.debug("arbilNodeSelection.dropAllowed: {}", arbilNodeSelection.dropAllowed);
		    if (arbilNodeSelection.dropAllowed) {
			dtdEvent.acceptDrag(dtdEvent.getDropAction());
		    } else {
			dtdEvent.rejectDrag();
		    }
		}

		public void drop(DropTargetDropEvent e) {
		    // handled by the TransferHandler
		}
	    });
	} catch (java.util.TooManyListenersException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    public void addDrag(JList listSource) {
	listSource.setDragEnabled(true);
	setTransferHandlerOnComponent(listSource);
    }

    public void setTransferHandlerOnComponent(JComponent targetComponent) {
	//targetComponent.setDragEnabled(true);
	targetComponent.setTransferHandler(arbilNodeSelection);
    }

    /**
     * Internal transfer handler class that deals with drag/drop of arbil nodes
     */
    private class ArbilNodeSelection extends TransferHandler implements Transferable, javax.swing.event.TreeSelectionListener {

	long dragStartMilliSeconds;
	DataFlavor flavors[] = {dataNodeFlavour};
	ArbilDataNode[] draggedArbilNodes;
	DefaultMutableTreeNode[] draggedTreeNodes;
	private boolean selectionDraggedFromLocalCorpus = false;
	private boolean selectionContainsArchivableLocalFile = false;
	private boolean selectionContainsLocalFile = false;
	private boolean selectionContainsLocalDirectory = false;
	private boolean selectionContainsArbilResource = false;
	private boolean selectionContainsArbilCorpus = false;
	//private boolean selectionContainsArbilInCache = false;
	private boolean selectionContainsImdiCatalogue = false;
	private boolean selectionContainsImdiSession = false;
	private boolean selectionContainsCmdiMetadata = false;
	private boolean selectionContainsArbilChild = false;
	private boolean selectionContainsLocal = false;
	private boolean selectionContainsRemote = false;
	private boolean selectionContainsFavourite = false;
	private JComponent currentDropTarget = null;
	protected boolean dropAllowed = false;

	public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
	    if (evt.getSource() == currentDropTarget) {
		logger.debug("Drag target selection change: {}", evt.getSource().toString());
		if (evt.getSource() instanceof ArbilTree) {
		    dropAllowed = canDropToTarget((ArbilTree) evt.getSource());
//                    DropTarget dropTarget = dropTree.getDropTarget();                    
		}
	    }
	}

	private boolean canDropToTarget(ArbilTree dropTree) {
	    ArbilNode currentLeadSelection = dropTree.getLeadSelectionNode();
	    if (currentLeadSelection instanceof ArbilRootNode) {
		// this check is for the root node of the trees
		if (treeHelper.componentIsTheFavouritesTree(currentDropTarget)) {
		    // allow drop to the favourites tree even when no selection is made
		    // allow drop to only the root node of the favourites tree
		    logger.debug("favourites tree check");
		    return !selectionContainsFavourite;
		} else if (treeHelper.componentIsTheLocalCorpusTree(currentDropTarget)) {
		    //if (dropTree.getSelectionPath().getPathCount() == 1) {
		    // allow import to local tree if no nodes are selected
		    // allow drop to the root node if it is an import
		    logger.debug("local corpus tree check");
		    return ((selectionContainsFavourite && !draggedNodesContainsChildNode()) // Non-child nodes can be dragged from favourites to local corpus root
			    || selectionContainsArbilCorpus || selectionContainsImdiCatalogue || selectionContainsImdiSession || selectionContainsCmdiMetadata);
		}
		logger.debug("no tree check");
	    } else if (currentLeadSelection instanceof ArbilDataNode) {
		// this check is for the child nodes of the trees
		logger.debug("currentLeadSelection: {}", currentLeadSelection);
//                todo: prevent dragging to self but allow dragging to other branch of parent session
//                todo: look for error dragging actor from favourites
//                todo: look for error in field triggers when merging from favourite (suppress trtiggeres when merging)
		if (treeHelper.componentIsTheLocalCorpusTree(currentDropTarget)) {
		    if (currentLeadSelection.isCmdiMetaDataNode()) {
			return ((ArbilDataNode) currentLeadSelection).canHaveResource();
		    } else if (((ArbilDataNode) currentLeadSelection).isDirectory()) {
			return false; // nothing can be dropped to a directory
		    } else if (currentLeadSelection.isCorpus()) {
			if ((selectionContainsArbilCorpus || selectionContainsImdiCatalogue || selectionContainsImdiSession) && !selectionContainsCmdiMetadata) {
			    return true;
			}
		    } else if (currentLeadSelection.isCatalogue()) {
			return false; // nothing can be dropped to a catalogue
		    } else if (currentLeadSelection.isSession()) {
			if (selectionContainsArchivableLocalFile || selectionContainsArbilChild) {
			    return true;
			}
		    } else if (currentLeadSelection.isEmptyMetaNode()) {
			if (selectionContainsArbilChild) {
			    return true;
			}
		    } else if (currentLeadSelection.isChildNode()) {
			// TODO: in this case we should loop over the dragged nodes and check each one for compatability
			if (selectionContainsLocalFile || (selectionContainsArbilChild && selectionContainsFavourite)) { // TODO: allow drag drop of appropriate imdi child nodes to sessions and compatable subnodes
			    return true;
			}
		    }
		}
	    }
	    return false;
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
	    logger.debug("exportToClipboard: {}", comp);
	    createTransferable(null); // clear the transfer objects
	    if (comp instanceof ArbilTree) {
		ArbilTree sourceTree = (ArbilTree) comp;
		ArbilDataNode[] selectedArbilDataNodes = sourceTree.getSelectedNodes();
		if (selectedArbilDataNodes != null) {
		    sourceTree.copyNodeUrlToClipboard(selectedArbilDataNodes);
		}
	    } else if (comp instanceof ArbilTable) {
		ArbilTable sourceTable = (ArbilTable) comp;
		tableController.copySelectedTableRowsToClipBoard(sourceTable);
	    } else {
		super.exportToClipboard(comp, clip, action);
	    }
	}

	@Override
	public int getSourceActions(JComponent c) {
	    if ((c instanceof JTree) || (c instanceof JTable) || (c instanceof JList)) {
		return TransferHandler.COPY;
	    } else {
		return TransferHandler.NONE;
	    }
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor flavor[]) {
	    logger.trace("canImport: {}", comp);
	    currentDropTarget = null;
	    dropAllowed = false;
	    if (comp instanceof JTree) {
		if (treeHelper.componentIsTheLocalCorpusTree(comp)) {
		    logger.trace("localcorpustree so can drop here");
		    if (selectionContainsArchivableLocalFile
			    || //selectionContainsLocalFile ||
			    //selectionContainsLocalDirectory ||
			    //selectionContainsImdiResource ||
			    //selectionContainsLocal ||
			    //selectionContainsRemote ||
			    selectionContainsArbilCorpus
			    || selectionContainsImdiCatalogue
			    || selectionContainsImdiSession
			    || selectionContainsCmdiMetadata
			    || selectionContainsArbilChild) {
			logger.trace("dragged contents are acceptable");
			currentDropTarget = comp; // store the source component for the tree node sensitive drop
			dropAllowed = (comp instanceof ArbilTree) && canDropToTarget((ArbilTree) comp);
			return true;
		    }
		}
		if (treeHelper.componentIsTheFavouritesTree(comp)) {
		    logger.trace("favourites tree so can drop here");
		    if (//selectionContainsArchivableLocalFile &&
			    //selectionContainsLocalFile ||
			    //selectionContainsLocalDirectory &&
			    //selectionContainsImdiResource ||
			    //selectionContainsLocal ||
			    //selectionContainsRemote ||
			    //selectionContainsImdiCorpus ||
			    selectionContainsImdiCatalogue
			    || selectionContainsImdiSession
			    || selectionContainsCmdiMetadata
			    || selectionContainsArbilChild) {
			logger.trace("dragged contents are acceptable");
			currentDropTarget = comp; // store the source component for the tree node sensitive drop
			dropAllowed = canDropToTarget((ArbilTree) comp);
			return true;
		    }
		}
	    } else if (comp instanceof ArbilTable) {
		return ((ArbilTable) comp).isAllowNodeDrop();
	    } else {
		// search through al the parent nodes to see if we can find a drop target
		dropAllowed = (null != findArbilDropableTarget(comp));
		logger.trace("dropAllowed: {}", dropAllowed);
		return dropAllowed;
	    }
	    logger.trace("canImport false");
	    return false;
	}

	private Container findArbilDropableTarget(Container tempCom) {
	    while (tempCom != null) {
		if (tempCom instanceof ArbilSplitPanel || tempCom instanceof JDesktopPane) {
		    logger.trace("canImport true");
		    return tempCom;
		}
		tempCom = tempCom.getParent();
	    }
	    return null;
	}

	@Override
	public Transferable createTransferable(JComponent comp) {
	    dragStartMilliSeconds = System.currentTimeMillis();
	    draggedArbilNodes = null;
	    draggedTreeNodes = null;
	    selectionDraggedFromLocalCorpus = false;
	    selectionContainsArchivableLocalFile = false;
	    selectionContainsLocalFile = false;
	    selectionContainsLocalDirectory = false;
	    selectionContainsArbilResource = false;
	    selectionContainsArbilCorpus = false;
	    selectionContainsImdiCatalogue = false;
	    selectionContainsImdiSession = false;
	    selectionContainsCmdiMetadata = false;
	    selectionContainsArbilChild = false;
	    selectionContainsLocal = false;
	    selectionContainsRemote = false;
	    selectionContainsFavourite = false;
//         if (comp != null)  { logger.debug("createTransferable: " + comp.toString()); }
	    if (comp instanceof ArbilTree) {
		ArbilTree draggedTree = (ArbilTree) comp;

		// Prevent root node from being dragged
		if (!(draggedTree.getSelectionCount() > 1 || (draggedTree.getSelectionCount() == 1 && draggedTree.getSelectionPath().getPathCount() > 1))) {
		    return null;
		}

		//logger.debug("selectedCount: " + draggedTree.getSelectionCount());
		draggedArbilNodes = new ArbilDataNode[draggedTree.getSelectionCount()];
		draggedTreeNodes = new DefaultMutableTreeNode[draggedTree.getSelectionCount()];
		for (int selectedCount = 0; selectedCount < draggedTree.getSelectionCount(); selectedCount++) {
		    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) draggedTree.getSelectionPaths()[selectedCount].getLastPathComponent();
		    //logger.debug("parentNode: " + parentNode.toString());
		    if (parentNode.getUserObject() instanceof ArbilDataNode) {
			//logger.debug("DraggedImdi: " + parentNode.getUserObject().toString());
			draggedArbilNodes[selectedCount] = (ArbilDataNode) (parentNode.getUserObject());
			draggedTreeNodes[selectedCount] = parentNode;
		    } else {
			draggedArbilNodes[selectedCount] = null;
			draggedTreeNodes[selectedCount] = null;
		    }
		}
		classifyTransferableContents();
		return this;
	    } else if (comp instanceof ArbilTable) {

		draggedArbilNodes = ((ArbilTable) comp).getSelectedRowsFromTable();
		classifyTransferableContents();
		return this;
	    } else if (comp instanceof JList) {
		Object[] selectedValues = ((JList) comp).getSelectedValues();
		//logger.debug("selectedValues: " + selectedValues);
		draggedArbilNodes = new ArbilDataNode[selectedValues.length];
		for (int selectedNodeCounter = 0; selectedNodeCounter < selectedValues.length; selectedNodeCounter++) {
		    if (selectedValues[selectedNodeCounter] instanceof ArbilDataNode) {
			draggedArbilNodes[selectedNodeCounter] = (ArbilDataNode) selectedValues[selectedNodeCounter];
		    }
		}
		classifyTransferableContents();
		return this;
	    }
	    return null;
	}

	private void classifyTransferableContents() {
	    logger.trace("classifyTransferableContents");
	    // classify the draggable bundle to help matching drop targets
	    for (ArbilDataNode currentDraggedObject : draggedArbilNodes) {
		if (currentDraggedObject != null) {
		    if (currentDraggedObject.isLocal()) {
			selectionContainsLocal = true;
			logger.trace("selectionContainsLocal");
			if (currentDraggedObject.isDirectory()) {
			    selectionContainsLocalDirectory = true;
			    logger.trace("selectionContainsLocalDirectory");
			} else {
			    if (!currentDraggedObject.isMetaDataNode()) {
				selectionContainsLocalFile = true;
				logger.trace("selectionContainsLocalFile");
				if (currentDraggedObject.isArchivableFile()) {
				    selectionContainsArchivableLocalFile = true;
				    logger.trace("selectionContainsArchivableLocalFile");
				}

			    }
			}
		    } else {
			selectionContainsRemote = true;
			logger.trace("selectionContainsRemote");
		    }
		    if (currentDraggedObject.isMetaDataNode()) {
			// TG 2011/3/2: selectionContainsArbilInCache member has been removed since it wasn't used
//                        if (currentDraggedObject.isLocal() && sessionStorage.pathIsInsideCache(currentDraggedObject.getFile())) {
//                            selectionContainsArbilInCache = true;
//                            logger.debug("selectionContainsImdiInCache");
//                        }
			if (currentDraggedObject.isChildNode()) {
			    selectionContainsArbilChild = true;
			    logger.trace("selectionContainsImdiChild");
			    // only an imdichild will contain a resource
			    if (currentDraggedObject.hasResource()) {
				selectionContainsArbilResource = true;
				logger.trace("selectionContainsImdiResource");
			    }
			} else if (currentDraggedObject.isSession()) {
			    selectionContainsImdiSession = true;
			    logger.trace("selectionContainsImdiSession");
			} else if (currentDraggedObject.isCmdiMetaDataNode()) {
			    selectionContainsCmdiMetadata = true;
			    logger.trace("selectionContainsCmdiMetadata");
			} else if (currentDraggedObject.isCatalogue()) {
			    selectionContainsImdiCatalogue = true;
			    logger.trace("selectionContainsImdiCatalogue");
			} else if (currentDraggedObject.isCorpus()) {
			    selectionContainsArbilCorpus = true;
			    logger.trace("selectionContainsImdiCorpus");
			}
			if (currentDraggedObject.isFavorite()) {
			    selectionContainsFavourite = true;
			    logger.trace("selectionContainsFavourite");
			}
		    }
		}
	    }
	    selectionDraggedFromLocalCorpus = draggedFromLocalCorpus();
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
	    // due to the swing api being far to keen to do a drag drop action on the windows platform users frequently loose nodes by dragging them into random locations
	    // so to avoid this we check the date time from when the transferable was created and if less than x seconds reject the drop
	    if (System.currentTimeMillis() - dragStartMilliSeconds < (100 * 1)) {
		// todo: (has beed reduced to 100 * 1 from 100 * 3) this may be too agressive and preventing valid drag events, particularly since "improveddraggesture" property is now set.
		return false;
	    }
	    try {
		logger.trace("importData: {}", comp);
		if (comp instanceof ArbilTable && draggedArbilNodes == null) {
		    tableController.pasteIntoSelectedTableRowsFromClipBoard((ArbilTable) comp);
		} else if (draggedArbilNodes != null) {
		    return importNodes(comp);
		}
	    } catch (Exception ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
	    } finally {
		createTransferable(null); // clear the transfer objects
	    }
	    return false;
	}

	private boolean importNodes(JComponent comp) {
	    if (comp instanceof ArbilTree && canDropToTarget((ArbilTree) comp)) {
		// Drop target is targetable tree
		return importToTree((ArbilTree) comp);
	    } else {
		// Drop target is not a tree or not a valid target. Get dropable target for component
		Container target = findArbilDropableTarget(comp);
		if (target instanceof ArbilSplitPanel) {
		    ArbilSplitPanel targetPanel = (ArbilSplitPanel) target;
		    targetPanel.arbilTable.updateStoredColumnWidths();
		    ArbilTableModel dropTableModel = targetPanel.arbilTable.getArbilTableModel();
		    dropTableModel.addArbilDataNodes(draggedArbilNodes);
		    return true; // we have achieved the drag so return true
		} else if (target instanceof JDesktopPane) {
		    // Open new table window on the desktop pane for dragged nodes
		    windowManager.openFloatingTableOnce(draggedArbilNodes, null);
		    return true; // we have achieved the drag so return true
		}
	    }
	    return false;
	}

	private boolean importToTree(ArbilTree dropTree) {
	    for (int draggedCounter = 0; draggedCounter < draggedArbilNodes.length; draggedCounter++) {
		logger.trace("dragged: {}", draggedArbilNodes[draggedCounter]);
	    }
	    if (treeHelper.componentIsTheFavouritesTree(currentDropTarget)) {
		try {
		    // Target component is the favourites tree
		    boolean resultValue = ArbilFavourites.getSingleInstance().toggleFavouritesList(draggedArbilNodes, true);
		    return resultValue;
		} catch (ArbilMetadataException ex) {
		    logger.error("Error while adding favourite", ex);
		    return false;
		}
	    } else {
		return importToLocalTree(dropTree);
	    }
	}

	private boolean draggedFromLocalCorpus() {
	    if (draggedTreeNodes != null && draggedTreeNodes.length > 0) {
		return draggedTreeNodes[0].getRoot() == treeHelper.getLocalCorpusTreeModel().getRoot();
	    }
	    return false;
	}

	private boolean draggedNodesContainsChildNode() {
	    if (draggedTreeNodes != null) {
		for (DefaultMutableTreeNode node : draggedTreeNodes) {
		    if (node.getUserObject() instanceof ArbilNode && ((ArbilNode) node.getUserObject()).isChildNode()) {
			return true;
		    }
		}
	    }
	    return false;
	}

	private boolean importToLocalTree(ArbilTree dropTree) {
	    // Drop on local corpus
	    DefaultMutableTreeNode targetNode = treeHelper.getLocalCorpusTreeSingleSelection();
	    Object dropTargetUserObject = targetNode.getUserObject();
	    Vector<ArbilDataNode> importNodeList = new Vector<ArbilDataNode>();
	    Hashtable<ArbilDataNode, Vector<ArbilDataNode>> arbilNodesDeleteList = new Hashtable<ArbilDataNode, Vector<ArbilDataNode>>();
	    logger.trace("to: {}", dropTargetUserObject);

	    final ArbilNode dropTargetNode;
	    if (dropTargetUserObject instanceof ArbilNode) {
		dropTargetNode = (ArbilNode) dropTargetUserObject;
	    } else {
		return false;
	    }
	    final ArbilDataNode dropTargetDataNode = (dropTargetNode instanceof ArbilDataNode) ? (ArbilDataNode) dropTargetNode : null;

	    if (dropTargetDataNode != null) {
		// Media files can be dropped onto CMDI's and on IMDI root Resources nodes
		if (dropTargetDataNode.getParentDomNode().isCmdiMetaDataNode() && !selectionDraggedFromLocalCorpus
			|| dropTargetDataNode.isSession() || ".METATRANSCRIPT.Session.Resources.MediaFile".equals(dropTargetDataNode.getURIFragment()) /* || ((ArbilDataNode) dropTargetUserObject).isImdiChild() */) {
		    if (selectionContainsArchivableLocalFile == true
			    && selectionContainsLocalFile == true
			    && selectionContainsLocalDirectory == false
			    && selectionContainsArbilResource == false
			    && selectionContainsArbilCorpus == false
			    && selectionContainsImdiSession == false
			    && selectionContainsArbilChild == false
			    && selectionContainsLocal == true
			    && selectionContainsRemote == false) {
			logger.trace("ok to add local file");

			logger.trace("dragged: {}", (Object) draggedArbilNodes);
			new MetadataBuilder().requestAddNodes(dropTargetDataNode, widgets.getString("RESOURCE"), draggedArbilNodes);
			return true;
		    }
		}
	    }

	    if ((selectionContainsArchivableLocalFile == false || selectionDraggedFromLocalCorpus)
		    // selectionContainsLocalFile == true &&
		    && selectionContainsLocalDirectory == false
		    && selectionContainsArbilResource == false
		    && (selectionContainsArbilCorpus == false || selectionContainsImdiSession == false)) {
		logger.trace("ok to move local IMDI");

		boolean moveMultiple = draggedArbilNodes.length > 1;
		boolean moveAll = false;
		boolean continueMove = true;

		for (int draggedCounter = 0; continueMove && draggedCounter < draggedArbilNodes.length; draggedCounter++) {
		    final ArbilDataNode currentNode = draggedArbilNodes[draggedCounter];
		    logger.trace("dragged: {}", currentNode);
		    if (!currentNode.isChildNode() || dropTargetDataNode != null && MetadataReader.getSingleInstance().nodeCanExistInNode(dropTargetDataNode, currentNode)) {
			//((ArbilDataNode) dropTargetUserObject).requestAddNode(GuiHelper.imdiSchema.getNodeTypeFromMimeType(draggedImdiObjects[draggedCounter].mpiMimeType), "Resource", null, draggedImdiObjects[draggedCounter].getUrlString(), draggedImdiObjects[draggedCounter].mpiMimeType);

			// check that the node has not been dragged into itself
			boolean draggedIntoSelf = false;
			DefaultMutableTreeNode ancestorNode = targetNode;
			while (ancestorNode != null) {
			    if (draggedTreeNodes[draggedCounter].equals(ancestorNode)) {
				draggedIntoSelf = true;
				logger.trace("found ancestor: {}:{}", draggedTreeNodes[draggedCounter], ancestorNode);
			    }
			    ancestorNode = (DefaultMutableTreeNode) ancestorNode.getParent();
			}
			// todo: test for dragged to parent session

			if (!draggedIntoSelf) {
			    if (currentNode.isFavorite()) {
				// Favourite dropped on local tree 
				if (dropTargetNode instanceof ArbilRootNode) {
				    // Dropped to local corpus root node
				    new MetadataBuilder().requestAddRootNode(currentNode, currentNode.toString());
				} else if (dropTargetDataNode != null) {
				    new MetadataBuilder().requestAddNode(dropTargetDataNode, currentNode.toString(), currentNode);
				} else {
				    BugCatcherManager.getBugCatcher().logError("Cannot handle node type for drag/drop: " + dropTargetNode.getClass().toString(), null);
				}
			    } else if (!draggedFromLocalCorpus() && !(currentNode.isLocal() && sessionStorage.pathIsInsideCache(currentNode.getFile()))) {
				// External file dropped on local tree; import file(s)
				importNodeList.add(currentNode);
			    } else {
				// Moving file within local corpus
				final String targetNodeName = dropTargetNode.toString();
				int detailsOption = 1;
				//                                        if (draggedTreeNodes[draggedCounter].getUserObject())
				if (!moveAll) {
				    detailsOption = JOptionPane.showOptionDialog(windowManager.getMainFrame(),
					    MessageFormat.format(widgets.getString("MOVE_SOURCE_NODE_TO_TARGET_NODE"), draggedTreeNodes[draggedCounter].getUserObject(), targetNodeName),
					    "Arbil",
					    JOptionPane.DEFAULT_OPTION,
					    JOptionPane.PLAIN_MESSAGE,
					    null,
					    (moveMultiple ? new Object[]{widgets.getString("MOVE"), widgets.getString("MOVE ALL"), widgets.getString("SKIP"), widgets.getString("ABORT")}
					    : new Object[]{widgets.getString("MOVE"), widgets.getString("CANCEL")}),
					    moveMultiple ? widgets.getString("SKIP") : widgets.getString("CANCEL"));
				    moveAll = moveMultiple && detailsOption == 1;
				    continueMove = !(moveMultiple && detailsOption == 3);
				}
				if (continueMove && (moveAll || detailsOption == 0)) {
				    try {
					doMoveLocalNodes(dropTargetUserObject, dropTargetDataNode, currentNode, draggedCounter, arbilNodesDeleteList);
				    } catch (IOException ex) {
					logger.error("Error moving {}", currentNode, ex);
					continueMove = dialogHandler.showConfirmDialogBox(MessageFormat.format(widgets.getString("COULD NOT MOVE %S DUE TO ERROR. SEE LOG FOR DETAILS. CONTINUE MOVING NODES?"), currentNode), widgets.getString("ERROR MOVING NODES"));
				    }
				}
			    }
			}
		    }
		}
		if (importNodeList.size() > 0) {
		    try {
			ImportExportDialog importExportDialog = new ImportExportDialog(dropTree);
			if (dropTargetDataNode != null) {
			    importExportDialog.setDestinationNode(dropTargetDataNode);
			} // otherwise assume local corpus root node
			importExportDialog.copyToCache(importNodeList);
		    } catch (Exception e) {
			logger.error("Exception while showing import dialog", e);
		    }
		}
		deleteMovedNodesOriginals(arbilNodesDeleteList);
		// NOTE: FindBugs thinks this is always the case:
		if (dropTargetDataNode != null) {
		    // TODO: this save is required to prevent user data loss, but the save and reload process may not really be required here
//                                        ((ArbilDataNode) dropTargetUserObject).saveChangesToCache(false);
		    dropTargetDataNode.reloadNode();
		} else {
		    treeHelper.applyRootLocations();
		}
		return true;
	    }
	    return false;
	}

	private void doMoveLocalNodes(Object dropTargetUserObject, final ArbilDataNode dropTargetDataNode, final ArbilDataNode currentNode, int draggedCounter, Hashtable<ArbilDataNode, Vector<ArbilDataNode>> arbilNodesDeleteList) throws IOException {
	    boolean addNodeResult = false;
	    if (dropTargetUserObject instanceof ArbilDataNode) {
		if (dropTargetDataNode.isCorpus()) {
		    addNodeResult = dropTargetDataNode.addCorpusLink(currentNode);
		} else if (!dropTargetDataNode.isCmdiMetaDataNode() && (dropTargetDataNode.isEmptyMetaNode() || dropTargetDataNode.isSession())) {
		    // Dragging metadata node onto empty node
		    if (MetadataReader.getSingleInstance().nodeCanExistInNode(dropTargetDataNode, currentNode)) {
			try {
			    // Add source to destination
			    new MetadataBuilder().addNodes(dropTargetDataNode, new String[]{currentNode.toString()}, new ArbilDataNode[]{currentNode});
			    addNodeResult = true;
			} catch (ArbilMetadataException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			    dialogHandler.addMessageDialogToQueue(ex.getLocalizedMessage(), widgets.getString("INSERT NODE ERROR"));
			}
		    }
		} else if (dropTargetDataNode.isCmdiMetaDataNode()) {
		    if (currentNode.isMetaDataNode() && !currentNode.isCmdiMetaDataNode()) {
			//TODO: Add support for converting IMDI to CMDI. Should also be possible to add IMDI as 'dead' resource
			dialogHandler.addMessageDialogToQueue(widgets.getString("MOVING IMDI METADATA TO CMDI METADATA FILES IS CURRENTLY NOT SUPPORTED"), widgets.getString("NOT SUPPORTED"));
		    } else {
			if (currentNode.isCmdiMetaDataNode() && currentNode.isChildNode()) {
			    try {
				addNodeResult = null != new ArbilComponentBuilder().insertFavouriteComponent(dropTargetDataNode, currentNode);
			    } catch (ArbilMetadataException ex) {
				addNodeResult = false;
			    }
			} else {
			    ArbilComponentBuilder arbilComponentBuilder = new ArbilComponentBuilder();
			    // Add as ResourceProxy
			    addNodeResult = null != arbilComponentBuilder.insertResourceProxy(dropTargetDataNode, currentNode);
			}
		    }
		}
	    } else {
		addNodeResult = treeHelper.addLocation(currentNode.getURI());
	    }

	    if (addNodeResult) {
		if (draggedTreeNodes[draggedCounter] != null) {
		    if (draggedTreeNodes[draggedCounter].getParent().equals(draggedTreeNodes[draggedCounter].getRoot())) {
			logger.trace("dragged from root");
			treeHelper.removeLocation(currentNode);
			treeHelper.applyRootLocations();
		    } else {
			ArbilDataNode parentNode = (ArbilDataNode) ((DefaultMutableTreeNode) draggedTreeNodes[draggedCounter].getParent()).getUserObject();
			logger.trace("removeing from parent: {}", parentNode);
			// add the parent and the child node to the deletelist
			if (!arbilNodesDeleteList.containsKey(parentNode)) {
			    arbilNodesDeleteList.put(parentNode, new Vector());
			}
			arbilNodesDeleteList.get(parentNode).add(currentNode);
		    }
		}
	    } else {
		BugCatcherManager.getBugCatcher().logError("Could not add node " + currentNode.toString() + " to target " + dropTargetUserObject.toString(), null);
	    }
	}

	private void deleteMovedNodesOriginals(Hashtable<ArbilDataNode, Vector<ArbilDataNode>> arbilNodesDeleteList) {
	    for (Entry<ArbilDataNode, Vector<ArbilDataNode>> entry : arbilNodesDeleteList.entrySet()) {
		final ArbilDataNode currentParent = entry.getKey();
		final Vector<ArbilDataNode> children = entry.getValue();
		if (currentParent.isCorpus()) {
		    // Removing sessions from corpus
		    logger.trace("deleting by corpus link");
		    ArbilDataNode[] arbilNodeArray = children.toArray(new ArbilDataNode[]{});
		    currentParent.deleteCorpusLink(arbilNodeArray);
		} else if (currentParent.isMetaDataNode()) {
		    // Removing metadata nodes from session
		    treeHelper.deleteChildNodes(currentParent, children);
		}
	    }
	}

	public Object getTransferData(DataFlavor flavor) {
	    logger.debug("getTransferData");
	    if (isDataFlavorSupported(flavor)) {
		return draggedArbilNodes;
	    }
	    return null;
	}

	public DataFlavor[] getTransferDataFlavors() {
	    logger.debug("getTransferDataFlavors");
	    return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
	    logger.debug("isDataFlavorSupported");
	    return flavors[0].equals(flavor);
	}
    }
}
