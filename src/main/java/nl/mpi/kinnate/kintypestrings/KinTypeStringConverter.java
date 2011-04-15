package nl.mpi.kinnate.kintypestrings;

import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.EntityData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import nl.mpi.kinnate.entityindexer.QueryParser.ParserHighlight;
import nl.mpi.kinnate.entityindexer.QueryParser.ParserHighlightType;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityRelation;

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
        private String codeString;
        private DataTypes.RelationType relationType;
        private EntityData.SymbolType symbolType;

        public String getCodeString() {
            return codeString;
        }

        public boolean isEgoType() {
            return codeString.equals("E");
        }
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
        new KinType("C", DataTypes.RelationType.descendant, EntityData.SymbolType.square),
        new KinType("X", DataTypes.RelationType.none, EntityData.SymbolType.none) // X is intended to indicate unknown or no type, for instance this is used after import to add all nodes to the graph
    };

    public class KinTypeElement {

        public KinTypeElement() {
            entityData = new ArrayList<EntityData>();
        }
        public KinTypeElement prevType;
        public KinTypeElement nextType;
        public KinType kinType;
        public ArrayList<String[]> queryTerm;
        public ArrayList<EntityData> entityData; // there may be multiple entities for each kin term
        ParserHighlight[] highlightLocs;
    }

//    public void highlightComments(String[] kinTypeStrings, ParserHighlight[][] parserHighlight) {
//        int lineCounter = 0;
//        for (String currentString : kinTypeStrings) {
//            parserHighlight[lineCounter] = new ParserHighlight[currentString.length()];
//            ParserHighlight currentHighlight = ParserHighlight.Unknown;
//            for (int charCounter = 0; charCounter < currentString.length(); charCounter++) {
//                if (currentHighlight != ParserHighlight.Comment && currentString.charAt(charCounter) == '#') {
//                    currentHighlight = ParserHighlight.Comment;
//                }
//                parserHighlight[lineCounter][charCounter] = currentHighlight;
//            }
//
//            lineCounter++;
//        }
//    }
    public boolean compareRequiresNextRelation(EntityData adjacentEntity, KinType requiredKinType, EntityRelation entityRelation) {
//        adjacentEntity.appendTempLabel("compareRequiresNextRelation" + "F: " + QueryParser.foundOrder++); // temp for testing // todo: remove testing labels
        if (adjacentEntity.getSymbolType().equals(EntityData.SymbolType.union.name())) {
            return true;
        }
        if (requiredKinType.relationType.equals(DataTypes.RelationType.ancestor) && entityRelation.relationType.equals(DataTypes.RelationType.sibling)) {
            return true;
        }
        return false;
    }

    public boolean compareRelationsToKinType(EntityData egoEntity, EntityData alterEntity, KinType requiredKinType, EntityRelation entityRelation, int generationalDistance) {
//        egoEntity.appendTempLabel("compareRelationsToKinType-egoEntity" + "F: " + QueryParser.foundOrder++);
//        alterEntity.appendTempLabel("compareRelationsToKinType-alterEntity" + "F: " + QueryParser.foundOrder++);
        // temp for testing // todo: remove testing labels
//        System.out.println("egoEntity.isEgo: " + egoEntity.isEgo);
//        System.out.println("alterEntity.isEgo: " + alterEntity.isEgo);
//        System.out.println("egoEntity.symbol: " + egoEntity.getSymbolType());
//        System.out.println("alterEntity.symbol: " + alterEntity.getSymbolType());
//        System.out.println("entityRelation.relationType: " + entityRelation.relationType);
//        System.out.println("entityRelation.symgenerationalDistancebol: " + entityRelation.generationalDistance);
        // note that this will get the kin type reversed for one of the adjacent entities and this must be accounted for in the kin type comparison
        // todo: note that the ego and alter are not correct labels
        // this array will get the kin type reversed for one of the adjacent entities
        if (egoEntity.isEgo) {// && alter.getSymbolType().equals(EntityData.SymbolType.triangle.name())) {
            return true;
        }
        if (requiredKinType.relationType.equals(entityRelation.relationType)) {
            return true;
        }
        return false;
    }

    public ArrayList<KinTypeElement> getKinTypeElements(String consumableString, ParserHighlight parserHighlight) {
        int initialLength = consumableString.length();
        ArrayList<KinTypeElement> kinTypeElementList = new ArrayList<KinTypeElement>();
        KinTypeElement previousElement = null;
        boolean foundKinType = true;
        while (foundKinType && consumableString.length() > 0) {
            for (KinType currentReferenceKinType : referenceKinTypes) {
                foundKinType = false;
                if (consumableString.startsWith(currentReferenceKinType.codeString)) {
                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.KinType, initialLength - consumableString.length());
                    KinTypeElement currentElement = new KinTypeElement();
                    if (previousElement != null) {
                        previousElement.nextType = currentElement;
                        currentElement.prevType = previousElement;
                    }
                    previousElement = currentElement;
                    currentElement.kinType = currentReferenceKinType;
                    consumableString = consumableString.substring(currentReferenceKinType.codeString.length());

                    if (consumableString.startsWith("=[")) {
                        parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Query, initialLength - consumableString.length());
                        consumableString = consumableString.substring("=".length());
                        while (consumableString.startsWith("[")) {
                            // todo: allow multiple terms such as "=[foo][bar]" or "=[foo][bar][NAME=Bob]"
                            int queryStart = "[".length();
                            int queryEnd = consumableString.indexOf("]");
                            if (queryEnd == -1) {
                                // if the terms are incomplete then ignore the rest of the line
                                foundKinType = false;
                                break;
                            }
                            if (currentElement.queryTerm == null) {
                                currentElement.queryTerm = new ArrayList<String[]>();
                            }
                            String queryText = consumableString.substring(queryStart, queryEnd);
                            consumableString = consumableString.substring(queryEnd + 1);
                            if (!queryText.contains("=")) {
                                if (queryText.length() > 2) {
                                    currentElement.queryTerm.add(new String[]{"*", queryText});
                                }
                            } else {
                                String[] queryTerm = queryText.split("=");
                                if (queryTerm.length == 2) {
                                    if (queryTerm[0].length() > 2 && queryTerm[1].length() > 2) {
                                        currentElement.queryTerm.add(new String[]{queryTerm[0], queryTerm[1]});
                                    }
                                }
                            }
                        }
                    }
                    kinTypeElementList.add(currentElement);
                    foundKinType = true;
                    break;
                }
            }
        }
        if (!foundKinType && !consumableString.startsWith("#")) {
            parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Error, initialLength - consumableString.length());
        }
        if (consumableString.contains("#")) {
            // check for any comments
            parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Comment, initialLength - consumableString.length() + consumableString.indexOf("#"));
        }
        return kinTypeElementList;
    }

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

    public void readKinTypes(String[] inputStringArray, KinTermGroup[] kinTermsArray) {
        HashMap<String, EntityData> graphDataNodeList = new HashMap<String, EntityData>();
        EntityData egoDataNode = new EntityData("E", "E", "E", EntityData.SymbolType.square, new String[]{"E"}, true);
        graphDataNodeList.put("E", egoDataNode);
        egoDataNode.isVisible = true;
        ArrayList<String> inputStringList = new ArrayList<String>();
        inputStringList.addAll(Arrays.asList(inputStringArray));
        for (KinTermGroup kinTerms : kinTermsArray) {
            if (kinTerms.graphGenerate) {
                for (KinTerm kinTerm : kinTerms.getKinTerms()) {
                    String[] kinTypeStrings = kinTerm.alterKinTypeStrings.split("\\|");
                    inputStringList.addAll(Arrays.asList(kinTypeStrings));
                }
            }
        }
        for (String inputString : inputStringList) {
            System.out.println("inputString: " + inputString);
            if (inputString != null) {
                inputString = inputString.trim();
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
                                currentGraphDataNode = new EntityData(fullKinTypeString, fullKinTypeString, fullKinTypeString, currentReferenceKinType.symbolType, new String[]{fullKinTypeString}, false);
                                DataTypes.RelationType opposingRelationType = DataTypes.getOpposingRelationType(currentReferenceKinType.relationType);
                                parentDataNode.addRelatedNode(currentGraphDataNode, 0, currentReferenceKinType.relationType, DataTypes.RelationLineType.square, null, null);
                                currentGraphDataNode.addRelatedNode(parentDataNode, 0, opposingRelationType, DataTypes.RelationLineType.square, null, null);
                                graphDataNodeList.put(fullKinTypeString, currentGraphDataNode);
                                currentGraphDataNode.isVisible = true;
                                // add any child nodes?
                                for (KinTermGroup kinTerms : kinTermsArray) {
                                    if (kinTerms.graphShow) {
                                        String kinTermLabel = kinTerms.getTermLabel(fullKinTypeString);
                                        if (kinTermLabel != null) {
                                            egoDataNode.addRelatedNode(currentGraphDataNode, 0, DataTypes.RelationType.none, DataTypes.RelationLineType.horizontalCurve, kinTerms.graphColour, kinTermLabel);
                                        }
                                    }
                                }
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
        graphDataNodeArray = graphDataNodeList.values().toArray(new EntityData[]{});
        sanguineSort();
    }
}
