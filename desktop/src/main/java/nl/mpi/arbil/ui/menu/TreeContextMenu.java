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
package nl.mpi.arbil.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilRootNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.ContainerNode;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.importexport.ArbilCsvImporter;
import nl.mpi.arbil.favourites.ArbilFavourites;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.templates.ArbilTemplateManager.MenuItemData;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilTreeController;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.ImportExportDialog;
import nl.mpi.arbil.ui.favourites.FavouritesImportExportGUI;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 * Context menu for tree UI components
 *
 * @author Twan Goosen
 */
public class TreeContextMenu extends ArbilContextMenu {
    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus");

    private final TreeHelper treeHelper;
    private final MessageDialogHandler dialogHandler;
    private final ArbilTreeController treeController;
    private final ArbilNode leadSelectedNode;
    private final SessionStorage sessionStorage;
    private final WindowManager windowManager;
    private final ApplicationVersionManager versionManager;

    public TreeContextMenu(ArbilTree tree, ArbilTreeController treeController, TreeHelper treeHelper, MessageDialogHandler dialogHandler, WindowManager windowManager, SessionStorage sessionStorage, ApplicationVersionManager versionManager) {
	this.treeController = treeController;
	this.treeHelper = treeHelper;
	this.dialogHandler = dialogHandler;
	this.sessionStorage = sessionStorage;
	this.windowManager = windowManager;
	this.versionManager = versionManager;
	this.tree = tree;
	setInvoker(tree);

	selectedTreeNodes = tree.getSelectedNodes();
	leadSelectedNode = tree.getLeadSelectionNode();
	leadSelectedDataNode = tree.getLeadSelectionDataNode();
    }

    @Override
    protected void setUpMenu() {
	setUpItems();
	setUpActions();
    }

    private void setUpItems() {
	final int selectionCount = tree.getSelectionCount();
	final int nodeLevel = (selectionCount > 0) ? tree.getSelectionPath().getPathCount() : -1;
	final boolean showRemoveLocationsTasks = (selectionCount == 1 && nodeLevel == 2) || selectionCount > 1;
	final boolean showAddLocationsTasks = selectionCount == 1 && nodeLevel == 1;

	viewSelectedNodesMenuItem.setText(menus.getString("VIEW SELECTED"));
	viewSelectedSubnodesMenuItem.setText(leadSelectedDataNode != null && leadSelectedDataNode.isEditable() ? menus.getString("EDIT ALL METADATA") : menus.getString("VIEW ALL METADATA"));
	editInLongFieldEditor.setText(leadSelectedDataNode != null && leadSelectedDataNode.getParentDomNode().isEditable() ? menus.getString("EDIT IN LONG FIELD EDITOR") : menus.getString("VIEW IN LONG FIELD EDITOR"));
//        mergeWithFavouritesMenu.setEnabled(false);
	deleteMenuItem.setEnabled(true);

	if (getTreePanel() != null) {
	    if (tree == getTreePanel().remoteCorpusTree) {
		removeRemoteCorpusMenuItem.setVisible(showRemoveLocationsTasks);
		addRemoteCorpusMenuItem.setVisible(showAddLocationsTasks);
		addRemoteCorpusToRootMenuItem.setVisible(selectionCount > 0 && nodeLevel > 2);
		copyBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
		addDefaultLocationsMenuItem.setVisible(showAddLocationsTasks);
		searchRemoteBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1 && !leadSelectedDataNode.isCmdiMetaDataNode());
	    }
	    if (tree == getTreePanel().localCorpusTree) {
		viewSelectedNodesMenuItem.setText(menus.getString("VIEW/EDIT SELECTED"));
		//removeCachedCopyMenuItem.setVisible(showRemoveLocationsTasks);
		pasteMenuItem1.setVisible(selectionCount > 0 && nodeLevel > 1);
		searchSubnodesMenuItem.setVisible(selectionCount > 0);
		// a corpus can be added even at the root node
		addMenu.setVisible(selectionCount == 1); // && /*nodeLevel > 1 &&*/ treeHelper.arbilTreePanel.localCorpusTree.getSelectionCount() > 0/* && ((DefaultMutableTreeNode)localCorpusTree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof */); // could check for imdi childnodes
//            addMenu.setEnabled(nodeLevel > 1); // not yet functional so lets dissable it for now
//            addMenu.setToolTipText("test balloon on dissabled menu item");
		deleteMenuItem.setVisible(nodeLevel > 1);
		if (leadSelectedDataNode != null) {
		    final boolean nodeIsChild = leadSelectedDataNode.isChildNode();

		    validateMenuItem.setVisible(!nodeIsChild);
		    historyMenu.setVisible(leadSelectedDataNode.hasHistory());
		    exportMenuItem.setVisible(!nodeIsChild);
		    importCsvMenuItem.setVisible(leadSelectedDataNode.isCorpus());
		    importBranchMenuItem.setVisible(leadSelectedDataNode.isCorpus());
		    reImportBranchMenuItem.setVisible(selectedTreeNodes.length == 1 && leadSelectedDataNode.archiveHandle != null && !leadSelectedDataNode.isChildNode());
		}
		// set up the favourites menu
		addFromFavouritesMenu.setVisible(true);
		addResourcesFavouritesMenu.setVisible(true);
	    }
	    if (tree == getTreePanel().localDirectoryTree) {
		removeLocalDirectoryMenuItem.setVisible(showRemoveLocationsTasks);
		if (showAddLocationsTasks) {
		    showHiddenFilesMenuItem.setState(treeHelper.isShowHiddenFilesInTree());
		    showHiddenFilesMenuItem.setVisible(true);
		}
		addLocalDirectoryMenuItem.setVisible(showAddLocationsTasks);
		if (leadSelectedDataNode != null) {
		    copyBranchMenuItem.setVisible(leadSelectedDataNode.isCorpus() || leadSelectedDataNode.isSession());
		}
	    }
	}

	if (leadSelectedDataNode != null) {

	    if (leadSelectedDataNode.canHaveResource()) {
		setManualResourceLocationMenuItem.setVisible(true);
	    }

	    if (leadSelectedDataNode.isResourceSet() || !leadSelectedDataNode.isMetaDataNode()) {
		forceTypeCheckMenuItem.setVisible(true);
	    }

	    if (leadSelectedDataNode.isFavorite()) {
		boolean isFavouriteTopLevel = treeHelper.isInFavouritesNodes(leadSelectedDataNode);
		addToFavouritesMenuItem.setVisible(false);
		removeFromFavouritesMenuItem.setVisible(isFavouriteTopLevel);
		removeFromFavouritesMenuItem.setEnabled(isFavouriteTopLevel);

		addMenu.setVisible(selectedTreeNodes.length == 1);// for now adding is limited to single node selections
		viewSelectedNodesMenuItem.setText(menus.getString("VIEW/EDIT SELECTED"));
		// for now deleting is limited to single node selections, to prevent top level favourites for being deleted in multi-selections
		deleteMenuItem.setVisible(!isFavouriteTopLevel && selectedTreeNodes.length == 1);
		deleteMenuItem.setEnabled(!isFavouriteTopLevel && selectedTreeNodes.length == 1);
	    } else { // Nodes that are not favourites
		removeFromFavouritesMenuItem.setVisible(false);
		addToFavouritesMenuItem.setVisible(leadSelectedDataNode.isMetaDataNode());
		addToFavouritesMenuItem.setEnabled(!leadSelectedDataNode.isCorpus() && leadSelectedDataNode.isMetaDataNode());
	    }
	} else {
	    addToFavouritesMenuItem.setVisible(false);
	}

	if (leadSelectedNode instanceof ArbilRootNode) {
	    if (leadSelectedNode.equals((((DefaultMutableTreeNode) treeHelper.getFavouritesTreeModel().getRoot()).getUserObject()))) {
		// Selected node is Favourites root node
		importExportFavouritesMenuItem.setVisible(true);

		groupFavouritesMenuItem.setState(treeHelper.isGroupFavouritesByType());
		groupFavouritesMenuItem.setVisible(true);
	    }
	}


	ArbilNode[] selectedNodes = tree.getAllSelectedNodes();

	copyNodeUrlMenuItem.setVisible(((selectionCount == 1 && nodeLevel > 1) || selectionCount > 1) && !(selectedNodes[0] instanceof ContainerNode));
	viewSelectedNodesMenuItem.setVisible(selectionCount >= 1 && nodeLevel > 1 && !(selectedNodes[0] instanceof ContainerNode));
	reloadSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1 && !(selectedNodes[0] instanceof ContainerNode));
	viewSelectedSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1
		&& selectedNodes[0].isMetaDataNode() && !selectedNodes[0].isCorpus() && !(selectedNodes[0] instanceof ContainerNode));
	editInLongFieldEditor.setVisible(selectionCount > 0 && nodeLevel > 1
		&& !selectedNodes[0].isEmptyMetaNode() && !(selectedNodes[0] instanceof ContainerNode));
    }

    private void setUpActions() {
	viewSelectedNodesMenuItem.setText(menus.getString("VIEW SELECTED"));
	viewSelectedNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		treeController.viewSelectedNodes((ArbilTree) getInvoker());
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_TOP, viewSelectedNodesMenuItem);

	editInLongFieldEditor.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		treeController.startLongFieldEditor((ArbilTree) getInvoker());
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_TOP + 1, editInLongFieldEditor);

	viewSelectedSubnodesMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		treeController.viewSelectedSubnodes((ArbilTree) getInvoker());
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_TOP + 2, viewSelectedSubnodesMenuItem);

	deleteMenuItem.setText(menus.getString("DELETE"));
	deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeHelper.deleteNodes(getInvoker());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_TOP + 10, deleteMenuItem);

	copyNodeUrlMenuItem.setText(menus.getString("COPY"));
	copyNodeUrlMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    if (selectedTreeNodes == null) {
			dialogHandler.addMessageDialogToQueue(menus.getString("NO NODE SELECTED"), menus.getString("COPY"));
		    } else {
			ArbilTree sourceTree = (ArbilTree) getInvoker();
			sourceTree.copyNodeUrlToClipboard(selectedTreeNodes);
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_TOP + 15, copyNodeUrlMenuItem);

	pasteMenuItem1.setText(menus.getString("PASTE"));
	pasteMenuItem1.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode currentNode : selectedTreeNodes) {
			currentNode.pasteIntoNode();
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_TOP + 20, pasteMenuItem1);

	searchRemoteBranchMenuItem.setText(menus.getString("SEARCH REMOTE CORPUS"));
	searchRemoteBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeController.searchRemoteSubnodes(getTreePanel());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_MIDDLE, searchRemoteBranchMenuItem);

	copyBranchMenuItem.setText(menus.getString("IMPORT TO LOCAL CORPUS"));
	copyBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeController.copyBranch(tree);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE, copyBranchMenuItem);

	searchSubnodesMenuItem.setText(menus.getString("SEARCH"));
	searchSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeController.searchSubnodes(getTreePanel());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_MIDDLE, searchSubnodesMenuItem);

	reloadSubnodesMenuItem.setText(menus.getString("RELOAD"));
	reloadSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode currentNode : selectedTreeNodes) {
			// this reload will first clear the save is required flag then reload
			currentNode.reloadNode();
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_MIDDLE + 5, reloadSubnodesMenuItem);

	groupFavouritesMenuItem.setText(menus.getString("GROUP FAVOURITES BY TYPE"));//menus.getString("SHOW HIDDEN FILES"));
	groupFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeHelper.setGroupFavouritesByType(groupFavouritesMenuItem.getState());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_TOP, groupFavouritesMenuItem);

	addMenu.setText(menus.getString("ADD"));
	addMenu.addMenuListener(new javax.swing.event.MenuListener() {
	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		try {
		    initAddMenu(addMenu, leadSelectedDataNode);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_TOP, addMenu);

	addFromFavouritesMenu.setText(menus.getString("ADD FROM FAVOURITES"));
	addFromFavouritesMenu.addMenuListener(new javax.swing.event.MenuListener() {
	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		initAddFromFavouritesMenu();
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_MIDDLE, addFromFavouritesMenu);

	addResourcesFavouritesMenu.setText(menus.getString("ADD BULK RESOURCES VIA FAVOURITES"));
	addResourcesFavouritesMenu.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {

		// handle add actions on the root tree node
		ArbilNode targetObject = leadSelectedDataNode;
		if (targetObject == null) {
		    // No lead selected tree node, so pass local corpus root node
		    targetObject = (ArbilNode) (((DefaultMutableTreeNode) treeHelper.getLocalCorpusTreeModel().getRoot()).getUserObject());
		}
		treeController.addBulkResources(targetObject);
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_MIDDLE, addResourcesFavouritesMenu);

	if (leadSelectedDataNode != null && leadSelectedDataNode.isContainerNode()) {
	    addToFavouritesMenuItem.setText(menus.getString("ADD CHILDREN TO FAVOURITES LIST"));
	} else {
	    addToFavouritesMenuItem.setText(menus.getString("ADD TO FAVOURITES LIST"));
	}
	addToFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilFavourites.getSingleInstance().toggleFavouritesList(((ArbilTree) getInvoker()).getSelectedNodes(), true);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_MIDDLE + 5, addToFavouritesMenuItem);

	removeFromFavouritesMenuItem.setText(menus.getString("REMOVE FROM FAVOURITES LIST"));
	removeFromFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilFavourites.getSingleInstance().toggleFavouritesList(((ArbilTree) getInvoker()).getSelectedNodes(), false);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_MIDDLE + 5, removeFromFavouritesMenuItem);

//        mergeWithFavouritesMenu.setText("Merge With Favourite");
//        mergeWithFavouritesMenu.setActionCommand("Merge With Favouurite");

	validateMenuItem.setText(menus.getString("CHECK XML CONFORMANCE"));
	validateMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeController.validateNodes(selectedTreeNodes);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_XML, PRIORITY_MIDDLE, validateMenuItem);

	historyMenu.setText(menus.getString("HISTORY"));
	historyMenu.addMenuListener(new javax.swing.event.MenuListener() {
	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		try {
		    initHistoryMenu();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_BOTTOM, historyMenu);


	addRemoteCorpusMenuItem.setText(menus.getString("ADD REMOTE LOCATION"));
	addRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeController.addRemoteCorpus();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE, addRemoteCorpusMenuItem);

	addRemoteCorpusToRootMenuItem.setText(menus.getString("ADD TO TREE ROOT"));
	addRemoteCorpusToRootMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode node : selectedTreeNodes) {
			treeController.addRemoteCorpus(node.getUrlString());
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE, addRemoteCorpusToRootMenuItem);
	addDefaultLocationsMenuItem.setText(menus.getString("ADD DEFAULT REMOTE LOCATIONS"));
	addDefaultLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    if (0 < treeHelper.addDefaultCorpusLocations()) {
			treeHelper.applyRootLocations();
		    } else {
			// alert the user when the node already exists and cannot be added again
			dialogHandler.addMessageDialogToQueue(menus.getString("THE DEFAULT LOCATIONS ALREADY EXISTS AND WILL NOT BE ADDED AGAIN"), menus.getString("ADD DEFAULT LOCATIONS"));
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE + 5, addDefaultLocationsMenuItem);

	removeRemoteCorpusMenuItem.setText(menus.getString("REMOVE REMOTE LOCATION"));
	removeRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode selectedNode : selectedTreeNodes) {
			treeHelper.removeLocation(selectedNode);
		    }
		    treeHelper.applyRootLocations();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE + 10, removeRemoteCorpusMenuItem);

//	removeCachedCopyMenuItem.setText("Remove Cache Link");
//	removeCachedCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {
//	    public void actionPerformed(java.awt.event.ActionEvent evt) {
//		try {
//		    removeCachedCopyMenuItemActionPerformed(evt);
//		} catch (Exception ex) {
//		    BugCatcherManager.getBugCatcher().logError(ex);
//		}
//	    }
//	});
//	addItem(CATEGORY_DISK, PRIORITY_BOTTOM + 5, removeCachedCopyMenuItem);

	addLocalDirectoryMenuItem.setText(menus.getString("ADD WORKING DIRECTORY"));
	addLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeController.addLocalDirectory();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	addItem(CATEGORY_WORKING_DIR, PRIORITY_TOP, addLocalDirectoryMenuItem);

	showHiddenFilesMenuItem.setText(menus.getString("SHOW HIDDEN FILES"));
	showHiddenFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeHelper.setShowHiddenFilesInTree(showHiddenFilesMenuItem.getState());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_WORKING_DIR, PRIORITY_MIDDLE + 5, showHiddenFilesMenuItem);

	removeLocalDirectoryMenuItem.setText(menus.getString("REMOVE LINK TO DIRECTORY"));
	removeLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode selectedNode : selectedTreeNodes) {
			treeHelper.removeLocation(selectedNode);
		    }
		    treeHelper.applyRootLocations();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_BOTTOM, removeLocalDirectoryMenuItem);

//        viewChangesMenuItem.setText("View Changes");
//        viewChangesMenuItem.setEnabled(false);
//        viewChangesMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    for (ArbilDataNode currentNode : selectedTreeNodes) {
////                        LinorgWindowManager.getSingleInstance().openDiffWindow(currentNode);
//                    }
//                } catch (Exception ex) {
//                    BugCatcherManager.getBugCatcher().logError(ex);
//                }
//            }
//        });
//        add(viewChangesMenuItem);

//        sendToServerMenuItem.setText("Send to Server");
//        sendToServerMenuItem.setEnabled(false);
//        sendToServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    sendToServerMenuItemActionPerformed(evt);
//
//                } catch (Exception ex) {
//                    BugCatcherManager.getBugCatcher().logError(ex);
//                }
//            }
//        });
//        add(sendToServerMenuItem);

	exportMenuItem.setText(menus.getString("EXPORT"));
	exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ImportExportDialog importExportDialog = new ImportExportDialog(getTreePanel().remoteCorpusTree);
		    importExportDialog.selectExportDirectoryAndExport(((ArbilTree) getInvoker()).getSelectedNodes());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_DISK, PRIORITY_TOP + 5, exportMenuItem);

	importCsvMenuItem.setText(menus.getString("IMPORT CSV"));
	importCsvMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilCsvImporter csvImporter = new ArbilCsvImporter(leadSelectedDataNode);
		    csvImporter.doImport();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_IMPORT, PRIORITY_TOP, importCsvMenuItem);

	importBranchMenuItem.setText(menus.getString("IMPORT BRANCH"));
	importBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ImportExportDialog importExportDialog = new ImportExportDialog(getTreePanel().localCorpusTree); // TODO: this may not always be to correct component and this code should be updated
		    importExportDialog.setDestinationNode(leadSelectedDataNode);
		    importExportDialog.importArbilBranch();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_IMPORT, PRIORITY_TOP + 5, importBranchMenuItem);

	if (leadSelectedDataNode != null) {
	    if (leadSelectedDataNode.isSession()) {
		reImportBranchMenuItem.setText(menus.getString("RE-IMPORT THIS SESSION"));
	    } else {
		reImportBranchMenuItem.setText(menus.getString("RE-IMPORT THIS BRANCH"));
	    }
	    reImportBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    treeController.reImportBranch(leadSelectedDataNode, getTreePanel());
		}
	    });
	    addItem(CATEGORY_IMPORT, PRIORITY_MIDDLE, reImportBranchMenuItem);
	}

	setManualResourceLocationMenuItem.setText(menus.getString("INSERT MANUAL RESOURCE LOCATION"));
	setManualResourceLocationMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		treeController.setManualResourceLocation(leadSelectedDataNode);
	    }
	});
	addItem(CATEGORY_RESOURCE, PRIORITY_MIDDLE, setManualResourceLocationMenuItem);

	forceTypeCheckMenuItem.setText(menus.getString("FORCE TYPE CHECKING"));
	forceTypeCheckMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		treeController.forceTypeCheck(leadSelectedDataNode);
	    }
	});
	addItem(CATEGORY_RESOURCE, PRIORITY_BOTTOM, forceTypeCheckMenuItem);

	importExportFavouritesMenuItem.setText(menus.getString("IMPORT/EXPORT FAVOURITES"));
	importExportFavouritesMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		final FavouritesImportExportGUI importExportUI = new FavouritesImportExportGUI(dialogHandler, sessionStorage, treeHelper, versionManager.getApplicationVersion());
		importExportUI.showDialog(windowManager.getMainFrame());
	    }
	});
	addItem(CATEGORY_IMPORT, PRIORITY_TOP, importExportFavouritesMenuItem);
    }

    public void initAddMenu(JMenu addMenu, Object targetNodeUserObject) {
	addMenu.removeAll();

	// For corpus nodes (and other nodes that are non-data i.e. root nodes(?)), show selected templates and profiles

	if (!(targetNodeUserObject instanceof ArbilDataNode) || ((ArbilDataNode) targetNodeUserObject).isCorpus()) {
	    // consume the selected templates here rather than the clarin profile list
	    for (MenuItemData currentAddable : ArbilTemplateManager.getSingleInstance().getSelectedTemplatesMenuItems()) {
		// Check type. For root nodes (null object), allow all. For IMDI corpus, we should only allow IMDI
		if (!(targetNodeUserObject instanceof ArbilDataNode) || currentAddable.type == MenuItemData.Type.IMDI) {
		    JMenuItem addMenuItem;
		    addMenuItem = new JMenuItem();
		    addMenuItem.setText(currentAddable.menuText);
		    addMenuItem.setName(currentAddable.menuText);
		    addMenuItem.setActionCommand(currentAddable.menuAction);
		    addMenuItem.setToolTipText(currentAddable.menuToolTip);
		    addMenuItem.setIcon(currentAddable.menuIcon);
		    addMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
			    treeController.addNodeFromTemplate(leadSelectedDataNode, evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
			}
		    });
		    addMenu.add(addMenuItem);
		}
	    }
	}

	// For all data nodes, get list of allowed types and add them to the menu
	if (targetNodeUserObject instanceof ArbilDataNode) {
	    final ArbilTemplate currentTemplate = ((ArbilDataNode) targetNodeUserObject).getNodeTemplate();

	    MetadataBuilder mdBuilder = new MetadataBuilder();

	    for (Enumeration menuItemName = currentTemplate.listTypesFor(targetNodeUserObject); menuItemName.hasMoreElements();) {
		final String[] currentField = (String[]) menuItemName.nextElement();
		final String nodeText = currentField[0];
		final String nodeType = currentField[1];

		JMenuItem addMenuItem;
		addMenuItem = new JMenuItem();
		addMenuItem.setText(nodeText);
		addMenuItem.setName(nodeText);
		addMenuItem.setToolTipText(nodeType);
		if (null != currentTemplate.pathIsChildNode(nodeType)) {
		    addMenuItem.setIcon(ArbilIcons.getSingleInstance().dataIcon);
		} else {
		    addMenuItem.setIcon(ArbilIcons.getSingleInstance().fieldIcon);
		}

		if (mdBuilder.canAddChildNode((ArbilDataNode) targetNodeUserObject, nodeType)) {

		    addMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
			    treeController.addSubnode(leadSelectedDataNode, nodeType, nodeText);
			}
		    });
		} else {
		    addMenuItem.setEnabled(false);
		}
		addMenu.add(addMenuItem);
	    }
	}
    }

    public void initHistoryMenu() {
	historyMenu.removeAll();
	for (String[] currentHistory : leadSelectedDataNode.getHistoryList()) {
	    JMenuItem revertHistoryMenuItem;
	    revertHistoryMenuItem = new JMenuItem();
	    revertHistoryMenuItem.setText(currentHistory[0]);
	    revertHistoryMenuItem.setName(currentHistory[0]);
	    revertHistoryMenuItem.setActionCommand(currentHistory[1]);
	    revertHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    try {
			if (!leadSelectedDataNode.resurrectHistory(evt.getActionCommand())) {
			    dialogHandler.addMessageDialogToQueue(menus.getString("COULD NOT REVERT VERSION, NO CHANGES MADE"), menus.getString("HISTORY"));
			}
		    } catch (Exception ex) {
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		}
	    });
	    historyMenu.add(revertHistoryMenuItem);
	}
    }

    public void initAddFromFavouritesMenu() {
	addFromFavouritesMenu.removeAll();
	Object targetObject = leadSelectedDataNode;
	if (targetObject == null) {
	    // No lead selected tree node, so pass local corpus root node
	    targetObject = ((DefaultMutableTreeNode) treeHelper.getLocalCorpusTreeModel().getRoot()).getUserObject();
	}
	for (ArbilDataNode menuItemName : ArbilFavourites.getSingleInstance().listFavouritesFor(targetObject)) {
	    JMenuItem addFavouriteMenuItem;
	    addFavouriteMenuItem = new JMenuItem();
	    addFavouriteMenuItem.setText(menuItemName.toString());
	    addFavouriteMenuItem.setName(menuItemName.toString());
	    addFavouriteMenuItem.setActionCommand(menuItemName.getUrlString());
	    addFavouriteMenuItem.setIcon(menuItemName.getIcon());
	    addFavouriteMenuItem.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    treeController.addFromFavourite(leadSelectedDataNode, evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
		}
	    });
	    addFromFavouritesMenu.add(addFavouriteMenuItem);
	}
    }

    private ArbilTreePanels getTreePanel() {
	if (treeHelper instanceof ArbilTreeHelper) {
	    return ((ArbilTreeHelper) treeHelper).getArbilTreePanel();
	} else {
	    return null;
	}
    }

    @Override
    protected void setAllInvisible() {
	removeCachedCopyMenuItem.setVisible(false);
	removeLocalDirectoryMenuItem.setVisible(false);
	addLocalDirectoryMenuItem.setVisible(false);
	showHiddenFilesMenuItem.setVisible(false);
	removeRemoteCorpusMenuItem.setVisible(false);
	addRemoteCorpusMenuItem.setVisible(false);
	addRemoteCorpusToRootMenuItem.setVisible(false);
	copyBranchMenuItem.setVisible(false);
	searchRemoteBranchMenuItem.setVisible(false);
	copyNodeUrlMenuItem.setVisible(false);
	pasteMenuItem1.setVisible(false);

	searchSubnodesMenuItem.setVisible(false);
	reloadSubnodesMenuItem.setVisible(false);
	addDefaultLocationsMenuItem.setVisible(false);
	addMenu.setVisible(false);
	deleteMenuItem.setVisible(false);
	viewSelectedNodesMenuItem.setVisible(false);
	addFromFavouritesMenu.setVisible(false);
	addResourcesFavouritesMenu.setVisible(false);
	importExportFavouritesMenuItem.setVisible(false);
	groupFavouritesMenuItem.setVisible(false);
	//viewChangesMenuItem.setVisible(false);
	//sendToServerMenuItem.setVisible(false);
	validateMenuItem.setVisible(false);
	historyMenu.setVisible(false);
	exportMenuItem.setVisible(false);
	importCsvMenuItem.setVisible(false);
	importBranchMenuItem.setVisible(false);
	reImportBranchMenuItem.setVisible(false);
	addToFavouritesMenuItem.setVisible(false);
	removeFromFavouritesMenuItem.setVisible(false);
	viewSelectedSubnodesMenuItem.setVisible(false);
	editInLongFieldEditor.setVisible(false);

	setManualResourceLocationMenuItem.setVisible(false);
	forceTypeCheckMenuItem.setVisible(false);
    }
    private ArbilTree tree;
    private JMenu addFromFavouritesMenu = new JMenu();
    private JMenuItem addResourcesFavouritesMenu = new JMenuItem();
    private JMenuItem addLocalDirectoryMenuItem = new JMenuItem();
    private JCheckBoxMenuItem showHiddenFilesMenuItem = new JCheckBoxMenuItem();
    private JCheckBoxMenuItem groupFavouritesMenuItem = new JCheckBoxMenuItem();
    private JMenuItem addDefaultLocationsMenuItem = new JMenuItem();
    private JMenu addMenu = new JMenu();
    private JMenuItem addRemoteCorpusMenuItem = new JMenuItem();
    private JMenuItem addRemoteCorpusToRootMenuItem = new JMenuItem();
    private JMenuItem addToFavouritesMenuItem = new JMenuItem();
    private JMenuItem removeFromFavouritesMenuItem = new JMenuItem();
    private JMenuItem copyBranchMenuItem = new JMenuItem();
    private JMenuItem searchRemoteBranchMenuItem = new JMenuItem();
    private JMenuItem copyNodeUrlMenuItem = new JMenuItem();
    private JMenuItem deleteMenuItem = new JMenuItem();
    private JMenuItem exportMenuItem = new JMenuItem();
    private JMenuItem importCsvMenuItem = new JMenuItem();
    private JMenuItem importBranchMenuItem = new JMenuItem();
    private JMenuItem reImportBranchMenuItem = new JMenuItem();
//    private JMenu mergeWithFavouritesMenu = new JMenu();
    private JMenuItem pasteMenuItem1 = new JMenuItem();
    private JMenuItem reloadSubnodesMenuItem = new JMenuItem();
    private JMenuItem removeCachedCopyMenuItem = new JMenuItem();
    private JMenuItem removeLocalDirectoryMenuItem = new JMenuItem();
    private JMenuItem removeRemoteCorpusMenuItem = new JMenuItem();
    private JMenuItem searchSubnodesMenuItem = new JMenuItem();
//    private JMenuItem sendToServerMenuItem = new JMenuItem();
    private JMenuItem validateMenuItem = new JMenuItem();
    private JMenu historyMenu = new JMenu();
//    private JMenuItem viewChangesMenuItem = new JMenuItem();
    private JMenuItem viewSelectedNodesMenuItem = new JMenuItem();
    private JMenuItem viewSelectedSubnodesMenuItem = new JMenuItem();
    private JMenuItem editInLongFieldEditor = new JMenuItem();
    private JMenuItem setManualResourceLocationMenuItem = new JMenuItem();
    private JMenuItem importExportFavouritesMenuItem = new JMenuItem();
    private JMenuItem forceTypeCheckMenuItem = new JMenuItem();
}
