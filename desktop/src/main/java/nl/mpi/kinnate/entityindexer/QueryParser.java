package nl.mpi.kinnate.entityindexer;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JProgressBar;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.kindata.EntityRelation;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.ParserHighlight;
import nl.mpi.kinnate.svg.DataStoreSvg;

/**
 *  Document   : QueryParser
 *  Created on : Mar 31, 2011, 2:38:20 PM
 *  Author     : Peter Withers
 */
public class QueryParser implements EntityService {

    HashMap<UniqueIdentifier, EntityData> loadedGraphNodes;
    EntityCollection entityCollection;

    public QueryParser(EntityData[] svgEntities) {
        entityCollection = new EntityCollection();
        loadedGraphNodes = new HashMap<UniqueIdentifier, EntityData>();
        if (svgEntities != null) {
            for (EntityData svgStoredEntity : svgEntities) {
                // todo: consider if having the entities stored in two locations (admitedly this one is a hash map, the other and array) but maybe this could be confusing?
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
        // todo: set the relations of this node
    }
//    private void getNextRelations(EntityData egoNode, ArrayList<KinType> remainingKinTypes, IndexerParameters indexParameters) {
//        if (remainingKinTypes.size() > 0) {
//            KinType currentKinType = remainingKinTypes.remove(0);
//            for (EntityRelation entityRelation : egoNode.getDistinctRelateNodes()) {
//                EntityData alterNode;
//                if (loadedGraphNodes.containsKey(entityRelation.alterUniqueIdentifier)) {
//                    alterNode = loadedGraphNodes.get(entityRelation.alterUniqueIdentifier);
//                } else {
//                    alterNode = entityCollection.getEntity(entityRelation.alterUniqueIdentifier, indexParameters);
//                    loadedGraphNodes.put(entityRelation.alterUniqueIdentifier, alterNode);
//                }
//                alterNode.isVisible = true;
//
////            if (egoNode.relationMatchesType(entityRelation, currentKinType)) {
//                // only traverse if the type matches
//
//                getNextRelations(alterNode, remainingKinTypes, indexParameters);
//            }
////            }
//        }
//    }
    static public int foundOrder; // temp for testing // todo: remove testing labels

//    private boolean loadMatchingRelations(EntityData immediateKinType, KinTypeStringConverter.KinTypeElement adjacentKinType, IndexerParameters indexParameters, int generationalDistance) { //EntityRelation entityRelation
//        boolean visibleEntityFound = false;
//        for (EntityRelation entityRelation : immediateKinType.getDistinctRelateNodes()) {
//            EntityData adjacentEntity;
//            if (loadedGraphNodes.containsKey(entityRelation.alterUniqueIdentifier)) {
//                adjacentEntity = loadedGraphNodes.get(entityRelation.alterUniqueIdentifier);
//            } else {
//                adjacentEntity = entityCollection.getEntity(entityRelation.alterUniqueIdentifier, indexParameters);
//                loadedGraphNodes.put(adjacentEntity.getUniqueIdentifier(), adjacentEntity);
//            }
//            entityRelation.setAlterNode(adjacentEntity);
//            adjacentKinType.entityData.add(adjacentEntity);
//            EntityData egoEntity;
//            EntityData alterEntity;
//            if (adjacentKinType.prevType.entityData.contains(immediateKinType)) {
//                egoEntity = immediateKinType;
//                alterEntity = adjacentEntity;
//            } else {
//                egoEntity = adjacentEntity;
//                alterEntity = immediateKinType;
//            }
//            // todo: determine which is th ego and which is the alter
////            firstEntity
////                   or
////            secondEntity
//            if (entityRelation.relationType.equals(DataTypes.RelationType.ancestor)) {
//                generationalDistance--;
//            }
//            if (entityRelation.relationType.equals(DataTypes.RelationType.descendant)) {
//                generationalDistance++;
//            }
//            if (new KinTypeStringConverter().compareRequiresNextRelation(adjacentEntity, adjacentKinType.kinType, entityRelation)) {
//                if (loadMatchingRelations(adjacentEntity, adjacentKinType, indexParameters, generationalDistance)) {
//                    alterEntity.isVisible = true;
//                    //alterEntity.appendTempLabel("Meta G:" + generationalDistance + "F: " + foundOrder++);
//                    visibleEntityFound = true;
//                }
//            } else if (new KinTypeStringConverter().compareRelationsToKinType(egoEntity, alterEntity, adjacentKinType.kinType, entityRelation, generationalDistance)) {
//                // todo assess if the found node is of the correct kin type
//                // todo: maybe add a chain so the prev and next of each loaded node can be reached here
//                alterEntity.isVisible = true;
//                alterEntity.addKinTypeString(adjacentKinType.kinType.getCodeString());
//                //alterEntity.appendTempLabel(adjacentKinType.kinType.getCodeString() + " G:" + generationalDistance + "F: " + foundOrder++);
////                alterEntity.appendTempLabel(adjacentKinType.kinType.getCodeString());
//                visibleEntityFound = true;
//            }
//        }
//        return visibleEntityFound;
//    }
    public EntityData[] processKinTypeStrings(URI[] egoNodes, String[] kinTypeStrings, ParserHighlight[] parserHighlight, IndexerParameters indexParameters, DataStoreSvg dataStoreSvg, JProgressBar progressBar) throws EntityServiceException {
        foundOrder = 0; // temp for testing // todo: remove testing labels
        if (indexParameters.valuesChanged) {
            // discard all entity data from previous queries
            indexParameters.valuesChanged = false;
            loadedGraphNodes = new HashMap<UniqueIdentifier, EntityData>();
        }
        KinTypeStringConverter kinTypeStringConverter = new KinTypeStringConverter(dataStoreSvg);
//        kinTypeStringConverter.highlightComments(kinTypeStrings, parserHighlight);
//        QueryParser queryParser = new QueryParser();
        for (EntityData graphDataNode : loadedGraphNodes.values()) {
            // clear flags and labels from any previous queries
            graphDataNode.clearVisibility();
            graphDataNode.clearTempLabels();
        }
        int totalProgressRequired = dataStoreSvg.requiredEntities.size() /*+ egoIdentifiers.size()*/ + kinTypeStrings.length;
        progressBar.setMaximum(totalProgressRequired);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        // only show specific progress when it is meaningful
        progressBar.setIndeterminate(totalProgressRequired < 3);
        int lineCounter = -1;
        // process each line of the users input
        for (String currentKinString : kinTypeStrings) {
            lineCounter++;
            parserHighlight[lineCounter] = new ParserHighlight();
            // convert the line into  kin types with queries if provided
            ArrayList<KinTypeStringConverter.KinTypeElement> kinTypeElementArray = kinTypeStringConverter.getKinTypeElements(currentKinString, parserHighlight[lineCounter]);
            for (KinTypeStringConverter.KinTypeElement kinTypeElement : kinTypeElementArray) {
                // handle all queries getting all matching entities
                if (kinTypeElement.queryTerm != null) {
                    for (UniqueIdentifier currentFoundId : entityCollection.getEntityIdByTerm(kinTypeElement)) {
                        EntityData queryNode;
//                        currentFoundId = currentFoundId.trim();
//                        if (currentFoundId.length() > 0 /* make sure that non results do not get mistaken for an identifier */) {
                        if (loadedGraphNodes.containsKey(currentFoundId)) {
                            queryNode = loadedGraphNodes.get(currentFoundId);
                        } else {
                            queryNode = entityCollection.getEntity(currentFoundId, indexParameters);
                            loadedGraphNodes.put(queryNode.getUniqueIdentifier(), queryNode);
                        }
                        queryNode.isVisible = true;
                        kinTypeElement.entityData.add(queryNode);
//                        queryNode.appendTempLabel(kinTypeElement.kinType.getCodeString());
                        if (kinTypeElement.kinType.isEgoType()) {
                            queryNode.isEgo = true; // there might be multiple types for a single entitiy
                            new KinTypeStringConverter(dataStoreSvg).setEgoKinTypeString(queryNode);
                        }
                    }
                }
            }
            if (kinTypeElementArray.size() > 0 && kinTypeElementArray.get(0).queryTerm == null) {
                // get any user specified ego nodes and add them as the first kin type if specified
                KinTypeStringConverter.KinTypeElement firstKinType = kinTypeElementArray.get(0);
                if (firstKinType.kinType.isEgoType()) {
                    // the following could be removed if the ego nodes are replaces with the equavelent kin type string eg "E=Identifier" and E at the begining of the line was mandatory (neither are likely to be the case)
                    for (UniqueIdentifier currentEgoId : dataStoreSvg.egoEntities) {
                        // load all entities specified as ego nodes
                        // if a query was not found on the first kintype then add all gernder matching egos to the first kin type
                        EntityData egoNode;
                        if (loadedGraphNodes.containsKey(currentEgoId)) {
                            egoNode = loadedGraphNodes.get(currentEgoId);
                        } else {
                            egoNode = entityCollection.getEntity(currentEgoId, indexParameters);
                            loadedGraphNodes.put(egoNode.getUniqueIdentifier(), egoNode);
                        }
                        egoNode.isEgo = true;
                        egoNode.isVisible = true;
                        if (firstKinType.kinType.matchesEgoEntity(egoNode, null)) {
                            firstKinType.entityData.add(egoNode);
                        }
                    }
                } else {
                    // todo: if no ego type has been specified then prepend all egos and match thie first kin type to the ego relations
                }
            }
            for (KinTypeStringConverter.KinTypeElement kinTypeElement : kinTypeElementArray) {
                // get all entities before and after each entity that has already found
                for (EntityData kinTypeEntityData : kinTypeElement.entityData) {
                    for (EntityRelation entityRelationToLoad : kinTypeEntityData.getRelatedNodesToBeLoaded()) {
                        // make sure all relation data is loaded and set in the relations of this entity
                        EntityData relatedNode;
                        if (loadedGraphNodes.containsKey(entityRelationToLoad.alterUniqueIdentifier)) {
                            relatedNode = loadedGraphNodes.get(entityRelationToLoad.alterUniqueIdentifier);
                        } else {
                            relatedNode = entityCollection.getEntity(entityRelationToLoad.alterUniqueIdentifier, indexParameters);
                            loadedGraphNodes.put(relatedNode.getUniqueIdentifier(), relatedNode);
                        }
                        entityRelationToLoad.setAlterNode(relatedNode);
                    }
                    for (EntityRelation entityRelation : kinTypeEntityData.getDistinctRelateNodes()) {
                        // compare each relation to the required kin type
                        if (kinTypeElement.nextType != null) {
                            if (kinTypeElement.nextType.kinType.matchesRelation(entityRelation, null)) { // todo: this should also take into account the modifier eg -, +, -1, -3, +2 etc..
                                kinTypeElement.nextType.entityData.add(entityRelation.getAlterNode());
                                entityRelation.getAlterNode().isVisible = true;
//                                entityRelation.getAlterNode().appendTempLabel(kinTypeElement.kinType.getCodeString());
                            }
                        }
                        // todo: continue with the kinTypeElement.prevType

                    }


//                    System.out.println("already loaded: " + kinTypeElement.kinType.getCodeString() + " : " + kinTypeElement.queryTerm);
//                    for (KinTypeStringConverter.KinTypeElement adjacentKinType : new KinTypeStringConverter.KinTypeElement[]{kinTypeElement.prevType, kinTypeElement.nextType}) {
//                        // note that this will reverse the kin type for one of the adjacent entities and this must be accounted for in the kin type comparison
//                        if (adjacentKinType != null) { //  && adjacentKinType.entityData.isEmpty(): checking that entity data is empty could miss cases where multiple entities match a kin term
//                            for (EntityData currentEntity : kinTypeElement.entityData) {
//                                // todo: when there are two queries on one line E[Jane]MFBDS[Jimmy] the parser will probably follow the relations and join at B which if this does not match both trails then both trails should be dumped
//                                loadMatchingRelations(currentEntity, adjacentKinType, indexParameters, 0);
//                                queryNode.appendTempLabel(kinTypeElement.kinType.getCodeString());
//                            }
//                        }
//                    }
                }
            }
            progressBar.setValue(progressBar.getValue() + 1);
        }
        for (Iterator<UniqueIdentifier> iterator = dataStoreSvg.requiredEntities.iterator(); iterator.hasNext();) {
            UniqueIdentifier currentEgoId = iterator.next();
            // load and show any mandatory entities
            EntityData requiredNode;
            if (loadedGraphNodes.containsKey(currentEgoId)) {
                requiredNode = loadedGraphNodes.get(currentEgoId);
            } else {
                requiredNode = entityCollection.getEntity(currentEgoId, indexParameters);
                loadedGraphNodes.put(requiredNode.getUniqueIdentifier(), requiredNode);
            }
            requiredNode.isVisible = true;
            progressBar.setValue(progressBar.getValue() + 1);
        }
        // set the alter entity for each relation if not already set (based on the known unique identifier)
        for (EntityData graphDataNode : loadedGraphNodes.values()) {
            for (EntityRelation nodeRelation : graphDataNode.getRelatedNodesToBeLoaded()) {
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
