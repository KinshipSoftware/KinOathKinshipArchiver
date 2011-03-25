package nl.mpi.kinnate.kintypestrings;

import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.EntityData;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *  Document   : KinTypeStringConverter
 *  Created on : Sep 29, 2010, 12:52:33 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringConverter extends GraphSorter {

    public class KinType {

        private KinType(String codeStringLocal, EntityData.RelationType relationTypeLocal, EntityData.SymbolType symbolTypeLocal) {
            codeString = codeStringLocal;
            relationType = relationTypeLocal;
            symbolType = symbolTypeLocal;
        }
        String codeString;
        private EntityData.RelationType relationType;
        private EntityData.SymbolType symbolType;
    }
    private KinType[] referenceKinTypes = new KinType[]{
        // type 1
        new KinType("Fa", EntityData.RelationType.ancestor, EntityData.SymbolType.triangle),
        new KinType("Mo", EntityData.RelationType.ancestor, EntityData.SymbolType.circle),
        new KinType("Br", EntityData.RelationType.sibling, EntityData.SymbolType.triangle),
        new KinType("Si", EntityData.RelationType.sibling, EntityData.SymbolType.circle),
        new KinType("So", EntityData.RelationType.descendant, EntityData.SymbolType.triangle),
        new KinType("Da", EntityData.RelationType.descendant, EntityData.SymbolType.circle),
        new KinType("Hu", EntityData.RelationType.union, EntityData.SymbolType.triangle),
        new KinType("Wi", EntityData.RelationType.union, EntityData.SymbolType.circle),
        new KinType("Pa", EntityData.RelationType.ancestor, EntityData.SymbolType.square),
        new KinType("Sb", EntityData.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("Sp", EntityData.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("Ch", EntityData.RelationType.descendant, EntityData.SymbolType.square),
        // type 2
        new KinType("F", EntityData.RelationType.ancestor, EntityData.SymbolType.triangle),
        new KinType("M", EntityData.RelationType.ancestor, EntityData.SymbolType.circle),
        new KinType("B", EntityData.RelationType.sibling, EntityData.SymbolType.triangle),
        new KinType("Z", EntityData.RelationType.sibling, EntityData.SymbolType.circle),
        new KinType("S", EntityData.RelationType.descendant, EntityData.SymbolType.triangle),
        new KinType("D", EntityData.RelationType.descendant, EntityData.SymbolType.circle),
        new KinType("H", EntityData.RelationType.union, EntityData.SymbolType.triangle),
        new KinType("W", EntityData.RelationType.union, EntityData.SymbolType.circle),
        new KinType("P", EntityData.RelationType.ancestor, EntityData.SymbolType.square),
        new KinType("G", EntityData.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("E", EntityData.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("C", EntityData.RelationType.descendant, EntityData.SymbolType.square)
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

    public void readKinTypes(String[] inputStringArray, KinTerms kinTerms) {
        HashMap<String, EntityData> graphDataNodeList = new HashMap<String, EntityData>();
        EntityData egoDataNode = new EntityData("ego", "ego", EntityData.SymbolType.square, new String[]{"ego"}, true);
        graphDataNodeList.put("ego", egoDataNode);
        egoDataNode.isVisible = true;
        for (String inputString : inputStringArray) {
            System.out.println("inputString: " + inputString);
            if (inputString != null) {
                String consumableString = inputString;
                EntityData parentDataNode = egoDataNode;
                while (consumableString.length() > 0) {
                    boolean kinTypeFound = false;
                    for (KinType currentReferenceKinType : referenceKinTypes) {
                        if (consumableString.startsWith(currentReferenceKinType.codeString)) {
                            consumableString = consumableString.substring(currentReferenceKinType.codeString.length());
                            String fullKinTypeString = inputString.substring(0, inputString.length() - consumableString.length());
                            System.out.println("kinTypeFound: " + currentReferenceKinType.codeString);
                            System.out.println("consumableString: " + consumableString);
                            System.out.println("fullKinTypeString: " + fullKinTypeString);
                            EntityData currentGraphDataNode;
                            if (graphDataNodeList.containsKey(fullKinTypeString)) {
                                currentGraphDataNode = graphDataNodeList.get(fullKinTypeString);
                                // add any child nodes?
                            } else {
                                currentGraphDataNode = new EntityData(fullKinTypeString, fullKinTypeString, currentReferenceKinType.symbolType, new String[]{fullKinTypeString}, false);
                                EntityData.RelationType opposingRelationType = EntityData.getOpposingRelationType(currentReferenceKinType.relationType);
                                parentDataNode.addRelatedNode(currentGraphDataNode, 0, currentReferenceKinType.relationType, EntityData.RelationLineType.square, null);
                                currentGraphDataNode.addRelatedNode(parentDataNode, 0, opposingRelationType, EntityData.RelationLineType.square, null);
                                graphDataNodeList.put(fullKinTypeString, currentGraphDataNode);
                                currentGraphDataNode.isVisible = true;
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
                String kinTermLabel = kinTerms.getTermLabel(inputString);
                if (kinTermLabel != null) {
                    egoDataNode.addRelatedNode(parentDataNode, 2, EntityData.RelationType.none, EntityData.RelationLineType.horizontalCurve, kinTermLabel);
                }
            }
        }
        graphDataNodeArray = graphDataNodeList.values().toArray(new EntityData[]{});
        sanguineSort();
    }
}
