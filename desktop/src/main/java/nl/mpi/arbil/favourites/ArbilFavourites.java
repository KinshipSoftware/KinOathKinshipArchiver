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
package nl.mpi.arbil.favourites;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader.CmdiResourceLink;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.MetadataFormat;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilFavourites
 * Created on : Mar 3, 2009, 11:19:14 AM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilFavourites implements FavouritesService {

    private final static Logger logger = LoggerFactory.getLogger(ArbilFavourites.class);
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    //    private Hashtable<String, ImdiTreeObject> userFavourites;
    static private ArbilFavourites singleInstance = null;

    static synchronized public ArbilFavourites getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilFavourites();
	}
	return singleInstance;
    }

    // this will load any favourites in the old format and delete the old format file
    // this is not used now but should be kept in case some users need to import locations from olde versions of the application
    public void convertOldFormatLocationListsX() {
	try {
	    File oldLocationsFile = new File(sessionStorage.getProjectDirectory(), "locationsList");
	    File oldFavouritesFile = new File(sessionStorage.getProjectDirectory(), "selectedFavourites");
	    if (oldLocationsFile.exists()) {
		Vector<String> locationsList = (Vector<String>) sessionStorage.loadObject("locationsList");
		if (oldFavouritesFile.exists()) {
		    Vector<String> userFavouritesStrings = (Vector<String>) sessionStorage.loadObject("selectedFavourites");
		    locationsList.addAll(userFavouritesStrings);
		    sessionStorage.saveStringArray("locationsList", locationsList.toArray(new String[]{}));
		    oldFavouritesFile.deleteOnExit();
		}
		if (null == sessionStorage.loadStringArray("locationsList")) {
		    sessionStorage.saveStringArray("locationsList", locationsList.toArray(new String[]{}));
		}
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    public boolean toggleFavouritesList(ArbilDataNode[] dataNodeArray, boolean setAsTempate) throws ArbilMetadataException {
	logger.debug("toggleFavouriteList: {}", setAsTempate);
	if (setAsTempate) {
	    boolean selectionNeedsSave = false;
	    for (ArbilDataNode currentNode : dataNodeArray) {
		if (currentNode.getNeedsSaveToDisk(false)) {
		    selectionNeedsSave = true;
		}
	    }
	    if (selectionNeedsSave) {
		messageDialogHandler.addMessageDialogToQueue("Changes must be saved before adding to the favourites.", "Add Favourites");
		return false;
	    }
	}
	for (ArbilDataNode currentNode : dataNodeArray) {
	    if (setAsTempate && currentNode.isContainerNode()) {
		// don't add this node, but do add its children (if there are any)
		if (currentNode.getChildArray().length > 0) {
		    toggleFavouritesList(currentNode.getChildArray(), true);
		}
	    } else {
		if (setAsTempate) {
		    addAsFavourite(currentNode.getURI());
		} else {
		    removeFromFavourites(currentNode.getURI());
		    // TODO: remove from any tables and update the tree roots
//                currentImdiObject.setFavouriteStatus(false);
		}
	    }
	}
	return true;
    }

    @Override
    public void addAsFavourite(URI imdiUri) throws ArbilMetadataException {
	try {
	    URI baseUri = new URI(imdiUri.toString().split("#")[0]);
	    String fileSuffix = imdiUri.getPath().substring(imdiUri.getPath().lastIndexOf("."));
	    File destinationFile = File.createTempFile("fav-", fileSuffix, sessionStorage.getFavouritesDir());
	    ArbilDataNode.getMetadataUtils(baseUri.toString()).copyMetadataFile(baseUri, destinationFile, makeLinksAbsolute(imdiUri), true);

	    URI copiedFileURI = destinationFile.toURI();
	    // creating a uri with separate parameters could cause the url to be reencoded
	    // hence this has been converted to use the string URI constuctor
//            URI favouriteUri = new URI(copiedFileURI.getScheme(), copiedFileURI.getUserInfo(), copiedFileURI.getHost(), copiedFileURI.getPort(), copiedFileURI.getPath(), copiedFileURI.getQuery(),
//                    imdiUri.getURIFragment());
	    String uriString = copiedFileURI.toString().split("#")[0] /* fragment removed */;
	    if (imdiUri.getFragment() != null) {
		uriString = uriString + "#" + imdiUri.getFragment();
	    }
	    URI favouriteUri = new URI(uriString);
//            if (!userFavourites.containsKey(favouriteUrlString)) {
//                ImdiTreeObject favouriteImdiObject = GuiHelper.imdiLoader.getImdiObject(null, favouriteUrlString);
//                userFavourites.put(favouriteUrlString, favouriteImdiObject);
//                saveSelectedFavourites();
//                loadSelectedFavourites();
//                favouriteImdiObject.setFavouriteStatus(true);
//            }
	    treeHelper.addLocation(favouriteUri);
	    treeHelper.applyRootLocations();
	} catch (ArbilMetadataException ex) {
	    throw (ex);
	} catch (Exception ex) {
	    logger.error("Error while adding favourite", ex);
	}
    }

    /**
     * Creates update array for CMDI resource links that need to be made absolute before being put into favorites
     *
     * @param imdiUri
     * @return update array for resource links with items for links that currently are not absolute. For IMDI will return null.
     */
    private URI[][] makeLinksAbsolute(URI imdiUri) {
	List<URI[]> relativeLinks = null;
	if (MetadataFormat.isPathCmdi(imdiUri.toString())) {
	    relativeLinks = new ArrayList<URI[]>();
	    ArrayList<CmdiResourceLink> resourceLinks = new CmdiComponentLinkReader().readLinks(imdiUri);
	    for (CmdiResourceLink link : resourceLinks) {
		try {
		    final URI linkUri = link.getLinkUri();
		    if (!linkUri.isAbsolute()) {
			relativeLinks.add(new URI[]{linkUri, link.getResolvedLinkUri()});
		    }
		} catch (URISyntaxException ex) {
		    // resource proxy has syntax error in link. skip
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	}
	if (relativeLinks != null) {
	    return relativeLinks.toArray(new URI[][]{});
	} else {
	    return null;
	}
    }

    private void removeFromFavourites(URI imdiUri) {
	treeHelper.removeLocation(imdiUri);
	treeHelper.applyRootLocations();
    }

//    public void saveSelectedFavourites() {
//        try {
//            LinorgSessionStorage.getSingleInstance().saveObject(new Vector(userFavourites.keySet()), "selectedFavourites");
//        } catch (Exception ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//        }
//    }
//    public Vector getFavouritesAsUrls() {
//        return new Vector(userFavourites.keySet());
//    }
//
//    public ImdiTreeObject[] listAllFavourites() {
//        return userFavourites.values().toArray(new ImdiTreeObject[userFavourites.size()]);
//    }
    public ArbilDataNode[] listFavouritesFor(Object targetNodeUserObject) {
	logger.debug("listFavouritesFor: {}", targetNodeUserObject);
	ArrayList<ArbilDataNode> validFavourites = new ArrayList<ArbilDataNode>();
	if (targetNodeUserObject instanceof ArbilNode) {
	    ArbilDataNode targetDataNode = null;

	    boolean targetIsDataNode = (targetNodeUserObject instanceof ArbilDataNode);
	    boolean targetIsLocalCorpusRoot;
	    if (targetIsDataNode) {
		targetDataNode = (ArbilDataNode) targetNodeUserObject;
		targetIsLocalCorpusRoot = false;
	    } else {
		targetIsLocalCorpusRoot = targetNodeUserObject == ((DefaultMutableTreeNode) treeHelper.getLocalCorpusTreeModel().getRoot()).getUserObject();
	    }

	    boolean targetIsCorpus = targetIsDataNode && targetDataNode.isCorpus();
	    boolean targetIsSession = targetIsDataNode && targetDataNode.isSession();
	    boolean targetIsChildNode = targetIsDataNode && targetDataNode.isChildNode();
	    for (ArbilDataNode currentFavouritesObject : treeHelper.getFavouriteNodes()) {
		boolean addThisFavourites = false;
		if (targetIsLocalCorpusRoot) {
		    // Local corpus root can only contain non-child nodes
		    addThisFavourites = !currentFavouritesObject.isChildNode();
		} else if (targetIsCorpus && !currentFavouritesObject.isChildNode()) {
		    addThisFavourites = true;
		} else if (targetIsSession && currentFavouritesObject.isChildNode()) {
		    addThisFavourites = MetadataReader.getSingleInstance().nodeCanExistInNode(targetDataNode, currentFavouritesObject);
		} else if (targetIsChildNode && currentFavouritesObject.isChildNode()) {
		    addThisFavourites = MetadataReader.getSingleInstance().nodeCanExistInNode(targetDataNode, currentFavouritesObject);
		}
		if (addThisFavourites) {
//                    logger.debug("adding: " + currentFavouritesObject);
		    validFavourites.add(currentFavouritesObject);
		} else {
		    // imdi child favourites cannot be added to a corpus
		    // sessions cannot be added to a session
//                    logger.debug("omitting: " + currentFavouriteObject);
		}
	    }
	}
	return validFavourites.toArray(new ArbilDataNode[0]);
    }

    public String getNodeType(ArbilDataNode favouriteNode, ArbilDataNode targetDataNode) {
	// takes the source path and destination path and creates a complete combined path
	// in:
	// .METATRANSCRIPT.Session.MDGroup.Actors.Actor(12).Languages.Language(5)
	// .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27)
	// or in:
	// favouriteXmlPath: .METATRANSCRIPT.Session.MDGroup.Actors.Actor(1)
	// targetXmlPath:.METATRANSCRIPT.Session.MDGroup.Actors.Actor(3)
	// out:
	// .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27).Languages.Language

	String favouriteXmlPath = favouriteNode.getURIFragment();
	String targetXmlPath = targetDataNode.getURIFragment();
	logger.debug("getNodeType: \nfavouriteXmlPath: {} targetXmlPath: {}", favouriteXmlPath, targetXmlPath);
	String returnValue;
	if (favouriteNode.isSession()) {
	    returnValue = MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session";
	} else if (favouriteNode.isCorpus()) {
	    returnValue = MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Corpus";
	} else if (favouriteNode.isChildNode()) {
	    if (targetXmlPath == null) {
		returnValue = favouriteXmlPath.replaceAll("\\(\\d*?\\)$", "");
	    } else {
		// pass the (x) values on in the return value
		logger.debug("targetXmlPath: {}", targetXmlPath);
		logger.debug("favouriteXmlPath: {}", favouriteXmlPath);
		favouriteXmlPath = favouriteXmlPath.replaceAll("\\(\\d*?\\)$", "");
		String[] splitFavouriteXmlPath = favouriteXmlPath.split("\\)");
		String[] splitTargetXmlPath = targetXmlPath.split("\\)");
		logger.debug("splitFavouriteXmlPath: {} splitTargetXmlPath: {}", splitFavouriteXmlPath.length, splitTargetXmlPath.length);
		returnValue = "";
		for (int partCounter = 0; partCounter < splitFavouriteXmlPath.length; partCounter++) {
		    if (splitTargetXmlPath.length > partCounter) {
			returnValue = returnValue.concat(splitTargetXmlPath[partCounter] + ")");
		    } else {
			returnValue = returnValue.concat(splitFavouriteXmlPath[partCounter] + ")");
		    }
		}
		returnValue = returnValue.replaceAll("\\)$", "");
		returnValue = returnValue.replaceAll("\\(\\d*?$", "");
	    }
	} else {
	    returnValue = null;
	}
	logger.debug("getNodeTypeReturnValue: {}", returnValue);
	return returnValue;
    }
//    public void mergeFromFavourite(ImdiTreeObject targetImdiObject, ImdiTreeObject favouriteImdiObject, boolean overwriteValues) {
////        logger.debug("mergeFromFavourite: " + addedNodeUrl + " : " + imdiTemplateUrl);
//        Hashtable<String, ImdiField[]> targetFieldsHash = targetImdiObject.getFields();
//        for (Enumeration<ImdiField[]> favouriteFieldEnum = favouriteImdiObject.getFields().elements(); favouriteFieldEnum.hasMoreElements();) {
//            ImdiField[] currentFavouriteFields = favouriteFieldEnum.nextElement();
//            if (currentFavouriteFields.length > 0) {
//                ImdiField[] targetNodeFields = targetFieldsHash.get(currentFavouriteFields[0].getTranslateFieldName());
//
//                logger.debug("TranslateFieldName: " + currentFavouriteFields[0].getTranslateFieldName());
//                logger.debug("targetImdiObjectLoading: " + targetImdiObject.isLoading());
//                if (targetNodeFields != null) {
//                    logger.debug("copy fields");
//                    for (int fieldCounter = 0; fieldCounter < currentFavouriteFields.length; fieldCounter++) {
//                        ImdiField currentField;
//                        if (targetNodeFields.length > fieldCounter) {
//                            // copy to the exisiting fields
//                            currentField = targetNodeFields[fieldCounter];
//                            currentField.setFieldValue(currentFavouriteFields[fieldCounter].getFieldValue(), false, false);
//                        } else {
//                            // add sub nodes if they dont already exist
//                            currentField = new ImdiField(0, targetImdiObject, currentFavouriteFields[fieldCounter].xmlPath, "", 0); // this is not correct but this section should be simplified asap
//                            currentField.setFieldValue(currentFavouriteFields[fieldCounter].getFieldValue(), false, true); // this is done separatly to trigger the needs save to disk flag
//                            targetImdiObject.addField(currentField);
////                            currentField.fieldNeedsSaveToDisk = true;
//                        }
//                        String currentLanguageId = currentFavouriteFields[fieldCounter].getLanguageId();
//                        if (currentLanguageId != null) {
//                            currentField.setLanguageId(currentLanguageId, false, true);
//                        }
//                    }
//                }
//            }
//        }
//    }
}
