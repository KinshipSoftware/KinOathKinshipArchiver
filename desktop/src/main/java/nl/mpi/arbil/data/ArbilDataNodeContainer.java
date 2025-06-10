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
package nl.mpi.arbil.data;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilDataNodeContainer {

    /**
     * Data node is to be removed from the container
     *
     * @param dataNode Data node that should be removed
     */
    void dataNodeRemoved(ArbilNode dataNode);

    /**
     * Data node is clearing its icon
     *
     * @param dataNode Data node that is clearing its icon
     */
    void dataNodeIconCleared(ArbilNode dataNode);

    /**
     * A new child node has been added to the destination node
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    void dataNodeChildAdded(ArbilNode destination, ArbilNode newChildNode);

    /**
     *
     * @return whether a {@link ArbilDataNode.LoadingState#LOADED} state is required. If false, this indicates a 
     * {@link ArbilDataNode.LoadingState#PARTIAL} suffices.
     */
    boolean isFullyLoadedNodeRequired();
}
