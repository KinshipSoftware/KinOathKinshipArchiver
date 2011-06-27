package nl.mpi.kinnate.kintypestrings;

import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.EntityData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kintypestrings.ParserHighlight.ParserHighlightType;
import nl.mpi.kinnate.svg.DataStoreSvg;

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
            // todo: this could be better handled by adding a boolean: isego to each KinType
            return codeString.contains("E");
        }

        public boolean matchesRelation(EntityRelation entityRelation) {
            if (entityRelation.getAlterNode().isEgo != this.isEgoType()) {
                return false;
            }
            if (!relationType.equals(entityRelation.relationType)) {
                return false;
            }
            if (!symbolType.name().equals(entityRelation.getAlterNode().getSymbolType())) {
                return false;
            }
            return true;
        }
    }
    private KinType[] referenceKinTypes = new KinType[]{
        // other types
        // todo: the gendered ego kin types Em and Ef are probably not correct and should be verified
        new KinType("Ef", DataTypes.RelationType.none, EntityData.SymbolType.circle),
        new KinType("Em", DataTypes.RelationType.none, EntityData.SymbolType.triangle),
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
        new KinType("Sb", DataTypes.RelationType.sibling, EntityData.SymbolType.square), //todo: are Sp and Sb correct?
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
        new KinType("E", DataTypes.RelationType.none, EntityData.SymbolType.square),
        new KinType("C", DataTypes.RelationType.descendant, EntityData.SymbolType.square),
        //        new KinType("X", DataTypes.RelationType.none, EntityData.SymbolType.none) // X is intended to indicate unknown or no type, for instance this is used after import to add all nodes to the graph

        // non ego types to be used to start a kin type string but cannot be used except at the beginning
        new KinType("m", DataTypes.RelationType.none, EntityData.SymbolType.triangle),
        new KinType("f", DataTypes.RelationType.none, EntityData.SymbolType.circle),
        new KinType("x", DataTypes.RelationType.none, EntityData.SymbolType.square)
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

    public void setEgoKinTypeString(EntityData entityData) {
        for (KinType kinType : referenceKinTypes) {
            if (kinType.isEgoType() && kinType.symbolType.name().equals(entityData.getSymbolType())) {
                entityData.addKinTypeString(kinType.codeString);
            }
        }
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

    @Deprecated
    public boolean compareRequiresNextRelation(EntityData adjacentEntity, KinType requiredKinType, EntityRelation entityRelation) {
//        adjacentEntity.appendTempLabel("compareRequiresNextRelation" + "F: " + QueryParser.foundOrder++); // temp for testing // todo: remove testing labels
        if (adjacentEntity.getSymbolType().equals(EntityData.SymbolType.union.name())) {
            return true;
        }
        // todo: continue here....
        if (requiredKinType.relationType.equals(DataTypes.RelationType.sibling) && entityRelation.relationType.equals(DataTypes.RelationType.ancestor)) {
            return true;
        }
        return false;
    }

    @Deprecated
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
        if (egoEntity.isEgo && requiredKinType.isEgoType()) {// && alter.getSymbolType().equals(EntityData.SymbolType.triangle.name())) {
            return true;
        }
        if (requiredKinType.relationType.equals(entityRelation.relationType)
                && requiredKinType.symbolType.name().equals(alterEntity.getSymbolType())) {
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
                                        // todo: *:* like namespace handling might be required here
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

    private String parseLabelStrings(String inputString) {
        if (inputString.startsWith(":")) {
            String[] inputStringParts = inputString.split(":");
            if (inputStringParts.length > 0) {
                return inputStringParts[1];
            }
        }
        return null;
    }

    public void readKinTypes(String[] inputStringArray, KinTermGroup[] kinTermsArray, DataStoreSvg dataStoreSvg, ParserHighlight[] parserHighlightArray) {
        HashMap<String, EntityData> graphDataNodeList = new HashMap<String, EntityData>();
//        EntityData egoDataNode = new EntityData("E", "E", "E", EntityData.SymbolType.square, new String[]{}, true);
//        graphDataNodeList.put("E", egoDataNode);
//        egoDataNode.isVisible = true;
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
        int lineCounter = -1;
        for (String inputString : inputStringList) {
            int insertedEgoOffset = 0;
            lineCounter++;
            boolean egoNodeFound = false;
//            System.out.println("inputString: " + inputString);
            ParserHighlight parserHighlight = new ParserHighlight();
            if (parserHighlightArray.length > lineCounter) {
                // if kin type strings have been added from the kin terms then there will be no space in the array
                parserHighlightArray[lineCounter] = parserHighlight;
            }
            if (inputString != null && inputString.length() > 0) {
                if (inputString.startsWith("#")) {
                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Comment, 0);
                } else {
//                    while (inputString.matches("^[\\s]")) {
//                        inputString = inputString.substring(1);
//                        insertedEgoOffset--;
//                    }
//                    inputString = inputString.trim();
//                    if (!inputString.startsWith("E")) {
//                        inputString = "E" + inputString;
//                        insertedEgoOffset++;
//                    }
                    int initialLength = inputString.length();
                    String consumableString = inputString;
                    EntityData parentDataNode = null;
                    EntityData egoDataNode = null;
                    String fullKinTypeString = "";
                    while (consumableString.length() > 0) {
                        int parserHighlightPosition = initialLength - consumableString.length() - insertedEgoOffset;
                        boolean kinTypeFound = false;
                        for (KinType currentReferenceKinType : referenceKinTypes) {
                            if (consumableString.startsWith(currentReferenceKinType.codeString)) {
                                String previousConsumableString = consumableString;
//                                if (currentReferenceKinType.isEgoType()) {
//                                    fullKinTypeString = "";
//                                }
                                if (currentReferenceKinType.relationType.equals(DataTypes.RelationType.none) && parentDataNode != null) {
                                    // prevent multiple egos on one line
                                    // going from one kin type to a second ego cannot specify the relation to the second ego and hence such syntax is not workable
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Error, parserHighlightPosition);
                                    int commentPosition = consumableString.indexOf("#");
                                    if (commentPosition > 0) {
                                        // allow comments after this point
                                        consumableString = consumableString.substring(commentPosition);
                                    }
                                    break;
                                } else {
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.KinType, parserHighlightPosition);
                                }
                                if (currentReferenceKinType.isEgoType()) {
                                    egoNodeFound = true;
                                }
                                consumableString = consumableString.substring(currentReferenceKinType.codeString.length());
                                consumableString = consumableString.replaceAll("^[-\\+\\d]*", "");
                                System.out.println("kinTypeFound: " + currentReferenceKinType.codeString);
                                System.out.println("consumableString: " + consumableString);
//                                System.out.println("fullKinTypeString: " + fullKinTypeString);
                                EntityData currentGraphDataNode = null;
                                String identifierString = parseLabelStrings(consumableString);
                                String labelStrings[];
//                                fullKinTypeString = fullKinTypeString + currentReferenceKinType.codeString;
                                fullKinTypeString = fullKinTypeString + previousConsumableString.substring(0, previousConsumableString.length() - consumableString.length());
                                if (null != identifierString) {
                                    // if an identifier has been specified then use it as the unique identifier
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Query, initialLength - consumableString.length() - insertedEgoOffset);
                                    if (consumableString.length() < identifierString.length() + 2) {
                                        consumableString = "";
                                    } else {
                                        consumableString = consumableString.substring(identifierString.length() + 2);
                                    }
                                    labelStrings = identifierString.split(";");
//                                    fullKinTypeString = ""; //fullKinTypeString + ":" + identifierString + ":";
                                } else {
//                                    fullKinTypeString = fullKinTypeString + previousConsumableString.substring(0, previousConsumableString.length() - consumableString.length());
//                                    fullKinTypeString = inputString.substring(0, inputString.length() - consumableString.length());
                                    // if no identifier has been specified then use the full kin type string
                                    identifierString = fullKinTypeString;
                                    labelStrings = new String[]{};
                                }
                                if (graphDataNodeList.containsKey(identifierString)) {
                                    currentGraphDataNode = graphDataNodeList.get(identifierString);
                                    if (currentGraphDataNode.isEgo) {
                                        egoDataNode = currentGraphDataNode;
//                                    fullKinTypeString = fullKinTypeString.replaceAll("^E[mf]", "");
                                    }
                                    // todo: check the gender or any other testable attrubute and give syntax highlight error if found...
                                } else {
                                    if (parentDataNode != null) {
                                        // look for any existing relaitons that match the required kin type
                                        for (EntityRelation entityRelation : parentDataNode.getAllRelations()) {
                                            if (currentReferenceKinType.matchesRelation(entityRelation)) {
                                                currentGraphDataNode = entityRelation.getAlterNode();
                                                break;
                                            }
                                        }
                                    }
                                    if (currentGraphDataNode == null) {
                                        currentGraphDataNode = new EntityData(identifierString, null, fullKinTypeString, currentReferenceKinType.symbolType, labelStrings, currentReferenceKinType.isEgoType());
                                    }
                                    if (currentGraphDataNode.isEgo) {
                                        egoDataNode = currentGraphDataNode;
//                                    fullKinTypeString = fullKinTypeString.replaceAll("^E[mf]", "");
                                    }
                                }
                                if (parentDataNode != null && !currentReferenceKinType.relationType.equals(DataTypes.RelationType.none)) {
                                    // allow relations only for kin types that do not start the kin type string
                                    parentDataNode.addRelatedNode(currentGraphDataNode, 0, currentReferenceKinType.relationType, DataTypes.RelationLineType.sanguineLine, null, null);
                                }
                                graphDataNodeList.put(identifierString, currentGraphDataNode);
                                currentGraphDataNode.isVisible = true;
                                // add any child nodes?
                                for (KinTermGroup kinTerms : kinTermsArray) {
                                    if (kinTerms.graphShow && egoDataNode != null) {
                                        for (String kinTermLabel : kinTerms.getTermLabel(fullKinTypeString)) {
                                            // todo: this could be running too many times, maybe check for efficiency
                                            currentGraphDataNode.addKinTermString(kinTermLabel, kinTerms.graphColour);
                                            egoDataNode.addRelatedNode(currentGraphDataNode, 0, DataTypes.RelationType.none, DataTypes.RelationLineType.kinTermLine, kinTerms.graphColour, kinTermLabel);
                                        }
                                    }
                                }
                                parentDataNode = currentGraphDataNode;
                                kinTypeFound = true;
                                break;
                            }
                        }
                        if (kinTypeFound == false) {
                            consumableString = consumableString.replaceAll("^[\\s]*", "");
                            if (consumableString.startsWith("#")) {
                                parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Comment, initialLength - consumableString.length() - insertedEgoOffset);
                            } else {
                                parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Error, initialLength - consumableString.length() - insertedEgoOffset);
                            }
                            break;
                        }
                    }
                }
            }
        }
        super.setEntitys(graphDataNodeList.values().toArray(new EntityData[]{}));
    }
}
