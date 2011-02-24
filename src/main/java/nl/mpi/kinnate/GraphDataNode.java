package nl.mpi.kinnate;

import java.util.ArrayList;
import nl.mpi.kinnate.KinTypeStringConverter.KinType;

/**
 *  Document   : GraphDataNode
 *  Created on : Sep 11, 2010, 4:30:41 PM
 *  Author     : Peter Withers
 */
public class GraphDataNode {

    public enum SymbolType {
        // symbol terms are used here to try to keep things agnostic

        square, triangle, circle, union, resource, ego, none
    }

    public enum RelationType {
        // todo: the term sibling is too specific and needs to encompas anything on the same generation such as union

        sibling, ancestor, descendant
    }

    public static RelationType getOpposingRelationType(RelationType relationType) {
        switch (relationType) {
            case ancestor:
                return GraphDataNode.RelationType.descendant;
            case descendant:
                return GraphDataNode.RelationType.ancestor;
            case sibling:
                return GraphDataNode.RelationType.sibling;
//            case union:
//                return GraphDataNode.RelationType.union;
        }
        return GraphDataNode.RelationType.sibling;
    }
    private String entityPath;
    private SymbolType symbolType;
    private String symbolTypeString;
    boolean isEgo = false;
    private String[] labelString;
    private ArrayList<NodeRelation> relatedNodes = new ArrayList<NodeRelation>();
    int xPos;
    int yPos;

    public class NodeRelation {

        public GraphDataNode sourceNode;
        public GraphDataNode linkedNode;
        public int generationalDistance;
        RelationType relationType;
    }

    public GraphDataNode(String entityPathLocal, String symbolTypeLocal, String[] labelStringLocal, boolean isEgoLocal) {
        entityPath = entityPathLocal;
        symbolType = null;
        symbolTypeString = symbolTypeLocal;
        labelString = labelStringLocal;
        isEgo = isEgoLocal;
    }

    public GraphDataNode(String entityPathLocal, SymbolType symbolIndex, String[] labelStringLocal, boolean isEgoLocal) {
        entityPath = entityPathLocal;
        symbolType = symbolIndex;
        labelString = labelStringLocal;
        isEgo = isEgoLocal;
    }
//    public GraphDataNode(SymbolType symbolIndex, String[] labelStringLocal) {
//        symbolType = symbolIndex;
//        labelString = labelStringLocal;
//    }

//    public GraphDataNode(String[] labelStringLocal) {
//        labelString = labelStringLocal;
//    }
    public String getSymbolType() {
        if (symbolType != null) {
            switch (symbolType) {
                case circle:
                    return "circle";
                case ego:
                    return "square";
                case none:
                    return null;
                case resource:
                    return "resource";
                case square:
                    return "square";
                case triangle:
                    return "triangle";
                case union:
                    return "union";
            }
        }
        return symbolTypeString;
    }

    public String getEntityPath() {
        return entityPath;
    }

    public String[] getLabel() {
        return labelString;
    }
    ArrayList<String> unhandledLinkTypesArray = new ArrayList<String>();

//    protected void calculateLinks(HashMap<String, GraphDataNode> graphDataNodeList) {
//        if (this.imdiTreeObject != null) {
//            this.imdiTreeObject.waitTillLoaded();
//
//
//            for (ImdiTreeObject childNode : this.imdiTreeObject.getAllChildren()) {
//                ImdiField[] currentField = childNode.getFields().get("Link");
//
//
//                if (currentField != null && currentField.length > 0) {
//                    GraphDataNode.RelationType relationType = GraphDataNode.RelationType.sibling;
//                    ImdiField[] relationTypeField = childNode.getFields().get("Type"); //todo: this RELA field might not be the best nor the only one to gather relation types from
//
//
//                    if (relationTypeField != null && relationTypeField.length > 0) {
//                        String typeString = relationTypeField[0].getFieldValue();
//                        System.out.println("link type field: " + relationTypeField[0].getFieldValue());
//                        List<String> ancestorTerms = Arrays.asList(new String[]{"SUBN", "_HME", "WIFE", "CHIL", "HUSB", "REPO", "OBJE", "NOTE", "FAMC", "FAMS", "SOUR", "ASSO", "SUBM", "ANCI", "DESI", "ALIA"});
//
//
//                        if (("Kinnate.Gedcom.Entity." + ancestorTerms).contains(typeString)) {
//                            relationType = GraphDataNode.RelationType.ancestor;
//
//
//                        } else {
//                            unhandledLinkTypesArray.add(typeString);
//
//
//                        }
//
////                        if ("Father".equals(typeString)) {
////                            relationType = GraphDataNode.RelationType.ancestor;
////                        } else if ("Mother".equals(typeString)) {
////                            relationType = GraphDataNode.RelationType.ancestor;
////                        }
//                    }
//                    System.out.println("link field: " + currentField[0].getFieldValue());
////                    linkArray.add(currentField[0].getFieldValue());
//                    GraphDataNode linkedNode = graphDataNodeList.get(currentField[0].getFieldValue());
//
//
//                    if (linkedNode != null) {
//                        this.addRelatedNode(linkedNode, 0, relationType);
//
//
//                    }
//                }
//            }
//        }
//        if (unhandledLinkTypesArray.size() > 0) {
//            System.err.println("unhandledLinkTypes: " + unhandledLinkTypesArray.toString());
//
//
//        }
//    }
//    public GraphDataNode[] getLinks() {
//        if (imdiTreeObject == null) {
//            return linkStringsArray;
//        } else {
//            ArrayList<String> linkArray = new ArrayList<String>();
//            imdiTreeObject.waitTillLoaded();
//            for (ImdiTreeObject childNode : imdiTreeObject.getAllChildren()) {
////            System.out.println("getAllChildren: " + childNode.getUrlString());
//                ImdiField[] currentField = childNode.getFields().get("Link");
//                if (currentField != null && currentField.length > 0) {
//                    System.out.println("link field: " + currentField[0].getFieldValue());
//                    linkArray.add(currentField[0].getFieldValue());
//                }
//            }
//            return linkArray.toArray(new String[]{});
//        }
//    }
    public void addRelatedNode(GraphDataNode relatedNode, int generationalDistance, RelationType relationType) {
        // note that the test gedcom file has multiple links for a given pair so in might be necessary to filter incoming links on a preferential basis
        NodeRelation nodeRelation = new NodeRelation();
        nodeRelation.sourceNode = this;
        nodeRelation.linkedNode = relatedNode;
        nodeRelation.generationalDistance = generationalDistance;
        nodeRelation.relationType = relationType;
        relatedNodes.add(nodeRelation);
        relatedNode.relatedNodes.add(nodeRelation);
    }

    public boolean relationMatchesType(String alterPath, KinType kinType) {
        // todo: compare the relation data
//        return kinType.symbolType == symbolType;
        return true;
    }

    public NodeRelation[] getNodeRelations() {
        return relatedNodes.toArray(new NodeRelation[]{});
    }
}
