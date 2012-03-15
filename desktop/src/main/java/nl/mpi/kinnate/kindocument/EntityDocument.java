package nl.mpi.kinnate.kindocument;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.kinnate.gedcomimport.ImportException;
import nl.mpi.kinnate.kindata.EntityData;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *  Document   : EntityBuilder
 *  Created on : May 30, 2011, 1:25:05 PM
 *  Author     : Peter Withers
 */
public class EntityDocument {

    File entityFile = null;
    Document metadataDom = null;
    Node kinnateNode = null;
    Element metadataNode = null;
    Node currentDomNode = null;
    public EntityData entityData = null;
    private ImportTranslator importTranslator;
    public static String defaultEntityType = "individual"; // todo:. add some xsd files to the jar file so that the user can work off line from the start and to make sure that the user does not need to wait for ages on the first entity added
    // todo:. when the menu requests a new node it should show the progress bar before making the request
    private SessionStorage sessionStorage;

    public EntityDocument(ImportTranslator importTranslator, SessionStorage sessionStorage) {
        this.importTranslator = importTranslator;
        this.sessionStorage = sessionStorage;
        assignIdentiferAndFile();
    }

    public EntityDocument(String entityType, ImportTranslator importTranslator, SessionStorage sessionStorage) throws ImportException {
        this.importTranslator = importTranslator;
        this.sessionStorage = sessionStorage;
        assignIdentiferAndFile();
        try {
            // construct the metadata file
            URI xsdUri = new CmdiTransformer(sessionStorage).getXsdUrlString(entityType);
            URI addedNodeUri = new ArbilComponentBuilder().createComponentFile(entityFile.toURI(), xsdUri, false);
        } catch (KinXsdException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        setDomNodesFromExistingFile();
    }

    public EntityDocument(EntityDocument entityDocumentToCopy, ImportTranslator importTranslator, SessionStorage sessionStorage) throws ImportException {
        this.importTranslator = importTranslator;
        this.sessionStorage = sessionStorage;
        assignIdentiferAndFile();
        try {
            // load the document that needs to be copied so that it can be saved into the new location
            metadataDom = ArbilComponentBuilder.getDocument(entityDocumentToCopy.entityFile.toURI());
            ArbilComponentBuilder.savePrettyFormatting(metadataDom, entityFile);
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        } catch (ParserConfigurationException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        } catch (SAXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        // replace the entity data in the new document
        setDomNodesFromExistingFile();
    }

    public EntityDocument(File destinationDirectory, String nameString, String entityType, ImportTranslator importTranslator, SessionStorage sessionStorage) throws ImportException {
        this.importTranslator = importTranslator;
        this.sessionStorage = sessionStorage;
        String idString;
        entityData = new EntityData(new UniqueIdentifier(UniqueIdentifier.IdentifierType.lid));
        if (nameString != null) {
            idString = nameString;
            entityFile = new File(destinationDirectory, nameString + ".kmdi");
        } else {
            idString = entityData.getUniqueIdentifier().getQueryIdentifier() + ".kmdi";
            File subDirectory = new File(destinationDirectory, idString.substring(0, 2));
            subDirectory.mkdir();
            entityFile = new File(subDirectory, idString);
        }
        try {
            // construct the metadata file
            URI xsdUri = new CmdiTransformer(sessionStorage).getXsdUrlString(entityType);
            URI addedNodeUri = new ArbilComponentBuilder().createComponentFile(entityFile.toURI(), xsdUri, false);
        } catch (KinXsdException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
        setDomNodesFromExistingFile();
    }

    public EntityDocument(URI entityUri, ImportTranslator importTranslator, SessionStorage sessionStorage) throws ImportException {
        this.importTranslator = importTranslator;
        this.sessionStorage = sessionStorage;
        entityFile = new File(entityUri);
        setDomNodesFromExistingFile();
    }

    private void assignIdentiferAndFile() {
        String idString;
        entityData = new EntityData(new UniqueIdentifier(UniqueIdentifier.IdentifierType.lid));
        idString = entityData.getUniqueIdentifier().getQueryIdentifier() + ".kmdi";
        File subDirectory = new File(sessionStorage.getCacheDirectory(), idString.substring(0, 2));
        subDirectory.mkdir();
        entityFile = new File(subDirectory, idString);
    }

    private void setDomNodesFromExistingFile() throws ImportException {
        try {
            metadataDom = ArbilComponentBuilder.getDocument(entityFile.toURI());
            kinnateNode = metadataDom.getDocumentElement();
            // final NodeList metadataNodeList = ((Element) kinnateNode).getElementsByTagNameNS("http://mpi.nl/tla/kin", "Metadata");
            final NodeList metadataNodeList = ((Element) kinnateNode).getElementsByTagName("CustomData");
            if (metadataNodeList.getLength() < 1) {
                throw new ImportException("Data node not found");
            }
            metadataNode = (Element) metadataNodeList.item(0);
            // remove any old entity data which will be replaced on save with the existingEntity data provided
            final NodeList entityNodeList = ((Element) kinnateNode).getElementsByTagNameNS("*", "Entity"); // todo: this name space could be specified when the schema is complete: "http://mpi.nl/tla/kin" instead of "*"
            for (int entityCounter = 0; entityCounter < entityNodeList.getLength(); entityCounter++) {
                if (entityData == null) {
                    JAXBContext jaxbContext = JAXBContext.newInstance(EntityData.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    entityData = (EntityData) unmarshaller.unmarshal(entityNodeList.item(entityCounter), EntityData.class).getValue();
                }
                kinnateNode.removeChild(entityNodeList.item(entityCounter));
            }
            currentDomNode = metadataNode;
            if (entityData == null) {
                throw new ImportException("Entity node not found");
            }
        } catch (JAXBException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        } catch (ParserConfigurationException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        } catch (SAXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            throw new ImportException("Error: " + exception.getMessage());
        }
    }

    public String getFileName() {
        return entityFile.getName();
    }

    public UniqueIdentifier getUniqueIdentifier() {
        return entityData.getUniqueIdentifier();
    }

    public URI createBlankDocument(boolean overwriteExisting) throws ImportException {
        if (metadataDom != null) {
            throw new ImportException("The document already exists");
        }
        URI entityUri;
        if (!overwriteExisting && entityFile.exists()) {
            throw new ImportException("Skipping existing entity file");
        } else { // start skip overwrite
            try {
                entityUri = entityFile.toURI();
                URI xsdUri = new CmdiTransformer(sessionStorage).getXsdUrlString("individual");
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                String templateXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<Kinnate \n"
                        + "xmlns:kin=\"http://mpi.nl/tla/kin\" \n"
                        + "xmlns:dcr=\"http://www.isocat.org/ns/dcr\" \n"
                        + "xmlns:ann=\"http://www.clarin.eu\" \n"
                        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
                        + "xmlns:cmd=\"http://www.clarin.eu/cmd/\" \n"
                        + "xmlns=\"http://www.clarin.eu/cmd/\" \n"
                        + "KmdiVersion=\"1.1\" \n"
                        + "xsi:schemaLocation=\"http://mpi.nl/tla/kin "
                        + xsdUri.toString()
                        + "\n \" />";
                System.out.println("templateXml: " + templateXml);
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                metadataDom = documentBuilder.parse(new InputSource(new StringReader(templateXml)));
                metadataNode = metadataDom.createElementNS("http://www.clarin.eu/cmd/", "CustomData");
                currentDomNode = metadataNode;
                kinnateNode = metadataDom.getDocumentElement();
            } catch (DOMException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (ParserConfigurationException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (IOException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (SAXException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            } catch (KinXsdException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                throw new ImportException("Error: " + exception.getMessage());
            }
            return entityUri;
        }
    }

    public void insertValue(String nodeName, String valueString) {
        // this method will create a flat xml file and reuse any existing nodes of the target name
        ImportTranslator.TranslationElement translationElement = importTranslator.translate(nodeName, valueString);

        System.out.println("insertValue: " + translationElement.fieldName + " : " + translationElement.fieldValue);
        Node currentNode = metadataNode.getFirstChild();
        while (currentNode != null) {
            if (translationElement.fieldName.equals(currentNode.getLocalName())) {
                if (currentNode.getTextContent() == null || currentNode.getTextContent().length() == 0) {
                    // put the value into this node
                    currentNode.setTextContent(translationElement.fieldValue);
                    return;
                }
                if (currentNode.getTextContent().equals(translationElement.fieldValue)) {
                    // if the value already exists then do not add again
                    return;
                }
            }
            currentNode = currentNode.getNextSibling();
        }
        Node valueElement = metadataDom.createElementNS("http://www.clarin.eu/cmd/", /*"cmd:" +*/ translationElement.fieldName);
        valueElement.setTextContent(translationElement.fieldValue);
        metadataNode.appendChild(valueElement);
    }

    private void importNode(Node foreignNode) {
        Node importedNode = metadataDom.importNode(foreignNode, true);
        while (importedNode.hasChildNodes()) {
            // the metadata node already exists so just add the child nodes of it
            Node currentChild = importedNode.getFirstChild();
            currentDomNode.appendChild(currentChild);
//            importedNode.removeChild(currentChild);
        }
    }

    public Node insertNode(String nodeName, String valueString) {
        ImportTranslator.TranslationElement translationElement = importTranslator.translate(nodeName, valueString);
        System.out.println("nodeName: " + translationElement.fieldName + " : " + translationElement.fieldValue);
        Node valueElement = metadataDom.createElementNS("http://www.clarin.eu/cmd/", /*"cmd:" +*/ translationElement.fieldName);
        valueElement.setTextContent(translationElement.fieldValue);
        currentDomNode.appendChild(valueElement);
        return valueElement;
    }

    public void assendToLevel(int nodeLevel) {
        int levelCount = 0;
        Node counterNode = currentDomNode;
        while (counterNode != null) {
            levelCount++;
            counterNode = counterNode.getParentNode();
        }
        levelCount = levelCount - 3; // always keep the kinnate.metadata nodes
        while (levelCount > nodeLevel) {
            levelCount--;
            currentDomNode = currentDomNode.getParentNode();
        }
    }

    public void appendValueToLast(String valueString) {
        System.out.println("appendValueToLast: " + valueString);
        currentDomNode.setTextContent(currentDomNode.getTextContent() + valueString);
    }

    public void appendValue(String nodeName, String valueString, int targetLevel) {
        // this method will create a structured xml file
        // the nodeName will be translated if required in insertNode()
        System.out.println("appendValue: " + nodeName + " : " + valueString + " : " + targetLevel);
        assendToLevel(targetLevel);
        NodeList childNodes = currentDomNode.getChildNodes();
        if (childNodes.getLength() == 1 && childNodes.item(0).getNodeType() == Node.TEXT_NODE) { // getTextContent returns the text value of all sub nodes so make sure there is only one node which would be the text node
            String currentValue = currentDomNode.getTextContent();
            if (currentValue != null && currentValue.trim().length() > 0) {
                Node spacerElement = metadataDom.createElementNS("http://www.clarin.eu/cmd/", /*"cmd:" +*/ currentDomNode.getLocalName());
                Node parentNode = currentDomNode.getParentNode();
                parentNode.removeChild(currentDomNode);
                spacerElement.appendChild(currentDomNode);
                parentNode.appendChild(spacerElement);
                currentDomNode = spacerElement;
//            currentDomNode.setTextContent("");
                //insertNode(currentDomNode.getLocalName(), currentValue);
            }
        }
        currentDomNode = insertNode(nodeName, valueString);
    }

//    public void insertDefaultMetadata() {
//        // todo: this could be done via Arbil code and the schema when that is ready
//        insertValue("Gender", "unspecified");
//        insertValue("Name", "unspecified");
//    }
    public File getFile() {
        return entityFile;
    }

    public String getFilePath() {
        return entityFile.getAbsolutePath();
    }

    public void setAsDeletedDocument() throws ImportException {
        // todo:
    }

    public void saveDocument() throws ImportException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EntityData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(entityData, kinnateNode);
        } catch (JAXBException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
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
        System.out.println("saved: " + entityFile.toURI().toString());
    }
//    private EntityDocument(File destinationDirectory, String typeString, String idString, HashMap<String, ArrayList<String>> createdNodeIds, boolean overwriteExisting) {
}
