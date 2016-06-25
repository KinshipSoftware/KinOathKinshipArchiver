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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityDate;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.KinPoint;
import nl.mpi.kinnate.kindata.UnsortablePointsException;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * @since Nov 23, 2014 6:34:54 PM (creation date)
 * @author Peter Withers
 */
public class SortingEntity implements Comparable<SortingEntity> {

    private final GraphSorter graphSorter;
    UniqueIdentifier selfEntityId;
    ArrayList<SortingEntity> mustBeBelow;
    ArrayList<SortingEntity> mustBeAbove;
    ArrayList<SortingEntity> mustBeNextTo;
    ArrayList<SortingEntity> couldBeNextTo;
    EntityRelation[] allRelateNodes;
    KinPoint calculatedPosition = null;
//        StringBuilder tempLabels = new StringBuilder(); // for testing only
    EntityData entityData; // for testing only

    public SortingEntity(EntityData entityData, GraphSorter graphSorter) {
        this.graphSorter = graphSorter;
        this.entityData = entityData; // for testing only
        this.entityData.clearTempLabels(); // for testing only
//        sortCounter = 0; // for testing only
        selfEntityId = entityData.getUniqueIdentifier();
        allRelateNodes = entityData.getAllRelations();
        mustBeBelow = new ArrayList<SortingEntity>();
        mustBeAbove = new ArrayList<SortingEntity>();
        mustBeNextTo = new ArrayList<SortingEntity>();
        couldBeNextTo = new ArrayList<SortingEntity>();
    }

    public int compareTo(SortingEntity o) {
        final EntityDate dateOfBirth = this.entityData.getDateOfBirth();
        final EntityDate dateOfBirth1 = o.entityData.getDateOfBirth();
        if (dateOfBirth == null && dateOfBirth1 == null) {
            return 0;
        }
        if (dateOfBirth == null) {
            return -1;
        }
        if (dateOfBirth1 == null) {
            return 1;
        }
        return dateOfBirth.compareTo(dateOfBirth1);
    }

    // for testing only
    public void addLabel(String labelString) {
//            tempLabels.append(labelString);
//            entityData.appendTempLabel(labelString);
    }

    public void calculateRelations(HashMap<UniqueIdentifier, SortingEntity> knownSortingEntities) {
        for (EntityRelation entityRelation : allRelateNodes) {
            if (entityRelation.getAlterNode() != null && entityRelation.getAlterNode().isVisible) {
                switch (entityRelation.getRelationType()) {
                    case ancestor:
                        mustBeBelow.add(knownSortingEntities.get(entityRelation.alterUniqueIdentifier));
                        break;
                    case descendant:
                        mustBeAbove.add(knownSortingEntities.get(entityRelation.alterUniqueIdentifier));
                        break;
                    case union:
                        mustBeNextTo.add(knownSortingEntities.get(entityRelation.alterUniqueIdentifier));
                    // no break here is deliberate: those that mustBeNextTo to need also to be in couldBeNextTo
                    case sibling:
                        couldBeNextTo.add(knownSortingEntities.get(entityRelation.alterUniqueIdentifier));
                        break;
                }
            }
        }
        Collections.sort(mustBeAbove);
        Collections.sort(couldBeNextTo);
    }

    private boolean positionIsFree(UniqueIdentifier currentIdentifier, KinPoint targetPosition, HashMap<UniqueIdentifier, KinPoint> entityPositions) throws UnsortablePointsException {
        int useCount = 0;
        int pointUseCount = 0;
        for (KinPoint currentPosition : entityPositions.values()) {
            if (targetPosition == currentPosition) {
                pointUseCount++;
            }
            if (currentPosition.x == targetPosition.x && currentPosition.y == targetPosition.y) {
                useCount++;
            }
        }
        if (pointUseCount >= 1) {
            throw new UnsortablePointsException("The same point instance was found " + pointUseCount + " times.");
        }
        if (useCount == 0) {
            return true;
        }
        if (useCount == 1) {
            KinPoint entityPosition = entityPositions.get(currentIdentifier);
            if (entityPosition != null) {
                // todo: change this to compare distance not exact location
                if (entityPosition.x == targetPosition.x && entityPosition.y == targetPosition.y) {
                    // if there is one entity already in this position then check if it is the current entity, in which case it is free
                    return true;
                }
            }
        }
        return false;
    }

    protected KinPoint getPosition(HashMap<UniqueIdentifier, KinPoint> entityPositions) throws UnsortablePointsException {
//            System.out.println("getPosition: " + selfEntityId.getAttributeIdentifier());
//        addLabel("Sorting:" + sortCounter++); // for testing only
        calculatedPosition = entityPositions.get(selfEntityId);
        if (calculatedPosition == null) {
            for (SortingEntity sortingEntity : mustBeBelow) {
                // note that this does not set the position and the result can be null
                KinPoint nextAbovePos = entityPositions.get(sortingEntity.selfEntityId);
                // this should not be called until all parents have locations set, hence nextAbovePos should never be null
                if (calculatedPosition == null && nextAbovePos != null) {
                    // calculate the parent average position
                    float averageX = 0;
                    // nextAbovePos has been observed being null here after importing a CSV from Halle
                    int maxParentY = nextAbovePos.y;
                    for (SortingEntity sortingEntityInner : mustBeBelow) {
                        final KinPoint parentPosition = entityPositions.get(sortingEntityInner.selfEntityId);
                        averageX = averageX + parentPosition.x;
//                                addLabel("TotalX:" + averageX);
                        if (maxParentY < parentPosition.y) {
                            maxParentY = parentPosition.y;
                        }
                    }
                    averageX = averageX / mustBeBelow.size();
//                            addLabel("AverageX1:" + averageX);
                    // offset by the number of siblings  
                    Set<SortingEntity> unionOfSiblings = new HashSet<SortingEntity>();
                    for (SortingEntity sortingEntityInner : mustBeBelow) {
                        unionOfSiblings.addAll(sortingEntityInner.mustBeAbove);
                    }
//                            Set<SortingEntity> intersection = new HashSet<SortingEntity>(mustBeAbove);
//                            intersection.retainAll(couldBeNextTo);
//                            averageX = averageX - xPadding * intersection.size() / 2;
                    addLabel("NumberOfSiblings:" + unionOfSiblings.size());
                    averageX = averageX - graphSorter.xPadding * (unionOfSiblings.size() - 1) / 2;
//                            addLabel("AverageX2:" + averageX);
                    calculatedPosition = new KinPoint((int) averageX, maxParentY + graphSorter.yPadding);
                    addLabel(":mustBeBelow");
                }
                break;
            }
            if (calculatedPosition == null) {
                for (SortingEntity sortingEntity : couldBeNextTo) {
                    // note that this does not set the position and the result can be null
                    KinPoint nextToPos = entityPositions.get(sortingEntity.selfEntityId);
                    if (calculatedPosition == null && nextToPos != null) {
                        calculatedPosition = new KinPoint(nextToPos.x, nextToPos.y);
                        addLabel(":couldBeNextTo");
                        break;
                    }
                }
            }
            if (calculatedPosition == null) {
                for (SortingEntity sortingEntity : mustBeAbove) {
                    // note that this does not set the position and the result can be null
                    KinPoint nextBelowPos = entityPositions.get(sortingEntity.selfEntityId);
                    if (nextBelowPos != null) {
                        // offset by the number of children
                        float averageX = nextBelowPos.x + graphSorter.xPadding * (mustBeAbove.size() - 1) / 2.0f;
                        if (calculatedPosition == null) {
                            calculatedPosition = new KinPoint((int) averageX, nextBelowPos.y);
                            addLabel(":mustBeAbove");
                        }
                        if (nextBelowPos.y < calculatedPosition.y + graphSorter.yPadding) {
                            calculatedPosition.setLocation(calculatedPosition.x, nextBelowPos.y - graphSorter.yPadding);
//                        calculatedPosition[0] = nextAbovePos[0];
//                                System.out.println("move up: " + selfEntityId.getAttributeIdentifier());
                            addLabel(":U");
                        }
                        break;
                    }
                }
            }
            if (calculatedPosition == null) {
                calculatedPosition = graphSorter.getDefaultPosition(entityPositions, selfEntityId);
                addLabel(":defaultPosition");
            }
            // make sure any spouses are in the same row
            // todo: this should probably be moved into a separate action and when a move is made then move in sequence the entities that are below and to the right
            // todo: mustBeNextTo could be sorted first 
//                for (SortingEntity sortingEntity : mustBeNextTo) {
//                    KinPoint nextToPos = entityPositions.get(sortingEntity.selfEntityId);
//                    if (nextToPos != null) {
//                        if (nextToPos.y > calculatedPosition.y) {
//                            calculatedPosition = new KinPoint(nextToPos.x, nextToPos.y);
//                            addLabel(":mustBeNextTo");
//                        }
//
//
////                    KinPoint nextToPos = entityPositions.get(sortingEntity.selfEntityId);
////                    if (nextToPos != null) {
////                        if (nextToPos.y > calculatedPosition.y) {
////                            calculatedPosition = new KinPoint(nextToPos.x, nextToPos.y);
////                            addLabel(":mustBeNextTo");
////                        }
//////                    } else {
//////                        // prepopulate the spouse position
//////                        float[] spousePosition = new float[]{calculatedPosition[0], calculatedPosition[1]};
//////                        while (!positionIsFree(sortingEntity.selfEntityId, spousePosition, entityPositions)) {
//////                            // todo: this should be checking min distance not free
//////                            spousePosition[0] = spousePosition[0] + xPadding;
//////                            System.out.println("move spouse right: " + selfEntityId);
//////                        }
//////                        entityPositions.put(sortingEntity.selfEntityId, spousePosition);
//                    }
//                }
            while (!positionIsFree(selfEntityId, calculatedPosition, entityPositions)) {
                // todo: this should be checking min distance not free
                // todo: this should be sorting by need for position (eg spouse with no parents needs to be next to while a spouse with parents needs to be below the parents) and then by age
                calculatedPosition.setLocation(calculatedPosition.x + graphSorter.xPadding, calculatedPosition.y);
//                    System.out.println("move right: " + selfEntityId.getAttributeIdentifier());
                addLabel(":R");
            }
//                System.out.println("Insert: " + selfEntityId + " : " + calculatedPosition[0] + " : " + calculatedPosition[1]);
            entityPositions.put(selfEntityId, calculatedPosition);
            addLabel("FinalX:" + calculatedPosition.x);
        }
//            System.out.println("Position: " + selfEntityId.getAttributeIdentifier() + " : " + calculatedPosition[0] + " : " + calculatedPosition[1]);
//            float[] debugArray = entityPositions.get("Charles II of Spain");
//            if (debugArray != null) {
//                System.out.println("Charles II of Spain: " + debugArray[0] + " : " + debugArray[1]);
//            }
        return calculatedPosition;
    }

    protected void getRelatedPositions(HashMap<UniqueIdentifier, KinPoint> entityPositions) throws UnsortablePointsException {
        ArrayList<SortingEntity> allRelations = new ArrayList<SortingEntity>();
        allRelations.addAll(mustBeBelow);
        allRelations.add(this);
        allRelations.addAll(couldBeNextTo);
        allRelations.addAll(mustBeNextTo); // those that are in mustBeNextTo are also in couldBeNextTo
        allRelations.addAll(mustBeAbove);
        for (SortingEntity sortingEntity : allRelations) {
            if (sortingEntity.calculatedPosition == null) {
                sortingEntity.addLabel("RelatedPositions");
                // make sure the parent entity has its polstion calculated
                for (SortingEntity sortingEntityInner : sortingEntity.mustBeBelow) {
                    if (sortingEntityInner.calculatedPosition == null) {
                        sortingEntityInner.addLabel("RelatedPositionsInner");
                        sortingEntityInner.getRelatedPositions(entityPositions);
                    }
                }
                sortingEntity.getPosition(entityPositions);
                sortingEntity.getRelatedPositions(entityPositions);
            }
        }
    }
}
