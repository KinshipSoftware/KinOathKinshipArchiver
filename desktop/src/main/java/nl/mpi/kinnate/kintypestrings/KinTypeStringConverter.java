package nl.mpi.kinnate.kintypestrings;

import nl.mpi.kinnate.kindata.GraphSorter;
import nl.mpi.kinnate.kindata.EntityData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import nl.mpi.kinnate.kindata.DataTypes;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kintypestrings.ParserHighlight.ParserHighlightType;
import nl.mpi.kinnate.svg.DataStoreSvg;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;

/**
 *  Document   : KinTypeStringConverter
 *  Created on : Sep 29, 2010, 12:52:33 PM
 *  Author     : Peter Withers
 */
public class KinTypeStringConverter extends GraphSorter {

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
        for (KinType kinType : KinType.referenceKinTypes) {
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

//    @Deprecated
//    public boolean compareRequiresNextRelation(EntityData adjacentEntity, KinType requiredKinType, EntityRelation entityRelation) {
////        adjacentEntity.appendTempLabel("compareRequiresNextRelation" + "F: " + QueryParser.foundOrder++); // temp for testing // todo: remove testing labels
//        if (adjacentEntity.getSymbolType().equals(EntityData.SymbolType.union.name())) {
//            return true;
//        }
//        // todo: continue here....
//        if (requiredKinType.relationType.equals(DataTypes.RelationType.sibling) && entityRelation.relationType.equals(DataTypes.RelationType.ancestor)) {
//            return true;
//        }
//        return false;
//    }
//    @Deprecated
//    public boolean compareRelationsToKinType(EntityData egoEntity, EntityData alterEntity, KinType requiredKinType, EntityRelation entityRelation, int generationalDistance) {
////        egoEntity.appendTempLabel("compareRelationsToKinType-egoEntity" + "F: " + QueryParser.foundOrder++);
////        alterEntity.appendTempLabel("compareRelationsToKinType-alterEntity" + "F: " + QueryParser.foundOrder++);
//        // temp for testing // todo: remove testing labels
////        System.out.println("egoEntity.isEgo: " + egoEntity.isEgo);
////        System.out.println("alterEntity.isEgo: " + alterEntity.isEgo);
////        System.out.println("egoEntity.symbol: " + egoEntity.getSymbolType());
////        System.out.println("alterEntity.symbol: " + alterEntity.getSymbolType());
////        System.out.println("entityRelation.relationType: " + entityRelation.relationType);
////        System.out.println("entityRelation.symgenerationalDistancebol: " + entityRelation.generationalDistance);
//        // note that this will get the kin type reversed for one of the adjacent entities and this must be accounted for in the kin type comparison
//        // todo: note that the ego and alter are not correct labels
//        // this array will get the kin type reversed for one of the adjacent entities
//        if (egoEntity.isEgo && requiredKinType.isEgoType()) {// && alter.getSymbolType().equals(EntityData.SymbolType.triangle.name())) {
//            return true;
//        }
//        if (requiredKinType.relationType.equals(entityRelation.relationType)
//                && requiredKinType.symbolType.name().equals(alterEntity.getSymbolType())) {
//            return true;
//        }
//        return false;
//    }
    public ArrayList<KinTypeElement> getKinTypeElements(String consumableString, ParserHighlight parserHighlight) {
        int initialLength = consumableString.length();
        if (consumableString.startsWith("[")) {
            // todo: this is added so that a query can start with a [ since the initial kin type is redundant, however the addition of x= causes syntax highlighing issues partly because the ParserHighlight always creates an empty highlight ahead of the current one, but also it would be better to not be modifying this string
            consumableString = "x=" + consumableString;
        }
        ArrayList<KinTypeElement> kinTypeElementList = new ArrayList<KinTypeElement>();
        KinTypeElement previousElement = null;
        boolean foundKinType = true;
        String errorMessage = null;
        while (foundKinType && consumableString.length() > 0) {
            for (KinType currentReferenceKinType : KinType.referenceKinTypes) {
                foundKinType = false;
                if (consumableString.startsWith(currentReferenceKinType.codeString)) {
                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.KinType, initialLength - consumableString.length(), currentReferenceKinType.displayString);
                    KinTypeElement currentElement = new KinTypeElement();
                    if (previousElement != null) {
                        previousElement.nextType = currentElement;
                        currentElement.prevType = previousElement;
                    }
                    previousElement = currentElement;
                    currentElement.kinType = currentReferenceKinType;
                    consumableString = consumableString.substring(currentReferenceKinType.codeString.length());

                    if (consumableString.startsWith("=[")) {
                        // todo: Ticket #1087 Multiple query terms should be possible in the kin type string queries. Eg: Ef=[Kundarr]PM=[Louise] (Based on Joe's data).
                        int highlightPosition = initialLength - consumableString.length();
                        String highlightMessage = "Query: ";
                        consumableString = consumableString.substring("=".length());
                        while (consumableString.startsWith("[")) {
                            // todo: allow multiple terms such as "=[foo][bar]" or "=[foo][bar][NAME=Bob]"
                            int queryStart = "[".length();
                            int queryEnd = consumableString.indexOf("]");
                            if (queryEnd == -1) {
                                // if the terms are incomplete then ignore the rest of the line
                                highlightMessage += "No closing bracket ']' found";
                                errorMessage = highlightMessage;
                                foundKinType = false;
                                break;
                            }
                            if (queryEnd - queryStart < 3) {
                                // the query string must be more than 2 chars
                                highlightMessage += "Query must be over 2 chars long";
                                errorMessage = highlightMessage;
                                foundKinType = false;
                                break;
                            }
                            if (currentElement.queryTerm == null) {
                                currentElement.queryTerm = new ArrayList<String[]>();
                            }
                            String queryText = consumableString.substring(queryStart, queryEnd);
                            consumableString = consumableString.substring(queryEnd + 1);
                            if (!queryText.contains("=")) {
                                currentElement.queryTerm.add(new String[]{"*", queryText});
                                highlightMessage += "Any field containing '" + queryText + "'";
                            } else {
                                String[] queryTerm = queryText.split("=");
                                if (queryTerm.length == 2) {
                                    if (queryTerm[0].length() > 2 && queryTerm[1].length() > 2) {
                                        // todo: *:* like namespace handling might be required here
                                        currentElement.queryTerm.add(new String[]{"*:" + queryTerm[0].replaceAll("\\.", "/*:"), queryTerm[1]});
                                        highlightMessage += "Only the field '" + queryTerm[0] + "' containing '" + queryTerm[1] + "'";
                                    }
                                }
                            }
                        }
                        parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Query, highlightPosition, highlightMessage);
                    }
                    kinTypeElementList.add(currentElement);
                    foundKinType = true;
                    break;
                }
            }
        }
        if (!foundKinType && !consumableString.startsWith("#")) {
            parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Error, initialLength - consumableString.length(), errorMessage);
        }
        if (consumableString.contains("#")) {
            // check for any comments
            parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Comment, initialLength - consumableString.length() + consumableString.indexOf("#"), null);
        }
        return kinTypeElementList;
    }

    public ArrayList<KinType> getKinTypes(String consumableString) {
        ArrayList<KinType> kinTypeList = new ArrayList<KinType>();
        boolean foundKinType = true;
        while (foundKinType && consumableString.length() > 0) {
            for (KinType currentReferenceKinType : KinType.referenceKinTypes) {
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

    public void readKinTypes(String[] inputStringArray, KinTermGroup[] kinTermsArray, DataStoreSvg dataStoreSvg, ParserHighlight[] parserHighlightArray) {
        HashMap<UniqueIdentifier, EntityData> namedEntitiesMap = new HashMap<UniqueIdentifier, EntityData>();
        HashSet<EntityData> allEntitiesSet = new HashSet<EntityData>();
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
        ArrayList<EntityData> egoDataNodeList = new ArrayList<EntityData>();
        int lineCounter = -1;
        for (String inputString : inputStringList) {
            lineCounter++;
//            System.out.println("inputString: " + inputString);
            ParserHighlight parserHighlight = new ParserHighlight();
            if (parserHighlightArray.length > lineCounter) {
                // if kin type strings have been added from the kin terms then there will be no space in the array
                parserHighlightArray[lineCounter] = parserHighlight;
            }
            if (inputString != null && inputString.length() > 0) {
                if (inputString.startsWith("#")) {
                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Comment, 0, null);
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
                    EntityData egoDataNode = null; // todo: replace this with the egoDataNodeList, currently waiting on the kin type strings update below
                    String fullKinTypeString = "";
                    while (consumableString.length() > 0) {
                        int parserHighlightPosition = initialLength - consumableString.length();
                        boolean kinTypeFound = false;
                        for (KinType currentReferenceKinType : KinType.referenceKinTypes) {
                            if (consumableString.startsWith(currentReferenceKinType.codeString)) {
                                String previousConsumableString = consumableString;
//                                if (currentReferenceKinType.isEgoType()) {
//                                    fullKinTypeString = "";
//                                }
                                if (currentReferenceKinType.relationType.equals(DataTypes.RelationType.none) && parentDataNode != null) {
                                    // prevent multiple egos on one line
                                    // going from one kin type to a second ego cannot specify the relation to the second ego and hence such syntax is not workable
//                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Error, parserHighlightPosition);
                                    // because kinTypeFound is not set then the error highlight will be added
                                    break;
                                } else {
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.KinType, parserHighlightPosition, currentReferenceKinType.displayString);
                                }
                                String currentKinTypeString = consumableString;
                                consumableString = consumableString.substring(currentReferenceKinType.codeString.length());
                                consumableString = consumableString.replaceAll("^[-\\+\\d]*", "");
                                currentKinTypeString = currentKinTypeString.substring(0, currentKinTypeString.length() - consumableString.length());
                                String kinTypeModifier = currentKinTypeString.substring(currentReferenceKinType.codeString.length());
                                System.out.println("kinTypeFound: " + currentReferenceKinType.codeString);
                                System.out.println("consumableString: " + consumableString);
//                                System.out.println("fullKinTypeString: " + fullKinTypeString);
                                EntityData currentGraphDataNode = null;
                                fullKinTypeString = fullKinTypeString + previousConsumableString.substring(0, previousConsumableString.length() - consumableString.length());
                                LabelStringsParser labelStringsParser = new LabelStringsParser(consumableString, parentDataNode, currentKinTypeString);
                                if (labelStringsParser.userDefinedIdentifierFound) {
                                    // add a highlight for the label section
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Query, initialLength - consumableString.length(), "Label text");
                                    consumableString = labelStringsParser.remainingInputString;
                                    // get any previously created entity with the same user defined identifier if it exists
                                    currentGraphDataNode = namedEntitiesMap.get(labelStringsParser.uniqueIdentifier);
                                    // todo: check the gender or any other testable attrubute and give syntax highlight error if found...
                                }
                                if (labelStringsParser.uidStartLocation > -1) {
                                    // add a highlight for the user id section
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Parameter, initialLength - labelStringsParser.uidStartLocation, "User defined identifier");
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Query, initialLength - labelStringsParser.uidEndLocation, "Label text");
                                }
                                if (labelStringsParser.dateLocation > -1) {
                                    // add a highlight for the date section
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Parameter, initialLength - labelStringsParser.dateLocation, "Date of birth/death");
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Query, initialLength - labelStringsParser.dateEndLocation, "Label text");
                                }
                                if (labelStringsParser.dateError) {
                                    // add a highlight for the date error section
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Error, initialLength - labelStringsParser.dateLocation, "Incorrect date format:"
                                            + " Valid formats are yyyy, yyyy/mm, yyyy/mm/dd with the birth date followed by death date eg yyyy/mm/dd-yyyy/mm/dd");
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Query, initialLength - labelStringsParser.dateEndLocation, "Label text");
                                }
                                if (currentGraphDataNode == null) {
                                    if (parentDataNode != null && !labelStringsParser.userDefinedIdentifierFound /* if a user defined identifier has been specified then skip this and always create or reuse that named entity */) {
                                        // look for any existing relaitons that match the required kin type
                                        for (EntityRelation entityRelation : parentDataNode.getAllRelations()) {
                                            if (currentReferenceKinType.matchesRelation(entityRelation, kinTypeModifier)) {
                                                currentGraphDataNode = entityRelation.getAlterNode();
                                                currentGraphDataNode.addKinTypeString(fullKinTypeString);
                                                break;
                                            }
                                        }
                                    } else if (parentDataNode == null && !labelStringsParser.userDefinedIdentifierFound) { /* also skip this if a user defined identifier has been given */
                                        // look through all the known egos to find a match (must be an ego to match), use case could be: Em:Richard:|Em which should re use the existing ego
                                        for (EntityData egoEntity : egoDataNodeList) {
                                            if (currentReferenceKinType.matchesEgoEntity(egoEntity, kinTypeModifier)) {
                                                currentGraphDataNode = egoEntity;
                                                currentGraphDataNode.addKinTypeString(fullKinTypeString);
                                                break;
                                            }
                                        }
                                    }
                                    if (currentGraphDataNode == null) {
                                        currentGraphDataNode = new EntityData(labelStringsParser, fullKinTypeString, currentReferenceKinType.symbolType, currentReferenceKinType.isEgoType());
                                        if (currentGraphDataNode.isEgo) {
                                            egoDataNodeList.add(currentGraphDataNode);
                                        }
                                        if (labelStringsParser.userDefinedIdentifierFound) {
                                            namedEntitiesMap.put(labelStringsParser.uniqueIdentifier, currentGraphDataNode);
                                        }
                                        allEntitiesSet.add(currentGraphDataNode);
                                    }
                                }
                                if (currentGraphDataNode.isEgo) {
                                    egoDataNode = currentGraphDataNode;
                                }
                                if (parentDataNode != null && !currentReferenceKinType.relationType.equals(DataTypes.RelationType.none)) {
                                    // allow relations only for kin types that do not start the kin type string
                                    parentDataNode.addRelatedNode(currentGraphDataNode, currentReferenceKinType.relationType, DataTypes.RelationLineType.sanguineLine, null, null);
                                }
                                currentGraphDataNode.isVisible = true;
                                // add any child nodes?
                                for (KinTermGroup kinTerms : kinTermsArray) {
                                    if (kinTerms.graphShow && egoDataNode != null) {
                                        // todo: replace this with a loop over egoDataNodeList and then calculate the actual kin type strings for each one rather than the user entered ones used here
                                        // todo: this should probably be done after all nodes have been created so that subsequent relations are taken into account when calculating the kin terms
                                        for (String kinTermLabel : kinTerms.getTermLabel(fullKinTypeString)) {
                                            // todo: this could be running too many times, maybe check for efficiency
                                            currentGraphDataNode.addKinTermString(kinTermLabel, kinTerms.graphColour);
                                            egoDataNode.addRelatedNode(currentGraphDataNode, DataTypes.RelationType.none, DataTypes.RelationLineType.kinTermLine, kinTerms.graphColour, kinTermLabel);
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
                                parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Comment, initialLength - consumableString.length(), null);
                            } else {
                                parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Error, initialLength - consumableString.length(), "Incorrect syntax");
                                int commentPosition = consumableString.indexOf("#");
                                if (commentPosition > 0) {
                                    // allow comments after this point
                                    consumableString = consumableString.substring(commentPosition);
                                    parserHighlight = parserHighlight.addHighlight(ParserHighlightType.Comment, initialLength - consumableString.length(), null);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        // make sure that no duplicates are returned, these duplicates may exist from strings like EmMS|EmB which map to the same individual but there are two kin type strings for it and hence two entries
        super.setEntitys(allEntitiesSet.toArray(new EntityData[]{}));
    }
}
