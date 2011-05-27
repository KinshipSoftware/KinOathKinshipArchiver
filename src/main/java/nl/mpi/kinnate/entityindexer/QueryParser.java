package nl.mpi.kinnate.entityindexer;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import nl.mpi.kinnate.kindata.DataTypes;
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

    public QueryParser(EntityData[] svgEntities) {
        entityCollection = new EntityCollection();
        loadedGraphNodes = new HashMap<String, EntityData>();
        if (svgEntities != null) {
            for (EntityData svgStoredEntity : svgEntities) {
                loadedGraphNodes.put(svgStoredEntity.getUniqueIdentifier(), svgStoredEntity);
            }
        }
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
    @Deprecated // it would seem not to be a good idea to try and use existing entities from an svg file when their relations might not exist, so we will allow the existing entities to be used on the graph but not for database actions
    public void primeWithEntities(EntityData[] preLoadedEntities) {
        for (EntityData currentEntity : preLoadedEntities) {
            if (!loadedGraphNodes.containsKey(currentEntity.getUniqueIdentifier())) {
                loadedGraphNodes.put(currentEntity.getUniqueIdentifier(), currentEntity);
            }
        }
    }

    private void getNextRelations(HashMap<String, EntityData> createdGraphNodes, EntityData egoNode, ArrayList<KinType> remainingKinTypes, IndexerParameters indexParameters) {
        if (remainingKinTypes.size() > 0) {
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

                getNextRelations(createdGraphNodes, alterNode, remainingKinTypes, indexParameters);
            }
//            }
        }
    }
    static public int foundOrder; // temp for testing // todo: remove testing labels

    private boolean loadMatchingRelations(EntityData immediateKinType, KinTypeStringConverter.KinTypeElement adjacentKinType, IndexerParameters indexParameters, int generationalDistance) { //EntityRelation entityRelation
        boolean visibleEntityFound = false;
        for (EntityRelation entityRelation : immediateKinType.getDistinctRelateNodes()) {
            EntityData adjacentEntity;
            if (loadedGraphNodes.containsKey(entityRelation.alterUniqueIdentifier)) {
                adjacentEntity = loadedGraphNodes.get(entityRelation.alterUniqueIdentifier);
            } else {
                adjacentEntity = entityCollection.getEntity(entityRelation.alterUniqueIdentifier, indexParameters);
                loadedGraphNodes.put(adjacentEntity.getUniqueIdentifier(), adjacentEntity);
            }
            entityRelation.setAlterNode(adjacentEntity);
            adjacentKinType.entityData.add(adjacentEntity);
            EntityData egoEntity;
            EntityData alterEntity;
            if (adjacentKinType.prevType.entityData.contains(immediateKinType)) {
                egoEntity = immediateKinType;
                alterEntity = adjacentEntity;
            } else {
                egoEntity = adjacentEntity;
                alterEntity = immediateKinType;
            }
            // todo: determine which is th ego and which is the alter
//            firstEntity
//                   or
//            secondEntity
            if (entityRelation.relationType.equals(DataTypes.RelationType.ancestor)) {
                generationalDistance--;
            }
            if (entityRelation.relationType.equals(DataTypes.RelationType.descendant)) {
                generationalDistance++;
            }
            if (new KinTypeStringConverter().compareRequiresNextRelation(adjacentEntity, adjacentKinType.kinType, entityRelation)) {
                if (loadMatchingRelations(adjacentEntity, adjacentKinType, indexParameters, generationalDistance)) {
                    alterEntity.isVisible = true;
                    //alterEntity.appendTempLabel("Meta G:" + generationalDistance + "F: " + foundOrder++);
                    visibleEntityFound = true;
                }
            } else if (new KinTypeStringConverter().compareRelationsToKinType(egoEntity, alterEntity, adjacentKinType.kinType, entityRelation, generationalDistance)) {
                // todo assess if the found node is of the correct kin type
                // todo: maybe add a chain so the prev and next of each loaded node can be reached here
                alterEntity.isVisible = true;
                alterEntity.addKinTypeString(adjacentKinType.kinType.getCodeString());
                //alterEntity.appendTempLabel(adjacentKinType.kinType.getCodeString() + " G:" + generationalDistance + "F: " + foundOrder++);
//                alterEntity.appendTempLabel(adjacentKinType.kinType.getCodeString());
                visibleEntityFound = true;
            }
        }
        return visibleEntityFound;
    }

    public EntityData[] getRelationsOfEgo(URI[] egoNodes, HashSet<String> egoIdentifiers, HashSet<String> requiredEntityIdentifiers, String[] kinTypeStrings, ParserHighlight[] parserHighlight, IndexerParameters indexParameters) throws EntityServiceException {
        foundOrder = 0; // temp for testing // todo: remove testing labels
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
                                loadedGraphNodes.put(queryNode.getUniqueIdentifier(), queryNode);
                            }
                            queryNode.isVisible = true;
                            kinTypeElement.entityData.add(queryNode);
//                            queryNode.appendTempLabel(kinTypeElement.kinType.getCodeString());
                            if (kinTypeElement.kinType.isEgoType()) {
                                queryNode.isEgo = true; // there might be multiple types for a single entitiy
                                new KinTypeStringConverter().setEgoKinTypeString(queryNode);
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
                        // note that this will reverse the kin type for one of the adjacent entities and this must be accounted for in the kin type comparison
                        if (adjacentKinType != null && adjacentKinType.entityData.isEmpty()) {
                            for (EntityData currentEntity : kinTypeElement.entityData) {
                                loadMatchingRelations(currentEntity, adjacentKinType, indexParameters, 0);
                            }
                        }
                    }
                }
            }
            lineCounter++;
        }
        // todo: the following could be removed if the ego nodes are replaces with the equavelent kin type string eg "E=Identifier"
        for (String currentEgoId : egoIdentifiers) {
            EntityData egoNode;
            if (loadedGraphNodes.containsKey(currentEgoId)) {
                egoNode = loadedGraphNodes.get(currentEgoId);
            } else {
                egoNode = entityCollection.getEntity(currentEgoId, indexParameters);
                loadedGraphNodes.put(egoNode.getUniqueIdentifier(), egoNode);
            }
            egoNode.isEgo = true;
            new KinTypeStringConverter().setEgoKinTypeString(egoNode);
            egoNode.isVisible = true;
            if (kinTypeStrings != null) {
                for (String currentKinString : kinTypeStrings) {
                    ArrayList<KinType> kinTypes = kinTypeStringConverter.getKinTypes(currentKinString);
                    getNextRelations(loadedGraphNodes, egoNode, kinTypes, indexParameters);
                }
            }
        }
        for (String currentEgoId : requiredEntityIdentifiers) {
            EntityData requiredNode;
            if (loadedGraphNodes.containsKey(currentEgoId)) {
                requiredNode = loadedGraphNodes.get(currentEgoId);
            } else {
                requiredNode = entityCollection.getEntity(currentEgoId, indexParameters);
                loadedGraphNodes.put(requiredNode.getUniqueIdentifier(), requiredNode);
            }
            requiredNode.isVisible = true;
        }
        // set the alter node object from the unique identifier
        for (EntityData graphDataNode : loadedGraphNodes.values()) {
            for (EntityRelation nodeRelation : graphDataNode.getDistinctRelateNodes()) {
                nodeRelation.setAlterNode(loadedGraphNodes.get(nodeRelation.alterUniqueIdentifier));
            }
        }
        // either strip out the entities that are not shown
//        ArrayList<EntityData> returnNodes = new ArrayList<EntityData>();
//        for (EntityData entityData : loadedGraphNodes.values()) {
//            if (entityData.isVisible) {
//                returnNodes.add(entityData);
//            }
//        }
//        return returnNodes.toArray(new EntityData[]{});
        // or return all of the loaded entities so they can be stored in the svg file
        return loadedGraphNodes.values().toArray(new EntityData[]{});


//        ArrayList<GraphDataNode> graphDataNodes = new ArrayList<GraphDataNode>();
//        for (String entityIdentifier : uniqueIdentifiers) {
//            graphDataNodes.add(getEntity(entityIdentifier, indexParameters));
//        }
//        // todo: process the kin type strings
//        return graphDataNodes.toArray(new GraphDataNode[]{});
    }
}
