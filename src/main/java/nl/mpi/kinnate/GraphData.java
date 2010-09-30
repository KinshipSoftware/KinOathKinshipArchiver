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
            sanguineSubnodeSort(generationRows, targetColumns, inputNodes, relatedNode.linkedNode);
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
            currentColumns.add(currentNode);
            sanguineSubnodeSort(generationRows, currentColumns, inputNodes, currentNode);
        }
        gridWidth = 0;
        int yPos = 0;
        for (ArrayList<GraphDataNode> currentRow : generationRows) {
            int xPos = 0;
            if (gridWidth < currentRow.size()) {
                gridWidth = currentRow.size();
            }
            for (GraphDataNode graphDataNode : currentRow) {
                graphDataNode.yPos = yPos;
                graphDataNode.xPos = xPos;
                xPos++;
            }
            yPos++;
        }
        System.out.println("gridWidth: " + gridWidth);
        gridHeight = yPos;
    }

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
