/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data.metadatafile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader.CmdiResourceLink;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeService;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.IMDIVocabularies;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
//import nl.mpi.arbil.userstorage.ArbilConfiguration;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Document : MetadataReader
 *
 * @author Peter.Withers@mpi.nl
 */
public class MetadataReader {

    private final static Logger logger = LoggerFactory.getLogger(MetadataReader.class);
    static private MetadataReader singleInstance = null;

    static synchronized public MetadataReader getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new MetadataReader();
        }
        return singleInstance;
    }
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
        dataNodeLoader = dataNodeLoaderInstance;
    }
    private static ApplicationVersionManager versionManager;

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
        versionManager = versionManagerInstance;
    }
    /**
     * http://www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd
     */
    //public File selectedTemplateDirectory = null;
    public final static String imdiPathSeparator = ".";
    private static final ResourceBundle services = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");
//    private ArbilConfiguration applicationConfiguration = new ArbilConfiguration(); //TODO: make immutable and get injected through constructor (post singleton)
    public boolean copyNewResourcesToCache = false;
    private boolean verbatimXmlTreeStructure = false;

    // todo: this should probably be moved into the arbiltemplate class
    public boolean nodeCanExistInNode(ArbilDataNode targetDataNode, ArbilDataNode childDataNode) {
        String targetImdiPath = getNodePath((ArbilDataNode) targetDataNode);
        String childPath = getNodePath((ArbilDataNode) childDataNode);
        targetImdiPath = targetImdiPath.replaceAll("\\(\\d*?\\)", "\\(x\\)");
        childPath = childPath.replaceAll("\\(\\d*?\\)", "\\(x\\)");
        //        logger.debug("nodeCanExistInNode: " + targetImdiPath + " : " + childPath);
        int targetBranchCount = targetImdiPath.replaceAll("[^(]*", "").length();
        int childBranchCount = childPath.replaceAll("[^(]*", "").length();
        //        logger.debug("targetBranchCount: " + targetBranchCount + " childBranchCount: " + childBranchCount);
        boolean hasCorrectSubNodeCount = childBranchCount - targetBranchCount < 2;
        return hasCorrectSubNodeCount && !childPath.equals(targetImdiPath) && childPath.startsWith(targetImdiPath);
    }

    public static String getNodePath(ArbilDataNode targetDataNode) {
        //TODO: this should probably be moved into the imditreeobject
        String xpath;
        if (targetDataNode.isSession()) {
            xpath = imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session";
        } else if (targetDataNode.isCatalogue()) {
            xpath = imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Catalogue";
        } else {
            xpath = imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus";
        }
        Object[] nodePathArray = ((ArbilDataNode) targetDataNode).getUrlString().split("#");
        //        logger.debug("nodePath0: " + nodePathArray[0]);
        if (nodePathArray.length > 1) {
            String nodePath = nodePathArray[1].toString();
            xpath = nodePath;
            //            logger.debug("nodePath1: " + nodePath);
            // convert the dot path to xpath
            //            xpath = nodePath.replaceAll("(\\(.?\\))?\\.", ".");
            //                xpath = nodePath.replaceAll("(\\(.?\\))?", "/");
            //            logger.debug("xpath: " + xpath);
        }
        return xpath;
    }

    private URL constructTemplateUrl(String templateType) {
        URL templateUrl;
        if (CmdiProfileReader.pathIsProfile(templateType)) {
            try {
                return new URL(templateType);
            } catch (MalformedURLException ex) {
                BugCatcherManager.getBugCatcher().logError(ex);
                templateUrl = null;
            }
        } else {
            templateUrl = MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/" + templateType.substring(1) + ".xml");
        }

        if (templateUrl == null) {
            try {
                templateUrl = ArbilTemplateManager.getSingleInstance().getDefaultComponentOfTemplate(templateType).toURI().toURL();
            } catch (MalformedURLException exception) {
                BugCatcherManager.getBugCatcher().logError(exception);
                return null;
            }
        }

        return templateUrl;
    }

    public URI addFromTemplate(File destinationFile, String templateType) {
        logger.debug("addFromJarTemplateFile: {} : {}", templateType, destinationFile);

        // Get local url for template type
        URL templateUrl = constructTemplateUrl(templateType);
        if (templateUrl == null) {
            return null;
        }

        try {
            // Copy (1:1) template to new local file
            copyToDisk(templateUrl, destinationFile);
        } catch (IOException ex) {
            logger.warn("Could not copy template file from {} to {} due to an I/O exception: {}", templateUrl, destinationFile, ex.getMessage());
            logger.debug("Details for I/O exception while copying from template file", ex);
            return null;
        }

        URI addedPathUri = destinationFile.toURI();
        try {
            // Open new metadata file
            Document addedDocument = ArbilComponentBuilder.getDocument(addedPathUri);
            if (addedDocument == null) {
                messageDialogHandler.addMessageDialogToQueue(services.getString("ERROR INSERTING CREATE DATE"), services.getString("ADD FROM TEMPLATE"));
            } else {
                // Set some values to new instance of metadata file

                Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(addedDocument, "/:METATRANSCRIPT");
                NamedNodeMap metatranscriptAttributes = linkNode.getAttributes();

                // Set the arbil version to the present version
                ApplicationVersion currentVersion = versionManager.getApplicationVersion();
                String arbilVersionString = "Arbil." + currentVersion.currentMajor + "." + currentVersion.currentMinor + "." + currentVersion.currentRevision;

                //                todo: the template must be stored at this point
                //                if (!ArbilTemplateManager.getSingleInstance().defaultTemplateIsCurrentTemplate()) {
                //                    if (!templateType.equals(".METATRANSCRIPT.Corpus")) { // prevent corpus branches getting a template so that the global template takes effect
                //                        arbilVersionString = arbilVersionString + ":" + ArbilTemplateManager.getSingleInstance().getCurrentTemplateName();
                //                    }
                //                }
                arbilVersionString = arbilVersionString + ":" + metatranscriptAttributes.getNamedItem("Originator").getNodeValue();
                metatranscriptAttributes.getNamedItem("Originator").setNodeValue(arbilVersionString);
                //metatranscriptAttributes.getNamedItem("Type").setNodeValue(ArbilTemplateManager.getSingleInstance().getCurrentTemplateName());

                // Set the date field to the current data + time
                metatranscriptAttributes.getNamedItem("Date").setNodeValue(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                // Save new document in formatted XML
                ArbilComponentBuilder.savePrettyFormatting(addedDocument, new File(addedPathUri));
            }
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
        return addedPathUri;
    }

    private void copyToDisk(URL sourceURL, File targetFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = sourceURL.openStream();
            out = new FileOutputStream(targetFile);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public String getNodeTypeFromMimeType(String mimeType) {
        logger.debug("getNodeTypeFromMimeType: {}", mimeType);
        for (String[] formatType : new String[][]{
            {"http://www.mpi.nl/IMDI/Schema/WrittenResource-Format.xml", ".METATRANSCRIPT.Session.Resources.WrittenResource", "Manual/WrittenResource"},
            {"http://www.mpi.nl/IMDI/Schema/MediaFile-Format.xml", ".METATRANSCRIPT.Session.Resources.MediaFile", "Manual/MediaFile"}
        }) {
            if (formatType[2].equals(mimeType)) {
                logger.debug("UsingOverrideNodeType: {}", formatType[1]);
                return formatType[1];
            } else if (IMDIVocabularies.getSingleInstance().vocabularyContains(formatType[0], mimeType)) {
                logger.debug("NodeType: {}", formatType[1]);
                //                    if (mimeType.equals("image/jpeg")) {
                return formatType[1];
            }
        }
        return null;
    }

    private String getNamedAttributeValue(NamedNodeMap namedNodeMap, String attributeName) {
        Node nameNode = namedNodeMap.getNamedItem(attributeName);
        if (nameNode != null) {
            return nameNode.getNodeValue();
        } else {
            return null;
        }
    }

    /**
     * Checks whether the component builder will be able to insert a node of
     * specified type in the specified target DOM
     */
    public boolean canInsertFromTemplate(ArbilTemplate currentTemplate, String elementName, String targetXmlPath, Document targetImdiDom) throws ArbilMetadataException {
        // This may be done more efficiently, but for now we basically prepare
        // an insertion up to the point we have the destination node and
        // potentially addable node which we can pass to the component builder
        // and ask whether this can actually be done.

        int maxOccurs = currentTemplate.getMaxOccursForTemplate(elementName);
        if (maxOccurs < 0) {
            return true;
        }

        URI addedPathURI = null;
        try {
            String templateFileString = templateFileStringFromElementName(elementName);
            URL templateUrl = urlForTemplateFile(currentTemplate, templateFileString);
            String targetRef = xPathFromXmlPath(targetXmlPath, elementName);
            String targetXpath = xPathFromTargetRef(targetRef);

            if (templateUrl != null) {
                Document insertableSectionDoc = ArbilComponentBuilder.getDocument(templateUrl.toURI());

                if (insertableSectionDoc != null) {
                    Node insertableNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*");
                    if (insertableNode != null) {
                        Node addableNode = targetImdiDom.importNode(insertableNode, true);
                        Node destinationNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath);

                        return ArbilComponentBuilder.canInsertNode(destinationNode, addableNode, maxOccurs);
                    }
                }
            }
        } catch (URISyntaxException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        } catch (MalformedURLException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        } catch (DOMException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        } catch (ParserConfigurationException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        } catch (SAXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        } catch (TransformerException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
        }
        logger.debug("addedPathString: {}", addedPathURI);
        return false;
    }

    public URI insertFromTemplate(ArbilTemplate currentTemplate, URI targetMetadataUri, File resourceDestinationDirectory, String elementName, String targetXmlPath, Document targetImdiDom, URI resourceUrl, String mimeType) throws ArbilMetadataException {
        logger.trace("insertFromTemplate: {} : {}", elementName, resourceUrl);
        logger.trace("targetXpath: {}", targetXmlPath);
        String insertBefore = currentTemplate.getInsertBeforeOfTemplate(elementName);
        logger.trace("insertBefore: {}", insertBefore);
        final int maxOccurs = currentTemplate.getMaxOccursForTemplate(elementName);
        logger.trace("maxOccurs: {}", maxOccurs);
        URI addedPathURI = null;
        try {
            String templateFileString = templateFileStringFromElementName(elementName);
            URL templateUrl = urlForTemplateFile(currentTemplate, templateFileString);
            String targetRef = xPathFromXmlPath(targetXmlPath, elementName);
            String targetXpath = xPathFromTargetRef(targetRef);

            logger.debug("targetXpath: {}", targetXpath);
            logger.debug("templateUrl: {}", templateUrl);

            if (templateUrl == null) {
                messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(services.getString("NO TEMPLATE FOUND FOR"), elementName.substring(1)), "Load Template");
                logger.error("No template found for: {}", elementName.substring(1));
            } else {
                Document insertableSectionDoc = ArbilComponentBuilder.getDocument(templateUrl.toURI());

                if (insertableSectionDoc == null) {
                    messageDialogHandler.addMessageDialogToQueue(services.getString("ERROR READING TEMPLATE"), services.getString("INSERT FROM TEMPLATE"));
                } else {
                    // insert values into the section that about to be added
                    if (resourceUrl != null) {
                        insertValuesForAddingFromTemplate(insertableSectionDoc, resourceUrl, resourceDestinationDirectory, targetMetadataUri);
                    }
                    if (mimeType != null) {
                        insertMimeTypeForAddingFromTemplate(insertableSectionDoc, mimeType);
                    }

                    Node insertableNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*");
                    if (insertableNode == null) {
                        logger.error("InsertableSection not found in the template");
                    }
                    return importNodesAddedFromTemplate(targetImdiDom, targetMetadataUri, targetXpath, targetRef, insertableNode, insertBefore, maxOccurs);
                }
            }
        } catch (URISyntaxException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        } catch (MalformedURLException ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        } catch (DOMException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            return null;
        } catch (IOException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            return null;
        } catch (ParserConfigurationException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            return null;
        } catch (SAXException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            return null;
        } catch (TransformerException exception) {
            BugCatcherManager.getBugCatcher().logError(exception);
            return null;
        }
        logger.debug("addedPathString: {}", addedPathURI);
        return addedPathURI;
    }

    private URI importNodesAddedFromTemplate(Document targetImdiDom, URI targetMetadataUri, String targetXpath, String targetRef, Node insertableNode, String insertBefore, final int maxOccurs) throws URISyntaxException, DOMException, ArbilMetadataException, TransformerException {
        Node addableNode = targetImdiDom.importNode(insertableNode, true);
        Node destinationNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath);
        final ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
        Node addedNode = componentBuilder.insertNodeInOrder(destinationNode, addableNode, insertBefore, maxOccurs);
        String nodeFragment = componentBuilder.convertNodeToNodePath(targetImdiDom, addedNode, targetRef);
        //                            try {
        logger.debug("nodeFragment: {}", nodeFragment);
        // return the child node url and path in the xml
        // first strip off any fragment then add the full node fragment
        return new URI(targetMetadataUri.toString().split("#")[0] + "#" + nodeFragment);
    }

    private void insertMimeTypeForAddingFromTemplate(Document insertableSectionDoc, String mimeType) throws DOMException, TransformerException {
        if (mimeType.equals("image/jpeg")) {
            // TODO: consider replacing this with exif imdifields in the original imdiobject and doing a merge
            //                    Hashtable exifTags = getExifMetadata(resourcePath);
            //                    String dateExifTag = "date";
            //                    if (exifTags.contains(dateExifTag)) {
            //                        Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:MediaFile/:Date");
            //                        linkNode.setTextContent(exifTags.get(dateExifTag).toString());
            //                    }
        }
        Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*/:Format");
        linkNode.setTextContent(mimeType);
    }

    private void insertValuesForAddingFromTemplate(Document insertableSectionDoc, URI resourceUrl, File resourceDestinationDirectory, URI targetMetadataUri) throws UnsupportedEncodingException, DOMException, TransformerException {
        URI finalResourceUrl = resourceUrl;
        //                    String localFilePath = resourcePath; // will be changed when copied to the cache
        // copy the file to the imdi directory
        try {
            if (copyNewResourcesToCache) {
                //                            URL resourceUrl = new URL(resourcePath);
                //                    String resourcesDirName = "resources";
                File originalFile = new File(resourceUrl);
                int suffixIndex = originalFile.getName().lastIndexOf(".");
                String targetFilename = originalFile.getName().substring(0, suffixIndex);
                String targetSuffix = originalFile.getName().substring(suffixIndex);
                logger.debug("targetFilename: {} targetSuffix: {}", targetFilename, targetSuffix);
                ///////////////////////////////////////////////////////////////////////
                // use the nodes child directory
                File destinationFileCopy = new File(resourceDestinationDirectory, targetFilename + targetSuffix);
                int fileCounter = 0;
                while (destinationFileCopy.exists()) {
                    fileCounter++;
                    destinationFileCopy = new File(resourceDestinationDirectory, targetFilename + "(" + fileCounter + ")" + targetSuffix);
                }
                URI fullURI = destinationFileCopy.toURI();
                finalResourceUrl = targetMetadataUri.relativize(fullURI);
                //destinationFileCopy.getAbsolutePath().replace(destinationFile.getParentFile().getPath(), "./").replace("\\", "/").replace("//", "/");
                // for easy reading in the fields keep the file in the same directory
                //                        File destinationDirectory = new File(destinationFile.getParentFile().getPath());
                //                        File destinationFileCopy = File.createTempFile(targetFilename, targetSuffix, destinationDirectory);
                //                        localFilePath = "./" + destinationFileCopy.getName();
                ///////////////////////////////////////////////////////////////////////
                copyToDisk(resourceUrl.toURL(), destinationFileCopy);
                logger.debug("destinationFileCopy: {}", destinationFileCopy);
            }
        } catch (Exception ex) {
            //localFilePath = resourcePath; // link to the original file
            BugCatcherManager.getBugCatcher().logError(ex);
        }
        Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*/:ResourceLink");
        String decodeUrlString = URLDecoder.decode(finalResourceUrl.toString(), "UTF-8");
        linkNode.setTextContent(decodeUrlString);
    }

    private static String xPathFromTargetRef(String targetRef) {
        String targetXpath = targetRef;
        // convert to xpath for the api
        targetXpath = targetXpath.replace(".", "/:");
        targetXpath = targetXpath.replace(")", "]");
        targetXpath = targetXpath.replace("(", "[");
        return targetXpath;
    }

    private static String xPathFromXmlPath(String targetXmlPath, String elementName) {
        // prepare the parent node
        String targetXpath = targetXmlPath;
        if (targetXpath == null) {
            targetXpath = elementName;
        } else {
            // make sure we have a complete path
            // for instance METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language
            // requires /:METATRANSCRIPT/:Session/:MDGroup/:Actors/:Actor[6].Languages
            // not /:METATRANSCRIPT/:Session/:MDGroup/:Actors/:Actor[6]
            // the last path component (.Language) will be removed later
            String[] targetXpathArray = targetXpath.split("\\)");
            String[] elementNameArray = elementName.split("\\)");
            StringBuilder targetXpathSB = new StringBuilder();
            for (int partCounter = 0; partCounter < elementNameArray.length; partCounter++) {
                if (targetXpathArray.length > partCounter) {
                    targetXpathSB.append(targetXpathArray[partCounter]);
                } else {
                    targetXpathSB.append(elementNameArray[partCounter]);
                }
                targetXpathSB.append(')');
            }
            targetXpath = targetXpathSB.toString().replaceAll("\\)$", "");
        }
        targetXpath = targetXpath.substring(0, targetXpath.lastIndexOf("."));
        return targetXpath;
    }

    private static URL urlForTemplateFile(ArbilTemplate currentTemplate, String templateFileString) throws MalformedURLException {
        URL templateUrl;
        File templateFile = new File(currentTemplate.getTemplateComponentDirectory(), templateFileString + ".xml");
        logger.debug("templateFile: {}", templateFile.getAbsolutePath());
        if (templateFile.exists()) {
            templateUrl = templateFile.toURI().toURL();
        } else {
            templateUrl = MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/" + templateFileString + ".xml");
        }
        return templateUrl;
    }

    private static String templateFileStringFromElementName(String elementName) {
        String templateFileString = elementName.substring(1); //TODO: this level of path change should not be done here but in the original caller
        logger.debug("templateFileString: {}", templateFileString);
        templateFileString = templateFileString.replaceAll("\\(\\d*?\\)", "(x)");
        logger.debug("templateFileString(x): {}", templateFileString);
        templateFileString = templateFileString.replaceAll("\\(x\\)$", "");
        return templateFileString;
    }

    public URI correctLinkPath(URI parentPath, String linkString) {
        URI linkURI = null;
        try {
            if (!linkString.toLowerCase().startsWith("http:") && !linkString.toLowerCase().startsWith("file:")) {
                //                    linkPath = parentPath /*+ File.separatorChar*/ + fieldToAdd.getFieldValue();
                linkURI = parentPath.resolve(new URI(null, linkString, null));
            } else if (linkString.toLowerCase().startsWith("&root;")) {
                // some imdi files contain "&root;" in its link paths
                linkURI = parentPath.resolve(new URI(null, linkString.substring(6), null));
            } else {
                linkURI = parentPath.resolve(linkString);
            }
        } catch (URISyntaxException exception) {
            BugCatcherManager.getBugCatcher().logError(parentPath.toString() + " : " + linkString, exception);
        }
        //                    logger.debug("linkPath: " + linkPath);
        //                    linkPath = new URL(linkPath).getPath();
        // clean the path for the local file system
        //        linkURI = linkURI.replaceAll("/\\./", "/");
        //        linkURI = linkURI.substring(0, 6) + (linkURI.substring(6).replaceAll("[/]+/", "/"));
        //        while (linkURI.contains("/../")) {
        ////                        logger.debug("linkPath: " + linkPath);
        //            linkURI = linkURI.replaceFirst("/[^/]+/\\.\\./", "/");
        //        }
        //                    logger.debug("linkPathCorrected: " + linkPath);
        if (linkURI != null) {
            linkURI = ArbilDataNodeService.normaliseURI(linkURI);
        }
        //        logger.debug("linkURI: " + linkURI.toString());
        return linkURI;
    }

    private void showDomIdFoundMessage() {
        if (!dataNodeLoader.nodesNeedSave()) {
            // Note TG: it may be good to add something like 'non-critical error' or something, so users feel safe to
            // ignore this if they do not know what it means.
            messageDialogHandler.addMessageDialogToQueue(services.getString("A DOM ID ATTRIBUTE HAS BEEN FOUND IN ONE OR MORE FILES, THESE FILES WILL NEED TO BE SAVED TO CORRECT THIS."), services.getString("LOAD IMDI FILES"));
        }
    }

    /**
     * loop all nodes; each end node becomes a field; any node that passes
     * pathIsChildNode becomes a subnode in a node named by the result string of
     * pathIsChildNode; the id of the node that passes pathIsChildNode is stored
     * in the subnode to allow for deletion from the dom if needed
     *
     * @param parentNode
     * @param childLinks
     * @param startNode
     * @param nodePath
     * @param fullNodePath
     * @param parentChildTree
     * @param siblingNodePathCounter
     * @param nodeOrderCounter
     * @return
     */
    public int iterateChildNodes(ArbilDataNode parentNode, List<String[]> childLinks, Node startNode, final String nodePath, String fullNodePath,
            Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree //, Hashtable<ImdiTreeObject, ImdiField[]> readFields
            , Map<String, Integer> siblingNodePathCounter, int nodeOrderCounter, boolean shallowLoading) {
        //        logger.debug("iterateChildNodes: " + nodePath);
        if (!parentChildTree.containsKey(parentNode)) {
            parentChildTree.put(parentNode, new HashSet<ArbilDataNode>());
        }
        //        int nodeCounter = 0;
        // add the fields and nodes
        for (Node childNode = startNode; childNode != null; childNode = childNode.getNextSibling()) {
            final String localName = childNode.getLocalName();
            final NamedNodeMap childNodeAttributes = childNode.getAttributes();
            if (localName != null) {
                final String childNodePath = new StringBuilder(3).append(nodePath).append(MetadataReader.imdiPathSeparator).append(localName).toString();
                // todo: these filter strings should really be read from the metadata format
                if ((childNodePath).equals(".CMD.Header")) {
                    // parse the MdSelfLink and extract either the archive handle or the URI
                    for (Node headerNode = childNode.getFirstChild(); headerNode != null; headerNode = headerNode.getNextSibling()) {
                        if (MD_SELF_LINK.equals(headerNode.getLocalName())) {
                            final String textContent = headerNode.getTextContent();
                            if (textContent != null) {
                                if (textContent.startsWith(HDL)) {
                                    parentNode.archiveHandle = textContent;
                                } else if (textContent.startsWith(HTTPHDLHANDLENET)) {
                                    parentNode.archiveHandle = textContent.replace(HTTPHDLHANDLENET, HDL);
                                }
                                parentNode.archiveUri = textContent;
                            }
                        }
                    }
                    continue;
                }
                if ((childNodePath).equals(".CMD.Resources")) {
                    continue;
                }
                if ((childNodePath).equals(".Kinnate.Entity")) {
                    continue;
                }

                // get the xml node id
                if (childNodeAttributes != null) {
                    removeImdiNodeIds(childNodeAttributes, parentNode);
                }

                if (fullNodePath.length() == 0) {
                    getTemplate(childNode, parentNode, childNodeAttributes);
                }
                if (localName.equals("Corpus")) { //TODO: Only for IMDI
                    getImdiCatalogue(childNodeAttributes, parentNode, childLinks, parentChildTree);
                }

                final ArbilDataNode parentDomNode = parentNode.getParentDomNode();
                final ArbilTemplate parentNodeTemplate = parentDomNode.getNodeTemplate();

                final StringBuilder fullSubNodePath = new StringBuilder(fullNodePath).append(MetadataReader.imdiPathSeparator).append(localName);
                final String parentNodePath = determineParentPath(parentNode);
                final String combinedPath = parentNodePath + childNodePath;
                final String childsMetaNode = parentNodeTemplate.pathIsChildNode(combinedPath);
                final int maxOccurs = parentNodeTemplate.getMaxOccursForTemplate(combinedPath);

                ArbilDataNode destinationNode;
                String siblingNodePath = childNodePath;
                if (childsMetaNode != null) {
                    try {
                        ArbilDataNode metaNode = null;
                        String pathUrlXpathSeparator = "";
                        if (!parentNode.getUrlString().contains("#")) {
                            pathUrlXpathSeparator = "#";
                        }
                        StringBuilder siblingSpacer;
                        boolean isSingleton = false;
                        // Build URI for metaNode or subNode
                        final StringBuilder nodeURIStringBuilder = new StringBuilder(4).append(parentNode.getURI().toString()).append(pathUrlXpathSeparator).append(siblingNodePath);
                        if ((!verbatimXmlTreeStructure && (maxOccurs > 1 || maxOccurs == -1))
                                || !(parentDomNode.nodeTemplate instanceof CmdiTemplate)) { // this version of the metanode creation should always be run for imdi files 
                            isSingleton = maxOccurs == 1; // 
                            metaNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));
                            metaNode.setParentDomNode(parentDomNode);

                            metaNode.setNodeText(childsMetaNode); // + "(" + localName + ")" + metaNodeImdiTreeObject.getURIFragment());
                            if (!parentChildTree.containsKey(metaNode)) {
                                parentChildTree.put(metaNode, new HashSet<ArbilDataNode>());
                            }
                            if (!isSingleton) {
                                // Add metanode to tree
                                parentChildTree.get(parentNode).add(metaNode);
                            }
                            // add brackets to conform with the imdi api notation
                            siblingSpacer = new StringBuilder(3).append("(").append(parentChildTree.get(metaNode).size() + 1).append(")");
                        } else {
                            int siblingCount = countSiblings(parentChildTree, parentNode, localName);
                            siblingSpacer = new StringBuilder(3).append("(").append(siblingCount).append(")");
//                            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(localName + " : " + childsMetaNode + " : " + maxOccurs, "filtered metanode");
                        }
                        fullSubNodePath.append(siblingSpacer);
                        // For subnode URI 
                        nodeURIStringBuilder.append(siblingSpacer);
                        ArbilDataNode subNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));
                        subNode.setParentDomNode(parentDomNode);

                        if (metaNode != null && !isSingleton) {
                            // Add subnode to metanode
                            parentChildTree.get(metaNode).add(subNode);
                            metaNode.setContainerNode(true);
                        } else {
                            // Add subnode directly to parent
                            parentChildTree.get(parentNode).add(subNode);
                            subNode.setSingletonMetadataNode(isSingleton);
                        }
                        //                parentNode.attachChildNode(metaNodeImdiTreeObject);
                        //                metaNodeImdiTreeObject.attachChildNode(subNodeImdiTreeObject);
                        if (!parentChildTree.containsKey(subNode)) {
                            parentChildTree.put(subNode, new HashSet<ArbilDataNode>());
                        }
                        destinationNode = subNode;
                    } catch (URISyntaxException ex) {
                        destinationNode = parentNode;
                        BugCatcherManager.getBugCatcher().logError(ex);
                    }
                    siblingNodePath = "";
                } else {
                    destinationNode = parentNode;
                }
                if (!shallowLoading || destinationNode == parentDomNode) {
                    nodeOrderCounter = enterChildNodesRecursion(parentNode, childLinks, childNode, childNodeAttributes, destinationNode, localName,
                            parentNodePath, siblingNodePath, fullSubNodePath.toString(), parentChildTree, siblingNodePathCounter, nodeOrderCounter, shallowLoading);
                }
            }
        }
        return nodeOrderCounter;
    }
    private static final String MD_SELF_LINK = "MdSelfLink";
    private static final String HDL = "hdl:";
    private static final String HTTPHDLHANDLENET = "http://hdl.handle.net/";

    /**
     * Updates counters and enters recursive iteration for child nodes. Also
     * adds referenced resources to the tree
     */
    private int enterChildNodesRecursion(ArbilDataNode parentNode, List<String[]> childLinks, Node childNode, NamedNodeMap childNodeAttributes,
            ArbilDataNode destinationNode, String localName, String parentNodePath, String siblingNodePath, String fullSubNodePath,
            Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree, Map<String, Integer> siblingNodePathCounter, int nodeOrderCounter, boolean shallowLoading) throws DOMException {

        final NodeList childNodes = childNode.getChildNodes();
        final boolean shouldAddCurrent = ((childNodes.getLength() == 0 && localName != null)
                || (childNodes.getLength() == 1 && childNodes.item(0).getNodeType() == Node.TEXT_NODE));
        // calculate the xpath index for multiple fields like description
        if (!siblingNodePathCounter.containsKey(fullSubNodePath)) {
            siblingNodePathCounter.put(fullSubNodePath, 0);
        } else {
            siblingNodePathCounter.put(fullSubNodePath, siblingNodePathCounter.get(fullSubNodePath) + 1);
        }
        if (parentNode.getParentDomNode().getNodeTemplate().pathIsEditableField(parentNodePath + siblingNodePath)) {
            // is a leaf not a branch
            final String fieldValue = (childNodes.getLength() == 1) ? childNodes.item(0).getTextContent() : "";
            nodeOrderCounter = addEditableField(nodeOrderCounter, destinationNode, siblingNodePath, fieldValue, siblingNodePathCounter, fullSubNodePath, parentNode, childLinks, parentChildTree, childNodeAttributes, shouldAddCurrent);
        } else {
            // for a branch, check if there are referenced resources to add
            addReferencedResources(parentNode, parentChildTree, childNodeAttributes, childLinks, destinationNode);
            // and add all editable component attributes as field
            if (childNodeAttributes != null) {
                if (parentNode.isCmdiMetaDataNode()) {
                    nodeOrderCounter = addCmdiAttributeFields(nodeOrderCounter, childNodeAttributes, destinationNode, siblingNodePath, siblingNodePathCounter, fullSubNodePath, parentNode, childLinks, parentChildTree);
                }
            }
        }

        return iterateChildNodes(destinationNode, childLinks, childNode.getFirstChild(), siblingNodePath, fullSubNodePath, parentChildTree, siblingNodePathCounter, nodeOrderCounter, shallowLoading);
    }

    private int countSiblings(Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree, ArbilDataNode parentNode, String localName) {
        // todo: this might need to be revisited
        // this version of the metanode code is for cmdi nodes only and only when there can only be one node instance
        int siblingCount = 1;
        for (ArbilDataNode siblingNode : parentChildTree.get(parentNode)) {
            String siblingPath = siblingNode.getURIFragment();
            if (siblingPath != null) {
                siblingPath = siblingPath.substring(siblingPath.lastIndexOf(".") + 1);
                siblingPath = siblingPath.replaceAll("\\(\\d+\\)", "");
                if (localName.equals(siblingPath)) {
                    siblingCount++;
                }
            }
        }
        return siblingCount;
    }

    private void removeImdiNodeIds(NamedNodeMap attributesMap, ArbilDataNode parentNode) {
        // look for node id attribites that should be removed from imdi files
        if (attributesMap.getNamedItem("id") != null) {
            if (!parentNode.hasDomIdAttribute) {
                if (!parentNode.isCmdiMetaDataNode()) {
                    // only if this is an imdi file we will require the node to be saved which will remove the dom id attributes
                    parentNode.hasDomIdAttribute = true;
                    showDomIdFoundMessage();
                    parentNode.setDataNodeNeedsSaveToDisk(null, false);
                }
            }
        } // end get the xml node id
    }

    private int addEditableField(int nodeOrderCounter, ArbilDataNode destinationNode, String siblingNodePath, String fieldValue, Map<String, Integer> siblingNodePathCounter, String fullSubNodePath, ArbilDataNode parentNode, List<String[]> childLinks, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree, NamedNodeMap childNodeAttributes, boolean shouldAddCurrent) {
        // Handle special attributes
        final String cvType;
        final String cvUrlString;
        final String keyName;
        final String languageId;
        final String archiveHandle;
        if (childNodeAttributes != null) {
            // IMDI only...
            cvType = getNamedAttributeValue(childNodeAttributes, "Type");
            // IMDI only...
            cvUrlString = getNamedAttributeValue(childNodeAttributes, "Link");
            // IMDI only...
            archiveHandle = getNamedAttributeValue(childNodeAttributes, "ArchiveHandle");

            final String languageIdAttribute = getNamedAttributeValue(childNodeAttributes, "LanguageId");
            if (languageIdAttribute != null) {
                // LanguageId attribute - IMDI only
                languageId = languageIdAttribute;
            } else {
                // xml:lang attribute - CMDI and generic XML
                languageId = getNamedAttributeValue(childNodeAttributes, "xml:lang");
            }
            // Key name - IMDI only
            keyName = getNamedAttributeValue(childNodeAttributes, "Name");
        } else {
            cvType = null;
            cvUrlString = null;
            keyName = null;
            languageId = null;
            archiveHandle = null;
        }

        final List<String[]> attributePaths;
        final Map<String, Object> attributesValueMap;
        final boolean allowsLanguageId;
        if (destinationNode.isCmdiMetaDataNode()) {
            // For CMDI nodes, get field attribute paths from schema and values from document before creating arbil field
            final String nodePath = fullSubNodePath.replaceAll("\\(\\d+\\)", "");
            CmdiTemplate template = (CmdiTemplate) destinationNode.getNodeTemplate();
            attributePaths = template.getEditableAttributesForPath(nodePath);
            attributesValueMap = new HashMap<String, Object>();
            if (childNodeAttributes != null) {
                for (int i = 0; i < childNodeAttributes.getLength(); i++) {
                    final Node attrNode = childNodeAttributes.item(i);
                    final String path = nodePath + ".@" + CmdiTemplate.getAttributePathSection(attrNode.getNamespaceURI(), attrNode.getLocalName());
                    attributesValueMap.put(path, attrNode.getNodeValue());
                }
            }
            allowsLanguageId = template.pathAllowsLanguageId(nodePath); //CMDI case where language id is optional as specified by schema
        } else {
            // IMDI case where language id comes from template
            allowsLanguageId = languageId != null;
            // No custom attributes
            attributePaths = null;
            attributesValueMap = null;
        }

        // is a leaf not a branch
        final ArbilField fieldToAdd = new ArbilField(nodeOrderCounter++, destinationNode, siblingNodePath, fieldValue, siblingNodePathCounter.get(fullSubNodePath), allowsLanguageId, attributePaths, attributesValueMap);
        // IMDI can contain archive handles on resource links and this must be added to the field because resource links are stored as a field object
        fieldToAdd.setArchiveHandle(archiveHandle);

// TODO: about to write this function
        //GuiHelper.imdiSchema.convertXmlPathToUiPath();
        // TODO: keep track of actual valid values here and only add to siblingCounter if siblings really exist
        // TODO: note that this method does not use any attributes without a node value
        //            if (childNode.getLocalName() != null) {
        //                nodeCounter++;
        //logger.debug("nodeCounter: " + nodeCounter + ":" + childNode.getLocalName());
        //            }
        if (childNodeAttributes != null) {
            fieldToAdd.setFieldAttribute(cvType, cvUrlString, languageId, keyName);
            if (fieldToAdd.xmlPath.endsWith("Description")) {
                if (cvUrlString != null && cvUrlString.length() > 0) {
                    // TODO: this field sould be put in the link node not the parent node
                    URI correcteLink = correctLinkPath(parentNode.getURI(), cvUrlString);
                    childLinks.add(new String[]{correcteLink.toString(), "Info Link"});
                    ArbilDataNode descriptionLinkNode = dataNodeLoader.getArbilDataNodeWithoutLoading(correcteLink);
                    descriptionLinkNode.isInfoLink = true;
                    descriptionLinkNode.setLoadingState(ArbilDataNode.LoadingState.LOADED);
                    parentChildTree.get(parentNode).add(descriptionLinkNode);
                    descriptionLinkNode.addField(fieldToAdd);
                }
            }
            addReferencedResources(parentNode, parentChildTree, childNodeAttributes, childLinks, destinationNode);
        }

        if (shouldAddCurrent) {
            if (fieldToAdd.isDisplayable()) {
                // add as field
                destinationNode.addField(fieldToAdd);
            } else if (fieldToAdd.xmlPath.startsWith(".METATRANSCRIPT.Corpus.CorpusLink") && fieldValue.length() > 0) {
                // add corpus link
                URI linkPath = correctLinkPath(parentNode.getURI(), fieldToAdd.getFieldValue());
                childLinks.add(new String[]{linkPath.toString(), "IMDI Link"});
                ArbilDataNode linkedNode = dataNodeLoader.getArbilDataNodeWithoutLoading(linkPath);
                linkedNode.setNodeText(fieldToAdd.getKeyName());
                parentChildTree.get(parentNode).add(linkedNode);
            }
        }
        fieldToAdd.finishLoading();
        return nodeOrderCounter;
    }

    private int addCmdiAttributeFields(int nodeOrderCounter, NamedNodeMap childNodeAttributes, ArbilDataNode destinationNode, String siblingNodePath, Map<String, Integer> siblingNodePathCounter, String fullSubNodePath, ArbilDataNode parentNode, List<String[]> childLinks, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) throws DOMException {
        for (int i = 0; i < childNodeAttributes.getLength(); i++) {
            Node attrNode = childNodeAttributes.item(i);
            String attrName = CmdiTemplate.getAttributePathSection(attrNode.getNamespaceURI(), attrNode.getLocalName());
            String attrPath = siblingNodePath + ".@" + attrName;
            String fullAttrPath = fullSubNodePath + ".@" + attrName;
            if (!siblingNodePathCounter.containsKey(fullAttrPath)) {
                siblingNodePathCounter.put(fullAttrPath, 0);
            }
            if (parentNode.getNodeTemplate().pathIsEditableField(fullAttrPath.replaceAll("\\(\\d*?\\)", ""))) {
                nodeOrderCounter = addEditableField(nodeOrderCounter,
                        destinationNode,
                        attrPath, //siblingNodePath,
                        attrNode.getNodeValue(),
                        siblingNodePathCounter,
                        fullAttrPath, //fullSubNodePath,
                        parentNode,
                        childLinks,
                        parentChildTree,
                        null, // don't pass childNodeAttributes as they're the parent's attributes 
                        true);
            }
        }
        return nodeOrderCounter;
    }

    private void addReferencedResources(ArbilDataNode parentNode, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree, NamedNodeMap childNodeAttributes, List<String[]> childLinks, ArbilDataNode destinationNode) {
        String clarinRefIds = getNamedAttributeValue(childNodeAttributes, "ref");
        if (clarinRefIds != null && clarinRefIds.length() > 0) {
            CmdiComponentLinkReader cmdiComponentLinkReader = parentNode.getCmdiComponentLinkReader();
            if (cmdiComponentLinkReader != null) {
                for (String refId : clarinRefIds.split(" ")) {
                    refId = refId.trim();
                    if (refId.length() > 0) {
                        CmdiResourceLink clarinLink = cmdiComponentLinkReader.getResourceLink(refId);
                        addResourceLinkNode(parentNode, destinationNode, parentChildTree, clarinLink, childLinks);
                    }
                }
            }
        }
    }

    /**
     * Add all unreferenced resources in a document to the parent node
     *
     * @param parentNode Parent node, to which resources will be added
     * @param parentChildTree Parent-child tree that is constructed
     * @param childLinks Child links collection that is constructed
     */
    public void addUnreferencedResources(ArbilDataNode parentNode, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree, List<String[]> childLinks) {
        CmdiComponentLinkReader cmdiComponentLinkReader = parentNode.getCmdiComponentLinkReader();
        if (cmdiComponentLinkReader != null) {
            for (CmdiResourceLink link : cmdiComponentLinkReader.cmdiResourceLinkArray) {
                if (link.getReferencingNodesCount() == 0) {
                    addResourceLinkNode(parentNode, parentNode, parentChildTree, link, childLinks);
                }
            }
        }
    }

    private void addResourceLinkNode(ArbilDataNode parentNode, ArbilDataNode destinationNode, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree, CmdiResourceLink clarinLink, List<String[]> childLinks) {
        if (clarinLink != null) {
            try {
                URI linkURI = clarinLink.getLinkUri();
                if (linkURI != null) {
                    linkURI = parentNode.getURI().resolve(linkURI);
                    childLinks.add(new String[]{clarinLink.toString(), clarinLink.resourceProxyId});
                    final ArbilDataNode resourceLinkNode = dataNodeLoader.getArbilDataNodeWithoutLoading(linkURI);
                    // Unless resource proxy type is metadata, treat as resource
                    resourceLinkNode.setResourceNode(!clarinLink.resourceType.equals("Metadata"));
                    parentChildTree.get(destinationNode).add(resourceLinkNode);
                    clarinLink.addReferencingNode();
                }
            } catch (URISyntaxException ex) {
                BugCatcherManager.getBugCatcher().logError("Error while reading resource link. Link not added: " + clarinLink.resourceRef, ex);
            }
        }
    }

    private String determineParentPath(ArbilDataNode parentNode) {
        String parentNodePath = parentNode.getURIFragment();
        if (parentNodePath == null) {
            // pathIsChildNode needs to have the entire path of the node not just the local part
            parentNodePath = "";
        } else {
            parentNodePath = parentNodePath.replaceAll("\\(\\d+\\)", "");
        }
        return parentNodePath;
    }

    private void getTemplate(Node childNode, ArbilDataNode parentNode, NamedNodeMap attributesMap) throws DOMException {
        // if this is the first node and it is not metatranscript then it is not an imdi so get the clarin template
        if (!childNode.getLocalName().equals("METATRANSCRIPT")) {
            // change made for clarin
            // TODO: for some reason getNamespaceURI does not retrieve the uri so we are resorting to simply gettting the attribute
            //                    logger.debug("startNode.getNamespaceURI():" + startNode.getNamespaceURI());
            //                    logger.debug("childNode.getNamespaceURI():" + childNode.getNamespaceURI());
            //                    logger.debug("schemaLocation:" + childNode.getAttributes().getNamedItem("xsi:schemaLocation"));
            //                    logger.debug("noNamespaceSchemaLocation:" + childNode.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation"));
            String schemaLocationString = null;
            Node schemaLocationNode = childNode.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation");
            if (schemaLocationNode == null) {
                schemaLocationNode = childNode.getAttributes().getNamedItem("xsi:schemaLocation");
            }
            if (schemaLocationNode != null) {
                schemaLocationString = schemaLocationNode.getNodeValue();
                String[] schemaLocation = schemaLocationString.split("\\s");
                schemaLocationString = schemaLocation[schemaLocation.length - 1];
                schemaLocationString = parentNode.getURI().resolve(schemaLocationString).toString();
                // this method of extracting the url has to accommadate many formatting variants such as \r\n or extra spaces
                // this method also assumes that the xsd url is fully resolved
                parentNode.nodeTemplate = ArbilTemplateManager.getSingleInstance().getCmdiTemplate(schemaLocationString);
            } else {
                final String message = "Could not find the schema url: " + childNode.toString();
                BugCatcherManager.getBugCatcher().logError(new Exception(message));
                messageDialogHandler.addMessageDialogToQueue(services.getString("COULD NOT FIND THE SCHEMA URL, SOME NODES WILL NOT DISPLAY CORRECTLY."), services.getString("CMDI SCHEMA LOCATION"));
            }
            //if (schemaLocation != null && schemaLocation.length > 0) {
		/*
             * // TODO: pass the resource node to a class to handle the resources
             * childNode = childNode.getAttributes().getNamedItem("Components");
             * nodeCounter = iterateChildNodes(parentNode, childLinks, childNode, nodePath, parentChildTree, nodeCounter);
             * break;
             */
        } else {
            // this is an imdi file so get an imdi template etc
            if (attributesMap != null) {
                // these attributes exist only in the metatranscript node
                Node archiveHandleAtt = attributesMap.getNamedItem("ArchiveHandle");
                if (archiveHandleAtt != null) {
                    parentNode.archiveHandle = archiveHandleAtt.getNodeValue();
                } else {
                    parentNode.archiveHandle = null;
                }
                Node templateOriginatorAtt = attributesMap.getNamedItem("Originator");
                if (templateOriginatorAtt != null) {
                    String templateOriginator = templateOriginatorAtt.getNodeValue();
                    int separatorIndex = templateOriginator.indexOf(":");
                    if (separatorIndex > -1) {
                        parentNode.nodeTemplate = ArbilTemplateManager.getSingleInstance().getTemplate(templateOriginator.substring(separatorIndex + 1));
                    } else {
                        // TODO: this is redundant but is here for backwards compatability
                        Node templateTypeAtt = attributesMap.getNamedItem("Type");
                        if (templateTypeAtt != null) {
                            String templateType = templateTypeAtt.getNodeValue();
                            // most of the time this will return the default template, but if the named template exixts it will be used
                            parentNode.nodeTemplate = ArbilTemplateManager.getSingleInstance().getTemplate(templateType);
                        }
                    }
                }
            }
        }
    }

    private void getImdiCatalogue(NamedNodeMap attributesMap, ArbilDataNode parentNode, List<String[]> childLinks, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) throws DOMException {
        // get the imdi catalogue if it exists
        Node catalogueLinkAtt = attributesMap.getNamedItem("CatalogueLink");
        if (catalogueLinkAtt != null) {
            String catalogueLink = catalogueLinkAtt.getNodeValue();
            if (catalogueLink.length() > 0) {
                URI correcteLink = correctLinkPath(parentNode.getURI(), catalogueLink);
                childLinks.add(new String[]{correcteLink.toString(), "CatalogueLink"});
                parentChildTree.get(parentNode).add(dataNodeLoader.getArbilDataNodeWithoutLoading(correcteLink));
            }
        }
    }

//    public void setApplicationConfiguration(ArbilConfiguration applicationConfiguration) {
//        this.applicationConfiguration = applicationConfiguration;
//    }
}
