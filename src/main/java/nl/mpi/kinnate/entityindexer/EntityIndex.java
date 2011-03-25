package nl.mpi.kinnate.entityindexer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.kinnate.svg.GraphDataNode;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter;
import nl.mpi.kinnate.kintypestrings.KinTypeStringConverter.KinType;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *  Document   : EntityIndex
 *  Created on : Feb 2, 2011, 17:29:36 PM
 *  Author     : Peter Withers
 */
public class EntityIndex implements EntityService {

//    IndexerParameters indexParameters;
    private HashMap<String /* url to the ego entity */, EntityData> knownEntities;
//    private EntityCollection entityCollection;

    public EntityIndex() {
//        indexParameters = indexParametersLocal;
//        entityCollection = new EntityCollection();
        knownEntities = new HashMap<String, EntityData>();
    }

    private EntityData getEntityData(String egoEntityUriString, IndexerParameters indexParameters) throws URISyntaxException {
        URI egoEntityUri = new URI(egoEntityUriString);
        return getEntityData(egoEntityUri, indexParameters);
    }

    private EntityData getEntityData(URI egoEntityUri, IndexerParameters indexParameters) {
        EntityData entityData = knownEntities.get(egoEntityUri.toASCIIString());
        if (entityData != null) {
            return entityData;
        } else {
            entityData = new EntityData(null); // todo: while this could pass the identifier it is unlikely that this class will use them as it relies on the url instead
            knownEntities.put(egoEntityUri.toASCIIString(), entityData);
            try {
                Document linksDom = new CmdiComponentBuilder().getDocument(egoEntityUri);
                NodeList relationLinkNodeList = org.apache.xpath.XPathAPI.selectNodeList(linksDom, indexParameters.linkPath);
                for (int nodeCounter = 0; nodeCounter < relationLinkNodeList.getLength(); nodeCounter++) {
                    Node relationLinkNode = relationLinkNodeList.item(nodeCounter);
                    if (relationLinkNode != null) {
                        // resolve the alter URL against its ego URI
                        URI alterUri = egoEntityUri.resolve(relationLinkNode.getTextContent());
                        entityData.addRelation(alterUri.toASCIIString());
                        // get any requested link data
                        for (String[] relevantDataPath : indexParameters.relevantLinkData.getValues()) {
                            for (Node linkDataNode = relationLinkNode.getParentNode().getFirstChild(); linkDataNode != null; linkDataNode = linkDataNode.getNextSibling()) {
                                if (relevantDataPath[0].equals(linkDataNode.getNodeName())) {
                                    entityData.addRelationData(alterUri.toASCIIString(), relevantDataPath[0], linkDataNode.getTextContent());
                                }
                            }
                        }
                    }
                }
                // get any requested entity data
                for (String[] relevantDataPath : indexParameters.getRelevantEntityData()) {
                    NodeList relevantaDataNodeList = org.apache.xpath.XPathAPI.selectNodeList(linksDom, relevantDataPath[0]);
                    for (int dataCounter = 0; dataCounter < relevantaDataNodeList.getLength(); dataCounter++) {
                        Node dataNode = relevantaDataNodeList.item(dataCounter);
                        if (dataNode != null) {
                            entityData.addEntityData(relevantDataPath[0], dataNode.getTextContent());
                        }
                    }
                }
            } catch (TransformerException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (DOMException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            }
            return entityData;
        }
    }

    public void printKnownEntities() {
        for (String currentEgo : knownEntities.keySet()) {
            System.out.println("currentEgo: " + currentEgo);
            EntityData currentEntityData = knownEntities.get(currentEgo);
            for (String[] currentRecord : currentEntityData.getEntityFields()) {
                System.out.println("-> entityField: " + currentRecord[0] + " : " + currentRecord[1]);
            }
            for (String currentLink : currentEntityData.getRelationPaths()) {
                System.out.println("--> currentLink: " + currentLink);
                for (String[] currentRecord : currentEntityData.getRelationData(currentLink)) {
                    System.out.println("---> linkField: " + currentRecord[0] + " : " + currentRecord[1]);
                }
            }
        }
    }

    public void loadAllEntities(IndexerParameters indexParameters) {
        String[] treeNodesArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
        if (treeNodesArray != null) {
            for (String currentNodeString : treeNodesArray) {
                try {
                    URI egoEntityUri = new URI(currentNodeString);
                    getEntityData(egoEntityUri, indexParameters);
                } catch (URISyntaxException exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
                }
            }
        }
    }

    public void setKinTypeStringTerm(String symbolString, String fieldPath, String fieldValue) {
        // todo: set the terms that combine to form the kin type strings
        // eg: setKinTypeStringTerm("M", "Kinnate.Gedcom.Entity.SEX", "F");
    }

    private GraphDataNode getGraphDataNode(boolean isEgo, URI entityUri, IndexerParameters indexParameters) {
        EntityData entityData = getEntityData(entityUri, indexParameters);
        ArrayList<String> labelTextList = new ArrayList<String>();
        for (String[] currentLabelField : indexParameters.labelFields.getValues()) {
            String labelTextTemp = entityData.getEntityField(currentLabelField[0]);
            if (labelTextTemp != null) {
                labelTextList.add(labelTextTemp);
            }
        }
        for (String currentSymbolField[] : indexParameters.symbolFieldsFields.getValues()) {
            String linkSymbolString = entityData.getEntityField(currentSymbolField[0]);
            if (linkSymbolString != null) {
                return new GraphDataNode(entityData.getUniqueIdentifier(), entityUri.toASCIIString(), currentSymbolField[1], labelTextList.toArray(new String[]{}), isEgo);
            }
        }
        return new GraphDataNode(entityData.getUniqueIdentifier(), entityUri.toASCIIString(), GraphDataNode.SymbolType.none, labelTextList.toArray(new String[]{}), isEgo);
    }

    private void setRelationData(GraphDataNode egoNode, GraphDataNode alterNode, EntityData egoData, String alterPath, IndexerParameters indexParameters) {
        GraphDataNode.RelationType egoType = null;
        GraphDataNode.RelationType alterType = null;
        String[][] alterRelationFields = egoData.getRelationData(alterPath);
        if (alterRelationFields != null) {
            for (String[] ancestorField : indexParameters.ancestorFields.getValues()) {
                for (String[] egoRelationField : alterRelationFields) {
                    if (ancestorField[0].equals(egoRelationField[1])) {
                        egoType = GraphDataNode.RelationType.ancestor;
                        alterType = GraphDataNode.RelationType.descendant;
                    }
                }
            }
            for (String[] ancestorField : indexParameters.decendantFields.getValues()) {
                for (String[] egoRelationField : alterRelationFields) {
                    if (ancestorField[0].equals(egoRelationField[1])) {
                        egoType = GraphDataNode.RelationType.descendant;
                        alterType = GraphDataNode.RelationType.ancestor;
                    }
                }
            }
            if (egoType != null && alterType != null) {
                egoNode.addRelatedNode(alterNode, 0, egoType, GraphDataNode.RelationLineType.square, null);
                alterNode.addRelatedNode(egoNode, 0, alterType, GraphDataNode.RelationLineType.square, null);
            }
        }
    }

    public GraphDataNode[] getEgoGraphData(URI[] egoNodes, IndexerParameters indexParameters) {
        ArrayList<GraphDataNode> graphDataNodeList = new ArrayList<GraphDataNode>();
        for (URI currentEgoUri : egoNodes) {
            graphDataNodeList.add(getGraphDataNode(true, currentEgoUri, indexParameters));
        }
        return graphDataNodeList.toArray(new GraphDataNode[]{});
    }

    private void getNextRelations(HashMap<String, GraphDataNode> createdGraphNodes, String currentEgoPath, GraphDataNode egoNode, ArrayList<KinType> remainingKinTypes, IndexerParameters indexParameters) throws URISyntaxException {
        EntityData egoData = getEntityData(currentEgoPath, indexParameters);
//        String currentKinType = remaningKinTypeString.substring(0, 1);
//        remaningKinTypeString = remaningKinTypeString.substring(1);
        KinType currentKinType = remainingKinTypes.remove(0);
//        for (String alterPath : entityCollection.getRelatedNodes(egoNode.getUniqueIdentifier())) {
        for (String alterPath : egoData.getRelationPaths()) {
            try {
                boolean relationAdded = false;
                GraphDataNode alterNode;
                if (createdGraphNodes.containsKey(alterPath)) {
                    alterNode = createdGraphNodes.get(alterPath);
                } else {
                    alterNode = getGraphDataNode(false, new URI(alterPath), indexParameters);
                    createdGraphNodes.put(alterPath, alterNode);
                    relationAdded = true;
                }
                EntityData alterData = getEntityData(currentEgoPath, indexParameters);
                setRelationData(egoNode, alterNode, egoData, alterPath, indexParameters);
                setRelationData(alterNode, egoNode, alterData, currentEgoPath, indexParameters);
                // todo: either prevent links being added if a node does not match the kin type or remove them when known
//                if (egoNode.relationMatchesType(alterPath, currentKinType)) {
                    // only traverse if the type matches
                    if (remainingKinTypes.size() > 0) {
                        getNextRelations(createdGraphNodes, alterPath, alterNode, remainingKinTypes, indexParameters);
                    }
//                } else if (relationAdded) {
//                    createdGraphNodes.remove(alterPath);
//                }
            } catch (URISyntaxException urise) {
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
    }

    public GraphDataNode[] getRelationsOfEgo(URI[] egoNodes, String[] uniqueIdentifiers, String[] kinTypeStrings, IndexerParameters indexParameters) throws EntityServiceException {
        KinTypeStringConverter kinTypeStringConverter = new KinTypeStringConverter();
        HashMap<String, GraphDataNode> createdGraphNodes = new HashMap<String, GraphDataNode>();
        for (URI currentEgoUri : egoNodes) {
            GraphDataNode egoNode;
            if (createdGraphNodes.containsKey(currentEgoUri.toASCIIString())) {
                egoNode = createdGraphNodes.get(currentEgoUri.toASCIIString());
            } else {
                egoNode = getGraphDataNode(true, currentEgoUri, indexParameters);
                createdGraphNodes.put(currentEgoUri.toASCIIString(), egoNode);
            }
            if (kinTypeStrings != null) {
                for (String currentKinString : kinTypeStrings) {
                    ArrayList<KinType> kinTypes = kinTypeStringConverter.getKinTypes(currentKinString);
                    try {
                        getNextRelations(createdGraphNodes, currentEgoUri.toASCIIString(), egoNode, kinTypes, indexParameters);
                    } catch (URISyntaxException exception) {
                        throw new EntityServiceException(exception.getMessage());
                    }
                }
            }
        }
        return createdGraphNodes.values().toArray(new GraphDataNode[]{});
    }

    public static void main(String[] args) {
        String[] entityStringArray = LinorgSessionStorage.getSingleInstance().loadStringArray("KinGraphTree");
        URI[] entityUriArray = new URI[entityStringArray.length];
        int uriCounter = 0;
        for (String currentEntityString : entityStringArray) {
            try {
                entityUriArray[uriCounter] = new URI(currentEntityString);
            } catch (URISyntaxException urise) {
                GuiHelper.linorgBugCatcher.logError(urise);
            }
            uriCounter++;
        }
        EntityIndex testEntityIndex = new EntityIndex();
        testEntityIndex.loadAllEntities(new IndexerParameters());
        testEntityIndex.printKnownEntities();
    }
}
