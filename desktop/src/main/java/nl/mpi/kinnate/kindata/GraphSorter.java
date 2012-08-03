package nl.mpi.kinnate.kindata;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlElement;
import nl.mpi.kinnate.svg.GraphPanelSize;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 * Document : GraphData
 * Created on : Sep 11, 2010, 4:51:36 PM
 * Author : Peter Withers
 */
public class GraphSorter {

    static int sortCounter = 0; // for testing only
    @XmlElement(name = "Entity", namespace = "http://mpi.nl/tla/kin")
    private EntityData[] graphDataNodeArray = new EntityData[]{};
    private HashMap<UniqueIdentifier, SortingEntity> knownSortingEntities;
    private HashMap<UniqueIdentifier, Point> preferredLocations = new HashMap<UniqueIdentifier, Point>();
    // todo: should these padding vars be stored in the svg, currently they are stored
    private int xPadding = 100; // todo sort out one place for this var
    private int yPadding = 100; // todo sort out one place for this var
//    private boolean requiresRedraw = false;
//    , int hSpacing, int vSpacing
//

    public void setPadding(GraphPanelSize graphPanelSize) {
        xPadding = graphPanelSize.getHorizontalSpacing();
        yPadding = graphPanelSize.getVerticalSpacing();
    }

    public void setPreferredEntityLocation(UniqueIdentifier[] egoIdentifierArray, Point defaultLocation) {
        if (preferredLocations == null) {
            preferredLocations = new HashMap<UniqueIdentifier, Point>();
        }
        for (UniqueIdentifier uniqueIdentifier : egoIdentifierArray) {
            preferredLocations.put(uniqueIdentifier, defaultLocation);
        }
    }

    private Point getDefaultPosition(HashMap<UniqueIdentifier, Point> entityPositions, UniqueIdentifier uniqueIdentifier) {
        if (preferredLocations != null) {
            Point preferredPoint = preferredLocations.get(uniqueIdentifier);
            if (preferredPoint != null) {
                return preferredPoint;
            }
        }
        Rectangle rectangle = getGraphSize(entityPositions);
        Point defaultPosition = new Point(rectangle.width + xPadding, rectangle.height + yPadding);
//                            Point defaultPosition = new Point(rectangle.width, rectangle.height);
        return new Point(defaultPosition.x, 0);
//        return new Point(0, 0);
    }

    private class SortingEntity implements Comparable<SortingEntity> {

        UniqueIdentifier selfEntityId;
        ArrayList<SortingEntity> mustBeBelow;
        ArrayList<SortingEntity> mustBeAbove;
        ArrayList<SortingEntity> mustBeNextTo;
        ArrayList<SortingEntity> couldBeNextTo;
        EntityRelation[] allRelateNodes;
        Point calculatedPosition = null;
//        StringBuilder tempLabels = new StringBuilder(); // for testing only
        EntityData entityData; // for testing only

        public SortingEntity(EntityData entityData) {
            this.entityData = entityData; // for testing only
            this.entityData.clearTempLabels(); // for testing only
            sortCounter = 0; // for testing only
            selfEntityId = entityData.getUniqueIdentifier();
            allRelateNodes = entityData.getAllRelations();
            mustBeBelow = new ArrayList<SortingEntity>();
            mustBeAbove = new ArrayList<SortingEntity>();
            mustBeNextTo = new ArrayList<SortingEntity>();
            couldBeNextTo = new ArrayList<SortingEntity>();
        }

        public int compareTo(SortingEntity o) {
            return this.entityData.getDateOfBirth().compareTo(o.entityData.getDateOfBirth());
        }

        // for testing only
        private void addLabel(String labelString) {
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

        private boolean positionIsFree(UniqueIdentifier currentIdentifier, Point targetPosition, HashMap<UniqueIdentifier, Point> entityPositions) {
            int useCount = 0;
            for (Point currentPosition : entityPositions.values()) {
                if (currentPosition.x == targetPosition.x && currentPosition.y == targetPosition.y) {
                    useCount++;
                }
            }
            if (useCount == 0) {
                return true;
            }
            if (useCount == 1) {
                Point entityPosition = entityPositions.get(currentIdentifier);
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

        protected Point getPosition(HashMap<UniqueIdentifier, Point> entityPositions) {
//            System.out.println("getPosition: " + selfEntityId.getAttributeIdentifier());
            addLabel("Sorting:" + sortCounter++); // for testing only
            calculatedPosition = entityPositions.get(selfEntityId);
            if (calculatedPosition == null) {
                for (SortingEntity sortingEntity : mustBeBelow) {
                    // note that this get position also sets the position and the result will not be null
//                    float[] nextAbovePos = sortingEntity.getPosition(entityPositions, defaultPosition);
                    // note that this does not set the position and the result can be null
                    Point nextAbovePos = entityPositions.get(sortingEntity.selfEntityId);
                    if (nextAbovePos != null) {
                        if (calculatedPosition == null) {
                            // calculate the parent average position
                            float averageX = 0;
                            for (SortingEntity sortingEntityInner : mustBeBelow) {
                                averageX = averageX + entityPositions.get(sortingEntityInner.selfEntityId).x;
                            }
                            averageX = averageX / mustBeBelow.size();
                            // offset by the number of siblings                             
//                            Set<SortingEntity> intersection = new HashSet<SortingEntity>(mustBeAbove);
//                            intersection.retainAll(couldBeNextTo);
//                            averageX = averageX - xPadding * intersection.size() / 2;
                            averageX = averageX - xPadding * couldBeNextTo.size() / 2;
                            calculatedPosition = new Point((int) averageX, nextAbovePos.y);
                            addLabel(":mustBeBelow");
                        }
                        if (nextAbovePos.y > calculatedPosition.y - yPadding) {
                            calculatedPosition.setLocation(calculatedPosition.x, nextAbovePos.y + yPadding);
//                        calculatedPosition[0] = nextAbovePos[0];
//                            System.out.println("move down: " + selfEntityId.getAttributeIdentifier());
                            addLabel(":D");
                        }
                        break;
                    }
                }
                if (calculatedPosition == null) {
                    for (SortingEntity sortingEntity : couldBeNextTo) {
                        // note that this does not set the position and the result can be null
                        Point nextToPos = entityPositions.get(sortingEntity.selfEntityId);
                        if (calculatedPosition == null && nextToPos != null) {
                            calculatedPosition = new Point(nextToPos.x, nextToPos.y);
                            addLabel(":couldBeNextTo");
                            break;
                        }
                    }
                }
                if (calculatedPosition == null) {
                    for (SortingEntity sortingEntity : mustBeAbove) {
                        // note that this does not set the position and the result can be null
                        Point nextBelowPos = entityPositions.get(sortingEntity.selfEntityId);
                        if (nextBelowPos != null) {
                            // offset by the number of children
                            float averageX = nextBelowPos.x + xPadding * (mustBeAbove.size() - 1) / 2.0f;
                            if (calculatedPosition == null) {
                                calculatedPosition = new Point((int) averageX, nextBelowPos.y);
                                addLabel(":mustBeAbove");
                            }
                            if (nextBelowPos.y < calculatedPosition.y + yPadding) {
                                calculatedPosition.setLocation(calculatedPosition.x, nextBelowPos.y - yPadding);
//                        calculatedPosition[0] = nextAbovePos[0];
//                                System.out.println("move up: " + selfEntityId.getAttributeIdentifier());
                                addLabel(":U");
                            }
                            break;
                        }
                    }
                }
                if (calculatedPosition == null) {
                    calculatedPosition = getDefaultPosition(entityPositions, selfEntityId);
                    addLabel(":defaultPosition");
                }
                // make sure any spouses are in the same row
                // todo: this should probably be moved into a separate action and when a move is made then move in sequence the entities that are below and to the right
                // todo: mustBeNextTo could be sorted first 
                for (SortingEntity sortingEntity : mustBeNextTo) {
                    Point nextToPos = entityPositions.get(sortingEntity.selfEntityId);
                    if (nextToPos != null) {
                        if (nextToPos.y > calculatedPosition.y) {
                            calculatedPosition = new Point(nextToPos.x, nextToPos.y);
                            addLabel(":mustBeNextTo");
                        }


//                    Point nextToPos = entityPositions.get(sortingEntity.selfEntityId);
//                    if (nextToPos != null) {
//                        if (nextToPos.y > calculatedPosition.y) {
//                            calculatedPosition = new Point(nextToPos.x, nextToPos.y);
//                            addLabel(":mustBeNextTo");
//                        }
////                    } else {
////                        // prepopulate the spouse position
////                        float[] spousePosition = new float[]{calculatedPosition[0], calculatedPosition[1]};
////                        while (!positionIsFree(sortingEntity.selfEntityId, spousePosition, entityPositions)) {
////                            // todo: this should be checking min distance not free
////                            spousePosition[0] = spousePosition[0] + xPadding;
////                            System.out.println("move spouse right: " + selfEntityId);
////                        }
////                        entityPositions.put(sortingEntity.selfEntityId, spousePosition);
                    }
                }
                while (!positionIsFree(selfEntityId, calculatedPosition, entityPositions)) {
                    // todo: this should be checking min distance not free
                    // todo: this should be sorting by need for position (eg spouse with no parents needs to be next to while a spouse with parents needs to be below the parents) and then by age
                    calculatedPosition.setLocation(calculatedPosition.x + xPadding, calculatedPosition.y);
//                    System.out.println("move right: " + selfEntityId.getAttributeIdentifier());
                    addLabel(":R");
                }
//                System.out.println("Insert: " + selfEntityId + " : " + calculatedPosition[0] + " : " + calculatedPosition[1]);
                entityPositions.put(selfEntityId, calculatedPosition);
            }
//            System.out.println("Position: " + selfEntityId.getAttributeIdentifier() + " : " + calculatedPosition[0] + " : " + calculatedPosition[1]);
//            float[] debugArray = entityPositions.get("Charles II of Spain");
//            if (debugArray != null) {
//                System.out.println("Charles II of Spain: " + debugArray[0] + " : " + debugArray[1]);
//            }
            return calculatedPosition;
        }

        protected void getRelatedPositions(HashMap<UniqueIdentifier, Point> entityPositions) {
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

    public void setEntitys(EntityData[] graphDataNodeArrayLocal) {
        graphDataNodeArray = graphDataNodeArrayLocal;

        // this section need only be done when the nodes are added to this graphsorter
        knownSortingEntities = new HashMap<UniqueIdentifier, SortingEntity>();
        for (EntityData currentNode : graphDataNodeArrayLocal) {
            if (currentNode.isVisible) {
                // only create sorting entities for visible entities
                knownSortingEntities.put(currentNode.getUniqueIdentifier(), new SortingEntity(currentNode));
            }
        }
        for (SortingEntity currentSorter : knownSortingEntities.values()) {
            currentSorter.calculateRelations(knownSortingEntities);
        }
//        sanguineSort();
        //printLocations(); // todo: remove this and maybe add a label of x,y post for each node to better see the sorting
    }

//    public boolean isResizeRequired() {
//        boolean returnBool = requiresRedraw;
//        requiresRedraw = false;
//        return returnBool;
//    }
//    private void placeRelatives(EntityData currentNode, ArrayList<EntityData> intendedSortOrder, HashMap<String, Float[]> entityPositions) {
//        for (EntityRelation entityRelation : currentNode.getVisiblyRelateNodes()) {
//            EntityData relatedEntity = entityRelation.getAlterNode();
//
//        }
//    }
    public Rectangle getGraphSize(HashMap<UniqueIdentifier, Point> entityPositions) {
        // get min positions
        // this should also take into account any graphics such as labels, although the border provided should be adequate, in other situations the page size could be set, in which case maybe an align option would be helpful
        int[] minPostion = null;
        int[] maxPostion = null;
        for (Point currentPosition : entityPositions.values()) {
            if (minPostion == null) {
                minPostion = new int[]{Math.round(currentPosition.x), Math.round(currentPosition.y)};
                maxPostion = new int[]{Math.round(currentPosition.x), Math.round(currentPosition.y)};
            } else {
                minPostion[0] = Math.min(minPostion[0], Math.round(currentPosition.x));
                minPostion[1] = Math.min(minPostion[1], Math.round(currentPosition.y));
                maxPostion[0] = Math.max(maxPostion[0], Math.round(currentPosition.x));
                maxPostion[1] = Math.max(maxPostion[1], Math.round(currentPosition.y));
            }
        }
        if (minPostion == null) {
            // when there are no entities this could be null and must be corrected here
            minPostion = new int[]{0, 0};
            maxPostion = new int[]{0, 0};
        }
        int xOffset = minPostion[0] - xPadding;
        int yOffset = minPostion[1] - yPadding;
        int graphWidth = maxPostion[0] + xPadding;
        int graphHeight = maxPostion[1] + yPadding;
        return new Rectangle(xOffset, yOffset, graphWidth, graphHeight);
    }

    public void placeAllNodes(HashMap<UniqueIdentifier, Point> entityPositions) {
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
//    // todo: look into http://www.jgraph.com/jgraph.html
//    // todo: and http://books.google.nl/books?id=diqHjRjMhW0C&pg=PA138&lpg=PA138&dq=SVGDOMImplementation+add+namespace&source=bl&ots=IuqzAz7dsz&sig=e5FW_B1bQbhnth6i2rifalv2LuQ&hl=nl&ei=zYpnTYD3E4KVOuPF2YoL&sa=X&oi=book_result&ct=result&resnum=3&ved=0CC0Q6AEwAg#v=onepage&q&f=false
//    // page 139 shows jgraph layout usage
//    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/hierarchical/JGraphHierarchicalLayout.html
//    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/JGraphLayout.html
//    // http://www.jgraph.com/doc/jgraph/com/jgraph/layout/JGraphFacade.html
//    // http://www.jgraph.com/doc/jgraph/org/jgraph/graph/GraphModel.html
//    // and maybe http://www.jgraph.com/doc/jgraph/org/jgraph/graph/GraphLayoutCache.html
//    // todo: lookinto:
////                    layouts.put("Hierarchical", new JGraphHierarchicalLayout());
////                layouts.put("Compound", new JGraphCompoundLayout());
////                layouts.put("CompactTree", new JGraphCompactTreeLayout());
////                layouts.put("Tree", new JGraphTreeLayout());
////                layouts.put("RadialTree", new JGraphRadialTreeLayout());
////                layouts.put("Organic", new JGraphOrganicLayout());
////                layouts.put("FastOrganic", new JGraphFastOrganicLayout());
////                layouts.put("SelfOrganizingOrganic", new JGraphSelfOrganizingOrganicLayout());
////                layouts.put("SimpleCircle", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE));
////                layouts.put("SimpleTilt", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_TILT));
////                layouts.put("SimpleRandom", new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_RANDOM));
////                layouts.put("Spring", new JGraphSpringLayout());
////                layouts.put("Grid", new SimpleGridLayout());

    public EntityData[] getDataNodes() {
        return graphDataNodeArray;
    }
}
