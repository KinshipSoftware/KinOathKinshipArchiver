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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.HandleUtils;
import nl.mpi.arbil.data.ArbilDataNode.LoadingState;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbilcommons.journal.ArbilJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeService {

    private final static Logger logger = LoggerFactory.getLogger(ArbilDataNodeService.class);
    //TODO: Move strings to 'services' resource..
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private final DataNodeLoader dataNodeLoader;
    private final MessageDialogHandler messageDialogHandler;
    private final SessionStorage sessionStorage;
    private final MimeHashQueue mimeHashQueue;
    private final TreeHelper treeHelper;
    private final ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
    private final MetadataReader metadataReader = MetadataReader.getSingleInstance();
    private final MetadataFormat metadataFormat = new MetadataFormat();

    public ArbilDataNodeService(DataNodeLoader dataNodeLoader, MessageDialogHandler messageDialogHandler, SessionStorage sessionStorage, MimeHashQueue mimeHashQueue, TreeHelper treeHelper) {
	this.messageDialogHandler = messageDialogHandler;
	this.sessionStorage = sessionStorage;
	this.mimeHashQueue = mimeHashQueue;
	this.treeHelper = treeHelper;
	this.dataNodeLoader = dataNodeLoader;
    }

    public boolean isEditable(ArbilDataNode dataNode) {
	if (dataNode.isLocal()) {
	    return (sessionStorage.pathIsInsideCache(dataNode.getFile()))
		    || sessionStorage.pathIsInFavourites(dataNode.getFile());
	} else {
	    return false;

	}
    }

    public boolean isFavorite(ArbilDataNode dataNode) {
	if (!dataNode.isLocal()) {
	    // only local files can be favourites
	    return false;
	}
	return sessionStorage.pathIsInFavourites(dataNode.getFile());
    }

    public void pasteIntoNode(ArbilDataNode dataNode) {
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable transfer = clipboard.getContents(null);
	try {
	    String clipBoardString = "";
	    Object clipBoardData = transfer.getTransferData(DataFlavor.stringFlavor);
	    if (clipBoardData != null) {//TODO: check that this is not null first but let it pass on null so that the no data to paste messages get sent to the user
		clipBoardString = clipBoardData.toString();
		logger.debug("clipBoardString: {}", clipBoardString);

		String[] elements;
		if (clipBoardString.contains("\n")) {
		    elements = clipBoardString.split("\n");
		} else {
		    elements = new String[]{clipBoardString};
		}
		for (String element : elements) {
		}
		for (ArbilDataNode clipboardNode : pasteIntoNode(dataNode, elements)) {
		    new MetadataBuilder().requestAddNode(dataNode, MessageFormat.format(widgets.getString("COPY OF {0}"), clipboardNode), clipboardNode);
		}
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    private Collection<ArbilDataNode> pasteIntoNode(ArbilDataNode dataNode, String[] clipBoardStrings) {
	try {
	    ArrayList<ArbilDataNode> nodesToAdd = new ArrayList<ArbilDataNode>();
	    boolean ignoreSaveChanges = false;
	    for (String clipBoardString : clipBoardStrings) {
		if (dataNode.isCorpus()) {
		    if (MetadataFormat.isPathMetadata(clipBoardString) || MetadataFormat.isStringChildNode(clipBoardString)) {
			ArbilDataNode clipboardNode = dataNodeLoader.getArbilDataNode(null, conformStringToUrl(clipBoardString));
			if (sessionStorage.pathIsInsideCache(clipboardNode.getFile())) {
			    if (!(MetadataFormat.isStringChildNode(clipBoardString) && (!dataNode.isSession() && !dataNode.isChildNode()))) {
				if (dataNode.getFile().exists()) {
				    if (!ignoreSaveChanges && clipboardNode.getNeedsSaveToDisk(false)) {
					if (JOptionPane.CANCEL_OPTION == messageDialogHandler.showDialogBox(
						widgets.getString("SOME OF THE NODES TO BE COPIED CONTAIN UNSAVED CHANGES"), widgets.getString("COPYING WITH UNSAVED CHANGES"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
					    return new ArrayList<ArbilDataNode>(0);
					} else {
					    ignoreSaveChanges = true;
					}
				    }

				    // this must use merge like favoirite to prevent instances end endless loops in corpus branches
				    nodesToAdd.add(clipboardNode);
				} else {
				    messageDialogHandler.addMessageDialogToQueue(widgets.getString("THE TARGET NODE'S FILE DOES NOT EXIST"), null);
				}
			    } else {
				messageDialogHandler.addMessageDialogToQueue(widgets.getString("CANNOT PASTE SESSION SUBNODES INTO A CORPUS"), null);
			    }
			} else {
			    messageDialogHandler.addMessageDialogToQueue(widgets.getString("THE TARGET FILE IS NOT IN THE CACHE"), null);
			}
		    } else {
			messageDialogHandler.addMessageDialogToQueue(widgets.getString("PASTED STRING IS NOT AND IMDI FILE"), null);
		    }
		} else if (dataNode.isMetaDataNode() || dataNode.isSession()) {
		    // Get source node
		    ArbilDataNode templateDataNode = dataNodeLoader.getArbilDataNode(null, conformStringToUrl(clipBoardString));
		    // Check if it can be contained by destination node
		    if (metadataReader.nodeCanExistInNode(dataNode, templateDataNode)) {
			// Add source to destination
			new MetadataBuilder().requestAddNode(dataNode, templateDataNode.toString(), templateDataNode);
		    } else {
			// Invalid copy/paste...
			messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(widgets.getString("CANNOT COPY {0} TO {1}"), templateDataNode.toString(), dataNode.toString()), "Cannot copy");
		    }
		} else { // Not corpus, session or metadata
		    messageDialogHandler.addMessageDialogToQueue(widgets.getString("NODES OF THIS TYPE CANNOT BE PASTED INTO AT THIS STAGE"), null);
		}
	    }
	    return nodesToAdd;
	} catch (URISyntaxException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	    return null;
	}
    }

    public boolean addCorpusLink(ArbilDataNode dataNode, ArbilDataNode targetNode) {
	boolean linkAlreadyExists = false;
	if (targetNode.isCatalogue()) {
	    if (dataNode.hasCatalogue()) {
		//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Only one catalogue can be added", null);
		// prevent adding a second catalogue file
		return false;
	    }
	}
	for (String[] currentLinkPair : dataNode.getChildLinks()) {
	    final String currentChildPath = currentLinkPair[0];
	    if (!targetNode.waitTillLoaded()) { // we must wait here before we can tell if it is a catalogue or not
		messageDialogHandler.addMessageDialogToQueue(widgets.getString("ERROR ADDING NODE, COULD NOT WAIT FOR FILE TO LOAD"), widgets.getString("LOADING ERROR"));
		return false;
	    }
	    if (currentChildPath.equals(targetNode.getUrlString())) {
		linkAlreadyExists = true;
	    }
	}
	if (targetNode.getUrlString().equals(dataNode.getUrlString())) {
	    messageDialogHandler.addMessageDialogToQueue(widgets.getString("CANNOT LINK OR MOVE A NODE INTO ITSELF"), null);
	    return false;
	}
	if (linkAlreadyExists) {
	    messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(widgets.getString("{0} ALREADY EXISTS IN {1} AND WILL NOT BE ADDED AGAIN"), targetNode, dataNode), null);
	    return false;
	} else {
	    // if link is not already there
	    // if needs saving then save now while you can
	    // TODO: it would be nice to warn the user about this, but its a corpus node so maybe it is not important
	    if (dataNode.isNeedsSaveToDisk()) {
		dataNode.saveChangesToCache(true);
	    }
	    try {
		bumpHistory(dataNode.getFile());
		copyLastHistoryToCurrent(dataNode); // bump history is normally used afteropen and before save, in this case we cannot use that order so we must make a copy
		synchronized (dataNode.getParentDomLockObject()) {
		    return dataNode.getMetadataUtils().addCorpusLink(dataNode.getURI(), new URI[]{targetNode.getURI()});
		}
	    } catch (IOException ex) {
		// Usually renaming issue. Try block includes add corpus link because this should not be attempted if history saving failed.
		BugCatcherManager.getBugCatcher().logError("I/O exception while moving node " + targetNode.toString() + " to " + dataNode.toString(), ex);
		messageDialogHandler.addMessageDialogToQueue(widgets.getString("COULD NOT MOVE NODES BECAUSE AN ERROR OCCURRED WHILE SAVING HISTORY FOR NODE. SEE ERROR LOG FOR DETAILS."), widgets.getString("ERROR WHILE MOVING NODES"));
		return false;
	    }
	}
    }

    public void deleteCorpusLink(ArbilDataNode dataNode, ArbilDataNode[] targetImdiNodes) {
	// TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
	if (dataNode.nodeNeedsSaveToDisk) {
	    dataNode.saveChangesToCache(false);
	}
	try {
	    dataNode.bumpHistory();
	    copyLastHistoryToCurrent(dataNode); // bump history is normally used afteropen and before save, in this case we cannot use that order so we must make a copy
	    synchronized (dataNode.getParentDomLockObject()) {
		logger.debug("deleting by corpus link");
		URI[] copusUriList = new URI[targetImdiNodes.length];
		for (int nodeCounter = 0; nodeCounter < targetImdiNodes.length; nodeCounter++) {
		    //                if (targetImdiNodes[nodeCounter].hasResource()) {
		    //                    copusUriList[nodeCounter] = targetImdiNodes[nodeCounter].getFullResourceURI(); // todo: should this resouce case be used here? maybe just the uri
		    //                } else {
		    copusUriList[nodeCounter] = targetImdiNodes[nodeCounter].getURI();
		    //                }
		}
		dataNode.getMetadataUtils().removeCorpusLink(dataNode.getURI(), copusUriList);
		dataNode.getParentDomNode().loadArbilDom();
	    }
	    //        for (ImdiTreeObject currentChildNode : targetImdiNodes) {
	    ////            currentChildNode.clearIcon();
	    //            ArbilTreeHelper.getSingleInstance().updateTreeNodeChildren(currentChildNode);
	    //        }
	    for (ArbilDataNode removedChild : targetImdiNodes) {
		removedChild.removeFromAllContainers();
	    }
	} catch (IOException ex) {
	    // Usually renaming issue. Try block includes add corpus link because this should not be attempted if history saving failed.
	    BugCatcherManager.getBugCatcher().logError("I/O exception while deleting nodes from " + dataNode.toString(), ex);
	    messageDialogHandler.addMessageDialogToQueue(widgets.getString("COULD NOT DELETE NODES BECAUSE AN ERROR OCCURRED WHILE SAVING HISTORY FOR NODE. SEE ERROR LOG FOR DETAILS."), widgets.getString("ERROR WHILE MOVING NODES"));
	}

	dataNode.getParentDomNode().clearIcon();
	dataNode.getParentDomNode().clearChildIcons();
	dataNode.clearIcon(); // this must be cleared so that the leaf / branch flag gets set
    }

    /**
     * Inserts/sets resource location. Behavior will depend on node type
     *
     * @param location Location to insert/set
     */
    public void insertResourceLocation(ArbilDataNode dataNode, URI location) throws ArbilMetadataException {
	if (dataNode.isCmdiMetaDataNode()) {
	    ArbilDataNode resourceNode = null;
	    try {
		resourceNode = dataNodeLoader.getArbilDataNodeWithoutLoading(location);
	    } catch (Exception ex) {
		throw new ArbilMetadataException("Error creating resource node for URI: " + location.toString(), ex);
	    }
	    if (resourceNode == null) {
		throw new ArbilMetadataException(MessageFormat.format(widgets.getString("UNKNOWN ERROR CREATING RESOURCE NODE FOR URI: {0}"), location.toString()));
	    }

	    new MetadataBuilder().requestAddNode(dataNode, null, resourceNode);
	} else {
	    if (dataNode.hasResource()) {
		dataNode.resourceUrlField.setFieldValue(location.toString(), true, false);
	    }
	}
    }

    public void addField(ArbilDataNode dataNode, ArbilField fieldToAdd) {
	//        System.addField:out.println("addField: " + this.getUrlString() + " : " + fieldToAdd.xmlPath + " : " + fieldToAdd.getFieldValue());
	ArbilField[] currentFieldsArray = dataNode.getFieldArray(fieldToAdd.getTranslateFieldName());
	if (currentFieldsArray == null) {
	    currentFieldsArray = new ArbilField[]{fieldToAdd};
	} else {
	    //            logger.debug("appendingField: " + fieldToAdd);
	    ArbilField[] appendedFieldsArray = new ArbilField[currentFieldsArray.length + 1];
	    System.arraycopy(currentFieldsArray, 0, appendedFieldsArray, 0, currentFieldsArray.length);
	    appendedFieldsArray[appendedFieldsArray.length - 1] = fieldToAdd;
	    currentFieldsArray = appendedFieldsArray;

	    //            for (ImdiField tempField : currentFieldsArray) {
	    //                logger.debug("appended fields: " + tempField);
	    //            }
	}
	dataNode.addFieldArray(fieldToAdd.getTranslateFieldName(), currentFieldsArray);

	if (fieldToAdd.xmlPath.endsWith(".ResourceLink") && fieldToAdd.getParentDataNode().isChildNode()/* && fieldToAdd.parentImdi.getUrlString().contains("MediaFile") */) {
	    dataNode.resourceUrlField = fieldToAdd;
	    mimeHashQueue.addToQueue(dataNode);
	}
    }

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history. the caller is
     * responsible for reloading the node if that is required
     */
    public void saveChangesToCache(ArbilDataNode datanode) {
	if (datanode != datanode.getParentDomNode()) {
	    //        if (this.isImdiChild()) {
	    saveChangesToCache(datanode.getParentDomNode());
	    return;
	}
	logger.debug("saveChangesToCache {}", datanode);

	synchronized (datanode.getParentDomLockObject()) {
	    // this lock is to prevent the metadata file being modified and reloaded in the middle of this process
	    ArbilJournal.getSingleInstance().clearFieldChangeHistory();
	    if (!datanode.isLocal() /* nodeUri.getScheme().toLowerCase().startsWith("http") */) {
		logger.debug("should not try to save remote files");
		return;
	    }
	    final List<FieldUpdateRequest> fieldUpdateRequests = createFieldUpdateRequests(datanode);
	    if (componentBuilder.setFieldValues(datanode, fieldUpdateRequests)) {
		datanode.nodeNeedsSaveToDisk = false;
	    } else {
		messageDialogHandler.addMessageDialogToQueue(widgets.getString("ERROR SAVING CHANGES TO DISK, CHECK THE LOG FILE VIA THE HELP MENU FOR MORE INFORMATION."), widgets.getString("SAVE"));
	    }
	}
	//        clearIcon(); this is called by setImdiNeedsSaveToDisk
    }

    private List<FieldUpdateRequest> createFieldUpdateRequests(ArbilDataNode datanode) {
	final List<FieldUpdateRequest> fieldUpdateRequests = new ArrayList<FieldUpdateRequest>();
	final List<ArbilField[]> allFields = new ArrayList<ArbilField[]>();
	getAllFields(datanode, allFields);
	for (ArbilField[] currentFieldArray : allFields) {
	    for (int fieldCounter = 0; fieldCounter < currentFieldArray.length; fieldCounter++) {
		final ArbilField currentField = currentFieldArray[fieldCounter];
		if (currentField.fieldNeedsSaveToDisk()) {
		    final FieldUpdateRequest currentFieldUpdateRequest = new FieldUpdateRequest();
		    currentFieldUpdateRequest.keyNameValue = currentField.getKeyName();
		    currentFieldUpdateRequest.fieldOldValue = currentField.originalFieldValue;
		    currentFieldUpdateRequest.fieldNewValue = currentField.getFieldValueForXml();
		    currentFieldUpdateRequest.fieldPath = currentField.getFullXmlPath();
		    currentFieldUpdateRequest.fieldLanguageId = currentField.getLanguageId();
		    currentFieldUpdateRequest.attributeValuesMap = currentField.getAttributeValuesMap();
		    fieldUpdateRequests.add(currentFieldUpdateRequest);
		}
	    }
	}
	return fieldUpdateRequests;
    }

    /**
     * Vector gets populated with all fields relevant to the parent node that
     * includes all indinodechild fields but not from any other imdi file
     *
     * @param dataNode node to get all children for
     * @param allFields List to populate
     */
    protected void getAllFields(ArbilDataNode dataNode, List<ArbilField[]> allFields) {
	logger.debug("getAllFields: {}", this);
	allFields.addAll(dataNode.getFields().values());
	for (ArbilDataNode currentChild : dataNode.getChildArray()) {
	    if (currentChild.isChildNode()) {
		getAllFields(currentChild, allFields);
	    }
	}
    }

    public void setDataNodeNeedsSaveToDisk(ArbilDataNode dataNode, ArbilField originatingField, boolean updateUI) {
	if (dataNode.resourceUrlField != null && dataNode.resourceUrlField.equals(originatingField)) {
	    dataNode.hashString = null;
	    dataNode.mpiMimeType = null;
	    dataNode.thumbnailFile = null;
	    dataNode.typeCheckerMessage = null;
	    mimeHashQueue.addToQueue(dataNode);
	}
	boolean needsSaveToDisk = dataNode.hasChangedFields() || dataNode.hasDomIdAttribute;
	if (dataNode.isMetaDataNode() && !dataNode.isChildNode()) {
	    if (needsSaveToDisk == false) {
		for (ArbilDataNode childNode : dataNode.getAllChildren()) {
		    if (childNode.nodeNeedsSaveToDisk) {
			needsSaveToDisk = true;
		    }
		}
	    }
	    if (dataNode.nodeNeedsSaveToDisk != needsSaveToDisk) {
		if (needsSaveToDisk) {
		    dataNodeLoader.addNodeNeedingSave(dataNode);
		} else {
		    dataNodeLoader.removeNodesNeedingSave(dataNode);
		}
		dataNode.nodeNeedsSaveToDisk = needsSaveToDisk;
	    }
	} else {
	    dataNode.nodeNeedsSaveToDisk = needsSaveToDisk; // this must be set before setImdiNeedsSaveToDisk is called
	    setDataNodeNeedsSaveToDisk(dataNode.getParentDomNode(), null, updateUI);
	}
	if (updateUI) {
	    dataNode.clearIcon();
	}
    }

    public void bumpHistory(File dataNodeFile) throws IOException {
	// update the files version number
	//TODO: the template add does not create a new history file
	int versionCounter = 0;
	File headVersion = dataNodeFile;
	//        if the .x file (the last head) exist then replace the current with it
	if (new File(dataNodeFile.getAbsolutePath() + ".x").exists()) {
	    versionCounter++;
	    headVersion = new File(dataNodeFile.getAbsolutePath() + ".x");
	}
	while (new File(dataNodeFile.getAbsolutePath() + "." + versionCounter).exists()) {
	    versionCounter++;
	}
	while (versionCounter >= 0) {
	    File lastFile = new File(dataNodeFile.getAbsolutePath() + "." + versionCounter);
	    versionCounter--;
	    File nextFile = new File(dataNodeFile.getAbsolutePath() + "." + versionCounter);
	    if (versionCounter >= 0) {
		logger.debug("renaming: {}: {}", nextFile, lastFile);
		if (!nextFile.renameTo(lastFile)) {
		    throw new IOException("Error while copying history files for metadata. Could not rename " + nextFile.toString() + " to " + lastFile.toString());
		}
	    } else {
		logger.debug("renaming: {}: {}", headVersion, lastFile);
		if (!headVersion.renameTo(lastFile)) {
		    throw new IOException("Error while copying history files for metadata. Could not rename " + headVersion.toString() + " to " + lastFile.toString());
		}
	    }
	}
    }

    public boolean resurrectHistory(ArbilDataNode dataNode, String historyVersion) {
	InputStream historyFile = null;
	OutputStream activeVersionFile = null;
	try {
	    if (historyVersion.equals(".x")) {
		if (dataNode.getFile().delete()) {
		    if (!new File(dataNode.getFile().getAbsolutePath() + ".x").renameTo(dataNode.getFile())) {
			throw new IOException("Could not rename history file '" + dataNode.getFile().getAbsolutePath() + ".x'");
		    }
		} else {
		    throw new IOException("Could not delete old history file: " + dataNode.getFile().getAbsolutePath());
		}
	    } else {
		try {
		    messageDialogHandler.offerUserToSaveChanges();
		} catch (Exception e) {
		    // user canceled the save action
		    // todo: alert user that nothing was done
		    return false;
		}
		if (!new File(dataNode.getFile().getAbsolutePath() + ".x").exists()) {
		    if (!dataNode.getFile().renameTo(new File(dataNode.getFile().getAbsolutePath() + ".x"))) {
			throw new IOException("Could not rename to history file: " + dataNode.getFile().getAbsolutePath());
		    }
		} else {
		    if (!dataNode.getFile().delete()) {
			throw new IOException("Could not delete history file: " + dataNode.getFile().getAbsolutePath());
		    }
		}
		historyFile = new FileInputStream(new File(dataNode.getFile().getAbsolutePath() + historyVersion));
		activeVersionFile = new FileOutputStream(dataNode.getFile(), true);

		byte[] copyBuffer = new byte[1024];
		int len;
		while ((len = historyFile.read(copyBuffer)) > 0) {
		    activeVersionFile.write(copyBuffer, 0, len);
		}

	    }
	} catch (FileNotFoundException e) {
	    messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(widgets.getString("{0}. HISTORY MAY BE BROKEN FOR {1}"), e.getLocalizedMessage(), dataNode.toString()), "File not found");
	    BugCatcherManager.getBugCatcher().logError(e);
	    return false;
	} catch (IOException e) {
	    messageDialogHandler.addMessageDialogToQueue(MessageFormat.format(widgets.getString("{0}. HISTORY MAY BE BROKEN FOR {1}"), e.getLocalizedMessage(), dataNode.toString()), "Error while reading or writing to disk");
	    BugCatcherManager.getBugCatcher().logError(e);
	    return false;
	} finally {
	    if (null != historyFile) {
		try {
		    historyFile.close();
		} catch (IOException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	    if (null != activeVersionFile) {
		try {
		    activeVersionFile.close();
		} catch (IOException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	}
	dataNodeLoader.requestReload(dataNode.getParentDomNode());

	return true;
    }

    public void copyLastHistoryToCurrent(ArbilDataNode dataNode) {
	FileOutputStream outFile = null;
	InputStream inputStream = null;
	try {
	    outFile = new FileOutputStream(dataNode.getFile());
	    inputStream = new FileInputStream(new File(dataNode.getFile().getAbsolutePath() + ".0"));
	    int bufferLength = 1024 * 4;
	    byte[] buffer = new byte[bufferLength];
	    int bytesread = 0;
	    while (bytesread >= 0) {
		bytesread = inputStream.read(buffer);
		if (bytesread == -1) {
		    break;
		}
		outFile.write(buffer, 0, bytesread);
	    }
	} catch (IOException iOException) {
	    messageDialogHandler.addMessageDialogToQueue(widgets.getString("COULD NOT COPY FILE WHEN RECOVERING FROM THE LAST HISTORY FILE."), widgets.getString("RECOVER HISTORY"));
	    BugCatcherManager.getBugCatcher().logError(iOException);
	} finally {
	    if (inputStream != null) {
		try {
		    inputStream.close();
		} catch (IOException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	    if (outFile != null) {
		try {
		    outFile.close();
		} catch (IOException ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	}
    }

    public ArbilDataNode loadArbilDataNode(Object registeringObject, URI localUri) {
	return dataNodeLoader.getArbilDataNode(registeringObject, localUri);
    }

    public void reloadNodeShallowly(ArbilDataNode dataNode) {
	dataNode.getParentDomNode().nodeNeedsSaveToDisk = false; // clear any changes
	dataNodeLoader.requestShallowReload(dataNode.getParentDomNode());
    }

    public void reloadNode(ArbilDataNode dataNode) {
	dataNode.getParentDomNode().nodeNeedsSaveToDisk = false; // clear any changes
	//        if (!this.isImdi()) {
	//            initNodeVariables();
	//            //loadChildNodes();
	//            clearIcon();
	//            // TODO: this could just remove the decendant nodes and let the user re open them
	//            ArbilTreeHelper.getSingleInstance().updateTreeNodeChildren(this);
	////            this.clearIcon();
	//        } else {
	////            if (getParentDomNode().isCorpus()) {
	////                getParentDomNode().autoLoadChildNodes = true;
	////            }
	dataNodeLoader.requestReload(dataNode.getParentDomNode());
	//        }
    }

    public void loadArbilDom(ArbilDataNode dataNode) {
	if (dataNode.getParentDomNode() != dataNode) {
	    dataNode.getParentDomNode().loadArbilDom();
	} else {
	    // we reduce the times the file type is checked by only checking when the type is unset, this is because for difficult files a deep check is required which requires downloading a small portion of the file
	    if (dataNode.getFormatType() == MetadataFormat.FileType.UNKNOWN) {
		dataNode.setFormatType(metadataFormat.deepCheck(dataNode.getURI()));
	    }
	    synchronized (dataNode.getParentDomLockObject()) {
		dataNode.initNodeVariables(); // this might be run too often here but it must be done in the loading thread and it also must be done when the object is created
		if (!dataNode.isMetaDataNode() && !dataNode.isDirectory() && dataNode.isLocal()) {
		    // if it is an not imdi or a loose file but not a direcotry then get the md5sum
		    mimeHashQueue.addToQueue(dataNode);
		    dataNode.setLoadingState(ArbilDataNode.LoadingState.LOADED);
		}
		if (dataNode.isDirectory()) {
		    getDirectoryLinks(dataNode);
		    dataNode.setLoadingState(ArbilDataNode.LoadingState.LOADED);
		    //            clearIcon();
		}
		if (dataNode.isMetaDataNode()) {
		    loadMetadataDom(dataNode);

		    LoadingState requestedLoadingState = dataNode.getRequestedLoadingState();
		    if (requestedLoadingState == null) {
			requestedLoadingState = LoadingState.LOADED;
		    }

		    dataNode.setLoadingState(requestedLoadingState);
		}
	    }
	}
    }

    /**
     * Sets requested loading state to {@link LoadingState#LOADED} and performs
     * a {@link #loadArbilDom() }
     */
    public void loadFullArbilDom(ArbilDataNode dataNode) {
	dataNode.setLoadingState(ArbilDataNode.LoadingState.UNLOADED);
	dataNode.setRequestedLoadingState(ArbilDataNode.LoadingState.LOADED);
	dataNode.updateLoadingState(+1);
	try {
	    dataNode.loadArbilDom();
	} finally {
	    dataNode.updateLoadingState(-1);
	}
    }

    /**
     * Retrieves the direct ancestor of the specified child node
     *
     * @param node child node to find direct ancestor for
     * @return the direct ancestor of the specified node
     */
    public ArbilDataNode getParentOfNode(ArbilDataNode node) {
	return searchParentOf(node.getParentDomNode(), node);
    }

    /**
     *
     * @param ancestor ancestor to start searching for child node
     * @param targetChild child node to find direct ancestor for
     * @return null if the ancestor is equal to the target child or is not an
     * ancestor of the target child; otherwise the direct ancestor of the target
     * child
     */
    private ArbilDataNode searchParentOf(final ArbilDataNode ancestor, final ArbilDataNode targetChild) {
	if (!ancestor.equals(targetChild)) {
	    for (ArbilDataNode child : ancestor.getChildArray()) {
		if (child.equals(targetChild)) {
		    return ancestor;
		} else {
		    final ArbilDataNode childResult = searchParentOf(child, targetChild);
		    if (childResult != null) {
			return childResult;
		    }
		}
	    }
	}
	return null;
    }

    //<editor-fold defaultstate="collapsed" desc="Utilities (should probably be moved into a separate utility class)">
    // TODO: this is not used yet but may be required for unicode paths
    public String urlEncodePath(String inputPath) {
	// url encode the path elements
	String encodedString = null;
	try {
	    for (String inputStringPart : inputPath.split("/")) {
		//                    logger.debug("inputStringPart: " + inputStringPart);
		if (encodedString == null) {
		    encodedString = URLEncoder.encode(inputStringPart, "UTF-8");
		} else {
		    encodedString = encodedString + "/" + URLEncoder.encode(inputStringPart, "UTF-8");
		}
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	return encodedString;
    }

    public static URI conformStringToUrl(String inputUrlString) throws URISyntaxException {
	//            localUrlString = localUrlString.replace("\\", "/");
//        if (inputUrlString.toLowerCase().startsWith("hdl:")) {
//            return new HandleUtils().resolveHandle(inputUrlString);
//        } else 
	if (!inputUrlString.toLowerCase().startsWith("hdl:") && !inputUrlString.toLowerCase().startsWith("http") && !inputUrlString.toLowerCase().startsWith("file:") && !inputUrlString.toLowerCase().startsWith(".")) {
	    return new File(inputUrlString).toURI();
	} else {
	    // apache method
	    //                URI tempURI = new URI(inputUrlString);
	    //                URI returnURI = URIUtils.createURI(tempURI.getScheme(), tempURI.getHost(), tempURI.getPort(), tempURI.getPath(), tempURI.getQuery(), tempURI.getURIFragment());
	    //                return returnURI;
	    // end apache method : this requires the uri to be broken into its parts so we might as well do it with the standard classes
	    // mpi method
	    //                URI returnURI = URIUtil.newURI(inputUrlString);
	    // end mpi method : this will url encode the # etc. and therefore loose the fragment and other parts
	    //                boolean isUncPath = inputUrlString.toLowerCase().startsWith("file:////");
	    //                if (isUncPath) {
	    //                    try {
	    //                        returnURI = new URI("file:////" + returnURI.toString().substring("file:/".length()));
	    //                    } catch (URISyntaxException urise) {
	    //                       .logError(urise);
	    //                    }
	    //                }

	    // separate the path and protocol
	    int protocolEndIndex;
	    if (inputUrlString.startsWith(".")) {
		// TODO: this is un tested for ./ paths, but at this stage it appears unlikey to ever be needed
		protocolEndIndex = 0;
	    } else {
		protocolEndIndex = inputUrlString.indexOf(":");
	    }
	    //                while (inputUrlString.charAt(protocolEndIndex) == '/') {
	    //                    protocolEndIndex++;
	    //                }
	    String protocolComponent = inputUrlString.substring(0, protocolEndIndex);
	    String remainingComponents = inputUrlString.substring(protocolEndIndex + 1);
	    String[] pathComponentArray = remainingComponents.split("#");
	    String pathComponent = pathComponentArray[0];
	    String fragmentComponent = null;
	    if (pathComponentArray.length > 1) {
		fragmentComponent = pathComponentArray[1];
	    }
	    // note that this must be done as separate parameters not a single string otherwise it will not get url encoded
	    // TODO: this could require the other url components to be added here
	    return new URI(protocolComponent, pathComponent, fragmentComponent);
	    //                logger.debug("returnUrl: " + returnUrl);
	    ////                int protocolEndIndex = inputUrlString.lastIndexOf("/", "xxxx:".length());

	    //                String pathComponentEncoded = URLEncoder.encode(pathComponent, "UTF-8");
	    //                returnUrl = new URI(protocolComponent + pathComponentEncoded);
	    //                logger.debug("returnUrl: " + returnUrl);
	}
	//            // if the imdi api finds only one / after the file: it will interpret the url as relative and make a bit of a mess of it, so we have to make sure that we have two for the url and one for the root
	//            if (returnUrl.toString().toLowerCase().startsWith("file:") && !returnUrl.toString().toLowerCase().startsWith("file:///")) {
	//                // here we assume that this application does not use relative file paths
	//                returnUrl = new URL("file", "", "//" + returnUrl.getPath());
	//            }
	//            logger.debug("conformStringToUrl URI: " + new URI(returnUrl.toString()));

	//        logger.debug("conformStringToUrl out: " + returnUrl.toString());
    }

    static public URI normaliseURI(URI inputURI) {
	//        logger.debug("normaliseURI: " + inputURI);
	boolean isUncPath = inputURI.toString().toLowerCase().startsWith("file:////");
	URI returnURI = inputURI.normalize();
	if (isUncPath) {
	    try {
		// note that this must use the single string parameter to prevent re url encoding
		returnURI = new URI("file:////" + returnURI.toString().substring("file:/".length()));
	    } catch (URISyntaxException urise) {
		System.err.println(urise.toString());
		//BugCatcherManager.getBugCatcher().logError(urise);
	    }
	}
	return returnURI;
    }
    //</editor-fold>

    //<editor-fold defaultstate="expanded" desc="Private methods">
    //<editor-fold defaultstate="collapsed" desc="Metadata DOM loading">    
    private void loadMetadataDom(ArbilDataNode dataNode) {
	if (dataNode.isLocal() && !dataNode.getFile().exists() && new File(dataNode.getFile().getAbsolutePath() + ".0").exists()) {
	    // if the file is missing then try to find a valid history file
	    copyLastHistoryToCurrent(dataNode);
	    messageDialogHandler.addMessageDialogToQueue(widgets.getString("MISSING FILE HAS BEEN RECOVERED FROM THE LAST HISTORY ITEM."), widgets.getString("RECOVER HISTORY"));
	}
	try {
	    //set the string name to unknown, it will be updated in the tostring function
	    dataNode.nodeText = "unknown";
	    initComponentLinkReader(dataNode);
	    updateMetadataChildNodes(dataNode);
	} catch (Exception mue) {
	    BugCatcherManager.getBugCatcher().logError(dataNode.getUrlString(), mue);
	    //            logger.debug("Invalid input URL: " + mue);
	    File nodeFile = dataNode.getFile();
	    if (nodeFile != null && nodeFile.exists()) {
		dataNode.nodeText = widgets.getString("COULD NOT LOAD DATA");
	    } else {
		dataNode.nodeText = widgets.getString("FILE NOT FOUND");
		dataNode.fileNotFound = true;
	    }
	}
    }

    private void initComponentLinkReader(ArbilDataNode dataNode) {
	if (dataNode.isCmdiMetaDataNode()) {
	    final URI matadataUri = new HandleUtils().resolveHandle(dataNode.getURI());
	    // load the links from the cmdi file
	    // the links will be hooked to the relevent nodes when the rest of the xml is read
	    dataNode.getCmdiComponentLinkReader().readLinks(matadataUri);
	}
    }

    private void updateMetadataChildNodes(ArbilDataNode dataNode) throws ParserConfigurationException, SAXException, IOException, TransformerException, ArbilMetadataException {
	final URI matadataUri = new HandleUtils().resolveHandle(dataNode.getURI());
	Document nodDom = ArbilComponentBuilder.getDocument(matadataUri);
	Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree = new HashMap<ArbilDataNode, Set<ArbilDataNode>>();
	dataNode.childLinks = loadMetadataChildNodes(dataNode, nodDom, parentChildTree);
	checkRemovedChildNodes(parentChildTree);
    }

    private List<String[]> loadMetadataChildNodes(ArbilDataNode dataNode, Document nodDom, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) throws TransformerException, ArbilMetadataException {
	final List<String[]> childLinks = new ArrayList<String[]>();
	final Map<String, Integer> siblingNodePathCounter = new HashMap<String, Integer>();
	// get the metadata format information required to read this nodes metadata
//        final String metadataStartPath = MetadataFormat.getMetadataStartPath(nodeUri.getPath());
	final String fullNodePath = "";
	final Node startNode = nodDom.getFirstChild();
//	if (metadataStartPath.length() > 0) {
//	    fullNodePath = metadataStartPath.substring(0, metadataStartPath.lastIndexOf("."));
//	    final String metadataXpath = metadataStartPath.replaceAll("\\.", "/:"); //"/:Kinnate/:Entity";
//	    final Node metadataNode = org.apache.xpath.XPathAPI.selectSingleNode(startNode, metadataXpath);
	// if this fails then we probably want to fail the reading of the node
//	    if (metadataNode == null) {
//		throw new ArbilMetadataException("Failed to find the start node for the metadata to read: " + fullNodePath);
//	    }
//	    startNode = metadataNode;
//	}
	// load the fields from the imdi file
	final boolean shallowLoading = LoadingState.PARTIAL.equals(dataNode.getRequestedLoadingState());
	metadataReader.iterateChildNodes(dataNode, childLinks, startNode, fullNodePath, fullNodePath, parentChildTree, siblingNodePathCounter, 0, shallowLoading);
	if (dataNode.isCmdiMetaDataNode()) {
	    // Add all links that have no references to the root node (might confuse users but at least it will show what's going on)
	    metadataReader.addUnreferencedResources(dataNode, parentChildTree, childLinks);
	}
	return childLinks;
    }

    private void checkRemovedChildNodes(Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) {
	for (Entry<ArbilDataNode, Set<ArbilDataNode>> entry : parentChildTree.entrySet()) {
	    ArbilDataNode currentNode = entry.getKey();
	    // logger.debug("setting childArray on: " + currentNode.getUrlString());
	    // save the old child array
	    ArbilDataNode[] oldChildArray = currentNode.childArray;
	    // set the new child array
	    final Set<ArbilDataNode> newChildren = entry.getValue();
	    currentNode.childArray = newChildren.toArray(new ArbilDataNode[newChildren.size()]);
	    // check the old child array and for each that is no longer in the child array make sure they are removed from any containers (tables or trees)
	    final List currentChildList = Arrays.asList(currentNode.childArray);
	    for (ArbilDataNode currentOldChild : oldChildArray) {
		if (!currentChildList.contains(currentOldChild)) {
		    // remove from any containers that its found in
		    for (ArbilDataNodeContainer currentContainer : currentOldChild.getRegisteredContainers()) {
			currentContainer.dataNodeRemoved(currentOldChild);
		    }
		}
	    }
	}
    }

    private void getDirectoryLinks(ArbilDataNode dataNode) {
	final File nodeFile = dataNode.getFile();
	if (nodeFile != null && nodeFile.isDirectory()) {
	    final File[] dirLinkArray = nodeFile.listFiles();
	    final List<ArbilDataNode> childLinksTemp = new ArrayList<ArbilDataNode>();
	    for (int linkCount = 0; linkCount < dirLinkArray.length; linkCount++) {
		try {
		    //                    logger.debug("nodeFile: " + nodeFile);
		    //                    logger.debug("dirLinkArray[linkCount]: " + dirLinkArray[linkCount]);
		    final URI childURI = dirLinkArray[linkCount].toURI();
		    final ArbilDataNode currentNode = dataNodeLoader.getArbilDataNodeWithoutLoading(childURI);
		    if (treeHelper.isShowHiddenFilesInTree() || !currentNode.getFile().isHidden()) {
			childLinksTemp.add(currentNode);
		    }
		} catch (Exception ex) {
		    messageDialogHandler.addMessageDialogToQueue(dirLinkArray[linkCount] + " could not be loaded in\n" + dataNode.getUrlString(), "Load Directory");
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	    //childLinks = childLinksTemp.toArray(new String[][]{});
	    dataNode.childArray = childLinksTemp.toArray(new ArbilDataNode[childLinksTemp.size()]);
	}
    }
    //</editor-fold>
    //</editor-fold>
}
