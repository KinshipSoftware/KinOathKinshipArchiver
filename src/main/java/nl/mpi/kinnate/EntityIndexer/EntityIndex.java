package nl.mpi.kinnate.EntityIndexer;

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
import nl.mpi.kinnate.GraphDataNode;
import nl.mpi.kinnate.KinTypeStringConverter;
import nl.mpi.kinnate.KinTypeStringConverter.KinType;
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
public class EntityIndex {

    private HashMap<String /* url to the ego entity */, EntityData> knownEntities;

    public EntityIndex() {
        knownEntities = new HashMap<String, EntityData>();
    }

    private void getLinksFromDom(URI egoEntityUri, EntityData entityData) {
        String[] relevantEntityData = {"Kinnate/Gedcom/Entity/NoteText", "Kinnate/Gedcom/Entity/SEX", "Kinnate/Gedcom/Entity/GedcomType", "Kinnate/Gedcom/Entity/NAME/NAME", "Kinnate/Gedcom/Entity/NAME/NPFX"}; // todo: the relevantData array comes from the user via the svg
        String[] relevantLinkData = {"Type"}; // todo: the relevantData array comes from the user via the svg
        try {
            String linkXpath = "/Kinnate/Relation/Link";
            Document linksDom = new CmdiComponentBuilder().getDocument(egoEntityUri);
            NodeList relationLinkNodeList = org.apache.xpath.XPathAPI.selectNodeList(linksDom, linkXpath);
            for (int nodeCounter = 0; nodeCounter < relationLinkNodeList.getLength(); nodeCounter++) {
                Node relationLinkNode = relationLinkNodeList.item(nodeCounter);
                if (relationLinkNode != null) {
                    entityData.addRelation(relationLinkNode.getTextContent());
                    // get any requested link data
                    for (String relevantDataPath : relevantLinkData) {
                        for (Node linkDataNode = relationLinkNode.getParentNode().getFirstChild(); linkDataNode != null; linkDataNode = linkDataNode.getNextSibling()) {
                            if (relevantDataPath.equals(linkDataNode.getNodeName())) {
                                entityData.addRelationData(relationLinkNode.getTextContent(), relevantDataPath, linkDataNode.getTextContent());
                            }
                        }
                    }
                }
            }
            // get any requested entity data
            for (String relevantDataPath : relevantEntityData) {
                NodeList relevantaDataNodeList = org.apache.xpath.XPathAPI.selectNodeList(linksDom, relevantDataPath);
                for (int dataCounter = 0; dataCounter < relevantaDataNodeList.getLength(); dataCounter++) {
                    Node dataNode = relevantaDataNodeList.item(dataCounter);
                    if (dataNode != null) {
                        entityData.addEntityData(relevantDataPath, dataNode.getTextContent());
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

    public boolean indexEntities(URI[] egoEntityUris) {
        for (URI egoEntityUri : egoEntityUris) {
            if (egoEntityUri != null) {
                EntityData entityData = new EntityData();
                knownEntities.put(egoEntityUri.toASCIIString(), entityData);
                getLinksFromDom(egoEntityUri, entityData);
            }
        }
        return false;
    }

    public void setKinTypeStringTerm(String symbolString, String fieldPath, String fieldValue) {
        // todo: set the terms that combine to form the kin type strings
        // eg: setKinTypeStringTerm("M", "Kinnate.Gedcom.Entity.SEX", "F");
    }

    private GraphDataNode getGraphDataNode(boolean isEgo, URI entityUri) {
        EntityData entityData = knownEntities.get(entityUri.toASCIIString());
        ArrayList<String> labelTextList = new ArrayList<String>();
        int entitySymbolIndex = 0;
        String[] labelFields = {"Kinnate/Gedcom/Entity/NAME/NAME", "Kinnate/Gedcom/Entity/GedcomType", "Kinnate/Gedcom/Entity/Text", "Kinnate/Gedcom/Entity/NAME/NPFX", "Kinnate/Gedcom/Entity/NoteText"};
        for (String currentLabelField : labelFields) {
            String labelTextTemp = entityData.getEntityField(currentLabelField);
            if (labelTextTemp != null) {
                labelTextList.add(labelTextTemp);
            }
        }
        if (isEgo) {
            entitySymbolIndex = 0;
        } else {
            String[] symbolFieldsFields = {"Kinnate/Gedcom/Entity/SEX", "Kinnate/Gedcom/Entity/GedcomType"};
            for (String currentSymbolField : symbolFieldsFields) {
                String linkSymbolString = entityData.getEntityField(currentSymbolField);
                if (linkSymbolString != null) {
                    if (linkSymbolString.equals("F")) {
                        entitySymbolIndex = 1;
                    }
                    if (linkSymbolString.equals("M")) {
                        entitySymbolIndex = 2;
                    }
                    if (linkSymbolString.equals("FAM")) {
                        entitySymbolIndex = 3;
                    }
                    if (linkSymbolString.equals("NOTE")) {
                        entitySymbolIndex = 4;
                    }
                    break;
                }
            }
        }
        return new GraphDataNode(entityUri.toASCIIString(), entitySymbolIndex, labelTextList.toArray(new String[]{}));
    }

    private void setRelationData(GraphDataNode egoNode, GraphDataNode alterNode, EntityData egoData, String alterPath) {
        GraphDataNode.RelationType egoType = null;
        GraphDataNode.RelationType alterType = null;
//        String[] relationFields = {"TYPE"};
        String[] ancestorFields = {"Kinnate.Gedcom.Entity.FAMC", "Kinnate.Gedcom.Entity.HUSB", "Kinnate.Gedcom.Entity.WIFE"};
        String[] decendantFields = {"Kinnate.Gedcom.Entity.CHIL", "Kinnate.Gedcom.Entity.FAMS"};
        String[][] alterRelationFields = egoData.getRelationData(alterPath);
        if (alterRelationFields != null) {
            for (String ancestorField : ancestorFields) {
                for (String[] egoRelationField : alterRelationFields) {
                    if (ancestorField.equals(egoRelationField[1])) {
                        egoType = GraphDataNode.RelationType.ancestor;
                        alterType = GraphDataNode.RelationType.descendant;
                    }
                }
            }
            for (String ancestorField : decendantFields) {
                for (String[] egoRelationField : alterRelationFields) {
                    if (ancestorField.equals(egoRelationField[1])) {
                        egoType = GraphDataNode.RelationType.descendant;
                        alterType = GraphDataNode.RelationType.ancestor;
                    }
                }
            }
            if (egoType != null && alterType != null) {
                egoNode.addRelatedNode(alterNode, 0, egoType);
                alterNode.addRelatedNode(egoNode, 0, alterType);
            }
        }
    }

    public GraphDataNode[] getEgoGraphData(URI[] egoNodes) {
        ArrayList<GraphDataNode> graphDataNodeList = new ArrayList<GraphDataNode>();
        for (URI currentEgoUri : egoNodes) {
            graphDataNodeList.add(getGraphDataNode(true, currentEgoUri));
        }
        return graphDataNodeList.toArray(new GraphDataNode[]{});
    }

    private void getNextRelations(HashMap<String, GraphDataNode> createdGraphNodes, String currentEgoPath, GraphDataNode egoNode, ArrayList<KinType> remainingKinTypes) {
        EntityData egoData = knownEntities.get(currentEgoPath);
//        String currentKinType = remaningKinTypeString.substring(0, 1);
//        remaningKinTypeString = remaningKinTypeString.substring(1);
        KinType currentKinType = remainingKinTypes.remove(0);
        for (String alterPath : egoData.getRelationPaths()) {
            try {
                boolean relationAdded = false;
                GraphDataNode alterNode;
                if (createdGraphNodes.containsKey(alterPath)) {
                    alterNode = createdGraphNodes.get(alterPath);
                } else {
                    alterNode = getGraphDataNode(false, new URI(alterPath));
                    createdGraphNodes.put(alterPath, alterNode);
                    relationAdded = true;
                }
                EntityData alterData = knownEntities.get(currentEgoPath);
                setRelationData(egoNode, alterNode, egoData, alterPath);
                setRelationData(alterNode, egoNode, alterData, currentEgoPath);
                // todo: either prevent links being added if a node does not match the kin type or remove them when known
                if (egoNode.relationMatchesType(alterPath, currentKinType)) {
                    // only traverse if the type matches
                    if (remainingKinTypes.size() > 0) {
                        getNextRelations(createdGraphNodes, alterPath, alterNode, remainingKinTypes);
                    }
                } else if (relationAdded) {
                    createdGraphNodes.remove(alterPath);
                }
            } catch (URISyntaxException urise) {
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
    }

    public GraphDataNode[] getRelationsOfEgo(URI[] egoNodes, String[] kinTypeStrings) {
        KinTypeStringConverter kinTypeStringConverter = new KinTypeStringConverter();
        HashMap<String, GraphDataNode> createdGraphNodes = new HashMap<String, GraphDataNode>();
        for (URI currentEgoUri : egoNodes) {
            GraphDataNode egoNode;
            if (createdGraphNodes.containsKey(currentEgoUri.toASCIIString())) {
                egoNode = createdGraphNodes.get(currentEgoUri.toASCIIString());
            } else {
                egoNode = getGraphDataNode(true, currentEgoUri);
                createdGraphNodes.put(currentEgoUri.toASCIIString(), egoNode);
            }
            if (kinTypeStrings != null) {
                for (String currentKinString : kinTypeStrings) {
                    ArrayList<KinType> kinTypes = kinTypeStringConverter.getKinTypes(currentKinString);
                    getNextRelations(createdGraphNodes, currentEgoUri.toASCIIString(), egoNode, kinTypes);
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
        testEntityIndex.indexEntities(entityUriArray);
        testEntityIndex.printKnownEntities();
    }
}
