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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface TreeHelper {

    void init();

    int addDefaultCorpusLocations();

    boolean addLocation(URI addedLocation);

    boolean addLocationInteractive(URI addableLocation);

    void applyRootLocations();

    void deleteNodes(Object sourceObject);

    /**
     * @return the favouriteNodes
     */
    ArbilDataNode[] getFavouriteNodes();

    /**
     * @return the favouritesTreeModel
     */
    DefaultTreeModel getFavouritesTreeModel();

    /**
     * @return the localCorpusNodes
     */
    ArbilDataNode[] getLocalCorpusNodes();

    /**
     * @return the localCorpusTreeModel
     */
    DefaultTreeModel getLocalCorpusTreeModel();

    /**
     * @return the localDirectoryTreeModel
     */
    DefaultTreeModel getLocalDirectoryTreeModel();

    /**
     * @return the localFileNodes
     */
    ArbilDataNode[] getLocalFileNodes();

    /**
     * @return the remoteCorpusNodes
     */
    ArbilDataNode[] getRemoteCorpusNodes();

    /**
     * @return the remoteCorpusTreeModel
     */
    DefaultTreeModel getRemoteCorpusTreeModel();

    boolean isInFavouritesNodes(ArbilDataNode dataNode);

    /**
     * @return the showHiddenFilesInTree
     */
    boolean isShowHiddenFilesInTree();
    
    boolean isGroupFavouritesByType();

    void jumpToSelectionInTree(boolean silent, ArbilDataNode cellDataNode);

    void loadLocationsList();

    boolean locationsHaveBeenAdded();

    void removeLocation(ArbilDataNode removeObject);

    void clearRemoteLocations();

    void addLocations(List<URI> locations);

    void addLocations(InputStream inputStream) throws IOException;

    void removeLocation(URI removeLocation);

    void saveLocations(ArbilDataNode[] nodesToAdd, ArbilDataNode[] nodesToRemove);

    void setShowHiddenFilesInTree(boolean showState);
    
    void setGroupFavouritesByType(boolean groupFavouritesByType);
}
