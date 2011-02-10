package nl.mpi.kinnate;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *  Document   : KinTypeStringConverter
 *  Created on : Sep 29, 2010, 12:52:33 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringConverter extends GraphData {

    public class KinType {

        private KinType(String codeStringLocal, GraphDataNode.RelationType relationTypeLocal, GraphDataNode.SymbolType symbolTypeLocal) {
            codeString = codeStringLocal;
            relationType = relationTypeLocal;
            symbolType = symbolTypeLocal;
        }
        String codeString;
        private GraphDataNode.RelationType relationType;
        private GraphDataNode.SymbolType symbolType;
    }
    private KinType[] referenceKinTypes = new KinType[]{
        // type 1
        new KinType("Fa", GraphDataNode.RelationType.ancestor, GraphDataNode.SymbolType.triangle),
        new KinType("Mo", GraphDataNode.RelationType.ancestor, GraphDataNode.SymbolType.circle),
        new KinType("Br", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.triangle),
        new KinType("Si", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.circle),
        new KinType("So", GraphDataNode.RelationType.descendant, GraphDataNode.SymbolType.triangle),
        new KinType("Da", GraphDataNode.RelationType.descendant, GraphDataNode.SymbolType.circle),
        new KinType("Hu", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.triangle),
        new KinType("Wi", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.circle),
        new KinType("Pa", GraphDataNode.RelationType.ancestor, GraphDataNode.SymbolType.square),
        new KinType("Sb", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.square),
        new KinType("Sp", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.square),
        new KinType("Ch", GraphDataNode.RelationType.descendant, GraphDataNode.SymbolType.square),
        // type 2
        new KinType("F", GraphDataNode.RelationType.ancestor, GraphDataNode.SymbolType.triangle),
        new KinType("M", GraphDataNode.RelationType.ancestor, GraphDataNode.SymbolType.circle),
        new KinType("B", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.triangle),
        new KinType("Z", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.circle),
        new KinType("S", GraphDataNode.RelationType.descendant, GraphDataNode.SymbolType.triangle),
        new KinType("D", GraphDataNode.RelationType.descendant, GraphDataNode.SymbolType.circle),
        new KinType("H", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.triangle),
        new KinType("W", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.circle),
        new KinType("P", GraphDataNode.RelationType.ancestor, GraphDataNode.SymbolType.square),
        new KinType("G", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.square),
        new KinType("E", GraphDataNode.RelationType.sibling, GraphDataNode.SymbolType.square),
        new KinType("C", GraphDataNode.RelationType.descendant, GraphDataNode.SymbolType.square)
    };

    public ArrayList<KinType> getKinTypes(String consumableString) {
        ArrayList<KinType> kinTypeList = new ArrayList<KinType>();
        boolean foundKinType = true;
        while (foundKinType && consumableString.length() > 0) {
            for (KinType currentReferenceKinType : referenceKinTypes) {
                foundKinType = false;
                if (consumableString.startsWith(currentReferenceKinType.codeString)) {
                    kinTypeList.add(currentReferenceKinType);
                    consumableString = consumableString.substring(currentReferenceKinType.codeString.length());
                    foundKinType = true;
                    break;
                }
            }
        }
        return kinTypeList;
    }

    public void readKinTypes(String[] inputStringArray) {
        HashMap<String, GraphDataNode> graphDataNodeList = new HashMap<String, GraphDataNode>();
        GraphDataNode egoDataNode = new GraphDataNode(new String[]{"ego"});
        egoDataNode.symbolType = GraphDataNode.SymbolType.square;
        egoDataNode.isEgo = true;
        graphDataNodeList.put("ego", egoDataNode);
        for (String inputString : inputStringArray) {
            System.out.println("inputString: " + inputString);
            if (inputString != null) {
                String consumableString = inputString;
                GraphDataNode parentDataNode = egoDataNode;
                while (consumableString.length() > 0) {
                    boolean kinTypeFound = false;
                    for (KinType currentReferenceKinType : referenceKinTypes) {
                        if (consumableString.startsWith(currentReferenceKinType.codeString)) {
                            consumableString = consumableString.substring(currentReferenceKinType.codeString.length());
                            String fullKinTypeString = inputString.substring(0, inputString.length() - consumableString.length());
                            System.out.println("kinTypeFound: " + currentReferenceKinType.codeString);
                            System.out.println("consumableString: " + consumableString);
                            System.out.println("fullKinTypeString: " + fullKinTypeString);
                            GraphDataNode currentGraphDataNode;
                            if (graphDataNodeList.containsKey(fullKinTypeString)) {
                                currentGraphDataNode = graphDataNodeList.get(fullKinTypeString);
                                // add any child nodes?
                            } else {
                                currentGraphDataNode = new GraphDataNode(new String[]{fullKinTypeString});
                                currentGraphDataNode.symbolType = currentReferenceKinType.symbolType;
                                GraphDataNode.RelationType opposingRelationType = GraphDataNode.getOpposingRelationType(currentReferenceKinType.relationType);
                                parentDataNode.addRelatedNode(currentGraphDataNode, 0, currentReferenceKinType.relationType);
                                currentGraphDataNode.addRelatedNode(parentDataNode, 0, opposingRelationType);
                                graphDataNodeList.put(fullKinTypeString, currentGraphDataNode);
                                // add any child nodes?
                            }
                            parentDataNode = currentGraphDataNode;
                            kinTypeFound = true;
                            break;
                        }
                    }
                    if (kinTypeFound == false) {
                        break;
                    }
                }
            }
        }
        graphDataNodeArray = graphDataNodeList.values().toArray(new GraphDataNode[]{});
        sanguineSort();
    }
}
