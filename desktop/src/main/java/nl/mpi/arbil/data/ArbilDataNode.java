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
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.ImageIcon;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.data.metadatafile.CmdiUtils;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.data.metadatafile.MetadataUtils;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.util.ArrayComparator;
import nl.mpi.arbil.util.MimeHashQueue.TypeCheckerState;
import nl.mpi.flap.model.DataField;
import nl.mpi.flap.model.DataNodeType;
import nl.mpi.flap.model.FieldGroup;
import nl.mpi.flap.model.PluginDataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilDataNode formerly known as ImdiTreeObject
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNode extends ArbilNode implements Comparable, PluginDataNode {

    private final static Logger logger = LoggerFactory.getLogger(ArbilDataNode.class);
    private final static MetadataFormat metadataFormat = new MetadataFormat();
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    public static enum LoadingState {

        UNLOADED,
        PARTIAL,
        LOADED
    }
    private ArbilDataNodeService dataNodeService;
    public MetadataUtils metadataUtils;
    public ArbilTemplate nodeTemplate;
    private HashMap<String, ArbilField[]> fieldHashtable; //// TODO: this should be changed to a vector or contain an array so that duplicate named fields can be stored ////
    protected ArbilDataNode[] childArray = new ArbilDataNode[0];
    //private boolean dataLoaded;
    private LoadingState loadingState = LoadingState.UNLOADED;
    private LoadingState requestedLoadingState = null;
    public int resourceFileServerResponse = -1; // -1 = not set otherwise this will be the http response code
    public String hashString;
    public String mpiMimeType = null;
    public String typeCheckerMessage;
    private TypeCheckerState typeCheckerState = TypeCheckerState.UNCHECKED;
    public int matchesInCache;
    public int matchesRemote;
    public int matchesLocalFileSystem;
    public boolean fileNotFound;
    public boolean isInfoLink = false;
    private boolean singletonMetadataNode = false;
    protected boolean nodeNeedsSaveToDisk;
    protected String nodeText, lastNodeText = NODE_LOADING_TEXT;
    //    private boolean nodeTextChanged = false;
    private final URI nodeUri;
    private boolean containerNode = false;
    public ArbilField resourceUrlField;
    private CmdiComponentLinkReader cmdiComponentLinkReader = null;
    private ImageIcon icon;
    private boolean nodeEnabled;
    public boolean hasSchemaError = false;
    // merge to one array of domid url ArbilDataNode
    protected List<String[]> childLinks = new ArrayList<String[]>(); // each element in this array is an array [linkPath, linkId]. When the link is from an imdi the id will be the node id, when from get links or list direcotry id will be null    
    private int isLoadingCount = 0;
    final private Object loadingCountLock = new Object();
    @Deprecated
    public boolean lockedByLoadingThread = false;
    //    private boolean isFavourite;
    public String archiveHandle = null;
    public String archiveUri = null;
    public boolean hasDomIdAttribute = false; // used to requre a save (that will remove the dom ids) if a node has any residual dom id attributes
    //public Vector<String[]> addQueue;
    public boolean scrollToRequested = false;
    //    public Vector<ImdiTreeObject> mergeQueue;
    //    public boolean jumpToRequested = false; // dubious about this being here but it seems to fit here best
    private ArbilDataNode domParentNode = null; // the parent imdi containing the dom, only set for imdi child nodes
    //public String xmlNodeId = null; // only set for imdi child nodes and is the xml node id relating to this imdi tree object
    public File thumbnailFile = null;
    private boolean resourceNode = false;
    private final Object domLockObjectPrivate = new Object();
    private final static String NODE_LOADING_TEXT = widgets.getString("LOADING NODE...");
    public static final String EMPTY_NODE_STRING_VALUE = "                      ";
    private MetadataFormat.FileType formatType = MetadataFormat.FileType.UNKNOWN;

    protected ArbilDataNode(ArbilDataNodeService dataNodeService, URI localUri, MetadataFormat.FileType formatType) {
        super();
        //        addQueue = new Vector<String[]>();
        this.dataNodeService = dataNodeService;
        this.formatType = formatType;
        nodeUri = localUri;
        if (nodeUri != null) {
            metadataUtils = ArbilDataNode.getMetadataUtils(nodeUri.toString());
        }
        initNodeVariables();
    }

    // set the node text only if it is null
    public void setNodeText(String localNodeText) {
        if (nodeText == null) {
            nodeText = localNodeText;
        }
    }
    // static methods for testing imdi file and object types

    static public boolean isArbilDataNode(Object unknownObj) {
        if (unknownObj == null) {
            return false;
        }
        return (unknownObj instanceof ArbilDataNode);
    }

    static public boolean isStringLocal(String urlString) {
        return (urlString.startsWith("file:")); // this has been changed from !http so that only file: will be considered local (file objects will be created), the previous use of http was done be cause the string was not as normalised as it is now, when using !http it would in the case of ftp will fail and cause a null pointer in pathIsInFavourites
    }

    static public boolean isUriLocal(URI uri) {
        return (uri.getScheme().equalsIgnoreCase("file"));
    }

    static public boolean isPathHistoryFile(String urlString) {
        return MetadataFormat.isPathMetadata(urlString.replaceAll("mdi.[0-9]*$", "mdi"));
    }

    static public MetadataUtils getMetadataUtils(String urlString) {
        if (MetadataFormat.isPathCmdi(urlString)) {
            return new CmdiUtils();
        } else if (MetadataFormat.isPathImdi(urlString)) {
            return new ImdiUtils();
        }
        return null;
    }

    public MetadataUtils getMetadataUtils() {
        return metadataUtils;
    }

    // end static methods for testing imdi file and object types
    public boolean getNeedsSaveToDisk(boolean onlyOfSubNode) {
        // when the dom parent node is saved all the sub nodes are also saved so we need to clear this flag
        if (nodeNeedsSaveToDisk && !this.getParentDomNode().nodeNeedsSaveToDisk) {
            nodeNeedsSaveToDisk = false;
        }
        if (onlyOfSubNode) {
            return nodeNeedsSaveToDisk;
        } else {
            return this.getParentDomNode().nodeNeedsSaveToDisk;
        }
    }

    protected boolean isNeedsSaveToDisk() {
        return nodeNeedsSaveToDisk;
    }

    public boolean hasChangedFields() {
        for (ArbilField[] currentFieldArray : this.fieldHashtable.values()) {
            for (ArbilField currentField : currentFieldArray) {
                if (currentField.fieldNeedsSaveToDisk()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Searches for pending changes in this node or one of its subnodes
     *
     * @return Whether this node or any of its descendants has changed fields
     * @see hasChangedFields()
     */
    public boolean hasChangedFieldsInSubtree() {
        if (hasChangedFields()) {
            return true;
        } else {
            for (ArbilDataNode child : getChildArray()) {
                if (child.hasChangedFieldsInSubtree()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setDataNodeNeedsSaveToDisk(ArbilField originatingField, boolean updateUI) {
        dataNodeService.setDataNodeNeedsSaveToDisk(this, originatingField, updateUI);
    }

    public String getAnyMimeType() {
        if (mpiMimeType == null && hasResource()) { // use the format from the imdi file if the type checker failed eg if the file is on the server
            ArbilField[] formatField = getFieldArray("Format");
            if (formatField != null && formatField.length > 0) {
                return formatField[0].getFieldValue();
            }
        }
        return mpiMimeType;
    }

    public void setMimeType(String[] typeCheckerMessageArray) {
        mpiMimeType = typeCheckerMessageArray[0];
        typeCheckerMessage = typeCheckerMessageArray[1];
        if (!isMetaDataNode() && isLocal() && mpiMimeType != null) {
            // add the mime type for loose files
            ArbilField mimeTypeField = new ArbilField(fieldHashtable.size(), this, "Format", this.mpiMimeType, 0, false, null, null);
            //            mimeTypeField.fieldID = "x" + fieldHashtable.size();
            addField(mimeTypeField);
        }
    }

    private String getNodeTypeNameFromUriFragment(String nodeFragmentName) {
        if (nodeFragmentName == null) {
            return null;
        }
        nodeFragmentName = nodeFragmentName.substring(nodeFragmentName.lastIndexOf(".") + 1);
        nodeFragmentName = nodeFragmentName.replaceAll("\\(\\d+\\)", "");
        return nodeFragmentName;
    }

    protected final void initNodeVariables() {
        // loop any indichildnodes and init
        if (childArray != null) {
            for (ArbilDataNode currentNode : childArray) {
                if (currentNode.isChildNode()) {
                    currentNode.initNodeVariables();
                }
            }
        }
        //        if (currentTemplate == null) {
        //            // this will be overwritten when the imdi file is read, provided that a template is specified in the imdi file
        //            if (isPathCmdi(nodeUri.getNodePath())) {
        //                // this must be loaded with the name space uri
        //                //   currentTemplate = ArbilTemplateManager.getSingleInstance().getCmdiTemplate();
        //            } else {
        //                currentTemplate = ArbilTemplateManager.getSingleInstance().getCurrentTemplate();
        //            }
        //        }
        fieldHashtable = new HashMap<String, ArbilField[]>();
        setLoadingState(LoadingState.UNLOADED);
        hashString = null;
        //mpiMimeType = null;
        matchesInCache = 0;
        matchesRemote = 0;
        matchesLocalFileSystem = 0;
        fileNotFound = false;
        nodeNeedsSaveToDisk = false;
        //    nodeText = null;
        //    urlString = null;
        //        resourceUrlField = null;        
        icon = null;
        nodeEnabled = true;
        singletonMetadataNode = false;
        containerNode = false;
        //        isLoadingCount = true;
        if (nodeUri != null) {
            if (!isMetaDataNode() && isLocal()) {
                File fileObject = getFile();
                if (fileObject != null) {
                    this.nodeText = fileObject.getName();
                    // TODO: check this on a windows box with a network drive and linux with symlinks
                    //                    this.isDirectory = !fileObject.isFile();
                    //                    logger.debug("isFile" + fileObject.isFile());
                    //                    logger.debug("isDirectory" + fileObject.isDirectory());
                    //                    logger.debug("getAbsolutePath" + fileObject.getAbsolutePath());
                }
            }
            if (!isMetaDataNode() && nodeText == null) {
                nodeText = this.getUrlString();
            }
        }
    }

    public void reloadNodeShallowly() {
        dataNodeService.reloadNodeShallowly(this);
    }

    public void reloadNode() {
        dataNodeService.reloadNode(this);
    }

    public void loadArbilDom() {
        dataNodeService.loadArbilDom(this);
    }

    /**
     * Sets requested loading state to {@link LoadingState#LOADED} and performs
     * a {@link #loadArbilDom()
     */
    public void loadFullArbilDom() {
        dataNodeService.loadFullArbilDom(this);
    }

    /**
     * Count the next level of child nodes. (non recursive)
     *
     * @return An integer of the next level of child nodes including corpus
     * links and Arbil child nodes.
     */
    public int getChildCount() {
        //        logger.debug("getChildCount: " + childLinks.size() + childrenHashtable.size() + " : " + this.getUrlString());
        return childArray.length;
    }

    /**
     * Calls getAllChildren(Vector<ArbilDataNode> allChildren) and returns the
     * result as an array
     *
     * @return an array of all the child nodes
     */
    public ArbilDataNode[] getAllChildren() {
        List<ArbilDataNode> allChildren = new ArrayList<ArbilDataNode>();
        getAllChildren(allChildren);
        return allChildren.toArray(new ArbilDataNode[]{});
    }

    /**
     * Used to get all the Arbil child nodes (all levels) of a session or all
     * the nodes contained in a corpus (one level only).
     *
     * @param An empty vector, to which all the child nodes will be added.
     */
    public void getAllChildren(List<ArbilDataNode> allChildren) {
        logger.debug("getAllChildren: {}", this.getUrlString());
        if (this.isSession() || this.isCatalogue() || this.isChildNode() || this.isCmdiMetaDataNode()) {
            for (ArbilDataNode currentChild : childArray) {
                if (currentChild != this) { // Should not happen but prevent looping by self reference
                    currentChild.getAllChildren(allChildren);
                    allChildren.add(currentChild);
                }
            }
        }
    }

    /**
     * Gets an array of the children of this node.
     *
     * @return An array of the next level child nodes.
     */
    public ArbilDataNode[] getChildArray() {
        return childArray;
    }

    @Override
    public List<? extends PluginDataNode> getChildList() {
        return Arrays.asList(getChildArray());
    }

    /**
     * Gets the second level child nodes from the fist level child node matching
     * the child type string. Used to populate the child nodes in the table
     * cell.
     *
     * @param childType The name of the first level child to query.
     * @return An object array of all second level child nodes in the first
     * level node.
     */
    public ArbilDataNode[] getChildNodesArray(String childType) {
        for (ArbilDataNode currentNode : childArray) {
            if (currentNode.toString().equals(childType)) {
                return currentNode.getChildArray();
            }
        }
        return null;
    }

    /**
     * Recursively checks all subnodes and their URI fragments, tries to find a
     * match to the provided path
     *
     * @param path Path to match
     * @return Matching child node, if found. Otherwise null
     */
    public ArbilDataNode getChildByPath(String path) {
        if (childArray != null && childArray.length > 0) {
            for (ArbilDataNode child : childArray) {
                if (child.getURI() != null && path.equals(child.getURIFragment())) {
                    return child;
                } else {
                    ArbilDataNode childMatch = child.getChildByPath(path);
                    if (childMatch != null) {
                        return childMatch;
                    }
                }
            }
        }
        return null;
    }

    public String getNodePath() {
        final String fragment = nodeUri.getFragment();
        if (fragment == null) {
            return null;
        } else {
            final String startPath = MetadataFormat.getMetadataStartPath(nodeUri.getPath());
            if (fragment.startsWith(startPath) && fragment.length() > startPath.length()) {
                return fragment.substring(startPath.length() + 1);
            } else {
                return fragment;
            }
        }
    }

    public ArbilTemplate getNodeTemplate() {
        if (nodeTemplate != null && !this.isCorpus()) {
            return nodeTemplate;
        } else if (this.isChildNode()) {
            return this.getParentDomNode().getNodeTemplate();
        } else {
            return ArbilTemplateManager.getSingleInstance().getDefaultTemplate();
        }
    }

    /**
     * create a subdirectory based on the file name of the node if that fails
     * then the current directory will be returned
     *
     * @return
     */
    public File getSubDirectory() {
        String currentFileName = this.getFile().getParent();
        if (MetadataFormat.isPathImdi(nodeUri.getPath()) || MetadataFormat.isPathCmdi(nodeUri.getPath())) {
            currentFileName = currentFileName + File.separatorChar + this.getFile().getName().substring(0, this.getFile().getName().length() - 5);
            File destinationDir = new File(currentFileName);
            if (!destinationDir.exists()) {
                if (!destinationDir.mkdir()) {
                    logger.error("Could not create directory {}", destinationDir.getAbsolutePath());
                }
            }
            return destinationDir;
        }
        return new File(this.getFile().getParent());
    }

    public boolean containsFieldValue(String fieldName, String searchValue) {
        boolean findResult = false;
        final String fieldNameLowerCase = fieldName.toLowerCase();
        // when we look for a feild name the check should be case insensitive
        for (String keyString : this.fieldHashtable.keySet()) {
            if (fieldNameLowerCase.equals(keyString.toLowerCase())) {
                ArbilField[] currentFieldArray = this.fieldHashtable.get(keyString);
                if (currentFieldArray != null) {
                    for (ArbilField currentField : currentFieldArray) {
                        logger.trace("containsFieldValue: {}:{}", currentField.getFieldValue(), searchValue);
                        if (currentField.getFieldValue().toLowerCase().contains(searchValue.toLowerCase())) {
                            return true;
                        }
                    }
                }
            }
        }
        logger.trace("result: {}:{}", findResult, this);
        return findResult;
    }

    public boolean containsFieldValue(String searchValue) {
        boolean findResult = false;
        for (ArbilField[] currentFieldArray : (Collection<ArbilField[]>) this.fieldHashtable.values()) {
            for (ArbilField currentField : currentFieldArray) {
                logger.trace("containsFieldValue: {}:{}", currentField.getFieldValue(), searchValue);
                if (currentField.getFieldValue().toLowerCase().contains(searchValue.toLowerCase())) {
                    return true;
                }
            }
        }
        logger.trace("result: {}:{}", findResult, this);
        return findResult;
    }

    // this is used to disable the node in the tree gui
    public boolean getNodeEnabled() {
        //       ---      TODO: here we could look through all the fields in this node against the current filed view, if node are showing then return false
        //       ---      when the global field view is changed then set all nodeEnabled blaaaa
        return nodeEnabled;
    }

    /**
     * Tests if this node has child nodes even if they are not yet loaded.
     *
     * @return boolean
     */
    public boolean canHaveChildren() {
        return childArray.length > 0;
    }

    // this is used to delete an IMDI node from a corpus branch
    public void deleteCorpusLink(ArbilDataNode[] targetImdiNodes) {
        dataNodeService.deleteCorpusLink(this, targetImdiNodes);
    }

    public boolean hasCatalogue() {
        for (ArbilDataNode childNode : childArray) {
            //            String currentChildPath = currentLinkPair[0];
            //            ImdiTreeObject childNode = ImdiLoader.getSingleInstance().getImdiObject(null, currentChildPath);
            //childNode.waitTillLoaded(); // if the child nodes have not been loaded this will fail so we must wait here
            if (childNode.isCatalogue()) {
                return true;
            }
        }
        return false;
    }

    public boolean addCorpusLink(ArbilDataNode targetImdiNode) {
        return dataNodeService.addCorpusLink(this, targetImdiNode);
    }

    public void pasteIntoNode() {
        dataNodeService.pasteIntoNode(this);
    }

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history. the caller is
     * responsible for reloading the node if that is required
     */
    public void saveChangesToCache(boolean updateUI) {
        dataNodeService.saveChangesToCache(this);
    }

    public void addField(ArbilField fieldToAdd) {
        dataNodeService.addField(this, fieldToAdd);
    }

    /**
     * Gets the fields in this node, this does not include any imdi child
     * fields. To get all fields relevant the imdi file use "getAllFields()"
     * which includes imdi child fields.
     *
     * @return A hashtable of the fields
     */
    public Map<String, ArbilField[]> getFields() {
        // store the Hastable for next call
        // if hashtable is null then load from imdi
        return fieldHashtable;
    }

    /**
     * Gets all the fields for this node, grouped by their display name in a
     * format acceptable to JaxB
     *
     * @return A List of FieldArrays, each element in the list represents a
     * group based on the display name for each field
     */
    @Override
    public List<FieldGroup> getFieldGroups() {
        ArrayList<FieldGroup> fieldArrays = new ArrayList<FieldGroup>();
        for (Map.Entry<String, ArbilField[]> currentEntry : getFields().entrySet()) {
            final List<DataField> fieldList = Arrays.<DataField>asList(currentEntry.getValue());
            fieldArrays.add(new FieldGroup(currentEntry.getKey(), fieldList));
        }
        return fieldArrays;
    }

    /**
     * Returns the fields of this data note sorted by field order
     *
     * @return
     */
    public List<ArbilField[]> getFieldsSorted() {
        List<ArbilField[]> fieldArrays = new ArrayList<ArbilField[]>(getFields().values());
        Collections.sort(fieldArrays, new ArrayComparator<ArbilField>(new ArbilFieldComparator(), 0));
        return fieldArrays;
    }

    /**
     * Compares this node to another based on its type and string value.
     *
     * @return The string comparison result.
     */
    public int compareTo(Object o) throws ClassCastException {
        if (isFavorite()) {
            return favouriteSorter.compare(this, o);
        } else {
            return dataNodeSorter.compare(this, o);
        }
    }

    public synchronized void notifyLoaded() {
        getParentDomLockObject().notifyAll();
    }

    /**
     * If isLoading(), i.e. loading state > 0, waits for loading state to become
     * 0
     *
     * @return
     */
    public boolean waitTillLoaded() {
        if (this != getParentDomNode()) { // isloading does this parent check pretty much already
            return getParentDomNode().waitTillLoaded();
        } else {
            synchronized (getParentDomLockObject()) {
                while (isLoading()) {
                    try {
                        logger.debug("wait for loading: {}", nodeUri);
                        getParentDomLockObject().wait(1000);
                    } catch (InterruptedException ex) {
                        logger.debug("Interrupted while waiting for node to be loaded", ex);
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                return true;
            }
        }
    }

    public void updateLoadingState(int countChange) {
        if (this != getParentDomNode()) {
            getParentDomNode().updateLoadingState(countChange);
        } else {
            final boolean wasLoading = isLoading();
            synchronized (loadingCountLock) {
                isLoadingCount += countChange;
            }
//            logger.debug("isLoadingCount: " + isLoadingCount);
            if (wasLoading != isLoading()) {
                //                    this.notifyAll();
                clearChildIcons();
                clearIcon();
            }
        }
    }

    public boolean isLoading() {
        if (this != getParentDomNode()) {
            return getParentDomNode().isLoading();
        } else {
            synchronized (loadingCountLock) {
                return getParentDomNode().isLoadingCount > 0;
            }
        }
    }

    @Override
    public String getLabel() {
        return this.toString();
    }

    @Override
    public String toString() {
        if (lastNodeText != null) {
            return lastNodeText;
        } else {
            return "unknown";
        }
    }

    public boolean isNodeTextDetermined() {
        return lastNodeText != null && !lastNodeText.equals(NODE_LOADING_TEXT) && !lastNodeText.equals(EMPTY_NODE_STRING_VALUE);
    }

    public String refreshStringValue() {
        if (isLoading()) {
            //            if (lastNodeText.length() > 0) {
            //                return lastNodeText;
            //            } else {asdasdasd
            ////                if (nodeText != null && nodeText.length() > 0) {
            return lastNodeText;
            //            }
        } else if (lastNodeText.equals(NODE_LOADING_TEXT) && isDataPartiallyLoaded()) {
            lastNodeText = EMPTY_NODE_STRING_VALUE;
        }
        //        if (commonFieldPathString != null && commonFieldPathString.length() > 0) {
        //            // todo: use the commonFieldPathString as the node name if not display preference is set or the ones that are set have no value
        //            nodeText = commonFieldPathString;
        //        }
        boolean foundPreferredNameField = false;
        boolean preferredNameFieldExists = false;

        //final String nodePath = getNodePath();
        getLabelString:
        for (String currentPreferredName : this.getNodeTemplate().getPreferredNameFields()) {
            for (ArbilField[] currentFieldArray : fieldHashtable.values().toArray(new ArbilField[][]{})) {

                // TODO: Field of child nodes should not give name to node. Line below will acomplish this but also ignores preferred names on
                // nodes that get ALL their fields from child elements in the XML (in case of 1:1 truncation)
                // if (!currentFieldArray[0].getTranslateFieldName().contains(".")) { // Field of child nodes should not give name to node
                if (currentFieldArray[0].getFullXmlPath().replaceAll("\\(\\d+\\)", "").equals(currentPreferredName)) {
                    preferredNameFieldExists = true;
                    for (ArbilField currentField : currentFieldArray) {
                        if (currentField != null) {
                            if (currentField.toString().trim().length() > 0) {
                                nodeText = currentField.toString();
                                foundPreferredNameField = true;
                                break getLabelString;
                            }
                        }
                    }
                }
            }
            ArbilField[] currentFieldArray = getFieldArray(currentPreferredName);
            if (currentFieldArray != null) {
                for (ArbilField currentField : currentFieldArray) {
                    if (currentField != null) {
                        if (currentField.toString().trim().length() > 0) {
                            nodeText = currentField.toString();
                            //                            logger.debug("nodeText: " + nodeText);
                            foundPreferredNameField = true;
                            break getLabelString;
                        }
                    }
                }
            }
        }
        if (!foundPreferredNameField && this.isCmdiMetaDataNode()/* && isCmdiMetaDataNode() *//* && fieldHashtable.size() > 0 && domParentImdi == this */) {
            String unamedText;
            String nodeFragmentName = this.getURIFragment();
            if (nodeFragmentName != null) {
                nodeFragmentName = getNodeTypeNameFromUriFragment(nodeFragmentName);
                unamedText = nodeFragmentName;
            } else if (this.nodeTemplate != null) {
                //            if (this.getNodeTemplate().preferredNameFields.length == 0) {
                //                nodeText = "no field specified to name this node (" + this.nodeTemplate.getTemplateName() + ")";
                //            } else {
                unamedText = this.nodeTemplate.getTemplateName();
            } else {
                unamedText = "";
            }
            if (preferredNameFieldExists) {
                nodeText = unamedText + " (unnamed)";
            } else {
                nodeText = unamedText;
            }
        }
        //        if (!foundPreferredNameField && isCmdiMetaDataNode() && domParentImdi == this && fieldHashtable.size() > 0) {
        //            // only if no name has been found and only for cmdi nodes and only when this is the dom parent node
        //            nodeText = fieldHashtable.elements().nextElement()[0].getFullXmlPath().split("\\.")[3];
        //        }
        if (hasResource()) {
            URI resourceUri = getFullResourceURI();
            if (resourceUri != null) {
                String resourcePathString = resourceUri.toString();
                int lastIndex = resourcePathString.lastIndexOf("/");
                //                if (lastIndex)
                resourcePathString = resourcePathString.substring(lastIndex + 1);
                try {
                    resourcePathString = URLDecoder.decode(resourcePathString, "UTF-8");
                } catch (UnsupportedEncodingException encodingException) {
                    // UTF-8 not supported, should never happen
                    throw new RuntimeException(encodingException);
                }
                nodeText = resourcePathString;
            }
        }
        if (isInfoLink) {
            final Iterator<ArbilField[]> fieldsIterator = fieldHashtable.values().iterator();
            if (fieldsIterator.hasNext()) {
                String infoTitle = fieldsIterator.next()[0].getFieldValue();
                infoTitle = infoTitle.trim();
                if (infoTitle.length() > 0) {
                    nodeText = infoTitle;
                }
            }
        }
        //        nodeTextChanged = lastNodeText.equals(nodeText + nameText);
        if (nodeText != null) {
            if (isMetaDataNode()) {
                File nodeFile = this.getFile();
                if (nodeFile != null && !isHeadRevision()) {
                    nodeText = nodeText + " (rev:" + getHistoryLabelStringForFile(nodeFile) + ")";
                }
            }
            lastNodeText = nodeText;
        }

        if (isContainerNode()) {
            lastNodeText = String.format("%1$s (%2$d)", lastNodeText, getChildCount());
        } else if (isSingletonMetadataNode()) {
            StringBuilder nodeTextSB = new StringBuilder(getNodeTypeNameFromUriFragment(getURIFragment()));
            if (nodeText != null && nodeText.length() > 0) {
                nodeTextSB.append(" (").append(nodeText).append(")");
            }
            lastNodeText = nodeTextSB.toString();
        }

        if (lastNodeText.length() == 0) {
            lastNodeText = EMPTY_NODE_STRING_VALUE;
        }
        return lastNodeText;// + "-" + clearIconCounterGlobal + "-" + clearIconCounter;
        //            }
    }

    /**
     * Tests if there is file associated with this node and if it is an
     * archivable type. The file could be either a resource file (getResource)
     * or a loose file (getUrlString).
     *
     * @return boolean
     */
    public boolean isArchivableFile() {
        return mpiMimeType != null;
    }

    /**
     * Tests if a resource file (local or remote) is associated with this node.
     *
     * @return boolean
     */
    public boolean hasResource() {
        return resourceUrlField != null && !resourceUrlField.getFieldValue().isEmpty();
    }

    public boolean canHaveResource() {
        if (resourceUrlField != null) {
            return true;
        } else if (isCmdiMetaDataNode()) {
            final ArbilTemplate template = getNodeTemplate();
            if (template instanceof CmdiTemplate) {
                return ((CmdiTemplate) template).pathCanHaveResource(nodeUri.getFragment());
            }
        }
        return false;
    }

    /**
     * Inserts/sets resource location. Behavior will depend on node type
     *
     * @param location Location to insert/set
     */
    public void insertResourceLocation(URI location) throws ArbilMetadataException {
        dataNodeService.insertResourceLocation(this, location);
    }

    /**
     * Tests if a local resource file is associated with this node.
     *
     * @return boolean
     */
    public boolean hasLocalResource() {
        if (!hasResource()) {
            return false;
        }
        if (resourceUrlField.getFieldValue().toLowerCase().startsWith("http")) {
            return false;
        }
        if (!this.isLocal()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean resourceFileNotFound() {
        if (hasLocalResource()) {
            if (resourceUrlField.getFieldValue().length() == 0) {
                return true;
            }
            try {
                return !(new File(this.getFullResourceURI())).exists();
            } catch (Exception e) {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean hasHistory() {
        if (!this.isLocal()) {
            // only local files can have a history
            return false;
        }
        return !this.isChildNode() && new File(this.getFile().getAbsolutePath() + ".0").exists();
    }

    private boolean isHeadRevision() {
        return !(new File(this.getFile().getAbsolutePath() + ".x").exists());
    }

    private String getHistoryLabelStringForFile(File historyFile) {
        Date mtime = new Date(historyFile.lastModified());
        String mTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mtime);
        return mTimeString;
    }

    public String[][] getHistoryList() {
        Vector<String[]> historyVector = new Vector<String[]>();
        int versionCounter = 0;
        File currentHistoryFile;
        //        historyVector.add(new String[]{"Current", ""});
        if (!isHeadRevision()) {
            historyVector.add(new String[]{widgets.getString("HISTORY_LAST SAVE"), ".x"});
        }
        do {
            currentHistoryFile = new File(this.getFile().getAbsolutePath() + "." + versionCounter);
            if (currentHistoryFile.exists()) {
                String mTimeString = getHistoryLabelStringForFile(currentHistoryFile);
                historyVector.add(new String[]{mTimeString, "." + versionCounter});
            }
            versionCounter++;
        } while (currentHistoryFile.exists());
        return historyVector.toArray(new String[][]{{}});
    }

    public boolean resurrectHistory(String historyVersion) {
        return dataNodeService.resurrectHistory(this, historyVersion);
    }

    /*
     * Increment the history file so that a new current file can be saved without overwritting the old
     * This will also have the effect of deleting the file until the dom is saved thereby recreating the file that was bumped into history
     */
    public void bumpHistory() throws IOException {
        dataNodeService.bumpHistory(this.getFile());
    }

    /**
     * Returns the archive handle to a resource file if it exists.
     *
     * @return The handle for the remote resource if it exists.
     */
    public String getResourceArchiveHandle() {
        return resourceUrlField.getArchiveHandle();
    }

    /**
     * Resolves the full path to a resource file if it exists.
     *
     * @return The path to remote resource if it exists.
     */
    public URI getFullResourceURI() {
        String targetUriString = resourceUrlField.getFieldValue();
        String[] uriParts = targetUriString.split(":/", 2);
        try {
            URI targetUri;
            if (uriParts.length > 1) {
                // todo: this will not allow urls that have square brackets in them due to yet another bug in the java URI class
                //                String bracketEncodedPath = uriParts[1].replaceAll("\\[", "%5B");
                //                bracketEncodedPath = bracketEncodedPath.replaceAll("\\]", "%5D");
                String bracketEncodedPath = uriParts[1];
		//org.apache.commons.httpclient.URI test = null;

                //if (bracketEncodedPath.c)
                targetUri = new URI(uriParts[0], "/" + bracketEncodedPath, null);
            } else {
                targetUri = new URI(null, targetUriString, null);
            }
            //            logger.debug("nodeUri: " + nodeUri);
            URI resourceUri = nodeUri.resolve(targetUri);
            //            logger.debug("targetUriString: " + targetUriString);
            //            logger.debug("targetUri: " + targetUri);
            //            logger.debug("resourceUri: " + resourceUri);
            if (!targetUri.equals(resourceUri)) {
                // maintain the UNC path
                boolean isUncPath = nodeUri.toString().toLowerCase().startsWith("file:////");
                if (isUncPath) {
                    resourceUri = URI.create("file:////" + resourceUri.toString().substring("file:/".length()));
                }
            }
            return resourceUri;
        } catch (Exception urise) {
            logger.error("URI syntax exception creating target URI from {}", targetUriString);
            logger.debug("URISyntaxException: {}", urise);
            return null;
        }
    }

    /**
     * Gets the ULR string provided when the node was created.
     *
     * @return a URL string of the IMDI
     */
    public String getUrlString() {
        // TODO: update the uses of this to use the uri not a string
        return nodeUri.toString();
    }

    public Object getParentDomLockObject() {
        return getParentDomNode().domLockObjectPrivate;
    }

    /**
     * Gets the ArbilDataNode parent of an imdi child node. The returned node
     * will be able to reload/save the dom for this node. Only relevant for imdi
     * child nodes.
     *
     * @return ArbilDataNode
     */
    public synchronized ArbilDataNode getParentDomNode() {
        //        logger.debug("nodeUri: " + nodeUri);
        if (domParentNode == null) {
            if (nodeUri.getFragment() != null) {
                //domParentImdi = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(nodeUri.getScheme(), nodeUri.getUserInfo(), nodeUri.getHost(), nodeUri.getPort(), nodeUri.getNodePath(), nodeUri.getQuery(), null /* fragment removed */));
                // the uri is created via the uri(string) constructor to prevent re-url-encoding the url
                domParentNode = dataNodeService.loadArbilDataNode(null, URI.create(nodeUri.toString().split("#")[0] /* fragment removed */));
                //                    logger.debug("nodeUri: " + nodeUri);
            } else {
                domParentNode = this;
            }
        }
        return domParentNode;
    }

    public synchronized void setParentDomNode(ArbilDataNode domParentNode) {
        this.domParentNode = domParentNode;
    }

    @Override
    public DataNodeType getType() {
        return ArbilDataNodeType.getTypeForNode(this);
    }

    public boolean isDirectory() {
        return MetadataFormat.FileType.DIRECTORY == formatType;
    }

    /**
     *
     * @return whether the node represents a file on disk
     */
    public boolean isFile() {
        return MetadataFormat.FileType.FILE == formatType;
    }

    public boolean isMetaDataNode() {
        if (resourceNode) {
            return false;
        } else {
            return metadataFormat.isMetaDataNode(formatType);
        }
    }

    public boolean isCmdiMetaDataNode() {
        if (MetadataFormat.FileType.CHILD == formatType) {
            return getParentDomNode().isCmdiMetaDataNode();
        } else {
            return MetadataFormat.FileType.CMDI == formatType;
        }
    }

    /**
     * Tests if this node represents an imdi file or if if it represents a child
     * node from an imdi file (created by adding fields with child nodes).
     *
     * @return boolean
     */
    public boolean isChildNode() {
        return MetadataFormat.FileType.CHILD == formatType;
    }

    public boolean isSession() {
        // test if this node is a session
        ArbilField[] nameFields = getFieldArray("Name");
        if (nameFields != null) {
            return nameFields[0].xmlPath.equals(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "Name");
        }
        return false;
    }

    /**
     * Tests if this node is a meta node that contains no fields and only child
     * nodes, such as the Languages, Actors, MediaFiles nodes etc..
     *
     * @return boolean
     */
    public boolean isEmptyMetaNode() {
        return this.getFields().isEmpty();
    }

    public boolean isCatalogue() {
        // test if this node is a catalogue
        ArbilField[] nameFields = getFieldArray("Name");
        if (nameFields != null) {
            return nameFields[0].xmlPath.equals(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Catalogue" + MetadataReader.imdiPathSeparator + "Name");
        }
        return false;
    }

    public boolean isCorpus() {
        if (isCmdiMetaDataNode()) {
            return false;
        }
        // test if this node is a corpus
        ArbilField[] nameFields = getFieldArray("Name");
        if (nameFields != null) {
            return nameFields[0].xmlPath.equals(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Corpus" + MetadataReader.imdiPathSeparator + "Name");
        }
        return false;
    }

    public boolean isLocal() {
        if (nodeUri != null) {
            return ArbilDataNode.isUriLocal(nodeUri);
        } else {
            return false;
        }
    }

    public boolean isEditable() {
        return dataNodeService.isEditable(this);
    }

    /**
     *
     * @return The URI that this node represents.
     */
    public URI getURI() {
        return nodeUri;
    }

    public String getURIFragment() {
        return nodeUri.getFragment();
    }

    public File getFile() { //TODO: make throw IllegalArgumentException, URISyntaxException
        try {
            return MetadataFormat.getFile(nodeUri);
        } catch (Exception urise) {
            logger.error(nodeUri.toString(), urise);
            return null;
        }
    }

    public String getParentDirectory() {
        String parentPath = this.getUrlString().substring(0, this.getUrlString().lastIndexOf("/")) + "/"; // this is a url so don't use the path separator
        return parentPath;
    }

    @Override
    public void registerContainer(ArbilDataNodeContainer containerToAdd) {
        // Node is contained by some object so make sure it's fully loaded or at least loading
        if (!isDataLoaded() && !isLoading()) {
            if (containerToAdd.isFullyLoadedNodeRequired()) {
                setRequestedLoadingState(LoadingState.LOADED);
                dataNodeService.reloadNode(this);
            } else {
                // Partial load required
                if (!isDataPartiallyLoaded()) {
                    dataNodeService.reloadNodeShallowly(this);
                }
            }
            //dataNodeLoader.requestReload(getParentDomNode());
        }
        super.registerContainer(containerToAdd);
    }

    /**
     * Clears the icon for all the imdi child nodes of this node. Used when
     * loading a session dom.
     */
    public void clearChildIcons() {
        //        logger.debug("clearChildIconsParent: " + this);
        for (ArbilDataNode currentChild : childArray) {
            //            if (!currentChild.equals(currentChild.getParentDomNode())) {
            //                logger.debug("clearChildIcons: " + currentChild);
            currentChild.clearChildIcons();
            currentChild.clearIcon();
            //            }
        }
    }
    //    public void addJumpToInTreeRequest() {
    //        jumpToRequested = true;
    //    }

    /**
     * Clears the icon calculated in "getIcon()" and notifies any UI containers
     * of this node.
     */
    public void clearIcon() {
        refreshStringValue();
	//        logger.debug("clearIcon: " + this);
        //        logger.debug("containersOfThisNode: " + containersOfThisNode.size());
        //        SwingUtilities.invokeLater(new Runnable() {

        //            public void run() {
        icon = ArbilIcons.getSingleInstance().getIconForNode(ArbilDataNode.this); // to avoid a race condition (where the loading icons remains after load) this is also set here rather than nulling the icon
        //                logger.debug("clearIcon invokeLater" + ImdiTreeObject.this.toString());
        //                logger.debug("containersOfThisNode: " + containersOfThisNode.size());
        // here we need to cause an update in the gui containers so that the new icon can be loaded
        for (ArbilDataNodeContainer currentContainer : containersOfThisNode.toArray(new ArbilDataNodeContainer[]{})) {
            logger.trace("Updating node {} in container {}", this, currentContainer);
            currentContainer.dataNodeIconCleared(this);
        }
        //            }
        //        });
        //        logger.debug("end clearIcon: " + this);
    }

    public synchronized void removeFromAllContainers() {
        // todo: this should also scan all child nodes and also remove them in the same way
        for (ArbilDataNode currentChildNode : this.getAllChildren()) {
            logger.trace("Removing node {} from all containers", this);
            currentChildNode.removeFromAllContainers();
        }
        for (ArbilDataNodeContainer currentContainer : containersOfThisNode.toArray(new ArbilDataNodeContainer[]{})) {
            logger.trace("Removing node {} from container {}", this, currentContainer);
            currentContainer.dataNodeRemoved(this);
        }
    }
    private Boolean isFavorite = null;

    public boolean isFavorite() {
        // Is being cached because comparator checks this every time
        if (isFavorite == null) {
            isFavorite = dataNodeService.isFavorite(this);
        }
        return isFavorite;

        //        return getParentDomNode().isFavourite;
    }

    @Override
    public String getID() {
        // this ID is used by JAXB. It does not need to be the archive handle, but it does need to he unique. Note that the archive handle is not always available.
        return archiveHandle;
    }

    //    public void setFavouriteStatus(boolean favouriteStatus) {
    //        getParentDomNode().isFavourite = favouriteStatus;
    //        clearIcon();
    //    }
    /**
     * If not already done calculates the required icon for this node in its
     * current state. Once calculated the stored icon will be returned. To clear
     * the icon and recalculate it "clearIcon()" should be called.
     *
     * @return The icon for this node.
     */
    public ImageIcon getIcon() {
        if (icon == null) {
            return ArbilIcons.getSingleInstance().loadingIcon;
        }
        return icon;
    }
    private static ArbilNodeSorter dataNodeSorter = new ArbilNodeSorter();
    private static ArbilNodeSorter favouriteSorter = new ArbilFavouritesSorter();

    public MetadataFormat.FileType getFormatType() {
        return formatType;
    }

    public void setFormatType(MetadataFormat.FileType formatType) {
        this.formatType = formatType;
    }

    /**
     * @return the loading state of this node (through its parent)
     */
    public synchronized LoadingState getLoadingState() {
        if (isChildNode()) {
            return getParentDomNode().getLoadingState();
        } else {
            return loadingState;
        }
    }

    /**
     * @param loadingState the loading state to set. This will in any case set
     * {@link #requestedLoadingState} to {@code null}.
     */
    public synchronized void setLoadingState(LoadingState loadingState) {
        this.loadingState = loadingState;
    }

    /**
     * @return the requested loading state. To be used by data node loaders to
     * determine the level of loading. Can be null.
     */
    public synchronized LoadingState getRequestedLoadingState() {
        return requestedLoadingState;
    }

    /**
     * @param requestedLoadingState the loading state to communicate to data
     * node loaders
     */
    public synchronized void setRequestedLoadingState(LoadingState requestedLoadingState) {
        this.requestedLoadingState = requestedLoadingState;
    }

    /**
     * @return whether {@link #getParentDomNode() }'s {@link #getLoadingState()
     * } equals {@link LoadingState#LOADED}
     */
    public synchronized boolean isDataLoaded() {
        return getLoadingState().equals(LoadingState.LOADED);
    }

    /**
     * @return whether {@link #getParentDomNode() }'s {@link #getLoadingState()
     * } does NOT equal {@link LoadingState#UNLOADED} (so might be
     * {@link LoadingState#PARTIAL} or {@link LoadingState#LOADED})
     */
    public synchronized boolean isDataPartiallyLoaded() {
        return !getLoadingState().equals(LoadingState.UNLOADED);
    }

    /**
     * @return Whether a resource URI has been set for this node
     */
    public boolean isResourceSet() {
        return resourceUrlField != null && resourceUrlField.getFieldValue().length() > 0;
    }

    public void invalidateThumbnails() {
        thumbnailFile = null;
        for (ArbilDataNode node : getChildArray()) {
            node.invalidateThumbnails();
        }
    }
//
//    @Override
//    public boolean equals(Object obj) {
//	if (obj instanceof ArbilDataNode && obj != null) {
//	    return nodeUri.equals(((ArbilDataNode) obj).nodeUri);
//	} else {
//	    return super.equals(obj);
//	}
//    }
//
//    @Override
//    public int hashCode() {
//	return nodeUri.hashCode();
//    }

    /**
     * @return Whether node is conflated with metanode because if it is
     * singleton (e.g. Project, Content). Null if this does not apply.
     */
    public boolean isSingletonMetadataNode() {
        return singletonMetadataNode;
    }

    /**
     * @param singletonMetadataNodeName Whether this node is conflated with
     * metanode because it is singleton (e.g. Project, Content)
     */
    public void setSingletonMetadataNode(boolean singletonMetadataNodeName) {
        this.singletonMetadataNode = singletonMetadataNodeName;
    }

    /**
     * Get the value of containerNode
     *
     * @return the value of containerNode
     */
    public boolean isContainerNode() {
        return containerNode;
    }

    /**
     * Set the value of containerNode
     *
     * @param containerNode new value of containerNode
     */
    public void setContainerNode(boolean containerNode) {
        this.containerNode = containerNode;
    }

    /**
     * @return the cmdiComponentLinkReader
     */
    public synchronized CmdiComponentLinkReader getCmdiComponentLinkReader() {
        if (cmdiComponentLinkReader == null) {
            cmdiComponentLinkReader = new CmdiComponentLinkReader();
        }
        return getParentDomNode().cmdiComponentLinkReader;
    }

    /**
     * @return the typeCheckerState
     */
    public TypeCheckerState getTypeCheckerState() {
        return typeCheckerState;
    }

    /**
     * @param typeCheckerState the typeCheckerState to set
     */
    public void setTypeCheckerState(TypeCheckerState typeCheckerState) {
        this.typeCheckerState = typeCheckerState;
    }

    /**
     * @return the childLinks
     */
    protected List<String[]> getChildLinks() {
        return childLinks;
    }

    protected ArbilField[] getFieldArray(String translateFieldName) {
        return fieldHashtable.get(translateFieldName);
    }

    protected void addFieldArray(String translateFieldName, ArbilField[] fieldArray) {
        fieldHashtable.put(translateFieldName, fieldArray);
    }

    public ArbilDataNode getParentNode() {
        return dataNodeService.getParentOfNode(this);
    }

    /**
     *
     * @param resource whether this node should be treated as resource
     * regardless of its URI
     * @see #isMetaDataNode()
     */
    public void setResourceNode(boolean resource) {
        this.resourceNode = resource;
    }
}
