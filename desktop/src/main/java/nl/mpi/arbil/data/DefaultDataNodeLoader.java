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

import java.net.URI;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Vector;
import nl.mpi.arbil.clarin.HandleUtils;
import nl.mpi.arbil.data.ArbilDataNode.LoadingState;
import nl.mpi.flap.model.PluginDataNode;
import nl.mpi.flap.plugin.WrongNodeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilDataNodeLoader formerly known as ImdiLoader Created on : Dec
 * 30, 2008, 3:04:39 PM
 *
 * @author Peter.Withers@mpi.nl
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DefaultDataNodeLoader implements DataNodeLoader {

    private final static Logger logger = LoggerFactory.getLogger(DefaultDataNodeLoader.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private Hashtable<String, ArbilDataNode> arbilHashTable = new Hashtable<String, ArbilDataNode>();
    private Vector<ArbilDataNode> nodesNeedingSave = new Vector<ArbilDataNode>();
    /**
     * Thread and queue manager, at the moment also has the implementation of
     * the actual loading threads (structure and relation between this class and
     * the thread manager should to be reconsidered)
     */
    private DataNodeLoaderThreadManager threadManager;
    private ArbilDataNodeService dataNodeService;
    private final MetadataFormat metadataFormat = new MetadataFormat();
    private final HandleUtils handleUtils = new HandleUtils();

    public DefaultDataNodeLoader(DataNodeLoaderThreadManager loaderThreadManager, ArbilDataNodeService dataNodeService) {
	this(loaderThreadManager);
	this.dataNodeService = dataNodeService;
    }

    /**
     * Constructor that does not set a data node service; <strong>remember to
     * call {@link #setDataNodeService(nl.mpi.arbil.data.ArbilDataNodeService)
     * } as soon as possible after construction</strong>. You may want to
     * consider constructing a {@link nl.mpi.arbil.data.ArbilDataNodeLoader}
     * instead.
     *
     * @param loaderThreadManager
     */
    protected DefaultDataNodeLoader(DataNodeLoaderThreadManager loaderThreadManager) {
	logger.debug("ArbilDataNodeLoader init");
	threadManager = loaderThreadManager;
    }

    protected final void setDataNodeService(ArbilDataNodeService dataNodeService) {
	this.dataNodeService = dataNodeService;
    }

    @Override
    public ArbilDataNode getArbilDataNodeWithoutLoading(URI localUri) {
	ArbilDataNode currentDataNode = null;
	if (localUri != null) {
	    localUri = resolveAndNormaliseUri(localUri);
	    // correct any variations in the url string
//            localUri = ImdiTreeObject.conformStringToUrl(localUri).toString();
	    currentDataNode = arbilHashTable.get(localUri.toString());
	    if (currentDataNode == null) {
		currentDataNode = new ArbilDataNode(dataNodeService, localUri, metadataFormat.shallowCheck(localUri));
		arbilHashTable.put(localUri.toString(), currentDataNode);
	    }
	}
	return currentDataNode;
    }

    /**
     * this is a transitional method and will be replaced when the time comes
     *
     * @return the ArbilDataNode that was obtained via getArbilDataNode and cast
     * to PluginDataNode
     */
    public PluginDataNode getPluginArbilDataNode(Object registeringObject, URI localUri) {
	return (PluginDataNode) getArbilDataNode(registeringObject, localUri);
    }

    @Override
    public ArbilDataNode getArbilDataNode(Object registeringObject, URI localUri) {// throws Exception {
//        if (localNodeText == null && localUrlString.contains("WrittenResource")) {
//            logger.debug("getImdiObject: " + localNodeText + " : " + localUrlString);
//        }
//        if (registeringObject == null) {
//            throw (new Exception("no container object provided"));
//        }
//       todo if (localUrlString == null) {
//            logger.debug("getImdiObject: " + localNodeText + " : " + localUrlString);
//       end todo }
	ArbilDataNode currentDataNode = null;
	if (localUri != null && localUri.toString().length() > 0) {
	    currentDataNode = getArbilDataNodeWithoutLoading(localUri);
	    if (!currentDataNode.getParentDomNode().isDataLoaded() && !currentDataNode.isLoading()) {
		if (MetadataFormat.isStringChildNode(currentDataNode.getUrlString())) {
		    // cause the parent node to be loaded
		    currentDataNode.getParentDomNode();
		} else if (MetadataFormat.isPathMetadata(currentDataNode.getUrlString()) || ArbilDataNode.isPathHistoryFile(currentDataNode.getUrlString())) {
		    threadManager.addNodeToQueue(currentDataNode);
		} else if (!MetadataFormat.isPathMetadata(currentDataNode.getUrlString())) {
//                    currentImdiObject.clearIcon(); // do not do this
		}
	    }
	    if (registeringObject != null && registeringObject instanceof ArbilDataNodeContainer) {
		currentDataNode.registerContainer((ArbilDataNodeContainer) registeringObject);
	    }
	}
	return currentDataNode;
    }

    // return the node only if it has already been loaded otherwise return null
    @Override
    public ArbilDataNode getArbilDataNodeOnlyIfLoaded(URI arbilUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
	arbilUri = resolveAndNormaliseUri(arbilUri);
	return arbilHashTable.get(arbilUri.toString());
    }

    // reload the node only if it has already been loaded otherwise ignore
    @Override
    public void requestReloadOnlyIfLoaded(URI arbilUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
	arbilUri = resolveAndNormaliseUri(arbilUri);
	ArbilDataNode currentDataNode = arbilHashTable.get(arbilUri.toString());
	if (currentDataNode != null) {
	    requestReload(currentDataNode);
	}
    }

    public void requestReload(ArbilDataNode currentDataNode) {
	requestReload(currentDataNode, null);
    }

    public void requestReload(ArbilDataNode currentDataNode, ArbilDataNodeLoaderCallBack callback) {
	requestReload(currentDataNode, currentDataNode.getRequestedLoadingState(), callback);
    }

    public void requestShallowReload(ArbilDataNode currentDataNode) {
	requestReload(currentDataNode, LoadingState.PARTIAL, null);
    }

    // reload the node or if it is an imdichild node then reload its parent
    private void requestReload(final ArbilDataNode requestNode, LoadingState loadingState, ArbilDataNodeLoaderCallBack callback) {
	// We want to reload the node's parent dom node
	final ArbilDataNode reloadNode;
	if (requestNode.isChildNode()) {
	    reloadNode = requestNode.getParentDomNode();
	} else {
	    reloadNode = requestNode;
	}
	removeNodesNeedingSave(reloadNode);

	// Never override requested full load with partial load
	if (!LoadingState.LOADED.equals(reloadNode.getRequestedLoadingState())) {
	    reloadNode.setRequestedLoadingState(loadingState);
	}

	if (callback != null) {
	    // Callback should happen on the request node
	    threadManager.addLoaderCallback(requestNode, callback);
	}
	threadManager.addNodeToQueue(reloadNode);
    }

    @Override
    public void requestReloadAllNodes() {
	final ArbilDataNode[] currentNodes = arbilHashTable.values().toArray(new ArbilDataNode[]{});
	for (ArbilDataNode currentDataNode : currentNodes) {
	    requestReload(currentDataNode);
	}
    }

    @Override
    public void requestReloadAllMetadataNodes() {
	final ArbilDataNode[] currentNodes = arbilHashTable.values().toArray(new ArbilDataNode[]{});
	for (ArbilDataNode currentDataNode : currentNodes) {
	    if (currentDataNode.isMetaDataNode()) {
		requestReload(currentDataNode);
	    }
	}
    }

    @Override
    public void startLoaderThreads() {
	threadManager.startLoaderThreads();
    }

    public void stopLoaderThreads() {
	threadManager.stopLoaderThreads();
    }

    @Override
    protected void finalize() throws Throwable {
	// stop the thread
	threadManager.setContinueThread(false);
	super.finalize();
    }

    @Override
    public void addNodeNeedingSave(ArbilDataNode nodeToSave) {
	nodeToSave = nodeToSave.getParentDomNode();
	if (!nodesNeedingSave.contains(nodeToSave)) {
	    nodesNeedingSave.add(nodeToSave);
	}
    }

    @Override
    public void removeNodesNeedingSave(ArbilDataNode savedNode) {
	nodesNeedingSave.remove(savedNode);
    }

    @Override
    public ArbilDataNode[] getNodesNeedSave() {
	return nodesNeedingSave.toArray(new ArbilDataNode[]{});
    }

    @Override
    public boolean nodesNeedSave() {
	return nodesNeedingSave.size() > 0;
    }

    @Override
    public synchronized void saveNodesNeedingSave(boolean updateIcons) {
	// this is syncronised to avoid issues from the key repeat on linux which fails to destinguish between key up events and key repeat events
	while (nodesNeedingSave.size() > 0) {
	    // remove the node from the save list not in the save function because otherwise if the save fails the application will lock up
	    ArbilDataNode currentNode = nodesNeedingSave.remove(0);
	    if (currentNode != null) {
		currentNode.saveChangesToCache(updateIcons); // saving removes the node from the nodesNeedingSave vector via removeNodesNeedingSave
		if (updateIcons) {
		    requestReload(currentNode);
		}
	    }
	}
    }

    public URI getNodeURI(PluginDataNode dataNode) throws WrongNodeTypeException {
	if (dataNode instanceof ArbilDataNode) {
	    return ((ArbilDataNode) dataNode).getURI();
	} else {
	    throw new WrongNodeTypeException("Not an ArbilDataNode.");
	}
    }

    public boolean isNodeLoading(PluginDataNode dataNode) {
	if (dataNode instanceof ArbilDataNode) {
	    return ((ArbilDataNode) dataNode).isLoading();
	} else {
	    return false;
	}
    }

    /**
     * @return the schemaCheckLocalFiles
     */
    @Override
    public boolean isSchemaCheckLocalFiles() {
	return threadManager.isSchemaCheckLocalFiles();
    }

    /**
     * @param schemaCheckLocalFiles the schemaCheckLocalFiles to set
     */
    @Override
    public void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles) {
	threadManager.setSchemaCheckLocalFiles(schemaCheckLocalFiles);
    }

    public ArbilDataNode createNewDataNode(URI uri) {
	uri = resolveAndNormaliseUri(uri);
	return new ArbilDataNode(dataNodeService, uri, metadataFormat.shallowCheck(uri));
    }

    /**
     * @return the threadManager
     */
    protected DataNodeLoaderThreadManager getThreadManager() {
	return threadManager;
    }

    /**
     * Normalises URI, follows any redirects and resolves any handles
     *
     * @param uri URI to resolve and normalise
     * @return normalised and resolved URI
     */
    private URI resolveAndNormaliseUri(URI uri) {
	return handleUtils.followRedirect(ArbilDataNodeService.normaliseURI(uri));
    }
}
