package nl.mpi.kinnate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 *  Document   : GraphData
 *  Created on : Sep 11, 2010, 4:51:36 PM
 *  Author     : Peter Withers
 */
public class GraphData {

    protected GraphDataNode[] graphDataNodeArray = new GraphDataNode[]{};
    public int gridWidth;
    public int gridHeight;

//    public void setEgoNodes(String[] treeNodesArray) {
//        if (treeNodesArray != null) {
//            graphDataNodeList = new HashMap<String, GraphDataNode>();
//            for (String currentNodeString : treeNodesArray) {
//                try {
//                    ImdiTreeObject imdiTreeObject = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString));
//                    graphDataNodeList.put(imdiTreeObject.getUrlString(), new GraphDataNode(imdiTreeObject));
//                } catch (URISyntaxException exception) {
//                    System.err.println(exception.getMessage());
//                    exception.printStackTrace();
//                }
//            }
//        }
//        calculateLinks();
//        sanguineSort();
//        printLocations();
//    }
    public void setEgoNodes(GraphDataNode[] graphDataNodeArrayLocal) {
        graphDataNodeArray = graphDataNodeArrayLocal;
//        if (graphDataNodeArray != null) {
//            graphDataNodeList = new HashMap<String, GraphDataNode>();
//            for (URI entityUri : treeNodesArray) {
//                graphDataNodeList.put(entityUri.toASCIIString(), new GraphDataNode(ImdiLoader.getSingleInstance().getImdiObject(null, entityUri)));
//            }
//        }
//        calculateLinks();
        sanguineSort();
        printLocations();
    }

//    public void readData() {
//        String[] treeNodesArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
//        if (treeNodesArray != null) {
//            for (String currentNodeString : treeNodesArray) {
//                try {
//                    ImdiTreeObject imdiTreeObject = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString));
//                    graphDataNodeList.put(imdiTreeObject.getUrlString(), new GraphDataNode(imdiTreeObject));
//                } catch (URISyntaxException exception) {
//                    System.err.println(exception.getMessage());
//                    exception.printStackTrace();
//                }
//            }
//        }
//        calculateLinks();
//        sanguineSort();
//        printLocations();
//    }
//    protected void calculateLinks() {
//        System.out.println("calculateLinks");
//        for (GraphDataNode currentNode : graphDataNodeList.values()) {
//            System.out.println("currentNode: " + currentNode.getLabel());
////            currentNode.calculateLinks(graphDataNodeList);
//        }
//    }
    private void sanguineSubnodeSort(ArrayList<HashSet<GraphDataNode>> generationRows, HashSet<GraphDataNode> currentColumns, ArrayList<GraphDataNode> inputNodes, GraphDataNode currentNode) {
        int currentRowIndex = generationRows.indexOf(currentColumns);
        HashSet<GraphDataNode> ancestorColumns;
        HashSet<GraphDataNode> descendentColumns;
        if (currentRowIndex < generationRows.size() - 1) {
            descendentColumns = generationRows.get(currentRowIndex + 1);
        } else {
            descendentColumns = new HashSet<GraphDataNode>();
            generationRows.add(currentRowIndex + 1, descendentColumns);
        }
        if (currentRowIndex > 0) {
            ancestorColumns = generationRows.get(currentRowIndex - 1);
        } else {
            ancestorColumns = new HashSet<GraphDataNode>();
            generationRows.add(currentRowIndex, ancestorColumns);
        }
        for (GraphDataNode.NodeRelation relatedNode : currentNode.getNodeRelations()) {
            if (relatedNode.sourceNode.equals(currentNode)) {
                if (inputNodes.contains(relatedNode.linkedNode)) {
                    HashSet<GraphDataNode> targetColumns = null;
                    switch (relatedNode.relationType) {
                        case ancestor:
                            targetColumns = ancestorColumns;
                            break;
                        case sibling:
                            targetColumns = currentColumns;
                            break;
                        case descendant:
                            targetColumns = descendentColumns;
                            break;
//                        case union:
//                            targetColumns = currentColumns;
//                            break;
                    }
                    inputNodes.remove(relatedNode.linkedNode);
                    targetColumns.add(relatedNode.linkedNode);
                    System.out.println("sorted: " + relatedNode.linkedNode.getLabel() + " : " + relatedNode.relationType + " of " + currentNode.getLabel());
                    sanguineSubnodeSort(generationRows, targetColumns, inputNodes, relatedNode.linkedNode);
                }
            }
        }
//        for (GraphDataNode reverceLinkNode : inputNodes.toArray(new GraphDataNode[]{})) {
//            for (GraphDataNode.NodeRelation relatedNode : reverceLinkNode.getNodeRelations()) {
        for (GraphDataNode.NodeRelation relatedNode : currentNode.getNodeRelations()) {
            if (!relatedNode.sourceNode.equals(currentNode)) {
                HashSet<GraphDataNode> targetColumns = null;
                switch (relatedNode.relationType) {
                    case ancestor:
                        targetColumns = descendentColumns;
                        break;
                    case sibling:
                        targetColumns = currentColumns;
                        break;
                    case descendant:
                        targetColumns = ancestorColumns;
                        break;
//                    case union:
//                        targetColumns = currentColumns;
//                        break;
                }
                inputNodes.remove(relatedNode.sourceNode);
                targetColumns.add(relatedNode.sourceNode);
                System.out.println("sorted: " + relatedNode.sourceNode.getLabel() + " : " + "reverse link" + " of " + currentNode.getLabel());
//                sanguineSubnodeSort(generationRows, targetColumns, inputNodes, relatedNode.sourceNode);
            }
        }
    }

    protected void sanguineSort() {
        System.out.println("calculateLocations");
        // create an array of rows
        ArrayList<HashSet<GraphDataNode>> generationRows = new ArrayList<HashSet<GraphDataNode>>();
        ArrayList<GraphDataNode> inputNodes = new ArrayList<GraphDataNode>();
        inputNodes.addAll(Arrays.asList(graphDataNodeArray));
        // put an array of columns into the current row
        HashSet<GraphDataNode> currentColumns = new HashSet<GraphDataNode>();
        generationRows.add(currentColumns);

        while (inputNodes.size() > 0) {
            GraphDataNode currentNode = inputNodes.remove(0);
            System.out.println("add as root node: " + currentNode.getLabel());
            currentColumns.add(currentNode);
            sanguineSubnodeSort(generationRows, currentColumns, inputNodes, currentNode);
        }
        gridWidth = 0;
        int yPos = 0;
        for (HashSet<GraphDataNode> currentRow : generationRows) {
            System.out.println("row: : " + yPos);
            if (currentRow.isEmpty()) {
                System.out.println("Skipping empty row");
            } else {
                int xPos = 0;
                if (gridWidth < currentRow.size()) {
                    gridWidth = currentRow.size();
                }
                for (GraphDataNode graphDataNode : currentRow) {
                    System.out.println("updating: " + xPos + " : " + yPos + " : " + graphDataNode.getLabel());
                    graphDataNode.yPos = yPos;
                    graphDataNode.xPos = xPos;
                    xPos++;
                }
                yPos++;
            }
        }
        System.out.println("gridWidth: " + gridWidth);
        gridHeight = yPos;
        sortByLinkDistance();
        sortByLinkDistance();
    }

    private void sortByLinkDistance() {
        GraphDataNode[][] graphGrid = new GraphDataNode[gridHeight][gridWidth];
        for (GraphDataNode graphDataNode : graphDataNodeArray) {
            graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
        }
        for (GraphDataNode graphDataNode : graphDataNodeArray) {
            int relationCounter = 0;
            int totalPositionCounter = 0;
            for (GraphDataNode.NodeRelation graphLinkNode : graphDataNode.getNodeRelations()) {
                relationCounter++;
                if (graphLinkNode.sourceNode.equals(graphDataNode)) {
                    totalPositionCounter += graphLinkNode.linkedNode.xPos;
                } else {
                    totalPositionCounter += graphLinkNode.sourceNode.xPos;
                }
                //totalPositionCounter += Math.abs(graphLinkNode.linkedNode.xPos - graphLinkNode.sourceNode.xPos);
//                totalPositionCounter += Math.abs(graphLinkNode.linkedNode.xPos - graphLinkNode.sourceNode.xPos);
                System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.sourceNode.xPos);
                System.out.println("totalPositionCounter: " + totalPositionCounter);
            }
            if (relationCounter > 0) {
                int averagePosition = totalPositionCounter / relationCounter;
                while (averagePosition < gridWidth - 1 && graphGrid[graphDataNode.yPos][averagePosition] != null) {
                    averagePosition++;
                }
                while (averagePosition > 0 && graphGrid[graphDataNode.yPos][averagePosition] != null) {
                    averagePosition--;
                }
                if (graphGrid[graphDataNode.yPos][averagePosition] == null) {
                    graphGrid[graphDataNode.yPos][graphDataNode.xPos] = null;
                    graphDataNode.xPos = averagePosition; // todo: swap what ever is aready there
                    graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
                }
                System.out.println("averagePosition: " + averagePosition);
            }
//            if (relationCounter > 0) {
//                int averagePosition = totalPositionCounter / relationCounter;
//                System.out.println("averagePosition: " + averagePosition);
//                if (graphGrid[graphDataNode.yPos][averagePosition] == null) {
//                    graphGrid[graphDataNode.yPos][graphDataNode.xPos] = null;
//                    graphDataNode.xPos = averagePosition; // todo: swap what ever is aready there
//                    graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
//                }
//            }
        }
    }
//
//    private void siblingSort() {
//        GraphDataNode[][] graphGrid = new GraphDataNode[gridHeight][gridWidth];
//        for (GraphDataNode graphDataNode : graphDataNodeList.values()) {
//            graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
//        }
//    }
//
//    private void calculateLinkDistance() {
//        GraphDataNode[][] graphGrid = new GraphDataNode[gridHeight][gridWidth];
//        for (GraphDataNode graphDataNode : graphDataNodeList.values()) {
//            graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
//        }
//    }

    public GraphDataNode[] getDataNodes() {
        return graphDataNodeArray;
    }

    private void printLocations() {
        System.out.println("printLocations");
        for (GraphDataNode graphDataNode : graphDataNodeArray) {
            System.out.println("node: " + graphDataNode.xPos + ":" + graphDataNode.yPos);
            for (GraphDataNode.NodeRelation graphLinkNode : graphDataNode.getNodeRelations()) {
                System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.linkedNode.yPos);
            }
        }
    }
//    public static void main(String args[]) {
//        GraphData graphData = new GraphData();
//        graphData.readData();
//        System.exit(0);
//    }
}
