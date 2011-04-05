package nl.mpi.kinnate.entityindexer;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter.KinType;

/**
 *  Document   : QueryParser
 *  Created on : Mar 31, 2011, 2:38:20 PM
 *  Author     : Peter Withers
 */
public class QueryParser implements EntityService {

    HashMap<String, EntityData> loadedGraphNodes;
    EntityCollection entityCollection;

    public enum ParserHighlightType {

        KinType, Comment, Error, Query, Unknown
    }

    public class ParserHighlight {

        public ParserHighlight nextHighlight = null;
        public ParserHighlightType highlight;
        public int startChar = 0;

        public ParserHighlight addHighlight(ParserHighlightType highlightType, int startChar) {

            this.highlight = highlightType;
            this.startChar = startChar;
            this.nextHighlight = new ParserHighlight();
            return this.nextHighlight;
        }
    }

    public QueryParser() {
        entityCollection = new EntityCollection();
        loadedGraphNodes = new HashMap<String, EntityData>();
    }

//    public String[][] getQueryStrings(String kinTypeString) {
//        String kinType = null;
//        ArrayList<String[]> queryTerms = new ArrayList<String[]>();
////        String[] queryParts = kinTypeString.split("(=\\[)|([\\]])");
//        String[] queryParts = kinTypeString.split("[\\]]");
////        String[] queryParts = kinTypeString.split("\n");
//        for (String querySection : queryParts) {
//            String[] subParts = querySection.split("=\\[");
//            if (subParts.length == 2) {
//                String queryText = subParts[1];
//                kinType = subParts[0];
//                //queryText = queryText.split("\\]")[0];
//                if (!queryText.contains("=")) {
//                    if (queryText.length() > 2) {
//                        queryTerms.add(new String[]{"*", queryText});
//                    }
//                } else {
//                    String[] queryTerm = queryText.split("=");
//                    if (queryTerm.length == 2) {
//                        if (queryTerm[0].length() > 2 && queryTerm[1].length() > 2) {
//                            queryTerms.add(new String[]{queryTerm[0], queryTerm[1]});
//                        }
//                    }
//                }
//            }
//        }
//        return queryTerms.toArray(new String[][]{});
//    }
    private void getNextRelations(HashMap<String, EntityData> createdGraphNodes, EntityData egoNode, ArrayList<KinType> remainingKinTypes, IndexerParameters indexParameters) {
        KinType currentKinType = remainingKinTypes.remove(0);
        for (EntityRelation entityRelation : egoNode.getDistinctRelateNodes()) {
            EntityData alterNode;
            if (createdGraphNodes.containsKey(entityRelation.alterUniqueIdentifier)) {
                alterNode = createdGraphNodes.get(entityRelation.alterUniqueIdentifier);
            } else {
                alterNode = entityCollection.getEntity(entityRelation.alterUniqueIdentifier, indexParameters);
                createdGraphNodes.put(entityRelation.alterUniqueIdentifier, alterNode);
            }
            alterNode.isVisible = true;

//            if (egoNode.relationMatchesType(entityRelation, currentKinType)) {
            // only traverse if the type matches
            if (remainingKinTypes.size() > 0) {
                getNextRelations(createdGraphNodes, alterNode, remainingKinTypes, indexParameters);
            }
//            }
        }
    }

    public EntityData[] getRelationsOfEgo(URI[] egoNodes, String[] uniqueIdentifiers, String[] kinTypeStrings, ParserHighlight[] parserHighlight, IndexerParameters indexParameters) throws EntityServiceException {
        if (indexParameters.valuesChanged) {
            indexParameters.valuesChanged = false;
            loadedGraphNodes = new HashMap<String, EntityData>();
        }
        KinTypeStringConverter kinTypeStringConverter = new KinTypeStringConverter();
//        kinTypeStringConverter.highlightComments(kinTypeStrings, parserHighlight);
//        QueryParser queryParser = new QueryParser();
        for (EntityData graphDataNode : loadedGraphNodes.values()) {
            graphDataNode.clearVisibility();
            graphDataNode.clearTempLabels();
        }
        int lineCounter = 0;
        for (String currentKinString : kinTypeStrings) {
            parserHighlight[lineCounter] = new ParserHighlight();
            ArrayList<KinTypeStringConverter.KinTypeElement> kinTypeElementArray = kinTypeStringConverter.getKinTypeElements(currentKinString, parserHighlight[lineCounter]);
            for (KinTypeStringConverter.KinTypeElement kinTypeElement : kinTypeElementArray) {
                // get all the entities with queries attached
                if (kinTypeElement.queryTerm != null) {
                    for (String currentFoundId : entityCollection.getEntityIdByTerm(kinTypeElement)) {
                        EntityData queryNode;
                        currentFoundId = currentFoundId.trim();
                        if (currentFoundId.length() > 0 /* make sure that non results do not get mistaken for an identifier */) {
                            if (loadedGraphNodes.containsKey(currentFoundId)) {
                                queryNode = loadedGraphNodes.get(currentFoundId);
                            } else {
                                queryNode = entityCollection.getEntity(currentFoundId, indexParameters);
                                loadedGraphNodes.put(currentFoundId, queryNode);
                            }
                            queryNode.isVisible = true;
                            kinTypeElement.entityData.add(queryNode);
                            queryNode.appendTempLabel(kinTypeElement.kinType.getCodeString());
                            if (kinTypeElement.kinType.isEgoType()) {
                                queryNode.isEgo = true; // there might be multiple types for a single entitiy
                            }
                        }
                    }
                } // todo: else get relations of x
                // todo: filter on the kin types
            }
            for (KinTypeStringConverter.KinTypeElement kinTypeElement : kinTypeElementArray) {
                // get all entities before and after each entity that has already found
                if (!kinTypeElement.entityData.isEmpty()) {
                    System.out.println("already loaded: " + kinTypeElement.kinType.getCodeString() + " : " + kinTypeElement.queryTerm);
                    for (KinTypeStringConverter.KinTypeElement adjacentKinType : new KinTypeStringConverter.KinTypeElement[]{kinTypeElement.prevType, kinTypeElement.nextType}) {
                        if (adjacentKinType != null && adjacentKinType.entityData.isEmpty()) {
                            for (EntityData currentEntity : kinTypeElement.entityData) {
                                for (EntityRelation entityRelation : currentEntity.getDistinctRelateNodes()) {
                                    EntityData alterNode;
                                    if (loadedGraphNodes.containsKey(entityRelation.alterUniqueIdentifier)) {
                                        alterNode = loadedGraphNodes.get(entityRelation.alterUniqueIdentifier);
                                    } else {
                                        alterNode = entityCollection.getEntity(entityRelation.alterUniqueIdentifier, indexParameters);
                                        loadedGraphNodes.put(entityRelation.alterUniqueIdentifier, alterNode);
                                    }
                                    // todo assess if the found node is of the correct kin type
                                    // todo: maybe add a chain so the prev and next of each loaded node can be reached here
                                    alterNode.isVisible = true;
                                    adjacentKinType.entityData.add(alterNode);
                                    alterNode.appendTempLabel(adjacentKinType.kinType.getCodeString());
                                }
                            }
                        }
                    }
                }
            }
            lineCounter++;
        }
        for (String currentEgoId : uniqueIdentifiers) {
            EntityData egoNode;
            if (loadedGraphNodes.containsKey(currentEgoId)) {
                egoNode = loadedGraphNodes.get(currentEgoId);
            } else {
                egoNode = entityCollection.getEntity(currentEgoId, indexParameters);
                loadedGraphNodes.put(currentEgoId, egoNode);
            }
            egoNode.isEgo = true;
            egoNode.isVisible = true;
            if (kinTypeStrings != null) {
                for (String currentKinString : kinTypeStrings) {
                    ArrayList<KinType> kinTypes = kinTypeStringConverter.getKinTypes(currentKinString);
                    getNextRelations(loadedGraphNodes, egoNode, kinTypes, indexParameters);
                }
            }
        }
        // set the alter node object from the unique identifier
        for (EntityData graphDataNode : loadedGraphNodes.values()) {
            for (EntityRelation nodeRelation : graphDataNode.getDistinctRelateNodes()) {
                nodeRelation.setAlterNode(loadedGraphNodes.get(nodeRelation.alterUniqueIdentifier));
            }
        }
        return loadedGraphNodes.values().toArray(new EntityData[]{});



//        ArrayList<GraphDataNode> graphDataNodes = new ArrayList<GraphDataNode>();
//        for (String entityIdentifier : uniqueIdentifiers) {
//            graphDataNodes.add(getEntity(entityIdentifier, indexParameters));
//        }
//        // todo: process the kin type strings
//        return graphDataNodes.toArray(new GraphDataNode[]{});
    }
}
