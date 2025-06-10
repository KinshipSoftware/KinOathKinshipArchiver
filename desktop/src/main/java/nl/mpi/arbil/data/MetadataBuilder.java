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
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.favourites.ArbilFavourites;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Document : MetadataBuilder
 * Created on : Jun 9, 2010, 4:03:07 PM
 * Author : Peter Withers
 */
public class MetadataBuilder {

    private final static Logger logger = LoggerFactory.getLogger(MetadataBuilder.class);
    private static final ResourceBundle services = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }
    private ArbilComponentBuilder arbilComponentBuilder = new ArbilComponentBuilder();

    /**
     * Requests to add a new node of given type to root
     *
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestRootAddNode(String nodeType, String nodeTypeDisplayName) {
	ArbilDataNode arbilDataNode = dataNodeLoader.createNewDataNode(sessionStorage.getNewArbilFileName(sessionStorage.getSaveLocation(""), nodeType));
	requestAddNode(arbilDataNode, nodeType, nodeTypeDisplayName);
    }

    /**
     * Requests to add a node on basis of a given existing node to the local corpus
     *
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    public void requestAddRootNode(final ArbilDataNode addableNode, final String nodeTypeDisplayNameLocal) {
	// Start new thread to add the node to its destination
	creatAddAddableNodeThread(null, new String[]{nodeTypeDisplayNameLocal}, new ArbilDataNode[]{addableNode}).start();
    }

    /**
     * Checks whether the destinationNode in its current state supports adding a node of the specified type
     *
     * @param destinationNode Proposed destination node
     * @param nodeType Full type name of the node to add
     * @return Whether the node can be added
     */
    public boolean canAddChildNode(final ArbilDataNode destinationNode, final String nodeType) {
	final String targetXmlPath = destinationNode.getURIFragment();

	synchronized (destinationNode.getParentDomLockObject()) {
	    // Ignore CMDI metadata
	    if (nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode()) {
		// Check whether clarin sub node can be added
		return arbilComponentBuilder.canInsertChildComponent(destinationNode, targetXmlPath, nodeType);
	    } else {                // Ignore non-child nodes
		if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType)) {
		    // Do a quick pre-check whether there is a finite number of occurrences
		    if (destinationNode.getNodeTemplate().getMaxOccursForTemplate(nodeType) >= 0) {
			logger.debug("adding to current node");
			try {
			    Document nodDom = ArbilComponentBuilder.getDocument(destinationNode.getURI());
			    if (nodDom == null) {
				messageDialogHandler.addMessageDialogToQueue(services.getString("THE METADATA FILE COULD NOT BE OPENED"), services.getString("ADD NODE"));
			    } else {
				return MetadataReader.getSingleInstance().canInsertFromTemplate(destinationNode.getNodeTemplate(), nodeType, targetXmlPath, nodDom);
			    }
			} catch (ParserConfigurationException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (SAXException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (IOException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (ArbilMetadataException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			}
		    }
		}
		// Other cases not handled
		return true;
	    }
	}
    }

    /**
     * Requests to add a new node of given type to given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestAddNode(final ArbilDataNode destinationNode, final String nodeType, final String nodeTypeDisplayName) {
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	new Thread("requestAddNode") {
	    @Override
	    public void run() {
		ArbilNode addedNode = null;
		destinationNode.updateLoadingState(1);
		try {
		    synchronized (destinationNode.getParentDomLockObject()) {
			try {
			    logger.debug("requestAddNode: {} : {}", nodeType, nodeTypeDisplayName);
			    addedNode = processAddNodes(destinationNode, nodeType, destinationNode.getURIFragment(), nodeTypeDisplayName, null, null, null);

			    // CODE REMOVED: previously, imdiLoaders was requested to reload destinationNode
			} catch (ArbilMetadataException exception) {
			    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), services.getString("INSERT NODE ERROR"));
			}
		    }
		} finally {
		    destinationNode.updateLoadingState(-1);
		}
		if (addedNode != null) {
		    destinationNode.triggerNodeAdded(addedNode);
		}
	    }

	    private ArbilDataNode processAddNodes(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) throws ArbilMetadataException {

		// make title for imdi table
		final String newTableTitleString;
		if (destinationNode.isMetaDataNode() && destinationNode.getFile().exists()) {
		    newTableTitleString = MessageFormat.format(services.getString("NEW {0} IN {1}"), nodeTypeDisplayName, destinationNode.toString());
		} else {
		    newTableTitleString = MessageFormat.format(services.getString("NEW {0}"), nodeTypeDisplayName);
		}

		logger.debug("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
		// Create child node
		URI addedNodeUri = addChildNode(destinationNode, nodeType, targetXmlPath, resourceUri, mimeType);
		// Get the newly created data node
		ArbilDataNode addedArbilNode = dataNodeLoader.getArbilDataNodeWithoutLoading(addedNodeUri);
		refreshNodes(destinationNode, addedArbilNode);
		windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
		return addedArbilNode;
	    }

	    private void refreshNodes(ArbilDataNode destinationNode, ArbilDataNode addedArbilNode) {
		if (addedArbilNode != null) {
		    addedArbilNode.getParentDomNode().updateLoadingState(+1);
		    try {
			addedArbilNode.scrollToRequested = true;
			if (destinationNode.getFile().exists()) { // if this is a root node request then the target node will not have a file to reload
			    destinationNode.getParentDomNode().loadArbilDom();
			}
			if (destinationNode.getParentDomNode() != addedArbilNode.getParentDomNode()) {
			    addedArbilNode.getParentDomNode().loadArbilDom();
			}
		    } finally {
			addedArbilNode.getParentDomNode().updateLoadingState(-1);
		    }
		}
	    }
	}.start();
    }

    /**
     * Requests to add a node on basis of a given existing node to the given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    public void requestAddNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) {
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	// Start new thread to add the node to its destination
	requestAddNodes(destinationNode, new String[]{nodeTypeDisplayNameLocal}, new ArbilDataNode[]{addableNode});
    }

    /**
     * Requests to add a metadata node for each resource file or one metadata file for all the resources by making a copy the given existing
     * node to the given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeTypeDisplayNameLocal the title of the table that will be opened with the results when complete
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     * @param resourceFiles An array of resource files or directories
     * @param copyDirectoryStructure if true then the directory structure should be replicated by generating a corpus structure
     * @param metadataFilePerResource if true then each resource will be given a separate metadata file, otherwise only one metadata file
     * will be created per directory
     */
    public void requestAddNodeAndResources(final ArbilNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode, final File[] resourceFiles, final boolean copyDirectoryStructure, final boolean metadataFilePerResource) {
	if (destinationNode instanceof ArbilDataNode && ((ArbilDataNode) destinationNode).getNeedsSaveToDisk(false)) {
	    ((ArbilDataNode) destinationNode).saveChangesToCache(true);
	}
	// Start new thread to add the node to its destination
	new Thread(creatAddNodeAndResources(destinationNode, nodeTypeDisplayNameLocal, addableNode, resourceFiles, copyDirectoryStructure, metadataFilePerResource), "AddNodeAndResources").start();
    }

    /**
     * Requests to add a list of nodes on basis of given existing nodes to the given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeTypeDisplayName name of node type to use for all nodes
     * @param addableNodes Nodes to base new node on
     */
    public void requestAddNodes(final ArbilDataNode destinationNode, final String nodeTypeDisplayName, final ArbilDataNode[] addableNodes) {
	String[] nodeTypeDisplayNames = new String[addableNodes.length];
	Arrays.fill(nodeTypeDisplayNames, nodeTypeDisplayName);
	requestAddNodes(destinationNode, nodeTypeDisplayNames, addableNodes);
    }

    /**
     * Requests to add a list of nodes on basis of given existing nodes to the given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Names of node type
     * @param addableNode Nodes to base new node on
     */
    public void requestAddNodes(final ArbilDataNode destinationNode, final String[] nodeTypeDisplayNames, final ArbilDataNode[] addableNode) {
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	// Start new thread to add the node to its destination
	creatAddAddableNodeThread(destinationNode, nodeTypeDisplayNames, addableNode).start();
    }

    /**
     * Creates a thread to be triggered by requestAddNode for addableNode
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param addableNodes Node to base new node on
     * @return New thread that adds the addable node
     */
    private Thread creatAddAddableNodeThread(final ArbilDataNode destinationNode, final String[] nodeTypeDisplayNames, final ArbilDataNode[] addableNodes) {
	return new Thread("requestAddNode") {
	    @Override
	    public void run() {
		try {
		    if (destinationNode != null) {
			destinationNode.updateLoadingState(1);
		    }
		    addNodes(destinationNode, nodeTypeDisplayNames, addableNodes);
		} catch (ArbilMetadataException exception) {
		    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), services.getString("INSERT NODE ERROR"));
		} catch (IOException exception) {
		    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), services.getString("INSERT NODE ERROR"));
		} catch (UnsupportedOperationException exception) {
		    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), services.getString("INSERT NODE ERROR"));
		} finally {
		    if (destinationNode != null) {
			destinationNode.updateLoadingState(-1);
		    }
		}
	    }
	};
    }

    private Runnable creatAddNodeAndResources(final ArbilNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode, final File[] resourceFiles, final boolean copyDirectoryStructure, final boolean metadataFilePerResource) {
	return new Runnable() {
	    public void run() {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	};
    }

    public void addNodes(final ArbilDataNode destinationNode, final String[] nodeTypeDisplayNames, final ArbilDataNode[] addableNodes) throws ArbilMetadataException, IOException {
	if (destinationNode == null) {
	    doAddNodes(null, nodeTypeDisplayNames, addableNodes);
	} else {
	    // synchronize on destination node, so that multiple additions do not happen simultaneously
	    synchronized (destinationNode.getParentDomLockObject()) {
		doAddNodes(destinationNode, nodeTypeDisplayNames, addableNodes);
	    }
	}
    }

    private void doAddNodes(final ArbilDataNode destinationNode, final String[] nodeTypeDisplayName, final ArbilDataNode[] addableNode) throws ArbilMetadataException, IOException {

	// Split inputs between metadata ndoes and non-metadata nodes

	// Lists will be initialized when needed
	List<ArbilDataNode> dataNodeMeta = null;
	List<ArbilDataNode> dataNodeNonMeta = null;
	List<String> nodeTypeDisplayNameNonMeta = null;

	for (int i = 0; i < nodeTypeDisplayName.length; i++) {
	    if (addableNode[i].isMetaDataNode()) {
		if (dataNodeMeta == null) {
		    dataNodeMeta = new ArrayList<ArbilDataNode>(addableNode.length);
		}
		dataNodeMeta.add(addableNode[i]);
	    } else {
		if (dataNodeNonMeta == null) {
		    dataNodeNonMeta = new ArrayList<ArbilDataNode>(addableNode.length);
		    nodeTypeDisplayNameNonMeta = new ArrayList<String>(nodeTypeDisplayName.length);
		}
		dataNodeNonMeta.add(addableNode[i]);
		nodeTypeDisplayNameNonMeta.add(nodeTypeDisplayName[i]);
	    }
	}

	if (dataNodeMeta != null && dataNodeMeta.size() > 0) {
	    addMetaDataNode(destinationNode, dataNodeMeta);
	}
	if (dataNodeNonMeta != null && dataNodeNonMeta.size() > 0) {
	    addNonMetaDataNode(destinationNode, nodeTypeDisplayNameNonMeta, dataNodeNonMeta);
	}
    }

    private void addNonMetaDataNode(final ArbilDataNode destinationNode, final List<String> nodeTypeDisplayNames, final List<ArbilDataNode> addableNodes) throws ArbilMetadataException {

	List<URI> addedNodeURIs = new ArrayList<URI>(addableNodes.size());

	destinationNode.getParentDomNode().updateLoadingState(+1);
	try {
	    for (int i = 0; i < addableNodes.size(); i++) {
		final ArbilDataNode addableNode = addableNodes.get(i);
		String nodeTypeDisplayName = nodeTypeDisplayNames.get(i);
		ArbilDataNode[] sourceArbilNodeArray;
		if (addableNode.isContainerNode()) {
		    sourceArbilNodeArray = addableNode.getChildArray();
		} else {
		    sourceArbilNodeArray = new ArbilDataNode[]{addableNode};
		}
		for (ArbilDataNode currentArbilNode : sourceArbilNodeArray) {
		    if (destinationNode.isCmdiMetaDataNode()) {
			new ArbilComponentBuilder().insertResourceProxy(destinationNode, addableNode);
//		    destinationNode.getParentDomNode().loadArbilDom();
		    } else {
			String nodeType;
			String favouriteUrlString = null;
			URI resourceUri = null;
			String mimeType = null;
			if (currentArbilNode.isArchivableFile() && !currentArbilNode.isMetaDataNode()) {
			    nodeType = MetadataReader.getSingleInstance().getNodeTypeFromMimeType(currentArbilNode.mpiMimeType);
			    if (nodeType == null) {
				nodeType = handleUnknownMimetype(currentArbilNode);
			    }
			    resourceUri = currentArbilNode.getURI();
			    mimeType = currentArbilNode.mpiMimeType;
			    nodeTypeDisplayName = "Resource";
			} else {
			    nodeType = ArbilFavourites.getSingleInstance().getNodeType(currentArbilNode, destinationNode);
			    favouriteUrlString = currentArbilNode.getUrlString();
			}
			if (nodeType != null) {
			    String targetXmlPath = destinationNode.getURIFragment();
			    logger.debug("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName + " : " + favouriteUrlString + " : " + resourceUri);

			    // Create child node
			    URI addedNodeUri = addChildNode(destinationNode, nodeType, targetXmlPath, resourceUri, mimeType);
			    addedNodeURIs.add(addedNodeUri);
			}
		    }
		}
	    }

	    // Adding done, reload desination node
	    destinationNode.scrollToRequested = true;
	    destinationNode.getParentDomNode().loadArbilDom();
	} finally {
	    destinationNode.getParentDomNode().updateLoadingState(-1);
	}
	if (addedNodeURIs.size() > 0) {
	    final String title;
	    if (addedNodeURIs.size() == 1) {
		title = MessageFormat.format(services.getString("NEW RESOURCE IN %S"), destinationNode);
	    } else {
		title = MessageFormat.format(services.getString("NEW RESOURCES IN %S"), destinationNode);
	    }
	    windowManager.openFloatingTableOnce(addedNodeURIs.toArray(new URI[]{}), title);
	}
    }

    /**
     *
     * @param currentArbilNode
     * @return Manual nodetype, if set. Otherwise null
     */
    private String handleUnknownMimetype(ArbilDataNode currentArbilNode) {
	if (JOptionPane.YES_OPTION == messageDialogHandler.showDialogBox(
		MessageFormat.format(services.getString("THERE IS NO CONTROLLED VOCABULARY FOR EITHER WRITTEN RESOURCE OR MEDIA FILE THAT MATCH \"{0}\"."), currentArbilNode.mpiMimeType) + "\n"
		+ services.getString("THIS PROBABLY MEANS THAT THE FILE IS NOT ARCHIVABLE. HOWEVER, YOU CAN PROCEED BY MANUALLY SELECTING THE RESOURCE TYPE.") + "\n\n"
		+ services.getString("DO YOU WANT TO PROCEED?") + "\n\n"
		+ services.getString("WARNING: DOING THIS WILL NOT GUARANTEE THAT YOUR DATA WILL BE UPLOADABLE TO THE CORPUS SERVER!"),
		services.getString("ADD RESOURCE"),
		JOptionPane.YES_NO_OPTION,
		JOptionPane.PLAIN_MESSAGE)) {
	    String originalMime = currentArbilNode.mpiMimeType;
	    currentArbilNode.mpiMimeType = null;
	    if (new ImdiUtils().overrideTypecheckerDecision(new ArbilDataNode[]{currentArbilNode})) {
		// Try again
		return MetadataReader.getSingleInstance().getNodeTypeFromMimeType(currentArbilNode.mpiMimeType);
	    } else {
		currentArbilNode.mpiMimeType = originalMime;
	    }
	}
	return null;
    }

    private void addMetaDataNode(final ArbilDataNode destinationNode, final List<ArbilDataNode> addableNodes) throws ArbilMetadataException, IOException {
	for (ArbilDataNode addableNode : addableNodes) {
	    URI addedNodeUri;
	    if (addableNode.getURIFragment() == null) {
		if (destinationNode != null) {
		    addedNodeUri = sessionStorage.getNewArbilFileName(destinationNode.getSubDirectory(), addableNode.getURI().getPath());
		} else {
		    addedNodeUri = sessionStorage.getNewArbilFileName(sessionStorage.getSaveLocation(""), addableNode.getURI().getPath());
		}
		ArbilDataNode.getMetadataUtils(addableNode.getURI().toString()).copyMetadataFile(addableNode.getURI(), new File(addedNodeUri), null, true);
		ArbilDataNode addedNode = dataNodeLoader.getArbilDataNodeWithoutLoading(addedNodeUri);
		new ArbilComponentBuilder().removeArchiveHandles(addedNode);
		if (destinationNode == null) {
		    // Destination node null means add to tree root
		    treeHelper.addLocation(addedNodeUri);
		    treeHelper.applyRootLocations();
		} else {
		    destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodeUri});
		}
		addedNode.loadArbilDom();
		addedNode.scrollToRequested = true;
	    } else {
		if (destinationNode == null) {
		    // Cannot add subnode to local corpus tree root
		    logger.warn("Attempt to add child node to local corpus root");
		    return;
		}
		addedNodeUri = arbilComponentBuilder.insertFavouriteComponent(destinationNode, addableNode);
		new ArbilComponentBuilder().removeArchiveHandles(destinationNode);
	    }
	    if (destinationNode != null) {
		destinationNode.getParentDomNode().loadArbilDom();
	    }
	    String newTableTitleString = "new " + addableNode + (destinationNode == null ? "" : (" in " + destinationNode));
	    windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
	}
    }

    public URI addChildNode(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
	return addChildNode(destinationNode, nodeType, targetXmlPath, resourceUri, mimeType, null);
    }

    /**
     * Add a new node based on a template and optionally attach a resource
     *
     * @param creationCallback option callback; if not null, its {@link NodeCreationCallback#nodeCreated(nl.mpi.arbil.data.ArbilDataNode, java.net.URI)
     * } method gets called as soon as the new node has been created and loaded.
     * @return String path to the added node
     */
    public URI addChildNode(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType, final NodeCreationCallback creationCallback) throws ArbilMetadataException {
	logger.debug("addChildNode:: {} : {}", nodeType, resourceUri);
	logger.debug("targetXmlPath:: {}", targetXmlPath);
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	URI addedNodePath = null;
	destinationNode.updateLoadingState(1);
	try {
	    synchronized (destinationNode.getParentDomLockObject()) {
		if (destinationNode.getNeedsSaveToDisk(false)) {
		    destinationNode.saveChangesToCache(false);
		}
		if (nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode()) {
		    // Add clarin sub nodes
		    addedNodePath = arbilComponentBuilder.insertChildComponent(destinationNode, targetXmlPath, nodeType);
		} else {
		    if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType) || (resourceUri != null && destinationNode.isSession())) {
			logger.debug("adding to current node");
			try {
			    Document nodDom = ArbilComponentBuilder.getDocument(destinationNode.getURI());
			    if (nodDom == null) {
				messageDialogHandler.addMessageDialogToQueue(services.getString("THE METADATA FILE COULD NOT BE OPENED"), services.getString("ADD NODE"));
			    } else {
				addedNodePath = MetadataReader.getSingleInstance().insertFromTemplate(destinationNode.getNodeTemplate(), destinationNode.getURI(), destinationNode.getSubDirectory(), nodeType, targetXmlPath, nodDom, resourceUri, mimeType);
				destinationNode.bumpHistory();
				ArbilComponentBuilder.savePrettyFormatting(nodDom, destinationNode.getFile());

				if (creationCallback == null) {
				    // reload without callback
				    dataNodeLoader.requestReload(destinationNode);
				} else {
				    // reload with new reload callback that wraps the creation callback
				    final URI newNodePath = addedNodePath;
				    dataNodeLoader.requestReload(destinationNode, new ArbilDataNodeLoaderCallBack() {
					public void dataNodeLoaded(ArbilDataNode reloadedDestinationNode) {
					    creationCallback.nodeCreated(reloadedDestinationNode, newNodePath);
					}
				    });
				}
			    }
			} catch (ParserConfigurationException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (SAXException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (IOException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			}
//            needsSaveToDisk = true;
		    } else {
			logger.debug("adding new metadata node of type {} as reference to {}", nodeType, destinationNode);
			if (!destinationNode.equals(destinationNode.getParentDomNode())) {
			    logger.error("Node type '{}' not a child node according to template. Destination node '{}' not a parent dom node.", nodeType, destinationNode);
			    throw new ArbilMetadataException("Cannot create reference on child node");
			}
			URI targetFileURI = sessionStorage.getNewArbilFileName(destinationNode.getSubDirectory(), nodeType);
			if (CmdiProfileReader.pathIsProfile(nodeType)) {
			    // Is CMDI profile
			    try {
				addedNodePath = arbilComponentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
				// TODO: some sort of warning like: "Could not add node of type: " + nodeType; would be useful here or downstream
//                    if (addedNodePath == null) {
//                      LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not add node of type: " + nodeType, "Error inserting node");
//                    }
			    } catch (URISyntaxException ex) {
				throw new ArbilMetadataException(String.format("Error while adding node to metadata hierarchy. Node type %s not a valid URI", nodeType), ex);
			    }
			} else {
			    addedNodePath = MetadataReader.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
			}
			if (addedNodePath == null) {
			    throw new ArbilMetadataException("A node of the requested could not be created. See previous messages for details.");
			} else {
			    if (destinationNode.getFile().exists()) {
				destinationNode.getParentDomNode().getMetadataUtils().addCorpusLink(destinationNode.getURI(), new URI[]{addedNodePath});
				destinationNode.getParentDomNode().loadArbilDom();
				if (creationCallback != null) {
				    creationCallback.nodeCreated(destinationNode, addedNodePath);
				}
			    } else {
				treeHelper.addLocation(addedNodePath);
				treeHelper.applyRootLocations();
			    }
			}
		    }
		    // CODE REMOVED: load then save the dom via the api to make sure there are id fields to each node then reload this imdi object
		}
	    }
	} finally {
	    destinationNode.updateLoadingState(-1);
	}
	return addedNodePath;
    }
}
