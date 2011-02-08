package nl.mpi.kinnate;

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

    private class EntityData {

        private HashMap<String /* url to the related entiry */, ArrayList<String[] /* relevant entity data (link vs entity is clear from the data path)
                eg: [link.famc, link.kinterm:uncle,entity.gender:m, entity.age:60, entity.birth.year:1960]
                 */>> relationData = new HashMap<String, ArrayList<String[]>>();
        private ArrayList<String[] /* relevant entity data (link vs entity is clear from the data path)
                eg: [link.famc, link.kinterm:uncle,entity.gender:m, entity.age:60, entity.birth.year:1960]
                 */> entityFields = new ArrayList<String[]>();

        public String getEntityField(String fieldName) {
            for (String[] currentField : entityFields) {
                if (currentField[0].equals(fieldName)) {
                    return currentField[1];
                }
            }
            return null;
        }
    }
    private HashMap<String /* url to the ego entity */, EntityData> knownEntities;

    public EntityIndex() {
        knownEntities = new HashMap<String, EntityData>();
    }

    private void getLinksFromDom(URI egoEntityUri, EntityData entityData) {
        // HashMap<String, ArrayList<String[]>> linkedEntities
        String[] relevantEntityData = {"Kinnate/Gedcom/Entity/SEX", "Kinnate/Gedcom/Entity/GedcomType", "Kinnate/Gedcom/Entity/NAME/NAME"}; // todo: the relevantData array comes from the user via the svg
        String[] relevantLinkData = {"Type"}; // todo: the relevantData array comes from the user via the svg
        try {
            String linkXpath = "/Kinnate/Relation/Link";
            Document linksDom = new CmdiComponentBuilder().getDocument(egoEntityUri);
            NodeList relationLinkNodeList = org.apache.xpath.XPathAPI.selectNodeList(linksDom, linkXpath);
            for (int nodeCounter = 0; nodeCounter < relationLinkNodeList.getLength(); nodeCounter++) {
                Node relationLinkNode = relationLinkNodeList.item(nodeCounter);
                if (relationLinkNode != null) {
                    ArrayList<String[]> releventDataFound = new ArrayList<String[]>();
                    entityData.relationData.put(relationLinkNode.getTextContent(), releventDataFound);
                    // get any requested link data
                    for (String relevantDataPath : relevantLinkData) {
                        for (Node linkDataNode = relationLinkNode.getParentNode().getFirstChild(); linkDataNode != null; linkDataNode = linkDataNode.getNextSibling()) {
                            if (relevantDataPath.equals(linkDataNode.getNodeName())) {
                                releventDataFound.add(new String[]{relevantDataPath, linkDataNode.getTextContent()});
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
                        entityData.entityFields.add(new String[]{relevantDataPath, dataNode.getTextContent()});
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
            for (String[] currentRecord : currentEntityData.entityFields) {
                System.out.println("-> entityField: " + currentRecord[0] + " : " + currentRecord[1]);
            }
            for (String currentLink : currentEntityData.relationData.keySet()) {
                System.out.println("--> currentLink: " + currentLink);
                ArrayList<String[]> currentData = currentEntityData.relationData.get(currentLink);
                for (String[] currentRecord : currentData) {
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

    private GraphDataNode getGraphDataNode(URI entityUri) {
        EntityData entityData = knownEntities.get(entityUri.toASCIIString());
//        HashMap<String, ArrayList<String[]>> currentLink = entityData.relationData;
        String labelText = "not found"; // todo: this could be an array so that multiple labels are avaiable
        int entitySymbolIndex = 0;
//       ImdiTreeObject currentImdi = ImdiLoader.getSingleInstance().getImdiObject(null, entityUri);
//        return new GraphDataNode(currentImdi.toString());
        String[] labelFields = {"Kinnate/Gedcom/Entity/NAME/NAME", "Kinnate/Gedcom/Entity/GedcomType"};
        for (String currentLabelField : labelFields) {
            String labelTextTemp = entityData.getEntityField(currentLabelField);
            if (labelTextTemp != null) {
                labelText = labelTextTemp;
                break;
            }
        }
        String[] symbolFieldsFields = {"Kinnate/Gedcom/Entity/SEX", "Kinnate/Gedcom/Entity/GedcomType"};
        for (String currentSymbolField : symbolFieldsFields) {
            String linkSymbolString = entityData.getEntityField(currentSymbolField);
            if (linkSymbolString != null) {
                if (linkSymbolString.equals("m")) {
                    entitySymbolIndex = 1;
                }
                if (linkSymbolString.equals("f")) {
                    entitySymbolIndex = 2;
                }
                break;
            }
        }
        return new GraphDataNode(entitySymbolIndex, labelText);
    }

    public GraphDataNode[] getEgoGraphData(URI[] egoNodes) {
        ArrayList<GraphDataNode> graphDataNodeList = new ArrayList<GraphDataNode>();
        for (URI currentEgoUri : egoNodes) {
            graphDataNodeList.add(getGraphDataNode(currentEgoUri));
        }
        return graphDataNodeList.toArray(new GraphDataNode[]{});
    }

    public URI[] getRelationsOfEgo(URI[] egoNodes, String[] kinTypeStrings) {
        ArrayList<String> relatedNodes = new ArrayList<String>();
        ArrayList<URI> relatedNodeUris = new ArrayList<URI>();
        // todo: this could return just the ego or also the reverce links of the ego
        for (URI currentEgoUri : egoNodes) {
            relatedNodeUris.add(currentEgoUri);
            HashMap<String, ArrayList<String[]>> currentLink = knownEntities.get(currentEgoUri.toASCIIString()).relationData;
            relatedNodes.addAll(currentLink.keySet());

//            HashMap<String, ArrayList<String[]>> currentLinks = knownEntities.get(currentEgo);
//            for (String currentLink : currentLinks.keySet()) {
//                System.out.println("-> currentLink: " + currentLink);
//                ArrayList<String[]> currentData = currentLinks.get(currentLink);
//                for (String[] currentRecord : currentData) {
//                    System.out.println("--> currentRecord: " + currentRecord[0] + " : " + currentRecord[1]);
//                }
//            }
        }
        for (String currentUriString : relatedNodes) {
            try {
                relatedNodeUris.add(new URI(currentUriString));
            } catch (URISyntaxException urise) {
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
        return relatedNodeUris.toArray(new URI[]{});
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
