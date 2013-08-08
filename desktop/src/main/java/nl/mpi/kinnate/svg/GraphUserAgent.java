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
package nl.mpi.kinnate.svg;

import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.kinnate.uniqueidentifiers.UniqueIdentifier;
import org.apache.batik.swing.svg.SVGUserAgentGUIAdapter;

/**
 *  Document   : GraphUserAgent
 *  Created on : Oct 25, 2011, 11:53:00 AM
 *  Author     : Peter Withers
 */
public class GraphUserAgent extends SVGUserAgentGUIAdapter {

    private GraphPanel graphPanel;
    private MessageDialogHandler dialogHandler;
    private ArbilDataNodeLoader dataNodeLoader;

    public GraphUserAgent(GraphPanel parentComponent, MessageDialogHandler dialogHandler, ArbilDataNodeLoader dataNodeLoader) {
        super(parentComponent);
        graphPanel = parentComponent;
        this.dialogHandler = dialogHandler;
        this.dataNodeLoader = dataNodeLoader;
    }

    @Override
    public void displayError(String message) {
        dialogHandler.addMessageDialogToQueue(message, "SVG Error");
    }

    @Override
    public void displayError(Exception exception) {
        BugCatcherManager.getBugCatcher().logError(exception);
    }

    @Override
    public void displayMessage(String message) {
        dialogHandler.addMessageDialogToQueue(message, "SVG Error");
    }

    @Override
    public void showAlert(String message) {
        dialogHandler.addMessageDialogToQueue(message, "SVG Notification");
    }

    @Override
    public void openLink(String targetUri, boolean newc) {
        graphPanel.metadataPanel.removeAllArbilDataNodeRows();
        try {
            // put link target into the table
            final ArbilDataNode arbilDataNode = dataNodeLoader.getArbilDataNode(null, new URI(targetUri));
            graphPanel.metadataPanel.addArbilDataNode(arbilDataNode);
        } catch (URISyntaxException urise) {
            BugCatcherManager.getBugCatcher().logError(urise);
        }
        // set the graph selection
        graphPanel.setSelectedIds(new UniqueIdentifier[]{});
        graphPanel.metadataPanel.updateEditorPane();
        // todo: provide a url for the imdiviewer or to launch arbil
//        try {
//            GuiHelper.getSingleInstance().openFileInExternalApplication(new URI(targetUri));
//        } catch (URISyntaxException exception) {
//            GuiHelper.linorgBugCatcher.logError(exception);
//        }
    }
}
