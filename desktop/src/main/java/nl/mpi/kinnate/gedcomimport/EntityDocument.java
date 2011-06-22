package nl.mpi.kinnate.gedcomimport;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.uniqueidentifiers.LocalIdentifier;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *  Document   : EntityBuilder
 *  Created on : May 30, 2011, 1:25:05 PM
 *  Author     : Peter Withers
 */
public class EntityDocument {

    String idString;
    File entityFile = null;
    Document metadataDom = null;
    Element previousField = null;
    Node currentDomNode = null;
    String uniquieIdentifier;

    public EntityDocument(File destinationDirectory, String nameString) {
        idString = nameString;
        entityFile = new File(destinationDirectory, nameString);
        uniquieIdentifier = new LocalIdentifier().getUniqueIdentifier(entityFile);
    }

    public String getFileName() {
        return entityFile.getName();
    }

    public String getUniquieIdentifier() {
        return uniquieIdentifier;
    }

    public URI createDocument(boolean overwriteExisting) throws ImportException {
        String gedcomXsdLocation = "/xsd/StandardEntity.xsd";
        URI entityUri;

        if (!overwriteExisting && entityFile.exists()) {
            throw new ImportException("Skipping existing entity file");
        } else { // start skip overwrite
            try {
                entityUri = new ArbilComponentBuilder().createComponentFile(entityFile.toURI(), this.getClass().getResource(gedcomXsdLocation).toURI(), false);

                metadataDom = ArbilComponentBuilder.getDocument(entityUri);
                currentDomNode = metadataDom.getDocumentElement();
//                            // find the deepest element node to start adding child nodes to
//                            for (Node childNode = currentDomNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
//                                System.out.println("childNode: " + childNode);
//                                System.out.println("childNodeType: " + childNode.getNodeType());
//                                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
//                                    System.out.println("entering node");
//                                    currentDomNode = childNode;
//                                    childNode = childNode.getFirstChild();
//                                    if (childNode == null) {
//                                        break;
//                                    }
//                                }
//                            }

                // add a unique identifier to the entity node
                Element localIdentifierElement = metadataDom.createElement("LocalIdentifier");
                localIdentifierElement.setTextContent(uniquieIdentifier);
                Node uniqueIdentifierNode = org.apache.xpath.XPathAPI.selectSingleNode(metadataDom, "/:Kinnate/:Entity/:UniqueIdentifier");
                uniqueIdentifierNode.appendChild(localIdentifierElement);
                currentDomNode = uniqueIdentifierNode.getParentNode();
            } catch (DOMException exception) {
                new ArbilBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (TransformerException exception) {
                new ArbilBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (URISyntaxException ex) {
                new ArbilBugCatcher().logError(ex);
                throw new ImportException("Error: " + ex.getMessage());
            } catch (ParserConfigurationException exception) {
                throw new ImportException("Error: " + exception.getMessage());
            } catch (IOException exception) {
                throw new ImportException("Error: " + exception.getMessage());
            } catch (SAXException exception) {
                throw new ImportException("Error: " + exception.getMessage());
            }
            return entityUri;
        }
    }

    public void insertValue(String nodeName, String valueString) {
//        System.out.println("insertValue: " + nodeName + " : " + valueString);
        nodeName = nodeName.replaceAll("\\s", "_");
        Node currentNode = currentDomNode.getFirstChild();
        while (currentNode != null) {
            if (nodeName.equals(currentNode.getLocalName())) {
                if (currentNode.getTextContent() == null || currentNode.getTextContent().length() == 0) {
                    // put the value into this node
                    currentNode.setTextContent(valueString);
                    return;
                }
                if (currentNode.getTextContent().equals(valueString)) {
                    // if the value already exists then do not add again
                    return;
                }
            }
            currentNode = currentNode.getNextSibling();
        }
        Node valueElement = metadataDom.createElement(nodeName);
        valueElement.setTextContent(valueString);
        currentDomNode.appendChild(valueElement);
    }

    public void insertRelation(RelationType relationType, String relationUniquieIdentifier, String fileNameString) {
        Element relationElement = metadataDom.createElement("Relation");
        metadataDom.getDocumentElement().appendChild(relationElement);

        Element linkElement = metadataDom.createElement("Link");
        linkElement.setTextContent("./" + fileNameString);
        relationElement.appendChild(linkElement);

        Element uniqueIdentifierElement = metadataDom.createElement("UniqueIdentifier");
        Element localIdentifierElement = metadataDom.createElement("LocalIdentifier");
        localIdentifierElement.setTextContent(relationUniquieIdentifier);
        uniqueIdentifierElement.appendChild(localIdentifierElement);
        relationElement.appendChild(uniqueIdentifierElement);

        Element typeElement = metadataDom.createElement("Type");
        typeElement.setTextContent(relationType.name());
        relationElement.appendChild(typeElement);

        Element targetNameElement = metadataDom.createElement("TargetName");
//        targetNameElement.setTextContent(lineParts[2]);
        relationElement.appendChild(targetNameElement);
    }

    public String getFilePath() {
        return entityFile.getAbsolutePath();
    }

    public void saveDocument() {
        ArbilComponentBuilder.savePrettyFormatting(metadataDom, entityFile);
    }
//    private EntityDocument(File destinationDirectory, String typeString, String idString, HashMap<String, ArrayList<String>> createdNodeIds, boolean overwriteExisting) {
}
