package nl.mpi.kingraph2;

import java.util.ArrayList;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : GraphDataNode
 *  Created on : Sep 11, 2010, 4:30:41 PM
 *  Author     : Peter Withers
 */
public class GraphDataNode {

    ImdiTreeObject imdiTreeObject;
    public GraphDataNode[] linkedNodes;
    int xPos;
    int yPos;

    public GraphDataNode(ImdiTreeObject imdiTreeObjectLocal) {
        imdiTreeObject = imdiTreeObjectLocal;
    }

    public String[] getLinks() {
        ArrayList<String> linkArray = new ArrayList<String>();
        imdiTreeObject.waitTillLoaded();
        for (ImdiTreeObject childNode : imdiTreeObject.getAllChildren()) {
//            System.out.println("getAllChildren: " + childNode.getUrlString());
            ImdiField[] currentField = childNode.getFields().get("Link");
            if (currentField != null && currentField.length > 0) {
                System.out.println("link field: " + currentField[0].getFieldValue());
                linkArray.add(currentField[0].getFieldValue());
            }
        }
        return linkArray.toArray(new String[]{});
    }

    public void setGraphDataNodes(GraphDataNode[] linkedNodesLocal) {
        linkedNodes = linkedNodesLocal;
    }
}
