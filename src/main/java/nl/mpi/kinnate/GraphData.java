package nl.mpi.kinnate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : GraphData
 *  Created on : Sep 11, 2010, 4:51:36 PM
 *  Author     : Peter Withers
 */
public class GraphData {

    protected HashMap<String, GraphDataNode> graphDataNodeList = new HashMap<String, GraphDataNode>();
    public int gridWidth;
    public int gridHeight;

    public void readData() {
        String[] treeNodesArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
        if (treeNodesArray != null) {
            for (String currentNodeString : treeNodesArray) {
                try {
                    ImdiTreeObject imdiTreeObject = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentNodeString));
                    graphDataNodeList.put(imdiTreeObject.getUrlString(), new GraphDataNode(imdiTreeObject));
                } catch (URISyntaxException exception) {
                    System.err.println(exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
        calculateLinks();
        sanguineSort();
        printLocations();
    }

    protected void calculateLinks() {
        System.out.println("calculateLinks");
        for (GraphDataNode currentNode : graphDataNodeList.values()) {
            System.out.println("currentNode: " + currentNode.getLabel());
//            ArrayList<GraphDataNode> linkNodes = new ArrayList<GraphDataNode>();
            for (String currentLinkString : currentNode.getLinks()) {
                GraphDataNode linkedNode = graphDataNodeList.get(currentLinkString);
                if (linkedNode != null) {
                    currentNode.addRelatedNode(linkedNode, 0, GraphDataNode.RelationType.sibling);
                    System.out.println("link found: " + currentLinkString);
                }
            }
        }

    }

    private void sanguineSubnodeSort(ArrayList<ArrayList<GraphDataNode>> generationRows, ArrayList<GraphDataNode> currentColumns, ArrayList<GraphDataNode> inputNodes, GraphDataNode currentNode) {
        int currentRowIndex = generationRows.indexOf(currentColumns);
        ArrayList<GraphDataNode> ancestorColumns;
        ArrayList<GraphDataNode> descendentColumns;
        if (currentRowIndex < generationRows.size() - 1) {
            descendentColumns = generationRows.get(currentRowIndex + 1);
        } else {
            descendentColumns = new ArrayList<GraphDataNode>();
            generationRows.add(currentRowIndex + 1, descendentColumns);
        }
        if (currentRowIndex > 0) {
            ancestorColumns = generationRows.get(currentRowIndex - 1);
        } else {
            ancestorColumns = new ArrayList<GraphDataNode>();
            generationRows.add(currentRowIndex, ancestorColumns);
        }
        for (GraphDataNode.NodeRelation relatedNode : currentNode.getNodeRelations()) {
            if (relatedNode.sourceNode.equals(currentNode)) {
                if (inputNodes.contains(relatedNode.linkedNode)) {
                    ArrayList<GraphDataNode> targetColumns = null;
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
                        case union:
                            targetColumns = currentColumns;
                            break;
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
                ArrayList<GraphDataNode> targetColumns = null;
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
                    case union:
                        targetColumns = currentColumns;
                        break;
                }
                inputNodes.remove(relatedNode.sourceNode);
                targetColumns.add(relatedNode.sourceNode);
                System.out.println("sorted: " + relatedNode.sourceNode.getLabel() + " : " + "reverce link" + " of " + currentNode.getLabel());
                sanguineSubnodeSort(generationRows, targetColumns, inputNodes, relatedNode.sourceNode);
            }
        }
    }

    protected void sanguineSort() {
        System.out.println("calculateLocations");
        // create an array of rows
        ArrayList<ArrayList<GraphDataNode>> generationRows = new ArrayList<ArrayList<GraphDataNode>>();
        ArrayList<GraphDataNode> inputNodes = new ArrayList<GraphDataNode>();
        inputNodes.addAll(graphDataNodeList.values());
        // put an array of columns into the current row
        ArrayList<GraphDataNode> currentColumns = new ArrayList<GraphDataNode>();
        generationRows.add(currentColumns);

        while (inputNodes.size() > 0) {
            GraphDataNode currentNode = inputNodes.remove(0);
            System.out.println("add as root node: " + currentNode.getLabel());
            currentColumns.add(currentNode);
            sanguineSubnodeSort(generationRows, currentColumns, inputNodes, currentNode);
        }
        gridWidth = 0;
        int yPos = 0;
        for (ArrayList<GraphDataNode> currentRow : generationRows) {
            System.out.println("row: : " + yPos);
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
        System.out.println("gridWidth: " + gridWidth);
        gridHeight = yPos;
        sortByLinkDistance();
        sortByLinkDistance();
    }

    private void sortByLinkDistance() {
        GraphDataNode[][] graphGrid = new GraphDataNode[gridHeight][gridWidth];
        for (GraphDataNode graphDataNode : graphDataNodeList.values()) {
            graphGrid[graphDataNode.yPos][graphDataNode.xPos] = graphDataNode;
        }
        for (GraphDataNode graphDataNode : graphDataNodeList.values()) {
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
        return graphDataNodeList.values().toArray(new GraphDataNode[]{});
    }

    private void printLocations() {
        System.out.println("printLocations");
        for (GraphDataNode graphDataNode : graphDataNodeList.values()) {
            System.out.println("node: " + graphDataNode.xPos + ":" + graphDataNode.yPos);
            for (GraphDataNode.NodeRelation graphLinkNode : graphDataNode.getNodeRelations()) {
                System.out.println("link: " + graphLinkNode.linkedNode.xPos + ":" + graphLinkNode.linkedNode.yPos);
            }
        }
    }

    public static void main(String args[]) {
        GraphData graphData = new GraphData();
        graphData.readData();
        System.exit(0);
    }
}
