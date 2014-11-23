/*
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.kinoath.graph;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.UnsortablePointsException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * @since Nov 23, 2014 6:04:05 PM (creation date)
 * @author Peter Withers
 */
public class DefaultSorter extends GraphSorter {

    private HashMap<UniqueIdentifier, SortingEntity> knownSortingEntities;

    @Override
    public void setEntitys(EntityData[] graphDataNodeArrayLocal) {
        super.setEntitys(graphDataNodeArrayLocal);
        // this section need only be done when the nodes are added to this graphsorter
        knownSortingEntities = new HashMap<UniqueIdentifier, SortingEntity>();
        for (EntityData currentNode : graphDataNodeArrayLocal) {
            if (currentNode.isVisible) {
                // only create sorting entities for visible entities
                knownSortingEntities.put(currentNode.getUniqueIdentifier(), new SortingEntity(currentNode, this));
            }
        }
        for (SortingEntity currentSorter : knownSortingEntities.values()) {
            currentSorter.calculateRelations(knownSortingEntities);
        }
//        sanguineSort();
        //printLocations(); // todo: remove this and maybe add a label of x,y post for each node to better see the sorting
    }

    public void placeAllNodes(HashMap<UniqueIdentifier, Point> entityPositions) throws UnsortablePointsException {
        // make a has table of all entites
        // find the first ego node
        // place it and all its immediate relatives onto the graph, each time checking that the space is free
        // contine to the next nearest relatives
        // when all done search for any unrelated nodes and do it all again

        // remove any transent nodes that are not in this list anymore
        // and make sure that invisible nodes are ignored
        ArrayList<UniqueIdentifier> removeNodeIds = new ArrayList<UniqueIdentifier>(entityPositions.keySet());
        for (EntityData currentNode : graphDataNodeArray) {
            removeNodeIds.remove(currentNode.getUniqueIdentifier());
            // remove any invisible node from the position list, the entities in a loaded svg should still get here even if they are not visible anymore
            if (!currentNode.isVisible) {
                entityPositions.remove(currentNode.getUniqueIdentifier());
            }
        }
        for (UniqueIdentifier currentRemoveId : removeNodeIds) {
            if (!(currentRemoveId.isGraphicsIdentifier())) {
                // remove the transent nodes making sure not to remove the positions of graphics such as labels
                entityPositions.remove(currentRemoveId);
            }
        }
        if (knownSortingEntities != null) {
            // start with the top most ancestors
            for (SortingEntity currentSorter : knownSortingEntities.values()) {
                // find all the entities without ancestors
                if (currentSorter.mustBeBelow.isEmpty()) {
                    boolean hasNoAncestors = true;
                    currentSorter.addLabel("HasNoAncestors");
                    // exclude those with spouses or siblings that have ancestors
                    for (SortingEntity spouseOrSibling : currentSorter.couldBeNextTo) {
                        if (!spouseOrSibling.mustBeBelow.isEmpty()) {
                            hasNoAncestors = false;
                            currentSorter.addLabel("SpouseHasAncestors");
                            break;
                        }
                    }
                    if (hasNoAncestors) {
                        currentSorter.getRelatedPositions(entityPositions);
                    }
                }
            }
            for (SortingEntity currentSorter : knownSortingEntities.values()) {
                currentSorter.getPosition(entityPositions);
                currentSorter.getRelatedPositions(entityPositions);
            }
        }
        for (UniqueIdentifier uniqueIdentifier : entityPositions.keySet()) {
            preferredLocations.put(uniqueIdentifier, entityPositions.get(uniqueIdentifier));
        }
    }
}
