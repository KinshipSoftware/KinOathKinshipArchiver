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
package nl.mpi.arbil.clarin.profiles;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JDialog;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilTreeController;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ProfilePreview
 * Created on : Jun 22, 2010, 2:51:03 PM
 * Author : Peter Withers
 */
public class ProfilePreview {

    private final static Logger logger = LoggerFactory.getLogger(ProfilePreview.class);
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public String schemaToTreeView(String uriString) {
	String returnString = "";
	//ArbilTemplateManager.getSingleInstance().getCmdiTemplate(returnString);
	try {
	    File tempFile = File.createTempFile("ArbilPreview", ".cmdi", sessionStorage.getApplicationSettingsDirectory());
	    tempFile.deleteOnExit();
	    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	    URI addedNodePath = componentBuilder.createComponentFile(tempFile.toURI(), new URI(uriString), true);
	    ArbilDataNode demoNode = dataNodeLoader.getArbilDataNode(null, addedNodePath);
//            ImdiTreeObject demoNode = ImdiLoader.getSingleInstance().getImdiObject(null, new URI("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/sign_language.imdi"));
	    demoNode.waitTillLoaded();
	    //add(new LinkTree("tree", myTreeModel));
	    //DefaultMutableTreeNode demoTreeNode = new DefaultMutableTreeNode(demoNode);
	    DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(demoNode);
	    //rootTreeNode.add(demoTreeNode);
	    DefaultTreeModel demoTreeModel = new DefaultTreeModel(rootTreeNode, true);
	    ArbilWindowManager windowManager = new ArbilWindowManager();
	    MessageDialogHandler dialogHandler = windowManager;
	    TreeHelper treeHelper = new ArbilTreeHelper(sessionStorage, windowManager);
	    ArbilTreeController treeController = new ArbilTreeController(sessionStorage, treeHelper, windowManager, dialogHandler, dataNodeLoader, new ArbilMimeHashQueue(windowManager, sessionStorage), new ApplicationVersionManager(new ArbilVersion()));
	    ArbilTree demoTree = new ArbilTree(treeController, treeHelper, new PreviewSplitPanel(windowManager, null));
	    demoTree.setModel(demoTreeModel);
	    demoTree.requestResort();
	    JDialog demoDialogue = new JDialog();
	    demoDialogue.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	    demoDialogue.pack();
	    demoDialogue.setContentPane(demoTree);
	    demoDialogue.setVisible(true);
	} catch (IOException exception) {
	    logger.debug(exception.getMessage());
	    ///GuiHelper.linorgBugCatcher.logError(exception);
	} catch (URISyntaxException exception) {
	    logger.debug(exception.getMessage());
	    //GuiHelper.linorgBugCatcher.logError(exception);
	}
	return returnString;
    }

    public static void main(String[] args) {
	new ProfilePreview().schemaToTreeView("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438166/xsd");
    }
}
