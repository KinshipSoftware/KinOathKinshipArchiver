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
package nl.mpi.arbil.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.ui.ArbilTrackingTree;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.flap.model.DataNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilTreeHelper Created on :
 *
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 */
public abstract class AbstractTreeHelper implements TreeHelper {

    public static final String SHOW_HIDDEN_FILES_IN_TREE_OPTION = "showHiddenFilesInTree";
    public static final String GROUP_FAVOURITES_BY_TYPE_OPTION = "groupFavouritesByType";
    private final static Logger logger = LoggerFactory.getLogger(AbstractTreeHelper.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private DefaultTreeModel localCorpusTreeModel;
    private DefaultTreeModel remoteCorpusTreeModel;
    private DefaultTreeModel localDirectoryTreeModel;
    private DefaultTreeModel favouritesTreeModel;
    private ArbilDataNode[] remoteCorpusNodes = new ArbilDataNode[]{};
    private ArbilDataNode[] localCorpusNodes = new ArbilDataNode[]{};
    private ArbilDataNode[] localFileNodes = new ArbilDataNode[]{};
    private ArbilDataNode[] favouriteNodes = new ArbilDataNode[]{};
    private boolean showHiddenFilesInTree = false;
    private MessageDialogHandler messageDialogHandler;
    private DataNodeLoader dataNodeLoader;
    private boolean groupFavouritesByType = true;
    /**
     * ArbilRootNode for local corpus tree
     */
    protected final ArbilRootNode localCorpusRootNodeObject;
    /**
     * ArbilRootNode for remote corpus tree
     */
    protected final ArbilRootNode remoteCorpusRootNodeObject;
    /**
     * ArbilRootNode for working directories tree ('files')
     */
    protected final ArbilRootNode localDirectoryRootNodeObject;
    /**
     * ArbilRootNode for favourites tree
     */
    protected final ArbilRootNode favouritesRootNodeObject;

    public AbstractTreeHelper(MessageDialogHandler messageDialogHandler) {
        this.messageDialogHandler = messageDialogHandler;
        this.localCorpusRootNodeObject = new ArbilRootNode(URI.create("LOCALCORPUS"), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("LOCAL CORPUS"), ArbilIcons.getSingleInstance().directoryIcon, true) {
            public ArbilDataNode[] getChildArray() {
                return getLocalCorpusNodes();
            }

            @Override
            public DataNodeType getType() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setType(DataNodeType dataNodeType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        remoteCorpusRootNodeObject = new ArbilRootNode(URI.create("REMOTECORPUS"), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("REMOTE CORPUS"), ArbilIcons.getSingleInstance().serverIcon, false) {
            public ArbilDataNode[] getChildArray() {
                return getRemoteCorpusNodes();
            }

            @Override
            public DataNodeType getType() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setType(DataNodeType dataNodeType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        localDirectoryRootNodeObject = new ArbilRootNode(URI.create("WORKINGDIRECTORIES"), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("WORKING DIRECTORIES"), ArbilIcons.getSingleInstance().computerIcon, true) {
            public ArbilDataNode[] getChildArray() {
                return getLocalFileNodes();
            }

            @Override
            public DataNodeType getType() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setType(DataNodeType dataNodeType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        favouritesRootNodeObject = new ArbilRootNode(URI.create("FAVOURITES"), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("FAVOURITES"), ArbilIcons.getSingleInstance().favouriteIcon, true) {
            Map<String, ContainerNode> containerNodeMap = new HashMap<String, ContainerNode>();

            public ArbilNode[] getChildArray() {
                if (groupFavouritesByType) {
                    containerNodeMap = groupTreeNodesByType(getFavouriteNodes(), containerNodeMap);
                    return containerNodeMap.values().toArray(new ArbilNode[]{});
                } else {
                    return getFavouriteNodes();
                }
            }

            @Override
            public DataNodeType getType() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setType(DataNodeType dataNodeType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    protected final void initTrees() {
        initTreeModels();
    }

    protected void initTreeModels() {
        // Create tree models using the ArbilRootNodes as user objects for the root tree nodes.
        //
        // Second parameter of DefaultTreeModel constructor: 
        //	asksAllowsChildren - a boolean, true if each node is asked to see if it can have children

        localCorpusTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(localCorpusRootNodeObject), true);
        remoteCorpusTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(remoteCorpusRootNodeObject), true);
        localDirectoryTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(localDirectoryRootNodeObject), true);
        favouritesTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(favouritesRootNodeObject), true);
    }

    @Override
    public int addDefaultCorpusLocations() {
        try {
            addLocations(getClass().getResourceAsStream("/nl/mpi/arbil/defaults/imdiLocations"));
            return remoteCorpusNodes.length;
        } catch (IOException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
            return 0;
        }
    }

    public int addDefaultCorpusLocationsOld() {
        HashSet<ArbilDataNode> remoteCorpusNodesSet = new HashSet<ArbilDataNode>();
        remoteCorpusNodesSet.addAll(Arrays.asList(remoteCorpusNodes));
        for (String currentUrlString : new String[]{
            "http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi",
            "http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi",
            "http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/sign_language.imdi"
        }) {
            try {
                remoteCorpusNodesSet.add(dataNodeLoader.getArbilDataNode(null, new URI(currentUrlString)));
            } catch (URISyntaxException ex) {
                BugCatcherManager.getBugCatcher().logError(ex);
            }
        }
        remoteCorpusNodes = remoteCorpusNodesSet.toArray(new ArbilDataNode[]{});
        return remoteCorpusNodesSet.size();
    }

    @Override
    public void saveLocations(ArbilDataNode[] nodesToAdd, ArbilDataNode[] nodesToRemove) {
        try {
            final HashSet<String> locationsSet = new HashSet<String>();
            for (ArbilDataNode[] currentTreeArray : new ArbilDataNode[][]{remoteCorpusNodes, localCorpusNodes, localFileNodes, favouriteNodes}) {
                for (ArbilDataNode currentLocation : currentTreeArray) {
                    locationsSet.add(currentLocation.getUrlString());
                }
            }
            if (nodesToAdd != null) {
                for (ArbilDataNode currentAddable : nodesToAdd) {
                    if (currentAddable != null) {
                        logger.debug("Adding location {} from locations list", currentAddable.getUrlString());
                        locationsSet.add(currentAddable.getUrlString());
                    }
                }
            }
            if (nodesToRemove != null) {
                for (ArbilDataNode currentRemoveable : nodesToRemove) {
                    logger.debug("Removing location {} from locations list", currentRemoveable.getUrlString());
                    locationsSet.remove(currentRemoveable.getUrlString());
                }
            }
            List<String> locationsList = new ArrayList<String>(); // this vector is kept for backwards compatability
            for (String currentLocation : locationsSet) {
                locationsList.add(URLDecoder.decode(currentLocation, "UTF-8"));
            }
            //LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
            getSessionStorage().saveStringArray("locationsList", locationsList.toArray(new String[]{}));
            logger.debug("saved locationsList");
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
//            logger.debug("save locationsList exception: " + ex.getMessage());
        }
    }

    @Override
    public final void loadLocationsList() {
        logger.debug("loading locationsList");
        String[] locationsArray = null;
        try {
            locationsArray = getSessionStorage().loadStringArray("locationsList");
        } catch (IOException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
            messageDialogHandler.addMessageDialogToQueue(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("COULD NOT FIND OR LOAD LOCATIONS. ADDING DEFAULT LOCATIONS."), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("ERROR"));
        }
        if (locationsArray != null) {
            ArrayList<ArbilDataNode> remoteCorpusNodesList = new ArrayList<ArbilDataNode>();
            ArrayList<ArbilDataNode> localCorpusNodesList = new ArrayList<ArbilDataNode>();
            ArrayList<ArbilDataNode> localFileNodesList = new ArrayList<ArbilDataNode>();
            ArrayList<ArbilDataNode> favouriteNodesList = new ArrayList<ArbilDataNode>();

            int failedLoads = 0;
            // this also removes all locations and replaces them with normalised paths
            for (String currentLocationString : locationsArray) {
                URI currentLocation = null;
                try {
                    currentLocation = ArbilDataNodeService.conformStringToUrl(currentLocationString);
                } catch (URISyntaxException ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
                if (currentLocation == null) {
                    BugCatcherManager.getBugCatcher().logError("Could conform string to url: " + currentLocationString, null);
                    failedLoads++;
                } else {
                    try {
                        ArbilDataNode currentTreeObject = dataNodeLoader.getArbilDataNode(null, currentLocation);
                        if (currentTreeObject.isLocal()) {
                            if (currentTreeObject.isFavorite()) {
                                favouriteNodesList.add(currentTreeObject);
                            } else if (getSessionStorage().pathIsInsideCache(currentTreeObject.getFile())) {
                                if (currentTreeObject.isMetaDataNode() && !currentTreeObject.isChildNode()) {
                                    localCorpusNodesList.add(currentTreeObject);
                                }
                            } else {
                                localFileNodesList.add(currentTreeObject);
                            }
                        } else {
                            remoteCorpusNodesList.add(currentTreeObject);
                        }
                    } catch (Exception ex) {
                        BugCatcherManager.getBugCatcher().logError("Failure in trying to load " + currentLocationString, ex);
                        failedLoads++;
                    }
                }
            }

            if (failedLoads > 0) {
                messageDialogHandler.addMessageDialogToQueue(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("FAILED TO LOAD {0} LOCATIONS. SEE ERROR LOG FOR DETAILS."), new Object[]{failedLoads}), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("WARNING"));
            }

            remoteCorpusNodes = remoteCorpusNodesList.toArray(new ArbilDataNode[]{});
            localCorpusNodes = localCorpusNodesList.toArray(new ArbilDataNode[]{});
            localFileNodes = localFileNodesList.toArray(new ArbilDataNode[]{});
            favouriteNodes = favouriteNodesList.toArray(new ArbilDataNode[]{});
        }
        showHiddenFilesInTree = getSessionStorage().loadBoolean(SHOW_HIDDEN_FILES_IN_TREE_OPTION, showHiddenFilesInTree);
        groupFavouritesByType = getSessionStorage().loadBoolean(GROUP_FAVOURITES_BY_TYPE_OPTION, groupFavouritesByType);
    }

    @Override
    public void setShowHiddenFilesInTree(boolean showState) {
        showHiddenFilesInTree = showState;
        reloadNodesInTree(localDirectoryTreeModel);
        try {
            getSessionStorage().saveBoolean(SHOW_HIDDEN_FILES_IN_TREE_OPTION, showHiddenFilesInTree);
        } catch (Exception ex) {
            logger.warn("save showHiddenFilesInTree failed", ex);
        }
    }

    @Override
    public void setGroupFavouritesByType(boolean groupFavouritesByType) {
        this.groupFavouritesByType = groupFavouritesByType;
        reloadNodesInTree(favouritesTreeModel);
        try {
            getSessionStorage().saveBoolean(GROUP_FAVOURITES_BY_TYPE_OPTION, groupFavouritesByType);
        } catch (Exception ex) {
            logger.warn("save groupFavouritesByType failed", ex);
        }
    }

    public void addLocations(List<URI> locations) {
        ArbilDataNode[] addedNodes = new ArbilDataNode[locations.size()];
        for (int i = 0; i < locations.size(); i++) {
            URI addedLocation = locations.get(i);
            logger.debug("addLocation: " + addedLocation.toString());
            // make sure the added location url matches that of the imdi node format
            addedNodes[i] = dataNodeLoader.getArbilDataNode(null, addedLocation);
        }

        saveLocations(addedNodes, null);
        loadLocationsList();
    }

    public void addLocations(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<URI> locationsList = new LinkedList<URI>();
        String location = reader.readLine();
        while (location != null) {
            try {
                URI uri = new URI(location);
                locationsList.add(uri);
            } catch (URISyntaxException ex) {
                BugCatcherManager.getBugCatcher().logError(ex);
            }
            location = reader.readLine();
        }
        addLocations(locationsList);
    }

    public void clearRemoteLocations() {
        for (ArbilDataNode removeNode : remoteCorpusNodes) {
            removeLocation(removeNode.getURI());
        }
    }

    @Override
    public boolean addLocationInteractive(URI addableLocation) {
        return addLocation(addableLocation);
    }

    @Override
    public boolean addLocation(URI addedLocation) {
        logger.debug("addLocation: " + addedLocation.toString());
        // make sure the added location url matches that of the imdi node format
        final ArbilDataNode addedLocationObject = dataNodeLoader.getArbilDataNode(null, addedLocation);
        //TODO: Synchronize this
        if (addedLocationObject != null) {
            addedLocationObject.reloadNode();
            saveLocations(new ArbilDataNode[]{addedLocationObject}, null);
            loadLocationsList();
            return true;
        } else {
            logger.warn("Could not retrieve data node for location added to tree {}", addedLocation);
            return false;
        }
    }

    @Override
    public void removeLocation(ArbilDataNode removeObject) {
        if (removeObject != null) {
            //TODO: Synchronize this
            saveLocations(null, new ArbilDataNode[]{removeObject});
            removeObject.removeFromAllContainers();
            loadLocationsList();
        }
    }

    @Override
    public void removeLocation(URI removeLocation) {
        logger.debug("removeLocation: " + removeLocation);
        removeLocation(dataNodeLoader.getArbilDataNode(null, removeLocation));
    }

    private void reloadNodesInTree(DefaultTreeModel treeModel) {
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        reloadNodesInTree(rootNode);
        applyRootLocations();
    }

    private void reloadNodesInTree(DefaultMutableTreeNode parentTreeNode) {
        // this will reload all nodes in a tree but not create any new child nodes
        for (Enumeration<TreeNode> childNodesEnum = parentTreeNode.children(); childNodesEnum.hasMoreElements();) {
            reloadNodesInTree((DefaultMutableTreeNode)childNodesEnum.nextElement());
        }
        if (parentTreeNode.getUserObject() instanceof ArbilDataNode) {
            if (((ArbilDataNode) parentTreeNode.getUserObject()).isDataLoaded()) {
                ((ArbilDataNode) parentTreeNode.getUserObject()).reloadNodeShallowly();
            }
        }
    }

    @Override
    public boolean locationsHaveBeenAdded() {
        return localCorpusNodes.length > 0;
    }

    @Override
    public abstract void applyRootLocations();

    @Override
    public abstract void deleteNodes(Object sourceObject);

    protected void deleteNodesFromParent(Collection<ArbilDataNode> nodesToDeleteFromParent) {
        final Map<ArbilDataNode, Collection<ArbilDataNode>> nodesToDelete = determineNodesToDelete(nodesToDeleteFromParent);
        // Ask confirmation for deletion for each set of child nodes (grouped by parent node)
        askDeleteNodes(nodesToDelete);
        // If nodes were selected that do not have a parent, give notification
        if (nodesToDelete.containsKey(null)) {
            final int nodeCount = nodesToDelete.get(null).size();
            if (nodeCount > 1) {
                messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(widgets.getString("%D NODES COULD NOT BE DELETED BECAUSE THEY HAVE NO PARENT"), nodeCount), widgets.getString("DELETE FROM PARENT"));
            } else {
                messageDialogHandler.addMessageDialogToQueue(widgets.getString("COULD NOT DELETE NODE BECAUSE IT HAS NO PARENT"), widgets.getString("DELETE FROM PARENT"));
            }
        }
    }

    private Map<ArbilDataNode, Collection<ArbilDataNode>> determineNodesToDelete(final Collection<ArbilDataNode> nodesToDeleteFromParent) {
        final Map<ArbilDataNode, Collection<ArbilDataNode>> deletionMap = new HashMap<ArbilDataNode, Collection<ArbilDataNode>>();
        for (ArbilDataNode selectedNode : nodesToDeleteFromParent) {
            final ArbilDataNode parentNode = selectedNode.getParentNode();
            // Look for existing collection for parent node
            Collection<ArbilDataNode> children = deletionMap.get(parentNode);
            if (children == null) {
                // First encounter of parent node, make list to store children
                children = new ArrayList<ArbilDataNode>();
                deletionMap.put(parentNode, children);
            }
            // Add child to parent's list 
            children.add(selectedNode);
        }
        return deletionMap;
    }

    private void askDeleteNodes(final Map<ArbilDataNode, Collection<ArbilDataNode>> nodesToDelete) {
        MessageDialogHandler.DialogBoxResult dialogResult = null;
        for (Entry<ArbilDataNode, Collection<ArbilDataNode>> entry : nodesToDelete.entrySet()) {
            final ArbilDataNode parentNode = entry.getKey();
            if (parentNode != null) {
                final Collection<ArbilDataNode> children = entry.getValue();
                if (dialogResult == null || !dialogResult.isRememberChoice()) {
                    dialogResult = messageDialogHandler.showDialogBoxRememberChoice(getNodeDeleteMessage(parentNode, children), widgets.getString("DELETE FROM PARENT"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                }
                if (dialogResult.getResult() == JOptionPane.OK_OPTION) {
                    deleteChildNodes(parentNode, children);
                } else if (dialogResult.getResult() == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
        }
    }

    private String getNodeDeleteMessage(ArbilDataNode parentNode, Collection<ArbilDataNode> children) {
        if (children.size() == 1) {
            return MessageFormat.format(widgets.getString("DELETE THE NODE '%S' FROM ITS PARENT '%S'?"), children.iterator().next(), parentNode.toString());
        } else {
            return MessageFormat.format(widgets.getString("DELETE %D NODES FROM THEIR PARENT '%S'?"), children.size(), parentNode.toString());
        }
    }

    public void deleteChildNodes(ArbilDataNode parent, Collection<ArbilDataNode> children) {
        Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList = new HashMap<ArbilDataNode, List<ArbilDataNode>>();
        Map<ArbilDataNode, List<String>> childNodeDeleteList = new HashMap<ArbilDataNode, List<String>>();
        Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList = new HashMap<ArbilDataNode, List<ArbilDataNode>>();
        for (ArbilDataNode child : children) {
            determineDeleteFromParent(child, parent, childNodeDeleteList, dataNodesDeleteList, cmdiLinksDeleteList);
        }
        // delete child nodes
        deleteNodesByChidXmlIdLink(childNodeDeleteList);
        // delete parent nodes
        deleteNodesByCorpusLink(dataNodesDeleteList);
        // delete cmdi links
        deleteCmdiLinks(cmdiLinksDeleteList);
    }

    protected void determineNodesToDelete(TreePath[] nodePaths, Map<ArbilDataNode, List<String>> childNodeDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList) {
        final Vector<ArbilDataNode> dataNodesToRemove = new Vector<ArbilDataNode>();
        for (TreePath currentNodePath : nodePaths) {
            if (currentNodePath != null) {
                final DefaultMutableTreeNode selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
                final Object selectedNode = selectedTreeNode.getUserObject();
                if (selectedNode instanceof ArbilDataNode) {
                    final boolean rootLevelNode = currentNodePath.getPath().length == 2;
                    final DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) selectedTreeNode.getParent();
                    determineNodesToDelete((ArbilDataNode) selectedNode, rootLevelNode, parentTreeNode, childNodeDeleteList, dataNodesDeleteList, cmdiLinksDeleteList, dataNodesToRemove);
                } else {
                    logger.warn("Cannot delete selected node {}, not an ArbilDataNode", selectedNode);
                }
            }
        }
    }

    private void determineNodesToDelete(ArbilDataNode selectedNode, boolean rootLevelNode, DefaultMutableTreeNode parentTreeNode, Map<ArbilDataNode, List<String>> childNodeDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList, Vector<ArbilDataNode> dataNodesToRemove) {
        logger.debug("trying to delete: {}", selectedNode);
        if (rootLevelNode || selectedNode.isFavorite()) {
            // In locations list (i.e. child of root node)
            logger.debug("removing by location: {}", selectedNode);
            removeLocation(selectedNode);
            applyRootLocations();
        } else {
            logger.debug("deleting from parent");
            if (parentTreeNode != null) {
                logger.debug("found parent to remove from");
                final Object parentTreeNodeObject = parentTreeNode.getUserObject();
                if (parentTreeNodeObject instanceof ArbilDataNode) {
                    ArbilDataNode parentDataNode = (ArbilDataNode) parentTreeNodeObject;
                    determineDeleteFromParent(selectedNode, parentDataNode, childNodeDeleteList, dataNodesDeleteList, cmdiLinksDeleteList);
                } else {
                    logger.warn("Cannot delete from selected parent node {}, not an ArbilDataNode", parentTreeNodeObject);
                }
            }
        }
        // todo: this fixes some of the nodes left after a delete EXCEPT; for example, the "actors" node when all the actors are deleted
        //                        ArbilTreeHelper.getSingleInstance().removeAndDetatchDescendantNodes(selectedTreeNode);
        // make a list of all child nodes so that they can be removed from any tables etc
        dataNodesToRemove.add((ArbilDataNode) selectedNode);
        ((ArbilDataNode) selectedNode).getAllChildren(dataNodesToRemove);
    }

    private void determineDeleteFromParent(ArbilDataNode childDataNode, ArbilDataNode parentDataNode, Map<ArbilDataNode, List<String>> childNodeDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList) {
        if (childDataNode.isChildNode()) {
            // there is a risk of the later deleted nodes being outof sync with the xml, so we add them all to a list and delete all at once before the node is reloaded
            if (!childNodeDeleteList.containsKey(childDataNode.getParentDomNode())) {
                childNodeDeleteList.put(childDataNode.getParentDomNode(), new Vector());
            }
            if (childDataNode.isEmptyMetaNode()) {
                for (ArbilDataNode metaChildNode : childDataNode.getChildArray()) {
                    childNodeDeleteList.get(childDataNode.getParentDomNode()).add(metaChildNode.getURIFragment());
                }
            }
            childNodeDeleteList.get(childDataNode.getParentDomNode()).add(childDataNode.getURIFragment());
            childDataNode.removeFromAllContainers();
        } else if (parentDataNode.isCmdiMetaDataNode()) {
            // CMDI link
            if (!cmdiLinksDeleteList.containsKey(parentDataNode)) {
                cmdiLinksDeleteList.put(parentDataNode, new ArrayList<ArbilDataNode>());
            }
            cmdiLinksDeleteList.get(parentDataNode).add(childDataNode);
        } else {
            // Not a child node or CMDI resource, ergo corpus child
            // Add the parent and the child node to the deletelist
            if (!dataNodesDeleteList.containsKey(parentDataNode)) {
                dataNodesDeleteList.put(parentDataNode, new Vector());
            }
            dataNodesDeleteList.get(parentDataNode).add(childDataNode);
        }
        // remove the deleted node from the favourites list if it is an imdichild node
        //                            if (userObject instanceof ImdiTreeObject) {
        //                                if (((ImdiTreeObject) userObject).isImdiChild()){
        //                                LinorgTemplates.getSingleInstance().removeFromFavourites(((ImdiTreeObject) userObject).getUrlString());
        //                                }
        //                            }
    }

    protected void deleteNodesByChidXmlIdLink(Map<ArbilDataNode, List<String>> childNodeDeleteList) {
        for (Entry<ArbilDataNode, List<String>> deleteEntry : childNodeDeleteList.entrySet()) {
            ArbilDataNode currentParent = deleteEntry.getKey();
            logger.debug("deleting by child xml id link");
            // TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
            //currentParent.deleteFromDomViaId(((Vector<String>) imdiChildNodeDeleteList.get(currentParent)).toArray(new String[]{}));
            ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
            boolean result = componentBuilder.removeChildNodes(currentParent, deleteEntry.getValue().toArray(new String[]{}));
            if (result) {
                // Invalidate all thumbnails for the parent node. If MediaFiles are deleted, this prevents the thumbnails to get 'shifted'
                // i.e. stick on the wrong note. This could perhaps be done a bit more sophisticated, it is not actually needed
                // unless MediaFiles are deleted
                currentParent.invalidateThumbnails();
                currentParent.reloadNode();
            } else {
                messageDialogHandler.addMessageDialogToQueue(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("ERROR DELETING NODE, CHECK THE LOG FILE VIA THE HELP MENU FOR MORE INFORMATION."), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("DELETE NODE"));
            }
        }
    }

    protected void deleteNodesByCorpusLink(Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList) {
        for (Entry<ArbilDataNode, List<ArbilDataNode>> deleteEntry : dataNodesDeleteList.entrySet()) {
            logger.debug("deleting by corpus link");
            deleteEntry.getKey().deleteCorpusLink(deleteEntry.getValue().toArray(new ArbilDataNode[]{}));
        }
    }

    protected void deleteCmdiLinks(Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinks) {
        ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
        for (Entry<ArbilDataNode, List<ArbilDataNode>> deleteEntry : cmdiLinks.entrySet()) {
            ArrayList<String> references = new ArrayList<String>(deleteEntry.getValue().size());
            for (ArbilDataNode node : deleteEntry.getValue()) {
                references.add(node.getUrlString());
            }
            if (componentBuilder.removeResourceProxyReferences(deleteEntry.getKey(), references)) {
                deleteEntry.getKey().reloadNode();
            } else {
                messageDialogHandler.addMessageDialogToQueue(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("ERROR DELETING NODE, CHECK THE LOG FILE VIA THE HELP MENU FOR MORE INFORMATION."), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("DELETE NODE"));
            }
        }
    }

    @Override
    public void jumpToSelectionInTree(boolean silent, ArbilDataNode cellDataNode) {
        // TODO: Now does not work for nodes that have not been exposed in the tree. This is because the tree
        // is not registered as container for those nodes. This needs to be detected and worked around, or if that is too messy
        // the jump to tree item should be disabled for those nodes.

        for (ArbilDataNodeContainer container : cellDataNode.getRegisteredContainers()) {
            if (container instanceof ArbilTrackingTree) {
                boolean found = false;
                if (cellDataNode.isChildNode()) {
                    // Try from parent first
                    found = ((ArbilTrackingTree) container).jumpToNode(cellDataNode.getParentDomNode(), cellDataNode);
                }
                if (!found) {
                    // Try from tree root
                    ((ArbilTrackingTree) container).jumpToNode(null, cellDataNode);
                }
            }
        }
    }

    @Override
    public boolean isInFavouritesNodes(ArbilDataNode dataNode) {
        return Arrays.asList(favouriteNodes).contains(dataNode);
    }

    /**
     * @return the localCorpusTreeModel
     */
    @Override
    public DefaultTreeModel getLocalCorpusTreeModel() {
        return localCorpusTreeModel;
    }

    /**
     * @return the remoteCorpusTreeModel
     */
    @Override
    public DefaultTreeModel getRemoteCorpusTreeModel() {
        return remoteCorpusTreeModel;
    }

    /**
     * @return the localDirectoryTreeModel
     */
    @Override
    public DefaultTreeModel getLocalDirectoryTreeModel() {
        return localDirectoryTreeModel;
    }

    /**
     * @return the favouritesTreeModel
     */
    @Override
    public DefaultTreeModel getFavouritesTreeModel() {
        return favouritesTreeModel;
    }

    /**
     * @return the remoteCorpusNodes
     */
    @Override
    public ArbilDataNode[] getRemoteCorpusNodes() {
        return remoteCorpusNodes;
    }

    /**
     * @return the localCorpusNodes
     */
    @Override
    public ArbilDataNode[] getLocalCorpusNodes() {
        return localCorpusNodes;
    }

    /**
     * @return the localFileNodes
     */
    @Override
    public ArbilDataNode[] getLocalFileNodes() {
        return localFileNodes;
    }

    /**
     * @return the favouriteNodes
     */
    @Override
    public ArbilDataNode[] getFavouriteNodes() {
        return favouriteNodes;
    }

    protected Map<String, ContainerNode> groupTreeNodesByType(ArbilDataNode[] favouriteNodes, Map<String, ContainerNode> containerNodeMap) {
        final Map<String, HashSet<ArbilDataNode>> metaNodeMap = new HashMap<String, HashSet<ArbilDataNode>>();
        for (ArbilDataNode arbilDataNode : favouriteNodes) {
            String containerNodeLabel = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("OTHER");
            if (arbilDataNode.isChildNode()) {
                final String urlString = arbilDataNode.getUrlString();
                containerNodeLabel = urlString.substring(urlString.lastIndexOf(".") + 1);
                containerNodeLabel = containerNodeLabel.replaceFirst("\\([0-9]*\\)", "");
                if (arbilDataNode.isCmdiMetaDataNode()) {
                    final ArbilTemplate nodeTemplate = arbilDataNode.getParentDomNode().nodeTemplate;
                    if (nodeTemplate != null) {
                        containerNodeLabel = containerNodeLabel + " (" + nodeTemplate.getTemplateName() + ")";
                    } else {
                        containerNodeLabel = containerNodeLabel + java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString(" (LOADING)");
                    }
                }
            } else if (arbilDataNode.isSession()) {
                containerNodeLabel = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("SESSION");
            } else if (arbilDataNode.isCatalogue()) {
                containerNodeLabel = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("CATALOGUE");
            } else if (arbilDataNode.isCmdiMetaDataNode()) {
                if (arbilDataNode.nodeTemplate == null) {
                    containerNodeLabel = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("CLARIN INSTANCE");
                } else {
                    containerNodeLabel = arbilDataNode.nodeTemplate.getTemplateName();
                }
            }
            if (!metaNodeMap.containsKey(containerNodeLabel)) {
                metaNodeMap.put(containerNodeLabel, new HashSet<ArbilDataNode>());
            }
            metaNodeMap.get(containerNodeLabel).add(arbilDataNode);
        }
        final Map<String, ContainerNode> containerNodeMapUpdated = new HashMap<String, ContainerNode>();
        for (Map.Entry<String, HashSet<ArbilDataNode>> filteredNodeEntry : metaNodeMap.entrySet()) {
            if (containerNodeMapUpdated.containsKey(filteredNodeEntry.getKey())) {
                containerNodeMapUpdated.get(filteredNodeEntry.getKey()).setChildNodes(filteredNodeEntry.getValue().toArray(new ArbilDataNode[]{}));
            } else if (containerNodeMap.containsKey(filteredNodeEntry.getKey())) {
                // use the entry from the 
                final ContainerNode foundEntry = containerNodeMap.get(filteredNodeEntry.getKey());
                foundEntry.setChildNodes(filteredNodeEntry.getValue().toArray(new ArbilDataNode[]{}));
                containerNodeMapUpdated.put(filteredNodeEntry.getKey(), foundEntry);
            } else {
                URI containerNodeUri = URI.create("ContainerNode");
                ContainerNode containerNode = new ContainerNode(containerNodeUri, filteredNodeEntry.getKey(), null, filteredNodeEntry.getValue().toArray(new ArbilDataNode[]{}));
                containerNodeMapUpdated.put(filteredNodeEntry.getKey(), containerNode);
            }
        }
        return containerNodeMapUpdated;
    }

    /**
     * @return the showHiddenFilesInTree
     */
    @Override
    public boolean isShowHiddenFilesInTree() {
        return showHiddenFilesInTree;
    }

    @Override
    public boolean isGroupFavouritesByType() {
        return groupFavouritesByType;
    }

    protected abstract SessionStorage getSessionStorage();

    /**
     * @return the messageDialogHandler
     */
    protected MessageDialogHandler getMessageDialogHandler() {
        return messageDialogHandler;
    }

    public void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
        dataNodeLoader = dataNodeLoaderInstance;
    }

    /**
     * @return the dataNodeLoader
     */
    protected DataNodeLoader getDataNodeLoader() {
        return dataNodeLoader;
    }
}
