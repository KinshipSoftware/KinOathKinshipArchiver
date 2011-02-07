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

    private HashMap<String /* url to the ego entity */, HashMap<String /* url to the related entiry */, ArrayList<String[] /* relevant entity data (link vs entity is clear from the data path)
            eg: [link.famc, link.kinterm:uncle,entity.gender:m, entity.age:60, entity.birth.year:1960]
             */>>> knownEntities;

    public EntityIndex() {
        knownEntities = new HashMap<String, HashMap<String, ArrayList<String[]>>>();
    }

    private void getLinksFromDom(URI egoEntityUri, HashMap<String, ArrayList<String[]>> linkedEntities) {
        String[] relevantEntityData = {"Kinnate/Gedcom/Entity/SEX", "Kinnate.Gedcom.Entity.GedcomType"}; // todo: the relevantData array comes from the user via the svg
        String[] relevantLinkData = {"Type"}; // todo: the relevantData array comes from the user via the svg
        try {
            String linkXpath = "/Kinnate/Relation/Link";
            Document linksDom = new CmdiComponentBuilder().getDocument(egoEntityUri);
            NodeList relationLinkNodeList = org.apache.xpath.XPathAPI.selectNodeList(linksDom, linkXpath);
            for (int nodeCounter = 0; nodeCounter < relationLinkNodeList.getLength(); nodeCounter++) {
                Node relationLinkNode = relationLinkNodeList.item(nodeCounter);
                if (relationLinkNode != null) {
                    ArrayList<String[]> releventDataFound = new ArrayList<String[]>();
                    linkedEntities.put(relationLinkNode.getTextContent(), releventDataFound);
                    // get any requested link data
                    for (String relevantDataPath : relevantLinkData) {
                       for (Node linkDataNode= relationLinkNode.getParentNode().getFirstChild(); linkDataNode!=null; linkDataNode = linkDataNode.getNextSibling()){
                           if (relevantDataPath.equals(linkDataNode.getNodeName())){
                               releventDataFound.add(new String[]{relevantDataPath, linkDataNode.getTextContent()});
                           }
                       }
                    }
                    // get any requested entity data
                    for (String relevantDataPath : relevantEntityData) {
                        NodeList relevantaDataNodeList = org.apache.xpath.XPathAPI.selectNodeList(linksDom, relevantDataPath);
                        for (int dataCounter = 0; dataCounter < relevantaDataNodeList.getLength(); dataCounter++) {
                            Node dataNode = relevantaDataNodeList.item(dataCounter);
                            if (dataNode != null) {
                                releventDataFound.add(new String[]{relevantDataPath, dataNode.getTextContent()});
                            }
                        }
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
            HashMap<String, ArrayList<String[]>> currentLinks = knownEntities.get(currentEgo);
            for (String currentLink : currentLinks.keySet()) {
                System.out.println("-> currentLink: " + currentLink);
                ArrayList<String[]> currentData = currentLinks.get(currentLink);
                for (String[] currentRecord : currentData) {
                    System.out.println("--> currentRecord: " + currentRecord[0] + " : " + currentRecord[1]);
                }
            }
        }
    }

    public boolean indexEntities(URI[] egoEntityUris) {
        for (URI egoEntityUri : egoEntityUris) {
            if (egoEntityUri != null) {
                HashMap<String, ArrayList<String[]>> linkedEntities = new HashMap<String, ArrayList<String[]>>();
                knownEntities.put(egoEntityUri.toASCIIString(), linkedEntities);
                getLinksFromDom(egoEntityUri, linkedEntities);
            }
        }
        return false;
    }

    public void setKinTypeStringTerm(String symbolString, String fieldPath, String fieldValue) {
        // todo: set the terms that combine to form the kin type strings
        // eg: setKinTypeStringTerm("M", "Kinnate.Gedcom.Entity.SEX", "F");
    }

    public URI[] getRelationsOfEgo(URI[] egoNodes, String[] kinTypeStrings) {
        // todo: return the next level relations of this individual
        return new URI[]{};
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
