package nl.mpi.kinnate.gedcomimport;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.kinnate.kindata.DataTypes.RelationLineType;
import nl.mpi.kinnate.kindata.DataTypes.RelationType;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
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
    Node kinnateNode = null;
    Element metadataNode = null;
    Node currentDomNode = null;
    public EntityData entityData;

    public EntityDocument(File destinationDirectory, String nameString) {
        entityData = new EntityData(new UniqueIdentifier(UniqueIdentifier.IdentifierType.lid));
        if (nameString != null) {
            idString = nameString;
            entityFile = new File(destinationDirectory, nameString);
        } else {
            idString = entityData.getUniqueIdentifier().getQueryIdentifier() + ".cmdi";
            File subDirectory = new File(destinationDirectory, idString.substring(0, 2));
            subDirectory.mkdir();
            entityFile = new File(subDirectory, idString);
        }
    }

    public String getFileName() {
        return entityFile.getName();
    }

    public UniqueIdentifier getUniqueIdentifier() {
        return entityData.getUniqueIdentifier();
    }

    public URI createDocument(boolean overwriteExisting) throws ImportException {
        String gedcomXsdLocation = "/xsd/StandardEntity.xsd";
        URI entityUri;

        if (!overwriteExisting && entityFile.exists()) {
            throw new ImportException("Skipping existing entity file");
        } else { // start skip overwrite
            try {
                entityUri = entityFile.toURI();
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                String templateXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<Kinnate xmlns:kin=\"http://mpi.nl/tla/kin\" xmlns=\"http://www.clarin.eu/cmd/\" version=\"1.0\"/>"; //<cmd:Metadata/></Kinnate>
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                metadataDom = documentBuilder.parse(new InputSource(new StringReader(templateXml)));
                metadataNode = metadataDom.createElementNS("http://www.clarin.eu/cmd/", "Metadata");
                currentDomNode = metadataNode;
                kinnateNode = metadataDom.getDocumentElement();
            } catch (DOMException exception) {
                new ArbilBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (ParserConfigurationException exception) {
                new ArbilBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (IOException exception) {
                new ArbilBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (SAXException exception) {
                new ArbilBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            }
            return entityUri;
        }
    }

    public void insertValue(String nodeName, String valueString) {
//        System.out.println("insertValue: " + nodeName + " : " + valueString);
        nodeName = nodeName.replaceAll("\\s", "_");
        Node currentNode = currentDomNode.getFirstChild();
        if (currentNode == null) {
            currentNode = metadataNode;
        }
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
        Node valueElement = metadataDom.createElementNS("http://www.clarin.eu/cmd/", /*"cmd:" +*/ nodeName);
        valueElement.setTextContent(valueString);
        currentDomNode.appendChild(valueElement);
    }

    public void insertDefaultMetadata() {
        // todo: this could be done via Arbil code and the schema when that is ready
        insertValue("Gender", "unspecified");
        insertValue("Name", "unspecified");
    }

    public void insertRelation(EntityData alterNodeLocal, RelationType relationType/*, UniqueIdentifier relationUniquieIdentifier*/, String fileNameString) {
        entityData.addRelatedNode(alterNodeLocal, 1, relationType, RelationLineType.sanguineLine, null, null);

//        Element relationElement = metadataDom.createElement("Relation");
//        metadataDom.getDocumentElement().appendChild(relationElement);

//        Element linkElement = metadataDom.createElement("Link");
//        linkElement.setTextContent("./" + fileNameString);
//        relationElement.appendChild(linkElement);

//        Element uniqueIdentifierElement = metadataDom.createElement("UniqueIdentifier");
//        Element localIdentifierElement = metadataDom.createElement("LocalIdentifier");
//        localIdentifierElement.setTextContent(relationUniquieIdentifier);
//        uniqueIdentifierElement.appendChild(localIdentifierElement);
//        relationElement.appendChild(uniqueIdentifierElement);

//        Element typeElement = metadataDom.createElement("Type");
//        typeElement.setTextContent(relationType.name());
//        relationElement.appendChild(typeElement);

//        Element targetNameElement = metadataDom.createElement("TargetName");
//        targetNameElement.setTextContent(lineParts[2]);
//        relationElement.appendChild(targetNameElement);
    }

    public File getFile() {
        return entityFile;
    }

    public String getFilePath() {
        return entityFile.getAbsolutePath();
    }

    public void saveDocument() throws ImportException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(entityData, kinnateNode);
        } catch (JAXBException exception) {
            new ArbilBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
//        try {
//            Node entityNode = org.apache.xpath.XPathAPI.selectSingleNode(metadataDom, "/:Kinnate/:Entity");
        kinnateNode.appendChild(metadataNode);
        // todo: maybe insert the user selected CMDI profile into the XML declaration of the kinnate node and let arbil handle the adding of sub nodes or consider using ArbilComponentBuilder to insert a cmdi sub component into the metadata node or keep the cmdi data in a separate file
//        } catch (TransformerException exception) {
//            new ArbilBugCatcher().logError(exception);
//            throw new ImportException("Error: " + exception.getMessage());
//        }
        ArbilComponentBuilder.savePrettyFormatting(metadataDom, entityFile);
    }
//    private EntityDocument(File destinationDirectory, String typeString, String idString, HashMap<String, ArrayList<String>> createdNodeIds, boolean overwriteExisting) {
}
