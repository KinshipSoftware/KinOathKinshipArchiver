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
package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilField;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Document : ArbilHyperlinkListener
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilHyperlinkListener implements HyperlinkListener {
    private final static Logger logger = LoggerFactory.getLogger(ArbilHyperlinkListener.class);

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public void hyperlinkUpdate(HyperlinkEvent evt) {
//        logger.debug("hyperlinkUpdate");
	if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	    JEditorPane pane = (JEditorPane) evt.getSource();

//            HTMLDocument doc = (HTMLDocument) pane.getDocument();
//            logger.debug("# of Components in JTextPane: " + pane.getComponentCount());

//            try {
//                logger.debug(evt.getURL());
//                pane.setPage(evt.getURL());
//            } catch (IOException e) {
//                logger.debug(e.getMessage());
//            }
//            logger.debug("getURL: " +evt.getURL());
//            logger.debug("getDescription: " + evt.getDescription());
//            logger.debug(evt.getSourceElement());
//            logger.debug(evt.getEventType());
//            logger.debug(evt.toString());
//            logger.debug(evt.getSource());
	    if (evt.getDescription().startsWith("arbilscript:")) {
		try {
		    ArbilDataNode currentImdiObject = null;
		    String arbilscriptString = evt.getDescription().substring("arbilscript:".length());
		    logger.debug("acting on arbilscript: {}", arbilscriptString);
		    String[] commandsArray = arbilscriptString.split("&");
		    for (String commandString : commandsArray) {
			logger.debug("commandString: {}", commandString);
			if (commandString.startsWith("add=")) {
			    String nodeTypeString = commandString.substring("add=".length());
			    logger.debug("nodeTypeString: {}", nodeTypeString);
			    currentImdiObject = addNode(currentImdiObject, nodeTypeString, "Wizard Corpus", null, null, null);
			}
			if (commandString.startsWith("set=")) {
			    String[] fieldCommand = commandString.substring("set=".length()).split(":");
			    logger.debug("set: {}", fieldCommand[0] + " = " + fieldCommand[1]);
			    setField(currentImdiObject, fieldCommand[0], fieldCommand[1]);
			}
		    }
		    // read the form values
		    // todo: resolve the issue of not being able to get the html name of the components, only the index number is available
		    for (int i = 0; i < pane.getComponentCount(); i++) {
			Container c = (Container) pane.getComponent(i);
			logger.debug("Component count {}", c.getComponentCount());
			Component swingComponentOfHTMLInputType = c.getComponent(0);
			logger.debug(swingComponentOfHTMLInputType.getClass().getName());
			if (swingComponentOfHTMLInputType instanceof JTextField) {
			    JTextField tf = (JTextField) swingComponentOfHTMLInputType;
			    logger.debug(tf.getName());
			    logger.debug(tf.getText());
			    logger.debug("{}", tf.getAction());
			    logger.debug(swingComponentOfHTMLInputType.getName());
			    logger.debug(swingComponentOfHTMLInputType.getName());
			    logger.debug(swingComponentOfHTMLInputType.getName());
			    String formCommandString = swingComponentOfHTMLInputType.getName();
			    logger.debug("formCommandString: {}", formCommandString);
			    if (formCommandString != null && formCommandString.startsWith("arbilscript:set=")) {
				String nodeTypeString = formCommandString.substring("arbilscript:set=".length());
				logger.debug("nodeTypeString: {}", nodeTypeString);
				currentImdiObject = addNode(currentImdiObject, nodeTypeString, tf.getText(), null, null, null);
			    }
			} else if (swingComponentOfHTMLInputType instanceof JButton) {
			}
		    }
		} catch (ArbilMetadataException exception) {
		    dialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
		}

	    } else if (evt.getURL() != null) {
//		try {
//		    // Try to open in ArbilHelp
//		    // TODO: Determine IMDI/CDMI from URL
//		    if (ArbilHelp.getArbilHelpInstance().showHelpItem(evt.getURL())) {
//			return;
//		    }
//		} catch (IOException ioEx) {
//		    BugCatcherManager.getBugCatcher().logError(ioEx);
//		} catch (SAXException saxEx) {
//		    BugCatcherManager.getBugCatcher().logError(saxEx);
//		}
		// Could not be opened in ArbilHelp, tell window manager to show
		windowManager.openUrlWindowOnce(evt.getURL().toString(), evt.getURL());
	    }
	}
    }

    private void setField(ArbilDataNode currentImdiObject, String fieldPath, String FieldValue) {
	for (ArbilField[] currentField : currentImdiObject.getFields().values()) {
	    if (currentField[0].getFullXmlPath().endsWith(fieldPath)) {
		currentField[0].setFieldValue(FieldValue, true, true);
	    }
	}
    }

    // note that this must not be used on nodes currently being edited because it bypasses the imdi loader process
    private ArbilDataNode addNode(ArbilDataNode parentNode, String nodeType, String nodeTypeDisplayName, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
	logger.debug("wizard add node: {}", nodeType);
	logger.debug("adding into: {}", parentNode);
	ArbilDataNode addedImdiObject;
	if (parentNode == null) {
	    URI targetFileURI = sessionStorage.getNewArbilFileName(sessionStorage.getProjectWorkingDirectory(), nodeType);
	    targetFileURI = MetadataReader.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
	    addedImdiObject = dataNodeLoader.getArbilDataNode(null, targetFileURI);
	    treeHelper.addLocation(targetFileURI);
	    treeHelper.applyRootLocations();
	} else {
	    parentNode.saveChangesToCache(true);
	    addedImdiObject = dataNodeLoader.getArbilDataNode(null, new MetadataBuilder().addChildNode(parentNode, nodeType, targetXmlPath, resourceUri, mimeType));
	}
	addedImdiObject.waitTillLoaded();
	windowManager.openFloatingTableOnce(new ArbilDataNode[]{addedImdiObject}, nodeTypeDisplayName);
	return addedImdiObject;
    }
}
