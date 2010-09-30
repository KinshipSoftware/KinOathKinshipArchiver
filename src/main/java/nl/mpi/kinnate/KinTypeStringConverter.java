package nl.mpi.kinnate;

/**
 *  Document   : KinTypeStringConverter
 *  Created on : Sep 29, 2010, 12:52:33 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringConverter extends GraphData {

    enum RelationType {

        ancestor, descendant, sibling, partner
    }

    class KinType {

        public KinType(String codeStringLocal, RelationType relationTypeLocal, GraphDataNode.SymbolType symbolTypeLocal) {
            codeString = codeStringLocal;
            relationType = relationTypeLocal;
            symbolType = symbolTypeLocal;
        }
        String codeString;
        RelationType relationType;
        GraphDataNode.SymbolType symbolType;
    }
    KinType[] referenceKinTypes = new KinType[]{
        // type 1
        new KinType("Fa", RelationType.ancestor, GraphDataNode.SymbolType.triangle),
        new KinType("Mo", RelationType.ancestor, GraphDataNode.SymbolType.circle),
        new KinType("Br", RelationType.sibling, GraphDataNode.SymbolType.triangle),
        new KinType("Si", RelationType.sibling, GraphDataNode.SymbolType.circle),
        new KinType("So", RelationType.descendant, GraphDataNode.SymbolType.triangle),
        new KinType("Da", RelationType.descendant, GraphDataNode.SymbolType.circle),
        new KinType("Hu", RelationType.partner, GraphDataNode.SymbolType.triangle),
        new KinType("Wi", RelationType.partner, GraphDataNode.SymbolType.circle),
        new KinType("Pa", RelationType.ancestor, GraphDataNode.SymbolType.square),
        new KinType("Sb", RelationType.sibling, GraphDataNode.SymbolType.square),
        new KinType("Sp", RelationType.partner, GraphDataNode.SymbolType.equals),
        new KinType("Ch", RelationType.descendant, GraphDataNode.SymbolType.square),
        // type 2
        new KinType("F", RelationType.ancestor, GraphDataNode.SymbolType.triangle),
        new KinType("M", RelationType.ancestor, GraphDataNode.SymbolType.circle),
        new KinType("B", RelationType.sibling, GraphDataNode.SymbolType.triangle),
        new KinType("Z", RelationType.sibling, GraphDataNode.SymbolType.circle),
        new KinType("S", RelationType.descendant, GraphDataNode.SymbolType.triangle),
        new KinType("D", RelationType.descendant, GraphDataNode.SymbolType.circle),
        new KinType("H", RelationType.partner, GraphDataNode.SymbolType.triangle),
        new KinType("W", RelationType.partner, GraphDataNode.SymbolType.circle),
        new KinType("P", RelationType.ancestor, GraphDataNode.SymbolType.square),
        new KinType("G", RelationType.sibling, GraphDataNode.SymbolType.square),
        new KinType("E", RelationType.partner, GraphDataNode.SymbolType.equals),
        new KinType("C", RelationType.descendant, GraphDataNode.SymbolType.square)
    };

    public void readKinTypes(String[] inputStringArray) {
        for (String inputString : inputStringArray) {
            System.out.println("inputString: " + inputString);
            if (inputString != null) {
                String consumableString = inputString;
                while (consumableString.length() > 0) {
                    boolean kinTypeFound = false;
                    for (KinType currentReferenceKinType : referenceKinTypes) {
                        if (consumableString.startsWith(currentReferenceKinType.codeString)) {
                            consumableString = consumableString.substring(currentReferenceKinType.codeString.length());
                            String fullKinTypeString = inputString.substring(0, inputString.length() - consumableString.length());
                            System.out.println("kinTypeFound: " + currentReferenceKinType.codeString);
                            System.out.println("consumableString: " + consumableString);
                            System.out.println("fullKinTypeString: " + fullKinTypeString);
                            if (graphDataNodeList.containsKey(fullKinTypeString)) {
                                graphDataNodeList.get(fullKinTypeString); // add any child nodes
                            } else {
                                GraphDataNode freshGraphDataNode =new GraphDataNode(fullKinTypeString);
                                freshGraphDataNode.symbolType = currentReferenceKinType.symbolType;
//                                freshGraphDataNode.symbolType = currentReferenceKinType.symbolType;
                                graphDataNodeList.put(fullKinTypeString, freshGraphDataNode);
                                // add any child nodes
                            }
                            kinTypeFound= true;
                            break;
                        }
                    }
                    if (kinTypeFound == false) {
                        break;
                    }
                }
            }
        }
        calculateLinks();
        calculateLocations();
//        printLocations();
    }
}
