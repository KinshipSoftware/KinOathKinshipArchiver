package nl.mpi.kinnate.kintypestrings;

import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.EntityData;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.kinnate.kindata.DataTypes;

/**
 *  Document   : KinTypeStringConverter
 *  Created on : Sep 29, 2010, 12:52:33 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringConverter extends GraphSorter {

    public class KinType {

        private KinType(String codeStringLocal, DataTypes.RelationType relationTypeLocal, EntityData.SymbolType symbolTypeLocal) {
            codeString = codeStringLocal;
            relationType = relationTypeLocal;
            symbolType = symbolTypeLocal;
        }
        String codeString;
        private DataTypes.RelationType relationType;
        private EntityData.SymbolType symbolType;
    }
    private KinType[] referenceKinTypes = new KinType[]{
        // type 1
        new KinType("Fa", DataTypes.RelationType.ancestor, EntityData.SymbolType.triangle),
        new KinType("Mo", DataTypes.RelationType.ancestor, EntityData.SymbolType.circle),
        new KinType("Br", DataTypes.RelationType.sibling, EntityData.SymbolType.triangle),
        new KinType("Si", DataTypes.RelationType.sibling, EntityData.SymbolType.circle),
        new KinType("So", DataTypes.RelationType.descendant, EntityData.SymbolType.triangle),
        new KinType("Da", DataTypes.RelationType.descendant, EntityData.SymbolType.circle),
        new KinType("Hu", DataTypes.RelationType.union, EntityData.SymbolType.triangle),
        new KinType("Wi", DataTypes.RelationType.union, EntityData.SymbolType.circle),
        new KinType("Pa", DataTypes.RelationType.ancestor, EntityData.SymbolType.square),
        new KinType("Sb", DataTypes.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("Sp", DataTypes.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("Ch", DataTypes.RelationType.descendant, EntityData.SymbolType.square),
        // type 2
        new KinType("F", DataTypes.RelationType.ancestor, EntityData.SymbolType.triangle),
        new KinType("M", DataTypes.RelationType.ancestor, EntityData.SymbolType.circle),
        new KinType("B", DataTypes.RelationType.sibling, EntityData.SymbolType.triangle),
        new KinType("Z", DataTypes.RelationType.sibling, EntityData.SymbolType.circle),
        new KinType("S", DataTypes.RelationType.descendant, EntityData.SymbolType.triangle),
        new KinType("D", DataTypes.RelationType.descendant, EntityData.SymbolType.circle),
        new KinType("H", DataTypes.RelationType.union, EntityData.SymbolType.triangle),
        new KinType("W", DataTypes.RelationType.union, EntityData.SymbolType.circle),
        new KinType("P", DataTypes.RelationType.ancestor, EntityData.SymbolType.square),
        new KinType("G", DataTypes.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("E", DataTypes.RelationType.sibling, EntityData.SymbolType.square),
        new KinType("C", DataTypes.RelationType.descendant, EntityData.SymbolType.square)
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
                                DataTypes.RelationType opposingRelationType = DataTypes.getOpposingRelationType(currentReferenceKinType.relationType);
                                parentDataNode.addRelatedNode(currentGraphDataNode, 0, currentReferenceKinType.relationType, DataTypes.RelationLineType.square, null);
                                currentGraphDataNode.addRelatedNode(parentDataNode, 0, opposingRelationType, DataTypes.RelationLineType.square, null);
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
                    egoDataNode.addRelatedNode(parentDataNode, 2, DataTypes.RelationType.none, DataTypes.RelationLineType.horizontalCurve, kinTermLabel);
                }
            }
        }
        graphDataNodeArray = graphDataNodeList.values().toArray(new EntityData[]{});
        sanguineSort();
    }
}
