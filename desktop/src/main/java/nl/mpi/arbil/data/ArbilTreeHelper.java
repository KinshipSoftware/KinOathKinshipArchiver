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

import java.awt.Component;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton instance of TreeHelper, for use with Arbil desktop application
 * Document : ArbilTreeHelper
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeHelper extends AbstractTreeHelper {

    private final static Logger logger = LoggerFactory.getLogger(ArbilTreeHelper.class);
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private SessionStorage sessionStorage;
    private ArbilTreePanels arbilTreePanel;

    public ArbilTreeHelper(SessionStorage sessionStorage, MessageDialogHandler messageDialogHandler) {
	super(messageDialogHandler);
	this.sessionStorage = sessionStorage;
    }

    public void init() {
	initTrees();
	// load any locations from the previous file formats
	//LinorgFavourites.getSingleInstance().convertOldFormatLocationLists();
	loadLocationsList();
    }

    @Override
    protected SessionStorage getSessionStorage() {
	return sessionStorage;
    }

    public void setTrees(ArbilTreePanels arbilTreePanelLocal) {
	arbilTreePanel = arbilTreePanelLocal;
	arbilTreePanel.remoteCorpusTree.setName("RemoteCorpusTree");
	arbilTreePanel.localCorpusTree.setName("LocalCorpusTree");
	arbilTreePanel.localDirectoryTree.setName("LocalDirectoryTree");
	arbilTreePanel.favouritesTree.setName("FavouritesTree");

	applyRootLocations();
    }

    public boolean componentIsTheLocalCorpusTree(Component componentToTest) {
	return componentToTest.equals(arbilTreePanel.localCorpusTree);
    }

    public boolean componentIsTheFavouritesTree(Component componentToTest) {
	return componentToTest.equals(arbilTreePanel.favouritesTree);
    }

    @Override
    public void applyRootLocations() {
	logger.debug("applyRootLocations");
	arbilTreePanel.localCorpusTree.requestResort();
	arbilTreePanel.remoteCorpusTree.requestResort();
	arbilTreePanel.localDirectoryTree.requestResort();
	arbilTreePanel.favouritesTree.requestResort();
    }

    public DefaultMutableTreeNode getLocalCorpusTreeSingleSelection() {
	logger.debug("localCorpusTree: {}", arbilTreePanel.localCorpusTree);
	return (DefaultMutableTreeNode) arbilTreePanel.localCorpusTree.getSelectionPath().getLastPathComponent();
    }

    public void deleteNodes(Object sourceObject) {
	logger.debug("deleteNode: {}", sourceObject);
	if (sourceObject == arbilTreePanel.localCorpusTree || sourceObject == arbilTreePanel.favouritesTree) {
	    final ArbilTree tree = (ArbilTree) sourceObject;
	    deleteNodesFromTree(tree);
	} else if (sourceObject instanceof ArbilTable) {
	    final ArbilTable table = (ArbilTable) sourceObject;
	    deleteNodesFromParent(Arrays.asList(table.getSelectedRowsFromTable()));
	} else {
	    logger.info("Cannot delete from this source: {}", sourceObject);
	}
    }

    private void deleteNodesFromTree(final ArbilTree tree) {
	TreePath currentNodePaths[] = tree.getSelectionPaths();
	int toDeleteCount = 0;
	// count the number of nodes to delete
	String nameOfFirst = null;
	for (TreePath currentNodePath : currentNodePaths) {
	    if (currentNodePath != null) {
		DefaultMutableTreeNode selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
		Object userObject = selectedTreeNode.getUserObject();
		if (userObject instanceof ArbilDataNode) {
		    if (((ArbilDataNode) userObject).fileNotFound) {
			toDeleteCount++;
		    } else if (((ArbilDataNode) userObject).isEmptyMetaNode()) {
			toDeleteCount += ((ArbilDataNode) userObject).getChildCount();
		    } else {
			toDeleteCount++;
		    }
		    if (nameOfFirst == null) {
			nameOfFirst = ((ArbilDataNode) userObject).toString();
		    }
		}
	    }
	}

	final String messageString;
	if (toDeleteCount == 1) {
	    messageString = MessageFormat.format(widgets.getString("DELETE THE NODE {0}?"), nameOfFirst) + "\n" + widgets.getString("THIS WILL ALSO SAVE ANY PENDING CHANGES TO DISK.");
	} else {
	    messageString = MessageFormat.format(widgets.getString("DELETE {0} NODES?"), toDeleteCount) + "\n" + widgets.getString("THIS WILL ALSO SAVE ANY PENDING CHANGES TO DISK.");
	}

	if (JOptionPane.OK_OPTION == getMessageDialogHandler().showDialogBox(
		messageString, widgets.getString("DELETE"),
		JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
	    // make lists of nodes to delete
	    Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList = new HashMap<ArbilDataNode, List<ArbilDataNode>>();
	    Map<ArbilDataNode, List<String>> childNodeDeleteList = new HashMap<ArbilDataNode, List<String>>();
	    Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList = new HashMap<ArbilDataNode, List<ArbilDataNode>>();
	    determineNodesToDelete(currentNodePaths, childNodeDeleteList, dataNodesDeleteList, cmdiLinksDeleteList);
	    // delete child nodes
	    deleteNodesByChidXmlIdLink(childNodeDeleteList);
	    // delete parent nodes
	    deleteNodesByCorpusLink(dataNodesDeleteList);
	    // delete CMDI link nodes
	    deleteCmdiLinks(cmdiLinksDeleteList);
	}
    }

    @Override
    public boolean addLocationInteractive(URI addableLocation) {
	boolean added = addLocation(addableLocation);
	if (!added) {
	    // alert the user when the node already exists and cannot be added again
	    getMessageDialogHandler().addMessageDialogToQueue(widgets.getString("THE LOCATION ALREADY EXISTS AND CANNOT BE ADDED AGAIN"), widgets.getString("ADD LOCATION"));
	}
	applyRootLocations();
	return added;
    }

    public ArbilTreePanels getArbilTreePanel() {
	return arbilTreePanel;
    }
}
