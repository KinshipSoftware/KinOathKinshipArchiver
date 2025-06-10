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
package nl.mpi.arbil;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.MetadataFormat;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.MimeHashQueue.TypeCheckerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilIcons
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilIcons {

    private final static Logger logger = LoggerFactory.getLogger(ArbilIcons.class);
    private static ApplicationVersionManager versionManager;

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
	versionManager = versionManagerInstance;
    }
    // the applicationIconName is set by the build script and will be in the jar file of the main class which might not be the arbil jar, so we use the ApplicationVersion class to access the correct jar file for the application icon.
    public ImageIcon linorgIcon = new ImageIcon(versionManager.getApplicationVersion().getClass().getResource(versionManager.getApplicationVersion().applicationIconName));
    // basic icons used in the gui
    public ImageIcon serverIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/server16x16.png"));
    public ImageIcon directoryIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/directory16x16.png"));
    public ImageIcon computerIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/computer16x16.png"));
    public ImageIcon loadingIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading01.png"));
    // complex icons used for the imdi files
//    private ImageIcon corpusicon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusnode_color.png"));
    private ImageIcon localicon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/local.png"));
    private ImageIcon remoteicon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/remote.png"));
    private ImageIcon localWithArchiveHandle = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/localarchivehandle.png"));
//    private ImageIcon blankIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/blank.png"));
    private ImageIcon writtenresourceIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/writtenresource.png"));
    private ImageIcon videoIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/video.png"));
//    private ImageIcon annotationIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/annotation.png"));
    private ImageIcon audioIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/audio.png"));
//    private ImageIcon mediafileIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/mediafile.png"));
//    private ImageIcon corpuslocal16x16cIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpuslocal16x16c.png"));
//    private ImageIcon metadataIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/metadata.png"));
    public ImageIcon corpusnodeColorIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusnode_color.png"));
    //private ImageIcon missingRedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/missing-red.png"));
    private ImageIcon missingRedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/notfound.png"));
//    private ImageIcon corpusnodeIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusnode.png"));
//    private ImageIcon openerClosedBlackIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/Opener_closed_black.png"));
//    private ImageIcon corpusIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpus.png"));
//    private ImageIcon openerOpenBlackIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/Opener_open_black.png"));
//    private ImageIcon corpusserver16x16cIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusserver16x16c.png"));
    private ImageIcon picturesIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/pictures.png"));
//    private ImageIcon corpusserverlocal16x16cIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/corpusserverlocal16x16c.png"));
    private ImageIcon questionRedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/question-red.png"));
    private ImageIcon questionBlackIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/question-black.png"));
    public ImageIcon dataIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/data.png"));
    public ImageIcon fieldIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/field.png"));
    private ImageIcon dataemptyIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/dataempty.png"));
    private ImageIcon dataContainerIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/datacontainer.png"));
//    private ImageIcon server16x16Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/server16x16.png"));
//    private ImageIcon directory16x16Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/directory16x16.png"));
//    private ImageIcon sessionColorLocalIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/session_color-local.png"));
//    private ImageIcon directoryclosed16x16Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/directoryclosed16x16.png"));
    public ImageIcon sessionColorIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/session_color.png"));
    public final static ImageIcon clarinIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/clarinE.png"));
    public final static ImageIcon kinOathIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/KinOath-16.png"));
    public ImageIcon catalogueColorIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/catalogue.png"));
    //private ImageIcon exclamationBlueIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/exclamation-blue.png"));
//    private ImageIcon sessionColorServerlocalIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/session_color-serverlocal.png"));
//    private ImageIcon exclamationGreenIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/exclamation-green.png"));
//    private ImageIcon sessionColorServerIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/session_color-server.png"));
    //private ImageIcon exclamationRedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/exclamation-red.png"));
    public ImageIcon languageIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/language.png"));
//    private ImageIcon sessionIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/session.png"));
//    private ImageIcon exclamationYellowIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/exclamation-yellow.png"));
//    private ImageIcon stopIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/stop.png"));
//    private ImageIcon file16x16Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/file16x16.png"));
//    private ImageIcon filelocal16x16Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/filelocal16x16.png"));
    private ImageIcon tickBlueIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/tick-blue.png"));
    private ImageIcon fileIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/file.png"));
    private ImageIcon fileCheckedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/file-checked.png"));
    private ImageIcon tickGreenIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/tick-green.png"));
//    private ImageIcon fileserver16x16Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/fileserver16x16.png"));
//    private ImageIcon tickRedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/tick-red.png"));
//    private ImageIcon fileserverlocal16x16Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/fileserverlocal16x16.png"));
//    private ImageIcon tickYellowIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/tick-yellow.png"));
    private ImageIcon infofileIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/infofile.png"));
//    private ImageIcon transcriptIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/transcript.png"));
//    private ImageIcon lexiconIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/lexicon.png"));    //  loading icons
//    private ImageIcon loading01Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading01.png"));
//    private ImageIcon loading02Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading02.png"));
//    private ImageIcon loading03Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading03.png"));
//    private ImageIcon loading04Icon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/loading04.png"));
    public ImageIcon favouriteIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/favourite.png"));
    public ImageIcon lockedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/lock.png"));
    public ImageIcon unLockedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/unlock.png"));
//    private ImageIcon templateIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/template.png"));
    public ImageIcon vocabularyOpenIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/vocabulary_open.png"));
    public ImageIcon vocabularyOpenListIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/vocabulary_open_list.png"));
    public ImageIcon vocabularyClosedIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/vocabulary_closed.png"));
    public ImageIcon vocabularyClosedListIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/vocabulary_closed_list.png"));
    public ImageIcon attributeIcon = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/attribute.png"));
    public final ImageIcon orderAsc = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/order-asc.png"));
    public final ImageIcon orderDesc = new ImageIcon(ArbilIcons.class.getResource("/nl/mpi/arbil/resources/icons/order-desc.png"));
//
    static private ArbilIcons singleInstance = null;

    static synchronized public ArbilIcons getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilIcons();
	}
	return singleInstance;
    }

    private ArbilIcons() {
    }

    public ImageIcon getIconForNode(ArbilDataNode[] arbilNodeArray) {
	int currentIconXPosition = 0;
	int width = 0;
	int heightMax = 0;
	for (ArbilDataNode currentNode : arbilNodeArray) {
	    width += currentNode.getIcon().getIconWidth();
	    int height = currentNode.getIcon().getIconHeight();
	    if (heightMax < height) {
		heightMax = height;
	    }
	}

	BufferedImage bufferedImage = new BufferedImage(width, heightMax, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics().create();
	for (Object childNode : arbilNodeArray) {
	    ImageIcon currentIcon = ((ArbilDataNode) childNode).getIcon();
	    currentIcon.paintIcon(null, g2d, currentIconXPosition, 0);
	    currentIconXPosition += currentIcon.getIconWidth();
	}
	g2d.dispose();
	return new ImageIcon(bufferedImage);
    }

    private ImageIcon compositIcons(List<Icon> iconArray) {
	int widthTotal = 0;
	int heightMax = 0;
	for (Object currentIcon : iconArray) {
	    int width = ((Icon) currentIcon).getIconWidth();
	    int height = ((Icon) currentIcon).getIconHeight();
	    if (currentIcon != missingRedIcon) {
		widthTotal += width;
	    }
	    if (heightMax < height) {
		heightMax = height;
	    }
	}
	int currentIconXPosition = 0;

	BufferedImage bufferedImage = new BufferedImage(widthTotal, heightMax, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics().create();
	try {
	    for (Object currentIcon : iconArray) {
		int yPos = (heightMax - ((Icon) currentIcon).getIconHeight()) / 2;
		if (currentIcon != missingRedIcon) { // the missing icon always overlays the previous icon
		    ((Icon) currentIcon).paintIcon(null, g2d, currentIconXPosition, yPos);
		    currentIconXPosition += ((Icon) currentIcon).getIconWidth();
		} else {
		    ((Icon) currentIcon).paintIcon(null, g2d, currentIconXPosition - missingRedIcon.getIconWidth(), yPos);
		}
	    }
	    g2d.dispose();
	} finally {
	}
	return new ImageIcon(bufferedImage);
    }

    public Icon getIconForVocabulary(ArbilField cellObject) {
	if (cellObject.hasVocabulary()) {
	    if (((ArbilField) cellObject).isVocabularyOpen()) {
		// Open vocabulary
		if (((ArbilField) cellObject).isVocabularyList()) {
		    // Open list
		    return vocabularyOpenListIcon;
		} else {
		    // Open single
		    return vocabularyOpenIcon;
		}
	    } else {
		// Closed vocabulary
		if (((ArbilField) cellObject).isVocabularyList()) {
		    // Closed list
		    return vocabularyClosedListIcon;
		} else {
		    // Closed single
		    return vocabularyClosedIcon;
		}
	    }
	} else {
	    return null;
	}
    }

    public Icon getIconForField(ArbilField field) {
	List<Icon> iconsList = new LinkedList<Icon>();
	if (field.hasVocabulary()) {
	    iconsList.add(getIconForVocabulary(field));
	}
	if (field.isAllowsLanguageId()) {
	    iconsList.add(languageIcon);
	}
	if (field.hasEditableFieldAttributes()) {
	    iconsList.add(attributeIcon);
	}

	switch (iconsList.size()) {
	    case 0:
		return null;
	    case 1:
		return iconsList.get(0);
	    default:
		return compositIcons(iconsList);
	}
    }

    public ImageIcon getIconForNode(ArbilDataNode arbilNode) {
	List<Icon> iconsList = new LinkedList<Icon>();

	if (arbilNode.isLoading() || (arbilNode.getParentDomNode().isMetaDataNode() && !arbilNode.getParentDomNode().isDataPartiallyLoaded())) {
	    iconsList.add(loadingIcon);
	}
	if (arbilNode.isLocal()) {
	    if (arbilNode.isMetaDataNode()) {
		if (arbilNode.matchesRemote == 0) {
		    if (arbilNode.archiveHandle == null) {
			iconsList.add(localicon);
		    } else {
			iconsList.add(localWithArchiveHandle);
		    }
		} else {
		    iconsList.add(remoteicon);
		}
	    }
	} else {
	    iconsList.add(remoteicon);
	    // don't show the corpuslocalservericon until the serverside is done, otherwise the icon will show only after copying a branch but not after a restart
//                            if (matchesLocal == 0) {
//                            } else {
//                                icon = corpuslocalservericon;
//                            }
	}
	if (arbilNode.resourceFileServerResponse == HttpURLConnection.HTTP_OK) {
	    iconsList.add(unLockedIcon);
	} else if (arbilNode.resourceFileServerResponse == HttpURLConnection.HTTP_MOVED_TEMP) {
	    iconsList.add(lockedIcon);
	}
	String mimeTypeForNode = arbilNode.getAnyMimeType();
	if (mimeTypeForNode != null) {
	    mimeTypeForNode = mimeTypeForNode.toLowerCase();
	    if (mimeTypeForNode.contains("audio")) {
		iconsList.add(audioIcon);
	    } else if (mimeTypeForNode.contains("video")) {
		iconsList.add(videoIcon);
	    } else if (mimeTypeForNode.contains("image")) {// ?????
		iconsList.add(picturesIcon);
	    } else if (mimeTypeForNode.contains("text")) {
		iconsList.add(writtenresourceIcon);
	    } else if (mimeTypeForNode.contains("xml")) {
		iconsList.add(writtenresourceIcon);
	    } else if (mimeTypeForNode.contains("chat")) {
		iconsList.add(writtenresourceIcon);
	    } else if (mimeTypeForNode.contains("pdf")) {
		iconsList.add(writtenresourceIcon);
	    } else if (mimeTypeForNode.contains("kml")) {
		iconsList.add(writtenresourceIcon);
	    } else if (mimeTypeForNode.contains("ogg")) { // application/ogg, video/ and audio/ already get captured above
		iconsList.add(picturesIcon);
	    } else if (mimeTypeForNode.contains("manual/mediafile")) {
		iconsList.add(picturesIcon);
	    } else if (mimeTypeForNode.contains("manual/writtenresource")) {
		iconsList.add(writtenresourceIcon);
	    } else if (mimeTypeForNode.contains("unspecified") || mimeTypeForNode.length() == 0) {
		// no icon for this
		iconsList.add(fileIcon);
	    } else if (mimeTypeForNode.contains("unknown")) {
		iconsList.add(fileCheckedIcon);
	    } else if (mimeTypeForNode.length() > 0) {
		iconsList.add(fileCheckedIcon);
		logger.debug("Icon not found for file type: {}", mimeTypeForNode);
	    }
	} else if (arbilNode.isInfoLink) {
	    iconsList.add(infofileIcon);
	} else if (arbilNode.hasResource()) {
	    // the resource is not found so show a unknow resource icon
	    iconsList.add(fileIcon);
	} else if (arbilNode.isMetaDataNode()) {
	    if (arbilNode.isChildNode()) {
		if (arbilNode.isContainerNode()) {
		    iconsList.add(dataContainerIcon);
		} else if (arbilNode.isEmptyMetaNode()) {
		    iconsList.add(dataemptyIcon);
		} else {
		    iconsList.add(dataIcon);
		}
	    } else if (arbilNode.isSession()) {
		iconsList.add(sessionColorIcon);
	    } else if (arbilNode.isCatalogue()) {
		iconsList.add(catalogueColorIcon);
	    } else if (arbilNode.isCorpus()) {
		iconsList.add(corpusnodeColorIcon);
	    } else if (arbilNode.isCmdiMetaDataNode()) {
		iconsList.add(MetadataFormat.getFormatIcon(arbilNode.getFormatType()));
	    } else {
		// this icon might not be the best one to show in this case
		if (arbilNode.isDataLoaded()) {
		    iconsList.add(fileIcon);
		}
		//iconsVector.add(blankIcon);
	    }
	} else if (arbilNode.isDirectory()) {
	    iconsList.add(UIManager.getIcon("FileView.directoryIcon"));
	} else {
	    if (arbilNode.getTypeCheckerState().equals(TypeCheckerState.CHECKED)) {
		iconsList.add(fileIcon);
	    } else {
		// File has not been type checked, indicate with question mark icon
		iconsList.add(questionBlackIcon);
	    }
	}
	// add missing file icon
	if (arbilNode.fileNotFound) {
	    iconsList.add(missingRedIcon);
	} else if (arbilNode.resourceFileNotFound()) {
	    if (arbilNode.isResourceSet()) {
		// Resource is set but file not found, this is an error
		iconsList.add(missingRedIcon);
	    } else {
		// Resource has not been set, therefore 'not found', this is a different case
		iconsList.add(questionRedIcon);
	    }
	}
	// add a file attached to a session icon
	if (!arbilNode.isMetaDataNode() && arbilNode.matchesInCache + arbilNode.matchesRemote > 0) {
	    if (arbilNode.matchesRemote > 0) {
		iconsList.add(tickGreenIcon);
	    } else {
		iconsList.add(tickBlueIcon);
	    }
	}
	// add icons for favourites
	if (arbilNode.isFavorite()) {
	    iconsList.add(favouriteIcon);
	}
	// add icons for save state
//        if (arbilNode.hasHistory()) {
//            iconsVector.add(exclamationBlueIcon);
//        }
//        if (arbilNode.getNeedsSaveToDisk()) {
//            iconsVector.add(exclamationRedIcon);
//        }
	return compositIcons(iconsList);// TODO: here we could construct a string describing the icon and only create if it does not alread exist in a hashtable
    }
}
