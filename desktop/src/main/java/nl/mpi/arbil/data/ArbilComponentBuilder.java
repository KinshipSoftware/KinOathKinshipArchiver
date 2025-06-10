/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader.CmdiResourceLink;
import nl.mpi.arbil.clarin.CmdiHeaderInfo;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbilcommons.journal.ArbilJournal;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Document : ArbilComponentBuilder <br> Created on : Mar 18, 2010, 1:40:35 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilComponentBuilder {

    private final static Logger logger = LoggerFactory.getLogger(ArbilComponentBuilder.class);
    private final static ResourceBundle services = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");
    public static final String CMD_NAMESPACE = "http://www.clarin.eu/cmd/";
    public static final String RESOURCE_ID_PREFIX = "res_";
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
        sessionStorage = sessionStorageInstance;
    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder;
    }
    private HashMap<ArbilDataNode, SchemaType> nodeSchemaTypeMap = new HashMap<ArbilDataNode, SchemaType>();

    public static Document getDocument(URI inputUri) throws ParserConfigurationException, SAXException, IOException {
        if (inputUri == null) {
            return getNewDocument();
        } else {
            // we open the stream here so we can set follow redirects
            URLConnection uRLConnection = inputUri.toURL().openConnection();
            if (uRLConnection instanceof HttpURLConnection) {
                ((HttpURLConnection) uRLConnection).setInstanceFollowRedirects(true);
            }
            final InputStream inputStream = uRLConnection.getInputStream();
            return getDocumentBuilder().parse(inputStream);
        }
    }

    public static Document getNewDocument() throws ParserConfigurationException {
        return getDocumentBuilder().newDocument();
    }

//    private Document createDocument(File inputFile) throws ParserConfigurationException, SAXException, IOException {
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//        documentBuilderFactory.setValidating(false);
//        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//        Document document = documentBuilder.newDocument();
//        return document;
//    }
    public static void savePrettyFormatting(Document document, File outputFile) {
        try {
            if (outputFile.getPath().endsWith(".imdi")) {
                removeImdiDomIds(document);  // remove any dom id attributes left over by the imdi api
            }
            // set up input and output
            DOMSource dOMSource = new DOMSource(document);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            StreamResult xmlOutput = new StreamResult(fileOutputStream);
            // configure transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(dOMSource, xmlOutput);
            xmlOutput.getOutputStream().close();

            // todo: this maybe excessive to do every time
            // this schema check has been moved to the point of loading the file rather than saving the file
//            XsdChecker xsdChecker = new XsdChecker();
//            String checkerResult;
//            checkerResult = xsdChecker.simpleCheck(outputFile, outputFile.toURI());
//            if (checkerResult != null) {
//                hasSchemaError = true;
////                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(checkerResult, "Schema Check");
//            }
            //logger.debug(xmlOutput.getWriter().toString());
        } catch (IllegalArgumentException illegalArgumentException) {
            BugCatcherManager.getBugCatcher().logError(illegalArgumentException);
        } catch (TransformerException transformerException) {
            BugCatcherManager.getBugCatcher().logError(transformerException);
        } catch (TransformerFactoryConfigurationError transformerFactoryConfigurationError) {
            logger.debug(transformerFactoryConfigurationError.getMessage());
        } catch (FileNotFoundException notFoundException) {
            BugCatcherManager.getBugCatcher().logError(notFoundException);
        } catch (IOException iOException) {
            BugCatcherManager.getBugCatcher().logError(iOException);
        }
    }

    public URI insertResourceProxy(ArbilDataNode arbilDataNode, ArbilDataNode resourceNode) {
        // there is no need to save the node at this point because metadatabuilder has already done so
        synchronized (arbilDataNode.getParentDomLockObject()) {
            String targetXmlPath = getTargetXmlPath(arbilDataNode);
            logger.debug("insertResourceProxy: {}", targetXmlPath);
//            File cmdiNodeFile = imdiTreeObject.getFile();
//            String nodeFragment = "";

            final CmdiComponentLinkReader linkReader = arbilDataNode.getParentDomNode().getCmdiComponentLinkReader();
            String resourceProxyId = linkReader.getProxyId(resourceNode.getUrlString());
            boolean newResourceProxy = (resourceProxyId == null);
            if (newResourceProxy) {
                // generate a uuid for new resource
                resourceProxyId = RESOURCE_ID_PREFIX + UUID.randomUUID().toString();
            }
            try {
                // load the dom
                Document targetDocument = getDocument(arbilDataNode.getURI());
                // insert the new section
                try {
                    try {
                        insertResourceProxyReference(targetDocument, targetXmlPath, resourceProxyId);
                    } catch (TransformerException exception) {
                        BugCatcherManager.getBugCatcher().logError(exception);
                        return null;
                    }
//                printoutDocument(targetDocument);
                    if (newResourceProxy) {
                        // load the schema
                        SchemaType schemaType = getFirstSchemaType(arbilDataNode.getNodeTemplate().getTemplateFile());
                        addNewResourceProxy(targetDocument, schemaType, resourceProxyId, resourceNode);
                    } else {
                        // Increase counter for referencing nodes
                        linkReader.getResourceLink(resourceProxyId).addReferencingNode();
                    }
                } catch (Exception exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    return null;
                }
                // bump the history
                arbilDataNode.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, arbilDataNode.getFile()); // note that we want to make sure that this gets saved even without changes because we have bumped the history ant there will be no file otherwise
            } catch (IOException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            } catch (ParserConfigurationException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            } catch (SAXException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            }
            return arbilDataNode.getURI();
        }
    }

    public void setHeaderInfo(Document document, CmdiHeaderInfo headerInfo) throws TransformerException {
        final String headerPath = "/:CMD/:Header";

        Node headerNode = XPathAPI.selectSingleNode(document.getFirstChild(), headerPath);

        if (headerNode != null) {
            setHeaderInfoItem(document, headerNode, "MdCreator", headerInfo.getMdCreator());
            setHeaderInfoItem(document, headerNode, "MdCreationDate", headerInfo.getMdCreationDate());
            setHeaderInfoItem(document, headerNode, "MdSelfLink", headerInfo.getMdSelfLink());
            setHeaderInfoItem(document, headerNode, "MdProfile", headerInfo.getMdProfile());
            setHeaderInfoItem(document, headerNode, "MdCollectionDisplayName", headerInfo.getMdCollectionDisplayName());
        }
    }

    private void setHeaderInfoItem(Document document, Node headerNode, final String nodeName, final String value) throws DOMException, TransformerException {
        Node childNode = XPathAPI.selectSingleNode(headerNode, "/:" + nodeName);
        if (value != null) {
            if (childNode == null) {
                childNode = document.createElementNS(CMD_NAMESPACE, nodeName);
            }
            childNode.setTextContent(value);
            headerNode.appendChild(childNode);
        } else {
            if (childNode != null) {
                headerNode.removeChild(childNode);
            }
        }
    }

    private String getTargetXmlPath(ArbilDataNode arbilDataNode) {
        String targetXmlPath = arbilDataNode.getURIFragment();
        if (targetXmlPath == null) {
            // Get the root CMD Component
            targetXmlPath = ".CMD.Components.*[1]";
        }
        return targetXmlPath;
    }

    private void insertResourceProxyReference(Document targetDocument, String targetXmlPath, String resourceProxyId) throws TransformerException, DOMException {
        //                    if (targetXmlPath == null) {
        //                        targetXmlPath = ".CMD.Components";
        //                    }

        Node documentNode = selectSingleNode(targetDocument, targetXmlPath);
        Node previousRefNode = documentNode.getAttributes().getNamedItem(CmdiTemplate.RESOURCE_REFERENCE_ATTRIBUTE);
        if (previousRefNode != null) {
            // Element already has resource proxy reference(s)
            String previousRefValue = documentNode.getAttributes().getNamedItem(CmdiTemplate.RESOURCE_REFERENCE_ATTRIBUTE).getNodeValue();
            // Append new id to previous value(s)
            ((Element) documentNode).setAttribute(CmdiTemplate.RESOURCE_REFERENCE_ATTRIBUTE, previousRefValue + " " + resourceProxyId);
        } else {
            // Just set new id as reference
            ((Element) documentNode).setAttribute(CmdiTemplate.RESOURCE_REFERENCE_ATTRIBUTE, resourceProxyId);
        }
    }

    private void addNewResourceProxy(Document targetDocument, SchemaType firstChildSchemaType, String resourceProxyId, ArbilDataNode resourceNode) throws ArbilMetadataException, DOMException {
        Node addedResourceNode = insertSectionToXpath(targetDocument, targetDocument.getFirstChild(), firstChildSchemaType, ".CMD.Resources.ResourceProxyList", ".CMD.Resources.ResourceProxyList.ResourceProxy");
        addedResourceNode.getAttributes().getNamedItem("id").setNodeValue(resourceProxyId);
        for (Node childNode = addedResourceNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
            String localName = childNode.getNodeName();
            if ("ResourceType".equals(localName)) {
                if (resourceNode.isCmdiMetaDataNode()) {
                    childNode.setTextContent("Metadata");
                } else {
                    ((Element) childNode).setAttribute("mimetype", resourceNode.mpiMimeType);
                    childNode.setTextContent("Resource");
                }
            }
            if ("ResourceRef".equals(localName)) {
                childNode.setTextContent(resourceNode.getUrlString());
            }
        }
    }

    public boolean removeResourceProxyReferences(ArbilDataNode parent, Collection<String> resourceProxyReferences) {
        synchronized (parent.getParentDomLockObject()) {
            CmdiComponentLinkReader linkReader = parent.getCmdiComponentLinkReader();
            if (linkReader == null) {
                // We do need (and expect) and link reader here...
                return false;
            }

            HashSet<String> resourceProxyIds = new HashSet<String>(resourceProxyReferences.size());
            for (String reference : resourceProxyReferences) {
                resourceProxyIds.add(linkReader.getProxyId(reference));
            }
            String targetXmlPath = getTargetXmlPath(parent);
            logger.debug("removeResourceProxyReferences: {}", targetXmlPath);
            try {
                Document targetDocument = getDocument(parent.getURI());
                // insert the new section
                try {
                    Node documentNode = selectSingleNode(targetDocument, targetXmlPath);
                    if (documentNode != null) { // Node is not there, nothing to check
                        Node previousRefNode = documentNode.getAttributes().getNamedItem(CmdiTemplate.RESOURCE_REFERENCE_ATTRIBUTE);
                        if (previousRefNode != null) {
                            // Get old references
                            String previousRefsValue = documentNode.getAttributes().getNamedItem(CmdiTemplate.RESOURCE_REFERENCE_ATTRIBUTE).getNodeValue();
                            // Create new reference set excluding the ones to be removed
                            StringBuilder newRefsValueSB = new StringBuilder();
                            for (String ref : previousRefsValue.split(" ")) {
                                ref = ref.trim();
                                if (ref.length() > 0 && !resourceProxyIds.contains(ref)) {
                                    newRefsValueSB.append(ref).append(" ");
                                }
                            }
                            String newRefsValue = newRefsValueSB.toString().trim();
                            if (newRefsValue.length() == 0) {
                                // No remaining references, remove ref attribute
                                ((Element) documentNode).removeAttribute(CmdiTemplate.RESOURCE_REFERENCE_ATTRIBUTE);
                            } else {
                                ((Element) documentNode).setAttribute(CmdiTemplate.RESOURCE_REFERENCE_ATTRIBUTE, newRefsValue);
                            }
                        }
                    }
                } catch (TransformerException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    return false;
                }

                // Check whether proxy can be deleted
                for (String id : resourceProxyIds) {
                    CmdiResourceLink link = linkReader.getResourceLink(id);
                    if (link == null) {
                        logger.error("Resource link not found for id {}", id);
                        return false;
                    }
                    link.removeReferencingNode();
                    if (link.getReferencingNodesCount() == 0) {
                        // There was only one reference to this proxy and we deleted it, so remove the proxy
                        if (!removeResourceProxy(targetDocument, id)) {
                            messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("FAILED TO REMOVE RESOURCE PROXY WITH ID {0}"), id), "Warning");
                        }
                    }
                }

                // bump the history
                parent.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, parent.getFile()); // note that we want to make sure that this gets saved even without changes because we have bumped the history ant there will be no file otherwise
                return true;
            } catch (IOException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (ParserConfigurationException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (SAXException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            }
        }
        return false;
    }

    /**
     * Removes the resource proxy with the specified id from the document. Note:
     * THIS DOES NOT CHECK WHETHER THERE ARE ANY REFERENCES LEFT
     *
     * @param document Document to remove resource proxy from
     * @param resourceProxyId Unique id (id="xyz") of resource proxy element
     * @return Whether resource proxy was removed successfully
     */
    private boolean removeResourceProxy(Document document, String resourceProxyId) {
        // Look for ResourceProxy node with specified id
        Node proxyNode = getResourceProxyNode(document, resourceProxyId);
        if (proxyNode != null) {
            // Node found. Remove from parent
            proxyNode.getParentNode().removeChild(proxyNode);
            return true;
        } else {
            return false;
        }
    }

    public boolean updateResourceProxyReference(Document document, String resourceProxyId, URI referenceURI) {
        return updateResourceProxyReference(document, resourceProxyId, referenceURI.toString());
    }

    public boolean updateResourceProxyReference(Document document, String resourceProxyId, String reference) {
        try {
            // Look for ResourceProxy node with specified id
            Node resourceRefNode = selectSingleNode(document, getPathForResourceProxynode(resourceProxyId) + ".ResourceRef");
            if (resourceRefNode != null) {
                resourceRefNode.setTextContent(reference);
                return true;
            }
        } catch (TransformerException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
        return false;
    }

    public boolean removeChildNodes(ArbilDataNode arbilDataNode, String nodePaths[]) {
        if (arbilDataNode.getNeedsSaveToDisk(false)) {
            arbilDataNode.saveChangesToCache(true);
        }
        synchronized (arbilDataNode.getParentDomLockObject()) {
            logger.debug("remove from parent nodes: " + arbilDataNode);
            File cmdiNodeFile = arbilDataNode.getFile();
            try {
                Document targetDocument = getDocument(arbilDataNode.getURI());
                // collect up all the nodes to be deleted without changing the xpath
                ArrayList<Node> selectedNodes = new ArrayList<Node>();
                for (String currentNodePath : nodePaths) {
                    logger.debug("removeChildNodes: " + currentNodePath);
                    // todo: search for and remove any reource links referenced by this node or its sub nodes
                    Node documentNode = selectSingleNode(targetDocument, currentNodePath);
                    if (documentNode != null) {
                        //logger.debug("documentNode: " + documentNode);
                        logger.debug("documentNodeName: " + documentNode != null ? documentNode.getNodeName() : "<null>");
                        selectedNodes.add(documentNode);
                    }

                }
                // delete all the nodes now that the xpath is no longer relevant
                logger.debug("{}", selectedNodes.size());
                for (Node currentNode : selectedNodes) {
                    if (currentNode instanceof Attr) {
                        Element parent = ((Attr) currentNode).getOwnerElement();
                        if (parent != null) {
                            parent.removeAttributeNode((Attr) currentNode);
                        }
                    } else {
                        Node parentNode = currentNode.getParentNode();
                        if (parentNode != null) {
                            parentNode.removeChild(currentNode);
                        }
                    }
                }
                // bump the history
                arbilDataNode.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, cmdiNodeFile);
                for (String currentNodePath : nodePaths) {
                    // todo log to jornal file
                }
                return true;
            } catch (ParserConfigurationException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (SAXException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (IOException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (TransformerException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            }
            return false;
        }
    }

    public boolean setFieldValues(ArbilDataNode arbilDataNode, Collection<FieldUpdateRequest> fieldUpdates) {
        synchronized (arbilDataNode.getParentDomLockObject()) {
            //new ImdiUtils().addDomIds(imdiTreeObject.getURI()); // testing only
            logger.debug("setFieldValues: " + arbilDataNode);
            File cmdiNodeFile = arbilDataNode.getFile();
            try {
                Document targetDocument = getDocument(arbilDataNode.getURI());
                if (!doFieldUpdates(fieldUpdates, targetDocument, arbilDataNode)) {
                    return false;
                }
                // bump the history
                arbilDataNode.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, cmdiNodeFile);
                for (FieldUpdateRequest currentFieldUpdate : fieldUpdates) {
                    // log to jornal file
                    ArbilJournal.getSingleInstance().saveJournalEntry(arbilDataNode.getUrlString(), currentFieldUpdate.fieldPath, currentFieldUpdate.fieldOldValue, currentFieldUpdate.fieldNewValue, "save");
                    if (currentFieldUpdate.fieldLanguageId != null) {
                        ArbilJournal.getSingleInstance().saveJournalEntry(arbilDataNode.getUrlString(), currentFieldUpdate.fieldPath + ":LanguageId", currentFieldUpdate.fieldLanguageId, "", "save");
                    }
                    if (currentFieldUpdate.keyNameValue != null) {
                        ArbilJournal.getSingleInstance().saveJournalEntry(arbilDataNode.getUrlString(), currentFieldUpdate.fieldPath + ":Name", currentFieldUpdate.keyNameValue, "", "save");
                    }
                }
                return true;
            } catch (ParserConfigurationException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (SAXException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (IOException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            } catch (TransformerException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            }
            return false;
        }
    }

    private boolean doFieldUpdates(Collection<FieldUpdateRequest> fieldUpdates, Document targetDocument, ArbilDataNode arbilDataNode) throws DOMException, TransformerException {
        for (FieldUpdateRequest currentFieldUpdate : fieldUpdates) {
            logger.debug("currentFieldUpdate: {}", currentFieldUpdate.fieldPath);
            // todo: search for and remove any reource links referenced by this node or its sub nodes
            final Node documentNode = selectSingleNode(targetDocument, currentFieldUpdate.fieldPath);
            if (currentFieldUpdate.fieldOldValue.equals(documentNode.getTextContent())) {
                documentNode.setTextContent(currentFieldUpdate.fieldNewValue);
            } else {
                logger.error("expecting \'" + currentFieldUpdate.fieldOldValue + "\' not \'" + documentNode.getTextContent() + "\' in " + currentFieldUpdate.fieldPath);
                return false;
            }
            if (documentNode instanceof Element) { // Attributes obviously don't have an attributesMap
                final NamedNodeMap attributesMap = documentNode.getAttributes();
                if (attributesMap != null) {
                    doUpdateAttributes(currentFieldUpdate, (Element) documentNode, arbilDataNode, attributesMap);
                }
            }
        }
        return true;
    }

    private void doUpdateAttributes(FieldUpdateRequest currentFieldUpdate, Element element, ArbilDataNode arbilDataNode, NamedNodeMap attributesMap) throws DOMException {
        final Map<String, Object> attributeValuesMap = currentFieldUpdate.attributeValuesMap;
        if (attributeValuesMap != null) {
            // Traverse values from attribute map
            for (Map.Entry<String, Object> attributeEntry : attributeValuesMap.entrySet()) {
                updateAttribute(element, attributeEntry.getKey(), attributeEntry.getValue());
            }
        }

        if (arbilDataNode.isCmdiMetaDataNode()) {
            updateCmdiAttributes(attributesMap, currentFieldUpdate, element);
        } else {
            updateImdiAttributes(attributesMap, currentFieldUpdate);
        }
    }

    private void updateAttribute(Element element, String attrPath, Object attrValue) throws DOMException {
        final Attr attrNode = getAttributeNodeFromPath(element, attrPath);

        if (attrValue == null || "".equals(attrValue)) {
            // Null or empty, remove if set
            if (attrNode != null) {
                element.removeAttributeNode(attrNode);
            }
        } else {
            // Value has been set, apply to document
            if (attrNode != null) {
                // Modify on existing attribute
                attrNode.setValue(attrValue.toString());
            } else {
                // Set new attribute
                addAttributeNodeFromPath(element, attrPath, attrValue.toString());
            }
        }
    }

    private void updateCmdiAttributes(NamedNodeMap attributesMap, FieldUpdateRequest currentFieldUpdate, Element element) throws DOMException {
        Node languageNode = attributesMap.getNamedItem("xml:lang");
        if (languageNode == null) {
            if (currentFieldUpdate.fieldLanguageId != null) {
                element.setAttribute("xml:lang", currentFieldUpdate.fieldLanguageId);
            }
        } else {
            if (currentFieldUpdate.fieldLanguageId == null) {
                element.removeAttribute("xml:lang");
            } else {
                languageNode.setNodeValue(currentFieldUpdate.fieldLanguageId);
            }
        }
    }

    private void updateImdiAttributes(NamedNodeMap attributesMap, FieldUpdateRequest currentFieldUpdate) throws DOMException {
        // isImdiMetadataNode()
        Node languageNode = attributesMap.getNamedItem("LanguageId");
        if (languageNode == null) {
            languageNode = attributesMap.getNamedItem("xml:lang");
        }
        if (languageNode != null) {
            languageNode.setNodeValue(currentFieldUpdate.fieldLanguageId);
        }


        if (currentFieldUpdate.keyNameValue != null) {
            Node keyNameNode = attributesMap.getNamedItem("Name");
            if (keyNameNode != null) {
                keyNameNode.setNodeValue(currentFieldUpdate.keyNameValue);
            }
        }
    }

    public URI insertFavouriteComponent(ArbilDataNode destinationArbilDataNode, ArbilDataNode favouriteArbilDataNode) throws ArbilMetadataException {
        URI returnUri = null;
        // this node has already been saved in the metadatabuilder which called this
        // but lets check this again in case this gets called elsewhere and to make things consistant
        String elementName = favouriteArbilDataNode.getURIFragment();
        //String insertBefore = destinationArbilDataNode.nodeTemplate.getInsertBeforeOfTemplate(elementName);
        String insertBefore = destinationArbilDataNode.getNodeTemplate().getInsertBeforeOfTemplate(elementName);
        logger.debug("insertBefore: {}", insertBefore);
        int maxOccurs = destinationArbilDataNode.getNodeTemplate().getMaxOccursForTemplate(elementName);
        logger.debug("maxOccurs: {}", maxOccurs);
        if (destinationArbilDataNode.getNeedsSaveToDisk(false)) {
            destinationArbilDataNode.saveChangesToCache(true);
        }
        try {
            Document favouriteDocument;
            synchronized (favouriteArbilDataNode.getParentDomLockObject()) {
                favouriteDocument = getDocument(favouriteArbilDataNode.getURI());
            }
            synchronized (destinationArbilDataNode.getParentDomLockObject()) {
                Document destinationDocument = getDocument(destinationArbilDataNode.getURI());
                String favouriteXpath = favouriteArbilDataNode.getURIFragment();
                String favouriteXpathTrimmed = favouriteXpath.replaceFirst("\\.[^(^.]+$", "");
                boolean onlySubNodes = !favouriteXpathTrimmed.equals(favouriteXpath);
                logger.debug("favouriteXpath: {}", favouriteXpathTrimmed);
                String destinationXpath;
                if (onlySubNodes) {
                    destinationXpath = favouriteXpathTrimmed;
                } else {
                    destinationXpath = favouriteXpathTrimmed.replaceFirst("\\.[^.]+$", "");
                }
                logger.debug("destinationXpath: {}", destinationXpath);

                destinationXpath = alignDestinationPathWithTarget(destinationXpath, destinationArbilDataNode);

                Node destinationNode = selectSingleNode(destinationDocument, destinationXpath);
                Node selectedNode = selectSingleNode(favouriteDocument, favouriteXpathTrimmed);
                Node importedNode = destinationDocument.importNode(selectedNode, true);
                Node[] favouriteNodes;
                if (onlySubNodes) {
                    NodeList selectedNodeList = importedNode.getChildNodes();
                    favouriteNodes = new Node[selectedNodeList.getLength()];
                    for (int nodeCounter = 0; nodeCounter < selectedNodeList.getLength(); nodeCounter++) {
                        favouriteNodes[nodeCounter] = selectedNodeList.item(nodeCounter);
                    }
                } else {
                    favouriteNodes = new Node[]{importedNode};
                }
                for (Node singleFavouriteNode : favouriteNodes) {
                    if (singleFavouriteNode.getNodeType() != Node.TEXT_NODE) {
                        insertNodeInOrder(destinationNode, singleFavouriteNode, insertBefore, maxOccurs);
//                        destinationNode.appendChild(singleFavouriteNode);
                        logger.debug("inserting favouriteNode: {}", singleFavouriteNode.getLocalName());
                    }
                }
                savePrettyFormatting(destinationDocument, destinationArbilDataNode.getFile());
                try {
                    String nodeFragment;
                    if (favouriteNodes.length != 1) {
                        nodeFragment = destinationXpath; // in this case show the target node
                    } else {
                        nodeFragment = convertNodeToNodePath(destinationDocument, favouriteNodes[0], destinationXpath);
                    }
                    logger.debug("nodeFragment: {}", nodeFragment);
                    // return the child node url and path in the xml
                    // first strip off any fragment then add the full node fragment
                    returnUri = new URI(destinationArbilDataNode.getURI().toString().split("#")[0] + "#" + nodeFragment);
                } catch (URISyntaxException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                }
            }
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        } catch (ParserConfigurationException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        } catch (SAXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        } catch (TransformerException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
        return returnUri;
    }

    /**
     * Aligns a destination path for a favourite with the target path (fragment)
     * within the target node
     *
     * Fixes issue reported in https://trac.mpi.nl/ticket/1157
     *
     * @param destinationXpath Destination path as provided by the favourite
     * @param destinationArbilDataNode target node
     * @return Aligned XPath, or original if could not be aligned
     */
    private String alignDestinationPathWithTarget(String destinationXpath, ArbilDataNode destinationArbilDataNode) {
        String targetFragment = destinationArbilDataNode.getURIFragment();
        if (targetFragment != null) { // If null, it's the document root

            // If container node, we want to know the actual level to add to, so remove container part from path
            if (destinationArbilDataNode.isContainerNode()) {
                targetFragment = targetFragment.substring(0, targetFragment.lastIndexOf("."));
            }

            // Canonicalize both destination path and target fragment
            String destinationXpathGeneric = destinationXpath.replaceAll("\\(\\d+\\)", "(x)");
            String targetFragmentGeneric = targetFragment.replaceAll("\\(\\d+\\)", "(x)");

            // Do they match up? Destination should begin with target
            if (destinationXpathGeneric.startsWith(targetFragmentGeneric)) {
                // Convert target fragment back into regular expression for matching with destination path
                final String commonPartRegEx = targetFragmentGeneric.replaceAll("\\(x\\)", "\\\\(\\\\d+\\\\)");
                String[] destinationXpathParts = destinationXpath.split(commonPartRegEx);
                if (destinationXpathParts.length == 0) {
                    // Complete match
                    destinationXpath = targetFragment;
                } else if (destinationXpathParts.length == 2) {
                    // Combine targetFragment with remainder from destination path
                    destinationXpath = targetFragment + destinationXpathParts[1];
                } else {
                    // Should not happen, either exact match or remainder
                    messageDialogHandler.addMessageDialogToQueue(services.getString("UNEXPECTED RELATION BETWEEN SOURCE AND TARGET PATHS. SEE ERROR LOG FOR DETAILS."), services.getString("INSERT NODE"));
                    BugCatcherManager.getBugCatcher().logError("destinationXpath: " + destinationXpath + "\ntargetFragment: " + targetFragment, null);
                }
            }
        }
        return destinationXpath;
    }

    public static boolean canInsertNode(Node destinationNode, Node addableNode, int maxOccurs) {
        if (maxOccurs > 0) {
            String addableName = addableNode.getLocalName();
            if (addableName == null) {
                addableName = addableNode.getNodeName();
            }
            NodeList childNodes = destinationNode.getChildNodes();
            int duplicateNodeCounter = 0;
            for (int childCounter = 0; childCounter < childNodes.getLength(); childCounter++) {
                String childsName = childNodes.item(childCounter).getLocalName();
                if (addableName.equals(childsName)) {
                    duplicateNodeCounter++;
                    if (duplicateNodeCounter >= maxOccurs) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    private final static Pattern attributePathPattern = Pattern.compile("^.*\\.@[^.]+$");
    private final static Pattern namespacePartPattern = Pattern.compile("\\{(.*)\\}");

    public static boolean pathIsAttribute(String pathString) {
        return attributePathPattern.matcher(pathString).matches();
    }

    public static boolean pathIsAttribute(String[] pathTokens) {
        return pathTokens.length > 0 && pathTokens[pathTokens.length - 1].startsWith("@");
    }

    /**
     *
     * @param path Full path (e.g. .CMD.Component.Test.@myattr) of attribute
     * @return Attribute, if found
     */
    public static Attr getAttributeNodeFromPath(Element parent, final String path) {
        try {
            if (pathIsAttribute(path)) {
                final String attributePart = path.replaceAll("^.*@", ""); // remove path suffix (including @) so only attribute remains
                Matcher matcher = (namespacePartPattern.matcher(attributePart)); // look for namespace part
                if (matcher.find()) {
                    String nsPart = URLDecoder.decode(matcher.group(1), "UTF-8"); // extract namespace part and decode
                    String localName = attributePart.replaceAll("\\{.*\\}", "");
                    return parent.getAttributeNodeNS(nsPart, localName);
                } else {
                    return parent.getAttributeNode(attributePart);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
        return null;
    }

    /**
     *
     * @param path Full path (e.g. .CMD.Component.Test.@myattr) of attribute
     * @return Successful creation
     */
    public static boolean addAttributeNodeFromPath(Element parent, final String path, final String value) {
        try {
            if (pathIsAttribute(path)) {
                final String attributePart = path.replaceAll("^.*@", ""); // remove path suffix (including @) so only attribute remains
                Matcher matcher = (namespacePartPattern.matcher(attributePart)); // look for namespace part
                if (matcher.find()) {
                    String nsPart = URLDecoder.decode(matcher.group(1), "UTF-8"); // extract namespace part and decode
                    String localName = attributePart.replaceAll("\\{.*\\}", "");
                    parent.setAttributeNS(nsPart, localName, value);
                } else {
                    parent.setAttribute(attributePart, value);
                }
                return true;
            }
        } catch (UnsupportedEncodingException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
        return false;
    }

    /**
     * URLEncode and replace dots so dots, slashes and colons in string won't
     * interfere with node path structure
     *
     * @param nsURI
     * @return Encoded nsURI
     */
    public static String encodeNsUriForAttributePath(String nsURI) {
        try {
            return URLEncoder.encode(nsURI, "UTF-8").replace(".", "%2E");
        } catch (UnsupportedEncodingException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
            return null;
        }
    }

    public Node insertNodeInOrder(Node destinationNode, Node addableNode, String insertBefore, int maxOccurs) throws TransformerException, ArbilMetadataException {
        if (!canInsertNode(destinationNode, addableNode, maxOccurs)) {
            throw new ArbilMetadataException("The maximum nodes of this type have already been added.\n");
        }
        final Node insertBeforeNode = getInsertBeforeNode(insertBefore, destinationNode);

        // find the node to add the new section to
        Node addedNode;
        if (insertBeforeNode != null) {
            logger.debug("inserting before: {}", insertBeforeNode.getNodeName());
            addedNode = destinationNode.insertBefore(addableNode, insertBeforeNode);
        } else {
            logger.debug("inserting");
            if (addableNode instanceof Attr) {
                if (destinationNode instanceof Element) {
                    addedNode = ((Element) destinationNode).setAttributeNode((Attr) addableNode);
                } else {
                    throw new ArbilMetadataException("Cannot insert attribute in node of this type: " + destinationNode.getNodeName());
                }
            } else {
                addedNode = destinationNode.appendChild(addableNode);
            }
        }
        return addedNode;
    }

    private Node getInsertBeforeNode(String insertBefore, Node destinationNode) {
        // todo: read the template for max occurs values and use them here and for all inserts
        if (insertBefore != null && insertBefore.length() > 0) {
            String[] insertBeforeArray = insertBefore.split(",");
            // find the node to add the new section before
            NodeList childNodes = destinationNode.getChildNodes();
            for (int childCounter = 0; childCounter < childNodes.getLength(); childCounter++) {
                String childsName = childNodes.item(childCounter).getLocalName();
                for (String currentInsertBefore : insertBeforeArray) {
                    if (currentInsertBefore.equals(childsName)) {
                        logger.debug("insertbefore: {}", childsName);
                        return childNodes.item(childCounter);
                    }
                }
            }
        }
        return null;
    }

    private String checkTargetXmlPath(String targetXmlPath, String cmdiComponentId) {
        // check for issues with the path
        if (targetXmlPath == null) {
            targetXmlPath = cmdiComponentId.replaceAll("\\.[^.]+$", "");
        } else if (targetXmlPath.replaceAll("\\(\\d+\\)", "").length() == cmdiComponentId.length()) {
            // trim the last path component if the destination equals the new node path
            // i.e. xsdPath: .CMD.Components.Session.Resources.MediaFile.Keys.Key into .CMD.Components.Session.Resources.MediaFile(1).Keys.Key
            targetXmlPath = targetXmlPath.replaceAll("\\.[^.]+$", "");
        }
        // make sure the target xpath has all the required parts
        String[] cmdiComponentArray = cmdiComponentId.split("\\.");
        String[] targetXmlPathArray = targetXmlPath.replaceAll("\\(\\d+\\)", "").split("\\.");
        StringBuilder arrayPathParts = new StringBuilder();
        for (int pathPartCounter = targetXmlPathArray.length; pathPartCounter < cmdiComponentArray.length - 1; pathPartCounter++) {
            logger.debug("adding missing path component: {}", cmdiComponentArray[pathPartCounter]);
            arrayPathParts.append('.');
            arrayPathParts.append(cmdiComponentArray[pathPartCounter]);
        }
        // end path corrections
        return targetXmlPath + arrayPathParts.toString();
    }

    /**
     * Adds a CMD component to a datanode
     *
     * @param arbilDataNode
     * @param targetXmlPath
     * @param cmdiComponentId
     * @return
     */
    public URI insertChildComponent(ArbilDataNode arbilDataNode, String targetXmlPath, String cmdiComponentId) {
        if (arbilDataNode.getNeedsSaveToDisk(false)) {
            arbilDataNode.saveChangesToCache(true);
        }
        synchronized (arbilDataNode.getParentDomLockObject()) {
            logger.debug("insertChildComponent: {}", cmdiComponentId);
            logger.debug("targetXmlPath: {}", targetXmlPath);
            targetXmlPath = checkTargetXmlPath(targetXmlPath, cmdiComponentId);

            logger.debug("trimmed targetXmlPath: {}", targetXmlPath);
            //String targetXpath = targetNode.getURIFragment();
            //logger.debug("targetXpath: " + targetXpath);
//            File cmdiNodeFile = imdiTreeObject.getFile();
            String nodeFragment = "";
            try {
                // load the schema
                SchemaType schemaType = getFirstSchemaType(arbilDataNode.getNodeTemplate().getTemplateFile());
                // load the dom
                Document targetDocument = getDocument(arbilDataNode.getURI());
                // insert the new section
                try {
//                printoutDocument(targetDocument);
                    Node AddedNode = insertSectionToXpath(targetDocument, targetDocument.getFirstChild(), schemaType, targetXmlPath, cmdiComponentId);
                    nodeFragment = convertNodeToNodePath(targetDocument, AddedNode, targetXmlPath);
                } catch (ArbilMetadataException exception) {
                    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                    return null;
                }
                // bump the history
                arbilDataNode.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, arbilDataNode.getFile()); // note that we want to make sure that this gets saved even without changes because we have bumped the history ant there will be no file otherwise
            } catch (IOException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            } catch (ParserConfigurationException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            } catch (SAXException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            }
//       diff_match_patch diffTool= new diff_match_patch();
//       diffTool.diff_main(targetXpath, targetXpath);
            try {
                logger.debug("nodeFragment: {}", nodeFragment);
                // return the child node url and path in the xml
                // first strip off any fragment then add the full node fragment
                return new URI(arbilDataNode.getURI().toString().split("#")[0] + "#" + nodeFragment);
            } catch (URISyntaxException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            }
        }
    }

    /**
     * Tests whether the specified CMD component can be added to the specified
     * datanode
     *
     * @param arbilDataNode
     * @param targetXmlPath
     * @param cmdiComponentId
     * @return
     */
    public boolean canInsertChildComponent(ArbilDataNode arbilDataNode, String targetXmlPath, String cmdiComponentId) {
        synchronized (arbilDataNode.getParentDomLockObject()) {
            targetXmlPath = checkTargetXmlPath(targetXmlPath, cmdiComponentId);
            try {
                // load the schema
                SchemaType schemaType = getFirstSchemaType(arbilDataNode);
                // load the dom
                Document targetDocument = getDocument(arbilDataNode.getURI());
                // insert the new section
                try {
                    return canInsertSectionToXpath(targetDocument, targetDocument.getFirstChild(), schemaType, targetXmlPath, cmdiComponentId);
                } catch (ArbilMetadataException exception) {
                    BugCatcherManager.getBugCatcher().logError(exception);
                    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), services.getString("INSERT NODE ERROR"));
                    return false;
                }
            } catch (IOException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return false;
            } catch (ParserConfigurationException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return false;
            } catch (SAXException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return false;
            } catch (DOMException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return false;
            }
        }
    }

    public void testRemoveArchiveHandles() {
        try {
            Document workingDocument = getDocument(new URI("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi"));
            removeArchiveHandles(workingDocument);
            printoutDocument(workingDocument);
        } catch (Exception exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
    }

    public void removeArchiveHandles(ArbilDataNode arbilDataNode) {
        synchronized (arbilDataNode.getParentDomLockObject()) {
            try {
                Document workingDocument = getDocument(arbilDataNode.getURI());
                removeArchiveHandles(workingDocument);
                savePrettyFormatting(workingDocument, arbilDataNode.getFile());
            } catch (Exception exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
            }
        }
    }

    private static void removeImdiDomIds(Document targetDocument) {
        String handleXpath = "/:METATRANSCRIPT[@id]|/:METATRANSCRIPT//*[@id]";
        try {
            NodeList domIdNodeList = XPathAPI.selectNodeList(targetDocument, handleXpath);
            for (int nodeCounter = 0; nodeCounter < domIdNodeList.getLength(); nodeCounter++) {
                Node domIdNode = domIdNodeList.item(nodeCounter);
                if (domIdNode != null) {
                    domIdNode.getAttributes().removeNamedItem("id");
                }
            }
        } catch (TransformerException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
    }

    private void removeArchiveHandles(Document targetDocument) {
        String handleXpath = "/:METATRANSCRIPT[@ArchiveHandle]|/:METATRANSCRIPT//*[@ArchiveHandle]";
        try {
            NodeList archiveHandleNodeList = XPathAPI.selectNodeList(targetDocument, handleXpath);
            for (int nodeCounter = 0; nodeCounter < archiveHandleNodeList.getLength(); nodeCounter++) {
                Node archiveHandleNode = archiveHandleNodeList.item(nodeCounter);
                if (archiveHandleNode != null) {
                    archiveHandleNode.getAttributes().removeNamedItem("ArchiveHandle");
                }
            }
        } catch (TransformerException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
    }

    /**
     * Looks up CMD/Resources/ResourceProxyList/ResourceProxy node with
     * resourceProxyId
     *
     * @param document
     * @param resourceProxyId
     * @return ProxyNode, if found (first if multiple found (should not occur));
     * otherwise null
     */
    private Node getResourceProxyNode(Document document, String resourceProxyId) {
        try {
            return selectSingleNode(document, getPathForResourceProxynode(resourceProxyId));
        } catch (TransformerException ex) {
            BugCatcherManager.getBugCatcher().logError("Exception while finding for removal resource proxy with id " + resourceProxyId, ex);
        }
        return null;
    }

    private String getPathForResourceProxynode(String resourceProxyId) {
        return ".CMD.Resources.ResourceProxyList.ResourceProxy[@id='" + resourceProxyId + "']";
    }

    private Node selectSingleNode(Document targetDocument, String targetXpath) throws TransformerException {
        String tempXpathArray[] = convertImdiPathToXPathOptions(targetXpath);
        if (tempXpathArray != null) {
            for (String tempXpath : tempXpathArray) {
                tempXpath = tempXpath.replaceAll("\\(", "[");
                tempXpath = tempXpath.replaceAll("\\)", "]");
//            tempXpath = "/CMD/Components/Session/MDGroup/Actors";
                logger.debug("tempXpath: {}", tempXpath);
                // find the target node of the xml
                Node returnNode = XPathAPI.selectSingleNode(targetDocument, tempXpath);
                if (returnNode != null) {
                    return returnNode;
                }
            }
        }
        return null;
    }

    private String[] convertImdiPathToXPathOptions(String targetXpath) {
        if (targetXpath == null) {
            return null;
        } else {
            // convert the syntax inherited from the imdi api into xpath
            // Because most imdi files use a name space syntax we need to try both queries
            return new String[]{
                targetXpath.replaceAll("\\.", "/"),
                targetXpath.replaceAll("\\.@([^.]*)$", "/@$1").replaceAll("\\.", "/:") // Attributes (.@) should not get colon hence the two steps
            };
        }

    }

    private Node insertSectionToXpath(Document targetDocument, Node documentNode, SchemaType schemaType, String targetXpath, String xsdPath) throws ArbilMetadataException {
        logger.debug("insertSectionToXpath");
        logger.debug("xsdPath: {}", xsdPath);
        logger.debug("targetXpath: {}", targetXpath);
        SchemaProperty foundProperty = null;
        String insertBefore = "";
        String strippedXpath = null;
        if (targetXpath == null) {
            documentNode = documentNode.getParentNode();
        } else {
            try {
                // test profile book is a good sample to test for errors; if you add Authors description from the root of the node it will cause a schema error but if you add from the author it is valid
                documentNode = selectSingleNode(targetDocument, targetXpath);
            } catch (TransformerException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            }
            strippedXpath = targetXpath.replaceAll("\\(\\d+\\)", "");
        }
        // at this point we have the xml node that the user acted on but next must get any additional nodes with the next section
        logger.debug("strippedXpath: {}", strippedXpath);
        for (String currentPathComponent : xsdPath.split("\\.")) {
            if (currentPathComponent.length() > 0) {
                foundProperty = null;
                for (SchemaProperty schemaProperty : schemaType.getProperties()) {
                    String currentName;
                    if (schemaProperty.isAttribute()) {
                        currentName = CmdiTemplate.getAttributePathSection(schemaProperty.getName());
                    } else {
                        currentName = schemaProperty.getName().getLocalPart();
                    }
                    //logger.debug("currentName: " + currentName);
                    if (foundProperty == null) {
                        if (schemaProperty.isAttribute()
                                ? currentPathComponent.equals("@" + currentName)
                                : currentPathComponent.equals(currentName)) {
                            foundProperty = schemaProperty;
                            insertBefore = "";
                        }
                    } else {
                        if (!schemaProperty.isAttribute()) {
                            if (insertBefore.length() < 1) {
                                insertBefore = currentName;
                            } else {
                                insertBefore = insertBefore + "," + currentName;
                            }
                        }
                    }
                }
                if (foundProperty == null) {
                    throw new ArbilMetadataException("failed to find the path in the schema: " + currentPathComponent);
                } else {
                    schemaType = foundProperty.getType();
                }
            }
        }
//        logger.debug("Adding marker node");
//        Element currentElement = targetDocument.createElement("MarkerNode");
//        currentElement.setTextContent(xsdPath);
//        documentNode.appendChild(currentElement);
        logger.debug("Adding destination sub nodes node to: {}", documentNode.getLocalName());
        Node addedNode = constructXml(foundProperty, xsdPath, targetDocument, null, documentNode, false);

        logger.debug("insertBefore: {}", insertBefore);
        int maxOccurs;
        if (foundProperty.getMaxOccurs() != null) {
            maxOccurs = foundProperty.getMaxOccurs().intValue();
        } else {
            maxOccurs = -1;
        }
        logger.debug("maxOccurs: {}", maxOccurs);
        if (insertBefore.length() > 0 || maxOccurs != -1) {
            if (addedNode instanceof Attr) {
                ((Element) documentNode).removeAttributeNode((Attr) addedNode);
            } else {
                documentNode.removeChild(addedNode);
            }
            try {
                insertNodeInOrder(documentNode, addedNode, insertBefore, maxOccurs);
            } catch (TransformerException exception) {
                throw new ArbilMetadataException(exception.getMessage());
            }
        }
        return addedNode;
    }

    private boolean canInsertSectionToXpath(Document targetDocument, Node documentNode, SchemaType schemaType, String targetXpath, String xsdPath) throws ArbilMetadataException {
        logger.debug("insertSectionToXpath");
        logger.debug("xsdPath: {}", xsdPath);
        logger.debug("targetXpath: {}", targetXpath);
        SchemaProperty foundProperty = null;
        String insertBefore = "";
        if (targetXpath == null) {
            documentNode = documentNode.getParentNode();
        } else {
            try {
                documentNode = selectSingleNode(targetDocument, targetXpath);
            } catch (TransformerException exception) {
                logger.error("Transformer exception while looking op node at path {} in target document {}", targetXpath, targetDocument.getBaseURI(), exception);
                return false;
            }
            if (documentNode == null) {
                logger.debug("Target document {} does not have a node at path {}", targetDocument.getBaseURI(), targetXpath);
                return false;
            }
        }
        // at this point we have the xml node that the user acted on but next must get any additional nodes with the next section
        for (String currentPathComponent : xsdPath.split("\\.")) {
            if (currentPathComponent.length() > 0) {
                foundProperty = null;
                for (SchemaProperty schemaProperty : schemaType.getProperties()) {
                    String currentName;
                    if (schemaProperty.isAttribute()) {
                        currentName = CmdiTemplate.getAttributePathSection(schemaProperty.getName());
                    } else {
                        currentName = schemaProperty.getName().getLocalPart();
                    }
                    //logger.debug("currentName: " + currentName);
                    if (foundProperty == null) {
                        if (schemaProperty.isAttribute()
                                ? currentPathComponent.equals("@" + currentName)
                                : currentPathComponent.equals(currentName)) {
                            foundProperty = schemaProperty;
                            insertBefore = "";
                        }
                    } else {
                        if (!schemaProperty.isAttribute()) {
                            if (insertBefore.length() < 1) {
                                insertBefore = currentName;
                            } else {
                                insertBefore = insertBefore + "," + currentName;
                            }
                        }
                    }
                }
                if (foundProperty == null) {
                    throw new ArbilMetadataException("failed to find the path in the schema: " + currentPathComponent);
                } else {
                    schemaType = foundProperty.getType();
                }
            }
        }
        logger.debug("Adding destination sub nodes node to: {}", documentNode.getLocalName());

        if (pathIsAttribute(xsdPath)) {
            return canInsertAttribute((Element) documentNode, foundProperty);
        } else {

            Node addedNode = constructXml(foundProperty, xsdPath, targetDocument, null, documentNode, false);

            logger.debug("insertBefore: {}", insertBefore);
            int maxOccurs;
            if (foundProperty.getMaxOccurs() != null) {
                maxOccurs = foundProperty.getMaxOccurs().intValue();
            } else {
                maxOccurs = -1;
            }
            logger.debug("maxOccurs: {}", maxOccurs);
            if (insertBefore.length() > 0 || maxOccurs != -1) {
                if (addedNode instanceof Attr) {
                    ((Element) documentNode).removeAttributeNode((Attr) addedNode);
                } else {
                    documentNode.removeChild(addedNode);
                }
                return canInsertNode(documentNode, addedNode, maxOccurs);
            }
            return true;
        }
    }

    private boolean canInsertAttribute(Element documentNode, SchemaProperty attributeSchemaProperty) {
        final QName attrName = attributeSchemaProperty.getName();
        if ((attrName.getNamespaceURI() != null && attrName.getNamespaceURI().length() > 0)
                ? documentNode.hasAttributeNS(attrName.getNamespaceURI(), attrName.getLocalPart())
                : documentNode.hasAttribute(attrName.getLocalPart())) {
            // Properties can only occur once, so if it's already there it can't be added again
            return false;
        } else {
            // Perhaps do some checking against schema, although we can safely assume it is allowed here by the schema
            return true;
        }
    }

    public String convertNodeToNodePath(Document targetDocument, Node documentNode, String targetXmlPath) {
        logger.debug("Calculating the added fragment");
        // count siblings to get the correct child index for the fragment
        int siblingCouter = 1;
        Node siblingNode = documentNode.getPreviousSibling();
        while (siblingNode != null) {
            if (documentNode.getNodeName().equals(siblingNode.getNodeName())) {
                siblingCouter++;
            }
            siblingNode = siblingNode.getPreviousSibling();
        }
        // get the current node name
        String nodeFragment = documentNode.getNodeName();
        if (documentNode instanceof Attr) {
            nodeFragment = "@" + nodeFragment;
        }
        String nodePathString = targetXmlPath + "." + nodeFragment + "(" + siblingCouter + ")";

//        String nodePathString = "";
//        Node parentNode = documentNode;
//        while (parentNode != null) {
//            // count siblings to get the correct child index for the fragment
//            int siblingCouter = 1;
//            Node siblingNode = parentNode.getPreviousSibling();
//            while (siblingNode != null) {
//                if (parentNode.getLocalName().equals(siblingNode.getLocalName())) {
//                    siblingCouter++;
//                }
//                siblingNode = siblingNode.getPreviousSibling();
//            }
//            // get the current node name
//            String nodeFragment = parentNode.getNodeName();
//
//            nodePathString = "." + nodeFragment + "(" + siblingCouter + ")" + nodePathString;
//            //logger.debug("nodePathString: " + nodePathString);
//            if (parentNode.isSameNode(targetDocument.getDocumentElement())) {
//                break;
//            }
//            parentNode = parentNode.getParentNode();
//        }
//        nodeFragment = "." + nodeFragment;
        logger.debug("nodeFragment: {}", nodePathString);
        logger.debug("targetXmlPath: {}", targetXmlPath);
        return nodePathString;
        //return childStartElement.
    }

    public URI createComponentFile(URI cmdiNodeFile, URI xsdFile, boolean addDummyData) {
        logger.trace("createComponentFile: {}: {}", cmdiNodeFile, xsdFile);
        try {
            Document workingDocument = getNewDocument();
            readSchema(workingDocument, xsdFile, addDummyData);

            savePrettyFormatting(workingDocument, new File(cmdiNodeFile));
            setDefaultCmdiHeaderInfo(cmdiNodeFile, xsdFile);
        } catch (IOException e) {
            BugCatcherManager.getBugCatcher().logError(e);
        } catch (ParserConfigurationException e) {
            BugCatcherManager.getBugCatcher().logError(e);
        } catch (SAXException e) {
            BugCatcherManager.getBugCatcher().logError(e);
        }
        return cmdiNodeFile;
    }

    private void setDefaultCmdiHeaderInfo(URI cmdiNodeFile, URI xsdFile) throws ParserConfigurationException, IOException, SAXException {
        // Reload DOM, needed for XPathAPI
        Document document = getDocument(cmdiNodeFile);
        // Construct header info, set and save
        CmdiHeaderInfo headerInfo = CmdiHeaderInfo.createDefault(xsdFile);
        try {
            setHeaderInfo(document, headerInfo);
            savePrettyFormatting(document, new File(cmdiNodeFile));
        } catch (TransformerException ex) {
            BugCatcherManager.getBugCatcher().logError("Could not set header info for new CMDI " + cmdiNodeFile, ex);
        }
    }

    /**
     * Caches schema type for data nodes as fetching the schema type is rather
     * expensive. This is not static, so only as long as this component builder
     * lives (e.g. during series of canInsertChildComponent calls)
     *
     * @param arbilDataNode
     * @return
     */
    private SchemaType getFirstSchemaType(ArbilDataNode arbilDataNode) {
        if (nodeSchemaTypeMap.containsKey(arbilDataNode)) {
            return nodeSchemaTypeMap.get(arbilDataNode);
        } else {
            SchemaType schemaType = getFirstSchemaType(arbilDataNode.getNodeTemplate().getTemplateFile());
            nodeSchemaTypeMap.put(arbilDataNode, schemaType);
            return schemaType;
        }
    }

    private SchemaType getFirstSchemaType(File schemaFile) {
        try {
            InputStream inputStream = new FileInputStream(schemaFile);
            try {
                //Since we're dealing with xml schema files here the character encoding is assumed to be UTF-8
                XmlOptions xmlOptions = new XmlOptions();
                xmlOptions.setCharacterEncoding("UTF-8");
//            CatalogDocument catalogDoc = CatalogDocument.Factory.newInstance(); 
                xmlOptions.setEntityResolver(new ArbilEntityResolver(sessionStorage.getOriginatingUri(schemaFile.toURI()))); // this schema file is in the cache and must be resolved back to the origin in order to get unresolved imports within the schema file
                //xmlOptions.setCompileDownloadUrls();
                SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(inputStream, xmlOptions)}, XmlBeans.getBuiltinTypeSystem(), xmlOptions);
                // there can only be a single root node so we just get the first one, note that the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef)
                return sts.documentTypes()[0];
            } catch (IOException e) {
                BugCatcherManager.getBugCatcher().logError(e);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            BugCatcherManager.getBugCatcher().logError(e);
        } catch (XmlException e) {
            // TODO: this is not really a good place to message this so modify to throw
            messageDialogHandler.addMessageDialogToQueue(services.getString("COULD NOT READ THE XML SCHEMA"), services.getString("ERROR INSERTING NODE"));
            BugCatcherManager.getBugCatcher().logError(e);
        }
        return null;
    }

    private void readSchema(Document workingDocument, URI xsdFile, boolean addDummyData) {
        File schemaFile;
        if (xsdFile.getScheme() == null || xsdFile.getScheme().length() == 0 || xsdFile.getScheme().toLowerCase().equals("file")) {
            // do not cache local xsd files
            schemaFile = new File(xsdFile);
        } else {
            schemaFile = sessionStorage.updateCache(xsdFile.toString(), 5, false);
        }
        SchemaType schemaType = getFirstSchemaType(schemaFile);
        constructXml(schemaType.getElementProperties()[0], "documentTypes", workingDocument, xsdFile.toString(), null, addDummyData);
    }

    private Node appendNode(Document workingDocument, String nameSpaceUri, Node parentElement, SchemaProperty schemaProperty, boolean addDummyData) {
        if (schemaProperty.isAttribute()) {
            return appendAttributeNode(workingDocument, nameSpaceUri, (Element) parentElement, schemaProperty, addDummyData);
        } else {
            return appendElementNode(workingDocument, nameSpaceUri, parentElement, schemaProperty, addDummyData);
        }
    }

    private Attr appendAttributeNode(Document workingDocument, String nameSpaceUri, Element parentElement, SchemaProperty schemaProperty, boolean addDummyData) {
        Attr currentAttribute = workingDocument.createAttributeNS(schemaProperty.getName().getNamespaceURI(), schemaProperty.getName().getLocalPart());
        if (schemaProperty.getDefaultText() != null) {
            currentAttribute.setNodeValue(schemaProperty.getDefaultText());
        }
        parentElement.setAttributeNode(currentAttribute);
        return currentAttribute;
    }

    private Element appendElementNode(Document workingDocument, String nameSpaceUri, Node parentElement, SchemaProperty schemaProperty, boolean addDummyData) {
//        Element currentElement = workingDocument.createElementNS("http://www.clarin.eu/cmd", schemaProperty.getName().getLocalPart());
        Element currentElement = workingDocument.createElementNS(CMD_NAMESPACE, schemaProperty.getName().getLocalPart());
        SchemaType currentSchemaType = schemaProperty.getType();
        for (SchemaProperty attributesProperty : currentSchemaType.getAttributeProperties()) {
            if (attributesProperty.getMinOccurs() != null && !attributesProperty.getMinOccurs().equals(BigInteger.ZERO)) {
                currentElement.setAttribute(attributesProperty.getName().getLocalPart(), attributesProperty.getDefaultText());
            }
        }
        if (parentElement == null) {
            // this is probably not the way to set these, however this will do for now (many other methods have been tested and all failed to function correctly)
            currentElement.setAttribute("CMDVersion", "1.1");
            currentElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            currentElement.setAttribute("xsi:schemaLocation", CMD_NAMESPACE + " " + nameSpaceUri);
            //          currentElement.setAttribute("xsi:schemaLocation", "cmd " + nameSpaceUri);
            workingDocument.appendChild(currentElement);
        } else {
            parentElement.appendChild(currentElement);
        }
        //currentElement.setTextContent(schemaProperty.getMinOccurs() + ":" + schemaProperty.getMinOccurs());
        return currentElement;
    }

    private Node constructXml(SchemaProperty currentSchemaProperty, String pathString, Document workingDocument, String nameSpaceUri, Node parentElement, boolean addDummyData) {
        Node returnNode = null;
        // this must be tested against getting the actor description not the actor of an imdi profile instance
        String currentPathString = pathString + "." + currentSchemaProperty.getName().getLocalPart();
        SchemaType currentSchemaType = currentSchemaProperty.getType();
        Node currentElement = appendNode(workingDocument, nameSpaceUri, parentElement, currentSchemaProperty, addDummyData);
        returnNode = currentElement;
        //logger.debug("printSchemaType " + schemaType.toString());
//        for (SchemaType schemaSubType : schemaType.getAnonymousTypes()) {
//            logger.debug("getAnonymousTypes:");
//            constructXml(schemaSubType, pathString + ".*getAnonymousTypes*", workingDocument, parentElement);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionConstituentTypes()) {
//            logger.debug("getUnionConstituentTypes:");
//            printSchemaType(schemaSubType);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionMemberTypes()) {
//            logger.debug("getUnionMemberTypes:");
//            constructXml(schemaSubType, pathString + ".*getUnionMemberTypes*", workingDocument, parentElement);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionSubTypes()) {
//            logger.debug("getUnionSubTypes:");
//            printSchemaType(schemaSubType);
//        }
        //SchemaType childType =schemaType.

        /////////////////////

        for (SchemaProperty schemaProperty : currentSchemaType.getElementProperties()) {
            //for (int childCounter = 0; childCounter < schemaProperty.getMinOccurs().intValue(); childCounter++) {
            // if the searched element is a child node of the given node return
            // its SchemaType
            //if (properties[i].getName().toString().equals(element)) {
            // if the searched element was not a child of the given Node
            // then again for each of these child nodes search recursively in
            // their child nodes, in the case they are a complex type, because
            // only complex types have child nodes
            //currentSchemaType.getAttributeProperties();
            //     if ((schemaProperty.getType() != null) && (!(currentSchemaType.isSimpleType()))) {

//            logger.debug("node name: " + schemaProperty.getName().getLocalPart());
//            logger.debug("node.getMinOccurs(): " + schemaProperty.getMinOccurs());
//            logger.debug("node.getMaxOccurs(): " + schemaProperty.getMaxOccurs());

            BigInteger maxNumberToAdd;
            if (addDummyData) {
                maxNumberToAdd = schemaProperty.getMaxOccurs();
                BigInteger dummyNumberToAdd = BigInteger.ONE.add(BigInteger.ONE).add(BigInteger.ONE);
                if (maxNumberToAdd == null) {
                    maxNumberToAdd = dummyNumberToAdd;
                } else {
                    if (dummyNumberToAdd.compareTo(maxNumberToAdd) == -1) {
                        // limit the number added and make sure it is less than the max number to add
                        maxNumberToAdd = dummyNumberToAdd;
                    }
                }
            } else {
                maxNumberToAdd = schemaProperty.getMinOccurs();
                if (maxNumberToAdd == null) {
                    maxNumberToAdd = BigInteger.ZERO;
                }
            }
            for (BigInteger addNodeCounter = BigInteger.ZERO; addNodeCounter.compareTo(maxNumberToAdd) < 0; addNodeCounter = addNodeCounter.add(BigInteger.ONE)) {
                constructXml(schemaProperty, currentPathString, workingDocument, nameSpaceUri, currentElement, addDummyData);
            }
        }
        return returnNode;
    }

    private void printoutDocument(Document workingDocument) {
        try {
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer transformer = tranFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source src = new DOMSource(workingDocument);
            Result dest = new StreamResult(System.out);
            transformer.transform(src, dest);
        } catch (Exception e) {
            BugCatcherManager.getBugCatcher().logError(e);
        }
    }
}
