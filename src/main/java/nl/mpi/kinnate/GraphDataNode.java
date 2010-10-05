package nl.mpi.kinnate;

import java.util.ArrayList;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 *  Document   : GraphDataNode
 *  Created on : Sep 11, 2010, 4:30:41 PM
 *  Author     : Peter Withers
 */
public class GraphDataNode {

    enum SymbolType {
        // symbol terms are used here to try to keep things agnostic

        square, triangle, circle
    }

    public enum RelationType {

        sibling, ancestor, descendant, union
    }
    SymbolType symbolType;
    boolean isEgo = false;
    private ImdiTreeObject imdiTreeObject;
    private String labelString;
    private String[] linkStringsArray = new String[]{};
    private ArrayList<NodeRelation> relatedNodes = new ArrayList<NodeRelation>();
    int xPos;
    int yPos;

    public class NodeRelation {

        public GraphDataNode sourceNode;
        public GraphDataNode linkedNode;
        public int generationalDistance;
        RelationType relationType;
    }

    public GraphDataNode(ImdiTreeObject imdiTreeObjectLocal) {
        imdiTreeObject = imdiTreeObjectLocal;
    }

    public GraphDataNode(String labelStringLocal) {
        labelString = labelStringLocal;
    }

    public String getLabel() {
        if (imdiTreeObject != null) {
            return imdiTreeObject.toString();
        } else {
            return labelString;
        }
    }

    public String[] getLinks() {
        if (imdiTreeObject == null) {
            return linkStringsArray;
        } else {
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
    }

    public void addRelatedNode(GraphDataNode relatedNode, int generationalDistance, RelationType relationType) {
        NodeRelation nodeRelation = new NodeRelation();
        nodeRelation.sourceNode = this;
        nodeRelation.linkedNode = relatedNode;
        nodeRelation.generationalDistance = generationalDistance;
        nodeRelation.relationType = relationType;
        relatedNodes.add(nodeRelation);
        relatedNode.relatedNodes.add(nodeRelation);
    }

    public NodeRelation[] getNodeRelations() {
        return relatedNodes.toArray(new NodeRelation[]{});
    }
}
