/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.HeadlessException;
import java.awt.Point;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeService;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.MetadataFormat;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;
import nl.mpi.arbil.ui.menu.TreeContextMenu;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.arbil.util.XsdChecker;

/**
 * Controller class for tree actions, most of them probably called from
 * {@link TreeContextMenu}
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTreeController {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private final SessionStorage sessionStorage;
    private final TreeHelper treeHelper;
    private final WindowManager windowManager;
    private final MessageDialogHandler dialogHandler;
    private final DataNodeLoader dataNodeLoader;
    private final ApplicationVersionManager versionManager;
    private final MimeHashQueue mimeHashQueue;

    public ArbilTreeController(SessionStorage sessionStorage, TreeHelper treeHelper, WindowManager windowManager, MessageDialogHandler dialogHandler, DataNodeLoader dataNodeLoader, MimeHashQueue mimeHashQueue, ApplicationVersionManager versionManager) {
        this.sessionStorage = sessionStorage;
        this.treeHelper = treeHelper;
        this.windowManager = windowManager;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
        this.mimeHashQueue = mimeHashQueue;
        this.versionManager = versionManager;
    }

    public void reImportBranch(ArbilDataNode leadSelectedTreeNode, ArbilTreePanels treePanels) {
        try {
            URI remoteDataFile = sessionStorage.getOriginatingUri(leadSelectedTreeNode.getURI());
            if (remoteDataFile != null) {
                ArbilDataNode originatingNode = dataNodeLoader.getArbilDataNodeWithoutLoading(remoteDataFile);
                if (originatingNode.isLocal() && !originatingNode.getFile().exists()) {
                    dialogHandler.addMessageDialogToQueue(widgets.getString("THE ORIGIONAL FILE LOCATION CANNOT BE FOUND"), widgets.getString("RE IMPORT BRANCH"));
                } else if (originatingNode.isMetaDataNode()) {
                    ImportExportDialog importExportDialog = new ImportExportDialog(treePanels.localCorpusTree); // TODO: this may not always be to correct component and this code should be updated
                    importExportDialog.setDestinationNode(leadSelectedTreeNode); // TODO: do not re add the location in this case
                    importExportDialog.copyToCache(new ArbilDataNode[]{originatingNode});
                } else {
                    dialogHandler.addMessageDialogToQueue(widgets.getString("COULD NOT DETERMINE THE ORIGIONAL NODE TYPE"), widgets.getString("RE IMPORT BRANCH"));
                }
            } else {
                dialogHandler.addMessageDialogToQueue(widgets.getString("COULD NOT DETERMINE THE ORIGIONAL LOCATION"), widgets.getString("RE IMPORT BRANCH"));
            }
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
    }

    public void setManualResourceLocation(ArbilDataNode leadSelectedTreeNode) throws HeadlessException {

        String initialValue;
        if (leadSelectedTreeNode.hasLocalResource()) {
            initialValue = leadSelectedTreeNode.resourceUrlField.getFieldValue();
        } else {
            initialValue = "";
        }
        String manualLocation = (String) JOptionPane.showInputDialog(windowManager.getMainFrame(), widgets.getString("ENTER THE RESOURCE URI:"), widgets.getString("MANUAL RESOURCE LOCATION"), JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
        if (manualLocation != null) { // Not canceled
            try {
                final URI locationURI = new URI(manualLocation);
                if (checkResourceLocation(locationURI)) {
                    leadSelectedTreeNode.insertResourceLocation(locationURI);
                }
            } catch (URISyntaxException ex) {
                dialogHandler.addMessageDialogToQueue(widgets.getString("THE URI ENTERED AS A RESOURCE LOCATION IS INVALID. PLEASE CHECK THE LOCATION AND TRY AGAIN."), widgets.getString("INVALID URI"));
            } catch (ArbilMetadataException ex) {
                BugCatcherManager.getBugCatcher().logError(ex);
                dialogHandler.addMessageDialogToQueue(widgets.getString("COULD NOT ADD RESOURCE TO THE METADATA. CHECK THE ERROR LOG FOR DETAILS."), widgets.getString("ERROR ADDING RESOURCE"));
            }
        }
    }

    private boolean checkResourceLocation(final URI locationURI) {
        if (locationURI.isAbsolute()) {
            try {
                // See if creating a file out of the URI does not cause any issues
                final File file = MetadataFormat.getFile(locationURI);
                if (file != null && !file.exists()) {
                    dialogHandler.addMessageDialogToQueue(widgets.getString("WARNING: NO FILE EXISTS AT THE SPECIFIED LOCATION!"), widgets.getString("MANUAL RESOURCE LOCATION"));
                }
                return true;
            } catch (IllegalArgumentException ex) {
                BugCatcherManager.getBugCatcher().logError(ex);
                dialogHandler.addMessageDialogToQueue(widgets.getString("ILLEGAL FILE NAME. CHECK THE ERROR LOG FOR DETAILS."), widgets.getString("ERROR ADDING RESOURCE"));
                return false;
            }
        } else {
            dialogHandler.addMessageDialogToQueue(widgets.getString("LOCATION SHOULD BE AN ABSOLUTE URI"), widgets.getString("ERROR ADDING RESOURCE"));
            return false;
        }
    }

    public void addRemoteCorpus() {
        String addableLocation = (String) JOptionPane.showInputDialog(windowManager.getMainFrame(), widgets.getString("ENTER THE URL"), widgets.getString("ADD LOCATION"), JOptionPane.PLAIN_MESSAGE);
        addRemoteCorpus(addableLocation.trim());
    }

    public void addRemoteCorpus(String addableLocation) {
        if ((addableLocation != null) && (addableLocation.length() > 0)) {
            try {
                treeHelper.addLocationInteractive(ArbilDataNodeService.conformStringToUrl(addableLocation));
            } catch (URISyntaxException ex) {
                dialogHandler.addMessageDialogToQueue(widgets.getString("FAILED TO ADD LOCATION TO REMOTE CORPUS. SEE ERROR LOG FOR DETAILS."), widgets.getString("ERROR"));
                BugCatcherManager.getBugCatcher().logError(ex);
            }
        }
    }

    public void searchSubnodes(ArbilTreePanels treePanels) {
        windowManager.openSearchTable(((ArbilTree) treePanels.localCorpusTree).getAllSelectedNodes(), widgets.getString("SEARCH"));
    }

    public void searchRemoteSubnodes(ArbilTreePanels treePanels) {
        windowManager.openSearchTable(((ArbilTree) treePanels.remoteCorpusTree).getSelectedNodes(), widgets.getString("SEARCH REMOTE CORPUS"));
    }

    public void validateNodes(ArbilDataNode[] selectedTreeNodes) {
        for (ArbilDataNode currentNode : selectedTreeNodes) {
            if (currentNode.getNeedsSaveToDisk(false)
                    && JOptionPane.YES_OPTION == dialogHandler.showDialogBox(
                            widgets.getString("VALIDATION WILL BE AGAINST THE FILE ON DISK. SAVE CHANGES FIRST?"),
                            widgets.getString("TREE_VALIDATION"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE)) {
                currentNode.saveChangesToCache(true);
                currentNode.reloadNode();
            }
            XsdChecker xsdChecker = new XsdChecker();
            ((ArbilWindowManager) windowManager).createWindow(widgets.getString("XSDCHECKER"), xsdChecker);
            xsdChecker.checkXML(currentNode);
            xsdChecker.setDividerLocation(0.5);
        }
    }

    public void addFromFavourite(ArbilDataNode leadSelectedTreeNode, String favouriteUrlString, String displayName) {
        try {
            String favouriteUrlStringDecoded = URLDecoder.decode(favouriteUrlString, "UTF-8");
            ArbilDataNode templateDataNode = dataNodeLoader.getArbilDataNode(null, ArbilDataNodeService.conformStringToUrl(favouriteUrlStringDecoded));
            if (leadSelectedTreeNode != null) {
                new MetadataBuilder().requestAddNode(leadSelectedTreeNode, displayName, templateDataNode);
            } else {
                new MetadataBuilder().requestAddRootNode(templateDataNode, displayName);
            }
//                    treeHelper.getImdiChildNodes(targetNode);
//                    String addedNodeUrlString = treeHelper.addImdiChildNode(targetNode, linorgFavourites.getNodeType(imdiTemplateUrlString), ((JMenuItem) evt.getSource()).getText());
//                    imdiLoader.getImdiObject("", addedNodeUrlString).requestMerge(imdiLoader.getImdiObject("", imdiTemplateUrlString));
//                    loop child nodes and insert them into the new node
//                    ImdiTreeObject templateImdiObject = GuiHelper.imdiLoader.getImdiObject("", imdiTemplateUrlString);
//                    ImdiTreeObject targetImdiObject = GuiHelper.imdiLoader.getImdiObject("", addedNodeUrl);
//
//                    for (Enumeration<ImdiTreeObject> childTemplateEnum = templateImdiObject.getChildEnum(); childTemplateEnum.hasMoreElements();) {
//                        ImdiTreeObject currentTemplateChild = childTemplateEnum.nextElement();
//                        String addedNodeUrl = treeHelper.addImdiChildNode(targetNode, linorgFavourites.getNodeType(currentTemplateChild.getUrlString()), currentTemplateChild.toString());
//                        linorgFavourites.mergeFromFavourite(addedNodeUrl, imdiTemplateUrlString, true);
//                    }
//                    treeHelper.reloadLocalCorpusTree(targetNode);
        } catch (Exception ex) {
            dialogHandler.addMessageDialogToQueue(widgets.getString("FAILED TO ADD FROM FAVOURITES, SEE ERROR LOG FOR DETAILS."), widgets.getString("ERROR"));
            BugCatcherManager.getBugCatcher().logError(ex);
        }
    }

    public void viewSelectedNodes(ArbilTree tree) {
        try {
            ArrayList<ArbilDataNode> filteredNodes = new ArrayList<ArbilDataNode>();
            for (ArbilDataNode currentItem : tree.getSelectedNodes()) {
                if (currentItem.isMetaDataNode() || currentItem.getFields().size() > 0) {
                    filteredNodes.add(currentItem);
                } else {
                    try {
                        windowManager.openUrlWindowOnce(currentItem.toString(), currentItem.getURI().toURL());
                    } catch (MalformedURLException murle) {
                        BugCatcherManager.getBugCatcher().logError(murle);
                    }
                }
            }
            if (filteredNodes.size() > 0) {
                windowManager.openFloatingTableOnce(filteredNodes.toArray(new ArbilDataNode[]{}), null);
            }
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
    }

    public void viewSelectedSubnodes(ArbilTree tree) {
        ArbilDataNode[] selectedNodes = tree.getSelectedNodes();
        ArrayList<ArbilDataNode> filteredNodes = new ArrayList<ArbilDataNode>(selectedNodes.length);
        for (ArbilDataNode dataNode : selectedNodes) {
            if (dataNode.isSession() || dataNode.isMetaDataNode()) {
                filteredNodes.add(dataNode);
            }
        }
        if (!filteredNodes.isEmpty()) {
            windowManager.openFloatingSubnodesWindows(filteredNodes.toArray(new ArbilDataNode[0]));
        }
    }

    public void openSelectedNodesInTable(ArbilTree tree) {
        windowManager.openFloatingTableOnce(tree.getSelectedNodes(), null);
    }

    /**
     * Starts long field editor for selected nodes
     */
    public void startLongFieldEditor(ArbilTree tree) {
        ArbilDataNode[] selectedNodes = tree.getSelectedNodes();
        for (ArbilDataNode node : selectedNodes) {
            if (node.getFields().size() > 0) {
                // Get fields for the node
                List<ArbilField[]> fieldArrays = node.getFieldsSorted();
                // Show the editor
                new ArbilLongFieldEditor().showEditor(fieldArrays.get(0), fieldArrays.get(0)[0].getFieldValue(), 0);
            }
        }
    }

    public void copyBranch(ArbilTree tree) {
        try {
            ImportExportDialog importExportDialog = new ImportExportDialog(tree);
            importExportDialog.copyToCache(tree.getSelectedNodes());
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
    }

    public void addLocalDirectory() {
        File[] selectedFiles = dialogHandler.showDirectorySelectBox(widgets.getString("ADD WORKING DIRECTORY"), true);
        if (selectedFiles != null && selectedFiles.length > 0) {
            for (File currentDirectory : selectedFiles) {
                treeHelper.addLocationInteractive(currentDirectory.toURI());
            }
        }
    }

    public void addBulkResources(ArbilNode targetObject) {
        try {
            final FavouriteSelectBox favouriteSelectBox = new FavouriteSelectBox(targetObject);
            File[] selectedFiles = dialogHandler.showFileSelectBox(widgets.getString("ADD BULK RESOURCES"), false, true, null, MessageDialogHandler.DialogueType.open, favouriteSelectBox);
            if (selectedFiles != null && selectedFiles.length > 0) {
//                        BulkResourcesAdder bulkResourcesAdder = new BulkResourcesAdder(dialogHandler, favouriteSelectBox.getTargetNode(), favouriteSelectBox.getSelectedFavouriteNode(), selectedFiles);
//                        bulkResourcesAdder.setCopyDirectoryStructure(favouriteSelectBox.getCopyDirectoryStructure());
//                        bulkResourcesAdder.setMetadataFilePerResource(favouriteSelectBox.getMetadataFilePerResource());
//                        bulkResourcesAdder.doBulkAdd();
//                        if (favouriteSelectBox.getTargetNode() instanceof ArbilDataNode) {
                new MetadataBuilder().requestAddNodeAndResources(favouriteSelectBox.getTargetNode(), widgets.getString("ADD BULK RESOURCES"), favouriteSelectBox.getSelectedFavouriteNode(), selectedFiles, favouriteSelectBox.getCopyDirectoryStructure(), favouriteSelectBox.getMetadataFilePerResource());
//                        } else {
//                            new MetadataBuilder().requestAddRootNode(favouriteSelectBox.getSelectedFavouriteNode(), ((JMenuItem) evt.getSource()).getText());
//                        }
            }
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
    }

    public void addNodeFromTemplate(ArbilDataNode leadSelectedTreeNode, String nodeType, String displayName) {
        try {
            if (leadSelectedTreeNode != null) {
                new MetadataBuilder().requestAddNode(leadSelectedTreeNode, nodeType, displayName);
            } else {
                // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
                new MetadataBuilder().requestRootAddNode(nodeType, displayName);
            }
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
    }

    public void addSubnode(ArbilDataNode leadSelectedTreeNode, String nodeType, String nodeText) {
        try {
            if (leadSelectedTreeNode != null) {
                if (!leadSelectedTreeNode.getParentDomNode().getNeedsSaveToDisk(false)
                        || dialogHandler.showConfirmDialogBox(
                                MessageFormat.format(widgets.getString("ADDING A NODE WILL SAVE PENDING CHANGES"), leadSelectedTreeNode.getParentDomNode().toString()),
                                widgets.getString("SAVE PENDING CHANGES?"))) {
                    new MetadataBuilder().requestAddNode(leadSelectedTreeNode, nodeType, nodeText);
                }
            } else {
                // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
                new MetadataBuilder().requestRootAddNode(nodeType, nodeText);
            }
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
    }

    public void forceTypeCheck(ArbilDataNode dataNode) {
        mimeHashQueue.forceInQueue(dataNode);
    }

    public void showContextMenu(ArbilTree tree, Point location) {
        new TreeContextMenu(tree, this, treeHelper, dialogHandler, windowManager, sessionStorage, versionManager).show(location.x, location.y);
    }
}
