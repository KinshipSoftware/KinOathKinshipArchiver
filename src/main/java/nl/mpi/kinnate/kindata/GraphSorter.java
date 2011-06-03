package nl.mpi.kinnate.kindata;

import java.util.HashMap;
import javax.xml.bind.annotation.XmlElement;
import nl.mpi.kinnate.svg.GraphPanel;

/**
 *  Document   : GraphData
 *  Created on : Sep 11, 2010, 4:51:36 PM
 *  Author     : Peter Withers
 */
public class GraphSorter {

    @XmlElement(name = "Entity", namespace = "http://mpi.nl/tla/kin")
    protected EntityData[] graphDataNodeArray = new EntityData[]{};
    public int graphWidth;
    public int graphHeight;
//    , int hSpacing, int vSpacing
//

    public void setEntitys(EntityData[] graphDataNodeArrayLocal) {
        graphDataNodeArray = graphDataNodeArrayLocal;
//        sanguineSort();
        //printLocations(); // todo: remove this and maybe add a label of x,y post for each node to better see the sorting
    }

    public void placeAllNodes(GraphPanel graphPanel, EntityData[] allEntitys, HashMap<String, Float[]> entityPositions) {
        // make a has table of all entites
        // find the first ego node
        // place it and all its immediate relatives onto the graph, each time checking that the space is free
        // contine to the next nearest relatives
        // when all done search for any unrelated nodes and do it all again
        // make sure that invisible nodes are ignored

        // store the max and min X Y so that the diagram size can be correctly specified
        for (EntityData currentNode : allEntitys) {
            if (currentNode.isVisible) {
                // loop through the filled locations and move to the right or left if not empty required
//            // todo: check the related nodes and average their positions then check to see if it is free and insert the node there
                boolean positionFree = false;
                float preferedX = 0;
                String currentIdentifier = currentNode.getUniqueIdentifier();
                Float[] storedPosition = entityPositions.get(currentIdentifier);
//                if (storedPosition == null) {
//                    storedPosition = new Float[]{preferedX, 0.0f};
//                }
                while (!positionFree) {
//                storedPosition = new Float[]{preferedX * hSpacing + hSpacing - symbolSize / 2.0f,
//                            currentNode.getyPos() * vSpacing + vSpacing - symbolSize / 2.0f};
//                if (entityPositions.isEmpty()) {
//                    break;
//                }
                    if (storedPosition != null) {
                        positionFree = true;
                        for (String comparedIdentifier : entityPositions.keySet()) {
                            if (!comparedIdentifier.equals(currentIdentifier)) {
                                Float[] currentPosition = entityPositions.get(comparedIdentifier);
                                positionFree = !currentPosition[0].equals(storedPosition[0]) || !currentPosition[1].equals(storedPosition[1]);
                                if (!positionFree) {
                                    break;
                                }
                            }
                        }
                        preferedX++;
                    }
                    if (!positionFree) {
                        storedPosition = new Float[]{preferedX, 0.0f};
                    }
                }
                entityPositions.put(currentIdentifier, storedPosition);
            }
        }
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
