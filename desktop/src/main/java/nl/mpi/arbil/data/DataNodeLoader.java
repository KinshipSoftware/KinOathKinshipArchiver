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
import nl.mpi.flap.plugin.PluginArbilDataNodeLoader;

/**
 * @author Peter.Withers@mpi.nl
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface DataNodeLoader extends PluginArbilDataNodeLoader {

    void addNodeNeedingSave(ArbilDataNode nodeToSave);

    ArbilDataNode getArbilDataNode(Object registeringObject, URI localUri);

    ArbilDataNode getArbilDataNodeOnlyIfLoaded(URI arbilUri);

    ArbilDataNode getArbilDataNodeWithoutLoading(URI localUri);

    ArbilDataNode[] getNodesNeedSave();

    /**
     * @return the schemaCheckLocalFiles
     */
    boolean isSchemaCheckLocalFiles();

    boolean nodesNeedSave();

    void removeNodesNeedingSave(ArbilDataNode savedNode);

    void requestReload(ArbilDataNode dataNode);

    /**
     *
     * @param currentDataNode node to request reload for
     * @param callback option callback; if not null, its {@link ArbilDataNodeLoaderCallBack#dataNodeLoaded(nl.mpi.arbil.data.ArbilDataNode)
     * } method gets called as soon as the node has been successfully reloaded
     */
    void requestReload(ArbilDataNode currentDataNode, ArbilDataNodeLoaderCallBack callback);

    public void requestShallowReload(ArbilDataNode dataNode);

    void requestReloadAllNodes();
    
    void requestReloadAllMetadataNodes();

    void requestReloadOnlyIfLoaded(URI arbilUri);

    void saveNodesNeedingSave(boolean updateIcons);

    /**
     * @param schemaCheckLocalFiles the schemaCheckLocalFiles to set
     */
    void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles);

    void startLoaderThreads();

    void stopLoaderThreads();

    ArbilDataNode createNewDataNode(URI uri);
}
