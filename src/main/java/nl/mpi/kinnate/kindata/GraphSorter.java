package nl.mpi.kinnate.kindata;

import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlElement;
import nl.mpi.kinnate.svg.GraphPanel;
import nl.mpi.kinnate.svg.GraphPanelSize;

/**
 *  Document   : GraphData
 *  Created on : Sep 11, 2010, 4:51:36 PM
 *  Author     : Peter Withers
 */
public class GraphSorter {

    @XmlElement(name = "Entity", namespace = "http://mpi.nl/tla/kin")
    private EntityData[] graphDataNodeArray = new EntityData[]{};
    HashMap<String, SortingEntity> knownSortingEntities;
    public int xPadding = 100; // todo sort out one place for this var
    public int yPadding = 100; // todo sort out one place for this var
//    private boolean requiresRedraw = false;
//    , int hSpacing, int vSpacing
//

    public void setPadding(GraphPanelSize graphPanelSize) {
        xPadding = graphPanelSize.getHorizontalSpacing();
        yPadding = graphPanelSize.getVerticalSpacing();
    }

    private class SortingEntity {

        String selfEntityId;
        ArrayList<SortingEntity> mustBeBelow;
        ArrayList<SortingEntity> mustBeAbove;
        ArrayList<SortingEntity> mustBeNextTo;
        ArrayList<SortingEntity> couldBeNextTo;
        EntityRelation[] visiblyRelateNodes;
        float[] calculatedPosition = null;

        public SortingEntity(EntityData entityData) {
            selfEntityId = entityData.getUniqueIdentifier();
            visiblyRelateNodes = entityData.getVisiblyRelateNodes();
            mustBeBelow = new ArrayList<SortingEntity>();
            mustBeAbove = new ArrayList<SortingEntity>();
            mustBeNextTo = new ArrayList<SortingEntity>();
            couldBeNextTo = new ArrayList<SortingEntity>();
        }

        public void calculateRelations(HashMap<String, SortingEntity> knownSortingEntities) {
            for (EntityRelation entityRelation : visiblyRelateNodes) {
                switch (entityRelation.relationType) {
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

        private boolean positionIsFree(String currentIdentifier, float[] targetPosition, HashMap<String, float[]> entityPositions) {
            int useCount = 0;
            for (float[] currentPosition : entityPositions.values()) {
                if (currentPosition[0] == targetPosition[0] && currentPosition[1] == targetPosition[1]) {
                    useCount++;
                }
            }
            if (useCount == 0) {
                return true;
            }
            if (useCount == 1) {
                float[] entityPosition = entityPositions.get(currentIdentifier);
                if (entityPosition != null) {
                    // todo: change this to compare distance not exact location
                    if (entityPosition[0] == targetPosition[0] && entityPosition[1] == targetPosition[1]) {
                        // if there is one entity already in this position then check if it is the current entity, in which case it is free
                        return true;
                    }
                }
            }
            return false;
        }

        protected float[] getPosition(HashMap<String, float[]> entityPositions) {
            System.out.println("getPosition: " + selfEntityId);
            calculatedPosition = entityPositions.get(selfEntityId);
            if (calculatedPosition == null) {
                for (SortingEntity sortingEntity : mustBeBelow) {
                    float[] nextAbovePos = sortingEntity.getPosition(entityPositions);
                    if (calculatedPosition == null) {
                        calculatedPosition = new float[]{nextAbovePos[0], nextAbovePos[1]};
                    }
                    if (nextAbovePos[1] > calculatedPosition[1] - yPadding) {
                        calculatedPosition[1] = nextAbovePos[1] + yPadding;
//                        calculatedPosition[0] = nextAbovePos[0];
                        System.out.println("move down: " + selfEntityId);
                    }
                }
                if (calculatedPosition == null) {
                    for (SortingEntity sortingEntity : couldBeNextTo) {
                        float[] nextToPos = sortingEntity.getPosition(entityPositions);
                        if (calculatedPosition == null) {
                            calculatedPosition = new float[]{nextToPos[0], nextToPos[1]};
                        }
                    }
                }
                if (calculatedPosition == null) {
                    float[] graphSize = getGraphSize(entityPositions);
                    calculatedPosition = new float[]{0.0f, graphSize[1]};
                }
                // make sure any spouses are in the same row
                // todo: this should probably be moved into a separate action and when a move is made then move in sequence the entities that are below and to the right
                for (SortingEntity sortingEntity : mustBeNextTo) {
                    float[] nextToPos = entityPositions.get(sortingEntity.selfEntityId);
                    if (nextToPos != null) {
                        if (nextToPos[1] > calculatedPosition[1]) {
                            calculatedPosition = new float[]{nextToPos[0], nextToPos[1]};
                        }
//                    } else {
//                        // prepopulate the spouse position
//                        float[] spousePosition = new float[]{calculatedPosition[0], calculatedPosition[1]};
//                        while (!positionIsFree(sortingEntity.selfEntityId, spousePosition, entityPositions)) {
//                            // todo: this should be checking min distance not free
//                            spousePosition[0] = spousePosition[0] + xPadding;
//                            System.out.println("move spouse right: " + selfEntityId);
//                        }
//                        entityPositions.put(sortingEntity.selfEntityId, spousePosition);
                    }
                }
                while (!positionIsFree(selfEntityId, calculatedPosition, entityPositions)) {
                    // todo: this should be checking min distance not free
                    calculatedPosition[0] = calculatedPosition[0] + xPadding;
                    System.out.println("move right: " + selfEntityId);
                }
                entityPositions.put(selfEntityId, calculatedPosition);
            }
            return calculatedPosition;
        }

        protected void getRelatedPositions(HashMap<String, float[]> entityPositions) {
            ArrayList<SortingEntity> allRelations = new ArrayList<SortingEntity>();
            allRelations.addAll(mustBeAbove);
            allRelations.addAll(mustBeBelow);
//            allRelations.addAll(mustBeNextTo); // those that are in mustBeNextTo are also in couldBeNextTo
            allRelations.addAll(couldBeNextTo);
            for (SortingEntity sortingEntity : allRelations) {
                if (sortingEntity.calculatedPosition == null) {
                    sortingEntity.getPosition(entityPositions);
                    sortingEntity.getRelatedPositions(entityPositions);
                }
            }
        }
    }

    public void setEntitys(EntityData[] graphDataNodeArrayLocal) {
        graphDataNodeArray = graphDataNodeArrayLocal;

        // this section need only be done when the nodes are added to this graphsorter
        knownSortingEntities = new HashMap<String, SortingEntity>();
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
    public float[] getGraphSize(HashMap<String, float[]> entityPositions) {
        // get max positions
        float graphWidth = 0;
        float graphHeight = 0;
        for (float[] currentPosition : entityPositions.values()) {
            if (graphWidth < currentPosition[0]) {
                graphWidth = currentPosition[0];
            }
            if (graphHeight < currentPosition[1]) {
                graphHeight = currentPosition[1];
            }
        }
        // make sure there is some padding on the right and bottom
        graphWidth += xPadding * 2;
        graphHeight += yPadding * 2;
        return new float[]{graphWidth, graphHeight};
    }

    public void placeAllNodes(GraphPanel graphPanel, HashMap<String, float[]> entityPositions) {
        // make a has table of all entites
        // find the first ego node
        // place it and all its immediate relatives onto the graph, each time checking that the space is free
        // contine to the next nearest relatives
        // when all done search for any unrelated nodes and do it all again
        // make sure that invisible nodes are ignored
        for (EntityData currentNode : graphDataNodeArray) {
            if (!currentNode.isVisible) {
                entityPositions.remove(currentNode.getUniqueIdentifier());
            }
        }
        for (SortingEntity currentSorter : knownSortingEntities.values()) {
            currentSorter.getPosition(entityPositions);
            currentSorter.getRelatedPositions(entityPositions);
        }
// get min positions
        // this must also take into account any graphics such as labels
        float[] minPostion = null;
        for (float[] currentPosition : entityPositions.values()) {
            if (minPostion == null) {
                minPostion = currentPosition;
            } else {
                if (minPostion[0] > currentPosition[0]) {
                    minPostion[0] = currentPosition[0];
                }
                if (minPostion[1] > currentPosition[1]) {
                    minPostion[1] = currentPosition[1];
                }
            }
        }
        if (minPostion == null) {
            // when there are no entities this could be null and must be corrected here
            minPostion = new float[]{0, 0};
        }
        // adjust the min position
        // todo: this requires that the svg is updated to match this change on all nodes (to test drag one node left past zero, which causes all other nodes to be moved, then move another node and the relation lines do not get placed correctly)
        float xOffset = xPadding - minPostion[0];
        float yOffset = yPadding - minPostion[1];
//        requiresRedraw = (yOffset != 0 || xOffset != 0);
        for (float[] currentPosition : entityPositions.values()) {
            currentPosition[0] = currentPosition[0] + xOffset;
            currentPosition[1] = currentPosition[1] + yOffset;
        }
//        ArrayList<EntityData> intendedSortOrder = new ArrayList<EntityData>();
////        ArrayList<EntityData> placedEntities = new ArrayList<EntityData>();
//        for (EntityData currentNode : allEntitys) {
//            if (currentNode.isVisible && entityPositions.containsKey(currentNode.getUniqueIdentifier())) {
//                // add all the placed entities first in the list
//                intendedSortOrder.add(currentNode);
//            }
//        }
//        boolean nodesNeedPlacement = false;
//        for (EntityData currentNode : allEntitys) {
//            if (currentNode.isVisible && !intendedSortOrder.contains(currentNode)) {
//                intendedSortOrder.add(currentNode);
//                nodesNeedPlacement = true;
//            }
//        }
//        if (!nodesNeedPlacement) {
//            return;
//        }
//        // store the max and min X Y so that the diagram size can be correctly specified
//        while (!intendedSortOrder.isEmpty()) {
//            EntityData currentNode = intendedSortOrder.remove(0);
////            placedEntities.add(currentNode);
//            if (currentNode.isVisible) {
//                // loop through the filled locations and move to the right or left if not empty required
////            // todo: check the related nodes and average their positions then check to see if it is free and insert the node there
//                boolean positionFree = false;
//                float preferedX = 0;
//                String currentIdentifier = currentNode.getUniqueIdentifier();
//                Float[] storedPosition = entityPositions.get(currentIdentifier);
////                if (storedPosition == null) {
////                    storedPosition = new Float[]{preferedX, 0.0f};
////                }
//                while (!positionFree) {
////                storedPosition = new Float[]{preferedX * hSpacing + hSpacing - symbolSize / 2.0f,
////                            currentNode.getyPos() * vSpacing + vSpacing - symbolSize / 2.0f};
////                if (entityPositions.isEmpty()) {
////                    break;
////                }
//                    if (storedPosition != null) {
//                        positionFree = true;
//                        for (String comparedIdentifier : entityPositions.keySet()) {
//                            if (!comparedIdentifier.equals(currentIdentifier)) {
//                                Float[] currentPosition = entityPositions.get(comparedIdentifier);
//                                positionFree = !currentPosition[0].equals(storedPosition[0]) || !currentPosition[1].equals(storedPosition[1]);
//                                if (!positionFree) {
//                                    break;
//                                }
//                            }
//                        }
//                        preferedX++;
//                    }
//                    if (!positionFree) {
//                        storedPosition = new Float[]{preferedX, 0.0f};
//                    }
//                }
//                entityPositions.put(currentIdentifier, storedPosition);
//            }
//        }
    }
//
////    public int[] getEntityLocation(String entityId) {
////        for (EntityData entityData : graphDataNodeArray) {
////            if (entityData.getUniqueIdentifier().equals(entityId)) {
////                return new int[]{entityData.xPos, entityData.yPos};
////            }
////        }
////        return null;
////    }
////
////    public void setEntityLocation(String entityId, int xPos, int yPos) {
////        for (EntityData entityData : graphDataNodeArray) {
////            if (entityData.getUniqueIdentifier().equals(entityId)) {
////                entityData.xPos = xPos;
////                entityData.yPos = yPos;
////                return;
////            }
////        }
////    }
//
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
//    private void sanguineSubnodeSort(ArrayList<HashSet<EntityData>> generationRows, HashSet<EntityData> currentColumns, ArrayList<EntityData> inputNodes, EntityData currentNode) {
//        int currentRowIndex = generationRows.indexOf(currentColumns);
//        HashSet<EntityData> ancestorColumns;
//        HashSet<EntityData> descendentColumns;
//        if (currentRowIndex < generationRows.size() - 1) {
//            descendentColumns = generationRows.get(currentRowIndex + 1);
//        } else {
//            descendentColumns = new HashSet<EntityData>();
//            generationRows.add(currentRowIndex + 1, descendentColumns);
//        }
//        if (currentRowIndex > 0) {
//            ancestorColumns = generationRows.get(currentRowIndex - 1);
//        } else {
//            ancestorColumns = new HashSet<EntityData>();
//            generationRows.add(currentRowIndex, ancestorColumns);
//        }
//        for (EntityRelation relatedNode : currentNode.getVisiblyRelateNodes()) { // todo: here we are soriting only visible nodes, sorting invisible nodes as well might cause issues or might help the layout and this must be tested
//            // todo: what happens here if there are multiple relations specified?
//            if (/*relatedNode.getAlterNode().isVisible &&*/inputNodes.contains(relatedNode.getAlterNode())) {
//                HashSet<EntityData> targetColumns;
//                switch (relatedNode.relationType) {
//                    case ancestor:
//                        targetColumns = ancestorColumns;
//                        break;
//                    case sibling:
//                        targetColumns = currentColumns;
//                        break;
//                    case descendant:
//                        targetColumns = descendentColumns;
//                        break;
//                    case union:
//                        targetColumns = currentColumns;
//                        break;
//                    case none:
//                        // this would be a kin term or other so skip when sorting
//                        targetColumns = null;
//                        break;
//                    default:
//                        targetColumns = null;
//                }
//                if (targetColumns != null) {
//                    inputNodes.remove(relatedNode.getAlterNode());
//                    targetColumns.add(relatedNode.getAlterNode());
////                    System.out.println("sorted: " + relatedNode.getAlterNode().getLabel() + " : " + relatedNode.relationType + " of " + currentNode.getLabel());
//                    sanguineSubnodeSort(generationRows, targetColumns, inputNodes, relatedNode.getAlterNode());
//                }
//            }
//        }
//    }
//
//    protected void sanguineSort() {
//        // todo: improve this sorting by adding a secondary row sort
//        System.out.println("calculateLocations");
//        // create an array of rows
//        ArrayList<HashSet<EntityData>> generationRows = new ArrayList<HashSet<EntityData>>();
//        ArrayList<EntityData> inputNodes = new ArrayList<EntityData>();
//        inputNodes.addAll(Arrays.asList(graphDataNodeArray));
//        // put an array of columns into the current row
//        HashSet<EntityData> currentColumns = new HashSet<EntityData>();
//        generationRows.add(currentColumns);
//
//        while (inputNodes.size() > 0) { // this loop checks all nodes provided for display, but the sanguineSubnodeSort will remove any related nodes before we return to this loop, so this loop would only run once if all nodes are related
//            EntityData currentNode = inputNodes.remove(0);
////            System.out.println("add as root node: " + currentNode.getLabel());
////            if (currentNode.isVisible) {
//            currentColumns.add(currentNode);
//            sanguineSubnodeSort(generationRows, currentColumns, inputNodes, currentNode);
////            }
//        }
//        gridWidth = 0;
//        int yPos = 0;
//        for (HashSet<EntityData> currentRow : generationRows) {
//            System.out.println("row: : " + yPos);
//            if (currentRow.isEmpty()) {
//                System.out.println("Skipping empty row");
//            } else {
//                int xPos = 0;
//                if (gridWidth < currentRow.size()) {
//                    gridWidth = currentRow.size();
//                }
//                for (EntityData graphDataNode : currentRow) {
////                    System.out.println("updating: " + xPos + " : " + yPos + " : " + graphDataNode.getLabel());
//                    graphDataNode.yPos = yPos;
//                    graphDataNode.xPos = xPos;
//                    //graphDataNode.appendTempLabel("X:" + xPos + " Y:" + yPos);
//                    xPos++;
//                }
//                yPos++;
//            }
//        }
//        gridHeight = yPos;
//        int maxRowWidth = 0;
//        // correct the grid width
//        for (HashSet<EntityData> currentRow : generationRows) {
//            if (maxRowWidth < currentRow.size()) {
//                maxRowWidth = currentRow.size();
//            }
//        }
//        gridWidth = maxRowWidth;
//        System.out.println("gridWidth: " + gridWidth);
////        sortRowsByAncestor(generationRows);
//        sortByLinkDistance();
//        sortByLinkDistance();
//    }
//
//    private void sortRowsByAncestor(ArrayList<HashSet<EntityData>> generationRows) {
//        // todo: handle reverse generations also
//        ArrayList<EntityData> sortedRow = new ArrayList<EntityData>();
//        int startRow = 0;
//        while (generationRows.get(startRow).isEmpty()) {
//            startRow++;
//        }
//        HashSet<EntityData> firstRow = generationRows.get(startRow);
//        for (EntityData currentEntity : firstRow) {
//            if (!sortedRow.contains(currentEntity)) { // if the entity has been added then do not look into any further
//                sortedRow.add(currentEntity);
//                // if this node has children in common with any other on this row then place them next to each other
//                // todo: add sort by DOB
//                for (EntityData contemporariesEntity : findContemporariesWithCommonDescendant(currentEntity, 2)) {
//                    if (!sortedRow.contains(contemporariesEntity)) {
//                        sortedRow.add(contemporariesEntity);
//                    }
//                }
//            }
//        }
//        while (sortedRow.size() > 0) {
//            assignRowOrder(sortedRow);
//            ArrayList<EntityData> nextRow = new ArrayList<EntityData>();
//            for (EntityData currentEntity : sortedRow) {
//                for (EntityRelation childRelation : currentEntity.getDistinctRelateNodes()) {
//                    if (childRelation.getAlterNode().yPos == currentEntity.yPos + 1) {
//                        if (!nextRow.contains(childRelation.getAlterNode())) {
//                            nextRow.add(childRelation.getAlterNode());
//                        }
//                    }
//                }
//            }
//            sortedRow = nextRow;
//        }
//    }
//
//    private void assignRowOrder(ArrayList<EntityData> sortedRow) {
//        int columnCount = 0;
//        for (EntityData currentEntity : sortedRow) {
//            // todo: space the nodes
//            currentEntity.xPos = columnCount;
//            columnCount++;
//            System.out.println("sorted: " + currentEntity.getLabel()[0] + " : " + currentEntity.xPos + "," + currentEntity.yPos);
//        }
//    }
//
//    private HashSet<EntityData> findContemporariesWithCommonDescendant(EntityData currentEntity, int depth) {
//        HashSet<EntityData> foundContemporaries = new HashSet<EntityData>();
//        for (EntityRelation childRelation : currentEntity.getDistinctRelateNodes()) {
//            if (childRelation.getAlterNode().yPos == currentEntity.yPos) {
//                foundContemporaries.add(childRelation.getAlterNode());
//            } else {
//                depth--;
//                if (depth > 0) {
//                    foundContemporaries.addAll(findContemporariesWithCommonDescendant(currentEntity, depth));
//                }
//            }
//        }
//        return foundContemporaries;
//    }
//
//    private void sortByLinkDistance() {
//        // todo: correct the grid width,
//        // start at the top row and count the childeren of each parent and space accordingly
//        EntityData[][] graphGrid = new EntityData[gridHeight][gridWidth];
//        for (EntityData graphDataNode : graphDataNodeArray) {
//            graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
//        }
//        for (EntityData graphDataNode : graphDataNodeArray) {
//            int relationCounter = 0;
//            int totalPositionCounter = 0;
//            for (EntityRelation graphLinkNode : graphDataNode.getVisiblyRelateNodes()) {
//                relationCounter++;
//                totalPositionCounter += graphLinkNode.getAlterNode().xPos;
//                //totalPositionCounter += Math.abs(graphLinkNode.linkedNode.xPos - graphLinkNode.sourceNode.xPos);
////                totalPositionCounter += Math.abs(graphLinkNode.linkedNode.xPos - graphLinkNode.sourceNode.xPos);
//                System.out.println("link: " + graphLinkNode.getAlterNode().xPos + ":" + graphLinkNode.getAlterNode().xPos);
//                System.out.println("totalPositionCounter: " + totalPositionCounter);
//            }
//            if (relationCounter > 0) {
//                int averagePosition = totalPositionCounter / relationCounter;
//                while (averagePosition < gridWidth - 1 && graphGrid[graphDataNode.yPos][averagePosition] != null) {
//                    averagePosition++;
//                }
//                while (averagePosition > 0 && graphGrid[graphDataNode.yPos][averagePosition] != null) {
//                    averagePosition--;
//                }
//                if (graphGrid[graphDataNode.yPos][averagePosition] == null) {
//                    graphGrid[graphDataNode.yPos][graphDataNode.xPos] = null;
//                    graphDataNode.xPos = averagePosition; // todo: swap what ever is aready there
//                    graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
//                }
//                System.out.println("averagePosition: " + averagePosition);
//            }
//        }
//    }
//

    public EntityData[] getDataNodes() {
        return graphDataNodeArray;
    }
//
//    private void printLocations() {
//        System.out.println("printLocations");
//        for (EntityData graphDataNode : graphDataNodeArray) {
//            System.out.println("node: " + graphDataNode.xPos + ":" + graphDataNode.yPos);
//            for (EntityRelation graphLinkNode : graphDataNode.getVisiblyRelateNodes()) {
//                System.out.println("link: " + graphLinkNode.getAlterNode().xPos + ":" + graphLinkNode.getAlterNode().yPos);
//            }
//        }
//    }
}
