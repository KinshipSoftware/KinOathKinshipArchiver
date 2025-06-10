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
package nl.mpi.arbil.search;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vehicle for searching local & remote corpora
 * Execute search in this order:
 * 1. splitLocalRemote() - this will separate the local and remote search nodes
 * 2. (Optional) fetchRemoteSearchResults() - only required for searches involving remote search nodes
 * 3. searchLocalNodes() - does the actual search on both local and remote search nodes
 * @see splitLocalRemote()
 * @see fetchRemoteSearchResults()
 * @see searchLocalNodes()
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSearch {
    private final static Logger logger = LoggerFactory.getLogger(ArbilSearch.class);

    private ArrayList<ArbilNode> localSearchNodes = new ArrayList<ArbilNode>();
    private ArrayList<ArbilNode> remoteSearchNodes = new ArrayList<ArbilNode>();
    private ArrayList<ArbilNode> foundNodes = new ArrayList<ArbilNode>();
    private boolean searchStopped = false;
    private int totalSearched;
    private int totalNodesToSearch = -1;
    private AbstractArbilTableModel resultsTableModel;
    private ArbilDataNodeContainer container;
    private Collection<? extends ArbilNode> selectedNodes;
    private Collection<ArbilNodeSearchTerm> nodeSearchTerms;
    private RemoteServerSearchTerm remoteServerSearchTerm;
    private ArbilSearchListener listener;
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    /**
     * Constructor without data container or search listener
     * @param selectedNodes
     * @param nodeSearchTerms
     * @param remoteServerSearchTerm Can be null for no remote search term
     * @param resultsTableModel
     */
    public ArbilSearch(Collection<? extends ArbilNode> selectedNodes, List<ArbilNodeSearchTerm> nodeSearchTerms, RemoteServerSearchTerm remoteServerSearchTerm, AbstractArbilTableModel resultsTableModel) {
	this(selectedNodes, nodeSearchTerms, remoteServerSearchTerm, resultsTableModel, null, null);
    }

    /**
     * Constructor that sets data container and listener
     * @param selectedNodes
     * @param nodeSearchTerms
     * @param remoteServerSearchTerm Can be null for no remote search term
     * @param resultsTableModel
     * @param container
     * @param listener 
     */
    public ArbilSearch(Collection<? extends ArbilNode> selectedNodes, List<ArbilNodeSearchTerm> nodeSearchTerms, RemoteServerSearchTerm remoteServerSearchTerm, AbstractArbilTableModel resultsTableModel, ArbilDataNodeContainer container, ArbilSearchListener listener) {
	this.remoteServerSearchTerm = remoteServerSearchTerm;
	this.selectedNodes = selectedNodes;
	this.nodeSearchTerms = nodeSearchTerms;
	this.resultsTableModel = resultsTableModel;
	this.container = container;
	this.listener = listener;
    }

    public void stopSearch() {
	this.searchStopped = true;
    }

    public boolean isSearchStopped() {
	return searchStopped;
    }

    /**
     * Call this before executing the actual search
     * @see fetchRemoteSearchResults()
     * @see searchLocalNodes()
     */
    public void splitLocalRemote() {
	for (ArbilNode arbilDataNode : selectedNodes) {
	    if (arbilDataNode.isLocal()) {
		localSearchNodes.add(arbilDataNode);
	    } else {
		remoteSearchNodes.add(arbilDataNode);
	    }
	}
    }

    /**
     * Call this as the final stage of the search process. 
     * searchLocalNodes() and optionally fetchRemoteSearchResults() should be called first
     * @see splitLocalRemote()
     * @see fetchRemoteSearchResults()
     */
    public void searchLocalNodes() {
	totalSearched = 0;
	while (localSearchNodes.size() > 0 && !searchStopped) {
	    Object currentElement = localSearchNodes.remove(0);
	    if (currentElement instanceof ArbilNode) {
		searchLocalNode((ArbilNode) currentElement);
	    }
	    if (listener != null) {
		listener.searchProgress(currentElement);
	    }
	}
    }

    /**
     * Optionally Call this before searchLocalNodes() but after splitLocalRemote()
     * @see splitLocalRemote()
     * @see searchLocalNodes()
     */
    public void fetchRemoteSearchResults() {
	if (remoteServerSearchTerm != null) {
	    for (URI serverFoundUrl : remoteServerSearchTerm.getServerSearchResults(remoteSearchNodes.toArray(new ArbilDataNode[]{}))) {
		logger.debug("remote node found: {}", serverFoundUrl);
		localSearchNodes.add(dataNodeLoader.getArbilDataNode(null, serverFoundUrl));
	    }
	}
    }

    /**
     * @return the foundNodes
     */
    public ArrayList<ArbilNode> getFoundNodes() {
	return foundNodes;
    }

    /**
     * @return the totalSearched
     */
    public int getTotalSearched() {
	return totalSearched;
    }

    /**
     * @return the totalNodesToSearch
     */
    public int getTotalNodesToSearch() {
	return totalNodesToSearch;
    }

    /**
     * @param totalNodesToSearch the totalNodesToSearch to set
     */
    public void setTotalNodesToSearch(int totalNodesToSearch) {
	this.totalNodesToSearch = totalNodesToSearch;
    }

    /**
     * @return the nodeSearchTerms
     */
    public Collection<ArbilNodeSearchTerm> getNodeSearchTerms() {
	return nodeSearchTerms;
    }

    public static interface ArbilSearchListener {

	/**
	 * Called for each element in the search process.
	 * @param currentElement  Listener needs to check whether it is actually a ArbilNode
	 */
	void searchProgress(Object currentElement);
    }

    private int searchLocalNode(ArbilNode currentNode) {
	// If node is ArbilDataNode, store in variable of that type
	ArbilDataNode dataNode = null;
	if (currentNode instanceof ArbilDataNode) {
	    dataNode = (ArbilDataNode) currentNode;
	}

	// Put unloaded data nodes back in the queue
	if (dataNode != null && !currentNode.isChildNode() && (currentNode.isLoading() || !currentNode.isDataLoaded())) {
	    logger.debug("searching: {}", dataNode.getUrlString());
	    if (dataNode.isMetaDataNode() || dataNode.isLocal()) {
		logger.debug("still loading so putting back into the list: {}", currentNode);
		if (!dataNode.fileNotFound) {
		    if (container != null) {
			dataNode.registerContainer(container); // this causes the node to be loaded
		    }
		    localSearchNodes.add(currentNode);
		}
	    } else {
		logger.debug("skipping unloaded remote resource: {}", currentNode);
	    }
	} else {
	    // perform the search
	    logger.debug("searching: {}", currentNode);
	    // add the child nodes
	    if (currentNode.isLocal() || !currentNode.isCorpus()) {
		// don't search remote corpus
		for (ArbilNode currentChildNode : currentNode.getChildArray()) {
		    logger.debug("adding to search list: {}", currentChildNode);
		    if (container != null) {
			currentChildNode.registerContainer(container); // this causes the node to be loaded
		    }
		    localSearchNodes.add(currentChildNode);
		}
	    }
	    // Do actual search only on data nodes (i.e. nodes that actually contain data)
	    if (dataNode != null) {
		searchLocalDataNode(dataNode);
	    }
	}
	return totalSearched;
    }

    private void searchLocalDataNode(ArbilDataNode dataNode) {
	boolean nodePassedFilter = true;
	//for (Component currentTermComponent : searchTermsPanel.getComponents()) {
	for (ArbilNodeSearchTerm currentTermPanel : nodeSearchTerms) {
	    boolean termPassedFilter = true;
	    // filter by the node type if entered	    
	    if (currentTermPanel.getNodeType().equals(ArbilNodeSearchTerm.NODE_TYPE_CORPUS)) {
		termPassedFilter = dataNode.isCorpus();
	    } else if (currentTermPanel.getNodeType().equals(ArbilNodeSearchTerm.NODE_TYPE_SESSION)) {
		termPassedFilter = dataNode.isSession();
	    } else if (currentTermPanel.getNodeType().equals(ArbilNodeSearchTerm.NODE_TYPE_CATALOGUE)) {
		termPassedFilter = dataNode.isCatalogue();
	    } else if (!currentTermPanel.getNodeType().equals(ArbilNodeSearchTerm.NODE_TYPE_ALL)) {
		termPassedFilter = dataNode.getUrlString().matches(".*" + currentTermPanel.getNodeType() + "\\(\\d*?\\)$");
	    }

	    if (termPassedFilter) {
		// Matches node type, now match against field value(s)
		if (null != currentTermPanel.getSearchFieldName() && currentTermPanel.getSearchFieldName().length() > 0) {
		    // filter by the field name and search string if entered
		    termPassedFilter = dataNode.containsFieldValue(currentTermPanel.getSearchFieldName(), currentTermPanel.getSearchString());
		} else if (null != currentTermPanel.getSearchString() && currentTermPanel.getSearchString().length() > 0) {
		    // filter by the search string if entered
		    termPassedFilter = dataNode.containsFieldValue(currentTermPanel.getSearchString());
		}
	    }

	    // invert based on the == / != selection
	    termPassedFilter = currentTermPanel.isNotEqual() != termPassedFilter;
	    // apply the and or booleans against the other search terms
	    if (!currentTermPanel.isBooleanAnd() && nodePassedFilter) {
		// we have moved into an OR block so if we already have a positive result then exit the term checking loop
		break;
	    }
	    if (currentTermPanel.isBooleanAnd()) {
		nodePassedFilter = (nodePassedFilter && termPassedFilter);
	    } else {
		nodePassedFilter = (nodePassedFilter || termPassedFilter);
	    }
	}
	totalSearched++;
	// if the node has no fields it should still be added since it will only pass a search if for instance the search is for actors and in that case it should be shown even if blank
	if (nodePassedFilter) {
	    foundNodes.add(dataNode);
	    resultsTableModel.addSingleArbilDataNode(dataNode);
	} else {
	    if (container != null) {
		dataNode.removeContainer(container);
	    }
	}
	if (totalNodesToSearch < totalSearched + localSearchNodes.size()) {
	    totalNodesToSearch = totalSearched + localSearchNodes.size();
	}
    }
}
