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
        calculateLocations();
        printLocations();
    }

    protected void calculateLinks() {
        System.out.println("calculateLinks");
        for (GraphDataNode currentNode : graphDataNodeList.values()) {
            System.out.println("currentNode: " + currentNode.getLabel());
            ArrayList<GraphDataNode> linkNodes = new ArrayList<GraphDataNode>();
            for (String currentLinkString : currentNode.getLinks()) {
                GraphDataNode linkedNode = graphDataNodeList.get(currentLinkString);
                if (linkedNode != null) {
                    linkNodes.add(linkedNode);
                    System.out.println("link found: " + currentLinkString);
                }
            }
            currentNode.setGraphDataNodes(linkNodes.toArray(new GraphDataNode[]{}));
        }

    }

    protected void calculateLocations() {
        System.out.println("calculateLocations");
        gridWidth = (int) Math.sqrt(graphDataNodeList.size());
        System.out.println("gridWidth: " + gridWidth);
        int xPos = 0;
        int yPos = 0;
        for (GraphDataNode graphDataNode : graphDataNodeList.values()) {
            graphDataNode.xPos = xPos;
            graphDataNode.yPos = yPos;
            xPos++;
            if (xPos > gridWidth) {
                xPos = 0;
                yPos++;
            }
        }
        gridHeight = yPos;
    }

    public GraphDataNode[] getDataNodes() {
        return graphDataNodeList.values().toArray(new GraphDataNode[]{});
    }

    private void printLocations() {
        System.out.println("printLocations");
        for (GraphDataNode graphDataNode : graphDataNodeList.values()) {
            System.out.println("node: " + graphDataNode.xPos + ":" + graphDataNode.yPos);
            for (GraphDataNode graphLinkNode : graphDataNode.linkedNodes) {
                System.out.println("link: " + graphLinkNode.xPos + ":" + graphLinkNode.yPos);
            }
        }
    }

    public static void main(String args[]) {
        GraphData graphData = new GraphData();
        graphData.readData();
        System.exit(0);
    }
}
